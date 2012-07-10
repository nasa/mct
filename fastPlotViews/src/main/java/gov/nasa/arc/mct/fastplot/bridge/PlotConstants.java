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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;
import java.text.NumberFormat;



public class PlotConstants {	
	/*
	 * Default Plot Properties.
	 */
	public static final int DEFAULT_NUMBER_OF_SUBPLOTS = 1;
	public static final boolean LOCAL_CONTROLS_ENABLED_BY_DEFAULT = true;
	public static final YAxisMaximumLocationSetting DEFAULT_Y_AXIS_MAX_LOCATION_SETTING = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
	public static final NonTimeAxisSubsequentBoundsSetting DEFAULT_NON_TIME_AXIS_MIN_SUBSEQUENT_SETTING  = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
	public static final NonTimeAxisSubsequentBoundsSetting DEFAULT_NON_TIME_AXIS_MAX_SUBSEQUENT_SETTING = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;

	public static final int MILLISECONDS_IN_SECOND = 1000;
	public static final int MILLISECONDS_IN_MIN = MILLISECONDS_IN_SECOND * 60;
	public static final int MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MIN * 60;
	public static final int MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24;
	
	public static final int DEFAUlT_PLOT_SPAN = 30 * 60 * 1000; // 30 mins in Milliseconds
	public static final Color ROLL_OVER_PLOT_LINE_COLOR = Color.white;
	public static final int DEFAULT_TIME_AXIS_FONT_SIZE = 10;
	public static final Font DEFAULT_TIME_AXIS_FONT = new Font("Arial", Font.PLAIN, DEFAULT_TIME_AXIS_FONT_SIZE);
	public static final int DEFAULT_PLOTLINE_THICKNESS = 1;
	public static final int SELECTED_LINE_THICKNESS = 2;
	public static final Color DEFAULT_PLOT_FRAME_BACKGROUND_COLOR = new Color(51, 51, 51);
	public static final Color DEFAULT_PLOT_AREA_BACKGROUND_COLOR = Color.black;
	public static final int DEFAULT_TIME_AXIS_INTERCEPT = 0;
	public static final Color DEFAULT_TIME_AXIS_COLOR = Color.white;
	public static final Color DEFAULT_TIME_AXIS_LABEL_COLOR = Color.white;

	public static final Color DEFAULT_NON_TIME_AXIS_COLOR= Color.white;
	public static final Color DEFAULT_GRID_LINE_COLOR = Color.LIGHT_GRAY;
	public static final int DEFAULT_MIN_SAMPLES_FOR_AUTO_SCALE = 0;
	public static final double DEFAULT_TIME_AXIS_PADDING = 0.25;
	public static final double DEFAULT_TIME_AXIS_PADDING_JUMP_MIN = 0.05;
	public static final double DEFAULT_TIME_AXIS_PADDING_JUMP_MAX = 0.25;
	public static final double DEFAULT_TIME_AXIS_PADDING_SCRUNCH_MIN = 0.20;
	public static final double DEFAULT_TIME_AXIS_PADDING_SCRUNCH_MAX = 0.25;
	public static final double DEFAULT_NON_TIME_AXIS_PADDING_MAX  = 0.05;
	public static final double DEFAULT_NON_TIME_AXIS_PADDING_MIN  = 0.05;
	public static final double DEFAULT_NON_TIME_AXIS_MIN_VALUE  = 0;
	public static final double DEFAULT_NON_TIME_AXIS_MAX_VALUE = 1;
	public static final long DEFAULT_TIME_AXIS_MIN_VALUE = new GregorianCalendar().getTimeInMillis();
	public static final long DEFAULT_TIME_AXIS_MAX_VALUE= DEFAULT_TIME_AXIS_MIN_VALUE  + DEFAUlT_PLOT_SPAN;
	public static final int  MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT = 30;
	public static final int  MAX_NUMBER_SUBPLOTS = 10;
	public static final PlotLineDrawingFlags DEFAULT_PLOT_LINE_DRAW = new PlotLineDrawingFlags(true, false);
	
