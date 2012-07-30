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
package gov.nasa.arc.mct.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import gov.nasa.arc.mct.db.util.IdGenerator;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdGeneratorTest {
    @Mock
    private Platform mockPlatform;
    
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        (new PlatformAccess()).setPlatform(mockPlatform);
        IdGenerator.reset();
    }

    @AfterMethod
    public void tearDown() {
        IdGenerator.reset();
    }

    @Test
    public void testIds() {
        assertFalse(IdGenerator.nextComponentId().equals(IdGenerator.nextComponentId()));

        IdGenerator.reset();

        assertEquals(IdGenerator.nextViewRoleId(), 1);

        assertEquals(IdGenerator.nextModelRoleId(), 1);
    }

}
