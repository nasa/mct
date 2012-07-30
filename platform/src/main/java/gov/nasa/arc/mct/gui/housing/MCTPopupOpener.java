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
 * MCTPopupOpener.java Feb 02, 2009
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.menu.MenuFactory;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Mouse listener used for triggering popup menus.
 * Converted to handle multiple selections.
 * 
 */
public class MCTPopupOpener extends MouseAdapter {

    private final static MCTLogger logger = MCTLogger.getLogger(MCTPopupOpener.class);

    private AbstractComponent component;
    private Set<View> multiViewManifestations = null;

    /**
     * This mouse listener uses the MCT component and view manifestation to build an Action context;
     * and the static set of view manifestations to anchor the popup menu display.
     * @param comp An MCT component.
     */
    public MCTPopupOpener(AbstractComponent comp) {
        component = comp;
    }

    // Single view manifestation
    public MCTPopupOpener(AbstractComponent comp, View viewManif) {
        this(comp);
        multiViewManifestations = new HashSet<View>();
        multiViewManifestations.add(viewManif);
    }

    // Multiple view manifestations
    public MCTPopupOpener(AbstractComponent comp, Set<View> viewManifSet) {
        this(comp);
        multiViewManifestations = new HashSet<View>();
        for (View manif : viewManifSet) {
            multiViewManifestations.add(manif);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3)
            showPopupMenu(e);
    }

    private void showPopupMenu(MouseEvent e) {
        ActionContextImpl context = new ActionContextImpl();
        context.setTargetComponent(component);
        // Set the context's housing field. Look for a non-null housing in one of the manifestations.
        Iterator<View> iter = multiViewManifestations.iterator();
        while (iter.hasNext()) {
            MCTHousing currentHousing = (MCTHousing) SwingUtilities.getAncestorOfClass(MCTAbstractHousing.class, iter.next());
            if (currentHousing != null) {
                context.setTargetHousing(currentHousing);
                break;
            }
        }

        for (View viewManifestation: multiViewManifestations) {
            context.addTargetViewComponent(viewManifestation);
        }
            
        JPopupMenu popupMenu = generatePopupMenu(context);
        if (popupMenu == null) {
            logger.error("Could not create popup menu using menu factory");
            return;
        }
        popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
    }

    JPopupMenu generatePopupMenu(ActionContextImpl context) {
        return MenuFactory.createUserObjectPopupMenu(context);
    }
}
