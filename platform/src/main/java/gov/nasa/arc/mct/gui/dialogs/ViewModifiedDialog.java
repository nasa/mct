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
package gov.nasa.arc.mct.gui.dialogs;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.actions.SaveAction;
import gov.nasa.arc.mct.gui.housing.Inspector;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

/**
 * The dialog that appears when switching away from or closing a view that has 
 * unsaved changes, present Save/Discard/Cancel options.
 * 
 * Properly speaking, not a dialog, but a class responsible for launching this 
 * dialog upon request.
 */
public class ViewModifiedDialog  {
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    Inspector.class.getName().substring(0, 
                            Inspector.class.getName().lastIndexOf("."))+".Bundle");
    
    private View view;
    private ContextAwareAction action;

    /**
     * Create a new dialog launcher for unsaved 
     * modifications in the specified view.
     * @param view the view which may have unsaved changes
     */
    public ViewModifiedDialog(View view) {
        super();
        this.view = view;
        this.action = new DialogSaveAction();
    }
    
    /**
     * Prompt the user to commit or abort pending changes, 
     * if there are any. Note that this may not be possible 
     * (component may not be writeable, for instance). 
     * 
     * This only returns false when the action is explicitly 
     * aborted (so if there is no prompt because the user 
     * chooses Save, this still returns true.)
     * 
     * @return false if change was aborted
     */
    public boolean commitOrAbortPendingChanges() {
        return commitOrAbortPendingChanges(MessageFormat.format(
                BUNDLE.getString("view.modified.alert.text"), 
                view.getInfo().getViewName(), 
                view.getManifestedComponent().getDisplayName()));
    }
    
    /**
     * Prompt the user to commit or abort pending changes, 
     * if there are any. Note that this may not be possible 
     * (component may not be writeable, for instance). 
     * 
     * This only returns false when the action is explicitly 
     * aborted (so if there is no prompt because the user 
     * chooses Save, this still returns true.)
     * 
     * @param dialogMessage the message to display to the user
     * @return false if change was aborted
     */
    public boolean commitOrAbortPendingChanges(String dialogMessage) {
        AbstractComponent committedComponent = 
                PlatformAccess.getPlatform().getPersistenceProvider().getComponent(
                        view.getManifestedComponent().getComponentId());
        if (committedComponent == null)
            return true;
        
        ActionContextImpl context = new ActionContextImpl();
        context.setTargetComponent(view.getManifestedComponent());
        context.addTargetViewComponent(view);
        context.setTargetHousing((MCTHousing) SwingUtilities.getAncestorOfClass(MCTHousing.class, view));
        if (!action.canHandle(context) || !action.isEnabled()) {
            return true; // Can't save anyway, so don't bother
        }
        
        String save = action.getValue(ContextAwareAction.NAME).toString();        
        String discard = BUNDLE.getString("view.modified.alert.discard");
        String cancel = BUNDLE.getString("view.modified.alert.cancel");
        
        // Show options - Save, Discard, or Cancel
        String[] options = new String[]{ save, discard, cancel };
    
        Map<String, Object> hints = new HashMap<String, Object>();
        hints.put(WindowManagerImpl.MESSAGE_TYPE, OptionBox.WARNING_MESSAGE);
        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
        hints.put(WindowManagerImpl.PARENT_COMPONENT, view);

        String answer = PlatformAccess.getPlatform().getWindowManager().showInputDialog(
                BUNDLE.getString("view.modified.alert.title"), 
                dialogMessage, 
                options, 
                options[0], 
                hints);
        
        if (save.equals(answer)) {
            // Handle the Save, as defined by the SaveAction
            action.actionPerformed(new ActionEvent(view, 0, ""));
        }
        
        return answer != null && !answer.equals(cancel);
    }
    
    private class DialogSaveAction extends SaveAction {
        private static final long serialVersionUID = -7455151149235456035L;

        @Override
        protected AbstractComponent getTargetComponent(ActionContextImpl actionContext) {
            return view.getManifestedComponent();
        }        
    }
}
