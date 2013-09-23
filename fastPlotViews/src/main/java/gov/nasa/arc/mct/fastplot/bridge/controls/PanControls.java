package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis.AxisVisibleOrientation;
import gov.nasa.arc.mct.fastplot.view.IconLoader.Icons;

import java.awt.event.KeyEvent;

/**
 * Buttons to pan along an axis.
 * 
 * @author vwoeltje
 *
 */
public class PanControls extends AbstractPanZoomControls {
	private static final long serialVersionUID = 3970100144412350694L;
	
	/**
	 * Create pan controls for the specified axis.
	 * @param axis
	 */
	public PanControls(AbstractAxis axis) {
		super(axis);
	}
	
	@Override
	public void adjustAxis(AbstractAxis axis, double currentSpan, boolean less) {		
		axis.shift(currentSpan * (PlotConstants.PANNING_PERCENTAGE / 100.) * (less ? -1 : 1));
	}

	@Override
	protected Icons getIcon(AxisVisibleOrientation o, boolean less) {
		switch (o) {
		case HORIZONTAL: return less ? Icons.PLOT_PAN_LEFT_ARROW_ICON : Icons.PLOT_PAN_RIGHT_ARROW_ICON;
		case VERTICAL  : return less ? Icons.PLOT_PAN_DOWN_ARROW_ICON : Icons.PLOT_PAN_UP_ARROW_ICON;
		}
		return null;
	}
	
	@Override
	protected String getName(AxisVisibleOrientation o, boolean less) {
		switch (o) {
		case HORIZONTAL: return less ? "PanLeft" : "PanRight";
		case VERTICAL  : return less ? "PanDown" : "PanUp";
		}
		return null;
	}

	@Override
	protected int getTriggerKeyCode() {
		return KeyEvent.VK_CONTROL;
	}
}
