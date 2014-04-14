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
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestDeleteAll {
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle("gov/nasa/arc/mct/gui/actions/Bundle"); 
    private static final String CONFIRM = BUNDLE.getString("DeleteAllCoreText");
    private static final String ABORT = BUNDLE.getString("DeleteAllAbortText");
    private static final String OK = BUNDLE.getString("DeleteAllErrorConfirm");
    
    
    @Mock ActionContextImpl mockContext;
    @Mock MCTHousing mockHousing;
    @Mock SelectionProvider mockSelection;
    @Mock MCTDirectoryArea mockDirectory;
    @Mock Platform mockPlatform;
    @Mock PersistenceProvider mockPersistence;
    @Mock WindowManager mockWindowing;
    @Mock MCTMutableTreeNode mockNode;
    @Mock PolicyManager mockPolicy;
       
    private TreePath[] selectedTreePaths; // Since JTree can't be mocked
    
    private Platform oldPlatform;
   
    // For policy support
    private ExecutionResult yes = new ExecutionResult(null, true,  "");
    private ExecutionResult no  = new ExecutionResult(null, false, "");
    private Set<AbstractComponent> canCompose = new HashSet<AbstractComponent>();
    private Set<AbstractComponent> canDelete  = new HashSet<AbstractComponent>();
    
    private ContextAwareAction deleteAll;
    
    @BeforeClass
    public void cachePlatform() {
        oldPlatform = PlatformAccess.getPlatform();
    }
    
    @AfterClass
    public void restorePlatform() {
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
    @BeforeMethod    
    public void setup() {
        MockitoAnnotations.initMocks(this);
        new PlatformAccess().setPlatform(mockPlatform);
        
        // Common setup
        Mockito.when(mockNode.getParentTree())
            .thenReturn(new PseudoJTree());
        Mockito.when(mockHousing.getSelectionProvider())
            .thenReturn(mockSelection);
        Mockito.when(mockPlatform.getPolicyManager())
            .thenReturn(mockPolicy);
        Mockito.when(mockPolicy.execute(
                Mockito.anyString(),Mockito.<PolicyContext>any()))
                .thenAnswer(new Answer<ExecutionResult>() {
                    @Override
                    public ExecutionResult answer(InvocationOnMock invocation) throws Throwable {
                        Set<AbstractComponent> set =
                                invocation.getArguments()[0].equals(
                                        PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey()) ?
                                                canCompose : canDelete;
                        PolicyContext context = (PolicyContext) invocation.getArguments()[1];
                        return set.contains(
                                context.getProperty(
                                        PolicyContext.PropertyName.TARGET_COMPONENT.getName(), 
                                        AbstractComponent.class)) ? 
                                                yes : no;
                    }                    
                });
        
        deleteAll = new DeleteAllAction();
    }
    
    @Test (dataProvider="canHandleTestCases")
    public void testCanHandle(
            boolean hasHousing,
            Collection<View> selection,
            boolean hasDirectory,
            boolean hasFirstNode,
            TreePath[] treePaths,
            boolean expected            
            ) {
        // Setup inputs
        Mockito.when(mockContext.getTargetHousing())
            .thenReturn(hasHousing ? mockHousing : null);
        Mockito.when(mockHousing.getDirectoryArea())
            .thenReturn(hasDirectory ? mockDirectory : null);
        Mockito.when(mockDirectory.getSelectedDirectoryNode())
            .thenReturn(hasFirstNode ? mockNode : null);
        Mockito.when(mockSelection.getSelectedManifestations())
            .thenReturn(selection);
            
        selectedTreePaths = treePaths;
        
        // Test can handle
        Assert.assertEquals(
                deleteAll.canHandle(mockContext), 
                expected);
    }
    
    @Test (dataProvider = "isEnabledTestCases")
    public void testIsEnabled(
            boolean canComposeParent,
            boolean canDeleteParent,
            boolean[] canCompose,
            boolean[] canDelete,
            boolean expected
            ) {
        // Setup inputs
        Collection<View> selection = 
                Arrays.asList(mockView(ViewType.NODE));
        Mockito.when(mockContext.getTargetHousing())
            .thenReturn(mockHousing);
        Mockito.when(mockHousing.getDirectoryArea())
            .thenReturn(mockDirectory);
        Mockito.when(mockDirectory.getSelectedDirectoryNode())
            .thenReturn(mockNode);
        Mockito.when(mockSelection.getSelectedManifestations())
            .thenReturn(selection);
        
        // Setup selected nodes, including a parent
        int nodes = Math.max(canCompose.length, canDelete.length);
        selectedTreePaths = new TreePath[nodes];
        AbstractComponent mockParent = mockComponent(canDeleteParent, canComposeParent);
        MCTMutableTreeNode mockParentNode = Mockito.mock(MCTMutableTreeNode.class);
        View mockParentView = mockView(mockParent, ViewType.NODE);
        Mockito.when(mockParentNode.getUserObject()).thenReturn(mockParentView);

        for (int i = 0; i < nodes; i++) {
            AbstractComponent mockChild = 
                    mockComponent(canDelete [i % canDelete.length ], 
                                  canCompose[i % canCompose.length]);
            
            TreePath mockPath = Mockito.mock(TreePath.class);
            MCTMutableTreeNode mockTreeNode = Mockito.mock(MCTMutableTreeNode.class);
            Mockito.when(mockPath.getLastPathComponent()).thenReturn(mockTreeNode);
            View mockView = mockView(mockChild, ViewType.NODE);            
            Mockito.when(mockTreeNode.getUserObject()).thenReturn(mockView);
            Mockito.when(mockTreeNode.getParent()).thenReturn(mockParentNode);
            selectedTreePaths[i] = mockPath;
        }
        
        // Verify precondition; obey ContextAwareAction life cycle
        Assert.assertTrue(deleteAll.canHandle(mockContext));
        
        // Test isEnabled
        Assert.assertEquals(deleteAll.isEnabled(), expected);
    }

    @Test (dataProvider = "actionPerformedTestCases")
    public void testActionPerformed(
            Collection<AbstractComponent> selected,
            String choice, // User choice in dialog
            final Collection<AbstractComponent> toDelete // null implies no persistence call
            ) {
        // Setup inputs
        Collection<View> selection = 
                Arrays.asList(mockView(ViewType.NODE));
        Mockito.when(mockContext.getTargetHousing())
            .thenReturn(mockHousing);
        Mockito.when(mockHousing.getDirectoryArea())
            .thenReturn(mockDirectory);
        Mockito.when(mockDirectory.getSelectedDirectoryNode())
            .thenReturn(mockNode);
        Mockito.when(mockSelection.getSelectedManifestations())
            .thenReturn(selection);
        Mockito.when(mockPlatform.getPersistenceProvider())
            .thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getWindowManager())
            .thenReturn(mockWindowing);
    
        // Setup selected nodes, including a parent        
        selectedTreePaths = new TreePath[selected.size()];
        AbstractComponent mockParent = mockComponent(false, true);
        MCTMutableTreeNode mockParentNode = Mockito.mock(MCTMutableTreeNode.class);
        View mockParentView = mockView(mockParent, ViewType.NODE);
        Mockito.when(mockParentNode.getUserObject()).thenReturn(mockParentView);

        int i = 0;
        for (AbstractComponent component : selected) {
            TreePath mockPath = Mockito.mock(TreePath.class);
            MCTMutableTreeNode mockTreeNode = Mockito.mock(MCTMutableTreeNode.class);
            Mockito.when(mockPath.getLastPathComponent()).thenReturn(mockTreeNode);
            View mockView = mockView(component, ViewType.NODE);            
            Mockito.when(mockTreeNode.getUserObject()).thenReturn(mockView);
            Mockito.when(mockTreeNode.getParent()).thenReturn(mockParentNode);
            selectedTreePaths[i++] = mockPath;
        }
        
        // Ensure dialog choice
        Mockito.when(mockWindowing.<Object>showInputDialog(
                Mockito.anyString(), Mockito.anyString(), 
                Mockito.<Object[]>any(), Mockito.any(), 
                Mockito.<Map<String,Object>>any()))
                .thenReturn(choice);
        
        // Verify precondition; obey ContextAwareAction life cycle
        Assert.assertTrue(deleteAll.canHandle(mockContext));
        
        // Verify precondition, obey life cycle
        Assert.assertEquals(deleteAll.isEnabled(), true);
        
        // Fire the action
        deleteAll.actionPerformed(Mockito.mock(ActionEvent.class));
        
        // Verify expected consequential interactions
        Mockito.verify(mockWindowing, 
                Mockito.times(toDelete != null ? toDelete.size() : 0))
                .closeWindows(Mockito.anyString());
        Mockito.verify(mockPersistence,
                Mockito.times(toDelete != null ? 1 : 0))
                .delete(Mockito.argThat(
                        new ArgumentMatcher<Collection<AbstractComponent>>() {
                            @Override
                            public boolean matches(Object argument) {
                                if (argument instanceof Collection) {
                                    Collection c = (Collection) argument;
                                    return c.containsAll(toDelete) && 
                                            toDelete.containsAll(c);
                                }
                                return false;
                            }
                        }
                ));
    }
    
    @DataProvider
    public Object[][] canHandleTestCases() {
        Collection<Object[]> testCases = new ArrayList<Object[]>();
        boolean truths[] = {false, true};
        
        Collection<View> validSelection =
                Arrays.asList(mockView(ViewType.NODE));
        
        Collection<?>[] selections = {
                Collections.emptyList(),
                Arrays.asList(mockView(ViewType.OBJECT)),
                validSelection
        };
        
        TreePath[][] treePaths = {
                null,
                {},
                { Mockito.mock(TreePath.class) } 
        };
        
        // Permute possible 
        for (boolean hasHousing : truths) {
            for (Collection<?> selection : selections) {
                for (boolean hasDirectory : truths) {
                    for (boolean hasNode : truths) {
                        for (TreePath[] treePath : treePaths) {
                            // Based expectation on conditions
                            boolean expected =
                                    hasHousing &&
                                    (selection == validSelection) &&
                                    hasDirectory &&
                                    hasNode &&
                                    (treePath != null) &&
                                    (treePath.length != 0);
                            
                            testCases.add(new Object[]{
                                    hasHousing,
                                    selection,
                                    hasDirectory,
                                    hasNode,
                                    treePath,
                                    expected
                            });                            
                        }
                    }
                }
            }
        }
        
        Object[][] result = new Object[testCases.size()][];
        int i = 0;
        Iterator<Object[]> iter = testCases.iterator();
        while (iter.hasNext()) {
            result[i++] = iter.next();
        }
        return result;
    }

    @DataProvider
    public Object[][] isEnabledTestCases() {
        Collection<Object[]> testCases = new ArrayList<Object[]>();
        boolean truths[] = { false, true };

        // A variety of true/false arrangements to permute
        // Note: Arrays are treated as circular by test,
        //       so {true} covers {true,true,true}
        boolean groups[][] = {
                {true},
                {false},
                {false, false, false, false, true},
                {true, true, false}
        };
        
        for (boolean canComposeParent : truths) {
            for (boolean canDeleteParent : truths) {
                for (boolean[] canCompose : groups) {
                    for (boolean[] canDelete : groups) {
                        // Should only care that parent can be composed,
                        // and that all selected children can be deleted
                        boolean expected = canComposeParent;
                        for (boolean canDeleteChild : canDelete) {
                            expected &= canDeleteChild;
                        }
                        testCases.add(new Object[] {
                                canComposeParent,
                                canDeleteParent,
                                canCompose,
                                canDelete,
                                expected
                        });
                    }
                }
            }
        }
        
        Object[][] result = new Object[testCases.size()][];
        int i = 0;
        Iterator<Object[]> iter = testCases.iterator();
        while (iter.hasNext()) {
            result[i++] = iter.next();
        }
        return result;    
    }
    
    @DataProvider
    public Object[][] actionPerformedTestCases() {
        Collection<Object[]> testCases = new ArrayList<Object[]>();
        boolean[][] groups = {
                {true, true},
                {true, false},
                {false, true},
                {false, false}
        };
        boolean[] truths = { true, false };
        
        // Some other parent, somewhere
        AbstractComponent mockParent = mockComponent(false, true);
        
        for (boolean last : truths) { // Whether or not comps are the last manifestations
            for (boolean[] group : groups) {
                // Assemble object graphs
                Collection<AbstractComponent> selected = new ArrayList<AbstractComponent>();
                Collection<AbstractComponent> toDelete = new HashSet<AbstractComponent>();
                for (int i = 0; i < group.length; i++) {
                    AbstractComponent mockComponent = mockComponent(true, true);
                    Mockito.when(mockComponent.getReferencingComponents())
                        .thenReturn(Arrays.asList(mockParent));
                    toDelete.add(mockComponent);
                    selected.add(mockComponent);
                    
                    List<AbstractComponent> children = new ArrayList<AbstractComponent>();
                    for (int j = 0; j < group.length ; j++){
                        AbstractComponent mockChild = mockComponent(group[j], true);                    
                        children.add(mockChild);
                        if (group[j]) {
                            toDelete.add(mockChild);
                        }
                        
                        List<AbstractComponent> grandchildren = new ArrayList<AbstractComponent>();    
                        for (int k = 0; k < group.length; k++) {
                            AbstractComponent mockGrandchild = mockComponent(group[k], true);                      
                            grandchildren.add(mockGrandchild);
                            if (group[j] && group[k]) {
                                toDelete.add(mockGrandchild);
                            }
                            Mockito.when(mockGrandchild.getReferencingComponents())
                                .thenReturn(last ? 
                                        Arrays.asList(mockChild) :
                                        Arrays.asList(mockParent, mockChild));    
                        }
                        Mockito.when(mockChild.getComponents())
                            .thenReturn(grandchildren);
                        Mockito.when(mockChild.getReferencingComponents())
                            .thenReturn(last ? 
                                    Arrays.asList(mockComponent) : 
                                    Arrays.asList(mockParent, mockComponent));
                    }
                    Mockito.when(mockComponent.getComponents())
                            .thenReturn(children);
                }
                
                // Can only delete if everything is deletable,
                // or there are no last manifestations.
                boolean canDelete = true;
                for (boolean delete : group) {
                   if (!delete) {
                       canDelete = !last;
                   }
                }
                
                // Exercise different choices for input dialog
                for (String choice : new String[] {CONFIRM, ABORT, OK}) {
                    if (choice.equals(ABORT) || !canDelete) {
                        toDelete = null;
                    }
                    testCases.add(new Object[]{selected,choice,toDelete});
                }
            }
        }
        
        
        Object[][] result = new Object[testCases.size()][];
        int i = 0;
        Iterator<Object[]> iter = testCases.iterator();
        while (iter.hasNext()) {
            result[i++] = iter.next();
        }
        return result;
    }   
    
    private static int counter = 0;    
    private AbstractComponent mockComponent(boolean canBeDeleted, boolean canBeComposed) {        
        final AbstractComponent mockComponent = 
                Mockito.mock(AbstractComponent.class);
        Mockito.when(mockComponent.getComponents())
                .thenReturn(Collections.<AbstractComponent>emptyList());
        Mockito.when(mockComponent.getComponentId())
                .thenReturn("mock" + counter++);
        if (canBeDeleted) {
            canDelete.add(mockComponent);
        }
        if (canBeComposed) {
            canCompose.add(mockComponent);
        }
        
        return mockComponent;
    }
    
    private View mockView(ViewType vt) {
        return mockView(
                Mockito.mock(AbstractComponent.class),
                vt
                );
    }
    
    private View mockView(AbstractComponent ac, ViewType vt) {
        View view = Mockito.mock(View.class);
        ViewInfo viewInfo = Mockito.mock(ViewInfo.class);
        Mockito.when(viewInfo.getViewType()).thenReturn(vt);
        Mockito.when(view.getInfo()).thenReturn(viewInfo);
        Mockito.when(view.getManifestedComponent()).thenReturn(ac);
        return view;
    }
    
    private class PseudoJTree extends JTree {
        private static final long serialVersionUID = 1L;

        @Override
        public TreePath[] getSelectionPaths() {
            return selectedTreePaths;
        }
    }
    
}
