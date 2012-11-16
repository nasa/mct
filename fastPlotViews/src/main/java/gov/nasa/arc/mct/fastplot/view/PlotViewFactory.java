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

import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;
import gov.nasa.arc.mct.fastplot.scatter.ScatterPlot;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotViewFactory {

	private final static Logger logger = LoggerFactory.getLogger(PlotViewFactory.class);
	
	/**
	 * Create the plot from persisted settings if available. Otherwise, create a default plot. 
	 */
	static PlotView createPlot(PlotSettings settings, long currentTime, PlotViewManifestation parentManifestation,
			                   int numberOfSubPlots, PlotView oldPlot, AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm, String viewStateTimeSystem) {		
        PlotView thePlot;
        
        // Insure we always have at least one plot.
        numberOfSubPlots = settings.getAxisOrientationSetting() == AxisOrientationSetting.Z_AXIS_AS_TIME ?
        		1 : Math.max(1,numberOfSubPlots);
        
		if (!settings.isNull()) {
			if (settings.getTimeSystemSetting() == null) {
				settings.setTimeSystemSetting(viewStateTimeSystem);
			}

			// The plot has persisted settings so apply them. 
			if (!settings.getPinTimeAxis()) {
				adjustPlotStartAndEndTimeToMatchCurrentTime(settings, currentTime);
			}
			thePlot = createPlotFromSettings(settings, numberOfSubPlots, plotLabelingAlgorithm);
		} else {
			// Setup a default plot to view while the user is configuring it.
			thePlot = new PlotView.Builder(PlotterPlot.class).
						 numberOfSubPlots(numberOfSubPlots).
			             plotLabelingAlgorithm(plotLabelingAlgorithm).build();
		} 	
		thePlot.setManifestation(parentManifestation);
		thePlot.setPlotLabelingAlgorithm(plotLabelingAlgorithm);
			
		assert thePlot!=null : "Plot labeling algorithm should NOT be NULL at this point.";
		
		logger.debug("plotLabelingAlgorithm.getPanelContextTitleList().size()=" 
				+ plotLabelingAlgorithm.getPanelContextTitleList().size()
				+ ", plotLabelingAlgorithm.getCanvasContextTitleList().size()=" + plotLabelingAlgorithm.getCanvasContextTitleList().size());
		
		// Copy across feed mapping from old plot, unless structure is different
		if (oldPlot!=null && oldPlot.subPlots.size() == numberOfSubPlots) {
			for (String dataSetName: oldPlot.dataSetNameToSubGroupMap.keySet()) {
				String nameLower = dataSetName.toLowerCase();
				for(AbstractPlottingPackage plot : oldPlot.dataSetNameToSubGroupMap.get(dataSetName)) {
					int indexInOldPlot = oldPlot.subPlots.indexOf(plot);
					thePlot.addDataSet(indexInOldPlot, dataSetName, oldPlot.dataSetNameToDisplayMap.get(nameLower));	
				}
			}
		} 
		return thePlot;
	}
	

	/**
	 * Create the plot using the persisted settings.
	 */
	static PlotView createPlotFromSettings(PlotConfiguration settings, int numberOfSubPlots, AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm) {			
			Class<? extends AbstractPlottingPackage> plottingPackage = 
				settings.getAxisOrientationSetting() != AxisOrientationSetting.Z_AXIS_AS_TIME ?
						PlotterPlot.class : ScatterPlot.class;
			PlotView newPlot = new PlotView.Builder(plottingPackage)
			.plotSettings(settings)
			.numberOfSubPlots(numberOfSubPlots)
			.plotLabelingAlgorithm(plotLabelingAlgorithm)
			.build();
			
			newPlot.setPlotLabelingAlgorithm(plotLabelingAlgorithm);
			
			return newPlot;
	}
	
	private static void adjustPlotStartAndEndTimeToMatchCurrentTime(PlotConfiguration settings, long currentTime) {
		if (settings.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			if (currentTime > settings.getMaxTime()) {
				// Fast forward to now on the upper bound. 
				settings.setMaxTime(currentTime);
			}
		} else if (settings.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.JUMP && 
				!settings.getPinTimeAxis()) {
			if (currentTime > settings.getMaxTime()) {
				long span = settings.getMaxTime() - settings.getMinTime();
				settings.setMaxTime(currentTime);
				settings.setMinTime(currentTime - span);
			}
		}
	}

}
