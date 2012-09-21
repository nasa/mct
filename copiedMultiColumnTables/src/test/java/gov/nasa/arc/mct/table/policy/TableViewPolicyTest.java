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
package gov.nasa.arc.mct.table.policy;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.evaluator.api.Evaluator;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;
import gov.nasa.arc.mct.table.access.ServiceAccess;
import gov.nasa.arc.mct.table.model.TableStructure;
import gov.nasa.arc.mct.table.model.TableType;
import gov.nasa.arc.mct.table.view.MultiColView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TableViewPolicyTest {

	@Mock private PolicyManager policyManager;
	
	private ExecutionResult success;
	private ExecutionResult failure;
	
	@Mock private AbstractComponent component;
	
	@Mock private Platform mockPlatform;
	@Mock private PolicyManager mockPolicyManager;
	@Mock private CoreComponentRegistry mockComponentRegistry;
	@Mock private PersistenceProvider mockPersistenceProvider;
	
	@Mock private AbstractComponent leaf;
	@Mock private AbstractComponent leafWithFeed;
	@Mock private FeedProvider mockFeedProvider;

	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
		Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockComponentRegistry);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);
        Mockito.when(mockPersistenceProvider.getReferencedComponents(Mockito.any(AbstractComponent.class))).thenReturn(Collections.<AbstractComponent>emptyList());

		Mockito.when(mockComponentRegistry.getViewInfos(Mockito.anyString(), Mockito.any(ViewType.class)))
			.thenReturn(Collections.singleton(new ViewInfo(MultiColView.class,"","",ViewType.EMBEDDED)));
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(new ExecutionResult(null, true, ""));
		(new PlatformAccess()).setPlatform(mockPlatform);
		
		new ServiceAccess().bind(policyManager);
		success = new ExecutionResult(new PolicyContext(), true, "success");
		failure = new ExecutionResult(new PolicyContext(), false, "failure");

		when(leaf.isLeaf()).thenReturn(true);
		when(leafWithFeed.isLeaf()).thenReturn(true);
		when(leafWithFeed.getCapability(FeedProvider.class)).thenReturn(mockFeedProvider);
	}

	@Test
	public void testPolicyExecution() {
		TableViewPolicy policy = new TableViewPolicy();
		PolicyContext context = new PolicyContext();
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), new ViewInfo(MultiColView.class,"table", ViewType.OBJECT));
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), leafWithFeed);
		Assert.assertTrue(policy.execute(context).getStatus());

		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), leaf);
		Assert.assertFalse(policy.execute(context).getStatus());
	}
	
	@Test
	public void testZeroDimensional() {
		when(policyManager.execute(isA(String.class), isA(PolicyContext.class))).thenReturn(success);

		AbstractComponent c;
		TableStructure structure;
		
		c = new MockLeafComponent();
		structure = TableViewPolicy.getTableStructure(c);
		assertEquals(structure.getType(), TableType.ZERO_DIMENSIONAL);
		assertEquals(structure.getRowCount(), 1);
		assertEquals(structure.getColumnCount(), 1);
		assertSame(structure.getValue(0, 0), c);
		
		c = new MockFeedComponent();
		structure = TableViewPolicy.getTableStructure(c);
		assertEquals(structure.getType(), TableType.ZERO_DIMENSIONAL);
		assertEquals(structure.getRowCount(), 1);
		assertEquals(structure.getColumnCount(), 1);
		assertSame(structure.getValue(0, 0), c);
		
		c = new MockEvaluator(new MockFeedComponent());
		structure = TableViewPolicy.getTableStructure(c);
		assertEquals(structure.getType(), TableType.ZERO_DIMENSIONAL);
		assertEquals(structure.getRowCount(), 1);
		assertEquals(structure.getColumnCount(), 1);
		assertSame(structure.getValue(0, 0), c);
	}
	
	@Test
	public void testOneDimensional() {
		when(policyManager.execute(isA(String.class), isA(PolicyContext.class))).thenReturn(success);
		
		AbstractComponent c1 = new MockLeafComponent();
		AbstractComponent c2 = new MockFeedComponent();
		AbstractComponent c3 = new MockCollection();
		AbstractComponent c4 = new MockEvaluator(c1,c2);
		
		AbstractComponent parent = new MockCollection(c1, c2, c3,c4);
		
		TableStructure structure = TableViewPolicy.getTableStructure(parent);
		assertEquals(structure.getType(), TableType.ONE_DIMENSIONAL);
		assertEquals(structure.getRowCount(), 4);
		assertEquals(structure.getColumnCount(), 1);
		assertSame(structure.getValue(0, 0), c1);
		assertSame(structure.getValue(1, 0), c2);
		assertSame(structure.getValue(2, 0), c3);
		assertSame(structure.getValue(3, 0), c4);
	}
	
	@Test
	public void testTwoDimensional() {
		when(policyManager.execute(isA(String.class), isA(PolicyContext.class))).thenReturn(success);
		
		AbstractComponent c1 = new MockFeedComponent();
		AbstractComponent c2 = new MockFeedComponent();
		AbstractComponent c3 = new MockFeedComponent();
		
		AbstractComponent parent1 = new MockCollection(c1, c2, c3);
		
		AbstractComponent d1 = new MockFeedComponent();
		AbstractComponent d2 = new MockLeafComponent();
		
		AbstractComponent parent2 = new MockCollection(d1, d2);

		AbstractComponent e1 = new MockFeedComponent();
		
		// Evaluators need > 1 child
		AbstractComponent evaluatorChild1 = new MockLeafComponent();
		AbstractComponent evaluatorChild2 = new MockLeafComponent();
		AbstractComponent e2 = new MockEvaluator(evaluatorChild1, evaluatorChild2);

		AbstractComponent parent3 = new MockCollection(e1, e2);

		AbstractComponent grandParent = new MockCollection(parent1, parent2, parent3);
		
		TableStructure structure = TableViewPolicy.getTableStructure(grandParent);
		assertEquals(structure.getType(), TableType.TWO_DIMENSIONAL);
		assertEquals(structure.getRowCount(), 3);
		assertEquals(structure.getColumnCount(), 3);
		assertSame(structure.getValue(0, 0), c1);
		assertSame(structure.getValue(0, 1), c2);
		assertSame(structure.getValue(0, 2), c3);
		assertSame(structure.getValue(1, 0), d1);
		assertSame(structure.getValue(1, 1), d2);
		assertNull(structure.getValue(1, 2));
		assertSame(structure.getValue(2, 0), e1);
		assertSame(structure.getValue(2, 1), e2);
		assertNull(structure.getValue(2, 2));
	}
	
	private static class MockComponent extends AbstractComponent {
		
		private static final AtomicInteger idGenerator = new AtomicInteger(0);
		
		public MockComponent() {
			setId(Integer.toString(idGenerator.incrementAndGet()));
		}
		
	}
	
	private static class MockCollection extends MockComponent {
		
		public MockCollection(AbstractComponent... children) {
			addDelegateComponents(Arrays.asList(children));
		}

	}
	
	private static class MockLeafComponent extends MockComponent {
		
		@Override
		public boolean isLeaf() {
			return true;
		}
		
	}
	
	private static class MockFeedComponent extends MockComponent implements FeedProvider {
		
		@Override
		protected <T> T handleGetCapability(Class<T> capability) {
			if (FeedProvider.class.isAssignableFrom(capability)) {
				return capability.cast(this);
			}
			return null;
		}

		@Override
		public String getCanonicalName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getLegendText() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMaximumSampleRate() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public boolean isPrediction() {
			return false;
		}

		@Override
		public String getSubscriptionId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public TimeService getTimeService() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RenderingInfo getRenderingInfo(Map<String, String> data) {
			return null;
		}
		
		@Override
		public FeedType getFeedType() {
			return FeedType.STRING;
		}
		
		@Override
		public long getValidDataExtent() {
			return System.currentTimeMillis();
		}

		@Override
		public boolean isNonCODDataBuffer() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private static class MockEvaluator extends MockCollection implements Evaluator {

		public MockEvaluator(AbstractComponent... children) {
			super(children);
		}
		
		@Override
		protected <T> T handleGetCapability(Class<T> capability) {
			if (Evaluator.class.isAssignableFrom(capability)) {
				return capability.cast(this);
			}
			return null;
		}

		@Override
		public String getCode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getLanguage() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean requiresMultipleInputs() {
			// TODO Auto-generated method stub
			return true;
		}
		
		@Override
		public FeedProvider.RenderingInfo evaluate(Map<String, List<Map<String, String>>> data,
				List<FeedProvider> providers) {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
}
