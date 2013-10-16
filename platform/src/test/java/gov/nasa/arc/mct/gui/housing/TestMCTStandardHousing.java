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
import gov.nasa.arc.mct.components.ObjectManager;
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.JFrame;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestMCTStandardHousing {
    @Mock ViewInfo mockInfo;
    @Mock MockableComponent mockComponent;
    @Mock Platform mockPlatform;
    @Mock WindowManager mockWindowing;
    @Mock SelectionProvider mockSelection;
    @Mock PersistenceProvider mockPersistence;
    @Mock MCTContentArea mockContent;
    @Mock PolicyManager mockPolicy;
    
    // Can't always mock swing components
    private View pseudoView = new MCTHousingViewManifestation() {
        private static final long serialVersionUID = 1L;

        @Override
        public MCTContentArea getContentArea() {
            return mockContent;
        }

        @Override
        public SelectionProvider getSelectionProvider() {
            return mockSelection;
        }

        @Override
        public AbstractComponent getManifestedComponent() {
            return mockComponent;
        }

        @Override
        public ViewInfo getInfo() {
            return mockInfo;
        }        
    };
    
    private MCTStandardHousing housing;
    private Platform oldPlatform;   
    private JFrame frame;
    
    @BeforeClass
    public void cachePlatform() {
        frame = new JFrame();
        frame.setVisible(true);
        oldPlatform = PlatformAccess.getPlatform();
    }
    
    @AfterClass
    public void restorePlatform() {
        frame.dispose();
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowing);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicy);
        Mockito.when(mockComponent.getViewInfos(Mockito.<ViewType>any())).thenReturn(Collections.singleton(mockInfo));
        Mockito.when(mockInfo.createView(mockComponent)).thenReturn(pseudoView);
        Mockito.when(mockContent.getHousedViewManifestation()).thenReturn(pseudoView);
        Mockito.when(mockPolicy.execute(Mockito.anyString(), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null,true,""));
        
        new PlatformAccess().setPlatform(mockPlatform);
    }
    
    @Test (dataProvider="dialogTestCases")
    public void testDialog(
            boolean isDirty,
            boolean isDirtyChild,
            final int choice,
            final int saveCount
            ) throws Exception {
        
        // Set up inputs (manifested component)
        Mockito.when(mockComponent.isDirty()).thenReturn(isDirty || isDirtyChild);
        if (isDirtyChild) {
            AbstractComponent child = Mockito.mock(AbstractComponent.class);
            ObjectManager om = Mockito.mock(ObjectManager.class);
            Mockito.when(om.getAllModifiedObjects()).thenReturn(Collections.singleton(child));
            Mockito.when(mockComponent.handleGetCapability(ObjectManager.class)).thenReturn(om);
        }
        
        // Set up "user input" (window manager's response)
        Mockito.when(mockWindowing.showInputDialog(
                Mockito.<String>any(), Mockito.<String>any(), 
                Mockito.<String[]>any(), Mockito.<String>any(), 
                Mockito.<Map<String,Object>>any())).thenAnswer(new Answer<String>() {
                    @Override
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        String[] options = (String[]) invocation.getArguments()[2];
                        if (options != null && choice < options.length) {
                            return options[choice];
                        }
                        return null;
                    }            
        });
        
        // Create the object and register it
        housing = new MCTStandardHousing("", 100, 100, JFrame.DO_NOTHING_ON_CLOSE, pseudoView);
        UserEnvironmentRegistry.registerHousing(housing);
              
        // Invoke the action
        housing.closeHousing(); 
        
        // Verify that dirty objects were persisted as expected
        Mockito.verify(mockPersistence, Mockito.times(saveCount > 0 ? 1 : 0)).persist(
                Mockito.argThat(new ArgumentMatcher<Collection<AbstractComponent>>() {
            @SuppressWarnings("rawtypes")
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Collection &&
                        ((Collection) argument).size() == saveCount; 
            }            
        }));

    }
    
    @DataProvider
    public Object[][] dialogTestCases() {       
        Object[][] testCases = new Object[16][];
        boolean truths[] = { false, true };
        
        int i = 0;
        for (boolean isDirty : truths) {
            for (boolean isDirtyChild : truths) {
                for (int choice = 0; choice < 4; choice++) {
                    int saveCount = 0;
                    if (isDirty && choice==0) saveCount = 1;
                    if (isDirtyChild && choice==1) saveCount = 2;
                    testCases[i++] = new Object[]{isDirty, isDirtyChild, choice, saveCount};
                }
            }
        }
        
        return testCases;
    }
    
    // To expose handleGetCapability to mocking
    private static class MockableComponent extends AbstractComponent {
        @Override
        protected <T> T handleGetCapability(Class<T> capability) {
            return super.handleGetCapability(capability);
        }        
    }
}
