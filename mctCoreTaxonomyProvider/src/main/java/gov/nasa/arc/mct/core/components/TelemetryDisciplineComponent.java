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
package gov.nasa.arc.mct.core.components;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.components.PropertyDescriptor;
import gov.nasa.arc.mct.components.PropertyDescriptor.VisualControlDescriptor;
import gov.nasa.arc.mct.components.PropertyEditor;
import gov.nasa.arc.mct.components.collection.Group;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
                
/**
 * TelemetryDisciplineComponent contain objects that are relevant to a whole group. 
 * These will be readable by all and writable by only group members (although this 
 * may be modified by policy)
 */
public final class TelemetryDisciplineComponent extends AbstractComponent implements Group, ModelStatePersistence {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("CoreTaxonomyResourceBundle"); //NOI18N
    private static final String groupName = bundle.getString("discipline_component_property"); //NOI18N
    
    private AtomicReference<String> group = new AtomicReference<String>("");     
        
    @Override
    protected <T> T handleGetCapability(Class<T> capability) {
        if (capability.isAssignableFrom(getClass())) {
            return capability.cast(this);
        }
        
        return null;
    }

    @Override
    public String getDiscipline() {
        return group.get();
    }

    @Override
    public String getModelState() {
        return group.get();
    }

    @Override
    public void setModelState(String state) {
        group.set(state);
    }

    @Override
    public List<PropertyDescriptor> getFieldDescriptors() {
        return Arrays.asList(
                new PropertyDescriptor(groupName, new PropertyEditor<String>() {
                    @Override
                    public String getAsText() {
                        return group.get();
                    }

                    @Override
                    public void setAsText(String text) throws IllegalArgumentException {
                        group.set(text);
                    }

                    @Override
                    public Object getValue() {
                        return group.get();
                    }

                    @Override
                    public void setValue(Object value) throws IllegalArgumentException {
                        group.set(value.toString());
                    }

                    @Override
                    public List<String> getTags() {
                        return Arrays.asList(group.get());
                    }
                }, VisualControlDescriptor.Label));
    }    
    
    
}
