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
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class MCTAbstractHousing extends JFrame implements MCTHousing {
    private GraphicsConfiguration gc;

    private static final Logger logger = LoggerFactory.getLogger(MCTAbstractHousing.class);
    
    public MCTAbstractHousing(String housedComponentId) {        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public MCTAbstractHousing(GraphicsConfiguration gc, String housedComponentId) {
        super(gc);
        this.gc = gc;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    @Override
    public View showHousedManifestationIfPresent(String manifestedType) {
        return null;
    }
    
    @Override
    public JFrame getHostedFrame() {
        return this;
    }
    
    @Override
    public void reloadHousedContent() {
        if (getWindowComponent() == PlatformAccess.getPlatform().getRootComponent()) {
            List<Window> initialOpenWindows = Arrays.asList(Window.getOwnerlessWindows());
            
            if (gc != null) {
                getWindowComponent().open(gc);
            } else {
                getWindowComponent().open();
            }
            List<Window> currentlyOpenWindows = new ArrayList<Window>(Arrays.asList(Window.getOwnerlessWindows()));
            currentlyOpenWindows.removeAll(initialOpenWindows);
            assert currentlyOpenWindows.size() == 1;
            Window newWindow = currentlyOpenWindows.get(0);
            newWindow.setSize(getSize());
            newWindow.setLocation(getX(), getY());
        } 
        UserEnvironmentRegistry.removeHousing(this);
        dispose();
    }
    
    public void refreshHousedContent() {
        
        if (getWindowComponent() != null) {
            logger.debug("View manifestations getWindowComponent().getDisplayName(): {}", getWindowComponent().getDisplayName());
            Set<View> viewManifestations = getWindowComponent().getAllViewManifestations();
            for (View view : viewManifestations) {
                if (view != null) {
                    view.updateMonitoredGUI();
                }
            }
        }
    }
    
    /**
     * Closes this housing by triggering the attached window listener.
     */
    public void closeHousing() {
        processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    @Override
    public boolean setHousedViewManifestation(ViewInfo viewInfo) {
        return false;
    }
    
}
