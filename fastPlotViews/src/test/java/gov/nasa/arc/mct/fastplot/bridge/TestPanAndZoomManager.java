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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PanDirection;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotDisplayState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.ZoomDirection;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.PinSupport;

import java.awt.Color;
import java.util.GregorianCalendar;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import plotter.xy.XYAxis;

public class TestPanAndZoomManager {

	@Mock
	private PlotAbstraction plotAbstraction;
	
	private PlotterPlot plotTimeOnX;
	private PlotterPlot plotTimeOnY;

	PanAndZoomManager panAndZoomManagerTimeOnX;
	PanAndZoomManager panAndZoomManagerTimeOnY;
	
	private long now = System.currentTimeMillis();
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		PinSupport pins = new PinSupport();
		
		Mockito.when(plotAbstraction.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());
		Mockito.when(plotAbstraction.getTimeAxis()).thenReturn(new Axis());
		Mockito.when(plotAbstraction.getTimeAxisUserPin()).thenReturn(pins.createPin());
		Mockito.when(plotAbstraction.getPlotLineDraw()).thenReturn(new PlotLineDrawingFlags(true,false));

		PlotSettings settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setMaxNonTime(100);
		settings.setMinNonTime(0);
		settings.setMinTime(now);
		settings.setMaxTime(now + 300000L);
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		
		PlotAbstraction testPlotTimeX = new PlotView.Builder(PlotterPlot.class).
			                        plotSettings(settings).
		                            build();
	    plotTimeOnX = (PlotterPlot) testPlotTimeX.returnPlottingPackage();
	    plotTimeOnX.setPlotAbstraction(plotAbstraction);
	    panAndZoomManagerTimeOnX = plotTimeOnX.panAndZoomManager;

		PlotSettings settings2 = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setMaxNonTime(100);
		settings.setMinNonTime(0);
		settings.setMinTime(now);
		settings.setMaxTime(now + 300000L);
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
	    
		PlotAbstraction testPlotTimeY = new PlotView.Builder(PlotterPlot.class).
							        plotSettings(settings2).
							        build();
                
