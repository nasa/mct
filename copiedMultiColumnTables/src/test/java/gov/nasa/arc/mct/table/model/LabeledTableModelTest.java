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

import javax.swing.ListModel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LabeledTableModelTest {
	
	private IdentifiedObject[][] values;
	private LabeledTableModel model;
	
	@BeforeMethod
	public void init() {
		values = new IdentifiedObject[][] {
				{ new IdentifiedObject("a", "alpha"), new IdentifiedObject("b", "bravo"), new IdentifiedObject("c", "charlie") }
		};
		
		model = new MockLabeledTableModel(values);
	}
	
	@Test
	public void testGetSetOrientation() {
		assertEquals(model.getOrientation(), TableOrientation.ROW_MAJOR);
		
		model.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(model.getOrientation(), TableOrientation.COLUMN_MAJOR);
	}

	@Test
	public void testGetRowCount() {
		assertEquals(model.getRowCount(), values.length);
		
		model.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(model.getRowCount(), values[0].length);
	}
	
	@Test
	public void testGetColumnCount() {
		assertEquals(model.getColumnCount(), values[0].length);
		
		model.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(model.getColumnCount(), values.length);
	}
	
	@Test
	public void testGetValueAt() {
		assertEquals(model.getValueAt(0,2), values[0][2].getValue());
		
		model.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(model.getValueAt(2,0), values[0][2].getValue());
	}
	
	@Test
	public void testColumnModel() {
		model.updateLabels();
		ListModel columnModel = model.getColumnLabelModel();
		assertEquals(columnModel.getElementAt(0), "a");
		
		// Should have no column labels for a 1xN table with only one column.
		model.setOrientation(TableOrientation.COLUMN_MAJOR);
		assertEquals(columnModel.getElementAt(0), "");
	}
	
	private static class IdentifiedObject {
		
		private String identifier;
		private Object value;
		
		public IdentifiedObject(String identifier, Object value) {
			this.identifier = identifier;
			this.value = value;
		}

		public String getIdentifier() {
			return identifier;
		}

		public Object getValue() {
			return value;
		}
		
	}
	
	private static class MockLabeledTableModel extends LabeledTableModel {

		private static final long serialVersionUID = 1L;

		private IdentifiedObject[][] values;

		public MockLabeledTableModel(IdentifiedObject[][] values) {
			super(new AbbreviatingTableLabelingAlgorithm(), TableOrientation.ROW_MAJOR);
			this.values = values;
		}

		@Override
		protected int getAttributeCount() {
			return values[0].length;
		}

		@Override
		protected Object getObjectAt(int rowIndex, int columnIndex) {
			return values[rowIndex][columnIndex].getValue();
		}

		@Override
		protected int getObjectCount() {
			return values.length;
		}

		@Override
		protected String getObjectIdentifierAt(int rowIndex, int columnIndex) {
			return values[rowIndex][columnIndex].getIdentifier();
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
