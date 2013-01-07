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
import java.awt.Stroke;

import plotter.DoubleData;
import plotter.DoubleDataDouble;


/**
 * Plots linear XY data.
 * Assumes that the X data is sorted.
 * @author Adam Crume
 */
public class LinearXYPlotLine extends XYPlotLine implements XYDataset {
	private static final long serialVersionUID = 1L;

	/** The X data. */
	private DoubleData xData = new DoubleDataDouble();

	/** The Y data. */
	private DoubleData yData = new DoubleDataDouble();

	/** The X axis, used to retrieve the min and max. */
	private XYAxis xAxis;

	/** The Y axis, used to retrieve the min and max. */
	private XYAxis yAxis;

	/** The algorithm used to draw the line. */
	private LineMode lineMode = LineMode.STRAIGHT;

	/** The stroke used to draw the line, or null to use the default. */
	private Stroke stroke;

	/** Specifies which lines to draw around a missing/invalid point. */
	private MissingPointMode missingPointMode = MissingPointMode.NONE;

	/** The independent dimension stores data in increasing or decreasing order.  May be null for a scatter plot or parametric plot, although this is not supported yet. */
	private XYDimension independentDimension;


	/**
	 * Creates a plot line.
	 * The independent dimension stores data in increasing or decreasing order.  May be null for a scatter plot or parametric plot, although this is not supported yet.
	 * @param xAxis the X axis
	 * @param yAxis the Y axis
	 * @param independentDimension the independent dimension
	 */
	public LinearXYPlotLine(XYAxis xAxis, XYAxis yAxis, XYDimension independentDimension) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.independentDimension = independentDimension;
	}


	@Override
	protected void paintComponent(Graphics g) {
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

		// Skip the points that are outside our clipping area
		// TODO: Support a stop index, not just a start index
		int index;
		if(independentDimension == XYDimension.X) {
			int clipx;
			if(xstart < xend) {
				clipx = g.getClipBounds().x;
			} else {
				clipx = (int) g.getClipBounds().getMaxX();
			}
			clipx = toAxisX(clipx) - 1;
			double min = xAxis.toLogical(clipx);
			index = xData.binarySearch(min);
			if(index < 0) {
				index = -index - 1;
			}
			index--;
		} else {
			int clipy;
			if(ystart < yend) {
				clipy = (int) g.getClipBounds().getMaxY();
			} else {
				clipy = (int) g.getClipBounds().getMinY();
			}
			clipy = toAxisY(clipy) - 1;
			double min = yAxis.toLogical(clipy);
			index = yData.binarySearch(min);
			if(index < 0) {
				index = -index - 1;
			}
			index--;
		}

		int i = Math.max(0, index);
		double xscale = width / (xend - xstart);
		double yscale = height / (yend - ystart);
		int[] pointsx = new int[(n - i) * 2];
		int[] pointsy = new int[pointsx.length];

		DoubleData dependentData = independentDimension == XYDimension.Y ? xData : yData;
		// Loop through all the points to draw.
		outer: while(i < n - 1) {
			// Find the first non-NaN point.
			while(Double.isNaN(dependentData.get(i))) {
				i++;
				if(i == n) {
					break outer;
				}
			}
			int x = (int) ((xData.get(i) - xstart) * xscale + .5) - 1;
			int y = height - (int) ((yData.get(i) - ystart) * yscale + .5);
			int points = 0;
			if(missingPointMode == MissingPointMode.RIGHT || missingPointMode == MissingPointMode.BOTH) {
				if(i > 0 && i > index) {
					if(independentDimension == XYDimension.X) {
						pointsx[points] = (int) ((xData.get(i - 1) - xstart) * xscale + .5) - 1;
						pointsy[points] = y;
						points++;
					} else if(independentDimension == XYDimension.Y) {
						pointsy[points] = height - (int) ((yData.get(i - 1) - ystart) * yscale + .5);
						pointsx[points] = x;
						points++;
					}
				}
			}
			pointsx[points]=x;
			pointsy[points]=y;
			points++;
			i++;
			// Add points until we come to the end or a NaN point.
			while(i < n) {
				if(Double.isNaN(dependentData.get(i))) {
					if(missingPointMode == MissingPointMode.BOTH || missingPointMode == MissingPointMode.LEFT) {
						if(independentDimension == XYDimension.X) {
							double xd = xData.get(i);
							int x2 = (int) ((xd - xstart) * xscale + .5) - 1;
							pointsx[points] = x2;
							pointsy[points] = y;
							points++;
						} else if(independentDimension == XYDimension.Y) {
							double yd = yData.get(i);
							int y2 = height - (int) ((yd - ystart) * yscale + .5);
							pointsx[points] = x;
							pointsy[points] = y2;
							points++;
						}
					}
					i++;
					break;
				}
				double xd = xData.get(i);
				double yd = yData.get(i);
				int x2 = (int) ((xd - xstart) * xscale + .5) - 1;
				int y2 = height - (int) ((yd - ystart) * yscale + .5);

				if(lineMode == LineMode.STRAIGHT) {
					pointsx[points]=x2;
					pointsy[points]=y2;
					points++;
				} else if(lineMode == LineMode.STEP_XY) {
					pointsx[points]=x2;
					pointsy[points]=y;
					points++;
					pointsx[points]=x2;
					pointsy[points]=y2;
					points++;
				} else {
					pointsx[points]=x;
					pointsy[points]=y2;
					points++;
					pointsx[points]=x2;
					pointsy[points]=y2;
					points++;
				}

				x = x2;
				y = y2;
				i++;
			}
			if(points > 1) {
				g2.drawPolyline(pointsx, pointsy, points);
			}
		}

		if(pointFill != null || pointOutline != null || pointIcon != null) {
			int oldx = 0;
			int oldy = 0;
			for(i = Math.max(0, index); i < n; i++) {
				double xx = xData.get(i);
				double yy = yData.get(i);
				if(!Double.isNaN(xx) && !Double.isNaN(yy)) {
					int x = (int) ((xx - xstart) * xscale + .5) - 1;
					int y = height - (int) ((yy - ystart) * yscale + .5);
					g2.translate(x - oldx, y - oldy);
					if(pointFill != null) {
						g2.fill(pointFill);
					}
					if(pointOutline != null) {
						g2.draw(pointOutline);
					}
					if(pointIcon != null) {
						pointIcon.paintIcon(this, g2, 0, 0);
					}
					oldx = x;
					oldy = y;
				}
			}
		}
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
	 * Returns the line mode used for connecting points.
	 * @return the line mode used for connecting points
	 */
	public LineMode getLineMode() {
		return lineMode;
	}


	/**
	 * Sets the line mode used for connecting points.
	 * @param lineMode the line mode used for connecting points
	 */
	public void setLineMode(LineMode lineMode) {
		this.lineMode = lineMode;
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


	/**
	 * Returns the missing point mode.
	 * @return the missing point mode
	 */
	public MissingPointMode getMissingPointMode() {
		return missingPointMode;
	}


	/**
	 * Sets the missing point mode.
	 * @param missingPointMode the missing point mode
	 */
	public void setMissingPointMode(MissingPointMode missingPointMode) {
		this.missingPointMode = missingPointMode;
	}


	/**
	 * Returns the independent dimension.  May be null.
	 * @return the independent dimension
	 */
	@Override
	public XYDimension getIndependentDimension() {
		return independentDimension;
	}


	@Override
	public void prepend(double[] x, int xoff, double[] y, int yoff, int len) {
		xData.prepend(x, xoff, len);
		yData.prepend(y, yoff, len);
		repaintData(0, len);
	}


	@Override
	public void prepend(DoubleData x, DoubleData y) {
		int len = x.getLength();
		if(len != y.getLength()) {
			throw new IllegalArgumentException("x and y must be the same length.  x.length = " + x.getLength() + ", y.length = " + y.getLength());
		}
		xData.prepend(x, 0, len);
		yData.prepend(y, 0, len);
		repaintData(0, len);
	}


	@Override
	public int getPointCount() {
		return xData.getLength();
	}


	@Override
	public void removeAllPoints() {
		xData.removeAll();
		yData.removeAll();
		repaint();
	}


	@Override
	public void add(double x, double y) {
		xData.add(x);
		yData.add(y);
		repaintData(xData.getLength() - 1);
	}


	@Override
	public void removeFirst(int removeCount) {
		repaintData(0, removeCount);
		xData.removeFirst(removeCount);
		yData.removeFirst(removeCount);
	}


	@Override
	public void removeLast(int count) {
		repaintData(xData.getLength() - count, count);
		xData.removeLast(count);
		yData.removeLast(count);
	}


	/**
	 * A line mode specifies how lines are drawn connecting points.
	 */
	public enum LineMode {
		/**
		 * Straight lines are drawn between points.
		 */
		STRAIGHT,

		/**
		 * A horizontal line is drawn to the new point's X coordinate, then a vertical line is drawn.
		 */
		STEP_XY,

		/**
		 * A vertical line is drawn the the new point's Y coordinate, then a horizontal line is drawn.
		 */
		STEP_YX
	}
}
