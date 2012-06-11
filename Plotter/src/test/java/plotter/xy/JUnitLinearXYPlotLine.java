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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import plotter.CountingGraphics;
import plotter.LineChecker;
import plotter.xy.LinearXYPlotLine.LineMode;

public class JUnitLinearXYPlotLine extends TestCase {
	private LinearXYPlotLine line;
	private XYPlot plot;
	private List<Rectangle> repaints = new ArrayList<Rectangle>();


	@Override
	protected void setUp() throws Exception {
		XYAxis xAxis = new LinearXYAxis(XYDimension.X);
		XYAxis yAxis = new LinearXYAxis(XYDimension.Y);
		xAxis.setStart(0);
		xAxis.setEnd(1);
		yAxis.setStart(0);
		yAxis.setEnd(1);
		line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X) {
			private static final long serialVersionUID = 1L;

			@Override
			public void repaint(int x, int y, int width, int height) {
				repaints.add(new Rectangle(x, y, width, height));
				super.repaint(x, y, width, height);
			}
		};
		XYPlotContents contents = new XYPlotContents();
		contents.add(line);
		plot = new XYPlot();
		plot.add(contents);
		plot.add(xAxis);
		plot.add(yAxis);
		plot.setXAxis(xAxis);
		plot.setYAxis(yAxis);
		new DefaultXYLayoutGenerator().generateLayout(plot);
	}


	private void add(double x, double y) {
		line.getXData().add(x);
		line.getYData().add(y);
	}


	public void testPaintSimple() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintInverted() throws InterruptedException, InvocationTargetException {
		plot.getXAxis().setStart(1);
		plot.getXAxis().setEnd(0);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(179, 180, 19, 20);
		c.check(g.getLines());
	}


	public void testPaintLeadingNaN() throws InterruptedException, InvocationTargetException {
		add(.05, Double.NaN);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintTrailingNaN() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.9, .9);
		add(.95, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMiddleNaN() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(4, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.require(159, 40, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintTrailingTripleNaN() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.9, .9);
		add(.95, Double.NaN);
		add(.96, Double.NaN);
		add(.97, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintSimpleStep() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_YX);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(3, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 19, 20);
		c.require(19, 20, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintLeadingNaNStep() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_YX);
		add(.05, Double.NaN);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(3, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 19, 20);
		c.require(19, 20, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintTrailingNaNStep() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_YX);
		add(.1, .1);
		add(.9, .9);
		add(.95, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(3, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 19, 20);
		c.require(19, 20, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMiddleNaNStep() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_YX);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(6, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 19, 160);
		c.require(19, 160, 39, 160);
		c.require(159, 40, 159, 20);
		c.require(159, 20, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintSimpleStepXY() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_XY);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(3, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 180);
		c.require(179, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintLeadingNaNStepXY() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_XY);
		add(.05, Double.NaN);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(3, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 180);
		c.require(179, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintTrailingNaNStepXY() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_XY);
		add(.1, .1);
		add(.9, .9);
		add(.95, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(3, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 180);
		c.require(179, 180, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMiddleNaNStepXY() throws InterruptedException, InvocationTargetException {
		line.setLineMode(LineMode.STEP_XY);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(6, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 180);
		c.require(39, 180, 39, 160);
		c.require(159, 40, 179, 40);
		c.require(179, 40, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintClip() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.5, .5);
		add(.9, .9);
		CountingGraphics g = paint(new Rectangle(150, 0, 50, 200));
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(99, 100, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintClipInverted() throws InterruptedException, InvocationTargetException {
		plot.getXAxis().setStart(1);
		plot.getXAxis().setEnd(0);
		add(.1, .1);
		add(.5, .5);
		add(.9, .9);
		CountingGraphics g = paint(new Rectangle(0, 0, 50, 200));
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(99, 100, 19, 20);
		c.check(g.getLines());
	}


	public void testPaintMissingPointLeft() throws InterruptedException, InvocationTargetException {
		line.setMissingPointMode(MissingPointMode.LEFT);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(5, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.require(39, 160, 99, 160);
		c.require(159, 40, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMissingPointRight() throws InterruptedException, InvocationTargetException {
		line.setMissingPointMode(MissingPointMode.RIGHT);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(5, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.require(99, 40, 159, 40);
		c.require(159, 40, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMissingPointBoth() throws InterruptedException, InvocationTargetException {
		line.setMissingPointMode(MissingPointMode.BOTH);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(6, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.require(39, 160, 99, 160);
		c.require(99, 40, 159, 40);
		c.require(159, 40, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMissingPointLeftWithMissingEnds() throws InterruptedException, InvocationTargetException {
		line.setMissingPointMode(MissingPointMode.LEFT);
		add(0, Double.NaN);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		add(1, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(6, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.require(39, 160, 99, 160);
		c.require(159, 40, 179, 20);
		c.require(179, 20, 199, 20);
		c.check(g.getLines());
	}


	public void testPaintMissingPointRightWithMissingEnds() throws InterruptedException, InvocationTargetException {
		line.setMissingPointMode(MissingPointMode.RIGHT);
		add(0, Double.NaN);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		add(1, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(6, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(-1, 180, 19, 180);
		c.require(19, 180, 39, 160);
		c.require(99, 40, 159, 40);
		c.require(159, 40, 179, 20);
		c.check(g.getLines());
	}


	public void testPaintMissingPointBothWithMissingEnds() throws InterruptedException, InvocationTargetException {
		line.setMissingPointMode(MissingPointMode.BOTH);
		add(0, Double.NaN);
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		add(1, Double.NaN);
		CountingGraphics g = paint();
		assertEquals(8, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(-1, 180, 19, 180);
		c.require(19, 180, 39, 160);
		c.require(39, 160, 99, 160);
		c.require(99, 40, 159, 40);
		c.require(159, 40, 179, 20);
		c.require(179, 20, 199, 20);
		c.check(g.getLines());
	}


	public void testRepaint() {
		add(.1, .2);
		add(.2, .3);
		add(.3, .4);
		line.setSize(200, 200);
		plot.getXAxis().setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(1);
		assertEquals(1, repaints.size());
		assertEquals(new Rectangle(18, 119, 42, 42), repaints.get(0));
	}


	public void testRepaintInverted() {
		add(.1, .2);
		add(.2, .3);
		add(.3, .4);
		line.setSize(200, 200);
		plot.getXAxis().setStart(1);
		plot.getXAxis().setEnd(0);
		plot.getXAxis().setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(1);
		assertEquals(1, repaints.size());
		assertEquals(new Rectangle(138, 119, 42, 42), repaints.get(0));
	}


	public void testPointOutline() throws InterruptedException, InvocationTargetException {
		line.setPointOutline(createSquare());
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(12, g.getPointCount());
		assertEquals(2, g.getShapeCount());

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);

		// Point 1
		c.require(14, 175, 14, 185);
		c.require(14, 185, 24, 185);
		c.require(24, 185, 24, 175);
		c.require(24, 175, 14, 175);

		// Point 2
		c.require(174, 15, 174, 25);
		c.require(174, 25, 184, 25);
		c.require(184, 25, 184, 15);
		c.require(184, 15, 174, 15);

		c.check(g.getLines());
	}


	public void testPointOutlineNaN() throws InterruptedException, InvocationTargetException {
		line.setPointOutline(createSquare());
		add(.1, .1);
		add(.5, Double.NaN);
		add(.9, .9);
		CountingGraphics g = paint();
		assertEquals(10, g.getPointCount());
		assertEquals(2, g.getShapeCount());

		LineChecker c = new LineChecker();

		// Point 1
		c.require(14, 175, 14, 185);
		c.require(14, 185, 24, 185);
		c.require(24, 185, 24, 175);
		c.require(24, 175, 14, 175);

		// Point 3
		c.require(174, 15, 174, 25);
		c.require(174, 25, 184, 25);
		c.require(184, 25, 184, 15);
		c.require(184, 15, 174, 15);

		c.check(g.getLines());
	}


	private GeneralPath createSquare() {
		GeneralPath pointShape = new GeneralPath();
		pointShape.moveTo(-5, -5);
		pointShape.lineTo(-5, 5);
		pointShape.lineTo(5, 5);
		pointShape.lineTo(5, -5);
		pointShape.lineTo(-5, -5);
		return pointShape;
	}


	private CountingGraphics paint() throws InterruptedException, InvocationTargetException {
		return paint(null);
	}


	// Ensures that the line is 200x200, paints it, and returns the stats.
	// We can't use 100x100 because on Mac OSX, the minimum frame width is 128.
	private CountingGraphics paint(Shape clip) throws InterruptedException, InvocationTargetException {
		final JFrame frame = new JFrame();
		frame.getContentPane().add(plot);
		BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_3BYTE_BGR);
		final Graphics2D imageG = image.createGraphics();
		final CountingGraphics g = new CountingGraphics(imageG);
		try {
			if(clip != null) {
				g.setClip(clip);
			}
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						frame.pack();
						int xo = 200 - line.getWidth();
						int yo = 200 - line.getHeight();
						frame.setSize(frame.getWidth() + xo, frame.getHeight() + yo);
						frame.validate();

						// These are sanity checks to make sure our setup is correct, not actual functionality tests.
						assertEquals(200, line.getWidth());
						assertEquals(200, line.getHeight());

						line.paint(g);
					} finally {
						frame.dispose();
					}
				}
			});
		} finally {
			imageG.dispose();
		}
		return g;
	}
}
