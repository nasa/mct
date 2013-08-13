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

import gov.nasa.arc.mct.identitymgr.IIdentityManager;
import gov.nasa.arc.mct.identitymgr.impl.IdentityManagerFactory;
import gov.nasa.arc.mct.util.exception.MCTException;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MCCIdentityManagerTest {

    MCCIdentityManager mccIDManagerSubsystem = null;

    final String TEST_USER = "issodin";
    final String TEST_GRP = "odinds";
    final String initialUser = "initialUser";
    final String initialGroup = "initialGroup";

    @BeforeClass
    void init() {
        IdTestUtils.setMctProperties();
    }

    @AfterClass
    void undo() {
        IdTestUtils.restoreMctProperties();
    }

    @BeforeMethod
    public void setUp() throws IOException, MCTException, InterruptedException {
        System.setProperty("mct.user", initialUser);
        System.setProperty("mct.group", initialGroup);
        mccIDManagerSubsystem = (MCCIdentityManager) IdentityManagerFactory.newIdentityManager(
                "properties/test.mcc.properties", null);
        Thread.sleep(300); // wait for accept() before tests start
    }

    @AfterMethod
    public void after() throws MCTException, IOException, InterruptedException {
        if (mccIDManagerSubsystem != null && mccIDManagerSubsystem.isMonitorRunning()) {
            mccIDManagerSubsystem.stopShiftChangeMonitor();
            Thread.sleep(500);
        }
    }

    @Test(enabled = true)
    public void getUserGroupNominal() throws MCTException {

        // initial value of the user is from MCCActivity
        Assert.assertEquals(mccIDManagerSubsystem.getCurrentUser(), initialUser);
        Assert.assertEquals(mccIDManagerSubsystem.getCurrentGroup(), initialGroup);

        // a shift change would set user
        mccIDManagerSubsystem.setCurrentUser(TEST_USER);
        Assert.assertEquals(mccIDManagerSubsystem.getCurrentUser(), TEST_USER);

        // get activity selection
        Assert.assertEquals(mccIDManagerSubsystem.getActivitySelection().getFlightID(), "29");

    }

    @Test(expectedExceptions = IOException.class)
    public void badPropertyFile() throws MCTException, IOException {
        IdentityManagerFactory.newIdentityManager("badPropertyFile", null);
    }

    @Test
    public void basicConstruction() throws MCTException, IOException {
        Assert.assertEquals(mccIDManagerSubsystem.getCurrentUser(), initialUser);
    }

    @Test
    public void badSite() throws MCTException, IOException {
        // We should only load an MCCIdentityManager if site is mcc
        IIdentityManager manager = IdentityManagerFactory.newIdentityManager("properties/invalid.mcc.properties", null);
        Assert.assertFalse(manager instanceof MCCIdentityManager);
    }

}
