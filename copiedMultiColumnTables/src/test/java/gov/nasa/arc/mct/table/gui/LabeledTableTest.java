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
package gov.nasa.arc.mct.table.gui;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.*;
import gov.nasa.arc.mct.table.model.AbbreviatingTableLabelingAlgorithm;
import gov.nasa.arc.mct.table.model.MockTableModel;
import gov.nasa.arc.mct.table.model.TableLabelingAlgorithm;
import gov.nasa.arc.mct.test.util.TestUtil;

import javax.swing.table.TableColumn;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LabeledTableTest {

	@Mock private TableLayoutListener layoutListener;
	@Mock private TableSelectionListener selectionListener;
	
	MockTableModel model;
	TableLabelingAlgorithm algorithm;
	LabeledTable table;
	
	private final static String[][] identifiers = {
			{ "Ku_VBSP_Ch_1_Mode_Select", "Ku_VBSP_Ch_1_Field_Rate", "Ku_VBSP_Ch_1_Reserved_Bit",
				"Ku_VBSP_Ch_1_Fiber_Optic_Activity", "Ku_VBSP_Ch_1_PFM_Activity", "Ku_VBSP_Ch_1_Return_Link_Sync_Lock" },
			{ "Ku_VBSP_Ch_2_Mode_Select", "Ku_VBSP_Ch_2_Field_Rate", "Ku_VBSP_Ch_2_Reserved_Bit",
				"Ku_VBSP_Ch_2_Fiber_Optic_Activity", "Ku_VBSP_Ch_2_PFM_Activity", "Ku_VBSP_Ch_2_Return_Link_Sync_Lock" },
			{ "Ku_VBSP_Ch_3_Mode_Select", "Ku_VBSP_Ch_3_Field_Rate", "Ku_VBSP_Ch_3_Reserved_Bit",
				"Ku_VBSP_Ch_3_Fiber_Optic_Activity", "Ku_VBSP_Ch_3_PFM_Activity", "Ku_VBSP_Ch_3_Return_Link_Sync_Lock" },
			{ "Ku_VBSP_Ch_4_Mode_Select", "Ku_VBSP_Ch_4_Field_Rate", "Ku_VBSP_Ch_4_Reserved_Bit",
				"Ku_VBSP_Ch_4_Fiber_Optic_Activity", "Ku_VBSP_Ch_4_PFM_Activity", "Ku_VBSP_Ch_4_Return_Link_Sync_Lock" },
	};

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		algorithm = new AbbreviatingTableLabelingAlgorithm();
		model = new MockTableModel(identifiers, algorithm);
		table = new LabeledTable(model);
	}
	
	@Test
	public void testLayoutListeners() {
		// Firing without a listener causes no interactions with the listener.
		table.fireTableLayoutChanged();
		verifyZeroInteractions(layoutListener);
		
		// Adding a listener and firing results in exactly one call.
		table.addTableLayoutListener(layoutListener);
		table.fireTableLayoutChanged();
		verify(layoutListener, times(1)).tableChanged(anyObject());
		
		// Adding a 2nd time and firing only causes one more call. (Proving it's
		// not added twice.)
		table.addTableLayoutListener(layoutListener);
		table.fireTableLayoutChanged();
		verify(layoutListener, times(2)).tableChanged(anyObject());
		
		// Removing the listener and firing does not result in another call.
		table.removeTableLayoutListener(layoutListener);
		table.fireTableLayoutChanged();
		verify(layoutListener, times(2)).tableChanged(anyObject());
	}
	
	@Test
	public void testSelectionChangeListeners() {
		// Firing without a listener causes no interactions with the listener.
		table.fireSelectionChanged();
		verifyZeroInteractions(selectionListener);
		
		// Adding a listener and firing results in exactly one call.
		table.addTableSelectionListener(selectionListener);
		table.fireSelectionChanged();
		verify(selectionListener, times(1)).selectionChanged(new int[0], new int[0]);
		
		// Adding a 2nd time and firing only causes one more call. (Proving it's
		// not added twice.)
		table.addTableSelectionListener(selectionListener);
		table.fireSelectionChanged();
		verify(selectionListener, times(2)).selectionChanged(new int[0], new int[0]);
		
		// Removing the listener and firing does not result in another call.
		table.removeTableSelectionListener(selectionListener);
		table.fireSelectionChanged();
		verify(selectionListener, times(2)).selectionChanged(new int[0], new int[0]);
	}
	
	@Test
	public void testSelectRow() {
		MySelectionChangeListener l = new MySelectionChangeListener();
		
		assertEquals(table.getSelectedRows().length, 0);
		assertEquals(table.getSelectedColumns().length, 0);
		
		table.addTableSelectionListener(l);

		// Test selection of row headers.
		table.getRowHeaders().setSelectedIndices(new int[] {0});
		sleep(LabeledTable.SELECTION_CHANGE_DELAY + 50);
		TestUtil.assertArraysEqual(l.getSelectedRows(), new int[] {0});
		TestUtil.assertArraysEqual(l.getSelectedColumns(), new int[] {0, 1, 2, 3, 4, 5});
		
		// Test selection of a portion of the table. Since we just had entire rows
		// selectedd, we have to select the columns first, or it looks like a
		// shift-select in the row headers, followed by a click within the table,
		// which will clear the row selection. This way it looks like a click
		// on (1,3), followed by a shift-click on (2,3) (or vice versa).
		table.getTable().setColumnSelectionInterval(3, 3);
		table.getTable().setRowSelectionInterval(1, 2);
		sleep(LabeledTable.SELECTION_CHANGE_DELAY + 50);
		TestUtil.assertArraysEqual(l.getSelectedRows(), new int[] {1, 2});
		TestUtil.assertArraysEqual(l.getSelectedColumns(), new int[] {3});
	}
	
	@Test
	public void testTableStructureChanged() {
		table.addTableLayoutListener(layoutListener);
		// Move column 0 to position 1.
		table.getTable().getColumnModel().moveColumn(0, 1);
		sleep(LabeledTable.TABLE_SAVE_DELAY + 50);
		
		verify(layoutListener).tableChanged(anyObject());
	}
	
	@Test
	public void testGetColumnWidths() {
		int[] origWidths = table.getColumnWidths();
		TableColumn col0 = table.getTable().getColumnModel().getColumn(0);
		col0.setPreferredWidth(origWidths[0] * 2);
		int[] newWidths = table.getColumnWidths();
		for (int i=0; i<table.getTable().getColumnCount(); ++i) {
			if (i == 0) {
				assertEquals(newWidths[i], origWidths[i] * 2);
			} else {
				assertEquals(newWidths[i], origWidths[i]);
			}
		}
		
		table.setColumnWidths(origWidths);
		TestUtil.assertArraysEqual(table.getColumnWidths(), origWidths);
	}
	
	@Test
	public void testGetColumnOrder() {
		TestUtil.assertArraysEqual(table.getColumnOrder(), new int[] {0, 1, 2, 3, 4, 5});
		table.getTable().getColumnModel().moveColumn(0, 1);
		TestUtil.assertArraysEqual(table.getColumnOrder(), new int[] {1, 0, 2, 3, 4, 5});
		
		table.setColumnOrder(new int[] {5, 4, 3, 2, 1, 0});
		TestUtil.assertArraysEqual(table.getColumnOrder(), new int[] {5, 4, 3, 2, 1, 0});
	}
	
	@Test
	public void testShowGrid() {
		table.getTable().setShowGrid(true);
		assertTrue(table.getShowGrid());
		
		table.getTable().setShowGrid(false);
		assertFalse(table.getShowGrid());
		
		table.setShowGrid(true);
		assertTrue(table.getTable().getShowHorizontalLines());
		assertTrue(table.getTable().getShowVerticalLines());
		
		table.setShowGrid(false);
		assertFalse(table.getTable().getShowHorizontalLines());
		assertFalse(table.getTable().getShowVerticalLines());
	}
	
	@Test
	public void testRowHeaderHeights() {
		assertTrue(table.getTable().getRowCount() > 0);
		assertEquals(table.getTable().getRowCount(), table.getRowHeaders().getModel().getSize());
		
		/* Set row header heights to unique values */
		for (int i = 0; i < table.getTable().getRowCount(); i++) {
			table.getTable().setRowHeight(i, 4 + i * 4);
		}
		for (int i = 0; i < table.getTable().getRowCount(); i++) {
			assertEquals(table.getTable().getRowHeight(i), 4 * (i + 1));
			assertEquals(table.getTable().getRowHeight(i), 4 * (i + 1));
		}
		
		/* Set them to the same value */
		for (int i = 0; i < table.getTable().getRowCount(); i++) {
			table.getTable().setRowHeight(i, 16);
		}
		for (int i = 0; i < table.getTable().getRowCount(); i++) {
			assertEquals(table.getTable().getRowHeight(i), 16);
			assertEquals(table.getTable().getRowHeight(i), 16);
		}
	}
	
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	private static class MySelectionChangeListener implements TableSelectionListener {
		
		int[] selectedRows = {};
		int[] selectedColumns = {};

		@Override
		public void selectionChanged(int[] selectedRows, int[] selectedColumns) {
			this.selectedRows = selectedRows.clone();
			this.selectedColumns = selectedColumns.clone();
		}

		public int[] getSelectedRows() {
			return selectedRows;
		}

		public int[] getSelectedColumns() {
			return selectedColumns;
		}
		
	}
	
}
