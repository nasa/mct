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
package gov.nasa.arc.mct.table.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * Implements a panel which is optimized for rendering inside a table cell. An
 * instance of this class assumes that it is used inside a <code>JTable</code>.
 * That class reuses the same renderer instance while drawing
 * portions of the table, updating the origin and properties before rendering
 * each new cell. This class optimizes such rendering by disabling certain events, to
 * avoid unnecessary propagation to the current parent of the label.
 * 
 * More details, copied from the Javadoc of <code>DefaultTableCellRenderer</code>:
 * 
 * <strong><a name="override">Implementation Note:</a></strong>
 * This class inherits from <code>JLabel</code>, a standard component class. 
 * However <code>JTable</code> employs a unique mechanism for rendering
 * its cells and therefore requires some slightly modified behavior
 * from its cell renderer.  
 * The table class defines a single cell renderer and uses it as a 
 * as a rubber-stamp for rendering all cells in the table; 
 * it renders the first cell,
 * changes the contents of that cell renderer, 
 * shifts the origin to the new location, re-draws it, and so on.
 * The standard <code>JLabel</code> component was not
 * designed to be used this way and we want to avoid 
 * triggering a <code>revalidate</code> each time the
 * cell is drawn. This would greatly decrease performance because the
 * <code>revalidate</code> message would be
 * passed up the hierarchy of the container to determine whether any other
 * components would be affected.  
 * As the renderer is only parented for the lifetime of a painting operation
 * we similarly want to avoid the overhead associated with walking the
 * hierarchy for painting operations.
 * So this class
 * overrides the <code>validate</code>, <code>invalidate</code>,
 * <code>revalidate</code>, <code>repaint</code>, and
 * <code>firePropertyChange</code> methods to be 
 * no-ops and override the <code>isOpaque</code> method solely to improve
 * performance.  If you write your own renderer,
 * please keep this performance consideration in mind.
 */
@SuppressWarnings("serial")
public class LightweightLabel extends JLabel {
	
	/**
	 * Creates a lightweight label that is initially opaque.
	 */
	public LightweightLabel() {
		super();
		setOpaque(true);
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 * 
	 * For the purposes of rendering as a table cell, a component are "opaque,"
	 * and thus need to draw its background, only if it is marked as opaque, and its
	 * parent is opaque, and if its background color doesn't match its parent.
	 */
	@Override
	public boolean isOpaque() { 
		Color back = getBackground();
		Component p = getParent();
		while (p!=null && !(p instanceof JTable)) {
			p = p.getParent(); 
		}

		// p should now be the JTable. 
		boolean colorMatch =
			(back != null) && (p != null)
			&& back.equals(p.getBackground())
			&& p.isOpaque();
		return !colorMatch && super.isOpaque(); 
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 *
	 * @since 1.5
	 */
	@Override
	public void invalidate() {}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 */
	@Override
	public void validate() {}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 */
	@Override
	public void revalidate() {}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 */
	@Override
	public void repaint(long tm, int x, int y, int width, int height) {}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 */
	@Override
	public void repaint(Rectangle r) {}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 *
	 * @since 1.5
	 */
	@Override
	public void repaint() {}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 */
	@Override
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {	
		// Strings get interned...
		if (propertyName == "text"
			|| propertyName == "labelFor"
			|| propertyName == "displayedMnemonic"
			|| ((propertyName == "font" || propertyName == "foreground")
				&& oldValue != newValue
				&& getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a> 
	 * for more information.
	 */
	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
	
}
