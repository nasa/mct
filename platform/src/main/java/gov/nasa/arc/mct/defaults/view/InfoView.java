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
package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.PropertyDescriptor;
import gov.nasa.arc.mct.components.PropertyDescriptor.VisualControlDescriptor;
import gov.nasa.arc.mct.components.PropertyEditor;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.RoleAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.ReloadEvent;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.internal.component.User;
import gov.nasa.arc.mct.util.LookAndFeelSettings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class InfoView extends View {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("DefaultView"); 
    private static final Logger l = LoggerFactory.getLogger(InfoView.class);

    private static final Insets OUTER_MARGINS = new Insets(16, 16, 16, 16);
    private static final Insets PANEL_MARGINS = new Insets(2, 2, 2, 2);
    private static final int DISPLAY_NAME_LENGTH = 20;
    private static final Insets LABEL_MARGINS = new Insets(0, 0, 0, 5);

    private JPanel content;
    private JComponent extendedProperties;
    private JTextField displayName;
    private JLabel displayNameTag;
    private JLabel componentType;
    private JLabel componentTypeTag;
    private JComboBox owner;
    private JLabel ownerTag;
    private JLabel mctId;
    private JLabel mctIdTag;
    private JLabel externalKey;
    private JLabel externalKeyTag;
    
    private JLabel creator;
    private JLabel creatorTag;
    private JLabel creationDate;
    private JLabel creationDateTag;
    
    DateFormat dfm = new SimpleDateFormat("MMM d HH:mm:ss z yyyy");
    String creationDateString = null;
    String lastModifiedDateString = null;

    private String initialDisplayNameText;
    private String initialOwnerText;
    private Map<JComponent,Object> extendedFieldCache = new HashMap<JComponent,Object>();
    private Map<PropertyDescriptor, JComponent> extendedFieldComponents = new HashMap<PropertyDescriptor, JComponent>();
    private Color borderUIColor = UIManager.getColor("border");
    private Color bgUIColor = UIManager.getColor("TextField.background");
    private Color fgUIColor = UIManager.getColor("TextField.foreground");
    private User currentUser = GlobalContext.getGlobalContext().getUser();

    /**
     * For internal use only
     */
    public InfoView() {
        super();
    }
   
    public InfoView(AbstractComponent ac, ViewInfo vi) {
        super(ac,vi);
        String accessibleName = null;
        
        accessibleName = bundle.getString("displayNameField"); 
        displayName = createDisplayNameField();
        displayNameTag = new JLabel(accessibleName + ":");
        displayNameTag.getAccessibleContext().setAccessibleName(accessibleName);
        displayNameTag.setLabelFor(displayName);
        
        accessibleName = bundle.getString("componentTypeField"); 
        componentType = new JLabel(getComponentTypeText());
        componentTypeTag = new JLabel(accessibleName + ":");
        componentTypeTag.getAccessibleContext().setAccessibleName(accessibleName);
        componentTypeTag.setLabelFor(componentType);
        
        accessibleName = bundle.getString("ownerField"); 
        owner =   createOwnerField();
        ownerTag = new JLabel(accessibleName + ":");
        ownerTag.getAccessibleContext().setAccessibleName(accessibleName);
        ownerTag.setLabelFor(owner);
        
        accessibleName = bundle.getString("mctIDField"); 
        mctId = new JLabel(getMasterComponent().getComponentId());
        mctIdTag =  new JLabel(accessibleName + ":");
        mctIdTag.getAccessibleContext().setAccessibleName(accessibleName);
        mctIdTag.setLabelFor(mctId);
        
        accessibleName = bundle.getString("creator"); 
        creator = new JLabel(getMasterComponent().getCreator());
        creatorTag =  new JLabel(accessibleName + ":");
        creatorTag.getAccessibleContext().setAccessibleName(accessibleName);
        creatorTag.setLabelFor(creator);
        
        accessibleName = bundle.getString("creationDate"); 
        if (getMasterComponent().getCreationDate() != null ) {
            dfm.setTimeZone(TimeZone.getDefault());
            creationDateString = dfm.format(getMasterComponent().getCreationDate()); 
        }
        creationDate = new JLabel(creationDateString);
        creationDateTag =  new JLabel(accessibleName + ":");
        creationDateTag.getAccessibleContext().setAccessibleName(accessibleName);
        creationDateTag.setLabelFor(creationDate);

        String externalid = getMasterComponent().getExternalKey();
        if (externalid != null && !externalid.isEmpty()){
            accessibleName = bundle.getString("externalIDField"); 
            externalKey = new JLabel(externalid);
            externalKeyTag = new JLabel(accessibleName + ":");
            externalKeyTag.getAccessibleContext().setAccessibleName(accessibleName);
            externalKeyTag.setLabelFor(externalKey);
        }
        
        if (borderUIColor != null) {
            displayName.setBorder( BorderFactory.createLineBorder(borderUIColor) );
            owner.setBorder(BorderFactory.createLineBorder(borderUIColor));
        }
        
        initialOwnerText = getManifestedComponent().getOwner();
        owner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                String currentSelection = (String) owner.getModel().getSelectedItem();
                if (!initialOwnerText.equals(currentSelection)) {
                    getManifestedComponent().setAndUpdateOwner(currentSelection);
                    getManifestedComponent().save();
                }
            }
        });  

        initialDisplayNameText = getManifestedComponent().getDisplayName();
        displayName.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = displayName.getText().trim();
                if (! currentText.equals(initialDisplayNameText)) {
                    getManifestedComponent().setAndUpdateDisplayName(currentText);
                    getManifestedComponent().save();
                    initialDisplayNameText = currentText;
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
            }
        });

        displayName.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String currentText = displayName.getText().trim();
                if (! currentText.equals(initialDisplayNameText)) {
                    getManifestedComponent().setAndUpdateDisplayName(currentText);
                    getManifestedComponent().save();
                    initialDisplayNameText = currentText;
                }
            }

        });

        setLayout(new BorderLayout());
        content = new JPanel();
        content.setLayout(new GridBagLayout());
        JPanel otherStuff = new JPanel();
        add(content,BorderLayout.NORTH);
        add(otherStuff,BorderLayout.CENTER);
        this.setBorder(new EmptyBorder(OUTER_MARGINS));
        extendedProperties = Box.createVerticalBox();
        addComponents(externalid);
        enterLockedState();
        
    }

    private JTextField createDisplayNameField() {
        final JTextField tf = new JTextField(getManifestedComponent().getDisplayName(), DISPLAY_NAME_LENGTH);
        Document doc = new PlainDocument(); 
        tf.setDocument(doc);
        tf.setText(getManifestedComponent().getDisplayName());
        return tf;
    }
    
    private JComboBox createOwnerField() {
        
        List<String> usersAndRoles = new ArrayList<String>();
        usersAndRoles.addAll(Arrays.asList(RoleAccess.getAllRoles()));
        usersAndRoles.addAll(Arrays.asList(RoleAccess.getAllUsers()));
        final JComboBox comboBox = new JComboBox(usersAndRoles.toArray());
        comboBox.setSelectedItem(getManifestedComponent().getOwner());
        return comboBox;
    }

    private String getComponentTypeText() {
        String fqTypeName = getMasterComponent().getComponentTypeID();
        return fqTypeName.substring(fqTypeName.lastIndexOf(".") + 1);
    }
    
    @Override
    public void updateMonitoredGUI() {
        // When a component's Base Displayed Name is programatically changed, update the text in
        // the one swing component for this name: text field.
        String name = getManifestedComponent().getDisplayName();
        if (displayName != null) {
            displayName.setText(name);
        }
        
        ActionListener[] originalListeners = owner.getActionListeners();
        for (ActionListener listener: originalListeners) {
            owner.removeActionListener(listener);
        }
        owner.setSelectedItem(getManifestedComponent().getOwner());
        for (ActionListener listener: originalListeners) {
            owner.addActionListener(listener);
        }
        
        // any of the model specific properties could have changed so reload all the properties
        addExtendedContent();
        
        // make sure view redraws on screen
        revalidate();
        repaint();
    }
    
    @Override
    public void updateMonitoredGUI(ReloadEvent event) {
       updateMonitoredGUI();
    }
    
    @Override
    public void updateMonitoredGUI(PropertyChangeEvent event) {
        updateMonitoredGUI();
    }
    
    private AbstractComponent getMasterComponent() {
        return getManifestedComponent();
    }

    /*
     * Interface: LockObservable
     * This interface API has reverse semantics from UE's locks.
     * When a component state is changed from private to public, update visibility field for current manifestations.
     * When a component changes locked state, control whether the display name is edit able.
     */

    @Override
    public void enterLockedState() {        
        boolean allowEdit = checkAllowComponentRenamePolicy();
        displayName.setFocusable(allowEdit);
        if (allowEdit) {
            displayName.setEnabled(true);
            displayName.setEditable(true);
        } else {
            displayName.setEnabled(false);
            displayName.setDisabledTextColor(fgUIColor);
        }


        boolean canChangeUser = RoleAccess.canChangeOwner(getManifestedComponent(), currentUser);
        owner.setFocusable(canChangeUser);
        owner.setEnabled(canChangeUser);
        if (!canChangeUser) {
            owner.setRenderer(new DefaultListCellRenderer() {
                public void paint(Graphics g) {
                    setBackground(bgUIColor);
                    setForeground(fgUIColor); 
                    super.paint(g);
                }
            });
        }
        // rebuild the extended content as the component has changed. The property descriptors are bound to a specific
        // component instance, so they will need to be reacquired when a lock changes
        addExtendedContent();
        for (JComponent jComponent : extendedFieldCache.keySet()) {
            jComponent.setFocusable(true);
        }
    }

    @Override
    public void exitLockedState() {
        boolean allowEdit = checkAllowComponentRenamePolicy();
        displayName.setFocusable(allowEdit);
        displayName.setEnabled(allowEdit);
        displayName.setEditable(allowEdit);


        owner.setFocusable(false);
        owner.setEnabled(false);
        owner.setRenderer(new DefaultListCellRenderer() {
            public void paint(Graphics g) {
                setBackground(bgUIColor);
                setForeground(fgUIColor); 
                super.paint(g);
            }
        });
        
        // rebuild the extended content as the component has changed. The property descriptors are bound to a specific
        // component instance, so they will need to be reacquired when a lock changes
        addExtendedContent();
        for (JComponent jComponent : extendedFieldCache.keySet()) {
            jComponent.setFocusable(false);
        }
    }
    

    @Override
    public void processDirtyState() {

        if (displayName instanceof JTextField) {
            String value = displayName.getText().trim();
            displayName.setText(value);
        }
    }

    boolean isDisplayNameFocusable() {
        return displayName.isFocusable();
    }

    private void addComponents(String externalid) {          
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(PANEL_MARGINS));

        JComponent north = Box.createVerticalBox();
        JComponent center = Box.createHorizontalBox();
        JComponent south = Box.createHorizontalBox();
        south.add( Box.createVerticalStrut( 25 ));

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        JComponent BDNbox = Box.createHorizontalBox();
        displayNameTag.setBorder(new EmptyBorder(LABEL_MARGINS));    
        BDNbox.setBorder(new EmptyBorder(PANEL_MARGINS));
        BDNbox.add (displayNameTag);
        BDNbox.add(displayName);
        BDNbox.add(Box.createHorizontalGlue());

        JComponent creationDateBox = Box.createHorizontalBox();
        creationDateTag.setBorder(new EmptyBorder(LABEL_MARGINS));              
        creationDateBox.setBorder(new EmptyBorder(PANEL_MARGINS));
        creationDateBox.add(creationDateTag);
        creationDateBox.add(creationDate);    
        creationDateBox.add(Box.createHorizontalGlue());
        
        JComponent creatorBox = Box.createHorizontalBox();
        creatorTag.setBorder(new EmptyBorder(LABEL_MARGINS));              
        creatorBox.setBorder(new EmptyBorder(PANEL_MARGINS));
        creatorBox.add(creatorTag);
        creatorBox.add(creator);    
        creatorBox.add(Box.createHorizontalGlue());
        
        JComponent ownerBox = Box.createHorizontalBox();
        ownerTag.setBorder(new EmptyBorder(LABEL_MARGINS));
        ownerBox.setBorder(new EmptyBorder(PANEL_MARGINS));
        ownerBox.add(ownerTag);
        ownerBox.add(owner);
        ownerBox.add(Box.createHorizontalGlue());
        
        JComponent typeBox = Box.createHorizontalBox();
        componentTypeTag.setBorder(new EmptyBorder(LABEL_MARGINS));             
        typeBox.setBorder(new EmptyBorder(PANEL_MARGINS));
        typeBox.add(componentTypeTag);
        typeBox.add(componentType);       
        typeBox.add(Box.createHorizontalGlue());

        JComponent idBox = Box.createHorizontalBox();
        mctIdTag.setBorder(new EmptyBorder(LABEL_MARGINS));           
        idBox.setBorder(new EmptyBorder(PANEL_MARGINS));
        idBox.add(mctIdTag);
        idBox.add(mctId);       
        idBox.add(Box.createHorizontalGlue());

        JComponent externalKeyBox = null;
        if (externalid != null && !externalid.isEmpty()){
            externalKeyBox = Box.createHorizontalBox();
            externalKeyTag.setBorder(new EmptyBorder(LABEL_MARGINS));           
            externalKeyBox.setBorder(new EmptyBorder(PANEL_MARGINS));
            externalKeyBox.add(externalKeyTag);
            externalKeyBox.add(externalKey);       
            externalKeyBox.add(Box.createHorizontalGlue());
        }
        
        north.add(BDNbox);
        north.add(ownerBox); // TODO editable
        north.add(creatorBox); 
        north.add(creationDateBox);
        north.add(typeBox);
        north.add(idBox);
        if (externalid != null && !externalid.isEmpty()){ 
            north.add(externalKeyBox);
        }

        north.add(extendedProperties);
        center.add( Box.createHorizontalStrut(25));
    }

    private void addExtendedContent() {
        extendedProperties.removeAll();
        extendedFieldCache.clear();
        extendedFieldComponents.clear();
        List<PropertyDescriptor> fields = getManifestedComponent().getFieldDescriptors();
        if (fields == null) {
            return;
        }
        for (PropertyDescriptor p : fields) {
            assert p.getPropertyEditor() != null : "The Property Editor may not be null.";
            
            final PropertyDescriptor pd = p;
            try {
                JComponent extendedJcomponent = LookAndFeelSettings.getColorProperties().getColorSchemeFor(getInfo().getViewClass().getSimpleName()).callUnderColorScheme( new Callable<JComponent>() {
                    public JComponent call() throws Exception {
                        return makeVisualComponent(pd);                
                    };
                });  
                
                final String labelText = p.getShortDescription() + ":";
                
                JLabel extendedLabel = LookAndFeelSettings.getColorProperties().getColorSchemeFor(getInfo().getViewClass().getSimpleName()).callUnderColorScheme( new Callable<JLabel>() {
                    public JLabel call() throws Exception {
                        return new JLabel(labelText);                
                    };
                });  
                extendedLabel.getAccessibleContext().setAccessibleName(p.getShortDescription()); 
                extendedLabel.setLabelFor(extendedJcomponent);
                extendedLabel.setBorder(new EmptyBorder(LABEL_MARGINS));  
                JComponent box = Box.createHorizontalBox();
                box.setBorder(new EmptyBorder(PANEL_MARGINS));
                box.add(extendedLabel);
                box.add(extendedJcomponent);       
                box.add(Box.createHorizontalGlue());
                extendedProperties.add(box);
            } catch (Exception e) {
                l.error("error when creating property descriptor " + p, e);
                continue;
            }

        }
        extendedProperties.invalidate();
    }

    /**
     * Refresh extended fields. Called after making changes to a component, in case 
     * there are interdependencies between fields (sometimes changing one field 
     * may also indirectly change another, depending on component implementation.)
     */
    private void refreshExtendedFields() {
        for (Entry<PropertyDescriptor, JComponent> visualComponent : this.extendedFieldComponents.entrySet()) {
            populateVisualComponent(visualComponent.getValue(), visualComponent.getKey());
        }
    }
    
    private void populateVisualComponent(JComponent component, PropertyDescriptor p) {
        Object newValue = null;
        // Set the visual component (specific method calls vary by component type)
        // Note that the methods used here should not trigger action listeners on the component!
        switch (p.getVisualControlDescriptor()) {
        case Label: {
            ((JLabel)component).setText((String) (newValue = p.getPropertyEditor().getAsText()));
            break;
        }
        case TextField: {
            ((JTextField)component).setText((String) (newValue = p.getPropertyEditor().getAsText()));
            break;
        }
        case CheckBox: {
            ((JCheckBox)component).setSelected((Boolean) (newValue = p.getPropertyEditor().getValue()));
            break;
        }
        case ComboBox: {
            ((JComboBox)component).getModel().setSelectedItem(newValue = p.getPropertyEditor().getValue());
            break;
        }
        case TextArea: {
            ((JTextArea)component).setText((String) (newValue = p.getPropertyEditor().getAsText()));
            break;
        }
        }
        // Cache the new value (will be used if validation fails)
        extendedFieldCache.put(component, newValue);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private JComponent makeVisualComponent(PropertyDescriptor p) {
        JComponent jComponent = null;
        VisualControlDescriptor visualControlDescriptorType = p.getVisualControlDescriptor();
        boolean isPrivateAndMutable = p.isFieldMutable();

        PropertyEditor<Object> ed = (PropertyEditor<Object>) p.getPropertyEditor();
        
        switch (visualControlDescriptorType) {
        case Label: {
            String valueText = ed.getAsText();
            jComponent = new JLabel(valueText);
            jComponent.setEnabled(true);
            jComponent.setFocusable(false); 
            break;
        }
        case TextField: {
            String valueText = ed.getAsText();
            jComponent = new JTextField(valueText, DISPLAY_NAME_LENGTH);
            if (p.isFieldMutable()) {
                hookupComponentListeners((JTextField)jComponent, ed);
            } else {
                ((JTextField)jComponent).setEditable(false); 
            }
            extendedFieldCache.put(jComponent, valueText);
            jComponent.setFocusable(isPrivateAndMutable); 
            if (borderUIColor != null) {
                jComponent.setBorder( BorderFactory.createLineBorder(borderUIColor) );
            }
            break;
        }
        case CheckBox: {
            Boolean isSelected = (Boolean) ed.getValue();
            jComponent = new JCheckBox();
            ((JCheckBox)jComponent).setSelected(isSelected); 

            if (isPrivateAndMutable) {
                hookupComponentListeners((JCheckBox)jComponent, ed);
            } else {
                jComponent.setEnabled(false);
                jComponent.setFocusable(false); 
            }
            if (borderUIColor != null) {
                jComponent.setBorder( BorderFactory.createLineBorder(borderUIColor) );
            }
            break;
        }
        case ComboBox: {
            List <Object> comboItems = ed.getTags();
            jComponent = new JComboBox(comboItems.toArray(new String[comboItems.size()]));
            JComboBox combo = (JComboBox)jComponent;

            combo.setSelectedItem(ed.getValue());
            combo.setEditable(false); 

            if (isPrivateAndMutable) {
                hookupComponentListeners((JComboBox)jComponent, ed);
                jComponent.setFocusable(true); 
                jComponent.setEnabled(true);
            } else {
                jComponent.setEnabled(false);
                jComponent.setFocusable(false); 
              
                combo.setRenderer(new DefaultListCellRenderer() {
                    public void paint(Graphics g) {
                        setBackground(bgUIColor);
                        setForeground(fgUIColor); 
                        super.paint(g);
                    }
                });
            }
           
            if (borderUIColor != null) {
                jComponent.setBorder( BorderFactory.createLineBorder(borderUIColor) );
            }
            for (int i = 0; i < combo.getComponentCount(); i++)  {
                if (combo.getComponent(i) instanceof AbstractButton) {
                    AbstractButton b = (AbstractButton)combo.getComponent(i);
                    b.setBorder(new EmptyBorder(0, 0,0,0)); //match look of text fields
                }
            }
            break;
        }
        case TextArea: {
            JTextArea textArea = new JTextArea(ed.getAsText());
            jComponent = textArea;
            
            textArea.setRows(6);
            textArea.setColumns(40);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.getViewport().add(textArea);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);           
            
            if (borderUIColor != null) {
                scrollPane.setBorder(BorderFactory.createLineBorder(borderUIColor));
                scrollPane.getVerticalScrollBar().setUI(new FlatScrollBarUI());
            }
            
            if (isPrivateAndMutable) {
                hookupComponentListeners(textArea, ed);
                jComponent.setEnabled(true);
                jComponent.setFocusable(true); 
            } else {
                jComponent.setEnabled(false);
                jComponent.setFocusable(false); 
            }
            
            extendedFieldComponents.put(p, textArea);     
            
            jComponent = scrollPane;
            break;
        }
        }
        
        // Some VisualControlDescriptors may have added this already
        // (for instance, if the real control is embedded with jComponent)
        if (!extendedFieldComponents.containsKey(p)) {
            extendedFieldComponents.put(p, jComponent);
        }
        
        return jComponent;
    }

    private void hookupComponentListeners(final JTextField jComponent, final PropertyEditor<?> ed) {
        jComponent.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                saveTextField(jComponent, ed);
            }
            
            @Override
            public void focusGained(FocusEvent e) {
            }
        });
        jComponent.addActionListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
                saveTextField(jComponent,ed);
            } 
        });
    }

    private void hookupComponentListeners(final JCheckBox jComponent, final PropertyEditor<?> ed) {
        jComponent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                JComponent key = jComponent;
                Boolean prev = (Boolean) extendedFieldCache.get(key); 
                Boolean currentSelection = jComponent.isSelected();
                if (currentSelection != prev) {
                    try {
                        ed.setValue(currentSelection); 
                        extendedFieldCache.put(key, currentSelection);
                        getManifestedComponent().save();
                        refreshExtendedFields();
                    } catch (IllegalArgumentException e1) {
                        jComponent.setSelected(prev);
                        ed.setValue(prev);
                        extendedFieldCache.put(key, prev);
                    }
                }
            }
        });  
    }

    private void hookupComponentListeners(final JComboBox jComponent, final PropertyEditor<?> ed) {
        
        jComponent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                JComponent key = jComponent;
                Object prev = extendedFieldCache.get(key); 
                Object currentSelection = jComponent.getModel().getSelectedItem();
                if (currentSelection != prev) {
                    try {
                        ed.setValue(currentSelection); 
                        extendedFieldCache.put(key, currentSelection);
                        getManifestedComponent().save();
                        refreshExtendedFields();

                    } catch (IllegalArgumentException e1) {
                        jComponent.setSelectedItem(prev);
                        ed.setValue(prev);
                        extendedFieldCache.put(key, prev);
                    }
                }
            }
        });        
    }
    
    private void hookupComponentListeners(final JTextArea jComponent, final PropertyEditor<?> ed) {
        jComponent.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                saveTextField(jComponent, ed);
            }
            
            @Override
            public void focusGained(FocusEvent e) {
            }
        });
    }

    private void saveTextField(final JTextComponent jComponent, final PropertyEditor<?> ed) {
        String prev = (String) extendedFieldCache.get(jComponent); 
        String currentText = jComponent.getText().trim();

        if (!currentText.equals(prev)) {
            try {
                ed.setAsText(currentText); 
                extendedFieldCache.put(jComponent, (Object)currentText); 
                getManifestedComponent().save();
                refreshExtendedFields();
            } catch (IllegalArgumentException e1) {
                jComponent.setText(prev);
                ed.setAsText(prev);
                extendedFieldCache.put(jComponent, (Object)prev);
            }
        }
    }
    
    protected boolean checkAllowComponentRenamePolicy() {
        boolean rv;
        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), getManifestedComponent());
        context.setProperty("NAME", getManifestedComponent().getDisplayName());
        context.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String renamingkey = PolicyInfo.CategoryType.ALLOW_COMPONENT_RENAME_POLICY_CATEGORY.getKey();
        rv =  PolicyManagerImpl.getInstance().execute(renamingkey, context).getStatus();
        return rv;
    }
    


