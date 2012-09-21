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
/**
 * 
 */
package gov.nasa.arc.mct.table.view;

import gov.nasa.arc.mct.abbreviation.AbbreviationService;
import gov.nasa.arc.mct.abbreviation.Abbreviations;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.evaluator.api.Evaluator;
import gov.nasa.arc.mct.table.access.ServiceAccess;
import gov.nasa.arc.mct.table.gui.LabeledTable;
import gov.nasa.arc.mct.table.model.LabeledTableModel;
import gov.nasa.arc.mct.table.model.MultiColTableModel;
import gov.nasa.arc.mct.table.model.TableColumnModelAdapter;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.view.BorderState.BorderEdge;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a controller that mediates between a labeled table and
 * a control panel for that table.
 */
public class TableControlPanelController extends TableSettingsController {
	
	private static final Logger logger = LoggerFactory.getLogger(TableControlPanelController.class);
	
	/** The resource bundle we should use for getting strings. */
	private static final ResourceBundle bundle = ResourceBundle.getBundle("TableSettingsControlPanel"); //NOI18N

	private LabeledTable table;
	private LabeledTableModel model;

	private MultiColView manifestation;

	/**
	 * Creates a new controller for a view, table, and table model.
	 * 
	 * @param manifestation the table view manifestation
	 * @param table the table
	 * @param model the table model
	 */
	public TableControlPanelController(MultiColView manifestation, LabeledTable table, LabeledTableModel model) {
		this.manifestation = manifestation;
		this.table = table;
		this.model = model;

		table.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				TableControlPanelController.this.fireSelectionChanged();
			}
		});
		
		table.getTable().getColumnModel().addColumnModelListener(new TableColumnModelAdapter() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				TableControlPanelController.this.fireSelectionChanged();
			}
		});
		
		table.getRowHeaders().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				TableControlPanelController.this.fireSelectionChanged();
			}
		});
	}
	
	@Override
	public boolean getShowGrid() {
		return table.getTable().getShowHorizontalLines();
	}

	@Override
	public TableOrientation getTableOrientation() {
		return model.getOrientation();
	}

	@Override
	public void setShowGrid(boolean showGrid) {
		table.setShowGrid(showGrid);
		manifestation.saveCellSettings();
	}

	@Override
	public void setTableOrientation(TableOrientation orientation) {
		model.setOrientation(orientation);
		manifestation.swapAbbreviationsUponTableOrientationChange(orientation);
		table.updateColumnsHeaderValuesOnly();
		table.updateDropMode();
		manifestation.saveCellSettingsUponTableOrientationChange();
		
		for (int row = 0; row < table.getTable().getRowCount(); row++) {
				table.getTable().setRowHeight(row, getRowHeightFromPersistence(row));
		}

	}

	@Override
	public void transposeTable() {
		logger.error("Should transpose the table.");
	}

	@Override
	public int getSelectedCellCount() {
		return getSelectedRowCount() * getSelectedColumnCount();
	}

	@Override
	public int getSelectedColumnCount() {
		if (model.isSkeleton()) {
			return 0;
		} /*else if (model.getTableType() == TableType.ZERO_DIMENSIONAL) {
			// We have to special-case a 0-D table since the
			// row or column selection might be zero while the other
			// is nonzero.
			return (table.getTable().isCellSelected(0, 0) ? 1 : 0);
		}*/ else {
			return table.getTable().getSelectedColumnCount();
		}
	}

	@Override
	public int getSelectedRowCount() {
		if (model.isSkeleton()) {
			return 0;
		} /*else if (model.getTableType() == TableType.ZERO_DIMENSIONAL) {
			// We have to special-case a 0-D table since the
			// row or column selection might be zero while the other
			// is nonzero.
			return (table.getTable().isCellSelected(0, 0) ? 1 : 0);
		}*/ else {
			return table.getTable().getSelectedRowCount();
		}
	}

	@Override
	public boolean isCanHideHeaders() {
		return true;
	}

	@Override
	public int getColumnWidth() {
		if (getSelectedColumnCount() == 0) {
			return -1;
		} else {
			int firstColumn = table.getTable().getColumnModel().getSelectedColumns()[0];
			return table.getTable().getColumnModel().getColumn(firstColumn).getPreferredWidth();
		}
	}

	private int getRowHeightFromPersistence(int rowIndex) {
		int rowHeight = table.getTable().getRowHeight();
		TableSettings tableSettings = manifestation.loadSettingsFromPersistence();
		for (TableSettings.AvailableSettings setting : TableSettings.AvailableSettings
				.values()) {
			try {
				String name = setting.name();
				String value = manifestation.getViewProperties().getProperty(name, String.class);
				if (name.equals(TableSettings.AvailableSettings.ROW_HEIGHTS.name())) {

					if ((value != null) && !value.isEmpty()) {
						int[] rowHeights = tableSettings.toIntegerArray(value);
						return rowHeights[rowIndex];
					}

				}
			} catch (Exception ex) {
				logger.error("Exception when loading persistent settings: ", ex);
			}
		}
		return rowHeight;
	}
	
	@Override
	public int getRowHeight() {
		int[] selectedRows = table.getTable().getSelectedRows();
		
		if (selectedRows == null || selectedRows.length == 0) {
			return -1;
		}
		
		int firstRowHeight = -1;
		
		for (int i = 0; i < selectedRows.length; i++) {
			int currentRowHeight = getRowHeightFromPersistence(selectedRows[i]);
			if (currentRowHeight > 0) {
				if (i == 0) {
					firstRowHeight = currentRowHeight;
				}
				setRowHeight(selectedRows[i], currentRowHeight);
			}
		}
	return firstRowHeight;
	}
	
