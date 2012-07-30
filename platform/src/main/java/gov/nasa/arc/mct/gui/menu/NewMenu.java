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
package gov.nasa.arc.mct.gui.menu;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentTypeInfo;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("serial")
public class NewMenu extends ContextAwareMenu {

    public NewMenu() {
        super("Create");
    }

    @Override
    public boolean canHandle(ActionContext context) {
        ActionContextImpl actionContext = (ActionContextImpl) context;
        // For now, this New submenu should only appear when there are external bundles
        ExternalComponentRegistryImpl extCompRegistry = ExternalComponentRegistryImpl.getInstance();
        Collection<ExtendedComponentTypeInfo> componentInfos = extCompRegistry.getComponentInfos();
        if (componentInfos.isEmpty())
            return false;

        AbstractComponent targetComponent = actionContext.getTargetComponent();
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), targetComponent);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
        return PolicyManagerImpl.getInstance().execute(compositionKey, policyContext).getStatus();
    }
    @Override
    protected void populate() {
        addMenuItemInfos("", Collections.<MenuItemInfo>singleton(new MenuItemInfo("OBJECTS_NEW_ACTION", MenuItemType.COMPOSITE)));
    }

}
