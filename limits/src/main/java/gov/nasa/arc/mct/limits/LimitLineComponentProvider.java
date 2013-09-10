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
package gov.nasa.arc.mct.limits;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;


public class LimitLineComponentProvider extends AbstractComponentProvider {

	private static final ResourceBundle bundle = ResourceBundle.getBundle("Limits"); 
	private final ComponentTypeInfo componentTypeInfo;

	public LimitLineComponentProvider() {

		componentTypeInfo = new ComponentTypeInfo(
				bundle.getString("display_name"), 
				bundle.getString("description"), 
				LimitLineComponent.class, 
				new LimitLineCreateWizardUI()
				);
	}

	@Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		return Collections.singleton(componentTypeInfo);
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		return Collections.emptyList();
	}

	@Override
	public Collection<MenuItemInfo> getMenuItemInfos() {
		return Collections.emptyList();
	}

	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
		return Collections.emptyList();
	}

}
