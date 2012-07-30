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
package gov.nasa.arc.mct.platform.core.access;

import gov.nasa.arc.mct.platform.spi.Platform;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The <code>PlatformAccess</code> class is used to inject an instance of <code>Platform</code> using declarative 
 * services. 
 * @author chris.webster@nasa.gov
 *
 */
public class PlatformAccess {
	private static final AtomicReference<Platform> platform =
		new AtomicReference<Platform>();

	/**
	 * Returns the platform instance. This will not return null as the cardinality of 
	 * the component specified through the OSGi components services is 1. 
	 * @return a platform service instance
	 */
	public static Platform getPlatform() {
		return platform.get();
	}
	
	/**
	 * set the active instance of the <code>Platform</code>. 
	 * @param platform available in MCT
	 */
	public void setPlatform(Platform aPlatform) {
		platform.set(aPlatform);
	}
	
	/**
	 * release the instance of <code>Platform</code>. This method is invoked by
	 * OSGi (see the OSGI-INF/component.xml file for additional details).
	 */
	public void releasePlatform() {
		platform.set(null);
	}
}
