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
import gov.nasa.arc.mct.util.property.MCTProperties;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ActivitySelectionTest {
    final String initialUser = "initialUser";
    final String initialGroup = "initialGroup";
    MCCIdentityManager mcci = null;

    @BeforeClass
    public void setUp() throws MCTException, IOException {
        IdTestUtils.setMctProperties();
        System.setProperty("mct.user", initialUser);
        System.setProperty("mct.group", initialGroup);
        mcci = (MCCIdentityManager) IdentityManagerFactory.newIdentityManager("properties/test.mcc.properties", null);
    }

    @AfterClass
    public void after() throws MCTException, IOException {
        IdTestUtils.restoreMctProperties();
        if (mcci.isMonitorRunning()) {
            mcci.stopShiftChangeMonitor();
        }
    }

    @Test
    public void readActivity() throws IOException {

        MCCActivity actSel = new MCCActivity(new MCTProperties("properties/test.mcc.properties"));

        Assert.assertEquals(actSel.getFlightID(), "29");
        Assert.assertEquals(actSel.getSimType(), "TST");
        Assert.assertEquals(actSel.getVehicleID(), "unset");

        Assert.assertEquals(actSel.getActivityID(), "22");
        Assert.assertEquals(actSel.getActivityName(), "integrated Sim");
        Assert.assertEquals(actSel.getActivityType(), "SIM");

        Assert.assertEquals(actSel.getProgram(), "ISS");
        Assert.assertEquals(actSel.getSoftwareLevel(), "cert");
        Assert.assertEquals(actSel.getReconID(), "cycleA");

        actSel.setUserID("nextUserAfterShiftChange");

    }

    @Test
    public void testMultInstances() throws IOException {
        MCCActivity actSel1 = new MCCActivity(new MCTProperties("properties/test.mcc.properties"));
        Assert.assertEquals(actSel1.getUserID(), initialUser);
        MCCActivity actSel2 = new MCCActivity(new MCTProperties("properties/test.mcc.properties"));

        actSel2.setUserID("sally-isNextUserAfterShiftChange");
        Assert.assertEquals(actSel2.getUserID(), "sally-isNextUserAfterShiftChange");

    }

    @Test(expectedExceptions = IOException.class)
    public void testBadFile() throws IOException {
        new MCCActivity(new MCTProperties("xx/test.mccxxxxx.properxxties"));
    }
}
