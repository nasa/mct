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
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.event.ActionEvent;
import java.util.Map;

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

/**
 * Unit tests for Refresh action
 * @author vwoeltje
 *
 */
public class TestRefresh {
    private Platform platform;
    private Platform mockPlatform;
    
    @Mock private ActionContext mockContext;
    @Mock private MCTHousingViewManifestation mockHousing;
    @Mock private MCTContentArea mockContentArea;
    @Mock private AbstractComponent mockComponent;
    @Mock private View mockView;
    @Mock private ViewInfo mockViewInfo;
    @Mock private ActionEvent mockEvent;
    @Mock private WindowManager mockWindowManager;
    @Mock private PersistenceProvider mockPersistence;
    
    @BeforeClass
    public void setup() {
        platform = PlatformAccess.getPlatform();
        mockPlatform = Mockito.mock(Platform.class);
        new PlatformAccess().setPlatform(mockPlatform);        
    }
    
    @AfterClass
    public void teardown() {
        new PlatformAccess().setPlatform(platform);
    }

    @BeforeMethod
    public void setupMethod() {
        MockitoAnnotations.initMocks(this);
        
        Mockito.when(mockContext.getWindowManifestation()).thenReturn(mockHousing);
        Mockito.when(mockHousing.getContentArea()).thenReturn(mockContentArea);
        Mockito.when(mockContentArea.getHousedViewManifestation()).thenReturn(mockView);
        Mockito.when(mockHousing.getManifestedComponent()).thenReturn(mockComponent);
        Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);
        Mockito.when(mockView.getInfo()).thenReturn(mockViewInfo);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistence);
        Mockito.when(mockPersistence.getComponent(Mockito.anyString())).thenReturn(mockComponent);
        Mockito.when(mockComponent.getComponentId()).thenReturn("mock");
    }
    
    // Test various 'canHandle' responses
    @Test
    public void testCanHandle() {
        // Mock context has a content area, so we should be able to handle this
        Assert.assertTrue(new RefreshAction().canHandle(mockContext));
        
        // With no housing, action can't be handled
        ActionContext noHousingContext = Mockito.mock(ActionContext.class);
        Mockito.when(noHousingContext.getWindowManifestation()).thenReturn(null);
        Assert.assertFalse(new RefreshAction().canHandle(noHousingContext));

        // With no content area, action can't be handled
        ActionContext noContentContext = Mockito.mock(ActionContext.class);
        MCTHousingViewManifestation noContentHousing = Mockito.mock(MCTHousingViewManifestation.class);
        Mockito.when(noContentContext.getWindowManifestation()).thenReturn(noContentHousing);
        Mockito.when(noContentHousing.getContentArea()).thenReturn(null);
        Assert.assertFalse(new RefreshAction().canHandle(noHousingContext));
    }
    
    @Test
    public void testIsEnabled() {
        // Should always be enabled if canHandle
        ContextAwareAction refresh = new RefreshAction();
        
        // Verify precondition (also verifed in previous test)
        // canHandle is a necessary part of ContextAwareAction life cycle
        Assert.assertTrue(refresh.canHandle(mockContext));
        
        // Verify that refresh menu item is enabled
        Assert.assertTrue(refresh.isEnabled());
    }
    
    @Test
    public void testActionPerformedNonDirty() {       
        Mockito.when(mockComponent.isDirty()).thenReturn(false);
        
        // The newer version of the component, for which a view should be created
        AbstractComponent newerComponent = Mockito.mock(AbstractComponent.class);
        Mockito.when(mockPersistence.getComponent("mock")).thenReturn(newerComponent);
        
        // Similarly, newer version of the view
        View newerView = Mockito.mock(View.class);
        Mockito.when(mockViewInfo.createView(newerComponent)).thenReturn(newerView);
        
        // Create refresh action, obey life cycle
        ContextAwareAction refresh = new RefreshAction();
        Assert.assertTrue(refresh.canHandle(mockContext));
        
        // Verify preconditions
        Mockito.verifyZeroInteractions(mockWindowManager);
        Mockito.verifyZeroInteractions(mockContentArea);
        Mockito.verifyZeroInteractions(mockViewInfo);
        
        // Perform action
        refresh.actionPerformed(mockEvent);
        
        // Verify that view was updated
        Mockito.verify(mockViewInfo).createView(newerComponent);
        Mockito.verify(mockContentArea).setOwnerComponentCanvasManifestation(newerView);
        
        // Verify that user was NOT prompted
        Mockito.verifyZeroInteractions(mockWindowManager);
    }
    
    @Test (dataProvider = "truthData")
    public void testActionPerformedDirty(final boolean confirmed) {
        Mockito.when(mockComponent.isDirty()).thenReturn(true);
        
        // The newer version of the component, for which a view should be created
        AbstractComponent newerComponent = Mockito.mock(AbstractComponent.class);
        Mockito.when(mockPersistence.getComponent("mock")).thenReturn(newerComponent);
        
        // Similarly, newer version of the view
        View newerView = Mockito.mock(View.class);
        Mockito.when(mockViewInfo.createView(newerComponent)).thenReturn(newerView);       
        
        // Mock user response to dialog
        Mockito.when(mockWindowManager.showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] options = (Object[]) invocation.getArguments()[2];
                return confirmed ? options[0] : options[1]; // options[0] presumed to mean "OK"
            }
        });
        
        // Create refresh action, obey life cycle
        ContextAwareAction refresh = new RefreshAction();
        Assert.assertTrue(refresh.canHandle(mockContext));
        
        // Verify preconditions
        Mockito.verifyZeroInteractions(mockWindowManager);
        Mockito.verifyZeroInteractions(mockContentArea);
        Mockito.verifyZeroInteractions(mockViewInfo);        
               
        // Perform action
        refresh.actionPerformed(mockEvent);

        // Verify that dialog was invoked
        Mockito.verify(mockWindowManager).showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any());
        
        // Verify that view was updated only if confirmed
        if (confirmed) {
            Mockito.verify(mockViewInfo).createView(newerComponent);
            Mockito.verify(mockContentArea).setOwnerComponentCanvasManifestation(newerView);
        } else {
            Mockito.verify(mockViewInfo, Mockito.never()).createView(Mockito.<AbstractComponent>any());
            Mockito.verify(mockContentArea, Mockito.never()).setOwnerComponentCanvasManifestation(Mockito.<View>any());            
        }
        
        // Verify that user was NOT prompted
        Mockito.verifyZeroInteractions(mockWindowManager);
    }
    
    @DataProvider
    public Object[][] truthData() {
        return new Object[][] { {true} , {false} };
    }
}
