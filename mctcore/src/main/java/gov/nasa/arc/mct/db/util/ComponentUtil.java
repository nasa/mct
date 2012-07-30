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
package gov.nasa.arc.mct.db.util;

import gov.nasa.arc.mct.components.AbstractComponent;

import java.util.Collection;

/**
 * Component utility.
 */
public class ComponentUtil {
    
    /**
     * Checks for contains child component.
     * @param parent - parent's abstract component.
     * @param childComponent - child's abstract component.
     * @return boolean - flag to check whether it contains a child component.
     */
    public static boolean containsChildComponent(AbstractComponent parent, AbstractComponent childComponent) {
        
        for (AbstractComponent referencedComponent : parent.getComponents()) {
            if (referencedComponent.getComponentId().equals(childComponent.getComponentId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for contains a collection of child components.
     * @param parent - parent's abstract component.
     * @param childComponents - collection. 
     * @return boolean - flag to check whether it contains a collection of child components.
     */
    public static boolean containsChildComponents(AbstractComponent parent, Collection<AbstractComponent> childComponents) {
        for (AbstractComponent childComponent : childComponents) {
            if (!containsChildComponent(parent, childComponent)) {
                return false;
            }
        }
        return true;
    }
    
}
