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
package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.plaf.ComboBoxUI;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestSwitcherView {
    @Mock ViewProvider mockViewProvider;
    @Mock ViewInfo mockViewInfo1;
    @Mock ViewInfo mockViewInfo2;
    @Mock Icon mockIcon;
    @Mock AbstractComponent mockComponent;
    
    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        
        @SuppressWarnings("serial")
        View view = new View(mockComponent, mockViewInfo1) {};
        Mockito.when(mockViewProvider.getHousedViewManifestation())
            .thenReturn(view);
        
        // Enforce desired test order of view infos, such that mockViewInfo1 is selected by default
        Set<ViewInfo> mockViewInfos = new TreeSet<ViewInfo>(new Comparator<ViewInfo>() {
            @Override
            public int compare(ViewInfo o1, ViewInfo o2) {
                return (o1.equals(o2)) ?  0 :
                    ((o1 == mockViewInfo1) ? -1 : 1);
            }
        });
        mockViewInfos.add(mockViewInfo1);
        mockViewInfos.add(mockViewInfo2);
        Mockito.when(mockComponent.getViewInfos(ViewType.OBJECT))
            .thenReturn(mockViewInfos);
        
    }
    
    @Test (dataProvider = "generateSwitcherTests")
    public void testSwitcher(boolean addGUI, boolean doChange, boolean acceptChange) {
        // Create the test object
        View switcherView = SwitcherView.VIEW_INFO.createView(mockComponent);
        
        // Act as though the view provider has/hasn't accepted the view switch
        Mockito.when(mockViewProvider.setHousedViewManifestation(mockViewInfo2))
            .thenReturn(acceptChange);
        
        // Attach view provider to gui, if this test iteration says we should
        if (addGUI) {
            switcherView.addMonitoredGUI(mockViewProvider);
        }
        
        // Find combo box so we can change selection
        @SuppressWarnings("rawtypes")
        JComboBox comboBox = findComponent(switcherView, JComboBox.class);
        Assert.assertNotNull(comboBox);
        
        // Change the item - again, only if we're supposed to
        if (doChange) {
            comboBox.setSelectedItem(mockViewInfo2);
        }
        
        // Verify that the change was reported to the GUI, 
        // if a GUI was attached and the change was done
        Mockito.verify(mockViewProvider, Mockito.times(addGUI && doChange ? 1 : 0))
            .setHousedViewManifestation(mockViewInfo2);
        
        // Verify that combo box state has/hasn't reset, as apparopriate
        // Specifically, only should if change was done, and an attached GUI didn't reject it
        boolean shouldChange = doChange && !(addGUI && !acceptChange);
        Assert.assertEquals(comboBox.getSelectedItem(), 
                shouldChange ? mockViewInfo2 : mockViewInfo1);
    }
    
    @Test
    public void testViewCardinalities() {      
        // SwitcherView should appear differently depending on number of views available
        // Multiple views -> Combo box
        // One view       -> Label
        // No views       -> Neither
        
        // Create the test object
        View switcherView = SwitcherView.VIEW_INFO.createView(mockComponent);
        
        // During normal initialization, should have two view infos.
        // A Combo Box should be found.
        Assert.assertNotNull(findComponent(switcherView, JComboBox.class));
        
        // With only one view info, should have a label instead
        Mockito.when(mockComponent.getViewInfos(ViewType.OBJECT))
            .thenReturn(Collections.singleton(mockViewInfo1));
        switcherView = SwitcherView.VIEW_INFO.createView(mockComponent);
        
        Assert.assertNull(findComponent(switcherView, JComboBox.class));
        Assert.assertNotNull(findComponent(switcherView, JLabel.class));
        
        // With no views, there should be no label or combo box
        Mockito.when(mockComponent.getViewInfos(ViewType.OBJECT))
            .thenReturn(Collections.<ViewInfo>emptySet());
        switcherView = SwitcherView.VIEW_INFO.createView(mockComponent);
    
        Assert.assertNull(findComponent(switcherView, JComboBox.class));
        Assert.assertNull(findComponent(switcherView, JLabel.class));
    
        
    }
    
    @Test
    public void testBadInput() {
        // Make sure that view does not break for unexpected input
        // (verify that null checks are present.)
        
        // Create the test object
        View switcherView = SwitcherView.VIEW_INFO.createView(mockComponent);
        
        // Add monitored GUI can take a variety of object types
        // Make sure an unexpected object type does not trigger an exception
        switcherView.addMonitoredGUI(null);
        switcherView.addMonitoredGUI("hello");        
        Mockito.when(mockViewProvider.getHousedViewManifestation())
            .thenReturn(null);
        switcherView.addMonitoredGUI(mockViewProvider);
    }
    
    @Test
    public void testAppearance() {
        // Exercise custom ComboBoxUI
        // Mostly visual, so not much to verify
        
        // Create the test object
        View switcherView = SwitcherView.VIEW_INFO.createView(mockComponent);
        
        // Find combo box so we can test its UI object
        @SuppressWarnings("rawtypes")
        JComboBox comboBox = findComponent(switcherView, JComboBox.class);        
        ComboBoxUI ui = comboBox.getUI();
        
                
        // Verify that ui paints a rounded rect (per spec)
        Graphics mockGraphics = Mockito.mock(Graphics.class, Mockito.RETURNS_MOCKS);
        ui.paint(mockGraphics, comboBox);
        Mockito.verify(mockGraphics).fillRoundRect(
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),Mockito.anyInt(),Mockito.anyInt(),Mockito.anyInt());
        
    }
    
    @DataProvider
    public Object[][] generateSwitcherTests() {
        Object[][] tests = new Object[8][];
        boolean[] truths = { true, false };
        int i = 0;
        for (boolean addGUI : truths) {
            for (boolean doChange : truths) {
                for (boolean acceptChange : truths) {
                    tests[i] = new Object[] { addGUI, doChange, acceptChange };
                    i++;  
                }
            }
        }
        return tests;
    }
    
    
    private <T extends Container> T findComponent(Container c, Class<T> targetClass) {
        if (targetClass.isAssignableFrom(c.getClass())) {
            return targetClass.cast(c);
        } else {
            for (Component child : c.getComponents()) {
                if (child instanceof Container) {
                    T comboBox = findComponent((Container) child, targetClass);
                    if (comboBox != null) {
                        return comboBox;
                    }
                }
            }
        }
        return null;
    }
}
