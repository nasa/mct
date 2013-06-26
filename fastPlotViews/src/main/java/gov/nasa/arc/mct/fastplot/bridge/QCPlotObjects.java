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
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

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
		assert plot.getPlotView()==null: "Plot already initalized.";

		// Create new instance of the plot 
		plot.setPlotView(new XYPlot());
		plot.getPlotView().setBackground(PlotConstants.DEFAULT_PLOT_FRAME_BACKGROUND_COLOR);
		XYPlotContents contents = new XYPlotContents();
		contents.setBackground(Color.black);
		plot.getPlotView().add(contents);
		plot.getPlotView().setPreferredSize(new Dimension(PlotterPlot.PLOT_PREFERED_WIDTH, PlotterPlot.PLOT_PREFERED_HEIGHT));

		JComponent panel = plot.getPlotPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		layout.setConstraints(plot.getPlotView(), constraints);
		panel.add(plot.getPlotView());
		
		// Setup the plot. 
		// Note: the order of these operations is important as there are dependencies between plot components.
		// Assertion have been added to detect disturbances in the order if the code is changed. 
		// However, care should be taken when editing this code. 

		// Setup the time coordinate base of the plot.
		setupTimeCoordinates();

		// Setup the x- and y-axis.
		setupAxis();

		setupScrollFrame();

		new DefaultXYLayoutGenerator().generateLayout(plot.getPlotView());
	}

	/**
	 * Set up the time coordinates according to the plot settings. This is the feature
	 * that determines axis location and maximum/minimum value locations.
	 */
	private void setupTimeCoordinates() {
		// Set the start/end time boundaries as specified. 
		plot.setStartTime(new GregorianCalendar());
		plot.getStartTime().setTimeInMillis(plot.getMinTime());
		plot.setEndTime(new GregorianCalendar());
		plot.getEndTime().setTimeInMillis(plot.getMaxTime());

		assert(plot.getStartTime()!=null): "Start time should have been initalized by this point.";
		assert(plot.getEndTime() != null): "End time should not have been intialized by this point";
	}

	private NumberFormat getNumberFormatter() {
		return PlotConstants.NON_TIME_FORMAT;
	}
	
	private void setupAxis() {
		assert plot.getPlotView() !=null : "Plot Object not initalized";

		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			// time is on the x-axis.	

			// Setup the axis. 
			TimeXYAxis xAxis = new TimeXYAxis(XYDimension.X);
			plot.setTimeAxis(xAxis);
			if(plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {
				xAxis.setStart(plot.getStartTime().getTimeInMillis());
				xAxis.setEnd(plot.getEndTime().getTimeInMillis());
			} else {
				xAxis.setStart(plot.getEndTime().getTimeInMillis());
				xAxis.setEnd(plot.getStartTime().getTimeInMillis());
			}
			LinearXYAxis yAxis = new LinearXYAxis(XYDimension.Y);
			plot.theNonTimeAxis = yAxis;
			if(plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				yAxis.setStart(plot.getPlotAbstraction().getMinNonTime());
				yAxis.setEnd(plot.getPlotAbstraction().getMaxNonTime());
			} else {
				yAxis.setStart(plot.getPlotAbstraction().getMaxNonTime());
				yAxis.setEnd(plot.getPlotAbstraction().getMinNonTime());
			}
			if(plot.isTimeLabelEnabled) {
				xAxis.setPreferredSize(new Dimension(1, 20));
			} else {
				xAxis.setPreferredSize(new Dimension(1, 10));
			}
			yAxis.setPreferredSize(new Dimension(PlotConstants.Y_AXIS_WHEN_NON_TIME_LABEL_WIDTH , 1));
			
			xAxis.setForeground(plot.getTimeAxisColor());
			yAxis.setForeground(plot.getNonTimeAxisColor());
			yAxis.setFormat(getNumberFormatter());
			plot.getPlotView().setXAxis(xAxis);
			plot.getPlotView().setYAxis(yAxis);
			plot.getPlotView().add(xAxis);
			plot.getPlotView().add(yAxis);

			// Setup the axis labels.
			if (plot.isTimeLabelEnabled) {
				String timeFormatSetting = plot.getTimeFormatSetting();
				SimpleDateFormat format = TimeFormatUtils.makeDataFormat(timeFormatSetting);
				xAxis.setFormat(new DateNumberFormat(format));
				SimpleDateFormat tooltipFormat = TimeFormatUtils.makeDateFormatWithYear(timeFormatSetting);
				xAxis.setToolTipFormat(new DateNumberFormat(tooltipFormat));
			} else {
				xAxis.setShowLabels(false);
			}

			xAxis.setFont(plot.getTimeAxisFont());
			yAxis.setFont(plot.getTimeAxisFont());

			// Setup the gridlines
			XYGrid grid = new XYGrid(xAxis, yAxis);
			grid.setForeground(plot.getGridLineColor());
			plot.getPlotView().getContents().add(grid);

			xAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			xAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			xAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 2);
			yAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			yAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			yAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 5);
		} else {
			assert (plot.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME);
			// Setup the axis. 
			TimeXYAxis yAxis = new TimeXYAxis(XYDimension.Y);
			if(plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				yAxis.setStart(plot.getStartTime().getTimeInMillis());
				yAxis.setEnd(plot.getEndTime().getTimeInMillis());
			} else {
				yAxis.setStart(plot.getEndTime().getTimeInMillis());
				yAxis.setEnd(plot.getStartTime().getTimeInMillis());
			}
			plot.setTimeAxis(yAxis);
			LinearXYAxis xAxis = new LinearXYAxis(XYDimension.X);
			if(plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {
				xAxis.setStart(plot.getPlotAbstraction().getMinNonTime());
				xAxis.setEnd(plot.getPlotAbstraction().getMaxNonTime());
			} else {
				xAxis.setStart(plot.getPlotAbstraction().getMaxNonTime());
				xAxis.setEnd(plot.getPlotAbstraction().getMinNonTime());
			}
			plot.theNonTimeAxis = xAxis;

			xAxis.setForeground(plot.getNonTimeAxisColor());
			yAxis.setForeground(plot.getTimeAxisColor());
			
			xAxis.setFormat(getNumberFormatter());
			
			xAxis.setPreferredSize(new Dimension(1, 20));
			yAxis.setPreferredSize(new Dimension(60, 1));
			plot.getPlotView().setXAxis(xAxis);
			plot.getPlotView().setYAxis(yAxis);
			plot.getPlotView().add(xAxis);
			plot.getPlotView().add(yAxis);

			// Setup the axis labels.
			if (plot.isTimeLabelEnabled) {
				String timeFormatSetting = plot.getTimeFormatSetting();
				SimpleDateFormat format = TimeFormatUtils.makeDataFormat(timeFormatSetting);
				yAxis.setFormat(new DateNumberFormat(format));
				SimpleDateFormat tooltipFormat = TimeFormatUtils.makeDateFormatWithYear(timeFormatSetting);
				yAxis.setToolTipFormat(new DateNumberFormat(tooltipFormat));
			} else {
				yAxis.setShowLabels(false);
			}

			xAxis.setFont(plot.getTimeAxisFont());
			yAxis.setFont(plot.getTimeAxisFont());

			// Setup the gridlines
			XYGrid grid = new XYGrid(xAxis, yAxis);
			grid.setForeground(plot.getGridLineColor());
			plot.getPlotView().getContents().add(grid);


			xAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			xAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			xAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 2);
			yAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
			yAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
			yAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 5);

		}	
	}

	void setupScrollFrame() {
		assert plot.getPlotView() !=null : "Plot Object not initalized";

		TimeAxisSubsequentBoundsSetting mode2 = plot.getTimeAxisSubsequentSetting();
			plot.timeScrollModeByPlotSettings = mode2;
			plot.setTimeAxisSubsequentSetting(mode2);

			// set the Y (non time) scroll mode.		
			boolean nonTimeMinFixed = plot.getNonTimeAxisSubsequentMinSetting() != NonTimeAxisSubsequentBoundsSetting.AUTO;
			boolean nonTimeMaxFixed = plot.getNonTimeAxisSubsequentMaxSetting() != NonTimeAxisSubsequentBoundsSetting.AUTO;;

			plot.setNonTimeMinFixedByPlotSettings(nonTimeMinFixed);
			plot.setNonTimeMaxFixedByPlotSettings(nonTimeMaxFixed);
			plot.setNonTimeMinFixed(nonTimeMinFixed);
			plot.setNonTimeMaxFixed(nonTimeMaxFixed);
		// Initialization now complete.
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

		desiredSpan = plot.getMaxTime() - plot.getMinTime();

		assert desiredSpan > 0 : "Miscaclulated desired span to be " + desiredSpan;

		if (plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.JUMP) {
			requestMaxTime = plot.getTimeAxis().getStartAsLong();
			requestMinTime = requestMaxTime - desiredSpan;
		} else if (plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			requestMinTime = plot.getMinTime();
			requestMaxTime = plot.getPlotAbstraction().getCurrentMCTTime();
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
		boolean normal = (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) ?
				plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT :
				plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
		assert requestMaxTime != requestMinTime;
		AbstractAxis axis = plot.getTimeAxis();
		if(normal) {	 
			axis.setStart(requestMinTime);
			axis.setEnd(requestMaxTime);
		} else {
			axis.setEnd(requestMinTime);
			axis.setStart(requestMaxTime);
		}
	}


}
