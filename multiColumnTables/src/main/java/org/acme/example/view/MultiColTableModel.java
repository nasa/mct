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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.Placeholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
class MultiColTableModel extends AbstractTableModel {
	private List<AbstractComponent> childrenList;
	private ViewSettings settings;
	
	private Map<String, Object> values = new HashMap<String, Object>();
	private Map<String,List<Integer>> componentLocations;
	
	public MultiColTableModel(List<AbstractComponent> childrenList, ViewSettings settings) {
		this.settings = settings;
		this.childrenList = childrenList;
		updateLocations();
	}
	
	@Override
	public int getColumnCount() { return settings.getNumberOfColumns(); }
	@Override
	public int getRowCount()    { return childrenList.size(); }
	
	public AbstractComponent getComponentOfRow(int rowIndex) {
		return childrenList.get(rowIndex);
	}

	@Override
	public String getColumnName(int colIndex) {
		ColumnType colType = settings.getColumnAtIndex(colIndex);
		return colType.name();
	}
	
	private void updateLocations() {
		componentLocations = new HashMap<String,List<Integer>>();
		for (int row=0; row < getRowCount(); ++row) {
			AbstractComponent component = childrenList.get(row);
			if (component != null) {
				List<Integer> locations = componentLocations.get(getKey(component));
				if (locations == null) {
					locations = new ArrayList<Integer>();
					componentLocations.put(getKey(component), locations);
				}
				locations.add(row);
			}
		}
	}
	
	/**
	 * Returns a unique key for a given component. This key is used by the
	 * code that responds to a feed update to pass along changes to the
	 * component's value.
	 * 
	 * @param component the component for which to determine the key
	 * @return a unique key for the component
	 */
	public String getKey(AbstractComponent component) {
		AbstractComponent delegate = component;
		FeedProvider fp = component.getCapability(FeedProvider.class);
		if (fp != null) {
			return fp.getSubscriptionId();
		}		
		return delegate.getComponentId();
	}
	
	@Override
	public Object getValueAt(int r, int c) {
		ColumnType colType = settings.getColumnAtIndex(c);
		Object cellDatum = null;
		AbstractComponent cellComponent = childrenList.get(r);
		switch(colType) {
		case ID:
			cellDatum = cellComponent.getId(); break;
		case TITLE:
			cellDatum = cellComponent.getDisplayName(); break;
		case VALUE:
			cellDatum = getValueForComponent(cellComponent); break;
		case TIME:
			cellDatum = (FeedProvider) cellComponent; break;
		}
		if(cellDatum==null) { return "(no data)"; }
		else                { return cellDatum; }
	}
	
	/**
	 * Sets the value of an object updated by a data feed. This change
	 * is propagated to all table cells displaying that object.
	 * 
	 * @param id the identifier for the object updated
	 * @param value the new value to display
	 */
	public void setValue(String id, Object value) {
		values.put(id, value);
		List<Integer> locations = componentLocations.get(id);
		if (locations != null) {
			for (Integer row : locations) {
				fireTableCellUpdated(row, settings.getIndexForColumn(ColumnType.VALUE)); 
				fireTableCellUpdated(row, settings.getIndexForColumn(ColumnType.TIME)); 
			}
		}
	}
	
	private Object getValueForComponent(AbstractComponent component) {
		Object value = values.get(getKey(component));
		if (value == null) {
			DisplayedValue displayedValue = new DisplayedValue();
			if (component.getCapability(Placeholder.class) != null) {
			    displayedValue.setValue(component.getCapability(Placeholder.class).getPlaceholderValue());
			} else {
				displayedValue.setValue(component.getDisplayName());
			}
			return displayedValue;
		} else {
			return value;
		}
	}
}