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
/**
 * MCTDirectoryArea.java Aug 1, 2008
 *
 * This code is the property of the National Aeronautics and Space Administration and was
 * produced for the Mission Control Technologies (MCT) Project.
 *
 */
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.Twistie;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewListener;
import gov.nasa.arc.mct.gui.ViewProvider;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.menu.MenuFactory;
import gov.nasa.arc.mct.gui.util.GUIUtil;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.roles.events.FocusEvent;
import gov.nasa.arc.mct.services.component.SearchProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.LafColor;
import gov.nasa.arc.mct.util.TreeUtil;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TooManyListenersException;
import java.util.WeakHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * This class defines the Directory Area of the standard MCT Housing.
 */
@SuppressWarnings("serial")
public class MCTDirectoryArea extends View implements ViewProvider, SelectionProvider {

    public static final String VIEW_NAME = "Directory";
    private final static MCTLogger logger = MCTLogger.getLogger(MCTDirectoryArea.class);
    private final static ResourceBundle bundle = ResourceBundle.getBundle("Platform"); //NOI18N
    private static final char DRAG_DROP_POLICY_ACTION_CODE = Character.valueOf('w');
    private final static int BROWSE_TAB_INDEX = 0;
    private final MCTMutableTreeNode rootNode;
    private final JTree directory;
    private final SearchPanel search;
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
    private List<View> selectedManifestations = Collections.emptyList();
    private final TreeSelectionListener treeListener = new DirectoryTreeSelectionListener();
    
    /** Default-access constructor used for unit testing. */
    MCTDirectoryArea() {
        directory = null;
        rootNode = null;
        search = new SearchPanel();
    }

    /**
     * Create a new directory area pane within a housing.
     * 
     * @param parentHousing
     *            the housing in which we're creating the directory pane
     */
    public MCTDirectoryArea(AbstractComponent component, ViewInfo vi) {
        super(component,vi);
        setLayout(new BorderLayout());
        this.rootNode = GUIUtil.cloneTreeNode(component, component.getViewInfos(ViewType.NODE)
                .iterator().next());
        
        // Set up the directory jtree with its listeners
        JTree parentTree = this.rootNode.getParentTree();
        if (parentTree == null) {
            directory = new JTree(); 
            rootNode.setParentTree(directory);
        } else {
            directory = parentTree;
        }
        setupDirectoryTree(directory, false);
        this.add(new DirectoryTitleArea(bundle.getString("DIRECTORY"), null), BorderLayout.NORTH);
        rootNode.removeAllChildren();
        View topManifestation = (View)rootNode.getUserObject();
        if (topManifestation.getViewListener() != null) {
            TreeExpansionEvent event = new TreeExpansionEvent(rootNode.getParentTree(), null);
            topManifestation.getViewListener().actionPerformed(event);
        }
       
        // setup handlers (previously in Bookmark)
        AbstractComponent parentComponent = ((View) rootNode.getUserObject()).getManifestedComponent();
        View activeManifestation = parentComponent.getViewInfos(ViewType.NODE).iterator().next().createView(parentComponent);
        activeManifestation.setOpaque(false);
        activeManifestation.addMouseMotionListener(new WidgetDragger());
        activeManifestation.setTransferHandler(new WidgetTransferHandler());
        activeManifestation.addMouseListener(new MCTWindowOpener(parentComponent));
        activeManifestation.addMouseListener(new MCTPopupOpener(parentComponent, activeManifestation));
        
        // populate tabbed pane
        JComponent spUI = null;
        List<JComponent> UIs = new ArrayList<JComponent>();
        DirectoryTreePanel treePanel = new DirectoryTreePanel( );
        search = new SearchPanel();
        tabbedPane.add(treePanel, BROWSE_TAB_INDEX);
        tabbedPane.setTitleAt(BROWSE_TAB_INDEX, bundle.getString("BROWSE_CONTROL_TEXT"));
        int i = 0;
        for  (SearchProvider sp : search.getProviderSearchUIs()) {          
            spUI = sp.createSearchUI();
            UIs.add(spUI);
            tabbedPane.add(spUI,  ++i);
            tabbedPane.setTitleAt(i, sp.getName());
        }
        setupSelectionManagers(this, UIs);
        
        // finish
        this.add(tabbedPane, BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(0, 0));
        directory.addTreeSelectionListener(treeListener);
        rootNode.setProxy(false);
        directory.setAutoscrolls(true);
        getDirectoryPanel().setTree(directory);
    }

