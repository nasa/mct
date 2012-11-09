package gov.nasa.arc.mct.plot.adapter;

import gov.nasa.arc.mct.plot.adapter.Plot.PlotAxisType;

import java.awt.Color;
import java.awt.Font;

public interface PlotAppearance {
	public Font  getFont(PlotAxisType axis);
	public Color getFrameBackground();
	public Color getBackground();
	public Color getGridColor();
	public Color getAxisColor();
	// TODO: Tick marks? Other stuffs?
}
