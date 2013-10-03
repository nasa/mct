package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis.AxisVisibleOrientation;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxisBoundManager;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataManager;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotDataSeries;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotLine;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.LegendManager;
import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotLimitManager;
import gov.nasa.arc.mct.fastplot.bridge.PlotObserver;
import gov.nasa.arc.mct.fastplot.bridge.PlotViewActionListener;
import gov.nasa.arc.mct.fastplot.bridge.controls.AbstractPlotLocalControl;
import gov.nasa.arc.mct.fastplot.bridge.controls.AbstractPlotLocalControl.AttachmentLocation;
import gov.nasa.arc.mct.fastplot.bridge.controls.AbstractPlotLocalControlsManager;
import gov.nasa.arc.mct.fastplot.bridge.controls.PlotLocalControlsManagerImpl;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotConfigurationDelegator;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SpringLayout;

import plotter.xy.ScatterXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYPlot;

public class ScatterPlot extends PlotConfigurationDelegator implements AbstractPlottingPackage {
	private ScatterPlotDataManager dataManager = new ScatterPlotDataManager(this);
	private ArrayList<PlotObserver> observers = new ArrayList<PlotObserver>();
	private Set<String> knownDataSeries = new HashSet<String>();
	private PlotAbstraction abstraction;	
	
	private XYPlot plotPanel;
	
	private ImplicitTimeAxis timeAxis = new ImplicitTimeAxis();
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	private LegendManager legendManager = new LegendManager(plotLabelingAlgorithm);
	
	private double initialNonTimeMin;
	private double initialNonTimeMax;

	private PlotLocalControlsManagerImpl localControls = new PlotLocalControlsManagerImpl();
	private PlotViewActionListener actionListener;
	
	private Map<AxisVisibleOrientation, Collection<AbstractAxisBoundManager>> boundManagers = 
		new HashMap<AxisVisibleOrientation, Collection<AbstractAxisBoundManager>>();
	
	public ScatterPlot() {
		this (new PlotSettings());
	}
	
