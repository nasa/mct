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
package gov.nasa.arc.mct.core.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.collection.Group;
import gov.nasa.arc.mct.platform.core.access.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;

/**
 * A Policy which enforces the rule that certain actions (generally speaking, 
 * model changes to component) can only be performed by owners, with 
 * exception for group-owned and wildcard-owned components.
 */
public class CheckComponentOwnerIsUserPolicy implements Policy {

    @Override
    public ExecutionResult execute(PolicyContext context) {
        AbstractComponent component = context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class);
        if (component == null)
            return new ExecutionResult(context, false, "Invalid component.");

        // "*" is the wildcard owner, so always allow this
        // Otherwise, check for a match on owner
        // Finally, check if there is Group ownership of this component
        if (!component.getOwner().equals("*") && !component.getOwner().equals(PlatformAccess.getPlatform().getCurrentUser().getUserId())) {
            Group group = component.getCapability(Group.class); // Check for group ownership
            if (group == null || !group.getDiscipline().equals(PlatformAccess.getPlatform().getCurrentUser().getDisciplineId())) {
                return new ExecutionResult(context, false, "User does not own this component.");
            }
        }
            
        
        return new ExecutionResult(context, true, "");
    }

}
