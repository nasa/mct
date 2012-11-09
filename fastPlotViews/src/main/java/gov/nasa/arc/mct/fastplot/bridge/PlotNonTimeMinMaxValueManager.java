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
package gov.nasa.arc.mct.fastplot.bridge;

import java.util.Collection;

/**
 * Responsible for supplying the min/max value currently displayed on the non-time axis within the time bounds
 * of the time axis. This is an expensive operation and a caching strategy is employed to prevent constantly having
 * to scan the entire plot to find out the min/max currently displayed. 
 */
public class PlotNonTimeMinMaxValueManager {
	
	PlotDataManager dataManager;
	
	private boolean minMaxCacheEnabled = true;
	
	private double maxNonTimeValueCurrentlyDisplayed = 0;
	private long timeOfNonTimeMaxValueCurrentlyDisplayed = 0;
	private double minNonTimeValueCurrentlyDisplayed = 0;
	private long timeOfNonTimeMinValueCurrentlyDisplayed = 0;

	PlotNonTimeMinMaxValueManager(PlotDataManager theDataManager) {
		 dataManager = theDataManager;
	}
	
	/**
	 * Return the maximum non time value currently displayed on the non-time axis
	 * @return
	 */
	double getNonTimeMaxDataValueCurrentlyDisplayed() {
		return maxNonTimeValueCurrentlyDisplayed;	
	}

	boolean isEnabled() {
		return minMaxCacheEnabled;
	}
	
	/**
	 * Return the minimum non time value currently displayed on the non-time axis
	 * @return
	 */
	double getNonTimeMinDataValueCurrentlyDisplayed() {
		return minNonTimeValueCurrentlyDisplayed;
	}	
	
	void updateMinMaxCache(long atTime, double value) {
		if (minMaxCacheEnabled ) {
			// Update value if >= or =< cached min/max. 
			// We use = as it pushes the time to a more recent value. 
	
			if (value >= maxNonTimeValueCurrentlyDisplayed) {
				maxNonTimeValueCurrentlyDisplayed = value;
				timeOfNonTimeMaxValueCurrentlyDisplayed = atTime;
			}
	
			if (value <= minNonTimeValueCurrentlyDisplayed) {
				minNonTimeValueCurrentlyDisplayed = value;
				timeOfNonTimeMinValueCurrentlyDisplayed = atTime;
			}
	
			// Refresh min/max if previous min/max value has scrolled of the plot.
			// This is the only time we need perform the expensive operation of 
			// looking over the whole plot. 
	
			if ( dataManager.getPlot().getMinTime() > timeOfNonTimeMaxValueCurrentlyDisplayed) {
				double[] maxValueAndTime = determineNonTimeMaxDataValueCurrentlyDisplayed();
				maxNonTimeValueCurrentlyDisplayed = maxValueAndTime[0];
				timeOfNonTimeMaxValueCurrentlyDisplayed = (long) maxValueAndTime[1];
			}
			if ( dataManager.getPlot().getMinTime() > timeOfNonTimeMinValueCurrentlyDisplayed) {
				double[] minValueAndTime = determineNonTimeMinDataValueCurrentlyDisplayed();
				minNonTimeValueCurrentlyDisplayed = minValueAndTime[0];
				timeOfNonTimeMinValueCurrentlyDisplayed = (long) minValueAndTime[1];
			}
		}
	}

	void setMinMaxCacheState(boolean state) {
		if (state && !minMaxCacheEnabled) {
	    	// compute cached values. 
				double[] maxValueAndTime = determineNonTimeMaxDataValueCurrentlyDisplayed();
				maxNonTimeValueCurrentlyDisplayed = maxValueAndTime[0];
				timeOfNonTimeMaxValueCurrentlyDisplayed = (long) maxValueAndTime[1];
				double[] minValueAndTime = determineNonTimeMinDataValueCurrentlyDisplayed();
				minNonTimeValueCurrentlyDisplayed = minValueAndTime[0];
				timeOfNonTimeMinValueCurrentlyDisplayed = (long) minValueAndTime[1];
	    }
		minMaxCacheEnabled = state;
	}
	
	
	/**
	 * Look over all data sets on the plot and find the maximum value displayed in the current
	 * plot window. 
	 * @return the maximum value
	 */
	private double[] determineNonTimeMaxDataValueCurrentlyDisplayed() {
		double[] maxAndTime = new double[2];
		maxAndTime[0] = -Double.MAX_VALUE;	
		maxAndTime[1] = 0;		
		Collection<PlotDataSeries> dataSets = dataManager.getDataSeries().values();
		for (PlotDataSeries data: dataSets) {
			double[] resultForDataSet = data.getMaxValue(dataManager.getPlot().getMaxTime(), dataManager.getPlot().getMinTime());
			if (resultForDataSet[0] > maxAndTime[0]) {
				maxAndTime = resultForDataSet;	
			}
		}
		if (maxAndTime[0] != -Double.MAX_VALUE) {
			return maxAndTime;
		} else {
			// Data not initialized, return a default max of 1.
			maxAndTime[0] = 1;
			return maxAndTime;
		}
	}

	/**
	 * Look over all data sets on the plot and find the maximum value displayed in the current
	 * plot window. 
	 * @return the minimum value
	 */
	private double[] determineNonTimeMinDataValueCurrentlyDisplayed() {
		double[] minAndTime = new double[2];
		minAndTime[0] = Double.MAX_VALUE;	
		minAndTime[1] = 0;		
		Collection<PlotDataSeries> dataSets = dataManager.getDataSeries().values();
		for (PlotDataSeries data: dataSets) {
			double[] resultForDataSet = data.getMinValue(dataManager.getPlot().getMaxTime(), dataManager.getPlot().getMinTime());
			if (resultForDataSet[0] < minAndTime[0]) {
				minAndTime = resultForDataSet;	
			}
		}
		if (minAndTime[0] != Double.MAX_VALUE) {
			return minAndTime;
		} else {
			// Data not initialized, return a default max of 1.
			minAndTime[0] = 1;
			return minAndTime;
		}
	}

	
}
