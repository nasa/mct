package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis.AxisVisibleOrientation;
import gov.nasa.arc.mct.fastplot.view.IconLoader.Icons;

import java.awt.event.KeyEvent;

/**
 * Buttons to zoom in/out on an axis.
 * 
 * @author vwoeltje
 *
 */
public class ZoomControls extends AbstractPanZoomControls {
	private static final long serialVersionUID = 3970100144412350694L;
	
	/**
	 * Create pan controls for the specified axis.
	 * @param axis
	 */
	public ZoomControls(AbstractAxis axis) {
		super(axis);
	}
	
	@Override
	public void adjustAxis(AbstractAxis axis, double currentSpan, boolean less) {
		double scale = 1.0 + (PlotConstants.ZOOMING_TIME_AXIS_PERCENTAGE / 100.0);
		if (!less) scale = 1 / scale;
		double next = currentSpan * scale;
		double diff = (next - currentSpan) / 2;
		axis.setStart(axis.getStart() - diff);
		axis.setEnd  (axis.getEnd  () + diff);
	}

	@Override
	protected Icons getIcon(AxisVisibleOrientation o, boolean less) {
		switch (o) {
		case HORIZONTAL: return less ? Icons.PLOT_ZOOM_OUT_X_ICON : Icons.PLOT_ZOOM_IN_X_ICON;
		case VERTICAL  : return less ? Icons.PLOT_ZOOM_OUT_Y_ICON : Icons.PLOT_ZOOM_IN_Y_ICON;
		}
		return null;
	}
	
	@Override
	protected String getName(AxisVisibleOrientation o, boolean less) {
		switch (o) {
		case HORIZONTAL: return less ? "ZoomOutX" : "ZoomInX";
		case VERTICAL  : return less ? "ZoomOutY" : "ZoomInY";
		}
		return null;
	}

	@Override
	protected int getTriggerKeyCode() {
		return KeyEvent.VK_ALT;
	}
}
