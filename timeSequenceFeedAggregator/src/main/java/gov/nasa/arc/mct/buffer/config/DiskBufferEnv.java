/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.buffer.config;

import gov.nasa.arc.mct.api.feed.DataProvider.LOS;
import gov.nasa.arc.mct.util.FilepathReplacer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public final class DiskBufferEnv implements DataBufferEnv, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskBufferEnv.class);
    
    private static final String META_DATABASE_PATH = "metaNonCODBuffer";
    private static final String META_DATABASE_NAME = "metaNonCOD";

    private static enum STATE {
        unInitialized, initializing, initialized;
    }

    private Environment dbufferEnv;
    private STATE state = STATE.unInitialized;
    private final Properties prop;
    private volatile long bufferTimeMills;
    private long evictorRecurrMills;
    private File envHome;
    private final int concurrency;
    private final int bufferWriteThreadPoolSize;
    private final int numOfBufferPartitions;
    private final int currentBufferPartition;
    private final long partitionOverlapMillis;
    private final long metaRefreshMillis;
    
    private TransactionConfig txnConfig;
    private CursorConfig cursorConfig;
    private List<EntityStore> openStores = new LinkedList<EntityStore>();
    private DiskQuotaHelper diskQuotaHelper;

    private static Properties loadDefaultPropertyFile() {
        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = ClassLoader.getSystemResourceAsStream("properties/feed.properties");
            prop.load(is);
        } catch (Exception e) {
            LOGGER.error("Cannot initialized DataBufferEnv properties", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    // ignore exception
                }
            }
        }
        return prop;
    }
    
    public String getErrorMsg() {
        return diskQuotaHelper.getErrorMsg();
    }
    
    public DiskBufferEnv(Properties prop) { 
        if (prop == null) {
            prop = loadDefaultPropertyFile();
        }
        this.prop = prop;
        this.currentBufferPartition = 0;
        File bufferHome = new File(FilepathReplacer.substitute(getPropertyWithPrecedence(prop, "buffer.disk.loc.non.cod")));     
        if (!bufferHome.exists()) {
            bufferHome.mkdirs();
        }

        envHome = new File(bufferHome, META_DATABASE_PATH);
        if (!envHome.exists()) {
            envHome.mkdirs();
        }
        
        concurrency = Integer.parseInt(prop.getProperty("buffer.concurrency"));
        evictorRecurrMills = Long.parseLong(prop.getProperty("buffer.evictor.recurrMills"));
        bufferWriteThreadPoolSize = Integer.parseInt(prop.getProperty("buffer.write.threadPool.size"));
        numOfBufferPartitions = Integer.parseInt(prop.getProperty("buffer.partitions"));
        bufferTimeMills = Long.parseLong(prop.getProperty("buffer.time.millis"));
        metaRefreshMillis = Long.parseLong(prop.getProperty("meta.buffer.refresh.millis"));
        if (bufferTimeMills > numOfBufferPartitions) {
            bufferTimeMills = bufferTimeMills / numOfBufferPartitions;
        }
        partitionOverlapMillis = Long.parseLong(prop.getProperty("buffer.partition.overlap.millis"));
        
        diskQuotaHelper = new DiskQuotaHelper(prop, bufferHome);
                    
        this.state = STATE.initializing;

        setup(false);
    }

    public DiskBufferEnv(Properties prop, int currentBufferPartition) {
        if (prop == null) {
            prop = loadDefaultPropertyFile();
        }
        this.prop = prop;
        this.currentBufferPartition = currentBufferPartition;
        File bufferHome = new File(FilepathReplacer.substitute(getPropertyWithPrecedence(prop, "buffer.disk.loc.non.cod")));
        if (!bufferHome.exists()) {
            bufferHome.mkdirs();
        }
        envHome = new File(bufferHome, String.valueOf(currentBufferPartition));
        if (!envHome.exists()) {
            envHome.mkdirs();
        }
        
        concurrency = Integer.parseInt(prop.getProperty("buffer.concurrency"));
        evictorRecurrMills = Long.parseLong(prop.getProperty("buffer.evictor.recurrMills"));
        bufferWriteThreadPoolSize = Integer.parseInt(prop.getProperty("buffer.write.threadPool.size"));
        numOfBufferPartitions = Integer.parseInt(prop.getProperty("buffer.partitions"));
        bufferTimeMills = Long.parseLong(prop.getProperty("buffer.time.millis"));
        bufferTimeMills = bufferTimeMills / numOfBufferPartitions;
        partitionOverlapMillis = Long.parseLong(prop.getProperty("buffer.partition.overlap.millis"));
        metaRefreshMillis = Long.parseLong(prop.getProperty("meta.buffer.refresh.millis"));
        
        diskQuotaHelper = new DiskQuotaHelper(prop, bufferHome);
            
        this.state = STATE.initializing;

        setup(false);
    }
    
    private void setup(boolean readOnly) {
        assertState(STATE.initializing);

        // Instantiate an environment configuration object
        EnvironmentConfig envConfig = new EnvironmentConfig();

        envConfig.setSharedCache(true);
        String cachePercent = prop.getProperty("bdb.cache.percent");
        if (cachePercent != null) {
            envConfig.setCachePercent(Integer.parseInt(cachePercent));
        }
        // Configure the environment for the read-only state as identified by
        // the readOnly parameter on this method call.
        envConfig.setReadOnly(readOnly);
        // If the environment is opened for write, then we want to be able to
        // create the environment if it does not exist.
        envConfig.setAllowCreate(!readOnly); //true
        
        envConfig.setConfigParam(EnvironmentConfig.CHECKPOINTER_BYTES_INTERVAL, "40000000");

        envConfig.setTransactional(false);
        envConfig.setDurability(Durability.COMMIT_NO_SYNC);
        envConfig.setConfigParam(EnvironmentConfig.ENV_RUN_CLEANER, Boolean.FALSE.toString());
        envConfig.setConfigParam(EnvironmentConfig.ENV_IS_LOCKING, Boolean.FALSE.toString());
        
        setupConfig();

        // Instantiate the Environment. This opens it and also possibly
        // creates it.
        try {
            dbufferEnv = new Environment(envHome, envConfig);
            state = STATE.initialized;
        } catch (DatabaseException de) {
          LOGGER.error("DatabaseException in setup", de);
          state = STATE.unInitialized;
        }
    }
    
    private void setupConfig() {
        txnConfig = new TransactionConfig();
        txnConfig.setReadUncommitted(true);
        txnConfig.setDurability(Durability.COMMIT_NO_SYNC);
        
        cursorConfig = new CursorConfig();
        cursorConfig.setReadUncommitted(true);
    }
    
    private String getPropertyWithPrecedence(Properties localProps, String key) {
        String systemProp = System.getProperty(key);
        return systemProp != null ? systemProp.trim() : localProps.getProperty(key, "unset").trim(); 
    }
    
    public EntityStore openMetaDiskStore() throws DatabaseException {
        assertState(STATE.initialized);
        
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setDeferredWrite(true);
        storeConfig.setTransactional(false);

    	// Changes due to timing issues for Apache Felix OSGi
        // Should have better performance.
        ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return new EntityStore(dbufferEnv, META_DATABASE_NAME, storeConfig);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassloader);
        }
    }
    
    public Database openMetaDiskDBStore() throws DatabaseException {
        assertState(STATE.initialized);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setSortedDuplicates(false);
        dbConfig.setTransactional(false);

        Database diskStore = dbufferEnv.openDatabase(null, META_DATABASE_NAME, dbConfig);

        return diskStore;
    }

    public EntityStore openDiskStore(String dbName) throws DatabaseException {
        assertState(STATE.initialized);
        
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setDeferredWrite(true);
        storeConfig.setTransactional(false);

        EntityStore store = new EntityStore(dbufferEnv, dbName, storeConfig);
        openStores.add(store);
        return store;
    }
    
    public Database openDiskDBStore(String dbName, SecondaryKeyCreator... keyCreators) throws DatabaseException {
        assertState(STATE.initialized);
        
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setSortedDuplicates(false);
        dbConfig.setTransactional(false);

        Database diskStore = dbufferEnv.openDatabase(null, dbName, dbConfig);

        int i=0;
        for (SecondaryKeyCreator keyCreator : keyCreators) {
            SecondaryConfig secDbConfig = new SecondaryConfig();
            secDbConfig.setKeyCreator(keyCreator);
            secDbConfig.setAllowCreate(true);
            secDbConfig.setSortedDuplicates(true);
            secDbConfig.setTransactional(false);

            // Perform the actual open
            String secDbName = dbName + i;
            dbufferEnv.openSecondaryDatabase(null, secDbName, diskStore, secDbConfig);
            i++;
        }

        return diskStore;
    }
    
    public boolean isDiskBufferFull() {
        return diskQuotaHelper.isDiskBufferFull();
    }
    
    public Transaction beginTransaction() throws DatabaseException {
        assertState(STATE.initialized);

        TransactionConfig txnConfig = new TransactionConfig();
        txnConfig.setReadUncommitted(true);
        return dbufferEnv.beginTransaction(null, txnConfig);
    }
    
    public SecondaryCursor openSecondaryCursor(Transaction txn, Database database, int index) throws DatabaseException {
        List<SecondaryDatabase> secDbs = database.getSecondaryDatabases();

        assert secDbs.size() == 2;

        SecondaryDatabase secDb = secDbs.get(index);
        
        SecondaryCursor mySecCursor = secDb.openCursor(txn, cursorConfig);

        return mySecCursor;

    }
    
    public void removeAndCloseAllDiskStores() throws DatabaseException {
        for (EntityStore store: openStores) {
            store.close();
        }
        openStores.clear();
        removeEnvironment();
    }

    public void closeDatabase(EntityStore store) throws DatabaseException {
        if (store == null) { return; }
        store.close();
        openStores.remove(store);
    }

    public void closeAndRestartEnvironment() throws DatabaseException {
        boolean isReadOnly = dbufferEnv.getConfig().getReadOnly();
        removeAndCloseAllDiskStores();
        restartEnvironment(isReadOnly);
    }

    public void restartEnvironment(boolean isReadOnly) throws DatabaseException {
        state = STATE.initializing;
        setup(isReadOnly);
    }
    
    public void removeEnvironment() throws DatabaseException {
        dbufferEnv.cleanLog();
        dbufferEnv.close();
        deleteDatabaseFile(currentBufferPartition);
        this.state = STATE.unInitialized;
    }

    public void closeEnvironment() throws DatabaseException {
        dbufferEnv.cleanLog();
        dbufferEnv.close();
        this.state = STATE.unInitialized;
    }

    

    public void closeDatabase(Database database) throws DatabaseException {
        if (database == null) { return; }
        List<SecondaryDatabase> secDbs = database.getSecondaryDatabases();
        for (Database secDb : secDbs) {
            secDb.close();
        }
        database.close();
    }

    
    public int getConcurrencyDegree() {
        return concurrency;
    }
    
    public int getBufferWriteThreadPoolSize() {
        return bufferWriteThreadPoolSize;
    }

    public long getBufferTime() {
        return bufferTimeMills;
    }
    
    public long getEvictorRecurr() {
        return evictorRecurrMills;
    }
    
    public int getNumOfBufferPartitions() {
        return numOfBufferPartitions;
    }

    public void setBufferTime(long bufferTimeMills) {
        this.bufferTimeMills = bufferTimeMills;
    }
    
    public long getBufferPartitionOverlap() {
        return partitionOverlapMillis;
    }
    
    public int getCurrentBufferPartition() {
        return currentBufferPartition;
    }
    
    public DataBufferEnv advanceBufferPartition() {
        int nextBufferPartition = nextBufferPartition();
        deleteDatabaseFile(nextBufferPartition);
        DiskBufferEnv newBufferEnv = new DiskBufferEnv(prop, (this.currentBufferPartition + 1) % numOfBufferPartitions);
        return newBufferEnv;
    }
    
    private void deleteDatabaseFile(int partitionNo) {
        File parentDir = this.envHome.getParentFile();
        File nextBufferPartitionDir = new File(parentDir, String.valueOf(partitionNo));
        if (nextBufferPartitionDir.exists()) {
            if (nextBufferPartitionDir.isDirectory()) {
                File[] files = nextBufferPartitionDir.listFiles();
                for (File f: files) {
                    f.delete();
                }
            }
            nextBufferPartitionDir.delete();
        }
    }
    
    public int nextBufferPartition() {
        return (this.currentBufferPartition+1)%numOfBufferPartitions;
    }
    
    public int previousBufferPartition(int currentPartition) {
        int i = currentPartition;
        if (i == 0) {
            i = this.numOfBufferPartitions-1;
        } else {
            i--;
        }
        return i;
    }
    
    public long getMetaRefresh() {
        return this.metaRefreshMillis;
    }
    
    @Override
    public Object clone() {
        return new DiskBufferEnv(prop, 0);
    }
    
    @Override
    public Object cloneMetaBuffer() {
        return new DiskBufferEnv(prop);
    }

    private void assertState(STATE expectedState) {
        assert this.state == expectedState;
    }
    
    @Override
    public Properties getConfigProperties() {
        return this.prop;
    }
    
    public void flush() {
        this.dbufferEnv.sync();
    }

    @Override
    public LOS getLOS() {
        return LOS.medium;
    }
}
