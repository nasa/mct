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
/**
 * IdentityManagerFactory.java 2009
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.identitymgr.impl;

import gov.nasa.arc.mct.identitymgr.mcc.MCCIdentityManager;
import gov.nasa.arc.mct.util.exception.MCTException;
import gov.nasa.arc.mct.util.property.MCTProperties;

import java.io.IOException;

/**
 * Provides a factory to make ID managers.
 *
 */
public class IdentityManagerFactory {

	/**
	 * Returns an ID Manager based on the site.
	 * 
	 * @param refreshRunnable task to run after a shift change.
	 * @return an identity manager instance.
	 * @throws MCTException - MCT customized exception.
	 * @throws IOException - I/O exception.
	 */
    public static IdentityManager newIdentityManager(Runnable refreshRunnable) throws MCTException, IOException {
        return newIdentityManager("properties/identityManager.properties", refreshRunnable);
    }

    /**
     * 
     * Returns an ID Manager based on the site.
     * 
     * @param prop properties to use for this ID manager.
	 * @param refreshRunnable task to run after a shift change.
	 * @return an identity manager instance.
     * @throws MCTException - MCT customized exception.
     * @throws IOException - I/O exception.
     */
    public static IdentityManager newIdentityManager(String prop, Runnable refreshRunnable) throws MCTException,
            IOException {
        MCTProperties idMgrProp = new MCTProperties(prop);

        // switch on the site
        if (idMgrProp.getProperty("site", "").equalsIgnoreCase("mcc")) {
            return new MCCIdentityManager(idMgrProp, refreshRunnable);
        } else {
            throw new MCTException("invalid identity manager site");

        }
    }
}
