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
package gov.nasa.arc.mct.gui.menu;

import gov.nasa.arc.mct.component.MockComponent;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.GroupAction.RadioAction;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.gui.MenuSection;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.ActionManager;
import gov.nasa.arc.mct.gui.impl.MenuExtensionManager;
import gov.nasa.arc.mct.gui.menu.actions.DoExtraAction;
import gov.nasa.arc.mct.gui.menu.actions.DoSubtask;
import gov.nasa.arc.mct.gui.menu.actions.DoThatAction;
import gov.nasa.arc.mct.gui.menu.actions.DoTheseAction;
import gov.nasa.arc.mct.gui.menu.actions.DoThisAction;
import gov.nasa.arc.mct.platform.spi.MockPlatform;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Unit test for dynamic menu.
 * 
 * @author nshi
 *
 */
public class DynamicMenuTest {

	private static final String MY_MODEL_B = "My Model B";
    private static final String MY_MODEL_A = "My Model A";
    private static final String TEST_BUNDLE = "Test Bundle";
    private static final String DO_EXTRA = "DO_EXTRA";
    private static final String DONT_DO_THIS_YET = "DONT_DO_THIS_YET";
    private static final String DO_SUBTASK = "DO_SUBTASK";
    private static final String TEST_SUBMENU = "TEST_SUBMENU";
    private static final String DO_THESE = "DO_THESE";
    private static final String DO_THAT = "DO_THAT";
    private static final String DO_THIS = "DO_THIS";
    private static final String TEST_MENU = "TEST_MENU";
    private AbstractComponent componentA;
	private AbstractComponent componentB;
	private MockHousing housingA;
    private MockHousing housingB;
	private TestMenu testMenu;
	List<ContextAwareMenu> popupMenus;
	
	private final PlatformAccess access = new PlatformAccess();
	private final Platform mockPlatform = new MockPlatform();
	
	@BeforeTest
	public void setup() {
	    if (GraphicsEnvironment.isHeadless()) {
	        return;
	    }
        ActionManager.registerMenu(TestMenu.class, TEST_MENU);
		ActionManager.registerAction(DoThisAction.class,  DO_THIS);
		ActionManager.registerAction(DoThatAction.class, DO_THAT);
		ActionManager.registerAction(DoTheseAction.class, DO_THESE);
		ActionManager.registerMenu(SubMenu.class, TEST_SUBMENU);
		ActionManager.registerAction(DoSubtask.class, DO_SUBTASK);
		MenuExtensionManager.getInstance().refreshExtendedMenus(
		        Collections.<ExtendedComponentProvider>singletonList(
		                new ExtendedComponentProvider(new TestProvider(), TEST_BUNDLE)));
		        
		popupMenus = new ArrayList<ContextAwareMenu>();
		
		access.setPlatform(mockPlatform);
		
	    componentA = new MockComponent();
	    componentB = new MockComponent();
	    
	    componentA.setDisplayName(MY_MODEL_A);
	    componentA.getCapability(ComponentInitializer.class).initialize();
	    componentB.setDisplayName(MY_MODEL_B);
	    componentB.getCapability(ComponentInitializer.class).initialize();

	    ViewInfo housingInfo = new ViewInfo(MCTHousingViewManifestation.class,"Housing",ViewType.LAYOUT);
		housingA = new MockHousing(new MCTHousingViewManifestation(componentA, housingInfo));
		housingB = new MockHousing(new MCTHousingViewManifestation(componentB, housingInfo));
		access.releasePlatform();
	}
	
	@Test(enabled=true)
	public void testRegisteredActions() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        
		testMenu = (TestMenu) ActionManager.getMenu(TEST_MENU, ActionContextImpl.NULL_CONTEXT);
		List<MenuSection> sections = testMenu.getMenuSections();
		
		Assert.assertNotNull(sections);
		Assert.assertEquals(sections.size(), 2);
		
	    MenuSection allSection = sections.get(0);
	    List<MenuItemInfo> allMenus = allSection.getMenuItemInfoList();
	    Assert.assertNotNull(allMenus);
	    Assert.assertEquals(allMenus.size(), 5);

	    MenuItemInfo info = allMenus.get(0);
	    Assert.assertEquals(info.getCommandKey(), DO_THIS);
	     
        info = allMenus.get(1);
        Assert.assertEquals(info.getCommandKey(), DO_THAT);
      
        info = allMenus.get(2);
        Assert.assertEquals(info.getCommandKey(), DO_THESE);

        info = allMenus.get(3);
        Assert.assertEquals(info.getCommandKey(), DONT_DO_THIS_YET);

        info = allMenus.get(4);
        Assert.assertEquals(info.getCommandKey(), TEST_SUBMENU);

        
	    MenuSection additionsSection = sections.get(1);
	    List<MenuItemInfo> addtionsMenus = additionsSection.getMenuItemInfoList();
	    
