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
package gov.nasa.arc.mct.canvas.panel;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.Color;
import java.awt.Container;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

/**
 * This class provides an icon view that supports the transferable protocol required for drag and drop. Instances of this
 * class are intended to replace the the use of the ICON view type as this implementation provides a way to transfer the actual view
 * being displayed instead of just the component instance. 
 *
 */
public class TransferableIcon extends JLabel {
    private static final long serialVersionUID = -7380332900682920418L;
    private static final Color ICON_COLOR = new Color(130,130,130);
    
    
    /**
     * This method creates a new Transferable Icon. 
     * @param referencedComponent component that is currently active in the view.
     * @param viewTransferCallback the callback to use during a drag and drop gesture to build the set of views available 
     * in the transfer. 
     */
    @SuppressWarnings("serial")
    public TransferableIcon(final AbstractComponent referencedComponent, final ViewTransferCallback viewTransferCallback) {
        super(MCTIcons.processIcon(referencedComponent.getAsset(ImageIcon.class), ICON_COLOR, false));
        final AtomicBoolean clicked = new AtomicBoolean(false);
        setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return canComponentBeContained()?COPY:NONE;
            }

            private boolean canComponentBeContained() {
                PolicyContext context = new PolicyContext();
                context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),Collections.singleton(referencedComponent));
                String policyCategoryKey = PolicyInfo.CategoryType.CAN_OBJECT_BE_CONTAINED_CATEGORY.getKey();
                Platform platform = PlatformAccess.getPlatform();
                PolicyManager policyManager = platform.getPolicyManager();
                ExecutionResult result = policyManager.execute(policyCategoryKey, context);
                return result.getStatus();
            }
            
            @Override
            protected Transferable createTransferable(JComponent c) {
                return new ViewRoleSelection(viewTransferCallback.getViewsToTransfer().toArray(new View[0]));
            }
            
            @Override
            protected void exportDone(JComponent source, Transferable data, int action) {
                super.exportDone(source, data, action);
                clicked.set(false);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (clicked.get()) {
                    JComponent c = (JComponent) e.getSource();
                    TransferHandler th = c.getTransferHandler();
                    th.exportAsDrag(c, e, TransferHandler.COPY);
                }
            }

        }); 
        addMouseListener(new MouseAdapter() {
            private void delegateMouseEvent(MouseEvent e) {
                Container container = getParent();
                while (container != null && !(container instanceof View)) {
                    container = container.getParent();
                }
                if (container != null) {
                    ((View) container).processMouseEvent(e);
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                clicked.set(true);
                delegateMouseEvent(e);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                delegateMouseEvent(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                clicked.set(false);
                delegateMouseEvent(e);
            }
        });
        
        setOpaque(false);
    }
    
    /**
     * This interface supports the ability to transfer the the currently showing views. This is expected to be dynamic
     * in most cases (the center pane for example) and this interface supports the ability to get the view to transfer.
     *
     */
    public interface ViewTransferCallback {
        
        /**
         * Returns the ordered set of views to provide during a drag and drop. 
         * @return the views to transfer. This method should not return null.
         */
        List<View> getViewsToTransfer();
    }
}
