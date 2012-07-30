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

import gov.nasa.arc.mct.gui.ContextAwareAction;

import javax.swing.Action;

/**
 * Action for toggle commands.
 * 
 * @author nshi
 *
 */
@SuppressWarnings("serial")
public abstract class MCTFlipFlopAction extends ContextAwareAction {

    private Action onStateAction;
    private Action offStateAction;
    private boolean state;
    
    /**
     * Creates a new toggle action with the given name and on and off
     * actions.
     * 
     * @param onStateAction the action to perform when the toggle reaches the on state
     * @param offStateAction the action to perform when the toggle reaches the off state
     * @param name the name of the new action
     */
    protected MCTFlipFlopAction(Action onStateAction, Action offStateAction, String name) {
        super(name);
        this.onStateAction = onStateAction;
        this.offStateAction = offStateAction;
    }

    /**
     * Sets the state of the toggle.
     * 
     * @param state the new state, true if the toggle is in the on state 
     */
    protected void setState(boolean state) {
        this.state = state;
    }
    
    /**
     * Gets the appropriate action to perform. The on action is
     * returned if the toggle is in the on state, the off action otherwise.
     * 
     * @return the on or off action, depending on the toggle state
     */
    public Action getAction() {
        return state ? onStateAction : offStateAction;
    }
    
}
