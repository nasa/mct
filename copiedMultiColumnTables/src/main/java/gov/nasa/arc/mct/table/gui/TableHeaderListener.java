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

import gov.nasa.arc.mct.table.utils.MouseInputHelper;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a listener for clicks within a table header row.
 * Clicks are interpreted as selections of an entire column.
 * The selection in a corresponding list of row headers is
 * cleared whenever there is a click in a table column
 * header. 
 */
public class TableHeaderListener extends MouseInputHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(TableHeaderListener.class);
	
	/** The table in whose header we detect clicks. */
	private JTable table;
	
	/** The corresponding list of row headers. */
	private JList rowHeaders;

	/**
	 * Creates a new listener for the given table and row
	 * header list.
	 * 
	 * @param table the table in whose header row we receive clicks
	 * @param rowHeaders the corresponding list of row headers
	 */
	public TableHeaderListener(JTable table, JList rowHeaders) {
		this.table = table;
		this.rowHeaders = rowHeaders;
	}
	
	@Override
	public void mouseLeftClicked(MouseEvent e) {
		clearRowSelection();
		int col = findColumnForEvent(e);
		if (col >= 0) {
			try { 
				table.setColumnSelectionInterval(col, col);
				table.setRowSelectionInterval(0, table.getRowCount()-1);
			} catch (Exception ex) {
				logger.warn("MouseLeftClickedEvent Exception: {0}", ex);
			}
		}
	}

	@Override
	public void mouseLeftClickedWithShift(MouseEvent e) {
		clearRowSelection();
		int col = findColumnForEvent(e);
		if (col >= 0) {
			try {
				table.addColumnSelectionInterval(table.getSelectedColumn(), col);
				table.setRowSelectionInterval(0, table.getRowCount()-1);
			} catch (Exception ex) {
				logger.warn("MouseLeftClickedWithShiftEvent Exception: {0}", ex);
			}
			
		}
	}
	
	/**
	 * Clears any row selections in the row headers list, since only columns
	 * should be selected following a click in a column heaader.
	 * 
	 * @param rowHeaders
	 */
	private void clearRowSelection() {
		// Unselect all row headers.
		rowHeaders.getSelectionModel().setAnchorSelectionIndex(0);
		rowHeaders.clearSelection();
	}

	/**
	 * Determines the column index corresponding to the position in the
	 * mouse event.
	 * 
	 * @param e the mouse event
	 * @return the column in which the mouse event occurred, or -1 if the event did
	 *   not occur in any column header
	 */
	private int findColumnForEvent(MouseEvent e) {
		Point p = e.getPoint();
		JTableHeader header = table.getTableHeader();
		for (int col=0; col<table.getColumnCount(); ++col) {
			if (header.getHeaderRect(col).contains(p)) {
				return col;
			}
		}
		
		return -1;
	}

}
