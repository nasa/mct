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
package gov.nasa.arc.mct.nontimeplot;

import gov.nasa.arc.mct.nontimeplot.policy.NonTimePolicy;
import gov.nasa.arc.mct.nontimeplot.view.NonTimePlotView;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class NonTimePlotComponentProvider extends AbstractComponentProvider {

	private Collection<PolicyInfo> policies = 
		Collections.singleton(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), NonTimePolicy.class));
	private Collection<ViewInfo>   views =
		Arrays.asList(new ViewInfo(NonTimePlotView.class, "Non Time",
						"gov.nasa.arc.mct.nontimeplot.NonTimePlotView",
						ViewType.OBJECT),
				
				new ViewInfo(NonTimePlotView.class, "Non Time",
						"gov.nasa.arc.mct.nontimeplot.NonTimePlotView",
						ViewType.EMBEDDED));
	
	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
		return policies; 
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		return views;
	}
}
