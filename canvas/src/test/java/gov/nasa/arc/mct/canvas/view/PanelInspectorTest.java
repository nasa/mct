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
package gov.nasa.arc.mct.canvas.view;

import gov.nasa.arc.mct.canvas.view.PanelBorderSelectionTest.MockTitleManifestation;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.MCTViewManifestationInfoImpl;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public final class PanelInspectorTest {
    
    private PanelInspector panelInspector;
    
    /*
     * Harleigh108: The following two variables we need for fixing this test, so that it can be run with
     * respect to Java7 (as well as still work in Java6).  Object 'view' used to be Mocked, but
     * this causes test 'testEnterLockedState()' to fail when built with respect to Java 7.
     */
    private View view = null;
    @Mock
    private AbstractComponent mockComponent;    
    
    Method showHideControllerMethod;
    
    @SuppressWarnings("serial")
    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
        /*Harleigh108: 
         * Rather than mocking a view, we create one with dummy-content, this is because adding a mocked container to a container
         * will cause a NPE.  See testMouseDragIncremental in MarqueSelectionListenerTest.java 
         */
        view = new MockManifestation(mockComponent, new ViewInfo(CanvasManifestation.class, "", ViewType.OBJECT)) {
            /*
             * Originally, in 'testLockedState()' we had Mockito.verify(view, Mockito.times(1)).exitLockedState(), that is we verified
             * that the method 'exitLockedState()' is called exactly once during the test 'testLockedState()'.  Since we can not use
             * mock-types (see comments in 'testLockedState()') we implemented the following instead:
             */
            private int NumberCalls = 0; //Number of calls to method exitLockedState
            
            @Override
            public void exitLockedState() {
                NumberCalls= NumberCalls +1; 
                super.exitLockedState();
            }
            
            @Override
            public int getNumberCallsToExitLockedState() {
                return NumberCalls;
            }
            
        };//--end class
        
        MCTViewManifestationInfoImpl info = new MCTViewManifestationInfoImpl();
        view.putClientProperty(CanvasManifestation.MANIFEST_INFO, info);
        
        Mockito.when(mockComponent.getViewInfos(ViewType.TITLE)).thenReturn(Collections.singleton(new ViewInfo(MockTitleManifestation.class,"", ViewType.TITLE)));
        Mockito.when(mockComponent.getDisplayName()).thenReturn("test comp");
        Mockito.when(mockComponent.getComponents()).thenReturn(Collections.<AbstractComponent> emptyList());
                
        AbstractComponent ac = Mockito.mock(AbstractComponent.class);
        ViewInfo vi = Mockito.mock(ViewInfo.class);
        panelInspector = new PanelInspector(ac, vi) {
            @Override
            protected JComponent getViewControls() {
                return new JPanel();
            }
        };
        
        try {
            // set content field
            Field cf = PanelInspector.class.getDeclaredField("content");
            cf.setAccessible(true);
            cf.set(panelInspector, new JPanel());
            
            // set view field
            Field vf = PanelInspector.class.getDeclaredField("view");
            vf.setAccessible(true);
            vf.set(panelInspector, view);
            
            showHideControllerMethod = PanelInspector.class.getDeclaredMethod("showOrHideController", boolean.class);
            showHideControllerMethod.setAccessible(true);
        } catch (SecurityException e) {
            Assert.fail(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Assert.fail(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage(), e);
        }
    }
    
    @Test
    public void testGetHousedViewManifestation() {
        Assert.assertEquals(panelInspector.getHousedViewManifestation(), view);
    }
    
    @Test
    public void testEnterLockedState() {
        try {
            
            // set canvas locked state (isLocked) to true
            Field cf = PanelInspector.class.getDeclaredField("isLocked");            
            cf.setAccessible(true);            
            cf.set(panelInspector, Boolean.TRUE);
                                    
            /* Harleigh108
             * In Java 7 we can not add mocked things to a container. In this case: in method 'showOrHideController' in PanelInspector.java
             * we received a NullPointerException when we added the view to the scroll_pane (i.e. scrollPane.setViewportView(view);).  We got
             * the NPE since 'view' was originally a Mock--google 'Container.add(mock(Container.class)) fails in Java 7'.  The fix was to 
             * create a real View object rather than a mock one.  Why all the NPEs?  java.awt.Container.addImp has been changed between Java 6 and Java 7.
             */
            showHideControllerMethod.invoke(panelInspector, true);
                        
            System.out.println("The number of times exitLockedState() has been called: " + ((MockManifestation)view).getNumberCallsToExitLockedState());

            //Harleigh108:
            /*  During the test for LockedState, we only want the method 'exitLockedState' to be called once, originally we had
             *  Mockito.verify(view, Mockito.times(1)).exitLockedState(); but as we no longer have a mock-type, we have the following instead.
             */            
            Assert.assertEquals(((MockManifestation)view).getNumberCallsToExitLockedState(), 1);
            
        } catch (SecurityException e) {
            Assert.fail(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Assert.fail(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Assert.fail(e.getMessage(), e);
        } catch (AssertionError e) {
            Assert.fail(e.getMessage(), e); 
        }
        
    }
    
    /* Harleigh108:
     * This is a little helper class so I can make a mock-view component without using Mockito (see method 'testMouseDragIncremental'
     * above for more detail.
     */
    @SuppressWarnings("serial")
    public static class MockManifestation extends View {
        public MockManifestation(AbstractComponent component, ViewInfo vi) {
            super(component,vi);
            this.setBackground(Color.GREEN);
        }
       
        public int getNumberCallsToExitLockedState() {
            // TODO Auto-generated method stub
            return 0;
        }
        
    }//End MockManifestation class
    

}
