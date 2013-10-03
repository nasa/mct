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

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.SpringLayout;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import plotter.xy.XYPlot;

public class TestLimitArrowIndicators {

	private static final int LIMIT_PLOT_NON_TIME_MIN = 5;
	private static final int LIMIT_PLOT_NON_TIME_MAX = 10;
	private static final int LIMIT_PLOT_SPAN = 2; // seconds.
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	
	@Mock
	private PlotterPlot mockPlot;
	
	@Mock
	private XYPlot plotView;
	
	@Mock
	private QCPlotObjects qcPlotObjects;

	@Mock
	private PlotViewManifestation mockPlotViewManifestation;

	@BeforeSuite
	public void setup() {
		MockitoAnnotations.initMocks(this);	
		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());
		Mockito.when(mockPlot.getPlotView()).thenReturn(plotView);
		Mockito.when(plotView.getLayout()).thenReturn(new SpringLayout());
	}
	
	@Test
	public void testMaxAlarmOnly() {
		// Create a simple in fix non time max and min modes with defined min/max bounds.
		long currentTime = 0;
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(currentTime + 3600000);
		settings.setMinTime(currentTime - 3600000);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a data set
		testPlot.addDataSet("DataSet1");
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Insert a value within bounds.
		testPlot.addData("DataSet1", currentTime, 5);
		
		// No alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Insert an value out of bounds but set it invalid
		// Insert a value within bounds.
		testPlot.addData("DataSet1", currentTime + 1000, Double.NaN);  
		
		// No alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		
		// Add a value out of max range.
		testPlot.addData("DataSet1", currentTime + 2000, 11);
		
		// Only max alarm should be raised. 
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		((PlotterPlot) testPlot.getSubPlots().iterator().next()).getLocalControlsManager().informAltKeyState(true);
		// Max alarm should still be raised. 
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
	}
	
	@Test
	public void testMinAlarmOnly() {
		// Create a simple in fix non time max and min modes with defined min/max bounds.
		long currentTime = 0;
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		settings.setMaxTime(currentTime + 3600000);
		settings.setMinTime(currentTime - 3600000);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a data set
		testPlot.addDataSet("DataSet1");
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Insert a value within bounds.
		testPlot.addData("DataSet1", currentTime, 1);
		
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a value out of min range.
		testPlot.addData("DataSet1", currentTime + 1000, -1);
		
		// Only max alarm should be raised. 
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.ALARM_RAISED);
		
	}
	
	@Test 
	void testBothAlarms() {
		// Create a simple in fix non time max and min modes with defined min/max bounds.
		GregorianCalendar now = new GregorianCalendar();
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setMaxNonTime(10.00);
		settings.setMinNonTime(0);
		settings.setMaxTime(now.getTimeInMillis() + 3600000);
		settings.setMinTime(now.getTimeInMillis() - 3600000);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
			
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a data set
		testPlot.addDataSet("DataSet1");
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		testPlot.addData("DataSet1", now.getTimeInMillis(), 1);
		
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a value out of min range.
		testPlot.addData("DataSet1", now.getTimeInMillis() + 1000, -1);
		
		// Only max alarm should be raised. 
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.ALARM_RAISED);
		
		// Add a value out of max range.
		testPlot.addData("DataSet1", now.getTimeInMillis() + 2000, 11);
		
		// Both alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.ALARM_RAISED);
	}
	
	
	@Test void testAlarmsDontGoOffWhenNotInFixedOrSemiFixedMode() {
		// Create a simple in fix non time max and min modes with defined min/max bounds.
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		settings.setMaxNonTime(10);
		settings.setMinNonTime(0);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	
		testPlot.setManifestation(mockPlotViewManifestation);
		
		testPlot.setCompressionEnabled(false);
		Assert.assertFalse(testPlot.isCompressionEnabled());
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a data set
		testPlot.addDataSet("DataSet1");
	
		// no alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Insert a value within bounds.
		testPlot.addData("DataSet1", System.currentTimeMillis(), 1);
		
		// No alarms should be raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a value out of min range.
		testPlot.addData("DataSet1", System.currentTimeMillis(), -1);
		
		// No alarms raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// Add a value out of max range.
		testPlot.addData("DataSet1", System.currentTimeMillis(), 11);
		
		// no alarms raised.
		Assert.assertEquals(testPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(testPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
	}
	
	@Test 
	void testAlarmTransitionSquenceMaxFixedMode() {
		GregorianCalendar now = new GregorianCalendar();
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setMaxNonTime(7.5);
		settings.setMinNonTime(0);
		settings.setMaxTime(now.getTimeInMillis() + 3600000);
		settings.setMinTime(now.getTimeInMillis() - 3600000);
		
		// Create a simple in fix non time max and min modes with defined min/max bounds.
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		testPlot.setManifestation(mockPlotViewManifestation);
		PlotterPlot plot = new PlotterPlot();
		plot.createChart( 
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
			            testPlot,
			            plotLabelingAlgorithm); 
	   
	   Assert.assertFalse(plot.isCompressionEnabled());
	   
	   plot.addDataSet("DataSet1", Color.red);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   
	   now.add(Calendar.MINUTE, 1);
	
	   // Add in limit value
	   plot.addData("DataSet1", now.getTimeInMillis(), 5.0);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   now.add(Calendar.MINUTE, 1);
	   // trigger max alarm. 
	   plot.addData("DataSet1", now.getTimeInMillis(), 10.0);
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);   
	   
		JFrame frame = new JFrame();
		frame.add(plot.getPlotComponent());
		frame.pack();
		frame.setVisible(true);
	   
	   now.add(Calendar.MINUTE, 1);
	   // trigger max alarm by pixel proximity
	   Point2D limitPointPhysical = new Point2D.Double(0,10.0);
	   plot.getPlotView().toPhysical(limitPointPhysical, limitPointPhysical);
	   Point2D valuePointLogical = new Point2D.Double(0,limitPointPhysical.getY()-0.5);
	   plot.getPlotView().toLogical(valuePointLogical, valuePointLogical);
	   plot.addData("DataSet1",now.getTimeInMillis(), valuePointLogical.getY());
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);  
	   
	}
	
	@Test
	void testAlarmTransitionSquenceMinFixedMode() {
		GregorianCalendar now = new GregorianCalendar();

		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10.01);
		settings.setMinNonTime(0);
		settings.setMaxTime(now.getTimeInMillis() + 3600000);
		settings.setMinTime(now.getTimeInMillis() - 3600000);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		
		testPlot.setManifestation(mockPlotViewManifestation);
		PlotterPlot plot = new PlotterPlot();
	   plot.createChart(
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
			            testPlot,
			            plotLabelingAlgorithm);
	   
	   Assert.assertFalse(plot.isCompressionEnabled());
	   
	   plot.addDataSet("DataSet1", Color.red);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   now.add(Calendar.MINUTE, 1);
	   // add inlimit value
	   plot.addData("DataSet1", now.getTimeInMillis(), 5);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   // trigger min alarm. 
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), -1);
	   
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
       plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);  
	   
		JFrame frame = new JFrame();
		frame.add(plot.getPlotComponent());
		frame.pack();
		frame.setVisible(true);

	   
	   // trigger min alarm by pixel proximity
	   Point2D limitPointPhysical = new Point2D.Double(0,0.0);
	   plot.getPlotView().toPhysical(limitPointPhysical, limitPointPhysical);
	   Point2D valuePointLogical = new Point2D.Double(0,limitPointPhysical.getY()+0.5);
	   plot.getPlotView().toLogical(valuePointLogical, valuePointLogical);
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), valuePointLogical.getY());
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   
       plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	}
	
	@Test 
	void testAlarmTransitionSquenceMaxSemiFixedMode() {
		// Create a simple in fix non time max and min modes with defined min/max bounds.
		GregorianCalendar now = new GregorianCalendar();
		
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setMaxNonTime(10.01);
		settings.setMinNonTime(0);
		settings.setMaxTime(now.getTimeInMillis() + 3600000);
		settings.setMinTime(now.getTimeInMillis() - 3600000);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();	
		testPlot.setManifestation(mockPlotViewManifestation);
		PlotterPlot plot = new PlotterPlot();
	   plot.createChart(new Font("Arial", Font.PLAIN, 1), 
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
			            testPlot,
			            plotLabelingAlgorithm);
	  

	   Assert.assertFalse(plot.isCompressionEnabled());
	   now.add(Calendar.MINUTE, 1);

	   plot.addDataSet("DataSet1", Color.red);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	  
	   // Add in limit value
	   plot.addData("DataSet1", now.getTimeInMillis(), 9);
	 
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   // trigger max alarm. 
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), 11);
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
       plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);   
	   
		JFrame frame = new JFrame();
		frame.add(plot.getPlotComponent());
		frame.pack();
		frame.setVisible(true);
	   
	   
	   // trigger max alarm by pixel proximity
	   Point2D limitPointPhysical = new Point2D.Double(0,plot.getMaxNonTime());
	   plot.getPlotView().toPhysical(limitPointPhysical, limitPointPhysical);
	   Point2D valuePointLogical = new Point2D.Double(0,limitPointPhysical.getY()-0.5);
	   plot.getPlotView().toLogical(valuePointLogical, valuePointLogical);
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), valuePointLogical.getY());
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
       plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMaxAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	}
	
	@Test
	void testAlarmTransitionSquenceMinSemiFixedMode() {
		GregorianCalendar now = new GregorianCalendar();


		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setMaxNonTime(10.00);
		settings.setMinNonTime(0);
		settings.setMaxTime(now.getTimeInMillis() + 3600000);
		settings.setMinTime(now.getTimeInMillis() - 3600000);
		
		PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
		.plotSettings(settings)
		.build();
		
		testPlot.setManifestation(mockPlotViewManifestation);
		PlotterPlot plot = new PlotterPlot();
	   plot.createChart(new Font("Arial", Font.PLAIN, 1), 
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
			            testPlot, 
			            plotLabelingAlgorithm);
	   
	   Assert.assertFalse(plot.isCompressionEnabled());
	   
	   plot.addDataSet("DataSet1", Color.red);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   // add inlimit value
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), 5);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   // trigger min alarm. 
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), -1);
	      
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
       plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM); 
	   
		JFrame frame = new JFrame();
		frame.add(plot.getPlotComponent());
		frame.pack();
		frame.setVisible(true);

	   
	   // trigger min alarm by pixel proximity
	   Point2D limitPointPhysical = new Point2D.Double(0,0.0);
	   plot.getPlotView().toPhysical(limitPointPhysical, limitPointPhysical);
	   Point2D valuePointLogical = new Point2D.Double(0,limitPointPhysical.getY()+0.5);
	   plot.getPlotView().toLogical(valuePointLogical, valuePointLogical);
	   now.add(Calendar.MINUTE, 1);
	   plot.addData("DataSet1", now.getTimeInMillis(), valuePointLogical.getY());
	   
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	   
       plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
	   
	   plot.getLimitManager().processMinAlertButtonPress();
	   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
	   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
	}
	
	
	@Test (expectedExceptions = AssertionError.class)
	void pushMaxButtonWhenNoAlarmRaised() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.build();	
		PlotterPlot plot = new PlotterPlot();
		   plot.createChart(new Font("Arial", Font.PLAIN, 1), 
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
				            testPlot, 
				            plotLabelingAlgorithm);
		   
		   Assert.assertFalse(plot.isCompressionEnabled());
		   
		   plot.addDataSet("DataSet1", Color.red);
		   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		   
		   // No push the max button and generate an assertion failure. 
		   plot.getLimitManager().processMaxAlertButtonPress();
	}
	
	@Test
	void testAlarmsOnAllFixedPlotSettings() {
		long currentTime = 0L;
		for(AxisOrientationSetting axisO : AxisOrientationSetting.values()) {
			for (XAxisMaximumLocationSetting xAxisMax: XAxisMaximumLocationSetting.values()) {
				for (YAxisMaximumLocationSetting  yAxisMax: YAxisMaximumLocationSetting.values()) {
					PlotSettings settings = new PlotSettings();
					settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
					settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
					settings.setMaxNonTime(10.00);
					settings.setMinNonTime(0);
					settings.setMaxTime(currentTime + 3600000);
					settings.setMinTime(currentTime - 3600000);
					
					PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
					.plotSettings(settings)
					.build();
					testPlot.setManifestation(mockPlotViewManifestation);
					PlotterPlot plot = new PlotterPlot();
					   plot.createChart(new Font("Arial", Font.PLAIN, 1), 
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
							            testPlot,
							            plotLabelingAlgorithm);
					  
					   Assert.assertFalse(plot.isCompressionEnabled());
					   
					   plot.addDataSet("DataSet1", Color.red);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   //Add inlimt value
					   plot.addData("DataSet1", currentTime + 1L, 5);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   // trigger min alarm. 
					   plot.addData("DataSet1", currentTime + 2L, -1);
					   
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_RAISED);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   plot.getLimitManager().processMinAlertButtonPress();
					   
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   plot.getLimitManager().processMinAlertButtonPress();
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
				       plot.getLimitManager().processMinAlertButtonPress();
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   plot.getLimitManager().processMinAlertButtonPress();
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);   
					   
					   // trigger max alarm. 
					   plot.addData("DataSet1", currentTime + 3L, 12);
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_RAISED);   
			    }
			}
		}	
	}
	
	
    @Test
	void testAlarmsOnAllPlotSemiFixedSettings() {
    	long currentTime = 0L;
		for(AxisOrientationSetting axisO : AxisOrientationSetting.values()) {
			for (XAxisMaximumLocationSetting xAxisMax: XAxisMaximumLocationSetting.values()) {
				for (YAxisMaximumLocationSetting  yAxisMax: YAxisMaximumLocationSetting.values()) {
					PlotSettings settings = new PlotSettings();
					settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
					settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
					settings.setMaxNonTime(10.00);
					settings.setMinNonTime(0);
					settings.setMaxTime(currentTime + 3600000);
					settings.setMinTime(currentTime - 3600000);
					
					PlotView testPlot = new PlotView.Builder(PlotterPlot.class)
					.plotSettings(settings)
					.build();
					testPlot.setManifestation(mockPlotViewManifestation);
					PlotterPlot plot = new PlotterPlot();
					   plot.createChart(new Font("Arial", Font.PLAIN, 1), 
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
							            testPlot,
							            plotLabelingAlgorithm);
					   
					   Assert.assertFalse(plot.isCompressionEnabled());
						
					   plot.addDataSet("DataSet1", Color.red);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   //Add inlimt value
					   plot.addData("DataSet1", currentTime + 0L, 5);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   // trigger min alarm. 
					   plot.addData("DataSet1", currentTime + 1L, -1);
					      
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   plot.getLimitManager().processMinAlertButtonPress();
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
				       plot.getLimitManager().processMinAlertButtonPress();
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
					   
					   plot.getLimitManager().processMinAlertButtonPress();
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);   
					   
					   // trigger max alarm. 
					   plot.addData("DataSet1", currentTime + 2L, 12);
					   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.ALARM_CLOSED_BY_USER);
					   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.ALARM_OPENED_BY_USER);   
			    }
			}
		}	
		
	}
	
	
	
	@Test (expectedExceptions = AssertionError.class)
	void pushMinButtonWhenNoAlarmRaised() {
		PlotAbstraction testPlot = new PlotView.Builder(PlotterPlot.class)
		.build();	
		PlotterPlot plot = new PlotterPlot();
		   plot.createChart(new Font("Arial", Font.PLAIN, 1), 
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
				            testPlot,
				            plotLabelingAlgorithm);
		   
		  Assert.assertFalse(plot.isCompressionEnabled());
		   
		   plot.addDataSet("DataSet1", Color.red);
		   Assert.assertEquals(plot.getDependentMaxAlarmState(), LimitAlarmState.NO_ALARM);
		   Assert.assertEquals(plot.getDependentMinAlarmState(), LimitAlarmState.NO_ALARM);
		   
		   // No push the max button and generate an assertion failure. 
		   plot.getLimitManager().processMinAlertButtonPress();
	}
	
	
	// The limit arrows need to disappear when the out of limit data scrolls of the visible
	// plot area. 
	@Test
    public void testLimitArrowsResetWhenOutOfLimitDataScrollsOffPlot() throws InterruptedException {
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar nowPlusSpan = new GregorianCalendar();
		nowPlusSpan.add(Calendar.MINUTE, LIMIT_PLOT_SPAN);
	
		PlotSettings settings = new PlotSettings();
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);
		settings.setMaxNonTime(LIMIT_PLOT_NON_TIME_MAX);
		settings.setMinNonTime(LIMIT_PLOT_NON_TIME_MIN);
		settings.setMaxTime(nowPlusSpan.getTimeInMillis());
		settings.setMinTime(now.getTimeInMillis());
		
		
		// Build a plot with fixed non time axis.
		PlotView limitPlot = new PlotView.Builder(PlotterPlot.class).
        plotSettings(settings).build();
		limitPlot.setManifestation(mockPlotViewManifestation);
		
		limitPlot.setCompressionEnabled(false);
		Assert.assertFalse(limitPlot.isCompressionEnabled());
		
		JFrame frame = new JFrame();
		frame.add(limitPlot.getPlotPanel());
		frame.pack();
		frame.setVisible(true);
		
		// Add a data set
		limitPlot.addDataSet("UpperDataSet", "\nTest Upper Data Set");	
		limitPlot.addDataSet("LowerDataSet", "\nTest Lower Data Set");	
		
		// Feed data point to limit plot within limit.
		limitPlot.addData("UpperDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MAX -1); 
		limitPlot.addData("LowerDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MIN +1);
		limitPlot.refreshDisplay();
		
		// No alarms should be raised.
		Assert.assertEquals(limitPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);
		Assert.assertEquals(limitPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);
		
		// move forward one seconds
		now.add(Calendar.MINUTE,1);
		
		// Feed an out of range value
		limitPlot.addData("UpperDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MAX + 1);
		limitPlot.addData("LowerDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MIN - 1);
		Assert.assertEquals(limitPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.ALARM_RAISED);
		Assert.assertEquals(limitPlot.getNonTimeMinAlarmState(0), LimitAlarmState.ALARM_RAISED);
		
		for (int i = 0; i < LIMIT_PLOT_SPAN + 1; i++) {
			now.add(Calendar.MINUTE,1);
			limitPlot.addData("UpperDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MAX - 1);
			limitPlot.addData("LowerDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MIN + 1);
		}

		Mockito.when(mockPlotViewManifestation.getCurrentMCTTime()).thenReturn(now.getTimeInMillis());
    	Thread.sleep(1500);

		now.add(Calendar.MINUTE,1);
		limitPlot.addData("UpperDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MAX - 1);
		limitPlot.addData("LowerDataSet", now.getTimeInMillis(), LIMIT_PLOT_NON_TIME_MIN + 1);

		// Alarms should no longer be raised as data has scrolled off plot.  	
		Assert.assertEquals(limitPlot.getNonTimeMaxAlarmState(0), LimitAlarmState.NO_ALARM);	
		Assert.assertEquals(limitPlot.getNonTimeMinAlarmState(0), LimitAlarmState.NO_ALARM);

		frame.dispose();
	}
	
	@Test
	public void testActionManagerCallsRefreshDisplayWhenPlotIsPaused() {
		Mockito.when(mockPlot.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		Mockito.when(mockPlot.getYAxisMaximumLocation()).thenReturn(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		Mockito.when(mockPlot.isNonTimeMaxFixed()).thenReturn(true);
		PlotLimitManager limitManager = new PlotLimitManager(mockPlot);
		limitManager.nonTimeMaxAlarm = LimitAlarmState.ALARM_CLOSED_BY_USER; 
	    limitManager.setupLimitButtons();
		ActionEvent e = new ActionEvent(limitManager.nonTimeMaxLimitButton, 0, null);
		
		Mockito.when(mockPlot.isPaused()).thenReturn(false);
		limitManager.actionPerformed(e);
		verify(mockPlot, never()).refreshDisplay();
		
		Mockito.when(mockPlot.isPaused()).thenReturn(true);
		limitManager.actionPerformed(e);
		verify(mockPlot, atMost(1)).refreshDisplay();
	}
	
}
