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
package gov.nasa.arc.mct.fastplot;

import gov.nasa.arc.mct.fastplot.policy.PlotStringPolicy;
import gov.nasa.arc.mct.fastplot.policy.PlotViewPolicy;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

/**
 * Factory for PlotViewRole.
 */
public class PlotViewProvider extends AbstractComponentProvider {

	final Collection<PolicyInfo> policyInfos;
	private final List<ViewInfo> viewInfos;
	
	private static final ImageIcon ICON =
			new ImageIcon(PlotViewProvider.class.getResource("/icons/mct_icon_menu_plot.png"));
	
	public PlotViewProvider() {
		policyInfos = Arrays.asList(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), PlotViewPolicy.class),
								    new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), PlotStringPolicy.class)
									);
		
		viewInfos = Arrays.asList(
				new ViewInfo(PlotViewManifestation.class, PlotViewManifestation.VIEW_ROLE_NAME, "gov.nasa.arc.mct.fastplot.view.PlotViewRole", ViewType.OBJECT),
				new ViewInfo(PlotViewManifestation.class, PlotViewManifestation.VIEW_ROLE_NAME, "gov.nasa.arc.mct.fastplot.view.PlotViewRole", ViewType.CENTER),
				new ViewInfo(PlotViewManifestation.class, PlotViewManifestation.VIEW_ROLE_NAME, "gov.nasa.arc.mct.fastplot.view.PlotViewRole", ViewType.EMBEDDED));
	}
	
	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		return viewInfos;
	}

	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
		return policyInfos;
	}
	
    @Override
    public <T> T getAsset(TypeInfo<?> typeInfo, Class<T> assetClass) {
        if (assetClass.isAssignableFrom(ImageIcon.class) && 
            typeInfo.getTypeClass().equals(PlotViewManifestation.class)) {
            return assetClass.cast(ICON);
        }
        return super.getAsset(typeInfo, assetClass);
    }
}
