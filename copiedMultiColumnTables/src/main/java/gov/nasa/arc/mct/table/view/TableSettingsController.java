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

import java.awt.Color;

import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.utils.ListenerManager;
import gov.nasa.arc.mct.table.utils.ListenerNotifier;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;

/**
 * Defines an adapter interface between the table settings panel and
 * the underlying tables and models.
 */
public abstract class TableSettingsController {
	
	private ListenerManager listenerManager = new ListenerManager();
	
	/**
	 * Adds a listener for selection changes in the table.
	 * 
	 * @param l the listener to add
	 */
	public void addSelectionListener(SelectionListener l) {
		listenerManager.addListener(SelectionListener.class, l);
	}
	
	/**
	 * Removes a listener for selection changes in the table.
	 * 
	 * @param l the listener to remove
	 */
	public void removeSelectionListener(SelectionListener l) {
		listenerManager.removeListener(SelectionListener.class, l);
	}
	
	/**
	 * Notifies all listeners that the selection has changed.
	 */
	protected void fireSelectionChanged() {
		listenerManager.fireEvent(SelectionListener.class, new ListenerNotifier<SelectionListener>() {
			@Override
			public void notifyEvent(SelectionListener listener) {
				listener.selectionChanged();
			}
		});
	}
	
	/**
	 * Tests whether the table orientation can be changed by the user.
	 * 
	 * @return true, if the user can change the table orientation
	 */
	public abstract boolean canSetOrientation();
	
	/**
	 * Tests whether the table can be transposed by the user.
	 * 
	 * @return true, if the table can be transposed
	 */
	public abstract boolean canTranspose();
	
	/**
	 * Gets the number of cells in the current selection
	 * 
	 * @return the number of cells selected
	 */
	public abstract int getSelectedCellCount();
	
	/**
	 * Gets the number of rows in the current selection.
	 * 
	 * @return the number of rows
	 */
	public abstract int getSelectedRowCount();
	
	/**
	 * Retrieves the table row count.
	 * @return table row count
	 */
	public abstract int getTableRowCount();
	
	/**
	 * Gets the number of columns in the current selection.
	 * 
	 * @return the number of columns
	 */
	public abstract int getSelectedColumnCount();
	
	/**
	 * Retrieves the table column count.
	 * @return table column count
	 */
	public abstract int getTableColumnCount();
	
	/**
	 * Tests whether the row and column headers can be hidden.
	 * The headers can only be hidden in a 1-dimensional table.
	 * 
	 * @return true, if the headers can be hidden
	 */
	public abstract boolean isCanHideHeaders();
	
	/**
	 * Gets the current table orientation.
	 * 
	 * @return the table orientation
	 */
	public abstract TableOrientation getTableOrientation();
	
	/**
	 * Sets the table orientation.
	 * 
	 * @param orientation the new table orientation
	 */
	public abstract void setTableOrientation(TableOrientation orientation);
	
	/**
	 * Gets the row height for the selected rows.
	 * 
	 * @return the current row height
	 */
	public abstract int getRowHeight();
	
	/**
	 * Sets the height of rows in the selection.
	 * 
	 * @param newHeight the new row height
	 */
	public abstract void setRowHeight(int newHeight);
	
	/**
	 * Gets the column width for the selected columns.
	 * 
	 * @return the current column width
	 */
	public abstract int getColumnWidth();
	
	/**
	 * Sets the width of column in the selection.
	 * 
	 * @param newWidth the new column width
	 */
	public abstract void setColumnWidth(int newWidth);
	
	/**
	 * Gets the alignment of the row header.
	 * 
	 * @return the row header alignment
	 */
	public abstract ContentAlignment getRowHeaderAlignment();
	
	/**
	 * Sets the alignment of the row header.
	 * 
	 * @param newAlignment the new header alignment
	 */
	public abstract void setRowHeaderAlignment(ContentAlignment newAlignment);
	
	/**
	 * Gets the alignment of the column header.
	 * 
	 * @return the column header alignment
	 */
	public abstract ContentAlignment getColumnHeaderAlignment();
	
	/**
	 * Sets the alignment of the column header.
	 * 
	 * @param newAlignment the new header alignment
	 */
	public abstract void setColumnHeaderAlignment(ContentAlignment newAlignment);
	
	/**
	 * Tests whether the table is showing grid lines.
	 * 
	 * @return true, if the table is showing grid lines
	 */
	public abstract boolean getShowGrid();
	
	/**
	 * Sets whether the table should show grid lines.
	 * 
	 * @param showGrid true, if the table should show grid lines
	 */
	public abstract void setShowGrid(boolean showGrid);
	
	/**
	 * Transposes the rows and columns in the table.
	 */
	public abstract void transposeTable();
	
	/**
	 * Gets the data model for the enumeration combo box.
	 * 
	 * @return the enumeration combo box model
	 */
	public abstract ComboBoxModel getEnumerationModel();

	/**
	 * Sets the enumeration to use for the selection.
	 * 
	 * @param model the data model of the enumeration control
	 */
	public abstract void setEnumeration(ComboBoxModel model);
	
	/**
	 * Sets the decimal places to use for the selection.
	 * 
	 * @param model the data model of the decimal places control
	 */
	public abstract void setDecimalPlaces(ComboBoxModel model);
	
	/**
	 * Gets the decimal places to use in the selection. 
	 * @return the number of decimal places to use in the selection.
	 */
	public abstract Integer getDecimalPlaces();
	
	/**
	 * Gets the cell font for selected cells. 
	 * @return the number of decimal places to use in the selection.
	 */
	public abstract JVMFontFamily getCellFontName();
	
	/**
	 * Sets date format to use for the selection.
	 * 
	 * @param model the data model of the asDate control
	 */
	public abstract void setDateFormat(ComboBoxModel model);
	
