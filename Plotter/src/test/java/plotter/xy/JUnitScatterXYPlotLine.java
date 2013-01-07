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
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import plotter.CountingGraphics;
import plotter.DoubleData;
import plotter.DoubleDataDouble;
import plotter.LineChecker;
import plotter.PropertyTester;

public class JUnitScatterXYPlotLine extends TestCase {
	private ScatterXYPlotLine line;
	private XYPlot plot;
	private List<Rectangle> repaints = new ArrayList<Rectangle>();
	private int linesPerBox = 3;


	@Override
	protected void setUp() throws Exception {
		XYAxis xAxis = new LinearXYAxis(XYDimension.X);
		XYAxis yAxis = new LinearXYAxis(XYDimension.Y);
		xAxis.setStart(0);
		xAxis.setEnd(1);
		yAxis.setStart(0);
		yAxis.setEnd(1);
		line = new ScatterXYPlotLine(xAxis, yAxis, linesPerBox) {
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
		line.add(x, y);
	}


	private void prepend(double x, double y) {
		line.prepend(new double[] { x }, 0, new double[] { y }, 0, 1);
	}


	private void prependDD(double x, double y) {
		DoubleData xData = new DoubleDataDouble();
		DoubleData yData = new DoubleDataDouble();
		xData.add(x);
		yData.add(y);
		line.prepend(xData, yData);
	}


	public void testPaintSimple() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
		assertEquals(2, g.getPointCount());
	}


	public void testPaintInverted() throws InterruptedException, InvocationTargetException {
		plot.getXAxis().setStart(1);
		plot.getXAxis().setEnd(0);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();

		LineChecker c = new LineChecker();
		c.require(179, 180, 19, 20);
		c.check(g.getLines());
		assertEquals(2, g.getPointCount());
	}


	public void testPaintLeadingNaN() throws InterruptedException, InvocationTargetException {
		add(.05, Double.NaN);
		add(.1, .1);
		add(.9, .9);
		CountingGraphics g = paint();

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
		assertEquals(2, g.getPointCount());
	}


