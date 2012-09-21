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

/**
 * Implements a data object that represents a value to be displayed
 * in a table. The value has a text representation and a color.
 */
public class DisplayedValue {

	/** The text to display. */
	private String value = "";
	/** The status text to show next to the value. */
	private String statusText = "";
	/** The color to use when displaying the text. */
	private Color color = Color.BLACK;
	/** The label for the value. */
	private String label = "";
	/** The display alignment of the cell value. */
	private ContentAlignment alignment = ContentAlignment.RIGHT;
	/** The number of decimals used in the display of the cell value. */
	private int numberOfDecimals;
	/** Track whether or not we have a real color, or are using default */
	private boolean hasColorBeenSet = false;
	
	/**
	 * Gets the textual representation of the value.
	 * 
	 * @return the text to display
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the color to use when displaying the value.
	 * 
	 * @return the color to use when displaying the value
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Gets the status text to display next to the value.
	 * 
	 * @return the status text
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * Gets the label for the value. The label may be empty or null.
	 * 
	 * @return return the label for the value
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		if (label.isEmpty()) {
			return value + statusText;
		} else {
			return value + statusText + " " + label;
		}
	}
	
	/**
	 * Sets the label for the value to display. By default there is
	 * no label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Sets the status text that should be shown next to the value.
	 * 
	 * @param statusText the new status text
	 */
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	/**
	 * Sets the color to use for the value and the status text.
	 * 
	 * @param valueColor the new color to use for display
	 */
	public void setValueColor(Color valueColor) {
		this.hasColorBeenSet = true;
		this.color = valueColor;
	}

	/**
	 * Sets the value to display.
	 * 
	 * @param valueString the new value to display
	 */
	public void setValue(String valueString) {
		this.value = valueString;
	}

	/** Gets the alignment of the cell. */
	public ContentAlignment getAlignment() {
		return alignment;
	}

	/** Sets the alignment of the cell value. */
	public void setAlignment(ContentAlignment alignment) {
		this.alignment = alignment;
	}

	/**
	 * Gets the number of decimals used to display the cell value.
	 * 
	 * @return the number of decimals in the display
	 */
	public int getNumberOfDecimals() {
		return numberOfDecimals;
	}
	
	/**
	 * Sets the number of decimals used to display the cell value.
	 * 
	 * @param numberOfDecimals the number of decimals in the displayed value
	 */
	public void setNumberOfDecimals(int numberOfDecimals) {
		this.numberOfDecimals = numberOfDecimals;
	}
	
	/**
	 * Check to see if this Displayed Value has been given a specific color, 
	 * instead of the default.
	 * 
	 * @return true if the color has been specified, false if using default
	 */
	public boolean hasColor() {
		return hasColorBeenSet;
	}

}
