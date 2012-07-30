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
package gov.nasa.arc.mct.gui.monitors;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.CompositeAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.components.DetectGraphicsDevices;

import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class OpenMultipleMonitorsObjectsAction extends CompositeAction {  

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(OpenMultipleMonitorsObjectsAction.class);
    private ActionContextImpl actionContext;
    
    public OpenMultipleMonitorsObjectsAction() { 
        super(DetectGraphicsDevices.DEFAULT_MULTIPLE_MONITOR_TEXT);
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        //do nothing in main action class
    }
    
    private class SubMultipleMonitorsObjectsAction extends AbstractAction {
        @SuppressWarnings("unused")
        private ActionContextImpl actionContextImpl;
        private String graphicsDeviceName;
        
        public SubMultipleMonitorsObjectsAction(String graphicsDeviceName, ActionContextImpl actionContextImpl) {
            
            putValue(Action.NAME, graphicsDeviceName);
            
            // For MacOS and Windows-based platform it's "\DisplayN", N is a number
            if (DetectGraphicsDevices.isMac() || DetectGraphicsDevices.isWindows()) {
                graphicsDeviceName = graphicsDeviceName.replace(DetectGraphicsDevices.PROPER_DEVICE_NAME_PREFIX, "Display");
            }
            
            // For UNIX/Linux OS platform, need to revert back to original display name (e.g. ":0.0", ":0.1", etc.)
            if (DetectGraphicsDevices.isUnixLinux()) {
                graphicsDeviceName = graphicsDeviceName.replace(DetectGraphicsDevices.PROPER_DEVICE_NAME_PREFIX, "");
                graphicsDeviceName = ":0." + graphicsDeviceName;
            }
             
            this.graphicsDeviceName = graphicsDeviceName;
            this.actionContextImpl = actionContextImpl;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Collection<View> viewManifestations = actionContext.getSelectedManifestations();
            
            for (View viewManif : viewManifestations) {
                AbstractComponent component = viewManif.getManifestedComponent();
                GraphicsConfiguration graphicsConfig = DetectGraphicsDevices.getInstance().getSingleGraphicDeviceConfig(this.graphicsDeviceName);
                component.open(graphicsConfig);
            }

        }
    }

    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;

        String targetDeviceNameID = actionContext.getTargetHousing().getHostedFrame().getGraphicsConfiguration().getDevice().getIDstring();
        
        ArrayList<String> graphicsDeviceNames = DetectGraphicsDevices.getInstance().getGraphicDeviceNames();
        List<Action> subActions = new ArrayList<Action>(graphicsDeviceNames.size());
                
        for (String graphicsDeviceName : graphicsDeviceNames) {
            
            if (!graphicsDeviceName.equals(targetDeviceNameID)) {
            
                // For MacOS and Windows-based platform it's "\DisplayN", N is a number
                if (DetectGraphicsDevices.isMac() || DetectGraphicsDevices.isWindows()) {
                    graphicsDeviceName = graphicsDeviceName.replace("\\Display", DetectGraphicsDevices.PROPER_DEVICE_NAME_PREFIX);
                }
            
                // For UNIX/Linux OS platform specific display environment variables (:0.0, :0.1, :0.N, where N is a number)
                if (DetectGraphicsDevices.isUnixLinux() && graphicsDeviceName.contains(":0.")) {
                    graphicsDeviceName = DetectGraphicsDevices.PROPER_DEVICE_NAME_PREFIX + graphicsDeviceName.replace(":0.", "");
                }
            
                subActions.add(new SubMultipleMonitorsObjectsAction(graphicsDeviceName, actionContext));
            }
        }
        setActions(subActions.toArray(new Action[subActions.size()]));
        
        return ((actionContext.getSelectedManifestations().size() == 1) &&
                (DetectGraphicsDevices.getInstance().getNumberGraphicsDevices() > DetectGraphicsDevices.MINIMUM_MONITOR_CHECK ? true : false));
    }

    @Override
    public boolean isEnabled() {
        boolean checkTargetComponent =  false;
        boolean checkTargetHousing = false;
        
        MCTHousing targetHousing = actionContext.getTargetHousing();
        checkTargetHousing = (targetHousing == null) ? false : true;
        
        AbstractComponent targetComponent = actionContext.getTargetComponent();
        checkTargetComponent = (targetComponent == null) ? false : true; 
        
        if (targetComponent.equals(targetHousing.getWindowComponent()))
            checkTargetComponent = false;
         
        return (checkTargetComponent && checkTargetHousing &&
                (DetectGraphicsDevices.getInstance().getNumberGraphicsDevices() > DetectGraphicsDevices.MINIMUM_MONITOR_CHECK));
    }
 
}
