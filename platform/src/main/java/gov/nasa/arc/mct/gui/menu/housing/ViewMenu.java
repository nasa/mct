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

import java.util.Arrays;
import java.util.Collections;

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;


/**
 * View Menu.
 * 
 * @author nshi
 */
@SuppressWarnings("serial")
public class ViewMenu extends ContextAwareMenu {
    
    private static final String VIEW_REFRESH_EXT = "/view/refresh.ext";
    private static final String VIEW_SELECT_EXT = "/view/select.ext";
    private static final String VIEW_AREAS_VISIBILITY_EXT = "/view/areas.visibility.ext";
    private static final String VIEW_FORMATTING_EXT = "/view/formatting.ext";
    private static final String VIEW_HOUSING_EXT ="/view/housing.ext";

    public ViewMenu() {
        super("View", new String[]{VIEW_FORMATTING_EXT});        
    }
    
    @Override
    public boolean canHandle(ActionContext context) {
        return true;
    }

    @Override
    protected void populate() {
        addMenuItemInfos(VIEW_SELECT_EXT, Arrays.asList(
                         new MenuItemInfo("VIEW_SELECT_ALL", MenuItemType.NORMAL)));
        
        addMenuItemInfos(VIEW_HOUSING_EXT, Collections.<MenuItemInfo>singleton(new MenuItemInfo("VIEW_CHANGE_HOUSING", MenuItemType.RADIO_GROUP)));
        
        addMenuItemInfos(VIEW_FORMATTING_EXT, Arrays.asList(
                new MenuItemInfo("VIEW_CHANGE_FORMAT_MODE", MenuItemType.RADIO_GROUP),
                new MenuItemInfo("VIEW_GRIDS", MenuItemType.SUBMENU)));

        addMenuItemInfos(VIEW_AREAS_VISIBILITY_EXT, Arrays.asList(
                new MenuItemInfo("VIEW_SHOW_CANVAS_TITLE_BAR", MenuItemType.CHECKBOX),
                new MenuItemInfo("VIEW_CONTROL_AREAS", MenuItemType.NORMAL)));
        
        addMenuItemInfos(VIEW_REFRESH_EXT, Arrays.asList(
                new MenuItemInfo("VIEW_REFRESH_ACTION", MenuItemType.NORMAL)
                ));
    } 
}
