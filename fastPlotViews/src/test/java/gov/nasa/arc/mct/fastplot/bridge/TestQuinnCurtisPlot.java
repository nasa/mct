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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class TestQuinnCurtisPlot {

	@Mock
	PlotObserver mockObserver1;
	@Mock
	PlotObserver mockObserver2;
	
	// Data series names
	final static String DATA_SET_X_1 = "DataSet1_x_as_time";
	final static String DATA_SET_X_2 = "DataSet2_x_as_time";

	final static String DATA_SET_Y_1 = "DataSet1_y_as_time";
	final static String DATA_SET_Y_2 = "DataSet2_y_as_time";
	
	PlotterPlot originalPlotTimeOnXAxis;
	PlotterPlot originalPlotTimeOnYAxis;
	
	@Mock
	private PlotAbstraction plotAbstraction;

	@Mock
	private PlotViewManifestation mockPlotViewManifestation;

	@Mock
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	
	@BeforeSuite
	public void setupPlotsToCopyInTests() {
		MockitoAnnotations.initMocks(this);
    	Mockito.when(plotAbstraction.getTimeAxis()).thenReturn(new Axis());
		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());

		// Create a simple plot with time on X axis.
		originalPlotTimeOnXAxis = new PlotterPlot();
		originalPlotTimeOnXAxis.createChart(AxisOrientationSetting.X_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				new Font("Arial", Font.PLAIN, 1), 
				1, 
				Color.white, 
				Color.white, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.white, 
				1, 
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				0, 
				10,
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
				
		originalPlotTimeOnXAxis.setCompressionEnabled(false);
		
		// create s simple plot with time on y axis.
		originalPlotTimeOnYAxis = new PlotterPlot();
		originalPlotTimeOnYAxis.createChart(AxisOrientationSetting.Y_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				new Font("Arial", Font.PLAIN, 1), 
				1, 
				Color.white, 
				Color.white, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.white, 
				1, 
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				0, 
				10,
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
		
    	Mockito.when(plotAbstraction.getSubPlots()).thenReturn(Arrays.asList((AbstractPlottingPackage) originalPlotTimeOnXAxis, originalPlotTimeOnYAxis));

		originalPlotTimeOnYAxis.setCompressionEnabled(false);

		// Feed data to plots.
		originalPlotTimeOnXAxis.plotView.getXAxis().setSize(100, 1);
		originalPlotTimeOnXAxis.addDataSet(DATA_SET_X_1 , Color.red);
		originalPlotTimeOnXAxis.addDataSet(DATA_SET_X_2, Color.red);
		
		Assert.assertEquals(originalPlotTimeOnXAxis.getDataSetSize(),2);

		originalPlotTimeOnYAxis.plotView.getYAxis().setSize(1, 100);
		originalPlotTimeOnYAxis.addDataSet(DATA_SET_Y_1, Color.red);
		originalPlotTimeOnYAxis.addDataSet(DATA_SET_Y_2, Color.red);

		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		GregorianCalendar timeThree= new GregorianCalendar();
		timeTwo.add(Calendar.SECOND, 100);
		timeThree.add(Calendar.SECOND, 200);
		
		try {
		originalPlotTimeOnXAxis.addData(DATA_SET_X_1 , timeOne.getTimeInMillis(), 10.0);
		originalPlotTimeOnXAxis.addData(DATA_SET_X_1 , timeTwo.getTimeInMillis(), 11.0);
		originalPlotTimeOnXAxis.addData(DATA_SET_X_1 , timeThree.getTimeInMillis(), 20.1);

		originalPlotTimeOnYAxis.addData(DATA_SET_Y_1, timeOne.getTimeInMillis(), 25.0);
		originalPlotTimeOnYAxis.addData(DATA_SET_Y_1, timeTwo.getTimeInMillis(), 107.0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testcheckThatSetsAreEqualMethod() {
		
		PlotDataSeries dataSeries = originalPlotTimeOnXAxis.plotDataManager.dataSeries.get(DATA_SET_X_1);
		Assert.assertNotNull(dataSeries);
		Assert.assertNotNull(dataSeries.getData());
		
		// Feeding the same plot should return true!
		checkThatSetsContainSameProcessVars(originalPlotTimeOnXAxis.plotDataManager.dataSeries, originalPlotTimeOnXAxis.plotDataManager.dataSeries);
	}
	
	/**
	 * Test to make sure we're setting plot buffer to the correct size. 
	 */
	@Test
	void testPlotBufferSize() {
		PlotterPlot testPlot = new PlotterPlot();
		testPlot.createChart(AxisOrientationSetting.X_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				new Font("Arial", Font.PLAIN, 1), 
				1, 
				Color.white, 
				Color.white, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.white, 
				1, 
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				0, 
				(10 * PlotConstants.MILLISECONDS_IN_SECOND),
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		
	}
	
	@Test
	public void testGetMinMaxNonTimeValues() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		GregorianCalendar timeThree= new GregorianCalendar();
		timeTwo.add(Calendar.SECOND, 100);
		timeThree.add(Calendar.SECOND, 200);
		
		PlotterPlot testPlot = new PlotterPlot();
		testPlot.createChart(AxisOrientationSetting.X_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				new Font("Arial", Font.PLAIN, 1), 
				1, 
				Color.white, 
				Color.white, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.white, 
				1, 
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				timeOne.getTimeInMillis(), 
				timeThree.getTimeInMillis(),
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		// put data into the plot
		testPlot.addDataSet(DATA_SET_X_1 , Color.red);
		testPlot.addDataSet(DATA_SET_X_2, Color.red);
		
		testPlot.addDataSet(DATA_SET_Y_1, Color.red);
	
		testPlot.addData(DATA_SET_X_1 , timeOne.getTimeInMillis(), 10.0);
		testPlot.addData(DATA_SET_X_1 , timeTwo.getTimeInMillis(), 11.0);
		testPlot.addData(DATA_SET_X_1 , timeThree.getTimeInMillis(), 20.1);
     
		Assert.assertEquals(testPlot.getNonTimeMaxDataValueCurrentlyDisplayed(), 20.1);
		Assert.assertEquals(testPlot.getNonTimeMinDataValueCurrentlyDisplayed(), 10.0);	
	}
	
	@Test
	void testIsKnownDataSet() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		GregorianCalendar timeThree= new GregorianCalendar();
		timeTwo.add(Calendar.SECOND, 100);
		timeThree.add(Calendar.SECOND, 200);
		
		PlotterPlot testPlot = new PlotterPlot();
		testPlot.createChart(AxisOrientationSetting.X_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				new Font("Arial", Font.PLAIN, 1), 
				1, 
				Color.white, 
				Color.white, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.white, 
				1, 
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				timeOne.getTimeInMillis(), 
				timeThree.getTimeInMillis(),
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		testPlot.addDataSet(DATA_SET_X_1 , Color.red);
		
		Assert.assertTrue(testPlot.isKnownDataSet(DATA_SET_X_1));
		Assert.assertFalse(testPlot.isKnownDataSet("Foo"));
	}
	
	@Test
	void testGetTimeAxisScrollMode() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		GregorianCalendar timeThree= new GregorianCalendar();
		timeTwo.add(Calendar.SECOND, 100);
		timeThree.add(Calendar.SECOND, 200);
		
		PlotterPlot testXAsTimePlot = new PlotterPlot();
		testXAsTimePlot.createChart(AxisOrientationSetting.X_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				new Font("Arial", Font.PLAIN, 1), 
				1, 
				Color.white, 
				Color.white, 
				0, 
				Color.white, 
				Color.white, 
				Color.white, 
				"dd", 
				Color.black, 
				Color.white, 
				1, 
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				timeOne.getTimeInMillis(), 
				timeThree.getTimeInMillis(),
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		 Assert.assertEquals(testXAsTimePlot.getTimeAxisSubsequentSetting(), TimeAxisSubsequentBoundsSetting.JUMP);
		 
		 PlotterPlot testYAsTimePlot = new PlotterPlot();
			testYAsTimePlot.createChart(AxisOrientationSetting.Y_AXIS_AS_TIME, 
					PlotConstants.DEFAULT_TIME_SYSTEM,
	                PlotConstants.DEFAULT_TIME_FORMAT,
					XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
					YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
					TimeAxisSubsequentBoundsSetting.SCRUNCH,
					PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
					PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
					new Font("Arial", Font.PLAIN, 1), 
					1, 
					Color.white, 
					Color.white, 
					0, 
					Color.white, 
					Color.white, 
					Color.white, 
					"dd", 
					Color.black, 
					Color.white, 
					1, 
					0.5, 
					0.5,
					0.5,
					0, 
					10, 
					timeOne.getTimeInMillis(), 
					timeThree.getTimeInMillis(),
					false,
					true,
					true,
					true,
					PlotConstants.DEFAULT_PLOT_LINE_DRAW,
					PlotLineConnectionType.STEP_X_THEN_Y,
					plotAbstraction,
					plotLabelingAlgorithm);
			
			 Assert.assertEquals(testYAsTimePlot.getTimeAxisSubsequentSetting(),TimeAxisSubsequentBoundsSetting.SCRUNCH);	
	}
	
	
	/**
	 * Checks that data sets are equal. Throws JUnit assertion problems if they are not. 
	 * @param dataSeries1
	 * @param dataSeries2
	 */
	private void checkThatSetsContainSameProcessVars(Map<String, PlotDataSeries> dataSeries1, Map<String, PlotDataSeries> dataSeries2) {
		Set<String> dataSet1Keys = dataSeries1.keySet();
		Set<String> dataSet2Keys = dataSeries2.keySet();
		Assert.assertEquals(dataSet1Keys.size(), dataSet2Keys.size());
		for (String key1: dataSet1Keys) {
			Assert.assertTrue(dataSet2Keys.contains(key1));
		}
		
		
	}
	
	@Test 
	public void testPositionPanels() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		GregorianCalendar timeThree= new GregorianCalendar();
		timeTwo.add(Calendar.SECOND, 100);
		timeThree.add(Calendar.SECOND, 200);

		PlotterPlot testPlot = new PlotterPlot();
		testPlot.createChart(AxisOrientationSetting.X_AXIS_AS_TIME, 
				PlotConstants.DEFAULT_TIME_SYSTEM,
                PlotConstants.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				TimeAxisSubsequentBoundsSetting.JUMP,
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
				PlotConstants.NonTimeAxisSubsequentBoundsSetting.AUTO, 
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
				0.5, 
				0.5,
				0.5,
				0, 
				10, 
				timeOne.getTimeInMillis(), 
				timeThree.getTimeInMillis(),
				false,
				true,
				true,
				true,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		testPlot.setCompressionEnabled(false);

		// put data into the plot
		testPlot.addDataSet(DATA_SET_X_1 , Color.red, "AAAAAAAAAAAAA\nAAAAAAAAAAAAA\nAAAAAAAAAAAAA");
		testPlot.addData(DATA_SET_X_1 , timeOne.getTimeInMillis(), 10.0);
		testPlot.addData(DATA_SET_X_1 , timeTwo.getTimeInMillis(), 11.0);

		testPlot.getPlotPanel().setPreferredSize(new Dimension(1000,1000));
		JFrame frame = new JFrame();
		frame.add(testPlot.getPlotPanel());
		frame.pack();
		testPlot.calculatePlotAreaLayout();
		
		

		LegendManager legendPanel = testPlot.legendManager;

		// legend panel should be at its full size.
		Assert.assertEquals(legendPanel.getSize().getHeight(), legendPanel.getPreferredSize().getHeight()); 
		Assert.assertEquals(legendPanel.getSize().getWidth(), legendPanel.getPreferredSize().getWidth());

		// Shrink the plot a little until it runs into the legend and that the legend should shrink.  
		// Need some padding to make legend panel visible due to changes in status indicator code 
		frame.setSize((int) legendPanel.getPreferredSize().getWidth() + PlotConstants.MINIMUM_PLOT_WIDTH + PlotConstants.LOCAL_CONTROL_WIDTH + 20, frame.getHeight());
		// Give swing a chance to resize the panel. 
		try {
			Thread.sleep(500);
		} catch (Exception e) {

		}

		Assert.assertTrue(legendPanel.getPreferredSize().getHeight() == legendPanel.getSize().getHeight()); 
		Assert.assertTrue(legendPanel.getPreferredSize().getWidth() >= legendPanel.getSize().getWidth());
		Assert.assertFalse(legendPanel.isVisible());

		// Shrink the plot a little more it forces the legends to switch to invisible. 
		frame.setSize((int) legendPanel.getPreferredSize().getWidth() + PlotConstants.MINIMUM_PLOT_WIDTH - 100 , frame.getHeight());
		try {
			Thread.sleep(500);
		} catch (Exception e) {

		}
		Assert.assertFalse(legendPanel.isVisible());
	}

	
	@Test
	public void testAddDataOffFixedPlotIsIgnored() {
		// If we attempt to add data that is off the screen on a fixed plot, it will not be added.
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.timeVariableAxisMinValue(0)
		.timeVariableAxisMaxValue(10000)
		.isCompressionEnabled(true)
		.build();	
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.addDataSet("test", Color.white);
		
		PlotterPlot plot = (PlotterPlot) testPlot.returnPlottingPackage();
		testPlot.getTimeAxisUserPin().setPinned(true);
        PlotDataSeries data = plot.plotDataManager.dataSeries.get("test");
		int dataSetSize = data.getData().getPointCount();
		
		testPlot.addData("test", 2000000, 10);
		
		// Nothing Added as we're in fixed mode. 
		Assert.assertEquals(dataSetSize,  data.getData().getPointCount());
	}
	
	@Test
	public void plotNoDeteGetNonTimeAxisMaxAndMin() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.build();	
		
		PlotterPlot plot = (PlotterPlot) testPlot.returnPlottingPackage();
			
		Assert.assertEquals(plot.getCurrentNonTimeAxisMax() , PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_VALUE);
		Assert.assertEquals(plot.getCurrentNonTimeAxisMin() , PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_VALUE);
	}
	
	@Test
	public void testAddAndRemoveObservors() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class).build();
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		qcPlot.registerObservor(mockObserver1);
		qcPlot.registerObservor(mockObserver2);
		
		qcPlot.removeObserver(mockObserver1);

		// no exception should be generated.
		qcPlot.removeObserver(mockObserver1);
		qcPlot.removeObserver(mockObserver2);
	}
	
	@Test
	public void testToString() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class).build();
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		qcPlot.addDataSet(DATA_SET_X_1 , Color.red);
		
		String plotAsString = qcPlot.toString();
		Assert.assertTrue(plotAsString.contains("Compression enabled"));	
	}
	

	@Test
	public void testUpdateFromStreamStateTransitions() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class).build();
		PlotterPlot qcPlot = (PlotterPlot) testPlot.returnPlottingPackage();
		
		qcPlot.informUpdateFromLiveDataStreamStarted();
		Assert.assertTrue(qcPlot.isUpdateFromLiveDataStreamInProcess());
		
		qcPlot.informUpdateFromLiveDataStreamCompleted();
		Assert.assertFalse(qcPlot.isUpdateFromLiveDataStreamInProcess());
		
		qcPlot.informUpdateCachedDataStreamStarted();
		Assert.assertTrue(qcPlot.isUpdateFromCacheDataStreamInProcess());
		
		qcPlot.informUpdateCacheDataStreamCompleted();
		Assert.assertFalse(qcPlot.isUpdateFromCacheDataStreamInProcess());		 
	}
	
}
