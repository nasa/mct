package gov.nasa.arc.mct.fastplot.bridge;

public interface AbstractAxis {
	public long getStartAsLong();
	public long getEndAsLong();
	public void setStart(long start);
	public void setEnd(long end);
	public double getStart();
	public double getEnd();
	public void setStart(double start);
	public void setEnd(double end);
	public void shift(double offset);
}
