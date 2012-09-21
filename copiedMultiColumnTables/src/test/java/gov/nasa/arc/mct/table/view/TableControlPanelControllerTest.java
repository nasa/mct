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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

//import java.awt.Color;
//import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.table.gui.LabeledTable;
import gov.nasa.arc.mct.table.model.ComponentTableModel;
import gov.nasa.arc.mct.table.model.LabeledTableModel;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.model.TableType;

import javax.swing.JList;
import javax.swing.JTable;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TableControlPanelControllerTest {

	private static final Object[][] rowData = new Object[][] { 
		{ 1, 2, 3 },
		{ 4, 5, 6 }
	};
	private static final String[] rowNames = new String[] { "One", "Two" };
	private static final String[] columnNames = new String[] { "A", "B", "C" };
	
	@Mock private MultiColView manifestation;
	@Mock private LabeledTable labeledTable;
	@Mock private LabeledTableModel model;
	
	private JTable table;
	private JList rowHeaders;
	private TableControlPanelController controller;
	
	@BeforeMethod
	public void init() {
		org.mockito.MockitoAnnotations.initMocks(this);
		
		table = new JTable(rowData, columnNames);
		rowHeaders = new JList(rowNames);
		
		when(labeledTable.getTable()).thenReturn(table);
		when(labeledTable.getRowHeaders()).thenReturn(rowHeaders);
		
		controller = new TableControlPanelController(manifestation, labeledTable, model);
	}
	
	@Test
	public void testGettersSetters() {
		table.setShowGrid(false);
		assertFalse(controller.getShowGrid());
		table.setShowGrid(true);
		assertTrue(controller.getShowGrid());
		
		controller.setShowGrid(false);
		verify(labeledTable).setShowGrid(false);
		
		when(model.getOrientation()).thenReturn(TableOrientation.ROW_MAJOR, TableOrientation.COLUMN_MAJOR);
		assertEquals(controller.getTableOrientation(), TableOrientation.ROW_MAJOR);
		assertEquals(controller.getTableOrientation(), TableOrientation.COLUMN_MAJOR);
		
		controller.setTableOrientation(TableOrientation.ROW_MAJOR);
		verify(model).setOrientation(TableOrientation.ROW_MAJOR);
	}
	
	@Test
	public void testSelectionCounts() {
		table.setRowSelectionInterval(0, 0);
		table.setColumnSelectionInterval(0, 0);
		assertEquals(controller.getSelectedRowCount(), 1);
		assertEquals(controller.getSelectedCellCount(), 1);
		table.clearSelection();
		assertEquals(controller.getSelectedRowCount(), 0);
		assertEquals(controller.getSelectedCellCount(), 0);
		
		table.setRowSelectionInterval(0, 1);
		table.setColumnSelectionInterval(1, 2);
		assertEquals(controller.getSelectedColumnCount(), 2);
		assertEquals(controller.getSelectedCellCount(), 4);
		table.clearSelection();
		assertEquals(controller.getSelectedColumnCount(), 0);
		assertEquals(controller.getSelectedCellCount(), 0);
	}
	
	@Test
	public void testColumnWidth() {
		table.clearSelection();
		assertTrue(controller.getColumnWidth() < 0);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(32);
		table.setColumnSelectionInterval(0, 0);
		assertEquals(controller.getColumnWidth(), 32);
		
		int[] selectedColumns = table.getSelectedColumns();
		assertEquals(selectedColumns.length, 1);
		assertEquals(selectedColumns[0], 0);
		controller.setColumnWidth(45);
		assertEquals(table.getColumnModel().getColumn(0).getPreferredWidth(), 45);
		
		// Now test setting the width for 2 columns.
		table.setColumnSelectionInterval(1, 2);
		controller.setColumnWidth(53);
		assertEquals(table.getColumnModel().getColumn(1).getPreferredWidth(), 53);
		assertEquals(table.getColumnModel().getColumn(2).getPreferredWidth(), 53);
	}
	
	@Test
	public void testRowHeight() {
		table.clearSelection();
		assertTrue(controller.getRowHeight() < 0);
		
		table.setRowHeight(0, 32);
		table.setRowSelectionInterval(0, 0);
		assertEquals(controller.getRowHeight(), table.getRowHeight());
		
		controller.setRowHeight(45);
		assertEquals(table.getRowHeight(0), 45);
		
		// Now test setting the height for 2 rows.
		table.setRowSelectionInterval(0, 1);
		controller.setRowHeight(53);
		assertEquals(table.getRowHeight(0), 53);
		assertEquals(table.getRowHeight(1), 53);
	}
	
	@Test(dataProvider="skeletonTests")
	public void testWhenSkeleton(boolean isSkeleton) {
		when(model.isSkeleton()).thenReturn(isSkeleton);
		
		table.setRowSelectionInterval(0, 0);
		table.setColumnSelectionInterval(0, 0);
		
		assertEquals(isSkeleton ? 0 : 1, controller.getSelectedRowCount());
		assertEquals(isSkeleton ? 0 : 1, controller.getSelectedColumnCount());
		assertEquals(isSkeleton ? 0 : 1, controller.getSelectedCellCount());
	}
	
	@DataProvider(name="skeletonTests")
	public Object[][] getSkeletonTests() {
		return new Object[][] {
				{ false },
				{ true }
		};
	}

	@Test
	public void testCanHideHeaders() {
		// Should always return true, for now, because the logic isn't fully implemented.
		assertTrue(controller.isCanHideHeaders());
	}
	
	@Test
	public void testCanTranspose() {
		when(model.getTableType()).thenReturn(TableType.ZERO_DIMENSIONAL, TableType.ONE_DIMENSIONAL, TableType.TWO_DIMENSIONAL);
		
		assertFalse(controller.canTranspose());
		assertFalse(controller.canTranspose());
		assertTrue(controller.canTranspose());
	}

	@Test
	public void testCanSetOrientation() {
		when(model.getTableType()).thenReturn(TableType.ZERO_DIMENSIONAL, TableType.ONE_DIMENSIONAL, TableType.TWO_DIMENSIONAL);
		
		assertFalse(controller.canSetOrientation());
		assertTrue(controller.canSetOrientation());
		assertTrue(controller.canSetOrientation());
	}
	
	@Test(dataProvider="selectionIntervals")
	public void testGetCellFormatInfoWhenNotSingleSelection(int row0, int row1, int col0, int col1, boolean isSingleCell) {
		if (row0 > 0 && col0 > 0) {
			table.setRowSelectionInterval(row0, row1);
			table.setColumnSelectionInterval(col0, col1);
		}
		
		if (!isSingleCell) {
			assertNull(controller.getRowLabelAbbreviationSettings());
			assertNull(controller.getColumnLabelAbbreviationSettings());
			assertNull(controller.getCellLabelAbbreviationSettings());
		}
	}
	
	@DataProvider(name="selectionIntervals")
	public Object[][] getSelectionIntervals() {
		return new Object[][] {
				{ -1, -1, -1, -1, false },
				{ 0, 0, 0, 0, true },
				{ 0, 1, 0, 0, false },
				{ 0, 0, 0, 1, false },
		};
	}
	
	
	@Test
	public void testGetDecimalPlaces() {
		ComponentTableModel componentTableModel = Mockito.mock(ComponentTableModel.class);
		TableCellSettings settings = Mockito.mock(TableCellSettings.class);
		AbstractComponent compWithFeed = Mockito.mock(AbstractComponent.class);
		FeedProvider mockFp = Mockito.mock(FeedProvider.class);
		Mockito.when(compWithFeed.getCapability(FeedProvider.class)).thenReturn(mockFp);
		AbstractComponent compWithoutFeed = Mockito.mock(AbstractComponent.class);
		Mockito.when(componentTableModel.getStoredValueAt(Mockito.eq(0), Mockito.anyInt())).thenReturn(compWithFeed);
		Mockito.when(componentTableModel.getCellSettings(Mockito.anyString())).thenReturn(settings);
		Mockito.when(settings.getNumberOfDecimals()).thenReturn(-1);
		Mockito.when(mockFp.getFeedType()).thenReturn(FeedType.FLOATING_POINT);
		controller = new TableControlPanelController(manifestation, labeledTable, componentTableModel);
		
		Mockito.when(labeledTable.getSelectedRows()).thenReturn(new int[] {0});
		Mockito.when(labeledTable.getSelectedColumns()).thenReturn(new int[] {0});
		Assert.assertEquals(controller.getDecimalPlaces().intValue(),2);
		Mockito.when(mockFp.getFeedType()).thenReturn(FeedType.INTEGER);
		Assert.assertEquals(controller.getDecimalPlaces().intValue(),0);

		Mockito.when(componentTableModel.getStoredValueAt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(compWithoutFeed);
		Mockito.when(labeledTable.getSelectedRows()).thenReturn(new int[] {1});
		Assert.assertEquals(controller.getDecimalPlaces().intValue(), -1);
	}
	
	@Test
	public void testGetCellFormats() {
		ComponentTableModel componentTableModel = Mockito.mock(ComponentTableModel.class);
		TableCellSettings settings = Mockito.mock(TableCellSettings.class);
		AbstractComponent compWithFeed = Mockito.mock(AbstractComponent.class);
		FeedProvider mockFp = Mockito.mock(FeedProvider.class);
		Mockito.when(compWithFeed.getCapability(FeedProvider.class)).thenReturn(mockFp);
		AbstractComponent compWithoutFeed = Mockito.mock(AbstractComponent.class);
		Mockito.when(componentTableModel.getStoredValueAt(Mockito.eq(0), Mockito.anyInt())).thenReturn(compWithFeed);
		Mockito.when(componentTableModel.getCellSettings(Mockito.anyString())).thenReturn(settings);
		Mockito.when(settings.getCellFont()).thenReturn(TableFormattingConstants.JVMFontFamily.Monospaced);
		Mockito.when(mockFp.getFeedType()).thenReturn(FeedType.FLOATING_POINT);
		controller = new TableControlPanelController(manifestation, labeledTable, componentTableModel);
		Mockito.when(labeledTable.getSelectedRows()).thenReturn(new int[] {0});
		Mockito.when(labeledTable.getSelectedColumns()).thenReturn(new int[] {0});

		Assert.assertEquals(controller.getCellFontName(),TableFormattingConstants.JVMFontFamily.Monospaced);
		Mockito.when(settings.getFontSize()).thenReturn(TableFormattingConstants.defaultFontSize);
		Assert.assertEquals(controller.getCellFontSize().intValue(), TableFormattingConstants.defaultFontSize);
		Mockito.when(settings.getFontStyle()).thenReturn(Font.PLAIN);
		Assert.assertEquals(controller.getCellFontStyle().intValue(), Font.PLAIN);
		Mockito.when(settings.getTextAttributeUnderline()).thenReturn(TextAttribute.UNDERLINE_ON);
		Assert.assertEquals(controller.getCellFontTextAttribute(), TextAttribute.UNDERLINE_ON);
		
	}
 	
	@Test
	public void testEnumerations() {
		ComponentTableModel componentTableModel = Mockito.mock(ComponentTableModel.class);
		TableCellSettings settings1 = Mockito.mock(TableCellSettings.class);
		TableCellSettings settings2 = Mockito.mock(TableCellSettings.class);
		TableCellSettings settings3 = Mockito.mock(TableCellSettings.class);
		TableCellSettings settings4 = Mockito.mock(TableCellSettings.class);
		AbstractComponent compWithFeed1 = Mockito.mock(AbstractComponent.class);
		AbstractComponent compWithFeed2 = Mockito.mock(AbstractComponent.class);
		AbstractComponent compWithFeed3 = Mockito.mock(AbstractComponent.class);
		AbstractComponent compWithFeed4 = Mockito.mock(AbstractComponent.class);
		Mockito.when(compWithFeed1.getComponentId()).thenReturn("comp 1");
		Mockito.when(compWithFeed2.getComponentId()).thenReturn("comp 2");
		Mockito.when(compWithFeed3.getComponentId()).thenReturn("comp 3");
		Mockito.when(compWithFeed4.getComponentId()).thenReturn("comp 4");

		AbstractComponent evaluator1 = Mockito.mock(AbstractComponent.class);
		AbstractComponent evaluator2 = Mockito.mock(AbstractComponent.class);
		AbstractComponent evaluator3 = Mockito.mock(AbstractComponent.class);
		AbstractComponent evaluator4 = Mockito.mock(AbstractComponent.class);
		Mockito.when(evaluator1.getId()).thenReturn("eval 1");
		Mockito.when(evaluator2.getId()).thenReturn("eval 2");
		Mockito.when(evaluator3.getId()).thenReturn("eval 3");
		Mockito.when(evaluator4.getId()).thenReturn("eval 4");
		
		Mockito.when(settings1.getEvaluator()).thenReturn(evaluator1);
		Mockito.when(settings2.getEvaluator()).thenReturn(null);
		Mockito.when(settings3.getEvaluator()).thenReturn(evaluator2);
		Mockito.when(settings4.getEvaluator()).thenReturn(evaluator1);
		FeedProvider mockFp1 = Mockito.mock(FeedProvider.class);
		FeedProvider mockFp2 = Mockito.mock(FeedProvider.class);
		FeedProvider mockFp3 = Mockito.mock(FeedProvider.class);
		FeedProvider mockFp4 = Mockito.mock(FeedProvider.class);
		Mockito.when(mockFp1.getSubscriptionId()).thenReturn("sub1");
		Mockito.when(mockFp2.getSubscriptionId()).thenReturn("sub2");
		Mockito.when(mockFp3.getSubscriptionId()).thenReturn("sub3");
		Mockito.when(mockFp4.getSubscriptionId()).thenReturn("sub4");
		Mockito.when(compWithFeed1.getCapability(FeedProvider.class)).thenReturn(mockFp1);
		Mockito.when(compWithFeed2.getCapability(FeedProvider.class)).thenReturn(mockFp2);
		Mockito.when(compWithFeed3.getCapability(FeedProvider.class)).thenReturn(mockFp3);
		Mockito.when(compWithFeed4.getCapability(FeedProvider.class)).thenReturn(mockFp4);
		Mockito.when(componentTableModel.getStoredValueAt(Mockito.anyInt(), Mockito.anyInt())).thenReturn(compWithFeed1);

		Assert.assertEquals(compWithFeed1.getComponentId(), "comp 1");
		Mockito.when(componentTableModel.getKey(compWithFeed1)).thenReturn("sub1");
		Mockito.when(componentTableModel.getKey(compWithFeed2)).thenReturn("sub2");
		Mockito.when(componentTableModel.getKey(compWithFeed3)).thenReturn("sub3");
		Mockito.when(componentTableModel.getKey(compWithFeed4)).thenReturn("sub4");
		Mockito.when(componentTableModel.getCellSettings("sub1")).thenReturn(settings1);
		Mockito.when(componentTableModel.getCellSettings("sub2")).thenReturn(settings2);
		Mockito.when(componentTableModel.getCellSettings("sub3")).thenReturn(settings3);
		Mockito.when(componentTableModel.getCellSettings("sub4")).thenReturn(settings4);

		controller = new TableControlPanelController(manifestation, labeledTable, componentTableModel);
		
		
		Mockito.when(labeledTable.getSelectedRows()).thenReturn(new int[] {0,1,2,3});
		Mockito.when(labeledTable.getSelectedColumns()).thenReturn(new int[] {0});
		Assert.assertFalse(controller.selectedCellsHaveMixedEnumerations());
		
		// Test false
		Mockito.when(componentTableModel.getStoredValueAt(3, 0)).thenReturn(compWithFeed4);
		Assert.assertFalse(controller.selectedCellsHaveMixedEnumerations());
		
		// Test true/mixed enums case
		Mockito.when(componentTableModel.getStoredValueAt(1, 0)).thenReturn(compWithFeed2);
		Assert.assertTrue(controller.selectedCellsHaveMixedEnumerations());
	}

}
