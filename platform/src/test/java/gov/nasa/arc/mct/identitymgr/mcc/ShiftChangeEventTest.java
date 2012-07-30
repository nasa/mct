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
 * IdentityManager
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */

package gov.nasa.arc.mct.identitymgr.mcc;

import gov.nasa.arc.mct.identitymgr.impl.IdentityManagerFactory;
import gov.nasa.arc.mct.util.exception.MCTException;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ShiftChangeEventTest {

    MCCIdentityManager mccIDManagerSubsystem = null;

    final static String TEST_USER = "issodin";
    final static String TEST_GRP = "odinds";
    static final String TEST_NEWUSER = "june";
    static final String TEST_NEWUSER2 = "june2";

    @BeforeClass
    public void setUp() throws IOException, MCTException {
        IdTestUtils.setMctProperties();
        mccIDManagerSubsystem = (MCCIdentityManager) IdentityManagerFactory.newIdentityManager(
                "properties/test.mcc.properties", null);
    }

    @AfterClass
    public void after() throws MCTException, IOException {
        IdTestUtils.restoreMctProperties();
        if (mccIDManagerSubsystem.isMonitorRunning()) {
            mccIDManagerSubsystem.stopShiftChangeMonitor();
        }
    }

    /*
     * simulate an event
     */
    @Test(enabled = true)
    public void simulateShiftChange() {
        ObserverOfNewuser observerOfNewUser = new ObserverOfNewuser();
        ObserverOfNewuser2 observerOfNewUser2 = new ObserverOfNewuser2();

        mccIDManagerSubsystem.addObserver(observerOfNewUser);
        mccIDManagerSubsystem.addObserver(observerOfNewUser2);

        mccIDManagerSubsystem.removeObserver(observerOfNewUser2);
        mccIDManagerSubsystem.removeObserver(observerOfNewUser);

        // no observers are registered, should not throw exception
        mccIDManagerSubsystem.getShiftChangeMonitor().simEvent(TEST_NEWUSER);

        mccIDManagerSubsystem.addObserver(observerOfNewUser);
        mccIDManagerSubsystem.addObserver(observerOfNewUser2);

        mccIDManagerSubsystem.getShiftChangeMonitor().simEvent(TEST_NEWUSER);
        Assert.assertEquals(mccIDManagerSubsystem.getCurrentUser(), TEST_NEWUSER);

    }

    class ObserverOfNewuser implements IShiftChangeObserver {

        @Override
        public void shiftChangeEvent(String userID) {
            Assert.assertEquals(userID, TEST_NEWUSER);
        }
    }

    class ObserverOfNewuser2 implements IShiftChangeObserver {

        @Override
        public void shiftChangeEvent(String userID) {
            Assert.assertEquals(userID, TEST_NEWUSER);
        }
    }
}
