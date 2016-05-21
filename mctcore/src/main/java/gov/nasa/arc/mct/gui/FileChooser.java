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
package gov.nasa.arc.mct.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Dialog.ModalityType;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Implements a file chooser for which the user can control the modality,
 * application-modal or document-modal. The default modality is document-modal,
 * rather than application-modal, as for {@link javax.swing.JFileChooser}.
 */
public class FileChooser {
    
    // A couple flags copied from JFileChooser
    
    /** Return value if approve (yes, ok) is chosen. */
    public final static int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
    
    /**  Instruction to display only files. */
    public final static int FILES_ONLY = JFileChooser.FILES_ONLY;

    /** Instruction to display only files. */
    public final static int FILES_AND_DIRECTORIES = JFileChooser.FILES_AND_DIRECTORIES;
        
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;
    
    private String dialogTitle;

    private ModalityType modalityType = ModalityType.DOCUMENT_MODAL;
    
    private JFileChooser chooser;
    
    private int returnValue;
    
    private JDialog dialog;
    
    /**
     * Creates a file chooser that can be shown as a dialog.
     */
    public FileChooser() {
        chooser = new Chooser();
    }
    
    /**
     * Pops up an "Open File" file chooser dialog. Note that the
     * text that appears in the approve button is determined by
     * the Look and Feel.
     *
     * @param    parent  the parent component of the dialog,
     *          can be <code>null</code>;
     *                  see <code>showDialog</code> for details
     * @return   the return state of the file chooser
     */
    public int showOpenDialog(Component parent) {
        return showDialog(parent, null);
    }

    /**
     * Pops a custom file chooser dialog with a custom approve button.
     *
     * @param   parent  the parent component of the dialog;
     *          can be <code>null</code>
     * @param   approveButtonText the text of the <code>ApproveButton</code>
     * @return  the return state of the file chooser
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public int showDialog(final Component parent, final String approveButtonText) throws HeadlessException {
        returnValue = JFileChooser.CANCEL_OPTION;
        Frame parentFrame = (parent != null ? JOptionPane.getFrameForComponent(parent) : null);
        
        dialog = new JDialog(parentFrame, dialogTitle, modalityType);
        dialog.setLayout(new BorderLayout());
        dialog.add(chooser, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
        dialog.dispose();
        dialog = null;
        
        return returnValue;
    }
    
    /**
     * Sets the modality type for the file chooser dialog that will be shown.
     * 
     * @param modalityType the new modality type
     */
    public void setModalityType(ModalityType modalityType) {
        this.modalityType = modalityType;
    }

    /**
     * Sets the title of the dialog that will be shown.
     *  
     * @param newTitle the new title
     */
    public void setDialogTitle(String newTitle) {
        dialogTitle = newTitle;
    }
    
    /**
     * Sets the initial directory for the file chooser that will be shown.
     *  
     * @param aDirectory to show
     */
    public void setInitialDirectory(File aDirectory) {
        chooser.setCurrentDirectory(aDirectory);
    }

    /**
     * Sets the text of the approval button. Default is "open"
     * for an open dialog box.
     * 
     * @param newText the new approve button text
     */
    public void setApproveButtonText(String newText) {
        chooser.setApproveButtonText(newText);
    }
    
    /**
     * Gets the file that was selected by the user, or null,
     * if no file was selected.
     * 
     * @return the selected file
     */
    public File getSelectedFile() {
        return chooser.getSelectedFile();
    }

    /**
     * Sets the filter controlling which files we be available
     * for selection.
     * 
     * @param filter the file filter
     */
    public void setFileFilter(FileFilter filter) {
        chooser.setFileFilter(filter);
    }

    /**
     * Sets the file selection mode. See {@link javax.swing.JFileChooser#setFileSelectionMode(int)}.
     * 
     * @param mode the new file selection mode
     */
    public void setFileSelectionMode(int mode) {
        chooser.setFileSelectionMode(mode);
    }

    /**
     * Sets whether multiselection is allowed.
     * 
     * @param b true, if multiple files may be selected
     */
    public void setMultiSelectionEnabled(boolean b) {
        chooser.setMultiSelectionEnabled(b);
    }
    
    private void setReturnValue(int returnValue) {
        this.returnValue = returnValue;
    }
    
    private void hideDialog() {
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }
    
    private class Chooser extends JFileChooser {

        private static final long serialVersionUID = 1L;

        @Override
        public void approveSelection() {
            super.approveSelection();
            setReturnValue(JFileChooser.APPROVE_OPTION);
            hideDialog();
        }

        @Override
        public void cancelSelection() {
            super.cancelSelection();
            setReturnValue(JFileChooser.CANCEL_OPTION);
            hideDialog();
        }
        
    }

}
