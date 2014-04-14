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
package gov.nasa.arc.mct.gui.housing;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MCTDirectoryAreaTransferHandlerTest {

    private static final DataFlavor GOOD_FLAVOR = View.DATA_FLAVOR;
    private static final DataFlavor BAD_FLAVOR = DataFlavor.imageFlavor;

    @Mock private JTree tree;
    @Mock private DefaultTreeModel model;
    @Mock private Platform mockPlatform;
    @Mock private PolicyManager mockPolicyManager;

    private MCTDirectoryArea dir;
    private StubbedHandler handler;
    
    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        dir = new MCTDirectoryArea();
        handler = new StubbedHandler(dir, tree, model);
        (new PlatformAccess()).setPlatform(mockPlatform);
        when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        ExecutionResult result = new ExecutionResult(new PolicyContext(), true, "mock");
        when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(result);
     }
    
    @AfterMethod
    public void tearDown() {
        (new PlatformAccess()).releasePlatform();
    }
    
    @Test
    public void testGetSourceActions() {
        JComponent c = mock(JTree.class);
        assertEquals(handler.getSourceActions(c), TransferHandler.NONE);
    }
    
    @Test(dataProvider="createTransferableTests")
    public void testCreateTransferable(TreePath[] paths, View[] views) throws UnsupportedFlavorException, IOException {
        when(tree.getSelectionPaths()).thenReturn(paths);

        ViewRoleSelection selection = (ViewRoleSelection) handler.createTransferable(tree);
        if (paths.length == 0) {
            assertNull(selection);
        } else {
            assertEquals(selection.getTransferDataFlavors().length, 1);
            DataFlavor flavor = selection.getTransferDataFlavors()[0];
            assertSame(flavor.getRepresentationClass(), View.class);
            View[] selectedViewRoles = (View[]) selection.getTransferData(flavor);
            assertEquals(selectedViewRoles.length, views.length);
            
            List<View> expectedViews = Arrays.asList(views);
            for (View view : selectedViewRoles) {
                assertTrue(expectedViews.contains(view));
            }
        }
    }
    
    @DataProvider(name="createTransferableTests")
    public Object[][] getCreateTransferableTests() {
    	 View p1View = mock(View.class);
         View cView = mock(View.class);
         MCTMutableTreeNode g = createMockNode(null);
         MCTMutableTreeNode p1 = createMockNode(p1View);
         MCTMutableTreeNode p2 = createMockNode(null);
         MCTMutableTreeNode c = createMockNode(cView);
         
         TreePath path1 = new TreePath(new Object[]{g, p1});
         TreePath path2 = new TreePath(new Object[]{g, p2, c});
         
         return new Object[][] {
                 new Object[] { new TreePath[0], new View[0] },
                 new Object[] { new TreePath[] { path1, path2 }, new View[] { p1View, cView } }
         };
    }
    
    private MCTMutableTreeNode createMockNode(View view) {
        MCTMutableTreeNode node = new MCTMutableTreeNode();
        if (view != null) {
            node.setUserObject(view);
        }
        return node;
    }
    
    @Test(dataProvider="canImportTests")
    public void testCanImport(boolean isDrop, DataFlavor flavor, boolean canImport) throws Exception {
        JComponent comp = mock(JComponent.class);
        DataFlavor[] flavors = flavor!=null ? new DataFlavor[]{ flavor } : new DataFlavor[0];
        Transferable transferable = createMockTransferable(flavors);
        TransferSupport support;
        if (!isDrop) {
            support = createPasteTransferSupport(comp, transferable);
        } else {
            support = createDropTransferSupport(comp, transferable, flavors);
        }
        
        assertEquals(handler.canImport(support), canImport);
    }
    
    private Transferable createMockTransferable(DataFlavor[] flavors) {
        Transferable transferable = mock(Transferable.class);
        when(transferable.getTransferDataFlavors()).thenReturn(flavors);
        return transferable;
    }
    
    private TransferSupport createPasteTransferSupport(Component comp, Transferable transferable) {
        return new TransferSupport(comp, transferable);
    }
    
    private TransferSupport createDropTransferSupport(Component comp, Transferable transferable, DataFlavor[] flavors) throws Exception {
        DropTargetDropEvent event = mock(DropTargetDropEvent.class);
        when(event.getTransferable()).thenReturn(transferable);
        when(event.getCurrentDataFlavors()).thenReturn(flavors);
        when(event.getLocation()).thenReturn(new Point(0,0));
        
        Constructor<TransferSupport> constructor = TransferSupport.class.getDeclaredConstructor(Component.class, DropTargetEvent.class);
        constructor.setAccessible(true);
        return constructor.newInstance(comp, event);
    }
    
    @DataProvider(name="canImportTests")
    public Object[][] getCanImportTests() {
        return new Object[][] {
                new Object[] { false, GOOD_FLAVOR, false },
                new Object[] { true, BAD_FLAVOR, false },
                new Object[] { true, GOOD_FLAVOR, true },
                new Object[] { true, null, false }
        };
    }
    
    @SuppressWarnings("unchecked")
    @Test(dataProvider="importTests")
    public void testImport(int childIndex) throws Exception {
        // To verify (AbstractComponent cannot be entirely stubbed)
        final Collection<AbstractComponent> added = new ArrayList<AbstractComponent>();
        
        // Set up the root node.
        View rootView = mock(View.class);
        MCTMutableTreeNode root = createMockNode(rootView);
        when(model.getRoot()).thenReturn(root);

        DataFlavor[] flavors = new DataFlavor[]{GOOD_FLAVOR};
        Transferable transferable = createMockTransferable(flavors);
        
        Component mockTree = mock(Component.class);
        
        TransferSupport support = createDropTransferSupport(mockTree, transferable, flavors);

        View targetView = mock(View.class);
        MCTMutableTreeNode target = createMockNode(targetView);
        AbstractComponent targetComponent = new AbstractComponent() {

            @Override
            protected void addDelegateComponentsCallback(Collection<AbstractComponent> childComponents) {
                added.addAll(childComponents);
                super.addDelegateComponentsCallback(childComponents);
            }
            
            
        };
        when(targetView.getManifestedComponent()).thenReturn(targetComponent);
        
        View mockView = mock(View.class);
        AbstractComponent mockComponent = new AbstractComponent() {};
        when(mockView.getManifestedComponent()).thenReturn(mockComponent);
        
        when(transferable.getTransferData(eq(GOOD_FLAVOR))).thenReturn(new View[]{mockView});
        
        // Mock persistence provider, policy manager, and platform
        PlatformAccess access = new PlatformAccess();
        Platform mockPlatform = Mockito.mock(Platform.class);
        PersistenceProvider mockPersistenceProvider = Mockito.mock(PersistenceProvider.class);
        PolicyManager mockpoPolicyManager = Mockito.mock(PolicyManager.class);       
        CoreComponentRegistry mockComponentRegistry = Mockito.mock(CoreComponentRegistry.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        
        access.setPlatform(mockPlatform);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockComponentRegistry);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        ExecutionResult mockResult = new ExecutionResult(new PolicyContext(), true, "");
        Mockito.when(mockpoPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(mockResult);
        Mockito.when(mockWindowManager.<Object>showInputDialog(
                Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.anyMap()))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return ((Object[])invocation.getArguments()[2])[0];
                    }            
        });
        
        handler.internalImport(support, target, new TreePath(target), childIndex);
        Assert.assertEquals(added,Collections.singleton(mockComponent));
    }
    
    @DataProvider(name="importTests")
    public Object[][] getImportTests() {
        return new Object[][] {
                new Object[] { -1 },
                new Object[] { 0 },
                new Object[] { 1 },
        };
    }
    
    @SuppressWarnings("unchecked")
    @Test(dataProvider="importTests")
    public void testImportIntoEmptyRoot(int childIndex) throws Exception {
        // Set up the root node.
        View rootView = mock(View.class);
        MCTMutableTreeNode root = createMockNode(rootView);
        when(model.getRoot()).thenReturn(root);
        AbstractComponent rootComponent = Mockito.mock(AbstractComponent.class);
        Mockito.when(rootView.getManifestedComponent()).thenReturn(rootComponent);

        DataFlavor[] flavors = new DataFlavor[]{GOOD_FLAVOR};
        Transferable transferable = createMockTransferable(flavors);
        
        Component mockTree = mock(Component.class);
        
        TransferSupport support = createDropTransferSupport(mockTree, transferable, flavors);

        View sourceView = mock(View.class);
        
        when(transferable.getTransferData(eq(GOOD_FLAVOR))).thenReturn(new View[]{sourceView});
        
        // Mock persistence provider, policy manager, and platform.
        PlatformAccess access = new PlatformAccess();
        Platform mockPlatform = Mockito.mock(Platform.class);
        PersistenceProvider mockPersistenceProvider = Mockito.mock(PersistenceProvider.class);
        PolicyManager mockpoPolicyManager = Mockito.mock(PolicyManager.class);       
        CoreComponentRegistry mockComponentRegistry = Mockito.mock(CoreComponentRegistry.class);
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        
        access.setPlatform(mockPlatform);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);        
        ExecutionResult mockResult = new ExecutionResult(new PolicyContext(), true, "");
        Mockito.when(mockpoPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(mockResult);
        Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockComponentRegistry);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        Mockito.when(mockWindowManager.<Object>showInputDialog(
                Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.anyMap()))
                .thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return ((Object[])invocation.getArguments()[2])[0];
                    }            
        });
        handler.internalImport(support, root, new TreePath(root), childIndex);
        
        // Verify that the tree model root structure changed.
        verify(model).nodeStructureChanged(root);
    }
    
    private static class StubbedHandler extends MCTDirectoryArea.DirectoryTreeTransferHandler {
        private static final long serialVersionUID = 1L;
    	public StubbedHandler(MCTDirectoryArea dir, JTree tree, DefaultTreeModel model) {
    		super(dir, model);
    	}
    }    
    
}
