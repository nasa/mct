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
package plotter.xy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;

import plotter.internal.GraphicsUtils;
import plotter.xy.XYAxis.TicksChangedListener;


/**
 * Draws a grid on an {@link XYPlot}.
 * Grids draw lines that correspond with major tick marks on axes.
 * @author Adam Crume
 */
public class XYGrid extends JComponent implements TicksChangedListener {
	private static final long serialVersionUID = 1L;

	/** Used to draw the grid lines. */
	private Stroke stroke = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);

	/** X axis this grid takes its values from. */
	private XYAxis xAxis;

	/** Y axis this grid takes its value from. */
	private XYAxis yAxis;

	/** Period length of the stroke, used to optimize drawing. */
	private double strokeLength = 8;

	/** True if optimized drawing is enabled. */
	private boolean customLines = true;

	/** Length of the dash.  Only relevant if {@link #customLines} is true. */
	private double customDash = 4;

	/** Length of the space.  Only relevant if {@link #customLines} is true. */
	private double customSpace = 4;


	/**
	 * Creates a grid.
	 * @param xAxis X axis this grid takes tick marks from
	 * @param yAxis Y axis this grid takes tick marks from
	 */
	public XYGrid(XYAxis xAxis, XYAxis yAxis) {
		assert xAxis != null;
		assert yAxis != null;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		xAxis.addTickListener(this);
		yAxis.addTickListener(this);
	}


	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Color origColor = g2.getColor();
		Stroke origStroke = g2.getStroke();
		g2.setColor(getForeground());
		if(!customLines) {
			g2.setStroke(stroke);
		}

		int height = getHeight();
		int width = getWidth();
		Rectangle clip = g2.getClipBounds();
		int xmin = clip.x;
		int xmax = clip.x + clip.width - 1;
		int ymin = clip.y;
		int ymax = clip.y + clip.height - 1;

		// Draw shorter lines when possible.
		// This makes drawing dashed lines faster.
		int linexmin = 0;
		int linexmax = width - 1;
		int lineymin = 0;
		int lineymax = height - 1;
		if(strokeLength != 0) {
			// We have to adjust the starting points so that the dashes are in phase
			// with what they would have been if we drew the whole line.
			linexmin = (int) (strokeLength * (int) (xmin / strokeLength));
			lineymin = (int) (strokeLength * (int) (ymin / strokeLength));
			linexmax = xmax + 1;
			lineymax = ymax + 1;
		}

		for(int x : xAxis.getMajorTicks()) {
			if(x >= xmin && x <= xmax) {
				if(customLines) {
					GraphicsUtils.drawDashedLine(g2, x, lineymin, x, lineymax, customDash, customSpace);
				} else {
					g2.drawLine(x, lineymin, x, lineymax);
				}
			}
		}
		for(int y : yAxis.getMajorTicks()) {
			y = height - 1 - y;
			if(y >= ymin && y <= ymax) {
				if(customLines) {
					GraphicsUtils.drawDashedLine(g2, linexmin, y, linexmax, y, customDash, customSpace);
				} else {
					g2.drawLine(linexmin, y, linexmax, y);
				}
			}
		}

		g2.setColor(origColor);
		if(!customLines) {
			g2.setStroke(origStroke);
		}
	}


	@Override
	public void ticksChanged(XYAxis a) {
		repaint();
	}


	/**
	 * Returns the stroke used to draw the lines.
	 * @return the stroke used to draw the lines
	 */
	public Stroke getStroke() {
		return stroke;
	}


	/**
	 * Sets the stroke used to draw the lines.
	 * Note that optimized drawing is enabled if the stroke is null,
	 * or else a {@link BasicStroke} with width 1, end {@link BasicStroke#CAP_BUTT}, phase 0, and a dash array of length 2. 
	 * @param stroke the stroke used to draw the lines
	 */
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
		if(stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke) stroke;
			float[] dashes = bs.getDashArray();
			float strokeLength = 0;
			for(float f : dashes) {
				strokeLength += f;
			}
			this.strokeLength = strokeLength;
			customLines = dashes.length == 2 && bs.getDashPhase() == 0 && bs.getEndCap() == BasicStroke.CAP_BUTT
					&& bs.getLineWidth() == 1;
			customDash = dashes[0];
			customSpace = dashes[1];
		} else {
			this.strokeLength = 0;
			customLines = false;
		}
	}
}
