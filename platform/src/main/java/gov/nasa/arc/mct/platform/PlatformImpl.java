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
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.gui.impl.MenuExtensionManager;
import gov.nasa.arc.mct.gui.impl.WindowManagerImpl;
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Provides the MCT Platform which supports the MCT API set. The platform is intended to decouple the MCT APIs
 * from the implementation. This allows for alternate implementations of the platform to be supplied, but the
 * intention is to loosely couple the APIs and their implementation. 
 *
 */
public class PlatformImpl implements Platform {

    private static final PlatformImpl INSTANCE = new PlatformImpl();
    
    private final RootComponent rootComponent = new RootComponent();
    
    private AbstractComponent mysanboxComponent; // initialized from the bootstrapping components loaded from the database
    private String userDropboxesId;
    private final AtomicReference<BundleContext> bundleContext = new AtomicReference<BundleContext>();
    
    private PlatformImpl() {}
    
    
    public static PlatformImpl getInstance() {
        return INSTANCE;
    }
    
    public void setBundleContext(BundleContext bc) {
        bundleContext.set(bc);
    }
    
    @Override
    public WindowManager getWindowManager() {
        return WindowManagerImpl.getInstance();
    }
    
    @Override
    public PersistenceProvider getPersistenceProvider() {
        return getService(PersistenceProvider.class,null);
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

    public <T> T getService(Class<T> serviceClass, String filter) {
        BundleContext bc = bundleContext.get();
        if (bc == null) { return null; }
        ServiceReference[] srs;
        try {
            srs = bc.getServiceReferences(serviceClass.getName(), filter);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }

        if (srs != null && srs.length > 0) {
            ServiceReference sr = srs[0];
            return serviceClass.cast(bc.getService(sr));

        }
        return null;
    }
    
    
    @Override
    public DefaultComponentProvider getDefaultComponentProvider() {
        return getService(DefaultComponentProvider.class,null);
    }

    @Override
    public SubscriptionManager getSubscriptionManager() {
        return getService(SubscriptionManager.class, null);
    }
    
    @Override
    public TimeService getTimeService() {
        return getService(TimeService.class, null);
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
        return getService(FeedAggregator.class, null);
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
        return getService(FeedManager.class, null);
    }
    
    /**
     * Gets the OSGi FeedDataArchive reference.
     * @return FeedDataArchive reference.
     */
    public FeedDataArchive getFeedDataArchive() {
        return getService(FeedDataArchive.class, null);
    }
    
    /**
     * Refreshes all the MCT housing content.
     */
    @Override
    public void refreshAllMCTHousedContent() {
        Collection<MCTAbstractHousing> housings = UserEnvironmentRegistry.getAllHousings();
        for (MCTAbstractHousing housing : housings) {
            String title = housing.getTitle();
            housing.refreshHousedContent();
            housing.setTitle(title);
        }
    }
}
