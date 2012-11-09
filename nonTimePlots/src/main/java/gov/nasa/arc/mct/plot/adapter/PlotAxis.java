package gov.nasa.arc.mct.plot.adapter;

import java.awt.geom.Point2D;

public interface PlotAxis extends PlotComponent {
	public void    setPadding (int padding);
	public void    setStart   (double start);
	public void    setEnd     (double end);
	public double  toLogical  (Point2D physical);
	public int     toPhysical (Point2D logical);
}
