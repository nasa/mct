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

import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.Pinnable;

import java.awt.Color;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JPanel;


/**
 * Interface for working with plots. 
 * 
 * Provides the ability to construct plots with configurable layout (axis directions etc.). 
 * Once constructed, requesting the plot panel will return the plot encapsulated in a JPanel
 * for integration into an user interface and presentation to the user. 
 * 
 * Plot configuration is immutable. Datasets may be added, removed, and reconfigured (line styles etc.)
 * 
 * Abstraction in bridge pattern.
 */
public interface PlotAbstraction extends PlotObserver {

	/**
	 * Get instance of the plot wrapped in a JFrame.
	 * @return the plot wrapped in a JFrame.
	 */
	public JPanel getPlotPanel();
	
	/**
	 * Add a data set to the plot.
	 * 
	 * The dataset must not have already been added to the plot.
	 * 
	 * @param dataSetName unique name of the data set.
	 */
	public void addDataSet(String dataSetName);
	
	/**
	 * Add a dataset to the plot.
	 * 
	 * @param dataSetName unique name of the data set.
	 * @param plottingColor desired plotting color of data set.
	 */
	public void addDataSet(String dataSetName, Color plottingColor);
	
	/**
	 * Add a dataset to the plot.
	 * 
	 * @param dataSetName unique name of the data set.
	 * @param displayName the base display name for the data set.
	 */
	public void addDataSet(String dataSetName, String displayName);

	/**
	 * Adds the data per feed Id, timestamp and telemetry value.
	 * @param feedID - feed Id.
	 * @param time - timestamp.
	 * @param value - telemetry value.
	 */
	public void addData(String feedID, long time, double value);

	/**
	 * Determine if data set is defined in plot.
	 * @param setName the dataset name
	 * @return true if the dataset is defined in the plot, false otherwise.
	 */
	public boolean isKnownDataSet(String setName);
	
	/**
	 * Instruct the plot to redraw.
	 */
	public void refreshDisplay();
	

	/**
	 * Update the legend entry for the corresponding data set. 
	 * @param dataSetName the unique name of the data set
	 * @param info the rendering information
	 */
	void updateLegend(String dataSetName, FeedProvider.RenderingInfo info);
	
    /**
     * Return the state of the alarm which indicates if plot has experienced data which is outside
     * of its current non time max axis limit. 
     * @return limit alarm state maximum.
     */
    public LimitAlarmState getNonTimeMaxAlarmState(int subGroupIndex);
    
    /**
     * Return the state of the alarm which indicates if plot has experienced data which is outside
     * of its current non time min axis limit. 
     * @return limit alarm state minimal.
     */
	public LimitAlarmState getNonTimeMinAlarmState(int subGroupIndex);
	
	/**
	 * Return the minimum time currently displayed on the time axis.
	 * @return Gregorian calendar for minimal time.
	 */
	public GregorianCalendar getMinTime();
	
	/**
	 * Return the maximum time currently displayed on the time axis.
	 * @return Gregorian calendar for maximum time.
	 */
	public GregorianCalendar getMaxTime();
	
    /**
     * Return the time axis setting which indicates if time is on the x or y axis.
     * @return axis orientation setting.
     */
	public AxisOrientationSetting getAxisOrientationSetting();
	
	/**
     * Get time system.
     * @return time system
     */
	public String getTimeSystem();

    /**
     * Get time format.
     * @return time format
     */
    public String getTimeFormat();

	/**
	 * Return the x-axis maximum location which indicates if the maximum is on the left or right end of this axis.
	 * @return x-axis maximum location setting.
	 */
	public XAxisMaximumLocationSetting getXAxisMaximumLocation();
	
	/**
	 * Return the y-axis maximum location which indicates if the maximum is at the top or bottom of this axis.
	 * @return y-axis maximum location setting.
	 */
	public YAxisMaximumLocationSetting getYAxisMaximumLocation();
	
	/**
	 * Return whether the ordinal position of each collection should be used to group stacked plots. 
	 * @return true if ordinal position should be used, false if the collection contents should be used.
	 */
	public boolean useOrdinalPositionForSubplots();
	
	/**
	 * Return the plot's mode when data exceeds the current span of the time axis.
	 * @return time axis subsequent bounds setting.
	 */
	public TimeAxisSubsequentBoundsSetting getTimeAxisSubsequentSetting();
	
	/**
	 * Return the plot's mode when data exceeds the current minimum bound of the non time axis.
	 * @return non-time axis subsequent bounds settings minimal.
	 */
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMinSetting();
	
	/**
	 * Return the plot's mode when data exceeds the current maximum bound of the non time axis.
	 * @return non-time axis subsequent bounds settings maximum.
	 */
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMaxSetting();
	
	/**
	 * Return the value specified initially as the non time axis minimum bound.
	 * @return non-time minimal.
	 */
	public double getNonTimeMin();
	
