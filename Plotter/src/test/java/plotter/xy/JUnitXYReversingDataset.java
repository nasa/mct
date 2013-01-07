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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import plotter.DoubleData;
import plotter.DoubleDataDouble;

public class JUnitXYReversingDataset extends TestCase {
	final List<Point2D> points = new ArrayList<Point2D>();


	public void testAll() {
		DummyXYDataset base = new DummyXYDataset();
		XYReversingDataset d = new XYReversingDataset(base);
		d.add(1, 2);
		assertEquals(1, d.getPointCount());
		assertEquals(1, points.size());
		assertEquals(new Point2D.Double(2, 1), points.get(0));

		DoubleData xData = new DoubleDataDouble();
		DoubleData yData = new DoubleDataDouble();
		xData.add(2);
		yData.add(3);
		xData.add(3);
		yData.add(4);
		d.prepend(xData, yData);
		assertEquals(3, d.getPointCount());
		assertEquals(3, points.size());
		assertEquals(new Point2D.Double(4, 3), points.get(0));
		assertEquals(new Point2D.Double(3, 2), points.get(1));

		d.prepend(new double[] { 4, 5 }, 0, new double[] { 5, 6 }, 0, 2);
		assertEquals(5, d.getPointCount());
		assertEquals(5, points.size());
		assertEquals(new Point2D.Double(6, 5), points.get(0));
		assertEquals(new Point2D.Double(5, 4), points.get(1));

		d.removeLast(1);
		assertEquals(4, d.getPointCount());
		assertEquals(4, points.size());

		d.removeAllPoints();
		assertEquals(0, d.getPointCount());
		assertEquals(0, points.size());
	}


	private class DummyXYDataset implements XYDataset {
		@Override
		public int getPointCount() {
			return points.size();
		}


		@Override
		public void removeAllPoints() {
			points.clear();
		}


		@Override
		public void add(double x, double y) {
			points.add(new Point2D.Double(x, y));
		}


		@Override
		public void removeLast(int count) {
			points.subList(points.size() - count, points.size()).clear();
		}


		@Override
		public void prepend(double[] x, int xoff, double[] y, int yoff, int len) {
			for(int i = 0; i < len; i++) {
				points.add(0, new Point2D.Double(x[xoff + i], y[yoff + i]));
			}
		}


		@Override
		public void prepend(DoubleData x, DoubleData y) {
			int n = x.getLength();
			for(int i = 0; i < n; i++) {
				points.add(0, new Point2D.Double(x.get(i), y.get(i)));
			}
		}
	}
}
