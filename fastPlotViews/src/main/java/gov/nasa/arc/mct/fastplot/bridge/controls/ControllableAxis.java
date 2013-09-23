package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.PlotSubject;

/**
 * An observable axis wraps around an existing plot axis, but notifies listeners on 
 * all updates.
 * 
 * @author vwoeltje
 *
 */
public class ControllableAxis implements AbstractAxis {
	private AbstractAxis axis;
	private PlotSubject subject;

	private boolean dirty = false;
	private double recordedStart;
	private double recordedEnd;
	
	public ControllableAxis(PlotSubject subject, AbstractAxis axis) {
		super();
		this.axis = axis;
		this.subject = subject;
		recordedStart = axis.getStart();
		recordedEnd   = axis.getEnd();
	}

	/**
	 * @return
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#getStartAsLong()
	 */
	public long getStartAsLong() {
		return axis.getStartAsLong();
	}

	/**
	 * @return
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#getEndAsLong()
	 */
	public long getEndAsLong() {
		return axis.getEndAsLong();
	}

	/**
	 * @param start
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#setStart(long)
	 */
	public void setStart(long start) {
		recordPositions();
		axis.setStart(start);
		subject.notifyObserversAxisChanged(this);
	}

	/**
	 * @param end
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#setEnd(long)
	 */
	public void setEnd(long end) {
		recordPositions();
		axis.setEnd(end);
		subject.notifyObserversAxisChanged(this);
	}

	/**
	 * @return
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#getStart()
	 */
	public double getStart() {
		return axis.getStart();
	}

	/**
	 * @return
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#getEnd()
	 */
	public double getEnd() {
		return axis.getEnd();
	}

	/**
	 * @param start
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#setStart(double)
	 */
	public void setStart(double start) {
		recordPositions();
		axis.setStart(start);
		subject.notifyObserversAxisChanged(this);
	}

	/**
	 * @param end
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#setEnd(double)
	 */
	public void setEnd(double end) {
		recordPositions();
		axis.setEnd(end);
		subject.notifyObserversAxisChanged(this);
	}

	/**
	 * @param offset
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#shift(double)
	 */
	public void shift(double offset) {
		recordPositions();
		axis.shift(offset);
		subject.notifyObserversAxisChanged(this);
	}

	/**
	 * @return
	 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractAxis#getVisibleOrientation()
	 */
	public AxisVisibleOrientation getVisibleOrientation() {
		return axis.getVisibleOrientation();
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void reset() {
		dirty = false;
		axis.setStart(recordedStart);
		axis.setEnd(recordedEnd);
		subject.notifyObserversAxisChanged(this);
	}
	
	private void recordPositions() {
		if (!dirty) {
			recordedStart = axis.getStart();
			recordedEnd = axis.getEnd();
			dirty = true;
		}
	}

}
