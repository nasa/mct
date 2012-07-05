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
package gov.nasa.arc.mct.fastplot.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.fastplot.access.PolicyManagerAccess;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotViewPolicy {
	
	@Mock
	private AbstractComponent leafWithAFeedComponent;
	
	@Mock
	private AbstractComponent leafWithOutAFeedComponent;
	
	@Mock
	private AbstractComponent nonLeafComponent;
	
	@Mock
	private AbstractComponent compoundComponent;
	
	@Mock
	private AbstractComponent childCompoundComponent1;
	
	@Mock
	private AbstractComponent childCompoundComponent2;
	
	
	@Mock
	private PolicyManager mockPolicyManager;
	
	@Mock
	private FeedProvider provider;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(leafWithAFeedComponent.isLeaf()).thenReturn(true);
		Mockito.when(leafWithAFeedComponent.getCapability(FeedProvider.class)).thenReturn(provider);
		
		Mockito.when(leafWithOutAFeedComponent.isLeaf()).thenReturn(true);
		Mockito.when(leafWithOutAFeedComponent.getCapability(FeedProvider.class)).thenReturn(null);
		
		Mockito.when(nonLeafComponent.isLeaf()).thenReturn(false);
		Mockito.when(nonLeafComponent.getCapability(FeedProvider.class)).thenReturn(provider);
		
	}
		
	@Test
	public void testHasFeed() {
		Assert.assertTrue(PlotViewPolicy.hasFeed(leafWithAFeedComponent));
		Assert.assertFalse(PlotViewPolicy.hasFeed(leafWithOutAFeedComponent));
		Assert.assertTrue(PlotViewPolicy.hasFeed(nonLeafComponent));
	}
	
	@Test
	public void testIsCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot() {
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(leafWithAFeedComponent));
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(leafWithOutAFeedComponent));
		
		// setup compound components with a child that requires a plot.
		List<AbstractComponent> childComponents = new ArrayList<AbstractComponent>();
		childComponents.add(leafWithAFeedComponent);	
		childComponents.add(leafWithOutAFeedComponent);
		Mockito.when(compoundComponent.getComponents()).thenReturn(childComponents);
		
		// we now have two children. One with a feed and one without. 
		Assert.assertTrue(PlotViewPolicy.isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(compoundComponent));
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithCompoundChildrenThatRequirePlots(compoundComponent));
		
		// change our feed with a child to one without. 
		childComponents.remove(leafWithAFeedComponent);
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(compoundComponent));	
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithCompoundChildrenThatRequirePlots(compoundComponent));
	}
	
	@Test
	public void testIsCompoundComponentWithCompoundChildrenThatRequirePlots() {
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithCompoundChildrenThatRequirePlots(leafWithAFeedComponent));
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithCompoundChildrenThatRequirePlots(leafWithOutAFeedComponent));
		
		List<AbstractComponent> childComponents = new ArrayList<AbstractComponent>();
		childComponents.add(childCompoundComponent1);	
		childComponents.add(childCompoundComponent2);
		Mockito.when(compoundComponent.getComponents()).thenReturn(childComponents);
		Mockito.when(compoundComponent.isLeaf()).thenReturn(false);
		
		
		List<AbstractComponent> childCompound1Children = new ArrayList<AbstractComponent>();
		List<AbstractComponent> childCompound2Children = new ArrayList<AbstractComponent>();
		
		childCompound1Children.add(leafWithAFeedComponent);	
		childCompound2Children.add(leafWithOutAFeedComponent);
		
		Mockito.when(childCompoundComponent1.getComponents()).thenReturn(childCompound1Children);
		Mockito.when(childCompoundComponent2.getComponents()).thenReturn(childCompound2Children);
		Mockito.when(childCompoundComponent1.isLeaf()).thenReturn(false);
		Mockito.when(childCompoundComponent2.isLeaf()).thenReturn(false);
		
		Assert.assertTrue(PlotViewPolicy.isCompoundComponentWithCompoundChildrenThatRequirePlots(compoundComponent));
		
		Mockito.when(childCompoundComponent1.getComponents()).thenReturn(childCompound2Children);
		
		Assert.assertFalse(PlotViewPolicy.isCompoundComponentWithCompoundChildrenThatRequirePlots(compoundComponent));
	}
	
	@Test
	public void testGetNonCompoundPlotComponents() {
		(new PolicyManagerAccess()).setPolicyManager(mockPolicyManager);
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(
				new ExecutionResult(null,true,"test"));
		
		AbstractComponent[][] components = PlotViewPolicy.getPlotComponents(leafWithAFeedComponent, true);
		Assert.assertEquals(components.length,1);
		Assert.assertSame(components[0][0], leafWithAFeedComponent);
		
		components = PlotViewPolicy.getPlotComponents(leafWithOutAFeedComponent, true);
		Assert.assertEquals(components.length,0);
		
		// setup compound components with a child that requires a plot.
		List<AbstractComponent> childComponents = new ArrayList<AbstractComponent>();
		childComponents.add(leafWithAFeedComponent);	
		childComponents.add(leafWithOutAFeedComponent);
		Mockito.when(compoundComponent.getComponents()).thenReturn(childComponents);
	}
	
	@Test
	public void testGetCompoundPlotComponents() {
		(new PolicyManagerAccess()).setPolicyManager(mockPolicyManager);
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(
				new ExecutionResult(null,true,"test"));
		// setup compound components with a child that requires a plot.
		List<AbstractComponent> childComponents = new ArrayList<AbstractComponent>();
		childComponents.add(leafWithAFeedComponent);	
		childComponents.add(leafWithOutAFeedComponent);
		Mockito.when(compoundComponent.getComponents()).thenReturn(childComponents);
		
		AbstractComponent[][] components = PlotViewPolicy.getPlotComponents(compoundComponent, true);
		Assert.assertEquals(components.length,1);
		Assert.assertSame(components[0][0], leafWithAFeedComponent);
	}
	
	private AbstractComponent createComponentWithNoFeed() {
		AbstractComponent comp = Mockito.mock(AbstractComponent.class);
		Mockito.when(comp.isLeaf()).thenReturn(true);
		
		return comp;
	}
	
	private AbstractComponent createComponentWithFeed(String displayName) {
		AbstractComponent comp = Mockito.mock(AbstractComponent.class);
		Mockito.when(comp.isLeaf()).thenReturn(true);
		Mockito.when(comp.getDisplayName()).thenReturn(displayName);
		Mockito.when(comp.getCapability(FeedProvider.class)).thenReturn(provider);
		
		return comp;
	}
	
	private AbstractComponent createComponentWithChildren(int numChildren, String prefix) {
		AbstractComponent comp = Mockito.mock(AbstractComponent.class);
		List<AbstractComponent> childComponents = new ArrayList<AbstractComponent>();
		Mockito.when(comp.getComponents()).thenReturn(childComponents);
		
		childComponents.add(createComponentWithNoFeed());
		
		for (int i =0; i < numChildren; i++) {
			childComponents.add(createComponentWithFeed(prefix+Integer.toString(i)));
		}
		
		return comp;
	}
	
	@Test
	public void testRejectCanvasView() throws Exception {
		(new PolicyManagerAccess()).setPolicyManager(mockPolicyManager);
		AbstractComponent comp = Mockito.mock(AbstractComponent.class);
		PlotViewPolicy policy = new PlotViewPolicy();
		PolicyContext context = new PolicyContext();
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), new ViewInfo(PlotViewManifestation.class,"",ViewType.CENTER));
		Method m = policy.getClass().getDeclaredMethod("rejectCanvasView", PolicyContext.class, AbstractComponent.class);
		m.setAccessible(true);
		Mockito.when(comp.isLeaf()).thenReturn(false);
		Assert.assertEquals(m.invoke(policy, context, comp), Boolean.FALSE, "direct access to plot view role should always be true");
		context.setProperty(PolicyContext.PropertyName.VIEW_TYPE.getName(), ViewType.CENTER);
		Assert.assertEquals(m.invoke(policy, context, comp), Boolean.TRUE, "collections must not support canvas view roles");
		Mockito.when(comp.isLeaf()).thenReturn(true);
		Assert.assertEquals(m.invoke(policy, context, comp), Boolean.FALSE, "leafs must support canvas view roles");

	}
	
	@Test
	public void testGetCompoundPlotComponentsWithChildren() {
		(new PolicyManagerAccess()).setPolicyManager(mockPolicyManager);
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(
				new ExecutionResult(null,true,"test"));
		// setup compound components with a child that requires a plot.
		List<AbstractComponent> childComponents = new ArrayList<AbstractComponent>();
		String start = "A";
		for (int i = 0; i < 12; i++) {
			AbstractComponent child = createComponentWithChildren(12, start);
			childComponents.add(child);
			start+="'";
		}
		Mockito.when(compoundComponent.getComponents()).thenReturn(childComponents);
		// also verify the sequence of components names to make sure the group is being divided equally
		
		AbstractComponent[][] components = PlotViewPolicy.getPlotComponents(compoundComponent, true);
		Assert.assertEquals(components.length,10);
		int offset = 0;
		for (AbstractComponent[] col:components) {
			Assert.assertEquals(col.length,12);
			String sequence = "A";
			for (AbstractComponent component : col) {
				Assert.assertEquals(component.getDisplayName(), sequence+Integer.valueOf(offset));
				sequence+="'";
			}
			offset++;
		}
	}
	
}
