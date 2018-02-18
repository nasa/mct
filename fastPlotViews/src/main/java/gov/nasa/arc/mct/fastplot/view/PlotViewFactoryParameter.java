package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

public class PlotViewFactoryParameter{
	
	PlotSettings settings;
	PlotViewManifestation parentManifestation;
	PlotView oldPlot;
	AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm;
	long currentTime;
    int numberOfSubPlots; 
    String viewStateTimeSystem;
	
    
    public PlotViewFactoryParameter(PlotSettings settings,
			PlotViewManifestation parentManifestation, PlotView oldPlot,
			AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm,
			long currentTime, int numberOfSubPlots, String viewStateTimeSystem) {
		super();
		this.settings = settings;
		this.parentManifestation = parentManifestation;
		this.oldPlot = oldPlot;
		this.plotLabelingAlgorithm = plotLabelingAlgorithm;
		this.currentTime = currentTime;
		this.numberOfSubPlots = numberOfSubPlots;
		this.viewStateTimeSystem = viewStateTimeSystem;
	}


	public PlotSettings getSettings() {
		return settings;
	}


	public PlotViewManifestation getParentManifestation() {
		return parentManifestation;
	}


	public PlotView getOldPlot() {
		return oldPlot;
	}


	public AbbreviatingPlotLabelingAlgorithm getPlotLabelingAlgorithm() {
		return plotLabelingAlgorithm;
	}


	public void setSettings(PlotSettings settings) {
		this.settings = settings;
	}


	public void setParentManifestation(PlotViewManifestation parentManifestation) {
		this.parentManifestation = parentManifestation;
	}


	public void setOldPlot(PlotView oldPlot) {
		this.oldPlot = oldPlot;
	}


	public void setPlotLabelingAlgorithm(
			AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm) {
		this.plotLabelingAlgorithm = plotLabelingAlgorithm;
	}


	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}


	public void setNumberOfSubPlots(int numberOfSubPlots) {
		this.numberOfSubPlots = numberOfSubPlots;
	}


	public void setViewStateTimeSystem(String viewStateTimeSystem) {
		this.viewStateTimeSystem = viewStateTimeSystem;
	}


	public long getCurrentTime() {
		return currentTime;
	}


	public int getNumberOfSubPlots() {
		return numberOfSubPlots;
	}


	public String getViewStateTimeSystem() {
		return viewStateTimeSystem;
	}
    
    
    
	
	
	
	

}
