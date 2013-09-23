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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.view.Pinnable;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import plotter.xy.XYDimension;

public class TestPlotView {

	
	
	@Mock
	private PlotViewManifestation mockPlotViewManifestation;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());
	}
	
	
	@Test
	public void testSimplePlotCreationAndManiputlation() {
		// Create a simple plot
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class).build();
		testPlot.setManifestation(mockPlotViewManifestation);
		// Add a data set
		testPlot.addDataSet("DataSet1");
		// Add a value to the data set
		testPlot.addData("DataSet1", System.currentTimeMillis(), 10.0);
		// Insure data set names are not case sensitive
			
		GregorianCalendar time = new GregorianCalendar();
		testPlot.addData("daTaset1", System.currentTimeMillis(), 11.0);
		
	
		testPlot.addData("daTASET1", System.currentTimeMillis(), 20.1);
		
		Assert.assertTrue(testPlot.isKnownDataSet("daTasET1"));
		Assert.assertFalse(testPlot.isKnownDataSet("daTasET2"));

		testPlot.addDataSet("DataSetWithColor", Color.red);
		Assert.assertTrue(testPlot.isKnownDataSet("DataSetWithColor"));

		JPanel panel = testPlot.getPlotPanel();
		Assert.assertNotNull(panel);

		// requesting the panel refreshes should not cause an exception.
		testPlot.refreshDisplay();	
		String asString = testPlot.toString();
		Assert.assertNotNull(asString);
	}
		
	@Test
	public void testPlotBuilderWithInitialSettings() {
		// Step through all permutations of plot parameters.
		// Insures all paths are executed. We have extensive assertion checking in the production code
		// so this should uncover problems by exercising the paths.

		PlotSettings settings;
		settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		
		PlotAbstraction plot1 = new PlotView.Builder(PlotterPlot.class).plotName("plot1")
		.plotSettings(settings)
		.build();
		
		Assert.assertFalse(plot1.inTimeSyncMode());

		settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);		
		
		@SuppressWarnings("unused")
		PlotAbstraction plot2 = new PlotView.Builder(PlotterPlot.class).plotName("plot2")
		.plotSettings(settings)
		.build();

		settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);		
		
		@SuppressWarnings("unused")
		PlotAbstraction plot3 = new PlotView.Builder(PlotterPlot.class)		
		.plotSettings(settings)
		.build();

		settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);		
		
		@SuppressWarnings("unused")
		PlotAbstraction plot4 = new PlotView.Builder(PlotterPlot.class)		
		.plotSettings(settings)
		.build();
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public void createPlotWithInvalidTimeAxisSpecification() {	
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar future = new GregorianCalendar();
		future.add(Calendar.SECOND, 1);

		PlotSettings settings = new PlotSettings();
		settings.setMinTime(future.getTimeInMillis());
		settings.setMaxTime(now.getTimeInMillis());
		
		@SuppressWarnings("unused")
		PlotAbstraction plot4 = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
	}
	
	@Test (expectedExceptions = IllegalArgumentException.class)
	public void testNullManifestationCausesException() {
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class).build();
		testPlot.setManifestation(null);
	}


	@Test
	public void addMoreDataSetsThanWeHaveColors() {
		// Create a simple plot
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class).build();
		// Add more data sets than there are colors. No exceptions should be thrown.
		for (int i=0; i < PlotLineColorPalette.getColorCount() + 1; i++) {
			testPlot.addDataSet("dataset" + i);			
		}
	}

	@Test (expectedExceptions = IllegalArgumentException.class)
	public void testOperationOnUndefinedDataSeries() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class).build();

		// Add data item without defining its data set should throw and exception.
		testPlot.addData("Undefined data set", System.currentTimeMillis(), 10.0);		
	}

	@Test
	public void testPlotMatchSettings(){		
		
		PlotConfiguration plotSettings = new PlotSettings();
		// Create a second set of settings with defaults...
		PlotConfiguration otherPlotSettings = new PlotSettings();
		// ...but explicitly make sure min/max times match 
		// (these are defined relative to "now", resulting in intermittent test failures otherwise)
		otherPlotSettings.setMinTime(plotSettings.getMinTime());
		otherPlotSettings.setMaxTime(plotSettings.getMaxTime());
		PlotView basePlot = new PlotView.Builder(PlotterPlot.class).plotSettings(otherPlotSettings).build();
		
		Assert.assertTrue(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		plotSettings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		plotSettings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		plotSettings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setNonTimeAxisSubsequentMinSetting(PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_SUBSEQUENT_SETTING);
		plotSettings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setNonTimeAxisSubsequentMaxSetting(PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_SUBSEQUENT_SETTING);
		plotSettings.setMaxTime(basePlot.getMaxTime()+10);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setMaxTime(basePlot.getMaxTime());
		plotSettings.setMinTime(basePlot.getMinTime() + 10);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setMinTime(basePlot.getMinTime());
		plotSettings.setMaxNonTime(PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_VALUE + 1);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setMaxNonTime(PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_VALUE);
		plotSettings.setMinNonTime(PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_VALUE + 1);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setMinNonTime(PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_VALUE);
		plotSettings.setTimePadding(PlotConstants.DEFAULT_TIME_AXIS_PADDING + 1);   
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setTimePadding(PlotConstants.DEFAULT_TIME_AXIS_PADDING);  
		plotSettings.setNonTimeMaxPadding(PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MAX + 1);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setNonTimeMaxPadding(PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MAX);
		plotSettings.setNonTimeMinPadding(PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MIN+ 1);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));
		
		plotSettings.setNonTimeMinPadding(PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MIN);
		plotSettings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		Assert.assertFalse(basePlot.plotMatchesSetting(plotSettings));

		plotSettings.setYAxisMaximumLocation(PlotConstants.DEFAULT_Y_AXIS_MAX_LOCATION_SETTING);
		Assert.assertTrue(basePlot.plotMatchesSetting(plotSettings));
	
	}
	
	
	@Test
	public void testBuilder() {
		// For coverage but the test is not worthless due 
		// to the number of assertions in the code.

		for(AxisOrientationSetting axisO : AxisOrientationSetting.values()) {
			for (XAxisMaximumLocationSetting xAxisMax: XAxisMaximumLocationSetting.values()) {
				for (YAxisMaximumLocationSetting  yAxisMax: YAxisMaximumLocationSetting.values()) {
					for (TimeAxisSubsequentBoundsSetting timeSubsequent: TimeAxisSubsequentBoundsSetting.values()) {
						for (NonTimeAxisSubsequentBoundsSetting nonTimeMinSubsequent: NonTimeAxisSubsequentBoundsSetting.values()) {
							for (NonTimeAxisSubsequentBoundsSetting nonTimeMaxSubsequent: NonTimeAxisSubsequentBoundsSetting.values()) {
								PlotSettings settings = new PlotSettings();
								settings.setAxisOrientationSetting(axisO);
								settings.setYAxisMaximumLocation(yAxisMax);
								settings.setXAxisMaximumLocation(xAxisMax);
								settings.setTimeAxisSubsequentSetting(timeSubsequent);
								settings.setNonTimeAxisSubsequentMinSetting(nonTimeMinSubsequent);
								settings.setNonTimeAxisSubsequentMaxSetting(nonTimeMaxSubsequent);
								settings.setTimePadding(0.05);
								settings.setMinNonTime(0);
								settings.setMaxNonTime(10);
								
								
								PlotAbstraction coverage = new PlotView.Builder(PlotterPlot.class)
								.plotName("name")
								.timeAxisFontSize(10)
								.timeAxisFont(new Font("Arial", Font.PLAIN, 10))
								.plotLineThickness(1)
								.plotBackgroundFrameColor(Color.white)
								.plotAreaBackgroundColor(Color.white)
								.timeAxisIntercept(0)
								.timeAxisColor(Color.white)
								.timeAxisTextColor(Color.white)
								.timeAxisDateFormat("0000")
								.nonTimeAxisColor(Color.white)
								.gridLineColor(Color.white)
								.minSamplesForAutoScale(10)
								.build();	

								coverage.addDataSet("coverage");

								String asString = coverage.toString();
								Assert.assertNotNull(asString);
							}
						}
					}
				}
			}
		}
	}


	@Test (enabled=false) // Partially moved to TestKeyEventDispatcher
	public void testKeyListener() {
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class).build();
		testPlot.setManifestation(mockPlotViewManifestation);
		PlotterPlot plot = (PlotterPlot) testPlot.getSubPlots().get(0);

		JFrame frame = new JFrame();
		frame.getContentPane().add(testPlot.getPlotPanel());
		frame.setVisible(true);
		try {
			plot.getPlotActionListener().mouseOutsideOfPlotArea = false;
			JComponent panel = plot.getPlotComponent();
			KeyEvent ctrlDown = new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL,
					KeyEvent.CHAR_UNDEFINED);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(ctrlDown);
			Assert.assertTrue(testPlot.isPinned());

			KeyEvent ctrlUp = new KeyEvent(panel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_CONTROL,
					KeyEvent.CHAR_UNDEFINED);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(ctrlUp);
			Assert.assertFalse(testPlot.isPinned());

			KeyEvent altDown = new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.ALT_MASK, KeyEvent.VK_ALT,
					KeyEvent.CHAR_UNDEFINED);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(altDown);
			Assert.assertTrue(testPlot.isPinned());

			KeyEvent altUp = new KeyEvent(panel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ALT, KeyEvent.CHAR_UNDEFINED);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(altUp);
			Assert.assertFalse(testPlot.isPinned());

			KeyEvent shiftDown = new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.SHIFT_MASK, KeyEvent.VK_SHIFT,
					KeyEvent.CHAR_UNDEFINED);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().dispatchEvent(shiftDown);
			KeyEvent shiftUp = new KeyEvent(panel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED);
			panel.dispatchEvent(shiftUp);
		} finally {
			frame.dispose();
		}
	}


	@Test
	public void testPauseCount() {
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class).build();
		testPlot.setManifestation(mockPlotViewManifestation);

		Assert.assertFalse(testPlot.isPinned());
		Pinnable pin1 = testPlot.createPin();
		pin1.setPinned(true);
		Assert.assertTrue(testPlot.isPinned());
		Pinnable pin2 = testPlot.createPin();
		pin2.setPinned(true);
		Assert.assertTrue(testPlot.isPinned());
		pin1.setPinned(false);
		Assert.assertTrue(testPlot.isPinned());
		pin2.setPinned(false);
		Assert.assertFalse(testPlot.isPinned());
	}
	
	@DataProvider(name="axisTestData")
    Object[][]  generateAxisTestData() {
		return new Object[][] {
				new Object[] {TimeAxisSubsequentBoundsSetting.JUMP,0},
				new Object[] {TimeAxisSubsequentBoundsSetting.JUMP,1},
				new Object[] {TimeAxisSubsequentBoundsSetting.SCRUNCH,0},
				new Object[] {TimeAxisSubsequentBoundsSetting.SCRUNCH,1}
		};
    }
	
	@Test(dataProvider="axisTestData")
	public void testAxisShiftWhenTimeRunsOut(TimeAxisSubsequentBoundsSetting jumpSetting, double padding) throws Exception {
		// verify that axis shifting is invoked on all plots
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class).build();
		
		testPlot.setTimeAxisSubsequentSetting(jumpSetting);
		testPlot.setTimePadding(padding);		
		
		AbstractPlottingPackage plot = Mockito.mock(AbstractPlottingPackage.class);
		
		List<AbstractPlottingPackage> subPlots = Collections.singletonList(plot);
		
		Field subPlotField = testPlot.getClass().getDeclaredField("subPlots");
		subPlotField.setAccessible(true);
		subPlotField.set(testPlot, subPlots);
		
		PlotViewManifestation pvm = Mockito.mock(PlotViewManifestation.class);
		Mockito.when(pvm.getCurrentMCTTime()).thenReturn(8L);
		
		TimeXYAxis timeAxis = new TimeXYAxis(XYDimension.X);
		timeAxis.setStart(0);
		timeAxis.setEnd(7);
		Field timeAxisField = testPlot.getClass().getDeclaredField("plotTimeAxis");
		timeAxisField.setAccessible(true);
		timeAxisField.set(testPlot, timeAxis);
		
		
		Field plotUserField = testPlot.getClass().getDeclaredField("plotUser");
		plotUserField.setAccessible(true);
		plotUserField.set(testPlot, pvm);
		
		Method m = testPlot.getClass().getDeclaredMethod("timeReachedEnd", new Class[0]);
		m.setAccessible(true);
		m.invoke(testPlot, new Object[0]);
		
		Mockito.verify(plot, Mockito.atLeastOnce()).setTimeAxisStartAndStop(Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(plot, Mockito.atMost(1)).setTimeAxisStartAndStop(Mockito.anyLong(), Mockito.anyLong());
	}
	
}




