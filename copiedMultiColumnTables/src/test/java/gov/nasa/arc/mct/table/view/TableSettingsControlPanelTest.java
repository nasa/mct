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

import static gov.nasa.arc.mct.table.view.ContentAlignment.CENTER;
import static gov.nasa.arc.mct.table.view.ContentAlignment.DECIMAL;
import static gov.nasa.arc.mct.table.view.ContentAlignment.LEFT;
import static gov.nasa.arc.mct.table.view.ContentAlignment.RIGHT;
import static org.testng.Assert.assertEquals;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.view.BorderState.BorderEdge;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;
import gov.nasa.arc.mct.test.util.gui.BaseUITest;
import gov.nasa.arc.mct.test.util.gui.Query;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ResourceBundle;

import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.fixture.FrameFixture;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TableSettingsControlPanelTest extends BaseUITest {

	/** The resource bundle we should use for getting strings. */
	private static final ResourceBundle bundle = ResourceBundle.getBundle("TableSettingsControlPanel"); //NOI18N
	
	private static final String PANEL_NAME = "Panel1";
	private static final String PANEL2_NAME = "Panel2";

	private static Query ROW_COLUMN_FORMATTING_PANEL = new Query().accessibleName(bundle.getString("ROW_COLUMN_FORMATTING_PANEL_NAME"));
	private static Query ROW_HEIGHT = new Query().accessibleName(bundle.getString("ROW_HEIGHT_NAME"));
	private static Query CELL_FORMATTING_PANEL = new Query().accessibleName(bundle.getString("CELL_FORMATTING_PANEL_NAME"));
	private static Query SHOW_GRID = new Query().accessibleName(bundle.getString("SHOW_GRID_NAME"));
	private static Query ROW_MAJOR = new Query().accessibleName(bundle.getString("ROW_MAJOR_NAME"));
	private static Query COLUMN_MAJOR = new Query().accessibleName(bundle.getString("COLUMN_MAJOR_NAME"));
	private static Query TRANSPOSE_TABLE = new Query().accessibleName(bundle.getString("TRANSPOSE_TABLE_NAME"));
	private static Query SHOW_ROW_HEADERS = new Query().accessibleName(bundle.getString("SHOW_ROW_HEADERS_NAME"));
	private static Query SHOW_COLUMN_HEADERS = new Query().accessibleName(bundle.getString("SHOW_COLUMN_HEADERS_NAME"));
	private static Query PROPERTY_TO_SHOW = new Query().accessibleName(bundle.getString("PROPERTY_TO_SHOW_NAME"));
	private static Query ENUMERATION = new Query().accessibleName(bundle.getString("ENUMERATION_NAME"));
	private static Query DECIMAL_PLACES = new Query().accessibleName(bundle.getString("NUMBER_OF_DECIMALS_NAME"));
	private static Query FORMAT_AS_DATE_NAME = new Query().accessibleName(bundle.getString("FORMAT_AS_DATE_NAME"));
	
	private static Query CELL_BORDER_LEFT_NAME = new Query().accessibleName(bundle.getString("CELL_BORDER_LEFT_NAME"));
	private static Query CELL_BORDER_RIGHT_NAME = new Query().accessibleName(bundle.getString("CELL_BORDER_RIGHT_NAME"));
	private static Query CELL_BORDER_BOTTOM_NAME = new Query().accessibleName(bundle.getString("CELL_BORDER_BOTTOM_NAME"));
	private static Query CELL_BORDER_TOP_NAME = new Query().accessibleName(bundle.getString("CELL_BORDER_TOP_NAME"));
	
	private static Query CELL_FONT_NAME = new Query().accessibleName(bundle.getString("CELL_FONT_NAME"));
	private static Query CELL_FONT_SIZE_NAME = new Query().accessibleName(bundle.getString("CELL_FONT_SIZE_NAME"));
	private static Query CELL_FONT_BOLD_NAME = new Query().accessibleName(bundle.getString("CELL_FONT_BOLD_NAME"));
	private static Query CELL_FONT_ITALIC_NAME = new Query().accessibleName(bundle.getString("CELL_FONT_ITALIC_NAME"));
	private static Query CELL_FONT_UNDERLINE_NAME = new Query().accessibleName(bundle.getString("CELL_FONT_UNDERLINE_NAME"));
	private static Query CELL_FONT_COLOR_NAME = new Query().accessibleName(bundle.getString("CELL_FONT_COLOR_NAME"));
	private static Query CELL_BACKGROUND_COLOR_NAME = new Query().accessibleName(bundle.getString("CELL_BACKGROUND_COLOR_NAME"));
	
	private static Query ROW_FONT_NAME = new Query().accessibleName(bundle.getString("ROW_FONT_NAME"));
	private static Query ROW_FONT_SIZE_NAME = new Query().accessibleName(bundle.getString("ROW_FONT_SIZE_NAME"));
	private static Query ROW_FONT_BOLD_NAME = new Query().accessibleName(bundle.getString("ROW_FONT_BOLD_NAME"));
	private static Query ROW_FONT_ITALIC_NAME = new Query().accessibleName(bundle.getString("ROW_FONT_ITALIC_NAME"));
	private static Query ROW_FONT_UNDERLINE_NAME = new Query().accessibleName(bundle.getString("ROW_FONT_UNDERLINE_NAME"));
	private static Query ROW_BORDER_LEFT_NAME = new Query().accessibleName(bundle.getString("ROW_BORDER_LEFT_NAME"));
	private static Query ROW_BORDER_TOP_NAME = new Query().accessibleName(bundle.getString("ROW_BORDER_TOP_NAME"));
	private static Query ROW_BORDER_BOTTOM_NAME = new Query().accessibleName(bundle.getString("ROW_BORDER_BOTTOM_NAME"));
	private static Query ROW_BORDER_RIGHT_NAME = new Query().accessibleName(bundle.getString("ROW_BORDER_RIGHT_NAME"));
	private static Query ROW_HEADER_BORDER_COLOR_NAME = new Query().accessibleName(bundle.getString("ROW_HEADER_BORDER_COLOR_NAME"));

	
	private static Query COL_FONT_NAME = new Query().accessibleName(bundle.getString("COL_FONT_NAME"));
	private static Query COL_FONT_SIZE_NAME = new Query().accessibleName(bundle.getString("COL_FONT_SIZE_NAME"));
	private static Query COL_FONT_BOLD_NAME = new Query().accessibleName(bundle.getString("COL_FONT_BOLD_NAME"));
	private static Query COL_FONT_ITALIC_NAME = new Query().accessibleName(bundle.getString("COL_FONT_ITALIC_NAME"));
	private static Query COL_FONT_UNDERLINE_NAME = new Query().accessibleName(bundle.getString("COL_FONT_UNDERLINE_NAME"));
	private static Query COL_BORDER_LEFT_NAME = new Query().accessibleName(bundle.getString("COL_BORDER_LEFT_NAME"));
	private static Query COL_BORDER_TOP_NAME = new Query().accessibleName(bundle.getString("COL_BORDER_TOP_NAME"));
	private static Query COL_BORDER_BOTTOM_NAME = new Query().accessibleName(bundle.getString("COL_BORDER_BOTTOM_NAME"));
	private static Query COL_BORDER_RIGHT_NAME = new Query().accessibleName(bundle.getString("COL_BORDER_RIGHT_NAME"));
	private static Query COL_HEADER_BORDER_COLOR_NAME = new Query().accessibleName(bundle.getString("COL_HEADER_BORDER_COLOR_NAME"));


	private FrameFixture window = null;
	private FrameFixture window2 = null;
	
	private Query getQuery(String accessibleNameKey) {
		return new Query().accessibleName(bundle.getString(accessibleNameKey));
	}
	
	@Test(dataProvider="settingsTests")
	public void testInitialSettings(boolean showGrid, TableOrientation orientation) {
		final TableSettingsController controller = new MockController();
		controller.setShowGrid(showGrid);
		controller.setTableOrientation(orientation);
		
		window = showInFrame(GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			}), PANEL_NAME);
		
		if (orientation == TableOrientation.ROW_MAJOR) {
			ROW_MAJOR.radioButtonIn(window).requireSelected();
			COLUMN_MAJOR.radioButtonIn(window).requireNotSelected();
		} else {
			ROW_MAJOR.radioButtonIn(window).requireNotSelected();
			COLUMN_MAJOR.radioButtonIn(window).requireSelected();
		}
		
		getQuery("ROW_HEADER_ALIGN_LEFT_NAME").radioButtonIn(window).requireSelected();
		getQuery("COLUMN_HEADER_ALIGN_LEFT_NAME").radioButtonIn(window).requireSelected();
		getQuery("FORMAT_AS_DATE_NAME").comboBoxIn(window).requireNotEditable();
		getQuery("CELL_BORDER_LEFT_NAME").radioButtonIn(window).requireNotSelected();
		getQuery("CELL_BORDER_RIGHT_NAME").radioButtonIn(window).requireVisible();


		DECIMAL_PLACES.comboBoxIn(window).requireSelection("2");
	}
	
	@DataProvider(name="settingsTests")
	public Object[][] getSettingsTests() {
		return new Object[][] {
				{ false, TableOrientation.ROW_MAJOR },
				{ false, TableOrientation.COLUMN_MAJOR },
				{ true, TableOrientation.ROW_MAJOR },
				{ true, TableOrientation.COLUMN_MAJOR },
		};
	}
	
	@Test(enabled=false)
	public void testLoadSettings() {
		final TableSettingsController controller = new MockController();
		controller.setShowGrid(false);
		controller.setTableOrientation(TableOrientation.ROW_MAJOR);
		controller.setRowHeaderAlignment(RIGHT);
		controller.setColumnHeaderAlignment(CENTER);
		controller.setCellAlignment(DECIMAL);
		DefaultComboBoxModel cellFontModel = new DefaultComboBoxModel();
		cellFontModel.addElement(JVMFontFamily.Serif);
		cellFontModel.addElement(JVMFontFamily.Monospaced);
		cellFontModel.setSelectedItem(JVMFontFamily.Serif);
		DefaultComboBoxModel rowHeaderFontModel = new DefaultComboBoxModel();
		rowHeaderFontModel.addElement(JVMFontFamily.Serif);
		rowHeaderFontModel.addElement(JVMFontFamily.Monospaced);
		rowHeaderFontModel.setSelectedItem(JVMFontFamily.Serif);
		DefaultComboBoxModel columnHeaderFontModel = new DefaultComboBoxModel();
		columnHeaderFontModel.addElement(JVMFontFamily.Serif);
		columnHeaderFontModel.addElement(JVMFontFamily.Monospaced);
		columnHeaderFontModel.setSelectedItem(JVMFontFamily.Serif);
		controller.setCellFont(cellFontModel);
		controller.setCellFontSize(12);
		controller.setCellFontStyle(Font.PLAIN);
		controller.setCellFontTextAttribute(TableFormattingConstants.UNDERLINE_OFF);
		controller.setCellFontColor(TableFormattingConstants.defaultFontColor);
		controller.setCellBackgroundColor(TableFormattingConstants.defaultBackgroundColor);
		controller.setRowHeaderFontName(rowHeaderFontModel);
		controller.setRowHeaderFontSize(12);
		controller.setRowHeaderFontStyle(Font.PLAIN);
		controller.setRowHeaderTextAttribute(TableFormattingConstants.UNDERLINE_OFF);
		controller.setColumnHeaderFontName(rowHeaderFontModel);
		controller.setColumnHeaderFontSize(12);
		controller.setColumnHeaderFontStyle(Font.PLAIN);
		controller.setColumnHeaderTextAttribute(TableFormattingConstants.UNDERLINE_OFF);
		controller.setRowHeaderBorderState(new BorderState(BorderEdge.NONE.value()));
		controller.setColumnHeaderBorderState(new BorderState(BorderEdge.NONE.value()));
		controller.setRowHeaderBorderColor(TableFormattingConstants.defaultFontColor);
		controller.setColumnHeaderBorderColor(TableFormattingConstants.defaultFontColor);
		
		DefaultComboBoxModel dateModel = new DefaultComboBoxModel();
		dateModel.addElement("None");
		dateModel.addElement("HHSS");
		dateModel.setSelectedItem(0);
		controller.setDateFormat(dateModel);
		
		final TableSettingsControlPanel panel = 
		GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			});
		window = showInFrame(panel, PANEL_NAME);

		ROW_MAJOR.radioButtonIn(window).requireSelected();
		COLUMN_MAJOR.radioButtonIn(window).requireNotSelected();
		DECIMAL_PLACES.comboBoxIn(window).requireSelection("2");
		CELL_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		CELL_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		CELL_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();
		window.comboBox(new ComboBoxMatcher("Cell Font Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Cell Background Color")).requireSelection(0);

		
		ROW_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		ROW_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_BORDER_LEFT_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_BORDER_RIGHT_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_BORDER_TOP_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_BORDER_BOTTOM_NAME.toggleButtonIn(window).requireNotSelected();
		COL_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		COL_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		COL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		COL_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();
		COL_BORDER_LEFT_NAME.toggleButtonIn(window).requireNotSelected();
		COL_BORDER_RIGHT_NAME.toggleButtonIn(window).requireNotSelected();
		COL_BORDER_TOP_NAME.toggleButtonIn(window).requireNotSelected();
		COL_BORDER_BOTTOM_NAME.toggleButtonIn(window).requireNotSelected();
		window.comboBox(new ComboBoxMatcher("Cell Font Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Cell Background Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Row Header Font Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Row Header Background Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Col Header Font Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Col Header Background Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Row Header Border Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Column Header Border Color")).requireSelection(0);
		
		FORMAT_AS_DATE_NAME.comboBoxIn(window).requireSelection(0); 
		controller.setTableOrientation(TableOrientation.COLUMN_MAJOR);
		
		getQuery("ROW_HEADER_ALIGN_RIGHT_NAME").radioButtonIn(window).requireSelected();
		getQuery("COLUMN_HEADER_ALIGN_CENTER_NAME").radioButtonIn(window).requireSelected();
		getQuery("CELL_ALIGN_DECIMAL_NAME").radioButtonIn(window).requireSelected();
		CELL_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		CELL_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		CELL_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();
		window.comboBox(new ComboBoxMatcher("Cell Font Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Cell Background Color")).requireSelection(0);
		ROW_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		ROW_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		ROW_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();
		COL_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		COL_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		COL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		COL_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();
		window.comboBox(new ComboBoxMatcher("Cell Font Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Cell Background Color")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Row Header Font Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Row Header Background Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Col Header Font Color Control")).requireSelection(0);
		window.comboBox(new ComboBoxMatcher("Col Header Background Color Control")).requireSelection(0);
		
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (int i=0; i <= 10; i++) {
			model.addElement(Integer.toString(i));
		}
		model.setSelectedItem(4);
		controller.setDecimalPlaces(model);
		GuiActionRunner.execute(new GuiTask() {
			  @Override
			  public void executeInEDT() {
				  panel.loadSettings();
			  }
			});
		getRobot().waitForIdle();
		ROW_MAJOR.radioButtonIn(window).requireNotSelected();
		COLUMN_MAJOR.radioButtonIn(window).requireSelected();
		DECIMAL_PLACES.comboBoxIn(window).requireSelection(4);
		CELL_FONT_NAME.comboBoxIn(window).requireSelection("Serif");
		CELL_FONT_SIZE_NAME.spinnerIn(window).requireValue(12);
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		CELL_FONT_UNDERLINE_NAME.toggleButtonIn(window).requireNotSelected();

		ENUMERATION.comboBoxIn(window).selectItem(0);
		DECIMAL_PLACES.comboBoxIn(window).selectItem(3);
		DECIMAL_PLACES.comboBoxIn(window).requireSelection(3);
		assertEquals(controller.getDecimalPlaces().intValue(), 3);
		CELL_FONT_NAME.comboBoxIn(window).selectItem("Dialog");
		CELL_FONT_NAME.comboBoxIn(window).requireSelection("Dialog");
		assertEquals(controller.getCellFontName().name(), "Dialog");
		CELL_FONT_SIZE_NAME.spinnerIn(window).select(20);
		CELL_FONT_SIZE_NAME.spinnerIn(window).requireValue(20);
		assertEquals(controller.getCellFontSize().intValue(),20);
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).click();
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).requireSelected();
		assertEquals(controller.getCellFontStyle().intValue(),Font.BOLD);
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).click();
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		assertEquals(controller.getCellFontStyle().intValue(),Font.PLAIN);
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).requireSelected();
		assertEquals(controller.getCellFontStyle().intValue(),Font.ITALIC);
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		assertEquals(controller.getCellFontStyle().intValue(),Font.PLAIN);
		CELL_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		CELL_FONT_BOLD_NAME.toggleButtonIn(window).click();
		assertEquals(controller.getCellFontStyle().intValue(),Font.BOLD+Font.ITALIC);
		window.comboBox(new ComboBoxMatcher("Cell Font Color")).selectItem(1);
		assertEquals(controller.getCellFontColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Cell Background Color")).selectItem(1);
		assertEquals(controller.getCellBackgroundColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Row Header Font Color Control")).selectItem(1);
		assertEquals(controller.getRowHeaderFontColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Row Header Background Color Control")).selectItem(1);
		assertEquals(controller.getRowHeaderBackgroundColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Col Header Font Color Control")).selectItem(1);
		assertEquals(controller.getColumnHeaderFontColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Col Header Background Color Control")).selectItem(1);
		assertEquals(controller.getColumnHeaderBackgroundColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Row Header Border Color")).selectItem(1);
		assertEquals(controller.getRowHeaderBorderColor(),new Color(0,0,255));
		window.comboBox(new ComboBoxMatcher("Column Header Border Color")).selectItem(1);
		assertEquals(controller.getColumnHeaderBorderColor(),new Color(0,0,255));
				
		ROW_FONT_NAME.comboBoxIn(window).selectItem("Dialog");
		ROW_FONT_NAME.comboBoxIn(window).requireSelection("Dialog");
		assertEquals(controller.getRowHeaderFontName().name(), "Dialog");
		ROW_FONT_SIZE_NAME.spinnerIn(window).select(20);
		ROW_FONT_SIZE_NAME.spinnerIn(window).requireValue(20);
		assertEquals(controller.getRowHeaderFontSize().intValue(),20);
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).click();
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).requireSelected();
		assertEquals(controller.getRowFontStyle().intValue(),Font.BOLD);
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).click();
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		assertEquals(controller.getRowFontStyle().intValue(),Font.PLAIN);
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).requireSelected();
		assertEquals(controller.getRowFontStyle().intValue(),Font.ITALIC);
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		assertEquals(controller.getRowFontStyle().intValue(),Font.PLAIN);
		ROW_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		ROW_FONT_BOLD_NAME.toggleButtonIn(window).click();
		assertEquals(controller.getRowFontStyle().intValue(),Font.BOLD+Font.ITALIC);
		
		// TODO: Test Row/Column Border State changes... a lot of stubbing?
		
		COL_FONT_NAME.comboBoxIn(window).selectItem("Dialog");
		COL_FONT_NAME.comboBoxIn(window).requireSelection("Dialog");
		assertEquals(controller.getColumnHeaderFontName().name(), "Dialog");
		COL_FONT_SIZE_NAME.spinnerIn(window).select(20);
		COL_FONT_SIZE_NAME.spinnerIn(window).requireValue(20);
		assertEquals(controller.getCellFontSize().intValue(),20);
		COL_FONT_BOLD_NAME.toggleButtonIn(window).click();
		COL_FONT_BOLD_NAME.toggleButtonIn(window).requireSelected();
		assertEquals(controller.getColumnHeaderFontStyle().intValue(),Font.BOLD);
		COL_FONT_BOLD_NAME.toggleButtonIn(window).click();
		COL_FONT_BOLD_NAME.toggleButtonIn(window).requireNotSelected();
		assertEquals(controller.getColumnHeaderFontStyle().intValue(),Font.PLAIN);
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).requireSelected();
		assertEquals(controller.getColumnHeaderFontStyle().intValue(),Font.ITALIC);
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).requireNotSelected();
		assertEquals(controller.getColumnHeaderFontStyle().intValue(),Font.PLAIN);
		COL_FONT_ITALIC_NAME.toggleButtonIn(window).click();
		COL_FONT_BOLD_NAME.toggleButtonIn(window).click();
		assertEquals(controller.getColumnHeaderFontStyle().intValue(),Font.BOLD+Font.ITALIC);
		
		window.comboBox(new ComboBoxMatcher("Row Header Border Color")).selectItem(0);
		assertEquals(controller.getRowHeaderBorderColor(),new Color(0,0,0));
		window.comboBox(new ComboBoxMatcher("Column Header Border Color")).selectItem(0);
		assertEquals(controller.getColumnHeaderBorderColor(),new Color(0,0,0));
		
	}
	
	@Test
	public void testChangeSettings() throws InterruptedException {
		long pause = 600;
		final TableSettingsController controller = new MockController();
		TableSettingsControlPanel panel = GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			});
		window = showInFrame(panel, PANEL_NAME);

		assertEquals(controller.getRowHeaderAlignment(), LEFT);
		for (ContentAlignment alignment : new ContentAlignment[] {RIGHT, CENTER, LEFT}) {
			String key = "ROW_HEADER_ALIGN_" + alignment.toString() + "_NAME";
			getQuery(key).radioButtonIn(window).click();
			Thread.sleep(pause);
			assertEquals(controller.getRowHeaderAlignment(), alignment);
		}

		assertEquals(controller.getColumnHeaderAlignment(), LEFT);
		for (ContentAlignment alignment : new ContentAlignment[] {RIGHT, CENTER, LEFT}) {
			String key = "COLUMN_HEADER_ALIGN_" + alignment.toString() + "_NAME";
			getQuery(key).radioButtonIn(window).click();
			Thread.sleep(pause);
			assertEquals(controller.getColumnHeaderAlignment(), alignment);
		}

		assertEquals(controller.getCellAlignment(), LEFT);
		for (ContentAlignment alignment : new ContentAlignment[] {RIGHT, CENTER, LEFT, DECIMAL}) {
			String key = "CELL_ALIGN_" + alignment.toString() + "_NAME";
			getQuery(key).radioButtonIn(window).click();
			Thread.sleep(pause);
			assertEquals(controller.getCellAlignment(), alignment);
		}
	}
	
	// Test disabled for MCT-2057, which hides the transpose button.
	// When the transpose functionality is added back in, should re-enable
	// this test.
	@Test(enabled=false)
	public void testTransposeTable() {
		final MockController controller = new MockController();
		controller.setShowGrid(false);
		controller.setTableOrientation(TableOrientation.ROW_MAJOR);
		
		TableSettingsControlPanel panel = GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			});
		window = showInFrame(panel, PANEL_NAME);

		assertEquals(controller.getTransposeCount(), 0);
		TRANSPOSE_TABLE.buttonIn(window).click();
		assertEquals(controller.getTransposeCount(), 0);
	}
	
	@Test
	public void testMultiplePanels() {
		final TableSettingsController controller = new MockController();
		controller.setShowGrid(false);
		controller.setTableOrientation(TableOrientation.ROW_MAJOR);

		window = showInFrame(GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			}), PANEL_NAME);
		window2 = showInFrame(GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			}), PANEL2_NAME);
		window2.moveTo(new Point(200,200));

		// Check that both panels have the desired settings.
		ROW_MAJOR.radioButtonIn(window).requireSelected();
		ROW_MAJOR.radioButtonIn(window2).requireSelected();
		
		// Change a setting in one panel and make sure it is reflected in the other.
		// Check collection orientation radio button is disabled if conditions are true
		if (controller.getSelectedCellCount() == (controller.getTableColumnCount() * controller.getTableRowCount())) {
			COLUMN_MAJOR.radioButtonIn(window2).requireEnabled(); 
			COLUMN_MAJOR.radioButtonIn(window).requireEnabled(); 
			ROW_MAJOR.radioButtonIn(window2).requireEnabled(); 
			ROW_MAJOR.radioButtonIn(window).requireEnabled(); 
		} else if (controller.getSelectedCellCount() == (controller.getTableColumnCount() * controller.getTableRowCount()) 
				&& (controller.getSelectedCellCount()!=0) && controller.canSetOrientation()) {
			COLUMN_MAJOR.radioButtonIn(window2).requireEnabled(); 
			COLUMN_MAJOR.radioButtonIn(window).requireEnabled(); 
			ROW_MAJOR.radioButtonIn(window2).requireEnabled(); 
			ROW_MAJOR.radioButtonIn(window).requireEnabled(); 
		} else if (controller.getSelectedCellCount() > 0) {
			COLUMN_MAJOR.radioButtonIn(window2).requireDisabled(); 
			COLUMN_MAJOR.radioButtonIn(window).requireDisabled(); 
			ROW_MAJOR.radioButtonIn(window2).requireDisabled(); 
			ROW_MAJOR.radioButtonIn(window).requireDisabled(); 
		} else if ((controller.getSelectedCellCount() == 0) && controller.canSetOrientation()) {
			COLUMN_MAJOR.radioButtonIn(window2).requireEnabled(); 
			COLUMN_MAJOR.radioButtonIn(window).requireEnabled(); 
			ROW_MAJOR.radioButtonIn(window2).requireEnabled(); 
			ROW_MAJOR.radioButtonIn(window).requireEnabled(); 
		} else {
			controller.setTableOrientation(TableOrientation.COLUMN_MAJOR);
			COLUMN_MAJOR.radioButtonIn(window2).click();
			COLUMN_MAJOR.radioButtonIn(window).requireSelected();
			COLUMN_MAJOR.radioButtonIn(window2).requireSelected();
		
			controller.setTableOrientation(TableOrientation.ROW_MAJOR);
			ROW_MAJOR.radioButtonIn(window).click();
			ROW_MAJOR.radioButtonIn(window).requireSelected();
			ROW_MAJOR.radioButtonIn(window2).requireSelected();
		}
	}
	
	@Test(dataProvider="hideControlsTests")
	public void testHideControls(boolean canHideHeaders, int selectedCellCount) {
		final MockController controller = new MockController();
		controller.setShowGrid(false);
		controller.setTableOrientation(TableOrientation.ROW_MAJOR);
		
		controller.setCanHideHeaders(canHideHeaders);
		controller.setSelectedCellCount(selectedCellCount);

		window = showInFrame(GuiActionRunner.execute(new GuiQuery<TableSettingsControlPanel>() {
			  @Override
			  public TableSettingsControlPanel executeInEDT() {
			    return new TableSettingsControlPanel(controller);
			  }
			}), PANEL_NAME);
		
		if (selectedCellCount == 0) {
			ROW_HEIGHT.spinnerIn(window).requireNotVisible();
			CELL_FORMATTING_PANEL.panelIn(window).requireNotVisible();
		} else {
			ROW_COLUMN_FORMATTING_PANEL.panelIn(window).requireVisible();
			
// For now, don't check the cell formatting, since they aren't yet implemented.
//			CELL_FORMATTING_PANEL.panelIn(window).requireVisible();

// For now, don't check whether the header controls are present,
// because they are not yet implemented.
//			if (canHideHeaders) {
//				SHOW_ROW_HEADERS.checkBoxIn(window).requireVisible();
//				SHOW_COLUMN_HEADERS.checkBoxIn(window).requireVisible();
//			} else {
//				SHOW_ROW_HEADERS.checkBoxIn(window).requireNotVisible();
//				SHOW_COLUMN_HEADERS.checkBoxIn(window).requireNotVisible();
//			}
			
			if (selectedCellCount == 1) {
// Not yet implemented.
//				PROPERTY_TO_SHOW.comboBoxIn(window).requireVisible();
				ENUMERATION.comboBoxIn(window).requireVisible();
			} else {
				// selectedCellCount > 1
// Not yet implemented.
//				PROPERTY_TO_SHOW.comboBoxIn(window).requireNotVisible();
				ENUMERATION.comboBoxIn(window).requireVisible();
			}
		}
	}
	
	@DataProvider(name="hideControlsTests")
	public Object[][] getHideControlsTests() {
		return new Object[][] {
				{ true, 0 },
				{ false, 1 },
				{ true, 1 },
				{ false, 2 },
				{ true, 2 }
		};
	}

	private static class MockController extends TableSettingsController {

		private boolean showGrid = false;
		private TableOrientation orientation = TableOrientation.ROW_MAJOR;
		private int transposeCount = 0;
		private int selectedCellCount = 1;
		private int selectedRowCount = 1;
		private int selectedColumnCount = 1;
		private boolean canHideHeaders = true;
		private ContentAlignment rowHeaderAlignment = LEFT;
		private ContentAlignment columnHeaderAlignment = LEFT;
		private ContentAlignment cellAlignment = LEFT;
		private int decimalPlaces = 2;
		private JVMFontFamily cellFontName;
		private int cellFontStyle;
		private int cellFontStyleUnderline;
		private int cellFontSize;
		private Color cellFontColor;
		private Color cellBackgroundColor;
		private JVMFontFamily rowHeaderFontName;
		private JVMFontFamily columnHeaderFontName;
		private Integer rowFontStyle;
		private Integer rowFontStyleUnderline;
		private Integer rowFontSize;
		private Color rowFontColor;
		private Color rowBackgroundColor;
		private Color rowHeaderBorderColor;
		private BorderState rowHeaderBorderState;
		private Integer columnFontSize;
		private Integer columnFontStyle;
		private Integer columnFontStyleUnderline;
		private Color columnFontColor;
		private Color columnBackgroundColor;
		private Color columnHeaderBorderColor;
		private BorderState columnHeaderBorderState;
		
		private ComboBoxModel comboBoxModel=null;

		public int getTransposeCount() {
			return transposeCount;
		}
		
		@Override
		public boolean getShowGrid() {
			return showGrid;
		}

		@Override
		public TableOrientation getTableOrientation() {
			return orientation;
		}

		@Override
		public void setShowGrid(boolean showGrid) {
			this.showGrid = showGrid;
		}

		@Override
		public void setTableOrientation(TableOrientation orientation) {
			this.orientation = orientation;
		}

		@Override
		public void transposeTable() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getSelectedCellCount() {
			return selectedCellCount;
		}

		public void setSelectedCellCount(int selectedCellCount) {
			this.selectedCellCount = selectedCellCount;
		}

		@Override
		public int getSelectedRowCount() {
			return selectedRowCount;
		}

		public void setSelectedRowCount(int selectedRowCount) {
			this.selectedRowCount = selectedRowCount;
		}

		@Override
		public int getSelectedColumnCount() {
			return selectedColumnCount;
		}

		public void setSelectedColumnCount(int selectedColumnCount) {
			this.selectedColumnCount = selectedColumnCount;
		}

		@Override
		public boolean isCanHideHeaders() {
			return canHideHeaders;
		}

		public void setCanHideHeaders(boolean canHideHeaders) {
			this.canHideHeaders = canHideHeaders;
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
			DefaultComboBoxModel enumModel = new DefaultComboBoxModel(new Object[] { "one", "two", "three" });
			enumModel.setSelectedItem(enumModel.getElementAt(0));
			return enumModel;
		}

		@Override
		public void setEnumeration(ComboBoxModel model) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean showDecimalPlaces() {
			return (selectedCellCount != 0);
		}
		
		@Override
		public Integer getDecimalPlaces() {
			return decimalPlaces;
		}
		
		@Override
		public void setDecimalPlaces(ComboBoxModel model) {
			decimalPlaces = ((Integer) model.getSelectedItem()).intValue();
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
			return rowHeaderAlignment;
		}

		@Override
		public void setRowHeaderAlignment(ContentAlignment newAlignment) {
			rowHeaderAlignment = newAlignment;
		}

		@Override
		public ContentAlignment getColumnHeaderAlignment() {
			return columnHeaderAlignment;
		}

		@Override
		public void setColumnHeaderAlignment(ContentAlignment newAlignment) {
			columnHeaderAlignment = newAlignment;
		}

		@Override
		public ContentAlignment getCellAlignment() {
			return cellAlignment;
		}

		@Override
		public void setCellAlignment(ContentAlignment newAlignment) {
			cellAlignment = newAlignment;
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
			String x =  model.getSelectedItem().toString();

			 comboBoxModel =model;
		}

		@Override
		public DateFormatItem getDateFormat() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean enumerationIsNone(ComboBoxModel model) {
			return true;
		}

		@Override
		public boolean dateIsNone(ComboBoxModel model) {
			return true;
		}

		@Override
		public BorderState getBorderState() {
			return new BorderState(BorderEdge.NONE.value());
		}

		@Override
		public void mergeBorderState(BorderState controllerState) {
		}
		
		@Override
		public boolean selectedCellsHaveMixedEnumerations() {
			return true;
		}

		@Override
		public JVMFontFamily getCellFontName() {
			return cellFontName;
		}

		@Override
		public void setCellFont(ComboBoxModel model) {
			// TODO Auto-generated method stub
			cellFontName = (JVMFontFamily) model.getSelectedItem();
		}

		@Override
		public void setRowHeaderFontName(ComboBoxModel model) {
			rowHeaderFontName = (JVMFontFamily) model.getSelectedItem();
		}

		@Override
		public void setColumnHeaderFontName(ComboBoxModel model) {
			columnHeaderFontName = (JVMFontFamily) model.getSelectedItem();
			
		}

		@Override
		public JVMFontFamily getRowHeaderFontName() {
			// TODO Auto-generated method stub
			return rowHeaderFontName;
		}

		@Override
		public JVMFontFamily getColumnHeaderFontName() {
			// TODO Auto-generated method stub
			return columnHeaderFontName;
		}

		@Override
		public void mergeCellFontStyle(ButtonModel boldModel,
				ButtonModel italicModel) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCellFontStyle(int newStyle) {
			cellFontStyle = newStyle;
			
		}

		@Override
		public void setRowHeaderFontStyle(int newStyle) {
			rowFontStyle = newStyle;
			
		}

		@Override
		public void setColumnHeaderFontStyle(int newStyle) {
			columnFontStyle = newStyle;
			
		}

		@Override
		public void setCellFontSize(int fontSize) {
			cellFontSize = fontSize;
			
		}

		@Override
		public void setRowHeaderFontSize(int fontSize) {
			rowFontSize  = fontSize;
			
		}

		@Override
		public void setColumnHeaderFontSize(int fontSize) {
			columnFontSize = fontSize;
			
		}

		@Override
		public void setCellFontColor(Color fontColor) {
			cellFontColor = fontColor;
			
		}

		@Override
		public void setRowHeaderFontColor(Color fontCOlor) {
			rowFontColor = fontCOlor;
			
		}

		@Override
		public void setColumnHeaderFontColor(Color fontColor) {
			columnFontColor = fontColor;
			
		}

		@Override
		public Integer getCellFontStyle() {
			// TODO Auto-generated method stub
			return cellFontStyle;
		}

		@Override
		public Integer getRowFontStyle() {
			// TODO Auto-generated method stub
			return rowFontStyle;
		}

		@Override
		public Integer getColumnHeaderFontStyle() {
			// TODO Auto-generated method stub
			return columnFontStyle;
		}

		@Override
		public Integer getCellFontSize() {
			// TODO Auto-generated method stub
			return cellFontSize;
		}

		@Override
		public Integer getRowHeaderFontSize() {
			// TODO Auto-generated method stub
			return rowFontSize;
		}

		@Override
		public Integer getColumnHeaderFontSize() {
			// TODO Auto-generated method stub
			return columnFontSize;
		}

		@Override
		public Color getCellFontColor() {
			// TODO Auto-generated method stub
			return cellFontColor;
		}

		@Override
		public Color getRowHeaderFontColor() {
			return rowFontColor;
		}

		@Override
		public Color getColumnHeaderFontColor() {
			return columnFontColor;
		}

		@Override
		public Color getRowHeaderBackgroundColor() {
			return rowBackgroundColor;
		}

		@Override
		public Color getColumnHeaderBackgroundColor() {
			// TODO Auto-generated method stub
			return columnBackgroundColor;
		}

		@Override
		public void setRowHeaderBackgroundColor(Color fontColor) {
			this.rowBackgroundColor = fontColor;
			
		}

		@Override
		public void setColumnHeaderBackgroundColor(Color fontColor) {
			this.columnBackgroundColor = fontColor;
		}

		@Override
		public void setCellBackgroundColor(Color backgroundColor) {
			cellBackgroundColor = backgroundColor;
			
		}

		@Override
		public Color getCellBackgroundColor() {
			// TODO Auto-generated method stub
			return cellBackgroundColor;
		}

		@Override
		public Integer getRowHeaderTextAttribute() {
			// TODO Auto-generated method stub
			return rowFontStyleUnderline;
		}

		@Override
		public void setRowHeaderTextAttribute(int newTextAttribute) {
			rowFontStyleUnderline = newTextAttribute;
			
		}

		@Override
		public void setColumnHeaderTextAttribute(int newTextAttribute) {
			columnFontStyleUnderline = newTextAttribute;
			
		}

		@Override
		public Integer getColumnHeaderTextAttribute() {
			// TODO Auto-generated method stub
			return columnFontStyleUnderline;
		}

		@Override
		public void setCellFontTextAttribute(int fontStyle) {
			cellFontStyleUnderline = fontStyle;
		}

		@Override
		public Integer getCellFontTextAttribute() {
			// TODO Auto-generated method stub
			return cellFontStyleUnderline;
		}

		@Override
		public BorderState getRowHeaderBorderState() {
			// TODO Auto-generated method stub
			return rowHeaderBorderState;
		}

		@Override
		public void setRowHeaderBorderState(BorderState newBorderState) {
			// TODO Auto-generated method stub
			rowHeaderBorderState = newBorderState;
		}

		@Override
		public BorderState getColumnHeaderBorderState() {
			// TODO Auto-generated method stub
			return columnHeaderBorderState;
		}

		@Override
		public void setColumnHeaderBorderState(BorderState newBorderState) {
			// TODO Auto-generated method stub
			columnHeaderBorderState = newBorderState;
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
			return rowHeaderBorderColor;
		}

		@Override
		public Color getColumnHeaderBorderColor() {
			// TODO Auto-generated method stub
			return columnHeaderBorderColor;
		}

		@Override
		public void setRowHeaderBorderColor(Color borderColor) {
			// TODO Auto-generated method stub
			rowHeaderBorderColor = borderColor;
		}

		@Override
		public void setColumnHeaderBorderColor(Color borderColor) {
			// TODO Auto-generated method stub
			columnHeaderBorderColor = borderColor;
		}
	}
	
    private static class ComboBoxMatcher extends GenericTypeMatcher<JComboBox> {
        private final String label;
        
        public ComboBoxMatcher(String label) {
            super(JComboBox.class, true);
            this.label = label;
        }
        
        @Override
        protected boolean isMatching(JComboBox cb) {
            return label.equals(cb.getAccessibleContext().getAccessibleName());
        }
        
    }

}
