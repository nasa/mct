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
package gov.nasa.arc.mct.gui.monitors;

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.components.DetectGraphicsDevices;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class OpenMultipleMonitorsThisMenu extends ContextAwareMenu {

    private static final Logger logger = LoggerFactory.getLogger(OpenMultipleMonitorsThisMenu.class);
    
    public OpenMultipleMonitorsThisMenu() {
        super(DetectGraphicsDevices.SHOW_THIS_ACTION_MULTIPLE_MONITORS_TEXT);
    }

    @Override
    public boolean canHandle(ActionContext context) {
        
        ActionContextImpl actionContext = (ActionContextImpl) context;
           
        if (actionContext.getWindowManifestation().getManifestedComponent() == null) {
            logger.warn("THIS Object ActionContext.getWindowManifestation().getManifestedComponent() is null!");
            return false;
        }
        
        return (DetectGraphicsDevices.getInstance().getNumberGraphicsDevices() > DetectGraphicsDevices.MINIMUM_MONITOR_CHECK);
    }
    
    @Override
    protected void populate() {
        ArrayList<String> graphicsDeviceNames = DetectGraphicsDevices.getInstance().getGraphicDeviceNames();
        List<MenuItemInfo> menuItemInfoList = new ArrayList<MenuItemInfo>(graphicsDeviceNames.size());
        menuItemInfoList.add(new MenuItemInfo(DetectGraphicsDevices.OPEN_MULTIPLE_MONITORS_THIS_ACTION, MenuItemType.COMPOSITE));
        addMenuItemInfos(DetectGraphicsDevices.THIS_ADDITIONS_MENU_PATH, menuItemInfoList);
    }
}
