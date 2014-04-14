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
package gov.nasa.arc.mct.core.components;

import gov.nasa.arc.mct.components.Bootstrap;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MineTaxonomyComponentTest {

    @Test
    public void testBootstrap() {
        // Test Bootstrap capability by getting one from the sandbox
        MineTaxonomyComponent mySandbox = new MineTaxonomyComponent();
        Bootstrap capability = mySandbox.getCapability(Bootstrap.class);
        
        // Should support the bootstrap capability
        Assert.assertNotNull(capability);
        
        // Is a sandbox
        Assert.assertTrue(capability.isSandbox());
        
        // Is not globally accessible (one per user)
        Assert.assertFalse(capability.isGlobal());
        
        // Appears at the end of sorted list of bootstraps
        Assert.assertEquals(capability.categoryIndex(), Integer.MAX_VALUE);
        Assert.assertEquals(capability.componentIndex(), Integer.MAX_VALUE);        
    }
    
}
