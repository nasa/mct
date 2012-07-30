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

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.awt.Color;
import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.impl.SplittablePane;
import gov.nasa.arc.mct.util.LafColor;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MCTDirectoryAreaTreeCellRendererTest {

    @Mock private SplittablePane splitPane;
    
    private MCTDirectoryArea.DirectoryTreeCellRenderer renderer;
    
    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        when(splitPane.isSplit()).thenReturn(false);
        renderer = new MCTDirectoryArea.DirectoryTreeCellRenderer();
    }
    
    @Test(dataProvider="cellRendererTests")
    public void testGetTreeCellRendererComponent(boolean sel, boolean isDrop, int row, int dropRow, int childIndex) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        JTree tree = new JTree();
        TreePath path = new TreePath(tree.getModel().getRoot());
        
        JTree.DropLocation loc = null;
        if (isDrop) {
            Constructor<JTree.DropLocation> cons = JTree.DropLocation.class.getDeclaredConstructor(Point.class, TreePath.class, int.class);
            cons.setAccessible(true);
            Point p = new Point(0,0);
            loc = cons.newInstance(p, path, childIndex);
        }
        
        Field f = JTree.class.getDeclaredField("dropLocation");
        f.setAccessible(true);
        f.set(tree, loc);
        
        View view = mock(View.class);
        MCTMutableTreeNode node = new MCTMutableTreeNode();
        node.setUserObject(view);

        View renderView = (View) renderer.getTreeCellRendererComponent(tree, node, sel, false, false, row, false);
        
        assertSame(renderView, view);
        
        // If we do the same thing again, we should get the same thing back.
        // But the 2nd time we don't expect another call to addMonitoredGUI().
        renderView = (View) renderer.getTreeCellRendererComponent(tree, node, sel, false, false, row, false);
        assertSame(renderView, view);

        Color bg =
            (sel || (loc!=null && childIndex==-1 && row==dropRow))
                    ? LafColor.TREE_SELECTION_BACKGROUND
                    : LafColor.WINDOW;
            
        verify(view, times(1)).addMonitoredGUI(node);
        verify(view, times(2)).setBackground(bg);
        
    }
    
    @DataProvider(name="cellRendererTests")
    public Object[][] getCellRendererTests() {
        return new Object[][] {
                new Object[] { false, false, 0, 0, -1 },
                new Object[] { true, false, 0, 0, -1 },
                new Object[] { false, true, 0, 0, -1 },
                new Object[] { false, true, 0, 0, 0 },
                new Object[] { false, true, 1, 0, 0 },
                new Object[] { true, true, 0, 0, -1 }
        };
    }
    
}
