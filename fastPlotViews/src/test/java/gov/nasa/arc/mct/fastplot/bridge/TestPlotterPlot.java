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
package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JButton;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import plotter.xy.CompressingXYDataset;
import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

public class TestPlotterPlot {
	private PlotterPlot plot;
	private XYPlot plotView;
	
	@BeforeMethod
	public void setup() throws Exception {
		plot = new PlotterPlot();
		PlotAbstraction abstraction = Mockito.mock(PlotAbstraction.class);
		setMemberVariable("plotAbstraction", abstraction);
		plot.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		plot.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		plotView = Mockito.mock(XYPlot.class);
		plot.setPlotView(plotView);
		plot.setTimeAxisFont((new JButton()).getFont());
		plot.setPlotLineDraw(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
		XYPlotContents contents = new XYPlotContents();
		Mockito.when(plotView.getContents()).thenReturn(contents);
		Mockito.when(abstraction.getPlotLineDraw()).thenReturn(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
	}
	
	private void setMemberVariable(String name, Object value) throws Exception {
		Field f = plot.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(plot, value);
	}
	
	@DataProvider(name="minMax")
	protected Object[][] minMaxTestCases() {
		return new Object[][] {
				new Object[] {AxisOrientationSetting.X_AXIS_AS_TIME, new TimeXYAxis(XYDimension.X)},
				new Object[] {AxisOrientationSetting.Y_AXIS_AS_TIME, new TimeXYAxis(XYDimension.Y)}
		};
	}
	
	private Object invokeCalculateMinNonTimeWithPadding(double newMin, double min, double max) throws Exception {
		Method m = plot.getClass().getDeclaredMethod("calculateMinNonTimeWithPadding", new Class[] {Double.TYPE, Double.TYPE, Double.TYPE});
		m.setAccessible(true);
		return m.invoke(plot, new Object[] {newMin,max,min});
	}
	
	private Object invokeCalculateMaxNonTimeWithPadding(double newMax, double min, double max) throws Exception {
		Method m = plot.getClass().getDeclaredMethod("calculateMaxNonTimeWithPadding",  new Class[] {Double.TYPE, Double.TYPE, Double.TYPE});
		m.setAccessible(true);
		return m.invoke(plot, new Object[]{newMax, min, max});
	}
	
	
	private void addToDataSet(AxisOrientationSetting setting, CompressingXYDataset dataSet, double nonTime, double time) {
		if (setting == AxisOrientationSetting.X_AXIS_AS_TIME) {
			dataSet.add(time, nonTime);
		} else {
			dataSet.add(nonTime, time);
		}
	}
	
	@DataProvider(name="minWithPadding")
	Object[][] generateMinWithPaddingTests() {
		return new Object[][] {
				new Object[] {0,-11,-10,10, -11},
				new Object[] {.2, -11, -10, 0, -13.2},
				new Object[] {0.2, -13, -10, 0, -15.6},
				new Object[] {0.2, -117946.80000000002,-1.0,-1.0,-141535.960000000024}
		};
	}
	
	@DataProvider(name="maxWithPadding")
	Object[][] generateMaxWithPaddingTests() {
		return new Object[][] {
				new Object[] {0,11,0,10, 11},
				new Object[] {.2,11,0,10,13.2},
				new Object[] {.2,13,0,10,15.6},
				new Object[] {.2,117946.80000000002,1.0,1.0,141535.960000000024}
		};
	}
	
	
	@Test(dataProvider="minWithPadding")
	public void testMinWithPadding(double padding, double newMin, double min, double max, double expected) throws Exception {
		plot.setNonTimeMinPadding(padding);
		double minWithPadding = (Double) invokeCalculateMinNonTimeWithPadding(newMin, min, max);
		Assert.assertTrue(minWithPadding == expected || Math.nextAfter(minWithPadding, Double.NEGATIVE_INFINITY) == expected);
	}
	
	@Test(dataProvider="maxWithPadding") 
	public void testMaxWithPadding(double padding, double newMax, double min, double max, double expected) throws Exception {
		plot.setNonTimeMaxPadding(padding);
		double maxWithPadding = (Double) invokeCalculateMaxNonTimeWithPadding(newMax, min, max);
		Assert.assertTrue(maxWithPadding == expected || Math.nextUp(maxWithPadding) == expected);
	}
	
	@DataProvider(name="startStop")
	Object[][] getTests() {
		long start = System.currentTimeMillis();
		long stop = start+100;
		return new Object[][] {
			new Object[] {start,stop,start},
			new Object[] {stop,start,start}
		};
	}
	
	@Test(dataProvider="startStop")
	public void testSetTimeAxisStartStop(long start, long stop, long expected) throws Exception {
		Method m = plot.getClass().getDeclaredMethod("setupListeners", new Class[0]);
		m.setAccessible(true);
		m.invoke(plot);
		Field axisField = plot.getClass().getDeclaredField("theTimeAxis");
		axisField.setAccessible(true);
		TimeXYAxis axis = new TimeXYAxis(XYDimension.X);
		axisField.set(plot,axis);
		axis.setStart(start - 10000);
		axis.setEnd(stop - 10000);
		
		Field f = plot.getClass().getDeclaredField("plotDataManager");
		f.setAccessible(true);
		PlotDataManager dataManager =  (PlotDataManager) f.get(plot);
		dataManager.addDataSet("test", Color.black);
		PlotDataSeries pds = dataManager.getDataSeries().values().iterator().next();
		CompressingXYDataset data = pds.getData();
		plot.setTimeAxisStartAndStop(start, stop);
		Assert.assertEquals(axis.getStartAsLong(), start);
		Assert.assertEquals(axis.getEndAsLong(), stop);
		Assert.assertEquals(Double.valueOf(expected),data.getTruncationPoint());
	}
	
	@Test(dataProvider="minMax")
	public void testMinMax(AxisOrientationSetting setting, TimeXYAxis axis) throws Exception {
		plot.setAxisOrientationSetting(setting);
		LinearXYAxis nonTime;
		if (setting == AxisOrientationSetting.X_AXIS_AS_TIME) {
			Mockito.when(plotView.getXAxis()).thenReturn(axis);
			nonTime = new LinearXYAxis(XYDimension.Y);
			Mockito.when(plotView.getYAxis()).thenReturn(nonTime);
		} else {
			Mockito.when(plotView.getYAxis()).thenReturn(axis);
			nonTime = new LinearXYAxis(XYDimension.X);
			Mockito.when(plotView.getXAxis()).thenReturn(nonTime);
		}
		plot.theNonTimeAxis = nonTime;
		Assert.assertNotNull(plot.getPlotView().getXAxis());
		Assert.assertNotNull(plot.getPlotView().getYAxis());
		plot.setTimeAxis(axis);
		plot.setNonTimeMinPadding(0);
		plot.setNonTimeMaxPadding(0);
		axis.setStart(100);
		axis.setEnd(200);
		double initialStart = 0;
		double initialEnd = 10;
		nonTime.setStart(initialStart);
		nonTime.setEnd(initialEnd);
		
		PlotDataManager dataManager = new PlotDataManager(plot);
		setMemberVariable("plotDataManager", dataManager);
		dataManager.addDataSet("TestData", Color.black);
		PlotDataSeries dataSeries = dataManager.getDataSeries().get("TestData");
		CompressingXYDataset dataSet = dataSeries.getData();
		dataSet.setCompressionScale(1);
		addToDataSet(setting, dataSet, 0, 100);
		addToDataSet(setting, dataSet, 5, 101);
		plot.newPointPlotted(100,0);
		plot.newPointPlotted(101,5);
	
		// test the case where the new point is less than the current data maximum, no change should occur
		addToDataSet(setting, dataSet, 4, 101.5);
		plot.newPointPlotted(new Double(101.5).longValue(), 4);
		Assert.assertEquals(nonTime.getStart(),initialStart);
		Assert.assertEquals(nonTime.getEnd(), 5.0);
		
		// test the case where the new point is less than the current axis, no change should occur
		// 2/26/12 Change in axis now occurs because non-Time axis responds to new max 6 in auto-adjust and semi-fixed mode.
		addToDataSet(setting, dataSet, 6, 102);
		plot.newPointPlotted(102, 6);
		Assert.assertEquals(nonTime.getStart(),initialStart);
		Assert.assertEquals(nonTime.getEnd(), 6.0);
		
		// test the case where the new point is greater than the current axis, the axis should increase (or decrease) 
		// this should include the padding
		double newMax = 15;
		addToDataSet(setting, dataSet, newMax, 103);
		plot.newPointPlotted(103, newMax);
		Assert.assertEquals(nonTime.getStart(),initialStart);
		Assert.assertEquals(nonTime.getEnd(), newMax);
		
		// test the case where the axis has now been collapsed back to the settings value 
		// if a new value comes in that is below the max value but above the current state of the axis, the axis should 
		// increase to the maximum value
		nonTime.setEnd(initialEnd);
		double twelve = 12;
		addToDataSet(setting, dataSet, twelve, 104);
		plot.newPointPlotted(104, twelve);
		Assert.assertEquals(nonTime.getStart(),initialStart);
		Assert.assertEquals(nonTime.getEnd(), newMax);
		
		// test where the max value has been truncated off the time line
		// The axis should contract down to 12.0, because newMax has been truncated off
		addToDataSet(setting, dataSet, 10, 105);
		plot.newPointPlotted(105, 10);
		addToDataSet(setting, dataSet, 10, 106);
		plot.newPointPlotted(106, 10);
		addToDataSet(setting, dataSet, 10, 107);
		plot.newPointPlotted(107, 10);
		dataSet.setTruncationOffset(0);
		dataSet.setTruncationPoint(104);
		addToDataSet(setting, dataSet, 10, 108);
		plot.newPointPlotted(108, 10);

		Assert.assertEquals(nonTime.getStart(),initialStart);
		Assert.assertEquals(nonTime.getEnd(), 12.0);
	}
	
	@Test
	public void testFastForward() {
		PlotAbstraction mockAbstraction = plot.getPlotAbstraction();
		
		Mockito.when(mockAbstraction.getMinTime()).thenReturn(-2000L);
		Mockito.when(mockAbstraction.getMaxTime()).thenReturn(-1000L);
		Mockito.when(mockAbstraction.getCurrentMCTTime()).thenReturn(500L);
		
		plot.setTimeAxis(new TimeXYAxis(XYDimension.X));
		
		plot.getTimeAxis().setStart(0L);
		plot.getTimeAxis().setEnd(100L);
		Assert.assertEquals(plot.getMinTime(),   0L);
		Assert.assertEquals(plot.getMaxTime(), 100L);
		plot.fastForwardTimeAxisToCurrentMCTTime(true);
		
		Assert.assertEquals(plot.getMinTime(), -500L);
		Assert.assertEquals(plot.getMaxTime(), 500L);		
	}
	
	
}
