package gov.nasa.arc.mct.plot.bridge;

import gov.nasa.arc.mct.plot.adapter.PlotAxis;

import java.awt.geom.Point2D;

import javax.swing.JComponent;

import plotter.xy.XYAxis;
import plotter.xy.XYDimension;

public class PlotterPlotAxis implements PlotAxis {
	private boolean inverted;
	private XYAxis  xyAxis;
		
	protected PlotterPlotAxis(XYAxis plotterPlotAxis, boolean inverted) {
		this.xyAxis = plotterPlotAxis;
		this.inverted = inverted;
	}

	@Override
	public JComponent getRepresentation() {
		return xyAxis;
	}

	@Override
	public void setPadding(int padding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStart(double start) {
		if (!inverted) xyAxis.setStart(start);
		else           xyAxis.setEnd  (start);
	}

	@Override
	public void setEnd(double end) {
		if ( inverted) xyAxis.setStart(end);
		else           xyAxis.setEnd  (end);
	}

	@Override
	public double toLogical(Point2D physical) {
		return xyAxis.toLogical(
				(int) ((xyAxis.getPlotDimension() == XYDimension.X) ?
				physical.getX() : physical.getY() ));
	}

	@Override
	public int toPhysical(Point2D logical) {
		return xyAxis.toPhysical(
				(xyAxis.getPlotDimension() == XYDimension.X) ?
				logical.getX() : logical.getY());
	}

}
