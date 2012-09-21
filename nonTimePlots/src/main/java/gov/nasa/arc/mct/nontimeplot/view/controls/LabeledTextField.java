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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class LabeledTextField extends JPanel implements NonTimeControlElement, ActionListener, FocusListener {
	private static final long serialVersionUID = 2570435681488814783L;
	private JLabel     label;
	private JTextField field;
	
	private OutputType outputType;
	
	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public LabeledTextField(String text) {
		this(text, OutputType.DOUBLE);
	}
	
	public LabeledTextField(String text, OutputType outputType) {
		this.outputType = outputType;
		add(label = new JLabel(text));
		add(field = new JTextField(outputType.fieldSize()));
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.WEST, field, 0, SpringLayout.EAST, label);
		layout.putConstraint(SpringLayout.EAST, this, 0, SpringLayout.EAST, field);
		
		layout.putConstraint(SpringLayout.NORTH, field, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, this,  0, SpringLayout.SOUTH, field);
		
		layout.putConstraint(SpringLayout.NORTH, label, 0, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.SOUTH, this);
		
		field.addActionListener(this);
		field.addFocusListener(this);
	}
	

	@Override
	public void setValues(double... values) {
		if (values.length > 0) field.setText(outputType.interpret(values[0]));
	}

	@Override
	public double[] getValues() {
		return new double[] { outputType.interpret(field.getText()) };
	}

	@Override
	public void addActionListener(ActionListener al) {
		actionListeners.add(al);
	}

	public static enum OutputType {
		DOUBLE() {
			@Override
			int fieldSize() {
				return 8;
			}

			@Override
			double interpret(String value) throws NumberFormatException {
				return Double.parseDouble(value);
			}

			@Override
			String interpret(double value) {
				return Double.toString(value);
			}			
		},
		INTEGER() {
			@Override
			int fieldSize() {
				return 4;
			}

			@Override
			double interpret(String value) throws NumberFormatException  {
				return (double) Integer.parseInt(value);
			}

			@Override
			String interpret(double value) {
				return Integer.toString((int) value);
			}			
		};
		abstract int fieldSize();
		abstract double interpret(String value);
		abstract String interpret(double value);
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		setAlertState(false);
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		actionPerformed(null); //TODO don't use null in case other ActionListeners are later attached
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			String text = field.getText();
			double d = outputType.interpret(text);
			for (ActionListener al : actionListeners) {
				al.actionPerformed(e);
			}
		} catch (NumberFormatException nfe) {
			setAlertState(true);
		}
	}
	
	public void setAlertState(boolean alertState) {
		field.setForeground(alertState ? Color.RED : Color.BLACK);
	}

}
