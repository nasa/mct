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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plotter.xy.CompressingXYDataset;
import plotter.xy.XYPlotContents;

/**
 * Manages the data associated with a plot. This class supports<br>
 * <ul>
 * <li>adding data and data sets
 * <li>compressing data (during adds)
 * <li>sizing plot data buffers based on the number of pixels available for the plot on the screen.
 * </ul>
 */
public class PlotDataManager implements AbstractPlotDataManager {

	private final static Logger logger = LoggerFactory.getLogger(PlotDataManager.class);

	/** Do we allow new values in data sets - should always be true. */
	final static boolean DATA_SET_ENABLE_UPDATE_STATE = true;
	/** Do we force the data buffer to be truncated when it becomes full */
	final static boolean DATA_SET_BUFFER_TRUNCATE_STATE = true;
    /** QC provide a different mechanism for triggering rescales on the non-time axis to us. 
	    This constant is the number and it must be set to ZERO. We use % padding to control
	    growth on this axis. */
	final static int MIN_SAMPLES_FOR_AUTOSCALE = 0;

	/** The Set of data items to be displayed on this plot. */ 
	private Map<String, PlotDataSeries> dataSeries;

	/** The QuinnCurtis plot on which we're displaying our data. */
	private PlotterPlot plot;

	/** Cache for maintaining min/max non time values displayed on the plot */
	private PlotNonTimeMinMaxValueManager minMaxValueManager;

	/** Timer to wait for user window resize actions to complete before 
	   requesting new data at the window's updated compression ratio.  */
	private Timer resizeTimmer;

	/** We only need to resize when the time axis dimension of the window is resized.
	    This variable caches the previous size so upon a resize event we can test to
	    see if the new size differs from the old. */
	private int previousTimeAxisDimensionSize = -1;

	/** Flag to record if a request needs to be made for a plot buffer update but that 
	    request could not happen because an updateFromFeed event was in process. */
	private boolean bufferRequestWaiting = false;

	/** Flag to record if a buffer truncation event occurred on a scrunch plot.*/
	private boolean scrunchBufferTruncationOccured = false;
	
	/** Span of the plot data buffer. */
	private GregorianCalendar plotDataBufferStartTime;
	private GregorianCalendar plotDataBufferEndTime;

	
	/**
	 * Create a datamanager for the plot passed in
	 * @param thePlot to manage data for
	 */
	public PlotDataManager(PlotterPlot thePlot) {
		plot = thePlot;
	
		dataSeries = new HashMap<String, PlotDataSeries>(PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT, 
				                                           PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT);
		minMaxValueManager = new PlotNonTimeMinMaxValueManager(this);
		setupResizeTimmer();
	}

