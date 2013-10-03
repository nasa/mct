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
package gov.nasa.arc.mct.services.internal.component;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.services.component.ComponentRegistry;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Set;

/**
 * This interface extends the ComponentRegistry interface to provide additional
 * API for creating new instances.
 * 
 * This interface is to be used by the MCT framework only.
 *
 */
public interface CoreComponentRegistry extends ComponentRegistry {

    /**
     * Create a new instance of the specified componentTypeInfo. This API is only used
     * within the MCT osgi framework internally to create an instance of component.
     * 
     * @param componentTypeInfo - component type info.
     * @return AbstractComponent - the abstract component.
     */
    public AbstractComponent newInstance(ComponentTypeInfo componentTypeInfo);
    
    /**
     * Gets all the view info for the specified component type. 
     * @param componentTypeId for the view infos
     * @param type to determine view info for.
     * @return applicable view infos
     */
    public Set<ViewInfo> getViewInfos(String componentTypeId, ViewType type);
    
    /**
     * Get an asset of a specific type, associated with some other type. 
     * For instance, this can be used to obtain an Icon 
     * for a specific component or view class.
     * 
     * @param <T> the type of asset desired
     * @param objectType the type with which the asset is associated
     * @param assetClass the type of the desired asset
     * @return an asset of the desired type (or null if none is available)
     */
    public <T> T getAsset(TypeInfo<?> objectType, Class<T> assetClass);
}
