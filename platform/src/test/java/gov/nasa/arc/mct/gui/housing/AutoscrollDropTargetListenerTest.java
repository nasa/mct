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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AutoscrollDropTargetListenerTest {

    @Mock private Timer timer;
    @Mock private JTree tree;
    @Mock private DropTargetDragEvent dragEvent;
    @Mock private DropTargetDropEvent dropEvent;
    
    private AutoscrollDropTargetListener listener;
    
    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        listener = new AutoscrollDropTargetListener(tree, timer);
    }
    
    // Make sure the initial delay is set.
    @Test
    public void testInitTimer() {
        verify(timer).setInitialDelay(anyInt());
    }
    
    @Test
    public void testStartDragging() {
        when(timer.isRunning()).thenReturn(false, true);
        listener.dragEnter(dragEvent);
        listener.dragEnter(dragEvent);
        verify(timer, times(1)).start();
    }
    
    @Test
    public void testExitWhenDragging() {
        when(timer.isRunning()).thenReturn(true, false);
        listener.dragExit(dragEvent);
        listener.dragExit(dragEvent);
        verify(timer, times(1)).stop();
    }
    
    @Test
    public void testDrop() {
        when(timer.isRunning()).thenReturn(true, false);
        listener.drop(dropEvent);
        listener.drop(dropEvent);
        verify(timer, times(1)).stop();
    }
    
    @Test(dataProvider="scrollTests")
    public void testScroll(
            int mouseX,
            int mouseY,
            int treeWidth,
            int treeHeight,
            int visibleX,
            int visibleY,
            int visibleWidth,
            int visibleHeight,
            int scrollXSign,
            int scrollYSign
    ) {
        Rectangle bounds = new Rectangle(-visibleX, -visibleY, treeWidth, treeHeight);
        Rectangle visible = new Rectangle(visibleX, visibleY, visibleWidth, visibleHeight);     
        
        when(tree.getBounds()).thenReturn(bounds);
        when(tree.getVisibleRect()).thenReturn(visible);
        when(tree.getScrollableUnitIncrement(isA(Rectangle.class), eq(SwingConstants.HORIZONTAL), anyInt())).thenReturn(1);
        when(tree.getScrollableUnitIncrement(isA(Rectangle.class), eq(SwingConstants.VERTICAL), anyInt())).thenReturn(1);
        
        try {
            Field mouseField = listener.getClass().getDeclaredField("mouse"); 
            mouseField.setAccessible(true);
            mouseField.set(listener, new Point(mouseX, mouseY));
        } catch (Exception e) {
            Assert.fail(); // We need to be able to explicitly set the listener's recorded mouse position
        }
        
        ActionEvent ae = mock(ActionEvent.class);
        listener.actionPerformed(ae);
        
        verifyZeroInteractions(ae);

        ArgumentCaptor<Rectangle> newVisibleArg = ArgumentCaptor.forClass(Rectangle.class);
        if (scrollXSign==0 && scrollYSign==0) {
            verify(tree, never()).scrollRectToVisible(isA(Rectangle.class));
        } else {
            verify(tree).scrollRectToVisible(newVisibleArg.capture());
            Rectangle newVisible = newVisibleArg.getValue();
            if (scrollXSign < 0) {
                assertTrue(newVisible.x < visible.x);
            } else if (scrollXSign > 0) {
                assertTrue(newVisible.x > visible.x);
            }
            if (scrollYSign < 0) {
                assertTrue(newVisible.y < visible.y);
            } else if (scrollYSign > 0) {
                assertTrue(newVisible.y > visible.y);
            }
        }
        
    }
    
    @DataProvider(name="scrollTests")
    public Object[][] getScrollTests() {
        return new Object[][] {
                // If all is visible, should never scroll.
                new Object[] { 0, 0, 100, 100, 0, 0, 100, 100, 0, 0 },
                new Object[] { 50, 0, 100, 100, 0, 0, 100, 100, 0, 0 },
                new Object[] { 99, 0, 100, 100, 0, 0, 100, 100, 0, 0 },
                new Object[] { 0, 99, 100, 100, 0, 0, 100, 100, 0, 0 },
                new Object[] { 50, 99, 100, 100, 0, 0, 100, 100, 0, 0 },
                new Object[] { 99, 99, 100, 100, 0, 0, 100, 100, 0, 0 },
                
                // If in the border area when not all is visible, should scroll.
                new Object[] { 21, 21, 100, 100, 20, 20, 60, 60, -1, -1 },
                new Object[] { 40, 21, 100, 100, 20, 20, 60, 60, 0, -1 },
                new Object[] { 79, 21, 100, 100, 20, 20, 60, 60, 1, -1 },
                new Object[] { 21, 40, 100, 100, 20, 20, 60, 60, -1, 0 },
                new Object[] { 79, 40, 100, 100, 20, 20, 60, 60, 1, 0 },
                new Object[] { 21, 79, 100, 100, 20, 20, 60, 60, -1, 1 },
                new Object[] { 40, 79, 100, 100, 20, 20, 60, 60, 0, 1 },
                new Object[] { 79, 79, 100, 100, 20, 20, 60, 60, 1, 1 },
                
                // If not in the border area when not all is visible, should not scroll.
                new Object[] { 40, 40, 100, 100, 20, 20, 60, 60, 0, 0 },
        };
    }
    
    @Test(dataProvider="scrollTests")
    public void testScrollWhenMouseNull(
            int mouseX,
            int mouseY,
            int treeWidth,
            int treeHeight,
            int visibleX,
            int visibleY,
            int visibleWidth,
            int visibleHeight,
            int scrollXSign,
            int scrollYSign
    ) {
        Rectangle bounds = new Rectangle(-visibleX, -visibleY, treeWidth, treeHeight);
        Rectangle visible = new Rectangle(visibleX, visibleY, visibleWidth, visibleHeight);
        
        when(tree.getBounds()).thenReturn(bounds);
        when(tree.getVisibleRect()).thenReturn(visible);
        when(tree.getMousePosition()).thenReturn(null);
        when(tree.getScrollableUnitIncrement(isA(Rectangle.class), eq(SwingConstants.HORIZONTAL), anyInt())).thenReturn(1);
        when(tree.getScrollableUnitIncrement(isA(Rectangle.class), eq(SwingConstants.VERTICAL), anyInt())).thenReturn(1);
        
        ActionEvent ae = mock(ActionEvent.class);
        listener.actionPerformed(ae);
        
        // Should never scroll.
        verify(tree, never()).scrollRectToVisible(isA(Rectangle.class));
        verifyZeroInteractions(ae);
    }
    
    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testBadInstantiationWithTimer() {
        JComponent o = mock(JComponent.class);
        @SuppressWarnings("unused")
        AutoscrollDropTargetListener l = new AutoscrollDropTargetListener(o, timer);
    }
    
    @Test(expectedExceptions={IllegalArgumentException.class})
    public void testBadInstantiationWithoutTimer() {
        JComponent o = mock(JComponent.class);
        @SuppressWarnings("unused")
        AutoscrollDropTargetListener l = new AutoscrollDropTargetListener(o);
    }
    
}
