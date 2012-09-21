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
package gov.nasa.arc.mct.table.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableFormattingConstants {

    // Items for use in drawing foreground colors...
   
    
    public static  int defaultFontSize = 12;
    public static final int defaultFontStyle = Font.PLAIN;
    public static final int defaultRowHeight = 14;
    public static Color defaultFontColor;
    public static Color defaultValueOKColor;
    public static Color defaultBackgroundColor = Color.black;
    public static final JVMFontFamily defaultJVMFontFamily = JVMFontFamily.SansSerif;
    public static final int UNDERLINE_OFF = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(TableFormattingConstants.class);
    
    public static final Map<TextAttribute, Object> underlineMap = new Hashtable<TextAttribute, Object>();
    
    static {
    	underlineMap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
	    try {
	    	Object fontSizeValue = UIManager.get("TableViewManifestation.fontSize");
	        if (fontSizeValue != null && fontSizeValue instanceof String) {
	        	defaultFontSize = Integer.parseInt((String) fontSizeValue);
	        }
	    } catch (NumberFormatException nfe) {
	    	LOGGER.error("Could not parse font size as integer; using default");
		}
	    try {
	    	Color tableForeground = UIManager.getColor("TableViewManifestation.foreground");
	        if (tableForeground != null) {
	        	defaultFontColor = tableForeground;
	        }
	    } catch (Exception e) {
	    	LOGGER.error("Could not parse table background as color; using default");
		}
	    try {
	    	Color tableBackground = UIManager.getColor("TableViewManifestation.background");
	        if (tableBackground != null) {
	        	defaultBackgroundColor = tableBackground; 
	        }
	    } catch (Exception e) {
	    	LOGGER.error("Could not parse table background as color; using default");
		}
	    try {
	    	Color valueOKColor = UIManager.getColor("ISPColor.ColorOK");
	        if (valueOKColor != null) {
	        	defaultValueOKColor = valueOKColor; 
	        }
	    } catch (Exception e) {
	    	LOGGER.error("Could not parse value OK as color; using default");
		}
	    
    }
    
    /**
     * The color set for background and foreground table controls
     */
    protected static final Color ForegroundColors[] = { new Color(000, 000, 000), // black
        new Color(000, 000, 255), // blue
        new Color(000, 128, 000), // Green
        new Color(032, 179, 170), // light sea green
        new Color(152, 251, 152), // Pale Green
        new Color(255, 140, 000), // Dark Orange
        new Color(255, 000, 255), // Magenta
        new Color(255, 69, 000), // Orange Red
        new Color(255, 215, 000), // Gold
        new Color(047, 79, 79), // Dark Slate Gray
        new Color(128, 128, 128), // Gray
        new Color(100, 149, 237), // Corn Flower blue
        new Color(000, 49, 042), // Brown
        new Color(000, 176, 176), // Aquamarine
        new Color(102, 051, 255), // Intermediate Violate blue
        defaultFontColor,
        defaultValueOKColor
    };
       
    
    /**
     * @author dcberrio
     * Enumerated standard JVM font families
     */
    public enum JVMFontFamily {
		Dialog ("Dialog"),
		DialogInput ("Input"),
		Monospaced ("Monospaced"),
		SansSerif ("Sans Serif"),
		Serif ("Serif");
		
		private final String displayName;
		JVMFontFamily(String displayName) {
			this.displayName = displayName;
		}
		/** Get pretty name for controls
		 * @return displayName
		 */
		public String getDisplayName() {
			return displayName;
		}
    }
}