	public ScatterPlot(PlotConfiguration delegate) {
		super(delegate);
		if (delegate instanceof PlotAbstraction) {
			setPlotAbstraction((PlotAbstraction) delegate);
		}
		timeAxis.setStart(delegate.getMinTime());
		timeAxis.setEnd(delegate.getMaxTime());

		initialNonTimeMin = delegate.getMinNonTime();
		initialNonTimeMax = delegate.getMaxNonTime();
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
	public void notifyObserversTimeChange() {
		//TODO need to implement for non-time?
	}

	@Override
	public void createChart(Font timeAxisFont, int plotLineThickness,
			Color plotBackgroundFrameColor, Color plotAreaBackgroundColor,
			int timeAxisIntercept, Color timeAxisColor,
			Color timeAxisLabelColor, Color nonTimeAxisLabelColor,
			String timeAxisDataFormat, Color nonTimeAxisColor,
			Color gridLineColor, int minSamplesForAutoScale,
			boolean isCompressionEnabled, boolean isTimeLabelsEnabled,
			boolean isLocalControlEnabled, PlotAbstraction thePlotAbstraction,
			AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm) {

		
		ScatterPlotObjects objects = new ScatterPlotObjects(this);
		objects.setAxisRepresentation(timeAxisFont, nonTimeAxisColor);
		plotPanel = objects.getXYPlot();
		
		setPlotAbstraction(thePlotAbstraction);
		setDelegate(thePlotAbstraction);

		setupAxisBounds();
		timeAxis.setStart(thePlotAbstraction.getMinTime());
		timeAxis.setEnd(thePlotAbstraction.getMaxTime());
		
		legendManager.setOpaque(false);		
		
		actionListener = new PlotViewActionListener(this);
		
		setupAxisBoundsManagers();
	}
	
	private void setupAxisBoundsManagers() {
		for (AbstractAxis axis : getAxes()) {
			AxisVisibleOrientation o = axis.getVisibleOrientation();
			if (o != null) {
				List<AbstractAxisBoundManager> bounds = new ArrayList<AbstractAxisBoundManager>(2);
				for (boolean maximal : new boolean[] { false, true } ) {
					NonTimeAxisSubsequentBoundsSetting setting = maximal ?
							getNonTimeAxisSubsequentMaxSetting() :
							getNonTimeAxisSubsequentMinSetting();
					switch (setting) {
					case AUTO:
						bounds.add(new NonTimeAutoExpandBoundManager(this, axis, maximal));
						break;
					case FIXED:
						bounds.add(new NonTimeFixedBoundManager(this, axis, maximal));
						break;
					case SEMI_FIXED:
						bounds.add(new NonTimeSemiFixedBoundManager(this, axis, maximal));
						break;
					}
				}
				boundManagers.put(o, bounds);
			}
		}
	}

	private void setupAxisBounds() {
		// Swap depending on MAXIMUM_AT_RIGHT etc
		double independentBounds[] = { getMinNonTime(), getMaxNonTime() };
		double dependentBounds[]   = { getMinDependent(), getMaxDependent() };
		int    minXIndex       = getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT ? 0 : 1;
		int    minYIndex       = getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP   ? 0 : 1;
		
		plotPanel.getXAxis().setStart(independentBounds[    minXIndex]);
		plotPanel.getXAxis().setEnd  (independentBounds[1 - minXIndex]);
		plotPanel.getYAxis().setStart(dependentBounds  [    minYIndex]);
		plotPanel.getYAxis().setEnd  (dependentBounds  [1 - minYIndex]);
	}
	

	@Override
	public JComponent getPlotComponent() {
		return plotPanel;
	}
	
	@Override
	public void addDataSet(String dataSetName, Color plottingColor) {
		dataManager.addDataSet(dataSetName, plottingColor);		
		AbstractPlotDataSeries series = dataManager.getNamedDataSeries(dataSetName);
		if (series != null && series instanceof ScatterPlotDataSeries) {
			((ScatterPlotDataSeries) series).getPlotLine().setColor(plottingColor);
			//series.getLegendEntry().setDataSetName(dataSetName);
		}
		if (dataSetName.contains(PlotConstants.NON_TIME_FEED_SEPARATOR)) {
			knownDataSeries.add(dataSetName);
			dataSetName = dataSetName.split(PlotConstants.NON_TIME_FEED_SEPARATOR)[1];
		}
		knownDataSeries.add(dataSetName);
	}

	@Override
	public void addDataSet(String lowerCase, Color plottingColor,
			String displayName) {
		addDataSet(lowerCase, plottingColor);
		AbstractPlotDataSeries series = dataManager.getNamedDataSeries(lowerCase);
		if (series != null) {
			series.getLegendEntry().setBaseDisplayName(displayName);
		}	
		plotPanel.revalidate();
	}

	@Override
	public boolean isKnownDataSet(String setName) {
		return true;//lotDataManager.getNamedDataSeries(setName) != null;
	}

	@Override
	public void updateLegend(String dataSetName, RenderingInfo info) {

	}

	@Override
	public void refreshDisplay() {
		
	}

	@Override
	public int getDataSetSize() {
		return dataManager.size();
	}

	@Override
	public LimitAlarmState getDependentMaxAlarmState() {
		return LimitAlarmState.NO_ALARM; // TODO - need limit alarm for scatterplot
	}

	@Override
	public LimitAlarmState getDependentMinAlarmState() {
		return LimitAlarmState.NO_ALARM; // TODO - need limit alarm for scatterplot
	}

	@Override
	public GregorianCalendar getCurrentTimeAxisMin() {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timeAxis.getStartAsLong());
		return gc;
	}

