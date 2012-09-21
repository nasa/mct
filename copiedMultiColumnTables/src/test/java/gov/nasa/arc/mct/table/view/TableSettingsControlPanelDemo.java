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

import gov.nasa.arc.mct.abbreviation.Abbreviations;
import gov.nasa.arc.mct.abbreviation.impl.AbbreviationsManager;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import java.awt.Color;
import java.util.Properties;

import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;

public class TableSettingsControlPanelDemo {
	
	private static AbbreviationsManager manager;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty("fiber optic", "F/O");
		props.setProperty("system", "Sys");
		props.setProperty("video baseband signal processor", "VBSP");
		props.setProperty("voltage", "Volts | V");
		props.setProperty("channel", "Ch");
		manager = new AbbreviationsManager(props);
		
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private static AbbreviationSettings getAbbreviationSettings(String label) {
		Abbreviations abbrevs = manager.getAbbreviations(label);
		LabelAbbreviations currentAbbrevs = new LabelAbbreviations();
		AbbreviationSettings settings = new AbbreviationSettings(label, abbrevs, currentAbbrevs);
		return settings;
		
		
	}

	protected static void createAndShowGUI() {
		JFrame frame = new JFrame("Table Settings Dialog");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		TableSettingsController controller = new TableSettingsController() {

			@Override
			public boolean getShowGrid() {
				return true;
			}
			
			@Override
			public boolean selectedCellsHaveMixedEnumerations() {
				return true;
			}

			@Override
			public TableOrientation getTableOrientation() {
				return TableOrientation.ROW_MAJOR;
			}

			@Override
			public void setShowGrid(boolean showGrid) {
				System.out.println("New show grid: " + showGrid);
			}

			@Override
			public void setTableOrientation(TableOrientation orientation) {
				System.out.println("New table orientation: " + orientation);
			}

			@Override
			public void transposeTable() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public int getSelectedCellCount() {
				return 1;
			}

			@Override
			public int getSelectedColumnCount() {
				return 1;
			}

			@Override
			public int getSelectedRowCount() {
				return 1;
			}

			@Override
			public boolean isCanHideHeaders() {
				return true;
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
				DefaultComboBoxModel model = new DefaultComboBoxModel(new Object[] { "one", "two", "three" });
				model.setSelectedItem("two");
				return model;
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
				return true;
			}

			@Override
			public boolean canTranspose() {
				return true;
			}

			@Override
			public AbbreviationSettings getCellLabelAbbreviationSettings() {
				return TableSettingsControlPanelDemo.getAbbreviationSettings("Voltage");
			}

			@Override
			public AbbreviationSettings getColumnLabelAbbreviationSettings() {
				return TableSettingsControlPanelDemo.getAbbreviationSettings("Fiber Optic MDC System");
			}

			@Override
			public AbbreviationSettings getRowLabelAbbreviationSettings() {
				return TableSettingsControlPanelDemo.getAbbreviationSettings("Channel 1 Video Baseband Signal Processor");
			}

			@Override
			public void setCellLabelAbbreviations(AbbreviationSettings settings) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setColumnLabelAbbreviations(
					AbbreviationSettings settings) {
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

		};

		//Create and set up the content pane.
		TableSettingsControlPanel panel = new TableSettingsControlPanel(controller);
		panel.setOpaque(true); //content panes must be opaque
		frame.setContentPane(panel);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

}
