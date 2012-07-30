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
import gov.nasa.arc.mct.gui.GroupAction;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;

/**
 * This action allows users to see all the currently opened MCT housing windows
 * and select one to bring to front.  
 * @author nija.shi@nasa.gov
 */
@SuppressWarnings("serial")
public class ListWindowsAction extends GroupAction {

    private Collection<MCTAbstractHousing> housings;
    
    public ListWindowsAction() {
        super("List Windows");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //NO OP
    }

    @Override
    public boolean canHandle(ActionContext context) {
        housings = getAllHousings();
        if (housings == null || housings.isEmpty())
            return false;
        
        Map<String, List<BringWindowToFrontAction>> map = new HashMap<String, List<BringWindowToFrontAction>>();
        for (MCTAbstractHousing housing : housings) {
            String title = housing.getTitle();
            List<BringWindowToFrontAction> list = map.get(title);
            if (list == null) {
                list = new ArrayList<BringWindowToFrontAction>();
                map.put(title, list);
            }
            list.add(new BringWindowToFrontAction(housing));
        }
        
        
        MCTAbstractHousing currentHousing = (MCTAbstractHousing) ((ActionContextImpl) context).getTargetHousing();
        List<BringWindowToFrontAction> actions = sort(map, currentHousing);
        setActions(actions.toArray(new BringWindowToFrontAction[actions.size()]));
        
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
    
    private List<BringWindowToFrontAction> sort(Map<String, List<BringWindowToFrontAction>> map, MCTAbstractHousing currentHousing) {
        List<BringWindowToFrontAction> list = new ArrayList<BringWindowToFrontAction>();
        String currentHousingTitle = currentHousing.getTitle();
        
        // First list current window and its kind 
        // (which means different housing windows of the same component)
        list.addAll(map.get(currentHousingTitle));
        
        // Then list the rest grouped by title
        for (Map.Entry<String, List<BringWindowToFrontAction>> entry : map.entrySet()) {
            if (!entry.getKey().equals(currentHousingTitle))
                list.addAll(entry.getValue());
        }
        return list;
    }
    
    private final class BringWindowToFrontAction extends GroupAction.RadioAction {
        private MCTAbstractHousing housing;
        
        public BringWindowToFrontAction(MCTAbstractHousing housing) {
            this.housing = housing;
            putValue(Action.NAME, housing.getTitle());
        }
        
        @Override
        public boolean isMixed() {
            return false;
        }
        
        @Override
        public boolean isSelected() {
            return housing.isFocused();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            housing.setState(JFrame.NORMAL);
            housing.toFront();
        }
        
    }

    Collection<MCTAbstractHousing> getAllHousings() {
        return UserEnvironmentRegistry.getAllHousings();
    }
    
}