	@Override
	public GregorianCalendar getCurrentTimeAxisMax() {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timeAxis.getEndAsLong());
		return gc;	}

	@Override
	public void showTimeSyncLine(GregorianCalendar time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTimeSyncLine() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getInitialNonTimeMinSetting() {
		return initialNonTimeMin;
	}

	@Override
	public double getInitialNonTimeMaxSetting() {
		return initialNonTimeMax;
	}

	@Override
	public long getInitialTimeMinSetting() {
		return super.getMinTime();
	}

	@Override
	public long getInitialTimeMaxSetting() {
		return super.getMaxTime();
	}

	@Override
	public boolean isTimeSyncLineVisible() {
		// TODO Auto-generated method stub
		return false;
	}	
	
	@Override
	public void setPlotAbstraction(PlotAbstraction plotView) {
		abstraction = plotView;
		abstraction.setPlotTimeAxis(timeAxis);
	}

	@Override
	public void notifyGlobalTimeSyncFinished() {
		
	}

	@Override
	public boolean inTimeSyncMode() {
		return false;
	}

	@Override
	public double getNonTimeMaxDataValueCurrentlyDisplayed() {
		return Math.max(
				Math.max(plotPanel.getXAxis().getStart(), plotPanel.getYAxis().getStart()), 
				Math.max(plotPanel.getXAxis().getEnd(),   plotPanel.getYAxis().getEnd()));
	}

	@Override
	public double getNonTimeMinDataValueCurrentlyDisplayed() {
		return Math.min(
				Math.min(plotPanel.getXAxis().getStart(), plotPanel.getYAxis().getStart()), 
				Math.min(plotPanel.getXAxis().getEnd(),   plotPanel.getYAxis().getEnd()));
	}

	@Override
	public void setCompressionEnabled(boolean state) {
		// Ignore compression! Makes no sense on scatter plot
	}

	@Override
	public boolean isCompressionEnabled() {
		return false;
	}

	@Override
	public void informUpdateCachedDataStreamStarted() {
		
	}

	@Override
	public void informUpdateCacheDataStreamCompleted() {
		
	}

	@Override
	public void informUpdateFromLiveDataStreamStarted() {
		
	}

	@Override
	public void informUpdateFromLiveDataStreamCompleted() {
		for (PlotObserver o : this.observers) {
			o.dataPlotted();
		}
	}

	@Override
	public void setTimeAxisStartAndStop(long startTime, long endTime) {
		timeAxis.setStart(startTime);
		timeAxis.setEnd(endTime);
	}

	@Override
	public void clearAllDataFromPlot() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Axis getNonTimeAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateResetButtons() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlotLabelingAlgorithm(
			AbbreviatingPlotLabelingAlgorithm thePlotLabelingAlgorithm) {
		plotLabelingAlgorithm = thePlotLabelingAlgorithm;
	}

	@Override
	public AbbreviatingPlotLabelingAlgorithm getPlotLabelingAlgorithm() {
		return this.plotLabelingAlgorithm;
	}

	@Override
	public void addData(String feedID, SortedMap<Long, Double> points) {
		legendManager.setVisible(true);
		dataManager.addData(feedID, points);
		
		// TODO: This will also need to work separately for dependent/independent bounds
//		boolean changed = false ;
//		for (boolean maximal : new boolean[]{true, false}) {
//			if ((maximal ? getNonTimeAxisSubsequentMaxSetting() : getNonTimeAxisSubsequentMinSetting()) 
//					== NonTimeAxisSubsequentBoundsSetting.AUTO) {
//				changed |= autoExpand(true, maximal);
//				changed |= autoExpand(false, maximal);
//			}
//		}
//		if (changed) {
//			setupAxisBounds();
//		}
	}
	
	private boolean autoExpand(boolean dependent, boolean maximal) {
		double current  = dependent ? (maximal ? getMaxDependent() : getMinDependent()) : 
			                         (maximal ? getMaxNonTime()   : getMinNonTime());
		double minimum  = dataManager.getExtremum(
				timeAxis.getStartAsLong(), timeAxis.getEndAsLong(), false, dependent);
		double maximum  = dataManager.getExtremum(
				timeAxis.getStartAsLong(), timeAxis.getEndAsLong(), true,  dependent);
		double extremum = maximal ? maximum : minimum;
		double padding  = (maximal ? getNonTimeMaxPadding() : getNonTimeMinPadding()) * 
				(maximum - minimum);
		if (maximal && extremum > current) {
			if (dependent) setMaxDependent(extremum + padding);
			else           setMaxNonTime(extremum + padding);
		} else if (!maximal && extremum < current) {
			if (dependent) setMinDependent(extremum - padding);
			else           setMinNonTime(extremum - padding);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void addData(String feed, long time, double value) {
		SortedMap<Long, Double> points = new TreeMap<Long, Double>();
		points.put(time, value);
		addData(feed, points);
	}

	@Override
	public void setTruncationPoint(double min) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateCompressionRatio() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PlotAbstraction getPlotAbstraction() {
		return abstraction;
	}

	@Override
	public LegendManager getLegendManager() {
		return legendManager;
	}

	@Override
	public AbstractPlotDataManager getPlotDataManager() {
		return dataManager;
	}

	@Override
	public AbstractPlotLocalControlsManager getLocalControlsManager() {
		return localControls;
	}

	@Override
	public PlotViewActionListener getPlotActionListener() {
		return actionListener;
	}

	@Override
	public PlotLimitManager getLimitManager() {
		
		return null;
	}

	@Override
	public AbstractPlotLine createPlotLine() {
		ScatterXYPlotLine plotterLine = 
			new ScatterXYPlotLine(plotPanel.getXAxis(), plotPanel.getYAxis());
		plotterLine.setForeground(Color.PINK);
		SimpleXYDataset data = new SimpleXYDataset(plotterLine);
		plotPanel.getContents().add(plotterLine);
		return new PlotLineWrapper(plotterLine, data);
	}

	private static class PlotLineWrapper implements AbstractPlotLine {
		private SimpleXYDataset data;
		private ScatterXYPlotLine plotLine;

		public PlotLineWrapper(ScatterXYPlotLine plotLine, SimpleXYDataset data) {
			this.plotLine = plotLine;
			this.data = data;
		}
		
		@Override
		public void appendData(double independent, double dependent) {
			data.add(independent, dependent);			
		}

		@Override
		public Color getColor() {
			return plotLine.getForeground();
		}

		@Override
		public Icon getIcon() {
			return plotLine.getPointIcon();
		}

		@Override
		public void setColor(Color c) {
			plotLine.setForeground(c);
		}

		@Override
		public void removeFirst(int count) {
			data.removeFirst(Math.min(count, data.getPointCount()));
		}

		@Override
		public void appendData(double[] independent, double[] dependent) {
			for (int i = 0 ; i < Math.min(independent.length, dependent.length); i++) {
				appendData(independent[i], dependent[i]);
			}
		}

		@Override
		public void prependData(double independent, double dependent) {
			prependData(new double[]{independent}, new double[]{dependent});
		}

		@Override
		public void prependData(double[] independent, double[] dependent) {
			data.prepend(independent, 0, dependent, 0, Math.min(independent.length, dependent.length));
		}

		@Override
		public void removeLast(int count) {
			data.removeLast(count);
		}
		
	}

	@Override
	public void addDataSet(String dataSetName, Color plottingColor,
			AbstractLegendEntry legend) {
		addDataSet(dataSetName, plottingColor);
		AbstractPlotDataSeries series = dataManager.getNamedDataSeries(dataSetName);
		if (series != null) {
			series.setLegendEntry(legend);
		}
	}

	@Override
	public void attachLocalControl(AbstractPlotLocalControl control) {
		plotPanel.add(control, 0);
		SpringLayout layout = (SpringLayout) plotPanel.getLayout();
		for (AttachmentLocation location : control.getDesiredAttachmentLocations()) {
			if (location != null) {
				layout.putConstraint(location.getControlPlacement(), control,
						location.getDistance(), location.getContentPlacement(), plotPanel.getContents());
			}
		}
		PlotObserver o = control.getPlotObserver();
		if (o != null) {
			this.registerObservor(o);
		}
		localControls.addControl(control);
	}

	@Override
	public Collection<AbstractAxis> getAxes() {
		return Arrays.asList( (AbstractAxis) plotPanel.getXAxis(), (AbstractAxis) plotPanel.getYAxis(), timeAxis) ;
	}

	@Override
	public void notifyObserversAxisChanged(AbstractAxis axis) {
		for (PlotObserver o : this.observers) {
			o.plotAxisChanged(this, axis);
		}
	}
	
	@Override
	public Collection<AbstractAxisBoundManager> getBoundManagers(AxisVisibleOrientation axis) {
		return boundManagers.get(axis);
	}
	
}
