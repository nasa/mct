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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotDisplayState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.Pinnable;
import gov.nasa.arc.mct.services.activity.TimeService;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plotter.xy.CompressingXYDataset;
import plotter.xy.LinearXYAxis;
import plotter.xy.XYAxis;
import plotter.xy.XYPlot;

/**
 * Provides the implementation of the general plotting package interface using the Fast plot 
 * plotting package. 
 * 
 * ConcreteImplementor in bridge pattern.
 */

public class PlotterPlot implements AbstractPlottingPackage {

	private final static Logger logger = LoggerFactory.getLogger(PlotterPlot.class);
	
	/** Default plot preferred height. */
	static final int PLOT_PREFERED_HEIGHT = 100;  // Values carefully selected with UE
	
	/** Default plot preferred width. */
	static final int PLOT_PREFERED_WIDTH = PlotConstants.MINIMUM_PLOT_WIDTH;   // to ensure plot is a reasonable size when dropped on to canvas.  
		
	/** Scroll frame only initialized once.*/
	boolean scrollFrameInitialized = false;

	/**
	 * Records if a mouse clicked operation is in progress. Used to prevent
	 * multiple mouse click operations from running together. 
	 */
	private boolean userOperationsLocked = false;

	// Chart settings
	/** Axis orientation setting. */
	AxisOrientationSetting axisOrientation;
	
	/** Time System. */
    String timeSystemSetting;

    /** Time Format. */
    String timeFormatSetting;
	
	/** X-Axis max location setting. */
	XAxisMaximumLocationSetting xAxisSetting;
	
	/** Y-Axis max location setting. */
	YAxisMaximumLocationSetting yAxisSetting;

	/** Time axis subsequent bounds setting. */
	TimeAxisSubsequentBoundsSetting timeAxisSubsequentSetting;
	
	/** Non-time axis minimal subsequent bounds setting. */
	NonTimeAxisSubsequentBoundsSetting nonTimeAxisMinSubsequentSetting;
	
	/** Non-time maximum subsequent bounds setting. */
	NonTimeAxisSubsequentBoundsSetting nonTimeAxisMaxSubsequentSetting;
	
	PlotLineDrawingFlags           plotLineDraw;
	PlotLineConnectionType plotLineConnectionType;
	
	/** The plot abstraction. */
	PlotAbstraction plotAbstraction;
	
	// Appearance

	// - Fonts.
	Font timeAxisFont;

	// Thickness of the plotted lines.
    int plotLineThickness;

	Color plotBackgroundFrameColor;
	Color plotAreaBackgroundColor;
	
	// - Axis
	// -- x-axis
	// Point where x-axis intercepts y axis
	int timeAxisIntercept;
	//color for drawing x-axis
	Color timeAxisColor;
	// x-axis labels
	Color timeAxisLabelColor;

	// format of the date when shown on the x-axis
	String timeAxisDateFormat;

	// -- y-axis
	Color nonTimeAxisColor;
	Color nonTimeAxisLabelColor;

	// - Grid lines on the plot
	Color gridLineColor;

	/* Scrolling and scaling behaviors */
	private double scrollRescaleMarginTime;
	private double scrollRescaleMarginNonTimeMin;
	private double scrollRescaleMarginNonTimeMax;

	double nonTimeVaribleAxisMinValue;
	double nonTimeVaribleAxisMaxValue;
	
	double nonTimeAxisMinPhysicalValue;
	double nonTimeAxisMaxPhysicalValue;

	long timeVariableAxisMinValue;
	long timeVariableAxisMaxValue;
	
	// The Plot
	XYPlot plotView;
	private JComponent plotPanel = new JPanel();

	GregorianCalendar startTime = new GregorianCalendar();
	GregorianCalendar endTime = new GregorianCalendar();
	
	// True if the plot initialization process is complete. 
	boolean isInitialized = false;
	
	// Limit Manager
	PlotLimitManager limitManager = new PlotLimitManager(this);
	
	// local controls manager
	PlotLocalControlsManager localControlsManager = new PlotLocalControlsManager(this);
	
	PanAndZoomManager panAndZoomManager = new PanAndZoomManager(this);
	
	PlotViewActionListener plotActionListener;
	
	PlotDataManager plotDataManager;
	
	QCPlotObjects qcPlotObjects;
	
	PlotCornerResetButtonManager cornerResetButtonManager;
	
	// Legend
	LegendManager legendManager;
	
	PlotTimeSyncLine timeSyncLine;
	PlotDataCursor dataCursor;
	
	boolean ordinalPositionInStackedPlot;
	
	// Reference to the time axis and minValue plotted used to keep the time axis intercept in line
	// with the lowest data point. 
	private TimeXYAxis theTimeAxis;
	LinearXYAxis theNonTimeAxis;
	
	double yAxisAxisLabelWidth = 0;
    double xAxisAxisLabelHeight = 0;
      
    boolean compressionIsEnabled = PlotConstants.COMPRESSION_ENABLED_BY_DEFAULT;
    
    boolean isTimeLabelEnabled = false;
    
    boolean isLocalControlsEnabled  = false;
   
    // Flag used to record if an updateFromFeed event is in process.
    private boolean updateFromLiveDataFeedInProcess = false;
    
    // Flag used to record if an updateData event is in process.
    private boolean updateFromCacheDataStreamInProcess = false;
    
    // Initially plots are set in DISPLAY_ONLY state. User actions are requested to
    // move them to another state. 
    PlotDisplayState plotDisplayState = PlotDisplayState.DISPLAY_ONLY;
    
