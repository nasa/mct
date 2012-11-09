package gov.nasa.arc.mct.plot.adapter;

import java.awt.Point;
import java.awt.geom.Point2D;

public interface PlotContents extends PlotComponent {
	public Point2D        toLogical  (Point   physical);
	public Point          toPhysical (Point2D logical );
}
