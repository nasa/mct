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

import gov.nasa.arc.mct.api.persistence.OptimisticLockException;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ObjectManager;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.internal.component.Updatable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * A Save action which includes any managed components of 
 * the component being saved (for instance, components modified 
 * in nested views.)
 */
public abstract class SaveAction extends ContextAwareAction{
    private static final long serialVersionUID = 3940626077815919451L;
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    SaveAction.class.getName().substring(0, 
                            SaveAction.class.getName().lastIndexOf("."))+".Bundle");
    private ActionContextImpl actionContext;
        

    public SaveAction() {
        super(BUNDLE.getString("SaveAllAction.label"));
    }
    
    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        return getTargetComponent(actionContext) != null;
    }

    private boolean isComponentWriteableByUser(AbstractComponent component) {
        Platform p = PlatformAccess.getPlatform();
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), component);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String inspectionKey = PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey();
        return p.getPolicyManager().execute(inspectionKey, policyContext).getStatus();
    }
    
    protected abstract AbstractComponent getTargetComponent(ActionContextImpl actionContext);
    
    @Override
    public boolean isEnabled() {
        AbstractComponent ac = getTargetComponent(actionContext);
        ObjectManager om = ac.getCapability(ObjectManager.class);
        Set<AbstractComponent> modified = om != null ?                
                om.getAllModifiedObjects() : 
                Collections.<AbstractComponent>emptySet();
        
        // Should enable if at least one object can be saved
        boolean hasWriteableComponents = 
                !ac.isStale() && 
                ac.isDirty() &&
                isComponentWriteableByUser(ac);
        if (!hasWriteableComponents) {
            for (AbstractComponent mod : modified) {
                if (isComponentWriteableByUser(mod)) {
                    hasWriteableComponents = true;
                    break;
                }
            }
        }
                
        return hasWriteableComponents;
    }

    /**
     * This method is invoked when the client side object is stale. This can occur when another client 
     * even another window in the same application instance has saved the component after it has been loaded. 
     * This implementation will try again, which will overwrite the previous change; however, this is where 
     * configuration could be added to display a message instead. 
     */
    private void handleStaleObject(AbstractComponent ac) {
        overwritePreviousChanges(ac);
    }
    
    private void overwritePreviousChanges(AbstractComponent ac) {
        AbstractComponent updatedComp = PlatformAccess.getPlatform().getPersistenceProvider().getComponentFromStore(ac.getComponentId());
        ac.getCapability(Updatable.class).setVersion(updatedComp.getVersion());
        actionPerformed(null);
    }
    
    /**
     * Prompt the user with a warning in the event that some objects cannot be saved.
     * @param canSave set of components which can be saved
     * @param cannotSave set of components which cannot be saved
     * @return true if save should be completed, otherwise false
     */
    private boolean handleWarnings(Collection<AbstractComponent> canSave, 
            Collection<AbstractComponent> cannotSave) {
        // No need to warn if all modified objects can be saved 
        if (cannotSave.isEmpty()) {
            return true;
        }
        
        // Get references to platform and window manager; these will be used a few times
        Platform platform = PlatformAccess.getPlatform();
        WindowManager windowManager = platform.getWindowManager();
        
        // Can only complete action if there are no components which cannot be removed
        String confirm = BUNDLE.getString("SaveConfirm");
        String abort = BUNDLE.getString("SaveAbort");
        String[] options = { confirm, abort };

        // Issue a warning dialog to the user
        Map<String, Object> hints = new HashMap<String, Object>();
        hints.put(WindowManagerImpl.PARENT_COMPONENT, actionContext.getWindowManifestation());
        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
        hints.put(WindowManagerImpl.MESSAGE_TYPE, OptionBox.WARNING_MESSAGE);
        hints.put(WindowManagerImpl.MESSAGE_OBJECT, buildWarningPanel(canSave, cannotSave));
        String choice = windowManager.showInputDialog(
                BUNDLE.getString("SaveWarningTitle"), //title
                "", // message - will be overridden by custom object 
                options, // options
                null, // default option
                hints); // hints
        
        // Complete the action, if the user has confirmed it
        return (confirm.equals(choice));
    }
    
    private JPanel buildWarningPanel(
            Collection<AbstractComponent> canSave, Collection<AbstractComponent> cannotSave) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildWarningPanel(
                BUNDLE.getString("CanSaveTitle"), 
                BUNDLE.getString("CanSaveText"), 
                canSave),
                BorderLayout.WEST);
        panel.add(buildWarningPanel(
                BUNDLE.getString("CannotSaveTitle"), 
                BUNDLE.getString("CannotSaveText"), 
                cannotSave),
                BorderLayout.EAST);
        return panel;
    }
    
    private JPanel buildWarningPanel(String title, String message, Collection<AbstractComponent> components) {
        List<String> compList = new ArrayList<String>(components.size());
        for (AbstractComponent comp : components) {
            compList.add(comp.getDisplayName());
        }
        JPanel warning = new JPanel(new FlowLayout());
        warning.setPreferredSize(new Dimension(400,300));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        JList compJList = new JList(compList.toArray());
        JScrollPane scrollPane = new JScrollPane(compJList);
        scrollPane.setPreferredSize(new Dimension(300,200));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setPreferredSize(new Dimension(300,20));
        titleLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        JTextArea warningMessage = new JTextArea(message);
        warningMessage.setWrapStyleWord(true);
        warningMessage.setLineWrap(true);
        warningMessage.setOpaque(false);
        warningMessage.setPreferredSize(new Dimension(300,60));
        warningMessage.setEditable(false);
        warning.add(warningMessage);
        warning.add(titleLabel);
        warning.add(scrollPane);
        return warning;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractComponent ac = getTargetComponent(actionContext);
        ObjectManager om = ac.getCapability(ObjectManager.class);
        Collection<AbstractComponent> modified = om != null ?
                om.getAllModifiedObjects() :
                Collections.<AbstractComponent>emptySet();

        // Assemble objects to save.
        // Make sure they pass the writeable-by-user test.
        Set<AbstractComponent> canSave = new HashSet<AbstractComponent>();
        Set<AbstractComponent> cannotSave = new HashSet<AbstractComponent>();
        for (AbstractComponent mod : modified) {
            (isComponentWriteableByUser(mod) ? canSave : cannotSave).add(mod);
        }
        if (ac.isDirty() && !ac.isStale()) {
            (isComponentWriteableByUser(ac) ? canSave : cannotSave).add(ac);
        }

        if (handleWarnings(canSave, cannotSave)) {
            try {
                PlatformAccess.getPlatform().getPersistenceProvider().persist(canSave);
            } catch (OptimisticLockException ole) {
                handleStaleObject(ac);
            }
            
            if (om != null) {
                om.notifySaved(canSave);
            }
        }
    }

    public static class ThisSaveAction extends SaveAction {
        private static final long serialVersionUID = -8750182309057992525L;

        @Override
        protected AbstractComponent getTargetComponent(ActionContextImpl actionContext)  {
            MCTHousing housing = actionContext.getTargetHousing();
            MCTContentArea contentArea = housing.getContentArea();
            return contentArea == null ? null : contentArea.getHousedViewManifestation().getManifestedComponent();
        }        
    }
    
    public static class ObjectsSaveAction extends SaveAction {
        private static final long serialVersionUID = -2536879130620462419L;

        @Override
        protected AbstractComponent getTargetComponent(ActionContextImpl actionContext)  {
            return actionContext.getInspectorComponent();
        }        
    }

}
