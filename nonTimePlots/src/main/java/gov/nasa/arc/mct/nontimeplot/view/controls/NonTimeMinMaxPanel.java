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

import gov.nasa.arc.mct.nontimeplot.view.controls.NonTimeControlPanel.NonTimeControlElement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class NonTimeMinMaxPanel extends JPanel implements NonTimeControlElement, ActionListener {
	private static final long serialVersionUID = 4883137388714665327L;

	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private LabeledTextField minField, maxField;
	
	public NonTimeMinMaxPanel(boolean horizontal) {
		
		setLayout (new BorderLayout());
		
		minField = new LabeledTextField("Min: ");
		maxField = new LabeledTextField("Max: ");		
		add (minField, horizontal ? BorderLayout.WEST : BorderLayout.SOUTH);
		add (maxField, horizontal ? BorderLayout.EAST : BorderLayout.NORTH);
		minField.addActionListener(this);
		maxField.addActionListener(this);
		
		add (horizontal ? Box.createHorizontalStrut(16) : Box.createVerticalStrut(12), BorderLayout.CENTER);		
	}
	

	private JPanel makePanel(JComponent left, JComponent right) {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(left, BorderLayout.WEST);
		p.add(right, BorderLayout.EAST);
		return p;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		double values[] = getValues();
		boolean valid = values[0] < values[1];
		if (valid) for (ActionListener al : actionListeners) al.actionPerformed(e);
		minField.setAlertState(!valid);
		maxField.setAlertState(!valid);
	}

	@Override
	public void setValues(double... values) {
		minField.setValues(values[0]);
		maxField.setValues(values[1]);
	}

	@Override
	public double[] getValues() {
		return new double[] { minField.getValues()[0], maxField.getValues()[0] };
	}


	@Override
	public void addActionListener(ActionListener al) {
		actionListeners.add(al);
	}
}
