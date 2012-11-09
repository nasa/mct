package gov.nasa.arc.mct.plot.bridge;

import gov.nasa.arc.mct.plot.adapter.PlotContents;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

import plotter.xy.XYPlotContents;

public class PlotterPlotContents extends XYPlotContents implements PlotContents {
	private static final long serialVersionUID = -292214241430140885L;

	@Override
	public JComponent getRepresentation() {
		return this;
	}

	@Override
	public Point2D toLogical(Point physical) {
		Point2D.Double point = new Point2D.Double();
		this.toLogical(point, physical);
		return point;
	}

	@Override
	public Point toPhysical(Point2D logical) {
		// TODO Auto-generated method stub
		return null;
	}
}
