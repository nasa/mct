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
 * MCTIcons.java Aug 18, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.util;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * MCT icons for errors, warnings and component object.
 *
 */
public class MCTIcons {

    private static ImageIcon warningIcon32x32 = new ImageIcon(MCTIcons.class.getResource("/images/warning_32x32.png"));
    private static ImageIcon errorIcon32x32 = new ImageIcon(MCTIcons.class.getResource("/images/error_32x32.png"));
    private static ImageIcon componentIcon12x12 = new ImageIcon(MCTIcons.class.getResource("/images/object_icon.png"));

    private static enum Icons { 
        WARNING_ICON,
        ERROR_ICON,
        COMPONENT_ICON    
    };
    
    private static ImageIcon getIcon(Icons anIconEnum) {
        switch(anIconEnum) {
            
        case WARNING_ICON:
            return warningIcon32x32;
            
        case ERROR_ICON:
            return errorIcon32x32;
            
        case COMPONENT_ICON:
            return componentIcon12x12;
                
        default:
            return null;
        }
        
    }

    /**
     * Gets the warning image icon.
     * @return ImageIcon - the image icon.
     */
    public static ImageIcon getWarningIcon() {
        return getIcon(Icons.WARNING_ICON);
    }

    /**
     * Gets the error image icon scaled to designated width and height.
     * @param width - number.
     * @param height - number.
     * @return the error image icon. 
     */
    public static ImageIcon getErrorIcon(int width, int height) {
         Image scaled = getIcon(Icons.ERROR_ICON).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
         return new ImageIcon(scaled);
    }

    /**
     * Gets the component image icon.
     * @return the component image icon.
     */
    public static ImageIcon getComponent() {
        return getIcon(Icons.COMPONENT_ICON);
    }
    
    private MCTIcons() {
        // no instantiation 
    }

}

