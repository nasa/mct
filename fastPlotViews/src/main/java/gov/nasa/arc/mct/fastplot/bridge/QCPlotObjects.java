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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;

import gov.nasa.arc.mct.fastplot.utils.TimeFormatUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.JComponent;

import plotter.DateNumberFormat;
import plotter.xy.DefaultXYLayoutGenerator;
import plotter.xy.LinearXYAxis;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYGrid;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

/**
 * Manages the Quinn Curtis objects that come together to form a plot. The axis, the background rectangle etc.
 */
public class QCPlotObjects {
	PlotterPlot plot;
	private String timeSystemId;
	
	public QCPlotObjects(PlotterPlot thePlot) {
		plot = thePlot;
		createPlotInstance();		
	}

	void createPlotInstance() {
		assert plot.plotView==null: "Plot already initalized.";

		// Create new instance of the plot 
		plot.plotView = new XYPlot();
		plot.plotView.setBackground(PlotConstants.DEFAULT_PLOT_FRAME_BACKGROUND_COLOR);
		XYPlotContents contents = new XYPlotContents();
		contents.setBackground(Color.black);
		plot.plotView.add(contents);
		plot.plotView.setPreferredSize(new Dimension(PlotterPlot.PLOT_PREFERED_WIDTH, PlotterPlot.PLOT_PREFERED_HEIGHT));

		JComponent panel = plot.getPlotPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		layout.setConstraints(plot.plotView, constraints);
		panel.add(plot.plotView);
		
		// Setup the plot. 
		// Note: the order of these operations is important as there are dependencies between plot components.
		// Assertion have been added to detect disturbances in the order if the code is changed. 
		// However, care should be taken when editing this code. 

		// Setup the time coordinate base of the plot.
		setupTimeCoordinates();

		// Setup the x- and y-axis.
		setupAxis();

		setupScrollFrame();

		new DefaultXYLayoutGenerator().generateLayout(plot.plotView);
	}

	/**
	 * Set up the time coordinates according to the plot settings. This is the feature
	 * that determines axis location and maximum/minimum value locations.
	 */
	private void setupTimeCoordinates() {
		// Set the start/end time boundaries as specified. 
		plot.startTime = new GregorianCalendar();
		plot.startTime.setTimeInMillis(plot.timeVariableAxisMinValue);
		plot.endTime = new GregorianCalendar();
		plot.endTime.setTimeInMillis(plot.timeVariableAxisMaxValue);

		assert(plot.startTime!=null): "Start time should have been initalized by this point.";
		assert(plot.endTime != null): "End time should not have been intialized by this point";
	}

	private NumberFormat getNumberFormatter() {
		return new DecimalFormat(PlotConstants.SCIENTIFIC_NUMBER_FORMAT);
	}
	
