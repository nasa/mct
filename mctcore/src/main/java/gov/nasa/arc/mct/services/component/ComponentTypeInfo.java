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


/**
 * This class describes an component type. This description is used in the platform to create new component type
 * instances both through the UI as well as programmatically.   
 * @author chris.webster@nasa.gov
 *
 */
public class ComponentTypeInfo extends TypeInfo<AbstractComponent> {
   
    private final String displayName;
    private final String description;
    private final String componentTypeId;
    private final boolean isCreatable;
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type. 
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must provide the required no-argument constructor
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass) throws IllegalArgumentException {
        this(displayName,description,componentClass, componentClass.getName(), true);
    }
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type. 
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must have a no argument constructor
     * @param isCreatable indicates if this component type is creatable under the Create menu.
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     */
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, boolean isCreatable) throws IllegalArgumentException {
        this(displayName,description,componentClass, componentClass.getName(), isCreatable);
    }
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must have a no argument constructor
     * @param isCreatable indicates if this component type is creatable under the Create menu.
     * @param icon the icon that represents this component type
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     * @deprecated icon and wizard now specified using ComponentProvider.getAsset
     */
    @Deprecated
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, boolean isCreatable, javax.swing.ImageIcon icon) throws IllegalArgumentException {
        this(displayName,description,componentClass, componentClass.getName(), isCreatable);
    }

    
    /**
     * Creates new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class
     * @param wizard creates the panel to be displayed for the dialog box
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     * @deprecated icon and wizard now specified using ComponentProvider.getAsset
     */
    @Deprecated
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, CreateWizardUI wizard) throws IllegalArgumentException {
        this(displayName,description,componentClass,componentClass.getName(), true);
    }
    
    /**
     * Creates new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class
     * @param wizard creates the panel to be displayed for the dialog box
     * @param icon the icon that represents this component type
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     * @deprecated icon and wizard now specified using ComponentProvider.getAsset
     */
    @Deprecated
    public ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, CreateWizardUI wizard, javax.swing.ImageIcon icon) throws IllegalArgumentException {
        this(displayName,description,componentClass,componentClass.getName(), true);
    }    
  
    /**
     * Creates a new ComponentTypeInfo representing a unique component type.
     * @param displayName non null human readable name for the component type
     * @param description human readable string describing the component type
     * @param componentClass non null component class, this class must have a no argument constructor
     * @param id globally unique identifier (across all modules) identifying this component.
     * @param isCreatable indicates if this component type can be created from the Create menu.
     * @throws IllegalArgumentException if componentClass, id, or displayName is null or if component class does not meet the requirements of 
     * <code>AbstractComponent</code>
     */
    protected ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, String id, boolean isCreatable) throws IllegalArgumentException {
        super(componentClass);
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
        this.componentTypeId = id;
        this.isCreatable = isCreatable;    
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
     * @deprecated icon and wizard now specified using ComponentProvider.getAsset
     */
    @Deprecated
    protected ComponentTypeInfo(String displayName, String description, Class<? extends AbstractComponent> componentClass, String id, boolean isCreatable, CreateWizardUI wizard, javax.swing.ImageIcon icon) throws IllegalArgumentException {
        this(displayName, description, componentClass, id, isCreatable);
    }
    
    
    /**
     * Returns the component class.
     * @return component class of this component type
     */
    public final Class<? extends AbstractComponent> getComponentClass() {
        return getTypeClass();
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
     * @deprecated use getAsset(CreateWizardUI.class) instead
     */
    @Deprecated
    public final CreateWizardUI getWizardUI() {
        return getAsset(CreateWizardUI.class);
    }

    /**
     * Returns the image icon that represents this component type.
     * @return icon for component type
     * @deprecated use getAsset(ImageIcon.class) instead
     */
    @Deprecated
    public final javax.swing.ImageIcon getIcon() {
        return getAsset(javax.swing.ImageIcon.class);
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
