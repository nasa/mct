package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis.AxisVisibleOrientation;
import gov.nasa.arc.mct.fastplot.bridge.PlotObserver;
import gov.nasa.arc.mct.fastplot.bridge.PlotSubject;
import gov.nasa.arc.mct.fastplot.view.IconLoader.Icons;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.SpringLayout;

/**
 * A CornerResetButton checks for changes made by local controls, and presents a button 
 * for the user to click in order to undo these changes and restore the previous axis 
 * state. 
 * 
 * @author vwoeltje
 *
 */
public class CornerResetButton extends AbstractPlotLocalControl implements PlotObserver, ActionListener {
	private static final long serialVersionUID = -348727120749498680L;
	
	/**
	 * The axes that this reset button looks at and may reset.
	 */
	private Collection<ControllableAxis> managedAxes = new ArrayList<ControllableAxis>();
	
	/**
	 * Create a new corner reset button; this will monitor the provided axes for local 
	 * changes and make a button visible as appropriate; when clicked, this button will reset 
	 * these all monitored axes.
	 * 
	 * Note that the shape and position of this button will be inferred by examining 
	 * the list of provided axes.
	 *  
	 * @param axes the axes to be monitored and reset by this button
	 */
	public CornerResetButton(ControllableAxis... axes) {
		super();
		setLayout(new GridLayout());
		Collections.addAll(managedAxes, axes);	
		add (makeButton(chooseIcon(), this, chooseName()));
		setVisible(false);
		setBorder(null);
	}

	/**
	 * Get the attachment location for this control.
	 * 
	 * X-Axis reset button appears bottom-right;
	 * Y-Axis reset button appears top-left;
	 * X & Y axis reset button appears bottom-left.
	 */
	@Override
	public Collection<AttachmentLocation> getDesiredAttachmentLocations() {
		String verticalEdge = isManaged(AxisVisibleOrientation.VERTICAL) ?
				SpringLayout.WEST : SpringLayout.EAST;
		String horizontalEdge = isManaged(AxisVisibleOrientation.HORIZONTAL) ?
				SpringLayout.SOUTH : SpringLayout.NORTH;
		return Arrays.asList(
				new AttachmentLocation(verticalEdge,   verticalEdge,   0),
				new AttachmentLocation(horizontalEdge, horizontalEdge, 0)
		);			
	}

	@Override
	public PlotObserver getPlotObserver() {
		return this;
	}

	@Override
	public void updateTimeAxis(PlotSubject subject, long startTime, long endTime) {
	}

	@Override
	public void plotAxisChanged(PlotSubject subject, AbstractAxis axis) {
		// When the plot axis has changed, check to see if those changes are local
		boolean dirty = true;
		for (ControllableAxis a : managedAxes) {
			// Only display if ALL axes have changed
			dirty &= a.isDirty();
		}
		setVisible(dirty);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Reset all axes when clicked
		for (ControllableAxis a : managedAxes) {
			a.reset();
		}
	}

	/**
	 * Utility method to determine if a given orientation (horizontal or vertical) is 
	 * among the axes being managed.
	 * @param o
	 * @return
	 */
	private boolean isManaged(AxisVisibleOrientation o) {		
		for (ControllableAxis axis : managedAxes) {
			if (axis.getVisibleOrientation() == o) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Choose the icon for this button, based on the orientation of the axes managed.
	 * @return
	 */
	private Icons chooseIcon() {
		Icons[] icons = { Icons.PLOT_CORNER_RESET_BUTTON_TOP_LEFT_GREY, Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY,
				          Icons.PLOT_CORNER_RESET_BUTTON_BOTTOM_LEFT_GREY, Icons.PLOT_CORNER_RESET_BUTTON_BOTTOM_RIGHT_GREY };
		int index = (isManaged(AxisVisibleOrientation.HORIZONTAL) ? 2 : 0) +
		         (isManaged(AxisVisibleOrientation.VERTICAL  ) ? 0 : 1);
		return icons[index];		
	}

	/**
	 * Choose the name for this button, based on the orientation of the axes managed.
	 * @return
	 */
	private String chooseName() {
		String[] names = { "TopLeftCornerButton", "TopRightCornerButton",
				           "BottomLeftCornerButton", "BottomRightCornerButton" };
		int index = (isManaged(AxisVisibleOrientation.HORIZONTAL) ? 2 : 0) +
		            (isManaged(AxisVisibleOrientation.VERTICAL  ) ? 0 : 1);
		return names[index];
	}

	@Override
	public void dataPlotted() {
		// TODO Auto-generated method stub
		
	}
}
