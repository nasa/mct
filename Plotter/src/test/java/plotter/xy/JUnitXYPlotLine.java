package plotter.xy;

import java.awt.geom.GeneralPath;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;
import plotter.DoubleData;
import plotter.PropertyTester;

public class JUnitXYPlotLine extends TestCase {
	private XYPlotLine line;


	protected void setUp() {
		line = new XYPlotLine() {
			@Override
			public void repaintData(int index, int count) {
			}


			@Override
			public void repaintData(int index) {
			}


			@Override
			public void removeLast(int removeCount) {
			}


			@Override
			public void removeFirst(int removeCount) {
			}


			@Override
			public void removeAllPoints() {
			}


			@Override
			public void prepend(double[] x, int xoff, double[] y, int yoff, int len) {
			}


			@Override
			public void prepend(DoubleData x, DoubleData y) {
			}


			@Override
			public DoubleData getYData() {
				return null;
			}


			@Override
			public DoubleData getXData() {
				return null;
			}


			@Override
			public XYDimension getIndependentDimension() {
				return null;
			}


			@Override
			public void add(double x, double y) {
			}
		};
	}


	public void testProperties() throws InvocationTargetException, IllegalAccessException, IntrospectionException {
		PropertyTester t = new PropertyTester(line);
		t.test("pointFill", null, new GeneralPath());
		t.test("pointOutline", null, new GeneralPath());
	}
}
