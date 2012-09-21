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
import java.awt.FontMetrics;

import javax.swing.JLabel;

/**
 * <p>Implements the algorithm for laying out the parts of the value
 * to be displayed in a table cell. The cell is rendered, usually, from a {@link DisplayedValue}
 * object that is the value of the table cell. That value consists
 * of three parts:</p>
 * 
 * <ol>
 *   <li>A cell label (any remaining fragment not placed in a header), which may be empty
 *   <li>A cell value, usually a telemetry value
 *   <li>A status string, which may be empty
 * </ol>
 * 
 * <p>In addition, the displayed value indicates a color to use for the cell
 * value and status string. The cell label is always displayed in black.</p>
 * 
 * <p>A table cell can be displayed with one of 4 alignments, defined as
 * enumerated values in {@link ContentAlignment}: left, centered, right, or
 * decimal. The 3 values are displayed as follows for the alignments.</p>
 * 
 * <p><em>Left alignment.</em> If the label is nonempty, the label
 * is shown at the left, followed by a space. Then the cell value is shown,
 * followed by the status.</p>
 * 
 * <p><em>Centered alignment.</em> The label, value, and status are
 * centered within the cell.</p>
 * 
 * <p><em>Right alignment.</em> The label is shown at the left, followed
 * by a space, if the label is nonempty. The value and status are shown at the
 * right.</p>
 * 
 * <p><em>Decimal alignment.</em> Same as right alignment, except that
 * the value is padded on the right so that decimals line up. The status values
 * will also line up for all right-aligned and decimal-aligned cells.</p>
 * 
 * <p><strong>Truncation of Label and Value</strong></p>
 * <p>If the cell does not have space to show the label and value in their
 * entirety, the following truncation rules apply.</p>
 * 
 * <ol>
 *   <li>The label is truncated on the right first, and an ellipsis is
 *   appended to the label whenever the label is truncated.
 *   <li>If there is still not enough room, the value is truncated on
 *   the right, and an ellipsis is shown. However, the status string
 *   is still shown as long as possible.
 *   <li>If there is not enough room to show an ellipsis for an empty
 *   label, an ellipsis for an empty value, and the status string,
 *   then a single ellipsis is shown.
 * </ol>
 */
public class TableCellFormatter {
	
	/** The string to show at the right end of a truncated string. */
	public static final String ELLIPSIS = "...";
	/** The status character of widest width. */
	public static final String WIDEST_STATUS_CHAR = "W";
	/** A string used to find the width to separate the label fragment from the value. */
	public static final String SPACE = " ";

	/** The default color, if the value to display does not define its
	 * own color.
	 */
	private Color defaultColor;
	private Color borderColor = Color.gray;
	
	private String cellLabel = "";
	private String cellValue = "";
	private String statusCode = "";
	private Color valueColor = Color.black;
	
	private int ellipsisWidth = 0;
	private int digitWidth = 0;
	private int decimalWidth = 0;
	private int statusCharWidth = 0;
	private int spaceWidth = 0;
	private int numberOfDecimals = 0;
	private int maxNumberOfDecimals = 0;
	
	// The locations of all the components to show.
	private int labelLocation = -1;
	private int labelClipWidth = 0;
	private int valueLocation = -1;
	private int valueClipWidth = 0;
	private int statusLocation = -1;
	private int statusClipWidth = 0;
	private final static int borderWidth = 1;

	private ContentAlignment alignment = ContentAlignment.RIGHT;
	
	TableCellFormatter() {
		JLabel dummy = new JLabel("");
		defaultColor = dummy.getForeground();
	}

	void getFixedStringWidths(FontMetrics fm) {
		ellipsisWidth = fm.stringWidth(ELLIPSIS);
		digitWidth = fm.stringWidth("0");
		decimalWidth = fm.stringWidth(".");
		statusCharWidth = fm.stringWidth(WIDEST_STATUS_CHAR);
		spaceWidth = fm.stringWidth(SPACE);
	}
	
