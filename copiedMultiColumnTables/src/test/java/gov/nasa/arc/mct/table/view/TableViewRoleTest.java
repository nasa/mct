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
package gov.nasa.arc.mct.table.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.gui.NamingContext;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.table.access.ServiceAccess;
import gov.nasa.arc.mct.table.gui.LabeledTable;
import gov.nasa.arc.mct.table.model.AbbreviatingTableLabelingAlgorithm;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class TableViewRoleTest {
	
	private MultiColView manifestation;
	@Mock
	private AbstractComponent component;
	@Mock
	private PolicyManager policyManager;
	@Mock
	private AbstractComponent row0, row1;
	private List<AbstractComponent> allComponents = new ArrayList<AbstractComponent>();
	
	
	@SuppressWarnings("serial")
	@BeforeMethod
	void init() {
		MockitoAnnotations.initMocks(this);
		allComponents.clear();
		Mockito.when(component.getDisplayName()).thenReturn("test");
		List<AbstractComponent> rootChildren = new ArrayList<AbstractComponent>();
		Mockito.when(component.getComponents()).thenReturn(rootChildren);
		ExecutionResult trueResult = new ExecutionResult(null, true, "");
		Mockito.when(policyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(trueResult);
		(new ServiceAccess()).bind(policyManager);
		List<AbstractComponent> row0Children = new ArrayList<AbstractComponent>();
		List<AbstractComponent> row1Children = new ArrayList<AbstractComponent>();
		
		Mockito.when(row0.getDisplayName()).thenReturn("row0");
		Mockito.when(row0.getComponents()).thenReturn(row0Children);
		rootChildren.add(row0);
		addComponents(2, row0Children, allComponents);
		
		Mockito.when(row1.getDisplayName()).thenReturn("row1");
		Mockito.when(row1.getComponents()).thenReturn(row1Children);
		addComponents(2, row1Children, allComponents);
		rootChildren.add(row1);
		final ExtendedProperties viewProps = new ExtendedProperties();
		manifestation =  new MultiColView(component,new ViewInfo(MultiColView.class,"",ViewType.CENTER)) {
			@Override
			public ExtendedProperties getViewProperties() {
				return viewProps;
			}
		};
	}
	
	private void addComponents(int components, List<AbstractComponent> childList, List<AbstractComponent> createdComponents) {
		for (int i = 0; i < components; i++) {
			AbstractComponent ac = Mockito.mock(AbstractComponent.class);
			Mockito.when(ac.getComponents()).thenReturn(Collections.<AbstractComponent>emptyList());
			Mockito.when(ac.isLeaf()).thenReturn(true);
			final View mvm = Mockito.mock(View.class);
			ViewInfo vi = new ViewInfo(MultiColView.class, "", ViewType.EMBEDDED) {
				@Override
				public View createView(AbstractComponent component) {
					return mvm;
				}
			};
			Mockito.when(ac.getViewInfos(ViewType.NODE)).thenReturn(Collections.singleton(vi));
			Mockito.when(ac.getViewInfos(ViewType.EMBEDDED)).thenReturn(Collections.singleton(vi));
			Mockito.when(ac.getComponentTypeID()).thenReturn("MockComponent");
			Mockito.when(mvm.getManifestedComponent()).thenReturn(ac);
			Mockito.when(mvm.getInfo()).thenReturn(vi);
			childList.add(ac);
			createdComponents.add(ac);
		}
	}
	
	@AfterMethod
	void tearDown() {
		(new ServiceAccess()).unbind(policyManager);
	}
	
	
	@Test
	public void testSelectionListener() throws Exception {
		final AtomicInteger eventsFired = new AtomicInteger(0);
		PropertyChangeListener pcl = new PropertyChangeListener() {
			
			@Override
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				eventsFired.incrementAndGet();
			}
		};
		
		manifestation.addSelectionChangeListener(pcl);
		Field f = MultiColView.class.getDeclaredField("table");
		f.setAccessible(true);
		LabeledTable labeledTable = (LabeledTable) f.get(manifestation);
		JTable jTable = labeledTable.getTable();
		
		ListSelectionModel rsm = jTable.getSelectionModel();
		ListSelectionModel csm = jTable.getColumnModel().getSelectionModel();
		
		// try combinations to ensure that each selection generates one and only one selection event
		for (int i = 0; i < 2; i++) {
			rsm.setSelectionInterval(i, i);
			eventsFired.set(0);
			for (int j = 0; j < 2; j++) {
				csm.setSelectionInterval(j, j);
				Assert.assertEquals(eventsFired.get(), 1);
				eventsFired.set(0);
				Collection<View> selected = manifestation.getSelectedManifestations();
				Assert.assertEquals(selected.size(), 1);
				Assert.assertSame(selected.iterator().next().getManifestedComponent(), allComponents.get(i*2+j));
			}
		}
	}
	
	String naming; 
	
	@Test
	public void testNamingContext() throws Exception {
		Field f = MultiColView.class.getDeclaredField("labelingAlgorithm");
		f.setAccessible(true);
		f.set(manifestation, new AbbreviatingTableLabelingAlgorithm() {
			@Override
			public void setContextLabels(String... s) { naming = s[0]; }
		});
		
		NamingContext nullContext = Mockito.mock(NamingContext.class);
		NamingContext blankContext = Mockito.mock(NamingContext.class);
		NamingContext specificContext = Mockito.mock(NamingContext.class);
		
		PropertyChangeEvent event = Mockito.mock(PropertyChangeEvent.class);
		
		Mockito.when(nullContext.getContextualName()).thenReturn(null);
		Mockito.when(blankContext.getContextualName()).thenReturn("");
		Mockito.when(specificContext.getContextualName()).thenReturn("Specific");
		
		manifestation.setNamingContext(nullContext);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, "");
		
		manifestation.setNamingContext(blankContext);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, manifestation.getManifestedComponent().getDisplayName());
		
		manifestation.setNamingContext(specificContext);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, "Specific");
		
		manifestation.setNamingContext(null);
		manifestation.updateMonitoredGUI(event);
		Assert.assertEquals(naming, manifestation.getManifestedComponent().getDisplayName());
	}
	
}
