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
package gov.nasa.arc.mct.importExport.provider;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The <code>ImportExportProvider</code> provides menu items to import data into a
 * taxonomy node to the MCT directory and export data from a node.
 * 
 */
public class ImportExportProvider extends AbstractComponentProvider {
	private static ResourceBundle bundle = ResourceBundle
			.getBundle("ImportExportProvider");
	private final List<ComponentTypeInfo> componentTypes;

	public ImportExportProvider() {
		componentTypes = Arrays.asList(
		/*
		 * new ComponentTypeInfo(bundle
		 * .getString("EventsCollectionComponentName"), bundle
		 * .getString("EventsCollectionComponentDescription"),
		 * EventsCollection.class, false),
		 */
		new ComponentTypeInfo(bundle.getString("display_name"), bundle
				.getString("description"), ImportExportComponent.class, false));

		assert componentTypes != null;
	}

	@Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		return componentTypes;
	}

	@Override
	public Collection<MenuItemInfo> getMenuItemInfos() {
		return Arrays.asList(new MenuItemInfo("/objects/additions",
				"EXPORT_MCT_ACTION", MenuItemType.NORMAL, ExportAction.class),
				new MenuItemInfo("/objects/additions", 
						"IMPORT_SUBMENU", 
						SubmenuMenu.class), 
				new MenuItemInfo("/objects/import.ext", 
						"IMPORT_MCT_ACTION",
						MenuItemType.NORMAL, ImportAction.class),
				new MenuItemInfo("/this/additions",
						"EXPORT_THIS_ACTION", MenuItemType.NORMAL, ExportThisAction.class));
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		return Collections.emptyList();
	}
}
