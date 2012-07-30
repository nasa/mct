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

import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.identitymgr.impl.IdentityManager;
import gov.nasa.arc.mct.services.internal.component.User;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * Identity manager for MCC site.
 *
 */
public class MCCIdentityManager extends IdentityManager  implements IShiftChangeObservable{

    private static MCTLogger logger = MCTLogger.getLogger(MCCIdentityManager.class);

    private MCCActivity activitySelection;
    private ShiftChangeMonitor shiftChangeMonitor;
    private final Runnable refreshRunnable;

    /**
     * Creates an MCC site ID manager.
     * @param props input properties
     * @param refreshRunnable runnable to run after a shift change
     * @throws IOException
     */
    public MCCIdentityManager(Properties props, Runnable refreshRunnable) throws IOException {
        activitySelection = new MCCActivity(props);
        this.refreshRunnable = refreshRunnable;

        // initialize ID Manager's initial user/group from ActivitySelection
        this.setCurrentUser(activitySelection.getUserID());
        this.setCurrentGroup(activitySelection.getGroupID());

        shiftChangeMonitor = new ShiftChangeMonitor(this);
        shiftChangeMonitor.addObserver(new MCCSimpleIDObserver());
        shiftChangeMonitor.startMonitor();
    }

    @Override 
    public void addObserver(IShiftChangeObserver o) {
        shiftChangeMonitor.addObserver(o);
    }
    
	@Override
	public void removeObserver(IShiftChangeObserver o) {
		shiftChangeMonitor.removeObserver(o);	
	}

	/**
	 * Stops the shift change monitor.
	 */
    public void stopShiftChangeMonitor() {
        shiftChangeMonitor.shutdownThread();
    }

    /**
     * Gets the shift change monitor instance
     * @return shift change monitor instance
     */
    public ShiftChangeMonitor getShiftChangeMonitor() {
        return shiftChangeMonitor;
    }

    private class MCCSimpleIDObserver implements IShiftChangeObserver {

        @Override
        public void shiftChangeEvent(String userID) {
            User newUser = GlobalContext.getGlobalContext().getUser().getValidUser(userID);
            if (newUser == null) {
                return;
            } else {
                Collection<MCTAbstractHousing> c = UserEnvironmentRegistry.getAllHousings();
                for (MCTAbstractHousing housing :c) {
                    UserEnvironmentRegistry.removeHousing(housing);
                }
                currentUser = userID;
                GlobalContext.getGlobalContext().switchUser(newUser, refreshRunnable);

                logger.info("Shift change reported. Components were opened for user {0}", currentUser);
            }
        }
    }

    // these setters are for testing only
    void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    void setCurrentGroup(String currentGroup) {
        this.currentGroup = currentGroup;
    }

    /**
     * gets the activity provider
     * @return activity provider
     */
    public MCCActivity getActivitySelection() {
        return activitySelection;

    }

    /**
     * Returns true if the shift change monitor is running.
     */
    public boolean isMonitorRunning() {
        return this.shiftChangeMonitor.isMonitorRunning();
    }

}
