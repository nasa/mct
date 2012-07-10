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

import gov.nasa.arc.mct.gui.ActionContextImpl;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.menu.MenuFactory;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Mouse listener used for triggering popup menus in the inspector.
 * 
 * @author atomo
 */
public class MCTPopupOpenerForInspector extends MouseAdapter {

    private final static MCTLogger logger = MCTLogger.getLogger(MCTPopupOpenerForInspector.class);

    private Inspector inspector = null;

    /**
     * This mouse listener uses the MCT component and view manifestation to build an Action context;
     * and the view manifestation to anchor the popup menu display.
     * This version has the ability to reach back into the calling class to get the latest list of manifestations.
     * @param comp An MCT component.
     */
    public MCTPopupOpenerForInspector(Inspector inspector) {
        this.inspector = inspector;
    }

//    @Override
//    public void mouseClicked(MouseEvent e) {
//        if (e.isPopupTrigger())
//            showPopupMenu(e);
//    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3)
            showPopupMenu(e);
    }

//    @Override
//    public void mouseReleased(MouseEvent e) {
//        if (e.isPopupTrigger())
//            showPopupMenu(e);
//    }

    private void showPopupMenu(MouseEvent e) {
        Set<View> viewManifestationSet = new HashSet<View>();
        if (inspector != null) {
            View selectedManifestation = inspector.getHousedViewManifestation();
            if (selectedManifestation != null) {
                viewManifestationSet.add(selectedManifestation);
            }
        }
        if (viewManifestationSet.size() == 0) {
            logger.error("View Manifestation count = 0, should be non-zero. MCTPopupOpenerForInspector");
            return;
        }

        // Set action context
        ActionContextImpl context = new ActionContextImpl();
        context.setTargetComponent(viewManifestationSet.iterator().next().getManifestedComponent());
        context.setInspectorComponent(inspector.getCurrentlyShowingComponent());

        context.setTargetHousing((MCTHousing) SwingUtilities.getAncestorOfClass(MCTAbstractHousing.class, viewManifestationSet.iterator().next()));
        for (View viewManifestation: viewManifestationSet) {
            context.addTargetViewComponent(viewManifestation);
        }
        JPopupMenu popupMenu = MenuFactory.createUserObjectPopupMenu(context);
        popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
        popupMenu.validate();
    }

}
//MODI-790
