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

import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.view.BorderState.BorderEdge;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;

import java.lang.reflect.Array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the settings for a table view.
 */
public class TableSettings {
	
	final static Logger logger = LoggerFactory.getLogger(TableSettings.class);

	/** Separates values for multiple cells in a string of values. */
	private static final String CELL_SEPARATOR = ":";
	
//	/** Separates values for multiple rows in a string of values for an entire array. */
//	private static final String ROW_SEPARATOR = "/";
	
	/** The list of all available settings. */
	public static enum AvailableSettings {
		/** The table orientation setting. */
		TABLE_ORIENTATION,
		/** The order of each model column in the displayed table. */
		COLUMN_ORDER,
		/** The the widths of each table column. */
		COLUMN_WIDTHS,
		/** The row header alignments. */
		ROW_HEADER_ALIGNMENT,
		/** The column header alignments. */
		COLUMN_HEADER_ALIGNMENT,
		/** The heights of each table row. */
		ROW_HEIGHTS,
		/** Whether to show the grid lines within the table. */
		SHOW_GRID,
		/** Row header font names		 */
		ROW_HEADER_FONT_NAMES,
		/**
		 * Row Header font colors
		 */
		ROW_HEADER_FONT_COLORS,
		/**
		 * Row Header background colors
		 */
		ROW_HEADER_BACKGROUND_COLORS,
		/**
		 * Row Header font sizes
		 */
		ROW_HEADER_FONT_SIZES,
		/**
		 * Row header font styles
		 */
		ROW_HEADER_FONT_STYLES,
		/**
		 * Row header text attributes
		 */
		ROW_HEADER_TEXT_ATTRIBUTES,
		/**
		 * Row header border states	
		 */
		ROW_HEADER_BORDER_STATES,
		/**
		 * Row header border colors	
		 */
		ROW_HEADER_BORDER_COLORS,
		/**
		 * Col header font names	
		 */
		COL_HEADER_FONT_NAMES,
		/**
		 *  Col Header font colors
		 */
		COL_HEADER_FONT_COLORS,
		/**
		 * Col Header background colors
		 */
		COL_HEADER_BACKGROUND_COLORS,
		/**
		 * Col Header font sizes
		 */
		COL_HEADER_FONT_SIZES,
		/**
		 * Col header font names	
		 */
		COL_HEADER_FONT_STYLES,
		/**
		 * Col header text attributes
		 */
		COL_HEADER_TEXT_ATTRIBUTES,
		/**
		 * Col header border states	
		 */
		COL_HEADER_BORDER_STATES,
		/**
		 * Col header border color	
		 */
		COL_HEADER_BORDER_COLORS;
		
	}
	
	private TableOrientation orientation;
	private String columnOrder;
	private String columnWidths;
	private String rowHeaderAlignments;
	private String columnHeaderAlignments;
	private String rowHeights;
	private String rowHeaderFontNames;
	private String rowHeaderFontColors;
	private String rowHeaderBackgroundColors;
	private String rowHeaderFontSizes;
	private String rowHeaderFontStyles;
	private String rowHeaderTextAttributes;
	private String rowHeaderBorderStates;
	private String columnHeaderFontNames;
	private String columnHeaderFontSizes;
	private String columnHeaderFontStyles;
	private String columnHeaderTextAttributes;
	private String columnHeaderFontColors;
	private String columnHeaderBackgroundColors;
	private String columnHeaderBorderStates;
	private String rowHeaderBorderColors;
	private String columnHeaderBorderColors;
	private boolean showGrid = true;

