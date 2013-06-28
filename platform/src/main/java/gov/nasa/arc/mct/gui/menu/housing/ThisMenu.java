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
package gov.nasa.arc.mct.gui.menu.housing;

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.util.property.MCTProperties;
import gov.nasa.arc.mct.components.DetectGraphicsDevices;

import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * This Menu.
 */
@SuppressWarnings("serial")
public class ThisMenu extends ContextAwareMenu {
    private static final String THIS_ADDITIONS = "/this/additions";
    private static final String THIS_OPEN_EXT = "/this/open.ext";
    
    private static final ResourceBundle bundle = ResourceBundle.getBundle("gov/nasa/arc/mct/gui/actions/Bundle");
    
    public ThisMenu() {
        // Note that name can be overridden either through mct.properties or system properties
        super(MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty("mct.menu.this", bundle.getString("ThisMenu.label")), new String[] { THIS_ADDITIONS });
    }

    @Override
    public boolean canHandle(ActionContext context) {
        return true;
    }
    
    @Override
    protected void populate() {
        addMenuItemInfos(THIS_OPEN_EXT, 
                Arrays.asList(
                        new MenuItemInfo("THIS_OPEN_ACTION_ID", MenuItemType.NORMAL),
                        new MenuItemInfo(DetectGraphicsDevices.THIS_OPEN_MULTIPLE_MONITORS_MENU, MenuItemType.SUBMENU),
                        new MenuItemInfo("THIS_SAVE_ACTION", MenuItemType.NORMAL),
                        new MenuItemInfo("THIS_SAVE_ALL_ACTION", MenuItemType.NORMAL),
                        new MenuItemInfo("VIEW_REVERT_TO_COMMITTED", MenuItemType.NORMAL),
                        new MenuItemInfo(bundle.getString("ExportThisAsImageCommandKey"), MenuItemType.NORMAL)
                ));
    }
    
}