    boolean legendDataValue = true;
    
    boolean isPaused = false;
    
    private ArrayList<PlotObserver> observers = new ArrayList<PlotObserver>();

    AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();

    /** The time and non time scroll mode as defined by the plots settings set by the user.
     *  The actual mode of the scroll frame may vary as we pan and zoom. However, these fundamental
     *  modes are cached here so we can restore to them instructed by user. 
     */
    TimeAxisSubsequentBoundsSetting timeScrollModeByPlotSettings;
    private boolean nonTimeMinFixedByPlotSettings;
    private boolean nonTimeMaxFixedByPlotSettings;
    
	private Axis nonTimeAxis = new Axis();

	private Pinnable nonTimeAxisUserPin = nonTimeAxis.createPin();

	private Pinnable nonTimePausePin;

	private Pinnable timePausePin;

	private boolean nonTimeMinFixed;
	private boolean nonTimeMaxFixed;
	
	private double oldMinNonTime = Double.POSITIVE_INFINITY;
	private double oldMaxNonTime = Double.NEGATIVE_INFINITY;


	public PlotterPlot() {
		plotPanel.setBackground(PlotConstants.DEFAULT_PLOT_FRAME_BACKGROUND_COLOR);
	}
    
	public void createChart(AxisOrientationSetting theAxisOrientation, 
			String theTimeSystem,
            String theTimeFormat,
			XAxisMaximumLocationSetting theXAxisSetting, 
			YAxisMaximumLocationSetting theYAxisSetting, 
			TimeAxisSubsequentBoundsSetting theTimeAxisSubsequentSetting, 
			NonTimeAxisSubsequentBoundsSetting theNonTimeAxisMinSubsequentSetting,
			NonTimeAxisSubsequentBoundsSetting theNonTimeAxisMaxSubsequentSetting,
			Font theTimeAxisFont, 
			int thePlotLineThickness,
			Color thePlotBackgroundFrameColor, 
			Color thePlotAreaBackgroundColor, 
			int theTimeAxisIntercept, 
			Color theTimeAxisColor, 
			Color theTimeAxisLabelColor, 
			Color theNonTimeAxisLabelColor,
			String theTimeAxisDateFormat, 
			Color theNonTimeAxisColor, 
			Color theGridLineColor,
			int theMinSamplesForAutoScale, 
			double theScrollRescaleTimeMargin,
			double theScrollRescaleNonTimeMinMargin,
			double theScrollRescaleNonTimeMaxMargin,
			double theNonTimeVaribleAxisMinValue, 
			double theNonTimeVaribleAxisMaxValue,
			long theTimeVariableAxisMinValue,
			long theTimeVariableAxisMaxValue,
			boolean isCompressionEnabled,
			boolean theIsTimeLabelEnabled,
			boolean theIsLocalControlsEnabled,
			boolean ordinalPositionInStackedPlot,
			PlotLineDrawingFlags thePlotLineDraw,
			PlotLineConnectionType thePlotLineConnectionType,
			PlotAbstraction thePlotAbstraction, 
			AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm) {

		axisOrientation = theAxisOrientation;
		timeSystemSetting = theTimeSystem;
        timeFormatSetting = theTimeFormat;
		xAxisSetting = theXAxisSetting;
		yAxisSetting = theYAxisSetting;
		timeAxisSubsequentSetting = theTimeAxisSubsequentSetting;
		nonTimeAxisMinSubsequentSetting = theNonTimeAxisMinSubsequentSetting;
		nonTimeAxisMaxSubsequentSetting = theNonTimeAxisMaxSubsequentSetting;
		this.ordinalPositionInStackedPlot = ordinalPositionInStackedPlot;

		timeAxisFont = theTimeAxisFont;
		plotLineThickness = thePlotLineThickness;
		plotBackgroundFrameColor = thePlotBackgroundFrameColor; 
		plotAreaBackgroundColor = thePlotAreaBackgroundColor;
		timeAxisIntercept = theTimeAxisIntercept; 
		timeAxisColor = theTimeAxisColor;
		timeAxisLabelColor = theTimeAxisLabelColor; 
		timeAxisDateFormat = theTimeAxisDateFormat; 
		nonTimeAxisColor = theNonTimeAxisColor;
		nonTimeAxisLabelColor = theNonTimeAxisLabelColor;
		gridLineColor = theGridLineColor;
		scrollRescaleMarginTime = theScrollRescaleTimeMargin;
		scrollRescaleMarginNonTimeMin = theScrollRescaleNonTimeMinMargin;
		scrollRescaleMarginNonTimeMax = theScrollRescaleNonTimeMaxMargin;
		nonTimeVaribleAxisMinValue = theNonTimeVaribleAxisMinValue;
		nonTimeVaribleAxisMaxValue = theNonTimeVaribleAxisMaxValue;
		timeVariableAxisMinValue = theTimeVariableAxisMinValue;
		timeVariableAxisMaxValue = theTimeVariableAxisMaxValue;
		compressionIsEnabled = isCompressionEnabled;
		isTimeLabelEnabled = theIsTimeLabelEnabled;
		isLocalControlsEnabled = theIsLocalControlsEnabled;
		plotAbstraction = thePlotAbstraction;
		plotLabelingAlgorithm = thePlotLabelingAlgorithm;
		plotLineDraw = thePlotLineDraw;
		plotLineConnectionType = thePlotLineConnectionType;

		if (theTimeVariableAxisMaxValue <= theTimeVariableAxisMinValue) {
			throw new IllegalArgumentException ("Time axis max value is less than and not equal to the min value");
		}
	
	    // Create the quinn curtis objects that make up the
	    // physical plot. 
	    setupPlotObjects();

		// Setup the limit manager. 
	    setupLimitManager();

	    // Setup action listeners
	    setupListeners();
	   
	    // setup the legends.
	    setupLegends();
		
		// Setup the data cursor
		setupDataCursor();	
		
	    setupLocalControlManager();
	    
	    setupCornerResetButtonManager();
		
		// Setup time sync line
		setupTimeSyncLine();
		
		// Layout the plot area. 
		calculatePlotAreaLayout();
		
		nonTimeAxisMinPhysicalValue = logicalToPhysicalY(nonTimeVaribleAxisMinValue);
		nonTimeAxisMaxPhysicalValue = logicalToPhysicalY(nonTimeVaribleAxisMaxValue);
		
	}
	