	/**
	 * Gets the value of a settings as a string.
	 * 
	 * @param setting the setting to convert to a string value
	 * @return the string value of the setting
	 */
	public String getValue(TableSettings.AvailableSettings setting) {
		switch (setting) {
		case TABLE_ORIENTATION:
			return orientation!=null ? orientation.toString() : null;
			
		case COLUMN_ORDER:
			return columnOrder;

		case COLUMN_WIDTHS:
			return columnWidths;
	
		case ROW_HEIGHTS:
			return rowHeights;
			
		case ROW_HEADER_ALIGNMENT:
			return rowHeaderAlignments;
			
		case COLUMN_HEADER_ALIGNMENT:
			return columnHeaderAlignments;
			
		case SHOW_GRID:
			return Boolean.toString(showGrid);
			
		case ROW_HEADER_FONT_NAMES:
			return rowHeaderFontNames;
		
		case ROW_HEADER_FONT_COLORS:
			return rowHeaderFontColors;
			
		case ROW_HEADER_BACKGROUND_COLORS:
			return rowHeaderBackgroundColors;			
			
		case ROW_HEADER_FONT_SIZES:
			return rowHeaderFontSizes;
			
		case ROW_HEADER_FONT_STYLES:
			return rowHeaderFontStyles;
				
		case ROW_HEADER_TEXT_ATTRIBUTES:
			return rowHeaderTextAttributes;
			
		case ROW_HEADER_BORDER_STATES:
			return rowHeaderBorderStates;
			
		case ROW_HEADER_BORDER_COLORS:
			return rowHeaderBorderColors;
			
		case COL_HEADER_FONT_NAMES:
			return columnHeaderFontNames;
			
		case COL_HEADER_FONT_COLORS:
			return columnHeaderFontColors;
			
		case COL_HEADER_BACKGROUND_COLORS:
			return columnHeaderBackgroundColors;
			
		case COL_HEADER_FONT_SIZES:
			return columnHeaderFontSizes;
			
		case COL_HEADER_FONT_STYLES:
			return columnHeaderFontStyles;
			
		case COL_HEADER_TEXT_ATTRIBUTES:
			return columnHeaderTextAttributes;
			
		case COL_HEADER_BORDER_STATES:
			return columnHeaderBorderStates;
			
		case COL_HEADER_BORDER_COLORS:
			return columnHeaderBorderColors;

		default:
			return null;
		}
	}

	/**
	 * Sets the value of a setting from a string external representation.
	 * 
	 * @param setting the setting
	 * @param value the string value of the setting
	 */
	public void setValue(TableSettings.AvailableSettings setting, String value) {
		switch (setting) {
		case TABLE_ORIENTATION: {
			try {
				orientation = TableOrientation.valueOf(value);
			} catch (Exception ex) {
				// Ignore - nothing was persisted.
			}
			break;
		}
		case COLUMN_ORDER: {
			columnOrder = value;
			break;
		}
		case COLUMN_WIDTHS: {
			columnWidths = value;
			break;
		}
		case ROW_HEADER_ALIGNMENT: {
			rowHeaderAlignments = value;
			break;
		}
		case COLUMN_HEADER_ALIGNMENT: {
			columnHeaderAlignments = value;
			break;
		}
		case ROW_HEIGHTS: {
			rowHeights = value;
			break;
		}
		case SHOW_GRID: {
			showGrid = Boolean.valueOf(value); // N.B.: Does not throw format exceptions.
			break;
		}
		
		case ROW_HEADER_FONT_NAMES:
			rowHeaderFontNames = value;
			break;
			
		case ROW_HEADER_FONT_COLORS:
			rowHeaderFontColors = value;
			break;
			
		case ROW_HEADER_BACKGROUND_COLORS:
			rowHeaderBackgroundColors = value;
			break;
			
		case ROW_HEADER_FONT_SIZES:
			rowHeaderFontSizes  = value;
			break;
			
		case ROW_HEADER_FONT_STYLES:
			rowHeaderFontStyles  = value;
			break;
			
		case ROW_HEADER_TEXT_ATTRIBUTES:
			rowHeaderTextAttributes  = value;
			break;
			
		case ROW_HEADER_BORDER_STATES:
			rowHeaderBorderStates  = value;
			break;
			
		case ROW_HEADER_BORDER_COLORS:
			rowHeaderBorderColors  = value;
			break;
			
		case COL_HEADER_FONT_NAMES:
			columnHeaderFontNames  = value;
			break;
			
		case COL_HEADER_FONT_COLORS:
			columnHeaderFontColors = value;
			break;
			
		case COL_HEADER_BACKGROUND_COLORS:
			columnHeaderBackgroundColors = value;
			break;
			
		case COL_HEADER_FONT_SIZES:
			columnHeaderFontSizes  = value;
			break;
			
		case COL_HEADER_FONT_STYLES:
			columnHeaderFontStyles  = value;
			break;
			
		case COL_HEADER_TEXT_ATTRIBUTES:
			columnHeaderTextAttributes  = value;
			break;
			
		case COL_HEADER_BORDER_STATES:
			columnHeaderBorderStates  = value;
			break;
			
		case COL_HEADER_BORDER_COLORS:
			columnHeaderBorderColors  = value;
			break;
			
		default: {
			// ignore - not supported yet
			break;
		}
		}
	}
	
