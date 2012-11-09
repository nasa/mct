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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import plotter.xy.XYAxis;

/**
 * Suite of tests that make sure Plots with inverted axis stay inverted when scrolling. 
 */
public class TestAxisInversion {
	@Mock
	private PlotViewManifestation mockPlotViewManifestation;


	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());
	}


	@Test
	public void NonTimeInvertedTimeOnX() {
		
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
	}
	
	@Test
	public void NonTimeInvertedTimeOnY() {
		
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		time.setTimeInMillis(0);
		endTime.setTimeInMillis(0);
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
	}
	
	
	@Test
	public void TimeInvertedTimeOnX() {	
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
	}
	
	@Test
	public void TimeInvertedTimeOnY() {	
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
	}
	
	
	@Test
	public void TimeAndNonTimeInvertedTimeOnX() {	
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
	}
	
	@Test
	public void TimeAndNonTimeInvertedTimeOnY() {	
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);

		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertTrue(yAxis.getStart() > yAxis.getEnd());
		Assert.assertTrue(xAxis.getStart() > xAxis.getEnd());
	}
	
	@Test
	public void NothingInverstedTimeOnX() {
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
	}
	
	@Test
	public void NothingInverstedTimeOnY() {
		GregorianCalendar time = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		endTime.add(Calendar.MINUTE, 10);
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(endTime.getTimeInMillis());
		settings.setMinTime(time.getTimeInMillis());
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		testPlot.addDataSet("DataSet1");
		testPlot.addData("DataSet1", time.getTimeInMillis(), 1);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		time.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 11);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
		
		
		time.add(Calendar.MINUTE, 20);
		testPlot.addData("DataSet1", time.getTimeInMillis(), 50);
		
		Assert.assertFalse(yAxis.getStart() > yAxis.getEnd());
		Assert.assertFalse(xAxis.getStart() > xAxis.getEnd());
	}
	
}
