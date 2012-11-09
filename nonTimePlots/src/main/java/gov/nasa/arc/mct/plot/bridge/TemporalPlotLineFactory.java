package gov.nasa.arc.mct.plot.bridge;

import gov.nasa.arc.mct.plot.adapter.PlotLine;
import plotter.xy.CompressingXYDataset;
import plotter.xy.DefaultCompressor;
import plotter.xy.LinearXYPlotLine;
import plotter.xy.XYDataset;
import plotter.xy.XYDimension;

public class TemporalPlotLineFactory implements PlotLineFactory {
	private PlotterPlot plot;
	private boolean     rotated;
	
	protected TemporalPlotLineFactory(PlotterPlot plot, boolean rotated) {
		this.plot = plot;
		this.rotated = rotated;
	}
	
	@Override
	public PlotLine createLine() {
		LinearXYPlotLine plotLine = new LinearXYPlotLine(plot.getXAxis(), plot.getYAxis(), rotated ? XYDimension.Y : XYDimension.X);
		XYDataset data = new CompressingXYDataset(plotLine, new DefaultCompressor());
		return new PlotterPlotLine(plotLine, data, rotated);
	}

}
