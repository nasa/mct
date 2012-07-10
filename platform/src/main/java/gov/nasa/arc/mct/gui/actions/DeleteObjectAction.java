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
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ActionContextImpl;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreePath;

public class DeleteObjectAction extends ContextAwareAction {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("gov/nasa/arc/mct/gui/actions/Bundle"); 
    private static final long serialVersionUID = 3047419887471823851L;
    private static String WARNING = bundle.getString("DeleteWarningTitle");
    private static String TEXT = "Delete";
    
    private TreePath[] selectedTreePaths;
    private ActionContextImpl actionContext;
    
    public DeleteObjectAction() {
        super(TEXT);
    }
    
    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        MCTHousing activeHousing = actionContext.getTargetHousing();
        if (activeHousing == null)
            return false;


        Collection<View> selection = 
            activeHousing.getSelectionProvider().getSelectedManifestations();
        
        if (selection.isEmpty()) {
            return false;
        }
        
        ViewInfo vi = selection.iterator().next().getInfo();
        
        if (!(vi != null && vi.getViewType() == ViewType.NODE)){
            return false;
        }

        if (!(activeHousing.getDirectoryArea() instanceof MCTDirectoryArea)) {
            return false;
        }
        
        MCTDirectoryArea directory = MCTDirectoryArea.class.cast(activeHousing.getDirectoryArea());
        MCTMutableTreeNode firstSelectedNode = directory.getSelectedDirectoryNode();
        
        if (firstSelectedNode == null)
            return false;
        
        JTree tree = firstSelectedNode.getParentTree();
        selectedTreePaths = tree.getSelectionPaths();
        return selectedTreePaths != null && selectedTreePaths.length > 0;
    }
    
    @Override
    public boolean isEnabled() {
        for (TreePath path : selectedTreePaths) {
            if (!isRemovable(path))
                return false;
        }
        return true;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Set<String> deleteComponents = new HashSet<String>();
        for (TreePath path : selectedTreePaths) {
            MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) path.getLastPathComponent();            
            AbstractComponent selectedComponent = ((View) selectedNode.getUserObject()).getManifestedComponent();
            
            // If has children,  
            if (selectedComponent.getComponents().size() > 0) {
                handleWarnings(false, deleteComponents);
                return;
            } else {
                deleteComponents.add(selectedComponent.getComponentId());
            }

        }
        handleWarnings(true,deleteComponents);
    }
    
    private void handleWarnings(boolean canRemoveAll, Set<String> deleteComponents) {
        if (!canRemoveAll) {
            OptionBox.showMessageDialog(actionContext.getWindowManifestation(), 
                    bundle.getString("RemoveLastManifestationHasDescendantsErrorText"), 
                    "ERROR: "+ WARNING, 
                    OptionBox.ERROR_MESSAGE);
        } else {
            Object[] options = { bundle.getString("DeleteCoreText"), bundle.getString("AbortDeleteText") };
            int choice = OptionBox.showOptionDialog(actionContext.getWindowManifestation(), 
                    buildWarningPanel(deleteComponents),
                    WARNING,
                    OptionBox.YES_NO_OPTION,
                    OptionBox.WARNING_MESSAGE,
                    null,
                    options,
                    null);
            if (choice == 0) {
                Set<AbstractComponent> deleteThese = new HashSet<AbstractComponent>();
                for (String id : deleteComponents) {
                    deleteThese.add(AbstractComponent.getComponentById(id));
                    PlatformAccess.getPlatform().getWindowManager().closeWindows(id);
                }
                PlatformAccess.getPlatform().getPersistenceProvider().delete(deleteThese);
            }
        }
    }
    
    private JPanel buildWarningPanel(Set<String> componentsToBeDeleted) {
        List<String> deleteComps = new ArrayList<String>(componentsToBeDeleted.size());
        for (String comp : componentsToBeDeleted) {
            deleteComps.add(AbstractComponent.getComponentById(comp).getDisplayName());
        }
        JPanel warning = new JPanel(new FlowLayout());
        warning.setPreferredSize(new Dimension(400,400));
        JList deleteList = new JList(deleteComps.toArray());
        JScrollPane scrollPane2 = new JScrollPane(deleteList);
        scrollPane2.setPreferredSize(new Dimension(300,200));
        JLabel deletingObjectsLabel = new JLabel(bundle.getString("RemoveManifestationBecomesDelete"));
        deletingObjectsLabel.setPreferredSize(new Dimension(300,20));
        deletingObjectsLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        deletingObjectsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        JTextArea warningMessage = new JTextArea(bundle.getString("DeleteWarningText"));
        warningMessage.setWrapStyleWord(true);
        warningMessage.setLineWrap(true);
        warningMessage.setOpaque(false);
        warningMessage.setPreferredSize(new Dimension(300,200));
        warning.add(warningMessage);
        warning.add(deletingObjectsLabel);
        warning.add(scrollPane2);
        return warning;
    }
    
    private boolean isRemovable(TreePath path) {
        MCTMutableTreeNode lastPathComponent = (MCTMutableTreeNode) path.getLastPathComponent();
        AbstractComponent selectedComponent = View.class.cast(lastPathComponent.getUserObject()).getManifestedComponent();

        MCTMutableTreeNode parentNode = (MCTMutableTreeNode) lastPathComponent.getParent();
        if (parentNode == null)
            return false;

        AbstractComponent parentComponent = ((View) parentNode.getUserObject()).getManifestedComponent();        
        if (!selectedComponent.canBeDeleted()) {
            return false;
        }
        
        
        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), parentComponent);
        context.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
        return PolicyManagerImpl.getInstance().execute(compositionKey, context).getStatus();
    }
    
}
