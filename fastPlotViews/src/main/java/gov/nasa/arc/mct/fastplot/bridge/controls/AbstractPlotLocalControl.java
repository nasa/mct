package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotLocalControlsManager;
import gov.nasa.arc.mct.fastplot.bridge.PlotObserver;
import gov.nasa.arc.mct.fastplot.view.IconLoader;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Base class for plot local controls. These are controls which will appear as part of the 
 * plot view, used to make immediate changes to what is being shown (as opposed to broader 
 * changes to the view).  
 * 
 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage#attachLocalControl(AbstractPlotLocalControl)
 * 
 * @author vwoeltje
 */
public abstract class AbstractPlotLocalControl extends JPanel {
	private static final long serialVersionUID = 5762300765204572813L;
	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(PlotLocalControlsManager.class.getName().substring(0, 
        			 	         PlotLocalControlsManager.class.getName().lastIndexOf("."))+".Bundle");
		
	private static final Border BUTTON_BORDER = BorderFactory.createEmptyBorder(
			PlotConstants.ARROW_BUTTON_BORDER_STYLE_BOTTOM, 
            PlotConstants.ARROW_BUTTON_BORDER_STYLE_LEFT, 
            PlotConstants.ARROW_BUTTON_BORDER_STYLE_BOTTOM,
            PlotConstants.ARROW_BUTTON_BORDER_STYLE_RIGHT);
	
	/**
	 * Controls are attached to one specific plot, represented by this object
	 */
	private AbstractPlottingPackage plottingPackage = null;
	
	/**
	 * Default constructor; does minimal setup of the underlying JPanel
	 */
	public AbstractPlotLocalControl() {
		setOpaque(false);
	}
	
	/**
	 * Inform the local control about a change in key state. Some 
	 * controls only show/hide while certain keys are pressed.
	 * @param key the key code, from KeyEvent
	 * @param pressed whether or not the key is down
	 */
	public void informKeyState(int key, boolean pressed) {
		// By default, do nothing; some controls may override this.		
	}
	
	/**
	 * Informs the local control about whether or not the mouse is 
	 * within the local control's area.
	 * @param inPlotArea true if the mouse is hovering over the plot
	 */
	public void informMouseHover(boolean inPlotArea) {
		// By default, do nothing; some controls may override this.
	}
	
	/**
	 * Get the desired attachment locations for this control. This collection describes 
	 * the desired positioning of this specific control upon the plot. 
	 * 
	 * @see AttachmentLocation
	 * 
	 * @return
	 */
	public abstract Collection<AttachmentLocation> getDesiredAttachmentLocations();
	
	/**
	 * Get a PlotObserver which should receive updates about the plot. Controls may use  
	 * this observer to respond to changes in the plot that should change the appearance 
	 * or behavior of control elements.
	 * 
	 * This method may return null to indicate that a control does not use an observer 
	 * (for instance, because it does not need to respond to any changes to the plot)
	 * 
	 * @return 
	 */
	public abstract PlotObserver getPlotObserver();
	
	/**
	 * Add this control to a specific instance of a plot. This updates the control's 
	 * stored reference to its targeted plotting package, invokes the plotting package's 
	 * attachControl method, and registers the control as an Observer for the plot.  
	 * 
	 * @param plottingPackage the plot to which this control should be attached
	 */
	public void attachTo(AbstractPlottingPackage plottingPackage) {
		this.plottingPackage = plottingPackage;
		PlotObserver observer = getPlotObserver();
		if (observer != null) {
			plottingPackage.registerObservor(observer);
		}
		//plottingPackage.attachControl(this)
	}
	
	@Override
	public boolean isOptimizedDrawingEnabled() {		
		return false;
	}
	
	/**
	 * Get the plot to which this control has been attached. (May be null, if 
	 * no plot attachment has been made)
	 * @return the plot that is being controlled
	 */
	protected AbstractPlottingPackage getPlot() {
		return plottingPackage;
	}
    
	/**
	 * Utility method to create an image-based JButton (since most plot controls use 
	 * this pattern, this method is provided as a convenience)
	 * 
	 * Note that buttonName will both be used to name the component, and to look up 
	 * an appropriate tooltip from the resource bundle (if available)
	 * 
	 * @param icon the icon used to represent this button
	 * @param listener the listener which should be notified of button presses
	 * @param buttonName the name for the button (also used to find tooltip)
	 * @return an image-based JButton attached to the appropriate listener
	 */
    protected JButton makeButton(IconLoader.Icons icon, ActionListener listener, String buttonName) {
    	JButton returnButton =  new JButton(icon.getIcon());
    	if (BUNDLE.containsKey(buttonName + ".Tooltip")) {
    		returnButton.setToolTipText(BUNDLE.getString(buttonName + ".Tooltip"));
    	}
        returnButton.setName(buttonName);
        returnButton.setOpaque(false);
        returnButton.setContentAreaFilled(false);
        returnButton.setFocusPainted(true);
        returnButton.setBorder(BUTTON_BORDER);
        if (listener != null) {
        	returnButton.addActionListener(listener);
        }
        return returnButton;
    }   
    
    /**
     * Describes the desired attachment between the control's Swing component 
     * and the plot content area.
     * 
     * Expected behavior from the plot should resemble:
     *   springLayout.putConstraint(controlPlacement, controlComponent,
     *                              distance,
     *                              contentPlacement, plotContentComponent);
     * 
     * Note that the plot (AbstractPlottingPackage) to which a local control 
     * is attached may not necessarily use SpringLayout, and is free to 
     * interpret these requested attachment locations differently.
     * 
     * @author vwoeltje
     */
    public static class AttachmentLocation {
    	private String controlPlacement;
    	private String contentPlacement;
    	private int    distance;
    	
    	protected AttachmentLocation (String placement) {
    		this(placement, placement, 0);
    	}
    	
		protected AttachmentLocation(String controlPlacement,
				String contentPlacement, int distance) {
			super();
			this.controlPlacement = controlPlacement;
			this.contentPlacement = contentPlacement;
			this.distance = distance;
		}
		
		/**
		 * Get the edge or location on the control's Swing component which should 
		 * be aligned to the plot area; should be one of SpringLayout.EAST, 
		 * SpringLayout.HORIZONTAL_CENTER, et cetera... (Although the plot does not 
		 * necessarily use SpringLayout, these parameters are well-suited to 
		 * describing desired control placement.) 
		 * 
		 * @return the location of the control's edge, as should be placed
		 */
		public String getControlPlacement() {
			return controlPlacement;
		}

		/**
		 * Get the edge or location on the plot content area where the control's  
		 * Swing component should be aligned; should be one of SpringLayout.EAST, 
		 * SpringLayout.HORIZONTAL_CENTER, et cetera... (Although the plot does not 
		 * necessarily use SpringLayout, these parameters are well-suited to 
		 * describing desired control placement.) 
		 * 
		 * @return the location on the plot where the control should be placed
		 */
		public String getContentPlacement() {
			return contentPlacement;
		}
		
		/**
		 * @return the distance from the placement point, in pixels
		 */
		public int getDistance() {
			return distance;
		}
    }        
}
