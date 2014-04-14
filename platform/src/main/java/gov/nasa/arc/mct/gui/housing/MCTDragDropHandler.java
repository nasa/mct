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
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCTDragDropHandler {
    public static final String CUSTOM_POLICY_KEY = "DRAG_DROP_ACTION_TYPE";
    public static final String MOVE_NAME = "Move";
    public static final String COPY_NAME = "Copy";
    public static final String LINK_NAME = "Copy Linked";
    public static final String LINK_VERB = "link";
    
    // Currently selected nodes, in their context
    // Keys: Containing views
    // Values: Selected views.
    private Collection<View> draggedViews;
    
    // The view onto which components were dropped
    private View dropView;
    
    // The index of the drop
    private int index;

    private String message = "";
    
    private List<AbstractComponent> droppedComponents = new ArrayList<AbstractComponent>();
    
    private final DragDropMode[] modes;
    
    /**
     * 
     * @param draggedViews the views that were dragged
     * @param dropView the view onto which the drop occurred
     * @param index index of the drop, or -1 if unspecified
     */
    public MCTDragDropHandler(Collection<View> draggedViews, View dropView, int index) {
        super();
        this.draggedViews = draggedViews;
        this.dropView = dropView;
        this.index = Math.min(index, dropView.getManifestedComponent().getComponents().size());
        
        for (View v : draggedViews) {
            AbstractComponent child = v.getManifestedComponent();
            if (child != null) {
                droppedComponents.add(child);
            }
        }     
        Collections.reverse(droppedComponents);
        
        modes = new DragDropMode[] {
                new DragDropMove(),
                new DragDropCopy(),
                new DragDropLink()
        };
    }
    
    /**
     * Perform the drag-drop action. This will present the user with a 
     * dialog of available actions, and complete the move per their 
     * selection.
     * @return true if completed; otherwise false
     */
    public boolean perform() {
        boolean complete = false;
        
        List<DragDropMode> options = new ArrayList<DragDropMode>();
        
        for (DragDropMode mode : modes) {
            if (mode.canPerform()) {
                options.add(mode);
            }
        }
        
        if (options.size() > 0) {
            
            Map<String, Object> hints = new HashMap<String, Object>();
            hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.DEFAULT_OPTION);

            DragDropMode choice = 
                    options.size() == 1 ? options.get(0) :
                    PlatformAccess.getPlatform().getWindowManager().showInputDialog(
                    "Add Manifestation - " + dropView.getManifestedComponent().getDisplayName(), 
                    createDialogMessage(options), 
                    options.toArray(new DragDropMode[options.size()]), 
                    options.get(0), 
                    hints);
            
            if (choice != null) {
                try {
                    PlatformAccess.getPlatform().getPersistenceProvider().startRelatedOperations();
                    choice.perform();    
                    complete = true; // Will not be set if there is an exception
                } finally {
                    PlatformAccess.getPlatform().getPersistenceProvider().completeRelatedOperations(complete);
                }
            } else {
                message = null;
            }
        }
        
        return complete;
    }
    
    public Collection<AbstractComponent> getDroppedComponents() {
        return droppedComponents;
    }
    
    public String getMessage() {
        return message;
    }
    
    private String createDialogMessage(List<DragDropMode> options) {
        String optionSummary = "";
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) {
                optionSummary += (i == options.size() - 1) ? " or " : ", ";
            }
            optionSummary += "<b>" + options.get(i).getVerb() + "</b>";
        }
        return "<html>There are multiple ways to complete this operation.<br>" +
               "Would you like to " + optionSummary + " these components?</html>";
       
    }
    
    private abstract class DragDropMode {
        public abstract String getName();
        public abstract boolean canPerform();
        public abstract void perform();
        
        @Override
        public String toString() {
            return getName();
        }
        
        public String getVerb() {
            return getName().toLowerCase();
        }
        
        protected boolean consultPolicy(PolicyInfo.CategoryType policyType) {
            return consultPolicy(policyType, makePolicyContext(droppedComponents, dropView.getManifestedComponent()));
        }
        
        protected boolean consultPolicy(PolicyInfo.CategoryType policyType, PolicyContext context) {
            ExecutionResult result = PlatformAccess.getPlatform().getPolicyManager().execute(
                    policyType.getKey(), 
                    context);
            
            if (!result.getStatus()) {            
                message = result.getMessage();
            }
            
            return result.getStatus();
        }
        
        protected PolicyContext makePolicyContext(List<AbstractComponent> sourceComponents, AbstractComponent targetComponent) {
            PolicyContext context = new PolicyContext();
            context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), targetComponent);
            context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(), sourceComponents);
            context.setProperty(PolicyContext.PropertyName.ACTION.getName(), Character.valueOf( 'w' ));
            context.setProperty(CUSTOM_POLICY_KEY, this.getName());
            return context;
        }
    }
    
    private class DragDropLink extends DragDropMode {
        @Override
        public String getName() {
            return LINK_NAME;
        }

        @Override
        public String getVerb() {
            return getName().equals(LINK_NAME) ? LINK_VERB : super.getVerb();
        }
        
        @Override
        public boolean canPerform() {
            return consultPolicy(PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY);
        }

        @Override
        public void perform() {
            dropView.getManifestedComponent().addDelegateComponents(index, droppedComponents);
            dropView.getManifestedComponent().save();
        }        
    }
    
    private class DragDropMove extends DragDropLink {
        private Map<String, AbstractComponent> parents =
                new HashMap<String, AbstractComponent>();
        private Map<String, List<AbstractComponent>> toRemove = 
                new HashMap<String, List<AbstractComponent>>();
        private boolean valid = true; 
        
        public DragDropMove() {
            for (View v : draggedViews) {
                View parent = v.getParentView();
                if (parent != null) {
                    AbstractComponent parentComponent = parent.getManifestedComponent();
                    String id = parentComponent.getId();
                    if (!id.equals(dropView.getManifestedComponent().getComponentId())) {
                        parents.put(id, parentComponent);
                        if (!toRemove.containsKey(id)) {
                            toRemove.put(id, new ArrayList<AbstractComponent>());
                        }
                        toRemove.get(id).add(v.getManifestedComponent());
                    }
                } else {
                    valid = false;
                    break;
                }
            }
        }
        
        @Override
        public String getName() {
            return MOVE_NAME;
        }

        @Override
        public boolean canPerform() {
            // Move involves a Remove Manifestation, so obey those rules
            for (String id : parents.keySet()) {
                PolicyContext context = makePolicyContext(toRemove.get(id), parents.get(id));
                if (!consultPolicy(PolicyInfo.CategoryType.CAN_REMOVE_MANIFESTATION_CATEGORY, context) || 
                    !consultPolicy(PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY, context)) {
                    return false;
                }
            }
            
            return valid && 
                   !toRemove.isEmpty() &&
                   super.canPerform();
        }

        @Override
        public void perform() {
            for (String id : parents.keySet()) {
                parents.get(id).removeDelegateComponents(toRemove.get(id));
                parents.get(id).save();
            }
            super.perform();
        }        
    }
    
    private class DragDropCopy extends DragDropLink {
        @Override
        public String getName() {
            return COPY_NAME;
        }

        @Override
        public boolean canPerform() {
            // Disallow copy if source and destination are same
            for (View view : draggedViews) {
                View parent = view.getParentView();
                if (parent != null && 
                    parent.getManifestedComponent().getComponentId().equals(dropView.getManifestedComponent().getComponentId())) {
                    return false;
                }
            }
            
            // Disallow copy if component has external key or is not creatable 
            for (AbstractComponent ac : droppedComponents) {
                if (ac.getExternalKey() != null) {
                    return false;
                }
                if (!PlatformAccess.getPlatform().getComponentRegistry().isCreatable(ac.getClass())) {
                    return false;
                }
            }
            
            return super.canPerform();
        }

        @Override
        public void perform() {
            droppedComponents = clone(droppedComponents);
            super.perform();
        }        
        
        private List<AbstractComponent> clone(List<AbstractComponent> droppedComponents) {
            List<AbstractComponent> clones = new ArrayList<AbstractComponent>();
            
            for (AbstractComponent component : droppedComponents) {
                clones.add(clone(component));
            }
            
            return clones;
        }
        
        private AbstractComponent clone(AbstractComponent component) {
            AbstractComponent duplicate = component.clone();
            ComponentInitializer ci = duplicate.getCapability(ComponentInitializer.class);
            ci.setCreator(PlatformAccess.getPlatform().getCurrentUser().getUserId());
            ci.setOwner(PlatformAccess.getPlatform().getCurrentUser().getUserId());
            duplicate.setDisplayName("copy of " + component.getDisplayName());
            duplicate.save();
            return duplicate;
        }
    }
}
