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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import plotter.Axis;
import plotter.CountingGraphics;
import plotter.LineChecker;
import plotter.TickMarkCalculator;
import junit.framework.TestCase;

public class JUnitXYGrid extends TestCase {
	private XYGrid grid;

	private XYPlot plot;


	@Override
	protected void setUp() throws Exception {
		LinearXYAxis xAxis = new LinearXYAxis(XYDimension.X);
		LinearXYAxis yAxis = new LinearXYAxis(XYDimension.Y);
		xAxis.setStart(0);
		xAxis.setEnd(1);
		yAxis.setStart(0);
		yAxis.setEnd(1);
		xAxis.setPreferredSize(new Dimension(1, 10));
		yAxis.setPreferredSize(new Dimension(10, 1));
		xAxis.setTickMarkCalculator(new TickMarkCalculator() {
			@Override
			public double[][] calculateTickMarks(Axis axis) {
				return new double[][] { { 0, .25, .5, .75, 1 }, {} };
			}
		});
		yAxis.setTickMarkCalculator(new TickMarkCalculator() {
			@Override
			public double[][] calculateTickMarks(Axis axis) {
				return new double[][] { { 0, .25, .5, .75, 1 }, {} };
			}
		});
		grid = new XYGrid(xAxis, yAxis);
		XYPlotContents contents = new XYPlotContents();
		contents.add(grid);
		plot = new XYPlot();
		plot.add(contents);
		plot.add(xAxis);
		plot.add(yAxis);
		plot.setXAxis(xAxis);
		plot.setYAxis(yAxis);
		new DefaultXYLayoutGenerator().generateLayout(plot);
	}


	public void testPaintSimple() throws InterruptedException, InvocationTargetException {
		CountingGraphics g = paint();
		LineChecker c = new LineChecker();

		for(int y = 0; y < 200; y += 8) {
			c.require(199, y, 199, y + 3);
			c.require(149, y, 149, y + 3);
			c.require(99, y, 99, y + 3);
			c.require(49, y, 49, y + 3);
		}

		for(int x = 0; x < 200; x += 8) {
			c.require(x, 0, x + 3, 0);
			c.require(x, 50, x + 3, 50);
			c.require(x, 100, x + 3, 100);
			c.require(x, 150, x + 3, 150);
		}

		c.check(g.getLines());
	}


	public void testPaintNonBasicStroke() throws InterruptedException, InvocationTargetException {
		grid.setStroke(new Stroke() {
			Stroke s = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);


			@Override
			public Shape createStrokedShape(Shape p) {
				return s.createStrokedShape(p);
			}
		});
		CountingGraphics g = paint();
		LineChecker c = new LineChecker();
		c.require(199, 0, 199, 199);
		c.require(149, 0, 149, 199);
		c.require(99, 0, 99, 199);
		c.require(49, 0, 49, 199);
		c.require(0, 150, 199, 150);
		c.require(0, 100, 199, 100);
		c.require(0, 50, 199, 50);
		c.require(0, 0, 199, 0);
		c.check(g.getLines());
	}


	public void testPaintClip() throws InterruptedException, InvocationTargetException {
		CountingGraphics g = paint(new Rectangle(25, 25, 50, 2));
		LineChecker c = new LineChecker();
		c.require(49, 24, 49, 27);
		c.check(g.getLines());
	}


	public void testPaintClipCustomStroke() throws InterruptedException, InvocationTargetException {
		grid.setStroke(new BasicStroke(1, 0, 0, 1, new float[] { 5, 5 }, 0));
		CountingGraphics g = paint(new Rectangle(25, 25, 50, 2));
		LineChecker c = new LineChecker();
		c.require(49, 20, 49, 24);
		c.check(g.getLines());
	}


	public void testPaintClipCustomStroke2() throws InterruptedException, InvocationTargetException {
		grid.setStroke(new BasicStroke(2, 0, 0, 1, new float[] { 5, 5 }, 0));
		CountingGraphics g = paint(new Rectangle(25, 25, 50, 2));
		LineChecker c = new LineChecker();
		c.require(49, 20, 49, 27);
		c.check(g.getLines());
	}


	public void testPaintClipNonBasicStroke() throws InterruptedException, InvocationTargetException {
		grid.setStroke(new Stroke() {
			Stroke s = new BasicStroke(1, 0, 0, 1, new float[] { 4, 4 }, 0);


			@Override
			public Shape createStrokedShape(Shape p) {
				return s.createStrokedShape(p);
			}
		});
		CountingGraphics g = paint(new Rectangle(25, 25, 50, 2));
		LineChecker c = new LineChecker();
		c.require(49, 0, 49, 199);
		c.check(g.getLines());
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
						int xo = 200 - grid.getWidth();
						int yo = 200 - grid.getHeight();
						frame.setSize(frame.getWidth() + xo, frame.getHeight() + yo);
						frame.validate();

						// These are sanity checks to make sure our setup is correct, not actual functionality tests.
						assertEquals(200, grid.getWidth());
						assertEquals(200, grid.getHeight());

						grid.paint(g);
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
