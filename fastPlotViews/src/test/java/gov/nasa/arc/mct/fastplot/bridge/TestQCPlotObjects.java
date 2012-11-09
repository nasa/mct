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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.view.Axis;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JPanel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import plotter.xy.LinearXYAxis;
import plotter.xy.XYAxis;
import plotter.xy.XYPlot;

public class TestQCPlotObjects {

	@Mock 
	private PlotterPlot mockPlot;
	
	@Mock
	private PlotAbstraction mockPlotAbstraction;
	
	
	@BeforeMethod
	public void setup() {		
		MockitoAnnotations.initMocks(this);
		//TODO: Mockito.when or switch to PlotConfiguration, throughout file
//		mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;

		Mockito.when(mockPlotAbstraction.getTimeAxis()).thenReturn(new Axis());
		Mockito.when(mockPlot.getPlotPanel()).thenReturn(new JPanel());
		final TimeXYAxis[] axis=new TimeXYAxis[1];
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				axis[0] = (TimeXYAxis) invocation.getArguments()[0];
				return null;
			}
		}).when(mockPlot).setTimeAxis(Mockito.<TimeXYAxis> any());
		Mockito.when(mockPlot.getTimeAxis()).thenAnswer(new Answer<TimeXYAxis>() {
			@Override
			public TimeXYAxis answer(InvocationOnMock invocation) throws Throwable {
				return axis[0];
			}
		});
	}
	
	@Test 
	public void testSetupScrollFrameTimeOnX() {
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		
//		mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		testQC.setupScrollFrame();
//		
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		testQC.setupScrollFrame();
	}
	
	@Test 
	public void testSetupScrollFrameTimeOnY() {
		
//		mockPlot.axisOrientation = AxisOrientationSetting.Y_AXIS_AS_TIME;
//	
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		testQC.setupScrollFrame();
//		
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.AUTO;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		testQC.setupScrollFrame();
//		
//		mockPlot.nonTimeAxisMinSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED;
//		mockPlot.nonTimeAxisMaxSubsequentSetting = NonTimeAxisSubsequentBoundsSetting.FIXED;
//		testQC.setupScrollFrame();
	}
	
	@Test
	public void testResetNonTimeAxisToOriginalValues()  {
		GregorianCalendar plotStartTime = new GregorianCalendar();
		GregorianCalendar plotEndTime = new GregorianCalendar();
		plotEndTime.add(Calendar.HOUR, 1);
		double plotNonTimeMin = 100;
		double plotNonTimeMax = 200;
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(plotStartTime.getTimeInMillis());
		settings.setMaxTime(plotEndTime.getTimeInMillis());
		settings.setMinNonTime(plotNonTimeMin);
		settings.setMaxNonTime(plotNonTimeMax);
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	

		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
		yAxis.setStart(10.0);
		yAxis.setEnd(99.0);
		
		qcPlot.resetNonTimeAxisToOriginalValues();

		Assert.assertEquals(plotNonTimeMin,  yAxis.getStart());
		Assert.assertEquals(plotNonTimeMax,  yAxis.getEnd());

		settings = new PlotSettings();
		settings.setMinTime(plotStartTime.getTimeInMillis());
		settings.setMaxTime(plotEndTime.getTimeInMillis());
		settings.setMinNonTime(plotNonTimeMin);
		settings.setMaxNonTime(plotNonTimeMax);
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		
		testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	

        qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
        yAxis = qcPlot.getPlotView().getYAxis();
		
		yAxis.setStart(10.0);
		yAxis.setEnd(99.0);
		
		qcPlot.resetNonTimeAxisToOriginalValues();

		Assert.assertEquals(plotNonTimeMin,  yAxis.getEnd());
		Assert.assertEquals(plotNonTimeMax,  yAxis.getStart());
		
		settings = new PlotSettings();
		settings.setMinTime(plotStartTime.getTimeInMillis());
		settings.setMaxTime(plotEndTime.getTimeInMillis());
		settings.setMinNonTime(plotNonTimeMin);
		settings.setMaxNonTime(plotNonTimeMax);
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		
		testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	

        qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
		xAxis.setStart(10.0);
		xAxis.setEnd(99.0);
		
		qcPlot.resetNonTimeAxisToOriginalValues();
		
		Assert.assertEquals(plotNonTimeMin,  xAxis.getStart());
		Assert.assertEquals(plotNonTimeMax,  xAxis.getEnd());
		
		settings = new PlotSettings();
		settings.setMinTime(plotStartTime.getTimeInMillis());
		settings.setMaxTime(plotEndTime.getTimeInMillis());
		settings.setMinNonTime(plotNonTimeMin);
		settings.setMaxNonTime(plotNonTimeMax);
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		
		testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	

        qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		xAxis = qcPlot.getPlotView().getXAxis();
		xAxis.setStart(10.0);
		xAxis.setEnd(99.0);
		
		qcPlot.resetNonTimeAxisToOriginalValues();
		
		Assert.assertEquals(plotNonTimeMin,  xAxis.getEnd());
		Assert.assertEquals(plotNonTimeMax,  xAxis.getStart());	
	}
	
	@Test
	public void testResetTimeAxisToOriginalValues()  {
//		long plotStartTime = 500;
//		long plotEndTime = 600;
//		double plotNonTimeMin = 100;
//		double plotNonTimeMax = 200;
//		
//		PlotSettings settings = new PlotSettings();
//		settings.setMinTime(plotStartTime);
//		settings.setMaxTime(plotEndTime);
//		settings.setMinNonTime(plotNonTimeMin);
//		settings.setMaxNonTime(plotNonTimeMax);
//		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
//		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
//		
//		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
//		.plotSettings(settings)
//		.build();	
//
//		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
//	
//		XYAxis xAxis = qcPlot.getPlotView().getXAxis();
//		xAxis.setStart(10.0);
//		xAxis.setEnd(99.0);
//		
//		Assert.assertEquals(500.0, xAxis.getStart());
//		Assert.assertEquals(600.0,  xAxis.getEnd());
//		
//		settings = new PlotSettings();
//		settings.setMinTime(plotStartTime);
//		settings.setMaxTime(plotEndTime);
//		settings.setMinNonTime(plotNonTimeMin);
//		settings.setMinNonTime(plotNonTimeMax);
//		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
//		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
//		
//		testPlot = new PlotView.Builder(PlotterPlot.class)
//		.plotSettings(settings)
//		.build();	
//
//        qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
//		
//		xAxis = qcPlot.getPlotView().getXAxis();
//		xAxis.setStart(10.0);
//		xAxis.setEnd(99.0);
//		
//		qcPlot.resetNonTimeAxisToOriginalValues();
//		
//		Assert.assertEquals(500.0,  xAxis.getEnd());
//		Assert.assertEquals(600.0,  xAxis.getStart());
//		
//		settings = new PlotSettings();
//		settings.setMinTime(plotStartTime);
//		settings.setMaxTime(plotEndTime);
//		settings.setMinNonTime(plotNonTimeMin);
//		settings.setMinNonTime(plotNonTimeMax);
//		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
//		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
//		
//		testPlot = new PlotView.Builder(PlotterPlot.class)
//		.plotSettings(settings)
//		.build();	
//
//        qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
//		
//		XYAxis yAxis = qcPlot.getPlotView().getYAxis();
//		yAxis.setStart(10.0);
//		yAxis.setEnd(99.0);
//		
//		qcPlot.resetNonTimeAxisToOriginalValues();
//		
//		Assert.assertEquals(500.0,  yAxis.getStart());
//		Assert.assertEquals(600.0,  yAxis.getEnd());
//		
//		settings = new PlotSettings();
//		settings.setMinTime(plotStartTime);
//		settings.setMaxTime(plotEndTime);
//		settings.setMinNonTime(plotNonTimeMin);
//		settings.setMinNonTime(plotNonTimeMax);
//		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
//		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
//		
//		testPlot = new PlotView.Builder(PlotterPlot.class)
//		.plotSettings(settings)
//		.build();	
//
//        qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
//		
//		yAxis = qcPlot.getPlotView().getYAxis();
//		yAxis.setStart(10.0);
//		yAxis.setEnd(99.0);
//		
//		qcPlot.resetNonTimeAxisToOriginalValues();
//		
//		Assert.assertEquals(500.0,  yAxis.getEnd());
//		Assert.assertEquals(600.0,  yAxis.getStart());
	}
	
	@Test
	public void testFastForwardToCurrentMCTTimeDefaultPlotSpan() {
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//		mockPlot.timeVariableAxisMaxValue = 200;
//		mockPlot.timeVariableAxisMinValue = 0;
//		
//		GregorianCalendar currentMCTTime = new GregorianCalendar();
//		currentMCTTime.setTimeInMillis(10000);
//		Mockito.when( mockPlotAbstraction.getCurrentMCTTime()).thenReturn(currentMCTTime.getTimeInMillis());
//		
//		mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		XYAxis xAxis = mockPlot.plotView.getXAxis();
//		Assert.assertEquals(xAxis.getStart(), 9800.0);
//		Assert.assertEquals(xAxis.getEnd(), 10000.0);
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		Assert.assertEquals(xAxis.getEnd(), 9800.0);
//		Assert.assertEquals(xAxis.getStart(), 10000.0);
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		Assert.assertEquals(xAxis.getStart(), 0.0);
//		Assert.assertEquals(xAxis.getEnd(), 10000.0);
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		Assert.assertEquals(xAxis.getEnd(), 0.0);
//		Assert.assertEquals(xAxis.getStart(), 10000.0);
	}

	@Test
	public void testFastForwardToCurrentMCTTimeDefaultPlotSpanYAxisAsTime() {
//		mockPlot.axisOrientation = AxisOrientationSetting.Y_AXIS_AS_TIME;
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//		mockPlot.timeVariableAxisMaxValue = 200;
//		mockPlot.timeVariableAxisMinValue = 0;
//		
//		GregorianCalendar currentMCTTime = new GregorianCalendar();
//		currentMCTTime.setTimeInMillis(10000);
//		Mockito.when( mockPlotAbstraction.getCurrentMCTTime()).thenReturn(currentMCTTime.getTimeInMillis());
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		XYAxis yAxis = mockPlot.plotView.getYAxis();
//		Assert.assertEquals(yAxis.getStart(), 9800.0);
//		Assert.assertEquals(yAxis.getEnd(), 10000.0);
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		Assert.assertEquals(yAxis.getEnd(), 9800.0);
//		Assert.assertEquals(yAxis.getStart(), 10000.0);
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		Assert.assertEquals(yAxis.getStart(), 0.0);
//		Assert.assertEquals(yAxis.getEnd(), 10000.0);
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(true);
//		Assert.assertEquals(yAxis.getEnd(), 0.0);
//		Assert.assertEquals(yAxis.getStart(), 10000.0);
	}	
	
	@Test
	public void testFastForwardToCurrentMCTTimeCurrentPlotSpanPlotSpan() {
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//		GregorianCalendar timeScaleStart = new GregorianCalendar();
//		GregorianCalendar timeScaleEnd = new GregorianCalendar();
//		
//		timeScaleEnd.add(Calendar.HOUR, 2);
//		
//		mockPlot.timeVariableAxisMaxValue = 200;
//		mockPlot.timeVariableAxisMinValue = 0;
//		
//		int pausePeriod = 50000;
//		
//		GregorianCalendar currentMCTTime = new GregorianCalendar();
//		currentMCTTime.setTimeInMillis(timeScaleEnd.getTimeInMillis() + pausePeriod);
//		Mockito.when( mockPlotAbstraction.getCurrentMCTTime()).thenReturn(currentMCTTime.getTimeInMillis());
//		
//		XYAxis xAxis = mockPlot.plotView.getXAxis();
//		xAxis.setStart(timeScaleStart.getTimeInMillis());
//		xAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		
//		mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(xAxis.getStart(), (double) timeScaleStart.getTimeInMillis() + pausePeriod);
//		Assert.assertEquals(xAxis.getEnd(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
//		xAxis.setStart(timeScaleEnd.getTimeInMillis());
//		xAxis.setEnd(timeScaleStart.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(xAxis.getEnd(), (double) timeScaleStart.getTimeInMillis() + pausePeriod);
//		Assert.assertEquals(xAxis.getStart(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
//				
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		xAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		xAxis.setStart(timeScaleStart.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(xAxis.getStart(), 0.0);
//		Assert.assertEquals(xAxis.getEnd(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
//		xAxis.setStart(timeScaleEnd.getTimeInMillis());
//		xAxis.setEnd(timeScaleStart.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(xAxis.getEnd(), 0.0);
//		Assert.assertEquals(xAxis.getStart(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
	}	

	@Test
	public void testFastForwardToCurrentMCTTimeCurrentPlotSpanPlotSpanYAxisAsTime() {
//    	mockPlot.axisOrientation = AxisOrientationSetting.Y_AXIS_AS_TIME;
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//		GregorianCalendar timeScaleStart = new GregorianCalendar();
//		GregorianCalendar timeScaleEnd = new GregorianCalendar();
//		
//		timeScaleEnd.add(Calendar.HOUR, 2);
//		
//		mockPlot.timeVariableAxisMaxValue = 200;
//		mockPlot.timeVariableAxisMinValue = 0;
//		
//		int pausePeriod = 50000;
//		
//		GregorianCalendar currentMCTTime = new GregorianCalendar();
//		currentMCTTime.setTimeInMillis(timeScaleEnd.getTimeInMillis() + pausePeriod);
//		Mockito.when( mockPlotAbstraction.getCurrentMCTTime()).thenReturn(currentMCTTime.getTimeInMillis());
//		
//		XYAxis xAxis = mockPlot.plotView.getXAxis();
//		xAxis.setStart(timeScaleStart.getTimeInMillis());
//		xAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		XYAxis yAxis = mockPlot.plotView.getYAxis();
//		yAxis.setStart(timeScaleStart.getTimeInMillis());
//		yAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(yAxis.getStart(), (double) timeScaleStart.getTimeInMillis() + pausePeriod);
//		Assert.assertEquals(yAxis.getEnd(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
//		
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
//		yAxis.setStart(timeScaleEnd.getTimeInMillis());
//		yAxis.setEnd(timeScaleStart.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(yAxis.getEnd(), (double) timeScaleStart.getTimeInMillis() + pausePeriod);
//		Assert.assertEquals(yAxis.getStart(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		yAxis.setStart(timeScaleStart.getTimeInMillis());
//		yAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(yAxis.getStart(), 0.0);
//		Assert.assertEquals(yAxis.getEnd(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
//		yAxis.setStart(timeScaleEnd.getTimeInMillis());
//		yAxis.setEnd(timeScaleStart.getTimeInMillis());
//		
//		testQC.fastForwardTimeAxisToCurrentMCTTime(false);
//		Assert.assertEquals(yAxis.getEnd(), 0.0);
//		Assert.assertEquals(yAxis.getStart(), (double) timeScaleEnd.getTimeInMillis() + pausePeriod);
	}	
	
	@Test
	public void testAdjustSpanWithoutFastForwardingToCurrenTime() {
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//		GregorianCalendar timeScaleStart = new GregorianCalendar();
//		GregorianCalendar timeScaleEnd = new GregorianCalendar();
//		
//		timeScaleEnd.add(Calendar.HOUR, 2);
//		
//		mockPlot.timeVariableAxisMaxValue = 10;
//		mockPlot.timeVariableAxisMinValue = 0;
//		
//		int desiredSpan = 10;
//		
//		int pausePeriod = 50000;
//		
//		GregorianCalendar currentMCTTime = new GregorianCalendar();
//		currentMCTTime.setTimeInMillis(timeScaleEnd.getTimeInMillis() + pausePeriod);
//		Mockito.when( mockPlotAbstraction.getCurrentMCTTime()).thenReturn(currentMCTTime.getTimeInMillis());
//
//		XYAxis xAxis = mockPlot.plotView.getXAxis();
//		xAxis.setStart(timeScaleStart.getTimeInMillis());
//		xAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		
//		mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.JUMP;
//		
//		testQC.adjustSpanToDesiredSpanWithoutFastFarwardingToCurrentTime();
//		
//		Assert.assertEquals(xAxis.getEnd(), (double) timeScaleStart.getTimeInMillis());
//		Assert.assertEquals(xAxis.getStart(), (double) timeScaleStart.getTimeInMillis() - desiredSpan);	
//		
//		mockPlot.timeAxisSubsequentSetting = TimeAxisSubsequentBoundsSetting.SCRUNCH;
//		xAxis.setStart(timeScaleStart.getTimeInMillis());
//		xAxis.setEnd(timeScaleEnd.getTimeInMillis());
//		
//        testQC.adjustSpanToDesiredSpanWithoutFastFarwardingToCurrentTime();
//		
//		Assert.assertEquals(xAxis.getEnd(), (double) currentMCTTime.getTimeInMillis());
//		Assert.assertEquals(xAxis.getStart(), (double) mockPlot.timeVariableAxisMinValue);	
	}
	
//	@Test
//	public void testIsTimeAxisInverted() {
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//	 	mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		Assert.assertFalse(testQC.isTimeAxisInverted());
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;		
//		Assert.assertTrue(testQC.isTimeAxisInverted());
//		
//		mockPlot.axisOrientation = AxisOrientationSetting.Y_AXIS_AS_TIME;
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		Assert.assertFalse(testQC.isTimeAxisInverted());
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
//		Assert.assertTrue(testQC.isTimeAxisInverted());		
//	}
//	
//	@Test
//	public void testIsNonTimeAxisInverted() {
//		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
//		mockPlot.plotAbstraction = mockPlotAbstraction;
//		
//	 	mockPlot.axisOrientation = AxisOrientationSetting.Y_AXIS_AS_TIME;
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT;
//		Assert.assertFalse(testQC.isNonTimeAxisInverted());
//		
//		mockPlot.xAxisSetting = XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;		
//		Assert.assertTrue(testQC.isNonTimeAxisInverted());
//		
//		mockPlot.axisOrientation = AxisOrientationSetting.X_AXIS_AS_TIME;
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_TOP;
//		Assert.assertFalse(testQC.isNonTimeAxisInverted());
//		
//		mockPlot.yAxisSetting = YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
//		Assert.assertTrue(testQC.isNonTimeAxisInverted());		
//	}

	@Test
	public void testScientificNotation() {
		XYPlot xyPlot = new XYPlot();
		Mockito.when(mockPlot.getPlotView()).thenReturn(null, xyPlot);
		Mockito.when(mockPlot.getStartTime()).thenReturn(new GregorianCalendar());
		Mockito.when(mockPlot.getEndTime()).thenReturn(new GregorianCalendar());
		Mockito.when(mockPlot.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		Mockito.when(mockPlot.getPlotAbstraction()).thenReturn(mockPlotAbstraction);
		
		QCPlotObjects testQC = new QCPlotObjects(mockPlot);
		XYAxis axis = testQC.plot.getPlotView().getYAxis();
		Assert.assertTrue(axis instanceof LinearXYAxis);
		NumberFormat format = ((LinearXYAxis) axis).getFormat();
		Assert.assertTrue(format.format(1111111).contains("E"));
		Assert.assertFalse(format.format(1).contains("E"));
	}
}
