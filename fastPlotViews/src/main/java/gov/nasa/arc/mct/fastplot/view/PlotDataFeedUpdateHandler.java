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
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;

import java.awt.Color;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotDataFeedUpdateHandler {
	private final static Logger logger = LoggerFactory
			.getLogger(PlotDataFeedUpdateHandler.class);

	private PlotViewManifestation plotViewManifestation;

	private static final RenderingInfo DEFAULT_RI = new RenderingInfo(null, Color.WHITE, "0", Color.WHITE, false);

	PlotDataFeedUpdateHandler(
			PlotViewManifestation supportedPlotViewManifestation) {
		plotViewManifestation = supportedPlotViewManifestation;
	}

	void synchronizeTime(Map<String, List<Map<String, String>>> data,
			long syncTime) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(syncTime);
		plotViewManifestation.getPlot().showTimeSyncLine(calendar);
		updateFromFeeds(data, true, true, false);
	}
	
	void updateFromFeed(Map<String, List<Map<String, String>>> data, boolean predictionOnly) {
		plotViewManifestation.getPlot().informUpdateFromFeedEventStarted();
		// Receiving any data from the feed informs us that any sync lines
		// should be
		// removed.
		// Check if we cached updates while the updateFromFeed was previously
		// locked.
		updateFromFeeds(data, false, true, predictionOnly);
		plotViewManifestation.getPlot().refreshDisplay();
		// Request that the plot updates its display with the new feed data.
		if (plotViewManifestation.controlPanel != null
				&& plotViewManifestation.controlPanel.isShowing()) {
			//plotViewManifestation.controlPanel.refreshDisplay();
			//TODO: Reconnect so that time marches on
		}
		plotViewManifestation.getPlot().informUpdateFromFeedEventCompleted();
	}

	public void processData(Map<String, List<Map<String, String>>> data) {
		if (logger.isDebugEnabled()) {
			logger.debug("\n Recived new slice {}", printDataOnSlice(data));
		}
		boolean currentCompressionState = plotViewManifestation.getPlot().isCompressionEnabled();
		try {
			plotViewManifestation.getPlot().setCompressionEnabled(false);
			updateFromFeeds(data, false, false, false);
		} finally {
			plotViewManifestation.getPlot().setCompressionEnabled(currentCompressionState);
		}
		
	}

	public void startDataRequest() {
		plotViewManifestation.getPlot().informUpdateDataEventStarted();
		plotViewManifestation.getPlot().refreshDisplay();
	}
	
	public void endDataRequest() {
		closeOutProcessData();
	}
	
	private void closeOutProcessData() {
		if (plotViewManifestation.controlPanel != null
				&& plotViewManifestation.controlPanel.isShowing()) {
			//plotViewManifestation.controlPanel.refreshDisplay();
			//TODO: Time must march on
		}
		// unlock the plot's update events.
		plotViewManifestation.getPlot().informUpdateDataEventCompleted();
		plotViewManifestation.getPlot().refreshDisplay();
	}

	/**
	 * Unwrap the data from the feed service.
	 * 
	 * @param feedIds
	 *            the set of feed IDs
	 * @param data
	 *            the data
	 */
	void updateFromFeeds(Map<String, List<Map<String, String>>> data,
			boolean legendOnly, boolean updateLegend, boolean predictionOnly) {
		if (data != null) {
			Map<String, SortedMap<Long, Double>> dataForPlot = new HashMap<String, SortedMap<Long,Double>>();

			// Iterate over the feeds (each is a plot line on the chart)
			Collection<FeedProvider> feeds = plotViewManifestation
					.getVisibleFeedProviders();

			for (FeedProvider provider : feeds) {
				if (provider.getFeedType() != FeedType.STRING) {
					String feedId = provider.getSubscriptionId();
					List<Map<String, String>> dataForThisFeed = data.get(feedId);
	
					// This prevents "live" data (from the current time, not the maximum valid time) from predictive feeds from
					// showing up in the plot.  This prevents a bug where live data arrives in the middle of a historical data request, and
					// the plot thinks the next historical slice is redundant and ignores it (since it overlaps with the live data).
					boolean allowPlotting = predictionOnly == provider.isPrediction() || !updateLegend;
					
					if (dataForThisFeed != null && plotViewManifestation.getPlot().isKnownDataSet(feedId) && allowPlotting) {
						SortedMap<Long, Double> dataForPlotThisFeed = dataForPlot.get(feedId);
						if(dataForPlotThisFeed == null) {
							dataForPlotThisFeed = new TreeMap<Long, Double>();
							dataForPlot.put(feedId, dataForPlotThisFeed);
						}
	
						RenderingInfo lastRI = DEFAULT_RI;
						boolean haveLegendInfo = false;
	
						// Loop over each point that needs to be plotted for this
						// feed.
						for (Map<String, String> pointsData : dataForThisFeed) {
							RenderingInfo ri = provider
									.getRenderingInfo(pointsData);
							assert pointsData != null : "PointsData is Null";
							String timeAsString = pointsData
									.get(FeedProvider.NORMALIZED_TIME_KEY);
							String valueAsString = ri.getValueText();
						    boolean isPlottable = ri.isPlottable();
	
							// Robust to time or value keys not being present.
							if (timeAsString != null && valueAsString != null) {
								try {
									long milliSecondsEpoch = Long
											.parseLong(timeAsString);
									if (!isPlottable) {
										valueAsString = "";
									}
									lastRI = ri;
									haveLegendInfo = true;
	
									if (!plotViewManifestation.getPlot()
											.isKnownDataSet(feedId)) {
										plotViewManifestation.getPlot().addDataSet(
												feedId, provider.getLegendText());
									}
	
									if (!legendOnly) {
										double value;
										if(isPlottable) {
											value = Double.parseDouble(valueAsString);
										} else {
											value = Double.NaN;
										}
										dataForPlotThisFeed.put(milliSecondsEpoch, value);
									}
	
								} catch (NumberFormatException e) {
									logger
											.error(
													"Number format exception converting string to double while processing the data feed entry {}, {}",
													timeAsString, valueAsString);
								}
							} else {
								logger
										.error(
												"Either time, value, or isValid entry was not defined. {}, {}",
												timeAsString, valueAsString);
							}
						}
						if (haveLegendInfo && updateLegend) {
							plotViewManifestation.getPlot().updateLegend(feedId, lastRI);
						}
					}
				}
			}

			plotViewManifestation.getPlot().addData(dataForPlot);
		} else {
			logger.debug("Data was null");
		}
	}

	private String printDataOnSlice(Map<String, List<Map<String, String>>> data) {
		long earliestTime = Long.MAX_VALUE;
		long latestTime = -Long.MAX_VALUE;
		int numberDataPoints = 0;

		for (String feedId : data.keySet()) {
			List<Map<String, String>> dataForThisFeed = data.get(feedId);
			if (dataForThisFeed != null) {
				for (Map<String, String> pointsData : dataForThisFeed) {
					assert pointsData != null : "PointsData is Null";
					String timeAsString = pointsData
							.get(FeedProvider.NORMALIZED_TIME_KEY);
					if (timeAsString != null) {
						try {
							numberDataPoints++;
							long milliSecondsEpoch = Long
									.parseLong(timeAsString);
							if (milliSecondsEpoch > latestTime) {
								latestTime = milliSecondsEpoch;
							}

							if (milliSecondsEpoch < earliestTime) {
								earliestTime = milliSecondsEpoch;
							}

							assert latestTime >= earliestTime : "latest < earliest!";

						} catch (NumberFormatException e) {
							logger
									.error(
											"Number format exception converting string to double while processing the data feed entry {}",
											timeAsString);
						}
					} else {
						logger
								.error(
										"Either time, value, or isValid entry was not defined. {}",
										timeAsString);
					}
				}
			}
		}
		if (numberDataPoints > 0) {
			return "Slice: "
					+ numberDataPoints;
		} else {
			return "Slice: " + numberDataPoints + " [ .. ]";
		}
	}

}