	public void testPaintTrailingNaN() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.9, .9);
		add(.95, Double.NaN);
		CountingGraphics g = paint();

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
		assertEquals(2, g.getPointCount());
	}


	public void testPaintMiddleNaN() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.5, Double.NaN);
		add(.8, .8);
		add(.9, .9);
		CountingGraphics g = paint();

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.require(159, 40, 179, 20);
		c.check(g.getLines());
		assertEquals(4, g.getPointCount());
	}


	public void testPaintTrailingTripleNaN() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.9, .9);
		add(.95, Double.NaN);
		add(.96, Double.NaN);
		add(.97, Double.NaN);
		CountingGraphics g = paint();

		LineChecker c = new LineChecker();
		c.require(19, 180, 179, 20);
		c.check(g.getLines());
		assertEquals(2, g.getPointCount());
	}


	public void testAddToEmptyBox() throws InterruptedException, InvocationTargetException {
		add(.1, Double.NaN);
		add(.2, Double.NaN);
		add(.3, Double.NaN);
		add(.4, .4);
		add(.5, .5);
		CountingGraphics g = paint(new Rectangle(84, 116, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.require(79, 120, 99, 100);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPrependToEmptyBox() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, Double.NaN);
		prepend(.5, Double.NaN);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPrependDDToEmptyBox() throws InterruptedException, InvocationTargetException {
		prependDD(.1, .1);
		prependDD(.2, .2);
		prependDD(.3, .3);
		prependDD(.4, Double.NaN);
		prependDD(.5, Double.NaN);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveAllPoints() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.3, .3);
		add(.4, .4);
		add(.5, .5);
		assertEquals(5, line.getPointCount());
		line.removeAllPoints();
		assertEquals(0, line.getPointCount());
		assertEquals(0, line.getXData().getLength());
		assertEquals(0, line.getYData().getLength());
		CountingGraphics g = paint();
		assertEquals(0, g.getPointCount());

		add(.1, .1);
		add(.2, .2);
		CountingGraphics g2 = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g2.getLines());
		assertEquals(g2.getLines().size() + 1, g2.getPointCount());
	}


	public void testPaintClip() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.3, .3);
		add(.4, .4);
		add(.5, .5);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClip2() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.3, .3);
		add(.4, .4);
		add(.5, .5);
		CountingGraphics g = paint(new Rectangle(44, 136, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClip3() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.3, .3);
		add(.4, .4);
		add(.5, .5);
		CountingGraphics g = paint(new Rectangle(64, 116, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.require(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClip4() throws InterruptedException, InvocationTargetException {
		add(.1, .1);
		add(.2, .2);
		add(.3, .3);
		add(.4, .4);
		add(.5, .5);
		CountingGraphics g = paint(new Rectangle(84, 96, 10, 10));

		LineChecker c = new LineChecker();
		c.require(79, 120, 99, 100);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClipInverted() throws InterruptedException, InvocationTargetException {
		plot.getXAxis().setStart(1);
		plot.getXAxis().setEnd(0);
		add(.9, .1);
		add(.8, .2);
		add(.7, .3);
		add(.6, .4);
		add(.5, .5);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClipPrepend() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClipPrepend2() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClipPrepend3() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClipPrepend4() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testPaintClipPrependDD() throws InterruptedException, InvocationTargetException {
		prependDD(.1, .1);
		prependDD(.2, .2);
		prependDD(.3, .3);
		prependDD(.4, .4);
		prependDD(.5, .5);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveFirst() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		line.removeFirst(1);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveFirst2() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		line.removeFirst(1);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveFirst3() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		prepend(.7, .7);
		line.removeFirst(1);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveFirst4() throws InterruptedException, InvocationTargetException {
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		prepend(.7, .7);
		line.removeFirst(1);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveLast() throws InterruptedException, InvocationTargetException {
		prepend(.6, .6);
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		line.removeLast(1);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveLast2() throws InterruptedException, InvocationTargetException {
		prepend(.0, .0);
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		line.removeLast(1);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.allow(79, 120, 99, 100);
		c.check(g.getLines());
		int lineCount = g.getLines().size();
		assertTrue(lineCount <= linesPerBox);
		assertEquals(lineCount + 1, g.getPointCount());
	}


	public void testRemoveLast3() throws InterruptedException, InvocationTargetException {
		prepend(.0, .0);
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		line.removeLast(1);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testRemoveLast4() throws InterruptedException, InvocationTargetException {
		prepend(-.3, -.3);
		prepend(-.2, -.2);
		prepend(-.1, -.1);
		prepend(.0, .0);
		prepend(.1, .1);
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		line.removeLast(4);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.allow(79, 120, 99, 100);
		c.check(g.getLines());
		int lineCount = g.getLines().size();
		assertTrue(lineCount <= linesPerBox);
		assertEquals(lineCount + 1, g.getPointCount());
	}


	public void testComplex() throws InterruptedException, InvocationTargetException {
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		line.removeFirst(1);
		line.add(.1, .1);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testComplex2() throws InterruptedException, InvocationTargetException {
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		line.removeFirst(1);
		line.add(.1, .1);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testComplex3() throws InterruptedException, InvocationTargetException {
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		prepend(.7, .7);
		line.removeFirst(1);
		line.add(.1, .1);
		CountingGraphics g = paint(new Rectangle(24, 176, 10, 10));

		LineChecker c = new LineChecker();
		c.require(19, 180, 39, 160);
		c.allow(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
	}


	public void testComplex4() throws InterruptedException, InvocationTargetException {
		prepend(.2, .2);
		prepend(.3, .3);
		prepend(.4, .4);
		prepend(.5, .5);
		prepend(.6, .6);
		prepend(.7, .7);
		line.removeFirst(1);
		line.add(.1, .1);
		CountingGraphics g = paint(new Rectangle(44, 156, 10, 10));

		LineChecker c = new LineChecker();
		c.allow(19, 180, 39, 160);
		c.require(39, 160, 59, 140);
		c.allow(59, 140, 79, 120);
		c.check(g.getLines());
		assertEquals(g.getLines().size() + 1, g.getPointCount());
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


	public void testRepaint2() {
		add(.1, .2);
		add(.2, .3);
		add(.3, .4);
		add(.4, .5);
		add(.5, .6);
		add(.6, .7);
		line.setSize(200, 200);
		plot.getXAxis().setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(2, 2);
		assertEquals(1, repaints.size());
		assertEquals(new Rectangle(38, 79, 62, 62), repaints.get(0));
	}


	public void testRepaint3() {
		add(.1, .4);
		add(.2, .3);
		add(.3, .2);
		line.setSize(200, 200);
		plot.getXAxis().setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(1);
		assertEquals(1, repaints.size());
		assertEquals(new Rectangle(18, 119, 42, 42), repaints.get(0));
	}


	public void testRepaint4() {
		add(.6, .7);
		add(.5, .6);
		add(.4, .5);
		add(.3, .4);
		add(.2, .3);
		add(.1, .2);
		line.setSize(200, 200);
		plot.getXAxis().setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(2, 2);
		assertEquals(1, repaints.size());
		assertEquals(new Rectangle(38, 79, 62, 62), repaints.get(0));
	}


	public void testRepaint5() {
		add(.1, .2);
		add(.2, .3);
		add(.3, .4);
		add(.4, .5);
		add(.5, .6);
		add(.6, .7);
		line.setSize(200, 200);
		XYAxis xAxis = plot.getXAxis();
		xAxis.setStart(1);
		xAxis.setEnd(0);
		xAxis.setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(2, 2);
		assertEquals(1, repaints.size());
		assertEquals(new Rectangle(98, 79, 62, 62), repaints.get(0));
	}


	public void testRepaintNone() {
		add(.1, .4);
		add(.2, .3);
		add(.3, .2);
		line.setSize(200, 200);
		plot.getXAxis().setSize(200, 1);
		plot.getYAxis().setSize(1, 200);

		repaints.clear();
		line.repaintData(0, 0);
		assertEquals(0, repaints.size());
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


	public void testIndependentDimension() {
		assertNull(line.getIndependentDimension());
	}


	public void testProperties() throws InvocationTargetException, IllegalAccessException, IntrospectionException {
		PropertyTester t = new PropertyTester(line);
		t.test("stroke", null, new BasicStroke(1));
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
