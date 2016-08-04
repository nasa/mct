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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import plotter.internal.GraphicsUtils;


/**
 * Draws a line at a particular value on an {@link XYPlot}.
 * @author Adam Crume
 */
public class XYMarkerLine extends JComponent {
	private static final long serialVersionUID = 1L;

	/** If the line moves by fewer than this many pixels, only one repaint is issued.
	 * Otherwise, two separate repaints (to erase the old and draw the new) are issued. */
	private static final int REPAINT_COMBINE_THRESHOLD = 5;

	/** Default stroke used to draw lines. */
	private static final Stroke DEFAULT_STROKE = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);
	
	/** Used to draw the line. */
	private Stroke stroke = DEFAULT_STROKE;

	/** Axis this line corresponds to. */
	private XYAxis axis;

	/** Value this line is drawn at. */
	private double value;

	/** Period length of the stroke, used to optimize drawing. */
	private double strokeLength = 8;

	/** True if optimized drawing is enabled. */
	private boolean customLines = true;

	/** Length of the dash.  Only relevant if {@link #customLines} is true. */
	private double customDash = 4;

	/** Length of the space.  Only relevant if {@link #customLines} is true. */
	private double customSpace = 4;


	/**
	 * Creates a marker line.
	 * @param axis axis the value lies on
	 * @param value value to draw the line at
	 */
	public XYMarkerLine(XYAxis axis, double value) {
		this.axis = axis;
		this.value = value;
	}


	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(getForeground());
		if(!customLines) {
			g2.setStroke(stroke);
		}
		int height = getHeight();
		Rectangle clip = g2.getClipBounds();
		int loc = axis.toPhysical(value);
		if(axis.getPlotDimension() == XYDimension.X) {
			Point p = SwingUtilities.convertPoint(axis, loc, 0, this);
			loc = p.x;
			int xmin = clip.x;
			int xmax = clip.x + clip.width;
			if(loc >= xmin && loc <= xmax) {
				// Draw shorter lines when possible.
				// This makes drawing dashed lines faster.
				int lineymin = 0;
				int lineymax = height;
				if(strokeLength != 0) {
					// We have to adjust the starting points so that the dashes are in phase
					// with what they would have been if we drew the whole line.
					lineymin = (int) (strokeLength * (int) (clip.y / strokeLength));
					lineymax = clip.y + clip.height;
				}

				if(customLines) {
					GraphicsUtils.drawDashedLine(g2, loc, lineymin, loc, lineymax, customDash, customSpace);
				} else {
					g2.drawLine(loc, lineymin, loc, lineymax);
				}
			}
		} else {
			Point p = SwingUtilities.convertPoint(axis, 0, loc, this);
			loc = p.y;
			int width = getWidth();
			int ymin = clip.y;
			int ymax = clip.y + clip.height;
			if(loc >= ymin && loc <= ymax) {
				// Draw shorter lines when possible.
				// This makes drawing dashed lines faster.
				int linexmin = 0;
				int linexmax = width;
				if(strokeLength != 0) {
					// We have to adjust the starting points so that the dashes are in phase
					// with what they would have been if we drew the whole line.
					linexmin = (int) (strokeLength * (int) (clip.x / strokeLength));
					linexmax = clip.x + clip.width;
				}

				if(customLines) {
					GraphicsUtils.drawDashedLine(g2, linexmin, loc, linexmax, loc, customDash, customSpace);
				} else {
					g2.drawLine(linexmin, loc, linexmax, loc);
				}
			}
		}
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


	/**
	 * Returns the value this line is drawn at.
	 * @return the value this line is drawn at
	 */
	public double getValue() {
		return value;
	}


	/**
	 * Sets the value this line is drawn at.
	 * @param value the value this line is drawn at
	 */
	public void setValue(double value) {
		double oldValue = this.value;
		if(value != oldValue) {
			this.value = value;

			int loc = axis.toPhysical(oldValue);
			int loc2 = axis.toPhysical(value);
			if(loc == loc2) {
				// Old and new values do not match exactly, but they paint the same.
				return;
			}

			int minLoc = Math.min(loc, loc2);
			int maxLoc = Math.max(loc, loc2);
			int span = maxLoc - minLoc + 1;
			// If the old and new values are close together, just issue one paint request.
			// Otherwise, issue two smaller paint requests.
			if(span < REPAINT_COMBINE_THRESHOLD) {
				if(axis.getPlotDimension() == XYDimension.X) {
					Point p = SwingUtilities.convertPoint(axis, minLoc, 0, this);
					repaint(p.x, 0, span, getHeight());
				} else {
					Point p = SwingUtilities.convertPoint(axis, 0, minLoc, this);
					repaint(0, p.y, getWidth(), span);
				}
			} else {
				if(axis.getPlotDimension() == XYDimension.X) {
					int xo = SwingUtilities.convertPoint(axis, 0, 0, this).x;
					int height = getHeight();
					repaint(loc + xo, 0, 1, height);
					repaint(loc2 + xo, 0, 1, height);
				} else {
					int yo = SwingUtilities.convertPoint(axis, 0, 0, this).y;
					int width = getWidth();
					repaint(0, loc + yo, width, 1);
					repaint(0, loc2 + yo, width, 1);
				}
			}
		}
	}
}
