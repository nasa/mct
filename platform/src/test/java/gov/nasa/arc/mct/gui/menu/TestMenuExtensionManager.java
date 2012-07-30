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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.ActionManager;
import gov.nasa.arc.mct.gui.impl.MenuExtensionManager;
import gov.nasa.arc.mct.gui.menu.housing.ViewMenu;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestMenuExtensionManager {

    @Test
    public void test() {
        // Register the View menu
        ActionManager.registerMenu(ViewMenu.class, "VIEW_MENU");
        
        // Setup manifestation for the action context
        View manifestation = Mockito.mock(View.class);
        AbstractComponent component = Mockito.mock(AbstractComponent.class);
        MCTAbstractHousing housing = Mockito.mock(MCTAbstractHousing.class);
        
        Mockito.when(manifestation.getManifestedComponent()).thenReturn(component);
        Mockito.when(manifestation.getParent()).thenReturn(housing);

        ActionContextImpl context = new ActionContextImpl();
        context.setTargetComponent(component);
        context.setTargetHousing(housing);
        
        // Check menu and action registration
        Assert.assertTrue(ActionManager.getMenu("VIEW_MENU", context) instanceof ViewMenu);
        
        JPopupMenu viewPopupMenu = MenuExtensionManager.getInstance().getViewPopupMenu(manifestation);
        Assert.assertNotNull(viewPopupMenu);
        Assert.assertEquals(viewPopupMenu.getComponentCount(), 0);
                
        ActionManager.registerAction(MockSelectAll.class, "VIEW_SELECT_ALL");
        viewPopupMenu = MenuExtensionManager.getInstance().getViewPopupMenu(manifestation);
        Assert.assertNotNull(viewPopupMenu);
        Assert.assertEquals(viewPopupMenu.getComponentCount(), 1);
        Assert.assertTrue(viewPopupMenu.getComponent(0) instanceof JMenuItem);
        JMenuItem menuItem = (JMenuItem) viewPopupMenu.getComponent(0);
        Assert.assertTrue(menuItem.getAction() instanceof ContextAwareAction);
        
        // Cleanup
        ActionManager.unregisterMenu(ViewMenu.class, "VIEW_MENU");
        ActionManager.unregisterAction(MockSelectAll.class, "VIEW_SELECT_ALL");
    }
    
    @SuppressWarnings("serial")
    public static final class MockSelectAll extends ContextAwareAction {
        
        public MockSelectAll() {
            super("View Action");
        }

        @Override
        public void actionPerformed(ActionEvent e) {            
        }

        @Override
        public boolean canHandle(ActionContext context) {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
        
    }

}