    private void setupDirectoryTree(final JTree dirTree, boolean rootVisible) {
        dirTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        dirTree.setRootVisible(rootVisible);
        dirTree.setShowsRootHandles(true);
        dirTree.setToggleClickCount(0);
        dirTree.addMouseListener(new DirectoryMouseListener());

        dirTree.addTreeExpansionListener(new DirectoryTreeExpansionListener());
        dirTree.setDragEnabled(true);
        dirTree.setTransferHandler(new DirectoryTreeTransferHandler(this, (DefaultTreeModel) dirTree.getModel()));
        dirTree.setDropMode(DropMode.ON_OR_INSERT);
        dirTree.setEnabled(true);
        dirTree.setScrollsOnExpand(true);

        try {
            dirTree.getDropTarget().addDropTargetListener(new AutoscrollDropTargetListener(dirTree));
        } catch (TooManyListenersException e) {
            // Can't happen--JTree drop target allows multiple listeners
        }

        final DefaultTreeModel treeModel = ((DefaultTreeModel) dirTree.getModel()); 
        treeModel.setRoot(rootNode);
 
            // Add a listener to detect when the items are first added to the tree. 
            treeModel.addTreeModelListener(new TreeModelListener() {

                @Override
                public void treeNodesChanged(TreeModelEvent e) {
                    // do nothing.

                }

                @Override
                public void treeNodesInserted(TreeModelEvent treeModelEvent) {
                    // If tree was empty before insertion, fire
                    // a structure change to ensure that the new nodes
                    // become visible to the user. Otherwise, do nothing. 
                   
                    // Determine if the tree was empty before the event by checking
                    // 1) the children are being added to the root (path length is one)
                    // 2) the number of children being added equals the number of children
                    //    of the root. 
                    
                    if ((treeModelEvent.getPath().length == 1) && 
                        (rootNode.getChildCount() == treeModelEvent.getChildren().length)) {
                        treeModel.nodeStructureChanged(rootNode);                
                    }
                  
               
                }

                @Override
                public void treeNodesRemoved(TreeModelEvent e) {
                    // do nothing

                }

                @Override
                public void treeStructureChanged(TreeModelEvent e) {
                    // do nothing.

                }

            });
                
        dirTree.setCellRenderer(new DirectoryTreeCellRenderer());
}

    /**
     * Return the panel containing the directory tree.
     * 
     * @return the Swing panel containing the directory info
     */
    public DirectoryTreePanel getDirectoryPanel() {
        DirectoryTreePanel panel = (DirectoryTreePanel) tabbedPane.getComponentAt(BROWSE_TAB_INDEX);
        assert panel != null;
        return panel;
    }

    /**
     * Get the node that is the root of the tree for the current tab.
     * 
     * @return the root node of the current tab
     */
    public MCTMutableTreeNode getTabRoot() {
        return this.rootNode;
    }

    @Override
    public Collection<View> getSelectedManifestations() {
        return selectedManifestations;
    }

