package gov.nasa.arc.mct.fastplot.bridge.controls;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a minimal implementation of a manager for local controls; simply ensures that key events 
 * and so forth are passed along to managed controls. 
 * @author vwoeltje
 */
public class PlotLocalControlsManagerImpl implements AbstractPlotLocalControlsManager {
	private List<AbstractPlotLocalControl> controls = new ArrayList<AbstractPlotLocalControl>();
	
	/**
	 * Add a new local control to be managed
	 * @param control
	 */
	public void addControl(AbstractPlotLocalControl control) {
		controls.add(control);
	}

	@Override
	public void informKeyState(int key, boolean pressed) {
		for (AbstractPlotLocalControl control : controls) {
			control.informKeyState(key, pressed);
		}
	}

	@Override
	public void informMouseHover(boolean inPlotArea) {
		for (AbstractPlotLocalControl control : controls) {
			control.informMouseHover(inPlotArea);
		}
	}

}
