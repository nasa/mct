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
package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.AbstractViewListener;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.util.GUIUtil;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.FocusEvent;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.ReloadEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.MCTIcons;
import gov.nasa.arc.mct.util.internal.ElapsedTimer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class NodeViewManifestation extends View {
    private static final Logger PERF_LOGGER = LoggerFactory.getLogger("gov.nasa.arc.mct.performance.node");

    private final JLabel spacebar = new JLabel(" ");
    private JLabel label;
    private MCTMutableTreeNode node;
    private PropertyChangeListener objectStaleListener = new PropertyChangeListener() {
        
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if ((Boolean) evt.getNewValue()) {
                if (getManifestedComponent().getComponentId() != null) {
                    AbstractComponent committedComponent = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(getManifestedComponent().getComponentId());
                    setManifestedComponent(committedComponent);
                    updateMonitoredGUI();
                }
            }
        }
    };

    public static final String DEFAULT_NODE_VIEW_ROLE_NAME = "DefaultNodeView";
    
    /**
     * For Internal Use only
     */
    public NodeViewManifestation() {
        super();
    }

    public NodeViewManifestation(AbstractComponent component, ViewInfo viewinfo) {
        super(component,viewinfo);
        label = new JLabel(component.getExtendedDisplayName());
        label.putClientProperty("TITLE", true);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel(getIcon(component)));
        add(spacebar);
        add(label);
        doLockRendering(label,component);
        setViewListener(new NodeViewManifestationListener());
        addPropertyChangeListener(VIEW_STALE_PROPERTY, objectStaleListener);
    }
    
    private ImageIcon getIcon(AbstractComponent ac) {
        ImageIcon baseIcon = ac.getAsset(ImageIcon.class);        
        return MCTIcons.processIcon(baseIcon, new Color(129, 154, 204), new Color(101, 131, 192), false);
    }
    
    private void doLockRendering(JLabel widget, AbstractComponent comp) {
        widget.getFont();
        if (comp != null) {
            comp.getId();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension defaultDim = super.getPreferredSize();
        Dimension newSize = new Dimension(defaultDim.width, defaultDim.height+5);
        return newSize;
    }
    
    @Override
    public void viewPersisted() {
        updateMonitoredGUI();
    }
    
    @Override
    public void updateMonitoredGUI() {
        // Sets display name if changed
        boolean labelChanged = false;
        if (!label.getText().equals(getManifestedComponent().getExtendedDisplayName())) {
            label.setText(getManifestedComponent().getExtendedDisplayName());
            labelChanged = true;
        }
        if (node != null) {
            JTree parentTree = node.getParentTree();
            if (parentTree == null)
                return;
            
            DefaultTreeModel treeModel = (DefaultTreeModel) parentTree.getModel();

            if (labelChanged) treeModel.nodeChanged(node);
            if (node.isProxy())                
                return;

            // First, remove any placeholder nodes
            for (int i = 0; i < node.getChildCount(); i++) {
                while (i < node.getChildCount() && !(node.getChildAt(i) instanceof MCTMutableTreeNode)) {
                    node.remove(i);
                }
            }
            
            // Check if a node structure refresh is necessary
            List<AbstractComponent> visibleChildComponents = new ArrayList<AbstractComponent>();
            for (AbstractComponent childComponent : getManifestedComponent().getComponents()) {
                visibleChildComponents.add(childComponent);
            }
            if (visibleChildComponents.size() == node.getChildCount()) {
                boolean changed = false; // Same number of children - but have any changed or moved?
                for (int index = 0; index < visibleChildComponents.size(); index++) {
                    MCTMutableTreeNode treeNode = (MCTMutableTreeNode) node.getChildAt(index);
                    AbstractComponent nodeComponent = ((View) treeNode.getUserObject()).getManifestedComponent();
                    if (!nodeComponent.getComponentId().equals(visibleChildComponents.get(index).getComponentId())) {
                        changed = true;
                        break;
                    }
                }
                if (!changed) return; // Don't continue with refresh if children are unchanged.
            }                

            // Note currently expanded nodes to restore state after re-ordering
            Set<String> expanded = new HashSet<String>();
            for (int index = 0; index < node.getChildCount(); index++) {
                MCTMutableTreeNode childNode = (MCTMutableTreeNode) node.getChildAt(index);
                View childView = (View) childNode.getUserObject();
                if (parentTree.isExpanded(childNode.getTreePath()))
                    expanded.add(childView.getManifestedComponent().getComponentId());               
            }
            
            // Insert nodes at the bottom which reflect current structure...
            for (AbstractComponent childComponent : visibleChildComponents) {
                Set<ViewInfo> viewInfos = childComponent.getViewInfos(ViewType.NODE);

                if (!node.isProxy()) {
                    MCTMutableTreeNode childNode = GUIUtil.cloneTreeNode(childComponent,viewInfos.iterator()
                            .next());
                    node.addChild(node.getChildCount(), childNode, objectStaleListener);
                }
            }
            
            // ...and then remove the old nodes from the top. (Removing first would cause node to collapse.)
            while (node.getChildCount() > visibleChildComponents.size()) {
                node.removeChild((MCTMutableTreeNode) node.getChildAt(0), objectStaleListener);
            }
            
            // Finally, restore selection paths.
            for (int index = 0; index < node.getChildCount(); index++) {
                MCTMutableTreeNode childNode = (MCTMutableTreeNode) node.getChildAt(index);
                View childView = (View) childNode.getUserObject();
                if (expanded.contains(childView.getManifestedComponent().getComponentId())) {
                    parentTree.expandPath(childNode.getTreePath());
                    childNode.setProxy(false); // If expanded node is mislabeled as proxy, it will lose updates
                }
            }

            treeModel.nodeChanged(node);
        }
    }

    @Override
    public void updateMonitoredGUI(AddChildEvent event) {
        if (node != null) {
            AbstractComponent parentComponent = ((View) node.getUserObject()).getManifestedComponent();
            PolicyContext context = new PolicyContext();
            context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), parentComponent);
            context.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
            PolicyManager policyManager = PlatformAccess.getPlatform().getPolicyManager();
            if (!policyManager.execute(PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey(), context).getStatus())
                return;

            AbstractComponent childComponent = event.getChildComponent();
            Set<ViewInfo> viewInfos = childComponent.getViewInfos(ViewType.NODE);

            if (!node.isProxy()) {
                MCTMutableTreeNode childNode = GUIUtil.cloneTreeNode(childComponent, viewInfos.iterator()
                        .next());
                node.addChild(event.getChildIndex(), childNode, objectStaleListener);
            }
        }

    }

    @Override
    public void updateMonitoredGUI(RemoveChildEvent event) {
        if (node != null && !node.isProxy()) {
            AbstractComponent targetChildComponent = event.getChildComponent();
            for (int i = 0; i < node.getChildCount(); i++) {
                MCTMutableTreeNode childNode = (MCTMutableTreeNode) node.getChildAt(i);
                AbstractComponent childComponent = ((View) childNode.getUserObject()).getManifestedComponent();
                if (targetChildComponent.equals(childComponent)) {
                    node.removeChild(childNode, objectStaleListener);
                }
            }
        }
    }

    @Override
    public void updateMonitoredGUI(FocusEvent event) {
        JTree tree = node.getParentTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        List<TreePath> treePaths = new ArrayList<TreePath>();
        for (int i = 0; i < node.getChildCount(); i++) {
            MCTMutableTreeNode childNode = (MCTMutableTreeNode) node.getChildAt(i);
            View childGUIComponent = (View) childNode.getUserObject();
            AbstractComponent childComponent = childGUIComponent.getManifestedComponent();
            if (event.getFocusComponents().contains(childComponent))
                treePaths.add(new TreePath(treeModel.getPathToRoot(childNode)));
        }
        if (treePaths.size() > 0) {
            tree.setSelectionPaths(treePaths.toArray(new TreePath[treePaths.size()]));
        }
    }

    @Override
    public void updateMonitoredGUI(PropertyChangeEvent event) {
        Object property = event.getProperty(PropertyChangeEvent.DISPLAY_NAME);
        if (property != null) {
            label.setText(getManifestedComponent().getExtendedDisplayName());
            if (node != null)
                ((DefaultTreeModel) this.node.getParentTree().getModel()).nodeChanged(node);
        }
    }

    @Override
    public void updateMonitoredGUI(ReloadEvent event) {
        updateMonitoredGUI();
     
        if (node != null && node.getChildCount() == 0) {
            node.setProxy(true);
            node.add(new MCTMutableTreeNode(View.NULL_VIEW_MANIFESTATION));
        }
    }

    @Override
    public <T> void addMonitoredGUI(T gui) {
        this.node = (MCTMutableTreeNode) gui;
    }

    public MCTMutableTreeNode getMCTMutableTreeNode() {
        return node;
    }

    @Override
    public void enterLockedState() {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        repaintIfTree(node);
    }

    @Override
    public void exitLockedState() {
        StringBuilder text = new StringBuilder(label.getText());
        if (text.charAt(0) == '*') {
            label.setText(text.substring(1));
        }
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        repaintIfTree(node);
    }
    
    // This view role may be used outside a tree.
    protected void repaintIfTree(MCTMutableTreeNode treeNode) {
        if (treeNode != null && treeNode.getParentTree() != null) {
            treeNode.getParentTree().repaint();
        }
    }
    
    protected class NodeViewManifestationListener extends AbstractViewListener {
        
        @Override
        public void actionPerformed(TreeExpansionEvent event) {
            final ElapsedTimer timer = new ElapsedTimer();
            timer.startInterval();
            
            JTree tree = (JTree) event.getSource();
            final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

            final MCTMutableTreeNode selectedNode = node;

            AbstractComponent component = getManifestedComponent();

            for (AbstractComponent childComponent : component.getComponents()) {
                Set<ViewInfo> nodeViews = childComponent.getViewInfos(ViewType.NODE);
                if (!nodeViews.isEmpty()) {
                    ViewInfo nextViewInfo = nodeViews.iterator().next();
                    
                    // Children are only allowed if the component is not a leaf or the component is another users drop box
                    boolean allowsChildren =!childComponent.isLeaf();
                    
                    View childNodeView = nextViewInfo.createView(childComponent);
                    MCTMutableTreeNode childNode = new MCTMutableTreeNode(childNodeView, tree, allowsChildren);

                    if (allowsChildren){
                        MCTMutableTreeNode grandChildNode = new MCTMutableTreeNode(View.NULL_VIEW_MANIFESTATION, tree);
                        childNode.add(grandChildNode);
                        childNode.setProxy(true);
                    }
                    selectedNode.add(childNode);
                    childNodeView.addPropertyChangeListener(VIEW_STALE_PROPERTY, objectStaleListener);
                }
            }
            treeModel.reload(selectedNode);
            
            timer.stopInterval();
            PERF_LOGGER.debug("Time to expand node {}: {}", component.getId(), timer.getIntervalInMillis());
        }
    }      
}