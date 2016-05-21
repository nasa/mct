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
package gov.nasa.arc.mct.api.feed;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;


/**
 * This interface provides an interface for retrieving data from
 * a feed.
 *
 */
public interface DataProvider extends FeedAggregator {
    /**
     * An enum which defines the various level of service (LOS) a data provider can provide.
     *
     */
    public static enum LOS {
        /** Fast enum. */
        fast, 
        /** Medium enum. */
        medium, 
        /** Slow enum.*/
        slow
    }
    
    /**
     * Returns a map of data for each feed. This allows the data to be queried in batch and improves performance.
     * @param feedIDs to retrieve data for
     * @param startTime the start time of the return data set.
     * @param endTime the end time of the return data set.
     * @param timeUnit the time unit of startTime and endTime parameters.
     * @return map of data for the specified feeds. Each entry in the map has data 
     * with a timestamp that is greater or equal startTime and less than or equal endTime ordered according to the time.
     */
    public Map<String, SortedMap<Long, Map<String, String>>> getData(Set<String> feedIDs, long startTime, long endTime, TimeUnit timeUnit);

    /**
     * Check if a request can be fully serviced by a data provider.
     * @param feedID feed that is requested
     * @param startTime start time of the request
     * @param timeUnit the time unit of startTime
     * @return true if a request can be fully serviced by a data provider.
     */
    public boolean isFullyWithinTimeSpan(String feedID, long startTime, TimeUnit timeUnit);
    
    /**
     * Return the level of service that a data retrieval provider can provide.
     * @return the level of service.
     */
    public LOS getLOS();
}
