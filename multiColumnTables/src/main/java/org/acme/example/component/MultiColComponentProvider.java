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

import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import org.acme.example.policy.MultiColViewPolicy;
import org.acme.example.view.MultiColView;

public class MultiColComponentProvider extends AbstractComponentProvider {

	// use a resource bundle for strings to enable localization in the future if required
	private static ResourceBundle bundle = ResourceBundle.getBundle("MultiColResourceBundle"); 
	
	/* Our Views associated with the MultiColumn plug-in: (from the getViews method) MCT knows that the class 'MultiColView' is providing
	 * visualizations 
	 *    
	 *  ViewType.OBJECT is for a view that can be displayed in the (MCT) Inspector View
	 *  ViewType.EMBEDDED is for a view that can be displayed in another view.  We have this so we can put MercatorProjections
	 *  within a (MCT) Canvas View
	 */
	private static final Collection<ViewInfo> VIEWS = Arrays.asList(
			new ViewInfo(MultiColView.class, bundle.getString("MultiColViewName"), ViewType.OBJECT),
			new ViewInfo(MultiColView.class, bundle.getString("MultiColViewName"), ViewType.EMBEDDED)
	);
	
	/* Our policies associated with this plug-in:  here we tell MCT that the class 'MultiColViewPolicy'
	 * is handling the policies for the MultiColumn plug-in
	 * 
	 */
	private static final Collection<PolicyInfo> POLICIES = Arrays.asList(
			new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), MultiColViewPolicy.class)
	);
	
	
	/*
	 * Here is where MCT talks to our plug-in: our plug-in tells MCT
	 * what views are associated with this plug-in.
	 */
	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		return VIEWS;
	}
	
	/*
	 * Here is where MCT learns of the polices associated with this plug-in
	 */
	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
        return POLICIES;
	}
	
	
}