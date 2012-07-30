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
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.menu.MenuFactory;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.MenuManager;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Implements a manager of extended menu items. The
 * singleton pattern is used to provide a single
 * instance of the manager.
 * 
 * @author nija.shi@nasa.gov
 */
public final class MenuExtensionManager implements MenuManager {
    
    private static final MCTLogger LOGGER = MCTLogger.getLogger(MenuExtensionManager.class);
    private static final MenuExtensionManager manager = new MenuExtensionManager();
    
    private final Map<String, List<MenuItemInfo>> map = new HashMap<String, List<MenuItemInfo>>();
    
    /** A private constructor, to enforce the singleton pattern. */
    private MenuExtensionManager() {
    }
    
    /**
     * Gets the singleton instance of the menu extension manager.
     * 
     * @return the menu extension manager instance
     */
    public static MenuExtensionManager getInstance() {
        return manager;
    }

    /**
     * Refreshes all extended menus by unregistering all extended
     * menu actions and then reregistering all those actions from
     * the given providers.
     * 
     * @param providers the set of providers of extended menu actions
     */
    public synchronized void refreshExtendedMenus(List<ExtendedComponentProvider> providers) {
        // Clean up existing extended actions.
        for (Entry<String, List<MenuItemInfo>> entry : map.entrySet()) {
            List<MenuItemInfo> infos = entry.getValue();
            for (MenuItemInfo info : infos) {
                if (info.getType() == MenuItemType.SUBMENU)
                    ActionManager.unregisterMenu(info.getMenuClass(), info.getCommandKey());
                else
                    ActionManager.unregisterAction(info.getActionClass(), info.getCommandKey());
            }
        }        
        map.clear();
        
        // Add new extended actions.
        for (ExtendedComponentProvider provider : providers) {
            try {
                if (provider.getMenuItemInfos() != null) {
                    for (MenuItemInfo info : provider.getMenuItemInfos()) {
                        registerExtendedMenu(info);
                    }
                }
            } catch (Exception e) {
                // if an exception occurs, log an error for the provider to resolve but
                // continue through the rest of the providers
                LOGGER.error("Error occurred while invoking provider: " + provider.getClass().getName() +
                        " from bundle: " + provider.getBundleSymbolicName(), e);
            }
        }    
    }
    
    /**
     * Registers a new extended menu item.
     * 
     * @param info the information describing the new menu item and the associated action
     */
    public void registerExtendedMenu(MenuItemInfo info) {
        if (info.getType() == MenuItemType.SUBMENU) {
            ActionManager.registerMenu(info.getMenuClass(), info.getCommandKey());
        } else {
            ActionManager.registerAction(info.getActionClass(), info.getCommandKey());
        }

        String menubarPath = info.getMenubarPath();
        List<MenuItemInfo> extendedMenus = map.get(menubarPath);
        if (extendedMenus == null) {
            extendedMenus = new ArrayList<MenuItemInfo>();
        }
            
        extendedMenus.add(info);   
        map.put(menubarPath, extendedMenus);        
    }
    
    /**
     * Gets a list of extended menu items for the given menu path.
     * 
     * @param menubarPath the path describing the menu path to the items
     * @return the list of extended menu items at that path
     */
    public List<MenuItemInfo> getExtendedMenus(String menubarPath) {
        List<MenuItemInfo> list = map.get(menubarPath);
        return list == null ? Collections.<MenuItemInfo>emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * Returns the <code>JPopupMenu</code> for this <code>MCTViewManifestation</code>.
     * @param manifestation the manifestation
     * @return the popup menu
     */
    public JPopupMenu getManifestationPopupMenu(View manifestation) {
        ActionContextImpl context = new ActionContextImpl();
        Container housing = SwingUtilities.getAncestorOfClass(MCTHousing.class, manifestation);
        assert housing instanceof MCTHousing : "All manifestations should be contained in an MCTHousing.";
        context.setTargetHousing((MCTHousing) housing);
        Collection<View> selectedManifestations = 
            context.getTargetHousing().getSelectionProvider().getSelectedManifestations();
        for (View selectedManifestation: selectedManifestations) {
            context.addTargetViewComponent(selectedManifestation);
        }
        return MenuFactory.createUserObjectPopupMenu(context);
    }

    /**
     * Returns the <code>JPopupMenu</code> applicable for this manifestation when
     * viewed in the center pane.
     * @param manifestation the manifestation in the center pane
     * @return the popup menu
     */
    @Override
    public JPopupMenu getViewPopupMenu(View manifestation) {
        ActionContextImpl context = new ActionContextImpl();
        context.setTargetComponent(manifestation.getManifestedComponent());
        context.setTargetHousing((MCTHousing) SwingUtilities.getAncestorOfClass(MCTAbstractHousing.class, manifestation));
        return MenuFactory.createViewPopupMenu(context);
    }
}
