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

import java.awt.FontMetrics;

import javax.swing.JLabel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TableCellFormatterTest {
	
	private static final String VOLTS = "Volts";
	private static final String PI = "3.14";
	
	private static final FontMetrics fm;
	private static final int digitWidth;
	private static final int ellipsisWidth;
	private static final int spaceWidth;
	private static final int statusWidth;
	private static final int piWidth;
	private static final int voltsWidth;
	private static double devicePixelTolerance = 2.0;
	
	
	static {
		JLabel label = new JLabel("");
		fm = label.getFontMetrics(label.getFont());
		
		digitWidth = fm.stringWidth("0");
		ellipsisWidth = fm.stringWidth("...");
		spaceWidth = fm.stringWidth(" ");
		statusWidth = fm.stringWidth("W");
		piWidth = fm.stringWidth(PI);
		voltsWidth = fm.stringWidth(VOLTS);
	}
	
	private TableCellFormatter formatter;
	
	@BeforeMethod
	public void init() {
		formatter = new TableCellFormatter();
		formatter.getFixedStringWidths(fm);
	}

	@Test(dataProvider="layoutTests")
	public void testLayout(
		String label, String value, String status, ContentAlignment alignment, int decimals, int maxDecimals, int width,
		int labelLocation, int labelClipWidth, int valueLocation, int valueClipWidth, int statusLocation, int statusClipWidth
	) {
		formatter.setCellLabel(label);
		formatter.setCellValue(value);
		formatter.setStatusCode(status);
		formatter.setAlignment(alignment);
		formatter.setNumberOfDecimals(decimals);
		formatter.setMaxNumberOfDecimals(maxDecimals);
		
		formatter.layoutCell(fm, width);
		
		// We're using floating-point tests here so that we can handle round-off errors of up to a pixel.
		assertPixelEquals(formatter.getLabelLocation(), labelLocation);
		assertPixelEquals(formatter.getLabelClipWidth(), labelClipWidth);
		assertPixelEquals(formatter.getValueLocation(), valueLocation);
		assertPixelEquals(formatter.getValueClipWidth(), valueClipWidth);
		assertPixelEquals(formatter.getStatusLocation(), statusLocation);
		assertPixelEquals(formatter.getStatusClipWidth(), statusClipWidth);
	}
	
	private void assertPixelEquals(int actual, int expected) {
		assertEquals((double) actual, (double) expected, devicePixelTolerance);
	}
	
	@DataProvider(name="layoutTests")
	private Object[][] getLayoutTests() {
		return new Object[][] {
				// label, value, status, alignment, decimals, maxDecimals, width, labelX, labelClip, valueX, valueClip, statusX, statusClip
				
				// Not enough room for anything but an ellipsis.
				{ null, "0", "", ContentAlignment.LEFT, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ "", "0", "", ContentAlignment.LEFT, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, PI, "", ContentAlignment.LEFT, 2, 2, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, "0", "", ContentAlignment.CENTER, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ "", "0", "", ContentAlignment.CENTER, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, PI, "", ContentAlignment.CENTER, 2, 2, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, "0", "", ContentAlignment.RIGHT, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ "", "0", "", ContentAlignment.RIGHT, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, PI, "", ContentAlignment.RIGHT, 2, 2, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, "0", "", ContentAlignment.DECIMAL, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ "", "0", "", ContentAlignment.DECIMAL, 0, 0, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				{ null, PI, "", ContentAlignment.DECIMAL, 2, 2, 1, -1, 0, 0, ellipsisWidth, -1, 0 },
				
				// Enough room for truncated value and status, but not label.
				// TODO: These cases fail on certain platforms - need to investigate
//				{ VOLTS, PI, "", ContentAlignment.LEFT, 2, 2, piWidth-1, 0, 0, 0, piWidth-1, piWidth-1, -1 },
//				{ VOLTS, PI, "", ContentAlignment.CENTER, 2, 2, piWidth-1, 0, 0, 0, piWidth-1, piWidth-1, -1 },
//				{ VOLTS, PI, "", ContentAlignment.RIGHT, 2, 2, piWidth-1, 0, 0, 0, piWidth-1-statusWidth, piWidth-1-statusWidth, -1 },
//				{ VOLTS, PI, "", ContentAlignment.DECIMAL, 2, 2, piWidth-1, 0, 0, 0, piWidth-1-statusWidth, piWidth-1-statusWidth, -1 },
				
				// Enough room for value and status plus truncated label.
				{ VOLTS, PI, "H", ContentAlignment.LEFT, 2, 2, piWidth+statusWidth+spaceWidth+ellipsisWidth,
					0, ellipsisWidth, ellipsisWidth+spaceWidth, -1, ellipsisWidth+spaceWidth+piWidth, -1 },
				{ VOLTS, PI, "H", ContentAlignment.CENTER, 2, 2, piWidth+statusWidth+spaceWidth+ellipsisWidth,
					0, ellipsisWidth, ellipsisWidth+spaceWidth, -1, ellipsisWidth+spaceWidth+piWidth, -1 },
				{ VOLTS, PI, "H", ContentAlignment.RIGHT, 2, 2, piWidth+statusWidth+spaceWidth+ellipsisWidth,
					0, ellipsisWidth, ellipsisWidth+spaceWidth, -1, ellipsisWidth+spaceWidth+piWidth, -1 },
				{ VOLTS, PI, "H", ContentAlignment.DECIMAL, 2, 2, piWidth+statusWidth+spaceWidth+ellipsisWidth,
					0, ellipsisWidth, ellipsisWidth+spaceWidth, -1, ellipsisWidth+spaceWidth+piWidth, -1 },

				// Enough room for value and status, with empty label.
				{ null, PI, "H", ContentAlignment.LEFT, 2, 2, piWidth+statusWidth+10,
					0, -1, 0, -1, piWidth, -1 },
				{ null, PI, "H", ContentAlignment.CENTER, 2, 2, piWidth+statusWidth+10,
					5, -1, 5, -1, piWidth+5, -1 },
				{ null, PI, "H", ContentAlignment.RIGHT, 2, 2, piWidth+statusWidth+10,
					0, -1, 10, -1, piWidth+10, -1 },
				{ null, PI, "H", ContentAlignment.DECIMAL, 2, 2, piWidth+statusWidth+10,
					0, -1, 10, -1, piWidth+10, -1 },
				{ null, PI, "H", ContentAlignment.DECIMAL, 2, 3, piWidth+statusWidth+digitWidth+10,
					0, -1, 10, -1, piWidth+digitWidth+10, -1 },

				// Enough room for value and status plus complete label.
				{ VOLTS, PI, "H", ContentAlignment.LEFT, 2, 2, voltsWidth+spaceWidth+piWidth+statusWidth+10,
					0, -1, voltsWidth+spaceWidth, -1, voltsWidth+spaceWidth+piWidth, -1 },
				{ VOLTS, PI, "H", ContentAlignment.CENTER, 2, 2, voltsWidth+spaceWidth+piWidth+statusWidth+10,
					5, -1, voltsWidth+spaceWidth+5, -1, voltsWidth+spaceWidth+piWidth+5, -1 },
				{ VOLTS, PI, "H", ContentAlignment.RIGHT, 2, 2, voltsWidth+spaceWidth+piWidth+statusWidth+10,
					0, -1, voltsWidth+spaceWidth+10, -1, voltsWidth+spaceWidth+piWidth+10, -1 },
				{ VOLTS, PI, "H", ContentAlignment.DECIMAL, 2, 2, voltsWidth+spaceWidth+piWidth+statusWidth+10,
					0, -1, voltsWidth+spaceWidth+10, -1, voltsWidth+spaceWidth+piWidth+10, -1 },
				{ VOLTS, PI, "H", ContentAlignment.DECIMAL, 2, 3, voltsWidth+spaceWidth+piWidth+statusWidth+digitWidth+10,
					0, -1, voltsWidth+spaceWidth+10, -1, voltsWidth+spaceWidth+piWidth+digitWidth+10, -1 },
		};
	}

}
