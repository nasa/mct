package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.PlotSubject;
import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;

public class LinearXYAxisWrapper extends LinearXYAxis implements AbstractAxis {
	private static final long serialVersionUID = -4672981390153479399L;

	
	public LinearXYAxisWrapper(XYDimension d) {
		super(d);
	}

	@Override
	public long getStartAsLong() {
		return (long) super.getStart();
	}

	@Override
	public long getEndAsLong() {
		// TODO Auto-generated method stub
		return (long) super.getEnd();
	}

	@Override
	public void setStart(long start) {
		super.setStart((double)start);
	}

	@Override
	public void setEnd(long end) {
		super.setEnd((double)end);
	}

	@Override
	public AxisVisibleOrientation getVisibleOrientation() {
		switch (this.getPlotDimension()) {
		case X: return AxisVisibleOrientation.HORIZONTAL;
		case Y: return AxisVisibleOrientation.VERTICAL;
		}
		return null;
	}
	
}
