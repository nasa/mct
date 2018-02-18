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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PanDirection;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotDisplayState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.ZoomDirection;
import gov.nasa.arc.mct.fastplot.view.Axis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plotter.xy.XYAxis;

public class PanAndZoomManager {

	private final static Logger logger = LoggerFactory.getLogger(PanAndZoomManager.class);
	
	private PlotterPlot plot;

	private boolean inZoomMode;
	
	private boolean inPanMode;

	public PanAndZoomManager(PlotterPlot quinnCurtisPlot) {
		plot = quinnCurtisPlot;
	}

	public void enteredPanMode() {
		logger.debug("Entering pan mode");
		inPanMode = true;
		plot.setPlotDisplayState(PlotDisplayState.USER_INTERACTION);
	}
	
	public void exitedPanMode() {
		inPanMode = false;
		logger.debug("Exited pan mode");
	}
	
	public void enteredZoomMode() {
		logger.debug("Entered zoom mode");
		inZoomMode = true;
		plot.setPlotDisplayState(PlotDisplayState.USER_INTERACTION);
	}
	
	public void exitedZoomMode() {
		inZoomMode = false;
		logger.debug("Exited zoom mode");	
	}


	public boolean isInZoomMode() {
		return inZoomMode;
	}


	public boolean isInPanMode() {
		return inPanMode;
	}

	
	public void panAction(PanDirection panningAction) {
		XYAxis xAxis = plot.getPlotView().getXAxis();
		XYAxis yAxis = plot.getPlotView().getYAxis();
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			double nonTimeScalePanAmount = yAxis.getEnd() - yAxis.getStart();
			double timeScalePanAmount = xAxis.getEnd() - xAxis.getStart();
			
			timeScalePanAmount = (timeScalePanAmount/100) * PlotConstants.PANNING_PERCENTAGE;
			nonTimeScalePanAmount= (nonTimeScalePanAmount/100) * PlotConstants.PANNING_PERCENTAGE;

			if (panningAction == PanDirection.PAN_HIGHER_Y_AXIS) {
				yAxis.shift(nonTimeScalePanAmount);
				pinNonTime();
			} else if (panningAction == PanDirection.PAN_LOWER_Y_AXIS) {
				yAxis.shift(-nonTimeScalePanAmount);
				pinNonTime();
			} else if (panningAction == PanDirection.PAN_LOWER_X_AXIS) {
				xAxis.shift(-timeScalePanAmount);
				pinTime();
				plot.notifyObserversTimeChange();
			} else if (panningAction == PanDirection.PAN_HIGHER_X_AXIS) {
				xAxis.shift(timeScalePanAmount);
				pinTime();
				plot.notifyObserversTimeChange();
			}		
		} else {
			
			double nonTimeScalePanAmount = xAxis.getEnd() - xAxis.getStart();
			double timeScalePanAmount = yAxis.getEnd() - yAxis.getStart();
			
			timeScalePanAmount = (timeScalePanAmount/100) * PlotConstants.PANNING_PERCENTAGE;
			nonTimeScalePanAmount= (nonTimeScalePanAmount/100) * PlotConstants.PANNING_PERCENTAGE;
			
			if (panningAction == PanDirection.PAN_HIGHER_Y_AXIS) {
				yAxis.shift(timeScalePanAmount);
				pinTime();
				plot.notifyObserversTimeChange();
			} else if (panningAction == PanDirection.PAN_LOWER_Y_AXIS) {
				yAxis.shift(-timeScalePanAmount);
				pinTime();
				plot.notifyObserversTimeChange();
			} else if (panningAction == PanDirection.PAN_LOWER_X_AXIS) {
				xAxis.shift(-nonTimeScalePanAmount);
				pinNonTime();
			} else if (panningAction == PanDirection.PAN_HIGHER_X_AXIS) {
				xAxis.shift(nonTimeScalePanAmount);
				pinNonTime();
			}	
		}
		plot.getPlotAbstraction().updateResetButtons();
		plot.refreshDisplay();
		//Always request data refresh
		plot.clearAllDataFromPlot();
		plot.getLimitManager().setModeUntranslated(false);
		plot.getPlotAbstraction().requestPlotData(plot.getCurrentTimeAxisMin(), plot.getCurrentTimeAxisMax());

	}


	private void pinTime() {
		plot.getPlotAbstraction().getTimeAxisUserPin().setPinned(true);
	}


	private void pinNonTime() {
		plot.getNonTimeAxisUserPin().setPinned(true);
		if (plot.getLimitManager().isUntranslated()) {
			plot.getLimitManager().setModeUntranslated(false);
		}
	}


	private void markTimeZoomed() {
		Axis axis = plot.getPlotAbstraction().getTimeAxis();
		pinTime();
		axis.setZoomed(true);
		if (plot.getLimitManager().isUntranslated()) {
			plot.getLimitManager().setModeUntranslated(false);
		}
	}

	private void markNonTimeZoomed() {
		Axis axis = plot.getNonTimeAxis();
		pinNonTime();
		axis.setZoomed(true);
		if (plot.getLimitManager().isUntranslated()) {
			plot.getLimitManager().setModeUntranslated(false);
		}
	}

	
	public void zoomAction(ZoomDirection zoomAction) {
		XYAxis xAxis = plot.getPlotView().getXAxis();
		XYAxis yAxis = plot.getPlotView().getYAxis();
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			double nonTimeScaleZoomAmount = yAxis.getEnd() - yAxis.getStart();
			double timeScaleZoomAmount = xAxis.getEnd() - xAxis.getStart();
			
			timeScaleZoomAmount = (timeScaleZoomAmount/100) * PlotConstants.ZOOMING_TIME_AXIS_PERCENTAGE;
			nonTimeScaleZoomAmount= (nonTimeScaleZoomAmount/100) * PlotConstants.ZOOMING_TIME_AXIS_PERCENTAGE;
			
			if (zoomAction == ZoomDirection.ZOOM_IN_HIGH_Y_AXIS) {
				yAxis.setEnd(yAxis.getEnd() - nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_HIGH_Y_AXIS) {
				  yAxis.setEnd(yAxis.getEnd() + nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_CENTER_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() + nonTimeScaleZoomAmount);
				yAxis.setEnd(yAxis.getEnd() - nonTimeScaleZoomAmount);
				 markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() - nonTimeScaleZoomAmount);
				yAxis.setEnd(yAxis.getEnd() + nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_LOW_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() + nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_LOW_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() - nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_LEFT_X_AXIS) {
				xAxis.setStart(xAxis.getStart() + timeScaleZoomAmount);
				  markTimeZoomed();
				 plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_LEFT_X_AXIS) {
					xAxis.setStart(xAxis.getStart() - timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_CENTER_X_AXIS) {
				xAxis.setStart(xAxis.getStart() + timeScaleZoomAmount);
				xAxis.setEnd(xAxis.getEnd() - timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_CENTER_X_AXIS) {
				xAxis.setStart(xAxis.getStart() - timeScaleZoomAmount);
				xAxis.setEnd(xAxis.getEnd() + timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_RIGHT_X_AXIS) {
				xAxis.setEnd(xAxis.getEnd() - timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_RIGHT_X_AXIS) {
				xAxis.setEnd(xAxis.getEnd() + timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			}
		
		} else {
			double nonTimeScaleZoomAmount = xAxis.getEnd() - xAxis.getStart();
			double timeScaleZoomAmount = yAxis.getEnd() - yAxis.getStart();
			
			timeScaleZoomAmount = (timeScaleZoomAmount/100) * PlotConstants.ZOOMING_TIME_AXIS_PERCENTAGE;
			nonTimeScaleZoomAmount = (nonTimeScaleZoomAmount/100) * PlotConstants.ZOOMING_TIME_AXIS_PERCENTAGE;
			
			if (zoomAction == ZoomDirection.ZOOM_IN_HIGH_Y_AXIS) {
					yAxis.setEnd(yAxis.getEnd() - timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_HIGH_Y_AXIS) {
				yAxis.setEnd(yAxis.getEnd() + timeScaleZoomAmount);
				  markTimeZoomed();
				 plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_CENTER_Y_AXIS) {
					yAxis.setStart(yAxis.getStart() + timeScaleZoomAmount);
					yAxis.setEnd(yAxis.getEnd() - timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() - timeScaleZoomAmount);
				yAxis.setEnd(yAxis.getEnd() + timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_LOW_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() + timeScaleZoomAmount);
				  markTimeZoomed();
				 plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_LOW_Y_AXIS) {
				yAxis.setStart(yAxis.getStart() - timeScaleZoomAmount);
				  markTimeZoomed();
				  plot.notifyObserversTimeChange();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_LEFT_X_AXIS) {
				xAxis.setStart(xAxis.getStart() + nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_LEFT_X_AXIS) {
					xAxis.setStart(xAxis.getStart() - nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_CENTER_X_AXIS) {
					xAxis.setStart(xAxis.getStart() + nonTimeScaleZoomAmount);
					xAxis.setEnd(xAxis.getEnd() - nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_CENTER_X_AXIS) {
				xAxis.setStart(xAxis.getStart() - nonTimeScaleZoomAmount);
				xAxis.setEnd(xAxis.getEnd() + nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_IN_RIGHT_X_AXIS) {
					xAxis.setEnd(xAxis.getEnd() - nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			} else if (zoomAction == ZoomDirection.ZOOM_OUT_RIGHT_X_AXIS) {
				xAxis.setEnd(xAxis.getEnd() + nonTimeScaleZoomAmount);
				  markNonTimeZoomed();
			}
		}
		plot.getPlotAbstraction().updateResetButtons();
		plot.refreshDisplay();
		//Always request data refresh
		plot.getLimitManager().setModeUntranslated(false);
		plot.getPlotDataManager().resizeAndReloadPlotBuffer();
	}
	
	
}




