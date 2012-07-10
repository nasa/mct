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
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.LegendEntryPopupMenuFactory;
import gov.nasa.arc.mct.fastplot.view.PinSupport;
import gov.nasa.arc.mct.fastplot.view.Pinnable;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.gui.FeedView.SynchronizationControl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the general plot interface. 
 * 
 * Construct using the builder pattern.
 * 
 * Example: 
 * <code>Plot.Builder(new <GeneralPlottingPackageInterface).plotName("Your Plot Name").build();</code> 
 * 
 * RefinedAbstraction in bridge pattern.
 */

public class PlotView implements PlotAbstraction {
	
	private final static Logger logger = LoggerFactory.getLogger(PlotView.class);	

	private static final Timer timer = new Timer();

    // Manifestation holding this plot
	private PlotViewManifestation plotUser;
	
	// Used in time synchronization line mode.
	private SynchronizationControl synControl;
	
	private Class <? extends AbstractPlottingPackage> plotPackage;
	private String plotName;
	
	// Initial Settings.
	private AxisOrientationSetting axisOrientation;
	private String timeSystem;
    private String timeFormat;
	private XAxisMaximumLocationSetting xAxisMaximumLocationSetting;
	private YAxisMaximumLocationSetting yAxisMaximumLocationSetting;
	private boolean useOrdinalPositionForSubplots;
	
	// Subsequent settings
	private TimeAxisSubsequentBoundsSetting timeAxisSubsequentSetting;
	private NonTimeAxisSubsequentBoundsSetting nonTimeAxisMinSubsequentSetting;
	private NonTimeAxisSubsequentBoundsSetting nonTimeAxisMaxSubsequentSetting;
	
	/* Appearance Constants */
	// - Fonts
	private Font timeAxisFont;

	// Thickness of the plotted lines.
	private int plotLineThickness;

	private Color plotBackgroundFrameColor;
	private Color plotAreaBackgroundColor;

	// - Axis
	// -- x-axis
	// Point where x-axis intercepts y axis
	private int timeAxisIntercept;
	//color for drawing x-axis
	private Color timeAxisColor;
	// x-axis labels
	private Color timeAxisLabelColor;
	private Color timeAxisLabelTextColor;

	// format of the date when shown on the x-axis
	private String timeAxisDataFormat;

	// -- y-axis
	private Color nonTimeAxisColor;

	// - Gridlines
	private Color gridLineColor;

	/* Scrolling and scaling behaviors */
	// Number of sample to accumulate before autoscaling the y-axis. This
	// prevents rapid changing of the y axis.
	private int minSamplesForAutoScale;
	private double scrollRescaleTimeMargin;
	private double scrollRescaleNonTimeMinMargin;
	private double scrollRescaleNonTimeMaxMargin;
 
	private double depdendentVaribleAxisMinValue;
	private double depdendentVaribleAxisMaxValue;
	
	private long timeVariableAxisMinValue;
    private long timeVariableAxisMaxValue;
    
    private boolean compressionEnabled;
    private boolean localControlsEnabled;
    private int numberOfSubPlots;
    
    // Plot line settings
    private PlotLineDrawingFlags plotLineDraw;
    private PlotLineConnectionType plotLineConnectionType;  
    
    /** The list of sub plots. */
    public List<AbstractPlottingPackage> subPlots;
    
    /** The plot panel. */
    JPanel plotPanel;
    
    /** Map for containing the data set name to sub-group. */
    public Map<String, Set<AbstractPlottingPackage>> dataSetNameToSubGroupMap = new HashMap<String, Set<AbstractPlottingPackage>>();
    
    /** Map for containing the data set name to display map. */
    public Map<String, String> dataSetNameToDisplayMap = new HashMap<String, String>();
    
    private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();;
    
    /** List of plot subjects. */
    List<PlotSubject> subPlotsToIgnoreNextUpdateFrom = new ArrayList<PlotSubject>();
    
    /** Time axis at start of update cycle. */
    GregorianCalendar timeAxisMaxAtStartOfDataUpdateCycle = new GregorianCalendar();
    
    /** Lock updates flag. */
    boolean lockUpdates = false;

	private PinSupport pinSupport = new PinSupport() {
		protected void informPinned(boolean pinned) {
			if(pinned) {
				pause();
			} else {
				unpause();
			}
		}
	};

	private Pinnable timeSyncLinePin = createPin();

	private Axis timeAxis = new Axis();

