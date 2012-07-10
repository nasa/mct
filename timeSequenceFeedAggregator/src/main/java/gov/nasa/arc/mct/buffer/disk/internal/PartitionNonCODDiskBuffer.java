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
package gov.nasa.arc.mct.buffer.disk.internal;

import gov.nasa.arc.mct.api.feed.BufferFullException;
import gov.nasa.arc.mct.buffer.config.DataBufferEnv;
import gov.nasa.arc.mct.buffer.config.DiskBufferEnv;
import gov.nasa.arc.mct.buffer.internal.MetaDataBuffer;
import gov.nasa.arc.mct.buffer.internal.PartitionDataBuffer;
import gov.nasa.arc.mct.buffer.util.ElapsedTimer;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;

public class PartitionNonCODDiskBuffer implements PartitionDataBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionNonCODDiskBuffer.class);
    private static final Logger READ_PERF_LOGGER = LoggerFactory
            .getLogger("gov.nasa.arc.mct.performance.nonCODDisk.partitionbuffer.read");
    private static final Logger WRITE_PERF_LOGGER = LoggerFactory
            .getLogger("gov.nasa.arc.mct.performance.nonCODDisk.partitionbuffer.write");

    private static final class TimeStampComparator implements Comparator<Long>, Serializable {
        private static final long serialVersionUID = -665810351953536404L;

        @Override
        public int compare(Long o1, Long o2) {
            return o1.compareTo(o2);
        }
    }

    private static final Comparator<Long> TIMESTAMP_COMPARATOR = new TimeStampComparator();
    
    private final EntityStore[] databases;
    private final DiskBufferEnv env;

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash
     * code are used to choose the segment.
     */
    private final int segmentMask;

    /**
     * Shift value for indexing within segments.
     */
    private final int segmentShift;
    
    private static final ThreadFactory tf = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setContextClassLoader(getClass().getClassLoader());
            return t;
        } 
    };
    
    private static final ExecutorService writeThreads = new ThreadPoolExecutor(4, 4,
            10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            tf);
    private static final ExecutorService readThreads = new ThreadPoolExecutor(0, 10,
            10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            tf);

    private volatile boolean active;

    public PartitionNonCODDiskBuffer(int partitionNumber) {
        this(new DiskBufferEnv(null, partitionNumber));
    }

    public PartitionNonCODDiskBuffer(DiskBufferEnv env) {
        this.env = env;
        int concurrencyLevel = env.getConcurrencyDegree();

        // Determine the degree of concurrency which is a power of 2 and closest
        // to what the user has indicated. For instance, if user specifies a
        // degree of concurrency of 5, the degree of concurrency we will be
        // using will be 8. This will allow a fairer hashing. This algorithm is
        // copied from java.util.concurrent.ConcurrentHashMap.
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        this.segmentMask = ssize - 1;

        this.databases = new EntityStore[ssize];
        setupDatabasePartition(env);

        this.active = true;
    }

    private synchronized void setupDatabasePartition(DiskBufferEnv env) {
        for (int i = 0; i < databases.length; i++) {
            try {
                this.databases[i] = env.openDiskStore(String.valueOf(i));
            } catch (DatabaseException e) {
                databases[i] = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String>[] groupInputFeeds(Set<String> feedIDs) {
        Set<String>[] groupFeeds = new Set[databases.length];
        for (int i = 0; i < groupFeeds.length; i++) {
            groupFeeds[i] = new HashSet<String>();
        }

        for (String feedID : feedIDs) {
            int segmentIndex = hash(feedID.hashCode());
            groupFeeds[segmentIndex].add(feedID);
        }
        return groupFeeds;
    }

    
    @SuppressWarnings("unchecked")
    public Map<String, SortedMap<Long, Map<String, String>>> getData(Set<String> feedIDs, final TimeUnit timeUnit,
            final long startTime, final long endTime) {
        final ElapsedTimer timer = new ElapsedTimer();
        timer.startInterval();

        final Set<String>[] groupFeeds = groupInputFeeds(feedIDs);
        final Map<String, SortedMap<Long, Map<String, String>>>[] dataSlices = new Map[groupFeeds.length];
        final CountDownLatch readLatch = new CountDownLatch(groupFeeds.length);
        for (int i = 0; i < groupFeeds.length; i++) {
            final int dataIndex = i;

            Runnable r = new Runnable() {

                @Override
                public void run() {
                    try {
                        Map<String, SortedMap<Long, Map<String, String>>> dataSlice = getData(databases[dataIndex],
                                groupFeeds[dataIndex], timeUnit, startTime, endTime);
                        if (dataSlice != null) {
                            dataSlices[dataIndex] = dataSlice;
                        }
                    } finally {
                        readLatch.countDown();
                    } 
                }
            };
            
            readThreads.execute(r);
        }
        
        try {
            readLatch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("Internal error during getData thread", e);
        } 

        Map<String, SortedMap<Long, Map<String, String>>> returnedData = new HashMap<String, SortedMap<Long, Map<String, String>>>();
        for (int i = 0; i < dataSlices.length; i++) {
            Map<String, SortedMap<Long, Map<String, String>>> dataSlice = dataSlices[i];
            if (dataSlice != null) {
                returnedData.putAll(dataSlice);
            }
        }
        timer.stopInterval();
        READ_PERF_LOGGER.debug("time to get 1 partition Data for {} feeds: {}", feedIDs.size(), timer
                .getIntervalInMillis());

        return returnedData;

    }
    
    @Override
    public Map<String, SortedMap<Long, Map<String, String>>> getLastData(Set<String> feedIDs, final TimeUnit timeUnit, final long startTime, final long endTime) {
                
        Map<String, SortedMap<Long, Map<String, String>>> returnedData = new HashMap<String, SortedMap<Long, Map<String, String>>>();
        return returnedData;
    }
    
    private Map<String, SortedMap<Long, Map<String, String>>> getData(EntityStore db, Set<String> feedIDs,
            TimeUnit timeUnit, long startTime, long endTime) {
        Map<String, SortedMap<Long, Map<String, String>>> returnedData = new HashMap<String, SortedMap<Long, Map<String, String>>>();

        startTime = TimeUnit.NANOSECONDS.convert(startTime, timeUnit);
        endTime = TimeUnit.NANOSECONDS.convert(endTime, timeUnit);

        PersistentBufferObjectAccess pObjectAccess = new PersistentBufferObjectAccess(db);

        PersistentBufferKey startKey = new PersistentBufferKey();
        PersistentBufferKey endKey = new PersistentBufferKey();
        for (String feedID : feedIDs) {
            startKey.feedID = feedID;
            startKey.timestamp = startTime;
            endKey.feedID = feedID;
            endKey.timestamp = endTime;

            EntityCursor<PersistentBufferObject> piCursor = pObjectAccess.pIdx.entities(startKey, true, endKey, true);
            try {
                for (PersistentBufferObject pObj : piCursor) {
                    SortedMap<Long, Map<String, String>> data = returnedData.get(feedID);
                    if (data == null) {
                        data = new TreeMap<Long, Map<String, String>>(TIMESTAMP_COMPARATOR);
                        returnedData.put(feedID, data);
                    }
                    data.put(pObj.getKey().timestamp, pObj.getData());
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
            } finally {
                piCursor.close();
            }
        }
        return returnedData;

    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<Long, Map<String, String>>>[] groupInputDataByFeed(
            Map<String, Map<Long, Map<String, String>>> value) {
        Map[] groupInputData = new Map[databases.length];
        for (int i = 0; i < groupInputData.length; i++) {
            groupInputData[i] = new HashMap<String, Map<Long, Map<String, String>>>();
        }
        for (Entry<String, Map<Long, Map<String, String>>> entry : value.entrySet()) {
            int segmentIndex = hash(entry.getKey().hashCode());
            groupInputData[segmentIndex].put(entry.getKey(), entry.getValue());
        }
        return (Map<String, Map<Long, Map<String, String>>>[]) groupInputData;
    }

    public Map<String, PartitionTimestamps> putData(Map<String, Map<Long, Map<String, String>>> value, final TimeUnit timeUnit) throws BufferFullException {
        final ElapsedTimer timer = new ElapsedTimer();
        timer.startInterval();
        final Map<String, Map<Long, Map<String, String>>>[] groupData = groupInputDataByFeed(value);
        final Map<String, PartitionTimestamps> timestamps = new HashMap<String, PartitionTimestamps>();

        final AtomicBoolean bufferFull = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(groupData.length);
        for (int i = 0; i < groupData.length; i++) {
            final int dataIndex = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Entry<String, Map<Long, Map<String, String>>> feedData : groupData[dataIndex].entrySet()) {
                            PartitionTimestamps timeStamp = null;
                            try {
                                timeStamp = putData(null, feedData.getKey(), databases[dataIndex], timeUnit, feedData.getValue());
                            } catch (BufferFullException e) {
                                bufferFull.compareAndSet(false, true);
                            }
                            if (timeStamp == null) {
                                break;
                            } else {
                                timestamps.put(feedData.getKey(), timeStamp);
                                LOGGER.debug("feedData.getKey(): " + feedData.getKey() + ", timeStamp: " + timeStamp);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            };

            writeThreads.execute(r);
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("Internal error during putData thread", e);
        } 
        if (bufferFull.get()) {
            throw new BufferFullException(env.getErrorMsg());
        }
        timer.stopInterval();

        WRITE_PERF_LOGGER.debug("Time to write {} feeds: {}", value.size(), timer.getIntervalInMillis());
        return timestamps;
    }

    @Override
    public void putData(Map<String, Map<Long, Map<String, String>>> value, final TimeUnit timeUnit, final MetaDataBuffer metadata, final int metadataIndex) throws BufferFullException {
        final ElapsedTimer timer = new ElapsedTimer();
        timer.startInterval();
        final Map<String, Map<Long, Map<String, String>>>[] groupData = groupInputDataByFeed(value);

        final AtomicBoolean bufferFull = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(groupData.length);
        for (int i = 0; i < groupData.length; i++) {
            final int dataIndex = i;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Entry<String, Map<Long, Map<String, String>>> feedData : groupData[dataIndex].entrySet()) {
                            PartitionTimestamps timeStamp = null;
                            try {
                                timeStamp = putData(null, feedData.getKey(), databases[dataIndex], timeUnit, feedData.getValue());
                            } catch (BufferFullException e) {
                                bufferFull.compareAndSet(false, true);
                            }
                            if (timeStamp == null) {
                                break;
                            } else {
                                metadata.updatePartitionMetaData(metadataIndex, feedData.getKey(), timeStamp.getStartTimestamp(), timeStamp.getEndTimestamp());
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            };

            writeThreads.execute(r);
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.warn("Internal error during putData thread", e);
        } 
        if (bufferFull.get()) {
            throw new BufferFullException(env.getErrorMsg());
        }
        timer.stopInterval();

        WRITE_PERF_LOGGER.debug("Time to write {} feeds: {}", value.size(), timer.getIntervalInMillis());
    }
    

    private PartitionTimestamps putData(Transaction txn, String feedID, EntityStore db, TimeUnit timeUnit,
            Map<Long, Map<String, String>> entries) throws BufferFullException {
        long largestTime = 0;
        long smallestTime = 0;

        try {
            PersistentBufferObjectAccess pObjAccess = new PersistentBufferObjectAccess(db);
            for (Long time : entries.keySet()) {
                try {
                    Map<String, String> value = entries.get(time);

                    time = TimeUnit.NANOSECONDS.convert(time, timeUnit);
                    LOGGER.debug("Putting data for feed {} with time {}", feedID, time);
                    if (time.longValue() > largestTime) {
                        largestTime = time.longValue();
                    }
                    if (smallestTime == 0) {
                        smallestTime = time.longValue();
                    } else if (time.longValue() < smallestTime) {
                        smallestTime = time.longValue();
                    }

                    PersistentBufferObject pObj = new PersistentBufferObject();
                    pObj.setKey(new PersistentBufferKey(feedID, time.longValue()));
                    pObj.setData(value);

                    pObjAccess.pIdx.putNoReturn(pObj);
                } catch (DatabaseException de) {
                    largestTime = -1;
                    LOGGER.error("Putting data for feed {} failed", feedID, de);
                    
                    if (env.isDiskBufferFull()) {
                        LOGGER.error("[PartitionFastDiskBuffer]: " + env.getErrorMsg());
                        throw new BufferFullException();
                    }
                    
                    break;
                }
            }
        } catch (DatabaseException de) {
            largestTime = -1;
            LOGGER.error("Putting data for feed {} failed", feedID, de);
            
            if (env.isDiskBufferFull()) {
                 LOGGER.error("[PartitionFastDiskBuffer]: " + env.getErrorMsg());
                 throw new BufferFullException();
            
            }
        }

        return new PartitionTimestamps(smallestTime, largestTime);

    }

    private int hash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        int i = h ^ (h >>> 16);

        return ((i >>> segmentShift) & segmentMask);
    }

    public synchronized void removeBuffer() {
        for (int i = 0; i < databases.length; i++) {
            try {
                if (databases[i] != null) {
                    env.closeDatabase(databases[i]);
                    databases[i] = null;
                }
            } catch (DatabaseException de) {
                LOGGER.debug("DatabaseException in closeBuffer", de);
            }
        }
        env.removeEnvironment();
    }
    
    public synchronized void closeBuffer() {
        this.env.flush();
        for (int i = 0; i < databases.length; i++) {
            try {
                if (databases[i] != null) {
                    env.closeDatabase(databases[i]);
                    databases[i] = null;
                }
            } catch (DatabaseException de) {
                LOGGER.debug("DatabaseException in closeBuffer", de);
            }
        }
        env.closeEnvironment();
    }
    
    public synchronized boolean isClosed() {
        for (int i = 0; i < databases.length; i++) {
            if (databases[i] != null) {
                return false;
            }
        }
        return true;
    }

    private void closeDatabases() {
        for (int i = 0; i < databases.length; i++) {
            try {
                if (databases[i] != null) {
                    env.closeDatabase(databases[i]);
                    databases[i] = null;
                }
            } catch (DatabaseException de) {
                LOGGER.debug("DatabaseException in closeBuffer", de);
            }
        }
    }

    public synchronized void resetBuffer() {
        closeDatabases();
        env.closeAndRestartEnvironment();
        setupDatabasePartition(env);
    }

    public void inactive() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public DataBufferEnv getBufferEnv() {
        return env;
    }
}
