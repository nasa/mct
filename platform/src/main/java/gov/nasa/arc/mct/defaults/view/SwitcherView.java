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
package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * A view containing a drop-down of available view types 
 * for the represented component. Used by Inspector views 
 * for view switching.
 */
public class SwitcherView extends View {
    private static final long serialVersionUID = -7338842560419381410L;
    
    private ViewProvider managedView = null; // Where to send "switch" events
    @SuppressWarnings("rawtypes")
    private JComboBox comboBox;
    private JLabel    label; 
    private static final float FONT_SIZE = 10f; 
    private static final Color ICON_COLOR = new Color(144,144,144);
    
    /**
     * The view info used to instantiate this view.
     */
    public static final ViewInfo VIEW_INFO = 
            new ViewInfo(SwitcherView.class, "Switcher", ViewType.VIEW_SWITCHER);
    

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SwitcherView(AbstractComponent ac, ViewInfo vi) {
        Set<ViewInfo> viewInfoSet = ac.getViewInfos(ViewType.OBJECT);
        ViewInfo[] viewInfos = viewInfoSet.toArray(new ViewInfo[viewInfoSet.size()]);

        if (viewInfos.length > 1) {
            // Only show combo box if there are multiple views
            comboBox = new JComboBox(viewInfos);
            comboBox.setUI(new SwitcherComboBoxUI());
            comboBox.setRenderer(viewInfoRenderer);
            comboBox.addItemListener(itemListener);
            comboBox.setVisible(false);
            comboBox.setEnabled(false);
            comboBox.setOpaque(false);
            add(comboBox);
        } else if (viewInfos.length == 1) {
            // Otherwise, just show the one available view as a label
            label = new JLabel();
            label.setIcon(getIcon(vi));
            label.setText(vi.getViewName());
            add(label);
        } else {
            // No views to show
        }
        
        setBorder(BorderFactory.createEmptyBorder(1,4,1,2));
        setOpaque(false);
    }
    
    @Override
    public <T> void addMonitoredGUI(T gui) {
        if (gui instanceof ViewProvider) {
            managedView = (ViewProvider) gui;
        }
        if (managedView != null) {
            resetSelection();
            if (comboBox != null) {
                comboBox.setVisible(true);
                comboBox.setEnabled(true);
            }
        }
        super.addMonitoredGUI(gui);
    }

    @Override
    public void setForeground(Color fg) {
        if (label != null) {
            label.setForeground(fg);
        }
        super.setForeground(fg);
    }
    
    private void resetSelection() {
        if (managedView != null) {
            View housedView = managedView.getHousedViewManifestation();
            if (housedView != null) {
                ViewInfo vi = housedView.getInfo();
                if (comboBox != null) {
                    comboBox.removeItemListener(itemListener); // Avoid triggering listener
                    comboBox.setSelectedItem(vi);
                    comboBox.addItemListener(itemListener);
                }
                if (label != null) {
                    label.setIcon(getIcon(vi));
                    label.setText(vi.getViewName());
                }
            }
        }
    }
    
    private static ImageIcon getIcon(ViewInfo vi) {
        return MCTIcons.processIcon(vi.getAsset(ImageIcon.class), ICON_COLOR, false);
    }
    
    private final ItemListener itemListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            // Only respond to SELECTED events to avoid double-firing
            if (managedView != null && e.getStateChange() == ItemEvent.SELECTED) {       
                Object item = e.getItem();
                if (item != null && item instanceof ViewInfo) {
                    // Try to change the view
                    if (!managedView.setHousedViewManifestation((ViewInfo) item)) {
                        // View says it didn't change - restore previous selection
                        resetSelection();
                    }
                }
            }
        }        
    };

    // Used for providing components to represent ViewInfos in the drop-down
    @SuppressWarnings("rawtypes")
    private static final ListCellRenderer viewInfoRenderer = new ListCellRenderer() {
        private JLabel label = new JLabel(); // Reuse, since it's only used for rendering
                
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            label.setFont(label.getFont().deriveFont(FONT_SIZE));
            if (value instanceof ViewInfo) {
                ViewInfo vi = (ViewInfo) value;
                label.setIcon(getIcon(vi));
                label.setText(vi.getViewName());
            } else {
                label.setIcon(null);
                label.setText("Error");                
            }
            return label;
        }
        
    };
    
    private static class SwitcherComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {            
            JButton emptyButton = new JButton();
            emptyButton.setIcon(ARROW_ICON);
            emptyButton.setBorder(BorderFactory.createEmptyBorder());
            emptyButton.setContentAreaFilled(false);
            return emptyButton;
        }
        
        @Override
        public Dimension getDisplaySize() {
            Dimension d = super.getDisplaySize();
            return new Dimension(d.width + 12, d.height);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.setColor(c.getBackground());
            g.fillRoundRect(0, 1, c.getWidth()-1, c.getHeight()-2, 10, 10);
            super.paint(g, c);
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            if (hasFocus) {
                if (g instanceof Graphics2D) {
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                g.setColor(UIManager.getColor("Button.focus"));
                g.drawRect(bounds.x + 2, bounds.y + 2, bounds.width-8, bounds.height-5);               
            }
        } 
        
    }
    
    private static final Icon ARROW_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int tx[] = { 3, 11, 7 };
            int ty[] = { 6, 6, 10 };
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.setColor(Color.DARK_GRAY);
            g.fillPolygon(tx, ty, 3);
        }

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 12;
        }
        
    };
}
