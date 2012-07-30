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

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TestMCTAction {

    @Test
    public void testConstructorAndGetters() throws Exception {
        MyAction action = new MyAction("Do something");
        assertEquals(action.getValue(Action.NAME), "Do something");
        assertEquals(action.getValue(Action.ACTION_COMMAND_KEY), null);
        assertTrue(!action.canHandle(null));
        assertTrue(!action.isEnabled());
    }
    
    @SuppressWarnings("serial")
    protected static class MyAction extends ContextAwareAction {

        protected MyAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // do nothing
        }

        @Override
        public boolean canHandle(ActionContext context) {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
        
    }
}
