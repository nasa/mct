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
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The "Refresh Now" menu option. Causes the view in the 
 * center pane to be re-created with the latest version 
 * of the object in the housing.
 * 
 * @author vwoeltje
 *
 */
public class RefreshAction extends ContextAwareAction {
    private static final long serialVersionUID = -224000420281170561L;
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    ThisSaveAllAction.class.getName().substring(0, 
                            ThisSaveAllAction.class.getName().lastIndexOf("."))+".Bundle"); //NOI18N
    private static final Icon ICON = 
            new ImageIcon(RefreshAction.class.getResource("/icons/mct_icon_refresh.png"));
    
    
    private boolean isHousing;
    private MCTHousingViewManifestation housing;
    private View view;
    
    /**
     * Create the refresh action.
     */
    public RefreshAction() {
        super(BUNDLE.getString("RefreshAction.label")); //NOI18N
        this.putValue(Action.LARGE_ICON_KEY, ICON);
    }
    
    @Override
    public boolean canHandle(ActionContext context) {
        // Get the view container whose contents will be refreshed.
        this.view = context.getWindowManifestation();
        isHousing = view != null && view instanceof MCTHousingViewManifestation;
        
        if (isHousing) {
            // Store reference to housing for "actionPerformed"
            this.housing = (MCTHousingViewManifestation) context.getWindowManifestation();
            
            // Only valid if we have a center pane
            return housing != null && housing.getContentArea() != null;
        } else {
            // Otherwise, we're dealing with some view directly
            return view != null && view.getHousedViewManifestation() != null;
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
        // Ideally, we want something like:
        // return housing.getContentArea().getHousedViewManifestation().getManifestedComponent().isStale();
        // But staleness in this view is not detected for some reason
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        View housedView = isHousing ?
                housing.getContentArea().getHousedViewManifestation() :
                view.getHousedViewManifestation();
        
        // Should not be null per canHandle, but check for safety
        if (housedView != null) {
            boolean doRefresh = true;
            
            // Give the user an opportunity to cancel the refresh if it would
            // overwrite unsaved changes.
            if (housedView.getManifestedComponent().isDirty()) {
                Map<String, Object> hints = new HashMap<String, Object>();
                hints.put(WindowManagerImpl.MESSAGE_TYPE, OptionBox.WARNING_MESSAGE);
                hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
                hints.put(WindowManagerImpl.PARENT_COMPONENT, housing);

                String ok = BUNDLE.getString("RefreshAction.ok"); //NOI18N
                String cancel = BUNDLE.getString("RefreshAction.cancel"); //NOI18N
                
                String input = PlatformAccess.getPlatform().getWindowManager().showInputDialog(
                        BUNDLE.getString("RefreshAction.title"), //NOI18N
                        BUNDLE.getString("RefreshAction.warning"), //NOI18N 
                        new String[]{ok, cancel}, 
                        ok, 
                        hints);
                doRefresh = ok.equals(input);
            }
           
            // Perform the refresh by re-creating view
            if (doRefresh) {
                // Update component from persistence
                AbstractComponent comp = housedView.getManifestedComponent();
                comp = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(comp.getComponentId());
                housedView.setManifestedComponent(comp);                
                
                // Re-create view
                ViewInfo vi = housedView.getInfo();
                if (isHousing) {
                    View newView = vi.createView(comp);
                    housing.getContentArea().setOwnerComponentCanvasManifestation(newView);
                } else {
                    housedView.setManifestedComponent(comp);
                    view.setHousedViewManifestation(vi);
                }
            }
        }
    }

}
