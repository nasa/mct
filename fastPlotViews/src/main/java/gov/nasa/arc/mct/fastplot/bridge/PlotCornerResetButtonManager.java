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

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.view.Axis;

import java.util.List;

/**
 * Manages the corner reset buttons on the plot area. 
 */
public class PlotCornerResetButtonManager {
	private PlotterPlot plot;
	
	public PlotCornerResetButtonManager(PlotterPlot thePlot) {
		plot = thePlot;
	}

	/**
	 * Notify manager that the action of unpausing and snapping to the current time has
	 * been selected.
	 */
	void informJumpToCurrentTimeSelected() {
		// unpause the plot. 
		plot.fastForwardTimeAxisToCurrentMCTTime(false);
		plot.notifyObserversTimeChange();
		plot.getPlotAbstraction().getTimeAxisUserPin().setPinned(false);
		if (!plot.getLimitManager().isUntranslated()) {
			plot.getLimitManager().setModeUntranslated(true);
		}
		plot.getPlotAbstraction().updateResetButtons();
		refreshPlotValues();
	}

	/**
	 * Notify manager that the action of resetting the Y axis has been selected. 
	 */
	void informResetYAxisActionSelected() {
		// perform axis reset. 
		resetY();	
		if (plot.isTimeLabelEnabled) {
		  rescalePlotOnTimeAxis();
		}
		plot.getPlotAbstraction().updateResetButtons();
		plot.refreshDisplay();
	}
	
	/**
	 * Notify manager that the action of resetting the X axis has been selected. 
	 */
	void informResetXAxisActionSelected() {
		// perform axis reset.
	    resetX();	
	    if (plot.isTimeLabelEnabled) {
		   rescalePlotOnTimeAxis();
	    }
		plot.getPlotAbstraction().updateResetButtons();
		plot.refreshDisplay();
	}
	
	/**
	 * Notify manager that the action of resetting both the X and Y axis has been selected.
	 */
	void informResetXAndYActionSelected() {
		resetX();
		resetY();
		rescalePlotOnTimeAxis();
		plot.getPlotAbstraction().updateResetButtons();
		plot.refreshDisplay();
	}

	/**
	 * Perform the reset of the x-axis by either fast forwarding to the current time
	 * if time is on the x axis or resetting the non time min max if time is on the y axis. 
	 */
	void resetX() {
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			resetTimeAxis();
		} else {
			resetNonTimeAxis();
		}
		if (!plot.getLimitManager().isUntranslated()) {
			plot.getLimitManager().setModeUntranslated(true);
		}
	}
	
	/**
	 * Perform the reset of the y-axis by either fast forwarding to the current time
	 * if time is on the y axis or resetting the non time min max if time is on the x axis. 
	 */
	void resetY() {
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			resetNonTimeAxis();
		} else {
			resetTimeAxis();
		}
	}
	
	private void resetTimeAxis() {
		Axis axis = plot.getPlotAbstraction().getTimeAxis();
		axis.setZoomed(false);
		plot.fastForwardTimeAxisToCurrentMCTTime(true);	
		plot.notifyObserversTimeChange();
		plot.getPlotAbstraction().getTimeAxisUserPin().setPinned(false);
		refreshPlotValues();
	}
	
	private void refreshPlotValues() {
		plot.clearAllDataFromPlot();
		plot.getPlotAbstraction().requestPlotData(plot.getCurrentTimeAxisMin(), plot.getCurrentTimeAxisMax());
	}
	
	private void resetNonTimeAxis() {
		Axis axis = plot.getNonTimeAxis();
		plot.getNonTimeAxisUserPin().setPinned(false);
		axis.setZoomed(false);
		plot.resetNonTimeAxisToOriginalValues();
		plot.setNonTimeMinFixed(plot.isNonTimeMinFixedByPlotSettings());
		plot.setNonTimeMaxFixed(plot.isNonTimeMaxFixedByPlotSettings());
		plot.getLimitManager().setModeUntranslated(true);
		refreshPlotValues();
	}
	
	/**
	 * Rescale the plot to match the current x-axis settings on the plot time coordinates
	 */
	private void rescalePlotOnTimeAxis() {
		if (plot.isPaused()) {
			plot.getPlotAbstraction().updateResetButtons();
		}
	}
	

	public void updateButtons() {
		Axis timeAxis = plot.getPlotAbstraction().getTimeAxis();
		Axis nonTimeAxis = plot.getNonTimeAxis();
		Axis xAxis;
		Axis yAxis;
		if(plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			xAxis = timeAxis;
			yAxis = nonTimeAxis;
		} else {
			xAxis = nonTimeAxis;
			yAxis = timeAxis;
		}

		List<AbstractPlottingPackage> plots = plot.getPlotAbstraction().getSubPlots();
		// Only show the top right reset button on the top plot.
		if(plots.get(0) == plot) {
			// This was changed to fix MCT-2613: [Plot] Top right corner button appears briefly in jump and scrunch modes, between the time that the plot line hits the end of the time axis and when the jump
			// The problem was that the jump occurs based on the maximum time plotted, which due to compression, is not the same as the current MCT time.
			// As an easy fix, the button is always hidden when the time axis is not pinned.
			// Assuming that data should never appear off the right of a jump plot, this works well enough.
			// If that assumption breaks, the code should be modified to check against the maximum plotted time instead of the current MCT time.
			long now = plot.getPlotAbstraction().getCurrentMCTTime();
			if(!timeAxis.isPinned()) {
				plot.localControlsManager.setJumpToCurrentTimeButtonVisible(false);
			} else if(plot.getMaxTime() < now || plot.getMinTime() > now) {
				plot.localControlsManager.setJumpToCurrentTimeButtonAlarm(true);
			} else {
				plot.localControlsManager.setJumpToCurrentTimeButtonAlarm(false);
			}
		} else {
			plot.localControlsManager.setJumpToCurrentTimeButtonVisible(false);
		}
		// Only show the time axis reset button on the bottom plot.
		boolean enableX = true;
		boolean enableY = true;
		if(plots.get(plots.size() - 1) != plot) {
			if(plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
				enableX = false;
			} else {
				enableY = false;
			}
		}

		plot.localControlsManager.setXAxisCornerResetButtonVisible(enableX && !xAxis.isInDefaultState());
		plot.localControlsManager.setYAxisCornerResetButtonVisible(enableY && !yAxis.isInDefaultState());
		plot.localControlsManager.setXAndYAxisCornerResetButtonVisible(!xAxis.isInDefaultState() && !yAxis.isInDefaultState());
	}
}

