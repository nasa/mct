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
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.acme.example.component.ExampleComponent;
import org.acme.example.component.ExampleModelRole;

/**
 * The <code>SaveModelStateViewRole</code> class provides a view exposed in the inspector area, which is
 * the right hand side of a window. This view demonstrates how to mutate, save, and respond to changes
 * in model data. 
 *
 */
@SuppressWarnings("serial")
public final class SaveModelStateView extends View {
	// use a resource bundle for strings to enable localization in the future if required
	private static ResourceBundle bundle = ResourceBundle.getBundle("ExampleResourceBundle");
    //private JFormattedTextField doubleDataTextField;
	private JTextField doubleDataTextField;
    private JTextField descriptionTextField;
    private JButton saveButton = new JButton(bundle.getString("SaveButton"));
    
    // get this component and its associated model role
    private ExampleModelRole mr = ExampleComponent.class.cast(getManifestedComponent()).getModel(); 

    //tiny method used in determining whether the value of doubleDataTextField is valid; if there exists
    //a character in the string they entered, the input is not valid.
    private boolean containsLetters(String s) {
    	for( char c: s.toCharArray()) {
    		if( Character.isLetter(c) )
    			return true;
    	}
    	return false;
    }
    
    // create the GUI: How is this called? Well,ExampleComponentProvider tells MCT that SaveStateModel is a view
    public SaveModelStateView(AbstractComponent ac, ViewInfo vi)  {
        super(ac,vi);
       
        // This GUI allows a user to modify the component's data and persist it.
        TitledBorder titledBorder = BorderFactory.createTitledBorder(bundle.getString("ModelBorderTitle"));
        final JPanel jp = new JPanel();
        jp.setBorder(titledBorder);
        descriptionTextField = new JTextField();
        descriptionTextField.setText(mr.getData().getDataDescription());
        descriptionTextField.setToolTipText(bundle.getString("DescriptionToolTip"));
        doubleDataTextField = new JTextField(String.valueOf(mr.getData().getDoubleData()));
        doubleDataTextField.setText(String.valueOf(mr.getData().getDoubleData()));
        doubleDataTextField.setToolTipText(bundle.getString("ValueToolTip"));
        
        /*Purpose for DocumentListener: disable the save button for illegal inputs in the doubleDataTextField.
         * An illegal input is when the user enters a non-number.  Note: parse double
         * parses "-46.81d" as "-46.81d" (that is, it allows d, i, L, f at the end of
         * a number).  I wanted to disable the save button if the user enters any letter,
         * so that is why I first check if the text-field contains any letters before trying
         * to parse it as a double.
         */
        doubleDataTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				if( !saveButton.isEnabled() )
					saveButton.setEnabled(true);
				try {
					if( containsLetters(doubleDataTextField.getText()))
						throw new NumberFormatException();
					
					Double.parseDouble(doubleDataTextField.getText());
					
				} catch (NumberFormatException s) {
					saveButton.setEnabled(false);
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				
				if( !saveButton.isEnabled() )
					saveButton.setEnabled(true);
				try {
					if( containsLetters(doubleDataTextField.getText()))
						throw new NumberFormatException();
					
					Double.parseDouble(doubleDataTextField.getText());
					
				} catch (NumberFormatException s) {
					saveButton.setEnabled(false);
				}
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				if( !saveButton.isEnabled() )
					saveButton.setEnabled(true);
				try {
					if( containsLetters(doubleDataTextField.getText()))
						throw new NumberFormatException();
					
					Double.parseDouble(doubleDataTextField.getText());
					
				} catch (NumberFormatException s) {
					saveButton.setEnabled(false);
				}
			}
		});
        
       jp.add(descriptionTextField);
       jp.add(doubleDataTextField);
       
       addToLayout(jp, bundle.getString("Description"), descriptionTextField, 
         				bundle.getString("Value"), doubleDataTextField, saveButton);
        
        saveButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {          	
                AbstractComponent component = getManifestedComponent();
                
                mr.getData().setDoubleData(Double.parseDouble(doubleDataTextField.getText()));
			    mr.getData().setDataDescription(descriptionTextField.getText());
      		
        		/*
        		 * Note that save() tells MCT that the user wishes to persist the data into the database.  Thus, when the user
        		 * clicks save, and then switches away from this view (like by choosing the info-view) MCT will prompt the user
        		 * if they wish to save the data. 
        		 */
        		component.save();  
        	}
        });
       
        //remember: MCT class 'View' extends a JPanel
        add(jp);
    }
    
    /**
     * Configure the grid bag layout that is not really relevant for demonstrating
     * MCT API usage. 
     **/
    private void addToLayout(Container c, String topLabelString, JTextField topField, 
    						String bottomLabelString, JTextField bottomField, JButton button) {
    	GridBagLayout gbl = new GridBagLayout();
		c.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(5, 5, 0, 0);
		
		JLabel topLabel = new JLabel(topLabelString);
		topLabel.setLabelFor(topField);
		JLabel bottomLabel = new JLabel(bottomLabelString);
		bottomLabel.setLabelFor(bottomField);
		c.add(topLabel, gbc);
		gbc.gridy = 1;
		c.add(bottomLabel, gbc);
		gbc.gridy = 2;
		gbc.gridx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.insets = new Insets(5, 5, 0, 5);
		c.add(button, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 0, 0, 5);
		c.add(topField, gbc);
		gbc.gridy = 1;
		c.add(bottomField, gbc);
    }
    
    // This method is a callback from BaseComponent refreshViewManifestations().
	// Its job is to refresh all the GUI manifestations of this view role. In the case of updating the model,
    // the refresh is done by copying new data from the model to the GUI visual elements.
	// In our example, the visual elements are a text field and a double value field.
    @Override
    public void updateMonitoredGUI() {
    	ExampleModelRole mr =  ExampleComponent.class.cast(getManifestedComponent()).getModel();
    	doubleDataTextField.setText(String.valueOf(mr.getData().getDoubleData()));
    	descriptionTextField.setText(mr.getData().getDataDescription());     	
    }
    
}