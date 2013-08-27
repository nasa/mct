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
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;

import java.awt.event.ActionEvent;

/**
 * This menu action is located under the This menu in the menu bar. It refreshes the
 * view content in the center pane.
 */
@SuppressWarnings("serial")
public class CenterPaneRevertToCommitted extends ContextAwareAction {
    public CenterPaneRevertToCommitted() {
        super(TEXT);
    }

    private static String TEXT = "Revert to Committed";    
    private ActionContext actionContext;
    private MCTHousingViewManifestation housingManifestation;
    
    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = context;
        housingManifestation = (MCTHousingViewManifestation) actionContext.getWindowManifestation();
        MCTContentArea contentArea = housingManifestation.getContentArea();
        if (contentArea == null)
            return false;
        return true;
    }

    @Override
    public boolean isEnabled() {
        View embeddedView = housingManifestation.getContentArea().getHousedViewManifestation();
        return embeddedView.getManifestedComponent().isStale();
            
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PersistenceProvider persistenceProvider = PlatformAccess.getPlatform().getPersistenceProvider();
        View embeddedView = housingManifestation.getContentArea().getHousedViewManifestation();
        AbstractComponent updatedComponent = persistenceProvider.getComponent(housingManifestation.getContentArea().getOwnerComponent().getComponentId());        
        housingManifestation.getContentArea().setOwnerComponentCanvasManifestation(embeddedView.getInfo().createView(updatedComponent));
        housingManifestation.setManifestedComponent(updatedComponent);
    }

}
