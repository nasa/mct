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
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.settings.PlotSettingsControlContainer;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.utils.ComponentTraverser;
import gov.nasa.arc.mct.gui.FeedView;
import gov.nasa.arc.mct.gui.FeedView.RenderingCallback;
import gov.nasa.arc.mct.gui.NamingContext;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Components have PlotViewRoles. Each PlotViewRole may appear in many places. Each is a PlotViewManifestation. 
 */
@SuppressWarnings("serial")
public class PlotViewManifestation extends FeedView implements RenderingCallback { 
	private final static Logger logger = LoggerFactory.getLogger(PlotViewManifestation.class);
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	private JPanel theView;
	private List<String> canvasContextTitleList = new ArrayList<String>();
	private List<String> panelContextTitleList = new ArrayList<String>();
	private Color plotFrameBackground;
	
	private PlotView thePlot;
	private PlotDataAssigner plotDataAssigner = new PlotDataAssigner(this);
	private PlotDataFeedUpdateHandler plotDataFeedUpdateHandler = new PlotDataFeedUpdateHandler(this);
	private PlotPersistenceHandler plotPersistenceHandler = new PlotPersistenceHandler(this);

	private SwingWorker<Map<String, List<Map<String, String>>>, Map<String, List<Map<String, String>>>> currentDataRequest;
	private SwingWorker<Map<String, List<Map<String, String>>>, Map<String, List<Map<String, String>>>> currentPredictionRequest;

	private List<Runnable> feedCallbacks = new ArrayList<Runnable>();
	
	JComponent controlPanel;
	public static final String VIEW_ROLE_NAME =  "Plot";
	
