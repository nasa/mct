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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import plotter.CountingGraphics;
import plotter.LineChecker;

public class JUnitXYMarkerLine extends TestCase {
	private TestXYMarkerLine line;
	private XYPlot plot;


	private void setupX() {
		XYAxis xAxis = new LinearXYAxis(XYDimension.X);
		XYAxis yAxis = new LinearXYAxis(XYDimension.Y);
		xAxis.setStart(0);
		xAxis.setEnd(1);
		yAxis.setStart(0);
		yAxis.setEnd(1);
		line = new TestXYMarkerLine(xAxis, .5);
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


	private void setupY() {
		XYAxis xAxis = new LinearXYAxis(XYDimension.X);
		XYAxis yAxis = new LinearXYAxis(XYDimension.Y);
		xAxis.setStart(0);
		xAxis.setEnd(1);
		yAxis.setStart(0);
		yAxis.setEnd(1);
		line = new TestXYMarkerLine(yAxis, .5);
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


	public void testPaintSimple() throws InterruptedException, InvocationTargetException {
		setupX();
		line.setValue(.5);
		CountingGraphics g = paint();
		LineChecker c = new LineChecker();
		for(int y = 0; y < 200; y += 8) {
			c.require(99, y, 99, y + 3);
		}
		c.check(g.getLines());
	}


	public void testPaintSimpleY() throws InterruptedException, InvocationTargetException {
		setupY();
		line.setValue(.5);
		CountingGraphics g = paint();
		LineChecker c = new LineChecker();
		for(int x = 0; x < 200; x += 8) {
			c.require(x, 100, x + 3, 100);
		}
		c.check(g.getLines());
	}


	public void testPaintNonBasicStroke() throws InterruptedException, InvocationTargetException {
		setupX();
		line.setStroke(new Stroke() {
			Stroke s = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);


			@Override
			public Shape createStrokedShape(Shape p) {
				return s.createStrokedShape(p);
			}
		});
		line.setValue(.5);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(99, 0, 99, 200);
		c.check(g.getLines());
	}


	public void testPaintNonBasicStrokeY() throws InterruptedException, InvocationTargetException {
		setupY();
		line.setStroke(new Stroke() {
			Stroke s = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);


			@Override
			public Shape createStrokedShape(Shape p) {
				return s.createStrokedShape(p);
			}
		});
		line.setValue(.5);
		CountingGraphics g = paint();
		assertEquals(2, g.getPointCount());

		LineChecker c = new LineChecker();
		c.require(0, 100, 200, 100);
		c.check(g.getLines());
	}


