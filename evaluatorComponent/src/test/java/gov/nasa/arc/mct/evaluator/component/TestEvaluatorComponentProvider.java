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
package gov.nasa.arc.mct.evaluator.component;

import gov.nasa.arc.mct.evaluator.expressions.ExpressionsViewManifestation;
import gov.nasa.arc.mct.evaluator.expressions.MultiViewManifestation;
import gov.nasa.arc.mct.evaluator.view.EnumeratorViewPolicy;
import gov.nasa.arc.mct.evaluator.view.EvaluatorViewPolicy;
import gov.nasa.arc.mct.evaluator.view.InfoViewManifestation;
import gov.nasa.arc.mct.evaluator.view.MultiChildRemovalPolicy;
import gov.nasa.arc.mct.evaluator.view.MultiCompositionPolicy;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestEvaluatorComponentProvider {
	private EvaluatorComponentProvider evaluatorProvider;
	private MultiComponentProvider multiProvider;
	
	@BeforeMethod
	public void setup() {
		evaluatorProvider = new EvaluatorComponentProvider();
		multiProvider = new MultiComponentProvider();
	}
	
	@Test
	public void testGetComponentTypes() {
		Assert.assertFalse(evaluatorProvider.getComponentTypes().isEmpty());
		Assert.assertEquals(evaluatorProvider.getComponentTypes().size(), 1);
		Assert.assertTrue(evaluatorProvider.getComponentTypes().iterator().next().isCreatable()); 
		Assert.assertFalse(multiProvider.getComponentTypes().isEmpty());
		Assert.assertEquals(multiProvider.getComponentTypes().size(), 1);
		Assert.assertTrue(multiProvider.getComponentTypes().iterator().next().isCreatable()); 
	}
	
	@Test
	public void testGetMenuItemInfos() {
		Assert.assertFalse(evaluatorProvider.getMenuItemInfos().isEmpty());
		Assert.assertFalse(multiProvider.getMenuItemInfos().isEmpty());
	}
	
	@Test
	public void testPolicyInfos() {
		Assert.assertEquals(evaluatorProvider.getPolicyInfos().size(), 2);
		Iterator<PolicyInfo> it = evaluatorProvider.getPolicyInfos().iterator();
		Assert.assertEquals(it.next().getPolicyClasses()[0],EvaluatorViewPolicy.class);
		Assert.assertEquals(it.next().getPolicyClasses()[0],EnumeratorViewPolicy.class);
		Assert.assertEquals(multiProvider.getPolicyInfos().size(), 4);
		it = multiProvider.getPolicyInfos().iterator();
		Assert.assertEquals(it.next().getPolicyClasses()[0],EvaluatorViewPolicy.class);
		Assert.assertEquals(it.next().getPolicyClasses()[0],EnumeratorViewPolicy.class);
		Assert.assertEquals(it.next().getPolicyClasses()[0],MultiChildRemovalPolicy.class);
		Assert.assertEquals(it.next().getPolicyClasses()[0],MultiCompositionPolicy.class);
	}
	
	@Test
	public void testViews() {
		Collection<ViewInfo> views = evaluatorProvider.getViews(EvaluatorComponent.class.getName());
		Assert.assertEquals(views.size(), 3);
		Assert.assertTrue(views.contains(new ViewInfo(ExpressionsViewManifestation.class,"", ViewType.OBJECT)));
		Assert.assertTrue(views.contains(new ViewInfo(ExpressionsViewManifestation.class,"", ExpressionsViewManifestation.class.getName(), ViewType.CENTER, true, EvaluatorComponent.class)));

		// Should return only one view (info) when component is unknown
		Assert.assertEquals(evaluatorProvider.getViews("unknowncomponent").size(), 1);

		Iterator<ViewInfo> it = evaluatorProvider.getViews(EvaluatorComponent.class.getName()).iterator();
		Assert.assertEquals(it.next(), new ViewInfo(InfoViewManifestation.class, InfoViewManifestation.VIEW_NAME, ViewType.OBJECT));
		Assert.assertEquals(it.next(), new ViewInfo(ExpressionsViewManifestation.class, ExpressionsViewManifestation.VIEW_NAME, ViewType.OBJECT));

		views = multiProvider.getViews(MultiComponent.class.getName());
		Assert.assertEquals(views.size(), 3);
		Assert.assertTrue(views.contains(new ViewInfo(MultiViewManifestation.class, MultiViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), ViewType.CENTER, true, MultiComponent.class)));	

		it = multiProvider.getViews(MultiComponent.class.getName()).iterator();
		Assert.assertEquals(it.next(), new ViewInfo(InfoViewManifestation.class, InfoViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), ViewType.OBJECT, false, MultiComponent.class));
		Assert.assertEquals(it.next(), new ViewInfo(MultiViewManifestation.class, MultiViewManifestation.VIEW_NAME, ViewType.OBJECT));
		Assert.assertEquals(it.next(), new ViewInfo(MultiViewManifestation.class, MultiViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), ViewType.CENTER, true, MultiComponent.class));
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
			ViewInfo info = new ViewInfo(InfoViewManifestation.class, InfoViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), type);
			ViewInfo expr = new ViewInfo(ExpressionsViewManifestation.class, ExpressionsViewManifestation.VIEW_NAME, InfoViewManifestation.class.getName(), type);
			TypeInfo<?> eval = new ComponentTypeInfo("","",EvaluatorComponent.class);
			TypeInfo<?> mult = new ComponentTypeInfo("","",MultiComponent.class);
			for (TypeInfo<?> vi : new TypeInfo<?>[]{info,expr,eval,mult}) {
				// Expressions view should have an image icon
				cases.add(new Object[] { new EvaluatorComponentProvider(), vi, ImageIcon.class, vi.getTypeClass().equals(ExpressionsViewManifestation.class)});
				// Verify assignable check also recognizes icon 
				cases.add(new Object[] { new EvaluatorComponentProvider(), vi, Icon.class, vi.getTypeClass().equals(ExpressionsViewManifestation.class)});
				// Evaluator should have a wizard
				cases.add(new Object[] { new EvaluatorComponentProvider(), vi, CreateWizardUI.class, vi.getTypeClass().equals(EvaluatorComponent.class)});
				// Multi component providers have no icons
				cases.add(new Object[] { new MultiComponentProvider(), vi, ImageIcon.class, false});
				// As above
				cases.add(new Object[] { new MultiComponentProvider(), vi, Icon.class, false});
				// MultiComponent should have a wizard
				cases.add(new Object[] { new MultiComponentProvider(), vi, CreateWizardUI.class, vi.getTypeClass().equals(MultiComponent.class)});
			}
		}
		Object[][] returnValue = new Object[cases.size()][];
		for (int i = 0; i < cases.size(); i++) {
			returnValue[i] = cases.get(i);
		}
		return returnValue;
	}
}
