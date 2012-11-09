package gov.nasa.arc.mct.plot.bridge;

import plotter.xy.ScatterXYPlotLine;
import plotter.xy.SimpleXYDataset;
import gov.nasa.arc.mct.plot.adapter.PlotLine;

public class ScatterPlotLineFactory implements PlotLineFactory {
	private PlotterPlot plot;
	private boolean     rotated;
	
	protected ScatterPlotLineFactory(PlotterPlot plot, boolean rotated) {
		this.plot = plot;
		this.rotated = rotated;
	}
	
	@Override
	public PlotLine createLine() {
		ScatterXYPlotLine plotLine = new ScatterXYPlotLine(plot.getXAxis(), plot.getYAxis());
		SimpleXYDataset data = new SimpleXYDataset(plotLine);
		return new PlotterPlotLine(plotLine, data, rotated);
	}
}
