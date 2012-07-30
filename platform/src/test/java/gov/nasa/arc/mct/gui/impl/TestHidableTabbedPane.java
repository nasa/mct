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

import gov.nasa.arc.mct.gui.impl.HidableTabbedPane;
import gov.nasa.arc.mct.gui.util.TestUtilities;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TestHidableTabbedPane {

    @Test
    public void testInstantiation() {
        HidableTabbedPane h;

        h = new HidableTabbedPane();
        assertNotNull(h);
        assertEquals(h.getTabCount(), 0);

        JLabel one = new JLabel("One");
        h = new HidableTabbedPane("Tab One", one);
        assertNotNull(h);
        assertEquals(h.getTabCount(), 1);
        assertEquals(h.getComponentAt(0), one);
        assertNull(h.getTabComponentAt(0)); // No special tab component

        JLabel two = new JLabel("Two");
        h.addTab("Tab Two", two);
        assertEquals(h.getComponentAt(1), two);
        assertNull(h.getTabComponentAt(1));

        JLabel tabLabel = new JLabel("Label Two");
        h.setTabComponentAt(1, tabLabel);
        assertEquals(h.getTabComponentAt(1), tabLabel);
    }

    @Test public void testComponents() {
        HidableTabbedPane h;

        JLabel one = new JLabel("One");
        h = new HidableTabbedPane("Tab One", one);
        assertEquals(h.getComponentAt(0), one);

        h.setComponentAt(0, null);
        assertNull(h.getComponentAt(0));

        JLabel onePrime = new JLabel("One Prime");
        h.setComponentAt(0, onePrime);
        assertEquals(h.getComponentAt(0), onePrime);

        JLabel two = new JLabel("Two");
        h.addTab("Tab Two", two);
        assertEquals(h.getComponentAt(1), two);

        JLabel twoPrime = new JLabel("One Prime");
        h.setComponentAt(1, twoPrime);
        assertEquals(h.getComponentAt(1), twoPrime);

        h.setComponentAt(0, one);
        assertEquals(h.getComponentAt(0), one);
    }

    @Test
    public void testGetSelectedIndex() {
        HidableTabbedPane h;

        h = new HidableTabbedPane();
        assertEquals(h.getSelectedIndex(), 0);

        JLabel one = new JLabel("One");
        h.addTab("Tab Two", one);
        assertEquals(h.getSelectedIndex(), 0);

        JLabel two = new JLabel("Two");
        h.addTab("Tab Two", two);
        assertEquals(h.getSelectedIndex(), 0);

        h.setSelectedIndex(1);
        assertEquals(h.getSelectedIndex(), 1);
    }

    @Test
    public void testSetValidIndex() {
        HidableTabbedPane h;

        JLabel one = new JLabel("One");
        h = new HidableTabbedPane("Tab One", one);
        h.setSelectedIndex(0);
        assertEquals(h.getSelectedIndex(), 0);

        JLabel two = new JLabel("Two");
        h.addTab("Tab Two", two);
        h.setSelectedIndex(1);
        assertEquals(h.getSelectedIndex(), 1);
    }

    @Test(expectedExceptions={IndexOutOfBoundsException.class})
    public void testIndexTooLow() {
        JLabel one = new JLabel("One");
        HidableTabbedPane h = new HidableTabbedPane("Tab One", one);
        h.setSelectedIndex(-2);
    }

    @Test(expectedExceptions={IndexOutOfBoundsException.class})
    public void testIndexTooHigh() {
        JLabel one = new JLabel("One");
        HidableTabbedPane h = new HidableTabbedPane("Tab One", one);
        h.setSelectedIndex(1);
    }
    
    @Test
    public void testRemoveTabAt() throws Exception {
        HidableTabbedPane h;

        JLabel one = new JLabel("One");
        JLabel two = new JLabel("Two");
        JLabel three = new JLabel("Three");
        JLabel four = new JLabel("Three");
        h = new HidableTabbedPane("Tab One", one);
        h.addTab("Tab Two", two);
        h.addTab("Tab Three", three);
        h.addTab("Tab Four", four);
        assertEquals(h.getTabCount(), 4);
        assertEquals(h.getComponentAt(0), one);
        assertEquals(h.getComponentAt(1), two);
        assertEquals(h.getComponentAt(2), three);
        assertEquals(h.getComponentAt(3), four);

        h.removeTabAt(2);
        assertEquals(h.getTabCount(), 3);
        assertEquals(h.getComponentAt(0), one);
        assertEquals(h.getComponentAt(1), two);
        assertEquals(h.getComponentAt(2), four);

        h.removeTabAt(0);
        assertEquals(h.getTabCount(), 2);
        assertEquals(h.getComponentAt(0), two);
        assertEquals(h.getComponentAt(1), four);
        
        h.removeTabAt(0);
        assertEquals(h.getTabCount(), 1);
        assertEquals(h.getComponentAt(0), four);
        
        // Remove the last tab.
        h.removeTabAt(0);
        assertEquals(h.getTabCount(), 0);
    }
    
    @Test
    public void testListener() throws Exception {
        final TestUtilities.Value v = new TestUtilities.Value(false);
        
        HidableTabbedPane h;

        JLabel one = new JLabel("One");
        JLabel two = new JLabel("Two");
        JLabel three = new JLabel("Three");
        JLabel four = new JLabel("Three");
        h = new HidableTabbedPane("Tab One", one);
        h.addTab("Tab Two", two);
        h.addTab("Tab Three", three);
        h.addTab("Tab Four", four);
        
        h.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                v.setValue(true);
            }
        });
        
        h.setSelectedIndex(1);
        
        assertTrue(v.getValue(), "Tab change listener called");
    }
}