	public void testPaintClip() throws InterruptedException, InvocationTargetException {
		setupX();
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(150, 0, 50, 200));
		assertEquals(0, g.getPointCount());
	}


	public void testPaintClipY() throws InterruptedException, InvocationTargetException {
		setupY();
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(0, 200, 150, 50));
		assertEquals(0, g.getPointCount());
	}


	public void testPaintClipPartial() throws InterruptedException, InvocationTargetException {
		setupX();
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(0, 49, 200, 100));
		LineChecker c = new LineChecker();
		for(int y = 48; y < 149; y += 8) {
			c.require(99, y, 99, y + 3);
		}
		c.check(g.getLines());
	}


	public void testPaintClipPartialY() throws InterruptedException, InvocationTargetException {
		setupY();
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(49, 0, 100, 200));
		LineChecker c = new LineChecker();
		for(int x = 48; x < 149; x += 8) {
			c.require(x, 100, x + 3, 100);
		}
		c.check(g.getLines());
	}


	public void testPaintClipPartialCustomStroke() throws InterruptedException, InvocationTargetException {
		setupX();
		line.setStroke(new BasicStroke(1, 0, 0, 1, new float[] { 5, 5 }, 0));
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(0, 49, 200, 100));
		LineChecker c = new LineChecker();
		for(int y = 40; y < 149; y += 10) {
			c.require(99, y, 99, y + 4);
		}
		c.check(g.getLines());
	}


	public void testPaintClipPartialCustomStrokeY() throws InterruptedException, InvocationTargetException {
		setupY();
		line.setStroke(new BasicStroke(1, 0, 0, 1, new float[] { 5, 5 }, 0));
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(49, 0, 100, 200));
		LineChecker c = new LineChecker();
		for(int x = 40; x < 149; x += 10) {
			c.require(x, 100, x + 4, 100);
		}
		c.check(g.getLines());
	}


	public void testPaintClipPartialNonBasicStroke() throws InterruptedException, InvocationTargetException {
		setupX();
		line.setStroke(new Stroke() {
			Stroke s = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);


			@Override
			public Shape createStrokedShape(Shape p) {
				return s.createStrokedShape(p);
			}
		});
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(0, 49, 200, 100));
		LineChecker c = new LineChecker();
		c.require(99, 0, 99, 200);
		c.check(g.getLines());
	}


	public void testPaintClipPartialNonBasicStrokeY() throws InterruptedException, InvocationTargetException {
		setupY();
		line.setStroke(new Stroke() {
			Stroke s = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);


			@Override
			public Shape createStrokedShape(Shape p) {
				return s.createStrokedShape(p);
			}
		});
		line.setValue(.5);
		CountingGraphics g = paint(new Rectangle(49, 0, 100, 200));
		LineChecker c = new LineChecker();
		c.require(0, 100, 200, 100);
		c.check(g.getLines());
	}


	public void testRepaintTinyChange() {
		// Same pixel, should not repaint
		setupX();
		line.setSize(100, 100);
		plot.getXAxis().setStartMargin(0);
		plot.getXAxis().setSize(100, 1);
		line.setValue(.5);
		line.repaints.clear();
		line.setValue(.501);
		assertTrue(line.repaints.isEmpty());
	}


	public void testRepaintTinyChangeY() {
		// Same pixel, should not repaint
		setupY();
		line.setSize(100, 100);
		plot.getYAxis().setStartMargin(0);
		plot.getYAxis().setSize(1, 100);
		line.setValue(.5);
		line.repaints.clear();
		line.setValue(.501);
		assertTrue(line.repaints.isEmpty());
	}


	public void testRepaintSmallChange() {
		// Adjacent pixel, should combine into one repaint
		setupX();
		line.setSize(100, 100);
		plot.getXAxis().setStartMargin(0);
		plot.getXAxis().setSize(100, 1);
		line.setValue(.5);
		line.repaints.clear();
		line.setValue(.51);
		assertEquals(1, line.repaints.size());
		assertTrue(line.repaints.contains(new Rectangle(49, 0, 2, 100)));
	}


	public void testRepaintSmallChangeY() {
		// Adjacent pixel, should combine into one repaint
		setupY();
		line.setSize(100, 100);
		plot.getYAxis().setStartMargin(0);
		plot.getYAxis().setSize(1, 100);
		line.setValue(.5);
		line.repaints.clear();
		line.setValue(.51);
		assertEquals(1, line.repaints.size());
		assertTrue(line.repaints.contains(new Rectangle(0, 49, 100, 2)));
	}


	public void testRepaintBigChange() {
		// Far apart pixels, should issue separate repaints
		setupX();
		line.setSize(100, 100);
		plot.getXAxis().setStartMargin(0);
		plot.getXAxis().setSize(100, 1);
		line.setValue(.5);
		line.repaints.clear();
		line.setValue(.9);
		assertEquals(2, line.repaints.size());
		assertTrue(line.repaints.contains(new Rectangle(49, 0, 1, 100)));
		assertTrue(line.repaints.contains(new Rectangle(89, 0, 1, 100)));
	}


	public void testRepaintBigChangeY() {
		// Far apart pixels, should issue separate repaints
		setupY();
		line.setSize(100, 100);
		plot.getYAxis().setStartMargin(0);
		plot.getYAxis().setSize(1, 100);
		line.setValue(.5);
		line.repaints.clear();
		line.setValue(.9);
		assertEquals(2, line.repaints.size());
		assertTrue(line.repaints.contains(new Rectangle(0, 50, 100, 1)));
		assertTrue(line.repaints.contains(new Rectangle(0, 10, 100, 1)));
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


	private static class TestXYMarkerLine extends XYMarkerLine {
		private static final long serialVersionUID = 1L;

		List<Rectangle> repaints = new ArrayList<Rectangle>();


		private TestXYMarkerLine(XYAxis axis, double value) {
			super(axis, value);
		}


		public void repaint(int x, int y, int width, int height) {
			repaints.add(new Rectangle(x, y, width, height));
			super.repaint(x, y, width, height);
		}
	}
}
