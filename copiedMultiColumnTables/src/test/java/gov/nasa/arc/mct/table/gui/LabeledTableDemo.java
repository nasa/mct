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
package gov.nasa.arc.mct.table.gui;

import gov.nasa.arc.mct.table.model.AbbreviatingTableLabelingAlgorithm;
import gov.nasa.arc.mct.table.model.LabeledTableModel;
import gov.nasa.arc.mct.table.model.TableOrientation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LabeledTableDemo {

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	@SuppressWarnings("serial")
	private static void createAndShowGUI() {
		final String[][] identifiers = {
				{ "Ku_VBSP_Ch_1_Mode_Select", "Ku_VBSP_Ch_1_Field_Rate", "Ku_VBSP_Ch_1_Reserved_Bit",
					"Ku_VBSP_Ch_1_Fiber_Optic_Activity", "Ku_VBSP_Ch_1_PFM_Activity", "Ku_VBSP_Ch_1_Return_Link_Sync_Lock" },
				{ "Ku_VBSP_Ch_2_Mode_Select", "Ku_VBSP_Ch_2_Field_Rate", "Ku_VBSP_Ch_2_Reserved_Bit",
					"Ku_VBSP_Ch_2_Fiber_Optic_Activity", "Ku_VBSP_Ch_2_PFM_Activity", "Ku_VBSP_Ch_2_Return_Link_Sync_Lock" },
				{ "Ku_VBSP_Ch_3_Mode_Select", "Ku_VBSP_Ch_3_Field_Rate", "Ku_VBSP_Ch_3_Reserved_Bit",
					"Ku_VBSP_Ch_3_Fiber_Optic_Activity", "Ku_VBSP_Ch_3_PFM_Activity", "Ku_VBSP_Ch_3_Return_Link_Sync_Lock" },
				{ "Ku_VBSP_Ch_4_Mode_Select", "Ku_VBSP_Ch_4_Field_Rate", "Ku_VBSP_Ch_4_Reserved_Bit",
					"Ku_VBSP_Ch_4_Fiber_Optic_Activity", "Ku_VBSP_Ch_4_PFM_Activity", "Ku_VBSP_Ch_4_Return_Link_Sync_Lock" },
		};
		
		final Object[][] data = {
				{ 3, 60, 6, 1, 1, 1 },
				{ 3, 30, 6, 1, 1, 1 },
				{ 3, 30, 6, 1, 1, 1 },
				{ 3, 30, 6, 1, 1, 1 },
		};
		
		assert identifiers.length == 4;
		assert data.length == 4;

		AbbreviatingTableLabelingAlgorithm algorithm = new AbbreviatingTableLabelingAlgorithm();
		algorithm.setOrientation(TableOrientation.COLUMN_MAJOR);
		algorithm.setContextLabels("Ku_VBSP");

		LabeledTableModel model = new LabeledTableModel(algorithm, TableOrientation.COLUMN_MAJOR) {

			@Override
			public int getObjectCount() {
				return data.length;
			}

			@Override
			public int getAttributeCount() {
				return data[0].length;
			}

			@Override
			public Object getObjectAt(int rowIndex, int columnIndex) {
				return data[rowIndex][columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

			@Override
			public String getObjectIdentifierAt(int rowIndex, int columnIndex) {
				// TODO Auto-generated method stub
				return identifiers[rowIndex][columnIndex];
			}

			@Override
			protected boolean canSetObjectAt(int rowIndex, int columnIndex,
					boolean isInsertRow, boolean isInsertColumn) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected void setObjectAt(Object value, int rowIndex,
					int columnIndex, boolean isInsertRow, boolean isInsertColumn) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getStoredObjectAt(int objectIndex, int attributeIndex) {
				return data[objectIndex][attributeIndex];
			}

			@Override
			public boolean isSkeleton() {
				// TODO Auto-generated method stub
				return false;
			}

		};
		model.updateLabels();

		//Create and set up the window.
		JFrame frame = new JFrame("SimpleTableDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		final LabeledTable table = new LabeledTable(model);
		table.setOpaque(true); //content panes must be opaque
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		JButton toggleRowHeader = new JButton("Toggle Row Headers");
		buttonPanel.add(toggleRowHeader);
		toggleRowHeader.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				table.setRowHeadersVisible(!table.isRowHeadersVisible());
			}
		});
		
		JButton toggleColumnHeader = new JButton("Toggle Column Headers");
		buttonPanel.add(toggleColumnHeader);
		toggleColumnHeader.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				table.setColumnHeadersVisible(!table.isColumnHeadersVisible());
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(table, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		frame.setContentPane(panel);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
