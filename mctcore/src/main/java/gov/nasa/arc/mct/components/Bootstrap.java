/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
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

import java.util.Comparator;

/**
 * Bootstrap is a capability exhibited by bootstrap components. 
 * This capability assists in ordering and tagging bootstrap 
 * components.
 * 
 * A bootstrap component is a "top-level" component which should 
 * appear in the User Environment for one or more users.
 * 
 * @author vwoeltje
 */
public interface Bootstrap {
    /**
     * Identifies whether or not this bootstrap component 
     * should be a bootstrap for all users. If false, appears 
     * only for the user indicated as the component's creator.
     * @return true if the component is a bootsrap for all users
     */
    public boolean isGlobal();
    
    /**
     * Used to identify My Sandbox. (More generally, the 
     * default destination for created objects.) 
     * @return true if the object is the user's sandbox
     */
    public boolean isSandbox();
    
    /**
     * Used for grouping bootstrap components, since these 
     * do not have strict order in the database. Bootstraps 
     * are sorted in ascending order by category index 
     * first, and component index second. Components 
     * introduced by the same plug-in should typically 
     * have the same category index and different component 
     * indexes, although this is not enforced. 
     * 
     * Bootstrap components provided by the platform will 
     * generally have a category of either 
     * Integer.MIN_VALUE or Integer.MAX_VALUE, depending on 
     * whether or not they should appear at the top or 
     * bottom of the User Environment's Browse area.
     * Other plug-ins should typically define category 
     * indices somewhere between these two values.
     *  
     * @return the index of the category
     */
    public int categoryIndex();
    
    /**
     * Used for grouping bootstrap components. Within any 
     * given category, bootstrap components are sorted 
     * by their component index in ascending order. 
     * Plug-ins which introduce bootstrap components 
     * may use different component indexes with the 
     * same category index in order to control ordering 
     * within an apparent group.  
     * 
     * @return the index of the component
     */
    public int componentIndex();
    
    /**
     * The sorting comparator for bootstrap components, 
     * using the category and component indexes.
     */
    public static final Comparator<AbstractComponent> COMPARATOR = 
                    new Comparator<AbstractComponent>() {
        @Override
        public int compare(AbstractComponent compA, AbstractComponent compB) {
            Bootstrap bootstrapA = compA.getCapability(Bootstrap.class);
            Bootstrap bootstrapB = compB.getCapability(Bootstrap.class);

            // First, consider the case where both components offer 
            // the capability.
            if (bootstrapA != null && bootstrapB != null) {
                int orderA = bootstrapA.categoryIndex();
                int orderB = bootstrapB.categoryIndex();
                // Use component index if both categories are the same
                if (orderA == orderB) {
                    orderA = bootstrapA.componentIndex();
                    orderB = bootstrapB.componentIndex();
                }
                return Integer.valueOf(orderA).compareTo(Integer.valueOf(orderB));
            }

            // Move bootstrap-capable components later
            if (bootstrapA != null && bootstrapB == null) {
                return 1;
            } else if (bootstrapA == null && bootstrapB != null) {
                return -1;
            }
            
            // Neither has bootstrap capability, so just use timestamp            
            return compA.getCreationDate().compareTo(compB.getCreationDate());
        }
    };
}