	private void setupPlotObjects() {
		qcPlotObjects = new QCPlotObjects(this);	
	}
	
	private void setupListeners() {
		plotActionListener = new PlotViewActionListener(this);
		plotDataManager = new PlotDataManager(this);
	}

	private void setupCornerResetButtonManager(){
		cornerResetButtonManager = new PlotCornerResetButtonManager(this);
	}
	
	/**
	 * Get the plot panel associated with this plot.
	 * @return the plot panel
	 */
	public JComponent getPlotPanel() {
		return plotPanel;
	}	

	private void setupLegends() {
		// Link the plot legend background color to the plot background frame color.
		assert plotLabelingAlgorithm != null : "Plot labeling algorithm should NOT be NULL at this point.";
		legendManager = new LegendManager(this, plotBackgroundFrameColor, plotLabelingAlgorithm);
		SpringLayout layout = (SpringLayout) plotView.getLayout();
		layout.putConstraint(SpringLayout.WEST, legendManager, PlotConstants.PLOT_LEGEND_OFFSET_FROM_LEFT_HAND_SIDE, SpringLayout.WEST, plotView);
		layout.putConstraint(SpringLayout.NORTH, legendManager, 0, SpringLayout.NORTH, plotView.getContents());
	}
	
	private void setupTimeSyncLine() { 
		  timeSyncLine = new PlotTimeSyncLine(this);
	}
	
	private void setupLimitManager() {
		limitManager.setupLimitButtons();
	}

	private void setupLocalControlManager() {
		getLocalControlsManager().setupLocalControlManager();
	}
	
	private void setupDataCursor() {
		assert plotView !=null : "Plot Object not initalized";
		
		dataCursor = new PlotDataCursor(this);
	}

	/**
	 * Get the number of pixels across the plot's span.
	 * @return
	 */
	int getPlotTimeWidthInPixels() {
		// TODO: See if this is the content area or the whole plot
		if (axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			return plotView.getContents().getWidth();
		} else {
			return plotView.getContents().getHeight();
	    }
	}
	

    @Override
	public void addDataSet(String dataSetName, Color plottingColor) {
    	plotDataManager.addDataSet(dataSetName, plottingColor);
		this.refreshDisplay();
	}
	
    
    @Override
	public void addDataSet(String dataSetName, Color plottingColor, String displayName) {
    	/* If this is a duplicate data set, render it with the already-defined color */ 
    	if (plotDataManager.dataSeries.containsKey(dataSetName)) {
    		plottingColor = plotDataManager.dataSeries.get(dataSetName).getColor();    		
    	}
    	if ( (dataSetName != null) && (displayName != null)) {
    	
    		plotDataManager.addDataSet(dataSetName, plottingColor, displayName); 
    		
    		if (plotDataManager.dataSeries.get(dataSetName) != null) {
    			legendManager.addLegendEntry(plotDataManager.dataSeries.get(dataSetName).getLegendEntry());
    			refreshDisplay();
    		} else {
    			logger.error("Legend entry or data series is null!");
    		}
    	
    	} else {
    		logger.error("Data set and display name are null for plot.");
    	}
	}
	
    @Override
	public boolean isKnownDataSet(String setName) {
		return plotDataManager.isKnownDataSet(setName);
	}


    @Override
    public void addData(String feed, SortedMap<Long, Double> points) {
		plotDataManager.addData(feed, points);
    	cornerResetButtonManager.updateButtons();
    }
    
    @Override
    public void addData(String feed, long time, double value) {
    	TreeMap<Long, Double> m = new TreeMap<Long, Double>();
    	m.put(time, value);
		plotDataManager.addData(feed, m);
    }

    @Override
    public void updateLegend(String dataSetName, FeedProvider.RenderingInfo info) {
    	if (dataSetName != null)
    		plotDataManager.updateLegend(dataSetName, info);	
    }
	
    @Override
	public void refreshDisplay() {		
		assert plotView !=null : "Plot Object not initalized";
	}
    
    @Override
	public int getDataSetSize() {
		return plotDataManager.getDataSetSize();
	}
    
	@Override
	public LimitAlarmState getNonTimeMaxAlarmState() {
		return limitManager.nonTimeMaxAlarm;
	}

	@Override
	public LimitAlarmState getNonTimeMinAlarmState() {
		return limitManager.nonTimeMinAlarm;
	}

	@Override
	public long getCurrentTimeAxisMinAsLong() {
		return Math.min(theTimeAxis.getStartAsLong(), theTimeAxis.getEndAsLong());
	}
	
