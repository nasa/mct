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

import gov.nasa.arc.mct.table.model.LabelChangeListener;
import gov.nasa.arc.mct.table.model.LabeledTableModel;
//import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.utils.ListenerManager;
import gov.nasa.arc.mct.table.utils.ListenerNotifier;
import gov.nasa.arc.mct.table.utils.NoSizeList;
import gov.nasa.arc.mct.table.view.BorderState;
import gov.nasa.arc.mct.table.view.ContentAlignment;
import gov.nasa.arc.mct.table.view.TableCellFormatter;
import gov.nasa.arc.mct.table.view.TableFormattingConstants;
import gov.nasa.arc.mct.table.view.BorderState.BorderEdge;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.util.LafColor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a table with row labels that has both a table model
 * and a labeling model. The table has both row and column headers,
 * and is embedded in a scroll pane, to control the header
 * locations.
 */
@SuppressWarnings("serial")
public class LabeledTable extends JPanel {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(LabeledTable.class);
	
	/** The delay, in milliseconds, between the time that the column widths
	 * or order changes and the time that a change event is sent to
	 * listeners.
	 */
	static final int TABLE_SAVE_DELAY = 1000;
	
	/** The delay, in milliseconds, between the time that the table detects
	 * a selection change and the time that a change event is sent to
	 * listeners.
	 */
	static final int SELECTION_CHANGE_DELAY = 50;
	
    private static final int DEFAULT_ROW_HEIGHT = 14;
    private static final int DEFAULT_COLUMN_WIDTH = 100;
    
    private static final int ROW_HEADER_MARGIN = 0;
	
	private Timer selectionChangeTimer = new Timer(SELECTION_CHANGE_DELAY, null);
	private ListenerManager listenerManager = new ListenerManager();

	private JTable table;
	private JList rowHeaders;
	
	private JList titleLabelList;
	
	private Border headerBorder = null;
	
	private NoSizeList<ContentAlignment> rowHeaderAlignments = new NoSizeList<ContentAlignment>();
	private NoSizeList<ContentAlignment> columnHeaderAlignments = new NoSizeList<ContentAlignment>();
	private NoSizeList<Integer> rowHeaderHeights = new NoSizeList<Integer>();
	private NoSizeList<JVMFontFamily> rowHeaderFontNames = new NoSizeList<JVMFontFamily>();
	private NoSizeList<JVMFontFamily> columnHeaderFontNames = new NoSizeList<JVMFontFamily>();
	private NoSizeList<Integer> rowHeaderFontStyles = new NoSizeList<Integer>();
	private NoSizeList<Integer> columnHeaderFontStyles = new NoSizeList<Integer>();
	private NoSizeList<Integer> rowHeaderTextAttributes = new NoSizeList<Integer>();
	private NoSizeList<BorderState> rowHeaderBorderStates = new NoSizeList<BorderState>();
	private NoSizeList<Integer> columnHeaderTextAttributes = new NoSizeList<Integer>();
	private NoSizeList<BorderState> columnHeaderBorderStates = new NoSizeList<BorderState>();
	private NoSizeList<Color> rowHeaderFontColors = new NoSizeList<Color>();
	private NoSizeList<Color> columnHeaderFontColors = new NoSizeList<Color>();	
	private NoSizeList<Color> rowHeaderBackgroundColors = new NoSizeList<Color>();
	private NoSizeList<Color> columnHeaderBackgroundColors = new NoSizeList<Color>();
	private NoSizeList<Color> rowHeaderBorderColors = new NoSizeList<Color>();
	private NoSizeList<Color> columnHeaderBorderColors = new NoSizeList<Color>();	
	private NoSizeList<Integer> rowHeaderFontSizes = new NoSizeList<Integer>();
	private NoSizeList<Integer> columnHeaderFontSizes = new NoSizeList<Integer>();
	private boolean isRestoringSettings = false;
	
