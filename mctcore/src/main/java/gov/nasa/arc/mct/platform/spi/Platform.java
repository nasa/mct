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
package gov.nasa.arc.mct.platform.spi;


import gov.nasa.arc.mct.api.feed.FeedAggregator;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.MenuManager;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ProviderDelegateService;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.List;

/**
 * The <code>Platform</code> interface represents the support required in the
 * underlying MCT core. This class allows the platform implementation to be 
 * encapsulated and used within external facing API components. An instance of this
 * class will be made available as an OSGi service during runtime. This interface
 * supports the use of delegation instead of extension allowing the API and platform 
 * code to be decoupled. This interface is designed for use only for code within
 * this package and thus is outside the scope of compatibility guarantees for other
 * APIs. 
 * 
 * <em>This class is not intended to be used by component authors</em>
 */
public interface Platform {
    
    /**
     * Returns the root object of all MCT objects.
     * @return root component
     */
    public AbstractComponent getRootComponent();
    
    /**
     * Returns the initial list of components used for bootstrapping MCT. The
     * order of components is provided by a <code>PersistenceProvider</code>.
     * The component "My Sandbox" must be available in the list. My Sandbox is
     * set in the platform after being loaded from <code>PersistenceProvider</code>.
     * @return list of bootstrapping components
     */
    public List<AbstractComponent> getBootstrapComponents();
    
    /**
     * Returns the object that represnts My Sandbox.
     * @return My Sandbox component
     */
    public AbstractComponent getMySandbox();
    
    /**
     * Returns the object that represents the User Drop Boxes component.
     * @return the User Drop Boxes component
     */
    public AbstractComponent getUserDropboxes();

    /**
     * Provides an instance of the window manager. 
     * @return window manager provided by the platform
     */
    public WindowManager getWindowManager();

    /**
     * Provides an instance of the persistence service.
     * @return persistence provider provided by the platform.
     */
    public PersistenceProvider getPersistenceProvider();
    
    /**
     * Provides an instance of the component registry.
     * @return component registry provided by the platform.
     */
    public CoreComponentRegistry getComponentRegistry();
    
    /**
     * Gets the current user.
     * @return current user.
     */
    public User getCurrentUser();
    
    /**
     * Provides an instance of the policy manager.
     * @return policy manager provided by the platform
     */
    public PolicyManager getPolicyManager();
    
    /**
     * Provides an instance of the menu manager.
     * @return
     */
    public MenuManager getMenuManager();
    
    /**
     * Provides an instance of the subscription manager.
     * @return subscription manager provided by the platform. 
     */
    public SubscriptionManager getSubscriptionManager();
    
    /**
     * Provides an instance of the time service.
     * @return time service provided by the platform. 
     */
    public TimeService getTimeService();

    /**
     * Provides an instance of the default component provider.
     * @return provider to access various types of default components
     */
    public DefaultComponentProvider getDefaultComponentProvider();
    
    public ProviderDelegateService getProviderDelegateService();

    /**
     * Returns the feed aggregator.
     * @return the feed aggregator
     */
    public FeedAggregator getFeedAggregator();
    
    /**
     * Refreshes all the MCT housing content.
     */
    public void refreshAllMCTHousedContent();
}
