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
import gov.nasa.arc.mct.table.model.MultiColTableModel;
import gov.nasa.arc.mct.util.LafColor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.font.TextAttribute;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * <p>Implements a table cell renderer for the tabular display view
 * role. The cell is rendered, usually, from a {@link DisplayedValue}
 * object that is the value of the table cell. A {@link TableCellFormatter}
 * is used to determine the locations of the various parts of the table
 * cell value.
 */
@SuppressWarnings("serial")
public final class TableViewCellRenderer extends LightweightLabel implements TableCellRenderer {

	/**
	 * An empty <code>Border</code>. This field might not be used. To change the
	 * <code>Border</code> used by this renderer override the 
	 * <code>getTableCellRendererComponent</code> method and set the border
	 * of the returned component directly.
	 */
	private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
	protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;
	
    // We need a place to store the color the JLabel should be returned 
    // to after its foreground and background colors have been set 
    // to the selection background color. 
    // These ivars will be made protected when their names are finalized. 
    private Color unselectedForeground; 
    private Color unselectedBackground; 

	/** The color of an active cell. */
	private static final Color ACTIVE_COLOR = LafColor.TREE_SELECTION_BACKGROUND;
	
	/** The cell formatter used to determine the layout of the parts of the
	 * cell value.
	 */
	private TableCellFormatter formatter = new TableCellFormatter();
	
	/** The last font that was used to draw a cell. The widths of certain
	 * fixed strings are only calculated when this font changes.
	 */
	private Font cellFont = null;
	
	/**
	 * Creates a table cell renderer that initially has a border that shows
	 * no focus. Save away the default label color, in case values to display
	 * do not define their own colors. Also set the component name to be the
	 * same as the default table cell renderer used by JTable, to make UI
	 * automation more compatible with the default JTable behavior.
	 */
	public TableViewCellRenderer() {
		super();
		setBorder(getNoFocusBorder());
		setForeground(Color.BLACK);    //Default color - usually gets replaced
		setName("Table.cellRenderer");
		formatter.setBorderColor(getColor("userGrid"));
	}

	private Color getColor(String key) {
		return UIManager.getColor(key, getLocale());
	}

	private Border getBorder(String key) {
		return UIManager.getBorder(key, getLocale());
	}

	private Border getNoFocusBorder() {
		Border border = getBorder("Table.cellNoFocusBorder");
		if (System.getSecurityManager() != null) {
			if (border != null) return border;
			return SAFE_NO_FOCUS_BORDER;
		} else if (border != null) {
			if (noFocusBorder == null || noFocusBorder == DEFAULT_NO_FOCUS_BORDER) {
				return border;
			}
		}
		return noFocusBorder;
	}

	/**
	 * Overrides <code>JComponent.setForeground</code> to assign
	 * the unselected-foreground color to the specified color.
	 * 
	 * @param c set the foreground color to this value
	 */
	@Override
	public void setForeground(Color c) {
		super.setForeground(c); 
		unselectedForeground = c; 
	}

	/**
	 * Overrides <code>JComponent.setBackground</code> to assign
	 * the unselected-background color to the specified color.
	 *
	 * @param c set the background color to this value
	 */
	@Override
	public void setBackground(Color c) {
		super.setBackground(c); 
		unselectedBackground = c; 
	}

	/**
	 * Notification from the <code>UIManager</code> that the look and feel
	 * [L&F] has changed.
	 * Replaces the current UI object with the latest version from the 
	 * <code>UIManager</code>.
	 *
	 * @see JComponent#updateUI
	 */
	@Override
	public void updateUI() {
		super.updateUI(); 
		setForeground(null);
		setBackground(null);
	}

