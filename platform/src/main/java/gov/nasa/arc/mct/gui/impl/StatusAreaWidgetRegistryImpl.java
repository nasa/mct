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
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.StatusAreaWidgetInfo;
import gov.nasa.arc.mct.services.component.StatusAreaWidgetRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides the implementation of <code>StatusAreaWidgetRegistry</code>.
 * It maintains a list of <code>StatusAreaWidgetInfo</code>s obtained
 * from the installed plugin bundles.
 */
public final class StatusAreaWidgetRegistryImpl implements StatusAreaWidgetRegistry {

    private final List<StatusAreaWidgetInfo> widgetInfos = new LinkedList<StatusAreaWidgetInfo>();
    private static final StatusAreaWidgetRegistryImpl INSTANCE = new StatusAreaWidgetRegistryImpl();
    
    private StatusAreaWidgetRegistryImpl() {
        
    }
    
    /**
     * Returns the single instance of <code>StatusAreaWidgetRegistryImpl</code>.
     * @return single instance of <code>StatusAreaWidgetRegistryImpl</code>
     */
    public static final StatusAreaWidgetRegistryImpl getInstance() {
        return INSTANCE;
    }
    
    @Override
    public Collection<StatusAreaWidgetInfo> getStatusAreaWidgetInfos() {
        return Collections.unmodifiableList(widgetInfos);
    }

    /**
     * Refreshes the list of stats widget infos maintained in the registry.
     * @param providers list of <code>ComponentProvider</code>
     */
    public void refresh(List<ExtendedComponentProvider> providers) {
        this.widgetInfos.clear();
        for (ExtendedComponentProvider provider : providers) {            
            Collection<StatusAreaWidgetInfo> widgetInfos = provider.getStatusAreaWidgetInfos();
            this.widgetInfos.addAll(widgetInfos);
        }
    }

}
