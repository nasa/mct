package gov.nasa.arc.mct.platform;
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
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.impl.MenuExtensionManager;
import gov.nasa.arc.mct.gui.impl.StatusAreaWidgetRegistryImpl;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntime;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntimeImpl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.search.SearchServiceImpl;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.internal.component.User;
import gov.nasa.arc.mct.util.LookAndFeelSettings;
import gov.nasa.arc.mct.util.exception.MCTRuntimeException;
import gov.nasa.arc.mct.util.logging.MCTLogger;
import gov.nasa.arc.mct.util.property.MCTProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class LaunchMCTService {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("DefaultIdentityManager"); //NOI18N    
    private static final MCTLogger logger = MCTLogger.getLogger(LaunchMCTService.class);
    private final Collection<ServiceReference> allProviders = new ArrayList<ServiceReference>();
    private boolean componentProvidersChanged = false;
    private boolean requiresRefresh = false;
    private boolean pluginsLoaded = false;
    private boolean haveInitialized = false;
    private Timer t;
            
    public void bind(Object provider) {
        // do nothing as this signals launch can proceed
        // (used to declare dependencies to persistence, default component provider)
    }
    
    public void unbind(Object provider) {
        
    }
    
    private synchronized void notifyPlatformServicesChanged() {
        List<ExtendedComponentProvider> providers = new ArrayList<ExtendedComponentProvider>(allProviders.size());
        for (ServiceReference sr:allProviders) {
            ComponentProvider cp = (ComponentProvider) sr.getBundle().getBundleContext().getService(sr);
            providers.add(new ExtendedComponentProvider(cp, sr.getBundle().getSymbolicName()));
        }

        ExternalComponentRegistryImpl.getInstance().refreshComponents(providers);
        MenuExtensionManager.getInstance().refreshExtendedMenus(providers);
        PolicyManagerImpl.getInstance().refreshExtendedPolicies(providers);
        ProviderDelegateServiceImpl.getInstance().refresh(providers);
        StatusAreaWidgetRegistryImpl.getInstance().refresh(providers);
        SearchServiceImpl.getInstance().refresh(providers);
        componentProvidersChanged = true;
        
        if (t == null && pluginsLoaded) {
            t = new Timer("Last Component Provider Loaded Checker", true);
            t.scheduleAtFixedRate(new TimerTask() {
             
                 @Override
                 public void run() {
                     synchronized(LaunchMCTService.this) {
                         if (componentProvidersChanged) {
                             componentProvidersChanged = false;
                             // wait until the next time to see if changes have stopped
                         } else {
                             requiresRefresh = true;
                             t.cancel();
                             t = null;
                             if (!haveInitialized) { // Only init once
                                 haveInitialized = true;
                                 
                                 SwingUtilities.invokeLater(new Runnable() {
                                     @Override
                                     public void run() {
                                         initUserInterface();
                                     }
                                 });
                             }
                         }
                     }
                 }
                 
            }, 5000, 1000);
        }
        
        if (requiresRefresh) {
            refreshUI();
        }
    }
    
    private void refreshUI() {
        GlobalContext.getGlobalContext().switchUser(GlobalContext.getGlobalContext().getUser(), new Runnable() {
            public void run() {
                PlatformImpl.getInstance().getWindowManager().refreshWindows();
            }
        });
    }
    
    public synchronized void componentProviderAdded(ServiceReference cp) {
        allProviders.add(cp);
        notifyPlatformServicesChanged();
    }
    
    public synchronized void componentProviderRemoved(ServiceReference cp) {
        allProviders.remove(cp);
        notifyPlatformServicesChanged();
    }
    
    public synchronized void activate(BundleContext context) {
        try {
            loadUser();
            pluginsLoaded = true;
            startOptionalModules();
        } catch (Exception e) {
            logger.error("unable to start MCT, exception thrown", e);
            System.exit(0);
        }
    }
    
    public void deactivate(BundleContext context) {
        
    }
    
    private synchronized void initUserInterface() {
        // For now, make the platform provide the window decorations,
        // specifically for Linux. Using false here will use the platform
        // style, with solid background color and platform-standard window
        // controls.
        // This method has to be invoked before a JFrame is instantiated, and
        // it stays in effect for all JFrames until the method is called again.
        JFrame.setDefaultLookAndFeelDecorated(false);

        // Initialize the Look and Feel Setting object to the value in
        // mct.properties file, or else use the default LAF.
        String lookAndFeelStr = MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty("mct.look.and.feel");
        LookAndFeelSettings.INSTANCE.setLAF(lookAndFeelStr);
        if (! LookAndFeelSettings.INSTANCE.isInitialized()) {
            logger.error("Could not initialize the Swing Look and Feel settings, MCT is closing.");
            System.exit(1);
        }

        initUserEnvironment();
        requiresRefresh = true;
    }
    
    private void initUserEnvironment() {
        new UserEnvironment();
    }
    
    private void startOptionalModules() {
        OSGIRuntime osgiRuntime = OSGIRuntimeImpl.getOSGIRuntime();
        osgiRuntime.startExternalBundles();
    }
    
    private boolean loadUser() {
        // Get the current user name (may be null)
        final String whoami = GlobalContext.getGlobalContext().getIdManager().getCurrentUser();
        
        // Try to load the user from persistence, if username was non-null
        User currentUser = whoami != null ? PlatformAccess.getPlatform().getPersistenceProvider().getUser(whoami) : null;
        
        // If no user was found (either in DB or simply because mct.user was unspecified) consider creating one.
        if (currentUser == null) {
            final String ADD_USER_PROP = "automatically.add.user";
            final String DEFAULT_GROUP_PROP = "default.user.group";
            if (Boolean.parseBoolean(System.getProperty(ADD_USER_PROP, MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty(ADD_USER_PROP, "false")))) {
                // Default to "testUser1" if no user name was ever specified
                String userId = whoami != null ? whoami : bundle.getString("DEFAULT_USER"); //NOI18N  
                Platform platform = PlatformAccess.getPlatform();
                
                // determine if the platform has been initialized
                if (platform.getBootstrapComponents().isEmpty()) {
                    GlobalContext.getGlobalContext().switchUser(new User() {
                        @Override
                        public String getDisciplineId() {
                            return null;
                        }
                        
                        @Override
                        public User getValidUser(String userID) {
                            return null;
                        }
                        
                        @Override
                        public String getUserId() {
                            return whoami;
                        }
                    }, null);
                    platform.getDefaultComponentProvider().createDefaultComponents();
                    // invoke getting default components again to ensure the platform has been bootstrapped
                    platform.getBootstrapComponents();
                }
                
                AbstractComponent mySandbox = platform.getDefaultComponentProvider().createSandbox(userId);
                AbstractComponent dropbox = platform.getDefaultComponentProvider().createDropbox(userId);
                
                String group = System.getProperty(DEFAULT_GROUP_PROP, MCTProperties.DEFAULT_MCT_PROPERTIES.getProperty(DEFAULT_GROUP_PROP, ""));
                if (group.isEmpty()) {
                    throw new MCTRuntimeException("Default group not specified, set the default group in mct.properties using the " + DEFAULT_GROUP_PROP + " property.");
                }
                platform.getPersistenceProvider().addNewUser(userId, group, mySandbox, dropbox);
                currentUser = platform.getPersistenceProvider().getUser(userId);
            } else {
                throw new MCTRuntimeException("MCT user '" + whoami
                        + "' is not in the MCT database. You can load MCT user(s) using MCT's load user tool.");
            }
        }
        GlobalContext.getGlobalContext().switchUser(currentUser, null);
        return true;
    }
}
