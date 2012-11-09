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

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import plotter.DoubleData;
import plotter.xy.CompressingXYDataset;

public class TestPlotDataManager {

	@Mock 
	private PlotAbstraction plotView;
	private GregorianCalendar currentMCTTime;

	@Mock
	private PlotViewManifestation plotUser;

	@BeforeMethod
	public void setup() {
		currentMCTTime = new GregorianCalendar();
		currentMCTTime.add(Calendar.HOUR, 10);

		MockitoAnnotations.initMocks(this);
		Mockito.when(plotView.getCurrentMCTTime()).thenReturn(currentMCTTime.getTimeInMillis());
		Mockito.when(plotView.getPlotLineDraw()).thenReturn(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
	}

	@Test
	public void makeSureMinSamplesForAutoScaleIsZero() {
		// This absolutely must be ZERO otherwise we're holding off showing updates until the > 0 value
		// it is set to is achieved. 
		Assert.assertEquals(PlotDataManager.MIN_SAMPLES_FOR_AUTOSCALE, 0);
	}

	@Test
	public void makeSureBufferUpdateEnableAndTrucateAreTrue() {
		// if this is false, then we cannot add items to the plot data buffers.
		Assert.assertEquals(PlotDataManager.DATA_SET_ENABLE_UPDATE_STATE, true);
		// if this is false, the we will not truncate plot local data buffers
		// when they become full. 
		Assert.assertEquals(PlotDataManager.DATA_SET_BUFFER_TRUNCATE_STATE, true);
	}

	@Test
	public void testInformResizeEvent() {
		PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
		build();
		PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();

		TestPlotDataManagerImplementation  dm = new TestPlotDataManagerImplementation(testPlot);
		testPlot.setPlotDataManager(dm);
		dm.resizeAndReloadCalled = false;

		// Fire the resize event
		dm.informResizeEvent();

		try {
			Thread.sleep(PlotConstants.RESIZE_TIMER + 2000);
		} catch (Exception e) {

		}
		Assert.assertTrue(dm.resizeAndReloadCalled);
	}

	@Test
	public void testScrunch() throws Exception {
		final long[] time = new long[1];
		PlotViewManifestation manifestation = Mockito.mock(PlotViewManifestation.class);
		Mockito.when(manifestation.getCurrentMCTTime()).thenAnswer(new Answer<Long>() {
			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				return time[0];
			}
		});
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(100);
		settings.setMaxTime(200);
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		PlotView plot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		plot.setManifestation(manifestation);
		PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();
		JPanel plotPanel = plot.getPlotPanel();

		final JFrame frame = new JFrame("testScrunch");
		frame.add(plotPanel);

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.pack();
				frame.setVisible(true);
			}
		});

		try {
			testPlot.addDataSet("feed", Color.white);
			for(int i = 0; i < 10000; i++) {
				testPlot.addData("feed", i, Math.sin(i / 10.0));
				time[0] = i;
			}
			Method m = testPlot.getPlotAbstraction().getClass().getDeclaredMethod("timeReachedEnd", new Class[0]);
			m.setAccessible(true);
			m.invoke(testPlot.getPlotAbstraction(), new Object[0]);
			CompressingXYDataset dataset = ((PlotDataSeries)testPlot.getPlotDataManager().getNamedDataSeries("feed")).dataset;
			int size = dataset.getPointCount();

			// We can have 4 points per pixel, and it can nearly double before recompressing, so we can have almost 8 points per pixel
			int max = testPlot.getPlotView().getContents().getWidth() * 8;
			Assert.assertTrue(size < max, "size = " + size + ", max = " + max);
		} finally {
			frame.dispose();
		}
	}


	@Test
	public void testScrunchProtect() {
		// not in scrunch
		GregorianCalendar before = new GregorianCalendar();
		GregorianCalendar start = new GregorianCalendar();
		GregorianCalendar end = new GregorianCalendar();
		end.add(Calendar.HOUR, 1);
		before.add(Calendar.HOUR, -1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(start.getTimeInMillis());
		settings.setMaxTime(end.getTimeInMillis());		
		
		PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
		plotSettings(settings).
		build();
		PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();

		Assert.assertFalse(((PlotDataManager)testPlot.getPlotDataManager()).scrunchProtect(end.getTimeInMillis()));
		Assert.assertFalse(((PlotDataManager)testPlot.getPlotDataManager()).scrunchProtect(before.getTimeInMillis()));

		settings = new PlotSettings();
		settings.setMinTime(start.getTimeInMillis());
		settings.setMaxTime(end.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		
		plot = new PlotView.Builder(PlotterPlot.class).
		plotSettings(settings).
		build();
		testPlot = (PlotterPlot) plot.returnPlottingPackage();

		Assert.assertFalse(((PlotDataManager)testPlot.getPlotDataManager()).scrunchProtect(end.getTimeInMillis()));
		Assert.assertTrue(((PlotDataManager)testPlot.getPlotDataManager()).scrunchProtect(before.getTimeInMillis()));
	}
	
	@Test 
	public void testSetupPlotBufferMinAndMaxTimesJump() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		
		plotPackage.setPlotAbstraction(plotView);
		
		((PlotDataManager)plotPackage.getPlotDataManager()).setupPlotBufferMinAndMaxTimes();
		// TODO: Confirm via reflection?
//		Assert.assertEquals(((PlotDataManager)plotPackage.getPlotDataManager()).plotDataBufferStartTime.getTimeInMillis(), plotPackage.getCurrentTimeAxisMin().getTimeInMillis() );
//		Assert.assertEquals(((PlotDataManager)plotPackage.getPlotDataManager()).plotDataBufferEndTime.getTimeInMillis(), plotPackage.getCurrentTimeAxisMax().getTimeInMillis() );
	}
	
	
	@Test 
	public void testSetupPlotBufferMinAndMaxTimesScrunch() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		
		plotPackage.setPlotAbstraction(plotView);

		//TODO: Confirm via reflection?
//		plotPackage.getPlotDataManager().setupPlotBufferMinAndMaxTimes();	
//		Assert.assertEquals(plotPackage.getPlotDataManager().plotDataBufferStartTime.getTimeInMillis(), plotPackage.getCurrentTimeAxisMin().getTimeInMillis() );
//		Assert.assertEquals(plotPackage.getPlotDataManager().plotDataBufferEndTime.getTimeInMillis(), plotPackage.getCurrentTimeAxisMax().getTimeInMillis() );
	}
	
	@Test 
	public void testSetupPlotBufferMinAndMaxTimesFixed() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		testPlot.getTimeAxisUserPin().setPinned(true);
		
		plotPackage.setPlotAbstraction(plotView);
		
		// TODO: Confirm via reflection?
