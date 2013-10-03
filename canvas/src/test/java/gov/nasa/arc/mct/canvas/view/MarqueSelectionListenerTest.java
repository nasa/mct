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

import gov.nasa.arc.mct.canvas.panel.Panel;
import gov.nasa.arc.mct.gui.MCTViewManifestationInfoImpl;
import gov.nasa.arc.mct.canvas.view.MarqueSelectionListener.MultipleSelectionProvider;
import gov.nasa.arc.mct.canvas.view.PanelBorderSelectionTest.MockTitleManifestation;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JPanel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MarqueSelectionListenerTest {
    @Mock
    MultipleSelectionProvider mockSelectionProvider;
    
    /*
     * Harleigh108: these two variables we need for fixing this test, so that it can be run with
     * respect to Java7 (as well as still work in Java6)
     */
    private View panelManifestation = null;
    @Mock
    private AbstractComponent mockComponent;
    //End new variables--Harleigh108
    
    private JPanel rootPanel;
    
    private MarqueSelectionListener listener;
    

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        rootPanel = new JPanel();
        listener = new MarqueSelectionListener(rootPanel, mockSelectionProvider);
        
        
        ///////Harleigh108
        Mockito.when(mockComponent.getViewInfos(ViewType.TITLE)).thenReturn(Collections.singleton(new ViewInfo(MockTitleManifestation.class,"", ViewType.TITLE)));
        Mockito.when(mockComponent.getDisplayName()).thenReturn("test comp");
        Mockito.when(mockComponent.getComponents()).thenReturn(Collections.<AbstractComponent> emptyList());
        //
    }


    @DataProvider(name="mouseDataWithCursor")
    public Object[][] getData() {
        int[] cursors = 
            new int[] {
                        Cursor.MOVE_CURSOR,
                        Cursor.N_RESIZE_CURSOR,
                        Cursor.NE_RESIZE_CURSOR,
                        Cursor.NW_RESIZE_CURSOR,
                        Cursor.S_RESIZE_CURSOR ,
                        Cursor.SE_RESIZE_CURSOR,
                        Cursor.SW_RESIZE_CURSOR,
                        Cursor.E_RESIZE_CURSOR,
                        Cursor.W_RESIZE_CURSOR
        };
        Object[][] parameters = new Object[cursors.length][];
        for (int i = 0; i < cursors.length; i++) {
            JPanel panel = new JPanel();
            panel.setCursor(Cursor.getPredefinedCursor(cursors[i]));
            parameters[i] = new Object[] {
                new MouseEvent(panel,123,System.currentTimeMillis(),0,2,2,1,false)
            };
        }
        
        return parameters;
    }
    
    @Test(dataProvider="mouseDataWithCursor")
    /**
     * ensure mouse dragged will not start a drag sequence if the state is not appropriate. This can be caused
     * because of the mouse cursor (another gesture is already active, resizing for example). 
     */
    public void testMouseDraggedStartCursorMove(MouseEvent e) {
        Mockito.when(mockSelectionProvider.pointInTopLevelPanel((Point)Mockito.anyObject())).thenReturn(true);
        listener.mouseDragged(e);
        Assert.assertEquals(rootPanel.getComponents().length, 0,
                        "marquee selection should not be added because cursor indicates move or resize event"
                        );
    }
    
    @Test
    /**
     * ensure mouse dragging with a point inside the container does not start a selection. The mouse movement
     * in this case should be delegated back into the owning component and not start a marquee selection. 
     */
    public void testMouseDraggedInsideAComponent() {
        JPanel panel = new JPanel();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        MouseEvent e =
            new MouseEvent(panel,123,System.currentTimeMillis(),0,2,2,1,false);
        Mockito.when(mockSelectionProvider.pointInTopLevelPanel((Point)Mockito.anyObject())).thenReturn(false);
        listener.mouseDragged(e);
        Assert.assertEquals(rootPanel.getComponents().length, 0,
                        "marquee selection should not be added the starting mouse event is inside an enclosing panel"
                        );
    }
    
    @DataProvider(name="mouseDraggingDirections")
    public Object[][] getMouseData() {
        return new Object[][] {
              new Object[] {new Rectangle(2,2,1,1), new Point(3,3)},
              new Object[] {new Rectangle(1,1,1,1), new Point(1,1)},
              new Object[] {new Rectangle(2,1,1,1), new Point(3,1)},
              new Object[] {new Rectangle(1,2,1,1), new Point(1,3)}
        };
    }
    
    @Test(dataProvider="mouseDraggingDirections")
    public void testMouseDraggedStart(Rectangle expectedBounds, Point mouseClicked) {
        JPanel panel = new JPanel();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        MouseEvent e =
            new MouseEvent(panel,123,System.currentTimeMillis(),0,2,2,1,false);
        Mockito.when(mockSelectionProvider.pointInTopLevelPanel((Point)Mockito.anyObject())).thenReturn(true);
        listener.mouseDragged(e);
        Assert.assertEquals(rootPanel.getComponents().length, 1,
                        "marquee selection should have been started"
                        );
        
        // now drag the mouse and check the selection is adjusted correctly
        e = new MouseEvent(panel,1234,System.currentTimeMillis(),0,mouseClicked.x,mouseClicked.y,1,false);
        listener.mouseDragged(e);
        Assert.assertEquals(rootPanel.getComponents()[0].getBounds(),expectedBounds);
    }
    
    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testMouseDragIncremental() {
        JPanel panel = new JPanel();
        
        /*
         * Harleigh108: MarqueSelectionListenerTest was failing when we built with respect to Java 7, the root cause (pun intended)
         * was that we were adding a mocked panel to the rootPanel.  As it turns out 'Container.add(mock(Container.class)) ' fails
         * in Java 7 but works in Java 6.  Why? Well, java.awt.Container.addImpl has changed between Java 6 and 7.
         *    The Solution: instead of adding a mock component (using Mockito), we have instead made a real View object when creating
         *    a panel.  Also, we used to have  Mockito.when(child.getBounds()).thenReturn(new Rectangle(2,2,1,1)); but because of
         *    the java 6-to-7 issue, we chose to make an abstract class here and override the getBounds method.
         * The old code was beautiful:
         *    Panel child = Mockito.mock(Panel.class);
         *    Mockito.when(child.getBounds()).thenReturn(new Rectangle(2,2,1,1));
         *    rootPanel.add(child);
         * But now we have the following:
         */
        View v = new View(){};
        v.putClientProperty(CanvasManifestation.MANIFEST_INFO, new MCTViewManifestationInfoImpl() );
        
        panelManifestation = new MockManifestation(mockComponent, new ViewInfo(CanvasManifestation.class, "", ViewType.OBJECT));
        MCTViewManifestationInfoImpl info = new MCTViewManifestationInfoImpl();
        panelManifestation.putClientProperty(CanvasManifestation.MANIFEST_INFO, info);
        
        Panel child = new Panel(panelManifestation,Mockito.mock(PanelFocusSelectionProvider.class) ) {  
            public Rectangle getBounds() {
                return new Rectangle(2,2,1,1);
            }
        };
        
        rootPanel.add(child);

        MouseEvent e =
            new MouseEvent(panel,123,System.currentTimeMillis(),0,2,2,1,false);
        Mockito.when(mockSelectionProvider.pointInTopLevelPanel((Point)Mockito.anyObject())).thenReturn(true);
        listener.mouseDragged(e);
        listener.mouseReleased(e);
        Mockito.verify(mockSelectionProvider).selectPanels((Collection<Panel>)Mockito.anyObject());
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
    }
    
}
