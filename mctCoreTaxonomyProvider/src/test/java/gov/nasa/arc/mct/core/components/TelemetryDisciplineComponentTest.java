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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.components.collection.Group;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test to ensure a telemetry discipline component exhibits group save/restore 
 * behavior as expected.
 * 
 * Note that ownership rules which use groups are tested separately (ownership 
 * issues are a policy concern)
 * 
 * @author vwoeltje
 *
 */
public class TelemetryDisciplineComponentTest {

    private AbstractComponent testComponent;
    
    @BeforeMethod
    public void setup() {
        testComponent = new TelemetryDisciplineComponent();
    }
    
    /*
     * Verifies that model data is written and read back identically.
     * If the component's ModelStatePersistence falls out of sync with 
     * the component itself, then this should break.
     */
    @Test
    public void testPersistence() {
        ModelStatePersistence persistence = testComponent.getCapability(ModelStatePersistence.class);
        Assert.assertNotNull(persistence);
        
        persistence.setModelState("test");
        Assert.assertEquals(persistence.getModelState(), "test");
        Assert.assertEquals(testComponent.getCapability(ModelStatePersistence.class).getModelState(), "test");

        persistence.setModelState("retest");
        Assert.assertEquals(persistence.getModelState(), "retest");
        Assert.assertEquals(testComponent.getCapability(ModelStatePersistence.class).getModelState(), "retest");        
    }
    
    /*
     * Verifies that the component exhibits a Group capability, 
     * and furthermore that its group matches what is set using persistence.
     */
    @Test
    public void testGroup() {
        Group group = testComponent.getCapability(Group.class);
        Assert.assertNotNull(group);
        Assert.assertNotNull(group.getDiscipline());
        
        testComponent.getCapability(ModelStatePersistence.class).setModelState("test");
        Assert.assertEquals(group.getDiscipline(), "test");
        Assert.assertEquals(testComponent.getCapability(Group.class).getDiscipline(), "test");
        
        testComponent.getCapability(ModelStatePersistence.class).setModelState("retest");
        Assert.assertEquals(group.getDiscipline(), "retest");
        Assert.assertEquals(testComponent.getCapability(Group.class).getDiscipline(), "retest");        
    }
}