	/**
	 * Calculate the position and clipped width of the label, value,
	 * and status.
	 * 
	 * @param fm the font metrics to be used for display
	 * @param width the total width of the table cell
	 */
	void layoutCell(FontMetrics fm, int width) {
		// Default is not to show any parts of the cell value.
		labelLocation = -1;
		labelClipWidth = 0;
		valueLocation = -1;
		valueClipWidth = 0;
		statusLocation = -1;
		statusClipWidth = 0;

		// If there wasn't room for at least 1 ellipsis and status char, then show just an ellipsis.
		if (width < ellipsisWidth + statusCharWidth) {
			valueLocation = borderWidth;
			valueClipWidth = ellipsisWidth;
			return;
		}
		
    	int labelWidth = 0;
    	if (cellLabel != null) {
    		labelWidth = fm.stringWidth(cellLabel);
    	}
    	
    	int valueWidth = fm.stringWidth(cellValue);
    	int valuePad = 0;

    	// Adjust the value width to account for padding to align decimals.
		if (alignment==ContentAlignment.DECIMAL && maxNumberOfDecimals > numberOfDecimals) {
			valuePad = digitWidth * (maxNumberOfDecimals - numberOfDecimals);
		}
		// Adjust so we always leave space for the decimal point, if any decimal points are shown.
		if (alignment==ContentAlignment.DECIMAL && maxNumberOfDecimals > 0 && numberOfDecimals==0) {
			valuePad += decimalWidth;
		}

		int maxLabelWidth = Math.max(ellipsisWidth, width - valueWidth - statusCharWidth - spaceWidth);
		
		// Show label if there's room to show ellipses for label and value, space separator, and status char.
		if (labelWidth > 0 && width >= 2*ellipsisWidth + spaceWidth + statusCharWidth) {
			labelClipWidth = Math.min(labelWidth, maxLabelWidth);
		}
		
		// If we're centering or left-aligning the value, we only need room for the actual status
		// width. Otherwise we pad the
		int statusWidth = fm.stringWidth(statusCode);
		if (alignment==ContentAlignment.LEFT || alignment==ContentAlignment.CENTER) {
			statusClipWidth = statusWidth;
		} else {
			statusClipWidth = statusCharWidth;
		}

		// Label (+ opt ellipsis); space; value (+ opt ellipsis); status char
		int maxValueWidth = width - statusClipWidth;
		if (labelClipWidth > 0) {
			maxValueWidth -= spaceWidth + ellipsisWidth;
		}
		if (maxValueWidth < ellipsisWidth) {
			maxValueWidth = ellipsisWidth;
		}

		// Show value if there's room for at least 1 ellipsis (for value) and status char.
		if (width >= ellipsisWidth + statusCharWidth) {
			valueClipWidth = Math.min(valueWidth, maxValueWidth);
		} else {
			statusClipWidth = 0;
		}

		if (alignment==ContentAlignment.RIGHT || alignment==ContentAlignment.DECIMAL) {
			labelLocation = borderWidth;
			statusLocation = width - statusCharWidth;
			valueLocation = statusLocation - valueClipWidth - valuePad;
		} else { // alignment==ContentAlignment.LEFT || alignment==ContentAlignment.CENTER
			int indent = 0;
			if (alignment == ContentAlignment.CENTER) {
				int totalWidth = labelClipWidth + valueClipWidth + statusClipWidth;
				if (labelClipWidth > 0) {
					totalWidth += spaceWidth;
				}
				indent = (width - totalWidth) / 2;
			}
			
			labelLocation = indent;
			if (labelClipWidth <= 0) {
				valueLocation = labelLocation;
			} else {
				valueLocation = labelLocation + labelClipWidth + spaceWidth;
			}
			statusLocation = valueLocation + valueClipWidth;
		}

		// Set the label and value clip widths to a sentinel value to indicate no
		// clipping, if there is room to show them without truncation.
		if (labelClipWidth >= labelWidth) {
			labelClipWidth = -1;
		}
		if (valueClipWidth >= valueWidth) {
			valueClipWidth = -1;
		}
		if (statusClipWidth >= statusWidth) {
			statusClipWidth = -1;
		}
	}

	/**
	 * Gets the label fragment to show within the cell, or an empty string
	 * if the fragment is null.
	 * 
	 * @return the label fragment to show, or an empty string if there is no label fragment
	 */
	String getCellLabel() {
		return (cellLabel != null ? cellLabel : "");
	}

	void setCellLabel(String cellLabel) {
		this.cellLabel = cellLabel;
	}

	String getCellValue() {
		return cellValue;
	}

	void setCellValue(String cellValue) {
		this.cellValue = cellValue;
	}

	String getStatusCode() {
		return statusCode;
	}

	void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	Color getValueColor() {
		return (valueColor!=null ? valueColor : defaultColor);
	}

	void setValueColor(Color valueColor) {
		this.valueColor = valueColor;
	}

	void setNumberOfDecimals(int numberOfDecimals) {
		this.numberOfDecimals = numberOfDecimals;
	}

	void setMaxNumberOfDecimals(int maxNumberOfDecimals) {
		this.maxNumberOfDecimals = maxNumberOfDecimals;
	}

	void setAlignment(ContentAlignment alignment) {
		this.alignment = alignment;
	}

	int getLabelLocation() {
		return labelLocation;
	}

	int getLabelClipWidth() {
		return labelClipWidth;
	}

	int getValueLocation() {
		return valueLocation;
	}

	int getValueClipWidth() {
		return valueClipWidth;
	}

	int getStatusLocation() {
		return statusLocation;
	}

	int getStatusClipWidth() {
		return statusClipWidth;
	}

	int getEllipsisWidth() {
		return ellipsisWidth;
	}

	Color getBorderColor() {
		return borderColor;
	}
    
	void setBorderColor(Color borderColor) {
    	if (borderColor != null) this.borderColor = borderColor;
    }

	public static int getBorderWidth() {
		return borderWidth;
	}
	
}