	/**
	 * Returns the table cell renderer.
	 * <p>
	 * During a printing operation, this method will be called with
	 * <code>isSelected</code> and <code>hasFocus</code> values of
	 * <code>false</code> to prevent selection and focus from appearing
	 * in the printed output. To do other customization based on whether
	 * or not the table is being printed, check the return value from
	 * {@link javax.swing.JComponent#isPaintingForPrint()}.
	 *
	 * @param table  the <code>JTable</code>
	 * @param value  the value to assign to the cell at
	 *			<code>[row, column]</code>
	 * @param isSelected true if cell is selected
	 * @param hasFocus true if cell has focus
	 * @param row  the row of the cell to render
	 * @param column the column of the cell to render
	 * @return the default table cell renderer
	 * @see javax.swing.JComponent#isPaintingForPrint()
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Color fg = null;
		Color bg = null;

		JTable.DropLocation dropLocation = table.getDropLocation();
		if (dropLocation != null
				&& !dropLocation.isInsertRow()
				&& !dropLocation.isInsertColumn()
				&& dropLocation.getRow() == row
				&& dropLocation.getColumn() == column) {

			fg = getColor("Table.dropCellForeground");
			bg = getColor("Table.dropCellBackground");

			isSelected = true;
		}

		if (isSelected) {
			super.setForeground(fg == null ? table.getSelectionForeground()
					: fg);
			super.setBackground(bg == null ? table.getSelectionBackground()
					: bg);
		} else {
			Color background = unselectedBackground != null
			? unselectedBackground
					: table.getBackground();
			if (background == null || background instanceof javax.swing.plaf.UIResource) {
				Color alternateColor = getColor("Table.alternateRowColor");
				if (alternateColor != null && row % 2 == 0)
					background = alternateColor;
			}
			super.setForeground(unselectedForeground != null
					? unselectedForeground
							: table.getForeground());
			super.setBackground(background);
		}

		/* Set up a border for the drawn component */
		assert table.getModel() instanceof MultiColTableModel;
		MultiColTableModel m = MultiColTableModel.class.cast(table.getModel());
		AbstractComponent comp = (AbstractComponent)m.getStoredValueAt(row, column);
		Color cellBackgroundColor = null;
		Font cellFont = table.getFont();
		setFont(cellFont);
		if (comp != null)  {
			TableCellSettings s = m.getCellSettings(m.getKey(comp));
			if (s.getCellFont() != null) {
				cellFont = new Font(s.getCellFont().name(),s.getFontStyle(),s.getFontSize());
			}
			if (s.getTextAttributeUnderline() == TextAttribute.UNDERLINE_ON) {
				cellFont = cellFont.deriveFont(TableFormattingConstants.underlineMap);
			}
			setFont(cellFont);
			formatter.getFixedStringWidths(getFontMetrics(getFont()));
			if (s.getBackgroundColor() != null) {
				cellBackgroundColor = s.getBackgroundColor();
			}
			BorderState b = s.getCellBorderState();
			assert b != null;
			boolean hasNorth = b.hasNorthBorder();
			boolean hasWest = b.hasWestBorder();
			boolean hasSouth = b.hasSouthBorder();
			boolean hasEast = b.hasEastBorder();
			int w = TableCellFormatter.getBorderWidth();

			Border outside = BorderFactory.createMatteBorder(hasNorth ? w : 0, hasWest ? w : 0, hasSouth ? w : 0, hasEast ? w : 0, formatter.getBorderColor());
			Border inside = BorderFactory.createEmptyBorder(hasNorth ? 0 : w, hasWest ? 0 : w, hasSouth ? 0 : w, hasEast ? 0 : w);
			setBorder(BorderFactory.createCompoundBorder(outside,inside));
		} else {
			setBorder(BorderFactory.createEmptyBorder());
		}
		
		/* Set background appropriate to our selection state */
		setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
		if (cellBackgroundColor != null) {
			setBackground(cellBackgroundColor);
		}
		
		/* Add a border inside of that if we have focus */
		if (hasFocus) {
			Border baseBorder  = getBorder();
			Border focusBorder = BorderFactory.createLineBorder(table.getSelectionBackground().brighter().brighter(), 1);
			setBorder(BorderFactory.createCompoundBorder(baseBorder, focusBorder));
		}

		if (!(value instanceof DisplayedValue)) {
			formatter.setCellValue(value!=null ? value.toString() : "");
			formatter.setCellLabel("");
			formatter.setStatusCode("");
			formatter.setValueColor(getForeground());
			formatter.setAlignment(ContentAlignment.RIGHT);
			formatter.setNumberOfDecimals(0);
		} else {
			DisplayedValue dv = (DisplayedValue) value;
			formatter.setCellValue(dv.getValue());
			formatter.setCellLabel(dv.getLabel());
			formatter.setStatusCode(dv.getStatusText());
			formatter.setValueColor(dv.hasColor() ? dv.getColor() : getForeground());
			formatter.setAlignment(dv.getAlignment());
			formatter.setNumberOfDecimals(dv.getNumberOfDecimals());
		}
		
