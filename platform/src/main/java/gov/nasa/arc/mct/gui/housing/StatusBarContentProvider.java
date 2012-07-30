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
 * StatusBarContentProvider.java Mar 29, 2009
 *
 * This code is property of the National Aeronautics and Space Administration and was
 * produced for the Mission Control Technologies (MCT) Project.
 *
 */
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.impl.StatusAreaWidgetRegistryImpl;
import gov.nasa.arc.mct.services.component.StatusAreaWidgetInfo;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class StatusBarContentProvider {
    /*
     * We could alternatively define a provider interface and then pass this object implementing
     * the interface into the housing factory.
     */

    private static MCTLogger logger = MCTLogger.getLogger(StatusBarContentProvider.class);
    
    private final List<Component> dynamicStatusWidgets = new LinkedList<Component>();
    private MCTHousing housing;

    public StatusBarContentProvider(MCTHousing housing) {
        super();
        if (housing == null) {
            logger.error("Null housing passed to status bar content provider");
            return;
        }
        this.housing = housing;
        MCTStatusArea statusArea = housing.getStatusArea();
        if (statusArea == null) {
            logger.debug("Housing with no status area object passed to status bar content provider");
            return;
        }
        
        // Populate status widgets provided by external plugins.
        Collection<StatusAreaWidgetInfo> widgetInfos = StatusAreaWidgetRegistryImpl.getInstance().getStatusAreaWidgetInfos();
        for (StatusAreaWidgetInfo info : widgetInfos) {
            statusArea.addToLeft(info.createWidget());
        }        
        
        // Populate status widgets (if any) provided by the manifestation in the center pane.
        MCTContentArea contentArea = housing.getContentArea();
        if (contentArea != null) {
            contentArea.addPropertyChangeListener(MCTContentArea.CENTER_PANE_VIEW_CHANGE, new PropertyChangeListener() {
                
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    refreshFromCenterPaneViewChange();
                }
            });

            View housedViewManifestation = contentArea.getHousedViewManifestation();
            List<? extends JComponent> statusWidgets = housedViewManifestation.getStatusWidgets();
            for (JComponent widget : statusWidgets) {
                dynamicStatusWidgets.add(widget);
                statusArea.addToLeft(widget);
            }
        }
        
        statusArea.setRightWidget(MemoryMeter.getInstance().getMemoryMeterWidget());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MemoryMeter.getInstance().setVisible(true);
            }
        });
    }

    private void refreshFromCenterPaneViewChange() {
        MCTStatusArea statusArea = housing.getStatusArea();
        for (Component widget : dynamicStatusWidgets) {
            statusArea.removeFromLeft((JComponent)widget);
        }
        dynamicStatusWidgets.clear();
        MCTContentArea contentArea = housing.getContentArea();
        for (JComponent widget : contentArea.getHousedViewManifestation().getStatusWidgets()) {
            statusArea.addToLeft(widget);
            dynamicStatusWidgets.add(widget);
        }
        statusArea.revalidate();
        statusArea.repaint();
    }    
}
