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
 * MCTStandardHousingMenuBar.java Aug 18, 2008
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.gui.menu;

import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.ActionManager;
import gov.nasa.arc.mct.gui.impl.MenuExtensionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuBar;

/**
 * Create the menu bar and its contents for the standard housing.
 *
 * @author nshi
 */
@SuppressWarnings("serial")
public class MCTStandardHousingMenuBar extends JMenuBar {
    
    private static Map<String, ContextAwareMenu> menuMap = new HashMap<String, ContextAwareMenu>();
    
    public static void registerMenu(String key, ContextAwareMenu menu) {
        menuMap.put(key, menu);        
    }
    
    public MCTStandardHousingMenuBar(MCTStandardHousing parentHousing) {
        if (parentHousing == null)
            throw new IllegalStateException("Should associate menu bar with a non-null MCTAbstractHousing.");
        instrumentNames();
        ActionContextImpl context = new ActionContextImpl();
        context.setTargetHousing(parentHousing);
        context.setTargetComponent(parentHousing.getWindowComponent());
        context.addTargetViewComponent(parentHousing.getHousedViewManifestation());
        
        List<String> list = new LinkedList<String>();
        list.add("ICON_MENU");
        list.add("THIS_MENU");
        list.add("OBJECTS_MENU");
        list.add("EDIT_MENU");
        list.add("VIEW_MENU");
        list.add("WINDOWS_MENU");
        list.add("CONVENIENCES_MENU");
        
        for (MenuItemInfo info : MenuExtensionManager.getInstance().getExtendedMenus("/")) {
            list.add(info.getCommandKey());
        }
        
        list.add("HELP_MENU");
        
        for (ContextAwareMenu menu : getMenusByContext(list.toArray(new String[list.size()]), context))
            add(menu);
    }

    private void instrumentNames() {
        setName("standardHousingMenuBar");
    }
    
    public List<ContextAwareMenu> getUserObjectMenus(ActionContextImpl context) {
        return getMenusByContext(new String[]{"OBJECTS_MENU", "EDIT_MENU"}, context);        
    }
    
    private List<ContextAwareMenu> getMenusByContext(String[] menuKeys, ActionContextImpl context) {
        List<ContextAwareMenu> list = new ArrayList<ContextAwareMenu>();
        for (String menuKey : menuKeys) {
            ContextAwareMenu menu = ActionManager.getMenu(menuKey, context);
            if (menu != null)
                list.add(menu);
        }
        return list;
    }    
}
