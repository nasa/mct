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
import gov.nasa.arc.mct.gui.GroupAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.dialogs.ViewModifiedDialog;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

/**
 * This class implements actions allowing a component to change housing in a window.
 * 
 * @author nshi
 *
 */
@SuppressWarnings("serial")
public class ChangeHousingViewAction extends GroupAction {
    private Map<AbstractComponent, List<? extends RadioAction>> componentActionsMap = new HashMap<AbstractComponent, List<? extends RadioAction>>();

    public ChangeHousingViewAction() {
        this("Change Housing View");
    }
    
    protected ChangeHousingViewAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public boolean canHandle(ActionContext context) {
        ActionContextImpl actionContext = (ActionContextImpl) context;
        MCTHousing targetHousing = actionContext.getTargetHousing();
        if (targetHousing == null)
            return false;
        
        AbstractComponent rootComponent = targetHousing.getWindowComponent();        
        if (rootComponent == null)
            return false;
        
        if (PlatformAccess.getPlatform().getRootComponent() == rootComponent)
            return false;
        
        List<? extends RadioAction> actions = componentActionsMap.get(rootComponent);
        if (actions == null) {
            List<ChangeSpecificViewAction> changeViewActions = new ArrayList<ChangeSpecificViewAction>();
            Set<ViewInfo> viewInfos = new LinkedHashSet<ViewInfo>();
            viewInfos.addAll(rootComponent.getViewInfos(ViewType.CENTER));
            viewInfos.addAll(rootComponent.getViewInfos(ViewType.OBJECT));
            
            for (ViewInfo viewInfo : viewInfos) {
                changeViewActions.add(new ChangeSpecificViewAction(viewInfo, actionContext));
            }
            setActions(changeViewActions.toArray(new RadioAction[changeViewActions.size()]));
            componentActionsMap.put(rootComponent, changeViewActions);
        }
        else {
            for (ChangeSpecificViewAction action : actions.toArray(new ChangeSpecificViewAction[actions.size()]))
                action.setContext(actionContext);
            setActions(actions.toArray(new RadioAction[actions.size()]));
        }

        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
    
    private class ChangeSpecificViewAction extends GroupAction.RadioAction {
        private static final String PLUS = " Plus";
        private ActionContextImpl context;
        private ViewInfo viewInfo;
        
        public ChangeSpecificViewAction(ViewInfo viewInfo, ActionContextImpl context) {
            this.context = context;
            this.viewInfo = viewInfo;
            putValue(Action.NAME, this.viewInfo.getViewName() + PLUS);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        private boolean commitOrAbortPendingChanges() {
            MCTHousingViewManifestation housingView = (MCTHousingViewManifestation) context.getWindowManifestation();
            View view = housingView.getContentArea().getHousedViewManifestation();
            return new ViewModifiedDialog(view).commitOrAbortPendingChanges();                
        }

        
        @Override
        public void actionPerformed(ActionEvent event) {
            MCTStandardHousing housing = (MCTStandardHousing) context.getTargetHousing();            
            AbstractComponent c = housing.getContentArea().getHousedViewManifestation().getManifestedComponent();
            if (!c.isStale() && c.isDirty()) {
                if (!commitOrAbortPendingChanges()) {
                    return; // Abandon event if user aborts.
                }
            }
            
            View currentCanvasViewManifestation = housing.getContentArea().getHousedViewManifestation();
            if (!viewInfo.equals(currentCanvasViewManifestation.getInfo())) {
                // get a component from the persistence provider to ensure the component always reflects the most recent updates
                // this also ensures that when changing a view any non saved changes will be lost
                AbstractComponent ac = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(currentCanvasViewManifestation.getManifestedComponent().getComponentId());
                View v = viewInfo.createView(ac);
                housing.getContentArea().setOwnerComponentCanvasManifestation(v);
                housing.setTitle(v.getManifestedComponent().getDisplayName() 
                        + " - " + viewInfo.getViewName() + PLUS);
                housing.getContentArea().getHousedViewManifestation().requestFocusInWindow();
            }
        }

        @Override
        public boolean isSelected() {
            putValue(Action.SELECTED_KEY, true);
            MCTStandardHousing housing = (MCTStandardHousing) context.getTargetHousing();
            View currentCanvasViewManifestation = housing.getContentArea().getHousedViewManifestation();
            return viewInfo.getType().equals(currentCanvasViewManifestation.getInfo().getType());
        }
        
        @Override
        public boolean isMixed() {
            return false;
        }
        
        public void setContext(ActionContextImpl context) {
            this.context = context;
        }
    }
}