	private void setupAxis() {
		assert plot.plotView !=null : "Plot Object not initalized";

		if (plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			// time is on the x-axis.	

			// Setup the axis. 
			TimeXYAxis xAxis = new TimeXYAxis(XYDimension.X);
			plot.setTimeAxis(xAxis);
			if(plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {
				xAxis.setStart(plot.startTime.getTimeInMillis());
				xAxis.setEnd(plot.endTime.getTimeInMillis());
			} else {
				xAxis.setStart(plot.endTime.getTimeInMillis());
				xAxis.setEnd(plot.startTime.getTimeInMillis());
			}
			LinearXYAxis yAxis = new LinearXYAxis(XYDimension.Y);
			plot.theNonTimeAxis = yAxis;
			if(plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				yAxis.setStart(plot.nonTimeVaribleAxisMinValue);
				yAxis.setEnd(plot.nonTimeVaribleAxisMaxValue);
			} else {
				yAxis.setStart(plot.nonTimeVaribleAxisMaxValue);
				yAxis.setEnd(plot.nonTimeVaribleAxisMinValue);
			}
			if(plot.isTimeLabelEnabled) {
				xAxis.setPreferredSize(new Dimension(1, 20));
			} else {
				xAxis.setPreferredSize(new Dimension(1, 10));
			}
			yAxis.setPreferredSize(new Dimension(PlotConstants.Y_AXIS_WHEN_NON_TIME_LABEL_WIDTH , 1));
			
			xAxis.setForeground(plot.timeAxisColor);
			yAxis.setForeground(plot.nonTimeAxisColor);
			yAxis.setFormat(getNumberFormatter());
			plot.plotView.setXAxis(xAxis);
			plot.plotView.setYAxis(yAxis);
			plot.plotView.add(xAxis);
			plot.plotView.add(yAxis);

			// Setup the axis labels.
			if (plot.isTimeLabelEnabled) {
				SimpleDateFormat format = TimeFormatUtils.makeDataFormat(plot.timeFormatSetting);
				xAxis.setFormat(new DateNumberFormat(format));
                xAxis.setTimeSystemAxisLabelName(timeSystemId);
			} else {
				xAxis.setShowLabels(false);
			}

			xAxis.setFont(plot.timeAxisFont);
			yAxis.setFont(plot.timeAxisFont);

			// Setup the gridlines
			XYGrid grid = new XYGrid(xAxis, yAxis);
			grid.setForeground(plot.gridLineColor);
			plot.plotView.getContents().add(grid);

			xAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			xAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			xAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 2);
			yAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			yAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			yAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 5);
		} else {
			assert (plot.axisOrientation == AxisOrientationSetting.Y_AXIS_AS_TIME);
			// Setup the axis. 
			TimeXYAxis yAxis = new TimeXYAxis(XYDimension.Y);
			if(plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				yAxis.setStart(plot.startTime.getTimeInMillis());
				yAxis.setEnd(plot.endTime.getTimeInMillis());
			} else {
				yAxis.setStart(plot.endTime.getTimeInMillis());
				yAxis.setEnd(plot.startTime.getTimeInMillis());
			}
			plot.setTimeAxis(yAxis);
			LinearXYAxis xAxis = new LinearXYAxis(XYDimension.X);
			if(plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {
				xAxis.setStart(plot.nonTimeVaribleAxisMinValue);
				xAxis.setEnd(plot.nonTimeVaribleAxisMaxValue);
			} else {
				xAxis.setStart(plot.nonTimeVaribleAxisMaxValue);
				xAxis.setEnd(plot.nonTimeVaribleAxisMinValue);
			}
			plot.theNonTimeAxis = xAxis;

			xAxis.setForeground(plot.nonTimeAxisColor);
			yAxis.setForeground(plot.timeAxisColor);
			
			xAxis.setFormat(getNumberFormatter());
			
			xAxis.setPreferredSize(new Dimension(1, 20));
			yAxis.setPreferredSize(new Dimension(60, 1));
			plot.plotView.setXAxis(xAxis);
			plot.plotView.setYAxis(yAxis);
			plot.plotView.add(xAxis);
			plot.plotView.add(yAxis);

			// Setup the axis labels.
			if (plot.isTimeLabelEnabled) {
				SimpleDateFormat format = TimeFormatUtils.makeDataFormat(plot.timeFormatSetting);
                yAxis.setFormat(new DateNumberFormat(format));
                yAxis.setTimeSystemAxisLabelName(timeSystemId);
			} else {
				yAxis.setShowLabels(false);
			}

			xAxis.setFont(plot.timeAxisFont);
			yAxis.setFont(plot.timeAxisFont);

			// Setup the gridlines
			XYGrid grid = new XYGrid(xAxis, yAxis);
			grid.setForeground(plot.gridLineColor);
			plot.plotView.getContents().add(grid);


			xAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			xAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			xAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 2);
			yAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			yAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			yAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 5);

		}	
	}

	void setupScrollFrame() {
		assert plot.plotView !=null : "Plot Object not initalized";

		plot.scrollFrameInitialized = true;

		TimeAxisSubsequentBoundsSetting mode2 = plot.timeAxisSubsequentSetting;
			plot.timeScrollModeByPlotSettings = mode2;
			plot.setTimeAxisSubsequentSetting(mode2);


			boolean nonTimeMinFixed;
			boolean nonTimeMaxFixed;
			// set the Y (non time) scroll mode.		
			if ( plot.nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.AUTO &&
					plot.nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.AUTO) {

				nonTimeMinFixed = false;
				nonTimeMaxFixed = false;

			} else if (plot.nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.AUTO &&
					(plot.nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.FIXED || 
							plot.nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED)) {

				nonTimeMinFixed = false;
				nonTimeMaxFixed = true;

			} else if ((plot.nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.FIXED || 
					plot.nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED) &&
					plot.nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.AUTO) {

				nonTimeMinFixed = true;
				nonTimeMaxFixed = false;

			} else if ((plot.nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.FIXED || 
					plot.nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED) &&
					(plot.nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.FIXED || 
							plot.nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED)) {

				nonTimeMinFixed = true;
				nonTimeMaxFixed = true;

			} else {
				assert false : "Undefined subsquent setting combindation on non time axis " ;
				nonTimeMinFixed = false;
				nonTimeMaxFixed = false;
			}
			plot.setNonTimeMinFixedByPlotSettings(nonTimeMinFixed);
			plot.setNonTimeMaxFixedByPlotSettings(nonTimeMaxFixed);
			plot.setNonTimeMinFixed(nonTimeMinFixed);
			plot.setNonTimeMaxFixed(nonTimeMaxFixed);
		// Initialization now complete.
	}

	void resetNonTimeAxisToOriginalValues() {
		// restore the non time axis scale taking into account axis inversion
		if (plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			XYAxis axis = plot.plotView.getYAxis();
			if (plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				axis.setStart(plot.nonTimeVaribleAxisMinValue);
				axis.setEnd(plot.nonTimeVaribleAxisMaxValue);
			} else {
				axis.setEnd(plot.nonTimeVaribleAxisMinValue);
				axis.setStart(plot.nonTimeVaribleAxisMaxValue);
			}
		} else {
			XYAxis axis = plot.plotView.getXAxis();
			if (plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {	    
				axis.setStart(plot.nonTimeVaribleAxisMinValue);
				axis.setEnd(plot.nonTimeVaribleAxisMaxValue);
			} else {
				axis.setEnd(plot.nonTimeVaribleAxisMinValue);
				axis.setStart(plot.nonTimeVaribleAxisMaxValue);
			}
		}
	}

	void resetTimeAxisToOriginalValues() {
		assert plot.timeVariableAxisMaxValue != plot.timeVariableAxisMinValue;
		// restore the non time axis scale taking into account axis inversion
		if (plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			XYAxis axis = plot.plotView.getXAxis();
			if (plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {	 
				axis.setStart(plot.timeVariableAxisMinValue);
				axis.setEnd(plot.timeVariableAxisMaxValue);
			} else {
				axis.setEnd(plot.timeVariableAxisMinValue);
				axis.setStart(plot.timeVariableAxisMaxValue);
			}
		} else {
			XYAxis axis = plot.plotView.getYAxis();
			if (plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				axis.setStart(plot.timeVariableAxisMinValue);
				axis.setEnd(plot.timeVariableAxisMaxValue);
			} else {
				axis.setEnd(plot.timeVariableAxisMinValue);
				axis.setStart(plot.timeVariableAxisMaxValue);
			}
		}
	}

	/**
	 * Returns true if time axis is inverted, false otherwise. It handles time being on the x or y axis. 
	 */
	boolean isTimeAxisInverted(){
		if (plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			if (plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {	 
				return false;
			} else {
				return true;
			}
		} else {
			if (plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				return false;
			} else {
				return true;
			}
		}
	}

	/**
	 * Returns true if non time axis is inverted, false otherwise. It handles time being on the x or y axis. 
	 */
	boolean isNonTimeAxisInverted(){
		if (plot.axisOrientation == AxisOrientationSetting.Y_AXIS_AS_TIME) {
			if (plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {	 
				return false;
			} else {
				return true;
			}
		} else {
			if (plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				return false;
			} else {
				return true;
			}
		}
	}


	/**
	 * Move plot forwards to current time. If resetSpan is true, it will reset the span of the plot to the original time span of the plot.
	 * If resetSpan is false, the span at the time the method is called will be used. 
	 * 
	 * Logic is dependent upon the plot's time axis subsequent bounds setting.
	 * <ul>
	 * <li>Jump - sets the upper time to current MCT time. Sets the lower time
	 * to the upper time minus the desired span</li>
	 * <li>Scrunch - by definition covers from plot inception to the current mct time. It will therefore
	 * set upper time to the current MCT time and the lower bound to the plot's original lower bound time.</li>
	 * <li>Fixed - sets upper and lower times to those provided at plot creation</li>
	 * </ul>
	 * 
	 * @param resetSpan
	 */
	void fastForwardTimeAxisToCurrentMCTTime(boolean resetSpan) {
		long desiredSpan  = -1;
		long requestMaxTime = -1;
		long requestMinTime = -1;

		if (resetSpan) {
			desiredSpan = plot.timeVariableAxisMaxValue - plot.timeVariableAxisMinValue;
		} else {
			XYAxis axis;
			if(plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
				axis = plot.plotView.getXAxis();
			} else {
				axis = plot.plotView.getYAxis();
			}
			// TODO: Check rounding, or change desiredSpan to a double
			desiredSpan = (long)Math.abs(axis.getEnd() - axis.getStart());
		}

		assert desiredSpan > 0 : "Miscaclulated desired span to be " + desiredSpan;

		if (plot.timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.JUMP) {
			requestMaxTime = plot.plotAbstraction.getCurrentMCTTime();
			requestMinTime = requestMaxTime - desiredSpan;
		} else if (plot.timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			requestMinTime = plot.timeVariableAxisMinValue;
			requestMaxTime = plot.plotAbstraction.getCurrentMCTTime();
		} else {
			assert false : "Unknown time axis subsquent settings mode: " + plot.timeAxisSubsequentSetting;
		requestMaxTime = plot.timeVariableAxisMaxValue;
		requestMinTime = plot.timeVariableAxisMinValue;
		}

		applyMinMaxTimesToPlot(requestMinTime, requestMaxTime);	
	}

	/**
	 * Adjusts the span of the plot to match that specified at plot creation time but does not
	 * fast forward to current time like the companion method @see fastForwardTimeAxisToCurrentMCTTime.
	 * 
	 * Logic is dependent upon the plot's time axis subsequent bounds setting.
	 * <ul>
	 * <li>Jump - sets the upper time to the time plot's current max time. Sets the lower time
	 * to the current plots max time minus the desired span</li>
	 * <li>Scrunch - by definition covers from plot inception to the current mct time. It will therefore
	 * set upper time to the current MCT time and the lower bound to the plot's original lower bound time.</li>
	 * <li>Fixed - sets the upper time to the time plot's current max time. Sets the lower time
	 * to the current plots max time minus the desired span</li>
	 * </ul>
	 *
	 */
	void adjustSpanToDesiredSpanWithoutFastFarwardingToCurrentTime() {
		long desiredSpan  = -1;
		long requestMaxTime = -1;
		long requestMinTime = -1;

		desiredSpan = plot.timeVariableAxisMaxValue - plot.timeVariableAxisMinValue;

		assert desiredSpan > 0 : "Miscaclulated desired span to be " + desiredSpan;

		if (plot.timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.JUMP) {
			requestMaxTime = plot.getTimeAxis().getStartAsLong();
			requestMinTime = requestMaxTime - desiredSpan;
		} else if (plot.timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			requestMinTime = plot.timeVariableAxisMinValue;
			requestMaxTime = plot.plotAbstraction.getCurrentMCTTime();
		} else  {
			assert false : "other modes not supported";
		}
		applyMinMaxTimesToPlot(requestMinTime, requestMaxTime);		
	}

	/**
	 * Sets the time scale start and stop of the plot to the requested min and max time. Method handles axis inversion. 
	 * @param requestMinTime
	 * @param requestMaxTime
	 */
	private void applyMinMaxTimesToPlot(long requestMinTime, long requestMaxTime) {
		boolean normal;
		if(plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			normal = plot.xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
		} else {
			normal = plot.yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
		}
		assert requestMaxTime != requestMinTime;
		TimeXYAxis axis = plot.getTimeAxis();
		if(normal) {	 
			axis.setStart(requestMinTime);
			axis.setEnd(requestMaxTime);
		} else {
			axis.setEnd(requestMinTime);
			axis.setStart(requestMaxTime);
		}
	}


	double getTimeAxisWidthInPixes() {
		Rectangle bounds = plot.plotView.getContents().getBounds();
		if (plot.axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			return bounds.getWidth();
		} else {
			return bounds.getHeight();
		}
	}
}
