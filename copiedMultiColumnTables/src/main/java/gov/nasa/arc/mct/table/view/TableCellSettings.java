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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.table.view.BorderState.BorderEdge;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import java.awt.Color;
import java.text.SimpleDateFormat;

/**
 * Implements a holder of cell formatting settings.
 */
public class TableCellSettings {
	
	/** The default number of decimals to use when displaying numeric values. */
	public static final int DEFAULT_DECIMALS = 2;

	private ContentAlignment alignment = ContentAlignment.LEFT;
	private int numberOfDecimals = DEFAULT_DECIMALS;
	private AbstractComponent evaluator; //this may not be relevant to multicolumn tables TODO
	private LabelAbbreviations abbreviations;
	private DateFormatItem dateFormat = DateFormatItem.None;
	private BorderState borderState = new BorderState(BorderEdge.NONE.value());
	private JVMFontFamily cellFontName = TableFormattingConstants.defaultJVMFontFamily;
	private int fontStyle = TableFormattingConstants.defaultFontStyle;
	private int fontSize = TableFormattingConstants.defaultFontSize;
	private Color foregroundColor;
	private Color backgroundColor;
	private int textAttributeUnderline = TableFormattingConstants.UNDERLINE_OFF;
	
	/**
	 * Gets the cell content alignment.
	 * 
	 * @return the content alignment
	 */
	public ContentAlignment getAlignment() {
		return alignment;
	}
	
	/**
	 * Sets the cell content alignment.
	 * 
	 * @param alignment the new alignment
	 */
	public void setAlignment(ContentAlignment alignment) {
		this.alignment = alignment;
	}
	
	/**
	 * Gets the number of decimal places to display.
	 * 
	 * @return the number of decimals to show
	 */
	public int getNumberOfDecimals() {
		return numberOfDecimals;
	}
	
	/**
	 * Sets the number of decimals to display.
	 * 
	 * @param numberOfDecimals the number of decimals to show
	 */
	public void setNumberOfDecimals(int numberOfDecimals) {
		this.numberOfDecimals = numberOfDecimals;
	}
	
	/**
	 * Gets date formatter.
	 * 
	 * @return date formatter object
	 */
	public SimpleDateFormat getDateFormatter() {
		DateFormatItem dfSetting = this.getDateFormat();
		return dfSetting.getFormatter();
	}
	
	/**
	 * Gets date format.
	 * 
	 * @return date format
	 */
	public DateFormatItem getDateFormat() {
		return dateFormat;
	}
	
	/**
	 * Sets date format.
	 * 
	 * @param setting date format
	 */
	public void setDateFormat(DateFormatItem setting) {
		this.dateFormat = setting;
	}

	
	/**
	 * Gets the evaluator for the cell value, if any.
	 * 
	 * @return the evaluator to use, or null for no evaulator
	 */
	public AbstractComponent getEvaluator() {
		return evaluator;
	}
	
	/**
	 * Sets the evaluator for the cell value.
	 * 
	 * @param evaluator the evaluator, or null for no evaluator
	 */
	public void setEvaluator(AbstractComponent evaluator) {
		this.evaluator = evaluator;
	}

	/**
	 * Gets the abbreviations that have been chosen for this cell.
	 * 
	 * @return the cell abbreviation settings
	 */
	public LabelAbbreviations getAbbreviations() {
		return abbreviations;
	}

	/**
	 * Sets the abbreviations for this cell.
	 * 
	 * @param abbreviations the label abbreviations for the cell
	 */
	public void setAbbreviations(LabelAbbreviations abbreviations) {
		this.abbreviations = abbreviations;
	}

	/** 
	 * Gets cell border state.
	 * @return state
	 */
	public BorderState getCellBorderState() {
		return borderState;
	}

	/**
	 * Sets cell border type.
	 * @param borderState the border state
	 */
	public void setCellBorderState(BorderState borderState) {
		this.borderState = borderState;
	}

	/** Get the font for the cell
	 * @return the font object of the cell
	 */
	public JVMFontFamily getCellFont() {
		return cellFontName;
	}

	/** Set the font for the cell
	 * @param cellFont
	 */
	public void setCellFont(JVMFontFamily cellFont) {
		this.cellFontName = cellFont;
	}

	/**Get the font style for this cell
	 * @return fontStyle the font style for this cell
	 */
	public int getFontStyle() {
		return fontStyle;
	}
	
	/**Get the font textAttribute for this cell
	 * @return textAttributeUnderline the font text attribute for this cell
	 */
	public int getTextAttributeUnderline() {
		return textAttributeUnderline;
	}

	/** Set the font style for this cell
	 * @param fontStyle the font style for this cell
	 */
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}
	
	/** Set the font style for this cell
	 * @param textAttribute the font style for this cell
	 */
	public void setTextAttributeUnderline(int textAttribute) {
		this.textAttributeUnderline = textAttribute;
	}

	/**Get the foreground color for this cell
	 * @return the foreground color for this cell
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}

	/**Set the foreground color for this cell
	 * @param foregroundColor
	 */
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	
	/**Get the background color for this cell
	 * @return the background color for this cell
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**Set the background color for this cell
	 * @param backgroundColor
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**Get the font size for this cell
	 * @return the font size for this cell
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**Set the font size for this cell
	 * @param fontSize font size for this cell
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	
	/**Get the font color for this cell
	 * Same as getForegroundColor, for font objects
	 * @return the font color for this cell
	 */
	public Color getFontColor() {
		return foregroundColor;
	}

	/**Set the font color for this cell
	 * @param afontColor font size for this cell
	 */
	public void setFontColor(Color afontColor) {
		this.foregroundColor = afontColor;
	}
	
}