    @Override
    public void addSelectionChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP, listener);
    }

    @Override
    public void removeSelectionChangeListener(PropertyChangeListener listener) {
        removePropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP, listener);
    }

    @Override
    public void clearCurrentSelections() {
        selectedManifestations = Collections.emptyList();
        directory.removeTreeSelectionListener(treeListener);
        directory.clearSelection(); 
        directory.addTreeSelectionListener(treeListener);
    }

    public MCTMutableTreeNode getSelectedNode() {
        JTree activeTree = getActiveTree();
        TreePath selection = activeTree.getSelectionPath();
        if (selection == null) {
            return getTabRoot();
        } else {
            return (MCTMutableTreeNode) selection.getLastPathComponent();
        }
    }

    /**
     * Sets the selected node in the directory tree to be newSelectedNode. 
     * 
     * If the directory tree does not contain newSelectedNode, no action is taken. 
     * 
     * @param newSelectedNode the new selected node. 
     */
    public void setSelectedNode(MCTMutableTreeNode newSelectedNode) {
        JTree activeTree = getActiveTree();
        DefaultTreeModel treeModel = (DefaultTreeModel) activeTree.getModel();
        TreePath path = new TreePath(treeModel.getPathToRoot(newSelectedNode));
        if (path != null) { // take no action because the newSelectedNode is not in the tree.
            activeTree.expandPath(path);
            activeTree.setSelectionPath(path);     
        }
    }

    /**
     * Determines the active tree.
     * @return the active directory tree
     */
    public JTree getActiveTree() {
        assert directory != null;
        return directory;
    }

    public DefaultMutableTreeNode[] getSelectedNodes() {
        JTree activeTree = getActiveTree();
        TreePath[] selectionPaths = activeTree.getSelectionModel().getSelectionPaths();
        if (selectionPaths == null || selectionPaths.length == 0)
            return new DefaultMutableTreeNode[0];
        List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
        for (TreePath path : selectionPaths) {
            nodes.add((DefaultMutableTreeNode) path.getLastPathComponent());
        }
        return nodes.toArray(new DefaultMutableTreeNode[nodes.size()]);

    }

    /**
     * This method returns the selected node from the directory area. 
     * If nothing is selected, then return the current active tab that represents the 
     * root node of a subtree within the directory area.
     *  
     * @return the selected node
     */
    public MCTMutableTreeNode getSelectedDirectoryNode() {
        Object selectedNode = directory.getLastSelectedPathComponent();
        return (MCTMutableTreeNode) (selectedNode == null ? getTabRoot() : selectedNode);
    }

    /**
     * This class is the Transfer Handler for drag and drop for the directory
     * tree.
     * 
     */
    static class DirectoryTreeTransferHandler extends TransferHandler {
        
        private MCTDirectoryArea theDirectory;
        private DefaultTreeModel theModel;
        
        public DirectoryTreeTransferHandler(MCTDirectoryArea dir, DefaultTreeModel model) {
            this.theDirectory = dir;
            this.theModel = model;
        }
        
        @Override
        public int getSourceActions(JComponent c) {
            return canDragSelectedComponent(c) ? COPY : NONE;
        }

        private boolean canDragSelectedComponent(JComponent c) {
            JTree tree = (JTree) c;
            TreePath[] selectionPaths = tree.getSelectionPaths();
            
            if (selectionPaths == null) {
                return false;
            }
            
            List<AbstractComponent> dragComponents = new ArrayList<AbstractComponent>();
            for (TreePath path : selectionPaths) {
                // Only add nodes that are components - this should always be the
                // case, but we check just to make sure, so that we don't try to
                // drag dummy nodes such as MessageTreeNode.
                if (path.getLastPathComponent() instanceof MCTMutableTreeNode) {
                    MCTMutableTreeNode node = (MCTMutableTreeNode) path.getLastPathComponent();
                    View gui = (View) node.getUserObject();
                    dragComponents.add(gui.getManifestedComponent());
                }
            }
            
            PolicyContext context = new PolicyContext();
            context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),dragComponents);
            String policyCategoryKey = PolicyInfo.CategoryType.CAN_OBJECT_BE_CONTAINED_CATEGORY.getKey();
            ExecutionResult result = PolicyManagerImpl.getInstance().execute(policyCategoryKey, context);
            return result.getStatus();
        }
        
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath[] selectionPaths = tree.getSelectionPaths();
            
            // If the user tries to drag a MessageTreeNode (used as a
            // placeholder for the "please wait..." message), the user
            // will try to drag, but the selection will be empty, since
            // we don't allow those nodes to be selected. In that case,
            // return null to disallow dragging.
            if (selectionPaths == null) {
                return null;
            }
            
            List<View> views = new ArrayList<View>();
            for (TreePath path : selectionPaths) {
                // Only add nodes that are components - this should always be the
                // case, but we check just to make sure, so that we don't try to
                // drag dummy nodes such as MessageTreeNode.
                if (path.getLastPathComponent() instanceof MCTMutableTreeNode) {
                    MCTMutableTreeNode node = (MCTMutableTreeNode) path.getLastPathComponent();
                    View gui = (View) node.getUserObject();
                    views.add(gui);
                }
            }

            // If we didn't find any applicable roles, just return null so that we
            // don't allow the drag.
            if (views.size() == 0) {
                return null;
            }
            
            return new ViewRoleSelection(views.toArray(new View[views.size()]));
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            support.setShowDropLocation(true);
            if (support.getDataFlavors().length <= 0) {
                return false;
            }
            
            // If we're trying to drop onto a MessageTreeNode, we don't
            // allow it. Those nodes are just placeholders while a node's
            // children are being loaded.
            if (support.getDropLocation() instanceof JTree.DropLocation) {
                JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
                if (location.getPath()!=null
                    && (location.getPath().getLastPathComponent() instanceof MessageTreeNode)) {
                    return false;
                }
            }
            
            DataFlavor flavor = support.getDataFlavors()[0];
            return (flavor.getRepresentationClass() == View.class);
        }

        @Override
        public boolean importData(TransferSupport support) {
            JTree.DropLocation location = (JTree.DropLocation) support.getDropLocation();
            TreePath dropPath = location.getPath();
            MCTMutableTreeNode parent; // The parent node under which we perform the drop.
            if (dropPath != null) {
                parent = (MCTMutableTreeNode) dropPath.getLastPathComponent();
            } else {
                parent = (MCTMutableTreeNode) theModel.getRoot();
            }
            return internalImport(support, parent, dropPath, location.getChildIndex());
        }
        
        // Abstract component addDelegate components is final so allow this method to be stubbed out during testing
        protected void addDelegateComponents(AbstractComponent component, List<AbstractComponent> delegates, int childIndex) {
            component.addDelegateComponents(childIndex, delegates);
        }
        
        private void actionPerformed(Collection<AbstractComponent> sourceComponents, View targetViewManifestation, final MCTDirectoryArea directoryArea, TreePath path, int childIndex) {
            AbstractComponent targetComponent = targetViewManifestation.getManifestedComponent();

            // Verify that policy constraints permit the drag and drop action. 
            final ExecutionResult policyExecutionResult = getPolicyExecutionResultDragDropOperation(targetComponent, sourceComponents, targetViewManifestation);

            if (policyExecutionResult.getStatus()) {
                // Action is permitted under policy constraints.
                
                // Add to set of dragged components to the target component and update
                // all view roles.
                List<AbstractComponent> reversedList = new ArrayList<AbstractComponent>(sourceComponents);
                Collections.reverse(reversedList);
                
                // Persist
                PersistenceProvider persistenceProvider = PlatformAccess.getPlatform().getPersistenceProvider();
                boolean successfulAction = false;
                try {
                    persistenceProvider.startRelatedOperations();
                    addDelegateComponents(targetComponent, reversedList, childIndex);
                    targetComponent.save();
                    successfulAction = true;
                } finally {
                    persistenceProvider.completeRelatedOperations(successfulAction);
                }
                targetViewManifestation.updateMonitoredGUI(); // Ensure change is reflected promptly
                
                // Update selection in the target window to be the set of dragged components.
                // This is consistent with the usability requirement of feeding back the results of user actions.                
                if (path != null) {
                    // The target tree is not empty, set selection      
                    MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) path.getLastPathComponent();
                    View selectedNodeGUIComponent = (View) selectedNode.getUserObject();
                    selectedNodeGUIComponent.updateMonitoredGUI(new FocusEvent(directoryArea, sourceComponents));
                }
                         
            } else {
                // Action is _not_ permitted under policy constraint
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Inform the user that policy prohibited the operation.
                        OptionBox.showMessageDialog(directoryArea, policyExecutionResult.getMessage(), "Composition Error - ", OptionBox.ERROR_MESSAGE);
                    }
                });
            }
        }
      
        private ExecutionResult getPolicyExecutionResultDragDropOperation(AbstractComponent targetComponent, Collection<AbstractComponent> sourceComponents, View targetViewManifesation) {
            // Establish policy context.
            PolicyContext context = new PolicyContext();
            context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), targetComponent);
            context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(), sourceComponents);
            context.setProperty(PolicyContext.PropertyName.ACTION.getName(), Character.valueOf( DRAG_DROP_POLICY_ACTION_CODE ));
            context.setProperty(PolicyContext.PropertyName.VIEW_MANIFESTATION_PROVIDER.getName(), targetViewManifesation);
            String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
            String acceptDelegateKey = PolicyInfo.CategoryType.ACCEPT_DELEGATE_MODEL_CATEGORY.getKey();
            // Execute policy
            ExecutionResult result = PlatformAccess.getPlatform().getPolicyManager().execute(compositionKey, context);
            if (result.getStatus()) {
                result = PlatformAccess.getPlatform().getPolicyManager().execute(acceptDelegateKey, context);
            }
            return result;
        }   
        
        
        boolean internalImport(TransferSupport support, MCTMutableTreeNode parent, TreePath parentPath, int childIndex) {
            if (childIndex == -1) {
                childIndex = parent.isProxy() ? 0 : parent.getChildCount();
            }
            
            View[] sourceViews;
            try {
                sourceViews = (View[]) support.getTransferable().getTransferData(View.DATA_FLAVOR);
            } catch (UnsupportedFlavorException e) {
                logger.error("Unexpected drop flavor", e);
                return false;
            } catch (IOException e) {
                logger.error("Exception getting dropped data", e);
                return false;
            }

            View gui = (View) parent.getUserObject();
            boolean insertingIntoEmptyRoot = (parent==theModel.getRoot() && parent.isLeaf());
            List<AbstractComponent> components = new ArrayList<AbstractComponent>(sourceViews.length);
            for (View v:sourceViews) {
                components.add(v.getManifestedComponent());
            }
            
            actionPerformed(components, gui, theDirectory, parentPath, childIndex);

            // If we inserted into an empty root, we have to indicate that
            // the structure changed. This works around a problem with JTree
            // where the first insert into an empty root doesn't update the
            // row count correctly, causing future drag-and-drop operations
            // to not show drop locations correctly.
            // See http://forums.java.net/jive/thread.jspa?threadID=17914
            // for a discussion. The proposed solution in that thread does
            // not solve the problem, however, while the code below works.
            if (insertingIntoEmptyRoot) {
                theModel.nodeStructureChanged(parent);
            }

            Window w = SwingUtilities.windowForComponent(support.getComponent());
            if (w != null) {
                w.toFront();
            }
            return true;
        }
    }

    /**
     * This class is the listener for tree expansion events.
     * 
     */
    private static final class DirectoryTreeExpansionListener implements TreeExpansionListener {
        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            logger.debug("Tree detected a collapse event.");
            TreePath path = event.getPath();
            MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) path.getLastPathComponent();
            JTree tree = (JTree) event.getSource();
            // if the node has already been initialized then remove the nodes 
            // and add back in the placeholder node. This will signal the node upon
            // expansion that the real children need to be created. 
            if (!selectedNode.isProxy() && selectedNode != tree.getModel().getRoot()) {
                MCTMutableTreeNode childNode = new MCTMutableTreeNode(View.NULL_VIEW_MANIFESTATION, (JTree) event.getSource());
                selectedNode.removeAllChildren();
                selectedNode.add(childNode);
                selectedNode.setProxy(true);
                // fire this event to ensure the cache has been updated. Changes to
                // the node itself do not fire events that the nodes have changed 
                ((DefaultTreeModel) tree.getModel()).reload(selectedNode);
            }
        }

        @Override
        public void treeExpanded(final TreeExpansionEvent event) {
            logger.debug("Tree detected an expansion event.");
            final JTree tree = (JTree) event.getSource();
            TreePath path = event.getPath();
            final MCTMutableTreeNode selectedNode = (MCTMutableTreeNode) path.getLastPathComponent();
            if (selectedNode.isProxy()) {
                // Insert a dummy node to alert the user that the expansion may take a while.
                final MutableTreeNode newChild = new MessageTreeNode(bundle.getString("PLACEHOLDER_NODE_TEXT"));
                
                // only add the node after the timer has expired to avoid flashing
                // 200 ms is the threshold that users can perceive a delay 
                final Timer t = new Timer(150, new ActionListener() {
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // if the node has been closed before the action has run
                        // but before this action has been canceled then do not 
                        // insert the please wait node
                        if (!selectedNode.isProxy()) {
                            ((DefaultTreeModel) tree.getModel()).insertNodeInto(newChild, selectedNode, 0);
                        }
                    }
                });
                t.setRepeats(false);
                // signal that initialization has occurred
                selectedNode.setProxy(false);
                t.start();
                
                // Load the actual children in a background thread. The dummy node will be
                // removed, and the tree redisplayed with the actual children, when the
                // child components are loaded from the database.
                (new SwingWorker<Object,Object>() {
                    
                    @Override
                    protected Object doInBackground() throws Exception {
                        View gui = (View) selectedNode.getUserObject();
                        AbstractComponent component = gui.getManifestedComponent();                        
                        return component.getComponents();
                    }
                    
                    @Override
                    protected void done() {
                        // this runs in the AWT Thread
                        t.stop();
                        // this is true if the node has been collapsed
                        if (selectedNode.isProxy()) {
                            return;
                        }
                        selectedNode.removeAllChildren();
                        View gui = (View) selectedNode.getUserObject();
                        ViewListener listener = gui.getViewListener();
                        
                        if (listener != null) {
                            // this action currently fires a structure change event
                            // so there is no need to fire another event here
                            listener.actionPerformed(event);
                        }
                        
                    }
                }).execute();
            }
        }
    }

    /**
     * These next two method handle the cell-rendering of the jtrees. One is for
     * the regular directory tree and the other is for the sub pane. The main
     * difference is that the regular directory renderer looks to see if it is
     * selected and decides which color to render the selection in - this is so
     * that when the sub pane is showing, it looks like the regular directory
     * cell is grayed out when the sub pane is showing.
     * 
     * 
     */
    static final class DirectoryTreeCellRenderer extends DefaultTreeCellRenderer {
        private Map<MCTMutableTreeNode, WeakReference<View>> renderedComponent = new WeakHashMap<MCTMutableTreeNode, WeakReference<View>>();
        
        /**
         * Creates a new renderer for a directory tree node. We
         * set the background selection and nonselection colors
         * to match those used elsewhere in MCT.
         */
        public DirectoryTreeCellRenderer() {
            setBackgroundNonSelectionColor(LafColor.WINDOW);
            setBackgroundSelectionColor(LafColor.TREE_SELECTION_BACKGROUND);
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            // We must call the superclass method in order to let it set some
            // internal, private variables used during paint(). (Don't ever
            // do that in your code! It makes DefaultTreeCellRenderer hard
            // to subclass, even though Sun's tutorial recommends that for
            // custom display.) We ignore the return value, since we're
            // creating our own component based on the view roles.
            @SuppressWarnings("unused")
            Component dummy = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            // If we are rendering a special message node, use a simple
            // label, without an icon, so it doesn't look like a real tree
            // node.
            if (value instanceof MessageTreeNode) {
                return new JLabel(value.toString());
            }
            
            MCTMutableTreeNode node = (MCTMutableTreeNode) value;
            WeakReference<View> viewRef = renderedComponent.get(node);
            View view = viewRef == null ? null : viewRef.get();
            if (view == null) {
                view = (View) node.getUserObject();
                view.addMonitoredGUI(node);
                renderedComponent.put(node, new WeakReference<View>(view));
            }

            // We highlight the tree node if either it's selected or it's
            // the target of a drag-and-drop.
            JTree.DropLocation dropLocation = tree.getDropLocation();
            if (sel
                || (dropLocation != null
                    && dropLocation.getChildIndex() == -1
                    && tree.getRowForPath(dropLocation.getPath()) == row)) {
                view.setBackground(LafColor.TREE_SELECTION_BACKGROUND);
            } else {
                view.setBackground(LafColor.WINDOW);
            }

            return view;
        }
    }
    
    /**
     *  Implements a dummy node used to display messages in a JTree instead
     *  of actual MCT components.
     */
    static class MessageTreeNode extends DefaultMutableTreeNode {
        
        public MessageTreeNode(String message) {
            super(message, false);
        }
        
    }

    private void handleSelectionChanged(TreePath[] selectedPaths) {
        List<View> previousManifestations = selectedManifestations;
        selectedManifestations = new ArrayList<View>();
        if (selectedPaths != null) {
            for (TreePath selectedPath: selectedPaths) {
                Object node = selectedPath.getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode) {
                    node = ((DefaultMutableTreeNode)node).getUserObject();
                }

                if (node instanceof View) {
                    selectedManifestations.add((View)node);
                } else {
                    assert false : "expected user object to be an MCTViewManifestation";
                }
            }
        }

        assert selectedManifestations != null;
        firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, previousManifestations, selectedManifestations);
    }

    /**
     * The selection listener determines what component is selected.
     */
    private final class DirectoryTreeSelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            // Remove from the selection any paths that lead to MessageTreeNode
            // elements, since those are placeholders while we are loading a
            // proxy node's children.
            List<TreePath> badPaths = new ArrayList<TreePath>();
            if (directory.getSelectionPaths() != null) {
                for (TreePath path : directory.getSelectionPaths()) {
                    if (path.getLastPathComponent() instanceof MessageTreeNode) {
                        badPaths.add(path);
                    }
                }
            }
            
            if (badPaths.size() > 0) {
                directory.removeSelectionPaths(badPaths.toArray(new TreePath[0]));
                return;
            }
            
            // else go ahead and respond to the selection.
            
            JTree source = (JTree) e.getSource();
            TreePath[] selectedPaths = source.getSelectionPaths();
            if (selectedPaths == null) {
                selectedPaths = new TreePath[0];
            }

            handleSelectionChanged(selectedPaths);
        }
    }

    /**
     * This class is the mouse listener for the Directory tree and subpanel
     * tree.
     * 
     */
    private final class DirectoryMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                rightClickEquivalent(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                rightClickEquivalent(e);
            }
        }

        /**
         * Determine if the click is outside the tree.
         * - right of the node
         * - below the last row in the tree
         */
        private boolean clickShouldDeselect(int x, int y, JTree tree) {
            boolean clickOffTree = tree.getRowForLocation(x, y) == -1;
            if (clickOffTree) {
                TreePath nearestPath = tree.getClosestPathForLocation(x, y);
                Rectangle r = tree.getPathBounds(nearestPath);
                /* selection to the left of the tree could be a toggle control so
                 * treat this as it is part of the tree.
                 */
                boolean clickToRight = r.getMaxX() < x;
                /*
                 * selections below the rows are considered outside the tree
                 */
                boolean clickBelowRow = r.getMaxY() < y;
                clickOffTree = clickToRight || clickBelowRow;
            }
            
            return clickOffTree;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            // When mouse click is not directly on the tree, treat it as a deselection gesture.
            JTree dirTree = (JTree) e.getSource();
            
            if (dirTree.getSelectionPath() != null && clickShouldDeselect(e.getX(), e.getY(), dirTree)) {
                dirTree.setSelectionRow(-1);
                return;   
            }
            

            if (e.isPopupTrigger()) {
                rightClickEquivalent(e);
                return;
            }

            // If we detect a double-click, and some node in the tree is selected,
            // open the component for that node into a new window. Note that
            // getSelectedNode() will return the root node if nothing else is
            // selected, so we need to ensure that both 1) the tree has a selection,
            // and 2) we've recognized the selection as an MCT view manifestation.
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                logger.debug("Tree detected a double click");
                if (dirTree.getSelectionPath()!=null && getSelectedNode()!=null) {
                    View gui = (View) getSelectedNode().getUserObject();
                    
                    if (gui != null) {
                        gui.getManifestedComponent().open();
                    }
                }
            }
        }

        /**
         * Right click (or equivalent) triggers a selection and a popup menu
         * @param e
         */
        private void rightClickEquivalent(MouseEvent e) {
            JTree dirTree = (JTree) e.getSource();
            if ( !(TreeUtil.isPointOnTree(dirTree, e.getX(), e.getY()))) {
                dirTree.setSelectionRow(-1);
                return;
            }

            int rowForLocation = dirTree.getRowForLocation(e.getX(), e.getY());
            int[] selectionRows = dirTree.getSelectionRows();
            if (selectionRows == null) {
                dirTree.setSelectionRow(rowForLocation);
            } else {
                Arrays.sort(selectionRows);
                if (Arrays.binarySearch(selectionRows, rowForLocation) < 0)
                    dirTree.setSelectionRow(rowForLocation);
            }
            showPopupMenu(e);
        }

        private void showPopupMenu(MouseEvent e) {
            if (getSelectedNode() == null)
                return;

            // Set action context.
            ActionContextImpl context = new ActionContextImpl();
            MCTHousing parentHousing = (MCTHousing) SwingUtilities.getAncestorOfClass(MCTHousing.class, e.getComponent());
            context.setTargetHousing(parentHousing);
            DefaultMutableTreeNode[] selectedNodes = getSelectedNodes();
            if (selectedNodes != null && selectedNodes.length > 0) {
                for (DefaultMutableTreeNode node : selectedNodes) {
                    context.addTargetViewComponent((JComponent) node.getUserObject());
                }
                context.setTargetComponent(((View) context.getTargetViewComponent()).getManifestedComponent());
                JPopupMenu popupMenu = MenuFactory.createUserObjectPopupMenu(context);
                popupMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
            }

        }
    }

    private static class DirectoryTitleArea extends JPanel {
        private static final Color BACKGROUND_COLOR = LafColor.WINDOW_BORDER.darker();
        private static final Color FOREGROUND_COLOR = LafColor.WINDOW.brighter();
        private static final int HORIZONTAL_SPACING = 5;

        public DirectoryTitleArea (String text, Twistie toggle) {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBackground(BACKGROUND_COLOR);
            JLabel title = new JLabel(text);
            title.setForeground(FOREGROUND_COLOR);
            add(Box.createHorizontalStrut(HORIZONTAL_SPACING));
            add(title);
            add(Box.createHorizontalStrut(HORIZONTAL_SPACING));
            instrumentNames(title);
        }

        private void instrumentNames(JLabel title) {
            title.setName("directoryTitle");
        }
    }

    protected JTree getDirectoryTree() {
        return directory;
    }


    
    /** Add an ancestor listener to the directory area. */
    public final void setupSelectionManagers(final MCTDirectoryArea dirArea, final List <JComponent>components) {

      addAncestorListener(new AncestorListener() {
      
      @Override
      public void ancestorAdded(AncestorEvent event) {
          MCTHousing housing = (MCTHousing) SwingUtilities.getAncestorOfClass(MCTHousing.class, dirArea);
          SelectionProvider selectionProvider = housing.getHousedViewManifestation().getSelectionProvider();
          assert selectionProvider instanceof SelectionManager;
          SelectionManager selectionManager = (SelectionManager) selectionProvider;
          components.add(dirArea);
          for (JComponent comp : components) {
              selectionManager.manageComponent(comp);
          }
          
      }

      @Override
      public void ancestorMoved(AncestorEvent event) {
          
      }

      @Override
      public void ancestorRemoved(AncestorEvent event) {
          
      }
      
  });
    }
    
    @Override
    public View getHousedViewManifestation() {
        View housedManifestation = (View) this.rootNode.getUserObject();
        // add the parent client property to support the locking manifestation algorithms as they will look at 
        // containment and the mutable tree node is not contained in any swing heirarchy 
        housedManifestation.putClientProperty(MCTMutableTreeNode.PARENT_CLIENT_PROPERTY_NAME, directory);
        return housedManifestation;
    }
    
    
    private static final class WidgetDragger extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            JComponent c = (JComponent) e.getSource();
            TransferHandler th = c.getTransferHandler();
            th.exportAsDrag(c, e, TransferHandler.COPY);
        }
    }

    private final class WidgetTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            return new ViewRoleSelection(new View[] {(View) rootNode.getUserObject() });
        }
    }

}
