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

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.policy.PolicyInfo;

import java.util.Collection;

/**
 * This interface can be implemented as an OSGI service to provide one
 * or more components or views for existing components. The MCT platform  
 * monitors instances of this service and adjusts the list of component types (and views)
 * based on the available instances.
 * @author chris.webster@nasa.gov
 */
public interface ComponentProvider {
    /**
     * Provides a list of supported component types.
     * @return non null collection of component types
     */
   Collection<ComponentTypeInfo> getComponentTypes();

   /**
    * Provides the view roles for the component type. This allows views to be added for any component type, 
    * including those that were supplied by another provider to extend a component's view. The platform
    * will attempt to provide default views where possible (currently this provides NodeViewRole, HousingViewRole, and CanvasViewRole instances)
    * so these roles do not need to be provided by the developer unless the default  
    * functionality is inadequate. 
    * @param componentTypeId to supply view roles for
    * @return non null collection of view roles that should be applied to the current component
    */
   Collection<ViewInfo> getViews(String componentTypeId);
   
   
   /**
    * Provide the menu information for this plugin. This allows external menu actions to be added to MCT.
    * @return non null collection of {@link MenuItemInfo}
    */
   Collection<MenuItemInfo> getMenuItemInfos();
   
   /**
    * Provide the policy information for this plugin. This allows external policies to be added to MCT.
    * @return non null collection of <code>PolicyInfo</code>
    */
   Collection<PolicyInfo> getPolicyInfos();
 
   /**
    * Provide a <code>ProviderDelegate</code> for this plugin. It defines the delegate methods for this plugin.
    * @return a <code>ProviderDelegate</code>
    */
   ProviderDelegate getProviderDelegate();
   
   /**
    * Provide status widgets for this plugin. This allows addition of status widgets populating the status area.
    * @return a collection of <code>StatusAreaWidgetInfo</code>
    */
   Collection<StatusAreaWidgetInfo> getStatusAreaWidgetInfos();
   
   /**
    * Provides search capability for this plugin. This allows the plugin to handle custom search. 
    * @return a search provider
    */
   SearchProvider getSearchProvider();
   
   /**
    * Get some asset associated with a given object type. Typically this will be used to obtain 
    * things like icons or create wizards which are associated with specific component or view 
    * types, but are not incorporated into those types directly to preserve separation of 
    * concerns. 
    * @param <T> the type of asset desired
    * @param objectType the type with which the requested asset is associated
    * @param assetClass the type of the requested asset
    * @return an asset of the requested type (or null, if none is provided)
    */
   <T> T getAsset(TypeInfo<?> objectType, Class<T> assetClass);
}
