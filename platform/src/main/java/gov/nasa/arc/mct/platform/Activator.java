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
package gov.nasa.arc.mct.platform;

import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.defaults.view.DefaultViewProvider;
import gov.nasa.arc.mct.exception.DefaultExceptionHandler;
import gov.nasa.arc.mct.gui.FeedManagerImpl;
import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.impl.MenuExtensionManager;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
import gov.nasa.arc.mct.identitymgr.impl.IdentityManagerFactory;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntimeImpl;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.services.component.FeedManager;
import gov.nasa.arc.mct.services.component.MenuManager;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.util.exception.MCTException;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    private static final MCTLogger logger = MCTLogger.getLogger(Activator.class);
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(Activator.class.getName());
    final Timer t = new Timer("MCT Launch check timer", true);    
    
    @Override
    public void start(final BundleContext context) {
        DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler);
               
        // wait one minute and then check to see if a PersistenceProvider has been installed
        t.schedule(new TimerTask() {
            private boolean confirmed = false;
            
            @Override
            public void run() {
                if (context.getServiceReference(PersistenceProvider.class.getName()) == null) {
                    WindowManager windowManager = getWindowManager();
                    Map<String, Object> hints = new HashMap<String, Object>();
                    
                    if (!confirmed) {
                        logger.warn("unable to obtain persistence provider");

                        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_NO_OPTION);
                        
                        String[] options = { 
                                BUNDLE.getString("persistence_warning_ok"), 
                                BUNDLE.getString("persistence_warning_cancel") 
                                };
                        String result = windowManager.showInputDialog(
                                BUNDLE.getString("persistence_warning_title"), 
                                BUNDLE.getString("persistence_warning_message"), 
                                options, 
                                options[0], 
                                hints);
                        if (result.equals(options[1])) {
                            System.exit(0);
                        } else {                        
                            confirmed = true;
                        }
                    } else {
                        logger.error("unable to obtain persistence provider");
                        
                        hints.put(WindowManagerImpl.OPTION_TYPE, OptionBox.YES_OPTION);
                        
                        String[] options = { 
                                BUNDLE.getString("persistence_error_ok"), 
                                };
                        windowManager.showInputDialog(
                                BUNDLE.getString("persistence_error_title"), 
                                BUNDLE.getString("persistence_error_message"), 
                                options, 
                                options[0], 
                                hints);
                        
                        System.exit(0);
                    }
                }
            }
            
            private WindowManager getWindowManager() {
                // Try to use the Platform's version, in case something other 
                // than WindowManagerImpl has been injected somewhere.
                Platform platform = PlatformAccess.getPlatform();
                if (platform != null) {
                    WindowManager windowManager = platform.getWindowManager();
                    if (windowManager != null) {
                        return windowManager;
                    }
                }
                // Otherwise, fall back to this bundle's version.
                return WindowManagerImpl.getInstance();
            }
        }, 20000, 40000); // Warn after twenty seconds, kill after one minute        

        initServicesAndHandlers(context);
    }
    
    private void initServicesAndHandlers(BundleContext bc) {
        @SuppressWarnings("rawtypes")
        Dictionary d = new Properties();
        PlatformImpl.getInstance().setBundleContext(bc);
        (new PlatformAccess()).setPlatform(PlatformImpl.getInstance());
        try {
            GlobalContext.getGlobalContext().setIdManager(IdentityManagerFactory.newIdentityManager(null));
        } catch (MCTException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        OSGIRuntimeImpl.getOSGIRuntime().setBundleContext(bc);
        bc.registerService(new String[] { gov.nasa.arc.mct.services.component.ComponentRegistry.class
                .getName() }, ExternalComponentRegistryImpl.getInstance(), d);

        bc.registerService(new String[] { PolicyManager.class.getName() }, PolicyManagerImpl.getInstance(),d);
        bc.registerService(new String[] { FeedManager.class.getName() }, FeedManagerImpl.getInstance(),d);
        bc.registerService(new String[] { MenuManager.class.getName() }, MenuExtensionManager.getInstance(),d);
        bc.registerService(new String[] { Platform.class.getName() }, PlatformImpl.getInstance(),d);

        ExternalComponentRegistryImpl.getInstance().setDefaultViewProvider(new DefaultViewProvider());
    }

    @Override
    public void stop(final BundleContext bc) throws Exception {
        // Once the platform bundle stops, we expect MCT shut down.
        // Timer is used to make sure all bundles have been stopped,
        // then System.exit() is invoked to ensure that any leaked 
        // background threads keep the process alive.
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Don't exit if any bundles have no completely stopped
                    for (Bundle b : bc.getBundles()) {
                        switch (b.getState()) {
                        case Bundle.ACTIVE:
                        case Bundle.STARTING:
                        case Bundle.STOPPING:
                            return;
                        }
                    }
                } catch (IllegalStateException ise) {
                    // This just implies that bundle shutdown was already complete
                }
                System.exit(0);
            }            
        }, 0, 200);        
    }

}