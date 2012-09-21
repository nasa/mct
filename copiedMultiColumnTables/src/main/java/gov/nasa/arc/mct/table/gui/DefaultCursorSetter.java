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
package gov.nasa.arc.mct.table.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Implements a mouse handler that saves and restores the cursor
 * when the mouse enters and leaves a component. While over the
 * component, the mouse is set to the default cursor.
 */
public class DefaultCursorSetter extends MouseAdapter {
	private Component component;
	private Cursor oldCursor;

	/**
	 * Creates a new instance of the cursor setter mouse handler.
	 * 
	 * @param component the component for which the handler will manager the cursor
	 */
	public DefaultCursorSetter(Component component) {
		this.component = component;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		oldCursor = component.getCursor();
		component.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		component.setCursor(oldCursor);
	}

}
