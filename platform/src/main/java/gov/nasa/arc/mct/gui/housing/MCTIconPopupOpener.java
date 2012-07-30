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
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ViewProvider;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.menu.MenuFactory;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

public class MCTIconPopupOpener extends MouseAdapter {
    private final static MCTLogger logger = MCTLogger.getLogger(MCTDirectoryArea.class);

    private AbstractComponent component;
    private JComponent viewManifestation;

    /**
     * This mouse listener uses the MCT component and view manifestation to build an Action context;
     * and the view manifestation to anchor the popup menu display.
     * @param comp An MCT component.
     * @param viewManif A JComponent that supports the ViewManifestationProvider interface.
     */
    public MCTIconPopupOpener(AbstractComponent comp, JComponent viewManif) {
        viewManifestation = viewManif;
        component = comp;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger())
            showPopupMenu(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger())
            showPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
            showPopupMenu(e);
    }

    private void showPopupMenu(MouseEvent e) {
        if (viewManifestation == null ) {
            return;
        }
        if ( !(viewManifestation instanceof ViewProvider)) {
            logger.error("JComponent object needs to implement ViewManifestationProvider interface");
            return;
        }
        // Set action context.
        ActionContextImpl context = new ActionContextImpl();
        context.setTargetComponent(component);
        context.addTargetViewComponent(viewManifestation);
        context.setTargetHousing(UserEnvironmentRegistry.getActiveHousing());

        JPopupMenu popupMenu = MenuFactory.createIconPopupMenu(context);
        viewManifestation.setComponentPopupMenu(popupMenu);
        popupMenu.show(viewManifestation, e.getX(), e.getY());
    }
}
