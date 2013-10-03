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
package gov.nasa.arc.mct.gui;

import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

/**
 * A toggle button used for showing/hiding configuration elements. 
 * (similar to Twistie)
 * 
 * 
 * @author vwoeltje
 *
 */
public class SettingsButton extends JToggleButton {

    private static final long serialVersionUID = -3090253457915724044L;
    private static final Icon CONFIG_DESELECTED =
                    MCTIcons.processIcon(
                            new ImageIcon(SettingsButton.class.getResource("/icons/mct_icon_config.png")),
                            0.9f, 0.9f, 0.9f, true);
    private static final Icon CONFIG_SELECTED =
                    MCTIcons.processIcon(
                            new ImageIcon(SettingsButton.class.getResource("/icons/mct_icon_config.png")),
                            1f, 1f, 1f, true);
            
    private static final Color SELECTED_BACKGROUND = new Color(193, 193, 193);
    private static final Color SELECTED_BORDER = new Color(138, 138, 138);
    private static final Color FOCUS_BORDER = new Color(138, 138, 200);
    
    private static final Dimension PREFERRED_SIZE = 
                    new Dimension(17, 17);
            
    /**
     * Create a new configuration button.
     */
    public SettingsButton() {
        setPreferredSize(PREFERRED_SIZE);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        setIcon(CONFIG_DESELECTED);
        setSelectedIcon(CONFIG_SELECTED);
        setSelected(false);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        setVisible(enabled);
        super.setEnabled(enabled);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if (isSelected()) {
            g.setColor(SELECTED_BACKGROUND);
            g.fillRoundRect(1, 1, getWidth()-3, getHeight()-2, 4, 4);
            g.setColor(SELECTED_BORDER);
            g.drawRoundRect(1, 1, getWidth()-3, getHeight()-2, 4, 4);
        }
        if (hasFocus()) {
            g.setColor(FOCUS_BORDER);
            g.drawRoundRect(1, 1, getWidth()-3, getHeight()-2, 4, 4);           
        }
        super.paintComponent(g);
    }
}
