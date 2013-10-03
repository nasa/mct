package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;

public class ImplicitTimeAxis implements AbstractAxis {
	private long start = 0;
	private long end = 1;

	@Override
	public long getStartAsLong() {
		return start;
	}

	@Override
	public long getEndAsLong() {
		return end;
	}

	@Override
	public void setStart(long start) {
		this.start = start;
	}

	@Override
	public void setEnd(long end) {
		this.end = end;
	}

	@Override
	public double getStart() {
		return start;
	}

	@Override
	public double getEnd() {
		return end;
	}

	@Override
	public void setStart(double start) {
		this.start = (long) start;
	}

	@Override
	public void setEnd(double end) {
		this.end = (long) end;
	}

	@Override
	public void shift(double offset) {
		this.start += (long) offset;
		this.end   += (long) offset;
	}

	@Override
	public AxisVisibleOrientation getVisibleOrientation() {
		// Time axis has no visible orientation
		return null;
	}

}
