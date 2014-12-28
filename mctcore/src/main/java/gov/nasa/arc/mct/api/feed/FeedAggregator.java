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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This interface defines the api for a Feed Aggregator.
 *
 */
public interface FeedAggregator {
	/**
	 * Returns a map of data for each feed. This allows the data to be queried in batch and improves performance.
	 * @param feedIDs to retrieve data for
	 * @param timeUnit the time unit of startTime and endTime parameters.
     * @param startTime the start time of the return data set.
     * @param endTime the end time of the return data set.
	 * @return map of data for the specified feeds. Each entry in the map has data 
	 * with a timestamp that is greater than or equal startTime and less than endTime.
	 */
	public Map<String, List<Map<String, String>>> getData(Set<String> feedIDs, TimeUnit timeUnit, long startTime, long endTime);

}
