package gov.nasa.arc.mct.fastplot.bridge;

import java.awt.Color;
import java.util.SortedMap;

public interface AbstractPlotDataManager {

	public abstract void addDataSet(String dataSetName, Color plottingColor);

	public abstract void addData(String feed, SortedMap<Long, Double> points);

	public abstract void informUpdateCacheDataStreamStarted();
	
	public abstract void informResizeEvent();

	public abstract void resizeAndReloadPlotBuffer();
	
	public abstract AbstractPlotDataSeries getNamedDataSeries(String name);
}