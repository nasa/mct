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
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TableLabelingAlgorithmTest {
	
	private String[][] cellIdentifiers = new String[][] {
			new String[] { "A1", "B1", "C1" },
			new String[] { "A2", "B2", "C2" }
	};
	
	private LabeledTableModel model;
	private SwitchingLabelingAlgorithm algorithm;
	
	@BeforeMethod
	public void init() {
		algorithm = new SwitchingLabelingAlgorithm();
		model = new MockTableModel(cellIdentifiers, algorithm);
		model.updateLabels();
	}

	@Test
	public void testDefaultLabeling() {
		for (int row=0; row < model.getRowCount(); ++row) {
			assertNull(model.getRowName(row));
		}
		for (int col=0; col < model.getColumnCount(); ++col) {
			assertNull(model.getColumnName(col));
		}
		for (int row=0; row < model.getRowCount(); ++row) {
			for (int col=0; col < model.getColumnCount(); ++col) {
				assertNull(model.getCellName(row, col));
			}
			
		}
	}
	
	@Test
	public void testIdentifierLabeling() {
		algorithm.setAlgorithm(SwitchingLabelingAlgorithm.Algorithm.USE_IDENTIFIERS);
		model.updateLabels();
		
		for (int row=0; row < model.getRowCount(); ++row) {
			assertEquals(model.getRowName(row), cellIdentifiers[row][0]);
		}
		for (int col=0; col < model.getColumnCount(); ++col) {
			assertEquals(model.getColumnName(col), cellIdentifiers[0][col]);
		}
		for (int row=0; row < model.getRowCount(); ++row) {
			for (int col=0; col < model.getColumnCount(); ++col) {
				assertEquals(model.getCellName(row, col), cellIdentifiers[row][col]);
			}
			
		}
	}
	
	@Test
	public void testIndexedLabeling() {
		algorithm.setAlgorithm(SwitchingLabelingAlgorithm.Algorithm.USE_INDICES);
		model.updateLabels();
		
		for (int row=0; row < model.getRowCount(); ++row) {
			assertEquals(model.getRowName(row), "Row " + row);
		}
		for (int col=0; col < model.getColumnCount(); ++col) {
			assertEquals(model.getColumnName(col), "Column " + col);
		}
		for (int row=0; row < model.getRowCount(); ++row) {
			for (int col=0; col < model.getColumnCount(); ++col) {
				assertEquals(model.getCellName(row, col), "" + row + "," + col);
			}
			
		}
	}
	
	private static class SwitchingLabelingAlgorithm extends TableLabelingAlgorithm {

		public static enum Algorithm {USE_NULL, USE_IDENTIFIERS, USE_INDICES};
		
		private Algorithm algorithm = Algorithm.USE_NULL;
		
		public SwitchingLabelingAlgorithm() {
			super(TableOrientation.ROW_MAJOR);
		}
		
		public void setAlgorithm(Algorithm algorithm) {
			this.algorithm = algorithm;
		}
		
		@Override
		public void computeLabels(LabeledTableModel model) {
			if (algorithm == Algorithm.USE_IDENTIFIERS) {
				computedIdentifiedLabels(model);
			} else if (algorithm == Algorithm.USE_INDICES){
				computeIndexLabels(model);
			}
			// else leave labels null
		}

		private void computeIndexLabels(LabeledTableModel model) {
			for (int row=0; row < model.getRowCount(); ++row) {
				model.setRowName(row, "Row " + row);
			}
			
			for (int col=0; col < model.getColumnCount(); ++col) {
				model.setColumnName(col, "Column " + col);
			}
			
			for (int row=0; row < model.getRowCount(); ++row) {
				for (int col=0; col < model.getColumnCount(); ++col) {
					model.setCellName(row, col, "" + row + "," + col);
				}
			}
		}

		private void computedIdentifiedLabels(LabeledTableModel model) {
			for (int row=0; row < model.getRowCount(); ++row) {
				model.setRowName(row, model.getIdentifierAt(row, 0));
			}
			
			for (int col=0; col < model.getColumnCount(); ++col) {
				model.setColumnName(col, model.getIdentifierAt(0, col));
			}
			
			for (int row=0; row < model.getRowCount(); ++row) {
				for (int col=0; col < model.getColumnCount(); ++col) {
					model.setCellName(row, col, model.getIdentifierAt(row, col));
				}
			}
		}

	}
	
}
