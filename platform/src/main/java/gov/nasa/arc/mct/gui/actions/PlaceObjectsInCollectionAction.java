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
import gov.nasa.arc.mct.components.DetectGraphicsDevices;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.dialogs.PlaceObjectsInCollectionDialog;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

/**
 * This action allows users to select a group of components and create
 * a collection component to encapsulate the selected components.
 */
@SuppressWarnings("serial")
public class PlaceObjectsInCollectionAction extends ContextAwareAction {
    private Collection<View> selectedManifestations;
    private ActionContextImpl actionContext;
    private String graphicsDeviceName;
    
    public PlaceObjectsInCollectionAction() {
        super("Place Objects in New Collection...");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Set<AbstractComponent> sourceComponents = new LinkedHashSet<AbstractComponent>();
        for (View manifestation : selectedManifestations)
            sourceComponents.add(manifestation.getManifestedComponent());

        final String name = getNewCollection(sourceComponents);
        if (name.isEmpty())
            return;
        
        final AbstractComponent collection = createNewCollection(sourceComponents);
        if (collection == null) {
            showErrorInCreateCollection();
        } else {
            collection.setDisplayName(name);
            // invoke later so that transaction will have already completed
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    openNewCollection(name, collection);
                }
            });
        }
    }

    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        
        if (DetectGraphicsDevices.getInstance().getNumberGraphicsDevices() > DetectGraphicsDevices.MINIMUM_MONITOR_CHECK) {       
            graphicsDeviceName = actionContext.getTargetHousing().getHostedFrame().getGraphicsConfiguration().getDevice().getIDstring();
            graphicsDeviceName = graphicsDeviceName.replace("\\", "");
        }
        
       selectedManifestations = context.getSelectedManifestations();
       if (selectedManifestations.isEmpty()) {
           // No objects selected to add to a new collections
           return false;
       }
       
       // Guards against attempting to put a top level object into a collection as this will result in 
       // that collection becoming a child of itself. The prime example of this is the "All" entry in 
       // the tree. All necessary contains everything so adding All to All would result in All being a child
       for (View manifestation : selectedManifestations) {
           if (manifestation.getManifestedComponent() == PlatformAccess.getPlatform().getRootComponent()) {
               return false;
           }
       }
       
       return true; 
    }

    @Override
    public boolean isEnabled() {
        List<AbstractComponent> components = new ArrayList<AbstractComponent>(selectedManifestations.size());
        for (View view:selectedManifestations) {
            components.add(view.getManifestedComponent());
        }
        
        // disable use for objects which cannot be contained
        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),components);
        String policyCategoryKey = PolicyInfo.CategoryType.CAN_OBJECT_BE_CONTAINED_CATEGORY.getKey();
        ExecutionResult result = PolicyManagerImpl.getInstance().execute(policyCategoryKey, context);
        return result.getStatus();
    }
    
    /**
     * Generates the list of names of the selected components.
     * @param context
     * @return a array of names
     */
    private String[] getSelectedComponentNames(Collection<AbstractComponent> components) {
        List<String> names = new ArrayList<String>();
        for (AbstractComponent component : components) {
            names.add(component.getDisplayName());
        }
        return names.toArray(new String[names.size()]);        
    }
    
    String getNewCollection(Collection<AbstractComponent> sourceComponents) {
        Frame frame = null;
        for (Frame f: Frame.getFrames()) {
            if (f.isActive() || f.isFocused()) {
                frame = f;
            }
        }

        assert frame != null : "Active frame cannot be null.";
        
        PlaceObjectsInCollectionDialog dialog = 
            new PlaceObjectsInCollectionDialog(frame, getSelectedComponentNames(sourceComponents));            
        return dialog.getConfirmedTelemetryGroupName();        
    }
    
    AbstractComponent createNewCollection(Collection<AbstractComponent> sourceComponents) {
        return ExternalComponentRegistryImpl.getInstance().newCollection(sourceComponents);
    }
    
    void showErrorInCreateCollection() {
        OptionBox.showMessageDialog(selectedManifestations.iterator().next(), "Internal Error - Unable to place selected objects to a new collection.", "Error", OptionBox.ERROR_MESSAGE);    
    }
    
    void openNewCollection(String name, AbstractComponent collection) {
        if (DetectGraphicsDevices.getInstance().getNumberGraphicsDevices() > DetectGraphicsDevices.MINIMUM_MONITOR_CHECK) {
            GraphicsConfiguration graphicsConfig = DetectGraphicsDevices.getInstance().getSingleGraphicDeviceConfig(graphicsDeviceName);
            collection.open(graphicsConfig);
        } else {
            collection.open();
        }
    }

}
