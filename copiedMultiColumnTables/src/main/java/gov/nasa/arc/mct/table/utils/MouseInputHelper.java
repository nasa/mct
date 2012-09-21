/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.table.utils;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import static java.awt.Event.ALT_MASK;
import static java.awt.Event.CTRL_MASK;
import static java.awt.Event.META_MASK;
import static java.awt.Event.SHIFT_MASK;

/**
 * Implements an adapter class for handling mouse input which has
 * separate handler methods for the different kinds of mouse clicks
 * that can occur.
 * 
 * The class extends {@link MouseInputAdapter} and overrides {@link MouseInputAdapter#mouseClicked(MouseEvent)},
 * inspecting the button and modifiers to call separate handler methods
 * for common mouse input cases. The following mouse clicks are handled:
 * 
 * <ul>
 *   <li>left single-click, no modifiers
 *   <li>left double-click, no modifiers
 *   <li>right single-click, no modifiers
 *   <li>left single-click with shift key down
 *   <li>left single-click with meta key down (command-click, on OS X)
 * </ul>
 */
public class MouseInputHelper extends MouseInputAdapter {
	
	private final static int ALL_KEY_MASK = ALT_MASK | CTRL_MASK | META_MASK | SHIFT_MASK;
	private final static int NO_KEYS_DOWN = 0;

	@Override
	public final void mouseClicked(MouseEvent e) {
		int keyState = e.getModifiers() & ALL_KEY_MASK;
		
		if (e.getButton()==MouseEvent.BUTTON1) {
			if (e.getClickCount()==1 && keyState==NO_KEYS_DOWN) {
				mouseLeftClicked(e);
			} else if (e.getClickCount()>1 && keyState == NO_KEYS_DOWN) {
				mouseLeftDoubleClicked(e);
			} else if (e.getClickCount()==1 && keyState==SHIFT_MASK) {
				mouseLeftClickedWithShift(e);
			} else if (e.getClickCount()==1 && keyState==META_MASK) {
				mouseLeftClickedWithMeta(e);
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			if (e.getClickCount()==1 && keyState==NO_KEYS_DOWN) {
				mouseRightClicked(e);
			}
		}
		// else do nothing
	}

	/**
	 * Processes a single click of the left mouse button when no modifier keys
	 * are depressed.
	 * 
	 * @param e the mouse event
	 */
	public void mouseLeftClicked(MouseEvent e) {
		// do nothing
	}
	
	/**
	 * Processes a double click of the left mouse button when no modifier keys
	 * are depressed.
	 * 
	 * @param e the mouse event
	 */
	public void mouseLeftDoubleClicked(MouseEvent e) {
		// do nothing
	}
	
	/**
	 * Processes a single click of the left mouse button when the shift key
	 * only is depressed.
	 * 
	 * @param e the mouse event
	 */
	public void mouseLeftClickedWithShift(MouseEvent e) {
		// no nothing
	}

	/**
	 * Processes a single click of the left mouse button when the meta key
	 * only is depressed.
	 * 
	 * @param e the mouse event
	 */
	public void mouseLeftClickedWithMeta(MouseEvent e) {
		// no nothing
	}

	/**
	 * Processes a single click of the right mouse button when no modifier keys
	 * are depressed.
	 * 
	 * @param e the mouse event
	 */
	public void mouseRightClicked(MouseEvent e) {
		// do nothing
	}
	
}
