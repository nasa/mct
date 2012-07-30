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

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTContentArea.PopupListener;
import gov.nasa.arc.mct.gui.impl.ActionManager;
import gov.nasa.arc.mct.gui.menu.housing.ViewMenu;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestContentArea {

    @SuppressWarnings("serial")
    class HousingCombo extends MCTStandardHousing implements MCTHousing {

        public HousingCombo(int width, int height, int closeAction, byte areaSelection, View housingView) {
            super(width, height, closeAction, housingView);
        }
    }

    @Mock
    HousingCombo mockHousing;
    @Mock
    AbstractComponent mockComponent;
    @Mock
    View canvasManifestation;
    @Mock
    View canvasManifestation2;
    @Mock
    SelectionProvider mockProvider;
    @Mock
    SelectionProvider mockProvider2;
    @Mock
    Platform mockPlatform;
    @Mock
    PolicyManager mockPolicyManager;

    private MCTContentArea contentArea;

    /**
     * Configure private variables that are accessed internally by swing. 
     * @param manifestation
     */
    private void configureManifestation(View manifestation) {
        try {
            Field f = Container.class.getDeclaredField("component");
            f.setAccessible(true);
            f.set(manifestation, Collections.<Component>emptyList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @AfterMethod
    protected void teardown() {
        (new PlatformAccess()).setPlatform(null);
    }
    
    @BeforeMethod
    protected void postSetup() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        MockitoAnnotations.initMocks(this);
        
        (new PlatformAccess()).setPlatform(mockPlatform);
        when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(new ExecutionResult(null,true,""));
        when(canvasManifestation.getInfo()).thenReturn(new ViewInfo(TestView.class,"",ViewType.CENTER));
        when(canvasManifestation.getComponents()).thenReturn(new Component[] {});
        configureManifestation(canvasManifestation);
        when(canvasManifestation.getSelectionProvider()).thenReturn(mockProvider);
        when(mockProvider.getSelectedManifestations()).thenReturn(Collections.singleton(canvasManifestation2));
        
        when(canvasManifestation2.getComponents()).thenReturn(new Component[] {});
        when(canvasManifestation2.getInfo()).thenReturn(new ViewInfo(TestView.class,"",ViewType.CENTER));
        configureManifestation(canvasManifestation2);
        when(canvasManifestation2.getSelectionProvider()).thenReturn(mockProvider2);
        when(mockProvider2.getSelectedManifestations()).thenReturn(Collections.singleton(canvasManifestation));

        Set<ViewInfo> vrs = new HashSet<ViewInfo>();
        ViewInfo vi = Mockito.mock(ViewInfo.class);
        View v = new TestView(mockComponent, vi);
        Mockito.when(vi.getViewName()).thenReturn("testView");
        Mockito.when(vi.createView(Mockito.any(AbstractComponent.class))).thenReturn(v);
        vrs.add(vi);
        when(mockComponent.getViewInfos(ViewType.CENTER)).thenReturn(vrs);

        PersistenceProvider mockPersistenceProvider = Mockito.mock(PersistenceProvider.class);
        when(mockPersistenceProvider.getComponent(Mockito.anyString())).thenReturn(mockComponent);
        when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);
        contentArea = new MCTContentArea(mockHousing, mockComponent);
    }

    @Test
    public void testConstructor() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        assertNotNull(contentArea);
    }

    @Test
    public void testGetContentAreaPane() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        assertNotNull(contentArea.getContentAreaPane());
    }
    
    @Test
    public void testGetOwnerComponent() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        assertNotNull(contentArea.getOwnerComponent());
    }

    @Test
    public void testPopupListener() {
        ActionManager.registerMenu(ViewMenu.class, "VIEW_MENU");
        PopupListener listener = contentArea.new PopupListener();
        listener.setTestMode();
        MouseEvent e = new MouseEvent(contentArea, 0, 0, 0, 0, 0, 0, true);
        assertFalse(listener.popupActivated());
        listener.mousePressed(e);
        assertTrue(listener.popupActivated());
        ActionManager.unregisterMenu(ViewMenu.class, "VIEW_MENU");
    }
    
    @Test
    public void testIsAreaEmpty() {
        JPanel jp = new JPanel();
        jp.add(new JButton());
        
        JPanel jp2 = new JPanel();
        jp2.add(new JPanel());
        Component[] components = new Component[] {
                jp,
                jp2
        };
        Mockito.when(canvasManifestation.getComponents()).thenReturn(components);
        Assert.assertTrue(contentArea.isAreaEmpty());
        setOwnerCanvasManifestation(contentArea, canvasManifestation);
        Assert.assertSame(contentArea.getHousedViewManifestation(),canvasManifestation);
        jp2.add(canvasManifestation2);
        Assert.assertFalse(contentArea.isAreaEmpty());
        jp2.remove(canvasManifestation2);
        Assert.assertTrue(contentArea.isAreaEmpty());
        
    }
    
    private void setOwnerCanvasManifestation(MCTContentArea contentArea, View canvasManifestation) {
        try {
            Method m = contentArea.getClass().getDeclaredMethod("setOwnerComponentCanvasManifestation", new Class[]{View.class});
            m.setAccessible(true);
            m.invoke(contentArea, new Object[]{canvasManifestation});
        } catch (SecurityException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
    
    @Test
    public void testSetOwnerComponentCanvasManifestation() {
        setOwnerCanvasManifestation(contentArea, canvasManifestation2);
        assertEquals(contentArea.getContentAreaPane(), canvasManifestation2);
        // verify that when switch the housing view, selection events are fired appropriately
        assertEquals(Collections.singleton(canvasManifestation), contentArea.getSelectedManifestations());
        PropertyChangeListener mockListener = Mockito.mock(PropertyChangeListener.class);
        contentArea.addSelectionChangeListener(mockListener);
        setOwnerCanvasManifestation(contentArea, canvasManifestation);
        assertEquals(Collections.singleton(canvasManifestation2), contentArea.getSelectedManifestations());
        Mockito.verify(mockListener).propertyChange(Mockito.any(PropertyChangeEvent.class));
    }

    @Test (dependsOnMethods = "testSetOwnerComponentCanvasManifestation")
    public void testSetControlVisible() {
        try {
            Field controlManifField = contentArea.getClass().getDeclaredField("controlManifestation");
            controlManifField.setAccessible(true);
            controlManifField.set(contentArea, new JPanel());

            Field splitPaneField = contentArea.getClass().getDeclaredField("splitPane");
            splitPaneField.setAccessible(true);
            splitPaneField.set(contentArea, new JSplitPane());

            JComponent controlManif = (JComponent) controlManifField.get(contentArea);
            contentArea.showControl(false);
            Assert.assertFalse(controlManif.isVisible());

            contentArea.showControl(true);
            Assert.assertTrue(controlManif.isVisible());
        } catch (SecurityException e) {
            Assert.fail(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            Assert.fail(e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("serial")
    public static class TestView extends View {
        public TestView(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
    }
}
