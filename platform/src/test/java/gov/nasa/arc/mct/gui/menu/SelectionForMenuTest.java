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
package gov.nasa.arc.mct.gui.menu;

import gov.nasa.arc.mct.component.MockComponent;
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.ActionManager;
import gov.nasa.arc.mct.platform.spi.MockPlatform;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JMenuBar;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class SelectionForMenuTest {
    
    private static final String MOCK_MENU = "MOCK_MENU";
    private static final String MOCK_ACTION = "MOCK_ACTION";
    
    private final PlatformAccess access = new PlatformAccess();
    private final Platform mockPlatform = new MockPlatform();

    private MockMenu mockMenu;
    
    @BeforeClass
    public void setup() {
        ActionManager.registerMenu(MockMenu.class, MOCK_MENU);
        ActionManager.registerAction(MockAction.class, MOCK_ACTION);

        mockMenu = (MockMenu) ActionManager.getMenu(MOCK_MENU, ActionContextImpl.NULL_CONTEXT);        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(mockMenu);
        MockComponent componentA = new MockComponent();
        componentA.setId("a");
        
        access.setPlatform(mockPlatform);
        MCTAbstractHousing housing = new MockHousing(new MCTHousingViewManifestation(componentA, new ViewInfo(MCTHousingViewManifestation.class,"", ViewType.LAYOUT)));
        access.releasePlatform();
        
        housing.setJMenuBar(menuBar);
        UserEnvironmentRegistry.registerHousing(housing);
    }
    
    @AfterClass
    public void tearDown() {
        
    }

    @Test
    public void test() {
        mockMenu.fireMenuSelected();
        mockMenu.fireMenuDeselected();
    }

    @SuppressWarnings("serial")
    public static class MockMenu extends ContextAwareMenu {

        public MockMenu() {
            super(MOCK_MENU);
        }
        
        @Override
        protected void fireMenuSelected() {
            super.fireMenuSelected();
        }
        
        @Override
        protected void fireMenuDeselected() {
            super.fireMenuDeselected();
        }
        
        @Override
        protected void populate() {
            addMenuItemInfos("mock/all.ext", Arrays.asList(
                new MenuItemInfo("MOCK_ACTION", MenuItemType.NORMAL)));
        }
        
        @Override
        public boolean canHandle(ActionContext context) {
            return true;
        }
    }
    
    @SuppressWarnings("serial")
    public static class MockAction extends ContextAwareAction {

        public MockAction() {
            super(MOCK_ACTION);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public boolean canHandle(ActionContext context) {
            Collection<View> selectedManifestations = context.getSelectedManifestations();
            Assert.assertEquals(selectedManifestations.size(), 2);
            return selectedManifestations.size() > 0;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
        
    }
    
    @SuppressWarnings("serial")
    private class MockHousing extends MCTStandardHousing {
        
        public MockHousing(View housingView) {
            super("Mock", 0, 0, 0, housingView);        
        }

        public MockHousing(int width, int height, int closeAction, byte areaSelection, View housingView) {
            super("Mock", width, height, closeAction, housingView);
        }
        
        @Override
        public SelectionProvider getSelectionProvider() {
            return new SelectionProvider() {
                
                @Override
                public void removeSelectionChangeListener(PropertyChangeListener listener) {
                }
                
                @Override
                public Collection<View> getSelectedManifestations() {
                    return Arrays.asList(Mockito.mock(View.class), Mockito.mock(View.class));
                }
                
                @Override
                public void clearCurrentSelections() {
                }
                
                @Override
                public void addSelectionChangeListener(PropertyChangeListener listener) {
                }
            };
        }
        
    }

}
