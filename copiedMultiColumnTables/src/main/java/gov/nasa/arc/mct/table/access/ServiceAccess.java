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
package gov.nasa.arc.mct.table.access;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements an accessor class for an OSGi service. This class is designed
 * to be used as an OSGi declarative services component. Instances of this
 * component may have references to more than one service. The services are
 * looked up by class. However, each class of service is limited to a
 * cardinality of 1, because the services are stored in a map.
 */
public class ServiceAccess {
	
	private static final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
	
	/**
	 * Returns the service instance, cast to the desired type. This method must
	 * take an argument of the desired type, because of a limitation of Java
	 * generic types: static methods cannot return the exact type desired, because
	 * they must have the same signature when type erasure is applied. In the case
	 * of this method, the only class we can return is the parameterized type of
	 * the derived class. But we can't force the return type to be that type
	 * after type erasure.
	 * 
	 * <p>The first matching service is found. A service matches if the desired class
	 * is a superclass of a bound service. If more then one service might match, the
	 * actual service returned is implementation-specific. (Depends on the order of
	 * the hash key set.)
	 * 
	 * @param <T> the service type desired
	 * @param clazz the class to cast the service to
	 * @return the bound instance of the service, cast to the desired type,
	 *   or null if the service has not yet been bound
	 * @throws ClassCastException if the service is not of the desired type
	 */
	public final static <T> T getService(Class<T> clazz) {
		synchronized(services) {
			for (Class<?> actualClass : services.keySet()) {
				if (clazz.isAssignableFrom(actualClass)) {
					return clazz.cast(services.get(actualClass));
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Sets the active instance of the service.
	 * 
	 * @param serviceInstance the instance of the service
	 */
	public final void bind(Object serviceInstance) {
		synchronized (services) {
			services.put(serviceInstance.getClass(), serviceInstance);
		}
	}
	
	/**
	 * Releases the instance of the service.
	 * 
	 * @param serviceInstance the service instance to remove from the set of bindings
	 */
	public final void unbind(Object serviceInstance) {
		synchronized (services) {
			services.remove(serviceInstance.getClass());
		}
	}
	
}
