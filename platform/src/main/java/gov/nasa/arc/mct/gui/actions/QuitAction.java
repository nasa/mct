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
package gov.nasa.arc.mct.gui.actions;

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntime;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntimeImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

/**
 * This action closes all open windows, thus quitting MCT
 */
@SuppressWarnings("serial")
public class QuitAction extends ContextAwareAction {
    private static final ResourceBundle SHUTDOWN_BUNDLE = 
            ResourceBundle.getBundle("ShutdownResource"); //NO18N
    private static String TEXT = SHUTDOWN_BUNDLE.getString("ACTION"); //NOI18N

    private Collection<MCTAbstractHousing> housings;
    
    public QuitAction() {
        super(TEXT);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Make sure the user really wants to do this
        // Pop up a confirmation dialog if this is the last window
        Object[] options = { SHUTDOWN_BUNDLE.getString("OK"), SHUTDOWN_BUNDLE.getString("CANCEL") }; //NOI18N

        Map<String, Object> hints = new HashMap<String, Object>();
        hints.put(WindowManagerImpl.MESSAGE_TYPE, OptionBox.WARNING_MESSAGE);
        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
        hints.put(WindowManagerImpl.PARENT_COMPONENT, (MCTAbstractHousing) UserEnvironmentRegistry.getActiveHousing());
        Object response = PlatformAccess.getPlatform().getWindowManager().showInputDialog(
                SHUTDOWN_BUNDLE.getString("TITLE"),   //NOI18N
                SHUTDOWN_BUNDLE.getString("MESSAGE"), //NOI18N
                options,
                options[0],
                hints);
        
        // If so, close all windows and stop OSGi
        if (response != null && response.equals(options[0])) { // "OK"            
            // Close every window
            for (MCTAbstractHousing housing : housings) {
                housing.closeHousing();
            }
            
            // User may have cancelled some window closings, so verify housing count
            if (UserEnvironmentRegistry.getHousingCount() == 0) {
                OSGIRuntime osgiRuntime = OSGIRuntimeImpl.getOSGIRuntime();
                try {
                    osgiRuntime.stopOSGI();
                } catch (Exception e1) {
                    LoggerFactory.getLogger(QuitAction.class).warn(e1.getMessage(), e1);
                }
            }
        }
    }

    @Override
    public boolean canHandle(ActionContext context) {
        housings = getAllHousings();
        if (housings == null || housings.isEmpty())
            return false;
        
        return true;
    }

    @Override
    public boolean isEnabled() {
        return housings != null && housings.size() > 0;
    }

    Collection<MCTAbstractHousing> getAllHousings() {
        return UserEnvironmentRegistry.getAllHousings();
    }
}