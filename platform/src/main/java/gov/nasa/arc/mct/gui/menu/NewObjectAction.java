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
import gov.nasa.arc.mct.gui.ActionContextImpl;
import gov.nasa.arc.mct.gui.CompositeAction;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.dialogs.DefaultWizardUI;
import gov.nasa.arc.mct.gui.dialogs.NewObjectDialog;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentTypeInfo;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class NewObjectAction extends CompositeAction {
    private ActionContextImpl actionContext;

    public NewObjectAction() {
        super("New <type>");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        AbstractComponent targetComponent = actionContext.getTargetComponent();
        ExternalComponentRegistryImpl extCompRegistry = ExternalComponentRegistryImpl.getInstance();
        Collection<ExtendedComponentTypeInfo> componentInfos = extCompRegistry.getComponentInfos();

        List<Action> subActions = new ArrayList<Action>(componentInfos.size());
        for (ExtendedComponentTypeInfo info : componentInfos) {
            if (info.isCreatable()) {
                PolicyContext policyContext = new PolicyContext();
                policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), targetComponent);
                policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
                String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
                AbstractComponent tempComponent =  extCompRegistry.newInstance(info);
                policyContext.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(), Collections.singleton(tempComponent));
                if (PolicyManagerImpl.getInstance().execute(compositionKey, policyContext).getStatus()) {
                    subActions.add(new NewTypeAction(info.getDisplayName(), info.getComponentClass(), targetComponent, info.getWizardUI(), info.getIcon()));
                } 
            }
        }
        setActions(subActions.toArray(new Action[subActions.size()]));
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private class NewTypeAction extends AbstractAction {
        private Class<? extends AbstractComponent> componentClass;
        private AbstractComponent targetComponent;
        private CreateWizardUI wizardUI;

        public NewTypeAction(String componentDisplayName, Class<? extends AbstractComponent> componentClass,
                AbstractComponent targetComponent, CreateWizardUI wizard, ImageIcon icon) {
            putValue(Action.NAME, componentDisplayName);
            putValue(Action.SMALL_ICON, icon);
            this.componentClass = componentClass;
            this.targetComponent = targetComponent;       
            this.wizardUI = wizard;
            if (wizardUI == null){
                wizardUI = new DefaultWizardUI(this.componentClass);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NewObjectDialog dialog = new NewObjectDialog(actionContext.getTargetHousing().getHostedFrame(), getValue(
                    Action.NAME).toString(), this.wizardUI);
            dialog.setVisible(true);
            if (!dialog.getConfirm()){
                return;
            }
                 
            String newComponentId = null;
            AbstractComponent c = this.wizardUI.createComp(ExternalComponentRegistryImpl.getInstance(), targetComponent);     
            completeWorkUnit();
            if (c != null){
                newComponentId = c.getComponentId();
            }
            
            View directoryAreaView = actionContext.getTargetHousing().getDirectoryArea();
            // Select new component (only if it is not null), selection is currently tied to the directory area
            // this implementation should be changed to make the selection support generic (added to the SelectionManager API)
            // to remove this dependency
            if (!(directoryAreaView instanceof MCTDirectoryArea)) {
                MCTLogger.getLogger(NewObjectAction.class).warn(
                        "new object not selected in directory as only MCTDirectoryArea instances are supported " + directoryAreaView.getClass().getName());
            }
            
            if (newComponentId != null && directoryAreaView instanceof MCTDirectoryArea) {
                MCTDirectoryArea directoryArea = MCTDirectoryArea.class.cast(directoryAreaView);
                JTree activeTree = directoryArea.getActiveTree();

                // Identify the currently selected node in the directory area. 
                MCTMutableTreeNode initiallySelectedNode = directoryArea.getSelectedNode();    

                if (initiallySelectedNode == null) {
                    // set the root of the tree as the initially selected node. 
                    initiallySelectedNode = (MCTMutableTreeNode) activeTree.getModel().getRoot();
                }
                assert initiallySelectedNode!=null : "Selected node must not be null by this point";

                // Check to see if children have been loaded. If not fire are tree expansion event to force them to be loaded.
                if (initiallySelectedNode.isProxy()) {
                    // Force children to be loaded.
                    DefaultTreeModel treeModel = (DefaultTreeModel) activeTree.getModel();
                    TreePath path = new TreePath(treeModel.getPathToRoot(initiallySelectedNode));

                    if (path!=null) {
                        View initiallySelectedManifestation = directoryArea.getSelectedManifestations().iterator().next();  
                        initiallySelectedManifestation.getViewListener().actionPerformed(new TreeExpansionEvent(activeTree, path));
                    }

                }
                
                // Find the child that corresponds to our new component. 
                for(int i=0; i< initiallySelectedNode.getChildCount(); i++) {
                    MCTMutableTreeNode childNode = (MCTMutableTreeNode) initiallySelectedNode.getChildAt(i);
                    if(childNode.getUserObject() instanceof View) {
                        View man = (View) childNode.getUserObject();
                        if (man.getManifestedComponent().getId().equalsIgnoreCase(newComponentId)) {
                            // Found the child that corresponds to our new component. Set it as selected.
                            directoryArea.setSelectedNode(childNode);
                            PropertyChangeEvent pce = new PropertyChangeEvent(man.getManifestedComponent());
                            pce.setProperty(PropertyChangeEvent.DISPLAY_NAME, PropertyChangeEvent.DISPLAY_NAME);
                            man.updateMonitoredGUI(pce);
                            break;
                        }
                    }            
                }
            }

        }

    }

}
