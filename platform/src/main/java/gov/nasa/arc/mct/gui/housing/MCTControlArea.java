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
/**
 * MCTControlArea.java Aug 1, 2008
 *
 * This code is the property of the National Aeronautics and Space Administration and was
 * produced for the Mission Control Technologies (MCT) Project.
 *
 */

package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * This class defines the Control area of the standard MCT Housing.
 * 
 */
@SuppressWarnings("serial")
public class MCTControlArea extends JPanel {

    private MCTHousing parentHousing;
    
    public MCTControlArea(MCTHousing parentHousing) {
        this.parentHousing = parentHousing;
        this.parentHousing.setControlArea(this);

        final ActionContextImpl actionContext = new ActionContextImpl();
        actionContext.setTargetHousing(parentHousing);
        actionContext.setTargetComponent( ((MCTStandardHousing) parentHousing).getWindowComponent() );
        actionContext.addTargetViewComponent(parentHousing.getHousedViewManifestation());
        if (parentHousing.getDirectoryArea() != null) {
            actionContext.addTargetViewComponent(parentHousing.getDirectoryArea().getHousedViewManifestation());
        }
        if (parentHousing.getContentArea() != null) {
            actionContext.addTargetViewComponent(parentHousing.getContentArea().getHousedViewManifestation());
        }
        this.setLayout(new BorderLayout());
    }
    
    public void setParentHousing(MCTHousing parentHousing) {
        this.parentHousing = parentHousing;
        this.parentHousing.setControlArea(this);
    }
}
