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

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;
import plotter.DoubleData;
import plotter.DoubleDataDouble;
import plotter.PropertyTester;
import plotter.xy.SimpleXYDataset.MinMaxChangeListener;

public class JUnitSimpleXYDataset extends TestCase {
	private static class MinMaxTracker implements MinMaxChangeListener {
		private int count;


		@Override
		public void minMaxChanged(SimpleXYDataset dataset, XYDimension dimension) {
			count++;
		}
	}


	private SimpleXYDataset createDataset(XYDimension independentDimension) {
		LinearXYAxis xAxis = new LinearXYAxis(XYDimension.X);
		LinearXYAxis yAxis = new LinearXYAxis(XYDimension.Y);
		LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, independentDimension);
		SimpleXYDataset dataset = new SimpleXYDataset(line);
		XYPlot plot = new XYPlot();
		XYPlotContents contents = new XYPlotContents();
		plot.add(contents);
		contents.add(line);
		plot.add(xAxis);
		plot.add(yAxis);
		return dataset;
	}


	public void testMinMax() {
		SimpleXYDataset dataset = createDataset(null);
		MinMaxTracker xTracker = new MinMaxTracker();
		MinMaxTracker yTracker = new MinMaxTracker();
		dataset.addXMinMaxChangeListener(xTracker);
		dataset.addYMinMaxChangeListener(yTracker);

		dataset.add(0, 0);
		assertEquals(1, xTracker.count);
		assertEquals(1, yTracker.count);
		assertEquals(0.0, dataset.getMinX());
		assertEquals(0.0, dataset.getMaxX());
		assertEquals(0.0, dataset.getMinY());
		assertEquals(0.0, dataset.getMaxY());

		dataset.add(1, 2);
		assertEquals(2, xTracker.count);
		assertEquals(2, yTracker.count);
		assertEquals(0.0, dataset.getMinX());
		assertEquals(1.0, dataset.getMaxX());
		assertEquals(0.0, dataset.getMinY());
		assertEquals(2.0, dataset.getMaxY());

		dataset.add(2, 1);
		assertEquals(3, xTracker.count);
		assertEquals(2, yTracker.count);
		assertEquals(0.0, dataset.getMinX());
		assertEquals(2.0, dataset.getMaxX());
		assertEquals(0.0, dataset.getMinY());
		assertEquals(2.0, dataset.getMaxY());

		dataset.removeAllXMinMaxChangeListeners();
		dataset.removeAllYMinMaxChangeListeners();
		dataset.add(100, 100);
		assertEquals(3, xTracker.count);
		assertEquals(2, yTracker.count);
	}


	public void testPrepend() {
		SimpleXYDataset dataset = createDataset(null);
		dataset.add(0, 0);
		dataset.add(1, 1);
		dataset.add(2, 2);
		dataset.add(3, 3);
		dataset.add(4, 4);
		assertEquals(5, dataset.getPointCount());

		double[] newx = { 10, 11, 12, 13, 14 };
		double[] newy = { 10, 11, 12, 13, 14 };
		dataset.prepend(newx, 0, newy, 0, 5);

		assertEquals(10, dataset.getPointCount());
		DoubleData x = dataset.getXData();
		DoubleData y = dataset.getYData();
		assertEquals(10, x.getLength());
		assertEquals(10, y.getLength());
		assertEquals(10.0, x.get(0));
		assertEquals(10.0, y.get(0));
		assertEquals(4.0, x.get(9));
		assertEquals(4.0, y.get(9));
		assertEquals(0.0, dataset.getMinX());
		assertEquals(14.0, dataset.getMaxX());
		assertEquals(0.0, dataset.getMinY());
		assertEquals(14.0, dataset.getMaxY());
	}


	public void testTruncate() {
		SimpleXYDataset dataset = createDataset(null);
		dataset.setMaxCapacity(5);
		dataset.add(10, 10);
		dataset.add(1, 1);
		dataset.add(0, 0);
		dataset.add(2, 2);
		dataset.add(3, 3);
		assertEquals(5, dataset.getPointCount());

		dataset.add(5, 5);
		assertEquals(5, dataset.getPointCount());
		DoubleData x = dataset.getXData();
		DoubleData y = dataset.getYData();
		assertEquals(5, x.getLength());
		assertEquals(5, y.getLength());
		assertEquals(1.0, x.get(0));
		assertEquals(1.0, y.get(0));
		assertEquals(5.0, x.get(4));
		assertEquals(5.0, y.get(4));
		assertEquals(0.0, dataset.getMinX());
		assertEquals(5.0, dataset.getMaxX());
		assertEquals(0.0, dataset.getMinY());
		assertEquals(5.0, dataset.getMaxY());
	}


	public void testTruncateSorted() {
		SimpleXYDataset dataset = createDataset(XYDimension.X);
		dataset.setMaxCapacity(5);
		dataset.add(0, 0);
		dataset.add(1, 1);
		dataset.add(2, 2);
		dataset.add(3, 3);
		dataset.add(4, 4);
		assertEquals(5, dataset.getPointCount());

		dataset.add(5, 5);
		assertEquals(5, dataset.getPointCount());
		DoubleData x = dataset.getXData();
		DoubleData y = dataset.getYData();
		assertEquals(5, x.getLength());
		assertEquals(5, y.getLength());
		assertEquals(1.0, x.get(0));
		assertEquals(1.0, y.get(0));
		assertEquals(5.0, x.get(4));
		assertEquals(5.0, y.get(4));
		assertEquals(1.0, dataset.getMinX());
		assertEquals(5.0, dataset.getMaxX());
		assertEquals(1.0, dataset.getMinY());
		assertEquals(5.0, dataset.getMaxY());
	}


	public void testRemoveFirst() {
		SimpleXYDataset dataset = createDataset(XYDimension.X);
		dataset.add(0, 0);
		dataset.add(1, 1);
		dataset.add(2, 2);
		dataset.add(3, 3);
		dataset.add(4, 4);
		dataset.removeFirst(2);
		assertEquals(3, dataset.getPointCount());
		assertEquals(2.0, dataset.getMinX());
		assertEquals(4.0, dataset.getMaxX());
		assertEquals(2.0, dataset.getMinY());
		assertEquals(4.0, dataset.getMaxY());

		dataset.removeFirst(3);
		assertEquals(0, dataset.getPointCount());
		assertEquals(Double.POSITIVE_INFINITY, dataset.getMinX());
		assertEquals(Double.NEGATIVE_INFINITY, dataset.getMaxX());
		assertEquals(Double.POSITIVE_INFINITY, dataset.getMinY());
		assertEquals(Double.NEGATIVE_INFINITY, dataset.getMaxY());
	}


	public void testRemoveAll() {
		SimpleXYDataset dataset = createDataset(null);
		dataset.add(0, 0);
		dataset.add(1, 1);
		dataset.add(2, 2);
		dataset.add(3, 3);
		dataset.add(4, 4);
		dataset.removeAllPoints();
		assertEquals(0, dataset.getPointCount());
		assertEquals(Double.POSITIVE_INFINITY, dataset.getMinX());
		assertEquals(Double.NEGATIVE_INFINITY, dataset.getMaxX());
		assertEquals(Double.POSITIVE_INFINITY, dataset.getMinY());
		assertEquals(Double.NEGATIVE_INFINITY, dataset.getMaxY());
	}


	public void testProperties() throws InvocationTargetException, IllegalAccessException, IntrospectionException {
		SimpleXYDataset dataset = createDataset(null);
		PropertyTester t = new PropertyTester(dataset);
		t.test("maxCapacity", 1, Integer.MAX_VALUE);
		t.test("XData", new DoubleDataDouble(), null);
		t.test("YData", new DoubleDataDouble(), null);
	}
}
