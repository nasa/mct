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

import gov.nasa.arc.mct.gui.MCTViewManifestationInfo;
import gov.nasa.arc.mct.gui.MCTViewManifestationInfoImpl;
import gov.nasa.arc.mct.util.LinkedHashSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This class contains the set of properties for a specific view. Each property is a (key, value) pair of type (String, String)
 * and each set of properties is associated with one component only.
 * 
 * Each set of properties will be shared across a cluster of MCT if the owner component is shared; therefore, each method of this
 * class will need to be synchronized to obtain the proper cluster-wide monitor.
 * 
 * The APIs of this class is only accessible from the package of the AbstractViewRole.
 * 
 */
@XmlRootElement(name="extendedInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtendedProperties implements Cloneable {
    @XmlJavaTypeAdapter(ExtendedPropertiesXmlAdapter.class)
    private HashMap<String, Set<Object>> viewRoleProperties = new LinkedHashMap<String, Set<Object>>();
   
    /**
     * Replace a property.
     * @param key the key of the property
     * @param value the value of the property
     */
    public synchronized void setProperty(String key, String value) {
        LinkedHashSet<Object> valueList = new LinkedHashSet<Object>();
        valueList.add(value);
        this.viewRoleProperties.put(key, valueList);
    }
    
    /**
     * Replace a property.
     * @param key the key of the property
     * @param value the value of the property
     */
    public synchronized void setProperty(String key, Object value) {
        assert value instanceof String || value instanceof MCTViewManifestationInfo : value.getClass().getName();
        
        LinkedHashSet<Object> valueList = new LinkedHashSet<Object>();
        valueList.add(value);
        this.viewRoleProperties.put(key, valueList);
    }
   
    /**
     * Replace all existing properties with the supplied properties.
     * @param viewInfo the supplied properties
     */
    public synchronized void setProperties(ExtendedProperties viewInfo) {
        this.viewRoleProperties.clear();
        this.viewRoleProperties.putAll(viewInfo.viewRoleProperties);
    }
    
    /**
     * Returns all properties.
     * @return map from a key to a set of properties that <code>Object</code>s
     */
    public Map<String, Set<Object>> getAllProperties() {
        return Collections.unmodifiableMap(viewRoleProperties);
    }
    
    /**
     * Add a property.
     * @param key the key of the property
     * @param value the value of the property
     */
    public synchronized void addProperty(String key, Object value) {
        assert value instanceof String || value instanceof MCTViewManifestationInfo;
        
        Set<Object> valueList = viewRoleProperties.get(key);
        if (valueList == null) {
            valueList = new LinkedHashSet<Object>();
            viewRoleProperties.put(key, valueList);
        }
        valueList.add(value);
    }

    /**
     * Get the value of a property.
     * @param <T> the type of the return type
     * @param key the key of the property
     * @param returnType the type of the returned property value
     * @return the value of the property
     */

    public synchronized<T> T getProperty(String key, Class<T> returnType) {
        Set<Object> valueList = viewRoleProperties.get(key);
        if (valueList == null || valueList.isEmpty()) { return null; }
        return returnType.cast(valueList.iterator().next());
    }
    
    /**
     * Gets the default extended property value. 
     * @param <T> data type of the return for this method
     * @param returnType the data type of the return
     * @return the return value
     */
    public synchronized<T> T getMostRecentProperty(Class<T> returnType) {
        Set<Object> valueList = null;
        Set<String> keySet = viewRoleProperties.keySet();
        Iterator<String> it = keySet.iterator();
        Object mostRecentKey = null;
        while (it.hasNext()) { 
            mostRecentKey = it.next();
        }
        valueList = viewRoleProperties.get(mostRecentKey);    
        return returnType.cast(valueList.iterator().next());
    }
    
    /**
     * Get the value of a property.
     * @param key the key of the property
     * @return the value of the property
     */
    public synchronized Set<Object> getProperty(String key) {
        return viewRoleProperties.get(key);
    }
    
    /**
     * Find out if there is any property defined.
     * @return true if there is at least one property defined; false otherwise.
     */
    public synchronized boolean hasProperty() {
        return !this.viewRoleProperties.isEmpty();
    }
    
    /**
     * Clone the properties and return the clone.
     * @return the cloned ViewRoleProperties object.
     */
    @Override
    public synchronized ExtendedProperties clone() {
        ExtendedProperties clonedProperties = new ExtendedProperties();
        for (Entry<String, Set<Object>> entry: this.viewRoleProperties.entrySet()) {
            LinkedHashSet<Object> clonedSet = new LinkedHashSet<Object>();
            for (Object o:entry.getValue()) {
                Object addedObject = o;
                if (o instanceof MCTViewManifestationInfoImpl) {
                    addedObject = MCTViewManifestationInfoImpl.class.cast(o).clone();
                } 
                clonedSet.add(addedObject);
            }
            clonedProperties.viewRoleProperties.put(entry.getKey(), clonedSet);
        }
        return clonedProperties;
    }
}
