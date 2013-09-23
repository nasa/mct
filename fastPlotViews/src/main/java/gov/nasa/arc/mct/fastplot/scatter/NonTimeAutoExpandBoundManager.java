package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;

/**
 * Manages plot axis boundary, implementing auto-expand behavior. This will adjust 
 * axes to fit data as it comes in, discarding the old axis boundary entirely. 
 * 
 * 
 * @author vwoeltje
 *
 */
public class NonTimeAutoExpandBoundManager extends NonTimeSemiFixedBoundManager {

	public NonTimeAutoExpandBoundManager(AbstractPlottingPackage plot,
			AbstractAxis axis, boolean isMaximal) {
		super(plot, axis, isMaximal);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.scatter.NonTimeFixedBoundManager#getState()
	 */
	@Override
	public LimitAlarmState getState() {
		// Auto expand always fits data and doesn't retain old bounds, so there 
		// is never any out-of-bounds state
		return LimitAlarmState.NO_ALARM;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.scatter.NonTimeFixedBoundManager#collapse()
	 */
	@Override
	public void collapse() {
		// Auto expand means old info about bounds can be discarded - no "collapse"
	}

}