	/**
	 * Creates a new table whose data is managed by a labeled table model.
	 * 
	 * @param model the model holding the table data
	 */
	public LabeledTable(final LabeledTableModel model) {
		selectionChangeTimer.setRepeats(false);
		
		// Set up a timer action that notifies listeners of changes to the table
		// selection.
		selectionChangeTimer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSelectionChanged();				
				table.getTableHeader().repaint();
				rowHeaders.repaint();
			}
			
		});
		
		table = new JTable(model) {
			@Override
			public void setRowHeight(int row, int rowHeight) {
				super.setRowHeight(row, rowHeight);
				updateRowHeaders();
			}

			@Override
			public void setRowHeight(int rowHeight) {				
				super.setRowHeight(rowHeight);
				updateRowHeaders();
			}		
		};
		table.setAutoCreateColumnsFromModel(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setTableFont(table);  
        table.setRowHeight(DEFAULT_ROW_HEIGHT);
       
        for (int col=0; col < model.getColumnCount(); ++col) {
        	table.getColumnModel().getColumn(col).setPreferredWidth(DEFAULT_COLUMN_WIDTH);
        }
        
		final TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
		final TableCellRenderer headerRenderer = new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				ContentAlignment alignment = LabeledTable.this.getColumnHeaderAlignment(column);
				label.setHorizontalAlignment(alignment.getComponentAlignment());
				Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
				if (headerBorder != null) label.setBorder(headerBorder);
				label.setBorder(BorderFactory.createCompoundBorder(label.getBorder(), padding));
				Font headerFont = new Font(LabeledTable.this.getColumnHeaderFontName(column).name(), 
						getColumnHeaderFontStyle(column).intValue(), 
						getColumnHeaderFontSize(column).intValue());
				
				
				if (LabeledTable.this.getColumnHeaderTextAttribute(column).equals(TextAttribute.UNDERLINE_ON)) {
					headerFont = headerFont.deriveFont(TableFormattingConstants.underlineMap);
				}
				label.setFont(headerFont);
				
				// Set the table header height to fit the font size
				int rowHeight = label.getFont().getSize() + 2; 
				Dimension d = label.getMinimumSize();
				d.setSize(d.getWidth() + 2*ROW_HEADER_MARGIN, rowHeight);
				label.setPreferredSize(d);
				
				int c = column - table.getSelectedColumn();
				if (c >= 0 && c < table.getSelectedColumnCount() && 
					row == -1 && table.getSelectedRowCount() == table.getRowCount()) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(table.getTableHeader().getBackground());
					label.setForeground(table.getTableHeader().getForeground());
				}
				label.setForeground(getColumnHeaderFontColor(column));
				label.setBackground(LabeledTable.this.getColumnHeaderBackgroundColor(column));
				BorderState b = getColumnHeaderBorderState(column);
				if (b != null) {
					boolean hasNorth = b.hasNorthBorder();
					boolean hasWest = b.hasWestBorder();
					boolean hasSouth = b.hasSouthBorder();
					boolean hasEast = b.hasEastBorder();
					int w = TableCellFormatter.getBorderWidth();
	
					Border outside = BorderFactory.createMatteBorder(hasNorth ? w : 0, hasWest ? w : 0, hasSouth ? w : 0, hasEast ? w : 0, 
							getColumnHeaderBorderColor(column));
					Border inside = BorderFactory.createEmptyBorder(hasNorth ? 0 : w, hasWest ? 0 : w, hasSouth ? 0 : w, hasEast ? 0 : w);
					label.setBorder(BorderFactory.createCompoundBorder(outside,inside));
				}
				return label;
			}
		};
		table.getTableHeader().setDefaultRenderer(headerRenderer);
		
		rowHeaders = new JList(model.getRowLabelModel());
		rowHeaders.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		rowHeaders.setCellRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean hasFocus) {
				JLabel label = (JLabel) defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, 0, index);
				ContentAlignment alignment = LabeledTable.this.getRowHeaderAlignment(index);
				label.setHorizontalAlignment(alignment.getComponentAlignment());
				Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
				if (headerBorder != null) label.setBorder(headerBorder);
				label.setBorder(BorderFactory.createCompoundBorder(label.getBorder(), padding));
				Font headerFont = new Font(LabeledTable.this.getRowHeaderFontName(index).name(), 
						getRowHeaderFontStyle(index).intValue(), 
						getRowHeaderFontSize(index).intValue());
				if (LabeledTable.this.getRowHeaderTextAttribute(index).equals(TextAttribute.UNDERLINE_ON)) {
					headerFont = headerFont.deriveFont(TableFormattingConstants.underlineMap);
				}
				label.setFont(headerFont);
				
				
				// Set the row header height to match the table row.
				int rowHeight = table.getRowHeight(index);
				Dimension d = label.getMinimumSize();
				d.setSize(d.getWidth() + 2*ROW_HEADER_MARGIN, rowHeight);
				label.setPreferredSize(d);
				
				int r = index - table.getSelectedRow();
				
				if (r >= 0 && r < table.getSelectedRowCount() && 
					table.getSelectedColumnCount() == table.getColumnCount()) {
					label.setBackground(table.getSelectionBackground());
					label.setForeground(table.getSelectionForeground());
				} else {
					label.setBackground(LabeledTable.this.getRowHeaderBackgroundColor(index));
					label.setForeground(table.getTableHeader().getForeground());
				}
				label.setForeground(getRowHeaderFontColor(index));
				label.setBackground(LabeledTable.this.getRowHeaderBackgroundColor(index));
				
				BorderState b = getRowHeaderBorderState(index);
				if (b != null) {
					boolean hasNorth = b.hasNorthBorder();
					boolean hasWest = b.hasWestBorder();
					boolean hasSouth = b.hasSouthBorder();
					boolean hasEast = b.hasEastBorder();
					int w = TableCellFormatter.getBorderWidth();
	
					Border outside = BorderFactory.createMatteBorder(hasNorth ? w : 0, hasWest ? w : 0, hasSouth ? w : 0, hasEast ? w : 0, 
							getRowHeaderBorderColor(index));
					Border inside = BorderFactory.createEmptyBorder(hasNorth ? 0 : w, hasWest ? 0 : w, hasSouth ? 0 : w, hasEast ? 0 : w);
					label.setBorder(BorderFactory.createCompoundBorder(outside,inside));
				}
				return label;
			}
			
		});
		
		titleLabelList = new JList(new String[]{" "});
		titleLabelList.setEnabled(false);
		
		setTableFont(titleLabelList);
		
		titleLabelList.setCellRenderer(new ListCellRenderer() {
	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean hasFocus) {
				
				Component comp = headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, 0, 0);
//				setTableFont(comp);
				
				// Set the row header height to match the table header height.
//				int rowHeight = comp.getFont().getSize() + 2; //no longer true with adjustable fonts
//				Dimension d = comp.getMinimumSize();
//				d.setSize(d.getWidth() + 2*ROW_HEADER_MARGIN, d.getHeight());
//				comp.setPreferredSize(d);
				comp.setBackground(Color.black);
				
				return comp;
			}
			
		});
		
		ListSelectionListener repaintingListener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				table.getTableHeader().repaint();
				rowHeaders.repaint();
			}
			
		};
		table.getSelectionModel().addListSelectionListener(repaintingListener);
		
		JPanel filler = new JPanel();
		filler.setMinimumSize(new Dimension(0,0));
		filler.setPreferredSize(new Dimension(0,0));
		filler.setOpaque(false);
		filler.setName("dummy");
		
		ConstraintBuilder builder = new ConstraintBuilder(this);
		builder.hvfill().weight(0,0).add(titleLabelList);
		builder.nw().add(table.getTableHeader());
		builder.span(2,1).hvfill().add(filler);

		builder.nextRow().nw().vfill().add(rowHeaders);
		builder.nw().vfill().add(table);

		setBackground(LafColor.WINDOW);
		
		setRowHeadersVisible(model.hasRowLabels() || model.isSkeleton());
		setColumnHeadersVisible(model.hasColumnLabels() || model.isSkeleton());
		model.addLabelChangeListener(new LabelChangeListener() {
			@Override
			public void labelsChanged() {
				setRowHeadersVisible(model.hasRowLabels() || model.isSkeleton());
				setColumnHeadersVisible(model.hasColumnLabels() || model.isSkeleton());
			}
		});
		
		// Add a listener for clicks in the table header.
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		
		header.addMouseListener(new TableHeaderListener(table, rowHeaders));

		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

			@Override
			public void columnAdded(TableColumnModelEvent e) {
				possiblySaveStateAfterDelay();
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				possiblySaveStateAfterDelay();
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// If the indices are different, a column was moved.
				if (e.getFromIndex() != e.getToIndex()) {
					possiblySaveStateAfterDelay();
				}
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				possiblySaveStateAfterDelay();
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// If the selection is not whole rows, then we should clear the
				// row list selection and set the new row list anchor position.
				if (table.getSelectedColumnCount() < table.getColumnCount()) {
					if (!e.getValueIsAdjusting()) {
						int anchorSelectionIndex = table.getSelectionModel().getAnchorSelectionIndex();
						if (anchorSelectionIndex > -1) {
							rowHeaders.getSelectionModel().setAnchorSelectionIndex(anchorSelectionIndex);
						} else {
							LOGGER.warn("table.getSelectionModel().getAnchorSelectionIndex(): {} is <= -1 (Row index out of range)", table.getSelectionModel().getAnchorSelectionIndex());
						}
						rowHeaders.clearSelection();
					}
				}
				
				// Notify listeners that the selection has changed.
				if (!e.getValueIsAdjusting()) {
					selectionChangeTimer.restart();
				}
			}

		});
		
		rowHeaders.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				try {
					if (!e.getValueIsAdjusting()
							&& rowHeaders.getSelectedIndex() > -1) {
						// Adjust the cell selection to match the row list selection.
						// We have to set the column selection first, so that the table knows
						// we've clicked on an entire row and doesn't try to deselect the
						// row selection immediately.

						table.setColumnSelectionInterval(0,
								table.getColumnCount() - 1);
						table.setRowSelectionInterval(
								rowHeaders.getMinSelectionIndex(),
								rowHeaders.getMaxSelectionIndex());
					}
					if (!e.getValueIsAdjusting()) {
						selectionChangeTimer.restart();
					}
				} catch (Exception ex) {
					LOGGER.warn("ListSelectionEvent Exception: {0}", ex);
				}
			}
			
		});
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {				
				if (e.getColumn() == TableModelEvent.ALL_COLUMNS || e.getType() != TableModelEvent.UPDATE) {
					updateColumnsFromModel(null);
					setRowHeadersVisible(LabeledTableModel.class.cast(table.getModel()).hasRowLabels());
					setColumnHeadersVisible(LabeledTableModel.class.cast(table.getModel()).hasColumnLabels());
					updateColumnsHeaderValuesOnly();
				}
			}
		});

	}
	
	/**
	 * Updates the columns in the table to match the current columns in the model.
	 * Optionally, sets all column widths to new values, otherwise preserves the existing
	 * widths. If too few widths are supplied, or the model has new columns, then the rest
	 * of the columns will get a default width.
	 * 
	 * @param columnWidths an array of new column widths, or null to preserve existing widths
	 */
	public void updateColumnsFromModel(int[] columnWidths) {
		LabeledTableModel model = LabeledTableModel.class.cast(table.getModel());
		TableColumnModel columnModel = table.getColumnModel();

		if (columnWidths == null) {
			columnWidths = new int[columnModel.getColumnCount()];
			for (int i=0; i < columnWidths.length; ++i) {
				columnWidths[i] = columnModel.getColumn(i).getPreferredWidth();
			}
		}
		
		while (columnModel.getColumnCount() > 0) {
			columnModel.removeColumn(columnModel.getColumn(0));
		}
		
		for (int i=0; i < model.getColumnCount(); ++i) {
			TableColumn column = new TableColumn(i, (columnWidths.length > i ? columnWidths[i] : DEFAULT_COLUMN_WIDTH));
			column.setHeaderValue(model.getColumnName(i));
			columnModel.addColumn(column);
		}
	}
	
	
	/**
	 * Update the header values of all columns.
	 */
	public void updateColumnsHeaderValuesOnly() {
		LabeledTableModel model = LabeledTableModel.class.cast(table.getModel());
		TableColumnModel columnModel = table.getColumnModel();
		// Note: cannot iterate over model.getColumnCount() because it is sometimes zero
		Enumeration<TableColumn>  allCols = columnModel.getColumns();
		int i = 0;
		while (allCols.hasMoreElements()) {
			TableColumn tc = allCols.nextElement();
			tc.setHeaderValue(model.getColumnName(i++));
		}
	}
	
	/**
	 * Sets a flag indicating that the table settings are being restored from
	 * persistent storage. While the flag is set, changes to table settings
	 * don't cause listeners to fire.
	 * 
	 * @param flag true, if the settings are being restored, false otherwise
	 */
	public void setRestoringSettings(boolean flag) {
		isRestoringSettings = flag;
	}
	
	private void possiblySaveStateAfterDelay() {
		if (!isRestoringSettings) {
			fireTableLayoutChanged();
		}
	}

	/**
	 * Add a listener for table visual layout changes. If the
	 * same listener is added more than once, only the first call
	 * has effect.
	 * 
	 * @param l
	 */
	public void addTableLayoutListener(TableLayoutListener l) {
		listenerManager.addListener(TableLayoutListener.class, l);
	}

	/**
	 * Remove a listener for table visual layout changes. If the
	 * listener was never added, the call has no effect.
	 * 
	 * @param l the listener to remove
	 */
	public void removeTableLayoutListener(TableLayoutListener l) {
		listenerManager.removeListener(TableLayoutListener.class, l);
	}
	
	/**
	 * Add a listener for selection changes on the table.
	 * 
	 * @param l the listener to add
	 */
	public void addTableSelectionListener(TableSelectionListener l) {
		listenerManager.addListener(TableSelectionListener.class, l);
	}
	
	/**
	 * Remove a listener for selection changes on the table.
	 * 
	 * @param l the listener to remove
	 */
	public void removeTableSelectionListener(TableSelectionListener l) {
		listenerManager.removeListener(TableSelectionListener.class, l);
	}
	
	/**
	 * Notify listeners that the visual layout of the table has changed.
	 */
	protected void fireTableLayoutChanged() {
		listenerManager.fireEvent(TableLayoutListener.class, new ListenerNotifier<TableLayoutListener>() {
			@Override
			public void notifyEvent(TableLayoutListener listener) {
				listener.tableChanged(this);
			}
			
		});
	}

	/**
	 * Notify listeners that the table selection has changed.
	 */
	protected void fireSelectionChanged() {
		final int[] selectedRows = table.getSelectedRows();
		final int[] selectedColumns = table.getSelectedColumns();
		
		listenerManager.fireEvent(TableSelectionListener.class, new ListenerNotifier<TableSelectionListener> () {
			@Override
			public void notifyEvent(TableSelectionListener listener) {
				listener.selectionChanged(selectedRows, selectedColumns);
			}
		});
	}
	
	/**
	 * Returns the indices of all selected rows.
	 * 
	 * @return an array of integers containing the indices of all selected rows, or an empty array if no row is selected
	 */
	public int[] getSelectedRows() {
		return table.getSelectedRows();
	}

	/**
	 * Returns the indices of all selected columns.
	 *  
	 * @return an array of integers containing the indices of all selected columns, or an empty array if no column is selected
	 */
	public int[] getSelectedColumns() {
		return table.getSelectedColumns();
	}
	
	/**
	 * Gets the widths of each column.
	 * 
	 * @return an array of column widths, in display order (not model order)
	 */
	public int[] getColumnWidths() {
		int[] widths = new int[table.getColumnCount()];

		for (int i=0; i<widths.length; ++i) {
			TableColumn col = table.getColumnModel().getColumn(i);
			widths[i] = col.getPreferredWidth();
		}

		return widths;
	}
	
	/**
	 * Sets the column widths to a given set of values.
	 * 
	 * @param widths an array of column widths, one entry for each column
	 */
	public void setColumnWidths(int[] widths) {
		boolean changed = false;
		
		for (int i=0; i<table.getColumnCount(); ++i) {
			int desiredWidth = DEFAULT_COLUMN_WIDTH;
			if (widths!=null && i < widths.length && widths[i] > 0) {
				desiredWidth = widths[i];
			}
			changed = setOneColumnWidth(i, desiredWidth) || changed;
		}
	}
	
	/**
	 * Sets the widths of selected columns in the table to the same value.
	 * 
	 * @param columnIndices an array with the indices of columns for which the width should be changed
	 * @param width the new width of the indicated columns
	 */
	public void setColumnWidths(int[] columnIndices, int width) {
		boolean changed = false;
		
		for (int columnIndex : columnIndices) {
			changed = setOneColumnWidth(columnIndex, width) || changed;
		}
	}
	
	private boolean setOneColumnWidth(int columnIndex, int width) {
		TableColumn col = table.getColumnModel().getColumn(columnIndex);
		if (width == col.getPreferredWidth()) {
			return false;
		} else {
			col.setPreferredWidth(width);
			return true;
		}
	}
	
	/**
	 * Gets the order of model columns in the display.
	 * 
	 * @return an array of model indices in the order they are shown in the displayed table
	 */
	public int[] getColumnOrder() {
		int[] order = new int[table.getColumnCount()];

		for (int i=0; i<order.length; ++i) {
			TableColumn col = table.getColumnModel().getColumn(i);
			order[i] = col.getModelIndex();
		}

		return order;
	}
	
	/**
	 * Sets the order of model columns in the display.
	 * 
	 * @param order an array of model indices in the order they are to be shown in the displayed table
	 */
	public void setColumnOrder(int[] order) {
		TableColumnModel colModel = table.getColumnModel();
		
		for (int i=0; i<colModel.getColumnCount() && i<order.length; ++i) {
			if (order[i]>=0 && order[i]!=colModel.getColumn(i).getModelIndex()) {
				// The wrong column was found at position i. The right column
				// must be to the right. Find the proper column for this position
				// and move it into position.
				for (int j=i+1; j < table.getColumnCount(); ++j) {
					if (colModel.getColumn(j).getModelIndex() == order[i]) {
						colModel.moveColumn(j, i);
						break;
					}
				}
			}
		}
	}

	/**
	 * Gets the heights of each row.
	 * 
	 * @return an array of row heights, in display order
	 */
