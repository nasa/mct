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
package gov.nasa.arc.mct.components;



/**
 * Provides descriptions of component fields suitable for inspection.
 * java's PropertyDescriptor implements a similar API.
 */
public class PropertyDescriptor  {

    /** 
     * Defines the supported visual control types.
     */
    public enum VisualControlDescriptor {
        /**A label visual component such as a JLabel. Its value is immutable. */
        Label, 
        /**A text field component such as a JTextField, modified using getValueAsText and setValueAsText. Initialized using getValueAsText. */
        TextField, 
        /**A check box component such as a JCheckBox. It is initialized and modified using getValue and setValue of type Boolean. */
        CheckBox,
        /**A combo box component such as a JComboBox, initialized and modified using getValue and setValue. Its enumerated list is populated using getTags. */
        ComboBox,
        /**A text area component such as a JTextArea, initialized and modified using getValueAsText and setValueAsText. Used for long-form text. */
        TextArea;
    };

    private boolean isFieldMutable = false;
    private String shortDescription;
    private VisualControlDescriptor visualControlDescriptor;
    private PropertyEditor<?> propertyEditor;


    /**
     * Constructs a descriptor.  This is typically used for an mutable field.
     * @param shortDescription the label text
     * @param propertyEditor the property editor
     * @param visualControlDescriptor specification of the visual component that will be used to render the value
     */
    public PropertyDescriptor(String shortDescription, PropertyEditor<?> propertyEditor, VisualControlDescriptor visualControlDescriptor) {
        super();
        this.shortDescription = shortDescription;
        this.propertyEditor = propertyEditor;
        this.visualControlDescriptor = visualControlDescriptor;
    }

    /**
     * Get the property editor.
     * @return the property editor
     */
    public PropertyEditor<?> getPropertyEditor() {
        return propertyEditor;
    }

    /**
     * Determines mutability.
     * @return true if this field is editable
     */
    public boolean isFieldMutable() {
        return isFieldMutable;
    }

    /**
     * Specify mutability.
     * @param isFieldMutable whether this field is editable
     */
    public void setFieldMutable(boolean isFieldMutable) {
        this.isFieldMutable = isFieldMutable;
    }

    /**
     * Get a short description of the property.
     * @return description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Get a description of a visual control (such as a Swing component).
     * @return visual control description
     */
    public VisualControlDescriptor getVisualControlDescriptor() {
        return visualControlDescriptor;
    }
}
