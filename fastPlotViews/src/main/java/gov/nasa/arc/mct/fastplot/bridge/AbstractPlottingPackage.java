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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;

import java.awt.Color;
import java.awt.Font;
import java.util.GregorianCalendar;
import java.util.SortedMap;

import javax.swing.JComponent;


/**
 * General interface to plotting packages. An implementation must be provided for each package.
 * 
 * Implementor in bridge pattern.
 */
public interface AbstractPlottingPackage extends PlotSubject, PlotConfiguration {
	
   /**
    * Construct a plot
    * @param theAxisOrientation the axis on which time appears
    * @param theTimeSystem the time system
    * @param theTimeFormat the time format
    * @param theXAxisSetting the location of the max and minimum points on the x-axis
    * @param theYAxisSetting the location of the max and minimum points on the y-axis
    * @param theTimeAxisSubsequentSetting specify how the time axis behaves after it is initially setup
    * @param theNonTimeAxisMinSubsequentSetting specify how the non time axis lower minimum point behaves after it is initially setup
    * @param theNonTimeAxisMaxSubsequentSetting specify how the non time axis maximum point behaves after it is initially setup
    * @param timeAxisFont the font of the time axis labels
    * @param plotLineThickness the thickness of the plot lines
    * @param plotBackgroundFrameColor color of the frame outside of the plot
    * @param plotAreaBackgroundColor color of the background of the plot
    * @param timeAxisIntercept the point at which the time axis intercepts the non time axis
    * @param timeAxisColor color of the time axis
    * @param timeAxisLabelColor color of the labels on the time axis
    * @param nonTimeAxisLabelColor color of labels on the non-time axis
    * @param timeAxisDataFormat format of the time axis labels
    * @param nonTimeAxisColor color of the non time axis
    * @param gridLineColor color of the grid lines
    * @param minSamplesForAutoScale
    * @param scrollRescaleTimeMargine padding percentage time axis
    * @param scrollRescaleNonTimeMinMargine padding percentage non time axis min end
    * @param scrollRescaleNonTimeMaxMargine padding percentage non time axis max end
    * @param theNonTimeVaribleAxisMinValue initial minimum non time axis value
    * @param theNonTimeVaribleAxisMaxValue initial maximum non time axis value
    * @param theTimeVariableAxisMinValue initial minimum time axis value
    * @param theTimeVariableAxisMaxValue initial maximum time axis value
    * @param isCompressionEnabled true if plot should compress data to match the pixel resolution. False if it should not. 
    * @param isTimeLabelsEnabled true if time labels enabled; otherwise false.
    * @param isLocalControlEnabled true if local control enabled; otherwise false.
    * @param ordinalPositionInStackedPlot true if ordinal position in stacked plot; otherwise false.
    * @param plotLineDraw indicates how to draw the plot (whether to include lines, markers, etc)
	* @param plotLineConnectionType the method of connecting lines on the plot
    * @param thePlotAbstraction plotAbstraction side of the bridge. 
    * @param thePlotLabelingAlgorithm the plot labeling abbreviation algorithm.
    */
	public void createChart(Font timeAxisFont, 
			    			int plotLineThickness,
			                Color plotBackgroundFrameColor, 
			                Color plotAreaBackgroundColor,
			                int timeAxisIntercept, 
			                Color timeAxisColor, 
			                Color timeAxisLabelColor, 
			                Color nonTimeAxisLabelColor,
			                String timeAxisDataFormat, 
			                Color nonTimeAxisColor, 
			                Color gridLineColor,
			                int minSamplesForAutoScale, 
			    			boolean isCompressionEnabled,
			    			boolean isTimeLabelsEnabled,
			    			boolean isLocalControlEnabled,
			    			PlotAbstraction thePlotAbstraction,
			    			AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm); 
	
	
	/**
	 * Get instance of the plot wrapped in a JFrame.
	 * @return the plot wrapped in a JFrame.
	 */
	public JComponent getPlotPanel();
	
	/**
	 * Add a data set to the plot.
	 * @param dataSetName unique name of the data set.
	 * @param plottingColor desired plotting color of data set.
	 */
	public void addDataSet(String dataSetName, Color plottingColor);
	
	/**
	 * Add a data set to the plot.
	 * @param lowerCase lowercase unique name of the data set.
	 * @param plottingColor desired plotting color of data set.
	 * @param displayName the display name.
	 */
	public void addDataSet(String lowerCase, Color plottingColor, String displayName);	
	
	/**
	 * Add a data set to the plot.
	 * @param lowerCase lowercase unique name of the data set.
	 * @param plottingColor desired plotting color of data set.
	 * @param displayName the display name.
	 */
	public void addDataSet(String lowerCase, Color plottingColor, AbstractLegendEntry legend);	

	/**
	 * Determine if data set is defined in plot.
	 * @param setName the dataset name.
	 * @return true if the dataset is defined in the plot, false otherwise.
	 */
	public boolean isKnownDataSet(String setName);


	/**
	 * Update the legend entry for the corresponding data set. 
	 * @param dataSetName name of the data set.
	 * @param info rendering information.
	 */
	public void updateLegend(String dataSetName, FeedProvider.RenderingInfo info); 
	
	/**
	 * Instruct the plot to redraw.
	 */
	public void refreshDisplay();
	
	/**
	 * Return the number of data sets on the plot.
	 * @return the number of data sets.
	 */
	public int getDataSetSize();
	
    /**
     * Return the state of the alarm which indicates if plot has experienced data which is outside.
     * of its current non time max axis limit. 
     * @return non-time max. limit alarm state.
     */
    public LimitAlarmState getDependentMaxAlarmState();
    