	/**
	 * Gets the table orientation setting.
	 * 
	 * @return the orientation, or null if not set
	 */
	public TableOrientation getOrientation() {
		return orientation;
	}

	/**
	 * Sets the table orientation setting.
	 * 
	 * @param orientation the new orientation
	 */
	public void setOrientation(TableOrientation orientation) {
		this.orientation = orientation;
	}
	
	/**
	 * Gets the widths of each column. If any width is less than zero,
	 * then it should be treated as unknown.
	 * 
	 * @return an array of column widths
	 */
	public int[] getColumnWidths() {
		return toIntegerArray(columnWidths);
	}
	
	/**
	 * Sets the widths of each column.
	 * 
	 * @param widths an array of column widths
	 */
	public void setColumnWidths(int[] widths) {
		columnWidths = fromIntegerArray(widths);
	}
	
	/**
	 * Gets the order of columns.
	 * 
	 * @return an array of model column indices, in the order they are displayed
	 */
	public int[] getColumnOrder() {
		return toIntegerArray(columnOrder);
	}
	
	/**
	 * Sets the order of columns.
	 * 
	 * @param order an array of model column indices, in the order they are displayed
	 */
	public void setColumnOrder(int[] order) {
		columnOrder = fromIntegerArray(order);
	}
	
	public ContentAlignment[] getRowHeaderAlignments() {
		return toEnumArray(ContentAlignment.class, rowHeaderAlignments, ContentAlignment.LEFT);
	}
	
	public void setRowHeaderAlignments(ContentAlignment[] alignments) {
		rowHeaderAlignments = fromEnumArray(alignments);
	}
	
	public ContentAlignment[] getColumnHeaderAlignments() {
		return toEnumArray(ContentAlignment.class, columnHeaderAlignments, ContentAlignment.LEFT);
	}
	
	public void setColumnHeaderAlignments(ContentAlignment[] alignments) {
		columnHeaderAlignments = fromEnumArray(alignments);
	}
	
	/** Get row header border state
	 * @return array of row header border states
	 */
	public BorderState[] getRowHeaderBorderStates() {
		return BorderStatesFromString(rowHeaderBorderStates);
	}
	
	/** Set the row header border states
	 * @param borderStates array of row header border edges
	 */
	public void setRowHeaderBorderStates(BorderState[] borderStates) {
		rowHeaderBorderStates = BorderStatesToString(borderStates);
	}
	
	/** Get col header border state
	 * @return array of col header border states
	 */
	public BorderState[] getColumnHeaderBorderStates() {
		return BorderStatesFromString(columnHeaderBorderStates);
	}
	
	/** Set the col header border states
	 * @param borderStates
	 */
	public void setColumnHeaderBorderStates(BorderState[] borderStates) {
		columnHeaderBorderStates = BorderStatesToString(borderStates);
	}
	
	/**
	 * Gets the height of each row.
	 * 
	 * @return and array of row heights
	 */
	public int[] getRowHeights() {
		return toIntegerArray(rowHeights);
	}
	
	/**
	 * Sets the height of each row.
	 * 
	 * @param heights an array of row heights
	 */
	public void setRowHeights(int[] heights) {
		rowHeights = fromIntegerArray(heights);
	}
	
