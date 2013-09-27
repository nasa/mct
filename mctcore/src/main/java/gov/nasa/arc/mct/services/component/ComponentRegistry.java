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
package gov.nasa.arc.mct.services.component;

import gov.nasa.arc.mct.components.AbstractComponent;

import java.util.Collection;

/**
 * This interface provides administrative functionality for component types.
 * An instance can be obtained through OSGI, using code like:
 * <code><pre>
 *    org.osgi.framework.BundleContext bc = ... // will be obtained through an activator
 *    ServiceReference ref = bc.getServiceReference(ComponentRegistry.class.getName());
 *    if (ref != null) 
 *      ComponentRegistry registry = (ComponentRegistry) bc.getService(ref);
 *    ...
 *    bc.ungetServiceReference(ref);
 * </pre></code>
 * An instance of this class can also be obtained using the  
 * <a href=" http://www.eclipsezone.com/eclipse/forums/t97690.html">OSGI declarative
 * service functionality</a>. 
  
 * @author chris.webster@nasa.gov
 *
 */
public interface ComponentRegistry {
    /**
     * Create a new instance of the specified component class. Components must be initialized with the MCT framework, so 
     * this method must be used when creating instances of components. Failure to do this will result in components which
     * are not fully initialized and the system behavior when using these components is undefined.
     * @param <T> specific base component type to create
     * @param componentClass used to identify a component. 
     * @param parent to add newly created instance to. This value can be null which signifies that this will only 
     * show up under the created by me area.
     * @return a new component instance or null if there is no component matching the given componentClass
     */
    <T extends AbstractComponent> T newInstance(Class<T> componentClass, AbstractComponent parent); 
    
    /**
     * Get the component based on the component id.
     * @param id the component id
     * @return the component object
     */
    public AbstractComponent getComponent(String id);

    /**
     * Creates a new collection that contains <code>components</code> under the Created By Me component.
     * @param components the components to be added in the new collection
     * @return the new collection; null if the action is rejected by policies
     */
    public AbstractComponent newCollection(Collection<AbstractComponent> components);

    /**
     * Create a new instance of the specified componentType. 
     * @param componentType to create new instance of
     * @return An instance of the component type, in which: 
     * <UL>
     *   <LI>   If there is a current user: the instance is initialized such that the creator and owner of the 
     *          component are set to the current user-id.
     *   <LI>   If there is not a current user (e.g.: MCT being loaded with a user not currently in the database),
     *          then the instance is returned uninitialized.
     *   <LI>   If the componentType does not exist then null is returned.
     * </UL>
     */
    public AbstractComponent newInstance(String componentType);
    
    /**
     * This method checks if the class can be created. If the class is creatable, it will
     * appear under the "Create" menu.
     * 
     * @param clazz the class to check if it is creatable.
     * @return true if clazz is creatable, false otherwise
     */
    public boolean isCreatable(Class<?> clazz);
}
