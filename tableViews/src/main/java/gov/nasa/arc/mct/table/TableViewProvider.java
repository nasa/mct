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
package gov.nasa.arc.mct.table;


import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.table.policy.TableViewPolicy;
import gov.nasa.arc.mct.table.view.TableViewManifestation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.ImageIcon;

/**
 * Factory for TableViewRole.
 */
public class TableViewProvider extends AbstractComponentProvider {

	final Collection<PolicyInfo> policyInfos;
	private final Collection<ViewInfo> viewInfos;
	
	/**
	 * Creates a new view provider service object. Initializes the set
	 * of objects that will be provided to the core.
	 */
	public TableViewProvider() {
		policyInfos = Collections.singleton(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), TableViewPolicy.class));
		viewInfos = Arrays.asList(
				new ViewInfo(TableViewManifestation.class, TableViewManifestation.VIEW_ROLE_NAME, "gov.nasa.arc.mct.table.view.TableViewRole", ViewType.OBJECT,
						new ImageIcon(getClass().getResource("/icons/mct_icon_menu_alpha.png"))),
				new ViewInfo(TableViewManifestation.class, TableViewManifestation.VIEW_ROLE_NAME, "gov.nasa.arc.mct.table.view.TableViewRole", ViewType.EMBEDDED,
						new ImageIcon(getClass().getResource("/icons/mct_icon_menu_alpha.png"))));
	}
	
	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		return viewInfos;
	}

	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
		return policyInfos;
	}
	
}
