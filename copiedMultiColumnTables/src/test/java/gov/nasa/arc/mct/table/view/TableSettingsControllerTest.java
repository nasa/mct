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

import java.awt.Color;

import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;

import org.testng.annotations.Test;

public class TableSettingsControllerTest {

	@Test
	public void testAddRemoveListener() {
		TableSettingsController controller = new MockController();
		MockSelectionListener l = new MockSelectionListener();

		assertEquals(l.getSelectionChangeCount(), 0);
		controller.fireSelectionChanged();
		assertEquals(l.getSelectionChangeCount(), 0);
		
		controller.addSelectionListener(l);
		controller.fireSelectionChanged();
		assertEquals(l.getSelectionChangeCount(), 1);
		
		controller.removeSelectionListener(l);
		controller.fireSelectionChanged();
		assertEquals(l.getSelectionChangeCount(), 1);
	}
	
	private static class MockSelectionListener implements SelectionListener {
		
		private int selectionChangeCount = 0;

		@Override
		public void selectionChanged() {
			++selectionChangeCount;
		}

		public int getSelectionChangeCount() {
			return selectionChangeCount;
		}
		
	}
	
	private static class MockController extends TableSettingsController {

		@Override
		public int getSelectedCellCount() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public boolean selectedCellsHaveMixedEnumerations() {
			return true;
		}

		@Override
		public int getSelectedColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getSelectedRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean getShowGrid() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public TableOrientation getTableOrientation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCanHideHeaders() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setShowGrid(boolean showGrid) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTableOrientation(TableOrientation orientation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void transposeTable() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getColumnWidth() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getRowHeight() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setColumnWidth(int newWidth) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRowHeight(int newHeight) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ComboBoxModel getEnumerationModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setEnumeration(ComboBoxModel model) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setDecimalPlaces(ComboBoxModel model) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public boolean showDecimalPlaces() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public Integer getDecimalPlaces() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean canSetOrientation() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canTranspose() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public AbbreviationSettings getCellLabelAbbreviationSettings() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AbbreviationSettings getColumnLabelAbbreviationSettings() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AbbreviationSettings getRowLabelAbbreviationSettings() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCellLabelAbbreviations(AbbreviationSettings settings) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnLabelAbbreviations(AbbreviationSettings settings) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRowLabelAbbreviations(AbbreviationSettings settings) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ContentAlignment getRowHeaderAlignment() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRowHeaderAlignment(ContentAlignment newAlignment) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ContentAlignment getColumnHeaderAlignment() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setColumnHeaderAlignment(ContentAlignment newAlignment) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ContentAlignment getCellAlignment() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCellAlignment(ContentAlignment newAlignment) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getTableRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getTableColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setDateFormat(ComboBoxModel model) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public DateFormatItem getDateFormat() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean enumerationIsNone(ComboBoxModel model) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean dateIsNone(ComboBoxModel model) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public BorderState getBorderState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void mergeBorderState(BorderState controllerState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public JVMFontFamily getCellFontName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCellFont(ComboBoxModel model) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRowHeaderFontName(ComboBoxModel model) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderFontName(ComboBoxModel model) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public JVMFontFamily getRowHeaderFontName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public JVMFontFamily getColumnHeaderFontName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void mergeCellFontStyle(ButtonModel boldModel,
				ButtonModel italicModel) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCellFontStyle(int newStyle) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRowHeaderFontStyle(int newStyle) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderFontStyle(int newStyle) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCellFontSize(int fontSize) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRowHeaderFontSize(int fontSize) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderFontSize(int fontSize) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCellFontColor(Color fontColor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRowHeaderFontColor(Color fontCOlor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderFontColor(Color fontColor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Integer getCellFontStyle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getRowFontStyle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getColumnHeaderFontStyle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getCellFontSize() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getRowHeaderFontSize() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getColumnHeaderFontSize() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getCellFontColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getRowHeaderFontColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getColumnHeaderFontColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getRowHeaderBackgroundColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getColumnHeaderBackgroundColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRowHeaderBackgroundColor(Color fontColor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderBackgroundColor(Color fontColor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCellBackgroundColor(Color backgroundColor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Color getCellBackgroundColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getRowHeaderTextAttribute() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRowHeaderTextAttribute(int newTextAttribute) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderTextAttribute(int newTextAttribute) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Integer getColumnHeaderTextAttribute() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCellFontTextAttribute(int fontStyle) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Integer getCellFontTextAttribute() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BorderState getRowHeaderBorderState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRowHeaderBorderState(BorderState newBorderState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public BorderState getColumnHeaderBorderState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setColumnHeaderBorderState(BorderState newBorderState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mergeRowHeaderBorderState(BorderState controllerState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mergeColumnHeaderBorderState(BorderState controllerState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Color getRowHeaderBorderColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getColumnHeaderBorderColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRowHeaderBorderColor(Color borderColor) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setColumnHeaderBorderColor(Color borderColor) {
			// TODO Auto-generated method stub
			
		}

	}
	
}
