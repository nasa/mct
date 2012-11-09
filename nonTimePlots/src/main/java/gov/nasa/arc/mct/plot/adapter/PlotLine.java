package gov.nasa.arc.mct.plot.adapter;

import gov.nasa.arc.mct.plot.data.DataReceiver;
import gov.nasa.arc.mct.plot.settings.LineSettings;

import java.awt.Color;

import javax.swing.Icon;

public interface PlotLine extends DataReceiver, PlotComponent {
	public Color getColor     ();
	public Icon  getIcon      ();
	public void  setHighlight (boolean highlighted);
	public void  configure    (LineSettings settings);
}
