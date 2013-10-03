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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.PinSupport;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotTimeSyncLine {

	@Mock 
	private PlotAbstraction plotView;

	@Mock
	private PlotViewManifestation mockPlotViewManifestation;

	private List<AbstractPlottingPackage> plots = new ArrayList<AbstractPlottingPackage>();

	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	
	@BeforeMethod
	public void setup() {
		long now = new GregorianCalendar().getTimeInMillis();
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(now);
		Mockito.when(plotView.getCurrentMCTTime()).thenReturn(now);
    	Mockito.when(plotView.getTimeAxis()).thenReturn(new Axis());
    	Mockito.when(plotView.getTimeAxisUserPin()).thenReturn(new PinSupport().createPin());
    	Mockito.when(plotView.getSubPlots()).thenReturn(plots);
    	Mockito.when(plotView.getMinTime()).thenReturn(now - 360000);
    	Mockito.when(plotView.getMaxTime()).thenReturn(now + 360000);
    	Mockito.when(plotView.getXAxisMaximumLocation()).thenReturn(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
    	Mockito.when(plotView.getYAxisMaximumLocation()).thenReturn(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
    	Mockito.when(plotView.getPlotLineDraw()).thenReturn(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
	}
	
	@Test 
	public void testUserMouseClickEstablishSyncLineXAsTime() {
		
		/*
		 * Plan: Simulate  a user by moving the mouse into the plot area. Click on
		 * the time axis. Release shift and click. Check that sync line appears and is removed. 
		 * Then try some clicks outside the target area or within without  pressed
		 * and test that a sync line does not appear. 
		 */
		
		Mockito.when(plotView.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		
		final PlotterPlot plot = new PlotterPlot();
		plot.createChart(
				new Font("Arial", Font.PLAIN, 12), 
				1, 
				Color.gray, 
				Color.black, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.black, 
				1, 
				false,
				true,
				true,
				plotView,
				plotLabelingAlgorithm);
		plots.add(plot);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.showTimeSyncLine((GregorianCalendar) invocation.getArguments()[0]);
				return null;
			}
		}).when(plotView).showTimeSyncLine(Mockito.<GregorianCalendar>any());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.removeTimeSyncLine();
				return null;
			}
		}).when(plotView).removeTimeSyncLine();
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.removeTimeSyncLine();
				return null;
			}
		}).when(plotView).notifyGlobalTimeSyncFinished();
		
		JFrame frame = new JFrame();
		frame.add(plot.getPlotComponent());
		frame.pack();
		
		// put data onto the plot so that it is initialized
		   plot.addDataSet("DataSet1", Color.red);
		   GregorianCalendar now = new GregorianCalendar();
		   // Add in limit value
		   plot.addData("DataSet1", now.getTimeInMillis(), 10.0);
		
		// Plot should have a time sync line. 
		Assert.assertNotNull(plot.timeSyncLine);
		
	    plot.refreshDisplay();
		  
		// User moves mouse into the general area. 
	    Rectangle contentRect = plot.getPlotView().getContents().getBounds();
		plot.timeSyncLine.mouseEntered(new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) contentRect.getMinX() + 1, (int) contentRect.getMaxY(), 0, false));
	
			
		// User clicks outside of the plot time axis area.	
		MouseEvent outsideMouseEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) contentRect.getMaxX()  -1, (int) contentRect.getMaxY() -1, 0, false);
		plot.timeSyncLine.mousePressed(outsideMouseEvent);	
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());
		
		plot.timeSyncLine.mouseReleased(outsideMouseEvent);
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());

		// User clicks inside of the plot time axis area.	
		MouseEvent insideMouseEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) contentRect.getMinX() + 1, 
				                                                                   (int) contentRect.getMaxY(), 0, false);
			   	
		plot.timeSyncLine.mousePressed(insideMouseEvent);	
		Assert.assertTrue(plot.timeSyncLine.timeSyncLineVisible());
		
		MouseEvent insideDragEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) contentRect.getMinX() + 20, (int) contentRect.getMaxY(), 0, false);
		plot.timeSyncLine.mouseDragged(insideDragEvent);	
		Assert.assertTrue(plot.timeSyncLine.timeSyncLineVisible());
		Assert.assertTrue(plot.timeSyncLine.inTimeSyncMode());
	}
	
	
	@Test
	public void testMouseExit() {
		Mockito.when(plotView.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		final PlotterPlot plot = new PlotterPlot();
		plot.createChart(
				new Font("Arial", Font.PLAIN, 12), 
				1, 
				Color.gray, 
				Color.black, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.black, 
				1, 
				false,
				true,
				true,
				plotView,
				plotLabelingAlgorithm);
		plots.add(plot);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.showTimeSyncLine((GregorianCalendar) invocation.getArguments()[0]);
				return null;
			}
		}).when(plotView).showTimeSyncLine(Mockito.<GregorianCalendar>any());
		
		JFrame frame = new JFrame();
		frame.add(plot.getPlotComponent());
		frame.pack();
		
		// put data onto the plot so that it is initialized
		   plot.addDataSet("DataSet1", Color.red);
		   GregorianCalendar now = new GregorianCalendar();
		   // Add in limit value
		   plot.addData("DataSet1", now.getTimeInMillis(), 10.0);
		
		// Plot should have a time sync line. 
		Assert.assertNotNull(plot.timeSyncLine);
		
	    plot.refreshDisplay();
		
	    Rectangle contentRect = plot.getPlotView().getContents().getBounds();
		MouseEvent pressEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) contentRect.getMinX() + 20, (int) contentRect.getMaxY(), 0, false, MouseEvent.BUTTON1);
		plot.timeSyncLine.mousePressed(pressEvent);
		Assert.assertTrue(plot.timeSyncLine.timeSyncLineVisible());
		Assert.assertTrue(plot.timeSyncLine.inTimeSyncMode());

		// Move mouse
		MouseEvent insideDragEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) contentRect.getMinX() + 20, (int) contentRect.getMaxY(), 0, false);
		plot.timeSyncLine.mouseDragged(insideDragEvent);	
		Assert.assertTrue(plot.timeSyncLine.timeSyncLineVisible());
		Assert.assertTrue(plot.timeSyncLine.inTimeSyncMode());
		
		// Move mouse outside. 
		MouseEvent mouseExit = new MouseEvent(new JPanel(), 1, 0, 0, 0, 0, 0, false);
		plot.timeSyncLine.mouseExited(mouseExit);	
		verify(plotView, never()).notifyGlobalTimeSyncFinished();
	}
	
	@Test
	public void testUserMouseClickEstablishSyncLineYAsTime() {
		
		/*
		 * Plan: Simulate  a user by moving the mouse into the plot area. Click on
		 * the time axis. Release shift and click. Check that sync line appears and is removed. 
		 * Then try some clicks outside the target area or within without  pressed
		 * and test that a sync line does not appear. 
		 */
		
		Mockito.when(plotView.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.Y_AXIS_AS_TIME);
		
		PlotterPlot plot = new PlotterPlot();
		plot.createChart(
				new Font("Arial", Font.PLAIN, 12), 
				1, 
				Color.gray, 
				Color.black, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.black, 
				1, 
				false,
				true,
				true,
				plotView,
				plotLabelingAlgorithm);
		plots.add(plot);
		
		// put data onto the plot so that it is initialized
		   plot.addDataSet("DataSet1", Color.red);
		   GregorianCalendar now = new GregorianCalendar();
		   // Add in limit value
		   plot.addData("DataSet1", now.getTimeInMillis(), 10.0);
		
		
		// Plot should have a time sync line. 
		Assert.assertNotNull(plot.timeSyncLine);
		
		Rectangle plotRectangle = plot.getPlotView().getContents().getBounds();
		  
		// User moves mouse into the general area. 
		plot.timeSyncLine.mouseEntered(new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) plotRectangle.getMinX() - 1, (int) plotRectangle.getMaxY() -1, 0, false));
		
		// User clicks outside of the plot time axis area.	
		MouseEvent outsideMouseEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) plotRectangle.getMinX()  +1, (int) plotRectangle.getMaxY() -1, 0, false);
		plot.timeSyncLine.mousePressed(outsideMouseEvent);	
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());
		
		plot.timeSyncLine.mouseReleased(outsideMouseEvent);
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());
		
		// Move the mouse below the timeline axis.
		outsideMouseEvent = new MouseEvent(plot.getPlotComponent(), 1, 0, 0, (int) plotRectangle.getMinX() + 1, (int) plotRectangle.getMaxY() + plot.getPlotView().getXAxis().getHeight()+ 1, 0, false);
		plot.timeSyncLine.mousePressed(outsideMouseEvent);	
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());
		plot.timeSyncLine.mouseReleased(outsideMouseEvent);	
	}
	
	@Test
	public void testProgramaticRequestsEstablishSyncLine() {
		/*
		 * Plan: Request and remove the sync line using the plot's API.
		 */
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class).build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.addDataSet("Foo");
		testPlot.addData("Foo", System.currentTimeMillis(), 10);
		
		Assert.assertFalse(testPlot.isTimeSyncLineVisible());
		testPlot.showTimeSyncLine(new GregorianCalendar());
		Assert.assertTrue(testPlot.isTimeSyncLineVisible());	
		testPlot.removeTimeSyncLine();
		Assert.assertFalse(testPlot.isTimeSyncLineVisible());
		
		PlotSettings settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		testPlot = new PlotView.Builder(PlotterPlot.class).plotSettings(settings).build();
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.addDataSet("Foo");
		testPlot.addData("Foo", System.currentTimeMillis(), 10);
		
		Assert.assertFalse(testPlot.isTimeSyncLineVisible());
		testPlot.showTimeSyncLine(new GregorianCalendar());
		Assert.assertTrue(testPlot.isTimeSyncLineVisible());
		testPlot.removeTimeSyncLine();
		Assert.assertFalse(testPlot.isTimeSyncLineVisible());
	}


	@Test
	public void testShiftAfterClick() {
		// Click and hold mouse button to start vertical line, then press shift to enter time sync mode.
		Mockito.when(plotView.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		final PlotterPlot plot = new PlotterPlot();
		plot.createChart(
				new Font("Arial", Font.PLAIN, 12), 
				1, 
				Color.gray, 
				Color.black, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.black, 
				1, 
				false,
				true,
				true,
				plotView,
				plotLabelingAlgorithm);
		plots.add(plot);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.showTimeSyncLine((GregorianCalendar) invocation.getArguments()[0]);
				return null;
			}
		}).when(plotView).showTimeSyncLine(Mockito.<GregorianCalendar> any());
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.removeTimeSyncLine();
				return null;
			}
		}).when(plotView).removeTimeSyncLine();
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				plot.removeTimeSyncLine();
				return null;
			}
		}).when(plotView).notifyGlobalTimeSyncFinished();

		JFrame frame = new JFrame();
		JComponent panel = plot.getPlotComponent();
		frame.add(panel);
		frame.pack();

		// put data onto the plot so that it is initialized
		plot.addDataSet("DataSet1", Color.red);
		GregorianCalendar now = new GregorianCalendar();
		// Add in limit value
		plot.addData("DataSet1", now.getTimeInMillis(), 10.0);

		// Plot should have a time sync line. 
		Assert.assertNotNull(plot.timeSyncLine);
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());

		plot.refreshDisplay();

		// User clicks inside of the plot time axis area.	
		Rectangle contentRect = plot.getPlotView().getContents().getBounds();
		MouseEvent pressEvent = new MouseEvent(panel, 1, 0, 0, (int) contentRect.getMinX() + 1, (int) contentRect.getMaxY(), 0, false);

		plot.timeSyncLine.mousePressed(pressEvent);
		Assert.assertTrue(plot.timeSyncLine.timeSyncLineVisible());
		Mockito.verify(plotView, Mockito.never()).initiateGlobalTimeSync(Mockito.<GregorianCalendar> any());

		plot.timeSyncLine.informShiftKeyState(true);
		Assert.assertTrue(plot.timeSyncLine.timeSyncLineVisible());
		Mockito.verify(plotView).initiateGlobalTimeSync(Mockito.<GregorianCalendar> any());

		MouseEvent releaseEvent = new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, (int) contentRect.getMinX() + 1, (int) contentRect.getMaxY(), 0, false);
		plot.timeSyncLine.mouseReleased(releaseEvent);
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());

		plot.timeSyncLine.informShiftKeyState(false);
		Assert.assertFalse(plot.timeSyncLine.timeSyncLineVisible());
	}
}
