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
import gov.nasa.arc.mct.defaults.view.NodeViewManifestation;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.fest.util.Arrays;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class DuplicateActionTest {
    @SuppressWarnings("serial")
    private DuplicateAction action = new DuplicateAction() {
        protected boolean isComponentCreatable(AbstractComponent ac) {
            return true;
        };        
    };
    
    @Test
    public void testHandleNoSelectionInDirectory() {
        MCTHousing mockHousing = Mockito.mock(MCTHousing.class);
        MCTDirectoryArea mockDirectoryArea = Mockito.mock(MCTDirectoryArea.class);
        Mockito.when(mockHousing.getDirectoryArea()).thenReturn(mockDirectoryArea);
        Mockito.when(mockDirectoryArea.getSelectedManifestations()).thenReturn(Collections.<View>emptySet());
        
        ActionContextImpl context = new ActionContextImpl();        
        context.setTargetHousing(mockHousing);
        Assert.assertFalse(action.canHandle(context));
    }
    
    @Test
    public void testIsEnabled() throws Exception {
        AbstractComponent componentWithExternalKey = Mockito.mock(AbstractComponent.class);
        Mockito.when(componentWithExternalKey.getExternalKey()).thenReturn("key");
        AbstractComponent componentWithoutExternalKey = Mockito.mock(AbstractComponent.class);
        
        Field f = DuplicateAction.class.getDeclaredField("actionContext");
        f.setAccessible(true);
        ActionContextImpl actionContext = new ActionContextImpl();
        f.set(action, actionContext);
        
        actionContext.setTargetComponent(componentWithoutExternalKey);
        Assert.assertTrue(action.isEnabled());
        actionContext.setTargetComponent(componentWithExternalKey);
        Assert.assertFalse(action.isEnabled());
    }
    
    public static class TestPolicyClass implements Policy {
        @Override
        public ExecutionResult execute(PolicyContext context) {
           return new ExecutionResult(context,false,"");
        }
    }
    
    private static class ExtendedComponentProviderTest extends ExtendedComponentProvider {
        public ExtendedComponentProviderTest() {
            super(null, "test");
        }
        
        @Override
        public Collection<PolicyInfo> getPolicyInfos() {
            return Collections.singleton(new PolicyInfo(PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey(),TestPolicyClass.class));
        }
    }
    
    @Test
    public void cannotHandle() {
        PolicyManagerImpl.getInstance().refreshExtendedPolicies(Collections.<ExtendedComponentProvider>singletonList(new ExtendedComponentProviderTest()));
        MCTHousing housing = Mockito.mock(MCTHousing.class);
        MCTDirectoryArea directoryArea = Mockito.mock(MCTDirectoryArea.class);
        MCTMutableTreeNode selectedNode = Mockito.mock(MCTMutableTreeNode.class); 
        MCTMutableTreeNode parentNode = Mockito.mock(MCTMutableTreeNode.class);
        AbstractComponent ac = Mockito.mock(AbstractComponent.class);
        AbstractComponent parent = Mockito.mock(AbstractComponent.class);
        JTree tree = Mockito.mock(JTree.class);
        TreePath treePath = Mockito.mock(TreePath.class);
        
        ActionContextImpl actionContext = Mockito.mock(ActionContextImpl.class);
        NodeViewManifestation selectedNodeView = Mockito.mock(NodeViewManifestation.class);        
        NodeViewManifestation parentNodeView = Mockito.mock(NodeViewManifestation.class);        
        Mockito.when(actionContext.getTargetHousing()).thenReturn(housing);
        Mockito.when(actionContext.getTargetComponent()).thenReturn(ac);
        Mockito.when(housing.getDirectoryArea()).thenReturn(directoryArea);
        Mockito.when(directoryArea.getSelectedManifestations()).thenReturn(Collections.<View>singleton(selectedNodeView));
        Mockito.when(directoryArea.getSelectedDirectoryNode()).thenReturn(selectedNode);
        Mockito.when(selectedNode.getParentTree()).thenReturn(tree);
        Mockito.when(tree.getSelectionPaths()).thenReturn(Arrays.array(treePath));
        Mockito.when(treePath.getLastPathComponent()).thenReturn(selectedNode);
        Mockito.when(selectedNode.getParent()).thenReturn(parentNode);
        Mockito.when(parentNode.getUserObject()).thenReturn(parentNodeView);
        Mockito.when(parentNodeView.getManifestedComponent()).thenReturn(parent);
        
        Assert.assertFalse(action.canHandle(actionContext));
    }
}
