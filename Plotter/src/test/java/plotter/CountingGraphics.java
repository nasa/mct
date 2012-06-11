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
/**
 * 
 */
package plotter;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


/**
 * Stores painting operations so they can be tested.
 * @author Adam Crume
 */
public class CountingGraphics extends GraphicsProxy {
	private final CountingGraphics root;
	private List<Line2D> lines = new ArrayList<Line2D>();
	private int points;
	private int simpleLines;
	private int polyLines;
	private int shapes;


	/**
	 * Creates a counting graphics context.
	 * @param base graphics context to forward painting operations to
	 */
	public CountingGraphics(Graphics2D base) {
		super(base);
		this.root = this;
	}


	private CountingGraphics(Graphics2D base, CountingGraphics root) {
		super(base);
		this.root = root;
	}


	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		super.drawPolyline(xPoints, yPoints, nPoints);
		root.points += nPoints;
		root.polyLines++;
		AffineTransform transform = base.getTransform();
		for(int i = 1; i < nPoints; i++) {
			Point2D p1 = transform.transform(new Point2D.Double(xPoints[i - 1], yPoints[i - 1]), null);
			Point2D p2 = transform.transform(new Point2D.Double(xPoints[i], yPoints[i]), null);
			root.lines.add(new Line2D.Double(p1, p2));
		}
	}


	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		super.drawLine(x1, y1, x2, y2);
		root.points += 2;
		root.simpleLines++;
		AffineTransform transform = base.getTransform();
		Point2D p1 = transform.transform(new Point2D.Double(x1, y1), null);
		Point2D p2 = transform.transform(new Point2D.Double(x2, y2), null);
		root.lines.add(new Line2D.Double(p1, p2));
	}


	@Override
	public void draw(Shape s) {
		super.draw(s);
		root.shapes++;
		PathIterator pi = s.getPathIterator(base.getTransform(), 0.5);
		float[] points = new float[6];
		Point2D p1 = null;
		Point2D cp = null;
		while(!pi.isDone()) {
			switch(pi.currentSegment(points)) {
			case PathIterator.SEG_MOVETO: {
				root.points++;
				double x1 = Math.floor(points[0]);
				double y1 = Math.floor(points[1]);
				p1 = new Point2D.Double(x1, y1);
				cp = p1;
				break;
			}
			case PathIterator.SEG_LINETO: {
				root.points++;
				double x2 = Math.floor(points[0]);
				double y2 = Math.floor(points[1]);
				Point2D p2 = new Point2D.Double(x2, y2);
				root.lines.add(new Line2D.Double(p1, p2));
				p1 = p2;
				break;
			}
			case PathIterator.SEG_CLOSE: {
				root.points++;
				root.lines.add(new Line2D.Double(p1, cp));
				p1 = cp;
				break;
			}
			}
			pi.next();
		}
	}


	@Override
	public Graphics create() {
		return new CountingGraphics((Graphics2D) base.create(), root);
	}


	@Override
	public Graphics create(int x, int y, int width, int height) {
		return new CountingGraphics((Graphics2D) base.create(x, y, width, height), root);
	}


	/**
	 * Returns the line segments that were drawn.
	 * This includes calls to {@link #drawLine(int, int, int, int)} and {@link #drawPolyline(int[], int[], int)}.
	 * With polylines, each individual line segment is added separately.
	 * @return the line segments that were drawn
	 */
	public List<Line2D> getLines() {
		return lines;
	}


	/**
	 * Returns the total point count.
	 * This is increased by 2 for each call to {@link #drawLine(int, int, int, int)}, and by <code>n</code> for each call to {@link #drawPolyline(int[], int[], int)},
	 * where <code>n</code> is the last parameter.
	 * @return number of points
	 */
	public int getPointCount() {
		return points;
	}


	/**
	 * Returns the number of calls to {@link #draw(Shape)}.
	 * @return number of {@link Shape}s drawn
	 */
	public int getShapeCount() {
		return shapes;
	}
}
