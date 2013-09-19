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
package gov.nasa.arc.mct.graphics;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.graphics.component.GraphicalComponent;
import gov.nasa.arc.mct.graphics.view.GraphicalManifestation;
import gov.nasa.arc.mct.graphics.view.StaticGraphicalView;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class GraphicalComponentProviderTest {

	private AbstractComponentProvider provider;
	
	@BeforeTest
	public void setup() {
		provider = new GraphicalComponentProvider();
	}
	
	@Test
	public void testViewInfos() {
		boolean foundDynamicGraphicalView = false;
		boolean foundStaticGraphicalView = true;
		
		for (ViewInfo info : provider.getViews(null)) {
			if (info.getViewClass().equals(GraphicalManifestation.class)) foundDynamicGraphicalView = true;
			if (info.getViewClass().equals(StaticGraphicalView.class)) foundStaticGraphicalView = true;
		}
		
		Assert.assertTrue(foundDynamicGraphicalView);
		Assert.assertTrue(foundStaticGraphicalView);
	}

	@Test
	public void testPolicyInfos() {
		boolean foundPolicy = false;
		
		for (PolicyInfo info : provider.getPolicyInfos()) {
			for (Class<?> c : info.getPolicyClasses()) {
				if (c.equals(GraphicalViewPolicy.class)) {
					foundPolicy = true;
					Assert.assertEquals(info.getCategoryKey(), PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey());
				}
			}
			
		}
		
		Assert.assertTrue(foundPolicy);
	}
	
	@Test
	public void testComponentTypeInfos() {
		boolean foundGraphicalComponent = false;
		
		for (ComponentTypeInfo info : provider.getComponentTypes()) {
			if (info.getComponentClass().equals(GraphicalComponent.class)){
				foundGraphicalComponent = true;
				Assert.assertTrue(info.isCreatable());
			}
		}
		
		Assert.assertTrue(foundGraphicalComponent);
	}
	
	
	@Test
	public void testAssets() {
		for (ViewInfo viewInfo : provider.getViews("")) {
			// Verify that there's an icon
			Assert.assertNotNull(provider.getAsset(viewInfo, ImageIcon.class));
			// Verify that assignability is correctly assessed
			Assert.assertNotNull(provider.getAsset(viewInfo, Icon.class));
			// Verify that unknown types are not reported by getAsset
			Assert.assertNull(provider.getAsset(viewInfo, UnknownType.class));
		}
		for (ComponentTypeInfo compInfo : provider.getComponentTypes()) {
			// Verify that there's a wizard
			Assert.assertNotNull(provider.getAsset(compInfo, CreateWizardUI.class));
			// Verify that unknown types are not reported by getAsset
			Assert.assertNull(provider.getAsset(compInfo, UnknownType.class));
		}
		// Verify that unknown types consistently return null
		TypeInfo<?>[] unknowns = { 
				new TypeInfo<Object>(UnknownType.class){},
				new ComponentTypeInfo("","",UnknownComponent.class),
				new ViewInfo(UnknownView.class, "", "", ViewType.OBJECT)
		};
		for (TypeInfo<?> unknown : unknowns) {
			Assert.assertNull(provider.getAsset(unknown, ImageIcon.class));
			Assert.assertNull(provider.getAsset(unknown, CreateWizardUI.class));
			Assert.assertNull(provider.getAsset(unknown, UnknownType.class));
		}
	}
	
	private static class UnknownType {}
	private static class UnknownComponent extends AbstractComponent {
		@SuppressWarnings("unused") // Verified when ComponentTypeInfo is instantiated
		public UnknownComponent() {}
	}
	private static class UnknownView extends View {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused") // Verified when ViewInfo is instantiated
		public UnknownView (AbstractComponent ac, ViewInfo vi) {}
	}
}