//		plotPackage.getPlotDataManager().setupPlotBufferMinAndMaxTimes();	
//		Assert.assertEquals(plotPackage.getPlotDataManager().plotDataBufferStartTime.getTimeInMillis(), plotPackage.getCurrentTimeAxisMin().getTimeInMillis() );
//		Assert.assertEquals(plotPackage.getPlotDataManager().plotDataBufferEndTime.getTimeInMillis(), plotPackage.getCurrentTimeAxisMax().getTimeInMillis() );
	}
	
	@Test
	public void testSetupCompressionRatioNotUserInteraction() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		
		((PlotDataManager)plotPackage.getPlotDataManager()).setupCompressionRatio();
	}
	
	@Test
	public void testSetupCompressionRatioUserInteraction() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		
		((PlotDataManager)plotPackage.getPlotDataManager()).setupCompressionRatio();
	}
	
	@Test
	public void testSetupCompressionRatioNoCompression() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(false)
		.build();	
		
		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		
		((PlotDataManager)plotPackage.getPlotDataManager()).setupCompressionRatio();
	}
		
	
	@Test
	public void testCalculationOfCompressionRatioAndDataBufferSize() {
		// Create a plot, test the size of the buffer and compression ratio then resize the plot
		// window and make sure that both values change correctly. 
		
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);

		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		
		// Plot with a one hour span
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		testPlot.addDataSet("test", Color.white);

		testPlot.addDataSet("test2", "myDisplayName");
		
		JPanel plotPanel = testPlot.getPlotPanel();

		JFrame frame = new JFrame();
		frame.add(plotPanel);
		frame.pack();
		frame.setVisible(true);

		// allow time for frame to be shown, otherwise getSize methods will fail. 
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			Assert.assertFalse(true);
		}

		PlotterPlot plotPackage = (PlotterPlot) testPlot.returnPlottingPackage();
		((PlotDataManager)plotPackage.getPlotDataManager()).setupBufferSizeAndCompressionRatio();		
		
		// Test three expected results
		//1) plot buffer min max times
		
		//TODO: Check via reflection?
		//Assert.assertEquals(((PlotDataManager)plotPackage.getPlotDataManager()).plotDataBufferEndTime.getTimeInMillis(), maxTime.getTimeInMillis());
		
		//2) compression ratio
		Assert.assertEquals(((PlotDataManager)plotPackage.getPlotDataManager()).getNamedDataSeries("test").dataset.getCompressionScale(), (double) (maxTime.getTimeInMillis() - minTime.getTimeInMillis()) / plotPackage.getPlotTimeWidthInPixels());
		
		Assert.assertFalse(((PlotDataManager)plotPackage.getPlotDataManager()).isBufferRequestWaiting());
		
		plotPanel.setVisible(false);
		
		// Repeat the request, this time while the plot is in an updateFromCacheDataStream is in process. We expect a buffer request waiting;
		plotPackage.setUpdateFromCacheDataStreamInProcess(true);
		
		plotPackage.getPlotDataManager().resizeAndReloadPlotBuffer();		
		
		Assert.assertTrue(((PlotDataManager)plotPackage.getPlotDataManager()).isBufferRequestWaiting());		
	}
	
	@Test
	public void testInformBufferTruncationOccuredJumpPlot() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotAbstraction testPlot = new PlotView.Builder(TestQCPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		TestQCPlot plotPackage  = (TestQCPlot) testPlot.returnPlottingPackage();
		
		Assert.assertFalse(((PlotDataManager)plotPackage.getPlotDataManager()).hasScrunchTruncationOccured());
		((PlotDataManager)plotPackage.getPlotDataManager()).informBufferTrunctionEventOccured();
		Assert.assertFalse(((PlotDataManager)plotPackage.getPlotDataManager()).hasScrunchTruncationOccured());
		
	}
	
	@Test
	public void testInformBufferTruncatoinOccuredScrunchPlot() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		
		PlotAbstraction testPlot = new PlotView.Builder(TestQCPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	
		
		TestQCPlot plotPackage  = (TestQCPlot) testPlot.returnPlottingPackage();
		
		Assert.assertFalse(((PlotDataManager)plotPackage.getPlotDataManager()).hasScrunchTruncationOccured());
		((PlotDataManager)plotPackage.getPlotDataManager()).informBufferTrunctionEventOccured();
		Assert.assertTrue(((PlotDataManager)plotPackage.getPlotDataManager()).hasScrunchTruncationOccured());
		
	}
	
	@Test
	public void testinformUpdateFromLiveDataStreamCompleted() {
		 PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
		 build();
		 PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();
		
		TestPlotDataManagerImplementation  dm = new TestPlotDataManagerImplementation(testPlot);
		testPlot.setPlotDataManager(dm);
		dm.resizeAndReloadCalled = false;
		
		//TODO: Access these fields via reflection? Look for more external conclusions?
//		// Testing initial settings.
//		Assert.assertFalse(dm.scrunchBufferTruncationOccured);
//		Assert.assertFalse(dm.bufferRequestWaiting);
//		
//		
//		dm.informUpdateFromLiveDataStreamCompleted();
//		Assert.assertFalse(dm.scrunchBufferTruncationOccured);
//		Assert.assertFalse(dm.bufferRequestWaiting);
//		// resize should not be called. 
//		Assert.assertFalse(dm.resizeAndReloadCalled);
//		
//		// fake a dm.scrunchBufferTruncationOccured
//		dm.scrunchBufferTruncationOccured = true;
//		dm.resizeAndReloadCalled = false;
//		dm.informUpdateFromLiveDataStreamCompleted();
//		Assert.assertFalse(dm.scrunchBufferTruncationOccured);
//		Assert.assertFalse(dm.bufferRequestWaiting);
//		// resize should not be called. 
//		Assert.assertTrue(dm.resizeAndReloadCalled);
//		
//		// fake a dm.bufferRequestWaiting
//		dm.bufferRequestWaiting = true;
//		dm.resizeAndReloadCalled = false;
//		dm.informUpdateFromLiveDataStreamCompleted();
//		Assert.assertFalse(dm.scrunchBufferTruncationOccured);
//		Assert.assertFalse(dm.bufferRequestWaiting);
//		// resize should not be called. 
//		Assert.assertTrue(dm.resizeAndReloadCalled);
	}
	
	@Test
	public void testInformUpdateCacheDataStreamStartedAndInformUpdateCacheDataStreamCompleted() {
		 PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
		 build();
		 PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();
		
		TestPlotDataManagerImplementation  dm = new TestPlotDataManagerImplementation(testPlot);
		testPlot.setPlotDataManager(dm);
		
		//TODO:
//		Assert.assertTrue(dm.minMaxValueManager.isEnabled());
//		dm.informUpdateCacheDataStreamStarted();
//		Assert.assertFalse(dm.minMaxValueManager.isEnabled());
//	
//		Assert.assertFalse(dm.resizeAndReloadCalled);
//		dm.informUpdateCacheDataStreamCompleted();
//		Assert.assertTrue(dm.minMaxValueManager.isEnabled());
//		Assert.assertFalse(dm.resizeAndReloadCalled);
//		
//		// fake a dm.scrunchBufferTruncationOccured we now expect a resize and reload buffer call. 
//		dm.scrunchBufferTruncationOccured = true;
//		dm.informUpdateCacheDataStreamCompleted();
//		Assert.assertTrue(dm.minMaxValueManager.isEnabled());
//		Assert.assertTrue(dm.resizeAndReloadCalled);
	}


	@Test
	public void testUpdateOutOfRange() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);

		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	

		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();

		plotPackage.setPlotAbstraction(plotView);
		PlotDataManager plotDataManager = (PlotDataManager) plotPackage.getPlotDataManager();
		plotDataManager.setupPlotBufferMinAndMaxTimes();
		Mockito.when(plotView.getTimeAxis()).thenReturn(new Axis());

		plotDataManager.addDataSet("dataset", Color.white);
		plotDataManager.addData("dataset", new TreeMap<Long,Double>(Collections.singletonMap(maxTime.getTimeInMillis() + 1, 1.0)));
	}


	@Test
	public void testUpdateOutOfRangePinned() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);

		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	

		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();

		plotPackage.setPlotAbstraction(plotView);
		PlotDataManager plotDataManager = (PlotDataManager) plotPackage.getPlotDataManager();
		plotDataManager.setupPlotBufferMinAndMaxTimes();

		Mockito.when(plotView.getTimeAxis()).thenReturn(new Axis());
		Mockito.when(plotView.getPlotLineDraw()).thenReturn(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
		Axis timeAxis = plotPackage.getPlotAbstraction().getTimeAxis();
		Assert.assertNotNull(timeAxis);
		timeAxis.createPin().setPinned(true);

		plotDataManager.addDataSet("dataset", Color.white);
		plotDataManager.addData("dataset", new TreeMap<Long,Double>(Collections.singletonMap(maxTime.getTimeInMillis() + 1, 1.0)));
	}


	@Test
	public void testUpdateInRangePinned() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.HOUR, 1);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);

		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	

		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();

		plotPackage.setPlotAbstraction(plotView);
		PlotDataManager plotDataManager = (PlotDataManager) plotPackage.getPlotDataManager();
		plotDataManager.setupPlotBufferMinAndMaxTimes();

		Mockito.when(plotView.getTimeAxis()).thenReturn(new Axis());
		Axis timeAxis = plotPackage.getPlotAbstraction().getTimeAxis();
		Assert.assertNotNull(timeAxis);
		timeAxis.createPin().setPinned(true);

		plotDataManager.addDataSet("dataset", Color.white);
		plotDataManager.addData("dataset", new TreeMap<Long,Double>(Collections.singletonMap((minTime.getTimeInMillis() + maxTime.getTimeInMillis()) / 2, 1.0)));
	}


	@Test
	public void testOverlappingUpdates() {
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		minTime.setTimeInMillis(0);
		maxTime.setTimeInMillis(minTime.getTimeInMillis());
		maxTime.add(Calendar.SECOND, 1);

		PlotSettings settings = new PlotSettings();
		settings.setMinTime(minTime.getTimeInMillis());
		settings.setMaxTime(maxTime.getTimeInMillis());
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.isCompressionEnabled(true)
		.build();	

		PlotterPlot plotPackage  = (PlotterPlot) testPlot.returnPlottingPackage();
		plotPackage.getPlotView().getContents().setSize(1000, 1000);

		plotPackage.setPlotAbstraction(plotView);
		PlotDataManager plotDataManager = (PlotDataManager) plotPackage.getPlotDataManager();
		plotDataManager.setupPlotBufferMinAndMaxTimes();

		Mockito.when(plotView.getTimeAxis()).thenReturn(new Axis());

		plotDataManager.addDataSet("dataset", Color.white);
		PlotDataSeries series = plotDataManager.getDataSeries().get("dataset");
		series.dataset.setCompressionScale(1);
		long time1 = (minTime.getTimeInMillis() + maxTime.getTimeInMillis()) / 2;
		plotDataManager.addData("dataset", new TreeMap<Long, Double>(Collections.singletonMap(time1, 1.0)));

		TreeMap<Long, Double> map = new TreeMap<Long, Double>();
		map.put(time1 - 100, 2.0);
		map.put(time1, 2.0);
		map.put(time1 + 100, 2.0);
		plotDataManager.addData("dataset", map);

		DoubleData xData = series.dataset.getXData();
		DoubleData yData = series.dataset.getYData();
		Assert.assertEquals(xData.getLength(), 4);
		Assert.assertEquals(xData.get(0), time1 - 100.0);
		Assert.assertEquals(xData.get(1), (double)time1);
		Assert.assertEquals(xData.get(2), (double)time1);
		Assert.assertEquals(xData.get(3), time1 + 100.0);
		Assert.assertEquals(yData.getLength(), 4);
		Assert.assertEquals(yData.get(0), 2.0);
		Assert.assertEquals(yData.get(1), 1.0);
		Assert.assertEquals(yData.get(2), 2.0);
		Assert.assertEquals(yData.get(3), 2.0);
	}


	static class TestQCPlot extends PlotterPlot {
		
		int widthInPixels = 0;
		
		@Override
		int getPlotTimeWidthInPixels() {
		  return widthInPixels;	
		}
	}
	
	static class TestPlotDataManagerImplementation extends PlotDataManager {
		
		boolean resizeAndReloadCalled = false;
		
		public TestPlotDataManagerImplementation(PlotterPlot thePlot) {
			super(thePlot);
		}

		@Override
		public void resizeAndReloadPlotBuffer() {
			resizeAndReloadCalled = true;
		}
	}
}
