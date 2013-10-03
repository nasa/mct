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
import java.util.Collections;

/**
 * An abstract class that implements <code>ComponentProvider</code>.
 * The methods in this class are empty. This class exists as
 * convenience for creating component providers.
 */
public abstract class AbstractComponentProvider implements ComponentProvider {

    @Override
    public Collection<ComponentTypeInfo> getComponentTypes() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ViewInfo> getViews(String componentTypeId) {
        return Collections.emptySet();
    }

    @Override
    public Collection<MenuItemInfo> getMenuItemInfos() {
        return Collections.emptySet();
    }

    @Override
    public Collection<PolicyInfo> getPolicyInfos() {
        return Collections.emptySet();
    }


    @Override
    public ProviderDelegate getProviderDelegate() {
        return null;
    }
    
    @Override
    public Collection<StatusAreaWidgetInfo> getStatusAreaWidgetInfos() {
        return Collections.emptyList();
    }
    
    @Override
    public SearchProvider getSearchProvider() {
        return null;
    }

    @Override
    public <T> T getAsset(TypeInfo<?> type, Class<T> assetClass) {
        return null;
    }
}
