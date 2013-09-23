package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxisBoundManager;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.bridge.PlotObserver;
import gov.nasa.arc.mct.fastplot.bridge.PlotSubject;
import gov.nasa.arc.mct.fastplot.view.IconLoader.Icons;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SpringLayout;

/**
 * A boundary arrow ("purple arrow") gives the user a visual indicator about whether or not 
 * out-of-bounds data has flowed; clicking this indicator will toggle between expanding the 
 * axis to show out of bounds data, and collapsing back to the original state.
 * 
 * Purple arrows states are:
 *  - (no arrow):  No out of bounds data in current time span
 *  - Solid:       Out of bounds data within the current plot time span (not yet seen)
 *  - Translucent: Currently expanded to show out-of-bounds data (clicking collapses)
 *  - Outlined:    Out of bounds data within the current plot time span (but it's been seen)
 * 
 * @author vwoeltje
 */
public class BoundaryArrow extends AbstractPlotLocalControl implements ActionListener, PlotObserver {
	private static final long serialVersionUID = 5672919575563084898L;
	
	private LimitAlarmState currentState;
	private AbstractAxisBoundManager boundary;
	private Map<LimitAlarmState, JComponent> boundaryIcons = new HashMap<LimitAlarmState, JComponent>();
	
	/**
	 * Create a new boundary arrow.
	 * @param boundary the boundary which this arrow is used to represent / interact with
	 * @param isStart whether or not this boundary is at the start of an axis
	 */
	public BoundaryArrow(AbstractAxisBoundManager boundary) {
		super();
		setLayout(new GridLayout());
		this.boundary = boundary;
		currentState = LimitAlarmState.NO_ALARM;
		initializeButtons();
		updateAppearance(boundary.getState());
		setBorder(null);
	}

	private void updateAppearance(LimitAlarmState state) {
		if (currentState != state) {
			if (state == LimitAlarmState.NO_ALARM) {
				setVisible(false);
			} else if (state != null) {
				removeAll();
				add(boundaryIcons.get(state));
				setVisible(true);
			}
			currentState = state;
		}
	}
	
	@Override
	public Collection<AttachmentLocation> getDesiredAttachmentLocations() {
		Collection<AttachmentLocation> attachments = new ArrayList<AttachmentLocation>();
		switch (boundary.getAxis().getVisibleOrientation()) {
		case HORIZONTAL:
			attachments.add(new AttachmentLocation(SpringLayout.VERTICAL_CENTER));
			attachments.add(new AttachmentLocation(boundary.isStart() ? 
					SpringLayout.WEST : SpringLayout.EAST));
			break;
		case VERTICAL:
			attachments.add(new AttachmentLocation(SpringLayout.HORIZONTAL_CENTER));
			attachments.add(new AttachmentLocation(boundary.isStart() ? 
					SpringLayout.SOUTH : SpringLayout.NORTH));
			break;
		}		
		return attachments;
	}

	@Override
	public PlotObserver getPlotObserver() {
		return this;
	}

	
	private void initializeButtons() {
		switch (boundary.getAxis().getVisibleOrientation()) {
		case HORIZONTAL:
			boundaryIcons.put(LimitAlarmState.ALARM_RAISED, makeButton(boundary.isStart() ?
					Icons.PLOT_LEFT_ARROW_SOLID_ICON : Icons.PLOT_RIGHT_ARROW_SOLID_ICON,
					this, "ShowAllData"));
			boundaryIcons.put(LimitAlarmState.ALARM_CLOSED_BY_USER, makeButton(boundary.isStart() ?
					Icons.PLOT_LEFT_ARROW_HOLLOW_ICON : Icons.PLOT_RIGHT_ARROW_HOLLOW_ICON,
					this, "ShowAllDataAgain"));
			boundaryIcons.put(LimitAlarmState.ALARM_OPENED_BY_USER, makeButton(!boundary.isStart() ?
					Icons.PLOT_LEFT_ARROW_TRANSLUCENT_ICON : Icons.PLOT_RIGHT_ARROW_TRANSLUCENT_ICON,
					this, "HideOOBData"));
			break;
		case VERTICAL:
			boundaryIcons.put(LimitAlarmState.ALARM_RAISED, makeButton(boundary.isStart() ?
					Icons.PLOT_DOWN_ARROW_SOLID_ICON : Icons.PLOT_UP_ARROW_SOLID_ICON,
					this, "ShowAllData"));
			boundaryIcons.put(LimitAlarmState.ALARM_CLOSED_BY_USER, makeButton(boundary.isStart() ?
					Icons.PLOT_DOWN_ARROW_HOLLOW_ICON : Icons.PLOT_UP_ARROW_HOLLOW_ICON,
					this, "ShowAllDataAgain"));
			boundaryIcons.put(LimitAlarmState.ALARM_OPENED_BY_USER, makeButton(!boundary.isStart() ?
					Icons.PLOT_DOWN_ARROW_TRANSLUCENT_ICON : Icons.PLOT_UP_ARROW_TRANSLUCENT_ICON,
					this, "HideOOBData"));
			break;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		switch (boundary.getState()) {
		case ALARM_RAISED:
		case ALARM_CLOSED_BY_USER:
			boundary.expand();
			break;
		case ALARM_OPENED_BY_USER:
			boundary.collapse();
			break;
		}
		updateAppearance(boundary.getState());
	}

	@Override
	public void updateTimeAxis(PlotSubject subject, long startTime, long endTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void plotAxisChanged(PlotSubject subject, AbstractAxis axis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataPlotted() {
		updateAppearance(boundary.getState());
	}
}