	@Override
	public GregorianCalendar getCurrentTimeAxisMin() {
		long time = getCurrentTimeAxisMinAsLong();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		return cal;
	}
	
	@Override
	public long getCurrentTimeAxisMaxAsLong() {
		return Math.max(theTimeAxis.getStartAsLong(), theTimeAxis.getEndAsLong());
	}
	
	@Override
	public GregorianCalendar getCurrentTimeAxisMax() {
		long time = getCurrentTimeAxisMaxAsLong();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		return cal;
	}
	
	@Override
	public double getCurrentNonTimeAxisMin() {
		return Math.min(theNonTimeAxis.getStart(), theNonTimeAxis.getEnd());
	}
	
	@Override
	public double getCurrentNonTimeAxisMax() {
		return Math.max(theNonTimeAxis.getStart(), theNonTimeAxis.getEnd());
	}
	
	@Override
	public void showTimeSyncLine(GregorianCalendar time) {	 
		if (isInitialized()) {
		timeSyncLine.drawTimeSyncLineAtTime(time);
		}
	}

	@Override
	public void removeTimeSyncLine() {
		timeSyncLine.removeTimeSyncLine();		
	}
	
	@Override
	public boolean isTimeSyncLineVisible() {
	  return timeSyncLine.timeSyncLineVisible(); 
	}

	boolean isInitialized() {
		return isInitialized;
	}
	
	private void informTimeAxisPinned(boolean pinned) {
        minMaxChanged();
	}
	
