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
package gov.nasa.arc.mct.canvas.view;

import gov.nasa.arc.mct.canvas.ComponentRegistryAccess;
import gov.nasa.arc.mct.canvas.PolicyManagerAccess;
import gov.nasa.arc.mct.canvas.formatting.ControlAreaFormattingConstants;
import gov.nasa.arc.mct.canvas.formatting.ControlAreaFormattingConstants.PANEL_ZORDER;
import gov.nasa.arc.mct.canvas.layout.CanvasLayoutManager;
import gov.nasa.arc.mct.canvas.panel.CanvasViewStrategy;
import gov.nasa.arc.mct.canvas.panel.Panel;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.gui.MCTViewManifestationInfo;
import gov.nasa.arc.mct.gui.MCTViewManifestationInfoImpl;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.ReloadEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.component.ComponentRegistry;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanvasManifestation extends View implements PanelFocusSelectionProvider, SelectionProvider {
    private static final long serialVersionUID = 984902490997930475L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CanvasManifestation.class);

    final Map<Integer, Panel> renderedPanels;
    private final Set<Panel> selectedPanels;
    private CanvasFormattingControlsPanel controlPanel;
    Augmentation augmentation;
    CanvasPanel canvasPanel;
    int panelId = 0;
    private Color  scrollColor = null;
    private Color  defaultBorderColor = null;
    private Color  titleLabelColor = null;
    private Color  titleBarColor = null;
    
    private boolean canvasEnabled = true;
    private boolean updating = false;
    /* Canvas enable/disable added to facilitate fix of MCT-2832 */
    private static Set<String> manifestingComponents = new HashSet<String>();

    /** MCT-3811 - rvhoof - Indicates whether selections are currently being cleared, used to prevent endless loop */
    private boolean m_clearingSelections = false;    
    
    private final PropertyChangeListener selectionListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            boolean hasSelectedManifestations = hasSelectedManifestations();
            if (!hasSelectedManifestations) {
                return;
            } // end if
            
            Panel selectedPanel = (Panel) evt.getSource();
            for (Panel p : renderedPanels.values()) {
                if (p == selectedPanel) {
                    augmentation.removeHighlights(Collections.singleton(p));
                    selectedPanels.remove(p);
                    augmentation.repaint();
                } else {
                    p.clearCurrentSelections();
                } // end if
            }

            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    /**
     * The client property used for storing manifest info in the swing client properties.
     */
    public static final String MANIFEST_INFO = "gov.nasa.arc.canvas.ManifestInfo";
    public static final String CANVAS_CONTENT_PROPERTY = "CANVAS CONTENT PROPERTY"; 
    
    private String getRealComponentId(AbstractComponent component) {
        return component.getComponentId();
    }
    
    public CanvasManifestation(AbstractComponent component, ViewInfo info) {
        super(component,info);
        setAutoscrolls(true);
        
        this.renderedPanels = new HashMap<Integer, Panel>();
        this.selectedPanels = new LinkedHashSet<Panel>();            
        canvasPanel = new CanvasPanel();
        canvasPanel.getAccessibleContext().setAccessibleName("Canvas");
        canvasPanel.setLayout(new CanvasLayoutManager(CanvasLayoutManager.MIX));
        
        /* 
         * Check to ensure that there are no other canvas manifestations being manifested above this 
         * element, to avoid infinite recursion (tunnel of mirrors)
         */
        if (manifestingComponents.add(getRealComponentId(getManifestedComponent()))) {
            try {
                
                augmentation = new Augmentation(canvasPanel, this);
                setDropTarget(new CanvasDropTarget(this, selectionListener));
                setBackground(getColor("background"));
                canvasPanel.setBackground(getColor("background"));
     
                
                scrollColor = getColor("scroll");
                titleBarColor = getColor("titlebar");
                defaultBorderColor = getColor("defaultBorderColor");
                titleLabelColor = getColor("titlebar.foreground");
                if (titleLabelColor == null) titleLabelColor = getColor("Label.foreground");
                
                     
                Color gridColor = getColor("grid");
                Color gridMinorColor = getColor("grid.minor");
                if (gridMinorColor == null) gridMinorColor = gridColor;
                if (gridColor != null) {
                    canvasPanel.setGridColors(gridColor, gridMinorColor);
                }                
                
                getManifestedComponent().getComponents(); 
                load();
                
                canvasPanel.setAutoscrolls(true);
                                                
                OverlayLayout overlayLayout = new OverlayLayout(this);
                setLayout(overlayLayout);
                add(augmentation);
                add(canvasPanel);
                setRepaintComponentPair(canvasPanel, augmentation);
                computePreferredSize();
            } finally {                
                manifestingComponents.remove(getRealComponentId(getManifestedComponent()));
            }
            
        } else {
            canvasEnabled = false; // Disable this manifestation.
            
            setBackground(Color.GRAY);  //TODO: Move colors & error message to external locations
            JLabel errorLabel = new JLabel("Cannot display canvas view: Nested canvas creates an endless cycle.");
            errorLabel.setForeground(Color.YELLOW); 
            errorLabel.setAlignmentY(TOP_ALIGNMENT);
            setLayout(new FlowLayout());
            add(errorLabel);
        }
    }

    private Color getColor(String name) {
        return UIManager.getColor(name);        
    }
    
    @Override
    public void enterLockedState() {
        super.enterLockedState();
        // ensure that all the references to manifestation info are reloaded so that the properties are associated with
        // the cloned manifestation info
        load();
    }
    
    private void load() {
        if (!canvasEnabled) return; // Disabled panels should load nothing
        ExtendedProperties viewProperties = getViewProperties();
        Set<Object> canvasContents = viewProperties.getProperty(CanvasManifestation.CANVAS_CONTENT_PROPERTY);

        if (canvasContents != null) {
            // Remove stale manifestation info in the canvas view property
            // Note that changes will not be persisted until the next commit.
            AbstractComponent component = getManifestedComponent();
            Iterator<Object> iterator = canvasContents.iterator();
            while(iterator.hasNext()) {
                MCTViewManifestationInfo info = (MCTViewManifestationInfo) iterator.next();
                if (!containsChildComponent(component, info.getComponentId())) {
                    iterator.remove();
                }
            }
        
            int zorder = 0;
            for (Object canvasContent : canvasContents) {
                buildGUI((MCTViewManifestationInfo) canvasContent, zorder);
                zorder++;
            }
            
            removeStalePanels(canvasContents);
            
            panelId++;
            canvasPanel.revalidate();
        } else {
            removeStalePanels(Collections.<Object> emptySet());
            canvasPanel.revalidate();
            canvasPanel.repaint();
        }
    }
    
    private boolean containsChildComponent(AbstractComponent parentComponent, String childComponentId) {
        for (AbstractComponent childComponent : parentComponent.getComponents()) {
            if (childComponentId.equals(childComponent.getComponentId())) return true;
        }
        return false;
        
    }
    private void removeStalePanels(Set<Object> canvasCotents) {
        Iterator<Panel> it = renderedPanels.values().iterator();
        while (it.hasNext()) {
            Panel p = it.next();
            MCTViewManifestationInfo paneInfo = CanvasManifestation.getManifestationInfo(p.getWrappedManifestation());
            if (!canvasCotents.contains(paneInfo)) {
                this.canvasPanel.remove(p);
                it.remove();
                selectedPanels.remove(p);
                augmentation.removeHighlights(Collections.singleton(p));
            }
        }
    }
    
    boolean contains(Panel panel) {
        return renderedPanels.get(panel.getId()) == panel;
    }
    
    /**
     * Returns true if the given point is not within a panel, is in the blank area of the canvas.
     * @param p point to check whether it is directly contained in the canvas or a panel
     * @return true if the point is in nested manifestation
     */
    boolean isPointinaPanel(Point p) {
        for (Entry<Integer, Panel> entry : renderedPanels.entrySet()) {
            Panel panel = entry.getValue();
            if (panel.containsPoint(p)) {
                return true;
            }
        }
        return false; 
    }
    
    /**
     * This method recursively finds the panel by this location on screen.
     * @param p location on screen
     * @return the <code>Panel</code> that contains point p
     */
    Panel findImmediatePanel(Point p) {
        if (!canvasEnabled) return null; // Disabled panels have no sub panels
        ExtendedProperties viewProperties = getViewProperties();
        gov.nasa.arc.mct.util.LinkedHashSet<Object> canvasContents = (gov.nasa.arc.mct.util.LinkedHashSet<Object>)viewProperties.getProperty(CanvasManifestation.CANVAS_CONTENT_PROPERTY);
        if (canvasContents != null) {
            for (Object object : canvasContents) {
                MCTViewManifestationInfo info = (MCTViewManifestationInfo) object;
                String panelOrder = info
                                .getInfoProperty(ControlAreaFormattingConstants.PANEL_ORDER);
                Panel panel = renderedPanels.get(Integer.valueOf(panelOrder));
                
                if (panel != null) { 
                    
                    if (panel.containsPoint(p)) {
                        View wrappedManifestation = panel.getWrappedManifestation();
                        if (wrappedManifestation instanceof CanvasManifestation) {
                            CanvasManifestation innerCanvasManifestation = (CanvasManifestation) wrappedManifestation;
                            Panel innerPanel = innerCanvasManifestation.findImmediatePanel(p);
                            if (innerPanel != null)
                                return innerPanel;
                        }
                      return panel;
                    }
                } else {
                    LOGGER.error("Panel with ID: " + panelOrder + " does not exist. It will be removed.");
                    renderedPanels.remove(panelOrder);
                }
            }
        }
        return null;
    }
    
    private View getManifestationFromViewInfo(AbstractComponent comp, String viewType, MCTViewManifestationInfo canvasContent) {
        Collection<ViewInfo> embeddedInfos = comp.getViewInfos(ViewType.EMBEDDED);
        
        if (embeddedInfos.isEmpty())
            return null;
        
        ViewInfo matchedVi = null;
        for (ViewInfo vi:embeddedInfos) {
            if (vi.getType().equals(viewType)) {
                matchedVi = vi;
                break;
            }
        }
        
        if (matchedVi == null) {
            matchedVi = embeddedInfos.iterator().next();
        }
        
        return CanvasViewStrategy.CANVAS_OWNED.createViewFromManifestInfo(matchedVi, comp, getManifestedComponent(), canvasContent);
    }
    
    private View getManifestation(AbstractComponent comp, String viewType, MCTViewManifestationInfo canvasContent) {
        View viewManifestation = getManifestationFromViewInfo(comp, viewType, canvasContent);
        if (viewManifestation == null) {
            LOGGER.error("Cannot find view type {}.", viewType);
            return null;
        }
        
        viewManifestation.putClientProperty(CanvasManifestation.MANIFEST_INFO, canvasContent);
        viewManifestation.setNamingContext(canvasContent);
        assert viewManifestation.getNamingContext() == canvasContent;
        return viewManifestation;
    }
            
    private void buildGUI(MCTViewManifestationInfo canvasContent, int zorder) {
        if (!canvasEnabled) return; // Nothing to build on a disabled canvas
        String componentId = canvasContent.getComponentId();
        String indexStr = canvasContent.getInfoProperty(ControlAreaFormattingConstants.PANEL_ORDER);
        int index = Integer.parseInt(indexStr);
        if (panelId < index) {
            panelId = index;
        }

        Panel existingPanel = renderedPanels.get(index);
        if (existingPanel != null) {
            if (!updating || selectedPanels.contains(existingPanel)) {
                // update existing panel
                existingPanel.update(canvasContent);
                changeOrder(existingPanel, zorder);
            }
        } else {
            String viewType = canvasContent.getManifestedViewType();
            ComponentRegistry cr = ComponentRegistryAccess.getComponentRegistry();
            AbstractComponent comp = cr.getComponent(componentId);

            View viewManifestation = getManifestation(comp, viewType, canvasContent);
            if (viewManifestation == null) { return; }
            
            Panel panel = createPanel(viewManifestation, index, this);
            viewManifestation.setNamingContext(panel);
            Dimension dimension = canvasContent.getDimension();
            Point location = canvasContent.getStartPoint();
            Rectangle bound = new Rectangle(location, dimension);

            panel.setBounds(bound);

            placeWidget(panel, zorder);
            renderedPanels.put(index, panel);
        }
    }

    private void placeWidget(Panel widget, int zorder) {
        canvasPanel.add(widget, null);
        changeOrder(widget, zorder);
    }
    
    public Panel createPanel(View manifestation, int panelId, PanelFocusSelectionProvider focusProvider) {
        Panel p = new Panel(manifestation, panelId, focusProvider);
        if (scrollColor != null) {
            p.setScrollColor(scrollColor, getBackground());
        }
        if (titleBarColor != null) {
            p.setTitleBarColor(titleBarColor, titleLabelColor);
        }
        if (defaultBorderColor != null) {
            p.setDefaultBorderColor(defaultBorderColor);
        }

        p.setBackground( getBackground() );
        
        p.getSelectionProvider().addSelectionChangeListener(selectionListener);
        return p;
    }

    @Override
    protected JComponent initializeControlManifestation() {
        // Set canvas control
        this.controlPanel = new CanvasFormattingControlsPanel(this);
        Dimension d = controlPanel.getMinimumSize();
        d.setSize(0, 0);
        controlPanel.setMinimumSize(d);
        
        Dimension preferredSize = controlPanel.getPreferredSize();
        JScrollPane jp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        preferredSize.height += jp.getHorizontalScrollBar().getPreferredSize().height;
        controlPanel.setPreferredSize(preferredSize);
        JScrollPane controlScrollPane = new JScrollPane(controlPanel,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return controlScrollPane;
    }
    
    List<Panel> getSelectedPanels() {
        return Collections.unmodifiableList(new LinkedList<Panel>(selectedPanels));
    }

    @Override
    public void updateMonitoredGUI() {
        if (updating)
            return;
        
        load();
    }
    
    @Override
    public void updateMonitoredGUI(ReloadEvent event) {
        updateMonitoredGUI();
    }

    @Override
    public void updateMonitoredGUI(AddChildEvent event) {
        updateMonitoredGUI();
    }
    
    @Override
    public void updateMonitoredGUI(RemoveChildEvent event) {
        updateMonitoredGUI();
    }
    
    @Override
    public boolean isContentOwner() {
        return true;
    }
    
    public Color getDefaultBorderColor() {
        return defaultBorderColor != null ? defaultBorderColor : Color.BLACK;
    }
    
    @Override
    public void fireFocusSelection(Panel panel) {
        if (panel != null) {
            clearSelections();
            select(panel);
            if (controlPanel != null) {
                controlPanel.informOnePanelSelected(getSelectedPanels());
            }
            firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedManifestations());
            augmentation.repaint();
        }
    }
    
    @Override
    public void fireSelectionRemoved(Panel panel) {
        assert selectedPanels.contains(panel) : "Panel must be selected in the canvas.";
        // Remove selection and highlight.
        augmentation.removeHighlights(Collections.singleton(panel));
        selectedPanels.remove(panel);
        firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedManifestations());
        if (controlPanel != null) {
            switch (selectedPanels.size()) {
            case 0: controlPanel.informZeroPanelsSelected(); break;
            case 1: controlPanel.informOnePanelSelected(getSelectedPanels()); break;
            default : controlPanel.informMultipleViewPanelsSelected(getSelectedPanels()); break;
            }
        }
        
        // Remove from canvas panel and persist.
        Integer key = null;
        for (Entry<Integer, Panel> entry : renderedPanels.entrySet()) {
            if (panel == entry.getValue()) {
                key = entry.getKey();
                break;
            }                    
        }
        assert key != null : "Panel not found in canvas view.";
        renderedPanels.remove(key);
        canvasPanel.remove(panel);
        canvasPanel.repaint();
        Set<Object> viewProperties = getViewProperties().getProperty(CanvasManifestation.CANVAS_CONTENT_PROPERTY);
        MCTViewManifestationInfo info = null;
        for (Object viewProperty : viewProperties) {
            assert viewProperty instanceof MCTViewManifestationInfo : "Canvas property must be MCTViewManifestationInfo";
            info = (MCTViewManifestationInfo) viewProperty;
            if (info == CanvasManifestation.getManifestationInfo(panel.getWrappedManifestation())) {
                break;
            }
        }
        viewProperties.remove(info);
        fireFocusPersist();
        firePropertyChange(PanelFocusTraversalPolicy.FOCUS_SELECTION_CHANGE_PROP, null, renderedPanels);
    }
    
    @Override
    public void fireSelectionCloned(Collection<Panel> panels) {
        for (Panel panel : panels) {
            int nextPanelId = panelId++;
            panel.setId(nextPanelId);
            View manifestation = panel.getWrappedManifestation();         
            MCTViewManifestationInfo viewManifestationInfo = CanvasManifestation.getManifestationInfo(manifestation);
            getViewProperties().addProperty(CANVAS_CONTENT_PROPERTY, viewManifestationInfo);            
            viewManifestationInfo.addInfoProperty(ControlAreaFormattingConstants.PANEL_ORDER, Integer.toString(nextPanelId));
            canvasPanel.add(panel, new Rectangle(viewManifestationInfo.getStartPoint(), viewManifestationInfo.getDimension()));
            renderedPanels.put(nextPanelId, panel);
        }

        canvasPanel.revalidate();
        setSelection(panels);
        fireFocusPersist();
        firePropertyChange(PanelFocusTraversalPolicy.FOCUS_SELECTION_CHANGE_PROP, null, renderedPanels);
    }

    @Override
    public void fireFocusPersist() {
        if (!isLocked()) {
            try {
                updating = true;
                getManifestedComponent().save();
            } finally {
                updating = false;
            }
        }
    }
    
    @Override
    public void fireManifestationChanged() {
        firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedManifestations());
    }
    
    public void firePanelsDropped() {
        firePropertyChange(PanelFocusTraversalPolicy.FOCUS_SELECTION_CHANGE_PROP, null, renderedPanels);
        canvasPanel.computePreferredSize();
    }
    
    public void updateController(Collection<Panel> panels) {
        // This occurs when the canvas manifestation is in a panel.
        // We are NOT currently supporting canvas control in a panel.
        if (controlPanel == null)                                       
            return;
        
        if (panels == null || panels.isEmpty())
            controlPanel.informZeroPanelsSelected();
        else if (panels.size() == 1)
            controlPanel.informOnePanelSelected(Collections.singletonList(panels.iterator().next()));
        else
            controlPanel.informMultipleViewPanelsSelected(panels);            
    }
    private void changeOrder(Panel panel, int order) {
        canvasPanel.setComponentZOrder(panel, order);
    }
    
    void changeOrder(Panel panel, PANEL_ZORDER order) {
        ExtendedProperties viewProperties = getViewProperties();
        gov.nasa.arc.mct.util.LinkedHashSet<Object> canvasContents = (gov.nasa.arc.mct.util.LinkedHashSet<Object>)viewProperties.getProperty(CanvasManifestation.CANVAS_CONTENT_PROPERTY);
        MCTViewManifestationInfo movedManifestInfo = CanvasManifestation.getManifestationInfo(panel.getWrappedManifestation());
        canvasContents.remove(movedManifestInfo);

        switch(order) {
            case FRONT: changeOrder(panel, 0);
                canvasContents.add(movedManifestInfo);
                break;
            case BACK: changeOrder(panel, canvasPanel.getComponentCount()-1);
                canvasContents.offerLast(movedManifestInfo);
                break;
        }
    }
    
    @Override
    public void fireOrderChange(Panel panel, PANEL_ZORDER order) {
        changeOrder(panel, order);
        canvasPanel.repaint();
    }
    
    public void enableGrid(int gridSize) {
        canvasPanel.paintGrid(gridSize);
        
    }
    
    /**
     * Sets a single selection based on the mouse location.
     * @param p <code>Point</code>
     */
    Panel setSingleSelection(Point p) {
        clearSelections();
        return addSingleSelection(p);
    }
    
    /**
     * Add a single selection based on mouse location.
     * @param p <code>Point</code>
     */
    Panel addSingleSelection(Point p) {
        Panel panel = findImmediatePanel(p);
        if (panel != null) {
            Container container = SwingUtilities.getAncestorOfClass(CanvasManifestation.class, panel);
            assert container instanceof CanvasManifestation : "Panels must be contained in a CanvasManifestation";
            CanvasManifestation nestedCanvasManifestation = (CanvasManifestation) container;
            nestedCanvasManifestation.select(panel);
        }        
        return panel;
    }
    
    /**
     * Sets the selected panels to the given selection
     * @param newlySelectedPanels panels to select
     */
    void setSelection(Collection<Panel> newlySelectedPanels) {
        clearSelections();
        selectedPanels.addAll(newlySelectedPanels);
        for (Panel p:newlySelectedPanels) {
            changeOrder(p, PANEL_ZORDER.FRONT);
        }
        augmentation.addHighlights(newlySelectedPanels); 
        if (controlPanel != null) {
            if (newlySelectedPanels.size() == 1) {
                controlPanel.informOnePanelSelected(Collections.singletonList(newlySelectedPanels.iterator().next()));
            } else {
                controlPanel.informMultipleViewPanelsSelected(newlySelectedPanels);
            }
        }
        firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedManifestations());
    }
    
    /**
     * Adds the target panel to the set of selectedPanels in this <code>CanvasManifestation</code>.
     * In addition, it highlights the panel and brings it to front.
     * @param panel
     */
    private void select(Panel panel) {
        if (selectedPanels.contains(panel))
            return;
        panel.clearCurrentSelections();
        selectedPanels.add(panel);
        if (controlPanel != null) {
            if (selectedPanels.size() == 1) {
                controlPanel.informOnePanelSelected(getSelectedPanels());
            } else {
                controlPanel.informMultipleViewPanelsSelected(getSelectedPanels());
            }
        }
        changeOrder(panel, PANEL_ZORDER.FRONT);
        augmentation.addHighlights(selectedPanels);
        firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedManifestations());
    }
    
    /**
     * Clears all selections and all its nested <code>CanvasManifestation</code>s.
     * Also resets the formatting control settings. 
     */
    private void clearSelections() {
        clearSelectionInCanvas();
        if (controlPanel != null) {
            controlPanel.informZeroPanelsSelected();
        }
        firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedManifestations());
    }
    
    private void clearSelectionInCanvas() {
        if (!canvasEnabled) return; // Disabled panels contain nothing
        augmentation.removeHighlights(selectedPanels);            
        selectedPanels.clear();
        for (Entry<Integer, Panel> entry : renderedPanels.entrySet()) {
            Panel panel = entry.getValue();
            panel.clearCurrentSelections();
        }
    }

    @Override
    public SelectionProvider getSelectionProvider() {
        return this;
    }
    
    @Override
    public void addSelectionChangeListener(PropertyChangeListener listener) {
        addPropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP, listener);            
    }

    @Override
    public void clearCurrentSelections() {
        if (!m_clearingSelections && hasSelectedManifestations()) {
            m_clearingSelections = true;
            clearSelections();
            
            // Clear selection in panels
            for (Panel p : renderedPanels.values()) {
                p.clearCurrentSelections();
            } // end for
            m_clearingSelections = false;
        } // end if
    } // clearCurrentSelections

    public void selectAll() {
        setSelection(renderedPanels.values());
        augmentation.repaint();
    }

    @Override
    public Collection<View> getSelectedManifestations() {
        List<View> manifestations = new ArrayList<View>();
        addToSelectionCollection(manifestations);
        return manifestations;
    }
    
    private void addToSelectionCollection(List<View> manifestations) {
        for (Panel panel : selectedPanels)
            manifestations.add(panel.getWrappedManifestation());
        for (Entry<Integer, Panel> entry : renderedPanels.entrySet()) {
            Panel panel = entry.getValue();
            View wrappedManifestation = panel.getWrappedManifestation();
            if (wrappedManifestation instanceof CanvasManifestation) {
                ((CanvasManifestation) wrappedManifestation).addToSelectionCollection(manifestations); 
            }
        }
    }

    /**
     * Checks whether this canvas has any selected manifestations either directly selected
     * on this canvas or in any of its panels.
     *
     * @return <code>true</code> if the canvas has selected manifestations, <code>false</code> otherwise
     */
    public boolean hasSelectedManifestations() {
        if (!selectedPanels.isEmpty()) {
            return true;
        } // end if
        for (Entry<Integer, Panel> entry : renderedPanels.entrySet()) {
            Panel panel = entry.getValue();
            View wrappedManifestation = panel.getWrappedManifestation();            
            if (wrappedManifestation instanceof CanvasManifestation) {
                if (((CanvasManifestation) wrappedManifestation).hasSelectedManifestations()) {
                    return true;
                } // end if
            } else {
                if (!wrappedManifestation.getSelectionProvider().getSelectedManifestations().isEmpty()) {
                    return true;
                } // end if
            } // end if
        } // end for
        return false;
    } // hasSelectedManifestations
    
    @Override
    public void removeSelectionChangeListener(PropertyChangeListener listener) {
        removePropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP, listener);
    }
    
    public int getGridSize() {
        return this.canvasPanel.getGridSize();
    }
    
    public boolean isSnapEnable() {
        return this.canvasPanel.isSnapEnable();
    }
    
    public void enableSnap(boolean snapToGrid) {
        if (getGridSize() != ControlAreaFormattingConstants.NO_GRID_SIZE) {
            this.canvasPanel.enableSnap(snapToGrid);
        }
    }
    
    public void retile() {
        this.canvasPanel.retile();
    }
    
    Dimension getCanvasSize() {
        return this.canvasPanel.getPreferredSize();
    }
    
    
    void computePreferredSize() {
        this.canvasPanel.computePreferredSize();
    }
    
    public static MCTViewManifestationInfo getManifestationInfo(View v) {
        return MCTViewManifestationInfo.class.cast(v.getClientProperty(CanvasManifestation.MANIFEST_INFO));
    }

    private final class CanvasPanel extends JPanel {
        private static final long serialVersionUID = 1066175421844534610L;
        
        private BufferedImage grid = null;
        
        private Color gridMajor = ControlAreaFormattingConstants.MAJOR_GRID_LINE_COLOR;
        private Color gridMinor = ControlAreaFormattingConstants.MINOR_GRID_LINE_COLOR;

        @Override
        public boolean isOptimizedDrawingEnabled() {
            return false;
        }
        
        public int getGridSize() {
            CanvasLayoutManager layoutManager = (CanvasLayoutManager)getLayout();
            return layoutManager.getGridSize();
        }
        
        public void computePreferredSize() {
            Component[] childComponents = getComponents();
            if (childComponents.length == 0) { return; }
            
            Rectangle bound = childComponents[0].getBounds();
            int largestWidth = bound.x + bound.width;
            int largestHeight = bound.y + bound.height;
            
            for (int i=1; i<childComponents.length; i++) {
                bound = childComponents[i].getBounds();
                if (bound.x+bound.width > largestWidth) {
                    largestWidth = bound.x + bound.width;
                }
                if (bound.y+bound.height > largestHeight) {
                    largestHeight = bound.y+bound.height;
                }
            }
            Dimension currentDimension = getPreferredSize();
            if (largestWidth != currentDimension.width || largestHeight != currentDimension.height) {
                if (((JComponent) CanvasManifestation.this.getParent()) != null) {
                    Rectangle visibleRect = ((JComponent) CanvasManifestation.this.getParent()).getVisibleRect();
                    if (largestWidth < visibleRect.width)
                        largestWidth = visibleRect.width;
                    if (largestHeight < visibleRect.height)
                        largestHeight = visibleRect.height;
                }
                setPreferredSize(new Dimension(largestWidth, largestHeight));
                
                revalidate();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (grid != null) {
                int x, y;
                int width, height;

                Rectangle clip = this.getBounds();

                width = grid.getHeight(this);
                height = grid.getWidth(this);
                
                if(width > 0 && height > 0) {
                    for(x = clip.x; x < (clip.x + clip.width) ; x += width) {
                        for(y = clip.y; y < (clip.y + clip.height) ; y += height) {
                            g.drawImage(grid,x,y,this);
                        }
                    }
                }

            }
        }
        
        public void paintGrid(int gridSize) {
            if (gridSize != getGridSize()) {
                CanvasLayoutManager layoutManager = (CanvasLayoutManager)getLayout();
                layoutManager.setGridSize(gridSize);
                if (gridSize == ControlAreaFormattingConstants.NO_GRID_SIZE) {
                    grid = null;
                } else {
                    int w = ControlAreaFormattingConstants.MAJOR_GRID_LINE;
                    int h = ControlAreaFormattingConstants.MAJOR_GRID_LINE;

                    grid = (BufferedImage) (this.createImage(w, h));

                    if (grid == null)
                        return; // unable to create a bufferedImage...

                    
                    Graphics2D gc = grid.createGraphics();

                // now, draw the grid lines. Note that the second drawLine is for
                // drawing Major lines.
                // The first drawLine is for the minor lines.

                    gc.setColor(this.getBackground());
                    gc.fillRect(0, 0, w, h);
                    
                    for (int x = 0; x < w; x += gridSize) {
                        gc.setColor(gridMinor);
                        gc.drawLine(x, 0, x, h); // minor line
                        if (x % ControlAreaFormattingConstants.MAJOR_GRID_LINE == 0) {
                            gc.setColor(gridMajor);
                            gc.drawLine(x + 1, 0, x + 1, h); // major line
                        }
                    }
                    for (int y = 0; y < h; y += gridSize) {
                        gc.setColor(gridMinor);
                        gc.drawLine(0, y, w, y); // minor line
                        if (y % ControlAreaFormattingConstants.MAJOR_GRID_LINE == 0) {
                            gc.setColor(gridMajor);
                            gc.drawLine(0, y + 1, w, y + 1); // major line
                        }
                    }
                }
                repaint();
            }
        }
        
        public void enableSnap(boolean snapToGrid) {
            CanvasLayoutManager layoutManager = (CanvasLayoutManager)getLayout();
            layoutManager.enableSnap(snapToGrid);
        }
        
        public boolean isSnapEnable() {
            CanvasLayoutManager layoutManager = (CanvasLayoutManager)getLayout();
            return layoutManager.isSnapEnable();
        }
        
        public void retile() {
            CanvasLayoutManager layoutManager = (CanvasLayoutManager)getLayout();
            layoutManager.switchLayout(CanvasLayoutManager.TILE);
            doLayout();
            layoutManager.switchLayout(CanvasLayoutManager.MIX);
        }
        
        public void setGridColors(Color major, Color minor) {
            this.gridMajor = major;
            this.gridMinor = minor;
        }
    }
    
    private static final class CanvasDropTarget extends DropTarget {
        private static final long serialVersionUID = 7461617221853901503L;

        private CanvasManifestation canvasManifestation;
        private PropertyChangeListener selectionListener;

        CanvasDropTarget(CanvasManifestation canvasManifestation, PropertyChangeListener selectionListener) {
            this.canvasManifestation = canvasManifestation;
            this.selectionListener = selectionListener;
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            AbstractComponent canvasComponent = canvasManifestation.getManifestedComponent();
            Transferable data = dtde.getTransferable();
            try {
                if (!data.isDataFlavorSupported(View.DATA_FLAVOR)) {
                    dtde.rejectDrop();
                    return;
                }
                if (canvasComponent != null) {
                    View[] viewRoles = (View[]) data.getTransferData(View.DATA_FLAVOR);
                    dropAction(canvasComponent, Arrays.asList(viewRoles), dtde.getLocation());
                }
            } catch (UnsupportedFlavorException e) {
                LOGGER.error("UnsupportedFlavorException", e);
            } catch (IOException e) {
                LOGGER.error("IOException", e);
            }
        }

        private void dropAction(AbstractComponent canvasComponent,
                        Collection<View> droppedViews, Point location) {
            // Check if component is locked and not editable.
            if (canvasManifestation.isLocked()) {
                showCompositionErrorMessage("Object \"" + canvasComponent.getDisplayName() + "\" is currently locked.");
                return;
            }
            
            // Differentiate dropped components between new and existing children.
            List<AbstractComponent> toBeComposed = new LinkedList<AbstractComponent>();
            List<AbstractComponent> referencedComponents = canvasComponent.getComponents();
            
            Collection<AbstractComponent> dropComponents = getDroppedComponents(droppedViews.toArray(new View[droppedViews.size()]));
            for (AbstractComponent sourceComponent : dropComponents) {
                AbstractComponent actualComponent = sourceComponent;
                boolean found = false;
                for (AbstractComponent ac : referencedComponents) {
                    if (ac.getComponentId().equals(actualComponent.getComponentId())) {
                        found = true;
                    }
                }
                if (!found)
                    toBeComposed.add(actualComponent);
            }
            
            // Check if allows adding/removing children from current component.
            if (!toBeComposed.isEmpty()) {
              ExecutionResult result = checkPolicyForDrops(canvasComponent, toBeComposed, canvasManifestation);
                  if (!result.getStatus()) {
                      showCompositionErrorMessage(result.getMessage());
                  return;
              }
            }
            
            if (!toBeComposed.isEmpty()) {
                canvasComponent.addDelegateComponents(toBeComposed);
            }
            
            final Collection<Panel> panels = new LinkedHashSet<Panel>();
                // Update the drop point just in case there is something in
                // the toBeComposed collection
            panels.addAll(addToCanvas(droppedViews, canvasManifestation, location));

            // Select the newly added panels.
            // JIRA 3782 - rvhoof - An NPE was raised due to setSelections clearing
            // the transfer handler in the inspector, causing the DnD framework not
            // being able to invoke exportDone. Now allowing DnD to complete and
            // to perform the selection of the dropped component(s) after DnD
            // completes
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    canvasManifestation.setSelection(panels);
                    canvasManifestation.firePanelsDropped();
                    Window w = SwingUtilities.getWindowAncestor(canvasManifestation);
                    if (w != null) {
                        w.toFront();
                    }
                } // run
            });
        }
 
        private void showCompositionErrorMessage(final String message) {
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    OptionBox.showMessageDialog(canvasManifestation, message,
                                    "Composition Error - ", OptionBox.ERROR_MESSAGE);
                }
            });

        }
        
        private boolean isViewEmbeddable(View v, AbstractComponent comp) {
            return comp.getViewInfos(ViewType.EMBEDDED).contains(v.getInfo());
        }
        
        private ViewInfo getViewInfoForCanvas(View v, AbstractComponent comp) {
            if (isViewEmbeddable(v, comp)) {
                return v.getInfo();
            }
            
            Set<ViewInfo> viewInfos = comp.getViewInfos(ViewType.EMBEDDED);    
            return viewInfos.isEmpty() ? EmptyPanelView.VIEW_INFO : viewInfos.iterator().next();
        }
        
        private Collection<Panel> addToCanvas(Collection<View> toBeAddedViews,
                        CanvasManifestation containerManifestation, Point dropPoint) {
            Collection<Panel> panels = new LinkedHashSet<Panel>();
            for (View v : toBeAddedViews) {
                AbstractComponent viewComp = v.getManifestedComponent();
                ViewInfo newViewInfo = getViewInfoForCanvas(v, viewComp);
                AbstractComponent comp = viewComp;
                
                int nextPanelId = containerManifestation.panelId++;
                MCTViewManifestationInfo viewManifestationInfo = new MCTViewManifestationInfoImpl();
                viewManifestationInfo.setComponentId(comp.getComponentId());
                viewManifestationInfo.setStartPoint(dropPoint);
                viewManifestationInfo.setManifestedViewType(newViewInfo.getType());
                if (v.getInfo().equals(newViewInfo)) {
                    ExtendedProperties ep = v.getViewProperties().clone();
                    ep.addProperty(CanvasViewStrategy.OWNED_TYPE_PROPERTY_NAME, v.getInfo().getType());
                    viewManifestationInfo.getOwnedProperties().add(ep);
                }
                viewManifestationInfo.addInfoProperty(ControlAreaFormattingConstants.PANEL_ORDER, String.valueOf(nextPanelId));
                // use the viewComp here instead of the master component to retrieve the actual properties for the view
                View addManifestation = CanvasViewStrategy.CANVAS_OWNED.createViewFromManifestInfo(newViewInfo, comp, canvasManifestation.getManifestedComponent(), viewManifestationInfo);
                
                addManifestation.putClientProperty(CanvasManifestation.MANIFEST_INFO, viewManifestationInfo);
                Panel panel = containerManifestation.createPanel(addManifestation, nextPanelId, containerManifestation);
                viewManifestationInfo.setDimension(panel.getPreferredSize());

                addManifestation.setNamingContext(panel);
                assert addManifestation.getNamingContext() == panel;
                
                // Add new panel info to the canvas content property list
                ExtendedProperties viewTypeProperties = containerManifestation.getViewProperties();
                viewTypeProperties.addProperty(CANVAS_CONTENT_PROPERTY, viewManifestationInfo);                
                containerManifestation.renderedPanels.put(
                                nextPanelId, panel);
                containerManifestation.canvasPanel.add(panel, new Rectangle(dropPoint, panel.getPreferredSize()));
                panels.add(panel);
                containerManifestation.changeOrder(panel, PANEL_ZORDER.FRONT);
                
                panel.getSelectionProvider().addSelectionChangeListener(selectionListener);
            }
            if (!panels.isEmpty()) {
                containerManifestation.canvasPanel.revalidate();
                containerManifestation.fireFocusPersist();
            }
                
            return panels;
        }

        protected ExecutionResult checkPolicyForDrops(AbstractComponent canvasComponent,
                        Collection<AbstractComponent> dropComponents,
                        View canvasManifestation) {
            PolicyContext context = new PolicyContext();
            context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(),
                            canvasComponent);
            context.setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),
                            dropComponents);
            context
                            .setProperty(PolicyContext.PropertyName.ACTION.getName(), Character
                                            .valueOf('w'));
            context.setProperty(PolicyContext.PropertyName.VIEW_MANIFESTATION_PROVIDER.getName(),
                            canvasManifestation);
            String policyCategoryKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
            ExecutionResult result = PolicyManagerAccess.getPolicyManager().execute(
                            policyCategoryKey, context);
            return result;
        }

        private List<AbstractComponent> getDroppedComponents(View[] origViews) {
            List<AbstractComponent> marshalledComponents = new LinkedList<AbstractComponent>();
            for (View v : origViews) {
                marshalledComponents.add(v.getManifestedComponent());
            }
            return marshalledComponents;
        }

    }
    
    public static class EmptyPanelView extends View {
        private static final long serialVersionUID = -5989827616630705392L;
        static final ViewInfo VIEW_INFO = 
                        new ViewInfo(EmptyPanelView.class, "", ViewType.EMBEDDED);
        public EmptyPanelView (AbstractComponent ac, ViewInfo vi) {
            super(ac, vi);
        }
    }
    
}