	public static final int MAJOR_TICK_MARK_LENGTH = 3;
	public static final int MINOR_TICK_MARK_LENGTH = 1;
	
	public static final String GMT = "GMT";
	public static final String DEFAULT_TIME_ZONE = GMT;
	public static final String DEFAULT_TIME_SYSTEM = GMT;
	public static final String DEFAULT_TIME_AXIS_DATA_FORMAT = "DDD/HH:mm:ss"; // add a z to see the time zone.
	public static String DEFAULT_TIME_FORMAT = DEFAULT_TIME_AXIS_DATA_FORMAT;
	
	// Field names for persistence
	public static final String TIME_SYSTEM_SETTING = "TimeSystem";
    public static final String TIME_FORMAT_SETTING = "TimeFormat";
	public static final String TIME_AXIS_SETTING = "PlotTimeAxisSetting";
	public static final String X_AXIS_MAXIMUM_LOCATION_SETTING = "PlotXAxisMaximumLocation";
	public static final String Y_AXIS_MAXIMUM_LOCATION_SETTING = "PlotYAxisMaximumLocation";
	public static final String TIME_AXIS_SUBSEQUENT_SETTING = "PlotTimeAxisSubsequentSetting";
	public static final String NON_TIME_AXIS_SUBSEQUENT_MIN_SETTING = "PlotNonTimeAxisSubsequentMinSetting";
	public static final String NON_TIME_AXIS_SUBSEQUENT_MAX_SETTING = "PlotNonTimeAxisSubsequentMaxSetting";
	public static final String NON_TIME_MAX = "NonTimeMax";
	public static final String NON_TIME_MIN = "NonTimeMin";
	public static final String TIME_MAX = "TimeMax";
	public static final String TIME_MIN = "TimeMin";
	public static final String TIME_PADDING = "TimePadding";
	public static final String NON_TIME_MIN_PADDING = "NonTimeMinPadding";
	public static final String NON_TIME_MAX_PADDING = "NonTimeMaxPadding";
	public static final String GROUP_BY_ORDINAL_POSITION = "GroupByOrdinalPosition";
	public static final String PIN_TIME_AXIS = "PinTimeAxis";
	public static final String DRAW_LINES = "PlotLineDrawLines";
	public static final String DRAW_MARKERS = "PlotLineDrawMarkers";
	public static final String DRAW_CHARACTERS = "PlotLineDrawCharacters";
	public static final String CONNECTION_TYPE = "PlotLineConnectionType";
	public static final String COLOR_ASSIGNMENTS = "PlotColorAssignments";
	public static final String LINE_SETTINGS = "PlotLineSettings";

	// Delay before firing a request for data at a higher resolution on a window. 
	public final static int RESIZE_TIMER = 200; // in milliseconds.
	
	// Limit button border settings
    public static final int ARROW_BUTTON_BORDER_STYLE_TOP = 1;
    public static final int ARROW_BUTTON_BORDER_STYLE_LEFT = 0;
    public static final int ARROW_BUTTON_BORDER_STYLE_BOTTOM = 0;
    public static final int ARROW_BUTTON_BORDER_STYLE_RIGHT = 0;
	
    // The size below which the plot will not go before it starts to truncate the legends. 
    public static final int MINIMUM_PLOT_WIDTH = 200; //200;
    public static final int MINIMUM_PLOT_HEIGHT = 100;
    public static final int Y_AXIS_WHEN_NON_TIME_LABEL_WIDTH = 28;
    
    // Legends
    public final static Color LEGEND_BACKGROUND_COLOR =  DEFAULT_PLOT_FRAME_BACKGROUND_COLOR;
    public static final int PLOT_LEGEND_BUFFER = 5;
    
    public static final int PLOT_LEGEND_WIDTH = 120; 
    public static final int PLOT_MINIMUM_LEGEND_WIDTH = 40;
	public static final int PLOT_LEGEND_OFFSET_FROM_LEFT_HAND_SIDE = 0;
	
	public static final String LEGEND_NEWLINE_CHARACTER = "\n";
	public static final String LEGEND_ELLIPSES = "...";    
	public static final int MAXIMUM_LEGEND_TEXT_SIZE = 20; //maximum width of a legend 
	
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.000");
	