		if (table.getModel() instanceof MultiColTableModel) {
			formatter.setMaxNumberOfDecimals(MultiColTableModel.class.cast(table.getModel()).getMaxDecimalsForColumn(column));
		} else {
			formatter.setMaxNumberOfDecimals(0);
		}
		
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
    	FontMetrics fm = getFontMetrics(getFont());
    	int height = fm.getHeight();
    	int ascent = fm.getAscent();
    	int y      = ascent + (getHeight() - height) / 2;

    	int width = g.getClip() == null ? getWidth() : (int) g.getClip().getBounds().getWidth();
    	formatter.layoutCell(fm, width);
    	
    	g.setColor(getForeground());
    	g.clipRect(0, 0, getWidth(), getHeight());
    	if (formatter.getLabelLocation() >= 0) {
    		drawStringWithEllipsis(g, formatter.getCellLabel(), formatter.getLabelLocation(), formatter.getLabelClipWidth(), height, y);
		}
		
		g.setColor(adjust(formatter.getValueColor(), getBackground()));
		if (formatter.getValueLocation() >= 0) {
			drawStringWithEllipsis(g, formatter.getCellValue(), formatter.getValueLocation(), formatter.getValueClipWidth(), height, y);
		}
		if (formatter.getStatusLocation() >= 0 && formatter.getStatusCode().length() > 0) {
			g.drawString(formatter.getStatusCode(), formatter.getStatusLocation(), y);
		}
	}
	
	/**
	 * Adjust the color for visibility on the specified base
	 */
	private Color adjust(Color c, Color base) {
		/* Nothing to change for black,
		 * and don't try to adjust up for "bright" colors */
		if (base.getRGB()  == Color.black.getRGB() ||
		    base.getRGB()  == Color.white.getRGB()) return c;
		
		double r = c.getRed() / 255.0;
		double g = c.getGreen() / 255.0;
		double b = c.getBlue() / 255.0;
		
		/* Brighten above dark backgrounds; darken below bright ones */
		r = (base.getRed() < 128)   ? (255 - base.getRed()) * r + base.getRed() :
			                          (base.getRed()) * r;
		g = (base.getGreen() < 128) ? (255 - base.getGreen()) * g + base.getGreen() :
            						  (base.getGreen()) * g;
		b = (base.getBlue() < 128)  ? (255 - base.getBlue()) * b + base.getBlue() :
                                      (base.getBlue()) * b;
			
		return new Color ((int) r, (int) g, (int) b);
	}
	
	/**
	 * Draws a string, truncating and showing an ellipsis if the string should be
	 * clipped to a maximum width.
	 * 
	 * @param g the graphics in which to display the string
	 * @param s the string to display
	 * @param x the horizontal position at which to display the string (character baseline is drawn at position x,y)
	 * @param clipWidth the width to which we should clip, or -1 if no clipping should occur
	 * @param height the font height used for drawing
	 * @param y the vertical position at which to display the string (character baseline is drawn at position x,y)
	 */
	private void drawStringWithEllipsis(Graphics g, String s, int x, int clipWidth, int height, int y) {
		if (s.length() == 0) {
			return;
		}
		
		if (clipWidth < 0) {
			g.drawString(s, x, y);
		} else if (clipWidth > 0) {
			Shape origClip = g.getClip();
			
			// Draw the truncated value if there's room to draw a portion while leaving
			// room for the ellipsis.
			int ellipsisWidth = formatter.getEllipsisWidth();
			if (clipWidth > ellipsisWidth) {
				g.clipRect(x, 0, clipWidth-ellipsisWidth, getHeight());
				g.drawString(s, x, y);
			}
			g.setClip(null);
			g.clipRect(0, 0, clipWidth, getHeight());
			g.drawString(TableCellFormatter.ELLIPSIS, x + clipWidth - ellipsisWidth, y);
			
			g.setClip(origClip);
		}
	}

}