	/**
	 * Setup a timer to cause a delay before data update requests are made when the
	 * plot window is resized.
	 */
	private void setupResizeTimmer() {
		resizeTimmer = new Timer(PlotConstants.RESIZE_TIMER, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resizeAndReloadPlotBuffer();
			}
		});
		resizeTimmer.setRepeats(false);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataManager#addDataSet(java.lang.String, java.awt.Color)
	 */
	@Override
	public void addDataSet(String dataSetName, Color plottingColor) {
		
		if (dataSetName != null) {
		// This is the first data item, setup up the plot buffer size etc. 
		if (dataSeries.size() == 0) {
			setupBufferSizeAndCompressionRatio();
		}
		LegendEntry legendEntry = new LegendEntry(PlotConstants.LEGEND_BACKGROUND_COLOR, plottingColor, Font.decode(Font.SANS_SERIF).deriveFont(9f), plot.getPlotLabelingAlgorithm());
		legendEntry.setDataSetName(dataSetName);
		dataSeries.put(dataSetName, new PlotDataSeries((PlotterPlot) plot, dataSetName, plottingColor));	
		// create the legend.

		legendEntry.setPlot(dataSeries.get(dataSetName).getPlot());
		legendEntry.setRegressionLine(dataSeries.get(dataSetName).getRegressionLine());
		dataSeries.get(dataSetName).setLegend(legendEntry);	
		}
	}

	void addDataSet(String dataSetName, Color plottingColor, String displayName) {
		addDataSet(dataSetName, plottingColor);
		assert dataSeries.get(dataSetName).getLegendEntry() != null : "Legend entry null!";
		dataSeries.get(dataSetName).getLegendEntry().setBaseDisplayName(displayName);
	}

	boolean isKnownDataSet(String setName) {
		assert dataSeries!=null;
		return dataSeries.containsKey(setName);
	}

	int getDataSetSize() {
		return dataSeries.size();
	}


	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataManager#addData(java.lang.String, java.util.SortedMap)
	 */
	@Override
	public void addData(String feed, SortedMap<Long, Double> points) {
		assert ((PlotterPlot) plot).getPlotView() !=null : "Plot Object not initalized";
		assert isKnownDataSet(feed) : "Data set " + feed + " not defined.";

		if(points.isEmpty()) {
			return;
		}

		setupCompressionRatio();

		// prevent plotting of data if it is not compatible with scrunch settings.
		if(plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			boolean needsFixing = false;
			for(Long time : points.keySet()) {
				if(time <= plot.getMinTime()) {
					needsFixing = true;
					break;
				}
			}
			if(needsFixing) {
				SortedMap<Long, Double> points2 = new TreeMap<Long, Double>();
				for(Entry<Long, Double> point : points.entrySet()) {
					if(point.getKey() > plot.getMinTime()) {
						points2.put(point.getKey(), point.getValue());
					}
				}
				points = points2;
			}
		}

		// Don't plot points off the end if the time axis is pinned
		if (plot.getPlotAbstraction().getTimeAxis().isPinned()) {
			long max = plot.getMaxTime();
			boolean needsFixing = false;
			for(Long time : points.keySet()) {
				if(time > max) {
					needsFixing = true;
					break;
				}
			}
			if(needsFixing) {
				SortedMap<Long, Double> points2 = new TreeMap<Long, Double>();
				for(Entry<Long, Double> point : points.entrySet()) {
					if(point.getKey() <= max) {
						points2.put(point.getKey(), point.getValue());
					}
				}
				points = points2;
				dataSeries.get(feed).setUpdateRegressionLine(false);
			}
		}

		if(points.isEmpty()) {
			return;
		}

		CompressingXYDataset dataset = dataSeries.get(feed).getData();
		double min;
		double max;
		if(plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			min = dataset.getMinX();
			max = dataset.getMaxX();
		} else {
			min = dataset.getMinY();
			max = dataset.getMaxY();
		}
		double datasetMinTime = Math.min(min, max);
		double datasetMaxTime = Math.max(min, max);

		if(dataset.getPointCount() == 0 || points.firstKey() >= datasetMaxTime) {
			// TODO: Change this to use an aggregate add method
			if(plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
				for(Entry<Long, Double> point : points.entrySet()) {
					dataset.add(point.getKey(), point.getValue());

				}
			} else {
				for(Entry<Long, Double> point : points.entrySet()) {
					dataset.add(point.getValue(), point.getKey());
				}
			} 
			if (plot.getMaxTime() >= datasetMaxTime) {
				dataSeries.get(feed).setUpdateRegressionLine(true);
			}
		} else if(points.lastKey() <= datasetMinTime) {
			// TODO: Make this efficient
			double[] x = new double[points.size()];
			double[] y = new double[x.length];
			int i = 0;
			for(Entry<Long, Double> p : points.entrySet()) {
				x[i] = p.getKey();
				y[i] = p.getValue();
				i++;
			}
			if(plot.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME) {
				double[] tmp = x;
				x = y;
				y = tmp;
			}
			dataset.prepend(x, 0, y, 0, x.length);
		} else {
			// Data appearing in the middle of the dataset.
			// Assume that it's caused by the last second of data arriving twice,
			// once from the initial historical request and once from the once-per-second update.
			// It may also be values for predictive data we already loaded.
			// In either case, the overlapping data should be identical to what we already have, so ignore it.

			// Append the data that isn't redundant.
			SortedMap<Long, Double> before = points.subMap(0L, (long) datasetMinTime);
			SortedMap<Long, Double> after = points.subMap((long) datasetMaxTime, Long.MAX_VALUE);
			SortedMap<Long, Double> overlap = points.subMap((long) datasetMinTime, (long) datasetMaxTime);
			if(!overlap.isEmpty()) {
				if(overlap.lastKey() - overlap.firstKey() > 10000) {
					logger.warn("Cannot currently insert into the middle of a dataset: minX = " + datasetMinTime + ", maxX = " + datasetMaxTime
							+ ", firstKey = " + points.firstKey() + ", lastKey = " + points.lastKey());
				}
			}
			// TODO: Change this to use an aggregate add method
 			if(plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
				if(!before.isEmpty()) {
					double[] x = new double[before.size()];
					double[] y = new double[x.length];
					int i = 0;
					for(Entry<Long, Double> point : before.entrySet()) {
						x[i] = point.getKey();
						y[i] = point.getValue();
						i++;
					}
					dataset.prepend(x, 0, y, 0, x.length);
				}
				for(Entry<Long, Double> point : after.entrySet()) {
 				 					dataset.add(point.getKey(), point.getValue());
 				}
 			} else {
				if(!before.isEmpty()) {
					double[] x = new double[before.size()];
					double[] y = new double[x.length];
					int i = 0;
					for(Entry<Long, Double> point : before.entrySet()) {
						y[i] = point.getKey();
						x[i] = point.getValue();
						i++;
					}
					dataset.prepend(x, 0, y, 0, x.length);
				}

				for(Entry<Long, Double> point : after.entrySet()) {
 					dataset.add(point.getValue(), point.getKey());
 				}
 			}
		}
		dataSeries.get(feed).updateRegressionLine();
		for(Entry<Long, Double> point : points.entrySet()) {
			Long timestamp = point.getKey();
			Double value = point.getValue();
			boolean isValidForPlot = !Double.isNaN(value);
			if (isValidForPlot) {
				minMaxValueManager.updateMinMaxCache(timestamp, value);
			}
		}
		
		for(Entry<Long, Double> e : points.entrySet()) {
			plot.getLimitManager().informPointPlottedAtTime(e.getKey(), e.getValue());
		}
		
		if (plot instanceof PlotterPlot) ((PlotterPlot) plot).setInitialized();
	}

	void updateLegend(String dataSetName, FeedProvider.RenderingInfo info) {
		dataSeries.get(dataSetName).getLegendEntry().setData(info);
	}


	double getNonTimeMaxDataValueCurrentlyDisplayed() {
		return minMaxValueManager.getNonTimeMaxDataValueCurrentlyDisplayed();	
	}

	double getNonTimeMinDataValueCurrentlyDisplayed() {
		return minMaxValueManager.getNonTimeMinDataValueCurrentlyDisplayed();	
	}	

	/**
	 * Returns true if a time is not valid with the Quinn Curtis scrunch mode panel on a plot. False, otherwise. 
	 * @param time the time to evaluate
	 * @return true if the time is not valid for a plot's scrunch mode. True otherwise.
	 */
	boolean scrunchProtect(long time) {
		// Protection only applies when we are in scrunch mode
		if (plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			// protection required if the time is before or equal to the plot's starts time.
			return time <= plot.getMinTime(); 
		} else {
			// not in scrunch mode.
			return false;
		}
	}

	/**
	 * An event has occurred that means the plots buffer needs to be resized
	 * and data requested at the new resolution demanded by that buffer. 
	 * 
	 * If an update from feed event is in process when a processDataBufferResizeEvent is
	 * requested, a flag will be set. When the updateFromFeed event is completed, it will check 
	 * for waiting buffer requests and initiate one. 
	 */
	public void resizeAndReloadPlotBuffer() {
		if (!plot.isUpdateFromCacheDataStreamInProcess()) {
			bufferRequestWaiting = false;
			resetPlotDataVariablesAndRequestDataRefreshAtNewResolution();
		} else {
			// update is locked out as we're in the middle of an updateFromFeedEvent.
			// Note that a bufferRequest is waiting. 
			bufferRequestWaiting = true;
		}
	}
	
	void setupCompressionRatio() {
		AbstractAxis axis = plot.getTimeAxis();
		double start = axis.getStart();
		double end = axis.getEnd();
		assert start != end;
		XYPlotContents contents = plot.getPlotView().getContents();
		// the height or width could be zero if the plot is showing in an area which is closed. One scenario is the inspector area where the slider is
		// closed
		double width = Math.max(0,plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME ? contents.getWidth() : contents.getHeight());
		double compressionScale = width == 0 ? Double.MAX_VALUE : Math.abs(end - start) / width;
		if(plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH) {
			for(PlotDataSeries s : dataSeries.values()) {
				CompressingXYDataset d = s.getData();
				double scale = d.getCompressionScale();
				// Compress by integral factors to minimize artifacts from multiple compression runs
				if(scale == 0) {
					scale = compressionScale;
				} else {
					while(scale < compressionScale / 2) {
						scale *= 2;
					}
				}
				if(scale > d.getCompressionScale()) {
					d.setCompressionOffset(start);
					d.setCompressionScale(scale);
					d.recompress();
				}
			}
		} else {
			for(PlotDataSeries s : dataSeries.values()) {
				CompressingXYDataset d = s.getData();
				d.setCompressionOffset(start);
				d.setCompressionScale(compressionScale);
			}
		}
	}

	
	/**
	 * Setup the data compression ratio and plot buffer sizes based on the number of pixels on the plot's span
	 */
	void setupBufferSizeAndCompressionRatio() {
		setupPlotBufferMinAndMaxTimes();
		setupCompressionRatio();
	}

	/**
	 * Reset every data series on the plot.
	 * remove all process variables from the scroll frame.
	 * It would be preferable to have each PlotDataSeries remove itself. However,
     * QC only provides a single method to remove all. 
	 */
	void resetPlotDataSeries() {
		for(String datasetName : dataSeries.keySet()) {
			dataSeries.get(datasetName).resetData();
		}
	}

	/**
	 * Reset the plot's buffer and compression ratio to the current plot window's size and request new data and the 
	 * updated compression ratio.
	 */
	private void resetPlotDataVariablesAndRequestDataRefreshAtNewResolution() {
	
		// Setup size of the buffer and compression ration of the plot. 
		setupBufferSizeAndCompressionRatio(); 
		
		/* IMPORTANT 
		 * setup the buffer size and compression ratio before resstPlotDataSeris is called as the data series
		 * is scaled to the buffer size calculated in the above call.
		 */
	     if (!scrunchBufferTruncationOccured) {
	    	// prevent further resize events from occurring until this event is completed.
		    plot.setUpdateFromCacheDataStreamInProcess(true);
		   // Window size has changed so recalculated compression ratio;
		    
		   assert  plotDataBufferEndTime.after(plotDataBufferStartTime) : "Attempting a request to the data buffer with negative span";	    
		   // limit request window to PLOT_DATA_BUFFER_SLIZE_REQUEST_SIZE
		   if (plotDataBufferEndTime.getTimeInMillis() - plotDataBufferStartTime.getTimeInMillis() > PlotConstants.MAXIMUM_PLOT_DATA_BUFFER_SLIZE_REQUEST_SIZE) {
			   plotDataBufferStartTime.setTimeInMillis(plotDataBufferEndTime.getTimeInMillis() - PlotConstants.MAXIMUM_PLOT_DATA_BUFFER_SLIZE_REQUEST_SIZE);
		   } 
		   requestDataFromMCTBuffer(plotDataBufferStartTime, plotDataBufferEndTime);
	     } else {
	    	 logger.debug("Refreshing a scrunch plots data buffer from its own buffer.");
	    	 // for scrunch plots, we compress the existing data in the plots local buffer when a truncation event occurs.
	    	 // We compress that data further rather than accept the overhead of going to the MCT data buffer and compressing
	    	 // data at full fidelity. 
	    	
	    	 assert plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH: "A scrunch event has occured on a non scrunch plot!";
	    	 minMaxValueManager.setMinMaxCacheState(false);
//	         // We will reset all the process vars from this plot, so remove all from scroll frame.
//	    	 plot.removeAllProcessVarFromScrollFrame();
	    	 for (String key: dataSeries.keySet()) {
	 			PlotDataSeries  data = dataSeries.get(key);
	 			data.compressByFiftyPercent();
	 		} 	 
	    	 minMaxValueManager.setMinMaxCacheState(true);
	     }
	}

	/**
	 * Determine the span of the plot data buffer based upon the current PlotDisplayState. 
	 */
	void setupPlotBufferMinAndMaxTimes() {
			// we only need to buffer from the start to end time of the plot. 
			plotDataBufferStartTime = plot.getCurrentTimeAxisMin();
			plotDataBufferEndTime = plot.getCurrentTimeAxisMax();
		
		assert plotDataBufferStartTime != null : "buffer start time not intialized when it should have been";
		assert plotDataBufferEndTime != null : "buffer end time not intialized when it should have been";	
	}
	
	/**
	 * Request data from the MCT buffer spanning startTime to endTime at the specified compression ratio. The method
	 * will also 
	 * @param startTime the start time of the data requested
	 * @param endTime the end time of the data requested
	 */
	private void requestDataFromMCTBuffer(GregorianCalendar startTime, GregorianCalendar endTime) {
		if (startTime == null || endTime == null) {
			throw new IllegalArgumentException("Start and/or end time was null.");
		}
	
		// Don't request if local controls are not enabled. This only occurs when we are a secondary plot in a stacked plot and we
		// rely on the master plot in the stack to make requests. 
		if (plot.getPlotAbstraction() != null && plot.isTimeLabelEnabled) {
			// Request new data.
			plot.getPlotAbstraction().requestPlotData(startTime, endTime);
		} 
	}
	
	void informUpdateFromLiveDataStreamStarted() {
	  // nothing to do.
	}
	
	void informUpdateFromLiveDataStreamCompleted() {
		if (scrunchBufferTruncationOccured || bufferRequestWaiting) {
			resizeAndReloadPlotBuffer();
			// flag must be reset after resizeAndReloadPlotBuffer call 
			// as method uses this flag to determine if it is process a scrunch truncation event. 
			// clear the waiting flags
			scrunchBufferTruncationOccured = false;
			bufferRequestWaiting = false;
		} 
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataManager#informUpdateCacheDataStreamStarted()
	 */
	@Override
	public void informUpdateCacheDataStreamStarted() {
		minMaxValueManager.setMinMaxCacheState(false);
		resetPlotDataSeries();
		// There should be no data on the plot at this point. 
 
	}
	
	void informUpdateCacheDataStreamCompleted() {
		logger.debug("Update from cache completed" );
		minMaxValueManager.setMinMaxCacheState(true);
		if (scrunchBufferTruncationOccured) {
			resizeAndReloadPlotBuffer();
			// flag must be reset after resizeAndReloadPlotBuffer call 
			// as method uses this flag to determine if it is process a scrunch truncation event. 
			scrunchBufferTruncationOccured = false;
		}
	}


	void informBufferTrunctionEventOccured() {	
		if (plot.getTimeAxisSubsequentSetting() == TimeAxisSubsequentBoundsSetting.SCRUNCH && 
				plot.isCompressionEnabled()) {
			logger.debug("Scrunch truncation event occured");
			// record that a buffer truncation event occurred. 
			scrunchBufferTruncationOccured = true;
		}
	}
	
	/**
	 * Inform the data manager that a resize event has occurred. The method
	 * determines if the event changed the size of the time axis and if it did starts
	 * the resize timmer which will cause the plots buffer to be resized and refreshed. 
	 */
	public void informResizeEvent() {
		// only initiate a resize if the time axis has change size.
		Rectangle bounds = plot.getPlotView().getContents().getBounds();
		int currentSize = (int) (
				(plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) ?
				bounds.getWidth() : bounds.getHeight() ) ;
		if (currentSize != previousTimeAxisDimensionSize) {
		  resizeTimmer.restart();	
		}
		// cache the now current size to compare with when the next resize event occurs. 
		previousTimeAxisDimensionSize = currentSize;
	}
	
	double getTimeAxisWidthInPixes() {
		Rectangle bounds = plot.getPlotView().getContents().getBounds();
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			return bounds.getWidth();
		} else {
			return bounds.getHeight();
		}
	}

	
	boolean isBufferRequestWaiting() {
		return bufferRequestWaiting;
	}
	
	boolean hasScrunchTruncationOccured() {
		return scrunchBufferTruncationOccured;
	}

	@Override
	public PlotDataSeries getNamedDataSeries(String name) {
		return dataSeries.get(name);
	}

	/**
	 * @param dataSeries the dataSeries to set
	 */
	public void setDataSeries(Map<String, PlotDataSeries> dataSeries) {
		this.dataSeries = dataSeries;
	}

	/**
	 * @return the dataSeries
	 */
	public Map<String, PlotDataSeries> getDataSeries() {
		return dataSeries;
	}

	/**
	 * @param plot the plot to set
	 */
	public void setPlot(PlotterPlot plot) {
		this.plot = plot;
	}

	/**
	 * @return the plot
	 */
	public AbstractPlottingPackage getPlot() {
		return plot;
	}
}
