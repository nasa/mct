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
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import plotter.xy.LinearXYPlotLine;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

public class TestPlotDataSeries {
	@Mock
	private PlotViewManifestation mockPlotViewManifestation;
	
	@Mock 
	private PlotterPlot mockPlot;
	@Mock
	private XYPlot plotView;
	
	@Mock
	private PlotDataManager plotDataManger;
	
	@Mock
	private PlotLimitManager plotLimitManager;
	
	@Mock 
	private LegendEntry legendEntry;

	private PlotAbstraction plotAbstraction;
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	
	private static final double EPSILON = 0.1;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());
		Mockito.when(mockPlot.isCompressionEnabled()).thenReturn(false);
		Mockito.when(plotView.getContents()).thenReturn(new XYPlotContents());
		Mockito.when(mockPlot.getPlotView()).thenReturn(plotView);
		Mockito.when(mockPlot.getPlotDataManager()).thenReturn(plotDataManger);
		Mockito.when(mockPlot.getLimitManager()).thenReturn(plotLimitManager);
		Mockito.when(mockPlot.getMinTime()).thenReturn(Long.valueOf(0));
		Mockito.when(mockPlot.getPlotLineDraw()).thenReturn(PlotConstants.DEFAULT_PLOT_LINE_DRAW);
		
		plotAbstraction = new PlotView.Builder(PlotterPlot.class).build();
		((PlotView)plotAbstraction).setManifestation(mockPlotViewManifestation);
		
		Mockito.when(mockPlot.getPlotAbstraction()).thenReturn(plotAbstraction);
	}
	
	@Test
	public void testGettersAndSetters() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		timeTwo.add(Calendar.MINUTE, 10);
		PlotterPlot testPlot = new PlotterPlot();
		testPlot.createChart(
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
				false,
				true,
				true,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
		
		
		XYAxis xAxis = plotView.getXAxis();
		XYAxis yAxis = plotView.getYAxis();
		LinearXYPlotLine plot = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		Font font = new Font("Arial", Font.PLAIN, 10);
		LegendEntry legend = new LegendEntry(Color.white,  Color.white, font, new AbbreviatingPlotLabelingAlgorithm());
		legend.setPlot(plot);
		PlotDataSeries data = new PlotDataSeries(testPlot, "test", Color.white);
        data.setLegend(legend);		
		Assert.assertEquals(data.getColor(), Color.white);
		data.setPlottingColor(Color.black);
		Assert.assertEquals(data.getColor(), Color.black);
		Assert.assertEquals(data.getLegendEntry(), legend);
	}
	
	@Test
	public void testGetMinAndMaxValuesTimeOnX() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		timeTwo.add(Calendar.MINUTE, 30);
		PlotterPlot testPlot = new PlotterPlot();
		testPlot.createChart(
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
				false,
				true,
				true,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
		
		// Setup a data series.
		testPlot.addDataSet("dataSet1", Color.white);
		PlotDataSeries data = (PlotDataSeries) testPlot.getPlotDataManager().getNamedDataSeries("dataSet1");
		data.dataset.setCompressionOffset(timeOne.getTimeInMillis());
		data.dataset.setCompressionScale(1);
		
		Assert.assertEquals(data.getDataSetName(), "dataSet1");
		
		GregorianCalendar insertTime = new GregorianCalendar();
		insertTime.setTimeInMillis(timeOne.getTimeInMillis());
		
		// Store data into the process var
		data.getData().add(insertTime.getTimeInMillis(), 0.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 100.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 10.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), -5.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 1000.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), Double.NaN); 
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 100000.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), -700.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 100.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 2.0);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 99999999.0);
		
		GregorianCalendar startTime = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
		
		startTime.setTimeInMillis(timeOne.getTimeInMillis() - 1001);
		endTime.setTimeInMillis(timeOne.getTimeInMillis() + 1001);
		endTime.add(Calendar.MINUTE, 1);
		
		// Test max and min methods
		double[] maxAndTime = data.getMaxValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		double[] minAndTime = data.getMinValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		
		Assert.assertEquals(maxAndTime[0], 100.0);
		Assert.assertEquals(minAndTime[0], 0.0);
		
		endTime.setTimeInMillis(timeOne.getTimeInMillis());
		endTime.add(Calendar.MINUTE, 10);
		// Test max and min methods
		maxAndTime = data.getMaxValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		minAndTime = data.getMinValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		
		Assert.assertEquals(maxAndTime[0], 99999999.0);
		Assert.assertEquals(minAndTime[0], -700.0);
		
		// Scan a sub area of the time line to insure we're only testing inside the bounds. 
		
		startTime.setTimeInMillis(timeOne.getTimeInMillis());
		endTime.setTimeInMillis(timeOne.getTimeInMillis());
		startTime.add(Calendar.MINUTE, 3);
		endTime.add(Calendar.MINUTE, 5);

		maxAndTime = data.getMaxValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		minAndTime = data.getMinValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		Assert.assertEquals(maxAndTime[0], 1000.0);
		Assert.assertEquals(minAndTime[0], -5.0);
		
		startTime.setTimeInMillis(timeOne.getTimeInMillis());
		endTime.setTimeInMillis(timeOne.getTimeInMillis());
		startTime.add(Calendar.MINUTE, 4);
		endTime.add(Calendar.MINUTE, 4);
		maxAndTime = data.getMaxValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		minAndTime = data.getMinValue(endTime.getTimeInMillis(), startTime.getTimeInMillis());
		
		Assert.assertEquals(maxAndTime[0], 1000.0);
		Assert.assertEquals(minAndTime[0], 1000.0);
		
		// negative time range.
		maxAndTime = data.getMaxValue(1, 2);
		minAndTime = data.getMinValue(1, 2);
		Assert.assertEquals(maxAndTime[0], -Double.MAX_VALUE);
		Assert.assertEquals(minAndTime[0], Double.MAX_VALUE);
		
	}	
	
	@Test (enabled = false)
	public void testGetMinAndMaxValuesTimeOnY() {
		GregorianCalendar timeNow = new GregorianCalendar();
		GregorianCalendar startTime = new GregorianCalendar();
		GregorianCalendar  endTime = new GregorianCalendar();
		timeNow.add(Calendar.MINUTE, 15);
		endTime.add(Calendar.MINUTE, 30);
		
		
		PlotterPlot testPlot = new PlotterPlot();
		// Yaxis as time
		testPlot.createChart( 
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
				false,
				true,
				true,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
		
		// Add a data set
		testPlot.addDataSet("DataSet1", Color.red);
	    PlotDataSeries data = (PlotDataSeries) testPlot.getPlotDataManager().getNamedDataSeries("DataSet1");
	    data.dataset.setCompressionOffset(startTime.getTimeInMillis());
	    data.dataset.setCompressionScale(1);
	
		testPlot.addData("DataSet1", timeNow.getTimeInMillis(), 0);
		timeNow.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", timeNow.getTimeInMillis(), 100);
		timeNow.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", timeNow.getTimeInMillis(), -5);
		timeNow.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", timeNow.getTimeInMillis(), 120);
		timeNow.add(Calendar.MINUTE, 1);
		testPlot.addData("DataSet1", timeNow.getTimeInMillis(), 2);
	
		// Test max and min methods
	    
	    double[] maxAndTime = data.getMaxValue(endTime.getTimeInMillis(),startTime.getTimeInMillis());
		double[] minAndTime = data.getMinValue(endTime.getTimeInMillis(),startTime.getTimeInMillis());
	    
	    
		Assert.assertEquals(maxAndTime[0], 120.0);
		Assert.assertEquals(minAndTime[0], -5.0);
		
		// Just insure no exception thrown.
		data.toString();
	}
	
	@Test (dataProvider = "settingsProvider")
	public void TestSetupDataNonScrunchPlot(PlotSettings settings) {
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
		                       plotSettings(settings).
		                       build();
		PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();
		testPlot.addDataSet("test", Color.white);
		
		// Makesure that "TEST" data set was setup as we expected.
		PlotDataSeries dataSeries = (PlotDataSeries) testPlot.getPlotDataManager().getNamedDataSeries("test");
		Assert.assertNotNull(dataSeries);
	}
	
	@Test (dataProvider = "settingsProvider")
	public void TestSetupDataScrunchPlotWithCompression(PlotSettings settings) {
		// Switch to a scrunch plot
		//PlotSettings settings = new PlotSettings();
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
        			plotSettings(settings).
        			isCompressionEnabled(true).
        			build();
		PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();
		testPlot.addDataSet("TEST", Color.white);
		
		PlotDataSeries dataSeries = (PlotDataSeries) testPlot.getPlotDataManager().getNamedDataSeries("TEST");
	}
	
	@Test
	public void TestSetupDataScrunchPlotWithOutCompression() {
		// Switch to a scrunch plot
		PlotSettings settings = new PlotSettings();
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		PlotAbstraction plot = new PlotView.Builder(PlotterPlot.class).
        			plotSettings(settings).
        			isCompressionEnabled(false).
        			build();
		PlotterPlot testPlot = (PlotterPlot) plot.returnPlottingPackage();
		testPlot.addDataSet("TEST", Color.white);
		PlotDataSeries dataSeries = (PlotDataSeries) testPlot.getPlotDataManager().getNamedDataSeries("TEST");
	}
	
	@Test
	public void TestSetLinePlots() {
		PlotDataSeries data = new PlotDataSeries(mockPlot, "test", Color.white);
		LinearXYPlotLine slp = new LinearXYPlotLine(plotView.getXAxis(), plotView.getYAxis(), XYDimension.X);
		data.setLinePlot(slp);
		Assert.assertEquals(data.linePlot, slp);
		LinearXYPlotLine regressionLine = new LinearXYPlotLine(plotView.getXAxis(), plotView.getYAxis(), XYDimension.X);
		data.setRegressionLine(regressionLine);
		Assert.assertEquals(data.regressionLine, regressionLine);
		data.resetData(); // Reset should also remove regression line
		Assert.assertNull(data.getRegressionLine());
	}	
	
	@Test
	public void TestUpdateRegressionLine() {
		GregorianCalendar timeOne = new GregorianCalendar();
		GregorianCalendar timeTwo = new GregorianCalendar();
		timeTwo.setTimeInMillis(timeOne.getTimeInMillis());
		timeTwo.add(Calendar.MINUTE, 30);
		PlotterPlot testPlot = new PlotterPlot();
		plotAbstraction.setMinTime(timeOne.getTimeInMillis());
		plotAbstraction.setMaxTime(timeTwo.getTimeInMillis());
		testPlot.createChart( 
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
				false,
				true,
				true,
				plotAbstraction,
				plotLabelingAlgorithm);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
		
		// Setup a data series.
		testPlot.addDataSet("dataSet1", Color.white);
		PlotDataSeries data = (PlotDataSeries) testPlot.getPlotDataManager().getNamedDataSeries("dataSet1");
		data.dataset.setCompressionOffset(timeOne.getTimeInMillis());
		data.dataset.setCompressionScale(1);
		
		Assert.assertEquals(data.getDataSetName(), "dataSet1");
		
		GregorianCalendar insertTime = new GregorianCalendar();
		insertTime.setTimeInMillis(timeOne.getTimeInMillis());
		double[] xData = new double[4];
		// Store data into the process var
		data.getData().add(insertTime.getTimeInMillis(), 0.0);
		xData[0] = Long.valueOf(insertTime.getTimeInMillis()).doubleValue();
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 10.0);
		xData[1] = Long.valueOf(insertTime.getTimeInMillis()).doubleValue();
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 20.0);
		xData[2] = Long.valueOf(insertTime.getTimeInMillis()).doubleValue();
		Mockito.when(legendEntry.hasRegressionLine()).thenReturn(true);
		Mockito.when(legendEntry.getNumberRegressionPoints()).thenReturn(2);
		data.setLegend(legendEntry);
		data.updateRegressionLine();
		Assert.assertEquals(data.getRegressionLine().getXData().getLength(), 2);
		insertTime.add(Calendar.MINUTE, 1);
		data.getData().add(insertTime.getTimeInMillis(), 30.0);
		xData[3] = Long.valueOf(insertTime.getTimeInMillis()).doubleValue();
		data.updateRegressionLine();
		HighPrecisionLinearRegression lr = new HighPrecisionLinearRegression( xData, new double[]{0.0, 10.0, 20.0, 30.0});
		Assert.assertEquals(data.getRegressionLine().getXData().get(0), Long.valueOf(insertTime.getTimeInMillis()).doubleValue(), EPSILON);
		Assert.assertEquals(data.getRegressionLine().getYData().get(0), 30.0, EPSILON);
		Assert.assertEquals(data.getRegressionLine().getXData().get(1), Long.valueOf(timeTwo.getTimeInMillis()).doubleValue(), EPSILON);
		Assert.assertEquals(data.getRegressionLine().getYData().get(1), lr.calculateY(
				Long.valueOf(timeTwo.getTimeInMillis()).doubleValue()) +
		(30 - lr.calculateY(xData[3])), EPSILON);
	}
	
	
	private static final boolean[] BOOLEANS = { false, true };
	
	
	// Ensure that PloteDataSeries functions under a variety of settings
	@DataProvider (name = "settingsProvider")
	public Object[][] createSettings() {
		List<Object[]> l = new ArrayList<Object[]>();
		
		for (boolean ordinal : BOOLEANS) {
			for (boolean pin : BOOLEANS) {
				for (boolean drawMarkers : BOOLEANS) {
					for (AxisOrientationSetting orientation : AxisOrientationSetting.values()) {
						for (PlotLineConnectionType connection : PlotLineConnectionType.values()) {
							for (XAxisMaximumLocationSetting xMax : XAxisMaximumLocationSetting.values()) {
								for (YAxisMaximumLocationSetting yMax : YAxisMaximumLocationSetting.values()) {
									if (orientation != AxisOrientationSetting.Z_AXIS_AS_TIME) { // Not compatible with PlotterPlot
										PlotSettings p = new PlotSettings();
										p.setOrdinalPositionForStackedPlots(ordinal);
										p.setPinTimeAxis(pin);
										p.setPlotLineDraw(new PlotLineDrawingFlags(true, drawMarkers));
										p.setAxisOrientationSetting(orientation);
										p.setPlotLineConnectionType(connection);
										p.setXAxisMaximumLocation(xMax);
										p.setYAxisMaximumLocation(yMax);
										l.add(new Object[]{p});
									}
								}								
							}
						}
					}
				}
			}
			
		}
		Object[][] params = new Object[l.size()][];
		for (int i = 0 ; i < l.size(); i++) {
			params[i] = l.get(i);
		}
		return params;
	};
}
