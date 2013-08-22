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
package gov.nasa.arc.mct.gui.dialogs;


import gov.nasa.arc.mct.services.component.CreateWizardUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;



/**
 * Show the dialog, extended from {@link JDialog}, for creating 
 * an object from the Objects->New menu.
 * @author nija.shi@nasa.gov
 */
@SuppressWarnings("serial")
public class NewObjectDialog extends JDialog {
    
    private static final ResourceBundle bundle;
    private static final int PADDING = 12;
    
    private final JButton create = new JButton();
    private final CreateWizardUI wizard;
    private boolean confirm = false;

    static{
        bundle = ResourceBundle.getBundle("NewObjectResource");
    }
    
    
    /**
     * The constructor that creates the dialog.
     * @param frame the owner of the {@link Frame} of which this
     * dialog is displayed.
     */
    public NewObjectDialog(JFrame frame, String componentTypeName, CreateWizardUI wiz) {
        super(frame, ModalityType.DOCUMENT_MODAL);

        this.wizard = wiz;
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(MessageFormat.format(bundle.getString( "DIALOG_TITLE"), componentTypeName, frame.getTitle()));
        
        JPanel controlPanel = new JPanel();
        create.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
  
                NewObjectDialog.this.setVisible(false);
                NewObjectDialog.this.dispose();
                confirm = true;
                
            }

        });
        create.setText(bundle.getString("CREATE_BUTTON")); //NOI18N
        JButton cancel = new JButton(bundle.getString("CANCEL_BUTTON")); //NOI18N
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NewObjectDialog.this.dispose();
            }

        });
        controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(create);
        controlPanel.add(cancel);
        
        // Allocate most available space to wizard's UI, with create/cancel on the bottom
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createEmptyBorder(PADDING,PADDING,PADDING-1,PADDING-1));
        add(wizard.getUI(create), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH); 

        // Set Create button as default to respond to enter key
        this.getRootPane().setDefaultButton(create);

        // Instrument the buttons
        create.setName("createButton");
        cancel.setName("cancelButton");

        // Pack window to fit contents and position
        pack();
        setLocationRelativeTo(frame);
        
        // Make sure there is room for the title
        int estimatedTitleWidth = getTitle().length() * 10;
        if (getSize().getWidth() < estimatedTitleWidth) {
            setSize(new Dimension(estimatedTitleWidth, (int) getSize().getHeight()));
        } 
    }
    
    public boolean getConfirm(){
        return confirm;
    }
  
}
