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
package gov.nasa.arc.mct.gui.util;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.db.util.IdGenerator;
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.actions.MockPolicy;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.menu.MCTStandardHousingMenuBar;
import gov.nasa.arc.mct.gui.menu.housing.ConveniencesMenu;
import gov.nasa.arc.mct.gui.menu.housing.EditMenu;
import gov.nasa.arc.mct.gui.menu.housing.HelpMenu;
import gov.nasa.arc.mct.gui.menu.housing.ObjectsMenu;
import gov.nasa.arc.mct.gui.menu.housing.ThisMenu;
import gov.nasa.arc.mct.gui.menu.housing.ViewMenu;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.User;

import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.WindowConstants;

public class TestSetupUtilities {

    /**
     * This setup code creates a Housing in UserEnvironmentRegistry so that
     * getActiveHousing() will return a non-null.
     */
    public static MCTHousing setUpActiveHousing() {
        class UtilComponent extends AbstractComponent {
            public UtilComponent(String id) {
                setId(id);
            }   
        }

        UtilComponent component = new UtilComponent(IdGenerator.nextComponentId());

        MCTHousingViewManifestation housingView = new MCTHousingViewManifestation(component, new ViewInfo(MCTHousingViewManifestation.class,"", ViewType.LAYOUT));
        MCTStandardHousing housing = new MCTStandardHousing(100,100,WindowConstants.EXIT_ON_CLOSE, housingView);

        UserEnvironmentRegistry.registerHousing(housing);
        housing.dispatchEvent(new WindowEvent(housing, WindowEvent.WINDOW_GAINED_FOCUS));
        return housing;
    }


    /**
     * Used in TestRemoveContentAreaManifestation.java.
     * 
     * This setup code enables use of ContentAreaStateDaoStrategy.java.
     * It was copied from mctLock project test directory,
     * gov.nasa.arc.mct.lock.MCTNonBlockingLockTest.java
     */
    public static void setUpForPersistence() {
        GlobalContext.getGlobalContext().switchUser(new UtilUser("CATO", "asi"), null);
        //HibernateUtil.initSessionFactory("/hibernate_test_derby.cfg.xml");
    }

    private static class UtilUser implements User {
        private final String disciplineId;
        private final String userId;
        
        public UtilUser(String disciplineId, String userId) {
            this.disciplineId = disciplineId;
            this.userId = userId;
        }
        
        @Override
        public String getDisciplineId() {
            return this.disciplineId;
        }
        
        @Override
        public String getUserId() {
            return this.userId;
        }

        @Override
        public User getValidUser(String userID) {
            if (userId.equals(userID)) {
                return this;
            }
            return null;
        }
        
    }

    private static final int ONE_CLICK = 1;
    private static final int TWO_CLICK = 2;
    private static final int X_LOC = 0;
    private static final int Y_LOC = 0;
    private static final int MODS = 0;
    private static final int WHEN = 0;

    public static MouseEvent eventPressed(JComponent source, int x, int y) {
        return new MouseEvent(source, MouseEvent.MOUSE_PRESSED, WHEN, MODS, x, y, ONE_CLICK, false, MouseEvent.BUTTON1);
    }

    public static MouseEvent eventOneClick(JComponent source) {
        return new MouseEvent(source, MouseEvent.MOUSE_CLICKED, WHEN, MODS, X_LOC, Y_LOC, ONE_CLICK, false, MouseEvent.BUTTON1);
    }

    public static MouseEvent eventTwoClick(JComponent source) {
        return new MouseEvent(source, MouseEvent.MOUSE_CLICKED, WHEN, MODS, X_LOC, Y_LOC, TWO_CLICK, false, MouseEvent.BUTTON1);
    }

    public static MouseEvent eventDragged(JComponent source) {
        return new MouseEvent(source, MouseEvent.MOUSE_DRAGGED, WHEN, MODS, X_LOC, Y_LOC, TWO_CLICK, false);
    }

    public static MouseEvent eventDragged(JComponent source, int x, int y) {
        return new MouseEvent(source, MouseEvent.MOUSE_DRAGGED, WHEN, MODS, x, y, TWO_CLICK, false);
    }

    public static void setupForMockPolicyManager() {
        PolicyManagerImpl.getInstance().refreshExtendedPolicies(Collections.singletonList(
                new ExtendedComponentProvider(new MockProvider(), "mock")));
    }
    
    public static void tearDownMockPolicyManager() {
        PolicyManagerImpl.getInstance().refreshExtendedPolicies(Collections.<ExtendedComponentProvider>emptyList());
    }

    public static void setupForMenuBar() {
        // Populate menu bar
        MCTStandardHousingMenuBar.registerMenu("THIS_MENU", new ThisMenu());
        MCTStandardHousingMenuBar.registerMenu("OBJECTS_MENU", new ObjectsMenu());
        MCTStandardHousingMenuBar.registerMenu("EDIT_MENU", new EditMenu());
        MCTStandardHousingMenuBar.registerMenu("VIEW_MENU", new ViewMenu());
        //MCTStandardHousingMenuBar.registerMenu("WINDOWS_MENU", new WindowsMenu());
        MCTStandardHousingMenuBar.registerMenu("CONVENIENCES_MENU", new ConveniencesMenu());
        MCTStandardHousingMenuBar.registerMenu("HELP_MENU", new HelpMenu());
    }

    private static class MockProvider extends AbstractComponentProvider {

        @Override
        public Collection<PolicyInfo> getPolicyInfos() {
            return Arrays.asList(new PolicyInfo(
                                        PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey(), 
                                        MockPolicy.class),
                                 new PolicyInfo(
                                        PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey(),
                                        MockPolicy.class));
        }

    }

}
