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
package gov.nasa.arc.mct.gui.actions;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.dialogs.DuplicateObjectDialog;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.util.StringUtil;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.testng.annotations.AfterMethod;

/**
 * This action duplicates a component.
 * @author jjpuin
 */
@SuppressWarnings("serial")
public class DuplicateAction extends ContextAwareAction {

    private static String TEXT = "Duplicate Object...";
        
    private TreePath[] selectedTreePaths;
    private MCTDirectoryArea directoryArea;
    private ActionContextImpl actionContext;
    
    public DuplicateAction() {
        super(TEXT);
    }
    
    @AfterMethod
    protected void teardown() {
        PolicyManagerImpl.getInstance().refreshExtendedPolicies(Collections.<ExtendedComponentProvider>emptyList());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {        
 
        for (TreePath path : selectedTreePaths) {
            
            MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) path.getLastPathComponent();
            MCTMutableTreeNode parentNode = (MCTMutableTreeNode) selectedNode.getParent();
        
            AbstractComponent parentComponent = ((View) parentNode.getUserObject()).getManifestedComponent();
            AbstractComponent selectedComponent = ((View) selectedNode.getUserObject()).getManifestedComponent();
        
            if (selectedComponent == null) {
                OptionBox.showMessageDialog(null, "Unable to create duplicate of this object!", "Error creating duplicate.", OptionBox.ERROR_MESSAGE);
                return;
            }
            
            DuplicateObjectDialog dialog = new DuplicateObjectDialog(actionContext.getTargetHousing().getHostedFrame(),
                                                            selectedComponent.getDisplayName());
            String name = dialog.getConfirmedTelemetryGroupName();

            if (!StringUtil.isEmpty(name)) {
                AbstractComponent duplicate = selectedComponent.clone();
                ComponentInitializer ci = duplicate.getCapability(ComponentInitializer.class);
                ci.setCreator(GlobalContext.getGlobalContext().getUser().getUserId());
                ci.setOwner(GlobalContext.getGlobalContext().getUser().getUserId());
                duplicate.setDisplayName(name);
                duplicate.save();
                parentComponent.addDelegateComponent(duplicate);
                parentComponent.save();
            }

        }

    }

    protected boolean isComponentCreatable(AbstractComponent ac) {
        ExternalComponentRegistryImpl extCompRegistry = ExternalComponentRegistryImpl.getInstance();
        return extCompRegistry.isCreatable(ac.getClass());
    }
    
    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        MCTHousing activeHousing = actionContext.getTargetHousing();
        if (activeHousing == null)
            return false;
        
        if (!(activeHousing.getDirectoryArea() instanceof MCTDirectoryArea)) {
            return false;
        }
        
        directoryArea = MCTDirectoryArea.class.cast(activeHousing.getDirectoryArea());
        Collection<View> selectedManifestationsInDirectory = directoryArea.getSelectedManifestations();
        
        // This action works only for selected items in the directory area.
        if (selectedManifestationsInDirectory == null || selectedManifestationsInDirectory.isEmpty())
            return false;
        
        MCTMutableTreeNode firstSelectedNode = directoryArea.getSelectedDirectoryNode();
        if (firstSelectedNode == null)
            return false;
        
        if (!isComponentCreatable(actionContext.getTargetComponent())) {
            return false;
        }
        
        JTree tree = firstSelectedNode.getParentTree();
        selectedTreePaths = tree.getSelectionPaths();
        
        if (selectedTreePaths.length > 1)
            return false;
        
        
        MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) selectedTreePaths[0].getLastPathComponent();
        MCTMutableTreeNode parentNode = (MCTMutableTreeNode) selectedNode.getParent();
        AbstractComponent parentComponent = ((View) parentNode.getUserObject()).getManifestedComponent();

        return isParentComponentModifiable(parentComponent);
    }

    private boolean isParentComponentModifiable(AbstractComponent parentComponent) {
        // Check if parent component can be modified.
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), parentComponent);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
        return PolicyManagerImpl.getInstance().execute(compositionKey, policyContext).getStatus();
    }
    
    @Override
    public boolean isEnabled() {
        AbstractComponent component = actionContext.getTargetComponent();
        if (component == null || component.getExternalKey() != null)
            return false;
        
        return true;
    }
    
}
