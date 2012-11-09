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

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestPlotViewFactory {
	
	@Mock
	private PlotViewManifestation plotManifestation;
	private AbbreviatingPlotLabelingAlgorithm labelingAlgorithm;
	
	@BeforeMethod
	void setUp() {
		MockitoAnnotations.initMocks(this);
		labelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	}

	private PlotConfiguration initSettings(long minTime, long maxTime, boolean pinAxis, TimeAxisSubsequentBoundsSetting subsequentSetting) {
		PlotConfiguration settings = new PlotSettings();
		settings.setAxisOrientationSetting(AxisOrientationSetting.X_AXIS_AS_TIME);
		settings.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		settings.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);

		settings.setTimeAxisSubsequentSetting(subsequentSetting);
		settings.setMinTime(minTime);
		settings.setMaxTime(maxTime);
		settings.setPinTimeAxis(pinAxis);
		return settings;
	}
	
	@DataProvider(name="plotViewFactoryData")
	protected Object[][] plotViewFactoryData() {
		
		return new Object[][] {
				new Object[] {
						initSettings(1,100,true,TimeAxisSubsequentBoundsSetting.JUMP), 1000, 1, 100, true
				},	
				new Object[] {
						initSettings(1,100,false,TimeAxisSubsequentBoundsSetting.JUMP), 10000, 1, 100, false
				},
				new Object[] {
						initSettings(1,100,true,TimeAxisSubsequentBoundsSetting.SCRUNCH), 1000, 1, 100, true
				},	
				new Object[] {
						initSettings(1,100,false,TimeAxisSubsequentBoundsSetting.SCRUNCH), 10000, 1, 10000, false
				}

		};
	}
	
	@Test(dataProvider="plotViewFactoryData")
	public void testCreatePlot(PlotSettings settings, long currentTime, long expectedMinTime, long expectedMaxTime, boolean pinned) {
		PlotView plotView = PlotViewFactory.createPlot(settings, currentTime, plotManifestation, 1, null, labelingAlgorithm, PlotConstants.DEFAULT_TIME_SYSTEM);
		Assert.assertEquals(plotView.getTimeAxisUserPin().isPinned(), pinned);
		Assert.assertEquals(plotView.getMinTime(), expectedMinTime);
		Assert.assertEquals(plotView.getMaxTime(), expectedMaxTime);		
	}
}
