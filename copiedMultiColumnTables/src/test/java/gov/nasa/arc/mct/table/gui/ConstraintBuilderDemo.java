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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ConstraintBuilderDemo {
	
	public void run() {
		final TaggedComponentManager mgr = new TaggedComponentManager();
		
		JFrame frame = new JFrame("Test");
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		final PanelSectionHeader header = new PanelSectionHeader("Row/Column Format");
		final JLabel label = new JLabel("Hello, world!");
		
		mgr.tagComponents("hello", label);
		
		final JButton hideShow = new JButton("Hide");
		hideShow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (label.isVisible()) {
					mgr.hide("hello", true);
				} else {
					mgr.show("hello", true);
				}
				hideShow.setText(label.isVisible() ? "Hide" : "Show");
			}
		});
		
		JLabel left = new JLabel("Left");
		JLabel right = new JLabel("Right");
		
		JLabel one = new JLabel("one");
		JPanel space = new JPanel();
		space.setSize(10, 1);
		JLabel two = new JLabel("two");
		
		ConstraintBuilder builder = new ConstraintBuilder(frame);
		builder.baseline_w().makeDefault();
	
		builder.span(1,2).hfill().add(header);
		
		// Label and left/right are toggled by the panel header.
		builder.nextRow().span(1,2).insets(5, 5, 5, 5).hfill().add(label);
		builder.nextRow().insets(5, 0, 5, 0).nw().add(left);
		builder.insets(5, 0, 5, 0).sw().add(right);
		
		header.addToggledComponents(label, left, right);
		
		builder.nextRow().vpad(10).baseline_w().vfill().add(hideShow);
		
		builder.nextRow().span(1, 2).add(one, space, two);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new ConstraintBuilderDemo().run();
			}
		});
	}

}
