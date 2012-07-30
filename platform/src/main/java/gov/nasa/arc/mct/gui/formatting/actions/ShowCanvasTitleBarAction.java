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
package gov.nasa.arc.mct.gui.formatting.actions;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;

import javax.swing.Action;

@SuppressWarnings("serial")
public class ShowCanvasTitleBarAction extends ContextAwareAction {

    ActionContextImpl actionContext;
    
    public ShowCanvasTitleBarAction() {
        super("Center Panel Title Bar");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isSelected = ((Boolean)getValue(Action.SELECTED_KEY)).booleanValue();
        if (isSelected) {
            actionContext.getTargetHousing().getContentArea().showCanvasTitle(true);
            putValue(Action.SELECTED_KEY, false);  
        } else {
            actionContext.getTargetHousing().getContentArea().showCanvasTitle(false);
            putValue(Action.SELECTED_KEY, true);  
        }    
    }

    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        
        AbstractComponent component = actionContext.getTargetComponent();        
        if (component == null)
            return false;

        MCTHousing targetHousing = actionContext.getTargetHousing();
        if (targetHousing == null || targetHousing.getContentArea() == null)
            return false;

        putValue(Action.SELECTED_KEY, targetHousing.getContentArea().isTitleBarShowing());
        return true;        
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
