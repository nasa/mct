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
package gov.nasa.jsc.mct.executables.buttons;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.RoleService;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.User;
import gov.nasa.jsc.mct.executable.buttons.ExecutableButtonComponent;
import gov.nasa.jsc.mct.executable.buttons.ExecutableButtonComponentProvider;
import gov.nasa.jsc.mct.executable.buttons.view.ExecutableButtonManifestation;
import gov.nasa.jsc.mct.executables.buttons.actions.ExecutableButtonAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestExecutableButtonComponentProvider {
	private ExecutableButtonComponentProvider provider;
	
	@Mock RoleService roleService;
	@Mock User user;
	@Mock Platform mockPlatform;
	
	@BeforeMethod
	public void testSetup() {
		provider = new ExecutableButtonComponentProvider();
		MockitoAnnotations.initMocks(this);
		(new PlatformAccess()).setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getCurrentUser()).thenReturn(user);
	}
	
	@AfterMethod
	public void tearDown() {
	}
	
	@Test
	public void testComponentTypes() {
		Mockito.when(roleService.hasRole(Mockito.any(User.class), Mockito.anyString())).thenReturn(false);
		Assert.assertEquals(provider.getComponentTypes().iterator().next().getComponentClass(), ExecutableButtonComponent.class);
		Assert.assertTrue(provider.getComponentTypes().iterator().next().isCreatable());
		Assert.assertEquals(provider.getComponentTypes().size(), 1);
	}
	
	@Test
	public void testComponentCreation() {
		Mockito.when(roleService.hasRole(Mockito.any(User.class), Mockito.eq("GA"))).thenReturn(true);
		Mockito.when(roleService.hasRole(Mockito.any(User.class), Mockito.eq("DTR"))).thenReturn(true);
		Assert.assertEquals(provider.getComponentTypes().iterator().next().getComponentClass(), ExecutableButtonComponent.class);
		Assert.assertTrue(provider.getComponentTypes().iterator().next().isCreatable());
		Assert.assertEquals(provider.getComponentTypes().size(), 1);
	}
	
	@Test
	public void testViews() {
		Assert.assertEquals(provider.getViews(ExecutableButtonComponent.class.getName()).size(), 2);
		Assert.assertTrue(provider.getViews("abc").isEmpty());
	}
	
	@Test
	public void testMenuItemInfos() {
		Collection<MenuItemInfo> menuItems = provider.getMenuItemInfos();
		Assert.assertEquals(menuItems.size(), 2);
		Assert.assertEquals(menuItems.iterator().next().getActionClass(), ExecutableButtonAction.class);
	}
	
	@Test
	public void testPolicyInfos() {
		Assert.assertTrue(provider.getPolicyInfos().isEmpty());
	}
	
	@Test (dataProvider="assetTestCases")
	public void testAssets(ComponentProvider provider, TypeInfo<?> info, Class<?> assetType, boolean expected) {
		// Used to verify whether provider offers certain assets
		if (expected) {
			Assert.assertNotNull(provider.getAsset(info, assetType));
		} else {
			Assert.assertNull(provider.getAsset(info, assetType));
		}
	}
	
	@DataProvider
	public Object[][] assetTestCases() {
		List<Object[]> cases = new ArrayList<Object[]>();
		// Consider all view types
		for (ViewType type : ViewType.values()) {
			TypeInfo<?> view = new ViewInfo(ExecutableButtonManifestation.class, ExecutableButtonManifestation.VIEW_NAME, ExecutableButtonManifestation.class.getName(), type);
			TypeInfo<?> comp = new ComponentTypeInfo("","",ExecutableButtonComponent.class);
			for (TypeInfo<?> vi : new TypeInfo<?>[]{view, comp}) {
				// Executable buttons have no view or component icons currently
				cases.add(new Object[] { new ExecutableButtonComponentProvider(), vi, ImageIcon.class, false});
				// As above
				cases.add(new Object[] { new ExecutableButtonComponentProvider(), vi, Icon.class, false});
				// Executable buttons should have a wizard
				cases.add(new Object[] { new ExecutableButtonComponentProvider(), vi, CreateWizardUI.class, vi.getTypeClass().equals(ExecutableButtonComponent.class)});
			}
		}
		Object[][] returnValue = new Object[cases.size()][];
		for (int i = 0; i < cases.size(); i++) {
			returnValue[i] = cases.get(i);
		}
		return returnValue;
	}
}
