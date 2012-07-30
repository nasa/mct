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
 * UserEnvironment.java Aug 18, 2008
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.platform;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.DetectGraphicsDevices;
import gov.nasa.arc.mct.gui.actions.AboutMCT;
import gov.nasa.arc.mct.gui.actions.AboutMCTLicenses;
import gov.nasa.arc.mct.gui.actions.ChangeHousingViewAction;
import gov.nasa.arc.mct.gui.actions.ConveniencesOpenMineGroupAction;
import gov.nasa.arc.mct.gui.actions.ConveniencesOpenUserEnvAction;
import gov.nasa.arc.mct.gui.actions.DeleteObjectAction;
import gov.nasa.arc.mct.gui.actions.DuplicateAction;
import gov.nasa.arc.mct.gui.actions.HelpMCTAction;
import gov.nasa.arc.mct.gui.actions.IconOpenAction;
import gov.nasa.arc.mct.gui.actions.InspectorPaneRevertToCommitted;
import gov.nasa.arc.mct.gui.actions.ListWindowsAction;
import gov.nasa.arc.mct.gui.actions.MemoryMeterAction;
import gov.nasa.arc.mct.gui.actions.ObjectsOpenAction;
import gov.nasa.arc.mct.gui.actions.ObjectsSaveAction;
import gov.nasa.arc.mct.gui.actions.PlaceObjectsInCollectionAction;
import gov.nasa.arc.mct.gui.actions.CenterPaneRevertToCommitted;
import gov.nasa.arc.mct.gui.actions.RedrawDataAction;
import gov.nasa.arc.mct.gui.actions.RemoveManifestationAction;
import gov.nasa.arc.mct.gui.actions.ResetFeedViewsAction;
import gov.nasa.arc.mct.gui.actions.ShowHideControlArea;
import gov.nasa.arc.mct.gui.actions.ThisOpenAction;
import gov.nasa.arc.mct.gui.actions.ThisSaveAction;
import gov.nasa.arc.mct.gui.actions.ViewShowControlAreaAction;
import gov.nasa.arc.mct.gui.actions.WindowsExclusiveCloseAction;
import gov.nasa.arc.mct.gui.formatting.actions.AlignToDecimalAction;
import gov.nasa.arc.mct.gui.formatting.actions.ShowCanvasTitleBarAction;
import gov.nasa.arc.mct.gui.impl.ActionManager;
import gov.nasa.arc.mct.gui.menu.NewMenu;
import gov.nasa.arc.mct.gui.menu.NewObjectAction;
import gov.nasa.arc.mct.gui.menu.housing.ConveniencesMenu;
import gov.nasa.arc.mct.gui.menu.housing.EditMenu;
import gov.nasa.arc.mct.gui.menu.housing.HelpMenu;
import gov.nasa.arc.mct.gui.menu.housing.IconMenu;
import gov.nasa.arc.mct.gui.menu.housing.ObjectsMenu;
import gov.nasa.arc.mct.gui.menu.housing.ThisMenu;
import gov.nasa.arc.mct.gui.menu.housing.ViewMenu;
import gov.nasa.arc.mct.gui.menu.housing.WindowsMenu;
import gov.nasa.arc.mct.gui.monitors.OpenMultipleMonitorsObjectsAction;
import gov.nasa.arc.mct.gui.monitors.OpenMultipleMonitorsObjectsMenu;
import gov.nasa.arc.mct.gui.monitors.OpenMultipleMonitorsThisAction;
import gov.nasa.arc.mct.gui.monitors.OpenMultipleMonitorsThisMenu;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.PluginStartupStatus;
import gov.nasa.arc.mct.services.component.ProviderDelegate;
import gov.nasa.arc.mct.services.component.ProviderDelegateService;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Create the User Environment.
 * 
 * 
 */

public class UserEnvironment {
 
    protected UserEnvironment() {
        initUI();
    }

