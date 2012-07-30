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

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.identitymgr.impl.IdentityManager;
import gov.nasa.arc.mct.identitymgr.impl.IdentityManagerFactory;
import gov.nasa.arc.mct.services.internal.component.User;
import gov.nasa.arc.mct.util.exception.MCTException;
import gov.nasa.arc.mct.util.exception.MCTRuntimeException;

import java.io.IOException;

import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdentityManagerTest {

    IdentityManager idManagerSubsystem = null;

    final String TEST_USER = "issodin";
    final String TEST_GRP = "odinds";
    final String initialUser = "initialUser";
    final String initialGroup = "initialGroup";

    public void setupSession() {
        MockitoAnnotations.initMocks(this);
        GlobalContext.getGlobalContext().switchUser(new MockUser(), null);
    }

    public void setupSessionWithInvalidUser() {
        MockitoAnnotations.initMocks(this);
        GlobalContext.getGlobalContext().switchUser(new InvalidMockUser(), null);
    }

    @BeforeMethod
    void init() {
        IdTestUtils.setMctProperties();
    }

    @AfterMethod
    void undo() {
        IdTestUtils.restoreMctProperties();
    }

    // test that user is initialized
    @Test
    public void getUserGroupNominal() throws MCTException, IOException {
        // initial value of the user is from MCCActivity
        System.setProperty("mct.user", initialUser);
        System.setProperty("mct.group", initialGroup);
        idManagerSubsystem = IdentityManagerFactory.newIdentityManager("properties/test.mcc.properties", null);

        Assert.assertEquals(idManagerSubsystem.getCurrentUser(), initialUser);
        Assert.assertEquals(idManagerSubsystem.getCurrentGroup(), initialGroup);
        if (idManagerSubsystem.isMonitorRunning()) {
            ((MCCIdentityManager) idManagerSubsystem).stopShiftChangeMonitor();
        }
        Assert.assertFalse(idManagerSubsystem.isMonitorRunning());
    }

    // Use MCCIdentityManager API
    @Test
    public void testValidUser() throws InterruptedException, MCTException, IOException {
        // start monitor via the id manager
        this.setupSession();
        MCCIdentityManager idm = (MCCIdentityManager) IdentityManagerFactory.newIdentityManager(null);

        // setup for using shift change reporter
        this.setupSession();
        ShiftChangeReporter shiftChangeReporter = new ShiftChangeReporter("Chad");

        // report valid user
        Assert.assertTrue(shiftChangeReporter.reportIfValueChanged(TEST_USER));
        Assert.assertEquals(shiftChangeReporter.getCurrentUser(), TEST_USER);

        // undo
        Thread.sleep(3000);
        idm.getShiftChangeMonitor().shutdownThread();
    }

    @Test
    public void testNullUser() throws InterruptedException, MCTException, IOException {
        this.setupSessionWithInvalidUser();
        MCCIdentityManager idm = (MCCIdentityManager) IdentityManagerFactory.newIdentityManager(null);

        idm.getShiftChangeMonitor().notifyShiftChangeObservers(null);
        // undo
        Thread.sleep(3000);
        idm.stopShiftChangeMonitor();
    }

    // Use ShiftChangeMonitor API directly
    @Test
    public void testServerSocket() throws InterruptedException, MCTException, IOException {

        // start a monitor
        ShiftChangeMonitor monitor = null;
        MCCIdentityManager idmgr = mock(MCCIdentityManager.class);
        monitor = new ShiftChangeMonitor(idmgr);
        Assert.assertFalse(monitor.isMonitorRunning());
        monitor.startMonitor();
        Thread.sleep(3000);
        Assert.assertTrue(monitor.isMonitorRunning());
        Assert.assertNotNull(monitor.getShutdownRunnable());

        // setup for using shift change reporter
        this.setupSession();
        ShiftChangeReporter shiftChangeReporter = new ShiftChangeReporter("Chad");
        shiftChangeReporter.sanityCheck();// we verify doesn't throw exception
        assertNotNull(shiftChangeReporter);

        // report an invalid user
        Assert.assertTrue(shiftChangeReporter.reportIfValueChanged("xx"));
        Assert.assertEquals(shiftChangeReporter.getCurrentUser(), "xx");
        Thread.sleep(3000);

        // shutdown monitor
        monitor.shutdownThread();
        Assert.assertFalse(monitor.isMonitorRunning());

        // test disabler
        System.setProperty("disableShiftChangeMonitor", "true");
        monitor = new ShiftChangeMonitor(idmgr);
        monitor.startMonitor();
        Assert.assertFalse(monitor.isMonitorRunning());
        System.setProperty("disableShiftChangeMonitor", "false");
    }

    // Since a shift change monitor is running, an attempt to start a second
    // will throw exception
    @Test(expectedExceptions = MCTRuntimeException.class)
    public void testMultipleMonitorsWithSamePort() throws Exception {

        // BSD semantics not implemented on Win O/S
        if (isWindows()) {
            throw new MCTRuntimeException("Testing of multiple Shift " + 
        "Change Monitors on Windows O/S is not supported");
        } else {
            MCCIdentityManager idmgr = mock(MCCIdentityManager.class);
            ShiftChangeMonitor monitor = new ShiftChangeMonitor(idmgr);
            Assert.assertFalse(monitor.isMonitorRunning());
            monitor.startMonitor();
            Thread.sleep(1000);
            Assert.assertTrue(monitor.isMonitorRunning());
    
            try {
                (new ShiftChangeMonitor(idmgr)).startMonitor();
            } catch (Exception e) {
                throw e;
            } finally {
                monitor.shutdownThread();
                Assert.assertFalse(monitor.isMonitorRunning());
            }
        }
    }
    
    // Windows OS platform
    protected static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    class MockUser implements User {

        @Override
        public String getDisciplineId() {
            return TEST_USER;
        }

        @Override
        public String getUserId() {
            return TEST_USER;
        }

        @Override
        public User getValidUser(String userID) {
            if (TEST_USER.equals(userID)) {
                return this;
            }
            return null;
        }
        
    }

    class InvalidMockUser implements User {

        @Override
        public String getDisciplineId() {
            return TEST_USER;
        }

        @Override
        public String getUserId() {
            return TEST_USER;
        }

        @Override
        public User getValidUser(String userID) {
            return null;
        }
        
    }

    class ObserverOfNewuser implements IShiftChangeObserver {

        @Override
        public void shiftChangeEvent(String userID) {
            ;
        }
    }
}