        plotTimeOnY = (PlotterPlot) testPlotTimeY.returnPlottingPackage();
        plotTimeOnY.setPlotAbstraction(plotAbstraction);
        panAndZoomManagerTimeOnY = plotTimeOnY.panAndZoomManager;  
	}
	
	@Test
	public void TestEnteringPanMode() {
		plotTimeOnX.setPlotDisplayState(PlotDisplayState.DISPLAY_ONLY);
		panAndZoomManagerTimeOnX.enteredPanMode();
		Assert.assertEquals(plotTimeOnX.getPlotDisplayState(), PlotDisplayState.USER_INTERACTION);	
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		panAndZoomManagerTimeOnX.exitedPanMode();
		
	}
	
	@Test
	public void TestEnteringZoomMode() {
		plotTimeOnX.setPlotDisplayState(PlotDisplayState.DISPLAY_ONLY);
		panAndZoomManagerTimeOnX.enteredZoomMode();
		Assert.assertEquals(plotTimeOnX.getPlotDisplayState(), PlotDisplayState.USER_INTERACTION);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		panAndZoomManagerTimeOnX.exitedZoomMode();
	}
	
	@Test
	public void TestPanningActionsTimeOnX() {
		XYAxis xAxis = plotTimeOnX.getPlotView().getXAxis();
		XYAxis yAxis = plotTimeOnX.getPlotView().getYAxis();

		double xStart = xAxis.getStart();
		double xStop = xAxis.getEnd();
		
		double yStart = yAxis.getStart();
		double yStop = yAxis.getEnd();
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		
		double newXStart = xAxis.getStart();
		double newXStop = xAxis.getEnd();
		double newYStart = yAxis.getStart();
		double newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop > xStop);
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);	
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newXStart < xStart);
		Assert.assertTrue(newXStop < xStop);
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_Y_AXIS);
	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart > yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_Y_AXIS);	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_Y_AXIS);	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop < yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
	}
	
	
	@Test
	public void TestPanningActionsTimeOnY() {
		XYAxis xAxis = plotTimeOnY.getPlotView().getXAxis();
		XYAxis yAxis = plotTimeOnY.getPlotView().getYAxis();

		double xStart = xAxis.getStart();
		double xStop = xAxis.getEnd();
		
		double yStart = yAxis.getStart();
		double yStop = yAxis.getEnd();
		
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		
		double newXStart = xAxis.getStart();
		double newXStop = xAxis.getEnd();
		double newYStart = yAxis.getStart();
		double newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop > xStop);
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_LOWER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);	
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_LOWER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newXStart < xStart);
		Assert.assertTrue(newXStop < xStop);
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_HIGHER_Y_AXIS);
	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart > yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_LOWER_Y_AXIS);	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertEquals(newYStop, yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.panAction(PanDirection.PAN_LOWER_Y_AXIS);	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop < yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
	}
	
	
	
	@Test
	public void TestZoomingActionsTimeX() {
		XYAxis xAxis = plotTimeOnX.getPlotView().getXAxis();
		XYAxis yAxis = plotTimeOnX.getPlotView().getYAxis();

		double xStart = xAxis.getStart();
		double xStop = xAxis.getEnd();	
		double yStart = yAxis.getStart();
		double yStop = yAxis.getEnd();
		
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_HIGH_Y_AXIS);

		double newXStart = xAxis.getStart();
		double newXStop = xAxis.getEnd();
		double newYStart = yAxis.getStart();
		double newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertTrue(newYStop < yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_HIGH_Y_AXIS);
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_HIGH_Y_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_Y_AXIS);
	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart > yStart);
		Assert.assertTrue(newYStop < yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS);
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS);
	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_LOW_Y_AXIS);
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_LOW_Y_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart > yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_LOW_Y_AXIS);
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_LOW_Y_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_LEFT_X_AXIS);
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_LEFT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_LEFT_X_AXIS);
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_LEFT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertEquals(newXStop, xStop);
			
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
	    Assert.assertTrue(newXStart > xStart);
	    Assert.assertTrue(newXStop < xStop);
	    Assert.assertTrue(newYStart < yStart);
	    Assert.assertTrue(newYStop > yStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop < xStop);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_RIGHT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop < xStop);

		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_OUT_RIGHT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop < xStop);	
	}
	
	@Test
	public void TestZoomingActionsTimeY() {
		XYAxis xAxis = plotTimeOnY.getPlotView().getXAxis();
		XYAxis yAxis = plotTimeOnY.getPlotView().getYAxis();
		
		double xStart = xAxis.getStart();
		double xStop = xAxis.getEnd();	
		double yStart = yAxis.getStart();
		double yStop = yAxis.getEnd();
		
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_HIGH_Y_AXIS);

		double newXStart = xAxis.getStart();
		double newXStop = xAxis.getEnd();
		double newYStart = yAxis.getStart();
		double newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertTrue(newYStop < yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_HIGH_Y_AXIS);
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_HIGH_Y_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertEquals(newYStart, yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_CENTER_Y_AXIS);
	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart > yStart);
		Assert.assertTrue(newYStop < yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS);
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS);
	
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_LOW_Y_AXIS);
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_LOW_Y_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart > yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_LOW_Y_AXIS);
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_LOW_Y_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertEquals(newXStart, xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_LEFT_X_AXIS);
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_LEFT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertEquals(newXStop, xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_LEFT_X_AXIS);
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_LEFT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertEquals(newXStop, xStop);
			
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
	    Assert.assertTrue(newXStart > xStart);
	    Assert.assertTrue(newXStop < xStop);
	    Assert.assertTrue(newYStart < yStart);
	    Assert.assertTrue(newYStop > yStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop < xStop);
		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_IN_RIGHT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop < xStop);

		
		panAndZoomManagerTimeOnY.zoomAction(ZoomDirection.ZOOM_OUT_RIGHT_X_AXIS);
		
		newXStart = xAxis.getStart();
		newXStop = xAxis.getEnd();
		newYStart = yAxis.getStart();
		newYStop = yAxis.getEnd();
		
		Assert.assertTrue(newYStart < yStart);
		Assert.assertTrue(newYStop > yStop);
		Assert.assertTrue(newXStart > xStart);
		Assert.assertTrue(newXStop < xStop);
		
	}
	
	@Test
	public void TestOutOfBoundsArrowsWithPanAndZoomActionsFixedNonTime() {
		now = System.currentTimeMillis();
		PlotSettings settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setMinNonTime(0);
		settings.setMaxNonTime(100);
		settings.setMinTime(now);
		settings.setMaxTime(now + 300000L);
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		
		PlotAbstraction testPlotTimeX = new PlotView.Builder(PlotterPlot.class).
                plotSettings(settings).
                build();
		plotTimeOnX = (PlotterPlot) testPlotTimeX.returnPlottingPackage();
		plotTimeOnX.setPlotAbstraction(plotAbstraction);
		Mockito.when(plotAbstraction.getMinTime()).thenReturn(0L);
		Mockito.when(plotAbstraction.getMaxTime()).thenReturn(100L);
		panAndZoomManagerTimeOnX = plotTimeOnX.panAndZoomManager; 
		
		// Add a data set
		plotTimeOnX.addDataSet("DataSet1", Color.RED);
		
		// Test Time axis zoom actions
		plotTimeOnX.addData("DataSet1", now + 10000L, 50);


		// All data in range
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);	
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_X_AXIS);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);	
		
		long time1 = now + 1L;  // should not appear in time-axis when zoomed or panned right
		
		// Add data out of range, but within plot area
		plotTimeOnX.addData("DataSet1", time1, 150);
		plotTimeOnX.addData("DataSet1", time1, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		// Add data out of range, and out of plot area
		plotTimeOnX.addData("DataSet1", time1, 150);
		plotTimeOnX.addData("DataSet1", time1, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);	
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_X_AXIS);
		// Add data out of range, but within plot area
		plotTimeOnX.addData("DataSet1", time1, 150);
		plotTimeOnX.addData("DataSet1", time1, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);	
		
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		plotTimeOnX.addDataSet("DataSet1", Color.RED);
		long time2 = (plotTimeOnX.getMinTime() +plotTimeOnX.getMaxTime() ) / 2L; 
		// middle point on x-axis, should appear when time center-zoomed
		plotTimeOnX.addData("DataSet1", time2, 150);
		plotTimeOnX.addData("DataSet1", time2, -50);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
	
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_Y_AXIS);
		plotTimeOnX.addData("DataSet1", time2, 150);
		plotTimeOnX.addData("DataSet1", time2, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_Y_AXIS);
		plotTimeOnX.addData("DataSet1", time2, 150);
		plotTimeOnX.addData("DataSet1", time2, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
	
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_Y_AXIS);
		plotTimeOnX.addData("DataSet1", time2, 150);
		plotTimeOnX.addData("DataSet1", time2, -50);
		// Should still raise alarm
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);

	}
	
	@Test
	public void TestOutOfBoundsArrowsWithPanAndZoomActionsAutoNonTime() {
		now = System.currentTimeMillis();

		PlotSettings settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setMaxNonTime(100);
		settings.setMinNonTime(0);
		settings.setMinTime(now);
		settings.setMaxTime(now + 300000L);
		settings.setNonTimeMinPadding(0);
		settings.setNonTimeMaxPadding(0);
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		
		Mockito.when(plotAbstraction.getMinTime()).thenReturn(now);
		Mockito.when(plotAbstraction.getMaxTime()).thenReturn(now + 300000L);
		
		PlotAbstraction testPlotTimeX = new PlotView.Builder(PlotterPlot.class).
                plotSettings(settings).
                build();
		plotTimeOnX = (PlotterPlot) testPlotTimeX.returnPlottingPackage();
		plotTimeOnX.setPlotAbstraction(plotAbstraction);
		panAndZoomManagerTimeOnX = plotTimeOnX.panAndZoomManager;
		
		// Add a data set
		plotTimeOnX.addDataSet("DataSet1", Color.RED);
		
		// Test Time axis zoom actions
		plotTimeOnX.addData("DataSet1", now + 10000L, 50);

		// All data in range
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);	
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_X_AXIS);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);	
		
		long time1 = now + 1L;  // should not appear in time-axis when zoomed or panned right
		
		plotTimeOnX.cornerResetButtonManager.informResetXAndYActionSelected();
		// Add data out of range, but within plot area: no alarms
		plotTimeOnX.addData("DataSet1", time1, 150);
		plotTimeOnX.addData("DataSet1", time1, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		// Add data out of range but within time area
		time1 = (plotTimeOnX.getMinTime() + plotTimeOnX.getMaxTime()) / 2L;
		plotTimeOnX.addData("DataSet1", time1, 150);
		plotTimeOnX.addData("DataSet1", time1, -50);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);	
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_X_AXIS);
		// Add data out of range, outside time area
		time1 = plotTimeOnX.getMaxTime() + 10000L;
		plotTimeOnX.addData("DataSet1", time1, 150);
		plotTimeOnX.addData("DataSet1", time1, -50);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);	
		
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		plotTimeOnX.addDataSet("DataSet1", Color.RED);
		long time2 = (plotTimeOnX.getMinTime() +plotTimeOnX.getMaxTime() ) / 2L; 
		// middle point on x-axis, should appear when time center-zoomed
		plotTimeOnX.addData("DataSet1", time2, 150);
		plotTimeOnX.addData("DataSet1", time2, -50);
		
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
	
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_HIGHER_Y_AXIS);
		
		time2 = (plotTimeOnX.getMinTime() +plotTimeOnX.getMaxTime() ) / 2L;
		double aboveMax = plotTimeOnX.getMaxNonTime() + 100;
		double belowMin = plotTimeOnX.getMinNonTime() - 100;

		plotTimeOnX.addData("DataSet1", time2, aboveMax);
		plotTimeOnX.addData("DataSet1", time2, belowMin);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
		
		panAndZoomManagerTimeOnX.panAction(PanDirection.PAN_LOWER_Y_AXIS);
		aboveMax = plotTimeOnX.getMaxNonTime() + 100;
		belowMin = plotTimeOnX.getMinNonTime() - 100;
		plotTimeOnX.addData("DataSet1", time2, aboveMax);
		plotTimeOnX.addData("DataSet1", time2, belowMin);
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
		
		
		plotTimeOnX.clearAllDataFromPlot();
		plotTimeOnX.cornerResetButtonManager.resetX();
		plotTimeOnX.cornerResetButtonManager.resetY();
	
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		
		panAndZoomManagerTimeOnX.zoomAction(ZoomDirection.ZOOM_IN_CENTER_Y_AXIS);
		time2 = (plotTimeOnX.getMinTime() +plotTimeOnX.getMaxTime() ) / 2L;
		aboveMax = plotTimeOnX.getMaxNonTime() + 100;
		belowMin = plotTimeOnX.getMinNonTime() - 100;
		plotTimeOnX.addData("DataSet1", time2, aboveMax);
		plotTimeOnX.addData("DataSet1", time2, belowMin);
		// Should still raise alarm
		Assert.assertEquals(plotTimeOnX.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(plotTimeOnX.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);

	}
}
