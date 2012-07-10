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
package gov.nasa.arc.mct.feed;

import gov.nasa.arc.mct.api.feed.BufferFullException;
import gov.nasa.arc.mct.api.feed.DataArchive;
import gov.nasa.arc.mct.api.feed.DataProvider;
import gov.nasa.arc.mct.api.feed.DataProvider.LOS;
import gov.nasa.arc.mct.api.feed.FeedAggregator;
import gov.nasa.arc.mct.api.feed.FeedDataArchive;
import gov.nasa.arc.mct.buffer.internal.DataBuffer;
import gov.nasa.arc.mct.buffer.internal.DataBufferFactory;
import gov.nasa.arc.mct.buffer.util.ElapsedTimer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FeedAggregatorService implements FeedDataArchive, FeedAggregator {
    private static Logger LOGGER = LoggerFactory.getLogger(FeedAggregatorService.class.getName());
    private static final Logger PERF_LOGGER = LoggerFactory.getLogger("gov.nasa.arc.mct.performance.feedAggregator");
    private static final Logger READ_PERF_LOGGER = LoggerFactory.getLogger("gov.nasa.arc.mct.performance.read.feedAggregator");
    
    private static final String DIALOG_WINDOW_TITLE = "MCT Buffer Available Space Warning";
    
    private static final String BUFFER_LIST_PROPERTY = "bufferList";
    
    private final Vector<DataProvider> dataProviders; // use a synchronized data
                                                      // structure due to the
                                                      // possibility of
                                                      // accessing from multiple
                                                      // threads.
    private final Vector<DataArchive> dataArchives;
    private final ThreadPoolExecutor bufferWorkers;
    private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    private DataArchive dormantArchive;
    private RunMode currentRunMode;
    private Properties configProp;
    private final AtomicInteger numOfWriteJobs = new AtomicInteger(0);
    
    private static enum RunMode {
        embedded, server;
    }
    
    private static enum DataBufferType {
        memory() {
            @Override
            public DataBuffer getDataBuffer(Properties props) {
                return DataBufferFactory.getMemoryDataBuffer(props);
            }
        },
        
        noncoddisk() {
            @Override
            public DataBuffer getDataBuffer(Properties props) {
                return DataBufferFactory.getNonCODDiskDataBuffer(props);
            }
        },

        fastdisk() {
            @Override
            public DataBuffer getDataBuffer(Properties props) {
                return DataBufferFactory.getFastDiskDataBuffer(props);
            }
        };
        
        abstract DataBuffer getDataBuffer(Properties props);
    }

  
    public FeedAggregatorService() {
        this(loadConfig());
    }


    public FeedAggregatorService(Properties props) {
        this.configProp = props;
        this.dataProviders = new Vector<DataProvider>();
        this.dataArchives = new Vector<DataArchive>();
        List<DataBuffer> dataBuffers = getDataBuffers();
        this.dataArchives.addAll(dataBuffers);

        this.bufferWorkers = new ThreadPoolExecutor(1, 1, 1l, TimeUnit.SECONDS,
                queue);
        this.dataProviders.addAll(dataBuffers);
        String runMode = configProp.getProperty("runMode");
        this.currentRunMode = Enum.valueOf(RunMode.class, runMode);
    }
    
    private static Properties loadConfig() {
        Properties configProp = new Properties();
        InputStream is = null;
        try {
            is = ClassLoader.getSystemResourceAsStream("properties/feed.properties");
            configProp.load(is);
        } catch (Exception e) {
            LOGGER.error("Cannot initialized FeedAggregatorService properties", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    // ignore exception
                }
            }
        }
        return configProp;
    }
    
    private List<DataBuffer> getDataBuffers() {
        String bufferList = System.getProperty(BUFFER_LIST_PROPERTY);
        if (bufferList == null) {
            bufferList = configProp.getProperty(BUFFER_LIST_PROPERTY);
        }
        assert bufferList != null : "No " + BUFFER_LIST_PROPERTY + " specified";
        String[] bufferTypes = bufferList.split(",");
        assert bufferTypes.length > 0;
        
        List<DataBuffer> dataBuffers = new ArrayList<DataBuffer>(bufferTypes.length);
        for (int i=0; i<bufferTypes.length; i++) {
            dataBuffers.add(Enum.valueOf(DataBufferType.class, bufferTypes[i]).getDataBuffer(configProp));
        }
        return dataBuffers;
    }

    public void addDataProvider(DataProvider retrievalService) {
        synchronized (this.dataProviders) {
            boolean added = false;
            for (int i=0; i < this.dataProviders.size() ;i++) {
                if (dataProviders.get(i).getLOS().compareTo(retrievalService.getLOS()) <= 0) {
                    continue;
                } else {
                    this.dataProviders.add(i, retrievalService);
                    added = true;
                    break;
                }
            }
            if (!added) {
                this.dataProviders.add(retrievalService);
            }
        }
    }

    public void removeDataProvider(DataProvider retrievalService) {
        this.dataProviders.remove(retrievalService);
    }

    @Override
    public Map<String, List<Map<String, String>>> getData(Set<String> feedIDs, TimeUnit timeUnit, long startTime,
            long endTime) {
        final ElapsedTimer timer = new ElapsedTimer();
        
        feedIDs = new HashSet<String>(feedIDs);
        int feedSize = feedIDs.size();
        Map<String, List<Map<String, String>>> returnedData = new HashMap<String, List<Map<String,String>>>();
        for (DataProvider dataRetrieval : dataProviders) {
            timer.startInterval();

            Map<String, SortedMap<Long, Map<String, String>>> obtainedValues = dataRetrieval
                    .getData(feedIDs, startTime, endTime, timeUnit);
            
            for (Entry<String, SortedMap<Long, Map<String, String>>> entry: obtainedValues.entrySet()) {
                returnedData.put(entry.getKey(), new LinkedList<Map<String,String>>(entry.getValue().values()));
            }
            filterObtainedFeeds(dataRetrieval, feedIDs, obtainedValues, timeUnit, startTime);
            
            timer.stopInterval();
            READ_PERF_LOGGER.debug("Time to get {} feeds: {} ms from provider " + dataRetrieval.getLOS(), feedSize, timer.getIntervalInMillis());

            if (feedIDs.isEmpty()) { break; }
        }
        return returnedData;
    }
    
    private void filterObtainedFeeds(DataProvider dataProvider, Set<String> feedIDs, Map<String, SortedMap<Long, Map<String, String>>> obtainedValues,
            TimeUnit timeUnit, long startTime) {
        startTime = TimeUnit.NANOSECONDS.convert(startTime, timeUnit);
        
        for (Iterator<String> itr = feedIDs.iterator(); itr.hasNext();) {
            String feedID = itr.next();
            SortedMap<Long, Map<String, String>> feedData = obtainedValues.get(feedID);
            if (feedData != null && !feedData.isEmpty()) {
                long valueTime = feedData.firstKey();
                if (valueTime < startTime || dataProvider.isFullyWithinTimeSpan(feedID, startTime, TimeUnit.NANOSECONDS)) {
                    itr.remove();
                }
            }
        }
    }

    @Override
    public void putData(String feedID, TimeUnit timeUnit, long time, Map<String, String> value) {
        PERF_LOGGER.debug("Queue size {}", queue.size());
        Map<Long, Map<String, String>> entries = new HashMap<Long, Map<String, String>>();
        entries.put(Long.valueOf(time), value);
        putData(feedID, timeUnit, entries);
    }

    /**
     * Asynchronously putting data into the data buffer.
     */
    @Override
    public void putData(String feedID, TimeUnit timeUnit, Map<Long, Map<String, String>> entries) {
        PERF_LOGGER.debug("Queue size {}", queue.size());
        DataArchive db = this.dataArchives.get(0);
        try {
            db.putData(feedID, timeUnit, entries);
        } catch (BufferFullException e) {
            LOGGER.error("Memory buffer should not be full", e);
        }
        PutDataTask task = new PutDataTask(feedID, timeUnit, entries);
        numOfWriteJobs.incrementAndGet();
        bufferWorkers.execute(task);
    }

    @Override
    public void putData(final Map<String, Map<Long, Map<String, String>>> value, final TimeUnit timeUnit,
            final Runnable callback) {
        PERF_LOGGER.debug("Queue size {}", queue.size());
        if (!value.isEmpty()) {
            DataArchive db = this.dataArchives.get(0);
            try {
                db.putData(value, timeUnit, null);
            } catch (BufferFullException e) {
                LOGGER.error("Memory buffer should not be full", e);
            }
            if (dataArchives.size() == 1) {
                callback.run();
            } else {
                Runnable bulkInsert = new Runnable() {
                    public void run() {
                        try {
                            for (Iterator<DataArchive> it = dataArchives.iterator(); it.hasNext();) {
                                DataArchive dataArchive = it.next();
                                if (dataArchive.getLOS() == LOS.fast) {
                                    continue;
                                }
                                try {
                                    if (it.hasNext()) {
                                        dataArchive.putData(value, timeUnit, null);
                                    } else {
                                        dataArchive.putData(value, timeUnit, callback);
                                    }
                                } catch (BufferFullException e) {
                                    dormantArchive = dataArchive;
                                    it.remove();
                                    bufferFullAlert(e.getMessage());
                                }
                            }
                        } finally {
                            synchronized(numOfWriteJobs) {
                                if (numOfWriteJobs.decrementAndGet() == 0) {
                                    numOfWriteJobs.notifyAll();
                                }
                            }
                        }
                    }
                };
                numOfWriteJobs.incrementAndGet();
                bufferWorkers.execute(bulkInsert);
            }
        }
    }
    
    private void bufferFullAlert(String msg) {
        if (currentRunMode == RunMode.embedded) {
            JOptionPane.showMessageDialog(null, msg, DIALOG_WINDOW_TITLE, JOptionPane.WARNING_MESSAGE);
        } else {
            LOGGER.error(msg);
        }
    }
    
    @Override
    public void reset() {
        for (DataArchive dataArchive: dataArchives) {
            dataArchive.reset();
        }
        if (dormantArchive != null) {
            dormantArchive.reset();
            dataArchives.add(1, dormantArchive);
            dormantArchive = null;
        }
    }

    protected void activate(ComponentContext context) {
        LOGGER.debug("FeedAggregatorService activated");
    }

    protected void deactivate(ComponentContext context) {
        shutdown();
        LOGGER.debug("FeedAggregatorService deactivated");
    }

    public void shutdown() {
        synchronized(numOfWriteJobs) {
            while (numOfWriteJobs.get() > 0) {
                try {
                    numOfWriteJobs.wait();
                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException during shutdown", e);
                }
            }
        }
        for (DataProvider dataProvider: dataProviders) {
            if (dataProvider instanceof DataBuffer) {
                DataBuffer.class.cast(dataProvider).closeBuffer();
            }
        }
    }

    private final class PutDataTask implements Runnable {
        private final String feedID;
        private final TimeUnit timeUnit;
        private final Map<Long, Map<String, String>> entries;

        public PutDataTask(String feedID, TimeUnit timeUnit, Map<Long, Map<String, String>> entries) {
            this.feedID = feedID;
            this.timeUnit = timeUnit;
            this.entries = entries;
        }

        @Override
        public void run() {
            try {
                if (dataArchives.size() > 1) {
                    for (Iterator<DataArchive> it = dataArchives.iterator(); it.hasNext();) {
                        DataArchive dataArchive = it.next();
                        if (dataArchive.getLOS() == LOS.fast) {
                            continue;
                        }
                        try {
                            dataArchive.putData(feedID, timeUnit, entries);
                        } catch (BufferFullException e) {
                            dormantArchive = dataArchive;
                            it.remove();
                            bufferFullAlert(e.getMessage());
                        }
                    }
                }
            } finally {
                synchronized(this) {
                    if (numOfWriteJobs.decrementAndGet() == 0) {
                        this.notifyAll();
                    }
                }
            }
        }
    }
}
