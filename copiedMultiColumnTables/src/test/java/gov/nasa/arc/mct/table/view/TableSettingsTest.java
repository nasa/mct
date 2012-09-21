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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.test.util.TestUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TableSettingsTest {

	private TableSettings settings;
	
	@BeforeMethod
	public void init() {
		settings = new TableSettings();
	}
	
	@Test
	public void testEmptySettings() {
		assertNull(settings.getOrientation());
		assertEquals(settings.getColumnWidths().length, 0);
		assertEquals(settings.getColumnOrder().length, 0);
		assertTrue(settings.isShowGrid());
		
		for (TableSettings.AvailableSettings setting : TableSettings.AvailableSettings.values()) {
			if (setting == TableSettings.AvailableSettings.SHOW_GRID) {
				assertEquals(settings.getValue(setting), Boolean.toString(true));
			} else {
				assertNull(settings.getValue(setting));
			}
		}
	}
	
	@Test
	public void testOrientation() {
		settings.setOrientation(TableOrientation.ROW_MAJOR);
		assertEquals(settings.getOrientation(), TableOrientation.ROW_MAJOR);
		assertEquals(settings.getValue(TableSettings.AvailableSettings.TABLE_ORIENTATION),
				TableOrientation.ROW_MAJOR.toString());

		settings.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(settings.getOrientation(), TableOrientation.COLUMN_MAJOR);
		assertEquals(settings.getValue(TableSettings.AvailableSettings.TABLE_ORIENTATION),
				TableOrientation.COLUMN_MAJOR.toString());
		
		settings.setValue(TableSettings.AvailableSettings.TABLE_ORIENTATION, TableOrientation.ROW_MAJOR.toString());
		assertEquals(settings.getOrientation(), TableOrientation.ROW_MAJOR);
	
		settings.setValue(TableSettings.AvailableSettings.TABLE_ORIENTATION, TableOrientation.COLUMN_MAJOR.toString());
		assertEquals(settings.getOrientation(), TableOrientation.COLUMN_MAJOR);
	}
	
	@Test(dataProvider="numericArrayTests")
	public void testRowHeights(int[] values) {
		settings.setRowHeights(values);
		int[] newValues = settings.getRowHeights();
		TestUtil.assertArraysEqual(newValues, values);
	}
	
	@Test(dataProvider="numericArrayTests")
	public void testColumnWidths(int[] values) {
		settings.setColumnWidths(values);
		int[] newValues = settings.getColumnWidths();
		TestUtil.assertArraysEqual(newValues, values);
	}
	
	@Test(dataProvider="numericArrayTests")
	public void testColumnOrder(int[] values) {
		settings.setColumnOrder(values);
		int[] newValues = settings.getColumnOrder();
		TestUtil.assertArraysEqual(newValues, values);
	}
	
	@DataProvider(name="numericArrayTests")
	public Object[][] getNumericArrayTests() {
		return new Object[][] {
				{ new int[0] },
				{ new int[] {0} },
				{ new int[] {0, 1, 2} },
				{ new int[] {0, 2, 1} }
		};
	}
	
	@Test(dataProvider="numericArrayConversionTests")
	public void testSetColumnWidthValue(String s, int[] expected) {
		settings.setValue(TableSettings.AvailableSettings.COLUMN_WIDTHS, s);
		assertEquals(settings.getValue(TableSettings.AvailableSettings.COLUMN_WIDTHS), s);
		
		int[] values = settings.getColumnWidths();
		TestUtil.assertArraysEqual(values, expected);
	}
	
	@Test(dataProvider="numericArrayConversionTests")
	public void testSetColumnOrderValue(String s, int[] expected) {
		settings.setValue(TableSettings.AvailableSettings.COLUMN_ORDER, s);
		assertEquals(settings.getValue(TableSettings.AvailableSettings.COLUMN_ORDER), s);
		
		int[] values = settings.getColumnOrder();
		TestUtil.assertArraysEqual(values, expected);
	}
	
	@DataProvider(name="numericArrayConversionTests")
	public Object[][] getNumericArrayConversionTests() {
		return new Object[][] {
				{ "", new int[0] },
				{ "0", new int[] {0} },
				{ " 0 ", new int[] {0} },
				{ "0:1:2", new int[] {0, 1, 2} },
				{ "0:2:1", new int[] {0, 2, 1} },
				{ "0:abc:1", new int[] {0, -1, 1} },
		};
	}
	
	@Test(dataProvider="alignmentArrayTests")
	public void testRowHeaderAlignment(ContentAlignment[] values) {
		settings.setRowHeaderAlignments(values);
		ContentAlignment[] newValues = settings.getRowHeaderAlignments();
		TestUtil.assertArraysEqual(newValues, values);
	}
	
	@Test(dataProvider="alignmentArrayTests")
	public void testColumnHeaderAlignment(ContentAlignment[] values) {
		settings.setColumnHeaderAlignments(values);
		ContentAlignment[] newValues = settings.getColumnHeaderAlignments();
		TestUtil.assertArraysEqual(newValues, values);
	}
	
	@DataProvider(name="alignmentArrayTests")
	public Object[][] getAlignmentArrayTests() {
		return new Object[][] {
				{ new ContentAlignment[0] },
				{ new ContentAlignment[] {ContentAlignment.LEFT} },
				{ new ContentAlignment[] {ContentAlignment.LEFT, ContentAlignment.CENTER, ContentAlignment.RIGHT} },
				{ new ContentAlignment[] {ContentAlignment.RIGHT, ContentAlignment.LEFT, ContentAlignment.CENTER} },
		};
	}
	
	@Test(dataProvider="alignmentArrayConversionTests")
	public void testRowHeaderAlignmentConversion(String s, ContentAlignment[] expected) {
		settings.setValue(TableSettings.AvailableSettings.ROW_HEADER_ALIGNMENT, s);
		assertEquals(settings.getValue(TableSettings.AvailableSettings.ROW_HEADER_ALIGNMENT), s);
		
		ContentAlignment[] values = settings.getRowHeaderAlignments();
		TestUtil.assertArraysEqual(values, expected);
	}
	
	@Test(dataProvider="alignmentArrayConversionTests")
	public void testColumnHeaderAlignmentConversion(String s, ContentAlignment[] expected) {
		settings.setValue(TableSettings.AvailableSettings.COLUMN_HEADER_ALIGNMENT, s);
		assertEquals(settings.getValue(TableSettings.AvailableSettings.COLUMN_HEADER_ALIGNMENT), s);
		
		ContentAlignment[] values = settings.getColumnHeaderAlignments();
		TestUtil.assertArraysEqual(values, expected);
	}
	
	@DataProvider(name="alignmentArrayConversionTests")
	public Object[][] getAlignmentArrayConversionTests() {
		return new Object[][] {
				{ "", new ContentAlignment[0] },
				{ "LEFT", new ContentAlignment[] {ContentAlignment.LEFT} },
				{ "CENTER", new ContentAlignment[] {ContentAlignment.CENTER} },
				{ "RIGHT", new ContentAlignment[] {ContentAlignment.RIGHT} },
				{ " LEFT ", new ContentAlignment[] {ContentAlignment.LEFT} },
				{ "LEFT:CENTER:RIGHT", new ContentAlignment[] {ContentAlignment.LEFT, ContentAlignment.CENTER, ContentAlignment.RIGHT} },
				{ "LEFT:abc:RIGHT", new ContentAlignment[] {ContentAlignment.LEFT, ContentAlignment.LEFT, ContentAlignment.RIGHT} },
		};
	}
	
	@Test
	public void testSetShowGrid() {
		assertTrue(settings.isShowGrid()); // The default setting.
		
		settings.setShowGrid(false);
		assertFalse(settings.isShowGrid());
		assertEquals(settings.getValue(TableSettings.AvailableSettings.SHOW_GRID), Boolean.toString(false));

		settings.setShowGrid(true);
		assertTrue(settings.isShowGrid());
		assertEquals(settings.getValue(TableSettings.AvailableSettings.SHOW_GRID), Boolean.toString(true));
		
		settings.setValue(TableSettings.AvailableSettings.SHOW_GRID, Boolean.toString(false));
		assertFalse(settings.isShowGrid());
		
		settings.setValue(TableSettings.AvailableSettings.SHOW_GRID, Boolean.toString(true));
		assertTrue(settings.isShowGrid());
	}
	
}
