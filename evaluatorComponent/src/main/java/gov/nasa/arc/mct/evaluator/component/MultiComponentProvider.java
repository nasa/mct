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

import gov.nasa.arc.mct.evaluator.enums.PlaceObjectsInMultiAction;
import gov.nasa.arc.mct.evaluator.expressions.MultiViewManifestation;
import gov.nasa.arc.mct.evaluator.view.EnumeratorViewPolicy;
import gov.nasa.arc.mct.evaluator.view.EvaluatorViewPolicy;
import gov.nasa.arc.mct.evaluator.view.InfoViewManifestation;
import gov.nasa.arc.mct.evaluator.view.MultiChildRemovalPolicy;
import gov.nasa.arc.mct.evaluator.view.MultiCompositionPolicy;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/** An object to provide component policies and views
 * @author dcberrio
 *
 */
public class MultiComponentProvider extends AbstractComponentProvider {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("MultiComponent"); 
	
	private final Collection<ComponentTypeInfo> infos;
	private final Collection<PolicyInfo> policies = new ArrayList<PolicyInfo>();
		
	
	public MultiComponentProvider() {
		infos = Arrays.asList(new ComponentTypeInfo(
				bundle.getString("display_name"),  
				bundle.getString("description"), 
				MultiComponent.class));
		policies.add(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), EvaluatorViewPolicy.class));
		policies.add(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), EnumeratorViewPolicy.class));
		policies.add(new PolicyInfo(PolicyInfo.CategoryType.CAN_REMOVE_MANIFESTATION_CATEGORY.getKey(), MultiChildRemovalPolicy.class));
		policies.add(new PolicyInfo(PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey(), MultiCompositionPolicy.class));
	}

	
	@Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		return infos;
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		if (MultiComponent.class.getName().equals(componentTypeId)) {
			List<ViewInfo> views = new ArrayList<ViewInfo>();
			views.add(new ViewInfo(InfoViewManifestation.class, InfoViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), ViewType.OBJECT, false, MultiComponent.class));
			views.add(new ViewInfo(MultiViewManifestation.class, MultiViewManifestation.VIEW_NAME, ViewType.OBJECT));
			views.add(new ViewInfo(MultiViewManifestation.class, MultiViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), ViewType.CENTER, true, MultiComponent.class));
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
	//Add menu items for creating multi with puis selected in My Directory
	public Collection<MenuItemInfo> getMenuItemInfos() {
		return Arrays.asList(
				new MenuItemInfo("/objects/creation.ext", "OBJECTS_CREATE_MULTIS",
                        MenuItemType.NORMAL, PlaceObjectsInMultiAction.class));
		
	}
	
    @Override
    public <T> T getAsset(TypeInfo<?> typeInfo, Class<T> assetClass) {
        if (assetClass.isAssignableFrom(CreateWizardUI.class)) {
        	if (typeInfo.getTypeClass().equals(MultiComponent.class)) {
        		return assetClass.cast(new MultiWizardUI());
        	}
        }
        return super.getAsset(typeInfo, assetClass);
    }
}
