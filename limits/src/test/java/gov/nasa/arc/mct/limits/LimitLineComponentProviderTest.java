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
package gov.nasa.arc.mct.limits;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LimitLineComponentProviderTest {

	LimitLineComponentProvider provider = new LimitLineComponentProvider();
	@Mock Platform mockPlatform;
	@Mock User user;

	@BeforeMethod
	public void testSetup() {
		MockitoAnnotations.initMocks(this);
		(new PlatformAccess()).setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getCurrentUser()).thenReturn(user);
	}

	@Test
	public void testComponentTypes() {
		Assert.assertEquals(provider.getComponentTypes().iterator().next().getComponentClass(),LimitLineComponent.class);
		Assert.assertTrue(provider.getComponentTypes().iterator().next().isCreatable());
		Assert.assertEquals(provider.getComponentTypes().size(), 1);
	}

	@Test
	public void getMenuItemInfos() {
		Collection<MenuItemInfo> menuItems = provider.getMenuItemInfos();
		Assert.assertEquals(menuItems.size(),0);
	}

	@Test
	public void testViewInfos() {
		Assert.assertEquals(provider.getViews(LimitLineComponent.class.getName()).size(), 0);
		Assert.assertTrue(provider.getViews("abc").isEmpty());
	}
	
	@Test
	public void testPolicyInfos() {
		Assert.assertEquals(provider.getPolicyInfos().size(), 0);
	}
	
    @Test
    public void testAssets() {       
		class UnknownType {};
        
    	for (ComponentTypeInfo info : provider.getComponentTypes()) {
            // No icon
    		Assert.assertNull(provider.getAsset(info, Icon.class));
    		// Should have a wizard
    		Assert.assertNotNull(provider.getAsset(info, CreateWizardUI.class));
    	}

    	// Should not have assets for unknown types
    	TypeInfo<?> unknownInfo = new TypeInfo<UnknownType>(UnknownType.class){};
    	for (Class<?> type : new Class<?>[] {ImageIcon.class,Icon.class,UnknownType.class}) {
    		Assert.assertNull(provider.getAsset(unknownInfo, type));
    	}    	
    }

}
