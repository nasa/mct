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

package org.acme.example.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.table.TableColumn;

public class ViewSettings {
	//Which columns are currently being displayed.
	private ArrayList<ColumnType> columnTypes;
	
	private Map<String,TableColumn> hiddenColumns;
	
	public ViewSettings() {
		setAllToDefaults();
		hiddenColumns = new HashMap<String,TableColumn>();
	}
	
	public void setAllToDefaults() {
		columnTypes = new ArrayList<ColumnType>();
		columnTypes.add(ColumnType.ID);
		columnTypes.add(ColumnType.TITLE);
		columnTypes.add(ColumnType.VALUE);		
		columnTypes.add(ColumnType.TIME);
	}
	
	public Set<String> getHiddenColumnIds() {
		return hiddenColumns.keySet();
	}
	public void hideColumn(TableColumn column, String identifier) {
		hiddenColumns.put(identifier, column);
	}
	
	public TableColumn retrieveColumn(String identifier) {
		TableColumn column = hiddenColumns.get(identifier);
		hiddenColumns.remove(identifier);
		return column;
	}
	
	public int getNumberOfColumns() { return columnTypes.size(); }
	
	public ColumnType getColumnAtIndex(int colIndex) {
		return columnTypes.get(colIndex);
	}
	
	public int getIndexForColumn(ColumnType colValue) {
		return columnTypes.indexOf(colValue);
	}

	public boolean isDisplayingColumn(ColumnType colValue) {
		return columnTypes.contains(colValue);
	}
		
	public void addColumnType(ColumnType colValue) {
		columnTypes.add(colValue);
	}
	
	public boolean removeColumnType(ColumnType colValue) {
		return columnTypes.remove(colValue);
	}
	
	public ArrayList<ColumnType> getColumnTypes() {
		return columnTypes;
	}
	
}


