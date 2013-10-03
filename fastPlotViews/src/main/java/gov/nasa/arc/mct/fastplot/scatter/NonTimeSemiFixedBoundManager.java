package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;

/**
 * Manages plot axis boundary, implementing semi-fixed behavior. In semi-fixed mode, 
 * boundaries will automatically adjust to show out-of-bounds data, but can return  
 * to original settings upon a "collapse" call. 
 * 
 * Note that this is equivalent to running in Fixed mode and invoking "expand" 
 * whenever out-of-bounds data is detected. 
 * 
 * @author vwoeltje
 *
 */
public class NonTimeSemiFixedBoundManager extends NonTimeFixedBoundManager {

	public NonTimeSemiFixedBoundManager(AbstractPlottingPackage plot,
			AbstractAxis axis, boolean isMaximal) {
		super(plot, axis, isMaximal);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.scatter.NonTimeFixedBoundManager#informPointPlottedAtTime(long, double)
	 */
	@Override
	public void informPointPlottedAtTime(long timestamp, double value) {
		// Semi-fixed is like regular fixed, but auto-expands whenever an alarm is raised
		super.informPointPlottedAtTime(timestamp, value);
		if (super.getState() == LimitAlarmState.ALARM_RAISED) {
			expand();
		}
	}
	
	

}