    private void initUI() {
        // Get root component for instantiating user environment window.
        AbstractComponent root = PlatformAccess.getPlatform().getRootComponent();
        
        Collection<AbstractComponent> bootstrapComponents = PlatformAccess.getPlatform().getBootstrapComponents();
        root.addDelegateComponents(bootstrapComponents);
        
        // Register standard housing actions.
        ActionManager.registerAction(NewObjectAction.class, "OBJECTS_NEW_ACTION");
        ActionManager.registerAction(PlaceObjectsInCollectionAction.class, "OBJECTS_PLACE_OBJS_IN_COLLECTION");
        ActionManager.registerAction(DuplicateAction.class, "OBJECTS_DUPLICATE");
        ActionManager.registerAction(RemoveManifestationAction.class, "OBJECTS_REMOVE_MANIFESTATION");
        ActionManager.registerAction(DeleteObjectAction.class, "DELETE_OBJECTS");
        ActionManager.registerAction(ShowHideControlArea.class, "VIEW_CONTROL");
        ActionManager.registerAction(AboutMCT.class, "HELP_ABOUT");
        ActionManager.registerAction(AboutMCTLicenses.class, "HELP_LICENSES");
        ActionManager.registerAction(MemoryMeterAction.class, "HELP_MEMORY");
        ActionManager.registerAction(HelpMCTAction.class, "HELP_MCT");
        ActionManager.registerAction(ThisOpenAction.class, "THIS_OPEN_ACTION_ID");
        ActionManager.registerAction(IconOpenAction.class, "ICON_OPEN_ACTION");
        ActionManager.registerAction(ObjectsOpenAction.class, "OBJECTS_OPEN");
        ActionManager.registerAction(ObjectsSaveAction.class, "OBJECTS_SAVE");
        ActionManager.registerAction(InspectorPaneRevertToCommitted.class, "OBJECT_REVERT_TO_COMMITTED");
        ActionManager.registerAction(ThisSaveAction.class, "THIS_SAVE_ACTION");
        ActionManager.registerAction(CenterPaneRevertToCommitted.class, "VIEW_REVERT_TO_COMMITTED");
        ActionManager.registerAction(ViewShowControlAreaAction.class, "VIEW_CONTROL_AREAS");
        ActionManager.registerAction(ChangeHousingViewAction.class, "VIEW_CHANGE_HOUSING");
        ActionManager.registerAction(WindowsExclusiveCloseAction.class, "WINDOW_EXCLUSIVE_CLOSE");
        ActionManager.registerAction(ListWindowsAction.class, "LIST_WINDOWS");
        ActionManager.registerAction(ConveniencesOpenMineGroupAction.class, "CONVENIENCES_OPEN_MINE_GROUP");
        ActionManager.registerAction(ConveniencesOpenUserEnvAction.class, "CONVENIENCES_OPEN_USER_ENV");
        ActionManager.registerAction(ResetFeedViewsAction.class, "RESET_FEED_VIEWS_ACTION");
        ActionManager.registerAction(RedrawDataAction.class, "REDRAW_DATA_ACTION");
        
        // Register formatting actions
        ActionManager.registerAction(AlignToDecimalAction.class, "OBJECTS_ALIGNMENT_TO_DECIMAL");
        ActionManager.registerAction(ShowCanvasTitleBarAction.class, "VIEW_SHOW_CANVAS_TITLE_BAR");
        
        //Register menu
        ActionManager.registerMenu(IconMenu.class, "ICON_MENU");
        ActionManager.registerMenu(ThisMenu.class, "THIS_MENU");
        ActionManager.registerMenu(ObjectsMenu.class, "OBJECTS_MENU");
        ActionManager.registerMenu(EditMenu.class, "EDIT_MENU");
        ActionManager.registerMenu(ViewMenu.class, "VIEW_MENU");
        ActionManager.registerMenu(WindowsMenu.class, "WINDOWS_MENU");
        ActionManager.registerMenu(ConveniencesMenu.class, "CONVENIENCES_MENU");
        ActionManager.registerMenu(HelpMenu.class, "HELP_MENU");
        ActionManager.registerMenu(NewMenu.class, "OBJECTS_NEW_MENU");
        
        List<PluginStartupStatus> statuses = new LinkedList<PluginStartupStatus>();
        // Check plugin startup statuses
        ProviderDelegateService providerDelegateService = PlatformImpl.getInstance().getProviderDelegateService();
        for (ProviderDelegate delegate : providerDelegateService.getDelegates()) {
            PluginStartupStatus status = delegate.check();
            if (!status.getStatus())
                statuses.add(status);
        }
        if (!statuses.isEmpty())
            new PluginStartupStatusDialog(statuses);

        // Detected multiple graphics monitor devices (more than 1)
        // Creates menu items for each graphics monitor devices available
        ActionManager.registerMenu(OpenMultipleMonitorsObjectsMenu.class, DetectGraphicsDevices.OBJECTS_OPEN_MULTIPLE_MONITORS_MENU);
        ActionManager.registerMenu(OpenMultipleMonitorsThisMenu.class, DetectGraphicsDevices.THIS_OPEN_MULTIPLE_MONITORS_MENU);  
        ActionManager.registerAction(OpenMultipleMonitorsObjectsAction.class, DetectGraphicsDevices.OPEN_MULTIPLE_MONITORS_OBJECTS_ACTION);
        ActionManager.registerAction(OpenMultipleMonitorsThisAction.class, DetectGraphicsDevices.OPEN_MULTIPLE_MONITORS_THIS_ACTION);
            
        root.open();
    }
}
