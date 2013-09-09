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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * MCT icons for errors, warnings and component object.
 *
 */
public class MCTIcons {

    private static ImageIcon warningIcon32x32 = new ImageIcon(MCTIcons.class.getResource("/images/warning_32x32.png"));
    private static ImageIcon errorIcon32x32 = new ImageIcon(MCTIcons.class.getResource("/images/error_32x32.png"));
    private static ImageIcon componentIcon12x12 = new ImageIcon(MCTIcons.class.getResource("/images/object_icon.png"));

    // Cache processed icons for later retrieval
    private static Map<ImageIcon, ImageIcon> processedIcons = new HashMap<ImageIcon, ImageIcon>();
    
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
    
    /**
     * Generate an icon based on the provided hash code. Icon will exhibit 
     * symmetry and have a generally similar appearance to other "default" 
     * icons.
     * 
     * Icons for any given hash are always identical. Icons for different 
     * hashes are unique up to at least the first 8 bits.
     * 
     * Useful when, for instance, there is a component type for which no 
     * icon has been provided. Generated icons will not be representative 
     * of the component's abstraction, but will generally permit that 
     * component type to be distinguished from others (in a "this is 
     * like that, this is not like that" sense)
     * 
     * @param hash a hash code
     * @param sz size, in pixels (icon will be square)
     * @param color color to use (background transparent)
     * @return a generated icon
     */
    public static ImageIcon generateIcon(int hash, int sz, Color color) {
        BufferedImage image = new BufferedImage(sz,sz,BufferedImage.TYPE_4BYTE_ABGR);
        
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw corners
        int w = hash & 3;
        hash>>>=1; // Only shift one; Will get shifted again BEFORE it's checked
        if (w > 0) {   
            w+=1;
            w*= (sz/14);
            g.fillRect(1,1,w,w);
            g.fillRect(sz-w,sz-w,w,w);
            g.fillRect(sz-w,1,w,w);
            g.fillRect(1,sz-w,w,w);            
        }

        // Draw concentric circles
        for (int radius = 1; radius < sz/2; radius += sz/7) {
            if (((hash>>>=1) & 1) != 0) {
                if (radius < 3) {
                    g.fillOval(sz/2-radius, sz/2-radius, radius*2, radius*2);
                } else {
                    g.drawOval(sz/2-radius, sz/2-radius, radius*2, radius*2);
                }
            }
        }

        // Draw concentric Squares
        for (int radius = 3; radius < sz/2; radius += sz/7) {
            if (((hash>>>=1) & 1) != 0) {
                if (radius < 3) {
                    g.fillRect(sz/2-radius, sz/2-radius, radius*2, radius*2);
                } else {
                    g.drawRect(sz/2-radius, sz/2-radius, radius*2, radius*2);
                }
            }
        }        
        
        
        // Draw top/bottom dots        
        if (((hash>>>=1) & 1) != 0) {   
            g.fillOval(1,sz/2-sz/14,sz/7,sz/7+1);
            g.fillOval(sz-sz/7,sz/2-sz/14,sz/7,sz/7+1);
        }
        if (((hash>>>=1) & 1) != 0) {   
            g.fillOval(sz/2-sz/14,1,sz/7+1,sz/7);
            g.fillOval(sz/2-sz/14,sz-sz/7,sz/7+1,sz/7);            
        }
                
        
        return new ImageIcon(image);
    }

    /**
     * Process the given icon to be consistent with MCT icon 
     * look and feel. (Add drop shadow, colorize).
     * @param icon the icon to process
     * @return an appropriately-processed icon
     */
    public static ImageIcon processIcon(ImageIcon icon) {
        // Check cache first
        if (processedIcons.containsKey(icon)) {
            return processedIcons.get(icon);
        } else if (processedIcons.containsValue(icon)) { // Already processed
            return icon;
        } 
            
        // Process icon with default appearance parameters
        ImageIcon newIcon = processIcon(icon, 0.775f, 0.85f, 0.95f, true);
        processedIcons.put(icon, newIcon);
        
        return newIcon;
    }
    
    /**
     * Process the given icon to be consistent with MCT 
     * icon look and feel. Desired color can be specified 
     * and drop shadow can be enabled / disabled. 
     *  
     * @param icon the icon to process
     * @param r scale for red channel
     * @param g scale for green channel
     * @param b scale for blue channel
     * @param dropShadow whether or not to add drop shadow
     * @return a processed icon
     */
    public static ImageIcon processIcon(ImageIcon icon, float r, float g, float b, boolean dropShadow) {
        float coloration[] = {r,g,b,1f};
        float preshadow[] = {0f,0f,0.25f,0.125f};
        float shadow[] = {0.1f,0.1f,0.1f,0.65f};
        float offset[] = {0f,0f,0f,0f};

        // Create a copy of the image with some extra padding for drop shadow
        BufferedImage bufferedImage = new BufferedImage(
                icon.getIconWidth() + 2, 
                icon.getIconHeight() + 2,
                BufferedImage.TYPE_4BYTE_ABGR);

        if (dropShadow) {
            // Draw the icon upper-left "shadow" (subtle outline)
            icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);           
            bufferedImage = new RescaleOp(preshadow, offset, null).filter(bufferedImage, null);
            
            // Draw the lower-right shadow
            icon.paintIcon(null, bufferedImage.getGraphics(), 2, 2);
            bufferedImage =  new RescaleOp(shadow, offset, null).filter(bufferedImage, null);
        }
        
        // Repaint original icon & colorize
        icon.paintIcon(null, bufferedImage.getGraphics(), 1, 1);
        bufferedImage =  new RescaleOp(coloration, offset, null).filter(bufferedImage, null);
        
        return new ImageIcon(bufferedImage);
    }

}

