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

import java.util.Collection;

/**
 * This interface provides access for status area widget registry.
 * An instance can be obtained through OSGI, using code like:
 * <code>
 *    org.osgi.framework.BundleContext bc = ... // will be obtained through an activator
 *    ServiceReference ref = bc.getServiceReference(StatusAreaWidgetRegistry.class.getName());
 *    if (ref != null) 
 *      StatusAreaWidgetRegistry registry = (StatusAreaWidgetRegistry) bc.getService(ref);
 *    ...
 *    bc.ungetServiceReference(ref);
 * </code>
 * An instance of this class can also be obtained using the  
 * <a href="http://www.eclipsezone.com/eclipse/forums/t97690.html">OSGI declarative
 * service functionality</a>. 
 */
public interface StatusAreaWidgetRegistry {
    
    /**
     * Returns the status area widget information provided by <code>ComponentProvider</code>s. 
     * @return a collection of <code>StatusAreaWidgetInfo</code>
     */
    public Collection<StatusAreaWidgetInfo> getStatusAreaWidgetInfos();
}
