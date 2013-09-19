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
 * MCTStandardHousing.java Aug 18, 2008
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.gui.housing;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.defaults.view.MCTHousingViewManifestation;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.TwiddleView;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewProvider;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntime;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntimeImpl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.util.MCTIcons;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.slf4j.LoggerFactory;

/**
 * A panel which contains the standard 3 section housing: a directory area, a
 * content area, and an inspector area.
 * 
 * TODO: Move the tree code elsewhere
 * 
 * @author asi
 * 
 */
@SuppressWarnings("serial")
public class MCTStandardHousing extends MCTAbstractHousing implements TwiddleView {
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    MCTStandardHousing.class.getName().substring(0, 
                            MCTStandardHousing.class.getName().lastIndexOf("."))+".Bundle");
    private static final ResourceBundle SHUTDOWN_BUNDLE = 
            ResourceBundle.getBundle("ShutdownResource"); //NO18N

    private final Map<String, MCTHousingViewManifestation> housedManifestations = new HashMap<String, MCTHousingViewManifestation>();

    private int width;
    private int height;

    private MCTHousingViewManifestation housingViewManifestation;
    private final JPanel displayPanel = new JPanel(new GridLayout());
    private SelectionProvider selectionProvider;
    private final List<WeakReference<ControlProvider>> controlAreas = new ArrayList<WeakReference<ControlProvider>>();

    public MCTStandardHousing(int width, int height, int closeAction, View housingView) {
        this(GlobalContext.getGlobalContext().getUser().getUserId() + "'s User Environment", width, height,
                closeAction, housingView);
    }

    public MCTStandardHousing(GraphicsConfiguration gc, String title, int width, int height, int closeAction, 
            View housingView) {
        super(gc, housingView.getManifestedComponent().getId());
        
        getContentPane().setLayout(new GridLayout());
        this.width = width;
        this.height = height;
        setSize(this.width, this.height);
        displayPanel.setSize(this.width, this.height);
        setTitle(title);
        setIcon(housingView.getManifestedComponent().getAsset(ImageIcon.class));
        setDefaultCloseOperation(closeAction);
        MCTHousingViewManifestation housingManifestation = (MCTHousingViewManifestation) housingView;
        setHousingViewManifesation(housingManifestation);
        addWindowListenerToHousing();
        getContentPane().add(displayPanel);
    }
    
    public MCTStandardHousing(String title, int width, int height, int closeAction, 
            View housingView) {
        super(housingView.getManifestedComponent().getId());

        getContentPane().setLayout(new GridLayout());
        this.width = width;
        this.height = height;
        setSize(this.width, this.height);
        displayPanel.setSize(this.width, this.height);
        setTitle(title);
        setIcon(housingView.getManifestedComponent().getAsset(ImageIcon.class));
        setDefaultCloseOperation(closeAction);
        MCTHousingViewManifestation housingManifestation = (MCTHousingViewManifestation) housingView;
        setHousingViewManifesation(housingManifestation);
        addWindowListenerToHousing();
        getContentPane().add(displayPanel);
    }
    
    private void setIcon(ImageIcon icon) {
        icon = MCTIcons.processIcon(icon, new Color(230,230,230), true);
        if (icon != null) {
            setIconImage(icon.getImage());
        }
    }

    @Override
    public SelectionProvider getSelectionProvider() {
        return selectionProvider;
    }

    public MCTHousingViewManifestation showHousedManifestationIfPresent(String manifestedType) {
        MCTHousingViewManifestation targetViewManifestation = this.housedManifestations.get(manifestedType);
        if (targetViewManifestation != null) {
            showHousedViewManifestation(manifestedType);
        }
        return targetViewManifestation;
    }

    /**
     * Swaps housing view manifestation.
     * 
     * @param housingViewManifestation
     */
    public void setHousingViewManifesation(MCTHousingViewManifestation housingViewManifestation) {
        String viewName = housingViewManifestation.getInfo().getViewName();
        housingViewManifestation.setParentHousing(this);
        housingViewManifestation.setVisible(true);
        housedManifestations.put(viewName, housingViewManifestation);
        showHousedViewManifestation(viewName);
        selectionProvider = housingViewManifestation.getSelectionProvider();
        if (selectionProvider == null) {
            throw new IllegalArgumentException("housing view role " + housingViewManifestation.getClass().getName()
                    + " must not return null from getSelectionProvider");
        }
    }

    private void showHousedViewManifestation(String manifestedType) {
        MCTHousingViewManifestation displayedViewManifestation = housedManifestations.get(manifestedType);
        displayPanel.removeAll();
        this.housingViewManifestation = displayedViewManifestation;
        displayPanel.add(this.housingViewManifestation);
        displayPanel.revalidate();
    }

    private void addWindowListenerToHousing() {
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                    if (UserEnvironmentRegistry.getHousingCount() == 1) {
                        // Pop up a confirmation dialog if this is the last window
                        Object[] options = { SHUTDOWN_BUNDLE.getString("OK"), SHUTDOWN_BUNDLE.getString("CANCEL") }; //NO18N  

                        Map<String, Object> hints = new HashMap<String, Object>();
                        hints.put(WindowManagerImpl.MESSAGE_TYPE, OptionBox.WARNING_MESSAGE);
                        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
                        hints.put(WindowManagerImpl.PARENT_COMPONENT, (MCTAbstractHousing) UserEnvironmentRegistry.getActiveHousing());
                        Object response = PlatformAccess.getPlatform().getWindowManager().showInputDialog(
                                SHUTDOWN_BUNDLE.getString("TITLE"),   //NOI18N
                                SHUTDOWN_BUNDLE.getString("MESSAGE"), //NOI18N
                                options,
                                options[0],
                                hints);
                        
                        if (response == options[0]) { // "OK"
                            OSGIRuntime osgiRuntime = OSGIRuntimeImpl.getOSGIRuntime();
                            try {
                                osgiRuntime.stopOSGI();
                            } catch (Exception e1) {
                                LoggerFactory.getLogger(MCTStandardHousing.class).warn(e1.getMessage(), e1);
                            }
                            disposeHousing();
                        }
                        
                    } else {                        
                        closeHousing();
                    }
                }
            


        });

    }
       

    public void buildGUI() {
        this.housingViewManifestation.buildGUI();
    }

    public void setControlArea(MCTControlArea controlArea) {
        this.housingViewManifestation.setControlArea(controlArea);
    }

    public void setControlAreaVisible(boolean flag) {
        if (this.housingViewManifestation != null)
            this.housingViewManifestation.setControlAreaVisible(flag);
    }

    public boolean isControlAreaVisible() {
        boolean visible = this.housingViewManifestation.isControlAreaVisible();
        for (WeakReference<ControlProvider> controlArea : controlAreas) {
            ControlProvider cp = controlArea.get();
            if (cp != null) {
                visible |= cp.isControlShowing();
            }
        }
        return visible;
    }

    public void setDirectoryArea(View directoryArea) {
        if (directoryArea instanceof ControlProvider) {
            addControlArea((ControlProvider)directoryArea);
        }
        this.housingViewManifestation.setDirectoryArea(directoryArea);
    }

    public void setContentArea(MCTContentArea contentArea) {
        addControlArea(contentArea);
        this.housingViewManifestation.setContentArea(contentArea);
    }

    public void setInspectionArea(View inspectionArea) {
        if (inspectionArea instanceof ControlProvider) {
            addControlArea((ControlProvider)inspectionArea);
        }
        this.housingViewManifestation.setInspectionArea(inspectionArea);
    }

    @Override
    public View getInspectionArea() {
        return this.housingViewManifestation.getInspectionArea();
    }

    @Override
    public View getDirectoryArea() {
        return this.housingViewManifestation.getDirectoryArea();
    }

    @Override
    public MCTContentArea getContentArea() {
        return this.housingViewManifestation.getContentArea();
    }

    @Override
    public MCTControlArea getControlArea() {
        return this.housingViewManifestation.getControlArea();
    }

    @Override
    public View getCurrentManifestation() {
        return this.housingViewManifestation.getCurrentManifestation();
    }

    public AbstractComponent getWindowComponent() {
        return this.housingViewManifestation.getManifestedComponent();
    }

    @Override
    public View getHousedViewManifestation() {
        return housingViewManifestation;
    }
    
    @Override
    public Collection<ViewProvider> getHousedManifestationProviders() {
        List<ViewProvider> housedProviders = new ArrayList<ViewProvider>();
        
        if (getDirectoryArea() != null) {
            housedProviders.add(getDirectoryArea());
        }
        
        if (getContentArea() != null) {
            housedProviders.add(getContentArea());
        }
        return housedProviders;
    }

    @Override
    public void setStatusArea(MCTStatusArea statusArea) {
        this.housingViewManifestation.setStatusArea(statusArea);
    }

    @Override
    public MCTStatusArea getStatusArea() {
        return this.housingViewManifestation.getStatusArea();
    }
    
    @Override
    public void toggleControlAreas(boolean showing) {
        Iterator<WeakReference<ControlProvider>> it = controlAreas.iterator();
        while (it.hasNext()) {
            ControlProvider cp = it.next().get();
            if (cp == null) {
                it.remove();
            } else {
                cp.showControl(showing);
            }
        }
    }
    
    @Override
    public void addControlArea(ControlProvider provider) {
        controlAreas.add(new WeakReference<ControlProvider>(provider));
    }

    private void disposeHousing() {
        UserEnvironmentRegistry.removeHousing(this);
        dispose();
    }

    @Override
    public void enterTwiddleMode(AbstractComponent twiddledComponent) {
        MCTHousingFactory.refreshHousing(this,twiddledComponent.getViewInfos(ViewType.LAYOUT).iterator().next().createView(twiddledComponent));
    }

    @Override
    public void exitTwiddleMode(AbstractComponent originalComponent) {
        MCTHousingFactory.refreshHousing(this, originalComponent.getViewInfos(ViewType.LAYOUT).iterator().next().createView(originalComponent));
    }
    
    @Override
    public void closeHousing() {
        boolean toCloseWindow = true;
        MCTContentArea centerPane = housingViewManifestation.getContentArea();
        if (centerPane != null) {
            View centerPaneView = centerPane.getHousedViewManifestation();
            AbstractComponent centerComponent = centerPaneView.getManifestedComponent();
            if (!centerComponent.isStale() && centerComponent.isDirty()) {
                toCloseWindow = commitOrAbortPendingChanges(centerPaneView, 
                        MessageFormat.format(BUNDLE.getString("centerpane.modified.alert.text"), 
                                centerPaneView.getInfo().getViewName(), 
                                centerComponent.getDisplayName()));
            }
        }
        View inspectionArea = housingViewManifestation.getInspectionArea();
        if (inspectionArea != null) {
            View inspectorPaneView = inspectionArea.getHousedViewManifestation();
            AbstractComponent inspectorComponent = inspectorPaneView.getManifestedComponent();
            if (!inspectorComponent.isStale() && inspectorComponent.isDirty()) {
                toCloseWindow = commitOrAbortPendingChanges(inspectionArea, 
                            MessageFormat.format(BUNDLE.getString("inspectorpane.modified.alert.text"), 
                                inspectorPaneView.getInfo().getViewName(), 
                                inspectorComponent.getDisplayName()));
            }
        }
        if (toCloseWindow) {
            disposeHousing();
        }
    }
    
    /**
     * Prompts users to commit or abort pending changes in view.
     * @param view the modified view
     * @param dialogMessage the dialog message which differs from where the view is located (in the center or inspector pane)
     * @return true to keep the window open, false to close the window
     */
    private boolean commitOrAbortPendingChanges(View view, String dialogMessage) {
        if (!isComponentWriteableByUser(view.getManifestedComponent()))
            return true; 
        
        Object[] options = {
                BUNDLE.getString("view.modified.alert.save"),
                BUNDLE.getString("view.modified.alert.abort"),
                BUNDLE.getString("view.modified.alert.cancel"),
            };
    
        int answer = OptionBox.showOptionDialog(view,
                dialogMessage,                         
                BUNDLE.getString("view.modified.alert.title"),
                OptionBox.YES_NO_CANCEL_OPTION,
                OptionBox.WARNING_MESSAGE,
                null,
                options, options[0]);
        
        switch (answer) {
        case OptionBox.CANCEL_OPTION:                    
            return false;
        case OptionBox.YES_OPTION:
            PlatformAccess.getPlatform().getPersistenceProvider().persist(Collections.singleton(view.getManifestedComponent()));
            return true;
        default:
            return true;
        }
    }
    
    private boolean isComponentWriteableByUser(AbstractComponent component) {
        Platform p = PlatformAccess.getPlatform();
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), component);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String inspectionKey = PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey();
        return p.getPolicyManager().execute(inspectionKey, policyContext).getStatus();
    }

}
