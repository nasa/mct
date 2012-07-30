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

import gov.nasa.arc.mct.component.MockComponentProvider;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.defaults.view.NodeViewManifestation;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Window;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestWindowManagerImpl {
    
    @Test
    public void testOpenInNewWindow() {
        JFrame f = new JFrame();
        f.setContentPane(new JPanel());
        f.getContentPane().add(new JButton("press me"));
        f.pack();
        f.setVisible(true);
        
        @SuppressWarnings("unchecked")
        ComponentProvider provider = new MockComponentProvider(Collections
                .EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Arrays.asList(
                        new ViewInfo(NodeViewManifestation.class, "test", ViewType.NODE)));
        
        ExternalComponentRegistryImpl.getInstance().refreshComponents(Collections.singletonList(new ExtendedComponentProvider(provider, "")));
        
        AbstractComponent mockedComponent = Mockito.mock(AbstractComponent.class);
        Set<ViewInfo> returnedViewInfos = Collections.singleton(new ViewInfo(NodeViewManifestation.class, "t", ViewType.NODE));
        Mockito.when(mockedComponent.getViewInfos(ViewType.NODE)).thenReturn(returnedViewInfos);
        Mockito.when(mockedComponent.getDisplayName()).thenReturn("testComponent");
        
        Mockito.when(mockedComponent.isLeaf()).thenReturn(true);
        Assert.assertTrue(mockedComponent.isLeaf());
        
        WindowManagerImplOverride windowMgr = new WindowManagerImplOverride();
        windowMgr.openInNewWindow(mockedComponent);
        
        Assert.assertEquals(windowMgr.displayName, mockedComponent.getDisplayName());
        Assert.assertSame(windowMgr.nodeView.getViewClass(), NodeViewManifestation.class);
        
        Assert.assertEquals(windowMgr.horizontalScale, WindowManagerImpl.LEAF_HORIZONTAL_SCALE);
        Assert.assertEquals(windowMgr.verticalScale, WindowManagerImpl.LEAF_VERTICAL_SCALE);
        
        Mockito.when(mockedComponent.isLeaf()).thenReturn(false);
        windowMgr.openInNewWindow(mockedComponent);
        
        Assert.assertEquals(windowMgr.horizontalScale, WindowManagerImpl.NON_LEAF_HORIZONTAL_SCALE);
        Assert.assertEquals(windowMgr.verticalScale, WindowManagerImpl.NON_LEAF_VERTICAL_SCALE);
        
        //Assert.assertSame(windowMgr.activeWindow, f);
        f.setVisible(false);
        f.dispose();
    }
}

class WindowManagerImplOverride extends WindowManagerImpl {
    public String displayName;
    public ViewInfo nodeView;
    public Window activeWindow;
    public double horizontalScale;
    public double verticalScale;
    
    @Override
    protected void openInWindow(String displayName, ViewInfo nodeView, Window activeWindow, 
                                double horizontalScale, double verticalScale, AbstractComponent component) {
        this.displayName = displayName;
        this.nodeView = nodeView;
        this.activeWindow = activeWindow;
        this.horizontalScale = horizontalScale;
        this.verticalScale = verticalScale;
        
    }
}