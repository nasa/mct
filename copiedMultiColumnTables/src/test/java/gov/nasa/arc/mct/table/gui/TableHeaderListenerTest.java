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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Event;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TableHeaderListenerTest {

	private static final int TABLE_ROWS = 2;
	private static final int TABLE_COLUMNS = 3;

	@Mock private MouseEvent e;

	@Mock private JTable table;
	@Mock private JTableHeader header;
	@Mock private JList rowHeaders;
	@Mock private ListSelectionModel rowHeaderSelectionModel;
	private TableHeaderListener listener;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(table.getTableHeader()).thenReturn(header);
		when(table.getColumnCount()).thenReturn(TABLE_COLUMNS);
		when(table.getRowCount()).thenReturn(TABLE_ROWS);
		when(rowHeaders.getSelectionModel()).thenReturn(rowHeaderSelectionModel);
		
		listener = new TableHeaderListener(table, rowHeaders);
	}

	@Test
	public void testColumnHeaderClick() {
		// Set a row header list selection so we can ensure that
		// the listener clears it.
		rowHeaders.setSelectionInterval(1, 1);

		// Click in the middle of the column 2 header.
		Rectangle r = new Rectangle(0, 0, 100, 100);
		Point[] columnHeaderCenters = new Point[TABLE_COLUMNS];
		for (int i=0; i<TABLE_COLUMNS; ++i) {
			when(header.getHeaderRect(i)).thenReturn((Rectangle) r.clone());
			columnHeaderCenters[i] = new Point((int) r.getCenterX(), (int) r.getCenterY());
			r.x += r.width;
		}

		when(e.getButton()).thenReturn(MouseEvent.BUTTON1);
		when(e.getClickCount()).thenReturn(1);
		when(e.getPoint()).thenReturn(columnHeaderCenters[1]);
		listener.mouseClicked(e);

		// We should have the entire column selected, but no selection
		// in the row header list.
		verify(table).setColumnSelectionInterval(1, 1);
		verify(table).setRowSelectionInterval(0, TABLE_ROWS-1);
		verify(rowHeaders).clearSelection();
		
		// Now test a shift-click.
		when(e.getModifiers()).thenReturn(Event.SHIFT_MASK);
		when(e.getPoint()).thenReturn(columnHeaderCenters[2]);
		when(table.getSelectedColumn()).thenReturn(1);
		listener.mouseClicked(e);
		
		verify(table).addColumnSelectionInterval(1, 2);
	}

}
