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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Implements a builder pattern for creating layout constraints for
 * the {@link java.awt.GridBagLayout} layout manager.
 */
public class ConstraintBuilder {
	
	/** The default amount of horizontal padding to add to cells. */
	public static final int DEFAULT_HPAD = 10;
	
	private Container container;
	private GridBagConstraints defaultConstraints;
	private GridBagConstraints constraints;

	/**
	 * Creates a new builder with default constraints.
	 * 
	 * @param container the container
	 */
	public ConstraintBuilder(Container container) {
		this.container = container;
		container.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		defaultConstraints = (GridBagConstraints) constraints.clone();
	}
	
	/**
	 * Resets all constraints to the default values, and set up for creating
	 * a single grid cell.
	 * 
	 * @return the builder
	 */
	public final ConstraintBuilder reset() {
		// Preserve the current location and set up for a single cell.
		defaultConstraints.gridx = constraints.gridx;
		defaultConstraints.gridy = constraints.gridy;
		defaultConstraints.gridheight = 1;
		defaultConstraints.gridwidth = 1;
		defaultConstraints.weightx = 0.0;
		defaultConstraints.weighty = 0.0;
		
		constraints = (GridBagConstraints) defaultConstraints.clone();

		return this;
	}
	
	/**
	 * Sets the current constraints to be the defaults.
	 * 
	 * @return the builder
	 */
	public final ConstraintBuilder makeDefault() {
		defaultConstraints = (GridBagConstraints) constraints.clone();
		return this;
	}
	
	/**
	 * Sets the grid position of a component.
	 * 
	 * @param row the row position for the component
	 * @param column the column position for the component
	 * @return the builder
	 */
	public ConstraintBuilder at(int row, int column) {
		constraints.gridy = row;
		constraints.gridx = column;
		return this;
	}
	
	/**
	 * Sets the grid span of a component.
	 * 
	 * @param nRows the number of rows the component should span
	 * @param nColumns the number of columns the component should span
	 * @return the builder
	 */
	public ConstraintBuilder span(int nRows, int nColumns) {
		constraints.gridheight = nRows;
		constraints.gridwidth = nColumns;
		return this;
	}
	
	/**
	 * Sets the inset widths for the cell containing the component to add.
	 * 
	 * @param top padding above of the component
	 * @param left padding to the left of the component
	 * @param bottom padding below of the component
	 * @param right padding to the right of the component
	 * @return the builder
	 */
	public ConstraintBuilder insets(int top, int left, int bottom, int right) {
		constraints.insets = new Insets(top, left, bottom, right);
		return this;
	}
	
	/**
	 * Sets the horizontal padding for the component.
	 * 
	 * @param padding the amount of padding to add
	 * @return the builder
	 */
	public ConstraintBuilder hpad(int padding) {
		constraints.ipadx = padding;
		return this;
	}
	
	/**
	 * Sets the vertical padding for the component.
	 * 
	 * @param padding the amount of padding to add
	 * @return the builder
	 */
	public ConstraintBuilder vpad(int padding) {
		constraints.ipady = padding;
		return this;
	}
	
	/**
	 * Sets the anchor position so that the component will be left-aligned
	 * in its cell, with its baseline aligned with the baselines of
	 * other components in the same row.
	 *  
	 * @return the builder
	 */
	public ConstraintBuilder baseline_w() {
		constraints.anchor = GridBagConstraints.BASELINE_LEADING;
		return this;
	}
	
	/**
	 * Sets the anchor position so that the component will be centered
	 * in its cell, with its baseline aligned with the baselines of
	 * other components in the same row.
	 *  
	 * @return the builder
	 */
	public ConstraintBuilder baseline_centered() {
		constraints.anchor = GridBagConstraints.BASELINE;
		return this;
	}
	
	/**
	 * Sets the anchor position so that the component will be right-aligned
	 * in its cell, with its baseline aligned with the baselines of
	 * other components in the same row.
	 *  
	 * @return the builder
	 */
	public ConstraintBuilder baseline_e() {
		constraints.anchor = GridBagConstraints.BASELINE_TRAILING;
		return this;
	}
	
