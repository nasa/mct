package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis.AxisVisibleOrientation;
import gov.nasa.arc.mct.fastplot.bridge.PlotObserver;
import gov.nasa.arc.mct.fastplot.view.IconLoader.Icons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.SpringLayout;

/**
 * Base class for pan- or zoom-style controls. Both controls behave similarly in 
 * terms of layout, interaction, etc.; this class describes those commonalities. 
 * 
 * @author vwoeltje
 *
 */
public abstract class AbstractPanZoomControls extends AbstractPlotLocalControl implements ActionListener {
	private static final long serialVersionUID = 3970100144412350694L;

	private static final List<AttachmentLocation> X_ATTACHMENTS = Arrays.asList(
				new AttachmentLocation(SpringLayout.NORTH, SpringLayout.SOUTH, 2),
				new AttachmentLocation(SpringLayout.HORIZONTAL_CENTER, SpringLayout.HORIZONTAL_CENTER, 0));
	private static final List<AttachmentLocation> Y_ATTACHMENTS = Arrays.asList(
				new AttachmentLocation(SpringLayout.EAST, SpringLayout.WEST, -2),
				new AttachmentLocation(SpringLayout.VERTICAL_CENTER, SpringLayout.VERTICAL_CENTER, 0));
	
	private AbstractAxis axis;
	private JButton      toStart, toEnd;
	private boolean      keyState = false;
	private boolean      mouseState = false;
	
	/**
	 * Create controls for the specified plot axis. The axis will be used to determine the orientation 
	 * of controls (horizontal or vertical), and will receive updates when controls are clicked.
	 * @param axis
	 */
	public AbstractPanZoomControls(AbstractAxis axis) {
		super();
		this.axis = axis;
		AxisVisibleOrientation orientation = axis.getVisibleOrientation();
		toStart = makeButton(getIcon(orientation, true), this, getName(orientation, true));
		toEnd   = makeButton(getIcon(orientation, false), this, getName(orientation, false));
		toStart.setVisible(true);
		toEnd.setVisible(true);

		switch(axis.getVisibleOrientation()) {
		case HORIZONTAL:
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(toStart);
			add(toEnd);
			break;
		case VERTICAL:
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(toEnd);
			add(toStart);
			break;						
		}
		
		setVisible(false);
	}
	
	/**
	 * Apply whatever adjustment this control performs (panning or zooming)
	 * The "less" argument indicates whether or not this adjustment goes toward the bottom-left.
	 * 
	 * Note that currentSpan is presumed to be calculated as getEnd-getStart for the axis; so, 
	 * the sign will be negative if the axes have been inverted (i.e. "max at left"). Implementing 
	 * classes may use this sign (implicitly or explicitly) to adjust the axis in the correct 
	 * direction.
	 * 
	 * @param axis the axis to update
	 * @param currentSpan the current axis span
	 * @param less
	 */
	public abstract void adjustAxis(AbstractAxis axis, double currentSpan, boolean less);

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.bridge.controls.AbstractPlotLocalControl#informKeyState(int, boolean)
	 */
	@Override
	public void informKeyState(int key, boolean pressed) {
		if (key == getTriggerKeyCode()) {
			keyState = pressed;
			setVisible(mouseState && keyState);
		}
	}


	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.bridge.controls.AbstractPlotLocalControl#informMouseHover(boolean)
	 */
	@Override
	public void informMouseHover(boolean inPlotArea) {
		mouseState = inPlotArea;
		setVisible(mouseState && keyState);
	}

	@Override
	public Collection<AttachmentLocation> getDesiredAttachmentLocations() {
		switch (axis.getVisibleOrientation()) {
		case HORIZONTAL: return X_ATTACHMENTS;
		case VERTICAL: return Y_ATTACHMENTS;
		}
		return Collections.<AttachmentLocation>emptyList();
	}

	@Override
	public PlotObserver getPlotObserver() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(toStart)) {
			adjustAxis(axis, axis.getEnd() - axis.getStart(), true);
		} else if (evt.getSource().equals(toEnd)) {
			adjustAxis(axis, axis.getEnd() - axis.getStart(), false);
		}		
		//Note: Axis is responsible for introducing a pin and/or notifying observers
	}
	

	/**
	 * Utility method to determine appropriate icon for pan button
	 * @param less true if the pan button goes toward the bottom or left; otherwise, false
	 * @return
	 */
	protected abstract Icons getIcon(AxisVisibleOrientation o, boolean less);
	
	/**
	 * Utility method to determine  an appropriate name for a pan button
	 * @param less true if the pan button goes toward the bottom or left; otherwise, false
	 * @return
	 */
	protected abstract String getName(AxisVisibleOrientation o, boolean less);
		
	/**
	 * Get the key that is used to show/hide this control; should be one of the 
	 * KeyEvent.VK_* codes. 
	 * @return the key code used to enable these controls
	 */
	protected abstract int getTriggerKeyCode();

}
