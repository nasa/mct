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
package gov.nasa.arc.mct.evaluator.view;

import java.util.Collection;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.evaluator.component.MultiComponent;
//import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;

/** Policy to restrict association of components to a multi-input evaluator to only 
 * those that have Feed provider capabilities
 * @author dcberrio
 *
 */
public class MultiCompositionPolicy implements Policy {

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.policy.Policy#execute(gov.nasa.arc.mct.policy.PolicyContext)
	 */
	@Override
	public ExecutionResult execute(PolicyContext context) {
		AbstractComponent multi = context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class);
		@SuppressWarnings("unchecked")
		Collection<AbstractComponent> sources = context.getProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(), Collection.class);
		if (multi.getCapability(MultiComponent.class) != null) {
			if (sources != null) {
				for (AbstractComponent ac : sources) {
					if (ac.getCapability(FeedProvider.class) == null) {
//						OptionBox.showMessageDialog(
//		                        null, 
//		                        "Only objects that provide data can be added\nto a new multi",
//		                        "Error", 
//		                        OptionBox.ERROR_MESSAGE);
						return new ExecutionResult(context, false, "Only objects that provide data can be added to a multi");
					}
				}
			}
		}
		return new ExecutionResult(context, true, "");
	}

}
