package gov.nasa.arc.mct.fastplot.bridge;

import java.awt.Color;

import javax.swing.Icon;

public interface AbstractPlotLine {
	public void  addData(double independent, double dependent);
	public Color getColor();
	public Icon  getIcon();
	public void  setColor(Color c);
	public void  removeFirst(int count);
}