    /**
     * Return the state of the alarm which indicates if plot has experienced data which is outside.
     * of its current non time min axis limit. 
     * @return non-time min. limit alarm state.
     */
	public LimitAlarmState getDependentMinAlarmState();
	
	
	
	/**
	 * Return the current time axis minimum.
	 * @return Gregorian calendar for current time axis minimal.
	 */
	public GregorianCalendar getCurrentTimeAxisMin();
	
	/**
	 * Return current time axis maximum.
	 * @return Gregorian calendar for current time axis maximum.
	 */
	public GregorianCalendar getCurrentTimeAxisMax();	
	
	
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
	 * Return the value specified initially as the non time axis minimum bound.
	 * @return double initial non-time minimal setting.
	 */
	public double getInitialNonTimeMinSetting();
	
	/**
	 * Return the value specified initially as the non time axis maximum bound.
	 * @return double initial non-time maximum setting.
	 */
    public double getInitialNonTimeMaxSetting();
    
    /**
     * Return the value specified initially as the time axis minimum bound. 
     * @return initial time minimal setting.
     */
    public long getInitialTimeMinSetting();
    
    /**
     * Return the value specified initially as the time axis maximum bound. 
     * @return long initial time maximum setting.
     */
    public long getInitialTimeMaxSetting();
    
    /**
     * Return true if the time sync line is visible. False otherwise.
     * @return boolean flag for time sync line visible.
     */
    public boolean isTimeSyncLineVisible();

    /**
     * Inform the plotting package of its related plotAbstraction.
     * @param plotView related plotAbstraction.
     */
	public void setPlotAbstraction(PlotAbstraction plotView);
	
	/**
	 * Notify that time synchronization mode is to end. 
	 */
	public void notifyGlobalTimeSyncFinished();
	
	/**
	 * Return true if plot is in time sync mode.
	 * @return boolean flag to check whether in time sync mode.
	 */
	public boolean inTimeSyncMode();
	
	/**
	 * Return the largest value currently displayed on the plot.
	 * @return double non-time maximum data value currently displayed.
	 */
	public double getNonTimeMaxDataValueCurrentlyDisplayed();
	    
	/**
	 * Return the smallest value currently displayed on the plot.
	 * @return double non-time minimal data value currently displayed.
	 */
	public double getNonTimeMinDataValueCurrentlyDisplayed();
	
	/**
	 * Set the compression mode of the plot to state. 
	 * @param state boolean flag.
	 */
	public void setCompressionEnabled(boolean state);
	
	/**
	 * Return true if plot data compression is enabled, false otherwise. 
	 * @return boolean is compression enabled.
	 */
	public boolean isCompressionEnabled();
	
	/**
	 * Inform the plot that a data buffer update event has started.
	 */
	public void informUpdateCachedDataStreamStarted();
	
	/**
	 * Inform the plot that a data buffer update event has finished. 
	 */
	public void informUpdateCacheDataStreamCompleted();
	
	/**
	 * Inform the plot that a regular data stream update event has started.
	 */
    public void informUpdateFromLiveDataStreamStarted();
	
    /**
	 * Inform the plot that a regular data stream update event has ended.
	 */
	public void informUpdateFromLiveDataStreamCompleted();

	/**
	 * Instruct the plot to set its time axis start and stop to the specified times.
	 * @param startTime plot start time.
	 * @param endTime plot end time.
	 */
    public void setTimeAxisStartAndStop(long startTime, long endTime);

	/**
	 * Instruct plot to clear all data.
	 */
	public void clearAllDataFromPlot();

	/**
	 * Pauses the plot if the argument is true, unpauses it otherwise.
	 * @param b true to pause the plot.
	 */
	public void pause(boolean b);

	/**
	 * Returns true if the plot is paused.
	 * @return true if the plot is paused.
	 */
	public boolean isPaused();

	/**
	 * Returns the non-time axis.
	 * @return the non-time axis.
	 */
	public Axis getNonTimeAxis();

	/**
	 * Updates the visibility of the corner reset buttons based on which axes are pinned and what data is visible.
	 */
	public void updateResetButtons();
	
	/**
	 * Setter/Getter for Plot Legend Labeling Algorithm.
	 * @param thePlotLabelingAlgorithm the plotting labeling algorithm based upon the table algorithm.
	 */
	public void setPlotLabelingAlgorithm(AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm);
	
	/**
	 * Gets the plot labeling algorithm instance.
	 * @return abbreviating plot labeling algorithm.
	 */
	public AbbreviatingPlotLabelingAlgorithm getPlotLabelingAlgorithm();

	/**
	 * Adds the data points per feed Id.
	 * @param feedID feed identifier.
	 * @param points sorted map of points.
	 */
	public void addData(String feedID, SortedMap<Long, Double> points);

	/**
	 * Adds the data per feed Id, timestamp and telemetry value.
	 * @param feed - feed Id.
	 * @param time - timestamp.
	 * @param value - telemetry value.
	 */
    public void addData(String feed, long time, double value);

    /**
     * Sets the minimal truncation value for telemetry point.
     * @param min minimal value
     */
	public void setTruncationPoint(double min);

	/**
	 * Updates the compression ratio.
	 */
	public void updateCompressionRatio();
	
	/**
	 * Get the PlotAbstraction associated with this plotting package instance.
	 * @return
	 */
	public PlotAbstraction getPlotAbstraction();

	/**
	 * Get the manager of legends for this plotting package.
	 * @return
	 */
	public LegendManager getLegendManager();


	public AbstractPlotDataManager getPlotDataManager();
	
	public PlotLocalControlsManager getLocalControlsManager();


	public PlotViewActionListener getPlotActionListener();


	public PlotLimitManager getLimitManager();
	
	public AbstractPlotLine createPlotLine();
}
