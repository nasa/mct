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
package gov.nasa.arc.mct.gui.util;

import java.util.Collections;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.db.util.IdGenerator;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGUIUtil {

    private MockComponent componentA;
    private MockComponent componentB;
    private MockComponent componentC;
    
    @Mock
    private Platform mockPlatform;
    @Mock
    private PolicyManager mockPolicyManager;
    @Mock
    private PersistenceProvider mockPersistenceProvider;
    
    /*
     * Component A
     *   |
     *   +-- Component B
     *         |
     *         +-- Component C
     */
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        (new PlatformAccess()).setPlatform(mockPlatform);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);
        Mockito.when(mockPersistenceProvider.getReferencedComponents(Mockito.any(AbstractComponent.class))).thenReturn(Collections.<AbstractComponent>emptyList());
        ExecutionResult er = new ExecutionResult(null, true, null);
        Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(er);
        
        componentA = new MockComponent(IdGenerator.nextComponentId(), false);
        componentA.getCapability(ComponentInitializer.class).initialize();

        componentB = new MockComponent(IdGenerator.nextComponentId(),false);
        componentB.getCapability(ComponentInitializer.class).initialize();

        componentC = new MockComponent(IdGenerator.nextComponentId(), false);
        componentC.getCapability(ComponentInitializer.class).initialize();
        
        componentA.addDelegateComponent(componentB);
        componentB.addDelegateComponent(componentC);
    }
    
    @Test
    public void testLazyLoading() {
        // Clone subtree of component A
        MCTMutableTreeNode clonedTreeNode = GUIUtil.cloneTreeNode(componentA, new ViewInfo(NodeViewTest.class, "", ViewType.NODE));
        Assert.assertNotNull(clonedTreeNode);
        Assert.assertEquals(clonedTreeNode.getChildCount(), 1);
        Assert.assertTrue(clonedTreeNode.isProxy());
    }
    
    public final static class NodeViewTest extends View {
        private static final long serialVersionUID = -4755584459025450842L;

        public NodeViewTest(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
    }

}
