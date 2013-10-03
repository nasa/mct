package gov.nasa.arc.mct.fastplot.bridge.controls;

import gov.nasa.arc.mct.fastplot.bridge.AbstractPlottingPackage;
import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Dispatches key events directly to plot local controls, regardless of focus (but only if the 
 * mouse is hovering over the plot). This is an alternative to other keyboard input strategies 
 * in Swing (KeyListener, InputMap) which only apply when components have focus, whose usage 
 * may require a lot of extra code to make sure that the plot has focus whenever the user might 
 * expect it to.
 * 
 * Usage note: It is advisable to attach this as an AncestorListener to a relevant JComponent 
 * instead of registering it as a KeyEventDispatcher directly. When acting as an 
 * AncestorListener, this will register and remove itself as a KeyEventDispatcher automatically 
 * when the component is added or removed from the Swing hierarchy. This helps ensure that 
 * stale references do not remain in the KeyboardFocusManager, which is global (so out-dated 
 * dispatchers which are not removed present memory leaks.)
 * 
 * @author vwoeltje
 */
public class LocalControlKeyEventDispatcher implements KeyEventDispatcher, AncestorListener {
	private PlotAbstraction abstraction;

	public LocalControlKeyEventDispatcher(PlotAbstraction abstraction) {
		super();
		this.abstraction = abstraction;
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int id = event.getID();
		/* Any PRESSED or RELEASED events should be reported to local controls */
		if (id == KeyEvent.KEY_PRESSED || id == KeyEvent.KEY_RELEASED) {
			boolean pressed = id == KeyEvent.KEY_PRESSED;
			for (AbstractPlottingPackage p : abstraction.getSubPlots()) {
				// Report all key released events, or any event in the plot area
				if (!pressed || !p.getPlotActionListener().isMouseOutsideOfPlotArea()) {
					p.getLocalControlsManager().informKeyState(event.getKeyCode(), pressed);
				}
			}
		}
		return false;
		/*
		 * Note that this is not particularly efficient; all key presses get forwarded to all 
		 * plots, which is potentially a lot of plots. It may make more sense to build a map 
		 * of keycodes -> interested local control managers at the time of instantiation. 
		 * But, this risks bugs if the map becomes out-of-date, and may also require additions 
		 * to related interfaces to allow these key bindings to be inferred or registered. 
		 */
	}



	@Override
	public void ancestorRemoved(AncestorEvent e) {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
	}

	@Override
	public void ancestorAdded(AncestorEvent e) {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);			
	}


	@Override
	public void ancestorMoved(AncestorEvent arg0) {
	}	

}