	/** This listens to key events for the plot view and all sub-components so it can forward modifier key presses and releases to the local controls managers. */
	private KeyListener keyListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
		}


		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				for(AbstractPlottingPackage p : subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informCtlKeyState(false);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_ALT) {
				for(AbstractPlottingPackage p : subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informAltKeyState(false);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
				for(AbstractPlottingPackage p : subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informShiftKeyState(false);
				}
			}
		}


		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				for(AbstractPlottingPackage p : subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informCtlKeyState(true);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_ALT) {
				for(AbstractPlottingPackage p : subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informAltKeyState(true);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
				for(AbstractPlottingPackage p : subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informShiftKeyState(true);
				}
			}
		}
	};

	private Pinnable timeAxisUserPin = timeAxis.createPin();

	private ContainerListener containerListener = new ContainerListener() {
		@Override
		public void componentAdded(ContainerEvent e) {
			addRecursiveListeners(e.getChild());
		}


		@Override
		public void componentRemoved(ContainerEvent e) {
			removeRecursiveListeners(e.getChild());
		}
	};

	private TimerTask updateTimeBoundsTask;

	private TimeXYAxis plotTimeAxis;

	/**
	 * Sets the plot view manifestation. 
	 * @param theManifestation - the plot view manifestation.
	 */
    public void setManifestation(PlotViewManifestation theManifestation) {
        if (theManifestation == null ) {
    	   throw new IllegalArgumentException("Plot must not have a null user");
        }
    	
    	plotUser = theManifestation;
    	for (AbstractPlottingPackage p: subPlots) {
    	    p.setPlotView(this);
    	}
    }
    
    
    @Override
	public JPanel getPlotPanel() {
		return plotPanel;
	}
	
	@Override
	public void addDataSet(String dataSetName) {
		addDataSet(dataSetName.toLowerCase(), getNextColor(subPlots.size()-1));
	}
	
	@Override
	public void addDataSet(String dataSetName, Color plottingColor) {
		throwIllegalArgumentExcpetionIfWeHaveNoPlots();
		
		getLastPlot().addDataSet(dataSetName.toLowerCase(), plottingColor);
		String name = dataSetName.toLowerCase();
		Set<AbstractPlottingPackage> set = dataSetNameToSubGroupMap.get(name);
		if(set == null) {
			set = new HashSet<AbstractPlottingPackage>();
			dataSetNameToSubGroupMap.put(name, set);
		}
		set.add(getLastPlot());
		dataSetNameToDisplayMap.put(dataSetName.toLowerCase(), dataSetName);
	}

	@Override
	public void addDataSet(String dataSetName, String displayName) {
		throwIllegalArgumentExcpetionIfWeHaveNoPlots();
		
		getLastPlot().addDataSet(dataSetName.toLowerCase(), getNextColor(subPlots.size()-1), displayName); 
		String name = dataSetName.toLowerCase();
		Set<AbstractPlottingPackage> set = dataSetNameToSubGroupMap.get(name);
		if(set == null) {
			set = new HashSet<AbstractPlottingPackage>();
			dataSetNameToSubGroupMap.put(name, set);
		}
		set.add(getLastPlot());
		dataSetNameToDisplayMap.put(dataSetName.toLowerCase(), displayName);
	}

	/**
	 * Adds the data set per subgroup index, data set name and display name.
	 * @param subGroupIndex - the subgroup index.
	 * @param dataSetName - data set name.
	 * @param displayName - base display name.
	 */
	public void addDataSet(int subGroupIndex, String dataSetName, String displayName) {
	  throwIllegalArgumentExcpetionIfIndexIsNotInSubPlots(subGroupIndex);
	  
	  String lowerCaseDataSetName = dataSetName.toLowerCase();
	  int actualIndex = subGroupIndex;
	  subPlots.get(actualIndex).addDataSet(lowerCaseDataSetName, getNextColor(actualIndex), displayName);   
		Set<AbstractPlottingPackage> set = dataSetNameToSubGroupMap.get(lowerCaseDataSetName);
		if(set == null) {
			set = new HashSet<AbstractPlottingPackage>();
			dataSetNameToSubGroupMap.put(lowerCaseDataSetName, set);
		}
		set.add(subPlots.get(actualIndex));
	  dataSetNameToDisplayMap.put(lowerCaseDataSetName, displayName);
	}
	
	/**
	 * Adds the popup menus to plot legend entry.
	 */
	public void addPopupMenus() {
		LegendEntryPopupMenuFactory popupManager = new LegendEntryPopupMenuFactory(plotUser);
		for (int index = 0; index < subPlots.size(); index++) {
			PlotterPlot plot = (PlotterPlot) subPlots.get(index);
			
			for (String dataSetName : plot.plotDataManager.dataSeries.keySet()) {
				plot.plotDataManager.dataSeries.get(dataSetName).legendEntry.setPopup(
						popupManager
						);
			}
			
		}
	}

	
	/**
	 * Get per-line settings currently in use for this stack of plots.
	 * Each element of the returned list corresponds, 
	 * in order, to the sub-plots displayed, and maps subscription ID to a 
	 * LineSettings object describing how its plot line should be drawn.
	 * @return a list of subscription->setting mappings for this plot
	 */
	public List<Map<String, LineSettings>> getLineSettings() {
		List<Map<String,LineSettings>> settingsAssignments = new ArrayList<Map<String,LineSettings>>();
		for (int subPlotIndex = 0; subPlotIndex < subPlots.size(); subPlotIndex++) {
			Map<String, LineSettings> settingsMap = new HashMap<String, LineSettings>();
			settingsAssignments.add(settingsMap);
			PlotterPlot plot = (PlotterPlot) subPlots.get(subPlotIndex);
			for (Entry<String, PlotDataSeries> entry : plot.plotDataManager.dataSeries.entrySet()) {
				settingsMap.put(entry.getKey(), entry.getValue().legendEntry.getLineSettings());
			}			
		}	
		return settingsAssignments;
	}
	

	/**
	 * Set line settings for use in this stack of plots.
	 * Each element corresponds, in order, to the sub-plots displayed, and maps 
	 * subscription ID to the line settings described by a LineSettings object
	 * @param lineSettings a list of subscription->line setting mappings for this plot
	 */
	public void setLineSettings(
			List<Map<String, LineSettings>> lineSettings) {
		if (lineSettings != null) {
			for (int subPlotIndex = 0; subPlotIndex < lineSettings.size() && subPlotIndex < subPlots.size(); subPlotIndex++) {
				PlotterPlot plot = (PlotterPlot) subPlots.get(subPlotIndex);			
				for (Entry<String, LineSettings> entry : lineSettings.get(subPlotIndex).entrySet()) {
					if (plot.plotDataManager.dataSeries.containsKey(entry.getKey())) {
						plot.plotDataManager.dataSeries.get(entry.getKey()).legendEntry.setLineSettings(entry.getValue()) ;//.setForeground(PlotLineColorPalette.getColor(entry.getValue()));
					}
				}
			}
		}		
	}

	
	@Override
	public boolean isKnownDataSet(String setName) {
		assert setName != null : "data set is null";
		
		return dataSetNameToSubGroupMap.containsKey(setName.toLowerCase());
	}

	@Override
	public void refreshDisplay() {
		for (AbstractPlottingPackage p: subPlots) {
			p.refreshDisplay();
		}
	}


	@Override
	public void updateLegend(String dataSetName, FeedProvider.RenderingInfo info) {
		String dataSetNameLower = dataSetName.toLowerCase();
		for(AbstractPlottingPackage plot : dataSetNameToSubGroupMap.get(dataSetNameLower)) {
			plot.updateLegend(dataSetNameLower, info);
		}
	}
	
	@Override
	public LimitAlarmState getNonTimeMaxAlarmState(int subGroupIndex) {
		throwIllegalArgumentExcpetionIfIndexIsNotInSubPlots(subGroupIndex);
		
		return subPlots.get(subGroupIndex).getNonTimeMaxAlarmState();
	}
	
	@Override
	public LimitAlarmState getNonTimeMinAlarmState(int subGroupIndex) {
		throwIllegalArgumentExcpetionIfIndexIsNotInSubPlots(subGroupIndex);
		
	    return subPlots.get(subGroupIndex).getNonTimeMinAlarmState();
	}
	
	@Override
	public GregorianCalendar getMinTime() {
		return getLastPlot().getCurrentTimeAxisMin();
	}

	@Override
	public GregorianCalendar getMaxTime() {
		return getLastPlot().getCurrentTimeAxisMax();
	}

	@Override
	public AxisOrientationSetting getAxisOrientationSetting() {
		return getLastPlot().getAxisOrientationSetting();
	}

	@Override
	public String getTimeSystem() {
		return getLastPlot().getTimeSystemSetting();
    }

	@Override
	public String getTimeFormat() {
		return  getLastPlot().getTimeFormatSetting();
	}

	@Override
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMaxSetting() {
		return getLastPlot().getNonTimeAxisSubsequentMaxSetting();
	}

	@Override
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMinSetting() {
		return getLastPlot().getNonTimeAxisSubsequentMinSetting();
	}

	@Override
	public double getNonTimeMax() {
		return getLastPlot().getInitialNonTimeMaxSetting();
	}

	@Override
	public double getNonTimeMaxPadding() {
		return getLastPlot().getNonTimeMaxPadding();
	}

	@Override
	public double getNonTimeMin() {
		return getLastPlot().getInitialNonTimeMinSetting();
	}

	@Override
	public double getNonTimeMinPadding() {
		return getLastPlot().getNonTimeMinPadding();
	}
	
	@Override
	public boolean useOrdinalPositionForSubplots() {
		return getLastPlot().getOrdinalPositionInStackedPlot(); 
	}
	
	@Override
	public TimeAxisSubsequentBoundsSetting getTimeAxisSubsequentSetting() {
		return getLastPlot().getTimeAxisSubsequentSetting();
	}

	@Override
	public long getTimeMax() {
		return getLastPlot().getInitialTimeMaxSetting();
	}

	@Override
	public long getTimeMin() {
		return getLastPlot().getInitialTimeMinSetting();
	}

	@Override
	public double getTimePadding() {
		return getLastPlot().getTimePadding();
	}

	@Override
	public XAxisMaximumLocationSetting getXAxisMaximumLocation() {
		return getLastPlot().getXAxisMaximumLocation();
	}

	@Override
	public YAxisMaximumLocationSetting getYAxisMaximumLocation() {
		return getLastPlot().getYAxisMaximumLocation();
	}
	
	
	@Override
	public void showTimeSyncLine(GregorianCalendar time) {
		assert time != null;
		timeSyncLinePin.setPinned(true);
		for (AbstractPlottingPackage p: subPlots) {
		 p.showTimeSyncLine(time);
		}
	}
	
	@Override
	public void removeTimeSyncLine() {
		timeSyncLinePin.setPinned(false);
		for (AbstractPlottingPackage p: subPlots) {
			 p.removeTimeSyncLine();
		}
	}
	
	@Override
	public boolean isTimeSyncLineVisible() {
		return getLastPlot().isTimeSyncLineVisible();
	}
	
	@Override
	public void initiateGlobalTimeSync(GregorianCalendar time) {
		synControl = plotUser.synchronizeTime(time.getTimeInMillis());
	}
	
	@Override
	public void updateGlobalTimeSync(GregorianCalendar time) {
		if(synControl == null) {
			synControl = plotUser.synchronizeTime(time.getTimeInMillis());
		} else {
			synControl.update(time.getTimeInMillis());
		}
	}

	@Override
	public void notifyGlobalTimeSyncFinished() {
		if (synControl!=null) {
		  synControl.synchronizationDone();
	    }
		removeTimeSyncLine();
	}
	
	@Override
	public boolean inTimeSyncMode() {
		return getLastPlot().inTimeSyncMode();
	}
	
	private Color getNextColor(int subGroupIndex) {
		throwIllegalArgumentExcpetionIfIndexIsNotInSubPlots(subGroupIndex);
		
		if (subPlots.get(subGroupIndex).getDataSetSize() < PlotLineColorPalette.getColorCount()) {
			return PlotLineColorPalette.getColor(subPlots.get(subGroupIndex).getDataSetSize());
		} else {
			// Exceeded the number of colors in the pallet.
			return PlotConstants.ROLL_OVER_PLOT_LINE_COLOR;
		}
	}
	
	@Override
	public double getNonTimeMaxCurrentlyDisplayed() {
		return getLastPlot().getNonTimeMaxDataValueCurrentlyDisplayed();
	}
	    
	@Override
	public double getNonTimeMinCurrentlyDisplayed() {
		return getLastPlot().getNonTimeMinDataValueCurrentlyDisplayed();
	}

	@Override
	public String toString() {
		assert plotPackage != null : "Plot package not initalized";

		return "Plot: + " + plotName + "\n" + plotPackage.toString();
	}

	/**
	 * Construct plots using the builder pattern.
	 * 
	 */
	public static class Builder {
		// Required parameters
		private Class<? extends AbstractPlottingPackage> plotPackage;

		//Optional parameters
		// default values give a "traditional" chart with time on the x-axis etc.
		private String plotName = "Plot Name Undefined";
		private AxisOrientationSetting axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
		private XAxisMaximumLocationSetting xAxisMaximumLocatoinSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
		private YAxisMaximumLocationSetting yAxisMaximumLocationSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
		private TimeAxisSubsequentBoundsSetting timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
		private NonTimeAxisSubsequentBoundsSetting nonTimeAxisMinSubsequentSetting = PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_SUBSEQUENT_SETTING;
		private NonTimeAxisSubsequentBoundsSetting nonTimeAxisMaxSubsequentSetting = PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_SUBSEQUENT_SETTING;
		private PlotLineDrawingFlags plotLineDraw = new PlotLineDrawingFlags(true, false); // TODO: Move to PlotConstants?
		private PlotLineConnectionType plotLineConnectionType = PlotLineGlobalConfiguration.getDefaultConnectionType(); 
		
		
		// initial settings
		private Font timeAxisFont = PlotConstants.DEFAULT_TIME_AXIS_FONT;
		private int plotLineThickness = PlotConstants.DEFAULT_PLOTLINE_THICKNESS ;
		private Color plotBackgroundFrameColor = PlotConstants.DEFAULT_PLOT_FRAME_BACKGROUND_COLOR;
		private Color plotAreaBackgroundColor = PlotConstants.DEFAULT_PLOT_AREA_BACKGROUND_COLOR;
		private int timeAxisIntercept = PlotConstants.DEFAULT_TIME_AXIS_INTERCEPT;
		private Color timeAxisColor = PlotConstants.DEFAULT_TIME_AXIS_COLOR;
		private Color timeAxisLabelColor = PlotConstants.DEFAULT_TIME_AXIS_LABEL_COLOR;
		private String timeSystem = null;
        private String timeFormat = PlotConstants.DEFAULT_TIME_FORMAT;
		private String timeAxisDateFormat = PlotConstants.DEFAULT_TIME_AXIS_DATA_FORMAT;
		private Color nonTimeAxisColor = PlotConstants.DEFAULT_NON_TIME_AXIS_COLOR;
		private Color gridLineColor = PlotConstants.DEFAULT_GRID_LINE_COLOR;
		private int minSamplesForAutoScale = PlotConstants.DEFAULT_MIN_SAMPLES_FOR_AUTO_SCALE;
		private double scrollRescaleMarginTimeAxis = PlotConstants.DEFAULT_TIME_AXIS_PADDING;
		private double scrollRescaleMarginNonTimeMinAxis = PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MIN;
		private double scrollRescaleMarginNonTimeMaxAxis = PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MAX;
		private double depdendentVaribleAxisMinValue  = PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_VALUE;
		private double dependentVaribleAxisMaxValue = PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_VALUE;
        private long timeVariableAxisMinValue = new GregorianCalendar().getTimeInMillis();
        private long timeVariableAxisMaxValue = timeVariableAxisMinValue + 	PlotConstants.DEFAUlT_PLOT_SPAN;
        private boolean compressionEnabled = PlotConstants.COMPRESSION_ENABLED_BY_DEFAULT;
        private int numberOfSubPlots = PlotConstants.DEFAULT_NUMBER_OF_SUBPLOTS;
        private boolean localControlsEnabled = PlotConstants.LOCAL_CONTROLS_ENABLED_BY_DEFAULT;
        private boolean useOrdinalPositionForSubplotSetting = true;
        private boolean pinTimeAxisSetting = false;
        private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();

        
		/**
		 * Specifies the required parameters for constructing a plot.
		 * @param selectedPlotPackage plotting package to render the plot
		 */
		public Builder(Class<? extends AbstractPlottingPackage>selectedPlotPackage) {
			this.plotPackage = selectedPlotPackage;
		}

		/**
		 * Specify the plot's user readable name.
		 * @param initPlotName the initial plot name.
		 * @return builder the plot view.
		 */
		public Builder plotName(String initPlotName) {
			plotName = initPlotName;
			return this;
		}

		/**
		 * Specify if the time axis should be oriented on the x- or y-axis.
		 * @param theAxisOrentation the axis orientation setting.
		 * @return builder the plot view.
		 */
		public Builder axisOrientation(PlotConstants.AxisOrientationSetting theAxisOrentation) {
			axisOrientation = theAxisOrentation;
			return this;
		}

		/**
		 * Specify if the x-axis maximum value should be on the left or right of the plot.
		 * @param theXAxisSetting the X-Axis setting.
		 * @return builder the plot view.
		 */           
		public Builder xAxisMaximumLocation(PlotConstants.XAxisMaximumLocationSetting theXAxisSetting) {
			xAxisMaximumLocatoinSetting = theXAxisSetting;
			return this;
		}

		/**
		 * Specify if the y-axis maximum value should be on the bottom or top  of the plot.
		 * @param theYAxisSetting the Y-Axis setting.
		 * @return the builder the plot view.
		 */
		public Builder yAxisMaximumLocation(PlotConstants.YAxisMaximumLocationSetting theYAxisSetting) {
			yAxisMaximumLocationSetting = theYAxisSetting;
			return this;
		}

		
		/**
		 * Specify how the bounds of the time axis will behave as plotting commences.
		 * @param theTimeAxisSubsequentSetting the time axis subsequent bounds settings.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisBoundsSubsequentSetting(PlotConstants.TimeAxisSubsequentBoundsSetting theTimeAxisSubsequentSetting) {
			timeAxisSubsequentSetting = theTimeAxisSubsequentSetting;
			return this;
		}

		/**
		 * Specify how the bounds of the non time axis will behave as plotting commences.
		 * @param theNonTimeAxisMinSubsequentSetting the non-time axis minimal subsequent setting.
		 * @return the builder the plot view.
		 */
		public Builder nonTimeAxisMinSubsequentSetting(PlotConstants.NonTimeAxisSubsequentBoundsSetting theNonTimeAxisMinSubsequentSetting) {
			nonTimeAxisMinSubsequentSetting = theNonTimeAxisMinSubsequentSetting;
			return this;
		}		
		
		/**
		 * Specify whether the ordinal position should be used to construct subplots. 
		 * @param useOrdinalPositionForSubplots whether ordinal position for subplots should be used.
		 * @return the builder the plot view.
		 */
		public Builder useOrdinalPositionForSubplots(boolean useOrdinalPositionForSubplots) {
			useOrdinalPositionForSubplotSetting = useOrdinalPositionForSubplots;
			return this;
		}
		
		/**
		 * Specify whether the initial time axis should be pinned. 
		 * @param pin whether time axis should initially be pinned.
		 * @return the builder the plot view.
		 */
		public Builder pinTimeAxis(boolean pin) {
			pinTimeAxisSetting = pin;
			return this;
		}
		
		/**
		 * Specify how the bounds of the non time axis will behave as plotting commences.
		 * @param theNonTimeAxisMaxSubsequentSetting the non-time axis minimal subsequent setting.
		 * @return the builder the plot view.
		 */
		public Builder nonTimeAxisMaxSubsequentSetting(PlotConstants.NonTimeAxisSubsequentBoundsSetting theNonTimeAxisMaxSubsequentSetting) {
			nonTimeAxisMaxSubsequentSetting = theNonTimeAxisMaxSubsequentSetting;
			return this;
		}	

		/**
		 * Specify the size of the font of the labels on the time axis.
		 * @param theTimeAxisFontSize font size.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisFontSize(int theTimeAxisFontSize) {
			timeAxisFont = new Font(timeAxisFont.getFontName(), Font.PLAIN, theTimeAxisFontSize);
			return this;
		}

		/**
		 * Specify the font that will be used to draw the labels on the axis axis.
		 * This parameter overrides the time axis font size parameter when specified.
		 * @param theTimeAxisFont the font size.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisFont(Font theTimeAxisFont) {
			timeAxisFont = theTimeAxisFont;
			return this;
		}

		/**
		 * Specify the thickness of the line used to plot data on the plot.
		 * @param theThickness the thickness.
		 * @return the builder the plot view.
		 */
		public Builder plotLineThickness(int theThickness) {
			plotLineThickness = theThickness;
			return this;
		}

		/**
		 * Specify the color of the frame surrounding the plot area.
		 * @param theBackgroundColor the color.
		 * @return the builder the plot view.
		 */
		public Builder plotBackgroundFrameColor(Color theBackgroundColor) {
			plotBackgroundFrameColor = theBackgroundColor;
			return this;
		}

		/**
		 * Specify the background color of the plot area.
		 * @param thePlotAreaColor the color.
		 * @return the builder the plot view.
		 */
		public Builder plotAreaBackgroundColor (Color thePlotAreaColor) {
			plotAreaBackgroundColor = thePlotAreaColor;
			return this;
		}

		/**
		 * Specify the point at which the time axis intercepts the non time axis.
		 * @param theIntercept the intercept point.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisIntercept(int theIntercept) {
			timeAxisIntercept = theIntercept;
			return this;
		}

		/**
		 * Specify the color of the time axis.
		 * @param theTimeAxisColor the color.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisColor(Color theTimeAxisColor) {
			timeAxisColor = theTimeAxisColor;
			return this;
		}

		/**
		 * Specify color of text on the time axis.
		 * @param theTimeAxisTextColor the color.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisTextColor(Color theTimeAxisTextColor) {
			timeAxisLabelColor = theTimeAxisTextColor;
			return this;
		}

		/**
         * Set the time system on the builder.
         * @param theTimeSystem time system
         * @return the builder the plot view
         */
		public Builder timeSystem(String theTimeSystem) {
			timeSystem = theTimeSystem;
			return this;
        }

        /**
         * Set the time format on the builder.
         * @param theTimeSystem time format
         * @return the builder the plot view
         */
        public Builder timeFormat(String theTimeFormat) {
        	timeFormat = theTimeFormat;
        	return this;
        }

		
		/**
		 * Set the format of how time information is printed on time axis labels.
		 * @param theTimeAxisDateFormat the format.
		 * @return the builder the plot view.
		 */
		public Builder timeAxisDateFormat(String theTimeAxisDateFormat) {
			timeAxisDateFormat = theTimeAxisDateFormat;
			return this;
		}

		/**
		 * Set the color of the non time axis.
		 * @param theNonTimeAxisColor the color.
		 * @return the builder the plot view.
		 */
		public Builder nonTimeAxisColor(Color theNonTimeAxisColor) {
			nonTimeAxisColor = theNonTimeAxisColor;
			return this;
		}

		/**
		 * Set the color of the plot gridlines.
		 * @param theGridLineColor the color.
		 * @return the builder the plot view.
		 */
		public Builder gridLineColor(Color theGridLineColor) {
			gridLineColor = theGridLineColor;
			return this;
		}

		/**
		 * The minimum number of samples to accumulate out of range before an autoscale occurs. This
		 * prevents rapid autoscaling on every plot action.
		 * @param theMinSamplesForAutoScale the number of samples.
		 * @return the plot view.
		 */
		public Builder minSamplesForAutoScale(int theMinSamplesForAutoScale) {
			minSamplesForAutoScale = theMinSamplesForAutoScale;
			return this;
		}

		/**
		 * Percentage of padding to use when rescalling the time axis.
		 * @param theScrollRescaleMargin the margin.
		 * @return the builder the plot view.
		 */
		public Builder scrollRescaleMarginTimeAxis(double theScrollRescaleMargin) {
			assert theScrollRescaleMargin <= 1 && theScrollRescaleMargin >=0 : "Attempting to set a scroll rescale margin (time padding) outside of 0 .. 1";
			scrollRescaleMarginTimeAxis = theScrollRescaleMargin;
			return this;
		}
		
		/** Percentage of padding to use when rescalling the non time axis min end.
		 * @param theScrollRescaleMargin the margin.
		 * @return the builder the plot view.
		 */
		public Builder scrollRescaleMarginNonTimeMinAxis(double theScrollRescaleMargin) {
			assert theScrollRescaleMargin <= 1 && theScrollRescaleMargin >=0 : "Attempting to set a scroll rescale margin (non time padding) outside of 0 .. 1";
			scrollRescaleMarginNonTimeMinAxis = theScrollRescaleMargin;
			return this;
		}
		
		/** Percentage of padding to use when rescalling the non time axis max end.
		 * @param theScrollRescaleMargin the margin.
		 * @return the builder the plot view.
		 */
		public Builder scrollRescaleMarginNonTimeMaxAxis(double theScrollRescaleMargin) {
			assert theScrollRescaleMargin <= 1 && theScrollRescaleMargin >=0 : "Attempting to set a scroll rescale margin (non time padding) outside of 0 .. 1";
			scrollRescaleMarginNonTimeMaxAxis = theScrollRescaleMargin;
			return this;
		}
		

		/**
		 * Specify the maximum extent of the dependent variable axis.
		 * @param theNonTimeVaribleAxisMaxValue the non-time variable axis max value.
		 * @return the plot view.
		 */
		public Builder nonTimeVaribleAxisMaxValue(double theNonTimeVaribleAxisMaxValue) {
			dependentVaribleAxisMaxValue = theNonTimeVaribleAxisMaxValue;
			return this;
		}

		/**
		 * Specify the minimum value of the dependent variable axis.
		 * @param theNonTimeVaribleAxisMinValue the non-time axis minimal value.
		 * @return the builder the plot view.
		 */
		public Builder nonTimeVaribleAxisMinValue(double theNonTimeVaribleAxisMinValue) {
			depdendentVaribleAxisMinValue = theNonTimeVaribleAxisMinValue;
			return this;
		}
		
		/**
		 * Specify the initial minimum value of the time axis.
		 * @param theTimeVariableAxisMinValue the time variable axis minimal value.
		 * @return the builder the plot view.
		 */
		public Builder timeVariableAxisMinValue(long theTimeVariableAxisMinValue) {
			timeVariableAxisMinValue = theTimeVariableAxisMinValue;
			return this;			
		}
		
		/**
		 * specify the initial maximum value of the time axis.
		 * @param theTimeVariableAxisMaxValue the time variable axis maximum value.
		 * @return the builder the plot view.
		 */
        public Builder timeVariableAxisMaxValue(long theTimeVariableAxisMaxValue) {
        	timeVariableAxisMaxValue = theTimeVariableAxisMaxValue;
			return this;
        }
	
        /**
         * Specify if the plot is to compress its data to match the screen resolution.
         * @param state true to compress, false otherwise.
         * @return the builder the plot view.
         */
        public Builder isCompressionEnabled(boolean state) {
        	compressionEnabled = state;
        	return this;
        }
        
        /**
         * Specify the number of subplots in this plotview.
         * @param theNumberOfSubPlots the number of sub-plots.
         * @return the builder the plot view.
         */
        public Builder numberOfSubPlots(int theNumberOfSubPlots) {
        	numberOfSubPlots = theNumberOfSubPlots;
        	return this;
        }
        
        /**
         * Turn the plot local controls on and off.
         * @param theIsEnabled true enabled; otherwise false.
         * @return builder the plot view.
         */
        public Builder localControlsEnabled(boolean theIsEnabled) {
        	localControlsEnabled = theIsEnabled;
        	return this;
        }
       
        /**
         * Specify the plot abbreviation labeling algorithm.
         * @param thePlotLabelingAlgorithm the plot labeling algorithm.
         * @return builder the plot view.
         */
        public Builder plotLabelingAlgorithm(AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm) {
        	plotLabelingAlgorithm = thePlotLabelingAlgorithm;
        	assert plotLabelingAlgorithm != null : "Plot labeling algorithm should NOT be NULL at this point.";
        	return this;
        }
        
        /**
         * Specify whether to draw lines, markers, or both.
         * @param plotLineDraw the plotting type
         * @return the plot view. 
         */
        public Builder plotLineDraw(PlotLineDrawingFlags plotLineDraw) {
        	this.plotLineDraw = plotLineDraw;
        	return this;
        }
        
        /**
         * 
         * @param plotLineConnectionType
         * @return
         */
        public Builder plotLineConnectionType(PlotLineConnectionType plotLineConnectionType) {
        	this.plotLineConnectionType = plotLineConnectionType;
        	return this;
        }
        
		/**
		 * Build a new plot instance and return it.
		 * @return the new plot instance.
		 */
		public PlotView build() {
			return new PlotView(this);
		}	 
	}

	// Private constructor. Construct using builder pattern.
	private PlotView(Builder builder) {	
		
		plotPackage = builder.plotPackage;
		plotName = builder.plotName;
		axisOrientation = builder.axisOrientation;
		timeSystem = builder.timeSystem;
        timeFormat = builder.timeFormat;
		xAxisMaximumLocationSetting = builder.xAxisMaximumLocatoinSetting;
		yAxisMaximumLocationSetting = builder.yAxisMaximumLocationSetting;
		timeAxisSubsequentSetting = builder.timeAxisSubsequentSetting;
		nonTimeAxisMinSubsequentSetting = builder.nonTimeAxisMinSubsequentSetting;	
		nonTimeAxisMaxSubsequentSetting = builder.nonTimeAxisMaxSubsequentSetting;	
		useOrdinalPositionForSubplots = builder.useOrdinalPositionForSubplotSetting;

		timeAxisFont = builder.timeAxisFont;
		plotLineThickness = builder.plotLineThickness;
		plotBackgroundFrameColor = builder.plotBackgroundFrameColor;
		plotAreaBackgroundColor = builder.plotAreaBackgroundColor;
		timeAxisIntercept = builder.timeAxisIntercept;
		timeAxisColor = builder.timeAxisColor;
		timeAxisLabelTextColor = builder.timeAxisLabelColor;
		timeAxisDataFormat = builder.timeAxisDateFormat;
		nonTimeAxisColor = builder.nonTimeAxisColor;
		gridLineColor = builder.gridLineColor;
		minSamplesForAutoScale = builder.minSamplesForAutoScale;
		scrollRescaleTimeMargin = builder.scrollRescaleMarginTimeAxis;
		scrollRescaleNonTimeMinMargin = builder.scrollRescaleMarginNonTimeMinAxis;
		scrollRescaleNonTimeMaxMargin = builder.scrollRescaleMarginNonTimeMaxAxis;
		depdendentVaribleAxisMinValue  = builder.depdendentVaribleAxisMinValue;
		depdendentVaribleAxisMaxValue = builder.dependentVaribleAxisMaxValue;
		timeVariableAxisMaxValue = builder.timeVariableAxisMaxValue;
		timeVariableAxisMinValue = builder.timeVariableAxisMinValue;
		compressionEnabled = builder.compressionEnabled;
		numberOfSubPlots = builder.numberOfSubPlots;
		localControlsEnabled = builder.localControlsEnabled;
		plotLabelingAlgorithm = builder.plotLabelingAlgorithm;
		
		setPlotLineDraw(builder.plotLineDraw);
		setPlotLineConnectionType(builder.plotLineConnectionType);

		plotPanel = new JPanel();
		plotPanel.addAncestorListener(new AncestorListener() {
			@Override
			public synchronized void ancestorRemoved(AncestorEvent event) {
				if(updateTimeBoundsTask != null) {
					updateTimeBoundsTask.cancel();
					updateTimeBoundsTask = null;
				}
			}


			@Override
			public void ancestorMoved(AncestorEvent event) {
			}


			@Override
			public synchronized void ancestorAdded(AncestorEvent event) {
				for(AbstractPlottingPackage p : subPlots) {
					p.updateCompressionRatio();
				}
				updateTimeBoundsTask = new TimerTask() {
					@Override
					public void run() {
						try {
							timeReachedEnd();
						} catch(Exception e) {
							// We need to catch exceptions because they can kill the timer.
							logger.error(e.toString(), e);
						}
					}
				};
				timer.schedule(updateTimeBoundsTask, 0, 1000);
			}
		});
		GridBagLayout layout = new StackPlotLayout(this);
		plotPanel.setLayout(layout);
		
		subPlots = new ArrayList<AbstractPlottingPackage>(numberOfSubPlots);
		
		// create the specified number of subplots
		for (int i=0; i< numberOfSubPlots; i++) {
			AbstractPlottingPackage newPlot;
			try {
				newPlot = plotPackage.newInstance();
				boolean isTimeLabelEnabled = i == (numberOfSubPlots -1);

				newPlot.createChart(axisOrientation, 
						timeSystem,
                        timeFormat,
						xAxisMaximumLocationSetting, 
						yAxisMaximumLocationSetting, 
						timeAxisSubsequentSetting, 
						nonTimeAxisMinSubsequentSetting, 
						nonTimeAxisMaxSubsequentSetting,
						timeAxisFont,
						plotLineThickness,
						plotBackgroundFrameColor, 
						plotAreaBackgroundColor, 
						timeAxisIntercept,
						timeAxisColor, 
						timeAxisLabelColor, 
						timeAxisLabelTextColor,
						timeAxisDataFormat, 
						nonTimeAxisColor, 
						gridLineColor,
						minSamplesForAutoScale, 
						scrollRescaleTimeMargin,
						scrollRescaleNonTimeMinMargin,
						scrollRescaleNonTimeMaxMargin,
						depdendentVaribleAxisMinValue, 
						depdendentVaribleAxisMaxValue,
						timeVariableAxisMinValue,
						timeVariableAxisMaxValue,
						compressionEnabled,
						isTimeLabelEnabled,
						localControlsEnabled,
						useOrdinalPositionForSubplots,
						getPlotLineDraw(), 
						getPlotLineConnectionType(), 
						this, plotLabelingAlgorithm);
				
				newPlot.setPlotLabelingAlgorithm(plotLabelingAlgorithm);
				subPlots.add(newPlot);
				newPlot.registerObservor(this);
				
				logger.debug("plotLabelingAlgorithm.getPanelContextTitleList().size()=" 
						+ plotLabelingAlgorithm.getPanelContextTitleList().size()
						+ ", plotLabelingAlgorithm.getCanvasContextTitleList().size()=" 
						+ plotLabelingAlgorithm.getCanvasContextTitleList().size());
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		
		if (axisOrientation == AxisOrientationSetting.Y_AXIS_AS_TIME) {
			Collections.reverse(subPlots);
		}
		
		for (AbstractPlottingPackage subPlot: subPlots) {
			JComponent subPanel = subPlot.getPlotPanel();
			plotPanel.add(subPanel);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1;
			c.weighty = 1;
			if(axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
				c.gridwidth = GridBagConstraints.REMAINDER;
			}
			layout.setConstraints(subPanel, c);
	    }

		// Note that using InputMap does not work for our situation.
		// See http://stackoverflow.com/questions/4880704/listening-to-key-events-for-a-component-hierarchy
		addRecursiveListeners(plotPanel);
		
		if (builder.pinTimeAxisSetting) {
			timeAxisUserPin.setPinned(true);
			// update the corner reset buttons after the plot is visible
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for(AbstractPlottingPackage subPlot : subPlots) {
						PlotterPlot plot = (PlotterPlot) subPlot;
						plot.updateResetButtons();
					}
				}
			});
		}
		
	}
	
	/**
	 * Gets the pinnable time axis by user.
	 * @return pinnable time axis. 
	 */
	public Pinnable getTimeAxisUserPin() {
		return timeAxisUserPin;
	}

	private void addRecursiveListeners(Component c) {
		c.addKeyListener(keyListener);
		if(c instanceof Container) {
			Container cont = (Container) c;
			cont.addContainerListener(containerListener);
			for(Component child : cont.getComponents()) {
				addRecursiveListeners(child);
			}
		}
	}


	private void removeRecursiveListeners(Component c) {
		c.removeKeyListener(keyListener);
		if(c instanceof Container) {
			Container cont = (Container) c;
			cont.removeContainerListener(containerListener);
			for(Component child : cont.getComponents()) {
				removeRecursiveListeners(child);
			}
		}
	}

	/**
	 * Gets the plot labeling algorithm.
	 * @return abbreviating plot labeling algorithm.
	 */
	public AbbreviatingPlotLabelingAlgorithm getPlotLabelingAlgorithm() {
		return plotLabelingAlgorithm;
	}

	/**
	 * Sets the plot labeling algorithm.
	 * @param thePlotLabelingAlgorithm the plot labeling algorithm.
	 */
	public void setPlotLabelingAlgorithm(AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm) {
		plotLabelingAlgorithm = thePlotLabelingAlgorithm;
	}
	
	@Override
	public AbstractPlottingPackage returnPlottingPackage() {
		return getLastPlot();
	}
	
	@Override
	public void setCompressionEnabled(boolean compression) {
		for (AbstractPlottingPackage p: subPlots) {
		  p.setCompressionEnabled(compression);
		}
	}
	
	@Override
	public boolean isCompresionEnabled() {
		return getLastPlot().isCompresionEnabled();
	}
	
	@Override
	public void requestPlotData(GregorianCalendar startTime, GregorianCalendar endTime) {
		plotUser.requestDataRefresh(startTime, endTime);
	}
	
	private void requestPredictivePlotData(GregorianCalendar startTime, GregorianCalendar endTime) {
		plotUser.requestPredictiveData(startTime, endTime);
	}


	@Override
	public void informUpdateDataEventStarted() {
		timeAxisMaxAtStartOfDataUpdateCycle.setTimeInMillis(this.getLastPlot().getCurrentTimeAxisMax().getTimeInMillis());
		for (AbstractPlottingPackage p: subPlots) {
		  p.informUpdateCachedDataStreamStarted();
		}
	}
	
	@Override
	public void informUpdateFromFeedEventStarted() {
		timeAxisMaxAtStartOfDataUpdateCycle.setTimeInMillis(this.getLastPlot().getCurrentTimeAxisMax().getTimeInMillis());
		for (AbstractPlottingPackage p: subPlots) {
		  p.informUpdateFromLiveDataStreamStarted();
		}		
	}
	
	@Override
	public void informUpdateDataEventCompleted() {
		for (AbstractPlottingPackage p: subPlots) {
		   p.informUpdateCacheDataStreamCompleted();
		}
		syncTimeAxisAcrossPlots();
	}
	
	@Override
    public void informUpdateFromFeedEventCompleted() {
		for (AbstractPlottingPackage p: subPlots) {
		  p.informUpdateFromLiveDataStreamCompleted();
		}
		syncTimeAxisAcrossPlots();
	}
	
	/**
	 * Synchronizes the time axis across all plots.
	 */
	void syncTimeAxisAcrossPlots() {
		long maxAtStart = timeAxisMaxAtStartOfDataUpdateCycle.getTimeInMillis();
		long currentMaxTime = maxAtStart;
		long currentMinTime = maxAtStart;
		for (AbstractPlottingPackage p: subPlots) {
			  long max = p.getCurrentTimeAxisMaxAsLong();
			  if (max > currentMaxTime) {
				  currentMaxTime = max;
				  currentMinTime = p.getCurrentTimeAxisMinAsLong();
			  }
		}

		if (currentMaxTime > maxAtStart) {
			boolean inverted;
			if(axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
				inverted = xAxisMaximumLocationSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
			} else {
				inverted = yAxisMaximumLocationSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
			}
			long start;
			long end;
			if(inverted) {
				start = currentMaxTime;
				end = currentMinTime;
			} else {
				start = currentMinTime;
				end = currentMaxTime;
			}
			for (AbstractPlottingPackage p: subPlots) {		
				p.setTimeAxisStartAndStop(start, end);
			}
		}
	}
	
	@Override
	public long getCurrentMCTTime() {	
    	return plotUser.getCurrentMCTTime();
    }

	/**
	 * Gets the plot user view manifestation.
	 * @return plotUser the plot user view manifestation.
	 */
	public PlotViewManifestation getPlotUser() {
		return plotUser;
	}

	/**
	 * Gets the last plot.
	 * @return abstract plotting package.
	 */
	AbstractPlottingPackage getLastPlot() {
	  throwIllegalArgumentExcpetionIfWeHaveNoPlots();
		
	  return subPlots.get(subPlots.size() - 1);
	}
	
	private void throwIllegalArgumentExcpetionIfWeHaveNoPlots() {
		 if (subPlots.size() < 1) {
			  throw new IllegalArgumentException("Plot contains no sub plots");
		  }
	}
	
	private void throwIllegalArgumentExcpetionIfIndexIsNotInSubPlots(int subGroupIndex) {
		if ((subPlots.size() -1) < subGroupIndex) {
			throw new IllegalArgumentException("subgroup is out of range" + subGroupIndex + " > " + (subPlots.size() -1));
		}
	}

	@Override
	public boolean plotMatchesSetting(PlotSettings settings) {
			if (getAxisOrientationSetting() != settings.timeAxisSetting) {
				return false;
			}
			
			if (getXAxisMaximumLocation() != settings.xAxisMaximumLocation) {
				return false;
			}
		
			if (getYAxisMaximumLocation() != settings.yAxisMaximumLocation) {
				return false;
			}
			
			if (getTimeAxisSubsequentSetting() != settings.timeAxisSubsequent) {
				return false;
			}
	
			if (getNonTimeAxisSubsequentMinSetting() != settings.nonTimeAxisSubsequentMinSetting) {
			    return false;	
			}
			
			if (getNonTimeAxisSubsequentMaxSetting() != settings.nonTimeAxisSubsequentMaxSetting) {
				  return false;	
		    }
			
			if (getTimeMax() != settings.maxTime) {
				  return false;	
		    }
			
			if (getTimeMin() != settings.minTime) {
				  return false;	
		    }
			
			if (getNonTimeMin() != settings.minNonTime) {
				  return false;	
		    }
			
			if (getNonTimeMax() != settings.maxNonTime) {
				  return false;	
		    }
			
			if (getTimePadding() != settings.timePadding) {
				return false;
			}
          
			if (getNonTimeMaxPadding() != settings.nonTimeMaxPadding) {
				return false;
			}
			
			if (getNonTimeMinPadding() != settings.nonTimeMinPadding) {
				return false;
			}
			
			if (useOrdinalPositionForSubplots() != settings.ordinalPositionForStackedPlots) {
				return false;
			}
			
			return true; 
		}

	@Override
	public void updateTimeAxis(PlotSubject subject, long startTime, long endTime) {				
	      for (AbstractPlottingPackage plot: subPlots) { 
	    	  if (plot!= subject) {
				  plot.setTimeAxisStartAndStop(startTime, endTime);
	    	  }
		  }
	}	


	@Override
	public void updateResetButtons() {
		for(AbstractPlottingPackage p : subPlots) {
			p.updateResetButtons();
		}
	}


    @Override
	public void clearAllDataFromPlot() {
    	for (AbstractPlottingPackage plot: subPlots) { 
			  plot.clearAllDataFromPlot();
	  }
		
	}


	@Override
	public Pinnable createPin() {
		return pinSupport.createPin();
	}


	private void pause() {
		for(AbstractPlottingPackage plot : subPlots) {
			plot.pause(true);
		}
	}

	private void unpause() {
		// Request data from buffer to fill in what was missed while paused.
		plotUser.updateFromFeed(null);
		for(AbstractPlottingPackage plot : subPlots) {
			plot.pause(false);
		}
	}

	@Override
	public boolean isPinned() {
		return pinSupport.isPinned();
	}

	@Override
	public List<AbstractPlottingPackage> getSubPlots() {
		return Collections.unmodifiableList(subPlots);
	}

	@Override
	public Axis getTimeAxis() {
		return timeAxis;
	}

	/**
	 * Adds data set per map.
	 * @param dataForPlot data map.
	 */
	public void addData(Map<String, SortedMap<Long, Double>> dataForPlot) {
		for(Entry<String, SortedMap<Long, Double>> feedData : dataForPlot.entrySet()) {
			String feedID = feedData.getKey();
			String dataSetNameLower = feedID.toLowerCase();
			if (!isKnownDataSet(dataSetNameLower)) {
				throw new IllegalArgumentException("Attempting to set value for an unknown data set " + feedID);
			}
			Set<AbstractPlottingPackage> feedPlots = dataSetNameToSubGroupMap.get(dataSetNameLower);

			SortedMap<Long, Double> points = feedData.getValue();
			for(AbstractPlottingPackage plot : feedPlots) {
				plot.addData(dataSetNameLower, points);
			}
		}
	}

	/**
	 * Adds data set per feed Id, timestamp, and telemetry value.
	 * @param feedID the feed Id.
	 * @param time timestamp in millisecs.
	 * @param value telemetry value in double.
	 */
	public void addData(String feedID, long time, double value) {
		SortedMap<Long, Double> points = new TreeMap<Long, Double>();
		points.put(time, value);
		addData(Collections.singletonMap(feedID, points));
	}

	/**
	 * Sets the plot X-Y time axis.
	 * @param axis the X-Y time axis.
	 */
	public void setPlotTimeAxis(TimeXYAxis axis) {
		this.plotTimeAxis = axis;
	}

	/**
	 * Gets the plot X-Y time axis.
	 * @return X-Y time axis. 
	 */
	public TimeXYAxis getPlotTimeAxis() {
		return plotTimeAxis;
	}


	private void timeReachedEnd() {
		long maxTime = getCurrentMCTTime();
		double plotMax = Math.max(plotTimeAxis.getStart(), plotTimeAxis.getEnd());
		double lag = maxTime - plotMax;
		double scrollRescaleTimeMargin = this.scrollRescaleTimeMargin;
		if (scrollRescaleTimeMargin == 0) {
			scrollRescaleTimeMargin = (maxTime - plotMax) / Math.abs(plotTimeAxis.getEnd() - plotTimeAxis.getStart());
		}
		if(lag > 0 && !timeAxis.isPinned()) {
			if(timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.JUMP) {
				double increment = Math.abs(scrollRescaleTimeMargin * (plotTimeAxis.getEnd() - plotTimeAxis.getStart()));
				plotTimeAxis.shift(Math.ceil(lag / increment) * increment);
				for(AbstractPlottingPackage subPlot : subPlots) {
					subPlot.setTimeAxisStartAndStop(plotTimeAxis.getStartAsLong(), plotTimeAxis.getEndAsLong());
				}
			} else if(timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
				double max = plotTimeAxis.getEnd();
				double min = plotTimeAxis.getStart();
				double diff = max - min;
				assert diff != 0 : "min = max = " + min;
				double scrunchFactor = 1 + scrollRescaleTimeMargin;
				if((max < min)) {
					min = max + (maxTime - max)*(scrunchFactor);
				} else {
					max = min + (maxTime - min)*(scrunchFactor);
				}
				plotTimeAxis.setStart(min);
				plotTimeAxis.setEnd(max);
				for(AbstractPlottingPackage subPlot : subPlots) {
					subPlot.setTimeAxisStartAndStop(plotTimeAxis.getStartAsLong(), plotTimeAxis.getEndAsLong());
					subPlot.updateCompressionRatio();
				}
			} else {
				assert false : "Unrecognized timeAxisSubsequentSetting: " + timeAxisSubsequentSetting;
			}
			double newPlotMax = Math.max(plotTimeAxis.getStart(), plotTimeAxis.getEnd());
			if(newPlotMax != plotMax) {
				GregorianCalendar start = new GregorianCalendar();
				GregorianCalendar end = new GregorianCalendar();
				start.setTimeInMillis((long) plotMax);
				end.setTimeInMillis((long) newPlotMax);
				requestPredictivePlotData(start, end);
			}
		}
	}


	@Override
	public PlotLineDrawingFlags getPlotLineDraw() {
		return plotLineDraw;
	}


	@Override
	public PlotLineConnectionType getPlotLineConnectionType() {
		return plotLineConnectionType;
	}


	@Override
	public void setPlotLineDraw(PlotLineDrawingFlags draw) {
		plotLineDraw = draw;
	}


	@Override
	public void setPlotLineConnectionType(PlotLineConnectionType type) {
		plotLineConnectionType = type;
	}
}
