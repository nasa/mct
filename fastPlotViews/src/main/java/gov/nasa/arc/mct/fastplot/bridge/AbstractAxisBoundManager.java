package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;

/**
 * Controls one bounding edge of a plot along one axis. For instance, will auto-expand or 
 * jump, interact with purple arrows... 
 * 
 * A plot may use four of these (one for each edge of the plot)
 * 
 * @author vwoeltje
 *
 */
public interface AbstractAxisBoundManager {
	
	/**
	 * Notify the bounds manager that a point has been plotted at a certain time. This 
	 * may cause bounds to adjust or purple arrows to appear, as is appropriate to a 
	 * given mode.
	 * @param timestamp the time at which the point was plotted
	 * @param value the value that has been plotted
	 */
	public void informPointPlottedAtTime(long timestamp, double value);
	
	/**
	 * Get the state of the boundary
	 * @return the maximum state
	 */
	public LimitAlarmState getState();
	
	/**
	 * Expand to fit the current extremum
	 */
	public void expand();
	
	/**
	 * Collapse back to the default bound at this edge.
	 */
	public void collapse();
	
	/**
	 * Get the axis that is being managed by this bound.
	 * @return the axis whose edge is being controlled
	 */
	public AbstractAxis getAxis();
	
	/**
	 * Whether or not this bound manages the start of the axis (if false, 
	 * it must manage the end). Note that this is distinct from "maximum" 
	 * or "minimum"
	 * 
	 * (Regardless of maximum bound location, the "start" is always the 
	 * bottom or left edge of the plot, and the "end" is always the 
	 * top or right edge.)
	 * 
	 * @return true if the "start" edge is managed; otherwise "false"
	 */
	public boolean isStart();
}
