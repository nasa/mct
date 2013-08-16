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
package gov.nasa.arc.mct.gui;

import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.util.ComponentModelUtil;
import gov.nasa.arc.mct.gui.FeedView.SynchronizationControl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.SubscriptionManager;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.util.logging.MCTLogger;
import gov.nasa.arc.mct.util.property.MCTProperties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * This class provides an internal thread pool and timing mechanism to control the paint rate of all feed based
 * displays (specifically displays implementing the <code>FeedViewManifestation</code> interface). The paint rate is how often the cycle occurs. 
 * There are several other rates that are of interest:
 * <ul>
 * <li><em>Paint Rate</em> How often feeds are repainted as a unit. Repaints may be triggered by other means
 * such as user driven events that trigger UI repaints, so this rate only affects automatic updating via feed
 * data. The paint rate is global for an MCT instance.</li>
 * <li><em>Downlink Rate</em> How much data is sent per cycle from the vehicle</li>
 * <li><em>Sample Rate</em> How many sensor data points are collected per second. This varies per sensor 
 * and the actual points sent may be compressed (like what is done via ISP) by sending only changes</li>
 * <li><em>Update Rate</em> How many data points of the samples (see Sample Rate) are presented in the UI. This
 * is controlled by the view.</li>
 * </ul>
 * 
 * This class will start a rendering cycle every Paint Rate time. This will get data from the feed provider 
 * and then dispatch painting to the View (in the AWT thread). The data acquisition and painting will be done 
 * as a group. 
 * 
 * This class will only start a maximum number of worker threads, if the current cycle would exceed 
 * the number of worker threads. The cycle is skipped and the next cycle will request a longer
 * time range. 
 * 
 * During the rendering cycle, subscriptions are managed using referencing counting. Thus when a 
 * transition from zero to one feed views occurs a subscription is requested and when the number of views
 * transitions from one to zero a subscription is removed. 
 */
class FeedRenderingPool {
    private final Timer timer;
    private final int paintRate; // milliseconds between updating feed views
    /**
     * This variable determines the currently active subscriptions and will only be accessed
     * from the timer thread; hence, no synchronization is required. 
     */
    private Set<FeedProvider> activeSubscriptions = Collections.emptySet();
    private final Set<FeedView> activeFeedViews = new ConcurrentSkipListSet<FeedView>(new IdentityComparator());
    private static final MCTLogger LOGGER = MCTLogger.getLogger(FeedRenderingPool.class);
    private final AtomicInteger activeRenderers = new AtomicInteger(0);
    private static final int MAX_ACTIVE_REQUESTS = 1;
    private final ConcurrentHashMap<FeedProvider, Long> activeFeeds = new ConcurrentHashMap<FeedProvider, Long>();
    private final AtomicReference<SynchronizationControl> activeSyncControl = new AtomicReference<SynchronizationControl>();
    private AtomicBoolean exceededMaxSubscriptions = new AtomicBoolean(false);
    private static final int maxSubscriptions = initMaxSubscriptions();
    private static final Comparator<FeedProvider> FEED_COMPARATOR = new Comparator<FeedProvider>() {
        @Override
        public int compare(FeedProvider o1, FeedProvider o2) {
            return o1.getSubscriptionId().compareTo(o2.getSubscriptionId());
        }
    };
    
