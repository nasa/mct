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
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.housing.MCTHousingFactory;
import gov.nasa.arc.mct.gui.housing.MCTStandardHousing;
import gov.nasa.arc.mct.gui.housing.StatusBarContentProvider;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.menu.MenuFactory;
import gov.nasa.arc.mct.gui.util.GUIUtil;
import gov.nasa.arc.mct.platform.RootComponent;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a window manager. This is the platform default window manager. It
 * is not designed to be subclassed by component developers. Uses a singleton
 * pattern to provide only a single, shared instance.
 */
public class WindowManagerImpl implements WindowManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WindowManagerImpl.class);
    private static final WindowManagerImpl INSTANCE = new WindowManagerImpl();

    // Scaling factors used to size child window relative to parent window.
    static final double LEAF_HORIZONTAL_SCALE = 0.5;
    static final double LEAF_VERTICAL_SCALE = 0.5;
    static final double NON_LEAF_HORIZONTAL_SCALE = 0.7;
    static final double NON_LEAF_VERTICAL_SCALE = 0.7;
    private static final double MAX_SCALE_FACTOR = 0.85;

    private static Icon mctIcon = new ImageIcon(WindowManagerImpl.class.getResource("/images/mcticon.png"));
    
    /**
     * Creates a new instance of the window manager. Protected, to enforce the
     * singleton pattern.
     */
    protected WindowManagerImpl() {
    }

    /**
     * Gets the singleton instance of the window manager.
     * 
     * @return the window manager
     */
    public static WindowManagerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void openInNewWindow(AbstractComponent component) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
        openInNewWindow(component, graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration());
    }

    
    // For Multiple Monitor Support with GraphicsConfiguration
    @Override
    public void openInNewWindow(AbstractComponent component, GraphicsConfiguration graphicsConfig) {
        
        assert component != null : "component should not be null";
        // we do not know which window this should be relative to, so find the
        // active window in the current
        // set of windows
        Window[] allWindows = Window.getWindows();
        assert allWindows != null;
        Window activeWindow = null;
        for (Window window : allWindows) {
            if (window.isActive()) {
                activeWindow = window;
                break;
            }
        }
        
        Set<ViewInfo> views = component.getViewInfos(ViewType.NODE);

        if (views.isEmpty()) {
            MCTLogger.getLogger(getClass()).warn("component " + component.getId() + "does not have a Node View");
            return;
        }

        ViewInfo nodeView = views.iterator().next();

        // Determine the desired horizontal/vertical scaling of the new window.
        double horizontalScale = 0;
        double verticalScale = 0;

        if (component.isLeaf()) {
            horizontalScale = LEAF_HORIZONTAL_SCALE;
            verticalScale = LEAF_VERTICAL_SCALE;
        } else {
            // !isLeaf()
            horizontalScale = NON_LEAF_HORIZONTAL_SCALE;
            verticalScale = NON_LEAF_VERTICAL_SCALE;
        }

        Window newActiveWindowWithGraphicsConfig = new Window(activeWindow, graphicsConfig);
        openInWindow(component.getDisplayName(), nodeView, newActiveWindowWithGraphicsConfig, horizontalScale, verticalScale, component);
    }
    
    /**
     * Opens a component into a new window.
     * 
     * @param displayName
     *            the display name for the new housing
     * @param nodeView
     *            the node view role of the component we will open in the new
     *            housing
     * @param activeWindow
     *            the current active window
     * @param horizontalScale
     *            the desired horizontal scaling of the window
     * @param verticalScale
     *            the desired vertical scaling of the window
     * @param component
     *            the component to open in the window
     */
    protected void openInWindow(String displayName, ViewInfo nodeView, Window activeWindow, double horizontalScale,
            double verticalScale, AbstractComponent component) {

        MCTHousing housing = null;
        if (component instanceof RootComponent) {

            // THIS Object menu open new user environment
            housing = MCTHousingFactory.newUserEnvironment();             
            
        } else if (component.isLeaf()) {
            Set<ViewInfo> possibleViews = new LinkedHashSet<ViewInfo>(component.getViewInfos(ViewType.CENTER));
            possibleViews.addAll(component.getViewInfos(ViewType.OBJECT));
            housing = MCTHousingFactory.newHousing(displayName,
                    (byte) (MCTHousingFactory.CONTROL_AREA_ENABLE
                            | MCTHousingFactory.CONTENT_AREA_ENABLE | MCTHousingFactory.STATUS_AREA_ENABLE),
                    JFrame.DO_NOTHING_ON_CLOSE, possibleViews.iterator().next().createView(component),
                    horizontalScale, verticalScale, activeWindow);
        } else {
            housing = MCTHousingFactory.newHousing(displayName, MCTHousingFactory.ENABLE_ALL_AREA,
                    JFrame.DO_NOTHING_ON_CLOSE, GUIUtil.cloneTreeNode(component, nodeView), false, horizontalScale, verticalScale,
                    activeWindow);
        }
        ((MCTAbstractHousing) housing).setJMenuBar(MenuFactory.createStandardHousingMenuBar((MCTStandardHousing) housing));
        new StatusBarContentProvider(housing);
        if (housing.getContentArea() != null && !housing.getContentArea().isAreaEmpty()) {
            // use preferred size since the content area is going to be the dominate focus
            MCTAbstractHousing abstractHousing = ((MCTAbstractHousing) housing);
            abstractHousing.pack();
            Rectangle maximumWindowBounds = abstractHousing.getGraphicsConfiguration() != null ? abstractHousing.getGraphicsConfiguration().getBounds() :
                                            GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            abstractHousing.setSize(Math.min((int) (maximumWindowBounds.width*MAX_SCALE_FACTOR), abstractHousing.getSize().width),
                            Math.min((int) (maximumWindowBounds.height*MAX_SCALE_FACTOR), abstractHousing.getSize().height));
            housing.getContentArea().getHousedViewManifestation().requestFocusInWindow();
        
        } 
        
        if (activeWindow != null) {
            Rectangle activeWindowGraphicsConfigBound = activeWindow.getGraphicsConfiguration().getBounds();
            ((MCTAbstractHousing) housing).setLocation(activeWindowGraphicsConfigBound.x, activeWindowGraphicsConfigBound.y);
        } else {
            logger.warn("ActiveWindow is NULL because it's not detected during 1st time MCT window opening.");
        }
        
        ((MCTAbstractHousing) housing).setVisible(true);
    }

    @Override
    public AbstractComponent getWindowRootComponent(Component component) {
        MCTHousing housing = (MCTHousing) SwingUtilities.getAncestorOfClass(MCTHousing.class, component);
        if (housing != null) {
            return housing.getWindowComponent();
        }
        return null;
    }

    @Override
    public View getWindowRootManifestation(Component component) {
        MCTHousing housing = (MCTHousing) SwingUtilities.getAncestorOfClass(MCTHousing.class, component);
        if (housing != null) {
            return housing.getHousedViewManifestation();
        }
        return null;
    }

    /**
     * Return the active windows, those which currently can be displayed (have a graphics context). This ensures windows which
     * have been disposed but not yet garbage collected will not be returned. This prevents the problem
     * where dispose has been called but the Garbage Collector has not been run so the window is returned
     * from Window.getWindows(). This causes problems where algorithms assume that Windows.getWindows only returns
     * windows which are really visible. 
     * @return
     */
    private Window[] getActiveWindows() {
        List<Window> activeWindows = new ArrayList<Window>(Arrays.asList(Window.getWindows()));
        Iterator<Window> it = activeWindows.iterator();
        while (it.hasNext()) {
            Window w = it.next();
            boolean hasWindowBeenDisposed = w.getGraphics() == null; // this is true if dispose has been called as the 
            // graphics context has been removed
            if (hasWindowBeenDisposed) {
                it.remove();
            }
        }
        
        return activeWindows.toArray(new Window[activeWindows.size()]);
    }
    
    @Override
    public void refreshWindows() {
        Window[] windows = getActiveWindows();
        for (Window window : windows) {
            if (MCTHousing.class.isAssignableFrom(window.getClass())) {
                MCTHousing.class.cast(window).reloadHousedContent();
            } else {
                window.dispose();
            }
        }
    }

    @Override
    public void closeWindows(String componentId) {
        Window[] windows = getActiveWindows();
        for (Window window : windows) {
            if (MCTAbstractHousing.class.isAssignableFrom(window.getClass())) {
                MCTAbstractHousing housing = MCTAbstractHousing.class.cast(window);
                View housedManifestation = housing.getHousedViewManifestation();
                if (housedManifestation != null && componentId.equals(housedManifestation.getManifestedComponent().getId())) {
                    UserEnvironmentRegistry.removeHousing(housing);
                    housing.dispose();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T showInputDialog(String title, String message, T[] options, T defaultOption) {
        return (T) JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE, mctIcon, options, defaultOption);
    }
}
