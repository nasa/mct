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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;
import java.util.Collection;


/**
 * This action opens user objects into their own windows. 
 *
 */
@SuppressWarnings("serial")
public class ObjectsOpenAction extends ContextAwareAction {

    private static String TEXT = "Open";
    
    private ActionContextImpl actionContext;

    public ObjectsOpenAction() {
        super(TEXT);
    }

    /**
     * Perform the action to open the selected components into their own windows (housings).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<View> viewManifestations = actionContext.getSelectedManifestations();

        for (View viewManif : viewManifestations) {
            AbstractComponent component = viewManif.getManifestedComponent();            
            component.open();
        }
    }

    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;        
        return true;
    }

    @Override
    public boolean isEnabled() {
        MCTHousing targetHousing = actionContext.getTargetHousing();
        if (targetHousing == null)
            return false;
        
        AbstractComponent targetComponent = actionContext.getTargetComponent();
        if (targetComponent == null)
            return false;
        
        if (targetComponent.equals(targetHousing.getWindowComponent()))
            return false;
        
        return true;
    }

}
