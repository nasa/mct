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
package gov.nasa.arc.mct.table.dnd;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.table.gui.LabeledTable;
import gov.nasa.arc.mct.table.model.ComponentTableModel;
import gov.nasa.arc.mct.table.model.LabeledTableModel;
import gov.nasa.arc.mct.table.view.MultiColView;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
//import java.awt.datatransfer.UnsupportedFlavorException;
//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a drag-and-drop transfer handler for the tabular data views.
 * The transfer method is compatible with that of the MCT directory area:
 * the data flavor is an array of view roles.
 */
public class TableTransferHandler extends TransferHandler {
	
	private static final long serialVersionUID = 1;

	private static final Logger logger = LoggerFactory.getLogger(TableTransferHandler.class);

	private MultiColView manifestation;
	private LabeledTable table;
	
	/**
	 * Creates a new transfer handler for a table and a manifestation of that table.
	 * 
	 * @param manifestation the table view manifestation
	 * @param table the labeled table being shown in the view manifestation
	 */
	public TableTransferHandler(MultiColView manifestation, LabeledTable table) {
		this.manifestation = manifestation;
		this.table = table;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		JTable.DropLocation location = (javax.swing.JTable.DropLocation) support.getDropLocation();
		boolean result = internalCanImport(support.getDataFlavors(), location.getRow(), location.getColumn(),
				location.isInsertRow(), location.isInsertColumn());
		
        support.setShowDropLocation(result);
        return result;
	}
	
	private boolean internalCanImport(DataFlavor[] flavors, int row, int column, boolean isInsertRow, boolean isInsertColumn) {
        // We can only import view roles, usually from the directory tree
        // or from a table cell.
        if (flavors.length < 1 || flavors[0].getRepresentationClass() != View.class) {
        	return false;
        }

        // Ask the model whether a value can be dropped at the location.
        LabeledTableModel model = LabeledTableModel.class.cast(table.getTable().getModel());
        return model.canSetValueAt(row, column, isInsertRow, isInsertColumn);
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
        // We must have at least one selected row.
        if (table.getSelectedRows().length==0 || table.getSelectedColumns().length==0) {
        	return null;
        }
        
        // Add a node view role for each selected component.
        List<View> views = new ArrayList<View>();
        ComponentTableModel model = ComponentTableModel.class.cast(table.getTable().getModel());
        for (int rowIndex : table.getSelectedRows()) {
        	for (int columnIndex : table.getSelectedColumns()) {
        		AbstractComponent component = (AbstractComponent) model.getStoredValueAt(rowIndex, columnIndex);
        		if (component != null) {
        			View view = component.getViewInfos(ViewType.NODE).iterator().next().createView(component);
        			if (view != null) {
        				views.add(view);
        			}
        		}
        	}
        }

        // If we didn't find any applicable roles, just return null so that we
        // don't allow the drag.
        if (views.isEmpty()) {
            return null;
        }
        
        return new ViewRoleSelection(views.toArray(new View[views.size()]));
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.LINK;
	}

	//commented out as part of the TableStructure setValue() debacle
	/*@Override
	public boolean importData(TransferSupport support) {
        JTable.DropLocation location = (JTable.DropLocation) support.getDropLocation();
        
        int row = location.getRow();
        int column = location.getColumn();
        boolean isInsertRow = location.isInsertRow();
        boolean isInsertColumn = location.isInsertColumn();

        View[] sourceViewRoles;
        try {
            sourceViewRoles = (View[]) support.getTransferable().getTransferData(View.DATA_FLAVOR);
        } catch (UnsupportedFlavorException e) {
            logger.error("Unexpected drop flavor", e);
            return false;
        } catch (IOException e) {
            logger.error("Exception getting dropped data", e);
            return false;
        }
        
        try {
        	return manifestation.handleDrop(sourceViewRoles, row, column, isInsertRow, isInsertColumn);
        } catch (Exception e) {
        	logger.error("Unexpected error during drop", e);
        	return false;
        }
    }*/
    
}
