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
package gov.nasa.arc.mct.table.model;

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
import gov.nasa.arc.mct.table.view.MultiColView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TableStructureTest {
	
	private AbstractComponent a0;
	private TableStructure zeroDimensional;

	private AbstractComponent a1;
	private AbstractComponent a2;
	private AbstractComponent a3;
	private TableStructure oneDimensional;

	private AbstractComponent c1;
	private AbstractComponent c2;
	private AbstractComponent c3;
	private AbstractComponent d1;
	private AbstractComponent d2;
	private AbstractComponent e1;
	private AbstractComponent e2;
	private TableStructure twoDimensional;
	@Mock private Platform mockPlatform;
	@Mock private PolicyManager mockPolicyManager;
	@Mock private CoreComponentRegistry mockComponentRegistry;
	@Mock private PersistenceProvider mockPersistenceProvider;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceProvider);
		Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockComponentRegistry);
		Mockito.when(mockComponentRegistry.getViewInfos(Mockito.anyString(), Mockito.any(ViewType.class)))
			.thenReturn(Collections.singleton(new ViewInfo(MultiColView.class,"","",ViewType.EMBEDDED)));
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(new ExecutionResult(null, true, ""));
        Mockito.when(mockPersistenceProvider.getReferencedComponents(Mockito.any(AbstractComponent.class))).thenReturn(Collections.<AbstractComponent>emptyList());

		(new PlatformAccess()).setPlatform(mockPlatform);
		
		a0 = new MockFeedComponent();
		zeroDimensional = new TableStructure(TableType.ZERO_DIMENSIONAL, a0);

		a1 = new MockLeafComponent();
		a2 = new MockFeedComponent();
		a3 = new MockCollection();
		
		oneDimensional = new TableStructure(TableType.ONE_DIMENSIONAL, new MockCollection(a1, a2, a3));

		c1 = new MockFeedComponent();
		c2 = new MockFeedComponent();
		c3 = new MockFeedComponent();
		
		AbstractComponent parent1 = new MockCollection(c1, c2, c3);
		
		d1 = new MockFeedComponent();
		d2 = new MockLeafComponent();
		
		AbstractComponent parent2 = new MockCollection(d1, d2);

		e1 = new MockFeedComponent();
		
		// Evaluators need > 1 child
		AbstractComponent evaluatorChild1 = new MockLeafComponent();
		AbstractComponent evaluatorChild2 = new MockLeafComponent();
		e2 = new MockEvaluator(evaluatorChild1, evaluatorChild2);

		AbstractComponent parent3 = new MockCollection(e1, e2);

		twoDimensional = new TableStructure(TableType.TWO_DIMENSIONAL, new MockCollection(parent1, parent2, parent3));
	}
	
	@Test(dataProvider="badIndexTests")
	public void testGetValueBadRowCount(TableType type, int rowIndex, int columnIndex) {
		TableStructure structure = getTableStructure(type);
		structure.getValue(rowIndex, columnIndex);
	}
	
	@DataProvider(name="badIndexTests")
	public Object[][] getBadIndexTests() {
		return new Object[][] {
				{ TableType.ZERO_DIMENSIONAL, -1, 0 },
				{ TableType.ZERO_DIMENSIONAL, 0, -1 },
				{ TableType.ONE_DIMENSIONAL, -1, 0 },
				{ TableType.ONE_DIMENSIONAL, 0, -1 },
				{ TableType.TWO_DIMENSIONAL, -1, 0 },
				{ TableType.TWO_DIMENSIONAL, 0, -1 },
		};
	}

	@Test
	public void testGetValue() {
		assertSame(zeroDimensional.getValue(0, 0), a0);

		assertSame(oneDimensional.getValue(0, 0), a1);
		assertSame(oneDimensional.getValue(1, 0), a2);
		assertSame(oneDimensional.getValue(2, 0), a3);

		assertSame(twoDimensional.getValue(0, 0), c1);
		assertSame(twoDimensional.getValue(0, 1), c2);
		assertSame(twoDimensional.getValue(0, 2), c3);
		assertSame(twoDimensional.getValue(1, 0), d1);
		assertSame(twoDimensional.getValue(1, 1), d2);
		assertNull(twoDimensional.getValue(1, 2));
		assertSame(twoDimensional.getValue(2, 0), e1);
		assertSame(twoDimensional.getValue(2, 1), e2);
		assertNull(twoDimensional.getValue(2, 2));
	}
	
	@Test(dataProvider="canSetTests")
	public void testCanSetValue(TableType type, int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn, boolean expected) {
		TableStructure structure = getTableStructure(type);
		assertEquals(structure.canSetValue(rowIndex, columnIndex, isInsertRow, isInsertColumn), expected);
	}
	
	private TableStructure getTableStructure(TableType type) {
		if (type == TableType.ZERO_DIMENSIONAL) {
			return zeroDimensional;
		} else if (type == TableType.ONE_DIMENSIONAL) {
			return oneDimensional;
		} else {
			return twoDimensional;
		}
	}

	@DataProvider(name="canSetTests")
	public Object[][] getCanSetTests() {
		return new Object[][] {
				{ TableType.ZERO_DIMENSIONAL, 0, 0, false, false, false },
				
				{ TableType.ONE_DIMENSIONAL, 0, 0, false, false, true },
				{ TableType.ONE_DIMENSIONAL, 1, 0, false, false, true },
				{ TableType.ONE_DIMENSIONAL, 2, 0, false, false, true },
				{ TableType.ONE_DIMENSIONAL, 3, 0, false, false, false },
				{ TableType.ONE_DIMENSIONAL, 4, 0, false, false, false },
				{ TableType.ONE_DIMENSIONAL, 0, 1, false, false, false },
				{ TableType.ONE_DIMENSIONAL, -1, 0, false, false, false },
				{ TableType.ONE_DIMENSIONAL, 0, -1, false, false, false },
				
				{ TableType.TWO_DIMENSIONAL, 0, 0, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 0, 1, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 0, 2, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 1, 0, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 1, 1, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 1, 2, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 2, 0, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 2, 1, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 2, 2, false, false, true },
				{ TableType.TWO_DIMENSIONAL, 0, 3, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 3, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 3, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 3, 0, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 4, 0, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 0, 4, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 4, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 4, false, false, false },
				{ TableType.TWO_DIMENSIONAL, -1, 0, false, false, false },
				{ TableType.TWO_DIMENSIONAL, 0, -1, false, false, false },
		};
	}

	@Test(dataProvider="setValueTests")
	public void testSetValue(TableType type, int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn) {
		TableStructure structure = getTableStructure(type);
		AbstractComponent[][] oldValues = new AbstractComponent[structure.getRowCount()][structure.getColumnCount()];
		for (int i=0; i < structure.getRowCount(); ++i) {
			for (int j=0; j < structure.getColumnCount(); ++j) {
				oldValues[i][j] = structure.getValue(i, j);
			}
		}
		AbstractComponent newValue = new MockFeedComponent();
		structure.setValue(rowIndex, columnIndex, isInsertRow, isInsertColumn, newValue);
		
		if (isInsertRow || isInsertColumn) {
			assertSame(structure.getValue(rowIndex, columnIndex), newValue);
		} else {
			for (int i=0; i < structure.getRowCount(); ++i) {
				for (int j=0; j < structure.getColumnCount(); ++j) {
					if (i==rowIndex && j==columnIndex) {
						assertSame(structure.getValue(i,j), newValue);
					} else {
						assertSame(structure.getValue(i,j), oldValues[i][j]);
					}
				}
			}
		}
	}
	
	@DataProvider(name="setValueTests")
	public Object[][] getSetValueTests() {
		return new Object[][] {
				{ TableType.ONE_DIMENSIONAL, 0, 0, false, false },
				{ TableType.ONE_DIMENSIONAL, 1, 0, false, false },
				{ TableType.ONE_DIMENSIONAL, 2, 0, false, false },
				{ TableType.ONE_DIMENSIONAL, 0, 0, true, false },
				{ TableType.ONE_DIMENSIONAL, 1, 0, true, false },
				{ TableType.ONE_DIMENSIONAL, 2, 0, true, false },
				{ TableType.ONE_DIMENSIONAL, 3, 0, true, false },
				
				{ TableType.TWO_DIMENSIONAL, 0, 0, false, false },
				{ TableType.TWO_DIMENSIONAL, 0, 1, false, false },
				{ TableType.TWO_DIMENSIONAL, 0, 2, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 0, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 1, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 2, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 0, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 1, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 2, false, false },
		};
	}

	@Test(dataProvider="badSetValueTests", expectedExceptions={ArrayIndexOutOfBoundsException.class, AssertionError.class})
	private void testBadSetValue(TableType type, Integer rowIndex, Integer columnIndex, boolean isInsertRow, boolean isInsertColumn) {
		TableStructure structure = getTableStructure(type);
		AbstractComponent newValue = new MockFeedComponent();
		structure.setValue(rowIndex, columnIndex, isInsertRow, isInsertColumn, newValue);
	}

	@DataProvider(name="badSetValueTests")
	private Object[][] getBadSetValueTests() {
		return new Object[][] {
				{ TableType.ZERO_DIMENSIONAL, 0, 0, false, false },
				
				{ TableType.ONE_DIMENSIONAL, 3, 0, false, false },
				{ TableType.ONE_DIMENSIONAL, 4, 0, false, false },
				{ TableType.ONE_DIMENSIONAL, 0, 1, false, false },
				{ TableType.ONE_DIMENSIONAL, -1, 0, false, false },
				{ TableType.ONE_DIMENSIONAL, 0, -1, false, false },
				
				{ TableType.TWO_DIMENSIONAL, 0, 3, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 3, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 3, false, false },
				{ TableType.TWO_DIMENSIONAL, 3, 0, false, false },
				{ TableType.TWO_DIMENSIONAL, 4, 0, false, false },
				{ TableType.TWO_DIMENSIONAL, 0, 4, false, false },
				{ TableType.TWO_DIMENSIONAL, 1, 4, false, false },
				{ TableType.TWO_DIMENSIONAL, 2, 4, false, false },
				{ TableType.TWO_DIMENSIONAL, -1, 0, false, false },
				{ TableType.TWO_DIMENSIONAL, 0, -1, false, false },
		};
	}

	private static class MockComponent extends AbstractComponent {
		
		private static final AtomicInteger idGenerator = new AtomicInteger(0);
		
		public MockComponent() {
			setId(Integer.toString(idGenerator.incrementAndGet()));
		}
		
	}
	
	// This needs to be public so it can be cloned.
	public static class MockCollection extends MockComponent {
		
		// Need a special constructor since we're not extending BaseComponent.
		public MockCollection(String id) {
			setId(id);
		}
		
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
		public String getSubscriptionId() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean isPrediction() {
			return false;
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
		public String getCanonicalName() {
			return null;
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
		public boolean requiresMultipleInputs() {
			return getComponents().size() > 1;
		}
		
		@Override
		public String getLanguage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FeedProvider.RenderingInfo evaluate(Map<String, List<Map<String, String>>> data,
				List<FeedProvider> providers) {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
}
