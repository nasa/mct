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
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

/**
 * This action removes a manifestation in the directory area. Note that 
 * removing a manifestation under "created by me" is not allowed, but
 * it not part of the composition policy category.
 * @author nija.shi@nasa.gov
 */
@SuppressWarnings("serial")
public class RemoveManifestationAction extends ContextAwareAction {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("gov/nasa/arc/mct/gui/actions/Bundle"); 
    private static String TEXT = "Remove Manifestation";
    private static String WARNING = bundle.getString("RemoveLastManifestationWarningTitle");
    private TreePath[] selectedTreePaths;
    private ActionContextImpl actionContext;
    
    public RemoveManifestationAction() {
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
        
        if (selection.isEmpty())
            return false;
        
        ViewInfo vi = selection.iterator().next().getInfo();
        
        if (selection.isEmpty() || 
                !(vi != null && vi.getViewType() == ViewType.NODE)){
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
        return selectedTreePaths != null && selectedTreePaths.length >  0;
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
        Map<String,Integer> numberOfParents = new HashMap<String,Integer>();
        Set<String> lastManifestationComponents = new HashSet<String>();
        List<Map<MCTMutableTreeNode, MCTMutableTreeNode>> okToRemoveManifestations = new ArrayList<Map<MCTMutableTreeNode, MCTMutableTreeNode>>();
        
        for (TreePath path : selectedTreePaths) {
            MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) path.getLastPathComponent();            
            MCTMutableTreeNode parentNode = (MCTMutableTreeNode) selectedNode.getParent();
            
            AbstractComponent selectedComponent = ((View) selectedNode.getUserObject()).getManifestedComponent();

            if (!numberOfParents.containsKey(selectedComponent.getComponentId()))  {
                numberOfParents.put(selectedComponent.getComponentId(), Integer.valueOf(selectedComponent.getReferencingComponents().size()));
            } 
            // If component is the last manifestation, 
            if (numberOfParents.get(selectedComponent.getComponentId()) == 1) {
                PolicyContext policyContext = new PolicyContext();
                policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), selectedComponent);
                policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
                String deleteKey = PolicyInfo.CategoryType.CAN_DELETE_COMPONENT_POLICY_CATEGORY.getKey();
                Object[] messages = new Object[2];
                JLabel label1 = new JLabel("Cannot remove");
                label1.setFont(label1.getFont().deriveFont(Font.BOLD));
                messages[0] = label1;
                messages[1] = bundle.getString("NotOwnerOfManifestation");
                if (!PolicyManagerImpl.getInstance().execute(deleteKey, policyContext).getStatus()) {
                    OptionBox.showMessageDialog(actionContext.getWindowManifestation(), 
                            messages, 
                            "ERROR: "+ WARNING, 
                            OptionBox.ERROR_MESSAGE);
                    return;
                }
                // If component has no children,
                if (selectedComponent.getComponents().size() == 0) {
                    lastManifestationComponents.add(selectedComponent.getComponentId());
                    //Remove it from ok-to-remove Manifestations
                    Iterator<Map<MCTMutableTreeNode, MCTMutableTreeNode>> iterator = okToRemoveManifestations.iterator();
                    while (iterator.hasNext()) {
                        Map<MCTMutableTreeNode, MCTMutableTreeNode> map = iterator.next();
                        for (MCTMutableTreeNode mapNode : map.values()) {
                            if (((View) mapNode.getUserObject()).getManifestedComponent().getComponentId().equals(selectedComponent.getComponentId())) {
                                iterator.remove();
                            }
                        }
                    }

                } else {
                    //At least one component has children
                    handleWarnings(false, okToRemoveManifestations, lastManifestationComponents);
                    return;
                }
            } else {
                // Has more than 1 parent
                Map<MCTMutableTreeNode, MCTMutableTreeNode> okManifestationMap = new HashMap<MCTMutableTreeNode, MCTMutableTreeNode>();
                okManifestationMap.put(parentNode, selectedNode);
                okToRemoveManifestations.add(okManifestationMap);
                numberOfParents.put(selectedComponent.getComponentId(), numberOfParents.get(selectedComponent.getComponentId())-1);
            }
        }
        if (okToRemoveManifestations.size() + lastManifestationComponents.size() > 0) {
            handleWarnings(true, okToRemoveManifestations, lastManifestationComponents);
        }
    }
    
    private void handleWarnings(boolean canRemove, List<Map<MCTMutableTreeNode, MCTMutableTreeNode>> okToRemoveManifestations, 
            Set<String> lastManifestationComponents) {
        
        if (!canRemove) {
            OptionBox.showMessageDialog(actionContext.getWindowManifestation(), 
                    bundle.getString("RemoveLastManifestationHasDescendantsErrorText"), 
                    "ERROR: "+ WARNING, 
                    OptionBox.ERROR_MESSAGE);
            return;
        } else {
            if (lastManifestationComponents.size() > 0) {
                Object[] options = { "OK" , "Cancel" };
                int choice = OptionBox.showOptionDialog(actionContext.getWindowManifestation(), 
                        buildWarningPanel(okToRemoveManifestations, lastManifestationComponents), 
                        WARNING,
                        OptionBox.YES_NO_OPTION,
                        OptionBox.WARNING_MESSAGE,
                        null,
                        options,
                        null);
                if (choice != 0) {
                    return;
                }
            }
        }
        // Remove and/or Delete Objects
        for (Map<MCTMutableTreeNode, MCTMutableTreeNode> okMap : okToRemoveManifestations) {
            AbstractComponent parentComponent = ((View) okMap.entrySet().iterator().next().getKey().getUserObject()).getManifestedComponent();
            AbstractComponent selectedComponent = ((View) okMap.entrySet().iterator().next().getValue().getUserObject()).getManifestedComponent();
            parentComponent.removeDelegateComponent(selectedComponent);
            parentComponent.save();
        }

        for (String selectedComponentId : lastManifestationComponents) {
            PlatformAccess.getPlatform().getPersistenceProvider().delete(Collections.singleton(AbstractComponent.getComponentById(selectedComponentId)));
            PlatformAccess.getPlatform().getWindowManager().closeWindows(selectedComponentId);             
        }
        
    }
    
    private JPanel buildWarningPanel(List<Map<MCTMutableTreeNode, MCTMutableTreeNode>> okToRemoveManifestations, 
            Set<String> lastManifestationComponents) {
        Set<String> okComps = new HashSet<String>(okToRemoveManifestations.size());
        List<String> lastComps = new ArrayList<String>(lastManifestationComponents.size());
        for (Map<MCTMutableTreeNode, MCTMutableTreeNode> okMap : okToRemoveManifestations) {
            AbstractComponent selectedComponent = ((View) okMap.entrySet().iterator().next().getValue().getUserObject()).getManifestedComponent();
            okComps.add(selectedComponent.getDisplayName());
        }
        for (String comp : lastManifestationComponents) {
            lastComps.add(AbstractComponent.getComponentById(comp).getDisplayName());
        }
        JPanel warning = new JPanel(new GridLayout(3,2, 0, 0));
        warning.setPreferredSize(new Dimension(600,220));
        @SuppressWarnings({ "rawtypes", "unchecked" }) // Java 7 compatibility
        JList okList = new JList(okComps.toArray());
        @SuppressWarnings({ "rawtypes", "unchecked" }) // Java 7 compatibility
        JList lastManifestationList = new JList(lastComps.toArray());
        JScrollPane scrollPane1 = new JScrollPane(okList);
        scrollPane1.setPreferredSize(new Dimension(180,100));
        JScrollPane scrollPane2 = new JScrollPane(lastManifestationList);
        scrollPane2.setPreferredSize(new Dimension(180,100));
        JLabel okLabel = new JLabel(bundle.getString("SafeToRemoveManifestation"));
        okLabel.setPreferredSize(new Dimension(200,20));
        okLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        JLabel lastManifestationLabel = new JLabel(bundle.getString("RemoveManifestationBecomesDelete"));
        lastManifestationLabel.setPreferredSize(new Dimension(200,20));
        lastManifestationLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        JTextArea warningMessage = new JTextArea(bundle.getString("RemoveLastManifestationWarningTextA"));
        warningMessage.setWrapStyleWord(true);
        warningMessage.setLineWrap(true);
        warningMessage.setOpaque(false);
        warningMessage.setPreferredSize(new Dimension(180,100));
        warningMessage.setEditable(false);
        JTextArea removeMessage = new JTextArea(bundle.getString("RemoveSafelyWarning"));
        removeMessage.setWrapStyleWord(true);
        removeMessage.setLineWrap(true);
        removeMessage.setOpaque(false);
        removeMessage.setPreferredSize(new Dimension(200,100));
        removeMessage.setMargin(new Insets(0,10,0,10));
        removeMessage.setEditable(false);
        warning.add(removeMessage);
        warning.add(warningMessage);
        warning.add(okLabel);
        warning.add(lastManifestationLabel);
        warning.add(scrollPane1);
        warning.add(scrollPane2);
        return warning;
    }
    
    private boolean isRemovable(TreePath path) {
        MCTMutableTreeNode lastPathComponent = (MCTMutableTreeNode) path.getLastPathComponent();
        MCTMutableTreeNode parentNode = (MCTMutableTreeNode) lastPathComponent.getParent();
        if (parentNode == null)
            return false;
        
        AbstractComponent parentComponent = ((View) parentNode.getUserObject()).getManifestedComponent();
        AbstractComponent selectedComponent = View.class.cast(lastPathComponent.getUserObject()).getManifestedComponent();

        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), parentComponent);
        context.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(), Collections.singleton(selectedComponent));
        context.setProperty(PolicyContext.PropertyName.VIEW_MANIFESTATION_PROVIDER.getName(), parentNode.getUserObject());
        
        String canRemoveManifestationKey = PolicyInfo.CategoryType.CAN_REMOVE_MANIFESTATION_CATEGORY.getKey();
        boolean canRemoveManifestation = PolicyManagerImpl.getInstance().execute(canRemoveManifestationKey, context).getStatus();
        
        if (canRemoveManifestation) {
            String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
            return PolicyManagerImpl.getInstance().execute(compositionKey, context).getStatus();
        }
        return canRemoveManifestation;
    }
}