	// Sync line
	public static final Color TIME_SYNC_LINE_COLOR = Color.orange;
    public static final int TIME_SYNC_LINE_WIDTH = 2;
    public static final int SYNC_LINE_STYLE = 9; // ChartConstants.LS_DASH_DOT;
    
    public static final int SHIFT_KEY_MASK = InputEvent.SHIFT_MASK;
    public static final int ALT_KEY_MASK = InputEvent.ALT_MASK;
    public static final int CTL_KEY_MASK = InputEvent.CTRL_MASK;
      
    // Data Cursor
    public static final Color DATA_CURSOR_COLOR =  new Color(235, 235, 235);//new Color(51, 102, 153);
    public static final int SLOPE_LINE_STYLE = 0; // ChartConstants.LS_SOLID;
    public static final int SLOPE_LINE_WIDTH = 1;
    public static final String SLOPE_UNIT = "/min";
    public static final String REGRESSION_LINE = "RegressionLine";
    public static final int NUMBER_REGRESSION_POINTS = 20;
    public static final int SLOPE_UNIT_DIVIDER_IN_MS = PlotConstants.MILLISECONDS_IN_MIN;  // per second. 
    public final static float dash1[] = {10.0f};
    
    // Data Compression
    // Sets the default value for data compression which can be overridden by the client.
    public static final boolean COMPRESSION_ENABLED_BY_DEFAULT = true;
    
    public static final int MAXIMUM_PLOT_DATA_BUFFER_SLIZE_REQUEST_SIZE = 12 * MILLISECONDS_IN_HOUR ;

    // Panning and zooming controls
    public static final double PANNING_NON_TIME_AXIS_PERCENTAGE = 25;
    public static final double PANNING_TIME_AXIS_PERCENTAGE = 25;
    public static final double ZOOMING_NON_TIME_AXIS_PERCENTAGE = 10;
    public static final double ZOOMING_TIME_AXIS_PERCENTAGE = 10;
    
    public static final int zoomingTimeAxisIncrementInMiliseconds = 30 * MILLISECONDS_IN_SECOND;
    public static final int zoomingNonTimeAxisIncrement = 10;
    
    public static final int LOCAL_CONTROL_HEIGHT = 25;
    public static final int LOCAL_CONTROL_WIDTH = 28;
    
    /** Use scientific notations to the thousandth decimal places. */
    public final static String SCIENTIFIC_NUMBER_FORMAT = "0.000E0";
    public final static DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat(SCIENTIFIC_NUMBER_FORMAT);
    public final static double MILLION_VALUES = 1E6;
    public final static double NEGATIVE_MILLION_VALUES = -MILLION_VALUES;

    
    /**
     * Orientation of the time axis. 
     */
	public enum AxisOrientationSetting {
		X_AXIS_AS_TIME, Y_AXIS_AS_TIME
	}

	public enum AxisBounds {
		MAX, MIN
	}
	public enum XAxisMaximumLocationSetting {
		MAXIMUM_AT_RIGHT, MAXIMUM_AT_LEFT
	}

	public enum YAxisMaximumLocationSetting {
		MAXIMUM_AT_TOP, MAXIMUM_AT_BOTTOM
	}

	/**
	 * Subsequent modes on the time axis. 
	 */
	public enum TimeAxisSubsequentBoundsSetting {
		JUMP, SCRUNCH
	}

	/**
	 * Subsequent modes on the non-time axis
	 */
	public enum NonTimeAxisSubsequentBoundsSetting {
		AUTO, FIXED, SEMI_FIXED	
	}

	/**
	 * State that limit alarms can be in. 
	 */
	public enum LimitAlarmState{
		NO_ALARM, ALARM_RAISED, ALARM_OPENED_BY_USER, ALARM_CLOSED_BY_USER
	}
	
	/**
	 * Panning actions
	 */
	public enum PanDirection {
		PAN_LOWER_X_AXIS, PAN_HIGHER_X_AXIS, PAN_LOWER_Y_AXIS, PAN_HIGHER_Y_AXIS;
	}
	