        info = addtionsMenus.get(0);
        Assert.assertEquals(info.getCommandKey(), DO_EXTRA);
	}
	
	@Test(enabled=true, dependsOnMethods = { "testRegisteredActions" })
	public void testMenuSelected() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        setTargetComponent(housingB);
		testMenu.fireMenuSelected();
		Assert.assertEquals(testMenu.getMenuComponentCount(), 6);
		
		JMenuItem menuItem = (JMenuItem) testMenu.getMenuComponent(0);
		Assert.assertEquals(menuItem.getActionCommand(), DO_THIS);
		Action action = menuItem.getAction();
		Assert.assertNotNull(action);
		Assert.assertTrue(action.isEnabled());
				
		menuItem = (JMenuItem) testMenu.getMenuComponent(5);
		action = menuItem.getAction();
        Assert.assertNotNull(action);
		Assert.assertEquals(menuItem.getActionCommand(), DO_EXTRA);
		Assert.assertFalse(action.isEnabled());

		testMenu.fireMenuDeselected();
		resetTargetComponent(housingB);
	}
	
	@Test(enabled=true, dependsOnMethods = { "testRegisteredActions" })
	public void testPopupMenu() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
		ActionContextImpl context = new ActionContextImpl();
		context.setTargetComponent(null);
		
		popupMenus.add(testMenu);
		
		JPopupMenu popupMenu = MenuFactory.createPopupMenu(context, popupMenus);
		Assert.assertNotNull(popupMenu);
		
		Assert.assertEquals(popupMenu.getComponentCount(),4);
		
		JMenuItem menuItem = (JMenuItem) popupMenu.getComponent(0);
        Action action = menuItem.getAction();
        Assert.assertNotNull(action);
		Assert.assertEquals(menuItem.getActionCommand(), DO_THIS);
        Assert.assertFalse(action.isEnabled());
		
        Assert.assertTrue(popupMenu.getComponent(2) instanceof JSeparator);
        
		menuItem = (JMenuItem) popupMenu.getComponent(3);
		Assert.assertEquals(menuItem.getActionCommand(), DO_EXTRA);
		action = menuItem.getAction();
		Assert.assertNotNull(action);
		Assert.assertFalse(action.isEnabled());
	}
	
	@Test(enabled=true, dependsOnMethods = { "testMenuSelected" })
	public void testSubMenu() {
        setTargetComponent(housingB);
        testMenu.fireMenuSelected();
        
        Assert.assertTrue(testMenu.getMenuComponent(3) instanceof ContextAwareMenu);
        SubMenu subMenu = (SubMenu) testMenu.getMenuComponent(3);

        subMenu.fireMenuSelected();
        Assert.assertEquals(subMenu.getMenuComponentCount(), 1);
        JMenuItem menuItem = (JMenuItem) subMenu.getMenuComponent(0);
        Action action = menuItem.getAction();
        Assert.assertNotNull(action);
        Assert.assertEquals(menuItem.getActionCommand(), DO_SUBTASK);
        Assert.assertFalse(action.isEnabled());
        subMenu.fireMenuDeselected();
        
        testMenu.fireMenuDeselected();
        resetTargetComponent(housingB);

	}
	
	@Test(enabled=true, dependsOnMethods = { "testPopupMenu" })
	public void testTargetContextPopup() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
		ActionContextImpl context = new ActionContextImpl();
		context.setTargetComponent(componentA);
		
		JPopupMenu popupMenu = MenuFactory.createPopupMenu(context, popupMenus);
		Assert.assertNotNull(popupMenu);
		
		Assert.assertEquals(popupMenu.getComponentCount(), 4);

		JMenuItem menuItem = (JMenuItem) popupMenu.getComponent(0);
		Assert.assertEquals(menuItem.getActionCommand(), DO_THIS);
        Action action = menuItem.getAction();
		Assert.assertNotNull(action);
		Assert.assertTrue(action.isEnabled());

        menuItem = (JMenuItem) popupMenu.getComponent(3);
        Assert.assertEquals(menuItem.getActionCommand(), DO_EXTRA);
        action = menuItem.getAction();
        Assert.assertNotNull(action);
        Assert.assertTrue(action.isEnabled());		
	}
	
	@Test(enabled=true, dependsOnMethods = { "testPopupMenu" })
	public void testTargetContextMenuSelected() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        setTargetComponent(housingB);
		
		testMenu.fireMenuSelected();
		Assert.assertEquals(testMenu.getMenuComponentCount(), 6);

		JMenuItem menuItem = (JMenuItem) testMenu.getMenuComponent(0);
	    Action action = menuItem.getAction();
	    Assert.assertNotNull(action);
	    Assert.assertEquals(action.getValue(Action.ACTION_COMMAND_KEY), DO_THIS);
	    Assert.assertTrue(action.isEnabled());

		Assert.assertTrue(testMenu.getMenuComponent(4) instanceof JSeparator);
	    
		menuItem = (JMenuItem) testMenu.getMenuComponent(5);
		action = menuItem.getAction();
		Assert.assertNotNull(action);
		Assert.assertEquals(action.getValue(Action.ACTION_COMMAND_KEY), DO_EXTRA);
		Assert.assertFalse(action.isEnabled());
		
		action.actionPerformed(new ActionEvent(testMenu, 0, null));
		
		testMenu.fireMenuDeselected();
		resetTargetComponent(housingB);
	}

	@Test(enabled=true, dependsOnMethods = { "testTargetContextMenuSelected" })
	public void testTargetContextFirstRadioMenuSelected() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        setTargetComponent(housingB);
		testMenu.fireMenuSelected();
		Assert.assertEquals(testMenu.getMenuComponentCount(), 6);

		JMenuItem menuItem = (JMenuItem) testMenu.getMenuComponent(1);
		RadioAction radioActionThis = (RadioAction) menuItem.getAction();
		Assert.assertNotNull(radioActionThis);
		Assert.assertTrue(radioActionThis.isEnabled());
		Assert.assertFalse(radioActionThis.isSelected());

		menuItem = (JMenuItem) testMenu.getMenuComponent(2);
		RadioAction radioActionThat = (RadioAction) menuItem.getAction();
		Assert.assertNotNull(radioActionThat);
		Assert.assertTrue(radioActionThat.isEnabled());
		Assert.assertFalse(radioActionThat.isSelected());

		radioActionThat.actionPerformed(new ActionEvent(testMenu, 0, null));
		testMenu.fireMenuDeselected();
		resetTargetComponent(housingB);
	}
	
	@Test(enabled=true, dependsOnMethods = { "testTargetContextFirstRadioMenuSelected" })
	public void testTargetContextSecondRadioMenuSelected() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        setTargetComponent(housingB);
		testMenu.fireMenuSelected();
		Assert.assertEquals(testMenu.getMenuComponentCount(), 6);

		JMenuItem menuItem = (JMenuItem) testMenu.getMenuComponent(2);
		RadioAction radioActionThat = (RadioAction) menuItem.getAction();
		Assert.assertNotNull(radioActionThat);
		Assert.assertTrue(radioActionThat.isEnabled());
		Assert.assertFalse(radioActionThat.isSelected());

		testMenu.fireMenuDeselected();
		resetTargetComponent(housingB);
	}

	@Test(enabled=true, dependsOnMethods = { "testRegisteredActions" })
	public void testCheckboxMenuSelected_1() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        setTargetComponent(housingA);
		testMenu.fireMenuSelected();
		Assert.assertEquals(testMenu.getMenuComponentCount(), 4);
		
		PlatformAccess access = new PlatformAccess();
		Platform mockPlatform = Mockito.mock(Platform.class);
		PersistenceProvider mockPersistenceProvider = Mockito.mock(PersistenceProvider.class);
		access.setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);

		JMenuItem menuItem = (JMenuItem) testMenu.getMenuComponent(3);
		JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) menuItem;
		Action action = menuItem.getAction();
		Assert.assertNotNull(action);
		checkBoxMenuItem.doClick();
		
		testMenu.fireMenuDeselected();
		resetTargetComponent(housingA);
		access.setPlatform(null);
	}
	
	@Test(enabled=true, dependsOnMethods = { "testCheckboxMenuSelected_1" })
	public void testCheckboxMenuSelected_2() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        setTargetComponent(housingA);
		
		testMenu.fireMenuSelected();
		Assert.assertEquals(testMenu.getMenuComponentCount(), 4);
		
		JMenuItem menuItem = (JMenuItem) testMenu.getMenuComponent(3);
		Assert.assertTrue(menuItem instanceof JCheckBoxMenuItem);
		Assert.assertTrue(((JCheckBoxMenuItem) menuItem).isSelected());

		testMenu.fireMenuDeselected();
		resetTargetComponent(housingA);
	}
	
	private void setTargetComponent(MCTAbstractHousing housing) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(testMenu);
        housing.setJMenuBar(menuBar);
        UserEnvironmentRegistry.registerHousing(housing);
	}
	
	private void resetTargetComponent(MCTAbstractHousing housing) {
	    housing.getJMenuBar().removeAll();
        UserEnvironmentRegistry.removeHousing(housing);
    }

	
    @SuppressWarnings("serial")
    private class MockHousing extends MCTStandardHousing {
        
        public MockHousing(View housingView) {
            super("Mock", 0, 0, 0, housingView);        
        }

        public MockHousing(int width, int height, int closeAction, byte areaSelection, View housingView) {
            super("Mock", width, height, closeAction, housingView);
        }
        
    }
    
    private class TestProvider extends AbstractComponentProvider {

        @Override
        public Collection<MenuItemInfo> getMenuItemInfos() {
            return Collections.<MenuItemInfo>singleton(new MenuItemInfo("test/additions", DO_EXTRA, MenuItemType.CHECKBOX, DoExtraAction.class));
        }

    }
}
