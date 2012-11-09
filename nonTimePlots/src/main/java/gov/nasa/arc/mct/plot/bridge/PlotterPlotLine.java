package gov.nasa.arc.mct.plot.bridge;

import gov.nasa.arc.mct.plot.adapter.PlotLine;
import gov.nasa.arc.mct.plot.settings.LineSettings;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JComponent;

import plotter.xy.XYDataset;
import plotter.xy.XYPlotLine;

public class PlotterPlotLine implements PlotLine {
	private XYPlotLine  plotLine;
	private XYDataset   dataSet;
	private boolean     rotated;
	
	protected PlotterPlotLine(XYPlotLine plotLine, XYDataset dataSet, boolean rotated) {
		this.plotLine = plotLine;
		this.dataSet  = dataSet;
		this.rotated  = rotated;
	}

	@Override
	public void addData(double independent, double dependent) {
		if (rotated)  dataSet.add(dependent, independent);
		else          dataSet.add(independent, dependent);
	}

	@Override
	public JComponent getRepresentation() {
		return plotLine;
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
	public void setHighlight(boolean highlighted) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure(LineSettings settings) {
		// TODO Auto-generated method stub
		
	}

}
