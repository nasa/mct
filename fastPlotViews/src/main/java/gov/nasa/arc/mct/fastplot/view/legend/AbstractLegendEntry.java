package gov.nasa.arc.mct.fastplot.view.legend;

import gov.nasa.arc.mct.fastplot.bridge.AbstractPlotLine;

public interface AbstractLegendEntry {
	public void attachPlotLine(AbstractPlotLine plotLine);
	public String getDisplayedName();
	public void setBaseDisplayName(String displayName);
}
