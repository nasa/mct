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
package gov.nasa.arc.mct.evaluator.component;

import gov.nasa.arc.mct.evaluator.enums.PlaceObjectsInEnumAction;
import gov.nasa.arc.mct.evaluator.expressions.ExpressionsViewManifestation;
import gov.nasa.arc.mct.evaluator.view.EnumeratorViewPolicy;
import gov.nasa.arc.mct.evaluator.view.EvaluatorViewPolicy;
import gov.nasa.arc.mct.evaluator.view.InfoViewManifestation;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class EvaluatorComponentProvider extends AbstractComponentProvider {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("Bundle"); 
	
	private final Collection<ComponentTypeInfo> infos;
	private final Collection<PolicyInfo> policies = new ArrayList<PolicyInfo>();
		
	
	public EvaluatorComponentProvider() {
		infos = Arrays.asList(new ComponentTypeInfo(
				bundle.getString("display_name"),  
				bundle.getString("description"), 
				EvaluatorComponent.class,
				new EvaluatorWizardUI()));
		policies.add(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), EvaluatorViewPolicy.class));
		policies.add(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), EnumeratorViewPolicy.class));
	}

	
	@Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		return infos;
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		if (EvaluatorComponent.class.getName().equals(componentTypeId)) {
			List<ViewInfo> views = new ArrayList<ViewInfo>();
			views.add(new ViewInfo(InfoViewManifestation.class, InfoViewManifestation.VIEW_NAME, ViewType.OBJECT));
			views.add(new ViewInfo(ExpressionsViewManifestation.class, ExpressionsViewManifestation.VIEW_NAME, ExpressionsViewManifestation.class.getName(),
					ViewType.OBJECT,
                    new ImageIcon(ExpressionsViewManifestation.class.getResource("/icons/mct_icon_menu_settings.png"))));
			views.add(new ViewInfo(ExpressionsViewManifestation.class, ExpressionsViewManifestation.VIEW_NAME, ExpressionsViewManifestation.class.getName(), ViewType.CENTER, null, null, true, EvaluatorComponent.class));
			return views;
		}		
		return Collections.singleton(new ViewInfo(InfoViewManifestation.class, InfoViewManifestation.VIEW_NAME, ViewType.OBJECT));
	}


	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
		//TODO add a policy to restrict the children to be feed providers
		return policies;
	}
	
	@Override
	//Add menu items for creating enums with puis selected in My Directory
	public Collection<MenuItemInfo> getMenuItemInfos() {
		return Arrays.asList(
				new MenuItemInfo("/objects/creation.ext", "OBJECTS_CREATE_ENUMS",
                        MenuItemType.NORMAL, PlaceObjectsInEnumAction.class));
		
	}
	
}
