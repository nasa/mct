package gov.nasa.arc.mct.fastplot.bridge;

import java.awt.Color;

import javax.swing.Icon;

public interface AbstractPlotLine {
	public void  appendData(double   independent, double dependent);
	public void  appendData(double[] independent, double[] dependent);
	public void  prependData(double   independent, double   dependent);
	public void  prependData(double[] independent, double[] dependent);
	public Color getColor();
	public Icon  getIcon();
	public void  setColor(Color c);
	public void  removeFirst(int count);
	public void  removeLast(int count);
}