	/**
	 * Gets the font style of each row header.
	 * 
	 * @return an array of row font styles
	 */
	public int[] getRowFontStyles() {
		return toIntegerArray(rowHeaderFontStyles);
	}
	
	/**
	 * Gets the font underline attribute of each row header.
	 * 
	 * @return an array of row underline attributes
	 */
	public int[] getRowHeaderTextAttributes() {
		return toIntegerArray(rowHeaderTextAttributes);
	}
	
	/**
	 * Sets the font style of each row.
	 * 
	 * @param styles an array of row font styles
	 */
	public void setRowFontStyles(int[] styles) {
		rowHeaderFontStyles = fromIntegerArray(styles);
	}
	/**
	 * Sets the text attribute of each row.
	 * 
	 * @param textAttributes an array of row font text attributes
	 */
	public void setRowTextAttributes(int[] textAttributes) {
		rowHeaderTextAttributes = fromIntegerArray(textAttributes);
	}
	
	/**
	 * Sets the text attribute of each column header.
	 * 
	 * @param textAttributes an array of column header font text attributes
	 */
	public void setColumnTextAttributes(int[] textAttributes) {
		columnHeaderTextAttributes = fromIntegerArray(textAttributes);
	}
	
	
	/**
	 * Gets the font style of each column header.
	 * 
	 * @return an array of column font styles
	 */
	public int[] getColumnFontStyles() {
		return toIntegerArray(columnHeaderFontStyles);
	}
	
	/**
	 * Gets the font underline attribute of each column header.
	 * 
	 * @return an array of column header underline attributes
	 */
	public int[] getColumnHeaderTextAttributes() {
		return toIntegerArray(columnHeaderTextAttributes);
	}
	
	/**
	 * Sets the font style of each column.
	 * 
	 * @param styles an array of column font styles
	 */
	public void setColumnFontStyles(int[] styles) {
		columnHeaderFontStyles = fromIntegerArray(styles);
	}
	
	/**
	 * Gets the font size of each row header.
	 * 
	 * @return an array of row font sizes
	 */
	public int[] getRowFontSizes() {
		return toIntegerArray(rowHeaderFontSizes);
	}
	
	/**
	 * Sets the font size of each row header.
	 * 
	 * @param sizes an array of row font sizes
	 */
	public void setRowFontSizes(int[] sizes) {
		rowHeaderFontSizes = fromIntegerArray(sizes);
	}
	
	//Font (Foreground) Colors
	
	/**
	 * Gets the font color of each row header.
	 * 
	 * @return an array of row font colors
	 */
	public int[] getRowFontColors() {
		return toIntegerArray(rowHeaderFontColors);
	}
	
	/**
	 * Sets the font color of each row header.
	 * 
	 * @param colors an array of row font colors
	 */
	public void setRowFontColors(int[] colors) {
		rowHeaderFontColors = fromIntegerArray(colors);
	}
	
	/**
	 * Gets the border color of each row header.
	 * 
	 * @return an array of row header border colors
	 */
	public int[] getRowHeaderBorderColors() {
		return toIntegerArray(rowHeaderBorderColors);
	}
	
	/**
	 * Sets the border color of each row header.
	 * 
	 * @param colors an array of row header border colors
	 */
	public void setRowHeaderBorderColors(int[] colors) {
		rowHeaderBorderColors = fromIntegerArray(colors);
	}
	
	/**
	 * Gets the font color of each col header.
	 * 
	 * @return an array of col header border colors
	 */
	public int[] getColumnHeaderBorderColors() {
		return toIntegerArray(columnHeaderBorderColors);
	}
	
	/**
	 * Sets the border color of each col header.
	 * 
	 * @param colors an array of col header border colors
	 */
	public void setColumnHeaderBorderColors(int[] colors) {
		columnHeaderBorderColors = fromIntegerArray(colors);
	}
	/**
	 * Gets the font color of each col header.
	 * 
	 * @return an array of col header font colors
	 */
	public int[] getColumnFontColors() {
		return toIntegerArray(columnHeaderFontColors);
	}
	
