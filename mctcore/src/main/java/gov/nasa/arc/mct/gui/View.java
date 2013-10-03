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
/**
 * MCTGUIComponent.java Sep 2, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.gui;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.lock.manager.LockObserver;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.FocusEvent;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.ReloadEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.OverlayLayout;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * Implements a skeleton of a view. Component developers
 * should derive from this class to implement their own view manifestation
 * classes. 
 */
@SuppressWarnings("serial")
public abstract class View extends JPanel implements ViewProvider, LockObserver {

    /**
     * Property name used to register property listeners to respond to
     * a view stale event triggered by stale object.
     */
    public static final String VIEW_STALE_PROPERTY = "VIEW_STALE_PROPERTY";

    /**
     * This member represents the data flavor used in the platform for drag and drop.  
     */
    public static final DataFlavor DATA_FLAVOR;
    
    static {
        RepaintManager.setCurrentManager(GlassPanelRepaintManager.getInstance());
        DataFlavor localDataFlavor=null;
        try {
            localDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType) {
               @Override
               public Class<?> getRepresentationClass() {
                   return View.class;
               }  
            };
        } catch (ClassNotFoundException e) {
            MCTLogger.getLogger(View.class).error("error when creating data flavor",e);
        }
        DATA_FLAVOR = localDataFlavor;
    }

    
    /**
     * A dummy view manifestation, used as a sentinel value.
     */
    public final static View NULL_VIEW_MANIFESTATION = new View() {
        @Override
        public AbstractComponent getManifestedComponent() {
            return AbstractComponent.NULL_COMPONENT;
        }

        @Override
        public void enterLockedState() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void exitLockedState() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void processDirtyState() {
            // TODO Auto-generated method stub
            
        }
    };
    
    private static final SelectionProvider NULL_SELECTION_PROVIDER = new SelectionProvider() {

        @Override
        public void addSelectionChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public Collection<View> getSelectedManifestations() {
            return Collections.emptyList();
        }

        @Override
        public void removeSelectionChangeListener(PropertyChangeListener listener) {
        }
        
        @Override
        public void clearCurrentSelections() {
        }
        
    };
    
    private AbstractComponent manifestedComponent;
    
    /** The listener for this view manifestation. */
    protected ViewListener viewManifestationListener;
    
    private JComponent controlManifestation;
    private ControlWrapper controlWrapper;
    private NamingContext namingContext;
    private ViewInfo info;
        
    /**
     * Creates a new view manifestation with no parent view role.
     * This constructor should be followed by a call to {@link #setInfo(ViewInfo)}.
     * This constructor should be used internally only.
     */
    public View() {
        super(new GridLayout(1, 1));
    }
    
    /**
     * Creates a new instance with the specified component and view info.
     * @param component to bind this view to. 
     * @param viewInfo used to create this component.
     */
    public View(AbstractComponent component, ViewInfo viewInfo) {
        this();
        setManifestedComponent(component);
        setInfo(viewInfo);
    }
        
    /**
     * Updates the view manifestation because of a change in the model
     * or relationships with the parent component.
     */
    public void updateMonitoredGUI() {
    }
    
    /**
     * Updates the view manifestation because a new child component has
     * been added.
     * 
     * @param event the add child event
     */
    public void updateMonitoredGUI(AddChildEvent event) {
        //
    }
    
    /**
     * Updates the view manifestation because the component state has been
     * reloaded from persistent storage.
     * 
     * @param event the reload event
     */
    public void updateMonitoredGUI(ReloadEvent event) {
        //
    }
    
    /**
     * Updates the view manifestation because a child component has
     * been removed.
     * 
     * @param event the remove child event
     */
    public void updateMonitoredGUI(RemoveChildEvent event) {
        //
    }
    
    /**
     * Updates the view manifestation because of a change in focus.
     * 
     * @param event the focus change event
     */
    public void updateMonitoredGUI(FocusEvent event) {
        //
    }
    
    /**
     * Updates the view manifestation because of a change in a
     * component property.
     * 
     * @param event the property change event
     */
    public void updateMonitoredGUI(PropertyChangeEvent event) {
        //
    }
    
    // This method is to get around the problem that TreeNode is not subclass of JComponent.
    /**
     * Adds a related GUI component that will be updated when this
     * one is.
     * 
     * @param gui the new monitored GUI component
     * @param <T> the type of the monitored GUI component
     */
    public<T> void addMonitoredGUI(T gui) {
        //
    }
    
    /**
     * Invoked when the view properties or the underlying component was persisted to the persistence store. The default 
     * implementation does nothing. 
     */
    public void viewPersisted() {
        
    }
    
    /**
     * Return the selection provider for this manifestation. The default implementation returns
     * a null provider (does not broadcast events). Views targeting the housing, content or directory area must return a selection provider.
     * @return selection provider for this manifestation
     */
    public SelectionProvider getSelectionProvider() {
        return NULL_SELECTION_PROVIDER;
    }
    
    /**
     * Sets the view manifestation listener for this view manifestation.
     * 
     * @param guiListener the new listener
     */
    public void setViewListener(ViewListener guiListener) {
        this.viewManifestationListener = guiListener;
    }
    
    /**
     * Gets the view manifestation listener.
     * 
     * @return the current view manifestation listener
     */
    public ViewListener getViewListener() {
        return this.viewManifestationListener;
    }

    /**
     * Gets the component that this view manifestation is associated
     * with.
     * 
     * @return the associated MCT component
     */
    public AbstractComponent getManifestedComponent() {
        return manifestedComponent;
    }
    
    /**
     * Sets the component that this view manifestation is associated
     * with.
     * 
     * @param manifestedComponent the MCT component
     */
    public void setManifestedComponent(AbstractComponent manifestedComponent) {
        this.manifestedComponent = manifestedComponent;
    }
    
    @Override
    public View getHousedViewManifestation() {
        return this;
    }

    @Override
    public boolean setHousedViewManifestation(ViewInfo vi) {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof View)) { return false; }
        if (this == NULL_VIEW_MANIFESTATION || obj == NULL_VIEW_MANIFESTATION) { return false; }
        
        return this == obj;
    }
    
    @Override
    public void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
    }
    
    @Override
    public void enterLockedState() {
        if (controlWrapper != null)
            controlWrapper.controlGlass.changeState(false);
    }

    @Override
    public void exitLockedState() {
        if (controlWrapper != null)
            controlWrapper.controlGlass.changeState(true);
    }

    @Override
    public void processDirtyState() {
    }

    /**
     * A view manifestation may be associated with a control manifestation,
     * which contains GUI controls that can manipulate the view manifestation.
     * The control manifestation will appear in the control area, which is placed
     * above the view manifestation. The control area is not initially visible
     * until the user clicks on the twistie (triangle) located in the title bar or a tab.
     *
     * @return the control manifestation which is a <code>JComponent</code>;
     * if no control manifestation is necessary for the view, then the method
     * returns null. If there is a control manifestation available, then
     * the method calls executes the 
     * <code>PolicyInfo.CategoryType.SHOW_HIDE_CTRL_MANIFESTATION</code> policy
     * category to check if the control manifestation should be available, i.e,
     * if the twistie should be visible.
     */
    public final JComponent getControlManifestation() {
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), getManifestedComponent());        
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), getInfo());
        ExecutionResult result = PlatformAccess.getPlatform().getPolicyManager().execute(PolicyInfo.CategoryType.SHOW_HIDE_CTRL_MANIFESTATION.getKey(), policyContext);
        if (!result.getStatus())
            return null;
        
        controlManifestation = initializeControlManifestation();
        if (controlManifestation != null) {
            controlWrapper = new ControlWrapper(controlManifestation);
            if (isLocked())
                exitLockedState();
            return controlWrapper;
        }
        return null;
    }
    /**
     * Provides a list of widgets to be displayed in the status
     * area of a window, canvas panel, and an inspector area.
     * @return the list of status widgets provided bu this manifestation
     */
    public List<? extends JComponent> getStatusWidgets() {
        return Collections.emptyList();
    }

    /**
     * Determines if this view owns the content. For example, the content of a canvas view contains panels,
     * and the canvas view <em>owns</em> the settings for each panel. This makes the canvas view a 
     * content owner. In the MCT UI, a content owner <em>must</em> provide its own inspector view, which gets
     * populated in the right pane of a window, when its content is being inspected.  
     * @return true if the view is a content owner; false otherwise. Returns false by default.
     */
    public boolean isContentOwner() {
        return false;
    }
    
    /**
     * This method must be overridden to create the control manifestation (if any)
     * of this view manifestation.
     * @return the control manifestation
     */
    protected JComponent initializeControlManifestation() {
        return null;
    }
    
    /**
     * Checks if this manifestation is currently locked. A manifestation is not modifiable when locked.
     * @return true if this manifestation is currently locked; false otherwise.
     */
    public final boolean isLocked() {
       return false;
    }
    
    /**
     * Calls the <code>MenuManager</code> that returns the <code>JPopupMenu</code> for this manifestation. 
     * The manifestations returned from {@link ActionContext#getSelectedManifestations()} will be the
     * selected manifestations from the containing housing. 
     * 
     * @return the popup menu for the selected manifestations
     */
    public final JPopupMenu getManifestationPopupMenu() {
        return PlatformAccess.getPlatform().getMenuManager().getManifestationPopupMenu(this);
    }
    
    /**
     * If this manifestation uses an overlay layout (i.e., <code>OverlayLayout</code>),
     * it may be desirable for the overlay pane to paint over the pane under it as it 
     * repaints itself. This method adds these <code>JComponent</code>s to the internal 
     * <code>RepaintManager</code> to ensure this behavior.
     * @param mainPane the pane under the overlay pane
     * @param overlayPane the overlay pane
     */
    public final void setRepaintComponentPair(JComponent mainPane, JComponent overlayPane) {
        GlassPanelRepaintManager.registerRepaintPair(mainPane, overlayPane);
    }
    
    /**
     * Gets the naming context for this view.
     * @return the namingContext
     */
    public final NamingContext getNamingContext() {
        return namingContext;
    }

    /**
     * Sets the naming context for this view. 
     * @param namingContext the namingContext to set
     */
    public final void setNamingContext(NamingContext namingContext) {
        this.namingContext = namingContext;
        handleNamingContextChange();
    }

    /**
     * This method is invoked when the naming context is changed. The implementing view should adjust the labels in response to an 
     * invocation of this method. The default implementation of this method does nothing. 
     */
    protected void handleNamingContextChange() {
        
    }
    
    /**
     * Gets the info used to create this view.
     * @return the type info used to create this view.
     */
    public ViewInfo getInfo() {
        return info;
    }

    /**
     * Sets the info used to create this view.
     * @param type the type to set
     */
    private void setInfo(ViewInfo type) {
        this.info = type;
    }

    /**
     * Implements the glass panel. The glass panel is a <code>JComponent</code>
     * that paints a translucent white finish and a lock pad when the underlying 
     * view manifestation is locked.  
     */
    static final class GlassPanel extends JComponent {
        private static final String TOOLTIP_TEXT = "<html>This manifestation is currently locked.<br>Please unlock the inspector title to make changes.</html>";
        private MouseListener mouseBlocker = new MouseAdapter() {};
        private MouseMotionListener motionBocker = new MouseAdapter() {};
        private KeyListener keyBlocker = new KeyAdapter() {};
        private boolean isEnabled;
        
        /**
         * Creates a glass panel.
         */
        public GlassPanel() {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    requestFocusInWindow();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            if (isEnabled) {
                g.setColor(new Color(1.0f, 1.0f, 1.0f, isEnabled ? .45f : 0));
                Rectangle clip = g.getClipBounds();
                g.fillRect(clip.x, clip.y, clip.width, clip.height);
            }
        }
        
        /**
         * Sets the state of the glass panel.
         * @param isEnabled true when is enabled; false otherwise
         */
        public void changeState(boolean isEnabled) {
            if (this.isEnabled == isEnabled)
                return;
            
            this.isEnabled = isEnabled;
            if (isEnabled) {
                addMouseListener(mouseBlocker);
                addMouseMotionListener(motionBocker);
                addKeyListener(keyBlocker);
                setFocusTraversalKeysEnabled(false);
                setToolTipText(TOOLTIP_TEXT);
            } else {
                removeMouseListener(mouseBlocker);
                removeMouseMotionListener(motionBocker);
                removeKeyListener(keyBlocker);
                setFocusTraversalKeysEnabled(true);
                setToolTipText(null);
            }
            requestFocus();
            repaint();
        }
    }
    
    /**
     * Implements a wrapper for the GUI control associated to the
     * view manifestation.  
     */
    static final class ControlWrapper extends JPanel {
        private GlassPanel controlGlass;
        
        public ControlWrapper(JComponent controlManifestation) {
            OverlayLayout controlOverlayLayout = new OverlayLayout(this);
            controlGlass = new GlassPanel();
            setLayout(controlOverlayLayout);
            add(controlGlass);
            add(controlManifestation);
        }
        
        GlassPanel getGlassPanel() {
            return controlGlass;
        }
    }
    
    static class GlassPanelRepaintManager extends RepaintManager {
        
        private static Map<WeakReference<JComponent>, WeakReference<JComponent>> repaintPairs = new HashMap<WeakReference<JComponent>, WeakReference<JComponent>>();
        private static final GlassPanelRepaintManager manager = new GlassPanelRepaintManager(); 
        
        GlassPanelRepaintManager() {}
        
        public static GlassPanelRepaintManager getInstance() {
            return manager;
        }
        
        public static void registerRepaintPair(JComponent mainPane, JComponent overlayPane) {
            Iterator<WeakReference<JComponent>> iterator = repaintPairs.keySet().iterator();
            while (iterator.hasNext()) {
                WeakReference<JComponent> ref = iterator.next();
                JComponent pane = ref.get();
                if (pane != null) {
                    if (SwingUtilities.isDescendingFrom(mainPane, pane))
                        return;
                    if (SwingUtilities.isDescendingFrom(pane, mainPane))
                        iterator.remove();
                }
            }
            repaintPairs.put(new WeakReference<JComponent>(mainPane), new WeakReference<JComponent>(overlayPane));
        }
        
        @Override
        public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            ControlWrapper controlWrapper = (ControlWrapper) SwingUtilities.getAncestorOfClass(ControlWrapper.class, c);
            if (controlWrapper != null) {
                Rectangle bounds = controlWrapper.controlGlass.getBounds();
                addDirtyRegionToSuperRepaintManager(controlWrapper.controlGlass, bounds.x, bounds.y, (int) bounds.getWidth(), (int) bounds.getHeight());
                return;
            }

            int overlayRegionsCount = 0;
            Iterator<WeakReference<JComponent>> iterator = repaintPairs.keySet().iterator();            
            while (iterator.hasNext()) {
                WeakReference<JComponent> mainPaneRef = iterator.next();
                JComponent mainPane = mainPaneRef.get();
                if (mainPane == null) {
                    iterator.remove();
                    continue;
                }
                                
                if (SwingUtilities.isDescendingFrom(c, mainPane)) {
                    JComponent overlayPane = repaintPairs.get(mainPaneRef).get();
                    Rectangle bounds = overlayPane.getBounds();
                    addDirtyRegionToSuperRepaintManager(overlayPane, bounds.x, bounds.y, (int) bounds.getWidth(), (int) bounds.getHeight());
                    overlayRegionsCount++;
                }
            }            
            if (overlayRegionsCount > 0)
                return;
            
            addDirtyRegionToSuperRepaintManager(c, x, y, w, h);
        }
        
        void addDirtyRegionToSuperRepaintManager(JComponent c, int x, int y, int w, int h) {
            super.addDirtyRegion(c, x, y, w, h);
        }
        
    }
    
    /**
     * Returns the view properties of this view type.
     * @return non-null <code>ExtendedProperties</code>
     */
    public ExtendedProperties getViewProperties() {
        String viewType = getInfo().getType();
        ComponentInitializer capability = getManifestedComponent().getCapability(ComponentInitializer.class);
        ExtendedProperties props = capability.getViewRoleProperties(viewType);
        if (props == null) {
            props = new ExtendedProperties();
            capability.setViewRoleProperty(viewType, props);            
        }
        return props;
    }
    
    /**
     * Fires property change event to registered listeners upon view stale.
     * @param isStale true if view is stale; false otherwise.
     */
    public final void notifyStaleState(boolean isStale) {
        firePropertyChange(VIEW_STALE_PROPERTY, null, isStale);
    }
}
