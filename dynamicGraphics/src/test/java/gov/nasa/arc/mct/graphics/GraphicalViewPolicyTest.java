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
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.graphics.component.GraphicalComponent;
import gov.nasa.arc.mct.graphics.view.GraphicalManifestation;
import gov.nasa.arc.mct.graphics.view.StaticGraphicalView;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class GraphicalViewPolicyTest {
	@Mock private FeedProvider  mockFeedProvider;
	
	private PolicyContext     context;
	
	private ViewInfo          graphicalViewInfo = new ViewInfo(GraphicalManifestation.class, GraphicalManifestation.VIEW_ROLE_NAME, ViewType.OBJECT);
	private ViewInfo          otherViewInfo = new ViewInfo(View.class, "", ViewType.OBJECT);
	private ViewInfo          staticViewInfo = new ViewInfo(StaticGraphicalView.class, "", ViewType.EMBEDDED);
	
	private AbstractComponent feedComponent = new AbstractComponent() {

		@Override
		protected <T> T handleGetCapability(Class<T> capability) {
			if (FeedProvider.class.isAssignableFrom(capability)) {
				return capability.cast(mockFeedProvider);
			} else {
				return null;
			}
		}
		
	};
	
	private AbstractComponent nonfeedComponent = new AbstractComponent() {

		@Override
		protected <T> T handleGetCapability(Class<T> capability) {
			return null;
		}
		
	};
	
	@Mock private GraphicalComponent mockGraphicalComponent;
	
	private Policy testPolicy;
	
	@BeforeTest
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testPolicy = new GraphicalViewPolicy();
		context = new PolicyContext();
	}
	
	@Test 
	public void testFeedComponent() {
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), feedComponent);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), graphicalViewInfo);
		
		Assert.assertTrue(testPolicy.execute(context).getStatus());
	}
	
	@Test 
	public void testNonFeedComponent() {
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), nonfeedComponent);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), graphicalViewInfo);
		
		Assert.assertFalse(testPolicy.execute(context).getStatus());
	}
	
	@Test 
	public void testOtherView() {
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), feedComponent);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), otherViewInfo);
		
		Assert.assertTrue(testPolicy.execute(context).getStatus());
	}
	
	@Test 
	public void testOtherView2() {
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), nonfeedComponent);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), otherViewInfo);
		
		Assert.assertTrue(testPolicy.execute(context).getStatus());
	}
	
	@Test
	public void testStaticGraphicalView() {
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), nonfeedComponent);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), staticViewInfo);
		
		Assert.assertFalse(testPolicy.execute(context).getStatus());
		
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), mockGraphicalComponent);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), staticViewInfo);
		
		Assert.assertTrue(testPolicy.execute(context).getStatus());
	}
	
	@Test
	public void testStringPolicy() {
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), mockFeedProvider);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), graphicalViewInfo);
		
		Policy policy = new GraphicalStringPolicy();
		
		Mockito.when(mockFeedProvider.getFeedType()).thenReturn(FeedType.STRING);
		Assert.assertFalse(policy.execute(context).getStatus());
		
		Mockito.when(mockFeedProvider.getFeedType()).thenReturn(FeedType.INTEGER);
		Assert.assertTrue(policy.execute(context).getStatus());
		
		Mockito.when(mockFeedProvider.getFeedType()).thenReturn(FeedType.FLOATING_POINT);
		Assert.assertTrue(policy.execute(context).getStatus());
	}
}
