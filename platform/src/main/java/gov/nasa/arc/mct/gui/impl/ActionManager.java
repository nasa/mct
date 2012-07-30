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
 * MCTActionManager.java Aug 18, 2008
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.menu.MenuFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the actions in MCT.
 * 
 * @author nshi
 */
public final class ActionManager {
    private static final Logger MENU_TOOLTIP_LOGGER = LoggerFactory.getLogger("gov.nasa.arc.mct.gui.menus");

    private static Map<String, List<Class<? extends ContextAwareMenu>>> commandMenuMap = new HashMap<String, List<Class<? extends ContextAwareMenu>>>();
    private static Map<String, List<Class<? extends ContextAwareAction>>> commandKeyActionMap = new HashMap<String, List<Class<? extends ContextAwareAction>>>();

    /**
     * Registers a new menu within MCT. Each menu within MCT
     * has an identifier, its command key. This command key
     * is used to look up the menu by ID using {@link #getMenu(String, ActionContextImpl)}.
     * 
     * @param menuClass the class that implements the menu
     * @param commandKey the unique identifier for the new menu
     */
    public static void registerMenu(Class<? extends ContextAwareMenu> menuClass, String commandKey) {
        List<Class<? extends ContextAwareMenu>> menuList = commandMenuMap.get(commandKey);
        if (menuList == null) {
            menuList = new ArrayList<Class<? extends ContextAwareMenu>>();
            commandMenuMap.put(commandKey, menuList);
        }
        menuList.add(menuClass);
    }
    
    /**
     * Finds a menu that has the given command key and is applicable
     * in the given action context. If more than one such menu exits,
     * return the first one registered.
     * 
     * @param commandKey the command key of menus to find
     * @param context the action context in which the menu should be applicable
     * @return the applicable menu
     */
    public static ContextAwareMenu getMenu(String commandKey, ActionContextImpl context) {
        List<Class<? extends ContextAwareMenu>> menuList = commandMenuMap.get(commandKey);
        if (menuList != null) {
            for (Class<? extends ContextAwareMenu> menuClass : menuList) {
                try {
                    ContextAwareMenu menu = menuClass.newInstance();
                    if (menu.canHandle(context)) {
                        menu.addMenuListener(MenuFactory.createMenuListener(menu));
                        // Populate built-in menus and/or menu items
                        menu.initialize();
                        // Populate extended menus and/or menu items
                        MenuExtensionManager manager = MenuExtensionManager.getInstance();
                        for (String menubarPath : menu.getExtensionMenubarPaths()) {
                            List<MenuItemInfo> extendedMenus = manager.getExtendedMenus(menubarPath);
                            menu.addMenuItemInfos(menubarPath, extendedMenus);                            
                        }
                        if (MENU_TOOLTIP_LOGGER.isDebugEnabled())
                            menu.setToolTipText(menu.getClass().getName());
                        return menu;
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
       
    /**
     * Registers an action for use within MCT. An action is associated
     * with a command key, which ties a menu entry to an action.
     * 
     * @param actionClass the class implementing the action
     * @param commandKey the command key for the action
     */
    public static void registerAction(Class<? extends ContextAwareAction> actionClass, String commandKey) {
        
        List<Class<? extends ContextAwareAction>> actionClasses = commandKeyActionMap.get(commandKey);
        if (actionClasses == null)
            actionClasses = new ArrayList<Class<? extends ContextAwareAction>>();
        actionClasses.add(actionClass);
        commandKeyActionMap.put(commandKey, actionClasses);
    }
    
    /**
     * Removes an action with the given class and command key.
     *  
     * @param actionClass the class implementing the action
     * @param commandKey the command key for the action to remove
     */
    public static void unregisterAction(Class<? extends ContextAwareAction> actionClass, String commandKey) {

        List<Class<? extends ContextAwareAction>> list = commandKeyActionMap.get(commandKey);
        list.remove(actionClass);
        if (list.isEmpty()) commandKeyActionMap.remove(commandKey);        
    }

    /**
     * Removes a submenu with the given class and command key.
     *  
     * @param menuClass the class implementing the submenu
     * @param commandKey the command key for the submenu to remove
     */
    public static void unregisterMenu(Class<? extends ContextAwareMenu> menuClass, String commandKey) {

        List<Class<? extends ContextAwareMenu>> list = commandMenuMap.get(commandKey);
        list.remove(menuClass);
        if (list.isEmpty()) commandKeyActionMap.remove(commandKey);        
    }

    
    /**
     * Removes a single action object.
     * 
     * @param action the action to remove
     */
    public static void deregisterAction(ContextAwareAction action) {
        if (action.getValue(Action.ACTION_COMMAND_KEY) != null) {
            if (commandKeyActionMap.containsKey(action.getValue(Action.ACTION_COMMAND_KEY))) {
                if (commandKeyActionMap.get(action.getValue(Action.ACTION_COMMAND_KEY)) != null) {
                    commandKeyActionMap.remove(action.getValue(Action.ACTION_COMMAND_KEY));
                }
            }
        }        
    }

    /**
     * Returns the first action that can handle the context.
     * @param key commandKey
     * @param context action context
     * @return an action or null if none applicable 
     */
    public static ContextAwareAction getAction(String key, ActionContextImpl context) {
        List<Class<? extends ContextAwareAction>> actionClassList = commandKeyActionMap.get(key);
        if (actionClassList == null)
            return null;

        for (Class<? extends ContextAwareAction> actionClass : actionClassList) {
            try {
                ContextAwareAction action = actionClass.newInstance();
                if (action.canHandle(context))
                    return action;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }        
}
