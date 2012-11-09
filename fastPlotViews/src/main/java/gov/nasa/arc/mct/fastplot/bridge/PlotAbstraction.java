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
public interface PlotAbstraction extends PlotObserver, PlotConfiguration {

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
	public boolean isCompressionEnabled();
	
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
	public boolean plotMatchesSetting(PlotConfiguration settings);
	

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
	public void setPlotTimeAxis(AbstractAxis axis);
	
	
}
