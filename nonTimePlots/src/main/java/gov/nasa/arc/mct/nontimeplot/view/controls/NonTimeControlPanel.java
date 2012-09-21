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
package gov.nasa.arc.mct.nontimeplot.view.controls;

import gov.nasa.arc.mct.nontimeplot.view.controls.LabeledTextField.OutputType;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NonTimeControlPanel extends JPanel {
	private static final long serialVersionUID = -1416453078528423628L;

	private static Icon EXAMPLE_PLOT;
	private JComponent control[] = new JComponent[3]; // x, y, z
	private JComponent image = null;
	private NonTimePlotSettings settings;
	
	
	
	public NonTimeControlPanel(NonTimePlotSettings settings) {
		this.settings = settings;

		if (EXAMPLE_PLOT == null) {
			EXAMPLE_PLOT = new ImageIcon(getClass().getClassLoader().getResource("images/nonTimeExample.png"));
		}
		

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Plot Setup"));
		
		control[0] = new NonTimeMinMaxPanel(true);
		control[1] = new NonTimeMinMaxPanel(false);
		control[2] = new LabeledTextField("Data Points: ", OutputType.INTEGER);
		image = new JLabel(EXAMPLE_PLOT);
		
		((NonTimeControlElement) control[0]).setValues(settings.getIndependentBounds());
		((NonTimeControlElement) control[1]).setValues(settings.getDependentBounds());
		((NonTimeControlElement) control[2]).setValues(settings.getDataPoints());
		
		
		for (int i = 0; i < control.length; i++) {
			panel.add(control[i]);
			if (control[i] instanceof NonTimeControlElement) {
				((NonTimeControlElement) control[i]).addActionListener(
						new AxisBoundsActionListener(i, (NonTimeControlElement) control[i]));
			}
		}
		for (JComponent c : control) panel.add(c);
		panel.add(image);
		
		setupLayout(panel);
		
		setLayout(new BorderLayout());
		JPanel p = new JPanel (new BorderLayout());
		p.add(panel, BorderLayout.WEST);
		add(p, BorderLayout.NORTH);
		
	}
	

	private void setupLayout(JPanel p) {
		SpringLayout layout = new SpringLayout();
		p.setLayout(layout);
		
		layout.putConstraint(SpringLayout.WEST, control[1], 0, SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.WEST, image, 0, SpringLayout.EAST, control[1]);
		layout.putConstraint(SpringLayout.EAST, p, 0, SpringLayout.EAST, image);
		
		layout.putConstraint(SpringLayout.NORTH, image, 0, SpringLayout.NORTH, p);
		layout.putConstraint(SpringLayout.NORTH, control[0], 0, SpringLayout.SOUTH, image);
		layout.putConstraint(SpringLayout.SOUTH, p, 0, SpringLayout.SOUTH, control[0]);
		
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, control[1], 0, SpringLayout.VERTICAL_CENTER, image);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, control[0], 0, SpringLayout.HORIZONTAL_CENTER, image);
		
//		layout.putConstraint(SpringLayout.WEST,  control[2], 0, SpringLayout.WEST,  p);
//		layout.putConstraint(SpringLayout.SOUTH, control[2], 0, SpringLayout.SOUTH, p);
		layout.putConstraint(SpringLayout.NORTH, control[2], 0, SpringLayout.SOUTH, image);
		layout.putConstraint(SpringLayout.EAST,  control[2], 0, SpringLayout.WEST,  image);
		
	}
	
	private class AxisBoundsActionListener implements ActionListener {
		private int axis;
		private NonTimeControlElement element;
		
		public AxisBoundsActionListener(int axis, NonTimeControlElement element) { 
			this.axis = axis;
			this.element = element;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			double[] values = element.getValues();
			switch (axis) {
			case 0:
				settings.setIndependentBounds(values[0], values[1]);
				break;
			case 1:
				settings.setDependentBounds(values[0], values[1]);
				break;
			case 2:
				settings.setDataPoints((int) values[0]);
				break;				
			}
		}		
	}
	
	public interface NonTimeControlElement {
		public void setValues(double... values);
		public double[] getValues();
		public void addActionListener(ActionListener al);
	}
	
}