/*	public int[] getRowHeights() {
		int[] heights = new int[table.getRowCount()];

		for (int i=0; i<heights.length; ++i) {
			heights[i] = table.getRowHeight(i);
		}

		return heights;
	}*/
	
	/**
	 * Gets the heights of each row.
	 * 
	 * @return an array of row heights, in display order
	 */
	public Integer[] getRowHeights() {
		Integer[] rowHeights = new Integer[table.getRowCount()];
		for (int i=0; i < rowHeights.length; ++i) {
			rowHeights[i] = getRowHeight(i);
		}
		return rowHeights;
	}
	
	/**
	 * Gets the row height of a single row .
	 * 
	 * @param rowIndex the index of the row 
	 * @return the row height of the row 
	 */
	public Integer getRowHeight(int rowIndex) {
		Integer rowHeight = rowHeaderHeights.get(rowIndex);
		return (rowHeight !=null ? rowHeight : 
			Integer.valueOf(TableFormattingConstants.defaultRowHeight));
	}
	
	/**
	 * Sets the row heights to a given set of values.
	 * 
	 * @param heights an array of row heights, one entry for each column
	 */
	public void setRowHeights(int[] heights) {

		
		rowHeaderHeights.clear();
		for (int i=0; i < heights.length && i < getTable().getRowCount(); ++i) {
			rowHeaderHeights.set(i, Integer.valueOf(heights[i]));
			setOneRowHeight(i, heights[i]);
		}
		
	}

	/**
	 * Sets the height of a single row. Both the table row height and
	 * the header row height are changed to match.
	 * 
	 * @param rowIndices the indices of the rows to change
	 * @param height the new height of the rows
	 */
	public void setRowHeights(int[] rowIndices, int height) {
		boolean changed = false;
		for (int rowIndex : rowIndices) {
			changed = setOneRowHeight(rowIndex, height) || changed;
		}
		
	}
	
	/**
	 * Sets the height of a single row. Both the table row height and
	 * the header row height are changed to match.
	 * 
	 * @param rowIndex the index of the rows to change
	 * @param height the new height of the rows
	 */
	public void setRowHeight(int rowIndex, int height) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderHeights.size() < rowIndex-1) {
				rowHeaderHeights.add(Integer.valueOf(TableFormattingConstants.defaultRowHeight));
			}
			rowHeaderHeights.set(rowIndex, Integer.valueOf(height));
		}
		boolean changed = false;
		changed = setOneRowHeight(rowIndex, height) || changed;
	}

	private boolean setOneRowHeight(int rowIndex, int height) {
		if (height == table.getRowHeight(rowIndex)) {
			return false;
		} else {
			table.setRowHeight(rowIndex, height);
			return true;
		}
	}
	
	/**
	 * Sets the alignments of all row headers.
	 * 
	 * @param alignments an array with the new row header alignments
	 */
	public void setRowHeaderAlignments(ContentAlignment[] alignments) {
		rowHeaderAlignments.clear();
		for (int i=0; i < alignments.length && i < getTable().getRowCount(); ++i) {
			rowHeaderAlignments.set(i, alignments[i]);
		}
	}
	
	/**
	 * Sets the alignment for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param newAlignment the new alignment for the for header
	 */
	public void setRowHeaderAlignment(int rowIndex, ContentAlignment newAlignment) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderAlignments.size() < rowIndex-1) {
				rowHeaderAlignments.add(ContentAlignment.LEFT);
			}
			rowHeaderAlignments.set(rowIndex, newAlignment);
		}
	}
	
	/**
	 * Sets the Border States of all row headers.
	 * 
	 * @param borderStates an array with the new row header BorderStates
	 */
	public void setRowHeaderBorderStates(BorderState[] borderStates) {
		rowHeaderBorderStates.clear();
		for (int i=0; i < borderStates.length && i < getTable().getRowCount(); ++i) {
			rowHeaderBorderStates.set(i, borderStates[i]);
		}
	}
	
	/**
	 * Sets the borderStates for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param newBorderState the new Border State for the for header
	 */
	public void setRowHeaderBorderState(int rowIndex, BorderState newBorderState) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderBorderStates.size() < rowIndex-1) {
				rowHeaderBorderStates.add(new BorderState(BorderEdge.NONE.value()));
			}
			rowHeaderBorderStates.set(rowIndex, newBorderState);
		}
	}
	
	/**
	 * Gets the font names of all row headers.
	 * 
	 * @return an array of row header font names
	 */
	public JVMFontFamily[] getRowHeaderFontNames() {
		JVMFontFamily[] fontNames = new JVMFontFamily[table.getRowCount()];
		for (int i=0; i < fontNames.length; ++i) {
			fontNames[i] = getRowHeaderFontName(i);
		}
		return fontNames;
	}
	
	/**
	 * Gets the font name of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return the font name of the row header
	 */
	public JVMFontFamily getRowHeaderFontName(int rowIndex) {
		JVMFontFamily fontName = rowHeaderFontNames.get(rowIndex);
		return (fontName !=null ? fontName : TableFormattingConstants.defaultJVMFontFamily);
	}
	
	/**
	 * Sets the font names of all row headers.
	 * 
	 * @param fontNames an array with the new row header alignments
	 */
	public void setRowHeaderFontNames(JVMFontFamily[] fontNames) {
		rowHeaderFontNames.clear();
		for (int i=0; i < fontNames.length && i < getTable().getRowCount(); ++i) {
			rowHeaderFontNames.set(i, fontNames[i]);
		}
	}
	
	/**
	 * Sets the font name for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param fontName the new font name for the row header
	 */
	public void setRowHeaderFontName(int rowIndex, JVMFontFamily fontName) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderFontNames.size() < rowIndex-1) {
				rowHeaderFontNames.add(TableFormattingConstants.defaultJVMFontFamily);
			}
			rowHeaderFontNames.set(rowIndex, fontName);
		}
	}
	
	
	/**
	 * Sets the font colors of all row headers.
	 * 
	 * @param fontColors an array with the new row font colors
	 */
	public void setRowHeaderFontColors(Color[] fontColors) {
		rowHeaderFontColors.clear();
		for (int i=0; i < fontColors.length && i < getTable().getRowCount(); ++i) {
			rowHeaderFontColors.set(i, fontColors[i]);
		}
	}
	/**
	 * Sets the font color for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param fontColor the new font color for the row header
	 */
	public void setRowHeaderFontColor(int rowIndex, Color fontColor) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderFontColors.size() < rowIndex-1) {
				rowHeaderFontColors.add(TableFormattingConstants.defaultFontColor);
			}
			rowHeaderFontColors.set(rowIndex, fontColor);
		}
	}
	
	/**
	 * Sets the border colors of all row headers.
	 * 
	 * @param borderColors an array with the new row header border colors
	 */
	public void setRowHeaderBorderColors(Color[] borderColors) {
		rowHeaderBorderColors.clear();
		for (int i=0; i < borderColors.length && i < getTable().getRowCount(); ++i) {
			rowHeaderBorderColors.set(i, borderColors[i]);
		}
	}
	/**
	 * Sets the border color for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param borderColor the new border color for the row header
	 */
	public void setRowHeaderBorderColor(int rowIndex, Color borderColor) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderBorderColors.size() < rowIndex-1) {
				rowHeaderBorderColors.add(TableFormattingConstants.defaultFontColor);
			}
			rowHeaderBorderColors.set(rowIndex, borderColor);
		}
	}
	
	/**
	 * Sets the background colors of all row headers.
	 * 
	 * @param bgColors an array with the new row background colors
	 */
	public void setRowHeaderBackgroundColors(Color[] bgColors) {
		rowHeaderBackgroundColors.clear();
		for (int i=0; i < bgColors.length && i < getTable().getRowCount(); ++i) {
			rowHeaderBackgroundColors.set(i, bgColors[i]);
		}
	}
	/**
	 * Sets the background color for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param bgColor the new background color for the row header
	 */
	public void setRowHeaderBackgroundColor(int rowIndex, Color bgColor) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderBackgroundColors.size() < rowIndex-1) {
				rowHeaderBackgroundColors.add(TableFormattingConstants.defaultBackgroundColor);
			}
			rowHeaderBackgroundColors.set(rowIndex, bgColor);
		}
	}
	
	/**
	 * Sets the font styles of all row headers.
	 * 
	 * @param fontStyles an array with the new row header font styles
	 */
	public void setRowHeaderFontStyles(Integer[] fontStyles) {
		rowHeaderFontStyles.clear();
		for (int i=0; i < fontStyles.length && i < getTable().getRowCount(); ++i) {
			rowHeaderFontStyles.set(i, fontStyles[i]);
		}
	}
	
	/**
	 * Sets the font style for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param fontStyle the new font style for the for header
	 */
	public void setRowHeaderFontStyle(int rowIndex, Integer fontStyle) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderFontStyles.size() < rowIndex-1) {
				rowHeaderFontStyles.add(Font.PLAIN);
			}
			rowHeaderFontStyles.set(rowIndex, Integer.valueOf(fontStyle));
		}
	}
	
	/**
	 * Sets the Text Attributes of all row headers.
	 * 
	 * @param attributes an array with the new row header TextAttributes
	 */
	public void setRowHeaderTextAttributes(Integer[] attributes) {
		rowHeaderTextAttributes.clear();
		for (int i=0; i < attributes.length && i < getTable().getRowCount(); ++i) {
			rowHeaderTextAttributes.set(i, attributes[i]);
		}
	}
	
	/**
	 * Sets the Text Attributes for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param attribute the new Text Attributes for the row header
	 */
	public void setRowHeaderTextAttribute(int rowIndex, Integer attribute) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderTextAttributes.size() < rowIndex-1) {
				rowHeaderTextAttributes.add(TableFormattingConstants.UNDERLINE_OFF);
			}
			rowHeaderTextAttributes.set(rowIndex, Integer.valueOf(attribute));
		}
	}
	
	/**
	 * Sets the font sizes of all row headers.
	 * 
	 * @param fontSizes an array with the new row header font sizes
	 */
	public void setRowHeaderFontSizes(Integer[] fontSizes) {
		rowHeaderFontSizes.clear();
		for (int i=0; i < fontSizes.length && i < getTable().getRowCount(); ++i) {
			rowHeaderFontSizes.set(i, fontSizes[i]);
		}
	}
	
	
	
	/**
	 * Sets the font size for a single row header.
	 * 
	 * @param rowIndex the index of the row header to change
	 * @param fontSize the new font size for the row header
	 */
	public void setRowHeaderFontSize(int rowIndex, Integer fontSize) {
		if (rowIndex < table.getRowCount()) {
			while (rowHeaderFontSizes.size() < rowIndex-1) {
				rowHeaderFontSizes.add(Integer.valueOf(TableFormattingConstants.defaultFontSize));
			}
			rowHeaderFontSizes.set(rowIndex, fontSize);
		}
	}
	
	/**
	 * Sets the font styles of all column headers.
	 * 
	 * @param fontStyles an array with the new column header font styles
	 */
	public void setColumnHeaderFontStyles(Integer[] fontStyles) {
		columnHeaderFontStyles.clear();
		for (int i=0; i < fontStyles.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderFontStyles.set(i, fontStyles[i]);
		}
	}
	
	/**
	 * Sets the font style for a single column header.
	 * 
	 * @param columnIndex the index of the column header to change
	 * @param fontStyle the new font style for the column header
	 */
	public void setColumnHeaderFontStyle(int columnIndex, Integer fontStyle) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderFontStyles.size() < columnIndex-1) {
				columnHeaderFontStyles.add(Font.PLAIN);
			}
			columnHeaderFontStyles.set(columnIndex, fontStyle);
		}
	}	
	
	/**
	 * Sets the font sizes of all column headers.
	 * 
	 * @param fontSizes an array with the new column header font sizes
	 */
	public void setColumnHeaderFontSizes(Integer[] fontSizes) {
		columnHeaderFontSizes.clear();
		for (int i=0; i < fontSizes.length && i < getTable().getRowCount(); ++i) {
			columnHeaderFontSizes.set(i, fontSizes[i]);
		}
	}
	
	/**
	 * Sets the font size for a single column header.
	 * 
	 * @param columnIndex the index of the column header to change
	 * @param fontSize the new font size for the column header
	 */
	public void setColumnHeaderFontSize(int columnIndex, Integer fontSize) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderFontSizes.size() < columnIndex-1) {
				columnHeaderFontSizes.add(TableFormattingConstants.defaultFontSize);
			}
			columnHeaderFontSizes.set(columnIndex, fontSize);
		}
	}	
	
	/**
	 * Gets the font names of all column headers.
	 * 
	 * @return an array of column header font names
	 */
	public JVMFontFamily[] getColumnHeaderFontNames() {
		JVMFontFamily[] fontNames = new JVMFontFamily[table.getColumnCount()];
		for (int i=0; i < fontNames.length; ++i) {
			fontNames[i] = getColumnHeaderFontName(i);
		}
		return fontNames;
	}
	
	/**
	 * Gets the font name of a single col header.
	 * 
	 * @param colIndex the index of the col header
	 * @return the font name of the col header
	 */
	public JVMFontFamily getColumnHeaderFontName(int colIndex) {
		JVMFontFamily fontName = columnHeaderFontNames.get(colIndex);
		return (fontName !=null ? fontName : TableFormattingConstants.defaultJVMFontFamily);
	}
	
	/**
	 * Sets the font names of all col headers.
	 * 
	 * @param fontNames an array with the new col header font names
	 */
	public void setColumnHeaderFontNames(JVMFontFamily[] fontNames) {
		columnHeaderFontNames.clear();
		for (int i=0; i < fontNames.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderFontNames.set(i, fontNames[i]);
		}
	}
	
	/**
	 * Sets the font name for a single col header.
	 * 
	 * @param columnIndex the index of the col header to change
	 * @param fontName the new font name for the col header
	 */
	public void setColumnHeaderFontName(int columnIndex, JVMFontFamily fontName) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderFontNames.size() < columnIndex-1) {
				columnHeaderFontNames.add(TableFormattingConstants.defaultJVMFontFamily);
			}
			columnHeaderFontNames.set(columnIndex, fontName);
		}
	}
	
	/**
	 * Sets the font color for all the  col headers.
	 * 
	 * @param fontColors an array of the new font colors for the columns
	 */
	public void setColumnHeaderFontColors(Color[] fontColors) {
		columnHeaderFontColors.clear();
		for (int i=0; i < fontColors.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderFontColors.set(i, fontColors[i]);
		}
	}
	
	/**
	 * Sets the font color for a single col header.
	 * 
	 * @param columnIndex the index of the col header to change
	 * @param fontColor the new font color for the col header
	 */
	public void setColumnHeaderFontColor(int columnIndex, Color fontColor) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderFontColors.size() < columnIndex-1) {
				columnHeaderFontColors.add(table.getTableHeader().getForeground());
			}
			columnHeaderFontColors.set(columnIndex, fontColor);
		}
	}
	
	/**
	 * Sets the border color for all the  col headers.
	 * 
	 * @param borderColors an array of the new border colors for the columns
	 */
	public void setColumnHeaderBorderColors(Color[] borderColors) {
		columnHeaderBorderColors.clear();
		for (int i=0; i < borderColors.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderBorderColors.set(i, borderColors[i]);
		}
	}
	
	/**
	 * Sets the border color for a single col header.
	 * 
	 * @param columnIndex the index of the col header to change
	 * @param borderColor the new border color for the col header
	 */
	public void setColumnHeaderBorderColor(int columnIndex, Color borderColor) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderBorderColors.size() < columnIndex-1) {
				columnHeaderBorderColors.add(TableFormattingConstants.defaultFontColor);
			}
			columnHeaderBorderColors.set(columnIndex, borderColor);
		}
	}
	
	/**
	 * Sets the background color for all the  col headers.
	 * 
	 * @param bgColors an array of the new background colors for the columns
	 */
	public void setColumnHeaderBackgroundColors(Color[] bgColors) {
		columnHeaderBackgroundColors.clear();
		for (int i=0; i < bgColors.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderBackgroundColors.set(i, bgColors[i]);
		}
	}
	
	/**
	 * Sets the background color for a single col header.
	 * 
	 * @param columnIndex the index of the col header to change
	 * @param bgColor the new background for the col header
	 */
	public void setColumnHeaderBackgroundColor(int columnIndex, Color bgColor) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderBackgroundColors.size() < columnIndex-1) {
				columnHeaderBackgroundColors.add(TableFormattingConstants.defaultBackgroundColor);
			}
			columnHeaderBackgroundColors.set(columnIndex, bgColor);
		}
	}
	
	/**
	 * Sets the Text Attributes of all column headers.
	 * 
	 * @param attributes an array with the new column header TextAttributes
	 */
	public void setColumnHeaderTextAttributes(Integer[] attributes) {
		columnHeaderTextAttributes.clear();
		for (int i=0; i < attributes.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderTextAttributes.set(i, attributes[i]);
		}
	}
	
	/**
	 * Sets the Text Attributes for a single column header.
	 * 
	 * @param colIndex the index of the column header to change
	 * @param attribute the new Text Attributes for the column header
	 */
	public void setColumnHeaderTextAttribute(int colIndex, Integer attribute) {
		if (colIndex < table.getColumnCount()) {
			while (columnHeaderTextAttributes.size() < colIndex-1) {
				columnHeaderTextAttributes.add(TableFormattingConstants.UNDERLINE_OFF);
			}
			columnHeaderTextAttributes.set(colIndex, attribute);
		}
	}
	
	
	/**
	 * Gets the alignments of all row headers.
	 * 
	 * @return an array of row header alignments
	 */
	public ContentAlignment[] getRowHeaderAlignments() {
		ContentAlignment[] alignments = new ContentAlignment[table.getRowCount()];
		for (int i=0; i < alignments.length; ++i) {
			alignments[i] = getRowHeaderAlignment(i);
		}
		return alignments;
	}
	
	/**
	 * Gets the alignment of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return the alignment of the row header
	 */
	public ContentAlignment getRowHeaderAlignment(int rowIndex) {
		ContentAlignment alignment = rowHeaderAlignments.get(rowIndex);
		return (alignment!=null ? alignment : ContentAlignment.LEFT);
	}
	
	/**
	 * Gets the border state of all row headers.
	 * 
	 * @return an array of row header border states
	 */
	public BorderState[] getRowHeaderBorderStates() {
		BorderState[] borderStates = new BorderState[table.getRowCount()];
		for (int i=0; i < borderStates.length; ++i) {
			borderStates[i] = getRowHeaderBorderState(i);
		}
		return borderStates;
	}
	
	/**
	 * Gets the border state of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return the border states of the row header
	 */
	public BorderState getRowHeaderBorderState(int rowIndex) {
		BorderState borderState = rowHeaderBorderStates.get(rowIndex);
		return (borderState !=null ? borderState : new BorderState(BorderEdge.NONE.value()));
	}
	
	/**
	 * Gets the font styles of all row headers.
	 * 
	 * @return an array of row header font styles
	 */
	public Integer[] getRowHeaderFontStyles() {
		Integer[] fontStyles = new Integer[table.getRowCount()];
		for (int i=0; i < fontStyles.length; ++i) {
			fontStyles[i] = getRowHeaderFontStyle(i);
		}
		return fontStyles;
	}
	
	/**
	 * Gets the font style of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return the font style of the row header
	 */
	public Integer getRowHeaderFontStyle(int rowIndex) {
		Integer fontStyle = rowHeaderFontStyles.get(rowIndex);
		return (fontStyle !=null ? fontStyle : Integer.valueOf(Font.PLAIN));
	}
	
	/**
	 * Gets the Text Attribute of all row headers.
	 * 
	 * @return an array of row header TextAttributes
	 */
	public Integer[] getRowHeaderTextAttributes() {
		Integer[] textAttributes = new Integer[table.getRowCount()];
		for (int i=0; i < textAttributes.length; ++i) {
			textAttributes[i] = getRowHeaderTextAttribute(i);
		}
		return textAttributes;
	}
	
	/**
	 * Gets the Text Attribute of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return the Text Attribute of the row header
	 */
	public Integer getRowHeaderTextAttribute(int rowIndex) {
		Integer textAttribute = rowHeaderTextAttributes.get(rowIndex);
		return (textAttribute !=null ? textAttribute : 
			Integer.valueOf(TableFormattingConstants.UNDERLINE_OFF));
	}
	
	/**
	 * Gets the Text Attribute of all column headers.
	 * 
	 * @return an array of column header TextAttributes
	 */
	public Integer[] getColumnHeaderTextAttributes() {
		Integer[] textAttributes = new Integer[table.getColumnCount()];
		for (int i=0; i < textAttributes.length; ++i) {
			textAttributes[i] = getColumnHeaderTextAttribute(i);
		}
		return textAttributes;
	}
	
	/**
	 * Gets the Text Attribute of a single column header.
	 * 
	 * @param colIndex the index of the column header
	 * @return textAttribute the Text Attribute of the column header
	 */
	public Integer getColumnHeaderTextAttribute(int colIndex) {
		Integer textAttribute = columnHeaderTextAttributes.get(colIndex);
		return (textAttribute !=null ? textAttribute : 
			Integer.valueOf(TableFormattingConstants.UNDERLINE_OFF));
	}
	
	/**
	 * Gets the font color of all row headers.
	 * 
	 * @return fontColors an array of row header font colors
	 */
	public Color[] getRowHeaderFontColors() {
		Color[] fontColors = new Color[table.getRowCount()];
		for (int i=0; i < fontColors.length; ++i) {
			fontColors[i] = getRowHeaderFontColor(i);
		}
		return fontColors;
	}
	
	/**
	 * Gets the font color of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return fontColor the font color of the row header
	 */
	public Color getRowHeaderFontColor(int rowIndex) {
		Color fontColor = rowHeaderFontColors.get(rowIndex);
		return (fontColor !=null ? fontColor : TableFormattingConstants.defaultFontColor);
	}
	
	/**
	 * Gets the border color of all row headers.
	 * 
	 * @return borderColors an array of row header border colors
	 */
	public Color[] getRowHeaderBorderColors() {
		Color[] borderColors = new Color[table.getRowCount()];
		for (int i=0; i < borderColors.length; ++i) {
			borderColors[i] = getRowHeaderBorderColor(i);
		}
		return borderColors;
	}
	
	/**
	 * Gets the border color of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return fontColor the border color of the row header
	 */
	public Color getRowHeaderBorderColor(int rowIndex) {
		Color borderColor = rowHeaderBorderColors.get(rowIndex);
		return (borderColor !=null ? borderColor : TableFormattingConstants.defaultFontColor);
	}
	
	/**
	 * Gets the background color of all row headers.
	 * 
	 * @return bgColors an array of row header background colors
	 */
	public Color[] getRowHeaderBackgroundColors() {
		Color[] bgColors = new Color[table.getRowCount()];
		for (int i=0; i < bgColors.length; ++i) {
			bgColors[i] = getRowHeaderBackgroundColor(i);
		}
		return bgColors;
	}
	
	/**
	 * Gets the background color of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return bgColor the background color of the row header
	 */
	public Color getRowHeaderBackgroundColor(int rowIndex) {
		Color bgColor = rowHeaderBackgroundColors.get(rowIndex);
		return (bgColor !=null ? bgColor : TableFormattingConstants.defaultBackgroundColor);
	}
	
	/**
	 * Gets the font sizes of all row headers.
	 * 
	 * @return an array of row header font sizes
	 */
	public Integer[] getRowHeaderFontSizes() {
		Integer[] fontSizes = new Integer[table.getRowCount()];
		for (int i=0; i < fontSizes.length; ++i) {
			fontSizes[i] = getRowHeaderFontSize(i);
		}
		return fontSizes;
	}
	
	/**
	 * Gets the font size of a single row header.
	 * 
	 * @param rowIndex the index of the row header
	 * @return the font size of the row header
	 */
	public Integer getRowHeaderFontSize(int rowIndex) {
		Integer fontSize = rowHeaderFontSizes.get(rowIndex);
		return (fontSize !=null ? fontSize : 
			Integer.valueOf(TableFormattingConstants.defaultFontSize));
	}
	
	/**
	 * Gets the font style of all column headers.
	 * 
	 * @return an array of column header font styles
	 */
	public Integer[] getColumnHeaderFontStyles() {
		Integer[] fontStyles = new Integer[table.getColumnCount()];
		for (int i=0; i < fontStyles.length; ++i) {
			fontStyles[i] = getColumnHeaderFontStyle(i);
		}
		return fontStyles;
	}
	
	/**
	 * Gets the font style of a single column header.
	 * 
	 * @param colIndex the index of the column header
	 * @return the font style of the column header
	 */
	public Integer getColumnHeaderFontStyle(int colIndex) {
		Integer fontStyle = columnHeaderFontStyles.get(colIndex);
		return (fontStyle!=null ? fontStyle : Integer.valueOf(Font.PLAIN));
	}
	
	/**
	 * Gets the font sizes of all column headers.
	 * 
	 * @return an array of column header font sizes
	 */
	public Integer[] getColumnHeaderFontSizes() {
		Integer[] fontSizes = new Integer[table.getColumnCount()];
		for (int i=0; i < fontSizes.length; ++i) {
			fontSizes[i] = getColumnHeaderFontSize(i);
		}
		return fontSizes;
	}
	
	/**
	 * Gets the font size of a single column header.
	 * 
	 * @param colIndex the index of the column header
	 * @return the font size of the column header
	 */
	public Integer getColumnHeaderFontSize(int colIndex) {
		Integer fontSize = columnHeaderFontSizes.get(colIndex);
		return (fontSize!=null ? fontSize : TableFormattingConstants.defaultFontSize);
	}
	
	/**
	 * Gets the font colors of all column headers.
	 * 
	 * @return an array of column header font colors
	 */
	public Color[] getColumnHeaderFontColors() {
		Color[] fontColors = new Color[table.getColumnCount()];
		for (int i=0; i < fontColors.length; ++i) {
			fontColors[i] = getColumnHeaderFontColor(i);
		}
		return fontColors;
	}
	
	/**
	 * Gets the font color of a single column header.
	 * 
	 * @param colIndex the index of the column header
	 * @return the font color of the column header
	 */
	public Color getColumnHeaderFontColor(int colIndex) {
		Color fontColor = columnHeaderFontColors.get(colIndex);
		return (fontColor!=null ? fontColor : TableFormattingConstants.defaultFontColor);
	}
	
	/**
	 * Gets the border colors of all column headers.
	 * 
	 * @return an array of column header border colors
	 */
	public Color[] getColumnHeaderBorderColors() {
		Color[] borderColors = new Color[table.getColumnCount()];
		for (int i=0; i < borderColors.length; ++i) {
			borderColors[i] = getColumnHeaderFontColor(i);
		}
		return borderColors;
	}
	
	/**
	 * Gets the border color of a single column header.
	 * 
	 * @param colIndex the index of the column header
	 * @return the border color of the column header
	 */
	public Color getColumnHeaderBorderColor(int colIndex) {
		Color borderColor = columnHeaderBorderColors.get(colIndex);
		return (borderColor!=null ? borderColor : TableFormattingConstants.defaultFontColor);
	}
	
	/**
	 * Gets the background colors of all column headers.
	 * 
	 * @return bgColors an array of column header background colors
	 */
	public Color[] getColumnHeaderBackgroundColors() {
		Color[] bgColors = new Color[table.getColumnCount()];
		for (int i=0; i < bgColors.length; ++i) {
			bgColors[i] = getColumnHeaderBackgroundColor(i);
		}
		return bgColors;
	}
	
	/**
	 * Gets the background color of a single column header.
	 * 
	 * @param colIndex the index of the column header
	 * @return bgColor the background color of the column header
	 */
	public Color getColumnHeaderBackgroundColor(int colIndex) {
		Color bgColor = columnHeaderBackgroundColors.get(colIndex);
		return (bgColor!=null ? bgColor : TableFormattingConstants.defaultBackgroundColor);
	}

	/**
	 * Sets the alignments of all column headers.
	 * 
	 * @param alignments an array containing the new column header alignments
	 */
	public void setColumnHeaderAlignments(ContentAlignment[] alignments) {
		columnHeaderAlignments.clear();
		for (int i=0; i < alignments.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderAlignments.set(i, alignments[i]);
		}
	}
	
	/**
	 * Sets the alignment of a single column header.
	 * 
	 * @param columnIndex the index of the column
	 * @param newAlignment the new alignment for the column header
	 */
	public void setColumnHeaderAlignment(int columnIndex, ContentAlignment newAlignment) {
		if (columnIndex < table.getColumnCount()) {
			while (columnHeaderAlignments.size() < columnIndex-1) {
				columnHeaderAlignments.add(ContentAlignment.LEFT);
			}
			columnHeaderAlignments.set(columnIndex, newAlignment);
		}
	}
	
	/**
	 * Sets the Border States of all col headers.
	 * 
	 * @param borderStates an array with the new col header BorderStates
	 */
	public void setColumnHeaderBorderStates(BorderState[] borderStates) {
		columnHeaderBorderStates.clear();
		for (int i=0; i < borderStates.length && i < getTable().getColumnCount(); ++i) {
			columnHeaderBorderStates.set(i, borderStates[i]);
		}
	}
	
	/**
	 * Sets the borderStates for a single col header.
	 * 
	 * @param colIndex the index of the row header to change
	 * @param newBorderState the new Border State for the col header
	 */
	public void setColumnHeaderBorderState(int colIndex, BorderState newBorderState) {
		if (colIndex < table.getColumnCount()) {
			while (columnHeaderBorderStates.size() < colIndex-1) {
				columnHeaderBorderStates.add(new BorderState(BorderEdge.NONE.value()));
			}
			columnHeaderBorderStates.set(colIndex, newBorderState);
		}
	}
	
	/**
	 * Gets all the column header alignments.
	 * 
	 * @return an array of column header alignments
	 */
	public ContentAlignment[] getColummnHeaderAlignments() {
		ContentAlignment[] alignments = new ContentAlignment[table.getColumnCount()];
		for (int i=0; i < alignments.length; ++i) {
			alignments[i] = getColumnHeaderAlignment(i);
		}
		return alignments;
	}
	
	/**
	 * Gets a single column header alignment.
	 * 
	 * @param columnIndex the index of the column
	 * @return the alignment of the column header
	 */
	public ContentAlignment getColumnHeaderAlignment(int columnIndex) {
		ContentAlignment alignment = columnHeaderAlignments.get(columnIndex);
		return (alignment!=null ? alignment : ContentAlignment.LEFT);
	}
	
	/**
	 * Gets the border state of all col headers.
	 * 
	 * @return an array of col header border states
	 */
	public BorderState[] getColumnHeaderBorderStates() {
		BorderState[] borderStates = new BorderState[table.getColumnCount()];
		for (int i=0; i < borderStates.length; ++i) {
			borderStates[i] = getColumnHeaderBorderState(i);
		}
		return borderStates;
	}
	
	/**
	 * Gets the border state of a single col header.
	 * 
	 * @param colIndex the index of the col header
	 * @return the border states of the col header
	 */
	public BorderState getColumnHeaderBorderState(int colIndex) {
		BorderState borderState = columnHeaderBorderStates.get(colIndex);
		return (borderState !=null ? borderState : new BorderState(BorderEdge.NONE.value()));
	}

	/**
	 * Tests whether a grid is shown, dividing the table cells up with
	 * narrow lines.
	 * 
	 * @return true, if the grid is visible
	 */
	public boolean getShowGrid() {
		return table.getShowHorizontalLines();
	}
	
	/**
	 * Shows or hides the grid.
	 * 
	 * @param showGrid true, if the grid should be visible
	 */
	public void setShowGrid(boolean showGrid) {
		if (showGrid != getShowGrid()) {
			table.setShowGrid(showGrid);
//			possiblyFireTableLayoutChanged();
		}
	}
	
	/**
	 * Gets the table widget underlying the labeled table.
	 * 
	 * @return the table widget
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * Gets the widget for the row headers of the labeled table.
	 * 
	 * @return the row headers widget
	 */
	public JList getRowHeaders() {
		return rowHeaders;
	}

	/**
	 * Tests whether the row headers are visible.
	 * 
	 * @return true, if the row headers are visible
	 */
	public boolean isRowHeadersVisible() {
		return rowHeaders.isVisible();
	}
	
	/**
	 * Tests whether the column headers are visible.
	 * 
	 * @return true, if the column headers are visible
	 */
	public boolean isColumnHeadersVisible() {
		return table.getTableHeader().isVisible();
	}
	
	/**
	 * Shows or hides the row headers.
	 * 
	 * @param isVisible true, if the row headers should be shown
	 */
	public void setRowHeadersVisible(boolean isVisible) {
		rowHeaders.setVisible(isVisible);
		titleLabelList.setVisible(isVisible && isColumnHeadersVisible());
	}

	/**
	 * Shows or hides the column headers.
	 * 
	 * @param isVisible true, if the column headers should be shown
	 */
	public void setColumnHeadersVisible(boolean isVisible) {
		table.getTableHeader().setVisible(isVisible);
		titleLabelList.setVisible(isVisible && isRowHeadersVisible());
	}
	
	/**
	 * Controls whether the table title is visible in the upper-left corner.
	 * 
	 * @param visible true, if the title should be visible, false otherwise
	 */
	public void setTitleVisible(boolean visible) {
		titleLabelList.setVisible(visible);
	}

	/**
	 * Updates the drop mode allowed based on what type of table is being viewed.
	 * In a one dimensional table we only allow insertion in one direction, depending
	 * on the table orientation, while  in a two dimensional table we allow
	 * insertions in both directions.
	 */
	public void updateDropMode() {
		LabeledTableModel model = LabeledTableModel.class.cast(table.getModel());
		//if (model.getTableType() == TableType.TWO_DIMENSIONAL) {
		table.setDropMode(DropMode.ON_OR_INSERT);
		/*} else if (model.getOrientation() == TableOrientation.ROW_MAJOR) {
			table.setDropMode(DropMode.ON_OR_INSERT_ROWS);
		} else {
			table.setDropMode(DropMode.ON_OR_INSERT_COLS);
		}*/
	}
	
	/**
	 * Sets the font for a component to be the correct size for rendering
	 * in a table, as either a row or column header or a table cell.
	 * 
	 * @param comp the component that should be rendered in a table
	 */
	private void setTableFont(Component comp) {
	    
	    Integer fontSize = 12; // Default
	    try {
	    	Object fontSizeValue = UIManager.get("TableViewManifestation.fontSize");
	        if (fontSizeValue != null && fontSizeValue instanceof String) {
	        	fontSize = Integer.parseInt((String) fontSizeValue);
	        }
	    } catch (NumberFormatException nfe) {
	    	LOGGER.error("Could not parse font size as integer; using default");
		}
	    
		if (comp.getFont().getSize() != fontSize) {
			comp.setFont(comp.getFont().deriveFont((float) fontSize));
		}
	}
	
	/**
	 * Set the border to be placed around header elements.
	 * 
	 * @param border the border to surround header elements
	 */
	public void setHeaderBorder(Border border) {
		this.headerBorder = border;
	}

	/**
	 * Update row headers to match height of table
	 */
	private void updateRowHeaders() {
		if (rowHeaders != null) {
			/* Trick row headers into updating */
			rowHeaders.setFixedCellHeight(1);
			rowHeaders.setFixedCellHeight(-1);			
		}		
	}

}
