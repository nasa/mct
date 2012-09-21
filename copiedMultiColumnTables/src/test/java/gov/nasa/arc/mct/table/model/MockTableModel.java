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
/**
 * 
 */
package gov.nasa.arc.mct.table.model;

import javax.swing.event.TableModelEvent;

@SuppressWarnings("serial")
public class MockTableModel extends LabeledTableModel {
	
	private String[][] cellIdentifiers;
	boolean isSkeleton = false;

	public MockTableModel(String[][] cellIdentifiers, TableLabelingAlgorithm algorithm) {
		super(algorithm, TableOrientation.ROW_MAJOR);
		this.cellIdentifiers = cellIdentifiers;
	}

	@Override
	protected int getAttributeCount() {
		int maxCols = 0;
		
		for (String[] rowIdentifiers : cellIdentifiers) {
			if (rowIdentifiers.length > maxCols) {
				maxCols = rowIdentifiers.length;
			}
		}
		
		return maxCols;
	}

	@Override
	protected int getObjectCount() {
		return cellIdentifiers.length;
	}

	@Override
	protected Object getObjectAt(int rowIndex, int columnIndex) {
		return null;
	}

	@Override
	public String getObjectIdentifierAt(int rowIndex, int columnIndex) {
		if (columnIndex >= cellIdentifiers[rowIndex].length) {
			return "";
		} else {
			return cellIdentifiers[rowIndex][columnIndex];
		}
	}

	public void fireTableChanged() {
		TableModelEvent e = new TableModelEvent(this);
		fireTableChanged(e);
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
		return isSkeleton;
	}
	
	public void setIsSkeleton(boolean value) {
		isSkeleton = value;
	}

}