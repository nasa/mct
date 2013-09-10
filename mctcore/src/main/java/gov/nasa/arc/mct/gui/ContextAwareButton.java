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
package gov.nasa.arc.mct.gui;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * A button containing a context aware action. 
 * Sets its enabled/disabled state in response to context 
 * changes. Must be notified of these context changes explicitly. 
 * 
 * @author vwoeltje
 *
 */
public class ContextAwareButton extends JButton {    
    private static final long serialVersionUID = -2380576132488962171L;

    /**
     * Create a new button for the specified action.
     * @param action the action to perform when the button is clicked
     */
    public ContextAwareButton(ContextAwareAction action) {
        this(action, null);
    }
    
    /**
     * Create a new button for the specified action. 
     * Provide an initial context, used to determine 
     * visible/enabled states (and typically to also 
     * inform the actual behavior of the button.) 
     * 
     * @param action the action to perform when the button is clicked
     * @param context the context for this action
     */
    public ContextAwareButton(ContextAwareAction action, ActionContext context) {
        super(action);
        setContext(context);
    }

    @Override
    public void setText(String text) {
        // If text is not the action name, make a tool tip
        Object name = getAction().getValue(Action.NAME);
        if (name != null && !name.equals(text)) {
            this.setToolTipText(name.toString());
        }
        super.setText(text);
    }

    /**
     * Set the context for the current action. 
     * Button will enable/disable based on the action's 
     * state with regard to the current context.
     * 
     * This method should be called whenever the action's 
     * context may have meaningfully changed. Otherwise, 
     * the action performed by the button may be outdated.
     * 
     * @param context the context for this action
     */
    public void setContext(ActionContext context) {
        if (context != null) {
            Action a = getAction();
            if (a instanceof ContextAwareAction) {
                ContextAwareAction action = (ContextAwareAction) a;
                setVisible(action.canHandle(context));
                setEnabled(action.isEnabled());
            }
        } else {
            setVisible(false);
            setEnabled(false);
        }
    }    
}
