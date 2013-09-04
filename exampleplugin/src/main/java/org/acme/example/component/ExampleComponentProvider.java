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
package org.acme.example.component;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.acme.example.actions.APICreationAction;
import org.acme.example.actions.AboutExampleAction;
import org.acme.example.actions.AddOrRemoveNodeBorderAction;
import org.acme.example.actions.BeepAction;
import org.acme.example.actions.SubmenuMenu;
import org.acme.example.policy.FilterViewPolicy;
import org.acme.example.telemetry.TelemetryComponent;
import org.acme.example.view.CenterPanePanel;
import org.acme.example.view.PrivateInfoView;
import org.acme.example.view.PublicInfoView;
import org.acme.example.view.SaveModelStateView;
import org.acme.example.view.ShowChildrenInTableView;

/**
 * The <code>ExampleComponentProvider</code> class exposes the <code>ExampleComponent</code> to MCT. 
 */
public class ExampleComponentProvider extends AbstractComponentProvider {

	// use a resource bundle for strings to enable localization in the future if required
	private static ResourceBundle bundle = ResourceBundle.getBundle("ExampleResourceBundle"); 
	
	private final ComponentTypeInfo exampleComponentType;
	private final ComponentTypeInfo telemetryComponentType;
		
	public ExampleComponentProvider() {
		// this is the component type we are providing. The display name and description are used
		// by the MCT menu system for creating new instances and thus should be human readable
		// and descriptive
		exampleComponentType = new ComponentTypeInfo(
				bundle.getString("display_name"),  
				bundle.getString("description"), 
				ExampleComponent.class);
		telemetryComponentType = new ComponentTypeInfo(
				bundle.getString("telemetry_display_name"),  
				bundle.getString("telemetry_description"), 
				TelemetryComponent.class,
				true,
				new ImageIcon(getClass().getResource("/icons/mct_icon_telemetry.png")));
	}

	
	@Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		// return the component types provided
		return Arrays.asList(exampleComponentType, telemetryComponentType);
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {

		// return a view if desired for the components being created. Note that this method is called
		// for every component type so a view could be supplied for any component type not just
		// components supplied by this provider.
		
		// Also, note that the default node view, canvas view, and housing view will be supplied
		// by the MCT platform.
		if (componentTypeId.equals(ExampleComponent.class.getName())) {
			return Arrays.asList(
					new ViewInfo(CenterPanePanel.class, bundle.getString("CenterPaneViewName"), ViewType.CENTER),
					new ViewInfo(CenterPanePanel.class, bundle.getString("CenterPaneViewName"), ViewType.OBJECT),
					new ViewInfo(PublicInfoView.class, bundle.getString("PublicViewName"), ViewType.OBJECT),
					new ViewInfo(PrivateInfoView.class, bundle.getString("PrivateViewName"), ViewType.OBJECT),
					new ViewInfo(SaveModelStateView.class, bundle.getString("SaveModelStateViewName"), ViewType.OBJECT),
					new ViewInfo(ShowChildrenInTableView.class, bundle.getString("ShowChildrenInTableViewName"), ViewType.OBJECT)
					
			);
		}
		return Collections.emptyList();
	}
	
	@Override
	public Collection<MenuItemInfo> getMenuItemInfos() {
		return Arrays.asList(
				// Additional menu items can be added to the following 
				// menus with the corresponding menubarPaths:
				// This    => /this/additions
				// Objects => /objects/additions
				// Help    => /help/additions
				
				// add menu items to help -- this will show up as a help topic for the example plugin
				new MenuItemInfo(
						"/help/additions", // NOI18N
						"ABOUT_EXAMPLE_ACTION", //NO18N
						MenuItemType.NORMAL,
						AboutExampleAction.class),
				// add menu item to the objects menu -- this will demonstrate programmatic creation for ExampleComponents
				new MenuItemInfo(
								"/objects/additions", // NOI18N
								"API_CREATION_ACTION", //NO18N
								MenuItemType.NORMAL,
								APICreationAction.class),
				// add menu item to the objects menu -- this will be inline as a radio button group under the objects menu
				new MenuItemInfo(
						"/objects/additions", //NOI18N
						"CHANGE_COLOR_ACTION", //NOI18N
						MenuItemType.RADIO_GROUP,
						AddOrRemoveNodeBorderAction.class),
				new MenuItemInfo(
						"/objects/additions", //NOI18N
						"SUBMENU", //NOI18N
						SubmenuMenu.class),
				new MenuItemInfo(
						"objects/submenu.ext", //NOI18N
						"OBJECTS_SUBMENU_BEEP", //NOI18N
						MenuItemType.NORMAL,
						BeepAction.class));
	}

	
	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
		/*
		 * Here is an example of registering a policy to a platform provided
		 * policy category. Platform-provided policy categories are defined
		 * in PolicyInfo.CatetoryType, which is an enum.
		 * A new category can also be added by passing in a unique String to
		 * the category name in PolicyInfo constructor.
		 */
		return Collections.singletonList(
				new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(),
							   FilterViewPolicy.class));
		
		/* 
		 * External plugins can execute a policy category by accessing the
		 * PolicyManager, which is available as an OSGi service.
		 * 
		 * To access the PolicyManager, a class PolicyManagerAccess should be created.
		 * This class is used to inject an instance of the PolicyManager using declarative 
		 * services (see OSGI-INF/component.xml for examples for ComponentRegistryAccess).
		 *
		 * The following code snippet shows how to execute a policy category:
		 * 
		 *   PolicyManager policyManager = PolicyManagerAccess.getPolicyManager();
		 *   PolicyContext context = new PolicyContext();
		 *   context.setProperty(String key, Object value);
		 *   ...
		 *   ... maybe more properties to be set
		 *   ...
		 *   ExecutionResult result = 
		 *     policyManager.execute(String categoryKey, PolicyContext context); 
		 */
	}
}
