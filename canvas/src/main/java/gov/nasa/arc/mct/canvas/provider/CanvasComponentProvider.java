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
package gov.nasa.arc.mct.canvas.provider;

import gov.nasa.arc.mct.canvas.policy.CanvasFilterViewPolicy;
import gov.nasa.arc.mct.canvas.policy.EmbeddedCanvasViewsAreNotWriteable;
import gov.nasa.arc.mct.canvas.view.CanvasManifestation;
import gov.nasa.arc.mct.canvas.view.ChangeGridSizeAction;
import gov.nasa.arc.mct.canvas.view.ChangeSnapAction;
import gov.nasa.arc.mct.canvas.view.PanelInspector;
import gov.nasa.arc.mct.canvas.view.ReTileAction;
import gov.nasa.arc.mct.canvas.view.WindowChangeGridSizeAction;
import gov.nasa.arc.mct.canvas.view.WindowChangeSnapAction;
import gov.nasa.arc.mct.canvas.view.WindowReTileAction;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.menu.AlignBottomAction;
import gov.nasa.arc.mct.menu.AlignHorizontalCenterAction;
import gov.nasa.arc.mct.menu.AlignLeftAction;
import gov.nasa.arc.mct.menu.AlignRightAction;
import gov.nasa.arc.mct.menu.AlignTopAction;
import gov.nasa.arc.mct.menu.AlignVerticalCenterAction;
import gov.nasa.arc.mct.menu.AlignmentMenu;
import gov.nasa.arc.mct.menu.BorderStylesAction;
import gov.nasa.arc.mct.menu.BorderStylesMenu;
import gov.nasa.arc.mct.menu.BordersAllOrNoneAction;
import gov.nasa.arc.mct.menu.BordersBottomAction;
import gov.nasa.arc.mct.menu.BordersLeftAction;
import gov.nasa.arc.mct.menu.BordersMenu;
import gov.nasa.arc.mct.menu.BordersRightAction;
import gov.nasa.arc.mct.menu.BordersTopAction;
import gov.nasa.arc.mct.menu.BringToFrontAction;
import gov.nasa.arc.mct.menu.ChangeViewAction;
import gov.nasa.arc.mct.menu.ExportPanelAsImageAction;
import gov.nasa.arc.mct.menu.GridMenu;
import gov.nasa.arc.mct.menu.PanelTitleBarAction;
import gov.nasa.arc.mct.menu.RemovePanelAction;
import gov.nasa.arc.mct.menu.SelectAllAction;
import gov.nasa.arc.mct.menu.SendToBackAction;
import gov.nasa.arc.mct.menu.WindowGridMenu;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class CanvasComponentProvider extends AbstractComponentProvider {
    private static ResourceBundle bundle = ResourceBundle.getBundle("CanvasResourceBundle");
    private static final Collection<ViewInfo> VIEW_INFOS =
                    Arrays.asList(
                                    new ViewInfo(CanvasManifestation.class, bundle.getString("Canvas"), "gov.nasa.arc.mct.canvas.view.CanvasView", 
                                          ViewType.OBJECT,
                                          new ImageIcon(CanvasComponentProvider.class.getResource("/icons/mct_icon_menu_canvas.png"))),
                                    new ViewInfo(CanvasManifestation.class, bundle.getString("Canvas"), "gov.nasa.arc.mct.canvas.view.CanvasView", 
                                          ViewType.CENTER),
                                    new ViewInfo(CanvasManifestation.class, bundle.getString("Canvas"), "gov.nasa.arc.mct.canvas.view.CanvasView", 
                                                          ViewType.EMBEDDED,
                                                          new ImageIcon(CanvasComponentProvider.class.getResource("/icons/mct_icon_menu_canvas.png"))), 
                                    new ViewInfo(PanelInspector.class, "Panel Inspector", ViewType.CENTER_OWNED_INSPECTOR));
    @Override
    public Collection<MenuItemInfo> getMenuItemInfos() {
        return Arrays.asList(
                        new MenuItemInfo("/objects/view.ext", "OBJECTS_CHANGE_VIEWS",
                                        MenuItemType.RADIO_GROUP, ChangeViewAction.class),
                        new MenuItemInfo("/objects/export.ext", bundle.getString("ExportViewAsImageCommandKey"),
                                        MenuItemType.NORMAL, ExportPanelAsImageAction.class),
                        new MenuItemInfo("/objects/deletion.ext", "OBJECTS_REMOVE_MANIFESTATION",
                                        MenuItemType.NORMAL, RemovePanelAction.class),
                        new MenuItemInfo("/objects/format.panel.ext", "OBJECTS_ALIGNMENT",
                                        AlignmentMenu.class),
                        // alignment menu actions
                        new MenuItemInfo(AlignmentMenu.OBJECTS_ALIGNMENT_ORIENTATION_EXT,
                                        AlignLeftAction.ACTION_KEY, MenuItemType.NORMAL,
                                        AlignLeftAction.class),

                        new MenuItemInfo(AlignmentMenu.OBJECTS_ALIGNMENT_ORIENTATION_EXT,
                                        AlignRightAction.ACTION_KEY, MenuItemType.NORMAL,
                                        AlignRightAction.class),
                        
                        new MenuItemInfo(AlignmentMenu.OBJECTS_ALIGNMENT_ORIENTATION_EXT,
                                        AlignTopAction.ACTION_KEY, MenuItemType.NORMAL,
                                        AlignTopAction.class),
                                        
                        new MenuItemInfo(AlignmentMenu.OBJECTS_ALIGNMENT_ORIENTATION_EXT,
                                        AlignBottomAction.ACTION_KEY, MenuItemType.NORMAL,
                                        AlignBottomAction.class),
                                        
                        new MenuItemInfo(AlignmentMenu.OBJECTS_ALIGNMENT_ORIENTATION_EXT,
                                        AlignVerticalCenterAction.ACTION_KEY, MenuItemType.NORMAL,
                                        AlignVerticalCenterAction.class),

                        new MenuItemInfo(AlignmentMenu.OBJECTS_ALIGNMENT_ORIENTATION_EXT,
                                        AlignHorizontalCenterAction.ACTION_KEY,
                                        MenuItemType.NORMAL, AlignHorizontalCenterAction.class),

                        new MenuItemInfo("/objects/format.panel.ext", "OBJECTS_BORDERS",
                                        BordersMenu.class),

                        new MenuItemInfo(BordersMenu.OBJECTS_BORDERS_SIDES_EXT,
                                        BordersBottomAction.ACTION_KEY, MenuItemType.CHECKBOX,
                                        BordersBottomAction.class),

                        new MenuItemInfo(BordersMenu.OBJECTS_BORDERS_SIDES_EXT,
                                        BordersTopAction.ACTION_KEY, MenuItemType.CHECKBOX,
                                        BordersTopAction.class),

                        new MenuItemInfo(BordersMenu.OBJECTS_BORDERS_SIDES_EXT,
                                        BordersLeftAction.ACTION_KEY, MenuItemType.CHECKBOX,
                                        BordersLeftAction.class),

                        new MenuItemInfo(BordersMenu.OBJECTS_BORDERS_SIDES_EXT,
                                        BordersRightAction.ACTION_KEY, MenuItemType.CHECKBOX,
                                        BordersRightAction.class),
                        new MenuItemInfo(BordersMenu.OBJECTS_BORDERS_ALLORNONE_EXT,
                                        BordersAllOrNoneAction.ACTION_KEY,
                                        MenuItemType.RADIO_GROUP, BordersAllOrNoneAction.class),
                        new MenuItemInfo("/objects/format.panel.ext", "OBJECTS_PANEL_TITLE_BAR",
                                        MenuItemType.CHECKBOX, PanelTitleBarAction.class),
                        new MenuItemInfo("/objects/format.zorder.ext",
                                        "OBJECTS_FORMATTING_BRING_TO_FRONT", MenuItemType.NORMAL,
                                        BringToFrontAction.class), new MenuItemInfo(
                                        "/objects/format.zorder.ext",
                                        "OBJECTS_FORMATTING_SEND_TO_BACK", MenuItemType.NORMAL,
                                        SendToBackAction.class),
                        new MenuItemInfo("/objects/additions", // NOI18N
                                        "OBJECTS_VIEW_GRIDS", // NOI18N
                                        GridMenu.class),
                        new MenuItemInfo("/objects/grid.size/sizes.ext", // NOI18N
                                        "PANEL_CHANGE_GRID_SIZE", // NOI18N
                                        MenuItemType.RADIO_GROUP, ChangeGridSizeAction.class),
                        new MenuItemInfo("/objects/additions", // NOI18N
                                         "CHANGE_SNAP", // NOI18N
                                         MenuItemType.CHECKBOX, ChangeSnapAction.class),
                        new MenuItemInfo("/objects/additions", // NOI18N
                                         "RETILE", // NOI18N
                                         MenuItemType.NORMAL, ReTileAction.class),
                        new MenuItemInfo("/view/formatting.ext", // NOI18N
                                         "VIEW_GRIDS", // NOI18N
                                         WindowGridMenu.class),
                        new MenuItemInfo(WindowGridMenu.VIEW_GRID_SIZE_SUBMENU_EXT, // NOI18N
                                         "PANEL_CHANGE_GRID_SIZE_WINDOW", // NOI18N
                                         MenuItemType.RADIO_GROUP, WindowChangeGridSizeAction.class),
                        new MenuItemInfo("/view/formatting.ext", // NOI18N
                                        "VIEW_CHANGE_SNAP", // NOI18N
                                        MenuItemType.CHECKBOX, WindowChangeSnapAction.class),
                        new MenuItemInfo("/view/formatting.ext", // NOI18N
                                        "VIEW_RETILE", // NOI18N
                                        MenuItemType.NORMAL, WindowReTileAction.class),
                        new MenuItemInfo("/view/select.ext", // NOI18N
                                        "VIEW_SELECT_ALL", // NOI18N
                                        MenuItemType.NORMAL, SelectAllAction.class),
                        new MenuItemInfo("/objects/format.panel.ext", "OBJECTS_BORDER_STYLES",
                                          BorderStylesMenu.class),          
                                          
                        new MenuItemInfo(BorderStylesMenu.OBJECTS_BORDERS_STYLES_EXT,
                                         BorderStylesAction.ACTION_KEY,
                                         MenuItemType.RADIO_GROUP,
                                         BorderStylesAction.class),
                                        
                        new MenuItemInfo("/objects/format.panel.ext", "OBJECTS_PANEL_TITLE_BAR",
                                        MenuItemType.CHECKBOX, PanelTitleBarAction.class),
                        new MenuItemInfo("/objects/format.zorder.ext",
                                        "OBJECTS_FORMATTING_BRING_TO_FRONT", MenuItemType.NORMAL,
                                        BringToFrontAction.class), new MenuItemInfo(
                                        "/objects/format.zorder.ext",
                                        "OBJECTS_FORMATTING_SEND_TO_BACK", MenuItemType.NORMAL,
                                        SendToBackAction.class));
    }

    @Override
    public Collection<PolicyInfo> getPolicyInfos() {
        return Arrays.asList(
                        new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), CanvasFilterViewPolicy.class),
                        new PolicyInfo(PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey(), EmbeddedCanvasViewsAreNotWriteable.class));
    }

    @Override
    public Collection<ViewInfo> getViews(String componentTypeId) {
        return VIEW_INFOS;
    }
}
