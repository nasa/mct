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

import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;

/**
 * Provides information about a type relevant to MCT. 
 * Serves as an abstract superclass for ComponentTypeInfo, 
 * ViewInfo, and PolicyInfo, allowing these to be dealt 
 * with in a unified manner where appropriate.
 * 
 * This should typically be parameterized with a high-level 
 * class (such as AbstractComponent, View, or Policy) 
 * and given a specific concrete subclass thereof in the 
 * constructor.
 * 
 * @author vwoeltje
 *
 * @param <T> the class which characterizes these types 
 */
public abstract class TypeInfo<T> {
    private Class<? extends T> typeClass;
    
    /**
     * Instantiate a new new TypeInfo object for the 
     * specified class. 
     * @param typeClass the specific class being described
     */
    public TypeInfo(Class<? extends T> typeClass) {
        this.typeClass = typeClass;
    }

    /**
     * Get the specific class being described by this 
     * type info.
     * @return the specific class being described
     */
    public Class<? extends T> getTypeClass() {
        return typeClass;
    }
        
    /**
     * Get an asset associated with this type.
     * For instance, getAsset(ImageIcon.class) to get 
     * an icon.
     * @param assetClass the type of asset desired
     * @param <A> the type of asset desired
     * @return an object of the desired type (or null if none is available)
     */
    public <A> A getAsset(Class<A> assetClass) {
        Platform platform = PlatformAccess.getPlatform();
        if (platform != null) {
            CoreComponentRegistry registry = platform.getComponentRegistry();
            if (registry != null) {
                return registry.getAsset(this, assetClass);
            } 
        }
        return null;
    }
}
