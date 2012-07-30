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
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class ShowHideControlArea extends ContextAwareAction {
    
    private static String TEXT = "All Control Areas";
    private MCTHousing housing;
    
    public ShowHideControlArea() {
        super(TEXT);
        putValue(Action.NAME, TEXT);
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    }

    @Override
    public boolean canHandle(ActionContext context) {
        if (context == null)
            throw new IllegalStateException("ActionContext is null.");
        housing = ((ActionContextImpl) context).getTargetHousing();
        return housing != null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        boolean currentControlVisibilityState = housing.isControlAreaVisible();
        housing.setControlAreaVisible(!currentControlVisibilityState);
        housing.toggleControlAreas(!currentControlVisibilityState);
    }
    
}
