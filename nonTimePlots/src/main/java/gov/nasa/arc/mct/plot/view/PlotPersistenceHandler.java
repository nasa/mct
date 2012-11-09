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
package gov.nasa.arc.mct.plot.view;

import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.plot.settings.LineSettings;
import gov.nasa.arc.mct.plot.settings.PlotConstants;
import gov.nasa.arc.mct.plot.settings.PlotSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotPersistenceHandler {
	private final static Logger logger = LoggerFactory.getLogger(PlotPersistenceHandler.class);
	
    private PlotView plotViewManifestation;
	
	PlotPersistenceHandler(PlotView supportedPlotViewManifestation) {
		plotViewManifestation = supportedPlotViewManifestation;
	}
	
	/**
	 * Load the settings for the manifestation from persistence.
	 * @return
	 */
	PlotSettings loadPlotSettingsFromPersistance() {
		PlotSettings settings = new PlotSettings();
		settings.loadFrom(plotViewManifestation);
		return settings;
	}
	
	public void persistPlotSettings(PlotSettings settings) {
		settings.persist(plotViewManifestation);
		plotViewManifestation.updateMonitoredGUI();
	}

	
	
	/**
	 * Retrieve persisted per-line plot settings (feed color assignments, line thicknesses, etc). 
	 * Each element of the returned list corresponds, in order, to the sub-plots displayed, 
	 * and maps subscription ID to a LineSettings object describing how the line is to be displayed. 
	 * @return the persisted line settings
	 */
	public List<Map<String, LineSettings>> loadLineSettingsFromPersistence() {
		List<Map<String, LineSettings>> lineSettingAssignments =
			new ArrayList<Map<String, LineSettings>>();

		String lineSettings = plotViewManifestation.getViewProperties().getProperty(PlotConstants.LINE_SETTINGS, String.class);
		if (lineSettings != null) {
			for (String plot : lineSettings.split("\n")) {
				Map<String, LineSettings> settingsMap = new HashMap<String, LineSettings>();
				
				for (String line : plot.split("\t")) {
					LineSettings settings = new LineSettings();
					
					String[] tokens = line.split(" ");
					try {
						int i = 0;
						if (tokens.length > i) settings.setIdentifier      (                     tokens[i++] );
						if (tokens.length > i) settings.setColorIndex      (Integer.parseInt    (tokens[i++]));
						if (tokens.length > i) settings.setThickness       (Integer.parseInt    (tokens[i++]));
						if (tokens.length > i) settings.setMarker          (Integer.parseInt    (tokens[i++]));
						if (tokens.length > i) settings.setCharacter       (                     tokens[i++] );
						if (tokens.length > i) settings.setUseCharacter    (Boolean.parseBoolean(tokens[i++]));
						if (tokens.length > i) settings.setHasRegression   (Boolean.parseBoolean(tokens[i++]));
						if (tokens.length > i) settings.setRegressionPoints(Integer.parseInt    (tokens[i++]));
					} catch (Exception e) {
						logger.error("Could not parse plot line settings from persistence", e);
					}
					
					if (!settings.getIdentifier().isEmpty()) {
						settingsMap.put(settings.getIdentifier(), settings);
					}
				}
				
				lineSettingAssignments.add(settingsMap);
			}
		}
		
		/* Merge in color assignments, if specified */
		List<Map<String, Integer>> colorAssignments = getColorAssignments();
		for (int i = 0; i < Math.min(colorAssignments.size(), lineSettingAssignments.size()); i++) {
			Map<String, LineSettings> settingsMap = lineSettingAssignments.get(i);
			for (Entry<String, Integer> e : colorAssignments.get(i).entrySet()) {
				if (!settingsMap.containsKey(e.getKey())) { // Only override unspecified settings
					LineSettings settings = new LineSettings();
					settings.setIdentifier(e.getKey());
					settings.setColorIndex(e.getValue());
					settings.setMarker(e.getValue()); // Use same index for markers by default
					settingsMap.put(e.getKey(), settings);
				}
			}
		}
		
		return lineSettingAssignments;
	}
	
	private List<Map<String, Integer>> getColorAssignments() {
		String colorAssignmentString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.COLOR_ASSIGNMENTS, String.class);
		List<Map<String, Integer>> colorAssignments = new ArrayList<Map<String, Integer>>();
		if (colorAssignmentString != null) {	
			StringTokenizer allAssignmentTokens = new StringTokenizer(colorAssignmentString, "\n");

			while (allAssignmentTokens.hasMoreTokens()) {
				StringTokenizer colorAssignmentTokens = new StringTokenizer(allAssignmentTokens.nextToken(), "\t");

				Map<String, Integer> subPlotMap = new HashMap<String, Integer>();
				colorAssignments.add(subPlotMap);
				while (colorAssignmentTokens.hasMoreTokens()) {					
					String dataSet   = colorAssignmentTokens.nextToken();
					int colorIndex   = Integer.parseInt(colorAssignmentTokens.nextToken());

					subPlotMap.put(dataSet, colorIndex);
				}
			}
		}
		return colorAssignments;
	}

	private StringBuilder initializeChannelViewProperties(String channelType, String id, 
			StringBuilder lineSettingsBuilder, LineSettings settings) {
		
		if ( (channelType != null) && !channelType.isEmpty()) {
			String channelId = id.substring(0, id.indexOf("-") + 1) + channelType 
					+ PlotConstants.SEPARATOR + id.substring(id.lastIndexOf(PlotConstants.SEPARATOR) + 1);
			lineSettingsBuilder.append(channelId);
		} else {
			lineSettingsBuilder.append(id);
		}
		
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(settings.getColorIndex());
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(settings.getThickness());
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(settings.getMarker()); //Marker
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(settings.getCharacter().replaceAll(" ", "_")); //Character
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(Boolean.toString(settings.getUseCharacter())); //Whether to use character as marker
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(Boolean.toString(settings.getHasRegression()));
		lineSettingsBuilder.append(' ');
		lineSettingsBuilder.append(settings.getRegressionPoints());
		lineSettingsBuilder.append('\t');
		
		return lineSettingsBuilder;
	}

	public void persistLineSettings(List<Map<String, LineSettings>> lineSettings) {
		StringBuilder lineSettingsBuilder = new StringBuilder(lineSettings.size() * 100);
		for (Map<String, LineSettings> subPlotMap : lineSettings) {
			for (Entry<String, LineSettings> entry : subPlotMap.entrySet()) {
				LineSettings settings = entry.getValue();
				
				String id = entry.getKey();
				// TODO:
//				if ((plotViewManifestation.getTimeSystemChoices() != null) && plotViewManifestation.getTimeSystemChoices().length > 1) {
//						String[] channelTypes = plotViewManifestation.getTimeSystemChoices();
//						for (int i=0; i < channelTypes.length; i++) {
//							lineSettingsBuilder = initializeChannelViewProperties(channelTypes[i].toLowerCase(), id, lineSettingsBuilder, settings);
//						}
//				} else {
					lineSettingsBuilder = initializeChannelViewProperties(null, id, lineSettingsBuilder, settings);
//				}
			}
			lineSettingsBuilder.append('\n');
		}
		
		ExtendedProperties viewProperties = plotViewManifestation.getViewProperties();
		
		viewProperties.setProperty(PlotConstants.LINE_SETTINGS, lineSettingsBuilder.toString());

		if (plotViewManifestation.getManifestedComponent() != null) {
			plotViewManifestation.getManifestedComponent().save();
		}
	}
}
