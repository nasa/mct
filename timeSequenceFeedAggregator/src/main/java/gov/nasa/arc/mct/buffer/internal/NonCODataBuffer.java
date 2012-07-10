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
package gov.nasa.arc.mct.buffer.internal;

import gov.nasa.arc.mct.api.feed.BufferFullException;
import gov.nasa.arc.mct.api.feed.DataArchive;
import gov.nasa.arc.mct.api.feed.DataProvider;
import gov.nasa.arc.mct.buffer.config.DataBufferEnv;
import gov.nasa.arc.mct.buffer.disk.internal.PartitionTimestamps;
import gov.nasa.arc.mct.buffer.util.ElapsedTimer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NonCODataBuffer extends DataBuffer implements DataArchive, DataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonCODataBuffer.class);
    private static final Logger PERF_READ_LOGGER = LoggerFactory
    .getLogger("gov.nasa.arc.mct.performance.read.noncodbuffer");
    private static final Logger PERF_WRITE_LOGGER = LoggerFactory
    .getLogger("gov.nasa.arc.mct.performance.write.noncodbuffer");
    private static final Logger PERF_LOGGER = LoggerFactory
                    .getLogger("gov.nasa.arc.mct.performance.noncodbuffer");

    NonCODataBuffer(DataBufferEnv env, DataBufferHelper partitionBufferFactory) {
        super(env, partitionBufferFactory);
    }
    
    @Override
    protected void setupPartitionBuffers(DataBufferEnv env, DataBufferHelper partitionBufferFactory) {
        for (int i=0; i<partitionDataBuffers.length; i++) {
            this.partitionDataBuffers[i] = new AtomicReference<PartitionDataBuffer>();
        }

        if (env == null) {
            for (int i=0; i<partitionDataBuffers.length; i++) {
                this.partitionDataBuffers[i].set(partitionBufferFactory.newPartitionBuffer(i));
                if (i == metaDataBuffer.getCurrentPartition()) {
                    this.currentParition = this.partitionDataBuffers[i].get();
                } else {
                    this.partitionDataBuffers[i].get().inactive();
                }
            }
        } else {
            this.currentParition = partitionBufferFactory.newPartitionBuffer(env);
            this.partitionDataBuffers[currentParition.getBufferEnv().getCurrentBufferPartition()].set(currentParition);
        }
    }
    
    private final static class FeedRequestContext {
        private final String feedID;
        private final boolean getLastDataIfNeeded;
        
        public FeedRequestContext(String feedID, boolean getLastDataIfNeeded) {
            this.feedID = feedID;
            this.getLastDataIfNeeded = getLastDataIfNeeded;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null || ! (obj instanceof FeedRequestContext)) { return false; }
            return feedID.equals(FeedRequestContext.class.cast(obj).feedID);
        }
        
        @Override
        public int hashCode() {
            return feedID.hashCode();
        }
        
        @Override
        public String toString() {
            return ("feedID: " + feedID + ", lastDataRequired: " + getLastDataIfNeeded);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, FeedRequestContext>[] mapFeedsToPartitions(Set<String> feedIDs, long startTime, long endTime, TimeUnit timeUnit) {
        Map<String, FeedRequestContext>[]  partitionFeeds = new Map[this.currentParition.getBufferEnv().getNumOfBufferPartitions()];
        
        int startPartition = this.currentParition.getBufferEnv().getCurrentBufferPartition();
        int i = startPartition;
        
        do {
            Map<String, FeedRequestContext> feedsForThisPartition = null;
            for (Iterator<String> it = feedIDs.iterator(); it.hasNext(); ) {
                String feedID = it.next();
                if (metaDataBuffer.hasFeed(i, feedID)) {
                    feedsForThisPartition = partitionFeeds[i];
                    if (feedsForThisPartition == null) {
                        feedsForThisPartition = new HashMap<String, FeedRequestContext>();
                        partitionFeeds[i] = feedsForThisPartition;
                    }
                    FeedRequestContext frc = new FeedRequestContext(feedID, false);
                    feedsForThisPartition.put(feedID, frc);
                }
            }
            i = this.currentParition.getBufferEnv().previousBufferPartition(i);
        } while (i != startPartition);
        return partitionFeeds;
    }
    
    @Override
    public Map<String, SortedMap<Long, Map<String, String>>> getData(Set<String> feedIDs, long startTime, long endTime,
            TimeUnit timeUnit) {
        Set<String> requestFeedIDs = new HashSet<String>(feedIDs);
        Map<String, FeedRequestContext>[] partitionFeeds = mapFeedsToPartitions(requestFeedIDs, startTime, endTime, timeUnit);
        
        synchronized (movePartitionLock) {
            if (reset) return Collections.emptyMap();
        }
        
        Map<String, SortedMap<Long, Map<String, String>>> aggregateData = new HashMap<String, SortedMap<Long, Map<String,String>>>();
        
        for (int i=0; i< partitionFeeds.length; i++) {
            Map<String, FeedRequestContext> partitionFeed = partitionFeeds[i];
            if (partitionFeed != null) {
                PartitionDataBuffer partitionBuffer = this.partitionDataBuffers[i].get();
                
                Map<String, SortedMap<Long, Map<String, String>>> data = getData(partitionBuffer, partitionFeed, timeUnit, startTime, endTime);
                for (Entry<String, SortedMap<Long, Map<String, String>>> entry: data.entrySet()) {
                    SortedMap<Long, Map<String, String>> cumulativeData = aggregateData.get(entry.getKey());
                    if (cumulativeData != null) {
                        cumulativeData.putAll(entry.getValue());
                    } else {
                        aggregateData.put(entry.getKey(), entry.getValue());
                    }
                }

            }
        }
        
        return aggregateData;
    }
    
    @Override
    public Map<String, List<Map<String, String>>> getData(Set<String> feedIDs, TimeUnit timeUnit, long startTime,
            long endTime) {
        Set<String> requestFeedIDs = new HashSet<String>(feedIDs);
        Map<String, FeedRequestContext>[] partitionFeeds = mapFeedsToPartitions(requestFeedIDs, startTime, endTime, timeUnit);
        
        synchronized (movePartitionLock) {
            if (reset) return Collections.emptyMap();
        }
        
        Map<String, List<Map<String, String>>> aggregateData = new HashMap<String, List<Map<String,String>>>();
        
        for (int i=0; i<partitionFeeds.length; i++) {
            Map<String, FeedRequestContext> partitionFeed = partitionFeeds[i];
            if (partitionFeed != null) {
                PartitionDataBuffer partitionBuffer = this.partitionDataBuffers[i].get();
                Map<String, SortedMap<Long, Map<String, String>>> data = getData(partitionBuffer, partitionFeed, timeUnit, startTime, endTime);
                for (Entry<String, SortedMap<Long, Map<String, String>>> entry: data.entrySet()) {
                    List<Map<String, String>> cumulativeData = aggregateData.get(entry.getKey());
                    if (cumulativeData != null) {
                        cumulativeData.addAll(0, entry.getValue().values());
                    } else {
                        aggregateData.put(entry.getKey(), new LinkedList<Map<String, String>>(entry.getValue().values()));
                    }
                }

            }
        }
        
        return aggregateData;
    }

    private Map<String, SortedMap<Long, Map<String, String>>> getData(PartitionDataBuffer partitionDataBuffer, Map<String, FeedRequestContext> feedRequestContexts, TimeUnit timeUnit,
                    long startTime, long endTime) {
        synchronized (movePartitionLock) {
            if (reset) return Collections.emptyMap();
            
            while (moveParitionInProgress) {
                try {
                    movePartitionLock.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            readInProgress = true;
        }
        
        try {
            final ElapsedTimer timer = new ElapsedTimer();
            timer.startInterval();

            Map<String, SortedMap<Long, Map<String, String>>> returnedData = partitionDataBuffer.getData(feedRequestContexts.keySet(), timeUnit, startTime, endTime);
            PERF_READ_LOGGER.debug("Get Regular Data feeds: {}  from partition: {}", returnedData, partitionDataBuffer.getBufferEnv().getCurrentBufferPartition());

            for (Iterator<Entry<String, FeedRequestContext>> it = feedRequestContexts.entrySet().iterator(); it.hasNext(); ) {
                Entry<String, FeedRequestContext> entry = it.next();
                String feedID = entry.getKey();
                SortedMap<Long, Map<String, String>> data = returnedData.get(feedID);
                boolean needPrevPoint = true;
                if (data != null && !data.isEmpty()) {
                    long firstPointTS = data.firstKey();
                    needPrevPoint = firstPointTS > TimeUnit.NANOSECONDS.convert(startTime, timeUnit);
                }
                if (!entry.getValue().getLastDataIfNeeded || !needPrevPoint) {
                    it.remove();
                }
            }
            if (!feedRequestContexts.isEmpty()) {
                Set<String> feedIDs = feedRequestContexts.keySet();
                Map<String, SortedMap<Long, Map<String, String>>> lastData = partitionDataBuffer.getLastData(feedIDs,
                        timeUnit, 0, startTime);
                for (Entry<String, SortedMap<Long, Map<String, String>>> entry : lastData.entrySet()) {
                    String feedID = entry.getKey();
                    SortedMap<Long, Map<String, String>> data = entry.getValue();
                    if (data != null && !data.isEmpty()) {
                        SortedMap<Long, Map<String, String>> feedData = returnedData.get(feedID);
                        if (feedData == null) {
                            feedData = new TreeMap<Long, Map<String, String>>();
                            returnedData.put(feedID, feedData);
                        }
                        Long ts = data.firstKey();
                        feedData.put(ts, data.get(ts));
                    }
                }
                
                PERF_READ_LOGGER.debug("Get Last Data feeds: {} from partition: {} ", returnedData, partitionDataBuffer.getBufferEnv().getCurrentBufferPartition());
            }

            timer.stopInterval();
            LOGGER.debug("time to get Data for feeds {}: {}", feedRequestContexts, timer.getIntervalInMillis());
            return returnedData;
        } finally {
            synchronized (movePartitionLock) {
                readInProgress = false;
                movePartitionLock.notifyAll();
            }
        }
    }

    @Override
    public void putData(String feedID, TimeUnit timeUnit, Map<Long, Map<String, String>> entries) throws BufferFullException {
        synchronized (movePartitionLock) {
            if (reset) return;
        }
        
        final ElapsedTimer timer = new ElapsedTimer();
        timer.startInterval();

        Map<String, Map<Long, Map<String, String>>> feedDataToPut = new HashMap<String, Map<Long,Map<String,String>>>();
        feedDataToPut.put(feedID, entries);
        
        int i = this.currentParition.getBufferEnv().getCurrentBufferPartition();
        int startPartition = i;
        do {
            PartitionDataBuffer partitionBuffer = this.partitionDataBuffers[i].get();
            if (partitionBuffer == null || !partitionBuffer.isActive()) {
                break;
            }
            
            LOGGER.debug("Putting in partition {}", i);

            Map<String, PartitionTimestamps> timeStamps = putData(partitionBuffer, feedDataToPut, timeUnit);
            if (timeStamps != null) {
                metaDataBuffer.updatePartitionMetaData(partitionBuffer.getBufferEnv().getCurrentBufferPartition(), timeStamps);
            }
            i = (i + 1) % this.currentParition.getBufferEnv().getNumOfBufferPartitions();
        } while (i != startPartition);

        timer.stopInterval();
        PERF_LOGGER.debug("Time to save data for feed {}: {}", feedID, timer.getIntervalInMillis());

    }
    
    private void putData(PartitionDataBuffer partitionBuffer, Map<String, Map<Long, Map<String, String>>> value, TimeUnit timeUnit, MetaDataBuffer metadata, int metadataIndex)  throws BufferFullException {
        synchronized (movePartitionLock) {
            if (reset) return;
            while (moveParitionInProgress) {
                try {
                    movePartitionLock.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            writeInProgress = true;
        }
        if (this.partitionDataBuffers[partitionBuffer.getBufferEnv().getCurrentBufferPartition()].get() == null) {
            return;
        }

        try {
            partitionBuffer.putData(value, timeUnit, metadata, metadataIndex);
        } finally {
            synchronized (movePartitionLock) {
                writeInProgress = false;
                movePartitionLock.notifyAll();
            }
        }
    
    }
    
    
    private Map<String, PartitionTimestamps> putData(PartitionDataBuffer partitionBuffer, Map<String, Map<Long, Map<String, String>>> value, TimeUnit timeUnit)  throws BufferFullException {
        synchronized (movePartitionLock) {
            if (reset) return null;
            while (moveParitionInProgress) {
                try {
                    movePartitionLock.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            writeInProgress = true;
        }
        if (this.partitionDataBuffers[partitionBuffer.getBufferEnv().getCurrentBufferPartition()].get() == null) {
            return null;
        }

        try {
            return partitionBuffer.putData(value, timeUnit);
        } finally {
            synchronized (movePartitionLock) {
                writeInProgress = false;
                movePartitionLock.notifyAll();
            }
        }
    }
    
    @Override
    public void putData(Map<String, Map<Long, Map<String, String>>> value, TimeUnit timeUnit,
                    Runnable callback)  throws BufferFullException {
        PERF_WRITE_LOGGER.debug("NonCOD Putting data for {} feeds", value);
        synchronized (movePartitionLock) {
            if (reset) return;
        }
        
        final ElapsedTimer timer = new ElapsedTimer();
        timer.startInterval();

        int i = this.currentParition.getBufferEnv().getCurrentBufferPartition();
        int startPartition = i;
        do {
            PartitionDataBuffer partitionBuffer = this.partitionDataBuffers[i].get();
            if (partitionBuffer == null || !partitionBuffer.isActive()) {
                break;
            }
            
            LOGGER.debug("Putting in partition {}", i);

            putData(partitionBuffer, value, timeUnit, metaDataBuffer, i);

            timer.stopInterval();
            PERF_LOGGER.debug("Time to save data for {} feeds: {}", value.size(), timer
                            .getIntervalInMillis());
            i = (i + 1) % currentParition.getBufferEnv().getNumOfBufferPartitions();
        } while (i != startPartition);

        if (callback != null) {
            callback.run();
        }
    }


    @Override
    public void putData(String feedID, TimeUnit timeUnit, long time, Map<String, String> value) throws BufferFullException {
        synchronized (movePartitionLock) {
            if (reset) return;
        }
        
        final ElapsedTimer timer = new ElapsedTimer();
        timer.startInterval();

        Map<Long, Map<String, String>> dataToPut = new HashMap<Long, Map<String, String>>();
        dataToPut.put(Long.valueOf(time), value);
        
        Map<String, Map<Long, Map<String, String>>> feedDataToPut = new HashMap<String, Map<Long,Map<String,String>>>();
        feedDataToPut.put(feedID, dataToPut);

        int i = this.currentParition.getBufferEnv().getCurrentBufferPartition();
        int startPartition = i;
        do {
            PartitionDataBuffer partitionBuffer = this.partitionDataBuffers[i].get();
            if (partitionBuffer == null || !partitionBuffer.isActive()) {
                break;
            }
            
            LOGGER.debug("Putting in partition {}", i);

            Map<String, PartitionTimestamps> timeStamps = putData(partitionBuffer, feedDataToPut, timeUnit);
            if (timeStamps != null) {
                metaDataBuffer.updatePartitionMetaData(partitionBuffer.getBufferEnv().getCurrentBufferPartition(), timeStamps);
            }
            i = (i + 1) % this.currentParition.getBufferEnv().getNumOfBufferPartitions();
        } while (i != startPartition);
        
        timer.stopInterval();
        PERF_LOGGER.debug("Time to save data for feed {}: {}", feedID, timer.getIntervalInMillis());
    }
    

}
