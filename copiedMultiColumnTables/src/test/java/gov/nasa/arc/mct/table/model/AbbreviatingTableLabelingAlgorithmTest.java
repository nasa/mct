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
package gov.nasa.arc.mct.table.model;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AbbreviatingTableLabelingAlgorithmTest {

	AbbreviatingTableLabelingAlgorithm algorithm;
	private LabeledTableModel model;
	
	
	@BeforeMethod
	public void init() {
		algorithm = new AbbreviatingTableLabelingAlgorithm();
		model = new MockLabeledTableModel();
	}
	
	@Test
	public void testGettersSetters() {
		
		assertEquals(algorithm.getContextLabels(), new String[0]);
		
		algorithm.setContextLabels("hello", "world");
		assertEquals(algorithm.getContextLabels(), new String[] {"hello", "world"});
		
		assertEquals(algorithm.getOrientation(), TableOrientation.ROW_MAJOR);
		
		algorithm.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(algorithm.getOrientation(), TableOrientation.COLUMN_MAJOR);
	}
	
	@Test
	public void testComputeCellLabels() {
		LabeledTableModel model = new MockLabeledTableModel();
		algorithm.computeCellLabels(model, Collections.<String>emptySet());
		assertEquals(model.getCellName(0, 0), "");
		assertEquals(model.getCellName(0, 1), "");
		assertEquals(model.getCellName(1, 0), "");
		assertEquals(model.getCellName(1, 1), "");

	}
	
	@Test(dataProvider="computeCellLabelTests")
	public void testComputeCellLabel(String cellIdentifier, String rowLabel, String colLabel, String expected) {
		assertEquals(algorithm.computeCellLabel(cellIdentifier, rowLabel, colLabel, new HashSet<String>()), expected);
	}

	@DataProvider(name="computeCellLabelTests")
	public Object[][] getComputeCellLabelTests() {
		return new Object[][] {
				// Tests without row, column labels
				new Object[] { "", "", "", "" },
				new Object[] { "oneword", "", "", "oneword" },
				new Object[] { "some cell label", "", "", "some cell label" },

				// Tests with row, column labels
				new Object[] { "", "Heater", "Pump_A", "" },
				new Object[] { "oneword", "Heater", "Pump_A", "oneword" },
				new Object[] { "Heater_Pump A", "Heater", "Pump_A", "" },
				new Object[] { "Heater_Pump A_Volts", "Heater", "Pump_A", "Volts" },
		};
	}
	
	@Test(dataProvider="identifiersTests")
	public void testGetRowIdentifiers(String[][] cellIdentifiers, int row, String[] expected) {
		LabeledTableModel model = new MockTableModel(cellIdentifiers, algorithm);
		List<String> rowIdentifiers = algorithm.getRowIdentifiers(row, model);
		assertEquals(rowIdentifiers, Arrays.asList(expected));
	}
	
	@DataProvider(name="identifiersTests")
	public Object[][] getIdentifiersTests() {
		String[][] oneRowNoIdentifiers = new String[][] {
				new String[0]
		};
		String[][] twoRowsOneWithIdentifiers = new String[][] {
				new String[0],
				new String[] {"a", "b"}
		};
		String[][] twoRowsWithIdentifiers = new String[][] {
				new String[] {"a"},
				new String[] {"a", "b"}
		};
		String[][] twoByTwo = new String[][] {
				new String[] {"Vacuum_Pump_A_Volts", "Vacuum_Pump_B_Pressure"},
				new String[] {"Heater_Pump_A_Amps", "Heater_Pump_B_Flow Rate"}
		};
		
		return new Object[][] {
				new Object[] { oneRowNoIdentifiers, 0, new String[0] },
				new Object[] { twoRowsOneWithIdentifiers, 0, new String[0] },
				new Object[] { twoRowsOneWithIdentifiers, 1, new String[] {"a", "b"} },
				new Object[] { twoRowsWithIdentifiers, 0, new String[] {"a"} },
				new Object[] { twoRowsWithIdentifiers, 1, new String[] {"a", "b"} },
				new Object[] { twoByTwo, 0, new String[] {"Vacuum_Pump_A_Volts", "Vacuum_Pump_B_Pressure"} },
				new Object[] { twoByTwo, 1, new String[] {"Heater_Pump_A_Amps", "Heater_Pump_B_Flow Rate"} },
		};
	}
	
	@Test(dataProvider="labelingTests")
	public void testLabeling(
			String[][] cellIdentifiers,
			String[] rowMajorRowLabels,
			String[] rowMajorColumnLabels,
			String[][] rowMajorCellLabels,
			String[] columnMajorRowLabels,
			String[] columnMajorColumnLabels,
			String[][] columnMajorCellLabels
	) {
		LabeledTableModel model = new MockTableModel(cellIdentifiers, algorithm);

		algorithm.setOrientation(TableOrientation.ROW_MAJOR);
		model.updateLabels();
		checkLabels(model, rowMajorRowLabels, rowMajorColumnLabels, rowMajorCellLabels);
		
		algorithm.setOrientation(TableOrientation.COLUMN_MAJOR);
		model.updateLabels();
		checkLabels(model, columnMajorRowLabels, columnMajorColumnLabels, columnMajorCellLabels);
	}
	
	private void checkLabels(
			LabeledTableModel model,
			String[] rowLabels, String[] columnLabels,
			String[][] cellLabels
	) {
		for (int row=0; row < model.getRowCount(); ++row) {
			assertEquals(model.getRowName(row), rowLabels[row]);
		}
		for (int col=0; col < model.getColumnCount(); ++col) {
			assertEquals(model.getColumnName(col), columnLabels[col]);
		}
		for (int row=0; row < model.getRowCount(); ++row) {
			for (int col=0; col < model.getColumnCount(); ++col) {
				assertEquals(model.getCellName(row, col), cellLabels[row][col]);
			}
		}
	}

	@DataProvider(name="labelingTests")
	public Object[][] getLabelingTests() {
		return new Object[][] {
				new Object[] {
						// Cell identifiers
						new String[][] {
								new String[] {"Vacuum_Pump_A_Volts", "Vacuum_Pump_B_Pressure"},
								new String[] {"Heater_Pump_A_Amps", "Heater_Pump_B_Flow Rate"}
						},
						// Row-major row, column, and cell labels
						new String[] { "Vacuum Pump", "Heater Pump" },
						new String[] { "A", "B" },
						new String[][] {
								new String[] {"Volts", "Pressure"},
								new String[] {"Amps", "Flow Rate"}
						},
						// Column-major row, column, and cell labels
						new String[] { "Vacuum", "Heater" },
						new String[] { "Pump A", "Pump B" },
						new String[][] {
								new String[] {"Volts", "Pressure"},
								new String[] {"Amps", "Flow Rate"}
						}
				},
				
				// Another example, with some blank cells.
				new Object[] {
						// Cell identifiers
						new String[][] {
								new String[] {"Vacuum_Pump_A_Volts", "", "Vacuum_Pump_B_Pressure"},
								new String[] {"", "Heater_Pump_A_Amps", "Heater_Pump_B_Flow Rate"}
						},
						// Row-major row, column, and cell labels
						new String[] { "Vacuum Pump", "Heater Pump" },
						new String[] { "A Volts", "A Amps", "B" },
						new String[][] {
								new String[] {"", "", "Pressure"},
								new String[] {"", "", "Flow Rate"}
						},
						// Column-major row, column, and cell labels
						new String[] { "", "" },
						new String[] { "Vacuum Pump A Volts", "Heater Pump A Amps", "Pump B" },
						new String[][] {
								new String[] {"", "", "Vacuum Pressure"},
								new String[] {"", "", "Heater Flow Rate"}
						}
				},
				
				new Object[] {
						// Cell identifiers
						new String[][] {
								{ "Ch1_Serial_Volts", "Ch2_Serial_Volts" },
								{ "Ch1_Serial_Amps", "Ch2_Serial_Amps" }
						},
						// Row-major row, column, and cell labels
						new String[] { "Serial Volts", "Serial Amps" },
						new String[] { "Ch1", "Ch2" },
						new String[][] {
								{ "", "" },
								{ "", "" }
						},
						// Column-major row, column, and cell labels
						new String[] { "Volts", "Amps" },
						new String[] { "Ch1 Serial", "Ch2 Serial" },
						new String[][] {
								{ "", "" },
								{ "", "" }
						},
				},
				
				new Object[] {
						// Cell identifiers
						new String[][] {
								new String[] {"BCA1_Ch1_Serial_Volts", "BCA1_Ch1_Serial_Amps", "FMT Status/Frame Counter"},
								new String[] {"BCA1_Ch2_Serial_Volts", "BCA1_Ch2_Serial_Amps"}
						},
						// Row-major row, column, and cell labels
						new String[] { "", "BCA1 Ch2 Serial" },
						new String[] { "Volts", "Amps", "FMT Status/Frame Counter" },
						new String[][] {
								new String[] {"BCA1 Ch1 Serial", "BCA1 Ch1 Serial", ""},
								new String[] {"", "", ""}
						},
						// Column-major row, column, and cell labels
						new String[] { "", "Ch2" },
						new String[] { "BCA1 Serial Volts", "BCA1 Serial Amps", "FMT Status/Frame Counter" },
						new String[][] {
								new String[] {"Ch1", "Ch1", ""},
								new String[] {"", "", ""}
						},
				},
		};
	}

	private static class MockLabeledTableModel extends LabeledTableModel {

		private static final long serialVersionUID = 1L;

		private String[][] values = new String[][] {
				new String[] {"BCA1_Ch1_Serial_Volts", "BCA1_Ch2_Serial_Volts"},
				new String[] {"BCA1_Ch1_Serial_Amps",  "BCA1_Ch2_Serial_Amps"}
		};

		private String[] rows = new String[] {"BCA1_Serial_Volts", "BCA1_Serial_Amps"};
		private String[] cols = new String[] {"Ch1", "Ch2"};

		public MockLabeledTableModel() {
			super(new AbbreviatingTableLabelingAlgorithm(), TableOrientation.ROW_MAJOR);
			rowLabels = new String[getRowCount()];
			columnLabels = new String[getColumnCount()];
			cellLabels = new String[getRowCount()][getColumnCount()];
		}

		@Override
		public String getRowName(int rowIndex) {
			return "xx";
		}
		
		@Override
		public String getFullRowName(int rowIndex) {
			return rows[rowIndex];
		}
		
		@Override
		public String getFullColumnName(int i) {
			return cols[i]; 
		}
		
		@Override
		protected int getAttributeCount() {
			return values[0].length;
		}

		@Override
		protected Object getObjectAt(int rowIndex, int columnIndex) {
			return values[rowIndex][columnIndex];
		}

		@Override
		protected int getObjectCount() {
			return values.length;
		}

		@Override
		protected String getObjectIdentifierAt(int rowIndex, int columnIndex) {
			//return "BCA1_Ch1_Serial_Volts";//values[rowIndex][columnIndex].getIdentifier();

			return values[rowIndex][columnIndex];
		}

		@Override
		protected boolean canSetObjectAt(int rowIndex, int columnIndex,
				boolean isInsertRow, boolean isInsertColumn) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected void setObjectAt(Object value, int rowIndex, int columnIndex,
				boolean isInsertRow, boolean isInsertColumn) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object getStoredObjectAt(int objectIndex, int attributeIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isSkeleton() {
			// TODO Auto-generated method stub
			return false;
		}

	}
}


