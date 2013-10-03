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
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.mockito.ArgumentMatcher;
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

public class TestInspector {

    private static final String TEST_COMPONENT_ID = "test";
    
    // Allow mocking a Container/SelectionProvider
    @SuppressWarnings("serial")
    abstract class SelectionContainer extends Container implements SelectionProvider {}
    
    @Mock private AbstractComponent mockComponent;
    @Mock private ViewInfo mockViewInfo;
    @Mock private ViewInfo mockInspectorInfo;
    @Mock private SelectionContainer mockSelection;
    @Mock private AncestorEvent mockAncestorEvent;
    @Mock private PropertyChangeEvent mockPropertyEvent;
    @Mock private Platform mockPlatform;
    @Mock private PersistenceProvider mockPersistence;
    @Mock private PolicyManager mockPolicyManager;
    @Mock private WindowManager mockWindowManager;
    
    private PropertyChangeListener inspectorPropertyChangeListener;
    private View view;
    private Inspector inspector;
    
    private Platform originalPlatform;
    
    @SuppressWarnings("serial")
    @BeforeMethod
    public void setupMethod() {
        MockitoAnnotations.initMocks(this);

        originalPlatform = PlatformAccess.getPlatform();
        
        // Can't mock Swing components reliably, so create a real one
        view = new View(mockComponent, mockViewInfo) {};
        
        new PlatformAccess().setPlatform(mockPlatform);
        
        inspectorPropertyChangeListener = null;
        
        Mockito.when(mockComponent.getViewInfos(Mockito.<ViewType>any()))
            .thenReturn(Collections.singleton(mockViewInfo));
        Mockito.when(mockViewInfo.createView(mockComponent))
            .thenReturn(view);
        Mockito.when(mockAncestorEvent.getAncestorParent())
            .thenReturn(mockSelection);
        Mockito.when(mockPropertyEvent.getNewValue())
            .thenReturn(Collections.singleton(view));
        Mockito.when(mockPlatform.getPersistenceProvider())
            .thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getPolicyManager())
            .thenReturn(mockPolicyManager);
        Mockito.when(mockPlatform.getWindowManager())
            .thenReturn(mockWindowManager);
        Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null, true, ""));
        Mockito.when(mockPersistence.getComponent(TEST_COMPONENT_ID))
            .thenReturn(mockComponent);
        Mockito.when(mockComponent.getComponentId())
            .thenReturn(TEST_COMPONENT_ID);
        Mockito.when(mockViewInfo.getType())
            .thenReturn("mock");
        
        
        // Used to retrieve the property change listener exposed by the 
        // inspector, which in turn can be used to handle 'selections'
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                inspectorPropertyChangeListener = (PropertyChangeListener) invocation.getArguments()[0];
                return null;
            }           
        }).when(mockSelection).addSelectionChangeListener(Mockito.any(PropertyChangeListener.class));;
        
        inspector = new Inspector(mockComponent, mockInspectorInfo);
        for (AncestorListener al : inspector.getAncestorListeners()) {
            al.ancestorAdded(mockAncestorEvent);
        }
        
    }
    
    @AfterMethod
    public void teardownMethod() {
        new PlatformAccess().setPlatform(originalPlatform);
    }
    
    @Test
    public void testGetCurrentlyShowingComponent() {
        // Select something in the inspector
        inspectorPropertyChangeListener.propertyChange(mockPropertyEvent);       
        
        // Verufy that we are showing mockComponent now
        Assert.assertEquals(inspector.getCurrentlyShowingComponent(), mockComponent);
    }
    
    @Test
    public void testRefreshInspector() {
        // Select something in the inspector
        inspectorPropertyChangeListener.propertyChange(mockPropertyEvent);
        
        // Reset, since some desired behavior may have been called by way of selecting
        Mockito.reset(mockViewInfo, mockPersistence);        
        Mockito.when(mockViewInfo.createView(mockComponent)).thenReturn(view);
        Mockito.when(mockPersistence.getComponent(TEST_COMPONENT_ID)).thenReturn(mockComponent);
        
        // Call method
        inspector.refreshCurrentlyShowingView();
        
        // Should create a new view from persistence
        Mockito.verify(mockPersistence).getComponent(TEST_COMPONENT_ID);
        Mockito.verify(mockViewInfo).createView(mockComponent);        
    }
    
    @Test
    public void testGetHousedViewManifestation() {
        // Should not be housing any view by default
        Assert.assertNull(inspector.getHousedViewManifestation());
        
        // Select something
        inspectorPropertyChangeListener.propertyChange(mockPropertyEvent);
        
        // Should now be showing the available view for that component
        Assert.assertEquals(inspector.getHousedViewManifestation(), view);
    }
    
    @Test (dataProvider="generateSetCases")
    public void testSetHousedViewManifestation(boolean stale, boolean dirty, final boolean save, boolean expectPrompt, boolean expectChange, boolean expectSave) {
        // Select something in the inspector
        inspectorPropertyChangeListener.propertyChange(mockPropertyEvent);
        
        // Create a new view to switch to
        ViewInfo newViewInfo = Mockito.mock(ViewInfo.class);       
        @SuppressWarnings("serial")
        View     newView = new View(mockComponent, newViewInfo) {};
               
        Mockito.when(mockComponent.isStale()).thenReturn(stale);
        Mockito.when(mockComponent.isDirty()).thenReturn(dirty);
        Mockito.when(newViewInfo.createView(mockComponent)).thenReturn(newView);
        Mockito.when(newViewInfo.getType()).thenReturn("new");
        Mockito.when(mockWindowManager.showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] options = (Object[]) invocation.getArguments()[2];
                return save ? options[0] : options[options.length-1]; // options[0] presumed to mean "OK"
            }
        });
        
        // Try to change housed view
        inspector.setHousedViewManifestation(newViewInfo);
        
        // Verify that dialog was popped up, iff it was expected
        Mockito.verify(mockWindowManager, Mockito.times(expectPrompt ? 1 : 0))
            .showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any());
        
        // Verify that view has or hasn't changed, based on expectations
        Assert.assertEquals(inspector.getHousedViewManifestation(), expectChange ? newView : view);
        
        // Verify that component was saved, if that was expected
        Mockito.verify(mockPersistence, Mockito.times(expectSave ? 1 : 0)).persist(Mockito.<Collection<AbstractComponent>>argThat(new ArgumentMatcher<Collection<AbstractComponent>>() {
            @Override
            public boolean matches(Object argument) {                
                return argument instanceof Collection && ((Collection)argument).contains(mockComponent);
            }
        }));
    }
    
    @DataProvider
    public Object[][] generateSetCases() {
        Object cases[][] = new Object[8][];
        int i = 0;
        boolean truths[] = { false, true };
        for (boolean stale : truths) {
            for (boolean dirty : truths) {
                for (boolean save : truths) {
                    cases[i] = new Object[] {
                            stale,
                            dirty,
                            save,
                            !stale && dirty, // Prompt if dirty, unless also stale (can't save anyway)
                            true, // Always expect view change - cancel not yet supported
                            !stale && dirty && save
                    };
                    i++;
                }
            }
        }
        return cases;
    }
}
