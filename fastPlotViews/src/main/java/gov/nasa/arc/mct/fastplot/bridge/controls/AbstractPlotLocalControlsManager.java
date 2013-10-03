package gov.nasa.arc.mct.fastplot.bridge.controls;

/**
 * Manages the local controls for a given plot. Primarily used to communicate user 
 * interactions to specific controls when the control is not able to listen for them 
 * locally in the Swing hierarchy (specifically, many controls are interested in either 
 * whether or not the mouse is over the plot area, or need to listen for key events 
 * even when they don't have focus.)
 * 
 * Note: May consider removing this and simply return a Collection<AbstractPlotLocalControl> 
 * from AbstractPlottingPackage to simplify API. (The current implementation 
 * permits some cross-compatibility with the older PlotLocalControlManager.) 
 * 
 * @see gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage#getLocalControlsManager()
 * 
 * @author vwoeltje
 */
public interface AbstractPlotLocalControlsManager {	
	/**
	 * Notify local controls that a specific key has been pressed or released.
	 * @param key the key code pressed/released (one of KeyEvent.VK_*)
	 * @param pressed true if press; false if released
	 */
	public void informKeyState(int key, boolean pressed);
	
	/**
	 * Notify local controls of changes to the mouse position with regard to the plot
	 * @param inPlotArea true if hovering over plot; false if mouse has left
	 */
	public void informMouseHover(boolean inPlotArea);
}
