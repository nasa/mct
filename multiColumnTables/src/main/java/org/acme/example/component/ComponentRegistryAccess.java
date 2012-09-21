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

package org.acme.example.component;

import gov.nasa.arc.mct.services.component.ComponentRegistry;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The <code>ComponentRegistryAccess</code> class is used to inject an instance of the <code>ComponentRegistry</code> using declarative 
 * services. This OSGi component does not expose an interface (see OSGI-INF/component.xml) and thus will be usable from other bundles (
 * the class is not exported from this bundle). This class is thread safe as this may be access 
 * from multiple threads and the registry instance must be visible across all threads. 
 * @author chris.webster@nasa.gov
 *
 */
public class ComponentRegistryAccess {
	private static AtomicReference<ComponentRegistry> registry =
		new AtomicReference<ComponentRegistry>();

	// this is not a traditional singleton as this class is created by the OSGi declarative services mechanism. 
	
	/**
	 * Returns the component registry instance. This will not return null as the cardinality of 
	 * the component specified through the OSGi components services is 1. 
	 * @return a component registry service instance
	 */
	public static ComponentRegistry getComponentRegistry() {
		return registry.get();
	}
	
	/**
	 * set the active instance of the <code>ComponentRegistry</code>. This method is invoked by
	 * OSGi (see the OSGI-INF/component.xml file for additional details).
	 * @param componentRegistry available in MCT
	 */
	public void setRegistry(ComponentRegistry componentRegistry) {
		registry.set(componentRegistry);
	}
	
	/**
	 * release the active instance of the <code>ComponentRegistry</code>. This method is invoked by
	 * OSGi (see the OSGI-INF/component.xml file for additional details).
	 * @param componentRegistry to be released
	 */
	public void releaseRegistry(ComponentRegistry componentRegistry) {
		registry.set(null);
	}
}
