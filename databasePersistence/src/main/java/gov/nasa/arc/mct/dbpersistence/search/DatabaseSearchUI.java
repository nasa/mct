/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
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
package gov.nasa.arc.mct.dbpersistence.search;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.dbpersistence.access.InternalDBPersistenceAccess;
import gov.nasa.arc.mct.dbpersistence.dao.ComponentSpec;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class DatabaseSearchUI extends JPanel implements SelectionProvider {

//    private final static ResourceBundle bundle = ResourceBundle.getBundle("Platform"); //NOI18N
    private static final int PADDING = 5;
    private JTextField baseDisplayedNameField;
    private DefaultListModel listModel = new DefaultListModel();
    private JList list;
    private JButton goButton;
    private JLabel resultStatus;
    private JCheckBox findObjectsCreatedByMe;

    public DatabaseSearchUI() {
                
        setLayout(new BorderLayout());
        
        JPanel displayNamePanel = new JPanel(new BorderLayout());
        JLabel displayNameLabel = new JLabel("Base Displayed Name:");
        baseDisplayedNameField = new JTextField();
        baseDisplayedNameField.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (goButton.isEnabled())
                    goButton.doClick();
            }
        });
        displayNameLabel.setLabelFor(baseDisplayedNameField);
        displayNamePanel.add(baseDisplayedNameField, BorderLayout.CENTER);
        displayNamePanel.add(displayNameLabel, BorderLayout.WEST);
        
        goButton = new JButton();
        String searchButtonText = "Search";//bundle.getString("SEARCH_BUTTON");
        goButton.getAccessibleContext().setAccessibleName(searchButtonText);
        goButton.setAction(new AbstractAction(searchButtonText) {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                goButton.setEnabled(false);
                SearchTask task = new SearchTask();
                listModel.removeAllElements();
                resultStatus.setText(null);
                task.execute();

            }
        });
        JPanel displayNameAndGoPanel = new JPanel(new BorderLayout());
        displayNameAndGoPanel.add(displayNamePanel, BorderLayout.CENTER);
        displayNameAndGoPanel.add(goButton, BorderLayout.EAST);
        
        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.getAccessibleContext().setAccessibleName("Search Result List");
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                ComponentInfo ci = (ComponentInfo) value;
                JLabel label = (JLabel) super.getListCellRendererComponent(list, ci.name, index, isSelected, cellHasFocus);
                label.setIcon(getIconForComponentType(ci.type));
                return label;
            } 
        });
        
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                  Collection<View> selectedManifestations = getSelectedManifestations();
                  if (!selectedManifestations.isEmpty()) {
                    firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, selectedManifestations);
                  } // end if
                } // end if
            }
        });
        
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Collection<View> selectedManifestations = getSelectedManifestations();
                if (selectedManifestations.isEmpty())
                    return;
                
                View manifestation = selectedManifestations.iterator().next();
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    manifestation.getManifestedComponent().open();
                    return;
                }
            }
        });
        list.setDragEnabled(true);
        list.setTransferHandler(new TransferHandler() {
            @Override
            protected Transferable createTransferable(JComponent c) {
                List<View> viewRoles = new ArrayList<View>();
                Collection<View> selectedManifestations = getSelectedManifestations();
                if (!selectedManifestations.isEmpty()) {
                  for (View manifestation : getSelectedManifestations())
                      viewRoles.add(manifestation);
                  return new ViewRoleSelection(viewRoles.toArray(new View[viewRoles.size()]));
                } else {
                  return null;
                } // end if
            }
            
            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }
            
        });
        
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        controlPanel.add(displayNameAndGoPanel);
        findObjectsCreatedByMe = new JCheckBox("Created By Me");
        controlPanel.add(findObjectsCreatedByMe);
        
        JPanel descriptionPanel = new JPanel(new GridLayout(2, 1));
        JLabel searchEverywhereLabel = new JLabel("Search everywhere");
        searchEverywhereLabel.setFont(searchEverywhereLabel.getFont().deriveFont(Font.BOLD));
        
        resultStatus = new JLabel();
        resultStatus.setForeground(Color.BLUE);
        
        descriptionPanel.add(searchEverywhereLabel);
        descriptionPanel.add(resultStatus);
        
        JPanel upperPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        upperPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        upperPanel.add(descriptionPanel, BorderLayout.NORTH);
        upperPanel.add(controlPanel, BorderLayout.CENTER);
                
        add(upperPanel, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }
    
    QueryResult search(String pattern, boolean isFindObjectsCreatedByMe) {
        Properties props = new Properties();
        if (isFindObjectsCreatedByMe)
            props.setProperty("creator", PlatformAccess.getPlatform().getCurrentUser().getUserId());
        return InternalDBPersistenceAccess.getService().findComponentsByBaseDisplayedNamePattern(pattern, props); 
    }
    
    private ImageIcon getIconForComponentType(String type) {
    	AbstractComponent iconInstance = PlatformAccess.getPlatform().getComponentRegistry().newInstance(type);
    	return iconInstance == null ? null : iconInstance.getAsset(ImageIcon.class);
    }
    
    private class SearchTask extends SwingWorker<List<ComponentInfo>, Void> {
        private AtomicInteger total = new AtomicInteger();
                        
        public SearchTask() {            
        }

        @Override
        protected List<ComponentInfo> doInBackground() throws Exception {            
            String displayNamePattern = baseDisplayedNameField.getText().trim();
            List<ComponentInfo> list = new ArrayList<DatabaseSearchUI.ComponentInfo>();
            QueryResult queryResult = search(displayNamePattern, findObjectsCreatedByMe.isSelected());
            @SuppressWarnings("unchecked")
            List<ComponentSpec> daoObjects = (List<ComponentSpec>) queryResult.getRecords();
            for (ComponentSpec cs : daoObjects) {
                ComponentInfo ci = new ComponentInfo(cs.getComponentId(), cs.getComponentName(), cs.getComponentType());
                list.add(ci);
            }
            total.set(queryResult.getCount());
            return list;
        }
        
        @Override
        public void done() {
            try {
                for (ComponentInfo ci : get()) {
                    listModel.addElement(ci);
                }
                resultStatus.setText("Search Results: " + listModel.size() + " out of " + total.get());
            } catch (InterruptedException e) {
                listModel.removeAllElements();
            } catch (ExecutionException e) {
                listModel.removeAllElements();
            } finally {            
                goButton.setEnabled(true);
            }
        }
    }
    
    @Override
    public String getName() {
        return "Search Name";
    }

    @Override
    public void addSelectionChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP, listener);
    }

    @Override
    public void removeSelectionChangeListener(PropertyChangeListener listener) {
        removePropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP, listener);        
    }

    //Harleigh108: this removes the warning received when we build with respect to java 7; getSelectedValues is now Deprecated in java7
    //from java7 javadocs (for JList) 'As of JDK 1.7, replaced by getSelectedValuesList()'
    //Note: we suppress this warning to stay compatible with java6
    @SuppressWarnings("deprecation")
    @Override
    public Collection<View> getSelectedManifestations() {
        Object[] selectedValues = list.getSelectedValues();
        if (selectedValues == null || selectedValues.length == 0)
            return Collections.emptySet();
        
        Collection<View> manifestations = new ArrayList<View>();
        for (Object value : selectedValues) {
            ComponentInfo ci = (ComponentInfo) value;
            AbstractComponent component = AbstractComponent.getComponentById(ci.id);
            // JIRA 3809 - rvanhoof - Fix NPE when component was deleted from another view
            if (component != null) {
              manifestations.add(component.getViewInfos(ViewType.NODE).iterator().next().createView(component));
            } // end if
        }
        return manifestations;
    }

    @Override
    public void clearCurrentSelections() {
        list.clearSelection();
        
    }
    
    private static class ComponentInfo {
        public ComponentInfo(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
        private String id;
        private String name;
        private String type;
    }

}
