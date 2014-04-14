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
package gov.nasa.arc.mct.importExport.provider;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.FileChooser;
import gov.nasa.arc.mct.gui.FrameUtil;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.RoleAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.internal.component.User;
import gov.nasa.jsc.mct.importExport.utilities.CustomFileChooser;
import gov.nasa.jsc.mct.importExport.utilities.Utilities;
import gov.nasa.jsc.mct.importExport.utilities.XMLFileFilter;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JProgressBar;


/**
 * The <code>ImportAction</code> allows user to select an XML file or a directory 
 * containing XML files containing MCT data to be imported into MCT.  
 */

public class ImportThisAction extends ContextAwareAction implements PropertyChangeListener {

    private static final long serialVersionUID = 5839600208723859922L;
    private static ResourceBundle bundle = 
    		                           ResourceBundle.getBundle("ImportExportProvider"); 
    private ActionContext currentContext;
    private DialogMgr dialogMgr = new DialogMgr(null);
    private JProgressBar bar;
    private JDialog jd;
    private String owner;

    public ImportThisAction() {
       super(bundle.getString("import_text"));
    }

    /**
     * Presents a FileChooser to user, then parses all .xml files returned 
     * into components. Displays dialog to user if no XML files returned.
     * 
     * @param e the {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	List<File> files = selectFiles(currentContext.getWindowManifestation());

    	if (files != null) {
    		if (files.isEmpty()) {
    			dialogMgr.showMessageDialog("No XML files found in specified directory");
    		}
    		else {
    			View manifestation = currentContext.getWindowManifestation();
    			assert manifestation != null;
    			AbstractComponent selectedComponent = 
    					               manifestation.getManifestedComponent();
    			buildProgressBar();   
    			Importer importReaderTask = new Importer(files, owner, selectedComponent, 
    					                                 bar, jd);
    			// ImportAction on EDT listens to Task's bound property progress
    			importReaderTask.addPropertyChangeListener(this);
    			importReaderTask.execute();
    		}
    	}
    }   

    private void buildProgressBar() {
    	AbstractComponent targetComponent = currentContext.getWindowManifestation()
                .getManifestedComponent();
    	Window activeWindow = 
    			  FrameUtil.getFrameForComponent(currentContext.getWindowManifestation());
		jd = new JDialog();
		jd.setModal(false);
		jd.setTitle(bundle.getString("import_message") + 
				" in Progress - " + targetComponent.getDisplayName());
		jd.setLocationRelativeTo(activeWindow);
		
		bar = new JProgressBar(0, 100);
		bar.setIndeterminate(false);
		bar.setStringPainted(true);
        bar.setBorder(BorderFactory.createEmptyBorder(5, 100, 5, 100));
        
		jd.getContentPane().add(bar);
		jd.pack();
		jd.setVisible(true);  
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {      
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            bar.setValue(progress);
        }
    }
    
	/**
     * Opens FileChooser with the passed in component as parent and returns a collection 
     * of File objects containing XML files specified by the user through the FileChooser.
     * User can select a single XML file or a directory. Returns any XML files contained
     * in the selected directory.
     *  
     * @param source parent component into which the FileChooser is to be opened
     * @return List of File objects returned by the FileChooser, which may be empty if 
     * directory contained no XML files,
     * or null if user canceled out of the FileChooser
     */
    public List<File> selectFiles(Component source){
    	AbstractComponent targetComponent = currentContext.getWindowManifestation()
    			.getManifestedComponent();
    	//Initialize the file chooser with the current user
    	User user = PlatformAccess.getPlatform().getCurrentUser();
        final CustomFileChooser fileChooser = new CustomFileChooser();
        
        //add the list of owners and set selection to current owner
        fileChooser.addOwners(Arrays.asList(RoleAccess.getAllUsers()));
        fileChooser.setOwner(user.getUserId());
       
        fileChooser.setDialogTitle(bundle.getString("import_message")
				+ targetComponent.getDisplayName());
        fileChooser.setApproveButtonText(bundle.getString("import_button"));
        
        fileChooser.setFileSelectionMode(FileChooser.FILES_AND_DIRECTORIES);

        fileChooser.setFileFilter(new XMLFileFilter());
        fileChooser.setMultiSelectionEnabled(true);
        
        if (source == null) return null;
        int returnVal = fileChooser.showOpenDialog(source);

        if (returnVal == FileChooser.APPROVE_OPTION) {
            File[] rootFileOrDir = fileChooser.getSelectedFiles();
            List<File> files = Utilities.filterSelectedFiles(Arrays.asList(rootFileOrDir));     
            //set the owner
            owner = fileChooser.getOwner();
            return files;
        } 
        return null;
    }
    
    /**
     * Given the {@link ActionContext}, this method determines if we have a single 
     * selection, which determines the availability (i.e., either shown or hidden)
     * of this action when appeared as a menu item.
     * 
     * @param context the {@link ActionContext}
     * @return a boolean that indicates the availability of the action
     */
    @Override
    public boolean canHandle(ActionContext context) {
        currentContext = context;       
        return currentContext.getWindowManifestation() != null;
    }

    /**
     * Controls whether menu item is gray or not, based on policy.
     * 
     * @return boolean indicating if menu item is enabled     
     */
    @Override
    public boolean isEnabled() {
        AbstractComponent targetComponent = currentContext.getWindowManifestation()
        		.getManifestedComponent();
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(),
        		                  targetComponent);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY
        		                .getKey();
        return PlatformAccess.getPlatform().getPolicyManager().execute(compositionKey, 
              policyContext).getStatus();
    }
    
    /**
     * Unit test enabler
     * @param dialogMgr
     */
    void setDialogMgr(DialogMgr dialogMgr) {
		this.dialogMgr = dialogMgr;
	}
}
