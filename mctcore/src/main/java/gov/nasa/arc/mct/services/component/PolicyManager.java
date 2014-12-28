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

import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;

/**
 * This interface provides access for policy management.
 * An instance can be obtained through OSGI, using code like:
 * <code>
 *    org.osgi.framework.BundleContext bc = ... // will be obtained through an activator
 *    ServiceReference ref = bc.getServiceReference(PolicyManager.class.getName());
 *    if (ref != null) 
 *      PolicyManager policyManager = (PolicyManager) bc.getService(ref);
 *    ...
 *    bc.ungetServiceReference(ref);
 * </code>
 * An instance of this class can also be obtained using the  
 * <a href="http://www.eclipsezone.com/eclipse/forums/t97690.html">OSGI declarative
 * service functionality</a>. 
 * 
 * @author nija.shi@nasa.gov
 */
public interface PolicyManager {
    
    /**
     * Executes all policies in the specified category and returns
     * the aggregate result. The result is true only if all policies
     * return true results, and false otherwise.
     * 
     * @param categoryKey the name of the category to execute
     * @param context the policy context to use when executing the policies
     * @return true, if the aggregate result of executing the policy category is true
     */
    public ExecutionResult execute(String categoryKey, PolicyContext context);
    
}