	/**
	 * Zoom actions
	 */
	public enum ZoomDirection {
		ZOOM_IN_HIGH_Y_AXIS, ZOOM_OUT_HIGH_Y_AXIS, 
		ZOOM_IN_CENTER_Y_AXIS, ZOOM_OUT_CENTER_Y_AXIS, 
		ZOOM_IN_LOW_Y_AXIS, ZOOM_OUT_LOW_Y_AXIS,
		ZOOM_IN_LEFT_X_AXIS, ZOOM_OUT_LEFT_X_AXIS, 
		ZOOM_IN_CENTER_X_AXIS, ZOOM_OUT_CENTER_X_AXIS, 
		ZOOM_IN_RIGHT_X_AXIS, ZOOM_OUT_RIGHT_X_AXIS;
	}
	
	public enum AxisType {
		TIME_IN_JUMP_MODE (DEFAULT_TIME_AXIS_PADDING_JUMP_MIN, 
							DEFAULT_TIME_AXIS_PADDING_JUMP_MAX), 
		TIME_IN_SCRUNCH_MODE (DEFAULT_TIME_AXIS_PADDING_SCRUNCH_MIN, 
							DEFAULT_TIME_AXIS_PADDING_SCRUNCH_MAX),
		NON_TIME (DEFAULT_NON_TIME_AXIS_PADDING_MIN, 
					DEFAULT_NON_TIME_AXIS_PADDING_MAX);
		
		private final double minimumDefaultPadding;
		private final double maximumDefaultPadding;
		
		AxisType(double minPadding, double maxPadding) {
			this.minimumDefaultPadding = minPadding;
			this.maximumDefaultPadding = maxPadding;
		}

		public double getMinimumDefaultPadding() {
			return minimumDefaultPadding;
		}
		
		public String getMinimumDefaultPaddingAsText() {
			String percentString = NumberFormat.getPercentInstance().format(this.minimumDefaultPadding);
			return percentString.substring(0, percentString.length()-1);
		}

		public double getMaximumDefaultPadding() {
			return maximumDefaultPadding;
		}
		
		public String getMaximumDefaultPaddingAsText() {
			String percentString = NumberFormat.getPercentInstance().format(this.maximumDefaultPadding);
			return percentString.substring(0, percentString.length()-1);
		}
	}
	
	/**
	 * DISPLAY_ONLY optimizes the plot buffering for displaying multiple plots with the minimum buffer wait. 
	 * Switching to USER_INTERACTION mode deepens and widens the plot buffer to support user interactions such
	 * as panning and zooming.
	 */
	public enum PlotDisplayState {
		DISPLAY_ONLY, USER_INTERACTION;
	}
	
	/**
	 * Indicates whether we will be drawing plot lines, point markers, or both.
	 */
	public static class PlotLineDrawingFlags {
		private boolean line, markers;
		
		public PlotLineDrawingFlags(boolean line, boolean markers) {
			this.line = line;
			this.markers = markers;
		}
		
		public boolean drawLine() {
			return line;
		}
		
		public boolean drawMarkers() {
			return markers;
		}
				
	}
	
	/**
	 * Indicates how to connect plot point with lines.
	 */
	public enum PlotLineConnectionType {
		DIRECT, STEP_X_THEN_Y
	}
	
	/**
	 * Params for Labeling Algorithm
	 */
	/**
	 * The regular expression defining the delimiter pattern between words.
	 * Words are delimited by a sequence of one or more spaces or underscores.
	 */
	public static final String WORD_DELIMITERS = "[ _]+";
	
	/**
	 * The compiled regular expression defining the delimiter pattern between
	 * words.
	 */
	public static final Pattern WORD_DELIMITER_PATTERN = Pattern.compile(WORD_DELIMITERS);
	
	/**
	 * The separator to use when concatenating words together to form labels.
	 */
	public static final String WORD_SEPARATOR = " ";

	/**
	 * The maximum thickness for a plot line's stroke
	 */
	public static final int MAX_LINE_THICKNESS = 5;
	
}
