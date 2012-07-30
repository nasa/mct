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
package gov.nasa.arc.mct.gui.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import gov.nasa.arc.mct.gui.impl.SplittablePane;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestSplittablePane {

    private JPanel content;
    private JLabel main;
    private JLabel secondary;

    @BeforeMethod
    public void setup() {
        content = new JPanel();
        content.setSize(300, 300);
        content.setLayout(new BorderLayout());

        main = new JLabel("main");
        secondary = new JLabel("secondary");
    }
    
    @Test
    public void testConstructors() throws Exception {
        SplittablePane splitPane;
        
        splitPane = new SplittablePane();
        splitPane.setSize(200, 100);
        assertNotNull(splitPane);
        assertTrue(!splitPane.isSplit());
        
        splitPane = new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main);
        assertEquals(splitPane.getMainComponent(), main);
        assertTrue(!splitPane.isSplit());

        splitPane = new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        assertEquals(splitPane.getMainComponent(), main);
        assertEquals(splitPane.getSecondaryComponent(), secondary);
        assertTrue(!splitPane.isSplit());
    }
    
    @Test
    public void testShowSplit() {
        SplittablePane splitPane;
        
        splitPane= new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        splitPane.setSize(200, 100);
        content.add(splitPane, BorderLayout.CENTER);
        
        assertTrue(!splitPane.isSplit());
        
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());
        assertEquals(splitPane.getDividerFraction(), SplittablePane.DEFAULT_DIVIDER_LOCATION, 0.1);

        // Show it, even though it's already shown.
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());

        splitPane.hideSplit();
        splitPane.setMainComponent(null);
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());
    }
    
    @Test
    public void testHideSplit() {
        SplittablePane splitPane;
        
        splitPane= new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        splitPane.setSize(200, 100);
        content.add(splitPane, BorderLayout.CENTER);
        content.doLayout();
        
        assertTrue(!splitPane.isSplit());
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());
        
        splitPane.hideSplit();
        assertTrue(!splitPane.isSplit());

        // Hide it again, to ensure nothing happens.
        splitPane.hideSplit();
        assertTrue(!splitPane.isSplit());
    }

    @Test
    public void testSetMainComponent() {
        SplittablePane splitPane;
        JLabel other = new JLabel("other");;
        
        splitPane = new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        splitPane.setSize(200, 100);
        assertEquals(splitPane.getMainComponent(), main);
        
        splitPane.setMainComponent(other);
        assertEquals(splitPane.getMainComponent(), other);
        
        splitPane = new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, null);
        assertEquals(splitPane.getMainComponent(), null);
        splitPane.setMainComponent(other);
        assertEquals(splitPane.getMainComponent(), other);
        
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());
        assertEquals(splitPane.getMainComponent(), other);
        splitPane.setMainComponent(secondary);
        assertEquals(splitPane.getMainComponent(), secondary);
        
        splitPane = new SplittablePane(SplittablePane.VERTICAL_SPLIT, main, secondary);
        assertEquals(splitPane.getMainComponent(), main);
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());
        
        splitPane.setMainComponent(other);
        assertEquals(splitPane.getMainComponent(), other);
    }
    
    @Test
    public void testSetDividerFraction() throws Exception {
        SplittablePane splitPane;
        
        splitPane= new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        splitPane.setSize(200, 100);
        content.add(splitPane, BorderLayout.CENTER);
        content.doLayout();
        
        assertTrue(!splitPane.isSplit());
        splitPane.showSplit();
        assertTrue(splitPane.isSplit());

        assertEquals(splitPane.getDividerFraction(), SplittablePane.DEFAULT_DIVIDER_LOCATION, 0.1);
        
        splitPane.setDividerFraction(0.5);
        assertEquals(splitPane.getDividerFraction(), 0.5, 0.1);
        
        splitPane.hideSplit();
        splitPane.setDividerFraction(0.3);
        assertEquals(splitPane.getDividerFraction(), 0.3, 0.1);
    }
    
    @Test
    public void testSecondaryComponent() throws Exception {
        SplittablePane splitPane;
        JLabel other = new JLabel("other");;
        
        splitPane = new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        assertEquals(splitPane.getMainComponent(), main);
        assertEquals(splitPane.getSecondaryComponent(), secondary);
        
        splitPane.setSecondaryComponent(other);
        assertEquals(splitPane.getSecondaryComponent(), other);
        
        // Also test with vertical split.
        splitPane = new SplittablePane(SplittablePane.VERTICAL_SPLIT, main, secondary);
        assertEquals(splitPane.getMainComponent(), main);
        assertEquals(splitPane.getSecondaryComponent(), secondary);
        
        splitPane.setSecondaryComponent(other);
        assertEquals(splitPane.getSecondaryComponent(), other);
    }

    @Test
    public void testSaveDividerLocation() throws Exception {
        SplittablePane splitPane = new SplittablePane(SplittablePane.HORIZONTAL_SPLIT, main, secondary);
        splitPane.setSize(200, 100);
        
        content.add(splitPane, BorderLayout.CENTER);
        content.doLayout();

        splitPane.showSplit();
        splitPane.saveDividerLocation();
        assertEquals(splitPane.getDividerFraction(), SplittablePane.DEFAULT_DIVIDER_LOCATION, 0.1);

        splitPane.setDividerFraction(0.4);
        splitPane.saveDividerLocation();
        assertEquals(splitPane.getDividerFraction(), 0.3, 0.1);
        
        splitPane.hideSplit();
        splitPane.saveDividerLocation();
        assertEquals(splitPane.getDividerFraction(), 0.3, 0.1);
    }
}
