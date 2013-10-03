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
 * LookAndFeelSetting.java Sep 3, 2010
 *
 * This code is property of the National Aeronautics and Space Administration and was
 * produced for the Mission Control Technologies (MCT) Project.
 *
 */
package gov.nasa.arc.mct.util;

import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;



/**
 * This class stores the current Look and Feel for the user interface.
 */
public enum LookAndFeelSettings {
    
    /** L&F settings instance. */
    INSTANCE;

    private static final MCTLogger logger = MCTLogger.getLogger(LookAndFeelSettings.class);
    private static UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();

    private static Color WINDOW;
    private static Color WINDOW_BORDER;

    private static Color MENUBAR_BACKGROUND;
    private static Color TREE_SELECTION_BACKGROUND;
    private static Color TEXT_HIGHLIGHT;

    private static ColorScheme BASE_PROPERTIES = new ColorScheme();
    
    /** The viewColor string constant. */
    public final static String viewColor = "viewColor";

    /**
     * As a convenience, allow config file to specify "Metal" or Nimbus". Absence of the
     * parameter results in default to Metal. Otherwise, the config file must provide a
     * fully qualified LAF name.
     * @param lookAndFeelStr - L&F string.
     */
    public void setLAF(String lookAndFeelStr) {
        String exceptionNotice = "";
        try {
            // Set the font for Metal LAF to non-bold, in case Metal is used
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            if (lookAndFeelStr == null || lookAndFeelStr.equalsIgnoreCase("Metal")) {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            } else
                if (lookAndFeelStr.equalsIgnoreCase("Nimbus")) {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if (info.getName().equals("Nimbus")) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } else {
                    UIManager.setLookAndFeel(lookAndFeelStr);
                }
        } catch (UnsupportedLookAndFeelException e) {
            exceptionNotice = "UnsupportedLookAndFeelException";
        } catch (ClassNotFoundException e) {
            exceptionNotice = "ClassNotFoundException";
        } catch (InstantiationException e) {
            exceptionNotice = "InstantiationException";
        } catch (IllegalAccessException e) {
            exceptionNotice = "IllegalAccessException";
        } finally {
            if (! exceptionNotice.isEmpty()) {
                try {
                    // The desired LAF was not created, so try to use the Metal LAF as a default.
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                } catch (UnsupportedLookAndFeelException e) {
                    logger.error("Could not initialize the Swing Look and Feel settings, MCT is closing.");
                    System.exit(1);
                }
            }
        }
        // Look and Feel has been successfully created, now set colors
        initializeColors(UIManager.getLookAndFeel());
        Properties props = new Properties();
        String filename = System.getProperty(viewColor,"resources/properties/viewColor.properties");
        FileReader reader = null;
        try {
        	reader = new FileReader(filename);
        	props.load(reader);
            BASE_PROPERTIES = new ColorScheme(props);
            BASE_PROPERTIES.applyColorScheme(); // Apply top-level color bindings
        } catch (Exception e) {
            logger.warn("Using default color and font properties because could not open viewColor properties file :"+filename);
            BASE_PROPERTIES = new ColorScheme();
        } finally {
        	try {
                if (reader != null) reader.close();
            } catch(IOException ioe1){ }
        }
    }

    /* Harleigh108:
     * Replaced if (lookAndFeel instanceof NimbusLookAndFeel) as NimbusLookAndFeel is deprecated 
     */
    private void initializeColors(LookAndFeel lookAndFeel) {
        lafDefaults = UIManager.getLookAndFeelDefaults();
        if (lookAndFeel instanceof MetalLookAndFeel) {
            WINDOW = lafDefaults.getColor("window");
            WINDOW_BORDER = lafDefaults.getColor("windowBorder");
        } else
        if(lookAndFeel.getClass().getSimpleName().equalsIgnoreCase("NimbusLookAndFeel")) {
            WINDOW = lafDefaults.getColor("Panel.background");
            WINDOW_BORDER = lafDefaults.getColor("nimbusBorder");
        } else {
            logger.error("Look and Feel reference is invalid: "
                    + lookAndFeel.getName() + ". MCT is closing.");
            System.exit(1);
        }
        MENUBAR_BACKGROUND = lafDefaults.getColor("MenuBar.background");
        TREE_SELECTION_BACKGROUND = lafDefaults.getColor("Tree.selectionBackground");
        TEXT_HIGHLIGHT = lafDefaults.getColor("textHighlight");

        LafColor.refresh();
    }

    /**
     * Checks for whether it's been initialized or not.
     * @return boolean - flag to check for initialization.
     */
    public boolean isInitialized() {
        return (WINDOW != null && WINDOW_BORDER != null);
    }

    /**
     * Gets the window color.
     * @return Color - window color.
     */
    public static Color getWindowColor() {
        return WINDOW;
    }

    /**
     * Window border color.
     * @return Color - window border color.
     */
    public static Color getWindowBorderColor() {
        return WINDOW_BORDER;
    }

    /**
     * Gets the menubar background color.
     * @return Color - Menubar background color.
     */
    public static Color getMenubarColor() {
        return MENUBAR_BACKGROUND;
    }

    /**
     * Gets the tree selection background color.
     * @return Color - Tree selection background color.
     */
    public static Color getTreeSelectionColor() {
        return TREE_SELECTION_BACKGROUND;
    }

    /**
     * Gets the text hightlight color.
     * @return Color - Text highlight color.
     */
    public static Color getTextHighlightColor() {
        return TEXT_HIGHLIGHT;
    }
    
    /**
     * Gets the base color properties.
     * @return Color - Base properties color.
     */     
    public static ColorScheme getColorProperties() {
  
        return BASE_PROPERTIES;
    }
}
