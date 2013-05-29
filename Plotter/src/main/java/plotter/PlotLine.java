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
package plotter;


import java.awt.Shape;
import java.awt.Stroke;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * Base class for all plot lines.
 * @author Adam Crume
 */
public abstract class PlotLine extends JComponent {
	private static final long serialVersionUID = 1L;

	/** Shape used to draw the outline of each data point.  May be null. */
	protected Shape pointOutline;

	/** Shape used to fill each data point.  May be null. */
	protected Shape pointFill;

	/** Icon to draw at each data point.  May be null */
	protected Icon pointIcon;

	/** The stroke used to draw the line, or null to use the default. */
	protected Stroke stroke;


	/**
	 * Repaints a data point and adjoining line segments.
	 * @param index index of the data point
	 */
	public abstract void repaintData(int index);


	/**
	 * Repaints data points and adjoining line segments.
	 * @param index index of the first data point
	 * @param count number of data points
	 */
	public abstract void repaintData(int index, int count);


	/**
	 * Removes points from the beginning.
	 * @param removeCount number of points to remove
	 */
	public abstract void removeFirst(int removeCount);


	/**
	 * Removes points from the end.
	 * @param removeCount number of points to remove
	 */
	public abstract void removeLast(int removeCount);


	/**
	 * Removes all points.
	 */
	public abstract void removeAllPoints();


	/**
	 * Returns the point outline.
	 * @return the point outline
	 */
	public Shape getPointOutline() {
		return pointOutline;
	}


	/**
	 * Sets the point outline
	 * @param pointOutline the point outline
	 */
	public void setPointOutline(Shape pointOutline) {
		this.pointOutline = pointOutline;
	}


	/**
	 * Returns the point fill.
	 * @return the point fill
	 */
	public Shape getPointFill() {
		return pointFill;
	}


	/**
	 * Sets the point fill.
	 * @param pointFill the point fill
	 */
	public void setPointFill(Shape pointFill) {
		this.pointFill = pointFill;
	}


	/**
	 * @return the icon used to draw each data point
	 */
	public Icon getPointIcon() {
		return pointIcon;
	}


	/**
	 * @param pointIcon the icon to draw at each data point
	 */
	public void setPointIcon(Icon pointIcon) {
		this.pointIcon = pointIcon;
	}


	/**
	 * Returns the stroke used to draw the line.
	 * @return the stroke used to draw the line
	 */
	public Stroke getStroke() {
		return stroke;
	}


	/**
	 * Sets the stroke used to draw the line.
	 * @param stroke the stroke used to draw the line
	 */
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}
}