/*	@Override
	public Integer getRowHeight() {
		Integer commonSize = null;
		
		for (Integer row : table.getSelectedRows()) {
			Integer aSize = table.getRowHeight(row);
			if (commonSize!=null && commonSize!=aSize) {
				return null;
			}
			commonSize = aSize;
		}
		
		return commonSize;
	}*/

	@Override
	public void setColumnWidth(int newWidth) {
		for (int col : table.getTable().getSelectedColumns()) {
			TableColumn column = table.getTable().getColumnModel().getColumn(col);
			if (column.getPreferredWidth() != newWidth) {
				column.setPreferredWidth(newWidth);
			}
		}
		manifestation.saveCellSettings();
	}

	@Override
	public void setRowHeight(int newHeight) {
		for (int row : table.getTable().getSelectedRows()) {
			if (table.getTable().getRowHeight(row) != newHeight) {
				table.setRowHeight(row, newHeight);
				table.getTable().setRowHeight(row, newHeight);
			}
		}
		manifestation.saveCellSettings();
	}
	
	private void setRowHeight(int rowIndex, int newHeight) {
		if (table.getTable().getRowHeight(rowIndex) != newHeight) {
				table.getTable().setRowHeight(rowIndex, newHeight);
				manifestation.saveCellSettings();
		}
	}
	
	/**
	 * Tests whether a single, non-empty cell has been selected. If a single
	 * cell selection exists, return true if the cell has a component at that
	 * location.
	 * 
	 * @return true, if a single, non-empty cell has been selected, false otherwise
	 */
	public boolean isSingleCellSelection() {
		if (getSelectedCellCount() != 1) {
			return false;
		} else {
			return !isSelectionContainsEmptyCells();
		}
	}
	
	private boolean isSelectionContainsEmptyCells() {
		for (int row : table.getSelectedRows()) {
			for (int column : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, column);
				if (component == null) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public ComboBoxModel getEnumerationModel() {
		DefaultComboBoxModel enumerationModel = new DefaultComboBoxModel();
		if (isSelectionContainsEmptyCells()) {
			enumerationModel.addElement(new EnumerationItem(null));
			return enumerationModel;
		}
		if (isSingleCellSelection()) {
			EnumerationItem selectedItem = null;
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			TableCellSettings cellSettings = tableModel.getCellSettings(tableModel.getKey(component));
			AbstractComponent evaluator = cellSettings.getEvaluator();
			//Enumeration control is now always visible, so show None as option
			enumerationModel.addElement(new EnumerationItem(null));
			
			// Add the component itself, if it can be a self-evaluator.
			if (component.getCapability(Evaluator.class) != null && !component.getCapability(Evaluator.class).requiresMultipleInputs()) {
				EnumerationItem item = new EnumerationItem(component);
				enumerationModel.addElement(item);
				if (evaluator!=null && evaluator.getId().equals(component.getId())) {
					selectedItem = item;
				}
			}

			AbstractComponent referencedComponent = component;
			
			for (AbstractComponent parent : referencedComponent.getReferencingComponents()) {
				// For now, we only find evaluators that have exactly one child (the current component).
				Evaluator e = parent.getCapability(Evaluator.class);
				if (e != null && !e.requiresMultipleInputs()) {
					// Found an evaluator for the component.
					EnumerationItem item = new EnumerationItem(parent);
					enumerationModel.addElement(item);
					if (evaluator!=null && evaluator.getId().equals(parent.getId())) {
						selectedItem = item;
					}
				}
			}
			
			if (selectedItem != null) {
				enumerationModel.setSelectedItem(selectedItem);
			}
		} else {
			addCommonEnumerators(enumerationModel);
		}
		
		return enumerationModel;
	}

	/** Find all common (i.e., intersection of) enumerators for selected cells
	 * @param enumerationModel
	 */
	private void addCommonEnumerators(DefaultComboBoxModel enumerationModel) {
		if (getSelectedCellCount() > 0) {
			Set<EnumerationItem> commonEnums = new HashSet<EnumerationItem>();
			AbstractComponent commonSelectedEvaluator = null;
			boolean commonEvaluator = true;
			boolean mixedEvaluatorSet = false;
			boolean firstCell = true;
			
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			// Enumeration control is always showing, so add None as option
			EnumerationItem none = new EnumerationItem(null);
			enumerationModel.addElement(none);
			enumerationModel.setSelectedItem(none);
					
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);				
			AbstractComponent referencedComponent = component;
			// Add the component itself, if it can be a self-evaluator.
			if (referencedComponent.getCapability(Evaluator.class) != null && 
					!referencedComponent.getCapability(Evaluator.class).requiresMultipleInputs()) {
				EnumerationItem item = new EnumerationItem(referencedComponent);
				enumerationModel.addElement(item);
			}
	
			for (AbstractComponent parent : referencedComponent.getReferencingComponents()) {
				// For now, we only find evaluators that have exactly one child (the current component).
				Evaluator e = parent.getCapability(Evaluator.class);
				if (e != null && !e.requiresMultipleInputs()) {
					// Found an enumeration for the component.
						commonEnums.add(new EnumerationItem(parent));
				}
			}
	
			for (int row : table.getSelectedRows()) {
				for (int column : table.getSelectedColumns()) {
					component = (AbstractComponent) model.getStoredValueAt(row, column);
					if (component == null) {
						mixedEvaluatorSet = true;
						continue;
					}
					referencedComponent = component;
					TableCellSettings cellSettings = tableModel.getCellSettings(tableModel.getKey(referencedComponent));
					AbstractComponent evaluator = cellSettings.getEvaluator();
					if (commonSelectedEvaluator != null)
						if (evaluator != null) {
							if (!evaluator.getId().equals(commonSelectedEvaluator.getId())) {
								mixedEvaluatorSet = true;
							}
						} else {
							mixedEvaluatorSet = true;
					} else {
						if (evaluator != null && !firstCell) {
							mixedEvaluatorSet = true;
						}
					}
					if (!mixedEvaluatorSet) {
						commonSelectedEvaluator = evaluator;
					}
					firstCell = false;
					
					Iterator<EnumerationItem> iterator = commonEnums.iterator(); 
					while (iterator.hasNext()) {
						EnumerationItem commonEnum = iterator.next();				
						if (!commonEnum.getEvaluator().getComponents().contains(referencedComponent)) {
							iterator.remove();
						}
					}
				}
			}			
			Iterator<EnumerationItem> iterator = commonEnums.iterator();
			while (iterator.hasNext()) {
				EnumerationItem nextItem = iterator.next();
				enumerationModel.addElement(nextItem);
				if (!mixedEvaluatorSet && nextItem.getEvaluator().equals(commonSelectedEvaluator)) {
					enumerationModel.setSelectedItem(nextItem);
				}
			}	
		}
	}
	
	/** Determine whether or not all selected cells have the same evaluator or not
	 * @return true if all the selected cells do not have the same evaluator
	 */
	@Override
	public boolean selectedCellsHaveMixedEnumerations() {
		AbstractComponent commonSelectedEvaluator = null;
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		boolean firstCell = true;
		for (int row : table.getSelectedRows()) {
			for (int column : table.getSelectedColumns()) {	
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, column);
				if (component == null) {
					return true;
				}
				AbstractComponent referencedComponent = component;
				TableCellSettings cellSettings = tableModel.getCellSettings(tableModel.getKey(referencedComponent));
				AbstractComponent evaluator = cellSettings.getEvaluator();
				if (commonSelectedEvaluator != null)
					if (evaluator != null) {
						if (!evaluator.getId().equals(commonSelectedEvaluator.getId())) {
							return true;
						}
					} else {
						return true;
				} else {
					if (evaluator != null && !firstCell) {
						return true;
					}
				}
				commonSelectedEvaluator = evaluator;
				firstCell = false;
				
			}
		}
		return false;
	}
	
	@Override
	public void setEnumeration(ComboBoxModel comboBoxModel) {
		if (isSingleCellSelection()) {
			TableCellSettings settings = getTableCellSettings(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			settings.setEvaluator(EnumerationItem.class.cast(comboBoxModel.getSelectedItem()).getEvaluator());

			manifestation.saveCellSettings();
		} else {
			for (Integer row : table.getSelectedRows()) {
				for (Integer column : table.getSelectedColumns()) {
					TableCellSettings settings = getTableCellSettings(row, column);
					settings.setEvaluator(EnumerationItem.class.cast(comboBoxModel.getSelectedItem()).getEvaluator());
				}
			}
			manifestation.saveCellSettings();
		}
	}
	
	@Override
	public boolean enumerationIsNone(ComboBoxModel model) {
		if (model.getSelectedItem() == null ||    //Mixed enum state
				(model.getSelectedItem() != null &&   // an enum is selected/set
				EnumerationItem.class.cast(model.getSelectedItem()).getEvaluator() != null)) {
			return false; //nothing is selected 
		}
		return (EnumerationItem.class.cast(model.getSelectedItem()).getEvaluator() == null);
	}
	
	@Override
	public boolean dateIsNone(ComboBoxModel model) {
		if (DateFormatItem.None == model.getSelectedItem()) {
			return true;
		}
		return false;
	}

	
	@Override
	public boolean showDecimalPlaces() {
		if (isSelectionContainsEmptyCells()) {
			return false;
		}
		if (table.getSelectedRows().length == 0 && table.getSelectedColumns().length == 0) {
			return false;
		}
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				FeedProvider fp = component.getCapability(FeedProvider.class);
				if (fp == null || fp.getFeedType() == FeedType.STRING) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public Integer getDecimalPlaces() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		Integer decimalPlaces = null;
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				int decimals = settings.getNumberOfDecimals();
				// determine default decimal places
				if (decimals == -1) {
					FeedProvider fp = component.getCapability(FeedProvider.class);
					if (fp != null) {
						decimals = fp.getFeedType() == FeedType.INTEGER ? 0 : TableCellSettings.DEFAULT_DECIMALS;
					}
				}
				if (decimalPlaces != null && decimalPlaces != decimals) {
					return null;
				}
				decimalPlaces = decimals;
			}
		}
		
		return decimalPlaces;
	}
	
	@Override
	public JVMFontFamily getCellFontName() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		if (isSingleCellSelection()) {
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String id = tableModel.getKey(component);
			TableCellSettings settings = tableModel.getCellSettings(id);
			return settings.getCellFont();
		}
		JVMFontFamily defaultFont = TableFormattingConstants.defaultJVMFontFamily;
		JVMFontFamily commonCellFont = null;
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				JVMFontFamily aCellFont = settings.getCellFont();
				if (aCellFont == null) {
						aCellFont = defaultFont;
				}
				if (commonCellFont != null && !aCellFont.equals(commonCellFont)) {
					return null;
				}
				commonCellFont = aCellFont;
			}
		}
		
		return commonCellFont;
	}
	
	@Override
	public Integer getCellFontSize() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		if (isSingleCellSelection()) {
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String id = tableModel.getKey(component);
			TableCellSettings settings = tableModel.getCellSettings(id);
			return Integer.valueOf(settings.getFontSize());
		}
		Integer defaultFontSize = Integer.valueOf(TableFormattingConstants.defaultFontSize);
		Integer commonCellFontSize = null;
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				Integer aCellFontSize = settings.getFontSize();
				// determine default decimal places
				if (aCellFontSize == null) {
						aCellFontSize = defaultFontSize;
				}
				if (commonCellFontSize != null && !aCellFontSize.equals(commonCellFontSize)) {
					return null;
				}
				commonCellFontSize = aCellFontSize;
			}
		}
		
		return commonCellFontSize;
	}
	
	@Override
	public Color getCellFontColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		if (isSingleCellSelection()) {
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String id = tableModel.getKey(component);
			TableCellSettings settings = tableModel.getCellSettings(id);
			return settings.getFontColor();
		}
		Color commonCellColor = null;
		
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				Color aCellFontColor = settings.getFontColor();
				if ((commonCellColor != null && !commonCellColor.equals(aCellFontColor))) {
					return null;
				}
				commonCellColor = aCellFontColor;
			}
		}
		
		return commonCellColor;
	}
	
	@Override
	public Color getCellBackgroundColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		if (isSingleCellSelection()) {
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String id = tableModel.getKey(component);
			TableCellSettings settings = tableModel.getCellSettings(id);
			return settings.getBackgroundColor();
		}
		Color commonCellColor = null;
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				Color aCellBackgroundColor = settings.getBackgroundColor();
				if (commonCellColor != null && !commonCellColor.equals(aCellBackgroundColor)) {
					return null;
				}
				commonCellColor = aCellBackgroundColor;
			}
		}
		
		return commonCellColor;
	}
	
	@Override
	public Integer getCellFontStyle() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		if (isSingleCellSelection()) {
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String id = tableModel.getKey(component);
			TableCellSettings settings = tableModel.getCellSettings(id);
			return Integer.valueOf(settings.getFontStyle());
		}
		Integer defaultFontStyle = TableFormattingConstants.defaultFontStyle;
		Integer commonCellFontStyle = null;
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				Integer aCellFontStyle = settings.getFontStyle();
				// determine default decimal places
				if (aCellFontStyle == null) {
					aCellFontStyle = defaultFontStyle;
				}
				if (commonCellFontStyle != null && !aCellFontStyle.equals(commonCellFontStyle)) {
					return null;
				}
				commonCellFontStyle = aCellFontStyle;
			}
		}
		
		return commonCellFontStyle;
	}
	
	@Override
	public Integer getCellFontTextAttribute() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		if (isSingleCellSelection()) {
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String id = tableModel.getKey(component);
			TableCellSettings settings = tableModel.getCellSettings(id);
			return Integer.valueOf(settings.getTextAttributeUnderline());
		}
		Integer defaultFontTextAttribute = TableFormattingConstants.UNDERLINE_OFF;
		Integer commonCellFontTextAttribute = null;
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				Integer aCellFontTextAttribute = settings.getTextAttributeUnderline();
				// determine default decimal places
				if (aCellFontTextAttribute == null) {
					aCellFontTextAttribute = defaultFontTextAttribute;
				}
				if (commonCellFontTextAttribute != null && !aCellFontTextAttribute.equals(commonCellFontTextAttribute)) {
					return null;
				}
				commonCellFontTextAttribute = aCellFontTextAttribute;
			}
		}
		
		return commonCellFontTextAttribute;
	}
	
	@Override
	public void setDecimalPlaces(ComboBoxModel comboBoxModel) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setNumberOfDecimals(Integer.class.cast(comboBoxModel.getSelectedItem()).intValue());
			}
		}
		
		tableModel.updateDecimalsForColumns();
		manifestation.saveCellSettings();
	}
	
	@Override
	public void setCellFont(ComboBoxModel comboBoxModel) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setCellFont(JVMFontFamily.class.cast(comboBoxModel.getSelectedItem()));
				
			}
		}
		manifestation.saveCellSettings();
	}
	
	@Override
	public void setCellFontColor(Color fontColor) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setForegroundColor(fontColor);
				
			}
		}
		manifestation.saveCellSettings();
	}
	
	@Override
	public void setCellBackgroundColor(Color backgroundColor) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setBackgroundColor(backgroundColor);
				
			}
		}
		manifestation.saveCellSettings();
	}
	
	@Override
	public void setCellFontSize(int fontSize) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setFontSize(fontSize);
			}
		}
		manifestation.saveCellSettings();
	}
	
	@Override
	public void setCellFontStyle(int fontStyle) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setFontStyle(fontStyle);
			}
		}
		manifestation.saveCellSettings();
	}
	
	@Override
	public void setCellFontTextAttribute(int fontStyle) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				settings.setTextAttributeUnderline(fontStyle);
			}
		}
		manifestation.saveCellSettings();
	}
	
	@Override
	public DateFormatItem getDateFormat() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		DateFormatItem df = null;
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				DateFormatItem cellDateFormat = settings.getDateFormat();
			
				df = cellDateFormat;
			}			
		}	
		return df;
	}

	@Override
	public void setDateFormat(ComboBoxModel comboBoxModel) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				DateFormatItem df = (DateFormatItem)comboBoxModel.getSelectedItem();
				settings.setDateFormat(df);
			}
		}
		manifestation.saveCellSettings();//persistDate
	}


	private TableCellSettings getTableCellSettings(int rowIndex, int columnIndex) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		AbstractComponent component = (AbstractComponent) model.getStoredValueAt(rowIndex, columnIndex);
		String id = tableModel.getKey(component);
		TableCellSettings settings = tableModel.getCellSettings(id);
		return settings;
	}

	private static class EnumerationItem implements Comparable<EnumerationItem> {
		
		private AbstractComponent evaluator;
		private String evalID = ""; 

		public EnumerationItem(AbstractComponent enumeration) {
			this.evaluator = enumeration;
			if (evaluator != null) { 
				this.evalID = evaluator.getComponentId();
			} 
		}
		
		@Override
		public String toString() {
			if (evaluator == null) {
				return bundle.getString("ENUMERATION_NONE");
			} else {
				return evaluator.getDisplayName() + " (" + evaluator.getCapability(Evaluator.class).getLanguage() + ")";
			}
		}

		public AbstractComponent getEvaluator() {
			return evaluator;
		}
		
		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if (other instanceof EnumerationItem) {
				EnumerationItem that = (EnumerationItem) other;
				result = (this.evalID.equals(that.evalID) && super.equals(that));
			}
			return result;
		}
		
	    @Override public int hashCode() {
	        return this.evalID.hashCode();
	    }
	    
	    @Override
		public int compareTo(EnumerationItem other) {
	    	return evalID.compareTo(other.getEvaluator().getComponentId());
	    }
	}
	
	@Override
	public boolean canSetOrientation() {
		return true; //model.getTableType() != TableType.ZERO_DIMENSIONAL;
	}

	@Override
	public boolean canTranspose() {
		return true; //model.getTableType() == TableType.TWO_DIMENSIONAL;
	}

	@Override
	public AbbreviationSettings getRowLabelAbbreviationSettings() {
		if (!isSingleCellSelection()) {
			return null;
		} else {
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			String fullLabel = tableModel.getFullRowName(table.getSelectedRows()[0]);
			LabelAbbreviations currentAbbrevs = tableModel.getRowLabelAbbreviations(table.getSelectedRows()[0]);
			Abbreviations availableAbbrevs = ServiceAccess.getService(AbbreviationService.class).getAbbreviations(fullLabel);
			return new AbbreviationSettings(fullLabel, availableAbbrevs, currentAbbrevs);
		}
	}

	@Override
	public void setRowLabelAbbreviations(AbbreviationSettings settings) {
		if (isSingleCellSelection()) {
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			tableModel.setRowLabelAbbreviations(table.getSelectedRows()[0], settings.getAbbreviations());
			manifestation.saveCellSettings();
		}
		
	}

	@Override
	public AbbreviationSettings getColumnLabelAbbreviationSettings() {
		if (!isSingleCellSelection()) {
			return null;
		} else {
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			String fullLabel = tableModel.getFullColumnName(table.getSelectedColumns()[0]);
			LabelAbbreviations currentAbbrevs = tableModel.getColumnLabelAbbreviations(table.getSelectedColumns()[0]);
			Abbreviations availableAbbrevs = ServiceAccess.getService(AbbreviationService.class).getAbbreviations(fullLabel);
			return new AbbreviationSettings(fullLabel, availableAbbrevs, currentAbbrevs);
		}
	}

	@Override
	public void setColumnLabelAbbreviations(AbbreviationSettings settings) {
		if (isSingleCellSelection()) {
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			tableModel.setColumnLabelAbbreviations(table.getSelectedColumns()[0], settings.getAbbreviations());
			table.updateColumnsHeaderValuesOnly();

			manifestation.saveCellSettings();
		}
	}

	@Override
	public AbbreviationSettings getCellLabelAbbreviationSettings() {
		if (!isSingleCellSelection()) {
			return null;
		} else {
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			String fullLabel = tableModel.getFullCellName(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			LabelAbbreviations currentAbbrevs = tableModel.getCellLabelAbbreviations(tableModel.getKey(component));
			Abbreviations availableAbbrevs = ServiceAccess.getService(AbbreviationService.class).getAbbreviations(fullLabel);
			return new AbbreviationSettings(fullLabel, availableAbbrevs, currentAbbrevs);
		}
	}

	@Override
	public void setCellLabelAbbreviations(AbbreviationSettings settings) {
		if (isSingleCellSelection()) {
			MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
			AbstractComponent component = (AbstractComponent) model.getStoredValueAt(table.getSelectedRows()[0], table.getSelectedColumns()[0]);
			tableModel.setCellLabelAbbreviations(tableModel.getKey(component), settings.getAbbreviations());
			manifestation.saveCellSettings();
		}
	}

	@Override
	public ContentAlignment getRowHeaderAlignment() {
		ContentAlignment alignment = null;
		
		for (Integer row : table.getSelectedRows()) {
			ContentAlignment rowAlignment = table.getRowHeaderAlignment(row);
			if (alignment!=null && alignment!=rowAlignment) {
				return null;
			}
			alignment = rowAlignment;
		}
		
		return alignment;
	}

	@Override
	public void setRowHeaderAlignment(ContentAlignment newAlignment) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderAlignment(row, newAlignment);
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}
	
	@Override
	public BorderState getRowHeaderBorderState() {
		BorderState commonBorderState = null;
		for (Integer row : table.getSelectedRows()) {
			BorderState rowBorderState = table.getRowHeaderBorderState(row);
			if (commonBorderState!=null && commonBorderState!=rowBorderState) {
				return null;
			}
			commonBorderState = rowBorderState;
		}
		
		return commonBorderState;
	}

	@Override
	public void setRowHeaderBorderState(BorderState newBorderState) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderBorderState(row, newBorderState);
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}
	
	@Override
	public void setRowHeaderFontName(ComboBoxModel comboBoxModel) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderFontName(row, 
					TableFormattingConstants.JVMFontFamily.class.cast(
							comboBoxModel.getSelectedItem()));	
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}
	
	@Override
	public void setRowHeaderFontStyle(int newStyle) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderFontStyle(row, Integer.valueOf(newStyle));
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}
	
	@Override
	public void setRowHeaderTextAttribute(int newTextAttribute) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderTextAttribute(row, Integer.valueOf(newTextAttribute));
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}
	
	@Override
	public void setColumnHeaderTextAttribute(int newTextAttribute) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderTextAttribute(col, Integer.valueOf(newTextAttribute));
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}
	
	@Override
	public void setColumnHeaderFontName(ComboBoxModel comboBoxModel) {
		for (Integer column : table.getSelectedColumns()) {
			table.setColumnHeaderFontName(column, JVMFontFamily.class.cast(comboBoxModel.getSelectedItem()));
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}
	
	@Override
	public void setColumnHeaderFontStyle(int fontStyle) {
		for (Integer column : table.getSelectedColumns()) {
			table.setColumnHeaderFontStyle(column, Integer.valueOf(fontStyle));
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}

	@Override
	public ContentAlignment getColumnHeaderAlignment() {
		ContentAlignment alignment = null;
		
		for (Integer col : table.getSelectedColumns()) {
			ContentAlignment columnAlignment = table.getColumnHeaderAlignment(col);
			if (alignment!=null && alignment!=columnAlignment) {
				return null;
			}
			alignment = columnAlignment;
		}
		
		return alignment;
	}

	@Override
	public void setColumnHeaderAlignment(ContentAlignment newAlignment) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderAlignment(col, newAlignment);
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}
	
	@Override
	public BorderState getColumnHeaderBorderState() {
		BorderState commonBorderState = null;
		for (Integer col : table.getSelectedColumns()) {
			BorderState colBorderState = table.getColumnHeaderBorderState(col);
			if (commonBorderState!=null && commonBorderState!=colBorderState) {
				return null;
			}
			commonBorderState = colBorderState;
		}
		
		return commonBorderState;
	}

	@Override
	public void setColumnHeaderBorderState(BorderState newBorderState) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderBorderState(col, newBorderState);
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}

	@Override
	public ContentAlignment getCellAlignment() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		
		ContentAlignment alignment = null;
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				TableCellSettings settings = tableModel.getCellSettings(tableModel.getKey(component));
				ContentAlignment cellAlignment = settings.getAlignment();
				
				if (alignment!=null && cellAlignment!=alignment) {
					return null;
				}
				
				alignment = cellAlignment;
			}
		}
		
		return alignment;
	}

	@Override
	public void setCellAlignment(ContentAlignment newAlignment) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells 
				TableCellSettings settings = tableModel.getCellSettings(tableModel.getKey(component));
				settings.setAlignment(newAlignment);
			}
		}
		
		manifestation.saveCellSettings();
	}

	@Override
	public int getTableRowCount() {
		return table.getTable().getRowCount();
	}

	@Override
	public int getTableColumnCount() {
		return table.getTable().getColumnCount();
	}

	@Override
	public BorderState getBorderState() {
		if (isSelectionContainsEmptyCells()) {
			return new BorderState(BorderEdge.NONE.value());
		}
		
		BorderState fourEdgeControlState =  new BorderState(BorderEdge.NONE.value());
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);
		
		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {

				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				fourEdgeControlState = settings.getCellBorderState();
				assert fourEdgeControlState != null;
			}			
		}	
		return fourEdgeControlState;
	}
	
	@Override
	public void mergeCellFontStyle(ButtonModel boldModel, ButtonModel italicModel) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells 
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				if (boldModel.isSelected()) {
					if (italicModel.isSelected()) {
						settings.setFontStyle(Font.BOLD+Font.ITALIC);
					} else {
						settings.setFontStyle(Font.BOLD);
					}
				} else {
					if (italicModel.isSelected()) {
						settings.setFontStyle(Font.ITALIC);
					} else {
						settings.setFontStyle(Font.PLAIN);
					}
				}
				manifestation.saveCellSettings();
			}
		}
				
	}
	
	@Override
	public void mergeBorderState(BorderState controllerState) {
		MultiColTableModel tableModel = MultiColTableModel.class.cast(model);

		for (Integer row : table.getSelectedRows()) {
			for (Integer col : table.getSelectedColumns()) {
				BorderState stateToUpdate = null;
				AbstractComponent component = (AbstractComponent) model.getStoredValueAt(row, col);
				if (component == null) continue; // component-less cells 
				String id = tableModel.getKey(component);
				TableCellSettings settings = tableModel.getCellSettings(id);
				stateToUpdate = settings.getCellBorderState();

				// for each selected cell, either clear it or set it according to the controller edge state and multiple selection criteria.  
				if (!controllerState.hasWestBorder()) {
					stateToUpdate.removeBorderState(BorderEdge.WEST.value());
				} else {
					if (controllerState.hasWestBorder() && shouldToggleWest(row, col)) {
						stateToUpdate.addBorderState(BorderEdge.WEST.value());
					}
				}
				if (!controllerState.hasNorthBorder()) {
					stateToUpdate.removeBorderState(BorderEdge.NORTH.value());
				} else {
					if (controllerState.hasNorthBorder() && shouldToggleNorth(row, col)) {
						stateToUpdate.addBorderState(BorderEdge.NORTH.value());
					}
				}
				if (!controllerState.hasEastBorder()) {
					stateToUpdate.removeBorderState(BorderEdge.EAST.value());
				} else {
					if (controllerState.hasEastBorder() && shouldToggleEast(row, col)) {
						stateToUpdate.addBorderState(BorderEdge.EAST.value());
					}
				}
				if (!controllerState.hasSouthBorder()) {
					stateToUpdate.removeBorderState(BorderEdge.SOUTH.value());
				} else {
					if (controllerState.hasSouthBorder() && shouldToggleSouth(row, col)) {
						stateToUpdate.addBorderState(BorderEdge.SOUTH.value());
					}
				}
				settings.setCellBorderState(stateToUpdate);
			}
		}
		manifestation.saveCellSettings();		
	}
	
	@Override
	public void mergeRowHeaderBorderState(BorderState controllerState) {
	
		for (Integer row : table.getSelectedRows()) {
			BorderState stateToUpdate = table.getRowHeaderBorderState(row);

			// for each selected cell, either clear it or set it according to the controller edge state and multiple selection criteria.  
			if (!controllerState.hasWestBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.WEST.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.WEST.value());
			}
			if (!controllerState.hasNorthBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.NORTH.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.NORTH.value());
			}
			if (!controllerState.hasEastBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.EAST.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.EAST.value());
			}
			if (!controllerState.hasSouthBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.SOUTH.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.SOUTH.value());
			}
			table.setRowHeaderBorderState(row,stateToUpdate);
		}
		manifestation.saveCellSettings();	
		table.getRowHeaders().repaint();
	}
	
	@Override
	public void mergeColumnHeaderBorderState(BorderState controllerState) {
	
		for (Integer col : table.getSelectedColumns()) {
			BorderState stateToUpdate = table.getColumnHeaderBorderState(col);

			// for each selected cell, either clear it or set it according to the controller edge state and multiple selection criteria.  
			if (!controllerState.hasWestBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.WEST.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.WEST.value());
			}
			if (!controllerState.hasNorthBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.NORTH.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.NORTH.value());
			}
			if (!controllerState.hasEastBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.EAST.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.EAST.value());
			}
			if (!controllerState.hasSouthBorder()) {
				stateToUpdate.removeBorderState(BorderEdge.SOUTH.value());
			} else {
				stateToUpdate.addBorderState(BorderEdge.SOUTH.value());
			}
			table.setColumnHeaderBorderState(col,stateToUpdate);
		}
		manifestation.saveCellSettings();	
		table.getTable().getTableHeader().repaint();
	}
	
	private boolean shouldToggleEast(int row, int col) {
		int [] selectedCols = table.getSelectedColumns();
		int maximumSelectedColumnInThisRow = -1;
		for (int r : table.getSelectedRows()) {
			if (r != row) continue;
			maximumSelectedColumnInThisRow = maximum(selectedCols);
		}
		assert maximumSelectedColumnInThisRow >= 0;
		return col == maximumSelectedColumnInThisRow;
	}
	
	private boolean shouldToggleWest(int row, int col) {
		int [] selectedCols = table.getSelectedColumns();
		int minimumSelectedColumnInThisRow = -1;
		for (int r : table.getSelectedRows()) {
			if (r != row) continue;
		    minimumSelectedColumnInThisRow = minimum(selectedCols);
		}
		assert minimumSelectedColumnInThisRow >= 0;
		return col == minimumSelectedColumnInThisRow;
	}

	private boolean shouldToggleNorth(int row, int col) {
		int [] selectedRows = table.getSelectedRows();
		int minimumSelectedRowInThisColumn = -1;
		for (int c : table.getSelectedColumns()) {
			if (c != col) continue;
			minimumSelectedRowInThisColumn = minimum(selectedRows);
		}
		assert minimumSelectedRowInThisColumn >= 0;
		return row == minimumSelectedRowInThisColumn;
	}
	
	private boolean shouldToggleSouth(int row, int col) {
		int [] selectedRows = table.getSelectedRows();
		int maximumSelectedRowInThisColumn = -1;
		for (int c : table.getSelectedColumns()) {
			if (c != col) continue;
			maximumSelectedRowInThisColumn = maximum(selectedRows);
		}
		assert maximumSelectedRowInThisColumn >= 0;
		return row == maximumSelectedRowInThisColumn;
	}

	/** Minimum
	 * @param array
	 * @return the min in this array
	 */
	private int minimum(int[] selectedItem) {
		assert selectedItem.length >= 1;
		int min = selectedItem[0];
		for (int i=1; i < selectedItem.length; i++) {
			if (selectedItem[i] < min) {
				min = selectedItem[i];
			}
		}
		return min;
	}
	
	/** Maximum.
	 * @param array
	 * @return the max in this array
	 */
	private int maximum(int[] selectedItem) {
		assert selectedItem.length >= 1;
		int max = selectedItem[0];
		for (int i=1; i< selectedItem.length; i++) {
			if (selectedItem[i] > max) {
				max = selectedItem[i];
			}
		}
		return max;
	}

	@Override
	public void setRowHeaderFontSize(int fontSize) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderFontSize(row, fontSize);
			
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}

	@Override
	public void setColumnHeaderFontSize(int fontSize) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderFontSize(col, fontSize);
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}

	@Override
	public void setRowHeaderFontColor(Color fontColor) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderFontColor(row, fontColor);	
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}
	
	@Override
	public void setRowHeaderBorderColor(Color borderColor) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderBorderColor(row, borderColor);	
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}

	@Override
	public void setColumnHeaderFontColor(Color fontColor) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderFontColor(col, fontColor);	
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}
	
	@Override
	public void setColumnHeaderBorderColor(Color borderColor) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderBorderColor(col, borderColor);	
		}
		manifestation.saveCellSettings();
		table.getTable().getTableHeader().repaint();
	}
	
	@Override
	public void setRowHeaderBackgroundColor(Color fontColor) {
		for (Integer row : table.getSelectedRows()) {
			table.setRowHeaderBackgroundColor(row, fontColor);	
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}

	@Override
	public void setColumnHeaderBackgroundColor(Color fontColor) {
		for (Integer col : table.getSelectedColumns()) {
			table.setColumnHeaderBackgroundColor(col, fontColor);	
		}
		manifestation.saveCellSettings();
		table.getRowHeaders().repaint();
	}

	@Override
	public Integer getRowFontStyle() {
		Integer commonStyle = null;
		
		for (Integer row : table.getSelectedRows()) {
			Integer aStyle = table.getRowHeaderFontStyle(row);
			if (commonStyle!=null && commonStyle!=aStyle) {
				return null;
			}
			commonStyle = aStyle;
		}
		
		return commonStyle;
	}
	
	@Override
	public Integer getRowHeaderTextAttribute() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getRowHeaderTextAttribute(table.getSelectedRows()[0]);
		}
		Integer commonTextAttribute = null;
		
		for (Integer row : table.getSelectedRows()) {
			Integer aTextAttribute = table.getRowHeaderTextAttribute(row);
			if (commonTextAttribute!=null && commonTextAttribute!=aTextAttribute) {
				return null;
			}
			commonTextAttribute = aTextAttribute;
		}
		
		return commonTextAttribute;
	}
	
	@Override
	public Integer getColumnHeaderTextAttribute() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderTextAttribute(table.getSelectedColumns()[0]);
		}
		Integer commonTextAttribute = null;
		
		for (Integer col : table.getSelectedColumns()) {
			Integer aTextAttribute = table.getColumnHeaderTextAttribute(col);
			if (commonTextAttribute!=null && commonTextAttribute!=aTextAttribute) {
				return null;
			}
			commonTextAttribute = aTextAttribute;
		}
		
		return commonTextAttribute;
	}
	
	@Override
	public JVMFontFamily getRowHeaderFontName() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getRowHeaderFontName(table.getSelectedRows()[0]);
		}
		JVMFontFamily commonFont = null;
		
		for (Integer row : table.getSelectedRows()) {
			JVMFontFamily aFont = table.getRowHeaderFontName(row);
			if (commonFont !=null && !commonFont.equals(aFont)) {
				return null;
			}
			commonFont = aFont;
		}
		
		return commonFont;
	}

	@Override
	public Integer getColumnHeaderFontStyle() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderFontStyle(table.getSelectedColumns()[0]);
		}
		Integer commonStyle = null;
		
		for (Integer col : table.getSelectedColumns()) {
			Integer aStyle = table.getColumnHeaderFontStyle(col);
			if (commonStyle!=null && commonStyle!=aStyle) {
				return null;
			}
			commonStyle = aStyle;
		}
		
		return commonStyle;
	}

	@Override
	public Integer getRowHeaderFontSize() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getRowHeaderFontSize(table.getSelectedRows()[0]);
		}
		Integer commonSize = null;
		
		for (Integer row : table.getSelectedRows()) {
			Integer aSize = table.getRowHeaderFontSize(row);
			if (commonSize!=null && commonSize!=aSize) {
				return null;
			}
			commonSize = aSize;
		}
		
		return commonSize;
	}

	@Override
	public Integer getColumnHeaderFontSize() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderFontSize(table.getSelectedColumns()[0]);
		}
		Integer commonSize = null;
		
		for (Integer col : table.getSelectedColumns()) {
			Integer aSize = table.getColumnHeaderFontSize(col);
			if (commonSize!=null && commonSize!=aSize) {
				return null;
			}
			commonSize = aSize;
		}
		
		return commonSize;
	}

	@Override
	public Color getRowHeaderFontColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getRowHeaderFontColor(table.getSelectedRows()[0]);
		}
		Color commonColor = null;
		
		for (Integer row : table.getSelectedRows()) {
			Color aColor = table.getRowHeaderFontColor(row);
			if (commonColor!=null && commonColor!=aColor) {
				return null;
			}
			commonColor = aColor;
		}
		
		return commonColor;
	}
	
	@Override
	public Color getRowHeaderBorderColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getRowHeaderBorderColor(table.getSelectedRows()[0]);
		}
		Color commonColor = null;
		
		for (Integer row : table.getSelectedRows()) {
			Color aColor = table.getRowHeaderBorderColor(row);
			if (commonColor!=null && commonColor!=aColor) {
				return null;
			}
			commonColor = aColor;
		}
		
		return commonColor;
	}

	@Override
	public Color getColumnHeaderFontColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderFontColor(table.getSelectedColumns()[0]);
		}
		Color commonColor = null;
		
		for (Integer col : table.getSelectedColumns()) {
			Color aColor = table.getColumnHeaderFontColor(col);
			if (commonColor!=null && commonColor!=aColor) {
				return null;
			}
			commonColor = aColor;
		}
		
		return commonColor;
	}
	
	@Override
	public Color getColumnHeaderBorderColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderBorderColor(table.getSelectedColumns()[0]);
		}
		Color commonColor = null;
		
		for (Integer col : table.getSelectedColumns()) {
			Color aColor = table.getColumnHeaderBorderColor(col);
			if (commonColor!=null && commonColor!=aColor) {
				return null;
			}
			commonColor = aColor;
		}
		
		return commonColor;
	}
	
	@Override
	public Color getRowHeaderBackgroundColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getRowHeaderBackgroundColor(table.getSelectedRows()[0]);
		}
		Color commonColor = null;
		
		for (Integer row : table.getSelectedRows()) {
			Color aColor = table.getRowHeaderBackgroundColor(row);
			if (commonColor!=null && commonColor!=aColor) {
				return null;
			}
			commonColor = aColor;
		}
		
		return commonColor;
	}

	@Override
	public Color getColumnHeaderBackgroundColor() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderBackgroundColor(table.getSelectedColumns()[0]);
		}
		Color commonColor = null;
		
		for (Integer col : table.getSelectedColumns()) {
			Color aColor = table.getColumnHeaderBackgroundColor(col);
			if (commonColor!=null && commonColor!=aColor) {
				return null;
			}
			commonColor = aColor;
		}
		
		return commonColor;
	}

	@Override
	public JVMFontFamily getColumnHeaderFontName() {
		if (isSelectionContainsEmptyCells()) {
			return null;
		}
		if (isSingleCellSelection()) {
			return table.getColumnHeaderFontName(table.getSelectedColumns()[0]);
		}
		JVMFontFamily commonFont = null;
		
		for (Integer col : table.getSelectedColumns()) {
			JVMFontFamily aFont = table.getColumnHeaderFontName(col);
			if (commonFont !=null && !commonFont.equals(aFont)) {
				return null;
			}
			commonFont = aFont;
		}
		
		return commonFont;
	}

	
}