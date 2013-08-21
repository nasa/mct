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
package gov.nasa.arc.mct.platform.spi;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.util.Map;

/**
 * The <code>WindowManager</code> interface represents the window manager provided by
 * the platform. 
 * 
 * <em>This class is not intended to be used by component authors</em>
 * @author chris.webster@nasa.gov
 */
public interface WindowManager {
    
    /**
     * Opens the specified component in a new top level window. The window 
     * will have the appropriate areas based on the component instance. For 
     * example, a window whose component cannot have children would not show the
     * directory area.  
     * @param component to be shown, must not be null
     */
    public void openInNewWindow(AbstractComponent component);
    
    /**
     * Opens the specified component in a new top level window for multiple monitors support. The window 
     * will have the appropriate areas based on the component instance. For 
     * example, a window whose component cannot have children would not show the
     * directory area.  
     * @param component to be shown, must not be null
     * @param graphicsConfig
     */
    public void openInNewWindow(AbstractComponent component, GraphicsConfiguration graphicsConfig);
    
    /**
     * Returns the root component of the window containing the 
     * Swing <code>component</code>.
     * @param component the Swing component
     * @return the root component 
     */
    public AbstractComponent getWindowRootComponent(Component component);
    
    /**
     * Returns the housing manifestation of the window containing the
     * Swing <code>component</code>.
     * @param component the housing manifestation contained in the window
     * @return
     */
    public View getWindowRootManifestation(Component component);
    
    /**
     * Refresh the windows
     */
    public void refreshWindows();

    /**
     * Close the housings for component with the specified component id.
     * @param componentId the component id of which the housing should be closed.
     */
    public void closeWindows(String componentId);
    
    /**
     * Display a blocking input dialog (or equivalent) to the user. The user will be presented with 
     * the specified options (typically from a combo box or similar); the return 
     * value is the user's selection, or null if the user cancelled.
     * @param title the title to display for the dialog
     * @param message the message to display describing the question
     * @param options the set of options available to the user
     * @param defaultOption the option which should be selected by default
     * @param hints used for platform-specific hints; may be null (same as "no hints")
     * @return the option chosen by the user (null if cancelled)
     */
    public <T> T showInputDialog(String title, String message, T[] options, T defaultOption, Map<String, Object> hints);
    
}