	void setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting scrollMode) {
		this.timeAxisSubsequentSetting = scrollMode;
	}
	
	void setNonTimeMinFixed(boolean fixed) {
		this.nonTimeMinFixed = fixed;
		if (!fixed) {
			adjustAxisMin();
		}
	}

	void setNonTimeMaxFixed(boolean fixed) {
		this.nonTimeMaxFixed = fixed;
		if (!fixed) {
			adjustAxisMax();
		}
	}

	boolean isNonTimeMinFixed() {
		return nonTimeMinFixed;
	}

	boolean isNonTimeMaxFixed() {
		return nonTimeMaxFixed;
	}

	void setNonTimeMinFixedByPlotSettings(boolean nonTimeMinFixedByPlotSettings) {
		this.nonTimeMinFixedByPlotSettings = nonTimeMinFixedByPlotSettings;
	}

	void setNonTimeMaxFixedByPlotSettings(boolean nonTimeMaxFixedByPlotSettings) {
		this.nonTimeMaxFixedByPlotSettings = nonTimeMaxFixedByPlotSettings;
	}

	boolean isNonTimeMinFixedByPlotSettings() {
		return nonTimeMinFixedByPlotSettings;
	}

	boolean isNonTimeMaxFixedByPlotSettings() {
		return nonTimeMaxFixedByPlotSettings;
	}
	
	void resetNonTimeMax() {
		adjustAxis(getCurrentNonTimeAxisMin(), getInitialNonTimeMaxSetting());
	}
	
	void resetNonTimeMin() {
		adjustAxis(getInitialNonTimeMinSetting(), getCurrentNonTimeAxisMax());
	}
	
	@Override
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMaxSetting() {
		return nonTimeAxisMaxSubsequentSetting;
	}

	@Override
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMinSetting() {
		return nonTimeAxisMinSubsequentSetting;
	}

	@Override
	public double getInitialNonTimeMaxSetting() {
		return nonTimeVaribleAxisMaxValue;
	}

	@Override
	public double getNonTimeMaxPadding() {
		return scrollRescaleMarginNonTimeMax;
	}

	@Override
	public double getInitialNonTimeMinSetting() {
		return nonTimeVaribleAxisMinValue;
	}

	@Override
	public double getNonTimeMinPadding() {
		return scrollRescaleMarginNonTimeMin;
	}

	@Override
	public AxisOrientationSetting getAxisOrientationSetting() {
		return axisOrientation;
	}

	@Override
	public TimeAxisSubsequentBoundsSetting getTimeAxisSubsequentSetting() {
		return timeAxisSubsequentSetting;
	}

	@Override
	public long getInitialTimeMaxSetting() {
		return timeVariableAxisMaxValue;
	}

	@Override
	public long getInitialTimeMinSetting() {
		return timeVariableAxisMinValue;
	}

	@Override
	public double getTimePadding() {
		return scrollRescaleMarginTime;
	}

	@Override
	public XAxisMaximumLocationSetting getXAxisMaximumLocation() {
		return xAxisSetting;
	}

	@Override
	public YAxisMaximumLocationSetting getYAxisMaximumLocation() {
        return yAxisSetting;
	}	
	
	void initiateGlobalTimeSync(GregorianCalendar time) {
		if (plotAbstraction != null) {
			plotAbstraction.initiateGlobalTimeSync(time);
		}
	}
	
	@Override
	public void setPlotView(PlotAbstraction plotView) {
		plotAbstraction = plotView;
	}
	
	@Override
	public void notifyGlobalTimeSyncFinished() {
		if (plotAbstraction != null) {
		  plotAbstraction.notifyGlobalTimeSyncFinished();
		}
    }
	@Override
	public boolean inTimeSyncMode() {
		return timeSyncLine.inTimeSyncMode();
	}
		
	@Override
	public double getNonTimeMaxDataValueCurrentlyDisplayed() {
		return plotDataManager.getNonTimeMaxDataValueCurrentlyDisplayed();	
	}
	
	@Override
	public double getNonTimeMinDataValueCurrentlyDisplayed() {
	   return plotDataManager.getNonTimeMinDataValueCurrentlyDisplayed();
	}	

	@Override
	public void informUpdateCachedDataStreamStarted() {
		setUpdateFromCacheDataStreamInProcess(true);
		plotDataManager.informUpdateCacheDataStreamStarted();
	}
		
	@Override
	public void informUpdateCacheDataStreamCompleted() {
		setUpdateFromCacheDataStreamInProcess(false);
		plotDataManager.informUpdateCacheDataStreamCompleted();
	}
	
	@Override
	public void informUpdateFromLiveDataStreamStarted() {
		setUpdateFromLiveDataStreamInProcess(true);
		plotDataManager.informUpdateFromLiveDataStreamStarted();
	}
	
	@Override
    public void informUpdateFromLiveDataStreamCompleted() {
		setUpdateFromLiveDataStreamInProcess(false);
		plotDataManager.informUpdateFromLiveDataStreamCompleted();
	}
	
	void setUpdateFromLiveDataStreamInProcess(boolean state) {
    	updateFromLiveDataFeedInProcess = state;
    }
	
	void setUpdateFromCacheDataStreamInProcess(boolean state) {
		updateFromCacheDataStreamInProcess = state;	 
		if (state) {
			legendDataValue = false;
		} else {
			legendDataValue = true;
		}
	}
	
	boolean isUpdateFromCacheDataStreamInProcess() {
		return updateFromCacheDataStreamInProcess;
	}
	
	boolean isUpdateFromLiveDataStreamInProcess() {
		return updateFromLiveDataFeedInProcess;
	}
 
	/**
	 * Set the pause state of the plot. If the  mode is set to true data will continue to accumulate in the plot's buffer
	 * but it will not be plotted. If the plot is already paused, it will not repause. If the
     * plot is already running, it will not rerun.
	 * @param pause true to pause the plot, false to run it.
	 */
	public void pause(boolean pause) {
		boolean oldState = isPaused;
		isPaused = pause;
		if (pause && !oldState) {
		    pausePlot();
		} else if (!pause && oldState ) {
			unpausePlot();
		} 
	}	
	
	
	private void pausePlot() {
		logger.debug("Plot pause called");
		// pause the nontime axis scrollpane. 

		Axis timeAxis = plotAbstraction.getTimeAxis();
		if(timePausePin == null) {
			timePausePin = timeAxis.createPin();
		}
		if(nonTimePausePin == null) {
			nonTimePausePin = nonTimeAxis.createPin();
		}
		nonTimePausePin.setPinned(true);
		timePausePin.setPinned(true);
		setNonTimeMinFixed(true);
		setNonTimeMaxFixed(true);
		getLocalControlsManager().updatePinButton();
	}

	private void unpausePlot() {
		if(timePausePin != null) {
			timePausePin.setPinned(false);
		}
		if(nonTimePausePin != null) {
			nonTimePausePin.setPinned(false);
		}
		getLocalControlsManager().updatePinButton();
			
		// If the user not reset the nontime axis we'll reset the scroll mode.
		if(nonTimeAxis.isInDefaultState()) {
			if (!limitManager.isEnabled()) {
				  limitManager.setEnabled(true);
			}		
			setNonTimeMinFixed(nonTimeMinFixedByPlotSettings);
			setNonTimeMaxFixed(nonTimeMaxFixedByPlotSettings);
		} 
		
	    // when we unpause, we want to fast forward the display to the current time.
		Axis timeAxis = plotAbstraction.getTimeAxis();
		setPlotDisplayState(PlotDisplayState.DISPLAY_ONLY);
		// no that we have fast forwarded to the current time, set back to display mode.
		informTimeAxisPinned(timeAxis.isPinned());
		notifyObserversTimeChange();	
		updateResetButtons();
	}
		
	/**
	 * Query the paused state of the plot.
	 * @return true if the plot is paused, false otherwise. 
	 */
	public boolean isPaused() {
	  return isPaused;
	}
	
	
	/**
	 * Calculate the location of the plot area within the graph area. Allow legends to take up as much
	 * space as they require until the plot reduces below PlotConstants.MINIMUM_PLOT_WIDTH.
	 */
	void calculatePlotAreaLayout() {
		double totalPlotWindowWidth = plotView.getSize().getWidth();
		double preferedLegendWidth = PlotConstants.PLOT_LEGEND_WIDTH;

		// If time is on the X axis then we set a fixed width to the y-axis labels. This
		// satisfies the requirements that the plots LHS line up. 
		yAxisAxisLabelWidth  = plotView.getYAxis().getPreferredSize().width;
        xAxisAxisLabelHeight = getXAxisLabelHeight();
                
		double totalLegendPlusAxisLabelPlusBufferWidth = preferedLegendWidth + yAxisAxisLabelWidth + PlotConstants.LOCAL_CONTROL_WIDTH 
		                                                 + PlotConstants.PLOT_LEGEND_BUFFER;
		
		boolean wasVisible = legendManager.isVisible();
		double spaceForPlot = totalPlotWindowWidth - totalLegendPlusAxisLabelPlusBufferWidth;
		int    minWidth = (getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) ?
				PlotConstants.MINIMUM_PLOT_WIDTH : PlotConstants.MINIMUM_PLOT_HEIGHT;
		// We never allows the plot area rectangle to go below PlotConstants.MINIMUIM_PLOT_WIDTH.
		if (spaceForPlot  < minWidth) {      
			// The plot area is below the minimum plot width. We now need to either shrink the legends
			// or make them invisible. 
		
			
			if (legendManager.getPreferredSize().getWidth() < PlotConstants.PLOT_MINIMUM_LEGEND_WIDTH) {
				// make the legend invisible.
				legendManager.setVisible(false);
			} else {
			   // shrink the legend a little. 
			   legendManager.setVisible(true);
			}
		} else {
			// no space problems, set the legend to its full size and insure it is visible. 
			legendManager.setVisible(true);
		}

		boolean visible = legendManager.isVisible();
		if(wasVisible != visible) {
			XYAxis yAxis = plotView.getYAxis();
			SpringLayout layout = (SpringLayout) plotView.getLayout();
			if (getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
				if(visible) {
					layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.EAST, legendManager);
				} else {
					layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.WEST, plotView);
				}
			} else {
				/* Make room for axis labels, but only if they're non-empty */
				if (yAxis.getComponentCount() > 0) {
					layout.putConstraint(SpringLayout.WEST, yAxis, 2, SpringLayout.EAST, legendManager);
				} else {
					/* Otherwise, push up against the plot contents */
					layout.putConstraint(SpringLayout.WEST, legendManager, 5, SpringLayout.WEST, plotView);
					layout.putConstraint(SpringLayout.WEST, yAxis, 2, SpringLayout.EAST, legendManager);
				}
			}
		}
	}
	
	/**
	 * Gets the Y-Axis label width. Defaults to 30 pixels.
	 * @return width 30 pixels; otherwise 0 if time label is not enabled.
	 */
	double getYAxisLabelWidth() {
		if (axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			return 30; // TODO
			//return theNonTimeAxis.getWidth();
		} else {
			if (isTimeLabelEnabled) {
				return 30; // TODO
				//return theTimeAxis.getWidth();
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * Gets the X-Axis label height. Defaults to 30 pixels.
	 * @return height 30 pixels; otherwise 0 if time label is not enabled.
	 */
	double getXAxisLabelHeight() {
		if (axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) { 
			if (isTimeLabelEnabled) { 
				return 30; // TODO
				//return theTimeAxis.getHeight();
			} else {
				return 0;
			}
		} else {
			return 30; // TODO
			//return theNonTimeAxis.getHeight();
		}
	}
	
	
	@Override
	public void setCompressionEnabled(boolean state) {
		compressionIsEnabled = state;  
	}

	@Override
	public boolean isCompresionEnabled() {
		return compressionIsEnabled;
	}
	
	/**
	 * Locks or unlocks mouse operations on the plot rectangle. This is used to prevent
	 * mouse operations such as the time sync line and slope line form interacting with each other. 
	 * @param state - lock or unlock.
	 */
    public void setUserOperationLockedState(boolean state){
    	userOperationsLocked = state;
    }
    
    /**
     * Checks whether user operation is locked or not. 
     * @return true if user operations are locked; false otherwise.
     */
    public boolean isUserOperationsLocked() { 
      return userOperationsLocked;
    }
    
    /**
     * Gets the plot display state.
     * @return plotDisplayState - the plot display state.
     */
    public PlotDisplayState getPlotDisplayState() {
    	return plotDisplayState;
    }
    
    /**
     * Sets the plot display state.
     * @param state - the plot display state.
     */
    public void setPlotDisplayState(PlotDisplayState state) {
    	plotDisplayState = state;
    }

 // Provide a text representation of the Plot and its data to facilitate debugging.
	@Override
	public String toString() {
		assert plotView !=null : "Plot Object not initalized";
		assert plotDataManager.dataSeries !=null : "Plot Data not initalized";

		final String DATE_FORMAT = TimeService.DEFAULT_TIME_FORMAT;
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		
		GregorianCalendar minTime = new GregorianCalendar();
		minTime.setTimeInMillis(timeVariableAxisMinValue);
		GregorianCalendar maxTime = new GregorianCalendar();
		maxTime.setTimeInMillis(timeVariableAxisMaxValue);
		
		StringBuilder stringRepresentation = new StringBuilder();
		stringRepresentation.append("Implmentation Package: Quinn-Curtis RT\n");
		stringRepresentation.append("Plot Configuration\n");
		stringRepresentation.append("  Axis Orientation: " + axisOrientation + "\n");
		stringRepresentation.append("  Time System: " + timeSystemSetting + "\n");
        stringRepresentation.append("  Time Format: " + timeFormatSetting + "\n");
		stringRepresentation.append("  X Axis Max: " + xAxisSetting + "\n");
		stringRepresentation.append("  Y Axis Max: " + yAxisSetting + "\n");
		stringRepresentation.append("  Time Axis Subsequent: " + timeAxisSubsequentSetting + "\n");
		stringRepresentation.append("  Non Time Subsequent - min: " +  nonTimeAxisMinSubsequentSetting + "\n");	
		stringRepresentation.append("  Non Time Subsequent - max: " +  nonTimeAxisMaxSubsequentSetting + "\n");	
		stringRepresentation.append("  Time Padding %: " + scrollRescaleMarginTime  + "\n");
		stringRepresentation.append("  Non Time Padding Min %: " + scrollRescaleMarginNonTimeMin  + "\n");
		stringRepresentation.append("  Non Time Padding Max %: " + scrollRescaleMarginNonTimeMax  + "\n");
		stringRepresentation.append("  Non Time Min: " + nonTimeVaribleAxisMinValue  + "\n");
		stringRepresentation.append("  Non Time Max: " + nonTimeVaribleAxisMaxValue  + "\n");
		stringRepresentation.append("  Time Min: " + dateFormat.format(minTime.getTime())  + "\n");
		stringRepresentation.append("  Time Max: " + dateFormat.format(maxTime.getTime())  + "\n");
		stringRepresentation.append("  Compression enabled: " + compressionIsEnabled  + "\n");
		

		stringRepresentation.append("Data in plot local buffer (size " + plotDataManager.dataSeries.size() + ")\n");
		Set<String> keys = plotDataManager.dataSeries.keySet();
		for(String key: keys) {
			PlotDataSeries series = plotDataManager.dataSeries.get(key);
			stringRepresentation.append("  RTProcessVar: " + key + " " + series.toString() + "\n");
		}
		stringRepresentation.append("\n");
		return stringRepresentation.toString();
	}

	/**
	 * Sets the time axis start and stop times.
	 * @param startTime time in millisecs.
	 * @param endTime time in millisecs.
	 */
	public void setTimeAxisStartAndStop(long startTime, long endTime) {
		assert startTime != endTime;
		for(PlotDataSeries d : plotDataManager.dataSeries.values()) {
			CompressingXYDataset data = d.getData();
			// if the min and max are reversed on the time axis, then the end may be < start time
			data.setTruncationPoint(Math.min(startTime,endTime));
		}
		theTimeAxis.setStart(startTime);
		theTimeAxis.setEnd(endTime);
	}

	@Override
	public void notifyObserversTimeChange() {
		for (PlotObserver o: observers) {
		   o.updateTimeAxis(this,
				   theTimeAxis.getStartAsLong(),
				   theTimeAxis.getEndAsLong());
		}
	}
	
	
	@Override
	public void registerObservor(PlotObserver o) {
		observers.add(o);	
	}

	@Override
	public void removeObserver(PlotObserver o) {
		observers.remove(o);
	}


	@Override
	public void clearAllDataFromPlot() {
		plotDataManager.resetPlotDataSeries();
	}

	@Override
	public boolean getOrdinalPositionInStackedPlot() {
		return ordinalPositionInStackedPlot;
	}
	
	@Override
	public Axis getNonTimeAxis() {
		return nonTimeAxis;
	}
	
	/**
	 * Gets the non-time pinnable axis by user. 
	 * @return pinnable non-time axis.
	 */
	public Pinnable getNonTimeAxisUserPin() {
		return nonTimeAxisUserPin;
	}

	@Override
	public void updateResetButtons() {
		cornerResetButtonManager.updateButtons();
	}

	@Override
	public AbbreviatingPlotLabelingAlgorithm getPlotLabelingAlgorithm() {
		return plotLabelingAlgorithm;
	}

	@Override
	public void setPlotLabelingAlgorithm(AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm) {
		plotLabelingAlgorithm = thePlotLabelingAlgorithm; 
	}

	/**
	 * Gets the 2D rectangle.
	 * @return 2D rectangle.
	 */
	public Rectangle2D getContentRect() {
		return plotView.getContents().getBounds();
	}

	/**
	 * Gets the legend manager instance.
	 * @return legendManager the legend manager instance.
	 */
	public LegendManager getLegendManager() {
		return legendManager;
	}

	/** 
	 * Make sure the axis is adjusted to show the data points if applicable.
	 * @param time
	 * @param value
	 */
	void newPointPlotted(long time, 
						double value) {
		// ? time parameter is not used.
		if (
				((nonTimeAxisMaxSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED && value > getCurrentNonTimeAxisMax() && !nonTimeMaxFixed) ||
				(nonTimeAxisMinSubsequentSetting == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED && value < getCurrentNonTimeAxisMin() && !nonTimeMinFixed))
			) {
				adjustAxis(Math.min(oldMinNonTime, getCurrentNonTimeAxisMin()), Math.max(oldMaxNonTime, getCurrentNonTimeAxisMax()));
		}
	}
	
	private void adjustAxisMin() {
		adjustAxis(calculateMinNonTimeWithPadding(oldMinNonTime, getCurrentNonTimeAxisMax(), getCurrentNonTimeAxisMin()), getCurrentNonTimeAxisMax());
	}
	
	private void adjustAxisMax() {
		adjustAxis(getCurrentNonTimeAxisMin(), calculateMaxNonTimeWithPadding(oldMaxNonTime,getCurrentNonTimeAxisMin(),getCurrentNonTimeAxisMax()));
	}
	
	private void adjustAxis(double min, double max) {
		boolean inverted;
		if(axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			inverted = yAxisSetting == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
		} else {
			inverted = xAxisSetting == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
		}
		if(inverted) {
			theNonTimeAxis.setStart(max);
			theNonTimeAxis.setEnd(min);
			// cache new max and min physical pixel values
			nonTimeAxisMinPhysicalValue = logicalToPhysicalY(max);
			nonTimeAxisMaxPhysicalValue = logicalToPhysicalY(min);			
		} else {
			theNonTimeAxis.setStart(min);
			theNonTimeAxis.setEnd(max);
			// cache new max and min physical pixel values
			nonTimeAxisMinPhysicalValue = logicalToPhysicalY(min);
			nonTimeAxisMaxPhysicalValue = logicalToPhysicalY(max);
		}
	}
	
	private double calculateMaxNonTimeWithPadding(double maxNonTime, double min, double originalMax) {

		double max = maxNonTime;
		double padding = scrollRescaleMarginNonTimeMax;
		
		if(maxNonTime > nonTimeVaribleAxisMaxValue) {
			if (max - min == 0) {
				max += 1;
			}
			if (padding > 0) {
				max = (max - min) * (1 + padding) + min;
			}
		} else {
			if (maxNonTime == Double.NEGATIVE_INFINITY) {
				max = nonTimeVaribleAxisMaxValue;
			} else { // The non-Time max should be reduced if time frame is shifted
				max = (maxNonTime - min)*(1 + padding) + min;
			}
		} 

		return max;
	}
	
	private double calculateMinNonTimeWithPadding(double minNonTime, double max, final double originalMin) {

		double min = minNonTime; // Start new non-Time min at new lowest value
		double padding = scrollRescaleMarginNonTimeMin;
		if (minNonTime < nonTimeVaribleAxisMinValue) {
			if (max - min == 0) {
				min -= 1;
			}
			if (padding > 0) {
				min = max - (max - min) * (1 + padding);
			}
		} else {
			if (minNonTime == Double.POSITIVE_INFINITY) {
				min = nonTimeVaribleAxisMinValue;
			} else {  // The non-Time min should be increased if time frame is shifted
				min = max - (max - minNonTime) * (1 + padding);
			}
		}
		
		return min;
	}
	
	/**
	 * Minimal/Maximum has changed.
	 */
	void minMaxChanged() {
		double minNonTime = Double.POSITIVE_INFINITY;
		double maxNonTime = Double.NEGATIVE_INFINITY;
		if(axisOrientation == AxisOrientationSetting.X_AXIS_AS_TIME) {
			for(PlotDataSeries d : plotDataManager.dataSeries.values()) {
				CompressingXYDataset data = d.getData();
				minNonTime = Math.min(minNonTime, data.getMinY());
				maxNonTime = Math.max(maxNonTime, data.getMaxY());
			}
		} else {
			for(PlotDataSeries d : plotDataManager.dataSeries.values()) {
				CompressingXYDataset data = d.getData();
				minNonTime = Math.min(minNonTime, data.getMinX());
				maxNonTime = Math.max(maxNonTime, data.getMaxX());
			}
		}

		if ((minNonTime != oldMinNonTime || maxNonTime != oldMaxNonTime) 
				&& !isPaused && !nonTimeAxis.isPinned()) {
			double start = theNonTimeAxis.getStart();
			double end = theNonTimeAxis.getEnd();
			double min = Math.min(start, end);
			double max = Math.max(start, end);
			assert max >= min;
			if (minNonTime <= min || 
					Math.abs(nonTimeAxisMinPhysicalValue - logicalToPhysicalY(minNonTime)) <= 1
					) {  // don't adjust min unless at or below axis min
				if(!nonTimeMinFixed) {
					min = calculateMinNonTimeWithPadding(minNonTime, maxNonTime, min);
				}
			}
			if (maxNonTime >= min ||
					Math.abs(nonTimeAxisMaxPhysicalValue - logicalToPhysicalY(maxNonTime)) <= 1
					) {  // don't adjust max unless at or above axis max
				if(!nonTimeMaxFixed) {
					max = calculateMaxNonTimeWithPadding(maxNonTime, minNonTime, max);
				}
			}
			if ((minNonTime <= min && !nonTimeMinFixed) || 
					(maxNonTime >= min && !nonTimeMaxFixed)) {
				adjustAxis(min,max);
			}
		}

		oldMinNonTime = minNonTime;
		oldMaxNonTime = maxNonTime;

	}
	
	/** Convert an input logical value to a physical value  by using
	 * Point2D (value as Y coordinate) and plot package transformation.
	 * @param logicalValue
	 * @return physical value
	 */
	private double logicalToPhysicalY(double logicalValue) {
		Point2D physicalPt = new Point2D.Double(0,logicalValue);
		if (plotView != null) {
			plotView.toPhysical(physicalPt, physicalPt);
			return physicalPt.getY();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the time axis.
	 * @return X-Y time axis.
	 */
	public TimeXYAxis getTimeAxis() {
		return theTimeAxis;
	}

	/**
	 * Sets the X-Y time axis.
	 * @param axis X-Y time axis
	 */
	void setTimeAxis(TimeXYAxis axis) {
		theTimeAxis = axis;
		plotAbstraction.setPlotTimeAxis(axis);
	}


	@Override
	public void setTruncationPoint(double min) {
		for(PlotDataSeries d : plotDataManager.dataSeries.values()) {
			CompressingXYDataset data = d.getData();
			data.setTruncationPoint(min);
		}
	}


	@Override
	public void updateCompressionRatio() {
		plotDataManager.setupCompressionRatio();
	}

	public PlotLocalControlsManager getLocalControlsManager() {
		return localControlsManager;
	}

	public void setLocalControlsManager(PlotLocalControlsManager localControlsManager) {
		this.localControlsManager = localControlsManager;
	}
	
	public String getTimeSystemSetting() {
		return timeSystemSetting;
	}

	public String getTimeFormatSetting() {
		return timeFormatSetting;
	}

	public static NumberFormat getNumberFormatter(double value) {
		NumberFormat format = PlotConstants.DECIMAL_FORMAT;

		try {
			if ( (value >= PlotConstants.MILLION_VALUES) || (value <= PlotConstants.NEGATIVE_MILLION_VALUES) ) {
				format = new DecimalFormat(PlotConstants.SCIENTIFIC_NUMBER_FORMAT);
			}
		} catch (NumberFormatException nfe) {
			logger.error("NumberFormatException in very large numbers: {}", nfe);
		}
        return format;
	}
}
