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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.policy.PlotViewPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the adding and removing of data feeds for plots.
 */
public class PlotDataAssigner {
	private final static Logger logger = LoggerFactory.getLogger(PlotDataAssigner.class);
	
	private PlotViewManifestation plotViewManifestation;
	
	final AtomicReference<Collection<FeedProvider>> feedProvidersRef;
	Collection<Collection<FeedProvider>> feedsToPlot;
	Collection<FeedProvider> predictiveFeeds;
	
	PlotDataAssigner(PlotViewManifestation supportedPlotViewManifestation) {
		plotViewManifestation = supportedPlotViewManifestation;
		feedProvidersRef = new AtomicReference<Collection<FeedProvider>>(new ArrayList<FeedProvider>());
		feedsToPlot = new ArrayList<Collection<FeedProvider>>();
		predictiveFeeds = new ArrayList<FeedProvider>();
	}
	
	String getTimeSystemDefaultChoice() {
		if (getTimeSystemChoices() != null) {
			if (getTimeSystemChoices().iterator().hasNext()) {
 				return getTimeSystemChoices().iterator().next();
 			}
 		}
 		return null;
	}

	Set<String> getTimeSystemChoices() {
		AbstractComponent[][] matrix = PlotViewPolicy.getPlotComponents(
 				plotViewManifestation.getManifestedComponent(), 
 				useOrdinalPosition());
		logger.debug("Time System matrix length: {}", matrix.length);
 		return aggregateTimeSystemChoices(matrix);			
 	}
 	
 	static Set<String> aggregateTimeSystemChoices(final AbstractComponent[][] matrix) {
 		Set<String> choices = new LinkedHashSet<String>();
 
 		for (AbstractComponent[] row : matrix) {
 			int numberOfItemsOnSubPlot = 0;
 			for (AbstractComponent component : row) {
 				if (numberOfItemsOnSubPlot < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT) {
 					// Alternate implementation is getCapabilities() from each component, then get time system ID from each of fp.
 					FeedProvider fp = component.getCapability(FeedProvider.class);
 					if (fp != null) {
 						String[] timeSystems = fp.getTimeService().getTimeSystems();
 						if (timeSystems != null) {
 							for (int i=0; i<timeSystems.length; i++) {
 								choices.add(timeSystems[i]);
 							}
 						}
 						numberOfItemsOnSubPlot++;
 					} 
 				}
 			}
 		}
 		return choices;
 	}
 	
 	Set<String> getTimeFormatChoices() {
 		AbstractComponent[][] matrix = PlotViewPolicy.getPlotComponents(
 				plotViewManifestation.getManifestedComponent(), 
 				useOrdinalPosition());
 		logger.debug("Time Formats matrix length: ", matrix.length);
 		return aggregateTimeFormatChoices(matrix);			
 	}
 	
 	static Set<String> aggregateTimeFormatChoices(final AbstractComponent[][] matrix) {
 		Set<String> choices = new LinkedHashSet<String>();
 		
 		for (AbstractComponent[] row : matrix) {
 			int numberOfItemsOnSubPlot = 0;
 			for (AbstractComponent component : row) {
 				if (numberOfItemsOnSubPlot < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT) {
 					FeedProvider fp = component.getCapability(FeedProvider.class);
 					if (fp != null) {
 						String[] timeFormats = fp.getTimeService().getTimeFormats();
 						if (timeFormats != null) {
 							for (int i=0; i<timeFormats.length; i++) {
 								choices.add(timeFormats[i]);
 							}
 							numberOfItemsOnSubPlot++;
 						}
 					}
 				}
 			}
 		}
 		return choices;
 	}
	
	Collection<FeedProvider> getVisibleFeedProviders() {
		if (!hasFeeds()) {
			updateFeedProviders();
		}
		return feedProvidersRef.get();
	}
	
	Collection<FeedProvider> getPredictiveFeedProviders() {
		if (!hasFeeds()) {
			updateFeedProviders();
		}
		return predictiveFeeds;
	}
	
	void informFeedProvidersHaveChanged() {
		updateFeedProviders();
	}
	
	int returnNumberOfSubPlots() {
		return feedsToPlot.size();
	}
	
	private void updateFeedProviders() {
		AbstractComponent[][] matrix = PlotViewPolicy.getPlotComponents(
				plotViewManifestation.getManifestedComponent(), 
				useOrdinalPosition());
		updateFeedProviders(matrix);			
	}

	private boolean useOrdinalPosition() {
		String groupByAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.GROUP_BY_ORDINAL_POSITION, String.class);
		return (groupByAsString == null || groupByAsString.isEmpty()) ? true : Boolean.valueOf(groupByAsString);
	}
	
	private void updateFeedProviders(AbstractComponent[][] matrix) {
		ArrayList<FeedProvider> feedProviders = new ArrayList<FeedProvider>();
		feedsToPlot.clear();
		predictiveFeeds.clear();
		for (AbstractComponent[] row : matrix) {
			Collection<FeedProvider> feedsForThisLevel = new ArrayList<FeedProvider>(); //this should be LMIT
			int numberOfItemsOnSubPlot = 0;

			for (AbstractComponent component : row) {
				if (numberOfItemsOnSubPlot < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT) {
					FeedProvider fp = plotViewManifestation.getFeedProvider(component);
					if (fp != null) {
						if(fp.getFeedType() != FeedType.STRING){  //only add to feed providers if not a string feed
							feedProviders.add(fp);
							
							if (fp.isPrediction()) {
								predictiveFeeds.add(fp);
							}
							feedsForThisLevel.add(fp);
						}
					}
					numberOfItemsOnSubPlot++;
				}
			}
			feedsToPlot.add(feedsForThisLevel);
		}
		feedProviders.trimToSize();
		feedProvidersRef.set(feedProviders);
	}
	
	/**
	 * Return true if the plot has feeds, false otherwise.
	 * @return
	 */
	boolean hasFeeds() {
		return !feedProvidersRef.get().isEmpty();
	}
	
	void assignFeedsToSubPlots() {
		assert feedsToPlot !=null : "Feeds to plot must be defined";	
		// Add feeds to the plot.
		int subPlotNumber = 0;
		for (Collection<FeedProvider> feedsForSubPlot: feedsToPlot) {
			assert feedsForSubPlot!=null;
			int numberOfItemsOnSubPlot = 0;
			for(FeedProvider fp: feedsForSubPlot) {
				if (numberOfItemsOnSubPlot < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT) {	
					plotViewManifestation.thePlot.addDataSet(subPlotNumber, fp.getSubscriptionId(), fp.getLegendText());
					numberOfItemsOnSubPlot++;
				}
			}
			subPlotNumber++;
		}		  
	}
}
