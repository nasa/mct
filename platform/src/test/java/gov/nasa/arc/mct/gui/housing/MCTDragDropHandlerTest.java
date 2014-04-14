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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

public class MCTDragDropHandlerTest {

    @Mock View mockTarget;
    @Mock View mockNonTarget;
        
    @Mock Platform mockPlatform;
    @Mock WindowManager mockWindowManager;
    @Mock PolicyManager mockPolicyManager;
    @Mock CoreComponentRegistry mockRegistry;
    @Mock PersistenceProvider mockPersistence;
    @Mock User mockUser;
    
    private Platform oldPlatform;
    
    private List<View> mockSelections = new ArrayList<View>();
    private List<AbstractComponent> pseudoComponents = new ArrayList<AbstractComponent>();
    private List<AbstractComponent> pseudoClones = new ArrayList<AbstractComponent>();
    private AbstractComponent pseudoTargetComponent;
    private AbstractComponent pseudoNonTargetComponent;
    
    @BeforeClass
    public void setupPlatform() {
        oldPlatform = PlatformAccess.getPlatform();
    }
    
    @AfterClass
    public void resetPlatform() {
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
    
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this); 
        
        // Mocking the platform
        new PlatformAccess().setPlatform(mockPlatform);
        
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockRegistry);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        Mockito.when(mockPlatform.getCurrentUser()).thenReturn(mockUser);
        
        Mockito.when(mockUser.getUserId()).thenReturn("mockUser");
        Mockito.when(mockRegistry.isCreatable(Mockito.<Class<?>>any())).thenReturn(true);
        Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext>any())).thenAnswer(
                new Answer<ExecutionResult>() {
                    @Override
                    public ExecutionResult answer(InvocationOnMock invocation) throws Throwable {
                        return new ExecutionResult((PolicyContext) invocation.getArguments()[1], true, "");
                    }                    
                }
        );
        
        mockSelections.clear();
        pseudoComponents.clear();
        pseudoClones.clear();
        pseudoTargetComponent = new PseudoComponent();
        pseudoNonTargetComponent = new PseudoComponent();
                
        Mockito.when(mockTarget.getManifestedComponent()).thenReturn(pseudoTargetComponent);
        Mockito.when(mockNonTarget.getManifestedComponent()).thenReturn(pseudoNonTargetComponent);
        
        for (int i = 0; i < 3; i++) {
            View mockView = Mockito.mock(View.class);
            final AbstractComponent pseudoComponentClone = new PseudoComponent();
            AbstractComponent pseudoComponent = new PseudoComponent() {
                @Override
                public AbstractComponent clone() {
                    return pseudoComponentClone;
                }
            };
            
            Mockito.when(mockView.getManifestedComponent()).thenReturn(pseudoComponent);
            Mockito.when(mockView.getParentView()).thenReturn(mockNonTarget);
            
            mockSelections.add(mockView);
            pseudoComponents.add(pseudoComponent);
            pseudoClones.add(pseudoComponentClone);
        }
        
    }
    
    @Test
    public void testMessage() {
        final String expected = "this is a policy message";
        
        Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext>any())).thenAnswer(
                new Answer<ExecutionResult>() {
                    @Override
                    public ExecutionResult answer(InvocationOnMock invocation) throws Throwable {
                        return new ExecutionResult((PolicyContext) invocation.getArguments()[1], false, expected);
                    }                    
                }
        );
        
        // Ensure that all three options are available under baseline/nominal preconditions
        MCTDragDropHandler handler = new MCTDragDropHandler(mockSelections, mockTarget, -1);
        
        handler.perform();
        
        Assert.assertEquals(handler.getMessage(), expected);
    }
    
    @Test
    public void testAllOptions() {
        // Ensure that all three options are available under baseline/nominal preconditions
        MCTDragDropHandler handler = new MCTDragDropHandler(mockSelections, mockTarget, -1);
        
        handler.perform();
        
        Mockito.verify(mockWindowManager).<Object>showInputDialog(
                Mockito.anyString(), 
                Mockito.anyString(), 
                Mockito.argThat(new ArgumentMatcher<Object[]>() {
                    @Override
                    public boolean matches(Object argument) {
                        return ((Object[])argument).length == 3;
                    }                    
                }),
                Mockito.<Object>any(),
                Mockito.<Map<String,Object>> any());
    }

    @Test (dataProvider = "testProvider")
    public void testAction(final String actionName, boolean expectCopies) {
        // Ensure that all three options are available under baseline/nominal preconditions
        MCTDragDropHandler handler = new MCTDragDropHandler(mockSelections, mockTarget, -1);
        
        Mockito.when(mockWindowManager.<Object>showInputDialog(
                Mockito.anyString(), 
                Mockito.anyString(), 
                Mockito.<Object[]>any(),
                Mockito.<Object>any(),
                Mockito.<Map<String,Object>> any())).thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Object[] options = (Object[]) invocation.getArguments()[2];
                        for (Object option : options) {
                            if (option.toString().equals(actionName)) {
                                return option;
                            }
                        }
                        return null;
                    }
                    
                });

        handler.perform();
        
        List<AbstractComponent> expected = expectCopies ? pseudoClones : pseudoComponents;
        Collections.reverse(expected);
        Assert.assertEquals(handler.getDroppedComponents(), expected);
    }
    
    @DataProvider
    public Object[][] testProvider() {
        return new Object[][] {
                {MCTDragDropHandler.MOVE_NAME, false},
                {MCTDragDropHandler.LINK_NAME, false},
                {MCTDragDropHandler.COPY_NAME, true }
        };
    }
    
    private static class PseudoComponent extends AbstractComponent {
        public PseudoComponent() {
            initialize();
        }
    }
}
