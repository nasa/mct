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
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.impl.MCTFlipFlopAction;
import gov.nasa.arc.mct.gui.util.TestersAction;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestMCTFlipFlopAction {

    private MCTFlipFlopAction flipAction;
    private TestersAction onAction;
    private TestersAction offAction;

    @BeforeClass
    public void setup() {
        onAction = new TestersAction("Jump up");
        offAction = new TestersAction("Jump down");
        flipAction = new MyFlipFlopAction(onAction, offAction, "FiFo");
    }
    
    @Test
    public void testConstructor() {
        assertEquals(flipAction, flipAction);
    }

    @Test
    public void testSetState() {
        flipAction.setState(true);
        assertEquals(flipAction.getAction(), onAction);
        flipAction.setState(false);
        assertEquals(flipAction.getAction(), offAction);
    }

    @SuppressWarnings("serial")
    public class MyFlipFlopAction extends MCTFlipFlopAction {

        public MyFlipFlopAction(Action action1, Action action2, String name) {
            super(action1, action2, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
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