	/**
	 * Gets the date format to use in the selection. 
	 * @return date format to use in the selection.
	 */
	public abstract DateFormatItem getDateFormat();
	
	/**
	 * Gets whether the decimal places control should be shown. 
	 * @return true if the control should be shown, false otherwise
	 */
	public abstract boolean showDecimalPlaces();

	public abstract AbbreviationSettings getRowLabelAbbreviationSettings();
	
	public abstract void setRowLabelAbbreviations(AbbreviationSettings settings);

	public abstract AbbreviationSettings getColumnLabelAbbreviationSettings();
	
	public abstract void setColumnLabelAbbreviations(AbbreviationSettings settings);

	public abstract AbbreviationSettings getCellLabelAbbreviationSettings();
	
	public abstract void setCellLabelAbbreviations(AbbreviationSettings settings);
	
	/**
	 * Gets the alignment of selected cells, or null if there are multiple alignments.
	 * 
	 * @return the alignment of selected cells, or null if the cells use multiple alignments
	 */
	public abstract ContentAlignment getCellAlignment();
	
	/**
	 * Sets the cell alignment for the selected cells.
	 * 
	 * @param newAlignment the new alignment for the cells
	 */
	public abstract void setCellAlignment(ContentAlignment newAlignment);

	/**
	 * Returns true if the enumeration control's selectedItem is none.
	 */
	public abstract boolean enumerationIsNone(ComboBoxModel model);

	/**
	 * Returns true if the date control's selection is none.
	 * @param model the model for the control combo box
	 * @return true if the selection is none
	 */
	public abstract boolean dateIsNone(ComboBoxModel model);
	
	/** Gets cell border state. 
	 *  @return state
	 */
	public abstract BorderState getBorderState();
	
	/**
	 * Merge new edge setting into the border states of all selected cells, and persist.
	 * @param controllerState  the current composite state of all border controller buttons.
	 */
	public abstract void mergeBorderState(BorderState controllerState);
	
	/** Determine whether or not all selected cells have the same evaluator or not
	 * @return true if all the selected cells do not have the same evaluator
	 */
	public abstract boolean selectedCellsHaveMixedEnumerations();
	
	/** Set the font for selected cells
	 * @param aFont
	 */
	public abstract void setCellFont(ComboBoxModel model);
	
	/** Set the font for selected row Headers
	 * @param aFont
	 */
	public abstract void setRowHeaderFontName(ComboBoxModel model);
	
	/** Set the font for selected col Headers
	 * @param aFont
	 */
	public abstract void setColumnHeaderFontName(ComboBoxModel model);
	
	/** Get the font for selected row Headers
	 * @return font name
	 */
	public abstract JVMFontFamily getRowHeaderFontName();
	
	/** Set the font for selected col Headers
	 * @return font name
	 */
	public abstract JVMFontFamily getColumnHeaderFontName();
	
	/**
	 * Merge new font style setting into the font style of all selected cells, and persist.
	 * @param boldModel  the current  state of the bold font style button
	 * @param italicModel  the current state of the italic font style button
	 */
	public abstract void mergeCellFontStyle(ButtonModel boldModel, ButtonModel italicModel);

	public abstract void setCellFontStyle(int newStyle);
	
	public abstract void setRowHeaderFontStyle(int newStyle);
	
	public abstract void setColumnHeaderFontStyle(int newStyle);
	
	public abstract void setCellFontSize(int fontSize);
	
	public abstract void setRowHeaderFontSize(int fontSize);
	
	public abstract void setColumnHeaderFontSize(int fontSize);

	public abstract void setCellFontColor(Color fontColor);
	
	public abstract void setRowHeaderFontColor(Color fontCOlor);
	
	public abstract void setColumnHeaderFontColor(Color fontColor);
	
	
	public abstract Integer getCellFontStyle();
	
	public abstract Integer getRowFontStyle();
	
	public abstract Integer getColumnHeaderFontStyle();
	
	public abstract Integer getCellFontSize();
	
	public abstract Integer getRowHeaderFontSize();
	
	public abstract Integer getColumnHeaderFontSize();

	public abstract Color getCellFontColor();
	
	public abstract Color getRowHeaderFontColor();
	
	public abstract Color getColumnHeaderFontColor();

	public abstract Color getRowHeaderBackgroundColor();

	public abstract Color getColumnHeaderBackgroundColor();

	public abstract void setRowHeaderBackgroundColor(Color fontColor);

	public abstract void setColumnHeaderBackgroundColor(Color fontColor);

	public abstract void setCellBackgroundColor(Color backgroundColor);

	public abstract Color getCellBackgroundColor();

	public abstract Integer getRowHeaderTextAttribute();

	public abstract void setRowHeaderTextAttribute(int newTextAttribute);

	public abstract void setColumnHeaderTextAttribute(int newTextAttribute);

	public abstract Integer getColumnHeaderTextAttribute();

	public abstract void setCellFontTextAttribute(int fontStyle);

	public abstract Integer getCellFontTextAttribute();

	public abstract BorderState getRowHeaderBorderState();

	public abstract void setRowHeaderBorderState(BorderState newBorderState);

	public abstract BorderState getColumnHeaderBorderState();

	public abstract void setColumnHeaderBorderState(BorderState newBorderState);

	public abstract void mergeRowHeaderBorderState(BorderState controllerState);
	
	public abstract void mergeColumnHeaderBorderState(BorderState controllerState);

	public abstract Color getRowHeaderBorderColor();

	public abstract Color getColumnHeaderBorderColor();

	public abstract void setRowHeaderBorderColor(Color borderColor);

	public abstract void setColumnHeaderBorderColor(Color borderColor);
}