    /**
     * Create a new instance.
     * @param paintRateInterval how often in milliseconds to paint the feed displays
     * @throws IllegalArgumentException if paintRateIntervalue is < 1
     */
    public FeedRenderingPool(int paintRateInterval) throws IllegalArgumentException {
        if (paintRateInterval < 1) {
            throw new IllegalArgumentException("paint rate interval must be greater than 0");
        }
        paintRate = paintRateInterval;
        timer = new Timer("MCT Painting timer",true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOGGER.debug("timer event fired");
                try {
                    startWorker();
                } catch (Exception e) {
                    LOGGER.error("exception thrown out of scheduled paint thread. " +
                            "The root cause of this exception should be fixed but operation should continue normally", e);
                }
            }
        };
        timer.scheduleAtFixedRate(task, paintRate, paintRate);
    }
    
    private static class IdentityComparator implements Comparator<Object>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Object o1, Object o2) {
            return System.identityHashCode(o2) - System.identityHashCode(o1);
        }
    }
    
    void cancelTimer() {
        timer.cancel();
    }
    
    /**
     * Add a view to list of feeds being rendered on the paint cycle.
     * @param manifestation to start delivering feed events to
     */
    public void addFeedView(FeedView manifestation) throws IllegalArgumentException {
        activeFeedViews.add(manifestation);
    }
    
    /**
     * Add a view to list of feeds being rendered on the paint cycle.
     * @param manifestation to stop delivering feed events to
     */
    public void removeFeedView(FeedView manifestation) {
        activeFeedViews.remove(manifestation);
    }
    
    public SynchronizationControl synchronizeTime(final long syncTime) {
        final Set<FeedView> syncedManifestations = new HashSet<FeedView>();
         SynchronizationControl sc = new SynchronizationControl() {
             @Override
            public void update(long syncTime) {
                 /* Assume the "time" is uniform across all providers, this is a limitation that needs to 
                  * removed when feeds (non ISP) are added. One way to do this is to pass the time
                  * service along with the time to get offsets between the various time services. For 
                  * example, TimeServiceA - 4, TimeServiceB - 2, then if time 3 was passed along with 
                  * TimeServiceA then the offset to TimeServiceB would be (2 - 4) = -2 + time to get the
                  * time from TimeServiceB. 
                  */ 
                 Map<FeedProvider, Long[]> times = new HashMap<FeedProvider,Long[]>();
                 for (FeedView manifestation:activeFeedViews) {
                     for (FeedProvider provider:manifestation.getVisibleFeedProviders()) {
                         times.put(provider, new Long[]{syncTime-2000, syncTime});
                     }
                 }
                 FeedCycleRenderer renderer = createSyncWorker(syncTime, times, activeFeedViews, syncedManifestations);

                 try {
                     renderer.execute();
                 } catch (Exception e) {
                     LOGGER.error(e);
                 }
            }

            @Override
            public void synchronizationDone() {
                 activeSyncControl.set(null);
                 for(FeedView m : syncedManifestations) {
                    m.synchronizationDone();
                }
            }
         };
        
         if (!activeSyncControl.compareAndSet(null, sc)) {
             return null;
         }
         
        /* Assume the "time" is uniform across all providers, this is a limitation that needs to 
         * removed when feeds (non ISP) are added. One way to do this is to pass the time
         * service along with the time to get offsets between the various time services. For 
         * example, TimeServiceA - 4, TimeServiceB - 2, then if time 3 was passed along with 
         * TimeServiceA then the offset to TimeServiceB would be (2 - 4) = -2 + time to get the
         * time from TimeServiceB. 
         */ 
        Map<FeedProvider, Long[]> times = new HashMap<FeedProvider,Long[]>();
        for (FeedView manifestation:activeFeedViews) {
            for (FeedProvider provider:manifestation.getVisibleFeedProviders()) {
                times.put(provider, new Long[]{syncTime-2000, syncTime});
            }
        }
        FeedCycleRenderer renderer = createSyncWorker(syncTime, times, activeFeedViews, syncedManifestations);
        
        try {
            renderer.execute();
        } catch (Exception e) {
            LOGGER.error(e);
            sc.synchronizationDone();
            sc = null;
        }
        
        return sc;
    }
    
    FeedCycleRenderer createSyncWorker(final long syncTime, Map<FeedProvider, Long[]> times, Set<FeedView> activeFeedViews, final Set<FeedView> syncedManifestations) {
        return new FeedCycleRenderer(times,activeFeedViews) {
            @Override
            protected void dispatchToFeed(FeedView manifestation,
                    Map<String, List<Map<String, String>>> data) {
                syncedManifestations.add(manifestation);
                manifestation.synchronizeTime(data,syncTime);
            }
        };
    }
    
    FeedCycleRenderer createWorker(Map<FeedProvider, Long[]> times, Set<FeedView> activeFeedViews) {
        return new FeedCycleRenderer(times, activeFeedViews);
    }
    
    /**
     * Start a new worker or extend an existing worker. This will only be called from the
     * timer thread. 
     */
    private void startWorker() {
        handleSubscriptions();
        
        // if the current number of active requests > max number of threads, then wait for the next
        // one
        if (activeRenderers.get() < MAX_ACTIVE_REQUESTS && activeSyncControl.get() == null) {
            Map<TimeService,Long> currentTimes = new HashMap<TimeService,Long>();
            Map<FeedProvider,Long[]> times = new TreeMap<FeedProvider,Long[]>(FEED_COMPARATOR);
            for (Entry<FeedProvider,Long> lastTimeMapping:activeFeeds.entrySet()) {
                FeedProvider feed = lastTimeMapping.getKey();
                long lastRequestTime = lastTimeMapping.getValue();
                // ensure that all values coming from the same time service reflect the same time
                Long cachedTime = currentTimes.get(feed.getTimeService());
                if (cachedTime == null) {
                    cachedTime = feed.getTimeService().getCurrentTime();
                    currentTimes.put(feed.getTimeService(), cachedTime);
                }
                long currentTime = cachedTime;
                Long[] timeRange = new Long[] {lastRequestTime, currentTime};
                if (timeRange[0] < timeRange[1]) {
                    timeRange[0]++;
                    times.put(feed, timeRange);
                    activeFeeds.put(feed, currentTime);
                } 
            }
            
            if (!times.isEmpty()) {
                FeedCycleRenderer worker = createWorker(times, activeFeedViews);
                worker.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
                            activeRenderers.decrementAndGet();
                        }
                    }
                });
                activeRenderers.incrementAndGet();
                worker.execute();
            } 
        }
    }
    
    /**
     * Determine the current subscriptions required by iterating through the active manifestations
     * and extracting the providers.  
     * @return
     */
    private Set<FeedProvider> buildRequiredSubscriptions() {
        Set<FeedProvider> requiredSubscriptions = new TreeSet<FeedProvider>(FEED_COMPARATOR);
        
        for (FeedView manifestation: activeFeedViews) {
            Collection<FeedProvider> providers = manifestation.getVisibleFeedProviders();
            if (providers != null) {
                requiredSubscriptions.addAll(providers);
            }
        }
        
        return requiredSubscriptions;
    }
    
    /**
     * Get the current subscription manager (may return null)
     * @return the current subscription manager, or null if these is none
     */
    SubscriptionManager getSubscriptionManager() {
        try {
            return PlatformAccess.getPlatform().getSubscriptionManager();
        } catch (IllegalStateException ise) {
            // This may happen if OSGi is shutting down,
            // which occurs on a separate thread.
            return null;
        }
    }
    
    /**
     * Use the subscription manager to manage subscriptions
     */
    void handleSubscriptions() {
        Set<FeedProvider> requiredSubscriptions = buildRequiredSubscriptions();
        List<FeedProvider> newSubscriptions = new ArrayList<FeedProvider>();
        List<FeedProvider> removedSubscriptions = new ArrayList<FeedProvider>();
        Set<FeedProvider> set = new TreeSet<FeedProvider>(FEED_COMPARATOR);
        set.addAll(requiredSubscriptions);
        ComponentModelUtil.computeAsymmetricSetDifferences(
                requiredSubscriptions, activeSubscriptions, newSubscriptions, removedSubscriptions,set);

        if (exceededMaxSubscriptions(requiredSubscriptions, newSubscriptions)) return;
     
        SubscriptionManager manager = getSubscriptionManager();
        if (manager != null) {

        	for (FeedProvider feed:removedSubscriptions) {
                LOGGER.debug("removing subscription for {0}", feed.getSubscriptionId());
                manager.unsubscribe(feed.getSubscriptionId());
                activeFeeds.remove(feed);
            }
        	
        	List<String> newlyAddedSubscriptionIds = new ArrayList<String> (newSubscriptions.size());
            for (FeedProvider feed:newSubscriptions) {
                LOGGER.debug("adding subscription for {0}", feed.getSubscriptionId());
                newlyAddedSubscriptionIds.add(feed.getSubscriptionId());
                activeFeeds.put(feed, feed.getTimeService().getCurrentTime());
            }
            
            assert newlyAddedSubscriptionIds.size() == newSubscriptions.size();
            if (!newlyAddedSubscriptionIds.isEmpty()) {
               manager.subscribe(newlyAddedSubscriptionIds.toArray(new String[newlyAddedSubscriptionIds.size()])); 
            }
            
            activeSubscriptions = requiredSubscriptions;
        } else {
            LOGGER.warn("subscription manager not available, subscriptions not updated");
        } 
        
    }

    private boolean exceededMaxSubscriptions(Set<FeedProvider> requiredSubscriptions, 
            List<FeedProvider> newSubscriptions) {

        if (requiredSubscriptions.size() > maxSubscriptions && !newSubscriptions.isEmpty()) {
            if (exceededMaxSubscriptions.getAndSet(true) == false) {
                LOGGER.error("You have exceeded the maximum number of active subscriptions configured for this application ("+maxSubscriptions+"). Please close some of your unused windows/views.");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OptionBox.showMessageDialog(
                                null, 
                                "You have exceeded the maximum number of active subscriptions \nconfigured for this application. \nPlease close some of your unused windows/views.",
                                "Maximum subscriptions Error", 
                                OptionBox.ERROR_MESSAGE);
                    }
                });
            }
            return true;
        } else {
            exceededMaxSubscriptions.set(false);
            return false;
        }       
    }

    private static int initMaxSubscriptions() {
        int defaultMax = 3000;
        int max = defaultMax;
        final MCTProperties mctProperties = MCTProperties.DEFAULT_MCT_PROPERTIES;
        String str = mctProperties.getProperty("max.subscriptions", "unset");
        if (str.equals("unset")) {
            LOGGER.error("Property mct.max.subscriptions is not set or empty. Using default of: "+ defaultMax);
        }
        try {
            max = new Integer(str);
        } catch (NumberFormatException e) {
            LOGGER.error("Could not convert mct.max.subscriptions to a valid number. Using default of: "+ defaultMax);
        }
        return max;
    }
    
    Set<FeedView> getAllActiveFeedManifestations() {
        if (activeFeedViews == null) { return Collections.emptySet(); }
        return Collections.<FeedView>unmodifiableSet(activeFeedViews);
    }
    
    void resetLastDataRequestTimeToCurrentTime() {
        for (Entry<FeedProvider, Long> entry : activeFeeds.entrySet()) {
            entry.setValue(entry.getKey().getTimeService().getCurrentTime());
        }
    }
}