	/** This listens to key events for the plot view and all sub-components so it can forward modifier key presses and releases to the local controls managers. */
	private KeyListener keyListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
		}


		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				for(AbstractPlottingPackage p : thePlot.subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informCtlKeyState(false);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_ALT) {
				for(AbstractPlottingPackage p : thePlot.subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informAltKeyState(false);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
				for(AbstractPlottingPackage p : thePlot.subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informShiftKeyState(false);
				}
			}
		}


		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				for(AbstractPlottingPackage p : thePlot.subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informCtlKeyState(true);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_ALT) {
				for(AbstractPlottingPackage p : thePlot.subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informAltKeyState(true);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
				for(AbstractPlottingPackage p : thePlot.subPlots) {
					((PlotterPlot) p).getLocalControlsManager().informShiftKeyState(true);
				}
			}
		}
	};

	public PlotViewManifestation(AbstractComponent component, ViewInfo vi) {
		super(component,vi);
		
		plotFrameBackground = getColor("plotFrame.background");
		if (plotFrameBackground == null) plotFrameBackground = PlotConstants.DEFAULT_PLOT_FRAME_BACKGROUND_COLOR;
		
		plotLabelingAlgorithm.setName("plotLabelingAlgorithm");
		setLabelingContext(plotLabelingAlgorithm, getNamingContext());
		
		// Generate the plot (& connect it to feeds, etc) 
		generatePlot();
		setFocusable(true);
		addKeyListener(keyListener);
		assert thePlot != null : "Plot should not be null at this point";		
	}

	/**
	 * Retrieval of a per-component feed provider is done by getting a list of FeedProvider capabilities, and filtering
	 * by the view-state. Thus if the component has capabilities  "A", "B" and "C", and current view-state is "A", a feed provider associated 
	 * with time system A is returned.
	 * 
	 * The determination of the view-state from the state data model, a combination of persisted state and controller state. 
	 * Otherwise, initialize the filter from the default time system for the feed providers that have been assigned to the plot.
	 * @param component - AbstractComponent
	 */
	@Override
	public FeedProvider getFeedProvider(AbstractComponent component) {
	
		List<FeedProvider> feedProviders = component.getCapabilities(FeedProvider.class);
	
		String viewStateFilter = null;
		PlotConfiguration settings = plotPersistenceHandler.loadPlotSettingsFromPersistance();
		String persistedState = settings != null ? settings.getTimeSystemSetting() : null; 
		String assignedComponentState = (plotDataAssigner != null) ? plotDataAssigner.getTimeSystemDefaultChoice() : null; 
	
		if (persistedState != null && !persistedState.isEmpty()) {
			viewStateFilter = persistedState;
		} else {
			// We do not yet have persisted state nor controller state; init by component type. Eg ERT for chill or GMT for example
			viewStateFilter = assignedComponentState; 
		}

		if (viewStateFilter != null && feedProviders != null && feedProviders.size() > 0) {
			for (FeedProvider fp : feedProviders) {
				String timeSystem = fp.getTimeService().getTimeSystemId();
				if (viewStateFilter.equals(timeSystem) || TimeService.WILDCARD_SERVICE_ID.equals(timeSystem)) {
					return fp;
				} 
			}
		}
		return component.getCapability(FeedProvider.class);
	}
	
	@Override
	protected void handleNamingContextChange() {
		updateMonitoredGUI();
	}
	
	private Color getColor(String name) {
        return UIManager.getColor(name);        
    }
	
	@Override
	protected JComponent initializeControlManifestation() {
		controlPanel = new PlotSettingsControlContainer(this);
		return controlPanel;
	}

	/**
	 * Create plot with specified settings and persist setting.
	 */
	public void setupPlot(PlotSettings settings) {

		// Persist plot setting and rely on updatedMoinitoredGUI to update this (and all other) manifestations.
		plotPersistenceHandler.persistPlotSettings(settings);    
	}
	
	/**
	 * Persist plot line settings (color, etc)
	 */
	public void persistPlotLineSettings() {
		if (thePlot != null)
			plotPersistenceHandler.persistLineSettings(thePlot.getLineSettings());

	}
	
	@Override
	public void updateMonitoredGUI() {	
		setLabelingContext(plotLabelingAlgorithm, getNamingContext());
		if (thePlot != null) {
			// the ordinal position may have changed so ensure the children are also up to date
			respondToSettingsChange();
		} 
	}
	
	@Override
	public void updateMonitoredGUI(PropertyChangeEvent evt) {
		updateMonitoredGUI();
	}
	
	@Override
	public void updateMonitoredGUI(AddChildEvent event) {
		setLabelingContext(plotLabelingAlgorithm, getNamingContext());
		respondToChildChangeEvent(); 
	}

	@Override
	public void updateMonitoredGUI(RemoveChildEvent event) {
		setLabelingContext(plotLabelingAlgorithm, getNamingContext());
		respondToChildChangeEvent();  
	}

	private void respondToChildChangeEvent() {
		generatePlot();
	}
	
	private void respondToSettingsChange() {
		generatePlot();
		if (controlPanel != null) {
			//controlPanel.updateControlsToMatchPlot();
		}
	}
	
	public String[] getTimeSystemChoices() {
		Set<String> s = plotDataAssigner.getTimeSystemChoices();
		return (String[])s.toArray(new String[s.size()]);
	}
 	
	public String[] getTimeFormatChoices() {
		Set<String> s = plotDataAssigner.getTimeFormatChoices();
		return (String[])s.toArray(new String[s.size()]);
	}

	private void generatePlot() {
		plotDataAssigner.informFeedProvidersHaveChanged();
		createPlotAndAddItToPanel();
		thePlot.initialDataRequest();
		plotDataAssigner.assignFeedsToSubPlots();		
		enforceBackgroundColor(plotFrameBackground);
		thePlot.addPopupMenus();
		thePlot.setLineSettings(plotPersistenceHandler.loadLineSettingsFromPersistence());
		//thePlot.setRegressionPointAssignments(plotPersistenceHandler.loadRegressionSettingsFromPersistence());
	}
	
	@Override
	public Collection<FeedProvider> getVisibleFeedProviders() {
		return plotDataAssigner.getVisibleFeedProviders();
	}

	
	private long getPointTime(Map<String,String> data) {
		return Long.parseLong(data.get(FeedProvider.NORMALIZED_TIME_KEY));
	}
	
	/*
	 * This method expands the data points before compression. This ensures the plot looks the
	 * same when retrieving data from the buffer which may be sparse and when getting data 
	 * directly in the stream which is returned in one second intervals. Differences occur when the data has few changes, as the data are 
	 * far apart in time and may not connect. For example, if there is a data point at time 1 and then 
	 * another data point at time 100, if there was a loss of service between the points there would be no
	 * connection (it is not possible to connect two points with an intervening LOS). This method will
	 * duplicate the points at one second intervals, which is what happens in the live stream.
	 */
	private void expandData(Map<String, List<Map<String, String>>> expandedData,
			final long startTime, final long endTime) {
		for (FeedProvider fp:getVisibleFeedProviders()) {
			List<Map<String,String>> points = expandedData.get(fp.getSubscriptionId());
			if (points != null && !points.isEmpty()) {
				
				if (fp.isNonCODDataBuffer()) {
                    continue;
				}

				List<Map<String,String>> expandedPoints = new ArrayList<Map<String,String>>();
				expandedData.put(fp.getSubscriptionId(), expandedPoints);
				long now = fp.getTimeService().getCurrentTime();
				for (int i = 0; i < points.size(); i++) {
					Map<String,String> point = points.get(i);
					expandedPoints.add(point);
					long pointTime = getPointTime(point);
					assert pointTime >= startTime: "point time is less than start time";
					pointTime = Math.max(pointTime, startTime);
					long nextPointTime = (points.size() > i+1) ? getPointTime(points.get(i+1)) : Math.min(now, endTime) + 1000;
					
					// go through each point get the starting value and then repeat the last 
					// point at one second intervals
					for (long currentTime = pointTime+1000; currentTime <= nextPointTime - 1000; currentTime+=1000) {
						Map<String,String> newPoint = new HashMap<String, String>(point);
						newPoint.put(FeedProvider.NORMALIZED_TIME_KEY, Long.toString(currentTime));
						expandedPoints.add(newPoint);
					}
				}
			}
		}
	}
	
	private DataTransformation getTransformation() {
		return new DataTransformation() {
			@Override
			public void transform(
					Map<String, List<Map<String, String>>> data,
					long startTime, long endTime) {
				expandData(data, startTime, endTime);
			}
		};
	}
	
	/**
	 * Request new data for the prediction lines in the plot. The prediction lines are assumed to have data from the start of time (or
	 * at least the earliest possible useful time in the plot) to the end of time (again based on the plot). The plot assumes
	 * that predictive feeds not stream data, but instead only retrieve data when the plot needs to request data (when the time changes, either
	 * due to an axis time change event, jump for example) or when the compression ratio changes, when {@link #requestDataRefresh(GregorianCalendar, GregorianCalendar)} is
	 * called. 
	 * @param startTime to request predictive data in
	 * @param endTime to bound predictive data with
	 */
	public void requestPredictiveData(GregorianCalendar startTime, GregorianCalendar endTime) {
		assert currentPredictionRequest == null : "prediction request should not be outstanding";
		if (plotDataAssigner.getPredictiveFeedProviders().isEmpty()) {
			return;
		}
		currentPredictionRequest = this.requestData(plotDataAssigner.getPredictiveFeedProviders(), startTime.getTimeInMillis(), endTime.getTimeInMillis(), 
													getTransformation(), 
													new RenderingCallback() {
														@Override
														public void render(Map<String, List<Map<String, String>>> data) {
															plotDataFeedUpdateHandler.updateFromFeed(data, true);
														}
														
													}, false);
		currentPredictionRequest.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				if (currentPredictionRequest == evt.getSource() && evt.getNewValue() == SwingWorker.StateValue.DONE) {
					assert SwingUtilities.isEventDispatchThread();
					currentPredictionRequest = null;
				}
			}
		});
	}
	
	/**
	 * Request new data for the plot
	 * @param startTime of the data requested
	 * @param endTime of the data requested
	 */
	public void requestDataRefresh(GregorianCalendar startTime, GregorianCalendar endTime) {
		// request data.
		if (plotDataAssigner.hasFeeds()) {
			cancelAnyOutstandingRequests();

			
			currentDataRequest = this.requestData(null, startTime.getTimeInMillis(), endTime.getTimeInMillis(), getTransformation(), this, true);
			currentDataRequest.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(java.beans.PropertyChangeEvent evt) {
					if (currentDataRequest.getState() == SwingWorker.StateValue.STARTED && evt.getOldValue()==SwingWorker.StateValue.PENDING) {
						plotDataFeedUpdateHandler.startDataRequest();
					}
					if (currentDataRequest == evt.getSource() && evt.getNewValue() == SwingWorker.StateValue.DONE) {
						assert SwingUtilities.isEventDispatchThread();
						currentDataRequest = null;
						plotDataFeedUpdateHandler.endDataRequest();
					}
				}
			});
		}
	}
	
	private void cancelOutstandingPredictionRequests() {
		logger.debug("PlotViewRole.cancelOutstandingPredictionRequests()");
		if (currentPredictionRequest !=null) {
			currentPredictionRequest.cancel(false);
			currentPredictionRequest = null;
		}
	}
	
	private void cancelAnyOutstandingRequests() {
		logger.debug("PlotViewRole.cancelAnyOutstandingRequests()");
		if (currentDataRequest !=null) {
			currentDataRequest.cancel(false);
		}
		cancelOutstandingPredictionRequests();
	}
	
	@Override
	public void synchronizeTime(
			Map<String, List<Map<String, String>>> data, long syncTime) {
		plotDataFeedUpdateHandler.synchronizeTime(data, syncTime);
	}

	@Override
	protected void synchronizationDone() {
		thePlot.removeTimeSyncLine();
	}

	@Override
	public void clear(Collection<FeedProvider> feedProviders) {
		updateMonitoredGUI();
	}

	/**
	 * Extract data from the feed and push it to the plot. We support three states.
	 */
	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		plotDataFeedUpdateHandler.updateFromFeed(data, false);
		for (Runnable r : feedCallbacks) {
			SwingUtilities.invokeLater(r);
		}
	}

	// Requests to MCT data buffer call back here. 
	@Override
	public void render(Map<String, List<Map<String, String>>> data) {
		plotDataFeedUpdateHandler.processData(data);
	}

	/**
	 * Returns the maximum value feed to this plot view role. 
	 * @return
	 */
	public double getMaxFeedValue() {
		return thePlot.getNonTimeMaxCurrentlyDisplayed();
	}

	/**
	 * Returns the minimum value feed to this plot view role.
	 * @return
	 */
	public double getMinFeedValue() {
		return thePlot.getNonTimeMinCurrentlyDisplayed();
	}

	/**
	 * Returns the current MCT time
	 * @return
	 */
	public long getCurrentMCTTime() {			
		long cachedTime = System.currentTimeMillis();	
		AbstractComponent manifestedComponent = getManifestedComponent();
		if (manifestedComponent!=null) {
			Collection<FeedProvider> feedproviders = getVisibleFeedProviders();
			if (!feedproviders.isEmpty()) {
			    
			    /* We want to get our "current time" from the feeds we're plotting */
			    Iterator<FeedProvider> feedIterator = feedproviders.iterator();
				FeedProvider firstProvider = feedIterator.next();
				FeedProvider fp = firstProvider;
				
				/* Find the first non-predictive feed provider; 
				 * predictive feeds may not have useful time values */
				while (fp.isPrediction() && feedIterator.hasNext()) {
				    fp = feedIterator.next();
				}
				
				/* If none is available, use the first predictive provider for consistency */
				if (fp.isPrediction()) fp = firstProvider;
				
				long currentTimeInMillis =  fp.getTimeService().getCurrentTime();
				if (currentTimeInMillis >= 0) {
					cachedTime = currentTimeInMillis;
				} else {
					logger.error("FeedProvider currentTimeMillis() returned a time less than zero: {}", currentTimeInMillis);
				}
			} else {
				logger.debug("No feed providers. Returning cached time: {}", cachedTime);
			}
		}
		return cachedTime;
	}
	
	private void createPlotAndAddItToPanel() {
		createPlot();		
		assert thePlot!=null: "Plot must be created";			
		addPlotToPanel();
	}
	
	private void createPlot(){			
		thePlot = PlotViewFactory.createPlot(plotPersistenceHandler.loadPlotSettingsFromPersistance(), 
								             getCurrentMCTTime(),
								             this, plotDataAssigner.returnNumberOfSubPlots(), null, plotLabelingAlgorithm, plotDataAssigner.getTimeSystemDefaultChoice());
	}
	
    private void addPlotToPanel() {
    	// Remove previous plot if there was one.
		if (theView!=null) {
         	remove(theView);
		}
					
		theView = thePlot.getPlotPanel();
			
		add(theView);  
		refreshPlotPanel();  	
    }
    
    private void enforceBackgroundColor (final Color bg) {
		// Enforce a background color
		this.setBackground(bg);
    	thePlot.getPlotPanel().setBackground(bg);
		
    	//TODO: Construct a less brute-force solution?
		ComponentTraverser.traverse(theView, new ComponentTraverser.ComponentProcedure() {			
			@Override
			public void run(Component c) {
				if ((PlotConstants.DEFAULT_PLOT_FRAME_BACKGROUND_COLOR).equals(c.getBackground())) {
					c.setBackground(bg);
				}
			}				
		});
    }

    private void refreshPlotPanel() {        	        	
 		thePlot.refreshDisplay();
    	enforceBackgroundColor(plotFrameBackground);
    	revalidate();
    }
    
	public PlotView getPlot() {
		return thePlot;
	}

	/**
	 * Only for use during testing.
	 */
	public void setPlot(PlotView plot) {
		thePlot = plot;
	}
	

	private void clearArrayList() {
		canvasContextTitleList.clear();
		panelContextTitleList.clear();
	}

	private void setLabelingContext(AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm, NamingContext context) {			

		clearArrayList();
		
		String surroundingName = "";
		
		if (context != null) {
			/* Is some name being shown by the labeling context? */
			if (context.getContextualName() != null) {
				surroundingName = context.getContextualName(); /* Get that name. */
				logger.debug("getPanelTitle surroundingName={}",surroundingName);
				if (surroundingName.isEmpty()) {
					/* A title bar or similar is displayed, but it's not overriding our * 
					 * base displayed name */
					surroundingName = getManifestedComponent().getDisplayName();
				} 
			}
			canvasContextTitleList.add(surroundingName);
		} else {
			/* Labeling context is null, so we are in our own window or inspector */
			surroundingName = getManifestedComponent().getDisplayName();
			panelContextTitleList.add(surroundingName);
		}
	
		plotLabelingAlgorithm.setPanelOrWindowTitle(surroundingName);
		plotLabelingAlgorithm.setCanvasContextTitleList(canvasContextTitleList);
		plotLabelingAlgorithm.setPanelContextTitleList(panelContextTitleList);
							
		if (logger.isDebugEnabled()) {
			printTitleArrayLists("*** DEBUG 2 *** panelContextTitleList", panelContextTitleList);
			printTitleArrayLists("*** DEBUG 2 *** canvasContextTitleList", canvasContextTitleList);
		}
	}			
	
	
	private void printTitleArrayLists(String name, List<String> arrayList) {
		for (int i=0; i < arrayList.size(); i++) {
			logger.debug(name + ".get(" + i + ")=" + arrayList.get(i));
		}
	}
	
	public void addFeedCallback(Runnable r) {
		feedCallbacks.add(r);
	}
	
	public void removeFeedCallback(Runnable r) {
		feedCallbacks.remove(r);
	}
	
}