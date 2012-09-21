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

import gov.nasa.arc.mct.table.utils.ListenerManager;
import gov.nasa.arc.mct.table.utils.ListenerNotifier;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.table.AbstractTableModel;

/**
 * Implements a table model where each cell has a unique identifier that
 * is used along with a labeling algorithm to determine row and column
 * labels.
 */
@SuppressWarnings("serial")
public abstract class LabeledTableModel extends AbstractTableModel {

	private TableLabelingAlgorithm algorithm;
	private TableOrientation orientation;

	/** Table row labels. */
	protected String[] rowLabels;
	
	/** Table column labels. */
	protected String[] columnLabels;
	
	/** Table cell labels. */
	protected String[][] cellLabels;
	
	/** A model for the row labels. */
	LabelListModel rowLabelModel;
	
	/** A model for the column labels. */
	LabelListModel columnLabelModel;
	
	/** A manager for listeners. */
	private ListenerManager listenerManager = new ListenerManager();

	/**
	 * Creates an instance of the table based on the given labeling
	 * algorithm. The labels are not updated immediately upon construction,
	 * since derived classes may not be fully constructed. Instead, the
	 * user should call xyz to update the labels before using the table
	 * model. Subsequent changes to the table model will cause automatic
	 * updating of the labels.
	 * 
	 * @param algorithm the table labeling algorithm
	 * @param orientation the table orientation
	 */
	public LabeledTableModel(TableLabelingAlgorithm algorithm, TableOrientation orientation) {
		this.algorithm = algorithm;
		this.orientation = orientation;
		algorithm.setOrientation(orientation);
		
		rowLabelModel = new LabelListModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Object getElementAt(int index) {
				return getRowName(index);
			}

			@Override
			public int getSize() {
				return getRowCount();
			}
			
		};
		
		columnLabelModel = new LabelListModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Object getElementAt(int index) {
				return getColumnName(index);
			}

