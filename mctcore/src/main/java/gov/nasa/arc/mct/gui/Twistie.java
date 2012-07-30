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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * This class implements the twistie. This toggle button has only two states.
 * Caller needs to provide implementation for state change that responds to a
 * mouse click event.
 * 
 * An example of this toggle button is used to show/hide the control area in the 
 * directory area.
 *  
 * @author nija.shi@nasa.gov
 *
 */
@SuppressWarnings("serial")
public abstract class Twistie extends JLabel {
    
    private static final String COLLAPSE_ICON = "/images/controlToggleClosed.png";
    private static final String EXPAND_ICON = "/images/controlToggleOpen.png";
    
    private Icon onIcon;
    private Icon offIcon;
    private boolean state;
    
    /**
     * Without any arguments, the standard MCT expand and collapse icons are provided
     * to represent toggle open and closed, respectively.
     */
    protected Twistie() {
        this(new ImageIcon(Twistie.class.getResource(COLLAPSE_ICON)), 
             new ImageIcon(Twistie.class.getResource(EXPAND_ICON)));
        getAccessibleContext().setAccessibleName("Control Area Toggle");
        getAccessibleContext().setAccessibleDescription("This toggle opens and closes the control area.");
    }
   
    /**
     * Creates a new toggle button label with the given icons
     * for the "on" and "off" states.
     * @param offIcon icon representing an "off" state.
     * @param onIcon icon representing an "on" state.
     */
    protected Twistie(Icon offIcon, Icon onIcon) {
        super(offIcon);
        state = false;
        this.offIcon = offIcon;
        this.onIcon = onIcon;
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                doMouseClicked(e);
            }
        });
    }

    /**
     * This method only changes the state without invoking the overridden changeStateAction method.
     * @param state the assigned state
     */
    public final void changeState(boolean state) {
        this.state = state;
        setIcon((this.state) ? onIcon : offIcon);
    }
    
    /**
     * Responds to a state change of the toggle button. Subclasses
     * should implement this method to define an appropriate response.
     * 
     * @param state the assigned state
     */
    protected abstract void changeStateAction(boolean state);
    
    /**
     * Responds to a mouse click on the toggle button. The selected
     * state is toggled, and the icon is changed to reflect the
     * new state. The {@link #changeState(boolean)} method is called
     * to allow subclasses to modify their behavior or display.
     *  
     * @param e the mouse event
     */
    private final void doMouseClicked(MouseEvent e) {
        setIcon((state) ? offIcon : onIcon);
        state = !state;
        changeStateAction(state);        
    }

}