	/**
	 * Sets the font color of each col header.
	 * 
	 * @param colors an array of col header font colors
	 */
	public void setColumnFontColors(int[] colors) {
		columnHeaderFontColors = fromIntegerArray(colors);
	}
	
	//Background Colors
	
	/**
	 * Gets the Background color of each row header.
	 * 
	 * @return an array of row Background colors
	 */
	public int[] getRowBackgroundColors() {
		return toIntegerArray(rowHeaderBackgroundColors);
	}
	
	/**
	 * Sets the Background color of each row header.
	 * 
	 * @param colors an array of row Background colors
	 */
	public void setRowBackgroundColors(int[] colors) {
		rowHeaderBackgroundColors = fromIntegerArray(colors);
	}
	
	/**
	 * Gets the Background color of each col header.
	 * 
	 * @return an array of col header Background colors
	 */
	public int[] getColumnBackgroundColors() {
		return toIntegerArray(columnHeaderBackgroundColors);
	}
	
	/**
	 * Sets the Background color of each col header.
	 * 
	 * @param colors an array of col header Background colors
	 */
	public void setColumnBackgroundColors(int[] colors) {
		columnHeaderBackgroundColors = fromIntegerArray(colors);
	}
	
	/**
	 * Gets the font size of each column header.
	 * 
	 * @return an array of column font sizes
	 */
	public int[] getColumnFontSizes() {
		return toIntegerArray(columnHeaderFontSizes);
	}
	
	/**
	 * Sets the font size of each column.
	 * 
	 * @param sizes an array of column font sizes
	 */
	public void setColumnFontSizes(int[] sizes) {
		columnHeaderFontSizes = fromIntegerArray(sizes);
	}
	
	/**
	 * Gets the font name of each row header.
	 * 
	 * @return an array of row font names
	 */
	public JVMFontFamily[] getRowFontNames() {
		return toEnumArray(JVMFontFamily.class, rowHeaderFontNames, 
				TableFormattingConstants.defaultJVMFontFamily);
	}
	
	/**
	 * Sets the font name of each row header.
	 * 
	 * @param names an array of row font names
	 */
	public void setRowFontNames(JVMFontFamily[] names) {
		rowHeaderFontNames = fromEnumArray(names);
	}
	
	/**
	 * Gets the font name of each column header.
	 * 
	 * @return an array of column font names
	 */
	public JVMFontFamily[] getColumnFontNames() {
		return toEnumArray(JVMFontFamily.class, columnHeaderFontNames, 
				TableFormattingConstants.defaultJVMFontFamily);
	}
	
	/**
	 * Sets the font name of each column.
	 * 
	 * @param names an array of column font names
	 */
	public void setColumnFontNames(JVMFontFamily[] names) {
		columnHeaderFontNames = fromEnumArray(names);
	}
	
	
	
	/**
	 * Tests whether to show gridlines in the table.
	 * 
	 * @return true, if we should show gridlines in the table display
	 */
	public boolean isShowGrid() {
		return showGrid;
	}

	/**
	 * Sets whether to show gridlines in the table.
	 * 
	 * @param showGrid true, if we should show gridlines in the table
	 */
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	/**
	 * Converts from the string representation of an numeric array
	 * to an array of those values.
	 * 
	 * @param value the string representation
	 * @return
	 */
	protected int[] toIntegerArray(String value) {
		if (value==null || value.trim().isEmpty()) {
			return new int[0];
		}
		
		String[] values = value.trim().split(CELL_SEPARATOR);
		
		int[] numericValues = new int[values.length];
		for (int i=0; i<values.length; ++i) {
			try {
				numericValues[i] = Integer.parseInt(values[i]);
			} catch (NumberFormatException ex) {
				logger.error("Bad numeric value persisted for tabular view manifestation: {}", value);
				numericValues[i] = -1;
			}
		}
		
		return numericValues;
	}

