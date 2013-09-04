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
package gov.nasa.arc.mct.services.component;



import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.Color;

import javax.swing.ImageIcon;

/**
 * This class describes an component type. This description is used in the platform to create new component type
 * instances both through the UI as well as programmatically.   
 * @author chris.webster@nasa.gov
 *
 */
public class ComponentTypeInfo {
    private static final int ICON_SIZE = 14;
    private static final Color BASE_ICON_COLOR = Color.WHITE;
    
    private final String displayName;
    private final String description;
    private final Class<? extends AbstractComponent> componentClass;
    private final String componentTypeId;
    private final boolean isCreatable;
    private final CreateWizardUI wizard;
    private final ImageIcon icon;
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type. 
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must provide the required no-argument constructor
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass) {
        this(displayName,description,componentClass, componentClass.getName(), true, null, null);
    }
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type. 
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must have a no argument constructor
     * @param isCreatable indicates if this component type is creatable under the Create menu.
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, boolean isCreatable) {
        this(displayName,description,componentClass, componentClass.getName(), isCreatable, null, null);
    }
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must have a no argument constructor
     * @param isCreatable indicates if this component type is creatable under the Create menu.
     * @param icon the icon that represents this component type
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, boolean isCreatable, ImageIcon icon) {
        this(displayName,description,componentClass, componentClass.getName(), isCreatable, null, icon);
    }

    
    /**
     * Creates new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class
     * @param wizard creates the panel to be displayed for the dialog box
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, CreateWizardUI wizard){
        this(displayName,description,componentClass,componentClass.getName(), true, wizard, null);
    }
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class
     * @param wizard creates the panel to be displayed for the dialog box
     * @param icon the icon that represents this component type
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, CreateWizardUI wizard, ImageIcon icon){
        this(displayName,description,componentClass,componentClass.getName(), true, wizard, icon);
    }    
    
    /**
     * Creates a new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must have a no argument constructor
     * @param id globally unique identifier (across all modules) identifying this component.
     * @param isCreatable indicates if this component type can be created from the Create menu.
     * @param wizard creates the panel to be displayed for the dialog box
     * @param icon the icon that represents this component type
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     */
    protected ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, String id, boolean isCreatable, CreateWizardUI wizard, ImageIcon icon) throws IllegalArgumentException {
        if (componentClass == null) {
            throw new IllegalArgumentException("componentClass must not be null");
        }
        AbstractComponent.checkBaseComponentRequirements(componentClass);
        
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (displayName == null) {
            throw new IllegalArgumentException("displayName must not be null");
        }
        this.displayName = displayName;
        this.description = description;
        this.componentClass = componentClass;
        this.componentTypeId = id;
        this.isCreatable = isCreatable;
        this.wizard = wizard;
        this.icon = MCTIcons.processIcon(
                        icon != null ? icon : 
                            MCTIcons.generateIcon(
                                            componentClass.getName().hashCode(),
                                            ICON_SIZE, BASE_ICON_COLOR));
    }
    
    
    /**
     * Returns the component class.
     * @return component class of this component type
     */
    public final Class<? extends AbstractComponent> getComponentClass() {
        return componentClass;
    }

    /**
     * Returns the human readable name of the component. This string will be
     * shown in the user interface
     * in labels and menus, so this should be short and internationalized.
     * @return a string representing the name of the component type (not instance)
     */
    public final String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the human readable description of the component.
     * This string will be shown in the user interface for a more descriptive
     * representation of a component (tool tips...) and should be internationalized.
     * @return string representing a description of the component type (not instance)
     */
    public final String getShortDescription() {
        return description;
    }
    
    /**
     * Returns a unique id for this component type. The id must be unique and stable for the component type across all
     * installations.
     * @return component type id 
     */
    public final String getId() {
        return componentTypeId;
    }
    
    /**
     * Returns if this component type can be created through the New menu.
     * @return if this component type is creatable.
     */
    public final boolean isCreatable() {
        return isCreatable;
    }
    
    /**
     * Returns wizard providing UI and action performed after creating. If null, a default wizard is used.
     * @return wizard for UI
     */
    public final CreateWizardUI getWizardUI() {
        return wizard;
    }

    /**
     * Returns the image icon that represents this component type.
     * @return icon for component type
     */
    public final ImageIcon getIcon() {
        return icon;
    }
    
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof ComponentTypeInfo &&
               ((ComponentTypeInfo)obj).getId().equals(getId());
    }
    
    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

}
