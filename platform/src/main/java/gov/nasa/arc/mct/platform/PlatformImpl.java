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

import gov.nasa.arc.mct.api.feed.FeedAggregator;
import gov.nasa.arc.mct.api.feed.FeedDataArchive;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.MenuExtensionManager;
import gov.nasa.arc.mct.gui.WindowManagerImpl;
import gov.nasa.arc.mct.osgi.platform.EquinoxOSGIRuntimeImpl;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntime;
import gov.nasa.arc.mct.platform.spi.DefaultComponentProvider;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.SubscriptionManager;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policymgr.PolicyManagerImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.FeedManager;
import gov.nasa.arc.mct.services.component.MenuManager;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ProviderDelegateService;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Provides the MCT Platform which supports the MCT API set. The platform is intended to decouple the MCT APIs
 * from the implementation. This allows for alternate implementations of the platform to be supplied, but the
 * intention is to loosely couple the APIs and their implementation. 
 *
 */
public class PlatformImpl implements Platform {

    private static final PlatformImpl INSTANCE = new PlatformImpl();
    
    private Map<Object, Set<ServiceRegistration>> serviceRegistrations = new HashMap<Object, Set<ServiceRegistration>>();
    
    private final RootComponent rootComponent = new RootComponent();
    
    private AbstractComponent mysanboxComponent; // initialized from the bootstrapping components loaded from the database
    private String userDropboxesId;
    
    private PlatformImpl() {}
    
    
    public static PlatformImpl getInstance() {
        return INSTANCE;
    }
    
    @Override
    public WindowManager getWindowManager() {
        return WindowManagerImpl.getInstance();
    }
    
    @Override
    public PersistenceProvider getPersistenceProvider() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(PersistenceProvider.class,null);
    }
    
    @Override
    public CoreComponentRegistry getComponentRegistry() {
        return ExternalComponentRegistryImpl.getInstance();
    }
    
    @Override
    public User getCurrentUser() {
        return GlobalContext.getGlobalContext().getUser();
    }

    @Override
    public PolicyManager getPolicyManager() {
        return PolicyManagerImpl.getInstance();
    }

    @Override
    public DefaultComponentProvider getDefaultComponentProvider() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(DefaultComponentProvider.class,null);
    }

    // This method just calls the default-access method below, passing the OSGi
    // runtime implementation.
    @Override
    public void registerService(Class<?> serviceClass, Object serviceObject, Dictionary<String,Object> props) throws IllegalArgumentException {
        registerService(EquinoxOSGIRuntimeImpl.getOSGIRuntime(), serviceClass, serviceObject, props);
    }
    
    /**
     * Registers a service using a specific OSGi runtime implementation.
     * This method is called by unit tests, to mock the OSGi implementation.
     * 
     * @param osgiRuntime the OSGi runtime to use
     * @param serviceClass the class under which name the service should be registered
     * @param serviceObject the object providing the service
     * @param props the properties for the new service
     * @throws IllegalArgumentException if the service object does not implement the service class
     */
    synchronized void registerService(OSGIRuntime osgiRuntime, Class<?> serviceClass, Object serviceObject, Dictionary<String,Object> props) throws IllegalArgumentException {
        if (!serviceClass.isInstance(serviceObject)) {
            throw new IllegalArgumentException("Service object is not instance of service class " + serviceClass.getName());
        }
        BundleContext bc = osgiRuntime.getBundleContext();
        ServiceRegistration reg = bc.registerService(serviceClass.getName(), serviceObject, props);
        Set<ServiceRegistration> existingRegistrations = serviceRegistrations.get(serviceObject);
        if (existingRegistrations == null) {
            existingRegistrations = new HashSet<ServiceRegistration>();
            serviceRegistrations.put(serviceObject, existingRegistrations);
        }
        existingRegistrations.add(reg);
    }

    @Override
    public synchronized void unregisterService(Object serviceObject) {
        Set<ServiceRegistration> existingRegistrations = serviceRegistrations.get(serviceObject);
        for (ServiceRegistration reg : existingRegistrations) {
            reg.unregister();
        }
        serviceRegistrations.remove(serviceObject);
    }
    
    @Override
    public SubscriptionManager getSubscriptionManager() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(SubscriptionManager.class, null);
    }
    
    @Override
    public TimeService getTimeService() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(TimeService.class, null);
    }

    @Override
    public MenuManager getMenuManager() {        
        return MenuExtensionManager.getInstance();
    }

    @Override
    public ProviderDelegateService getProviderDelegateService() {
        return ProviderDelegateServiceImpl.getInstance();
    }

    @Override
    public FeedAggregator getFeedAggregator() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(FeedAggregator.class, null);
    }


    @Override
    public AbstractComponent getRootComponent() {
        return rootComponent;
    }


    @Override
    public List<AbstractComponent> getBootstrapComponents() {
        // Inject root and My Sandbox components to GlobalComponentRegistry
        List<AbstractComponent> bootstrapComponents = getPersistenceProvider().getBootstrapComponents();
        for (AbstractComponent ac : bootstrapComponents) {
            // Should introduce a property indicating an AbstractComponent to be My Sandbox.
            // Assuming that there's only one My Sandbox among the bootstrap components. 
            if ("gov.nasa.arc.mct.core.components.MineTaxonomyComponent".equals(ac.getComponentTypeID())) {
                mysanboxComponent = ac;
            }
            if ("/UserDropBoxes".equals(ac.getExternalKey())) {
                userDropboxesId = ac.getComponentId();
            }
        }
        return bootstrapComponents;
    }


    @Override
    public AbstractComponent getMySandbox() {
        return mysanboxComponent;
    }


    @Override
    public AbstractComponent getUserDropboxes() {
        return userDropboxesId == null ? null : getPersistenceProvider().getComponent(userDropboxesId);
    }
    
    /**
     * Gets the OSGi FeedManager reference.
     * @return FeedManager reference.
     */
    public FeedManager getFeedManager() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(FeedManager.class, null);
    }
    
    /**
     * Gets the OSGi FeedDataArchive reference.
     * @return FeedDataArchive reference.
     */
    public FeedDataArchive getFeedDataArchive() {
        OSGIRuntime osgiRuntime = EquinoxOSGIRuntimeImpl.getOSGIRuntime();
        return osgiRuntime.getService(FeedDataArchive.class, null);
    }
}
