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
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.Twistie;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewRoleSelection;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
    private static final ImageIcon BUTTON_ICON = new ImageIcon(ClassLoader.getSystemResource("images/infoViewButton-OFF.png"));
    private static final ImageIcon BUTTON_PRESSED_ICON = new ImageIcon(ClassLoader.getSystemResource("images/infoViewButton-ON.png"));
    
    private static final String DASH = " - ";
    
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    Inspector.class.getName().substring(0, 
                            Inspector.class.getName().lastIndexOf("."))+".Bundle");

    
    private final PropertyChangeListener selectionChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            @SuppressWarnings("unchecked")
            Collection<View> selectedViews =  (Collection<View>) evt.getNewValue();
            if (selectedViews.isEmpty() || selectedViews.size() > 1) {
                selectedManifestationChanged(null);
            } else {
                if (view != null && view.getManifestedComponent().isDirty()) {
                    commitOrAbortPendingChanges();
                }
                // Retrieve component from database.
                AbstractComponent ac = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(selectedViews.iterator().next().getManifestedComponent().getComponentId());
                // Selection changed is fired when a tree node is removed. 
                if (ac != null)
                    selectedManifestationChanged(ac.getViewInfos(ViewType.OBJECT).iterator().next().createView(ac));
            }
        }
    };

    private JLabel viewTitle = new JLabel();
    private JLabel space = new JLabel(" ");
    private JPanel emptyPanel = new JPanel();
    private JComponent content;
    private View view;
    private JComponent viewControls;
    private JPanel titlebar = new JPanel();
    private JPanel viewButtonBar = new JPanel();
    private GridBagConstraints c = new GridBagConstraints();
    private ControllerTwistie controllerTwistie;

    public Inspector(AbstractComponent ac, ViewInfo vi) {    
        super(ac,vi);
        registerSelectionChange();        
        setLayout(new BorderLayout());
                
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
        viewButtonBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        viewButtonBar.setBackground(BACKGROUND_COLOR);
        
        add(titlebar, BorderLayout.NORTH);
        add(emptyPanel, BorderLayout.CENTER);
        
        content = emptyPanel;
        setMinimumSize(new Dimension(0, 0));
    }
    
    public AbstractComponent getCurrentlyShowingComponent() {
        return view.getManifestedComponent();
    }
    
    private void commitOrAbortPendingChanges() {
        Object[] options = {
                BUNDLE.getString("view.modified.alert.save"),
                BUNDLE.getString("view.modified.alert.abort"),
            };
    
        int answer = OptionBox.showOptionDialog(view, 
                MessageFormat.format(BUNDLE.getString("view.modified.alert.text"), view.getInfo().getViewName(), view.getManifestedComponent().getDisplayName()),                         
                BUNDLE.getString("view.modified.alert.title"),
                OptionBox.YES_NO_OPTION,
                OptionBox.WARNING_MESSAGE,
                null,
                options, options[0]);
        
        if (answer == OptionBox.YES_OPTION) {
            PlatformAccess.getPlatform().getPersistenceProvider().persist(Collections.singleton(view.getManifestedComponent()));
        }                    
    }
    
    public void refreshCurrentlyShowingView() {
        selectedManifestationChanged(view.getInfo().createView(view.getManifestedComponent()));
    }
    
    @Override
    public View getHousedViewManifestation() {
        return view;
    }

    private ButtonGroup createViewSelectionButtons(AbstractComponent ac, ViewInfo selectedViewInfo) {
        ButtonGroup buttonGroup = new ButtonGroup();
        final Set<ViewInfo> viewInfos = ac.getViewInfos(ViewType.OBJECT);
        for (ViewInfo vi : viewInfos) {
            ViewChoiceButton button = new ViewChoiceButton(vi);
            buttonGroup.add(button);
            viewButtonBar.add(button);
            
            if (vi.equals(selectedViewInfo))
                buttonGroup.setSelected(button.getModel(), true);
        }
        return buttonGroup;
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
            viewTitle.setText(view.getManifestedComponent().getDisplayName() + DASH + view.getInfo().getViewName());
            viewTitle.setTransferHandler(new WidgetTransferHandler());
            content = this.view = view.getInfo().createView(view.getManifestedComponent());
            JComponent viewControls = getViewControls();
            if (viewControls != null) {
                c.weightx = 0;
                controllerTwistie = new ControllerTwistie();
                titlebar.add(controllerTwistie, c);
            }
            createViewSelectionButtons(view.getManifestedComponent(), view.getInfo());

            c.anchor = GridBagConstraints.LINE_END;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;       
            titlebar.add(viewButtonBar, c);
        }
        Dimension preferredSize = content.getPreferredSize();
        JScrollPane jp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        preferredSize.height += jp.getHorizontalScrollBar().getPreferredSize().height;
        JScrollPane inspectorScrollPane = new JScrollPane(content,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        content = inspectorScrollPane;
        add(inspectorScrollPane, BorderLayout.CENTER);
        revalidate();
        this.view.requestFocusInWindow();
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
    
    @Override
    public SelectionProvider getSelectionProvider() {
        return super.getSelectionProvider();
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
    
    private final class ViewChoiceButton extends JToggleButton {
        private static final String SWITCH_TO = "Switch to ";

        public ViewChoiceButton(final ViewInfo viewInfo) {
            setBorder(BorderFactory.createEmptyBorder());
            setAction(new AbstractAction() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (view.getManifestedComponent().isDirty()) {
                        commitOrAbortPendingChanges();
                    }
                    Inspector.this.remove(content);
                    AbstractComponent ac = PlatformAccess.getPlatform().getPersistenceProvider().getComponent(view.getManifestedComponent().getComponentId());
                    content = view = viewInfo.createView(ac);
                    Dimension preferredSize = content.getPreferredSize();
                    JScrollPane jp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    preferredSize.height += jp.getHorizontalScrollBar().getPreferredSize().height;
                    JScrollPane inspectorScrollPane = new JScrollPane(content,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    Inspector.this.add(inspectorScrollPane, BorderLayout.CENTER);
                    Inspector.this.revalidate();
                    content = inspectorScrollPane;
                    viewTitle.setText(view.getManifestedComponent().getDisplayName() + DASH + view.getInfo().getViewName());
                    viewControls = null;
                    if (controllerTwistie != null)
                        controllerTwistie.changeState(false);
                    view.requestFocusInWindow();
                }
                
            });
            setIcon(viewInfo.getIcon() == null ? BUTTON_ICON : viewInfo.getIcon());
            setPressedIcon(viewInfo.getIcon() == null ? BUTTON_ICON : viewInfo.getIcon());
            setSelectedIcon(viewInfo.getSelectedIcon() == null ?  BUTTON_PRESSED_ICON : viewInfo.getSelectedIcon());
            setToolTipText(SWITCH_TO + viewInfo.getViewName());
        }        
    }
    
    private final class ControllerTwistie extends Twistie {
        
        public ControllerTwistie() {
            super();
        }
        
        @Override
        protected void changeStateAction(boolean state) {
            showOrHideController(state);
        }        
    }
}
