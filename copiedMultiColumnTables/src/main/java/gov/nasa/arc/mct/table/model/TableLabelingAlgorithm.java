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


/**
 * Defines the interface to a specific table labeling algorithm.
 * The labeling algorithm will be called when the table data model
 * changes to update the table labeling model.
 */
public abstract class TableLabelingAlgorithm {

	private TableOrientation orientation;
	
	/**
	 * Creates a new labeling algorithm with the given table orientation.
	 * 
	 * @param orientation the initial orientation of the table.
	 */
	public TableLabelingAlgorithm(TableOrientation orientation) {
		this.orientation = orientation;
	}
	
	/**
	 * Gets the orientation of the table to use for calculating the
	 * labels. The orientation determines whether row or column labels
	 * are calculated first. The labels calculated first contain the
	 * words in common along that row or column, whereas the other
	 * axis distinguishes the elements in that row or column.
	 * 
	 * @return the algorithm orientation
	 */
	public TableOrientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Sets the table orientation for the labeling algorithm.
	 * 
	 * @param newOrientation the new orientation of the table
	 */
	public void setOrientation(TableOrientation newOrientation) {
		orientation = newOrientation;
	}
	
	/**
	 * Recalculates the labels for the rows, columns, and cells, and
	 * inserts them into the label model.
	 * 
	 * @param model the table label model
	 */
	public abstract void computeLabels(LabeledTableModel model);
	
}
