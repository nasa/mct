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

import gov.nasa.arc.mct.buffer.config.FastDiskBufferEnv;
import gov.nasa.arc.mct.buffer.config.DiskBufferEnv;
import gov.nasa.arc.mct.buffer.internal.MetaDataBuffer;
import gov.nasa.arc.mct.buffer.internal.PartitionMetaData;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class MetaDiskBuffer extends MetaDataBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDiskBuffer.class);

    private FastDiskBufferEnv metaEnv;
    private DiskBufferEnv nonCODMetaEnv;
    private EntityStore metaDatabase;
    private final Timer updateTimer;

    public MetaDiskBuffer(DiskBufferEnv env) {
        super(env);
        nonCODMetaEnv = env;
        metaDatabase = nonCODMetaEnv.openMetaDiskStore();
               
        loadAllPartitionsInformation();
         
        long metaRefreshTime = nonCODMetaEnv.getMetaRefresh();
        if (metaRefreshTime == -1) {
            updateTimer = null;
        } else {
            updateTimer = new Timer("Meta Non-COD Data Buffer Update timer");
            updateTimer.schedule(new TimerTask() {
        
                @Override
                public void run() {
                    for (int i = 0; i < partitionMetaDatas.length; i++) {
                        if (partitionMetaDatas[i] != null) {
                            writePartitionMetaData(i);
                        }
                    }
                }
            }, metaRefreshTime, metaRefreshTime);
        }
    }
    
    public MetaDiskBuffer(FastDiskBufferEnv env) {
        super(env);
        metaEnv = env;
        metaDatabase = metaEnv.openMetaDiskStore();
        
        loadAllPartitionsInformation();

        long metaRefreshTime = metaEnv.getMetaRefresh();
        if (metaRefreshTime == -1) {
            updateTimer = null;
        } else {
            updateTimer = new Timer("Meta Data Buffer Update timer");
            updateTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    for (int i = 0; i < partitionMetaDatas.length; i++) {
                        if (partitionMetaDatas[i] != null) {
                            writePartitionMetaData(i);
                        }
                    }

                }
            }, metaRefreshTime, metaRefreshTime);
        }
    }
    
    private PrimaryIndex<Integer, PartitionMetaData> getMetaStoreIndex() {
        ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return metaDatabase.getPrimaryIndex(Integer.class, PartitionMetaData.class);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassloader);
        }
    }

    private void loadAllPartitionsInformation() {
        try {
            PrimaryIndex<Integer, PartitionMetaData> pi = getMetaStoreIndex();
            if (pi.count() == 0) {
                writeCurrentBufferPartition(0);
                return;
            }
            EntityCursor<PartitionMetaData> piCursor = pi.entities();
            try {
                for (PartitionMetaData pObj : piCursor) {
                    partitionMetaDatas[pObj.getPartitionId()] = pObj;
                    if (pObj.isCurrentPartition()) {
                        this.currentPartition = pObj.getPartitionId();
                    }
                }
            } finally {
                if (piCursor != null) {
                    piCursor.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in loadAllPartitionInformation", e);
        }
    }

    public PartitionMetaData removePartitionMetaData(int bufferPartition) {
        PartitionMetaData pObj = super.removePartitionMetaData(bufferPartition);
        if (pObj == null) { return null; }

        try {
            getMetaStoreIndex().delete(pObj.getPartitionId());
        } catch (Exception e) {
            LOGGER.error("Exception in getData", e);
        } finally {
            if (metaEnv != null) {
                metaEnv.flush();
            }
            
            if (nonCODMetaEnv != null) {
                nonCODMetaEnv.flush();
            }
            
            LOGGER.info("Removing partition {} timestamp", bufferPartition);
        }
        return pObj;
    }
    
    public Set<String> resetPartitionMetaData(int bufferPartition) {
        Set<String> rowoverFeedIDs = super.resetPartitionMetaData(bufferPartition);
        PartitionMetaData pObj = getPartitionMetaData(bufferPartition);
        if (pObj != null) {
            try {
                getMetaStoreIndex().putNoReturn(pObj);
            } catch (Exception e) {
                LOGGER.error("Exception in getData", e);
            } finally {
                if (metaEnv != null) {
                    metaEnv.flush();
                }
                
                if (nonCODMetaEnv != null) {
                    nonCODMetaEnv.flush();
                }
                LOGGER.info("Removing partition {} timestamp", bufferPartition);
            }
        }
        return rowoverFeedIDs;
    }

    @Override
    public void writePartitionMetaData(int bufferPartition) {
        PartitionMetaData pObj = getPartitionMetaData(bufferPartition);
        if (pObj == null) {
            return;
        }
        
        try {
            getMetaStoreIndex().putNoReturn(pObj);
        } catch (Exception e) {
            LOGGER.error("Exception in getData", e);
        } finally {
            if (metaEnv != null) {
                metaEnv.flush();
            }
            
            if (nonCODMetaEnv != null) {
                nonCODMetaEnv.flush();
            }
            LOGGER.debug("Putting start time and end time of partition {}", bufferPartition);
        }
    }

    @Override
    public void writeCurrentBufferPartition(int newCurrentBufferPartition) {
        PartitionMetaData existingPartitionMetaData = getPartitionMetaData(this.currentPartition);
        PartitionMetaData newPartitionMetaData = getPartitionMetaData(newCurrentBufferPartition);
        if (existingPartitionMetaData != null) {
            existingPartitionMetaData.setCurrentPartition(false);
        }
        
        if (newPartitionMetaData == null) {
            newPartitionMetaData = new PartitionMetaData(newCurrentBufferPartition);
            synchronized(this) {
                this.partitionMetaDatas[newCurrentBufferPartition] = newPartitionMetaData;
            }
        }
        newPartitionMetaData.setCurrentPartition(true);
        
        boolean failed = false;
        try {
            if (existingPartitionMetaData != null) {
                getMetaStoreIndex().putNoReturn(existingPartitionMetaData);
            }
            getMetaStoreIndex().putNoReturn(newPartitionMetaData);
        } catch (Exception e) {
            LOGGER.error("Exception in getData", e);
            failed = true;
        } finally {
            if (!failed) {
                if (metaEnv != null) {
                    metaEnv.flush();
                }
                
                if (nonCODMetaEnv != null) {
                    nonCODMetaEnv.flush();
                }
                this.currentPartition = newCurrentBufferPartition;
                LOGGER.info("moving to partition {}", newCurrentBufferPartition);
            }
        }
    }

    public void close() {
        if (metaEnv != null) {
            metaEnv.closeDatabase(metaDatabase);
        }
               
        if (nonCODMetaEnv != null) {
            nonCODMetaEnv.closeDatabase(metaDatabase);
        }
        super.close();
    }

    public void closeDatabase() {
        if (metaEnv != null) {
            metaEnv.closeDatabase(metaDatabase);
        }
               
        if (nonCODMetaEnv != null) {
            nonCODMetaEnv.closeDatabase(metaDatabase);
        }
        super.closeDatabase();
    }
    
    public void restart() {
        int numOfBufferPartitions = 0;
        
        if (metaEnv != null) {
            numOfBufferPartitions = metaEnv.getNumOfBufferPartitions();
        }
             
        if (nonCODMetaEnv != null) {
            numOfBufferPartitions = nonCODMetaEnv.getNumOfBufferPartitions();
        }

        for (int i=0; i<numOfBufferPartitions; i++) {
            removePartitionMetaData(i);
        }
        super.restart();
        writeCurrentBufferPartition(0);
    }
}
