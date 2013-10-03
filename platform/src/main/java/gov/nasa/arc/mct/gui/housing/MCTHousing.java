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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.CompositeViewManifestationProvider;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;

import javax.swing.JFrame;

public interface MCTHousing extends CompositeViewManifestationProvider {
    public void setControlArea(MCTControlArea controlArea);

    public MCTControlArea getControlArea();

    public void setControlAreaVisible(boolean flag);

    public boolean isControlAreaVisible();

    public void setDirectoryArea(View directoryArea);

    public View getDirectoryArea();

    public void setContentArea(MCTContentArea contentArea);

    public MCTContentArea getContentArea();
    
    public void toggleControlAreas(boolean showing);
    
    public interface ControlProvider {
        public void showControl(boolean show);
        public boolean isControlShowing();
    }
    
    public void addControlArea(ControlProvider provider);

    public void setInspectionArea(View inspectionArea);

    public View getInspectionArea();

    /**
     * Provides one of the currently selected manifestations or the housing manifestation if 
     * nothing is selected.
     * @return a manifestation that should be acted upon
     */
    public View getCurrentManifestation();
    
    public JFrame getHostedFrame();
    
    public AbstractComponent getWindowComponent();

    public void setStatusArea(MCTStatusArea statusArea);

    public MCTStatusArea getStatusArea();
    
    public void reloadHousedContent();
    
    public View showHousedManifestationIfPresent(String manifestedType);
    
    public SelectionProvider getSelectionProvider();
    
}