	/**
	 * Return the value specified initially as the non time axis maximum bound.
	 * @return non-time maximum.
	 */
    public double getNonTimeMax();
    
    /**
     * Return the value specified initially as the time axis minimum bound. 
     * @return time minimal.
     */
    public long getTimeMin();
    
    /**
     * Return the value specified initially as the time axis maximum bound. 
     * @return time maximum.
     */
    public long getTimeMax();
    
    /**
     * Return the percentage padding to apply when expanding the time axis.
     * @return time padding.
     */
    public double getTimePadding();
    
    /**
     * Return the percentage padding to apply when expanding the non time axis minimum bound.
     * @return non-time minimal padding.
     */
    public double getNonTimeMinPadding();
    
    /**
     * Return the percentage padding to apply when expanding the non time axis minimum bound.
     * @return non-time maximum padding.
     */
    public double getNonTimeMaxPadding();	
    
    /**
     * Instruct the plot to show a time sync line.
     * @param time at which to show the time sync line.
     */
	public void showTimeSyncLine(GregorianCalendar time);
	
	/**
	 * Instruct the plot to remove any time sync line currently being displayed.
	 */
	public void removeTimeSyncLine();
	
	 /**
     * Return true if the time sync line is visible. False otherwise.
     * @return boolean flag is time sync line visible.
     */
    public boolean isTimeSyncLineVisible();
    
    /**
     * Initialize time Synchronization mode.
     * @param time time at which to synchronize.
     */
    public void initiateGlobalTimeSync(GregorianCalendar time);

    /**
     * Updates the synchronization time.
     * @param time new time to synchronize on.
     */
    public void updateGlobalTimeSync(GregorianCalendar time);

    /**
     * Notify plot manager that synchronization mode has terminated. 
     */
    public void notifyGlobalTimeSyncFinished();
    
    /**
     * Return true if the plot is in time sync mode, false otherwise.
     * @return true if in time sync mode; false otherwise.
     */
    public boolean inTimeSyncMode();
    
    /**
     * Return the largest value shown on the non-time axis currently visible on the plot.
     * @return non-time maximum currently displayed.
     */
    public double getNonTimeMaxCurrentlyDisplayed();
    
    /**
     * Return the smallest value shown on the non-time axis currently visible on the plot.
     * @return non-time minimal currently displayed.
     */
    public double getNonTimeMinCurrentlyDisplayed();
	
   /**
    * Return the underlying plotting package. This method is only to be used for testing. 
    * @return the underlying plotting package.
    */
	AbstractPlottingPackage returnPlottingPackage();
	
	/**
	 * Enable or disable plot data compression.
	 * @param compression enabled flag.
	 */
	public void setCompressionEnabled(boolean compression);
	
	/**
	 * Return true if plot data compression is enabled, false otherwise.
	 * @return true if compression is enabled; false otherwise.
	 */
	public boolean isCompresionEnabled();
	
	/**
	 * Allows the plot package to request a data refresh at the given compression ratio.  
	 * @param startTime of data requested.
	 * @param endTime of data requested.
	 */
	void requestPlotData(GregorianCalendar startTime,GregorianCalendar endTime);

	/**
	 * Inform the plot that a data buffer update event has started.
	 */
	public void informUpdateDataEventStarted();
	
	/**
	 * Inform the plot that a data buffer update event has finished. 
	 */
	public void informUpdateDataEventCompleted();
	
	/**
	 * Inform the plot that a regular data stream update event has started.
	 */
    public void informUpdateFromFeedEventStarted();
	
    /**
	 * Inform the plot that a regular data stream update event has ended.
	 */
	public void informUpdateFromFeedEventCompleted();
	
	/**
	 * Returns the current MCT time.
	 * @return the current MCT time in UNIX timestamp.
	 */
	public long getCurrentMCTTime();

	/**
	 * Return true if plot matches settings, false otherwise.
	 * @param settings the plot settings.
	 * @return true if plot matches the settings; false otherwise.
	 */
	public boolean plotMatchesSetting(PlotSettings settings);
	
	/**
	 * Hold the settings for a plot.
	 */
	public class PlotSettings {
		
		/** Time axis orientation setting. */
		public AxisOrientationSetting timeAxisSetting = null;
		
		/** Time System setting. */
        public String timeSystemSetting = null;

        /** Time Format setting. */
        public String timeFormatSetting =  null;
		
		/** X-axis maximum location setting. */
		public XAxisMaximumLocationSetting xAxisMaximumLocation = null;
		
		/** Y-axis maximum location setting. */
		public YAxisMaximumLocationSetting yAxisMaximumLocation = null;
		
		/** Time axis subsequent bounds settings. */
		public TimeAxisSubsequentBoundsSetting timeAxisSubsequent = null;
		
		/** Non-time axis minimal subsequent bounds setting. */
		public NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting = null;
		
		/** Non-time axis maximum subsequent bounds setting. */
		public NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting = null;
		
