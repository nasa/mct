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
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.gui.housing.MCTControlArea;
import gov.nasa.arc.mct.gui.housing.MCTHousingFactory;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.housing.MCTStatusArea;
import gov.nasa.arc.mct.gui.housing.SelectionManager;
import gov.nasa.arc.mct.gui.impl.MCTSplitPaneFactory;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class MCTHousingViewManifestation extends View {

    private final JPanel controlPanel = new JPanel();
    private final JPanel directoryPanel = new JPanel(new GridLayout(1, 1));
    private final JPanel contentPanel = new JPanel(new GridLayout(1, 1));
    private final JPanel inspectionPanel = new JPanel(new GridLayout(1, 1));
    private final JPanel statusPanel = new JPanel(new GridLayout(1, 1));
    private MCTControlArea controlArea;
    private View directoryArea;
    private MCTContentArea contentArea;
    private View inspectionArea;
    private MCTStandardHousing parentHousing;
    private final SelectionManager selectionManager = new SelectionManager(this);
    private static final double LEAF_HORIZONTAL_SCALE = 0.5;
    private static final double LEAF_VERTICAL_SCALE = 0.5;
    private static final double NON_LEAF_HORIZONTAL_SCALE = 0.7;
    private static final double NON_LEAF_VERTICAL_SCALE = 0.7;
    private static final double USER_ENV_HORIZONTAL_SCALE = 0.5;
    private static final double USER_ENV_VERTICAL_SCALE = 0.75;

    private boolean controlAreaVisible;
    private MCTStatusArea statusArea;
    
    private JComponent splitPanes;
    public static final String VIEW_ROLE_NAME = "Canvas Plus";
    
    /**
     * For internal use only.
     */
    public MCTHousingViewManifestation() {
        super();
    }

    // A container for <directoryPanel, contentPanel, inspectionPanel>
    public MCTHousingViewManifestation(AbstractComponent component, ViewInfo vi) {
        super(component,vi);
        setLayout(new BorderLayout());
        
        double horizontalScale = 0;
        double verticalScale = 0;

        if (component.isLeaf()) {
            horizontalScale = LEAF_HORIZONTAL_SCALE;
            verticalScale = LEAF_VERTICAL_SCALE;
        } else {
            // !isLeaf()
            horizontalScale = NON_LEAF_HORIZONTAL_SCALE;
            verticalScale = NON_LEAF_VERTICAL_SCALE;
            
            if (isRootComponent(component)) {
                horizontalScale = USER_ENV_HORIZONTAL_SCALE;
                verticalScale = USER_ENV_VERTICAL_SCALE;
            }
        }
        
        Dimension d = MCTHousingFactory.getWindowDimension(horizontalScale, verticalScale);
        setSize(d);

        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
        add(controlPanel, BorderLayout.NORTH);

        List<JPanel> panels = new ArrayList<JPanel>();
        byte areaSelection = calculateAreaSelection(component);
        if ((areaSelection & MCTHousingFactory.DIRECTORY_AREA_ENABLE) > 0) {
            panels.add(directoryPanel);
        }
        if ((areaSelection & MCTHousingFactory.CONTENT_AREA_ENABLE) > 0) {
            panels.add(contentPanel);
        }
        if ((areaSelection & MCTHousingFactory.INSPECTION_AREA_ENABLE) > 0) {
            panels.add(inspectionPanel);
        }
        splitPanes = MCTSplitPaneFactory.createSplitPanes(this, panels, JSplitPane.HORIZONTAL_SPLIT);
        add(splitPanes, BorderLayout.CENTER);

        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private static boolean isRootComponent(AbstractComponent component) {
        return component == PlatformAccess.getPlatform().getRootComponent();
    }
    
    private static byte calculateAreaSelection(AbstractComponent component) {
        if (component.isLeaf()) {
            return    MCTHousingFactory.CONTROL_AREA_ENABLE
                    | MCTHousingFactory.CONTENT_AREA_ENABLE | MCTHousingFactory.STATUS_AREA_ENABLE;
        }
        
        if (isRootComponent(component)) {
            return MCTHousingFactory.DIRECTORY_AREA_ENABLE | MCTHousingFactory.INSPECTION_AREA_ENABLE | MCTHousingFactory.CONTROL_AREA_ENABLE
                    | MCTHousingFactory.STATUS_AREA_ENABLE;
        }
        
        return MCTHousingFactory.ENABLE_ALL_AREA;
    }

    @Override
    public void updateMonitoredGUI() {
        String newName = getManifestedComponent().getDisplayName();
        updateTitleText(newName);
    }
    
    @Override
    public SelectionProvider getSelectionProvider() {
        return selectionManager;
    }
    
    @Override
    public void updateMonitoredGUI(PropertyChangeEvent event) {
        String newDisplayName = (String) event.getProperty(PropertyChangeEvent.DISPLAY_NAME);
        if (newDisplayName.equals(getManifestedComponent().getDisplayName()))
            return;

        updateTitleText(newDisplayName);
    }

    // Helper method for updateMonitoredGUI's...
    private void updateTitleText(String newDisplayName) {
        JFrame hostedFrame = parentHousing.getHostedFrame();
        String title = hostedFrame.getTitle();
        int PLUS_VIEW_DELIMETER = title.indexOf("-"); 
        hostedFrame.setTitle(( PLUS_VIEW_DELIMETER > 0) 
                             ? title.replace(title.subSequence(0, PLUS_VIEW_DELIMETER), newDisplayName)
                             : newDisplayName);
    }

    public void buildGUI() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        if (controlAreaVisible && controlArea != null) {
            controlPanel.add(controlArea);
        }

        if (directoryArea != null) {
            directoryPanel.add(directoryArea);
        } else {
            directoryPanel.setVisible(false);
        }

        if (contentArea != null) {
            contentPanel.add(contentArea);
        } else {
            contentPanel.setVisible(false);
        }

        if (inspectionArea != null) {
            inspectionPanel.add(inspectionArea);
        } else {
            inspectionPanel.setVisible(false);
        }

        if (statusArea != null) {
            statusPanel.add(statusArea);
        } else {
            statusPanel.setVisible(false);
        }
    }

    public void setParentHousing(MCTStandardHousing housing) {
        this.parentHousing = housing;
    }

    public void setControlArea(MCTControlArea controlArea) {
        this.controlArea = controlArea;
    }
    
    public void setControlAreaVisible(boolean flag) {
        if (this.controlArea != null) {
            if (flag)
                this.controlPanel.add(this.controlArea);
            else
                this.controlPanel.remove(this.controlArea);

            this.controlPanel.revalidate();
        }
        this.controlAreaVisible = flag;

    }

    public boolean isControlAreaVisible() {
        return controlAreaVisible;
    }

    public void setDirectoryArea(View directoryArea) {
        this.directoryArea = directoryArea;
        selectionManager.manageComponent(directoryArea);
    }

    public void setContentArea(MCTContentArea contentArea) {
        if (this.contentArea == null && splitPanes instanceof JSplitPane && !contentArea.isAreaEmpty()) {
            JSplitPane splitPane = JSplitPane.class.cast(splitPanes);
            assert splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
            JSplitPane leftInnerPane = JSplitPane.class.cast(splitPane.getLeftComponent());
            assert leftInnerPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;

            leftInnerPane.getLeftComponent().setMinimumSize(new Dimension(0,0));
            leftInnerPane.setDividerLocation(0);
            
            splitPane.getRightComponent().setMinimumSize(new Dimension(0,0));
            splitPane.addAncestorListener(new AncestorListener() {

                @Override
                public void ancestorAdded(AncestorEvent event) {
                    JSplitPane pane = ((JSplitPane) event.getSource());
                    boolean continuousLayout = pane.isContinuousLayout();
                    pane.setContinuousLayout(false);
                    pane.setDividerLocation(1.0);
                    pane.setContinuousLayout(continuousLayout);
                    pane.removeAncestorListener(this);
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                }
                
            });
        }
        this.contentArea = contentArea;
        selectionManager.manageComponent(contentArea);
    }

    public void setInspectionArea(View inspectionArea) {
        this.inspectionArea = inspectionArea;
    }

    public View getInspectionArea() {
        return inspectionArea;
    }

    public View getDirectoryArea() {
        return directoryArea;
    }

    public MCTContentArea getContentArea() {
        return contentArea;
    }

    public MCTControlArea getControlArea() {
        return controlArea;
    }

    public View getCurrentManifestation() {
        Collection<View> selectedManifestations = 
            getSelectionProvider().getSelectedManifestations();
        
        if (selectedManifestations.isEmpty()) {
            return this;
        }
        
        return selectedManifestations.iterator().next();
    }

    public void setStatusArea(MCTStatusArea statusArea) {
        this.statusArea = statusArea;
    }
    
    public MCTStatusArea getStatusArea() {
        return this.statusArea;
    }

    @Override
    public void enterLockedState() {
        selectionManager.clearCurrentSelections();
    }

    @Override
    public void exitLockedState() {
        JFrame hostedFrame = parentHousing.getHostedFrame();
        StringBuilder title = new StringBuilder(hostedFrame.getTitle());
        if (title.indexOf("*")==0) {
            hostedFrame.setTitle(title.substring(1));
        }
        selectionManager.clearCurrentSelections();
    }

    @Override
    public void processDirtyState() {
    }
}