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
package gov.nasa.arc.mct.gui.menu.housing;

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.Color;
import java.awt.Container;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

@SuppressWarnings("serial")
public class IconMenu extends ContextAwareMenu {
    private static final Color ICON_COLOR = new Color(40, 40, 40);
    
    public IconMenu() {
        super("");
    }

    @Override
    public boolean canHandle(final ActionContext context) {
        ImageIcon icon = context.getWindowManifestation().getManifestedComponent().getAsset(ImageIcon.class);
        setIcon(MCTIcons.processIcon(icon, ICON_COLOR, false));
        
        setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return canComponentBeContained()?COPY:NONE;
            }
            
            private boolean canComponentBeContained() {
                PolicyContext policyContext = new PolicyContext();
                policyContext.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),Collections.singleton(context.getWindowManifestation().getManifestedComponent()));
                String policyCategoryKey = PolicyInfo.CategoryType.CAN_OBJECT_BE_CONTAINED_CATEGORY.getKey();
                ExecutionResult result = PolicyManagerImpl.getInstance().execute(policyCategoryKey, policyContext);
                return result.getStatus();
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                View v = context.getWindowManifestation();
                Container container = v.getTopLevelAncestor();
                if (container instanceof MCTStandardHousing) {
                    MCTStandardHousing housing = (MCTStandardHousing) container;
                    MCTContentArea contentArea = housing.getContentArea();
                    if (contentArea != null) {
                        v = contentArea.getHousedViewManifestation();
                    }
                }
                return new ViewRoleSelection(new View[] { v });
            }
        });
        
        // Dragging of IconMenu is not permitted for the user environment. 
        // Achieve this by not adding the mouse drag listener. 
        if (context.getWindowManifestation().getManifestedComponent() != PlatformAccess.getPlatform().getRootComponent()) {
            addMouseMotionListener(new MouseMotionAdapter() {
    
                @Override
                public void mouseDragged(MouseEvent e) {
                    JComponent c = (JComponent) e.getSource();
                    TransferHandler th = c.getTransferHandler();
                   th.exportAsDrag(c, e, TransferHandler.COPY);
                }
    
            });
        }

        return true;
    }
    
    @Override
    protected void populate() {
        addMenuItemInfos("icon/open.ext", Collections.<MenuItemInfo>singleton(new MenuItemInfo("ICON_OPEN_ACTION", MenuItemType.NORMAL)));        
    }
}
