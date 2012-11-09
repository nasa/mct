package gov.nasa.arc.mct.plot.adapter;

import gov.nasa.arc.mct.plot.settings.PlotConfiguration;

import java.util.List;

/** 
 * A single instance of a plot; may contain one or more lines.
 * @author vwoeltje
 */
public interface Plot extends PlotComponent {
	public void           configure(PlotConfiguration configuration);
	public PlotLine       createPlotLine();
	public List<PlotLine> getPlotLines();
	public PlotAppearance getAppearance();
	public PlotAxis       getAxis(PlotAxisType axisType);
	public PlotContents   getPlotContents();
	public void           setAppearance(PlotAppearance appearance);
	
	
	public static enum PlotAxisType {
		DEPENDENT , INDEPENDENT , X , Y , TEMPORAL
	}
}
