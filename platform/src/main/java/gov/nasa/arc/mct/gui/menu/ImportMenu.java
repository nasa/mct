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
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;

import java.util.Arrays;
import java.util.Collection;

/**
 * Submenu under the "this" or "objects" menu to contain Export menu items.
 * 
 * @author vwoeltje
 * 
 */
public abstract class ImportMenu extends ContextAwareMenu {
    private static final long serialVersionUID = -8236153843562941664L;
    private static final String THIS_SUBMENU_EXT = "/this/import.ext";
    private static final String OBJECTS_SUBMENU_EXT = "/objects/import.ext";

    public ImportMenu(String extension) {
        super("Import", new String[]{ extension });
    }

    @Override
    public boolean canHandle(ActionContext context) {
        return true;
    }

    protected boolean isWritable(AbstractComponent targetComponent) {
        if (targetComponent == null) {
            return false;
        }
        
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), targetComponent);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();

        return PlatformAccess.getPlatform().getPolicyManager().execute(compositionKey, policyContext).getStatus();
    }

    /**
     * Export submenu under "This"
     */
    public static class ThisImportMenu extends ImportMenu {
        private static final long serialVersionUID = 8183646675956371635L;
       
        public ThisImportMenu() {
            super(THIS_SUBMENU_EXT);
        }
                
        @Override
        protected void populate() {
            addMenuItemInfos(THIS_SUBMENU_EXT, Arrays.<MenuItemInfo> asList(
            // new MenuItemInfo("EXPORT_THIS_TO_IMAGE", MenuItemType.NORMAL))
                    ));
        }
        
        @Override
        public boolean canHandle(ActionContext context) {   
            View view = context.getWindowManifestation();
            return view != null && 
                   isWritable(view.getManifestedComponent()) &&
                   super.canHandle(context);
        }
    }

    /**
     * Export submenu under "Objects"
     */
    public static class ObjectsImportMenu extends ImportMenu {
        private static final long serialVersionUID = -6144371321092560706L;
        
        public ObjectsImportMenu() {
            super(OBJECTS_SUBMENU_EXT);
        }
        
        @Override
        protected void populate() {
            addMenuItemInfos(OBJECTS_SUBMENU_EXT, Arrays.<MenuItemInfo> asList(
            // new MenuItemInfo("EXPORT_VIEW_TO_IMAGE", MenuItemType.NORMAL))
                    ));
        }
        
        @Override
        public boolean canHandle(ActionContext context) {
            Collection<View> selection = context.getSelectedManifestations();            
            return selection != null &&
                   selection.size() == 1 && 
                   isWritable(selection
                           .iterator()
                           .next()
                           .getManifestedComponent()) &&
                   super.canHandle(context);
        }
    }
}