private class FlatScrollBarUI extends BasicScrollBarUI {
    @Override
    protected void paintDecreaseHighlight(Graphics g) {}

    @Override
    protected void paintIncreaseHighlight(Graphics g) {}

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(borderUIColor != null ? borderUIColor : fgUIColor);
        g2.fill(thumbBounds);
        g2.setColor(bgUIColor);
        int x = (thumbBounds.x)+ thumbBounds.width / 2;
        int y = (thumbBounds.y) + thumbBounds.height / 2;
        int w = thumbBounds.width / 4;
        g2.drawLine(x-w, y-2, x+w, y-2);
        g2.drawLine(x-w, y+0, x+w, y+0);
        g2.drawLine(x-w, y+2, x+w, y+2);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(bgUIColor);
        g2.fill(trackBounds);
        g2.setColor(borderUIColor != null ? borderUIColor : fgUIColor);
        g2.draw(trackBounds);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        Color fgColor = borderUIColor != null ? borderUIColor : fgUIColor;
        JButton b = new BasicArrowButton(orientation, bgUIColor, bgUIColor, fgColor, fgColor);
        b.setBorder(BorderFactory.createLineBorder(fgColor, 1));
        return b;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        Color fgColor = borderUIColor != null ? borderUIColor : fgUIColor;
        JButton b = new BasicArrowButton(orientation, bgUIColor, bgUIColor, fgColor, fgColor);
        b.setBorder(BorderFactory.createLineBorder(fgColor, 1));
        return b;
    }
}
}