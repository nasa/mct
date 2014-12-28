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
package gov.nasa.arc.mct.services.activity;

/**
 * This interface provides time related functionality for the current activity. Activity time 
 * is based on the time in the current mission which may be a simulation (in which case the time is different than the
 * wall clock time and clock ticks may deviate significantly) or be slightly delayed to reflect latency between
 * the vehicle and the ground systems. 
 * An instance can be obtained through OSGI, using code like:
 * <code>
 *    org.osgi.framework.BundleContext bc = ... // will be obtained through an activator
 *    ServiceReference ref = bc.getServiceReference(TimeService.class.getName());
 *    if (ref != null) 
 *      TimeService registry = (TimeService) bc.getService(ref);
 *    ...
 *    bc.ungetServiceReference(ref);
 * </code>
 * An instance of this class can also be obtained using the  
 * <a href="http://www.eclipsezone.com/eclipse/forums/t97690.html">OSGI declarative
 * service functionality</a>. 
 *
 */
public abstract class TimeService {
    
    /**
     * Time service ID that matches everything.
     */
     public static final String WILDCARD_SERVICE_ID = "*";
     
     /**
      * Time service default time format.
      */
     public static final String DEFAULT_TIME_FORMAT = "DDD'/'HH:mm:ss";
     
     /**
      * Time service default time system.
      */
     public static final String DEFAULT_TIME_SYSTEM = "GMT";

     /**
      * Returns the current time for the active activity. The time is based on the Unix Epoch (Java time) 
      * which are the milliseconds since midnight, January 1, 1970 UTC
      * @return time for the current activity
      */
     public abstract long getCurrentTime();

     /**
      * Returns time systems supported by this time service.
      * @return time systems
      */
     public String[] getTimeSystems() {
         return new String[]{DEFAULT_TIME_SYSTEM};
     }

     /**
      * Returns time formats supported by this time service.
      * @return time formats
      */
     public String[] getTimeFormats() {
         return new String[]{DEFAULT_TIME_FORMAT};
     }
     
     /**
      * Gets the time system id key for this component if it exists.
      * Time service implementations that support multiple time service types should override
      * this method to provide identification.
      * @return idKey Time System Id if it exists, null otherwise
      */
     public String getTimeSystemId() {
         return DEFAULT_TIME_SYSTEM;
     }
}
