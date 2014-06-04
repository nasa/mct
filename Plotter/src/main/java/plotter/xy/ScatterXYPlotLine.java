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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import plotter.DoubleData;
import plotter.DoubleDataDouble;


/**
 * Plots linear XY data.
 * @author Adam Crume
 */
public class ScatterXYPlotLine extends XYPlotLine implements XYDataset {
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_LINES_PER_BOUNDING_BOX = 25;

	/** The X data. */
	private DoubleData xData = new DoubleDataDouble();

	/** The Y data. */
	private DoubleData yData = new DoubleDataDouble();

	/** The X axis, used to retrieve the min and max. */
	private XYAxis xAxis;

	/** The Y axis, used to retrieve the min and max. */
	private XYAxis yAxis;

	/** Number of line segments per bounding box. */
	private int linesPerBoundingBox;

	/** Number of line segments missing from the beginning of the first bounding box. */
	private int boundingBoxOffset;

	/** Bounding boxes for consecutive groups of line segments. */
	private List<Rectangle2D> boundingBoxes;


	/**
	 * Creates a plot line.
	 * @param xAxis the X axis
	 * @param yAxis the Y axis
	 */
	public ScatterXYPlotLine(XYAxis xAxis, XYAxis yAxis) {
		this(xAxis, yAxis, DEFAULT_LINES_PER_BOUNDING_BOX);
	}