		/** Max time in millisecs. */
		public long maxTime = 0;
		
		/** Min time in millisecs. */
		public long minTime = 0;
		
		/** Max non-time value. */
		public double maxNonTime = 0;
		
		/** Min non-time value. */
		public double minNonTime = 0;
		
		/** Time padding value. */
		public double timePadding = 0;       
		
		/** Non-time max padding. */
		public double nonTimeMaxPadding = 0;        
		
		/** Non-time min padding. */
		public double nonTimeMinPadding = 0;    	
		
		/** Ordinal position for stacked plots. Defaults to true. */
		public boolean ordinalPositionForStackedPlots = true;
		
		/** Pin time axis. Defaults to false. */
		public boolean pinTimeAxis = false;
		
		/** Plot line drawing type; line, markers, or both. */
		public PlotLineDrawingFlags plotLineDraw = null;
		
		/** Plot line connection style; direct or step. */
		public PlotLineConnectionType plotLineConnectionType = null;
		
		/**
		 * Checks for time axis orientation setting null.
		 * @return time axis orientation setting null check.
		 */
		public boolean isNull() {
			return timeAxisSetting == null;
		}
	}
	
	/**
	 * Contains settings for specific lines on a plot.
	 */
	public class LineSettings {
		private String  identifier       = "";
		private Integer colorIndex       = 0;
		private Integer thickness        = 1;
		private Integer marker           = 0;
		private String  character        = "";
		private boolean useCharacter     = false;
		private boolean hasRegression    = false;
		private Integer regressionPoints = PlotConstants.NUMBER_REGRESSION_POINTS;
		
		public String getIdentifier() {
			return identifier;
		}
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		public Integer getColorIndex() {
			return colorIndex;
		}
		public void setColorIndex(Integer colorIndex) {
			this.colorIndex = colorIndex;
		}
		public Integer getThickness() {
			return thickness;
		}
		public void setThickness(Integer thickness) {
			this.thickness = thickness;
		}
		public Integer getMarker() {
			return marker;
		}
		public void setMarker(Integer marker) {
			this.marker = marker;
		}
		public String getCharacter() {
			return character;
		}
		public void setCharacter(String character) {
			this.character = character;
		}
		public boolean getUseCharacter() {
			return useCharacter;
		}
		public void setUseCharacter(boolean useCharacter) {
			this.useCharacter = useCharacter;
		}
		public boolean getHasRegression() {
			return hasRegression;
		}
		public void setHasRegression(boolean hasRegression) {
			this.hasRegression = hasRegression;
		}
		public Integer getRegressionPoints() {
			return regressionPoints;
		}
		public void setRegressionPoints(Integer regressionPoints) {
			this.regressionPoints = regressionPoints;
		}
	}

	/**
	 * Instruct plot to remove all its current data.
	 */
	void clearAllDataFromPlot();

	/**
	 * Creates a new pin for this plot.
	 * The plot is pinned if any of its pins are pinned.
	 * @return new pin.
	 */
	public Pinnable createPin();

	/**
	 * Returns the user pin for the time axis.
	 * This is a special pin that the user uses to manually pin the time axis. 
	 * @return manual time axis pin.
	 */
	public Pinnable getTimeAxisUserPin();

	/**
	 * Returns true if the plot is pinned.
	 * The plot is pinned if any of its pins are pinned.
	 * @return true if the plot is pinned.
	 */
	boolean isPinned();

	/**
	 * Returns a list of sub plots on this plot.
	 * The returned list may not be modifiable.
	 * @return sub plots.
	 */
	public List<AbstractPlottingPackage> getSubPlots();

	/**
	 * Returns the time axis.
	 * @return the time axis.
	 */
	public Axis getTimeAxis();

	/**
	 * Updates the visibility of the corner reset buttons based on which axes are pinned and what data is visible.
	 */
	public void updateResetButtons();

	/**
	 * Sets the X-Y time axis.
	 * @param axis X-Y time axis
	 */
	public void setPlotTimeAxis(TimeXYAxis axis);
	
	/**
	 * Get the drawing mode (lines, markers, both) associated with this plot.
	 * @return the drawing mode 
	 */
	public PlotLineDrawingFlags getPlotLineDraw();
	
	/**
	 * Get the connection type (direct, or some form of step) used to connect 
	 * data points on a plot. 
	 * @return the method for connecting points on this plot
	 */
	public PlotLineConnectionType getPlotLineConnectionType();
	
	/**
	 * Set the drawing mode (lines, markers, both) for this plot
	 * @param draw the drawing mode
	 */
	public void setPlotLineDraw(PlotLineDrawingFlags draw);
	
	/**
	 * Set the line connection type (direct, or some form of step) used to 
	 * connect data point on this plot.
	 * @param type the method for connecting points on this plot
	 */
	public void setPlotLineConnectionType(PlotLineConnectionType type);
	
}
