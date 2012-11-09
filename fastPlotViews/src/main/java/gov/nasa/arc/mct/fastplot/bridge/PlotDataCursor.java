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
import gov.nasa.arc.mct.fastplot.utils.TimeFormatUtils;
import gov.nasa.arc.mct.fastplot.view.Pinnable;

import java.awt.Dimension;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.SpringLayout;

import plotter.TimeSystemFormattedLabel;
import plotter.xy.SlopeLine;
import plotter.xy.SlopeLineDisplay;
import plotter.xy.XYLocationDisplay;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

/**
 * Manages the movement of the mouse pointer within a plot to show the X,Y data cursor and respond to mouse clicks 
 * to show the slope line. Includes the math functions to calculate the slope line. 
 * 
 * The X,Y data cursor runs off mouseMoved events.
 * 
 * The slope line is initiated with a mousePress, moves with mouseDragged, and ends with a mouseReleased event. 
 */
class PlotDataCursor {
	private final static String HTML_WHITESPACES = "&nbsp;&nbsp;&nbsp;";
	
	@SuppressWarnings("serial")
	private static final NumberFormat TIME_SPAN_FORMAT = new NumberFormat() {

		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			toAppendTo.append(formatTime((long) number));
			return toAppendTo;
		}


		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			toAppendTo.append(formatTime(number));
			return toAppendTo;
		}


		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			throw new RuntimeException("Not implemented");
		}
	};

	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(PlotDataCursor.class.getName().substring(0, 
        		       PlotDataCursor.class.getName().lastIndexOf("."))+".Bundle");

	private PlotterPlot parentPlot;

	private SimpleDateFormat dateFormat;
	private XYLocationDisplay pointerXYValueLabel = new XYLocationDisplay();
	private TimeSystemFormattedLabel timeSystemFormattedLabel = new TimeSystemFormattedLabel();
	
	private SlopeLineDisplay slopeLabel = new SlopeLineDisplay();
	private SlopeLine slopeLine; // TODO: Use one SlopeLine across multiple plots
	private boolean slopeLineEnabled = false;
	private Pinnable pin;

	PlotDataCursor (PlotterPlot plot)  {
		parentPlot = plot;
		dateFormat = TimeFormatUtils.makeDataFormat(parentPlot.getTimeFormatSetting());
		
		if (plot.getTimeSystemSetting() != null) {
            timeSystemFormattedLabel.setTimeSystemAxisLabelName(parentPlot.getTimeSystemSetting());
		}

		setupTimeSystemLabel();
		setupXYDisplay();
		setupMarqueeZoom();
		setupSlopeLineDisplay();
		pin = plot.getPlotAbstraction().createPin();
	}
	
	private void setupMarqueeZoom() {
		MarqueeZoomListener marqueeZoomListener = new MarqueeZoomListener(parentPlot.localControlsManager, parentPlot.getPlotView(), dateFormat, parentPlot.getTimeAxisFont());
		parentPlot.getPlotView().getContents().addMouseListener(marqueeZoomListener);
		parentPlot.getPlotView().getContents().addMouseMotionListener(marqueeZoomListener);
	}

	private void setupTimeSystemLabel() {
		timeSystemFormattedLabel.setSize((int) timeSystemFormattedLabel.getPreferredSize().getWidth(),
				(int) timeSystemFormattedLabel.getPreferredSize().getHeight());
		timeSystemFormattedLabel.setFont(parentPlot.getTimeAxisFont());
		timeSystemFormattedLabel.setForeground(PlotConstants.DATA_CURSOR_COLOR);

		MessageFormat format = new MessageFormat("<html><body style=\"white-space:nowrap\"><B>" + HTML_WHITESPACES + timeSystemFormattedLabel.getTimeSystemAxisLabelName()
				+ HTML_WHITESPACES
				+ "</B></body></html>");
		timeSystemFormattedLabel.setFormat(format);
	}
	
	/**
	 * Setup the mouse position x,y label that will be positioned at the top of the plot. 
	 */
	private void setupXYDisplay() {
		pointerXYValueLabel.setSize((int) pointerXYValueLabel.getPreferredSize().getWidth(), 
				(int) pointerXYValueLabel.getPreferredSize().getHeight());
		pointerXYValueLabel.setFont(parentPlot.getTimeAxisFont());
		pointerXYValueLabel.setForeground(PlotConstants.DATA_CURSOR_COLOR);
		pointerXYValueLabel.attach(parentPlot.getPlotView());
		
		if (parentPlot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			MessageFormat format = new MessageFormat("<html><body style=\"white-space:nowrap\"><B>(X:</B> {0}" + HTML_WHITESPACES + "<B>Y:</B> {1})</body></html>");
			format.setFormatByArgumentIndex(0, dateFormat);
			format.setFormatByArgumentIndex(1, PlotConstants.NON_TIME_FORMAT);
			pointerXYValueLabel.setFormat(format);
		} else {
			MessageFormat format = new MessageFormat("<html><body style=\"white-space:nowrap\"><B>(Y:</B> {1}" + HTML_WHITESPACES + "<B>X:</B> {0})</body></html>");
			format.setFormatByArgumentIndex(0, PlotConstants.NON_TIME_FORMAT);
			format.setFormatByArgumentIndex(1, dateFormat);
			pointerXYValueLabel.setFormat(format);
		}
		
		// This sets the preferred height to the normal height so the component doesn't collapse to height 0 when the text is empty.
		// Note that mimimumSize does not work for some reason.
		pointerXYValueLabel.setText("Ag");
		Dimension size = pointerXYValueLabel.getPreferredSize();
		size.width = 100;
		pointerXYValueLabel.setText("");
		pointerXYValueLabel.setPreferredSize(size);

		XYPlot plot = parentPlot.getPlotView();
		XYPlotContents contents = plot.getContents();
		plot.add(pointerXYValueLabel);
		plot.add(timeSystemFormattedLabel);
		
		SpringLayout layout2 = (SpringLayout) plot.getLayout();
        layout2.putConstraint(SpringLayout.NORTH, pointerXYValueLabel, 0, SpringLayout.NORTH, plot);
        layout2.putConstraint(SpringLayout.WEST, pointerXYValueLabel, 0, SpringLayout.WEST, contents);
        layout2.putConstraint(SpringLayout.WEST, timeSystemFormattedLabel, 0, SpringLayout.WEST, contents);
        layout2.putConstraint(SpringLayout.NORTH, timeSystemFormattedLabel, 0, SpringLayout.NORTH, plot);
        layout2.putConstraint(SpringLayout.WEST, pointerXYValueLabel, 10, SpringLayout.EAST, timeSystemFormattedLabel);
        
        // Pick the tallest label to lay out the plot contents against
        JComponent top = (pointerXYValueLabel.getPreferredSize().height > timeSystemFormattedLabel.getPreferredSize().height) ?
        		pointerXYValueLabel : timeSystemFormattedLabel;
        layout2.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.SOUTH, top);        
        plot.getYAxis().setEndMargin(top.getPreferredSize().height);
	}
	/**
	 * Setup the slope dx, dy label that will be positioned at the top of the plot
	 */
	@SuppressWarnings("serial")
	private void setupSlopeLineDisplay() {
		slopeLine = new SlopeLine();
		slopeLine.attach(parentPlot.getPlotView());
		slopeLine.addListenerForPlot(parentPlot.getPlotView(), slopeLabel);
		slopeLine.addListenerForPlot(parentPlot.getPlotView(), new SlopeLine.Listener() {
			@Override
			public void slopeLineUpdated(SlopeLine line, XYPlot plot, double arg2, double arg3, double arg4, double arg5) {
				// ignore
			}
			
		
			@Override
			public void slopeLineRemoved(SlopeLine line, XYPlot plot) {
				pin.setPinned(false);
			}
			
		
			@Override
			public void slopeLineAdded(SlopeLine line, XYPlot plot, double arg2, double arg3) {
				pin.setPinned(true);
			}
		});
		slopeLine.setForeground(PlotConstants.DATA_CURSOR_COLOR);
		slopeLabel.setFont(parentPlot.getTimeAxisFont());
		slopeLabel.setForeground(PlotConstants.DATA_CURSOR_COLOR);

		if (parentPlot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			MessageFormat format = new MessageFormat("<html><body style=\"white-space:nowrap\"><B>&#916;X:</B> {0}" + HTML_WHITESPACES 
					+ "<B>&#916;Y:</B> {1}" + HTML_WHITESPACES + "<B>"
					+ BUNDLE.getString("Slope.label") + ":</B> {2}" + PlotConstants.SLOPE_UNIT + "</body></html>");
			format.setFormatByArgumentIndex(0, TIME_SPAN_FORMAT);
			format.setFormatByArgumentIndex(1, PlotConstants.NON_TIME_FORMAT);
			format.setFormatByArgumentIndex(2, new NumberFormat() {
				@Override
				public Number parse(String source, ParsePosition parsePosition) {
					throw new RuntimeException("Not implemented");
				}


				@Override
				public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
					toAppendTo.append(PlotConstants.NON_TIME_FORMAT.format(number * PlotConstants.SLOPE_UNIT_DIVIDER_IN_MS));
					return toAppendTo;
				}


				@Override
				public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
					toAppendTo.append(PlotConstants.NON_TIME_FORMAT.format(number * PlotConstants.SLOPE_UNIT_DIVIDER_IN_MS));
					return toAppendTo;
				}
			});
			slopeLabel.setFormat(format);
		} else {
			MessageFormat format = new MessageFormat("<html><body style=\"white-space:nowrap\"><B>&#916;Y:</B> {0}" 
					+ HTML_WHITESPACES + "<B>&#916;X:</B> {1}" + HTML_WHITESPACES + "<B>"
					+ BUNDLE.getString("Slope.label") + ":</B> {2}" + PlotConstants.SLOPE_UNIT + "</body></html>");
			format.setFormatByArgumentIndex(0, PlotConstants.NON_TIME_FORMAT);
			format.setFormatByArgumentIndex(1, TIME_SPAN_FORMAT);
			format.setFormatByArgumentIndex(2, new NumberFormat() {
				@Override
				public Number parse(String source, ParsePosition parsePosition) {
					throw new RuntimeException("Not implemented");
				}


				@Override
				public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
					toAppendTo.append(PlotConstants.NON_TIME_FORMAT.format(PlotConstants.SLOPE_UNIT_DIVIDER_IN_MS / (double) number));
					return toAppendTo;
				}


				@Override
				public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
					toAppendTo.append(PlotConstants.NON_TIME_FORMAT.format(PlotConstants.SLOPE_UNIT_DIVIDER_IN_MS / number));
					return toAppendTo;
				}
			});
			slopeLabel.setFormat(format);
		}
		
		// Sets the preferred height to the normal height so the component doesn't collapse to height 0 when the text is empty.
        // Note that mimimumSize does not work for some reason.
		slopeLabel.setText("Ag");
		Dimension size = slopeLabel.getPreferredSize();
		size.width = 100;
		slopeLabel.setText("");
		slopeLabel.setPreferredSize(size);

		XYPlot plot = parentPlot.getPlotView();
		plot.add(slopeLabel);
		XYPlotContents contents = plot.getContents();
		SpringLayout layout2 = (SpringLayout) plot.getLayout();
		layout2.putConstraint(SpringLayout.NORTH, slopeLabel, 0, SpringLayout.NORTH, plot);
        layout2.putConstraint(SpringLayout.EAST, slopeLabel, 0, SpringLayout.EAST, contents);
        layout2.putConstraint(SpringLayout.WEST, timeSystemFormattedLabel, 0, SpringLayout.WEST, contents);
        layout2.putConstraint(SpringLayout.NORTH, timeSystemFormattedLabel, 0, SpringLayout.NORTH, plot);
        layout2.putConstraint(SpringLayout.WEST, pointerXYValueLabel, 10, SpringLayout.EAST, timeSystemFormattedLabel);

        // Pick the tallest label to lay out the plot contents against
        JComponent top = (pointerXYValueLabel.getPreferredSize().height > timeSystemFormattedLabel.getPreferredSize().height) ?
        		pointerXYValueLabel : timeSystemFormattedLabel;
        layout2.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.SOUTH, top);        
        plot.getYAxis().setEndMargin(top.getPreferredSize().height);        
	}

	
	static String formatTime(long timeDelta) {
		String sign = "+";
		if (timeDelta < 0) {
			sign = "-";
			timeDelta = timeDelta * -1;
		}
		
		// build the time string
		StringBuilder timeString = new StringBuilder();

	    long days =  timeDelta / PlotConstants.MILLISECONDS_IN_DAY;
		long remainder = timeDelta % PlotConstants.MILLISECONDS_IN_DAY;
		long hours = remainder / PlotConstants.MILLISECONDS_IN_HOUR;
		remainder = remainder % PlotConstants.MILLISECONDS_IN_HOUR;
		long mins = remainder / PlotConstants.MILLISECONDS_IN_MIN;
		remainder = remainder % PlotConstants.MILLISECONDS_IN_MIN;
		long seconds = remainder / PlotConstants.MILLISECONDS_IN_SECOND;
		remainder = remainder % PlotConstants.MILLISECONDS_IN_SECOND;
		
        timeString.append(sign);
        timeString.append(days);
        timeString.append("/");
        if (hours < 10) {
        	timeString.append("0");
        }
        timeString.append(hours);
        timeString.append(":");
        if (mins < 10) {
        	timeString.append("0");
        }
        timeString.append(mins);
        timeString.append(":");
        if (seconds < 10) {
        	timeString.append("0");
        }
        timeString.append(seconds);
        return timeString.toString();
	}
	public boolean isSlopeLineEnabled() {
		return slopeLineEnabled;
	}
}