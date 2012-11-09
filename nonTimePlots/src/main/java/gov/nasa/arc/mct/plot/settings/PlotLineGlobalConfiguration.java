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
package gov.nasa.arc.mct.plot.settings;

import gov.nasa.arc.mct.plot.settings.PlotConstants.PlotLineConnectionType;

import java.awt.Font;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles global settings for plot lines (fonts for plot markers, default connection types)
 */
public class PlotLineGlobalConfiguration {
	private final static Logger logger = LoggerFactory.getLogger(PlotLineGlobalConfiguration.class);
	
	private static boolean initialized = false;
	
	// Defaults:
	private static String                 fontFace   = Font.MONOSPACED;
	private static int                    fontSize   = 18;
	private static int                    fontStyle  = Font.BOLD;
	
	private static Font                   markerFont;
	private static PlotLineConnectionType connection = PlotLineConnectionType.STEP_X_THEN_Y;
	
	private static void ensureInitialized() {
		if (!initialized) {
			initialized = true;
			
			String face = UIManager.getString("PlotLine.fontFace");
			if (face != null) {
				if (Font.decode(face) != null) {
					fontFace = face;
				} else {
					logger.warn("Font face %s specified in properties is unavailable. Defaulting to %s.", face, fontFace);
				}
			}
			
			String size = UIManager.getString("PlotLine.fontSize");
			if (size != null) {
				try {	
					fontSize = Integer.parseInt(size);
				} catch (NumberFormatException nfe) {
					logger.warn("Font size %s specified in properties is not a valid integer. Defaulting to %s.", size, Integer.toString(fontSize));
				}	
			}

			String style = UIManager.getString("PlotLine.fontStyle");
			if (style != null) {
				if (style.toUpperCase().equals("BOLD")) {
					fontStyle = Font.BOLD;
				} else if (style.toUpperCase().equals("ITALIC")) {
					fontStyle = Font.ITALIC;
				} else if (style.toUpperCase().equals("BOLDITALIC")) {
					fontStyle = Font.BOLD | Font.ITALIC;
				} else {
					logger.warn("Font style %s specified in properties is not recognized. Using defaults", style);
				}
			}

			String conn = UIManager.getString("PlotLine.connection");
			if (conn != null) {
				try {	
					connection = Enum.valueOf(PlotLineConnectionType.class, conn);
				} catch (IllegalArgumentException iae) {
					logger.warn("Connection type %s specified in properties is unknown. Defaulting to %s.", conn, connection.name());
				}	
			}
			
			markerFont = Font.decode(fontFace).deriveFont(fontStyle, fontSize);		
		}
	}
	
	public static Font getMarkerFont() {
		ensureInitialized();
		return markerFont;
	}
	
	public static PlotLineConnectionType getDefaultConnectionType() {
		ensureInitialized();
		return connection;
	}
	
}