	/**
	 * Converts from an array of numeric values to a string representation.
	 * 
	 * @param values an array of integer values
	 * @return the string representation of the array
	 */
	private String fromIntegerArray(int[] values) {
		StringBuilder s = new StringBuilder();
		
		for (int v : values) {
			if (s.length() > 0) {
				s.append(CELL_SEPARATOR);
			}
			s.append(Integer.toString(v));
		}
		
		return s.toString();
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Enum> T[] toEnumArray(Class<T> clazz, String value, T defaultValue) {
		if (value==null || value.trim().isEmpty()) {
			return (T[]) Array.newInstance(clazz, 0);
		}
		
		String[] values = value.trim().split(CELL_SEPARATOR);
		
		T[] numericValues = (T[]) Array.newInstance(clazz, values.length);
		for (int i=0; i<values.length; ++i) {
			try {
				numericValues[i] = (T) Enum.valueOf(clazz, values[i]);
			} catch (IllegalArgumentException ex) {
				logger.error("Bad enumeration value persisted for tabular view manifestation: {}", value);
				numericValues[i] = defaultValue;
			}
		}
		
		return numericValues;
	}

	private <T extends Enum> String fromEnumArray(T[] values) {
		StringBuilder s = new StringBuilder();
		
		for (T v : values) {
			if (s.length() > 0) {
				s.append(CELL_SEPARATOR);
			}
			s.append(v.toString());
		}
		
		return s.toString();
	}
	
	private String BorderStatesToString(BorderState[] borderStates) {
		StringBuilder rv = new StringBuilder();
		
		for (BorderState borderState : borderStates) {
			if (rv.length() > 0) {
				rv.append(CELL_SEPARATOR);
			}
			if (borderState.hasWestBorder()) {  
				rv.append("1" );
			} else {
				rv.append("0" );
			}
			rv.append("|");
			if (borderState.hasNorthBorder()) { 
				rv.append("1" ); 
			} else {
				rv.append("0" );
			}
			rv.append("|");
			if (borderState.hasEastBorder()) { 
				rv.append("1" );
			} else {
				rv.append("0" );
			}
			rv.append("|");
			if (borderState.hasSouthBorder()) {  
				rv.append("1" ); 
			} else {
				rv.append("0" );
			}
			
		}
		return rv.length() == 0 ?  BorderState.BorderEdge.NONE.name() : rv.toString();
	}
	
	private BorderState[] BorderStatesFromString(String value) {
		if (value==null || value.trim().isEmpty()) {
			return new BorderState[0];
		}
		
		String[] values = value.trim().split(CELL_SEPARATOR);
		
		BorderState[] borderStateValues = new BorderState[values.length];
		for (int i=0; i<values.length; ++i) {
			try {
				String[] edgeValues = values[i].trim().split("\\|");
				BorderState borderState = new BorderState(BorderEdge.NONE.value());
				if (Integer.parseInt(edgeValues[0]) == 1) {
					borderState.addBorderState(BorderEdge.WEST.value());
				} 
				if (Integer.parseInt(edgeValues[1]) == 1 ) {
					borderState.addBorderState(BorderEdge.NORTH.value());
				} 
				if (Integer.parseInt(edgeValues[2]) == 1) {
					borderState.addBorderState(BorderEdge.EAST.value());
				} 
				if (Integer.parseInt(edgeValues[3]) == 1) {
					borderState.addBorderState(BorderEdge.SOUTH.value());
				}
				borderStateValues[i] = borderState;
			} catch (Exception ex) {
				logger.error("Bad border state value persisted for tabular view manifestation: {}", value);
				borderStateValues[i] = new BorderState(BorderEdge.NONE.value());
			}
		}
		return borderStateValues;
	}
	
//	/**
//	 * Returns a string that encodes an array of values.
//	 * 
//	 * @param values the values to encode
//	 * @return a string representation of the array of values
//	 */
//	private String fromObjectArray(Object[][] values) {
//		StringBuilder s = new StringBuilder();
//		
//		for (int row=0; row<values.length; ++row) {
//			if (row > 0) {
//				s.append(ROW_SEPARATOR);
//			}
//			for (int col=0; col<values[row].length; ++col) {
//				if (col > 0) {
//					s.append(CELL_SEPARATOR);
//				}
//			}
//		}
//		
//		return s.toString();
//	}
	
}
