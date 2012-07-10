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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotPersistanceHandler {
	private final static Logger logger = LoggerFactory.getLogger(PlotPersistanceHandler.class);
	
    private PlotViewManifestation plotViewManifestation;
	
	PlotPersistanceHandler(PlotViewManifestation supportedPlotViewManifestation) {
		plotViewManifestation = supportedPlotViewManifestation;
	}
	
	/**
	 * Load the settings for the manifestation from persistence.
	 * @return
	 */
	PlotSettings loadPlotSettingsFromPersistance() {

		PlotSettings settings = new PlotSettings();		

		try {
			settings.timeAxisSetting = Enum.valueOf(AxisOrientationSetting.class, plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_AXIS_SETTING, String.class).trim().toUpperCase());
		} catch (Exception e) {
			// No persisted settings for this plot.
			return settings;
		}
	
		String pinTimeAxisAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.PIN_TIME_AXIS, String.class);

		try {
			settings.xAxisMaximumLocation = Enum.valueOf(XAxisMaximumLocationSetting.class, plotViewManifestation.getViewProperties().getProperty(PlotConstants.X_AXIS_MAXIMUM_LOCATION_SETTING, String.class).trim().toUpperCase());
			settings.yAxisMaximumLocation = Enum.valueOf(YAxisMaximumLocationSetting.class, plotViewManifestation.getViewProperties().getProperty(PlotConstants.Y_AXIS_MAXIMUM_LOCATION_SETTING, String.class).trim().toUpperCase());
			String timeAxisSubsequent = plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, String.class).trim().toUpperCase();
			// Support the old FIXED mode from the old plots
			if("FIXED".equals(timeAxisSubsequent)) {
				settings.timeAxisSubsequent = TimeAxisSubsequentBoundsSetting.JUMP;
				pinTimeAxisAsString = "true";
			} else {
				settings.timeAxisSubsequent = Enum.valueOf(TimeAxisSubsequentBoundsSetting.class, timeAxisSubsequent);
			}
			
			settings.timeSystemSetting = plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_SYSTEM_SETTING, String.class).trim();
			settings.timeFormatSetting = plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_FORMAT_SETTING, String.class).trim();

			settings.nonTimeAxisSubsequentMinSetting = Enum.valueOf(NonTimeAxisSubsequentBoundsSetting.class, plotViewManifestation.getViewProperties().getProperty(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MIN_SETTING, String.class).trim().toUpperCase());
			settings.nonTimeAxisSubsequentMaxSetting = Enum.valueOf(NonTimeAxisSubsequentBoundsSetting.class, plotViewManifestation.getViewProperties().getProperty(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MAX_SETTING, String.class).trim().toUpperCase());
			
			settings.plotLineDraw = new PlotLineDrawingFlags(
					Boolean.parseBoolean(plotViewManifestation.getViewProperties().getProperty(PlotConstants.DRAW_LINES, String.class)),
					Boolean.parseBoolean(plotViewManifestation.getViewProperties().getProperty(PlotConstants.DRAW_MARKERS, String.class))
					);
			settings.plotLineConnectionType = Enum.valueOf(PlotLineConnectionType.class, plotViewManifestation.getViewProperties().getProperty(PlotConstants.CONNECTION_TYPE, String.class).trim().toUpperCase());
			
		} catch (Exception e) {
			logger.error("Problem reading plot settings back from persistence. Continuing with default settings.");
		}

		try {
			String maxTimeAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_MAX, String.class);
			String minTimeAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_MIN, String.class);

			String maxNonTimeAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.NON_TIME_MAX, String.class);
			String minNonTimeAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.NON_TIME_MIN, String.class);

			String timePaddingAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.TIME_PADDING, String.class);

			String nonTimeMinPaddingAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.NON_TIME_MIN_PADDING, String.class);
			String nonTimeMaxPaddingAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.NON_TIME_MAX_PADDING, String.class);
			String groupByOrdinalPositionAsString = plotViewManifestation.getViewProperties().getProperty(PlotConstants.GROUP_BY_ORDINAL_POSITION, String.class);
			
			settings.maxTime = Long.parseLong(maxTimeAsString.trim());
			settings.minTime = Long.parseLong(minTimeAsString.trim());

			settings.maxNonTime = Double.parseDouble(maxNonTimeAsString.trim());
			settings.minNonTime = Double.parseDouble(minNonTimeAsString.trim());

			settings.timePadding = Double.parseDouble(timePaddingAsString.trim());       
			settings.nonTimeMaxPadding = Double.parseDouble(nonTimeMaxPaddingAsString.trim());        
			settings.nonTimeMinPadding = Double.parseDouble(nonTimeMinPaddingAsString.trim());    
			
			if (groupByOrdinalPositionAsString != null && !groupByOrdinalPositionAsString.isEmpty()) {
				settings.ordinalPositionForStackedPlots = Boolean.parseBoolean(groupByOrdinalPositionAsString);
			}
			
			if (pinTimeAxisAsString != null && !pinTimeAxisAsString.isEmpty()) {
				settings.pinTimeAxis = Boolean.parseBoolean(pinTimeAxisAsString);
			}

		} catch (NumberFormatException nfe) {
			logger.error("NumberFormatException: " + nfe.getMessage());
		}
		return settings;
	}
	
	

	/**
	 * Persist the plots settings.
	 * @param timeAxisSetting
	 * @param xAxisMaximumLocation
	 * @param yAxisMaximumLocation
	 * @param timeAxisSubsequentSetting
	 * @param nonTimeAxisSubsequentMinSetting
	 * @param nonTimeAxisSubsequentMaxSetting
	 * @param nonTimeMax
	 * @param nonTimeMin
	 * @param minTime
	 * @param maxTime
	 * @param timePadding
	 * @param nonTimeMaxPadding
	 * @param nonTimeMinPadding
	 * @param plotLineConnectionType 
	 * @param plotLineDraw 
	 */
	void persistPlotSettings(AxisOrientationSetting timeAxisSetting,
			String timeSystem,
			String timeFormat,
			XAxisMaximumLocationSetting xAxisMaximumLocation,
			YAxisMaximumLocationSetting yAxisMaximumLocation,
			TimeAxisSubsequentBoundsSetting timeAxisSubsequentSetting,
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting,
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting,
			double nonTimeMax, double nonTimeMin, GregorianCalendar minTime,
			GregorianCalendar maxTime, 
			Double timePadding,
			Double nonTimeMaxPadding,
			Double nonTimeMinPadding, 
			boolean groupByOrdinalPosition,
			boolean timeAxisPinned, 
			PlotLineDrawingFlags plotLineDraw, 
			PlotLineConnectionType plotLineConnectionType) {

		ExtendedProperties viewProperties = plotViewManifestation.getViewProperties();
		viewProperties.setProperty(PlotConstants.TIME_AXIS_SETTING, "" + timeAxisSetting);
		viewProperties.setProperty(PlotConstants.X_AXIS_MAXIMUM_LOCATION_SETTING, "" + xAxisMaximumLocation);
		viewProperties.setProperty(PlotConstants.Y_AXIS_MAXIMUM_LOCATION_SETTING, "" + yAxisMaximumLocation);
		viewProperties.setProperty(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, "" + timeAxisSubsequentSetting);
		viewProperties.setProperty(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MIN_SETTING, "" + nonTimeAxisSubsequentMinSetting);
		viewProperties.setProperty(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MAX_SETTING, "" + nonTimeAxisSubsequentMaxSetting);
		viewProperties.setProperty(PlotConstants.NON_TIME_MAX, "" + nonTimeMax);
		viewProperties.setProperty(PlotConstants.NON_TIME_MIN, "" + nonTimeMin);
		viewProperties.setProperty(PlotConstants.TIME_MIN, "" + minTime.getTimeInMillis());
		viewProperties.setProperty(PlotConstants.TIME_MAX, "" + maxTime.getTimeInMillis());
		viewProperties.setProperty(PlotConstants.TIME_PADDING, "" + timePadding);
		viewProperties.setProperty(PlotConstants.NON_TIME_MAX_PADDING, "" + nonTimeMaxPadding);
		viewProperties.setProperty(PlotConstants.NON_TIME_MIN_PADDING, "" + nonTimeMinPadding);
		viewProperties.setProperty(PlotConstants.GROUP_BY_ORDINAL_POSITION, Boolean.toString(groupByOrdinalPosition));
		viewProperties.setProperty(PlotConstants.TIME_SYSTEM_SETTING, "" + timeSystem);
		viewProperties.setProperty(PlotConstants.TIME_FORMAT_SETTING, "" + timeFormat);
		viewProperties.setProperty(PlotConstants.PIN_TIME_AXIS, Boolean.toString(timeAxisPinned));
		viewProperties.setProperty(PlotConstants.DRAW_LINES, "" + plotLineDraw.drawLine());
		viewProperties.setProperty(PlotConstants.DRAW_MARKERS, "" + plotLineDraw.drawMarkers());
		viewProperties.setProperty(PlotConstants.CONNECTION_TYPE, "" + plotLineConnectionType);
			
		if (plotViewManifestation.getManifestedComponent() != null) {
			plotViewManifestation.getManifestedComponent().save();
			plotViewManifestation.updateMonitoredGUI();
		}
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

	

	public void persistLineSettings(List<Map<String, LineSettings>> lineSettings) {
		StringBuilder lineSettingsBuilder = new StringBuilder(lineSettings.size() * 100);
		for (Map<String, LineSettings> subPlotMap : lineSettings) {
			for (Entry<String, LineSettings> entry : subPlotMap.entrySet()) {
				LineSettings settings = entry.getValue();
				
				lineSettingsBuilder.append(entry.getKey());
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
