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
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * This action allows users to close all windows except for 
 * the current active one up front.
 * @author nija.shi@nasa.gov
 */
@SuppressWarnings("serial")
public class WindowsExclusiveCloseAction extends ContextAwareAction {

    private static String TEXT = "Close All MCT Windows But This One";

    private Collection<MCTAbstractHousing> housings;
    private MCTAbstractHousing currentHousing;
    
    public WindowsExclusiveCloseAction() {
        super(TEXT);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        for (MCTAbstractHousing housing : housings) {
            if (!housing.equals(currentHousing)) {
                housing.closeHousing();
            }
        }
    }

    @Override
    public boolean canHandle(ActionContext context) {
        housings = getAllHousings();
        if (housings == null || housings.isEmpty())
            return false;
        
        return (currentHousing = (MCTAbstractHousing) ((ActionContextImpl) context).getTargetHousing()) != null;
    }

    @Override
    public boolean isEnabled() {
        return housings != null && housings.size() > 1;
    }

    Collection<MCTAbstractHousing> getAllHousings() {
        return UserEnvironmentRegistry.getAllHousings();
    }
}