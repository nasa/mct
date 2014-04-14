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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A CustomVisualControl may be used in cases where the set of specific GUI components 
 * in the enum {@link gov.nasa.arc.mct.components.PropertyDescriptor.VisualControlDescriptor }
 * are insufficient to provide the user interface desired in the Info View. 
 * Including a PropertyDescriptor which uses VisualControlDescriptor indicates that 
 * a CustomVisualControl should be identified and used instead.
 * 
 * A CustomVisualControl is responsible for notifying any change listeners when 
 * the user has completed editing. What constitutes the completion of editing may 
 * vary depending on specific user interface needs; typically, this includes 
 * pressing enter or changing focus.   
 * 
 * Instances of this object should be delivered via 
 * {@link gov.nasa.arc.mct.services.component.ComponentProvider#getAsset(gov.nasa.arc.mct.services.component.TypeInfo, Class) }
 * 
 * A new instance should be returned on each invocation, as 
 * this may be used in multiple views simultaneously.
 */
public abstract class CustomVisualControl extends JPanel {
    private static final long serialVersionUID = 8741519005964436112L;

    private List<ChangeListener> changeListeners = 
                    new ArrayList<ChangeListener>();
    
    /**
     * Add a change listener to the custom visual control. 
     * The control should fire change events whenever the 
     * user completes editing.
     * @param listener the listener to add
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }
    
    /**
     * Remove a change listener for the custom visual control.    
     * @param listener the listener to remove
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    /**
     * Notifies all change listeners attached to this object 
     * that a change has been completed.
     * Listeners for changes should use getEditedProperty 
     * to retrieve the new value from the change.
     */
    protected void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(event);
        }
    }
    
    /**
     * Update the contents of this visual control with the 
     * property of an object. This is as returned from 
     * {@link gov.nasa.arc.mct.components.PropertyEditor#getValue()}.
     * @param value the new value to display
     */
    public abstract void setValue(Object value);
    
    /**
     * Retrieve the value as edited within this control. 
     * This will be used as the argument for
     * {@link gov.nasa.arc.mct.components.PropertyEditor#setValue(Object)}.
     * @return the value which has resulted from editing
     */
    public abstract Object getValue();
    
    /**
     * Set whether or not the property being edited by this 
     * control is mutable.
     * If the control is not mutable, it should also not
     * be editable or focusable. 
     * @param mutable the new mutable state
     */
    public abstract void setMutable(boolean mutable);
}
