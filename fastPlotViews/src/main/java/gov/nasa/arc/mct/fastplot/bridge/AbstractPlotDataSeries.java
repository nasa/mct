package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;

public interface AbstractPlotDataSeries {
	public AbstractLegendEntry getLegendEntry();
	public void setLegendEntry(AbstractLegendEntry entry);
}
