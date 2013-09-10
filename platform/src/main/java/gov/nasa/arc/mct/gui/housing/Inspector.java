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
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.defaults.view.SwitcherView;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareButton;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.SettingsButton;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.gui.actions.RefreshAction;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.LafColor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@SuppressWarnings("serial")
public class Inspector extends View {
    public static final String DEFAULT_INSPECTOR_VIEW_PROP_KEY = "DefaultInspector";

    private static final Color BACKGROUND_COLOR = LafColor.WINDOW_BORDER.darker();
    private static final Color FOREGROUND_COLOR = LafColor.WINDOW.brighter();

    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    Inspector.class.getName().substring(0, 
                            Inspector.class.getName().lastIndexOf("."))+".Bundle");
   
    private static final String INFO_VIEW_TYPE = "gov.nasa.arc.mct.defaults.view.InfoView";
    private static String preferredViewType = INFO_VIEW_TYPE;

    private final PropertyChangeListener selectionChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (view != null && !view.getManifestedComponent().isStale() && view.getManifestedComponent().isDirty()) {
                commitOrAbortPendingChanges();
            }
            @SuppressWarnings("unchecked")
            Collection<View> selectedViews =  (Collection<View>) evt.getNewValue();
            if (selectedViews.isEmpty() || selectedViews.size() > 1) {
                selectedManifestationChanged(null);
            } else {
                // Retrieve component from database.
                AbstractComponent ac = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(selectedViews.iterator().next().getManifestedComponent().getComponentId());
                // Selection changed is fired when a tree node is removed. 
                if (ac != null) {
                    Set<ViewInfo> viewInfos = ac.getViewInfos(ViewType.OBJECT);
                    ViewInfo preferredViewInfo = null, infoViewInfo = null;
                    for (ViewInfo vi : viewInfos) {
                        if (preferredViewType.equals(vi.getType()))
                            preferredViewInfo = vi;
                        if (INFO_VIEW_TYPE.equals(vi.getType()))
                            infoViewInfo = vi;
                    }
                    if (preferredViewInfo == null && infoViewInfo == null)
                        selectedManifestationChanged(viewInfos.iterator().next().createView(ac));
                    else
                        selectedManifestationChanged(preferredViewInfo != null ? preferredViewInfo.createView(ac) : infoViewInfo.createView(ac));
                }
            }
        }
    };
    
    private final PropertyChangeListener objectStaleListener = new PropertyChangeListener() {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            STALE_LABEL.setVisible((Boolean)evt.getNewValue());            
        }
    };

    private final JLabel STALE_LABEL = new JLabel(BUNDLE.getString("view.modified.status.bar.text"));
    private JLabel viewTitle = new JLabel();
    private JLabel space = new JLabel(" ");
    private JPanel emptyPanel = new JPanel();
    private JComponent content;
    private View view;
    private JComponent viewControls;
    private JPanel titlebar = new JPanel();
    private JPanel statusbar = new JPanel();
    private GridBagConstraints c = new GridBagConstraints();
    private JToggleButton controlAreaToggle = new SettingsButton();
    private ContextAwareButton refreshButton = new ContextAwareButton(new RefreshAction());
    
    public Inspector(AbstractComponent ac, ViewInfo vi) {    
        super(ac,vi);
        STALE_LABEL.setToolTipText(BUNDLE.getString("view.modified.status.bar.tooltip.text"));
        registerSelectionChange();        
        setLayout(new BorderLayout());
        
        controlAreaToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOrHideController(controlAreaToggle.isSelected());
            }            
        });
                
        titlebar.setLayout(new GridBagLayout());
        JLabel titleLabel = new JLabel("Inspector:  ");
        
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0;
        c.gridwidth = 1;
        titlebar.add(titleLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        titlebar.add(viewTitle, c);
        
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        titlebar.add(space);
        
        titleLabel.setForeground(FOREGROUND_COLOR);
        viewTitle.setForeground(FOREGROUND_COLOR);
        viewTitle.addMouseMotionListener(new WidgetDragger());
        viewTitle.addMouseListener(new MCTPopupOpenerForInspector(this));
        titlebar.setBackground(BACKGROUND_COLOR);
        statusbar.setBackground(BACKGROUND_COLOR);
        statusbar.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        
        add(titlebar, BorderLayout.NORTH);
        add(emptyPanel, BorderLayout.CENTER);
        add(statusbar, BorderLayout.SOUTH);
        
        STALE_LABEL.setForeground(Color.red);
        content = emptyPanel;
        setMinimumSize(new Dimension(0, 0));
        
        refreshButton.setContentAreaFilled(false);
        refreshButton.setText("");
        refreshButton.setBorder(null);
        refreshButton.setContext(context);
    }
    
    public AbstractComponent getCurrentlyShowingComponent() {
        return view.getManifestedComponent();
    }
    
    private ActionContext context = new ActionContextImpl() {
        @Override
        public View getWindowManifestation() {
            return Inspector.this;
        }
    };
    
    /**
     * Prompt the user to commit or abort pending changes, 
     * if there are any. Note that this may not be possible 
     * (component may not be writeable, for instance). 
     * 
     * This only returns false when the action is explicitly 
     * aborted (so if there is no prompt because the user 
     * chooses Save, this still returns true.)
     * 
     * @return false if change was aborted
     */
    private boolean commitOrAbortPendingChanges() {
        AbstractComponent committedComponent = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(view.getManifestedComponent().getComponentId());
        if (committedComponent == null)
            return true;
        
        if (!isComponentWriteableByUser(view.getManifestedComponent()))
            return true;
        
        String save = BUNDLE.getString("view.modified.alert.save");
        String saveAll = BUNDLE.getString("view.modified.alert.saveAll");
        String abort = BUNDLE.getString("view.modified.alert.abort");
        
        // Show options - Save, Abort, or maybe Save All
        String[] options = view.getManifestedComponent().getAllModifiedObjects().isEmpty() ?
        		    new String[]{ save, abort } :
        		    new String[]{ save, saveAll, abort};
    
        Map<String, Object> hints = new HashMap<String, Object>();
        hints.put(WindowManagerImpl.MESSAGE_TYPE, OptionBox.WARNING_MESSAGE);
        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
        hints.put(WindowManagerImpl.PARENT_COMPONENT, view);

        Object answer = PlatformAccess.getPlatform().getWindowManager().showInputDialog(
                BUNDLE.getString("view.modified.alert.title"), 
                MessageFormat.format(BUNDLE.getString("view.modified.alert.text"), view.getInfo().getViewName(), view.getManifestedComponent().getDisplayName()), 
                options, 
                options[0], 
                hints);
        
        if (answer.equals(save)) {
            PlatformAccess.getPlatform().getPersistenceProvider().persist(Collections.singleton(view.getManifestedComponent()));
        } else if (answer.equals(saveAll)) { // Save All
            AbstractComponent comp = view.getManifestedComponent();
            Set<AbstractComponent> allModifiedObjects = comp.getAllModifiedObjects();
            if (comp.isDirty()) {
                allModifiedObjects.add(comp);
            }
            PlatformAccess.getPlatform().getPersistenceProvider().persist(allModifiedObjects);
        }
        
        return true;
    }
    
    private boolean isComponentWriteableByUser(AbstractComponent component) {
        Platform p = PlatformAccess.getPlatform();
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), component);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String inspectionKey = PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey();
        return p.getPolicyManager().execute(inspectionKey, policyContext).getStatus();
    }

    public void refreshCurrentlyShowingView() {
        refreshInspector(view.getInfo());
    }
    
    private void refreshInspector(ViewInfo viewInfo) {
        Inspector.this.remove(content);
        AbstractComponent ac = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(view.getManifestedComponent().getComponentId());
        view.removePropertyChangeListener(VIEW_STALE_PROPERTY, objectStaleListener);
        content = view = viewInfo.createView(ac);
        view.addPropertyChangeListener(VIEW_STALE_PROPERTY, objectStaleListener);
        Dimension preferredSize = content.getPreferredSize();
        JScrollPane jp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        preferredSize.height += jp.getHorizontalScrollBar().getPreferredSize().height;
        JScrollPane inspectorScrollPane = new JScrollPane(content,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Inspector.this.add(inspectorScrollPane, BorderLayout.CENTER);
        Inspector.this.revalidate();
        content = inspectorScrollPane;
        viewTitle.setText(view.getManifestedComponent().getDisplayName());
        viewControls = view.getControlManifestation();
        if (controlAreaToggle.isSelected()) { // Close control area if it's open
            controlAreaToggle.doClick();
        }
        controlAreaToggle.setEnabled(viewControls != null);
           
        STALE_LABEL.setVisible(false);
        populateStatusBar();
        
        refreshButton.setContext(context);
        
        view.requestFocusInWindow();
    }
    
    private void populateStatusBar() {
        // add status widgets
        statusbar.removeAll();
        for (JComponent w : view.getStatusWidgets()) {
            statusbar.add(w);
        }
        statusbar.add(STALE_LABEL);
        STALE_LABEL.setVisible(false);
    }
    
    @Override
    public View getHousedViewManifestation() {
        return view;
    }
    
    private void selectedManifestationChanged(View view) {
        remove(content);
        if (view == null) {
            viewTitle.setIcon(null);
            viewTitle.setText("");   
            viewTitle.setTransferHandler(null);
            content = emptyPanel;
        } else {
            viewTitle.setIcon(view.getManifestedComponent().getIcon());
            viewTitle.setText(view.getManifestedComponent().getDisplayName());
            viewTitle.setTransferHandler(new WidgetTransferHandler());
            if (this.view != null)
                this.view.removePropertyChangeListener(VIEW_STALE_PROPERTY, objectStaleListener);
            content = this.view = view.getInfo().createView(view.getManifestedComponent());

            if (controlAreaToggle.isSelected()) { // Close control area if it's open
                controlAreaToggle.doClick();
            }
            controlAreaToggle.setEnabled(getViewControls() != null);
            
            c.anchor = GridBagConstraints.LINE_END;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;       
            JPanel p = new JPanel(new BorderLayout());
            View switcher = SwitcherView.VIEW_INFO.createView(view.getManifestedComponent());
            switcher.addMonitoredGUI(this);
            switcher.setForeground(FOREGROUND_COLOR);
            p.setOpaque(false);
            p.add(refreshButton, BorderLayout.WEST);
            p.add(switcher, BorderLayout.CENTER);
            p.add(controlAreaToggle, BorderLayout.EAST);
            titlebar.add(p, c);
            
            
            populateStatusBar();
            this.view.addPropertyChangeListener(VIEW_STALE_PROPERTY, objectStaleListener);
            this.view.requestFocusInWindow();
        }
        Dimension preferredSize = content.getPreferredSize();
        JScrollPane jp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        preferredSize.height += jp.getHorizontalScrollBar().getPreferredSize().height;
        JScrollPane inspectorScrollPane = new JScrollPane(content,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        content = inspectorScrollPane;
        add(inspectorScrollPane, BorderLayout.CENTER);
        
        refreshButton.setContext(context);
        
        revalidate();
        
    }

    private void registerSelectionChange() {
        // Register when the panel inspector is added to the window.
        addAncestorListener(new AncestorListener() {
            SelectionProvider selectionProvider;
            @Override
            public void ancestorAdded(AncestorEvent event) {
                selectionProvider = (SelectionProvider) event.getAncestorParent();
                selectionProvider.addSelectionChangeListener(selectionChangeListener);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                if (selectionProvider != null) {
                    selectionProvider.removeSelectionChangeListener(selectionChangeListener);
                }
            }
            
        });            
    }
    
    private void showOrHideController(boolean toShow) {
        remove(content);
        
        JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(view);
        if (toShow) {
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getViewControls(), scrollPane);
            splitPane.setResizeWeight(.66);
            splitPane.setOneTouchExpandable(true);
            splitPane.setContinuousLayout(true);
            splitPane.setBorder(null);
            content = splitPane;
            // Overwrite lock state for the panel-specific view
            if (isLocked)
                view.exitLockedState();
            else
                view.enterLockedState();
        } else {
            content = scrollPane;
        }
        add(content, BorderLayout.CENTER);
        revalidate();
    }
    
    protected JComponent getViewControls() {
        assert view != null;
        if (viewControls == null)
            viewControls = view.getControlManifestation();
        return viewControls;
    }

    private boolean isLocked = false;
    
    @Override
    public void enterLockedState() {
        isLocked = false;
        super.enterLockedState();
        view.enterLockedState();
    }
    
    @Override
    public void exitLockedState() {
        isLocked = true;
        super.exitLockedState();
        if (view != null) 
            view.exitLockedState();
    }
    
    @Override
    public boolean setHousedViewManifestation(ViewInfo viewInfo) {
        AbstractComponent ac = view.getManifestedComponent();
        if (!ac.isStale() && ac.isDirty()) {
            if (!commitOrAbortPendingChanges()) {
                return false;
            }
        }
        refreshInspector(viewInfo);
        preferredViewType = viewInfo.getType();
        return true;
    }


    private static final class WidgetDragger extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            JComponent c = (JComponent) e.getSource();
            TransferHandler th = c.getTransferHandler();
            th.exportAsDrag(c, e, TransferHandler.COPY);
        }
    }
    
    private final class WidgetTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (view != null) {
                return new ViewRoleSelection(new View[] { view});
            } else {
                return null;
            }
        }
    }

}
