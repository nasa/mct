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
/**
 * LafColor.java Oct 3, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.util;

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Colors from the look and feel defaults. Intended for selective use.
 * 
 */
public class LafColor {

    private final static UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
    
    /** The default Window Look and Feel color. */
    public static Color WINDOW = lafDefaults.getColor("window");
    
    /** The default Window border Look and Feel color. */
    public static Color WINDOW_BORDER = lafDefaults.getColor("windowBorder");

    /** The default menubar background color. */
    public static Color MENUBAR_BACKGROUND = lafDefaults.getColor("MenuBar.background");
    
    /** The default tree selection background color. */
    public static Color TREE_SELECTION_BACKGROUND = lafDefaults.getColor("Tree.selectionBackground");
    
    /** The default text highlight color. */
    public static Color TEXT_HIGHLIGHT = lafDefaults.getColor("textHighlight");
    
    /** 
     * Gets the color based on UIManager defaults.
     * @param str - the parameter .
     * @return Color - the color. 
     */
    public static Color get( String str ) {
        return UIManager.getLookAndFeelDefaults().getColor(str);
    }

    /**
     * Refreshes all the color constants.
     */
    public static void refresh() {
        WINDOW = LookAndFeelSettings.getWindowColor();
        WINDOW_BORDER = LookAndFeelSettings.getWindowBorderColor();
        MENUBAR_BACKGROUND = LookAndFeelSettings.getMenubarColor();
        TREE_SELECTION_BACKGROUND = LookAndFeelSettings.getTreeSelectionColor();
        TEXT_HIGHLIGHT = LookAndFeelSettings.getTextHighlightColor();
    }
}
