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
package gov.nasa.arc.mct.nontimeplot.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.nontimeplot.view.NonTimePlotView;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;

public class NonTimePolicy implements Policy {

	@Override
	public ExecutionResult execute(PolicyContext context) {
		boolean result = true;
		
		ViewInfo viewInfo = context.getProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), ViewInfo.class);
	
		if (viewInfo.getViewClass().equals(NonTimePlotView.class)) {
			AbstractComponent targetComponent = context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class);
			
			result = false;
			if (!targetComponent.isLeaf()) {
				int feeds = 0;
				for (AbstractComponent child : targetComponent.getComponents()) {
					if (hasFeed(child)) feeds++; 
				}
				result = feeds >= 2;
			}						
		}
		
		String message = result ? "Requested Plot View has two or more children with data feeds." :
								"Requested Plot View did not have two or more children with data feeds.";
		
		return new ExecutionResult(context, result, message);

	}
	
	private boolean hasFeed(AbstractComponent comp) {
		return comp.getCapability(FeedProvider.class) != null;
	}

}
