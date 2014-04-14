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
package gov.nasa.arc.mct.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Some components are capable of aggregating modifications made to other 
 * objects, typically in support of "Save All." Since the "All" in "Save All" 
 * may vary from view to view and component to component, this is communicated 
 * through a capability.
 * 
 * ObjectManager instances must be available from {@link gov.nasa.arc.mct.components.AbstractComponent#getCapability(Class)}. 
 * @author vwoeltje
 */
public interface ObjectManager {
    /**
     * For Save All action. This method should return a set of modified objects
     * to be saved. These modified objects are not required to be related to this object.
     * The save all action automatically includes saving changes for this object, so this
     * method does not need to include this object to be saved.
     *
     * This method should never return null.
     *
     * @return a set of <code>AbstractComponent</code> that have been modified and are ready to be saved
     */
    public Set<AbstractComponent> getAllModifiedObjects();
    
    /**
     * Include another object in the list of modified objects.
     * For instance, a view in which multiple changes are made to other 
     * visible objects may wish to add these to the set returned by 
     * getAllModifiedObjects.
     * Implementations of ObjectManager are free to disregard this 
     * method call, in which case they should return false.
     * 
     * @param comp the component to include
     * @return true if the component was added; otherwise false
     */
    public boolean addModifiedObject(AbstractComponent comp);
    
    /**
     * Used by save all to notify an object that a specific 
     * set of modified objects has been saved.
     * @param modified the objects that were saved.
     */
    public void notifySaved(Set<AbstractComponent> modified);
    
    /**
     * A default implementation which aggregates modified objects from 
     * among the children of a given object.
     */
    public static class DirtyObjectManager implements ObjectManager {
        /**
         * The component from whom this capability was retrieved.
         */
        private AbstractComponent component;
        
        /**
         * Since this form visits children, there is a risk of getting 
         * caught in cycles. Maintain a list of active callers of 
         * getAllModifiedObjects on the current thread to avoid this.
         */
        private static ThreadLocal<Set<String>> ignore = 
            new ThreadLocal<Set<String>>() {
                @Override
                    protected Set<String> initialValue() {
                        return new HashSet<String>();
                    }            
        };
        
        /**
         * Construct an object manager which aggregates all modified 
         * objects from among its children.        
         * @param component the component which acts as an aggregator
         */
        public DirtyObjectManager(AbstractComponent component) {
            this.component = component;
        }

        @Override
        public Set<AbstractComponent> getAllModifiedObjects() {
            // Return value. Don't allocate unless needed.
            Set<AbstractComponent> modified = null;
            
            // Set of objects to ignore (not to visit)
            Set<String> ignore = DirtyObjectManager.ignore.get();
            
            // Are we the initiator of this getAllModifiedObjects call?
            boolean initiator = ignore.isEmpty();
            
            // Make sure we don't revisit components during this invocation
            ignore.add(component.getComponentId());
            
            for (AbstractComponent child : component.getComponents()) {
                // Aggregate all dirty children
                if (child.isDirty()) {
                    if (modified == null) {
                        modified = new HashSet<AbstractComponent>();
                    }
                    modified.add(child);
                }
                
                // Aggregate their modified objects
                if (!ignore.contains(child.getComponentId())) {
                    ObjectManager childObjectManager = 
                                    child.getCapability(ObjectManager.class);
                    if (childObjectManager != null) {
                        if (modified == null) {
                            modified = new HashSet<AbstractComponent>();
                        }
                        modified.addAll(childObjectManager.getAllModifiedObjects());
                    }
                }
            }
                 
            // If stack is unwound, clear the ignore list
            if (initiator) {
                ignore.clear();
            }
            
            return (modified != null) ? modified :
                Collections.<AbstractComponent> emptySet();
        }

        @Override
        public boolean addModifiedObject(AbstractComponent comp) {
            // Modified objects are only aggregated & inferred by dirty state
            return false;
        }

        @Override
        public void notifySaved(Set<AbstractComponent> modified) {
            // No action needed
        }        
    }
    
    /**
     * An ObjectManager implementation which only tracks objects 
     * which have been explicitly marked as modified by addModifiedObject.
     * This is empty at the time of creation.
     */
    public static class ExplicitObjectManager implements ObjectManager {
        /**
         * All objects which have been recorded as modified.
         * This is mapped from component id to component in order 
         * notified; old versions will therefore be overwritten.
         */
        private Map<String, AbstractComponent> modifiedMap = 
                        new HashMap<String, AbstractComponent>();
        
        @Override
        public Set<AbstractComponent> getAllModifiedObjects() {
            Set<AbstractComponent> modified = 
                            new HashSet<AbstractComponent>();
            modified.addAll(modifiedMap.values());
            return modified;
        }

        @Override
        public boolean addModifiedObject(AbstractComponent comp) {
            modifiedMap.put(comp.getComponentId(), comp);            
            return true;
        }

        @Override
        public void notifySaved(Set<AbstractComponent> modified) {
            // Remove from the list
            for (AbstractComponent modifiedComponent : modified) {
                modifiedMap.remove(modifiedComponent.getComponentId());
            }
        }
    }
}
