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
 * IdGenerator.java Aug 18, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.db.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class generates IDs for the components.
 *
 */
public class IdGenerator {
    
    /** The NULL_VIEW_ID constant. */
    public final static int NULL_VIEW_ID = 0;

    private final static AtomicInteger viewRoleIdGenerator = new AtomicInteger(0);
    private final static AtomicInteger modelRoleIdGenerator = new AtomicInteger(0);
    
    private IdGenerator() {
        // prevent from instantiating this class.
    }

    /**
     * Gets a randomly generated Java UUID and replace all dashes with empty string. 
     * @return UUID - Randomly generated unique component id.
     */
    public static String nextComponentId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    /**
     * Gets the next incremental view role id.
     * @return integer - incremental view role id.
     */
    public static int nextViewRoleId() {
        return viewRoleIdGenerator.incrementAndGet();
    }

    /**
     * Gets the next incremental model role id.
     * @return integer - incremental model role id.
     */
    public static int nextModelRoleId() {
        return modelRoleIdGenerator.incrementAndGet();
    }
    
    /**
     * Resets the view role and model role id generators back to zero. 
     */
    public static void reset() {
        viewRoleIdGenerator.set(0);
        modelRoleIdGenerator.set(0);
    }
}
