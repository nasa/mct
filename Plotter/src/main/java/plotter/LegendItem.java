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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


/**
 * Displays a sample and description of a single plot line.
 * @author Adam Crume
 */
public class LegendItem extends JComponent {
	// Implementation note:
	// We don't use a JLabel for the text and the line sample because the gap between the icon and the text in a JLabel is fixed.
	// If the user wants right-aligned text to the right of the icon, there's no way to make the gap stretchy so that icons for
	// multiple legend entries line up.  (Except for computing what the gap should be and setting it to that fixed value, but it
	// would have to be recomputed whenever the text or font changes, and it's very hackish.)

	private static final long serialVersionUID = 1L;

	private String description;

	private final PlotLine line;

	private LineSample sample;

	private JLabel label;


	/**
	 * Creates a legend entry with the description in the default position.
	 * @param description description of the plot line
	 * @param line plot line being displayed
	 */
	public LegendItem(String description, PlotLine line) {
		this(description, line, null, SwingConstants.LEFT);
	}


	/**
	 * Creates a legend entry.
	 * @param description description of the plot line
	 * @param line plot line being displayed
	 * @param descPos position of the description relative to the line sample, may be null for default
	 * @param textAlignment alignment of the text within the description ({@link SwingConstants#LEFT}, {@link SwingConstants#CENTER}, or {@link SwingConstants#RIGHT}) 
	 */
	public LegendItem(String description, PlotLine line, DescriptionPosition descPos, int textAlignment) {
		this.description = description;
		this.line = line;

		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 2, 0, 2);
		sample = new LineSample(line);
		sample.setPreferredSize(new Dimension(20, 10));

		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(0, 2, 0, 2);
		c2.weightx = 1;
		c2.fill = GridBagConstraints.BOTH;
		label = new JLabel(description);
		label.setForeground(getForeground());
		label.setFont(getFont());
		label.setHorizontalAlignment(textAlignment);

		if(descPos == null) {
			descPos = DescriptionPosition.RIGHT;
		}
		if(descPos == DescriptionPosition.LEFT) {
			c.gridwidth = GridBagConstraints.REMAINDER;
			add(label, c2);
			add(sample, c);
		} else {
			c2.gridwidth = GridBagConstraints.REMAINDER;
			add(sample, c);
			add(label, c2);
		}

		addPropertyChangeListener("foreground", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				label.setForeground((Color) evt.getNewValue());
			}
		});

		addPropertyChangeListener("font", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				label.setFont((Font) evt.getNewValue());
			}
		});
	}


	/**
	 * Returns the description.
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * Sets the description.
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * Returns the line being plotted
	 * @return the plot line
	 */
	public PlotLine getLine() {
		return line;
	}


	/**
	 * Displays a sample of the plot line.
	 */
	private static class LineSample extends JComponent {
		private static final long serialVersionUID = 1L;

		private final PlotLine line;


		public LineSample(PlotLine line) {
			this.line = line;
		}


		@Override
		public void paint(Graphics g) {
			Stroke stroke = line.getStroke();
			Graphics2D g2 = (Graphics2D)g;
			if(stroke != null) {
				g2.setStroke(stroke);
			}
			g.setColor(line.getForeground());
			int x = getWidth();
			int y = getHeight() / 2;
			g.drawLine(0, y, x, y);
			g.translate(x / 2, y);
			Shape pointOutline = line.getPointOutline();
			if(pointOutline != null) {
				 g2.draw(pointOutline);
			}
			Shape pointFill = line.getPointFill();
			if(pointFill != null) {
				 g2.fill(pointFill);
			}
		}
	}


	/**
	 * Position of the description relative to the line sample.
	 */
	public static enum DescriptionPosition {
		/** The description is left of the line sample. */
		LEFT,

		/** The description is right of the line sample. */
		RIGHT
	}
}
