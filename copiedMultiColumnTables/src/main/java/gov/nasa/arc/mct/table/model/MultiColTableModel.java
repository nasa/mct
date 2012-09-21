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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.Placeholder;
import gov.nasa.arc.mct.evaluator.api.Evaluator;
import gov.nasa.arc.mct.table.policy.TableViewPolicy;
import gov.nasa.arc.mct.table.utils.NoSizeList;
import gov.nasa.arc.mct.table.view.DisplayedValue;
import gov.nasa.arc.mct.table.view.LabelAbbreviations;
import gov.nasa.arc.mct.table.view.MultiColView;
import gov.nasa.arc.mct.table.view.TableCellSettings;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiColTableModel extends LabeledTableModel {
	//do i need a serialVersionUID?
	//do i want to revise cellLabelAbbreviations?+++ not per component id?
	private Map<Point, TableCellSettings> cellSettings;
	private Map<String, LabelAbbreviations> cellLabelAbbreviations = new HashMap<String, LabelAbbreviations>();	
	private NoSizeList<LabelAbbreviations> rowLabelAbbreviations = new NoSizeList<LabelAbbreviations>();
	private NoSizeList<LabelAbbreviations> columnLabelAbbreviations = new NoSizeList<LabelAbbreviations>();
	/** The maximum number of decimals for each column, if known. */
	private NoSizeList<Integer> maxDecimalsForColumn = new NoSizeList<Integer>();
	
	private static final Logger logger = LoggerFactory.getLogger(MultiColTableModel.class);
	private AbstractComponent rootComponent; 
	private int columnCount = 0;
	private ArrayList<TableRow> rowList;//TODO: refactor/rename to componentList.

	
	private MultiColView viewManifestation;
	
	public MultiColTableModel(TableLabelingAlgorithm algorithm, MultiColView viewManifestation) {
		super(algorithm, TableOrientation.ROW_MAJOR);
		this.viewManifestation = viewManifestation;
		rootComponent = viewManifestation.getManifestedComponent();
		buildTable();
		updateColumnCount();
		cellSettings = new HashMap<Point, TableCellSettings>();
	}
	
	private void buildTable () {
		rowList = new ArrayList<TableRow>();
		List<AbstractComponent> children = rootComponent.getComponents();
		//if it has no children:
		if(children==Collections.<AbstractComponent>emptyList()) {
			rowList.add(new TableRow(0,rootComponent));
		} else {
			int rowIndex = 0;
			for(AbstractComponent child : children) {
				if(canEmbedInTable(child)) {
					rowList.add(new TableRow(rowIndex,child));
				}
				rowIndex++;
			}
		}
	}
	
	/**
	 * Determine if this component is displayable as a cell within a table.
	 * @param comp the abstract component.
	 * @return true if the component can be displayed in a table, false if it should be skipped.
	 */
	private boolean canEmbedInTable(AbstractComponent comp) {
		return TableViewPolicy.canEmbedInTable(comp);
	}
	
	/**
	 * Gets the number of components ("objects") in the tabular structure.
	 * 
	 * @return the number of rows
	 */
	//this name is temp. later rename 'row' and 'column' to be orientation-independent.+++ //?
	@Override
	public int getComponentCount() {
		return Math.max(1, rowList.size()); //?+++
	}
	
	/**
	 * Gets the number of attributes for each object in the table.
	 * Not all objects may have values for each of the attributes.
	 * 
	 * @return the number of attributes.
	 */
	@Override
	public int getAttributeCount() {
		return rowList.size();
	}
	
	//TODO: rename to something clearer, perhaps getColumnCount()
	public int updateColumnCount() {
		columnCount = 3; //mockup. later, determined by settings. 
		return columnCount;
	}
	
	//was getValue() in TableStructure
	public AbstractComponent getComponent(int rowIndex) {
		try {
			if (rowIndex < 0 || rowIndex >= getRowCount()) {
				logger.warn("Row index out of bounds, " + rowIndex + " in range 0 to " + (getRowCount() - 1));
			}
			if(rowList.size() > 0) {
				return rowList.get(rowIndex).getComponent();
			} else {
				return null;
			}
		} //this catch was copied from tablestructure's getValue and might be useless
		catch (IndexOutOfBoundsException outOfBoundsException) {
			logger.warn("IndexOutOfBoundsException: {0}", outOfBoundsException);
			return null; 
		}
	}

	/**
	 * Gets the object at the given row and column. The representation on the
	 * screen may be at the transposed position, depending on the table orientation.
	 * 
	 * @param rowIndex the row at which to get the object.
	 * @param columnIndex the column at which to get the object.
	 * @return the object at that row and column.
	 */
	@Override
	public Object getObjectAt(int rowIndex, int columnIndex) {
		if (getOrientation() == TableOrientation.ROW_MAJOR) {
			return getStoredObjectAt(rowIndex,columnIndex);
		} else {
			return getStoredObjectAt(columnIndex,rowIndex);
		}
	}
	
	/**
	 * Gets the stored object at specific object and attribute indices.
	 * Distinct from table display orientation.
	 * @param objectIndex the object index.
	 * @param attributeIndex the attribute index.
	 * @return the stored object.
	 */
	@Override
	public Object getStoredObjectAt(int componentIndex, int attributeIndex) {
		AbstractComponent component = getComponent(attributeIndex);
		
		if (component == null) {
			return null;
		} else if  (component == AbstractComponent.NULL_COMPONENT) {
			return "";
		} else {
			String cellName = getObjectName(componentIndex, attributeIndex);

			Object value = getValueForComponent(component, attributeIndex);
			if (value instanceof DisplayedValue) {
				DisplayedValue dv = (DisplayedValue) value;
				TableCellSettings cellSettings = getCellSettings(componentIndex,attributeIndex);
				dv.setLabel(cellName!=null ? cellName : "");
				dv.setAlignment(cellSettings.getAlignment());
				dv.setNumberOfDecimals(cellSettings.getNumberOfDecimals());
			}
			return value;
		}
	}
	
	//TODO: TS's setValue() has been omitted for now. 
	
	/**
	 * Tests whether a value can be set at a position. We can only set values
	 * where existing values already exist, or where they are within the bounds
	 * of the matrix and would extend a row or column. That is, we cannot allow gaps to
	 * exist within an object.
	 * 
	 * @param rowIndex the row index at which to set the value
	 * @param columnIndex the column index at which to set the value
	 * @param isInsertRow true, if the value should be inserted in a new row prior to the indicated row
	 * @param isInsertColumn true, if the value should be inserted in a new column prior to the indicated row
	 * @return true, if the value can be set at the position
	 */
	@Override
	public boolean canSetObjectAt(int rowIndex, int columnIndex, boolean isInsertRow, boolean isInsertColumn) {
		if (isInsertRow && isInsertColumn) {
			return false;
		}
		if (rowIndex < 0 || columnIndex < 0) {
			return false;
		}
		if (rowIndex > rowList.size() || columnIndex > columnCount) {
			return false;
		}
		//We have to ensure there is an existing value at the position, or
		// that the position is within the bounds of the matrix and just past the last value.
		
		// Special case of an empty table. We can only insert a row into 0, 0.
		if (rowList.size() == 0) {
			return rowIndex==0 && columnIndex==0 && isInsertRow;
		}
		
		// Can only insert rows at column zero.
		if (isInsertRow) {
			return columnIndex==0 && 0<=rowIndex && rowIndex <= getRowCount();
		}
		
		// Otherwise must set value in an existing row.
		if (rowIndex < 0 || rowIndex >= getRowCount()) {
			return false;
		}

		TableRow row = rowList.get(rowIndex);
		if (isInsertColumn) {
			return 0<=columnIndex && columnIndex <= 3; //later, this number 3 will come from settings about which columns are active
		} else {
			return 0<=columnIndex && columnIndex <= 3 && columnIndex < columnCount; //later, this number 3 will come from settings 
		}
	}
	
	
	
	//TODO:  put this mockup code somewhere sensible
	public Object getValueForComponent(AbstractComponent component, int colIndex) {
		//mockup of asking settings what should be displayed in that column:
		Object value;
		switch(colIndex) {
		case 0:
			//let's pretend settings call for the display name in col 0
			value = component.getDisplayName(); break;
		case 1:
			//settings call for creation date
			value = component.getCreationDate(); break;
		case 2:
			//owner
			value = component.getOwner(); break;
		default:
			value = null; //this is pretty mockup-y i think
		}
		if (value == null) {
			DisplayedValue displayedValue = new DisplayedValue();
			if (component.getCapability(Placeholder.class) != null) {
			    displayedValue.setValue(component.getCapability(Placeholder.class).getPlaceholderValue());
			} else {
				displayedValue.setValue(component.getDisplayName());
			}
			return displayedValue;
		} else {
			return value;
		}
	}
	
	/**
	 * Gets the cell settings for a particular location.
	 * 
	 * @return the cell settings, or a new set of default settings if no settings have been stored
	 */
	public TableCellSettings getCellSettings(int rowIndex, int colIndex) {
		TableCellSettings settings = cellSettings.get(new Point(rowIndex,colIndex));
		if (settings == null) {
			settings = new TableCellSettings();
			cellSettings.put(new Point(rowIndex,colIndex), settings);
		}
		return settings;
	}
	
	//this method may or may not need to be specialized by column
	@Override
	protected String getObjectIdentifierAt(int rowIndex) {
		AbstractComponent component = getComponent(rowIndex);
		if (component == null) {
			return "";
		} else if  (component == AbstractComponent.NULL_COMPONENT) {
			return "\u00A0";
		} else {
			return getCanonicalName(component);
		}
	}
	
	/**
	 * Gets a complete identifier labeling the component. These <em>canonical
	 * names</em> for each table cell are used in the table labeling algorithm
	 * to calculate labels for columns, rows, and cells. The canonical name
	 * is the canonical name of the feed provider, if it exists and is not
	 * empty. Otherwise it is the display name for the component.
	 * 
	 * @param component the component for which we need the canonical name
	 * @return the canonical name for the component
	 */
	private String getCanonicalName(AbstractComponent component) {
		String canonicalName = null;
		
		// Try to get the canonical name for the feed provider.
		FeedProvider feedProvider = component.getCapability(FeedProvider.class);
		if (feedProvider != null) {
			canonicalName = feedProvider.getCanonicalName();
		}
		
		// If the feed provider doesn't have a canonical name, use the component display name.
		if (canonicalName==null || canonicalName.isEmpty()) {
			canonicalName = component.getDisplayName();
		}
		
		return canonicalName;
	}
	
	/**
	 * Returns a unique key for a given component. This key is used by the
	 * code that responds to a feed update to pass along changes to the
	 * component's value.
	 * 
	 * @param component the component for which to determine the key
	 * @return a unique key for the component
	 */
	public String getKey(AbstractComponent component) {
		AbstractComponent delegate = component;
		Evaluator e = component.getCapability(Evaluator.class);
		if (e != null && component.getComponents().size() > 1) {
			return component.getComponentId();
		}
		FeedProvider fp = component.getCapability(FeedProvider.class);
		if (fp != null) {
			return fp.getSubscriptionId();
		}
		
		return delegate.getComponentId();
	}
	
	//right now i don't store the value that's displayed in a cell in Map values 
	//like tableViews did. It's just stored in component as normal. Not sure if 
	//will need to change this+++
	//needs to be orientation-robust+++
	/**
	 * Sets the value of a component updated by a data feed. This change
	 * is propagated to every table cell in that component's row.
	 * 
	 * @param id the identifier for the object updated
	 */
	public void setValue(String id) {
		for(TableRow row : rowList) {
			if(getKey(row.getComponent()).equals(id)) {
				for(int c=0; c<3; c++) {//later the 3 will come from settings++++
					fireTableCellUpdated(row.getIndex(), c);
				}
			}
		}
	}
	
	/**
	 * Gets the row label abbreviations for a specified row.
	 *  
	 * @param row the row to retrieve abbreviations for
	 * @return the abbreviations
	 */
	public LabelAbbreviations getRowLabelAbbreviations(int row) {
		LabelAbbreviations abbrevs = rowLabelAbbreviations.get(row);
		if (abbrevs != null) {
			return abbrevs;
		} else {
			return new LabelAbbreviations();
		}
	}
	
	/**
	 * Sets the row label abbreviations for a specified row.
	 *  
	 * @param rowIndex the row to set abbreviations for
	 * @param abbreviations the new abbreviations
	 */
	public void setRowLabelAbbreviations(int rowIndex, LabelAbbreviations abbreviations) {
		rowLabelAbbreviations.set(rowIndex, abbreviations);
		fireLabelsChanged();
	}
	
	/**
	 * Gets column label abbreviations for a specified column.
	 *  
	 * @param column the column index to retrieve abbreviations for
	 * @return the abbreviations
	 */
	public LabelAbbreviations getColumnLabelAbbreviations(int column) {
		LabelAbbreviations abbrevs = columnLabelAbbreviations.get(column);
		if (abbrevs != null) {
			return abbrevs;
		} else {
			return new LabelAbbreviations();
		}
	}
	
	/**
	 * Sets the column label abbreviations for a specified column.
	 *  
	 * @param columnIndex the column to set abbreviations for
	 * @param abbreviations the new abbreviations
	 */
	public void setColumnLabelAbbreviations(int columnIndex, LabelAbbreviations abbreviations) {
		columnLabelAbbreviations.set(columnIndex, abbreviations);
		fireLabelsChanged();
	}
	
	//I'm putting this in but i'm not certain it's a good idea+++
	/**
	 * Indicates that the labels of the rows or columns have changed.
	 */
	public void fireHeaderLabelsChanged() {
		fireTableStructureChanged();
	}
	
	/**
	 * Gets the cell label abbreviations for a specific feed provider ID.
	 *  
	 * @param id the feed provider ID (a PUI, if using ISP)
	 * @return the cell label abbreviations
	 */
	public LabelAbbreviations getCellLabelAbbreviations(String id) {
		LabelAbbreviations abbrevs = cellLabelAbbreviations.get(id);
		if (abbrevs == null) {
			abbrevs = new LabelAbbreviations();
			cellLabelAbbreviations.put(id, abbrevs);
		}
		return abbrevs;
	}
	
	/**
	 * Sets the cell label abbreviations for a specific feed provider ID.
	 *  
	 * @param id the feed provider ID (a PUI, if using ISP)
	 * @param abbreviations the new cell label abbreviations
	 */
	public void setCellLabelAbbreviations(String id, LabelAbbreviations abbreviations) {
		cellLabelAbbreviations.put(id, abbreviations);
	}

	@Override
	public String getRowName(int rowIndex) {
		return abbreviateLabel(getFullRowName(rowIndex), getRowLabelAbbreviations(rowIndex));
	}
	
	private String abbreviateLabel(String fullLabel, LabelAbbreviations abbrevs) {
		if (abbrevs == null) {
			return fullLabel;
		} else {
			return abbrevs.applyAbbreviations(fullLabel);
		}
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		LabelAbbreviations x = getColumnLabelAbbreviations(columnIndex);
		return abbreviateLabel(getFullColumnName(columnIndex), getColumnLabelAbbreviations(columnIndex));
	}

	@Override
	public String getCellName(int rowIndex, int columnIndex) {
		String fullLabel = super.getCellName(rowIndex, columnIndex);
		AbstractComponent component = (AbstractComponent) getStoredValueAt(rowIndex, columnIndex);
		if (component == null) {
			return fullLabel;
		} else {
			LabelAbbreviations abbrevs = getCellLabelAbbreviations(getKey(component));
			return abbrevs.applyAbbreviations(fullLabel);
		}
	}
	
	@Override
	public void updateLabels() {
		super.updateLabels();

		// Forget about any abbreviations we're no longer using.
		rowLabelAbbreviations.truncate(getRowCount());
		columnLabelAbbreviations.truncate(getColumnCount());
	}

	/**
	 * Tests whether the table structure is a skeleton, ready for values to be
	 * dropped in to flesh out the table cells and columns. A skeleton table
	 * structure is a 2-D table that doesn't yet have any values.
	 * 
	 * @return true, if the table is a skeleton
	 */
	@Override
	public boolean isSkeleton() {
		return rowList.size()==0;
	}
	
	/**
	 * Gets the maximum number of decimals shown in any cell in a column.
	 * If we have already calculated the maximum for a column, returns it.
	 * Otherwise iterates over all cells in the column to determine the
	 * maximum, and remembers that value.
	 * 
	 * @param columnIndex the column for which we want the max decimals setting
	 * @return the maximum number of decimals shown in the column.
	 */
	public int getMaxDecimalsForColumn(int columnIndex) {
		Integer decimals = maxDecimalsForColumn.get(columnIndex);
		
		if (decimals != null) {
			return decimals;
		}
		
		int maxDecimals = 0;
		for (int rowIndex=0; rowIndex < getRowCount(); ++rowIndex) {
			AbstractComponent component = (AbstractComponent) getStoredValueAt(rowIndex, columnIndex);
			if (component != null) {
				TableCellSettings cellSettings = getCellSettings(rowIndex, columnIndex);
				int cellDecimals = cellSettings.getNumberOfDecimals();
				if (cellDecimals < 0) {
					cellDecimals = TableCellSettings.DEFAULT_DECIMALS;
				}
				
				maxDecimals = Math.max(maxDecimals, cellDecimals);
			}
		}
		
		maxDecimalsForColumn.set(columnIndex, maxDecimals);
		return maxDecimals;
	}

	/**
	 * Updates the maximumd decimals for a column. For simplicity,
	 * just forgets all maximum decimal settings. The max for each
	 * column will be updated when {@link #getMaxDecimalsForColumn(int)} is
	 * called by the table cell renderer.
	 */
	public void updateDecimalsForColumns() {
		maxDecimalsForColumn.clear();
	}
	
	
	private class TableRow {
		private static final long serialVersionUID = 6852306825313602935L;
		private int index;
		private AbstractComponent component;

		public TableRow(int index, AbstractComponent component) {
			this.index = index;
			this.component = component;
		}
		
		public int               getIndex()     { return index; }		
		public AbstractComponent getComponent() { return component; }
	}
}