	/**
	 * Creates a plot line.
	 * @param xAxis the X axis
	 * @param yAxis the Y axis
	 * @param linesPerBoundingBox lines per bounding box, a tuning parameter for clipping
	 */
	public ScatterXYPlotLine(XYAxis xAxis, XYAxis yAxis, int linesPerBoundingBox) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.linesPerBoundingBox = linesPerBoundingBox;
	}


	private boolean invariants() {
		assert 0 <= boundingBoxOffset && boundingBoxOffset < linesPerBoundingBox : "boundingBoxOffset = " + boundingBoxOffset
				+ ", linesPerBoundingBox = " + linesPerBoundingBox;
		if(boundingBoxes != null) {
			int expectedSize = (xData.getLength() + boundingBoxOffset + linesPerBoundingBox - 1) / linesPerBoundingBox;
			assert boundingBoxes.size() == expectedSize : "Expected boundingBoxes to be of size " + expectedSize
					+ ", but was " + boundingBoxes.size();
			if(!boundingBoxes.isEmpty()) {
				assert firstIndex(0) == 0 : "firstIndex(0) = " + firstIndex(0);
			}
			if(boundingBoxes.size() > 0) {
				assert firstIndex(1) == linesPerBoundingBox - boundingBoxOffset : "firstIndex(1) = " + firstIndex(1)
						+ ", linesPerBoundingBox = " + linesPerBoundingBox;
			}
		}
		return true;
	}


	@Override
	protected void paintComponent(Graphics g) {
		computeBoundingBoxes();
		assert invariants();
		int n = xData.getLength();
		Graphics2D g2 = (Graphics2D) g;
		final double xstart = xAxis.getStart();
		final double xend = xAxis.getEnd();
		final double ystart = yAxis.getStart();
		final double yend = yAxis.getEnd();
		final int width = getWidth();
		final int height = getHeight();
		g2.setColor(getForeground());
		if(stroke != null) {
			g2.setStroke(stroke);
		}

		// Convert the clipping rectangle to logical coordinates
		Rectangle clipBounds = g2.getClipBounds();
		double xscale = width / (xend - xstart);
		double yscale = height / (yend - ystart);
		double xmin = xstart + (clipBounds.getMinX() - 1) / xscale;
		double xmax = xstart + (clipBounds.getMaxX() + 1) / xscale;
		double ymin = ystart + (height - clipBounds.getMinY() + 1) / yscale;
		double ymax = ystart + (height - clipBounds.getMaxY() - 1) / yscale;
		if(xmin > xmax) {
			double tmp = xmin;
			xmin = xmax;
			xmax = tmp;
		}
		if(ymin > ymax) {
			double tmp = ymin;
			ymin = ymax;
			ymax = tmp;
		}
		Rectangle2D clipLogical = new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);

		// Only paint lines in bounding boxes that intersect the logical clipping rectangle
		int boxCount = boundingBoxes.size();
		for(int k = 0; k < boxCount; k++) {
			Rectangle2D box = boundingBoxes.get(k);
			if(box != null && box.intersects(clipLogical)) {
				int index = firstIndex(k);
				int endIndex = Math.min(n, index + linesPerBoundingBox + 1);

				int i = index;
				int[] pointsx = new int[(n - i) * 2];
				int[] pointsy = new int[pointsx.length];

				// Loop through all the points to draw.
				outer: while(i < endIndex - 1) {
					// Find the first non-NaN point.
					while(Double.isNaN(xData.get(i)) || Double.isNaN(yData.get(i))) {
						i++;
						if(i == n) {
							break outer;
						}
					}
					int x = (int) ((xData.get(i) - xstart) * xscale + .5) - 1;
					int y = height - (int) ((yData.get(i) - ystart) * yscale + .5);
					int points = 0;
					pointsx[points] = x;
					pointsy[points] = y;
					points++;
					i++;
					// Add points until we come to the end or a NaN point.
					while(i < endIndex) {
						double xd = xData.get(i);
						double yd = yData.get(i);
						if(Double.isNaN(xd) || Double.isNaN(yd)) {
							i++;
							break;
						}
						int x2 = (int) ((xd - xstart) * xscale + .5) - 1;
						int y2 = height - (int) ((yd - ystart) * yscale + .5);

						pointsx[points] = x2;
						pointsy[points] = y2;
						points++;

						x = x2;
						y = y2;
						i++;
					}
					if(points > 1) {
						g2.drawPolyline(pointsx, pointsy, points);
					}
				}
			}
		}

		if(pointFill != null || pointOutline != null) {
			int oldx = 0;
			int oldy = 0;
			for(int k = 0; k < boxCount; k++) {
				Rectangle2D box = boundingBoxes.get(k);
				if(box == null || !box.intersects(clipLogical)) {
					continue;
				}
				int index = firstIndex(k);
				int endIndex = Math.min(n, index + linesPerBoundingBox + 1);
				for(int i = index; i < endIndex; i++) {
					double xx = xData.get(i);
					double yy = yData.get(i);
					if(Double.isNaN(xx) || Double.isNaN(yy)) {
						continue;
					}
					int x = (int) ((xx - xstart) * xscale + .5) - 1;
					int y = height - (int) ((yy - ystart) * yscale + .5);
					g2.translate(x - oldx, y - oldy);
					if(pointFill != null) {
						g2.fill(pointFill);
					}
					if(pointOutline != null) {
						g2.draw(pointOutline);
					}
					oldx = x;
					oldy = y;
				}
			}
		}

		assert invariants();
	}


	/**
	 * Returns the index of the first point in this bounding box.
	 * @param boundingBoxIndex bounding box index
	 * @return index of the first point in this bounding box
	 */
	private int firstIndex(int boundingBoxIndex) {
		return Math.max(0, boundingBoxIndex * linesPerBoundingBox - boundingBoxOffset);
	}


	private int toAxisX(int x) {
		// Assumption: plot line is contained in an XYPlotContents, which is contained in an XYPlot.  xAxis is contained in the XYPlot.
		return x + getParent().getX() - xAxis.getX();
	}


	private int toAxisY(int y) {
		// Assumption: plot line is contained in an XYPlotContents, which is contained in an XYPlot.  yAxis is contained in the XYPlot.
		return y + getParent().getY() - yAxis.getY();
	}


	/**
	 * Repaints a data point and adjoining line segments.
	 * @param index index of the data point
	 */
	@Override
	public void repaintData(int index) {
		// Implementation note:
		// We don't call repaintData(int,int) with a count of 1
		// because this method gets called a lot (from SimpleXYDataset.add, for example)
		// so we want it to be fast.

		// Calculate the bounding box of the line segment(s) that need to be repainted
		double x = xData.get(index);
		double y = yData.get(index);
		XYAxis xAxis = getXAxis();
		XYAxis yAxis = getYAxis();
		int xmin = xAxis.toPhysical(x);
		int xmax = xmin;
		int ymin = yAxis.toPhysical(y);
		int ymax = ymin;

		// Take care of the previous point
		if(index > 0) {
			int x2p = xAxis.toPhysical(xData.get(index - 1));
			if(x2p > xmax) {
				xmax = x2p;
			} else if(x2p < xmin) {
				xmin = x2p;
			}
			int y2p = yAxis.toPhysical(yData.get(index - 1));
			if(y2p > ymax) {
				ymax = y2p;
			} else if(y2p < ymin) {
				ymin = y2p;
			}
		}

		// Take care of the next point
		if(index < xData.getLength() - 1) {
			int x2p = xAxis.toPhysical(xData.get(index + 1));
			if(x2p > xmax) {
				xmax = x2p;
			} else if(x2p < xmin) {
				xmin = x2p;
			}
			int y2p = yAxis.toPhysical(yData.get(index + 1));
			if(y2p > ymax) {
				ymax = y2p;
			} else if(y2p < ymin) {
				ymin = y2p;
			}
		}

		// Adjust for offsets.
		int xo = toAxisX(0);
		int yo = toAxisY(0);
		xmin -= xo;
		xmax -= xo;
		ymin -= yo;
		ymax -= yo;

		// Add a fudge factor, just to be sure.
		int fudge = 1;
		xmin -= fudge;
		xmax += fudge;
		ymin -= fudge;
		ymax += fudge;

		repaint(xmin, ymin, xmax - xmin, ymax - ymin);
	}


	/**
	 * Repaints data points and adjoining line segments.
	 * @param index index of the first data point
	 * @param count number of data points
	 */
	@Override
	public void repaintData(int index, int count) {
		if(count == 0) {
			return;
		}

		// Calculate the bounding box of the line segment(s) that need to be repainted
		XYAxis xAxis = getXAxis();
		XYAxis yAxis = getYAxis();
		double xmin = Double.POSITIVE_INFINITY;
		double xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY;
		double ymax = Double.NEGATIVE_INFINITY;

		// Take care of the interior points
		for(int i = 0; i < count; i++) {
			double x = xData.get(index + i);
			double y = yData.get(index + i);
			if(x > xmax) {
				xmax = x;
			}
			if(x < xmin) {
				xmin = x;
			}
			if(y > ymax) {
				ymax = y;
			}
			if(y < ymin) {
				ymin = y;
			}
		}

		// Take care of the previous point
		if(index > 0) {
			double x = xData.get(index - 1);
			if(x > xmax) {
				xmax = x;
			}
			if(x < xmin) {
				xmin = x;
			}
			double y = yData.get(index - 1);
			if(y > ymax) {
				ymax = y;
			}
			if(y < ymin) {
				ymin = y;
			}
		}

		// Take care of the next point
		if(index + count < xData.getLength()) {
			double x = xData.get(index + count);
			if(x > xmax) {
				xmax = x;
			}
			if(x < xmin) {
				xmin = x;
			}
			double y = yData.get(index + count);
			if(y > ymax) {
				ymax = y;
			}
			if(y < ymin) {
				ymin = y;
			}
		}

		int xmin2 = xAxis.toPhysical(xmin);
		int xmax2 = xAxis.toPhysical(xmax);
		int ymin2 = yAxis.toPhysical(ymin);
		int ymax2 = yAxis.toPhysical(ymax);
		if(xmin2 > xmax2) {
			int tmp = xmin2;
			xmin2 = xmax2;
			xmax2 = tmp;
		}
		if(ymin2 > ymax2) {
			int tmp = ymin2;
			ymin2 = ymax2;
			ymax2 = tmp;
		}

		// Adjust for offsets.
		int xo = toAxisX(0);
		int yo = toAxisY(0);
		xmin2 -= xo;
		xmax2 -= xo;
		ymin2 -= yo;
		ymax2 -= yo;

		// Add a fudge factor, just to be sure.
		int fudge = 1;
		xmin2 -= fudge;
		xmax2 += fudge;
		ymin2 -= fudge;
		ymax2 += fudge;

		repaint(xmin2, ymin2, xmax2 - xmin2, ymax2 - ymin2);
	}


	/**
	 * Returns the X data.
	 * @return the X data
	 */
	@Override
	public DoubleData getXData() {
		return xData;
	}


	/**
	 * Sets the X data.
	 * @param xData the X data
	 */
	public void setXData(DoubleData xData) {
		this.xData = xData;
		boundingBoxes = null;
	}


	/**
	 * Returns the Y data.
	 * @return the Y data
	 */
	@Override
	public DoubleData getYData() {
		return yData;
	}


	/**
	 * Sets the Y data.
	 * @param yData the Y data
	 */
	public void setYData(DoubleData yData) {
		this.yData = yData;
		boundingBoxes = null;
	}


	/**
	 * Returns the X axis.
	 * @return the X axis
	 */
	public XYAxis getXAxis() {
		return xAxis;
	}


	/**
	 * Returns the Y axis.
	 * @return the Y axis
	 */
	public XYAxis getYAxis() {
		return yAxis;
	}


	/**
	 * Returns null.
	 * @return null
	 */
	@Override
	public XYDimension getIndependentDimension() {
		return null;
	}


	@Override
	public void prepend(double[] x, int xoff, double[] y, int yoff, int len) {
		computeBoundingBoxes();
		assert invariants();
		for(int i = len - 1; i >= 0; i--) {
			double xx = x[xoff + i];
			double yy = y[yoff + i];
			boundingBoxOffset--;
			if(boundingBoxOffset == -1) {
				boundingBoxOffset = linesPerBoundingBox - 1;
				boundingBoxes.add(0, null);
			}
			if(!Double.isNaN(xx) && !Double.isNaN(yy)) {
				Rectangle2D box = boundingBoxes.get(0);
				if(box == null) {
					box = new Rectangle2D.Double(xx, yy, 0, 0);
					boundingBoxes.set(0, box);
				} else {
					box.add(xx, yy);
				}
			}
			if(xData.getLength() > 0) {
				double xxx = xData.get(0);
				double yyy = yData.get(0);
				if(!Double.isNaN(xxx) && !Double.isNaN(yyy)) {
					Rectangle2D box = boundingBoxes.get(0);
					if(box == null) {
						box = new Rectangle2D.Double(xxx, yyy, 0, 0);
						boundingBoxes.set(0, box);
					} else {
						box.add(xxx, yyy);
					}
				}
			}
		}
		xData.prepend(x, xoff, len);
		yData.prepend(y, yoff, len);
		repaintData(0, len);
		assert invariants();
	}


	@Override
	public void prepend(DoubleData x, DoubleData y) {
		computeBoundingBoxes();
		assert invariants();
		double firstx = Double.NaN;
		double firsty = Double.NaN;
		if(xData.getLength() > 0) {
			firstx = xData.get(0);
			firsty = yData.get(0);
		}
		int len = x.getLength();
		for(int i = len - 1; i >= 0; i--) {
			double xx = x.get(i);
			double yy = y.get(i);
			boundingBoxOffset--;
			if(boundingBoxOffset == -1) {
				boundingBoxOffset = linesPerBoundingBox - 1;
				boundingBoxes.add(0, null);
			}
			if(!Double.isNaN(xx) && !Double.isNaN(yy)) {
				Rectangle2D box = boundingBoxes.get(0);
				if(box == null) {
					box = new Rectangle2D.Double(xx, yy, 0, 0);
					boundingBoxes.set(0, box);
				} else {
					box.add(xx, yy);
				}
			}
			if(!Double.isNaN(firstx) && !Double.isNaN(firsty)) {
				Rectangle2D box = boundingBoxes.get(0);
				if(box == null) {
					box = new Rectangle2D.Double(firstx, firsty, 0, 0);
					boundingBoxes.set(0, box);
				} else {
					box.add(firstx, firsty);
				}
			}
			firstx = xx;
			firsty = yy;
		}
		xData.prepend(x, 0, len);
		yData.prepend(y, 0, len);
		repaintData(0, len);
		assert invariants();
	}


	@Override
	public int getPointCount() {
		return xData.getLength();
	}


	@Override
	public void removeAllPoints() {
		assert invariants();
		xData.removeAll();
		yData.removeAll();
		if(boundingBoxes != null) {
			boundingBoxes.clear();
		}
		boundingBoxOffset = 0;
		repaint();
		assert invariants();
	}


	private void computeBoundingBoxes() {
		if(boundingBoxes != null) {
		    return;
		}
		boundingBoxes = new ArrayList<Rectangle2D>();
		boundingBoxOffset = 0;
		int numPoints = xData.getLength();
		int numBoxes = 0;
		int lastBoxSize = 0;
		for(int i = 0; i < numPoints; i++) {
			double x = xData.get(i);
			double y = yData.get(i);
			if(lastBoxSize == 0) {
				if(numBoxes > 0 && !Double.isNaN(x) && !Double.isNaN(y)) {
					Rectangle2D box = boundingBoxes.get(numBoxes - 1);
					if(box == null) {
						box = new Rectangle2D.Double(x, y, 0, 0);
						boundingBoxes.set(numBoxes - 1, box);
					} else {
						box.add(x, y);
					}
				}
				boundingBoxes.add(null);
				numBoxes++;
			}
			if(!Double.isNaN(x) && !Double.isNaN(y)) {
				Rectangle2D box = boundingBoxes.get(numBoxes - 1);
				if(box == null) {
					box = new Rectangle2D.Double(x, y, 0, 0);
					boundingBoxes.set(numBoxes - 1, box);
				} else {
					box.add(x, y);
				}
			}
			lastBoxSize++;
			if(lastBoxSize == linesPerBoundingBox) {
				lastBoxSize = 0;
			}
		}
	}


	@Override
	public void add(double x, double y) {
		computeBoundingBoxes();
		assert invariants();
		int nPoints = xData.getLength();
		int lastBoxSize = nPoints + boundingBoxOffset;
		while(lastBoxSize >= linesPerBoundingBox) {
			lastBoxSize -= linesPerBoundingBox;
		}
		int n = boundingBoxes.size();
		if(lastBoxSize == 0) {
			if(n > 0 && !Double.isNaN(x) && !Double.isNaN(y)) {
				Rectangle2D box = boundingBoxes.get(n - 1);
				if(box == null) {
					box = new Rectangle2D.Double(x, y, 0, 0);
					boundingBoxes.set(n - 1, box);
				} else {
					box.add(x, y);
				}
			}
			boundingBoxes.add(null);
			n++;
		}
		if(!Double.isNaN(x) && !Double.isNaN(y)) {
			Rectangle2D box = boundingBoxes.get(n - 1);
			if(box == null) {
				box = new Rectangle2D.Double(x, y, 0, 0);
				boundingBoxes.set(n - 1, box);
			} else {
				box.add(x, y);
			}
		}
		xData.add(x);
		yData.add(y);
		repaintData(nPoints);
		assert invariants();
	}


	@Override
	public void removeFirst(int removeCount) {
		computeBoundingBoxes();
		assert invariants();
		repaintData(0, removeCount);
		boundingBoxOffset += removeCount;
		while(boundingBoxOffset >= linesPerBoundingBox) {
			boundingBoxes.remove(0);
			boundingBoxOffset -= linesPerBoundingBox;
		}
		xData.removeFirst(removeCount);
		yData.removeFirst(removeCount);
		assert invariants();
	}


	@Override
	public void removeLast(int count) {
		computeBoundingBoxes();
		assert invariants();
		int length = xData.getLength();
		repaintData(length - count, count);
		int boxCount = (length - count + boundingBoxOffset + linesPerBoundingBox - 1) / linesPerBoundingBox;
		while(boxCount < boundingBoxes.size()) {
			boundingBoxes.remove(boundingBoxes.size() - 1);
		}
		xData.removeLast(count);
		yData.removeLast(count);
		assert invariants();
	}
}