	/**
	 * Sets the anchor position so that the component will be in the
	 * northwest corner of its cell.
	 *  
	 * @return the builder
	 */
	public ConstraintBuilder nw() {
		constraints.anchor = GridBagConstraints.NORTHWEST;
		return this;
	}
	
	/**
	 * Sets the anchor position so that the component will be in
	 * the southwest corner of its cell.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder sw() {
		constraints.anchor = GridBagConstraints.SOUTHWEST;
		return this;
	}
	
	/**
	 * Sets the anchor position so that the component will be centered
	 * vertically within its cell, at the west edge.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder w() {
		constraints.anchor = GridBagConstraints.WEST;
		return this;
	}
	
	/**
	 * Sets the constraints so that the component will be sized to
	 * fill its cell horizontally, if there is extra horizontal room
	 * in the cell.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder hfill() {
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		return this;
	}
	
	/**
	 * Sets the constraints so that the component will be sized to
	 * fill its cell vertically, if there is extra vertical room
	 * in the cell.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder vfill() {
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.weighty = 1.0;
		return this;
	}
	
	/**
	 * Sets the constraints so that the component will be sized to
	 * fill its cell both horizontally and vertically, if there is
	 * extra room in the cell.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder hvfill() {
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		return this;
	}
	
	/**
	 * Sets the stretch weights so the component will take some of the
	 * extra space allowed for the container, if any. If the weights
	 * are set to 0.0, then the component's cell will not expand.
	 * 
	 * @param weightx the horizontal stretch weight
	 * @param weighty the vertical stretch weight
	 * @return the builder
	 */
	public ConstraintBuilder weight(float weightx, float weighty) {
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		return this;
	}
	
	/**
	 * Gets a clone of the current constraints.
	 * 
	 * @return a copy of the current constraints
	 */
	GridBagConstraints getConstraints() {
		return (GridBagConstraints) constraints.clone();
	}
	
	/**
	 * Finishes constraint building and adds a component to the container
	 * using the resulting constraints. Also resets the span to 1x1 and
	 * the fill to none for the next constraints constructed.
	 * 
	 * @param component the component to add
	 */
	public void add(Component component) {
		container.add(component, constraints);
		reset();
		++constraints.gridx;
	}
	
	/**
	 * Moves to the next row down, in the first column.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder nextRow() {
		++constraints.gridy;
		constraints.gridx = 0;
		return this;
	}
	
	/**
	 * Moves to the next column in the grid.
	 * 
	 * @return the builder
	 */
	public ConstraintBuilder nextColumn() {
		++constraints.gridx;
		return this;
	}
	
	/**
	 * Finishes constraint building and adds a sequence of components to the
	 * container using the resulting constraints. Also resets the span to 1x1 and
	 * the fill to none for the next constraints constructed. The sequence of
	 * components is laid out in a subpanel using a flow layout to get left-to-right
	 * rendering
	 * 
	 * @param components the component to add
	 */
	public void add(Component... components) {
		JPanel panel = new JPanel();
		ConstraintBuilder subBuilder = new ConstraintBuilder(panel);
		subBuilder.constraints.anchor = constraints.anchor;
		
		for (Component component : components) {
			subBuilder.add(component);
		}
		
		add(panel);
	}
	
	/**
	 * Gets a rigid, zero-height component of a desired width, to aid spacing
	 * apart adjacent components.
	 * 
	 * @param width the width of the new component
	 * @return the rigid spacing component
	 */
	public static Component hbox(int width) {
		return hbox(width,width,width);
	}
	
	/**
	 * Returns a stretchable, zero-height component of desired minimum, preferred, and maximum
	 * width, to aid in spacing apart adjacent components. If all three widths are equal, then
	 * the spacer will be of rigid width.
	 * 
	 * @param minWidth the minimum width of the spacer component
	 * @param preferredWidth the preferred width of the spacer component
	 * @param maxWidth the maximum width of the spacer comopnent
	 * @return the stretchable spacer component
	 */
	public static Component hbox(int minWidth, int preferredWidth, int maxWidth) {
		JComponent c = new Box.Filler(new Dimension(minWidth,0), new Dimension(preferredWidth,0), new Dimension(maxWidth,0));
		c.setOpaque(false);
		return c;
	}
	
}