			@Override
			public int getSize() {
				return getColumnCount();
			}
			
		};
	}
	
	/**
	 * Adds a listener for events fired when the labels have been updated.
	 * 
	 * @param listener the label change listener.
	 */
	public void addLabelChangeListener(LabelChangeListener listener) {
		listenerManager.addListener(LabelChangeListener.class, listener);
	}
	
	/**
	 * Removes the listener for events.
	 * @param listener the label change listener.
	 */
	public void removeLabelChangeListener(LabelChangeListener listener) {
		listenerManager.removeListener(LabelChangeListener.class, listener);
	}
	
	/**
	 * Gets the table type.
	 * @return 2-D TableType 
	 */
	/*public TableType getTableType() {
		return TableType.TWO_DIMENSIONAL;
	}*/

	/**
	 * Get the number of components represented by the table.
	 * 
	 * @return the number of components.
	 */
	protected abstract int getComponentCount();
	
	/**
	 * Gets the number of attributes for each object in the table.
	 * Not all objects may have values for each of the attributes.
	 * 
	 * @return the number of attributes.
	 */
	protected abstract int getAttributeCount();
	
	/**
	 * Gets the object at the given row and column. The representation on the
	 * screen may be at the transposed position, depending on the table orientation.
	 * 
	 * @param rowIndex the row at which to get the object.
	 * @param columnIndex the column at which to get the object.
	 * @return the object at that row and column.
	 */
	protected abstract Object getObjectAt(int rowIndex, int columnIndex);
	
	/**
	 * Gets the identifier for the object at a given position. The identifier
	 * is used by the labeling algorithm to compute row, column, and cell
	 * labels.
	 * 
	 * @param rowIndex the row at which to get the object identifier
	 * @return the object identifier at that row and column
	 */
	protected abstract String getObjectIdentifierAt(int rowIndex);
	
	@Override
	public final int getRowCount() {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return getComponentCount();
		} else {
			return getAttributeCount();
		}
	}

	@Override
	public final int getColumnCount() {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return getAttributeCount();
		} else {
			return getComponentCount();
		}
	}
	
	@Override
	public final Object getValueAt(int rowIndex, int columnIndex) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return getObjectAt(rowIndex, columnIndex);			
		} else {
			return getObjectAt(columnIndex, rowIndex);			
		}
	}
	
	/**
	 * Gets the stored value at specific row and column indices.
	 * @param rowIndex the row index.
	 * @param columnIndex the column index.
	 * @return the stored object value.
	 */
	public final Object getStoredValueAt(int rowIndex, int columnIndex) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return getStoredObjectAt(rowIndex, columnIndex);			
		} else {
			return getStoredObjectAt(columnIndex, rowIndex);			
		}
	}
	
	/**
	 * Gets the stored object at specific object and attribute indices.
	 * @param objectIndex the object index.
	 * @param attributeIndex the attribute index.
	 * @return the stored object.
	 */
	protected abstract Object getStoredObjectAt(int objectIndex, int attributeIndex);
	
	/**
	 * Gets the current table orientation.
	 * 
	 * @return the table orientation.
	 */
	public TableOrientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Sets the table orientation. When the orientation is changed,
	 * the labels are recalculated, and a structure change event
	 * is fired so the entire table redraws. If the new orientation
	 * is the same as the old orientation, does nothing.
	 * 
	 * @param newOrientation the new table orientation.
	 */
	public void setOrientation(TableOrientation newOrientation) {
		algorithm.setOrientation(newOrientation);
		if (newOrientation != orientation) {
			orientation = newOrientation;
			updateLabels();
			fireTableStructureChanged();
		}
	}
	
	/**
	 * Handles an event where the row, column, or cell labels have changed.
	 * Triggers a structure changed event so that the table will be redrawn
	 * completely.
	 */
	public void fireLabelsChanged() {
		rowLabelModel.fireLabelsChanged();
		columnLabelModel.fireLabelsChanged();
		listenerManager.fireEvent(LabelChangeListener.class, new ListenerNotifier<LabelChangeListener>() {
			@Override
			public void notifyEvent(LabelChangeListener listener) {
				listener.labelsChanged();
			}
		});
	}

	@Override
	public void fireTableCellUpdated(int row, int column) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			super.fireTableCellUpdated(row, column);
		} else {
			super.fireTableCellUpdated(column, row);
		}
	}

	/**
	 * Recalculates all row, column, and cell labels. This method is executed
	 * automatically when the table model changes. However, the user can
	 * call this method at other times, if desired.
	 */
	public void updateLabels() {
		rowLabels = new String[getRowCount()];
		columnLabels = new String[getColumnCount()];
		cellLabels = new String[getRowCount()][getColumnCount()];
		
		algorithm.computeLabels(this);
		rowLabelModel.fireLabelsChanged();
		columnLabelModel.fireLabelsChanged();
	}
	
	/**
	 * Tests whether there are row labels. If any row label is nonempty,
	 * then we have row labels.
	 * 
	 * @return true, if at least one row label is nonempty
	 */
	public boolean hasRowLabels() {
		if (rowLabels == null) {
			return true; // Don't yet have labels.
		}
		
		for (String label : rowLabels) {
			if (!label.isEmpty()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Tests whether there are column labels. If any column label is nonempty,
	 * then we have column labels.
	 * 
	 * @return true, if at least one column label is nonempty
	 */
	public boolean hasColumnLabels() {
		if (columnLabels == null) {
			return true; // Don't yet have defined labels.
		}
		
		for (String label : columnLabels) {
			if (!label.isEmpty()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets a row label, possibly abbreviated.
	 * 
	 * @param rowIndex the index of the row for which to get the label
	 * @return the row label
	 */
	public String getRowName(int rowIndex) {
		return getFullRowName(rowIndex);
	}
	
	/**
	 * Gets the unabbrevaited label for a row.
	 * 
	 * @param rowIndex the index of the row for which to get the label
	 * @return the row label
	 */
	public String getFullRowName(int rowIndex) {
		if (rowLabels==null || rowIndex >= rowLabels.length) {
			return ""; // Must not have updated labels yet.
		} else {
			return rowLabels[rowIndex];
		}
	}
	
	/**
	 * Sets a row label. This is designed to be called by the labeling
	 * algorithm.
	 * 
	 * @param rowIndex the index of the row label to set
	 * @param label the new row label
	 */
	void setRowName(int rowIndex, String label) {
		rowLabels[rowIndex] = label;
	}
	
	/**
	 * Gets a column label, possibly abbreviated.
	 * 
	 * @param columnIndex the index of the column for which to get the label
	 * @return the column label
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return getFullColumnName(columnIndex);
	}
	
	/**
	 * Gets the unabbreviated label for a column.
	 * 
	 * @param columnIndex the index of the column for which to get the label
	 * @return the column label
	 */
	public String getFullColumnName(int columnIndex) {
		if (columnLabels==null || columnIndex >= columnLabels.length) {
			return ""; // Must not have updated labels yet.
		} else {
			return columnLabels[columnIndex];
		}
	}
	
	/**
	 * Sets a column label. This is designed to be called by the labeling
	 * algorithm.
	 * 
	 * @param columnIndex the index of the column label to set
	 * @param label the new column label
	 */
	void setColumnName(int columnIndex, String label) {
		columnLabels[columnIndex] = label;
	}
	
	/**
	 * Gets a cell label, possibly abbreviated.
	 * 
	 * @param rowIndex the index of the row containing the cell
	 * @param columnIndex the index of the column containing the cell
	 * @return the cell label
	 */
	public String getCellName(int rowIndex, int columnIndex) {
		return getFullCellName(rowIndex, columnIndex);
	}

	/**
	 * Gets the unabbreviated label for a cell.
	 * 
	 * @param rowIndex the index of the row containing the cell
	 * @param columnIndex the index of the column containing the cell
	 * @return the cell label
	 */
	public final String getFullCellName(int rowIndex, int columnIndex) {
		if (rowIndex >= cellLabels.length || columnIndex >= cellLabels[rowIndex].length) {
			return "";
		} else {
			return cellLabels[rowIndex][columnIndex];
		}
	}

	/**
	 * Gets a label for an object in a cell. This method takes into consideration
	 * the orientation of the table.
	 * 
	 * @param objectIndex the index of the object within the table
	 * @param attributeIndex the index of the attribute object
	 * @return the label for the object
	 */
	String getObjectName(int objectIndex, int attributeIndex) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return getCellName(objectIndex, attributeIndex);
		} else {
			return getCellName(attributeIndex, objectIndex);
		}
	}
	
	/**
	 * Sets a cell label. This is designed to be called by the labeling
	 * algorithm.
	 * 
	 * @param rowIndex the index of the row containing the cell
	 * @param columnIndex the index of the column containing the cell
	 * @param label the new cell label
	 */
	void setCellName(int rowIndex, int columnIndex, String label) {
		cellLabels[rowIndex][columnIndex] = label;
	}

	/**
	 * Gets a list model for the row labels.
	 * 
	 * @return the list model
	 */
	public ListModel getRowLabelModel() {
		return rowLabelModel;
	}

	/**
	 * Gets a list model for the column labels.
	 * 
	 * @return the list model
	 */
	public ListModel getColumnLabelModel() {
		return columnLabelModel;
	}
	
	/**
	 * Gets the unique identifier for the data in the indicated cell.
	 * The unique identifier is often used with a table labeling
	 * algorithm to determine abbreviated row, column, and cell
	 * labels.
	 * 
	 * @param rowIndex the row containing the cell
	 * @param columnIndex the column containing the cell
	 * @return the unique identifier for the cell
	 */
	public final String getIdentifierAt(int rowIndex, int columnIndex) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return getObjectIdentifierAt(rowIndex);			
		} else {
			return getObjectIdentifierAt(columnIndex);			
		}
	}
	
	/**
	 * Tests whether a value can be placed into a position within a table. The
	 * new position may cause a row or column to be inserted.
	 * 
	 * @param rowIndex the row at which to place the new item
	 * @param columnIndex the column at which to place the new item
	 * @param isInsertRow true, if a new row should be inserted above the position
	 * @param isInsertColumn true, if a new column should be inserted to the left of the position
	 * @return true, if a value can be placed or inserted at the position
	 */
	public final boolean canSetValueAt(int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			return canSetObjectAt(rowIndex, columnIndex, isInsertRow, isInsertColumn);			
		} else {
			return canSetObjectAt(columnIndex, rowIndex, isInsertColumn, isInsertRow);
		}
	}
	
	
	/**
	 * Checks whether object can be set at specific row and column indices along with isInsertRow and isInsertColumn boolean flags.
	 * @param rowIndex the row index.
	 * @param columnIndex the column index.
	 * @param isInsertRow boolean flag to check for whether can insert row.
	 * @param isInsertColumn boolean flag to check for whether can insert column.
	 * @return boolean flag to check whether object can be set.
	 */
	protected abstract boolean canSetObjectAt(int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn);
	
	/**
	 * Implements a list model with an additional method that makes it
	 * easy to fire an event when the list contents have changed.
	 */
	private static abstract class LabelListModel extends AbstractListModel {
		
		private static final long serialVersionUID = 1L;

		/**
		 * Notify all listeners that the list contents have changed.
		 */
		protected void fireLabelsChanged() {
			fireContentsChanged(this, 0, getSize());
		}

	}

	/**
	 * Sets the object value at specific row and column indices along with isInsertRow and isInsertColumn boolean flags.
	 * @param aValue the object value.
	 * @param rowIndex the row index.
	 * @param columnIndex the column index.
	 * @param isInsertRow boolean flag to check for whether can insert row.
	 * @param isInsertColumn boolean flag to check for whether can insert column.
	 */
	/*public final void setValueAt(Object aValue, int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn) {
		if (orientation == TableOrientation.ROW_MAJOR) {
			setObjectAt(aValue, rowIndex, columnIndex, isInsertRow, isInsertColumn);			
		} else {
			setObjectAt(aValue, columnIndex, rowIndex, isInsertColumn, isInsertRow);			
		}
		updateLabels();
	}*/
	
	//i have commented out setValueAt() and setObjectAt() because TableStructure's setValue() have
	//been omitted because TS's setValue() is crazy and it might not be applicable and i don't
	//understand them. 

	/**
	 * Sets the object at the given row and column. The representation on the
	 * screen may be at the transposed position, depending on the table orientation.
	 * 
	 * @param value the new object at that row and column.
	 * @param rowIndex the row at which to set the object.
	 * @param columnIndex the column at which to set the object.
	 * @param isInsertRow boolean flag to check for whether can insert row.
	 * @param isInsertColumn boolean flag to check for whether can insert column.
	 */
	//protected abstract void setObjectAt(Object value, int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn);
	
	/**
	 * Tests whether the table is a skeleton, ready for values to be
	 * dropped in to flesh out the table cells and columns. A skeleton
	 * table needs to have rows and column labels displayed even though
	 * the labels will be empty.
	 * 
	 * @return true, if the table is a skeleton
	 */
	public abstract boolean isSkeleton();
	
	
	
}
