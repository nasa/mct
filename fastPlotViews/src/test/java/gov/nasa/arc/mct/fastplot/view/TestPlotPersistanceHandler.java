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

import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction.LineSettings;
import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction.PlotSettings;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotPersistanceHandler {
	@Mock
	private PlotViewManifestation manifestation;

	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(manifestation.getInfo()).thenReturn(new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		Mockito.when(manifestation.getViewProperties()).thenReturn(new ExtendedProperties());
	}


	@Test
	public void testMigrateFixed() {
		
		final String anyTimeSystem = "anyTimeSystem"; 
		
		PlotPersistanceHandler h = new PlotPersistanceHandler(manifestation);
		h.persistPlotSettings(AxisOrientationSetting.X_AXIS_AS_TIME, 
				anyTimeSystem,
                TimeService.DEFAULT_TIME_FORMAT,
				XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT,
				YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, TimeAxisSubsequentBoundsSetting.SCRUNCH, NonTimeAxisSubsequentBoundsSetting.FIXED,
				NonTimeAxisSubsequentBoundsSetting.FIXED, 0.0, 1.0, new GregorianCalendar(), new GregorianCalendar(), 0.0, 0.0, 0.0, true, false,
				PlotConstants.DEFAULT_PLOT_LINE_DRAW,
				PlotLineConnectionType.STEP_X_THEN_Y);
		manifestation.getViewProperties().setProperty(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, "FIXED");
		PlotSettings settings = h.loadPlotSettingsFromPersistance();

		Assert.assertEquals(settings.timeAxisSubsequent, TimeAxisSubsequentBoundsSetting.JUMP);
		Assert.assertTrue(settings.pinTimeAxis);
	}

	@Test
	public void testLineSettings() {
		String    name      = "name";
		int []    numbers   = { 0,   1,   2,  3 };
		String[]  strings   = { "a", "b", "c"   };
		boolean[] truth     = { false, true     };
		
		PlotPersistanceHandler h = new PlotPersistanceHandler(manifestation);
		
		List<Map<String, LineSettings>> settingsToPersist = new ArrayList<Map<String,LineSettings>>();
		
		// Try a lot of combinations		
		for (int color : numbers)
		for (int thickness : numbers)
		for (int marker : numbers)
		for (String character : strings)
		for (boolean useCharacter : truth)
		for (boolean hasRegression : truth)
		for (int points : numbers) {		
			LineSettings source = new LineSettings();
			source.setColorIndex(color);
			source.setThickness(thickness);
			source.setMarker(marker);
			source.setCharacter(character);
			source.setUseCharacter(useCharacter);
			source.setHasRegression(hasRegression);
			source.setRegressionPoints(points);
			
			Map<String, LineSettings> map = new HashMap<String, LineSettings>();
			map.put(name, source);
			settingsToPersist.add(map);
		}					
									
		h.persistLineSettings(settingsToPersist);
		
		List<Map<String, LineSettings>> persistedSettings = h.loadLineSettingsFromPersistence();
		int i = 0;
		
		for (Integer color : numbers)
		for (Integer thickness : numbers)
		for (Integer marker : numbers)
		for (String character : strings)
		for (boolean useCharacter : truth)
		for (boolean hasRegression : truth)
		for (Integer points : numbers) {
			LineSettings retrieved = persistedSettings.get(i++).get(name);
			Assert.assertEquals(retrieved.getColorIndex(), color);
			Assert.assertEquals(retrieved.getThickness(), thickness);
			Assert.assertEquals(retrieved.getMarker(), marker);
			Assert.assertEquals(retrieved.getCharacter(), character);
			Assert.assertEquals(retrieved.getUseCharacter(), useCharacter);
			Assert.assertEquals(retrieved.getHasRegression(), hasRegression);
			Assert.assertEquals(retrieved.getRegressionPoints(), points);
		}					
					

	}

	
}
