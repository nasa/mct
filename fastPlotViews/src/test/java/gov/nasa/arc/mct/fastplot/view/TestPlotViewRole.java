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
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.fastplot.access.PolicyManagerAccess;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;
import gov.nasa.arc.mct.fastplot.bridge.ShellPlotPackageImplementation;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.gui.NamingContext;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestPlotViewRole {
	
	@Mock 
	private AbstractComponent parentComponent;

	
	@Mock
	private FeedProvider feed1;
	
	@Mock
	private FeedProvider feed2;
	
	@Mock
	private FeedProvider feed3;
	
	@Mock
	private TimeService timeService;
	
	@Mock
	private AbstractComponent feed1Component;
	
	@Mock
	private AbstractComponent feed2Component;

	@Mock
	private AbstractComponent feed3Component;
	
	@Mock 
	private PolicyManager policyManager;
	@Mock private PersistenceProvider mockProvider;
	@Mock private Platform mockPlatform;
	
	
	private AbstractComponent mockComponent;
	
	PolicyManagerAccess access;
	ExecutionResult result;
	String valueFifty = "50.0";

	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(feed1Component.getCapability(FeedProvider.class)).thenReturn(feed1);
		Mockito.when(feed1Component.isLeaf()).thenReturn(true);
		Mockito.when(feed2Component.getCapability(FeedProvider.class)).thenReturn(feed2);
		Mockito.when(timeService.getCurrentTime()).thenReturn(System.currentTimeMillis());
		Mockito.when(feed1.getTimeService()).thenReturn(timeService);
		Mockito.when(feed2.getTimeService()).thenReturn(timeService);
		
	    Mockito.when(parentComponent.getComponents()).thenReturn(
                 Collections.<AbstractComponent> emptyList());
		
		access = new PolicyManagerAccess();
		access.setPolicyManager(policyManager);
		PolicyContext context = new PolicyContext();
        result = new ExecutionResult(context, true, "");
        Mockito.when(policyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(result);  
        mockComponent = new DummyComponent();
        new PlatformAccess().setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockProvider);
	}
	
	@AfterMethod
	public void teardown() {
		new PlatformAccess().setPlatform(null);
	}
		
	public static class TestersComponent extends AbstractComponent{
	    public TestersComponent(String id) {
	        setId(id);
	    }
	}
	
	
	@Test
	public void testSettingPersistance() {			
		    // Test SetupPlot
			final String anyTimeSystem = "anyTimeSystem";
	        MockitoAnnotations.initMocks(this);
			TestersComponent component = new TestersComponent("x");
			PlotViewManifestation originalPlotMan = new PlotViewManifestation(component, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
			
			GregorianCalendar now = new GregorianCalendar();
			GregorianCalendar nowPlus = new GregorianCalendar();
			nowPlus.add(Calendar.MINUTE, 1);
						
			originalPlotMan.setupPlot(new PlotSettings(AxisOrientationSetting.X_AXIS_AS_TIME,
					 anyTimeSystem,
                     TimeService.DEFAULT_TIME_FORMAT,
			         XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
			         YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
			         TimeAxisSubsequentBoundsSetting.JUMP, 
			         NonTimeAxisSubsequentBoundsSetting.AUTO, 
			         NonTimeAxisSubsequentBoundsSetting.AUTO, 
			         nowPlus.getTimeInMillis(), now.getTimeInMillis(), 150, 100, 0.01, 0.20, 0.20, true, false,
				 	 PlotConstants.DEFAULT_PLOT_LINE_DRAW,
					 PlotLineConnectionType.STEP_X_THEN_Y));
			
			// for coverage. 
			originalPlotMan.updateMonitoredGUI();
			originalPlotMan.updateMonitoredGUI(new AddChildEvent(nowPlus, feed1Component));
			originalPlotMan.updateMonitoredGUI(new RemoveChildEvent(nowPlus, feed1Component));
			
			PlotView thePlotView = originalPlotMan.getPlot();			
			PlotConfiguration settings = new PlotPersistenceHandler(originalPlotMan).loadPlotSettingsFromPersistance();
			PlotView secondPlotView =  PlotViewFactory.createPlotFromSettings(settings, 1, plotLabelingAlgorithm);
			
			// Should be different plots.
			Assert.assertNotSame(thePlotView, secondPlotView);
			
			originalPlotMan.setupPlot(new PlotSettings(AxisOrientationSetting.X_AXIS_AS_TIME, 
					 anyTimeSystem,
                     TimeService.DEFAULT_TIME_FORMAT,
			         XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
			         YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
			         TimeAxisSubsequentBoundsSetting.SCRUNCH, 
			         NonTimeAxisSubsequentBoundsSetting.AUTO, 
			         NonTimeAxisSubsequentBoundsSetting.AUTO, 
			         nowPlus.getTimeInMillis(), now.getTimeInMillis(), 150, 100, 0.01, 0.20, 0.20, true, false,
					 PlotConstants.DEFAULT_PLOT_LINE_DRAW,
					 PlotLineConnectionType.STEP_X_THEN_Y));
			
			originalPlotMan.updateMonitoredGUI();
			
			thePlotView = originalPlotMan.getPlot();			
			settings = new PlotPersistenceHandler(originalPlotMan).loadPlotSettingsFromPersistance();; 
			secondPlotView = PlotViewFactory.createPlotFromSettings(settings, 1, plotLabelingAlgorithm);
				
			// Should be different plots.
			Assert.assertNotSame(thePlotView, secondPlotView);
			
			
	}
	
	@Test (dataProvider="ingoresPredictiveTimeServiceTestCases")
	public void testIgnoresPredictiveTimeService(boolean p1, boolean p2, boolean p3, final int t) {
        MockitoAnnotations.initMocks(this);

		Mockito.when(feed1Component.getCapability(FeedProvider.class)).thenReturn(feed1);
		Mockito.when(feed2Component.getCapability(FeedProvider.class)).thenReturn(feed2);
		Mockito.when(feed3Component.getCapability(FeedProvider.class)).thenReturn(feed3);
		Mockito.when(feed1Component.isLeaf()).thenReturn(true);
		Mockito.when(feed2Component.isLeaf()).thenReturn(true);
		Mockito.when(feed3Component.isLeaf()).thenReturn(true);

		Mockito.when(feed1.getTimeService()).thenReturn(makeStaticTimeService(1));
		Mockito.when(feed2.getTimeService()).thenReturn(makeStaticTimeService(2));
		Mockito.when(feed3.getTimeService()).thenReturn(makeStaticTimeService(3));
		Mockito.when(feed1.getSubscriptionId()).thenReturn("feed1");
		Mockito.when(feed2.getSubscriptionId()).thenReturn("feed2");
		Mockito.when(feed3.getSubscriptionId()).thenReturn("feed3");

        Mockito.when(feed1.isPrediction()).thenReturn(p1);
        Mockito.when(feed2.isPrediction()).thenReturn(p2);
        Mockito.when(feed3.isPrediction()).thenReturn(p3);
        
        SwingUtilities.invokeLater(new Runnable() { 
        	public void run() {

        		TestersComponent component = new TestersComponent("x") {
        			@Override
        			public synchronized List<AbstractComponent> getComponents() {	
        				return Arrays.asList(feed1Component, feed2Component, feed3Component);
        			}
        		};

        		PlotViewManifestation plot;

        		plot = new PlotViewManifestation(component, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
        		Assert.assertEquals(plot.getCurrentMCTTime(), t); // First non-predictive;
        	}
        });

	}
	
	@DataProvider
	public Object[][] ingoresPredictiveTimeServiceTestCases() {
		return new Object[][]{
				{true,true,true,1},
				{true,false,false,2},
				{false,false,false,1}				
		};
	}
	
	private TimeService makeStaticTimeService(final long time) {
		return new TimeService() {
			@Override
			public long getCurrentTime() {
				return time;
			}
		};
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	@Test
	public void testUpdateFromDataFeed() throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {

		final ExtendedProperties viewProps = new ExtendedProperties();
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent,new ViewInfo(PlotViewManifestation.class,"", ViewType.OBJECT)) {
			@Override
			public ExtendedProperties getViewProperties() {
				return viewProps;
			}
		};

		panel.setPlot(new PlotView.Builder(ShellPlotPackageImplementation.class).build());
		ShellPlotPackageImplementation testPackage = (ShellPlotPackageImplementation) panel.getPlot().returnPlottingPackage();

		panel.getPlot().addDataSet("PUI1");
		panel.getPlot().addDataSet("PUI2");

		// Make feed Data
		Map<String, List<Map<String, String>>> theData = new Hashtable<String, List<Map<String, String>>>();

		List<Map<String, String>> dataSetA = new ArrayList<Map<String, String>>();
		List<Map<String, String>> dataSetB = new ArrayList<Map<String, String>>();

		// data set A two points
		Map<String, String> dataAPoint1 = new Hashtable<String, String>();
		dataAPoint1.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataAPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, valueFifty);
		dataAPoint1.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataAPoint1.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		RenderingInfo ri1 = new RenderingInfo(valueFifty, Color.BLACK, "X", Color.BLACK, true);
		Mockito.when(feed1.getRenderingInfo(Mockito.anyMap())).thenReturn(ri1);

		Map<String, String> dataAPoint2 = new Hashtable<String, String>();
		dataAPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataAPoint2.put(FeedProvider.NORMALIZED_VALUE_KEY, "86.2");
		dataAPoint2.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataAPoint2.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

		dataSetA.add(dataAPoint1);
		dataSetA.add(dataAPoint2);

		theData.put("PUI1", dataSetA);

		// data set B two points
		Map<String, String> dataBPoint1 = new Hashtable<String, String>();
		dataBPoint1.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataBPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, "25.0");
		dataBPoint1.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataBPoint1.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		RenderingInfo ri2 = new RenderingInfo("25.0", Color.BLACK, "X", Color.BLACK, true);
		Mockito.when(feed2.getRenderingInfo(Mockito.anyMap())).thenReturn(ri2);
		
		Map<String, String> dataBPoint2 = new Hashtable<String, String>();
		dataBPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataBPoint2.put(FeedProvider.NORMALIZED_VALUE_KEY, "110.9");
		dataBPoint2.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataBPoint2.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

		dataSetB.add(dataBPoint1);
		dataSetB.add(dataBPoint2);

		theData.put("PUI2", dataSetB);

		Mockito.when(feed1.getSubscriptionId()).thenReturn("PUI1");
		Mockito.when(feed2.getSubscriptionId()).thenReturn("PUI2");

		Field pda = PlotViewManifestation.class.getDeclaredField("plotDataAssigner");
		pda.setAccessible(true);
		PlotDataAssigner plotDataAssigner = (PlotDataAssigner) pda.get(panel);
		plotDataAssigner.getVisibleFeedProviders().add(feed1);
		plotDataAssigner.getVisibleFeedProviders().add(feed2);


		Assert.assertEquals(panel.getMaxFeedValue(), 0.0);
		Assert.assertEquals(panel.getMinFeedValue(), 0.0);
		// Push feed to plot.
		panel.updateFromFeed(theData);

		// Check data made it to the plot
		Map<String, ArrayList<Double>> plotDataSet =  testPackage.getDataSet();

		Assert.assertNotNull(plotDataSet);
		Assert.assertEquals(plotDataSet.size(), 2);
		Assert.assertTrue(plotDataSet.containsKey("pui2"));
		Assert.assertTrue(plotDataSet.containsKey("pui1"));
		ArrayList<Double> dataADataFromPlot = plotDataSet.get("pui1");
		ArrayList<Double> dataBDataFromPlot = plotDataSet.get("pui2");

		
		Assert.assertEquals(dataADataFromPlot.get(0), 50.0);


		Assert.assertEquals(dataBDataFromPlot.get(0), 25.0);

		panel.updateMonitoredGUI();

		GregorianCalendar now = new GregorianCalendar();
		Long mctTime = panel.getCurrentMCTTime();

		// Allow MCT time to be within a second of current time. This allows
		// time for the method call.
		Assert.assertTrue( mctTime <= now.getTimeInMillis() + 1000);

	}
		
	@Test
	public void testSetupChart() {
		TestersComponent component = new TestersComponent("x");
		PlotViewManifestation panel = new PlotViewManifestation(component, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		PlotView originalPlot = new PlotView.Builder(ShellPlotPackageImplementation.class).build();
		
		panel.setPlot(originalPlot);
		
		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		maxTime.setTimeInMillis(System.currentTimeMillis()+1);
		panel.setupPlot(new PlotSettings(AxisOrientationSetting.X_AXIS_AS_TIME, 
						 PlotConstants.DEFAULT_TIME_SYSTEM,
						 PlotConstants.DEFAULT_TIME_FORMAT,
				         XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, 
				         YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, 
				         TimeAxisSubsequentBoundsSetting.JUMP, 
				         NonTimeAxisSubsequentBoundsSetting.AUTO, 
				         NonTimeAxisSubsequentBoundsSetting.AUTO, 
				         maxTime.getTimeInMillis(), minTime.getTimeInMillis(), 0, 100, 0.05, 0.20, 0.20, true, false,
						 PlotConstants.DEFAULT_PLOT_LINE_DRAW,
					     PlotLineConnectionType.STEP_X_THEN_Y));
	}
		
	@Test 
	public void testUpdateFromDataFeedNoData() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.setPlot(new PlotView.Builder(ShellPlotPackageImplementation.class).build());
		
		// Robust to no data.
		panel.updateFromFeed(null);	
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testRobustToNoDataForAFeed() throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.setPlot(new PlotView.Builder(ShellPlotPackageImplementation.class).build());
		
		panel.getPlot().addDataSet("PUI1");
		
		// Robust to no data for feed.
		List<Map<String, String>> dataSetA = new ArrayList<Map<String, String>>();
		List<Map<String, String>> dataSetB = new ArrayList<Map<String, String>>();
		
		// data set A two points
		Map<String, String> dataAPoint1 = new Hashtable<String, String>();
		dataAPoint1.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataAPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, valueFifty);
		dataAPoint1.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");	
		dataAPoint1.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
	    RenderingInfo ri1 = new RenderingInfo(valueFifty, Color.BLACK, "X", Color.BLACK, true);
  	    Mockito.when(feed1.getRenderingInfo(Mockito.anyMap())).thenReturn(ri1);

		Map<String, String> dataAPoint2 = new Hashtable<String, String>();
		dataAPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataAPoint2.put(FeedProvider.NORMALIZED_VALUE_KEY, "86.2");
		dataAPoint2.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataAPoint2.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");


		Map<String, String> dataAPoint3 = new Hashtable<String, String>();
		dataAPoint3.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataAPoint3.put(FeedProvider.NORMALIZED_VALUE_KEY, "");
		dataAPoint3.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataAPoint3.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

		Map<String, String> dataAPoint4 = new Hashtable<String, String>();
		dataAPoint4.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataAPoint4.put(FeedProvider.NORMALIZED_VALUE_KEY, "86.2");
		dataAPoint4.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "");
		dataAPoint4.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

		Map<String, String> dataAPoint5 = new Hashtable<String, String>();
		dataAPoint5.put(FeedProvider.NORMALIZED_TIME_KEY, "");
		dataAPoint5.put(FeedProvider.NORMALIZED_VALUE_KEY, "86.2");
		dataAPoint5.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "true");
		dataAPoint5.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");

		dataSetA.add(dataAPoint1);
		dataSetA.add(dataAPoint2);
		dataSetA.add(dataAPoint3);
		dataSetA.add(dataAPoint4);

		// Make feed Data
 		Map<String, List<Map<String, String>>> theData = new Hashtable<String, List<Map<String, String>>>();
		theData.put("PUI1", dataSetA);
		theData.put("PUI2", dataSetB);

		Mockito.when(feed1.getSubscriptionId()).thenReturn("PUI1");
		Mockito.when(feed2.getSubscriptionId()).thenReturn("PUI2");

		Field pda = panel.getClass().getDeclaredField("plotDataAssigner");
		pda.setAccessible(true);
		PlotDataAssigner plotDataAssigner = (PlotDataAssigner) pda.get(panel);
		plotDataAssigner.getVisibleFeedProviders().add(feed1);
		plotDataAssigner.getVisibleFeedProviders().add(feed2);

		// Push feed to plot.
		panel.updateFromFeed(theData);

		// Check data made it to the plot
		ShellPlotPackageImplementation testPackage = (ShellPlotPackageImplementation) panel.getPlot().returnPlottingPackage(); 
		Map<String, ArrayList<Double>> plotDataSet = testPackage.getDataSet();

		Assert.assertEquals(plotDataSet.size(), 1);
		Assert.assertTrue(plotDataSet.containsKey("pui1"));
		Assert.assertFalse(plotDataSet.containsKey("pui2"));
		ArrayList<Double> dataADataFromPlot = plotDataSet.get("pui1");

		
		Assert.assertEquals(dataADataFromPlot.get(0), 50.0);
	}
	
	
	@Test (enabled = false)
	public void testUpdateDataAndThatUpdateFromFeedCachesWhileLockedOut() {
		PlotViewManifestation panel = new PlotViewManifestation(parentComponent,new ViewInfo(PlotViewManifestation.class,"",ViewType.CENTER));
		panel.setPlot(new PlotView.Builder(ShellPlotPackageImplementation.class).build());
		
		panel.getPlot().addDataSet("PUI1");
			
		List<Map<String, String>> dataSetAUpdateFromFeed = new ArrayList<Map<String, String>>();
		List<Map<String, String>> dataSetBUpdateData = new ArrayList<Map<String, String>>();
		
		// data set A two points
		Map<String, String> dataAPoint1 = new Hashtable<String, String>();
		dataAPoint1.put(FeedProvider.NORMALIZED_TIME_KEY, "2");
		dataAPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, valueFifty);
		dataAPoint1.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");	
		dataAPoint1.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		
		Map<String, String> dataAPoint2 = new Hashtable<String, String>();
		dataAPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "3");
		dataAPoint2.put(FeedProvider.NORMALIZED_VALUE_KEY, "86.2");
		dataAPoint2.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataAPoint2.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		
		dataSetAUpdateFromFeed.add(dataAPoint1);
		dataSetAUpdateFromFeed.add(dataAPoint2);
		
		// data set B two points
		Map<String, String> dataBPoint1 = new Hashtable<String, String>();
		dataBPoint1.put(FeedProvider.NORMALIZED_TIME_KEY, "0");
		dataBPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, "25.0");
		dataBPoint1.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataBPoint1.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		Map<String, String> dataBPoint2 = new Hashtable<String, String>();
		dataBPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		dataBPoint2.put(FeedProvider.NORMALIZED_VALUE_KEY, "110.9");
		dataBPoint2.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataBPoint2.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		
		dataSetBUpdateData.add(dataBPoint1);
		dataSetBUpdateData.add(dataBPoint2);
		
		Map<String, List<Map<String, String>>> updateFromFeedData = new Hashtable<String, List<Map<String, String>>>();
		updateFromFeedData.put("PUI1", dataSetAUpdateFromFeed);
		
		Map<String, List<Map<String, String>>> updateData = new Hashtable<String, List<Map<String, String>>>();
		updateData.put("PUI1", dataSetBUpdateData);
		
		Mockito.when(feed1.getSubscriptionId()).thenReturn("PUI1");
		
		    
	}

	
	@SuppressWarnings("serial")
	@Test
	public void testSynchronizeTime() {
		final ExtendedProperties viewProps = new ExtendedProperties();
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent,new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT)) {
			@Override
			public ExtendedProperties getViewProperties() {
				return viewProps;
			}
		};
		PlotView thePlot = new PlotView.Builder(PlotterPlot.class).build();
		
		AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
		final String baseDisplayName = "DSCU PUI1";
		plotLabelingAlgorithm.setCanvasPanelTitle("PUI123");
		plotLabelingAlgorithm.setPanelOrWindowTitle("DSCU");
		
		panel.setPlot(thePlot);
		thePlot.setManifestation(panel);
		Assert.assertEquals(panel.getPlot(), thePlot);
		
		panel.getPlot().addDataSet(baseDisplayName);
		
		List<Map<String, String>> dataSetAUpdateFromFeed = new ArrayList<Map<String, String>>();
		
		// data set A two points
		Map<String, String> dataAPoint1 = new Hashtable<String, String>();
		dataAPoint1.put(FeedProvider.NORMALIZED_TIME_KEY, "2");
		dataAPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, valueFifty);
		dataAPoint1.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");	
		dataAPoint1.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		
		Map<String, String> dataAPoint2 = new Hashtable<String, String>();
		dataAPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "3");
		dataAPoint2.put(FeedProvider.NORMALIZED_VALUE_KEY, "86.2");
		dataAPoint2.put(FeedProvider.NORMALIZED_IS_VALID_KEY, "TRUE");
		dataAPoint2.put(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY, "1");
		
		dataSetAUpdateFromFeed.add(dataAPoint1);
		dataSetAUpdateFromFeed.add(dataAPoint2);
		
		
		Map<String, List<Map<String, String>>> synchronizeTime = new Hashtable<String, List<Map<String, String>>>();
		synchronizeTime.put(baseDisplayName, dataSetAUpdateFromFeed);
		
		
		Mockito.when(feed1.getSubscriptionId()).thenReturn(baseDisplayName);

		panel.synchronizeTime(synchronizeTime, 0);
		
	}

	@Test 
	public void testRobustToMissingData() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.setPlot(new PlotView.Builder(ShellPlotPackageImplementation.class).build());



		// Robust to no data for feed.
		List<Map<String, String>> dataSetA = new ArrayList<Map<String, String>>();

		// data set A two points
		Map<String, String> dataAPoint1 = new Hashtable<String, String>();
		// no time key
		dataAPoint1.put(FeedProvider.NORMALIZED_VALUE_KEY, valueFifty);
		Map<String, String> dataAPoint2 = new Hashtable<String, String>();
		dataAPoint2.put(FeedProvider.NORMALIZED_TIME_KEY, "1");
		// no value key.
		dataSetA.add(dataAPoint1);
		dataSetA.add(dataAPoint2);

		// Make feed Data
		Map<String, List<Map<String, String>>> theData = new Hashtable<String, List<Map<String, String>>>();
		theData.put("PUI1", dataSetA);

		Set<String> feedIDS = new HashSet<String>();
		feedIDS.add("PUI1");


		// Push feed to plot.
		panel.updateFromFeed(theData);

		ShellPlotPackageImplementation testPackage = (ShellPlotPackageImplementation) panel.getPlot().returnPlottingPackage(); 

		// Check data made it to the plot
		Map<String, ArrayList<Double>> plotDataSet = testPackage.getDataSet();

		Assert.assertEquals(plotDataSet.size(), 0);
	}
	
	
	/* Test equals and hashcode methods *.
	 * 
	 * equals and hashCode must depend on the same set of "significant" fields. 
	 * You must use the same set of fields in both of these methods. 
	 * You are not required to use all fields. 
	 * For example, a calculated field that depends on others should very likely be 
	 * omitted from equals and hashCode.
	 */
	
	@Test 
	public void testEqualsAndHashCode() {
		
		// if two objects are equal, then their hashCode values must be equal as well
		PlotViewManifestation panelA = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		PlotViewManifestation panelPlotEqual = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		PlotViewManifestation panelNotEqualA = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		
		PlotView panelAsPlot = new PlotView.Builder(ShellPlotPackageImplementation.class).build();
		PlotView notPanelAsPlot = new PlotView.Builder(ShellPlotPackageImplementation.class).build();
		
		panelA.setPlot(panelAsPlot);
		panelPlotEqual.setPlot(panelAsPlot);
		panelNotEqualA.setPlot(notPanelAsPlot);
		
		// Two different manifestations.
		Assert.assertFalse(panelA.equals(panelPlotEqual));
	
		Assert.assertNotSame(panelA.hashCode(), panelPlotEqual.hashCode());
		
		// Check that do not equal a  general object
		Assert.assertFalse(panelA.equals(new Object()));
		
		// Equals one's self
		Assert.assertTrue(panelA.equals(panelA));
		
	}
	
	private Map<String,List<Map<String, String>>> generateDataSet(long...times) {
		List<Map<String,String>> dataSets = new ArrayList<Map<String,String>>();
		for (Long time : times) {
			Map<String,String> dataSet = new HashMap<String, String>();
			dataSets.add(dataSet);
			dataSet.put(FeedProvider.NORMALIZED_TIME_KEY, time.toString());
			dataSet.put(FeedProvider.NORMALIZED_VALUE_KEY, time.toString());
		}
		
		Map<String, List<Map<String,String>>> map = new HashMap<String, List<Map<String,String>>>();
		map.put("feedId", dataSets);
		return map;
	}
	
	@DataProvider(name="expandDataTest")
	public Object[][] createExpandDataTest() {
		return new Object[][] {
			new Object[] {generateDataSet(1L), 1L, 100L,new Long[] {1L}},
			new Object[] {generateDataSet(1L), 1L, 1001L, new Long[] {1L, 1001L}},
			new Object[] {generateDataSet(1L, 999L), 1L, 1001L, new Long[] {1L, 999L}},
			new Object[] {generateDataSet(1L), 1L, 3000L, new Long[] {1L, 1001L, 2001L}},
		};
	}
	
	@Test(dataProvider="expandDataTest")
	public void testExpandData(Map<String, List<Map<String, String>>> expandedData,
			long startTime, long endTime, Long[] expectedTimes) throws Exception {
		final FeedProvider fp = Mockito.mock(FeedProvider.class);
		final String feedId = "feedId";
		Mockito.when(fp.getSubscriptionId()).thenReturn(feedId);
		PlotViewManifestation manifestation = new PlotViewManifestation(mockComponent,new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT)) {
			private static final long serialVersionUID = 1L;

			@Override
			public Collection<FeedProvider> getVisibleFeedProviders() {
				return Collections.singleton(fp);
			}
			
			@Override
			public long getCurrentMCTTime() {
				return 7L;
			}
		};
		Mockito.when(fp.getTimeService()).thenReturn(timeService);

		Method m = PlotViewManifestation.class.getDeclaredMethod("expandData", Map.class, Long.TYPE, Long.TYPE);
		m.setAccessible(true);
		m.invoke(manifestation, expandedData,startTime, endTime);
		List<Map<String, String>> values = expandedData.get(feedId);
		Assert.assertEquals(values.size(), expectedTimes.length);
		int i = 0;
		for (Map<String,String> point : values) {
			Assert.assertEquals(expectedTimes[i++].toString(),point.get(FeedProvider.NORMALIZED_TIME_KEY));
		}
	}
	
	@Test
	public void testGetMaxAndMinTimesWorksWhenTimeDirectionChanged() {
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(0);
		settings.setMaxTime(100);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		
		PlotView plotMaxAtRight = new PlotView.Builder(PlotterPlot.class).plotSettings(settings).build();
		
		
		Assert.assertEquals(plotMaxAtRight.getMaxTime(), 100);
		Assert.assertEquals(plotMaxAtRight.getMinTime(), 0);
		
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
		
		PlotView plotMaxAtLeft = new PlotView.Builder(PlotterPlot.class).plotSettings(settings).build();
		
		Assert.assertEquals(plotMaxAtLeft.getMaxTime(), 100);
		Assert.assertEquals(plotMaxAtLeft.getMinTime(), 0);
		
		settings.setAxisOrientationSetting(AxisOrientationSetting.Y_AXIS_AS_TIME);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		
		PlotView plotTimeOnYMaxAtTop = new PlotView.Builder(PlotterPlot.class).plotSettings(settings).build();
		
		Assert.assertEquals(plotTimeOnYMaxAtTop.getMaxTime(), 100);
		Assert.assertEquals(plotTimeOnYMaxAtTop.getMinTime(), 0);

		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
		
		PlotView plotTimeOnYMaxAtBottom = new PlotView.Builder(PlotterPlot.class).plotSettings(settings).build();
		
		Assert.assertEquals(plotTimeOnYMaxAtBottom.getMaxTime(), 100);
		Assert.assertEquals(plotTimeOnYMaxAtBottom.getMinTime(), 0);	
	}
	
	@Test 
	public void testInitializeControlManifestation() {
		PlotViewManifestation pvm = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		
		JComponent controlMan = pvm.initializeControlManifestation();
		Assert.assertNotNull(controlMan);
	}
	
	
	private String naming;
	@Test
	public void testNamingContext() throws Exception{
		PlotViewManifestation manifestation = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		
		Field f = PlotViewManifestation.class.getDeclaredField("plotLabelingAlgorithm");
		f.setAccessible(true);
		f.set(manifestation, new AbbreviatingPlotLabelingAlgorithm() {
			@Override
			public void setCanvasContextTitleList(List<String> s) { if(!s.isEmpty()) naming = s.get(0); }
			public void setPanelContextTitleList(List<String> s) { if(!s.isEmpty()) naming = s.get(0); }
		});
		
		NamingContext nullContext = Mockito.mock(NamingContext.class);
		NamingContext blankContext = Mockito.mock(NamingContext.class);
		NamingContext specificContext = Mockito.mock(NamingContext.class);
		
		PropertyChangeEvent event = Mockito.mock(PropertyChangeEvent.class);
		
		Mockito.when(nullContext.getContextualName()).thenReturn(null);
		Mockito.when(blankContext.getContextualName()).thenReturn("");
		Mockito.when(specificContext.getContextualName()).thenReturn("Specific");
		
		manifestation.setNamingContext(nullContext);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, "");
		
		manifestation.setNamingContext(blankContext);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, manifestation.getManifestedComponent().getDisplayName());
		
		manifestation.setNamingContext(specificContext);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, "Specific");
		
		manifestation.setNamingContext(null);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, manifestation.getManifestedComponent().getDisplayName());

	}
	
	private static final String[] TEST_TIME_SYSTEMS = { "SYSA", "SYSB", "SYSC" };
	
	@DataProvider (name = "timeSystems")
	public Object[][] createTimeSystems() {
		return new Object[][] {
				{ TEST_TIME_SYSTEMS[0] } , { TEST_TIME_SYSTEMS[1] } , { TEST_TIME_SYSTEMS[2] }, { "NONE" }	
		};
	}
	
	@Test (dataProvider = "timeSystems")
	public void testMultipleFeedProviders(String setting) {
		PlotViewManifestation manifestation = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		
		manifestation.getViewProperties().setProperty(PlotConstants.TIME_SYSTEM_SETTING, setting);
		
		List<FeedProvider> fps = new ArrayList<FeedProvider>();
		for (String timeSys : TEST_TIME_SYSTEMS) {
			TimeService ts = Mockito.mock(TimeService.class);
			Mockito.when(ts.getTimeSystemId()).thenReturn(timeSys);
			FeedProvider fp = Mockito.mock(FeedProvider.class);
			Mockito.when(fp.getTimeService()).thenReturn(ts);			
			fps.add(fp);
		}
		
		AbstractComponent fpMockComponent = Mockito.mock(AbstractComponent.class);
		Mockito.when(fpMockComponent.getCapabilities(FeedProvider.class)).thenReturn(fps);
		Mockito.when(fpMockComponent.getCapability(FeedProvider.class)).thenReturn(null);
		FeedProvider fp = manifestation.getFeedProvider(fpMockComponent);
		
		if (setting.startsWith("SYS")) { // One of the expected test time systems
			Assert.assertEquals(fp.getTimeService().getTimeSystemId(), setting);
		} else { // One of the time systems that should fail.
			Assert.assertNull(fp);
		}
	}
	
	private static class DummyComponent extends AbstractComponent {
		public DummyComponent() {
			super();
		}
	}
}


