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

import gov.nasa.arc.mct.api.feed.FeedDataArchive;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.PlatformImpl;
import gov.nasa.arc.mct.platform.spi.SubscriptionManager;
import gov.nasa.arc.mct.services.component.FeedManager;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetFeedViewsAction extends ContextAwareAction {
    private static final long serialVersionUID = 217492157805448386L;
    
    private static final Logger logger = LoggerFactory.getLogger(ResetFeedViewsAction.class);
    
    private ActionContextImpl actionContext;
    private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle("ResetBufferMenu");
    
    private static String TEXT = BUNDLE.getString("ResetFeedMenu");
    
    public ResetFeedViewsAction() {
        super(TEXT);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        JFrame parentFrame = actionContext.getTargetHousing().getHostedFrame();
        int confirmationResult = JOptionPane.showConfirmDialog(parentFrame,
                String.format(BUNDLE.getString("ResetBufferConfirmDialogMsg")),
                BUNDLE.getString("ResetBufferConfirmDialogTitle"),
                JOptionPane.YES_NO_OPTION);
                
        if (confirmationResult == 0) {
            FeedDataArchive dataArchive =PlatformImpl.getInstance().getFeedDataArchive();            
            if (dataArchive != null) {
                dataArchive.reset();
            } else {
                logger.error("DataArchive is null.");
            }
             
            FeedManager feedManager = PlatformImpl.getInstance().getFeedManager();
            if (feedManager != null) {
        	    feedManager.clear();
            } else {
                logger.error("FeedManager is null.");
            }
             
            SubscriptionManager mgr = PlatformImpl.getInstance().getSubscriptionManager();
            if (mgr != null) {
                mgr.refresh();
            } else {
                logger.error("SubscriptionManager is null.");
            }
        } 
    }

    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        if (actionContext.getTargetHousing() == null) {
            return false;
        }
        return true;
    }
}
