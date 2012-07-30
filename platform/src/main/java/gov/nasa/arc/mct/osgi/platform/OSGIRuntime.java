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
package gov.nasa.arc.mct.osgi.platform;

import java.util.Collection;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * A helper object that provides methods to interact with the
 * OSGi framework and other services that touch the external
 * environment. Can be overridden for unit testing.
 * 
 * @author mrose
 *
 */
public interface OSGIRuntime {
	/**
	 * Strategy to apply when the set of services changes. 
	 */
	public interface ServicesChanged {
		/**
		 * invoked when the set of services changes
		 * @param services the current set of available services, the string value is the
		 * symbolic name of the bundle where the collection of objects represents the set of services available 
		 * in that bundle
		 */
		void servicesChanged(Map<String, Collection<Object>> services);
		
		/**
		 * invoked when a service is added
		 * @param bundleId the symbolic name of the bundle supplying the service
		 * @param service the newly added service object
		 */
		void serviceAdded(String bundleId, Object service);
		
		/**
		 * Notifies that a service has been removed.
		 * 
		 * @param bundleId the symbolic name of the bundle supplying the service
		 * @param service the newly removed service object
		 */
		void serviceRemoved(String bundleId, Object service);
	}
	
	public void startExternalBundles();
	
	/**
	 * Return the bundle context for the system bundle.
	 * 
	 * @return the system bundle context
	 * @throws Exception if OSGi is not started or if there is another error
	 */
	public BundleContext getBundleContext();
	
	/**
	 * Stop the OSGi framework.
	 * 
	 * @throws Exception if there is an error stopping the framework.
	 */
	public void stopOSGI() throws BundleException, InterruptedException;
}
