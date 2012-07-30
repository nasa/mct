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

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.ActionManager;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ActionManagerTest {

    @Mock
    private ActionContextImpl contextImpl;
   
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testMenus() {
        final String commandKey = "key";
        ActionManager.registerMenu(TestContextAwareMenu.class, commandKey);
        Assert.assertNotNull(ActionManager.getMenu(commandKey, contextImpl));
        Assert.assertNull(ActionManager.getMenu("noKey", contextImpl));
    }
    
    @Test
    public void testActions() {
        final String actionKey = "key";
        ActionManager.registerAction(TestContextAwareAction.class,actionKey);
        Assert.assertNotNull(ActionManager.getAction(actionKey, contextImpl));
        Assert.assertNull(ActionManager.getAction("junk", contextImpl));
        ActionManager.unregisterAction(TestContextAwareAction.class, actionKey);
        Assert.assertNull(ActionManager.getAction(actionKey, contextImpl));
        
        // add the action and deregister it
        ContextAwareAction action = null;
        ActionManager.registerAction(TestContextAwareAction.class,actionKey);
        Assert.assertNotNull(action = ActionManager.getAction(actionKey, contextImpl));
        ActionManager.deregisterAction(action);
    }
    
    public static class TestContextAwareAction extends ContextAwareAction {

        protected TestContextAwareAction() {
            super("abc");
            putValue(Action.ACTION_COMMAND_KEY, "key");
        }

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public boolean canHandle(ActionContext context) {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
        
    }
    
    public static class TestContextAwareMenu extends ContextAwareMenu {
        private static final long serialVersionUID = 1L;
        public boolean populated = false;
        
        public TestContextAwareMenu() {
            super("test");
        }
        
        @Override
        protected void populate() {
            populated = true;
        }
        
        @Override
        public boolean canHandle(ActionContext context) {
            return true;
        }
        
    }
}
