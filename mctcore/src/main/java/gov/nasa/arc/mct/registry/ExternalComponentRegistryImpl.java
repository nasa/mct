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
package gov.nasa.arc.mct.registry;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.collection.CollectionComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.platform.spi.DefaultComponentProvider;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ProviderDelegate;
import gov.nasa.arc.mct.services.component.SearchProvider;
import gov.nasa.arc.mct.services.component.StatusAreaWidgetInfo;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;
import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provide an instance of the component registry which supports creating components.
 * A singleton pattern is used to give access to a single instance of the registry. This class is not intended to be used
 * by component developers and will be removed from the public API. 
 * 
 */
public class ExternalComponentRegistryImpl implements CoreComponentRegistry {
    private static final MCTLogger LOGGER = MCTLogger.getLogger(ExternalComponentRegistryImpl.class);
    private static final ExternalComponentRegistryImpl INSTANCE = new ExternalComponentRegistryImpl();
    
    /**
     * A constant for defining the permission of a private component.
     */
    public static final byte USER_DEFINED_GROUP_PERMISSION = (byte)112;
    
    private static final Collection<ViewType> ALLOWS_MULTIPLE_DEFAULT_VIEWS = Arrays.asList(ViewType.CENTER, ViewType.OBJECT);

    
    
    // use a normally synchronized map instead of concurrent hash map as the resync needs to be atomic and thus
    // requires a standard lock
    private final Map<String, ExtendedComponentTypeInfo> availableComponents = Collections.synchronizedMap(new HashMap<String,ExtendedComponentTypeInfo>());
    private final AtomicReference<Collection<ExtendedComponentProvider>> activeProviders = 
        new AtomicReference<Collection<ExtendedComponentProvider>>(Collections.<ExtendedComponentProvider>emptyList());
    private final AtomicReference<ComponentProvider> defaultViewProvider =
        new AtomicReference<ComponentProvider>();
    
    /**
     * Creates an instance of the component registry. This method
     * is protected to enforce the singleton pattern.
     */
    ExternalComponentRegistryImpl() {
        // do nothing
    }

    /**
     * Gets the singleton instance of the component registry.
     * 
     * @return the component registry
     */
    public static ExternalComponentRegistryImpl getInstance() {
        return INSTANCE;
    }
    
    /**
     * Gets the user ID of the default owning user when creating
     * components. This is currently the ID of the currently logged-in user.
     * 
     * @return the default user ID
     */
    protected String getDefaultUser() {
        return GlobalContext.getGlobalContext().getUser().getUserId();
    }
    
    /**
     * Gets the component class for a component type ID.
     * 
     * @param componentTypeId the type ID for which we want the component class
     * @return the component class, or null if no such component type
     */
    Class<? extends AbstractComponent> getComponentType(String componentTypeId) {
        ComponentTypeInfo info = availableComponents.get(componentTypeId);
        Class<? extends AbstractComponent> componentClass = null;
        if (info != null) {
            componentClass = info.getComponentClass();
        }
        return componentClass;
    }
    
    @Override
    public Set<ViewInfo> getViewInfos(String componentTypeId, ViewType type) {
        Set<ViewInfo> infos = new HashSet<ViewInfo>();
        for (ComponentProvider provider:activeProviders.get()) {
            for (ViewInfo info:provider.getViews(componentTypeId)) {
                if (type == info.getViewType()) {
                    infos.add(info);
                }
            }
        }
        
        ComponentProvider defaultProvider = defaultViewProvider.get();
        if (defaultProvider != null && (infos.isEmpty() || (ALLOWS_MULTIPLE_DEFAULT_VIEWS.contains(type)))) {
            for (ViewInfo view : defaultProvider.getViews(componentTypeId)) {
                if (type == view.getViewType()) {
                    infos.add(view);
                }
            }
        }
        
        return infos;
    }
    
    /**
     * Gets all component type information that has been registered.
     * 
     * @return a collection of {@link ExtendedComponentTypeInfo} for the available component types
     */
    public Collection<ExtendedComponentTypeInfo> getComponentInfos() {
        return availableComponents.values();
    }
    

    @Override
    public <T> T getAsset(TypeInfo<?> objectType, Class<T> assetClass) {
        for (ExtendedComponentProvider provider : activeProviders.get()) {
            T asset = provider.getAsset(objectType, assetClass);
            if (asset != null) {
                return asset;
            }
        }
        return defaultViewProvider.get().getAsset(objectType, assetClass);
    }

    /**
     * Sets the default component provider that is used when looking
     * up compatible view roles. Should be called by the framework
     * only.
     * 
     * @param provider the new provider to use
     */
    public void setDefaultViewProvider(ComponentProvider provider) {
        defaultViewProvider.set(provider);
    }
    
    @Override
    public AbstractComponent newInstance(ComponentTypeInfo componentTypeInfo) {
        Class<? extends AbstractComponent> componentClass = componentTypeInfo.getComponentClass();
        LOGGER.debug("new instance called for {0}", componentClass.getClass().getName());
        AbstractComponent newComponent;
        try {
            newComponent = componentClass.newInstance();
            newComponent.getCapability(ComponentInitializer.class).initialize();
            newComponent.setOwner(getDefaultUser());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        }
        return newComponent;
    }
    
    @Override
    public AbstractComponent newCollection(Collection<AbstractComponent> components) {
        AbstractComponent collectionComponent = null;
        AbstractComponent mySandbox = PlatformAccess.getPlatform().getMySandbox();
        
        collectionComponent = newInstance(CollectionComponent.class, mySandbox);
        collectionComponent.save();
        if (!addComponents(components, collectionComponent)) {
            mySandbox.removeDelegateComponent(collectionComponent);
            collectionComponent = null;
        }
        mySandbox.save();
        return collectionComponent;
    }
    
    @Override
    public <T extends AbstractComponent> T newInstance(Class<T> componentClass, AbstractComponent parent) {
        LOGGER.debug("new instance called for {0}", componentClass.getClass().getName());
        if (parent == null) {
            parent = PlatformAccess.getPlatform().getMySandbox();
        }
        assert parent != null;
        String componentTypeId = componentClass.getName();
        AbstractComponent newComponent;
        try {
            newComponent = createComponent(componentTypeId);
            
            // there is no way for a plugin to recover (and these really shouldn't happen after development) 
            // from the following exceptions so rethrow them as unchecked exceptions    
        } catch (IllegalAccessException e) {
           throw new RuntimeException(e);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        }
        
        if (newComponent != null) {
            newComponent.setOwner(getDefaultUser());
            ComponentInitializer ci = newComponent.getCapability(ComponentInitializer.class);
            ci.setCreator(getDefaultUser());
            
            parent.addDelegateComponent(newComponent);  
            parent.save();
            newComponent.save();
        }
        
        return componentClass.cast(newComponent);
    }
    
    boolean addComponents(Collection<AbstractComponent> childComponents, AbstractComponent parentComponent) {
        return parentComponent.addDelegateComponents(childComponents);
    }
    
    private AbstractComponent createComponent(String componentTypeId) 
    throws InstantiationException, IllegalAccessException {
        ExtendedComponentTypeInfo type = availableComponents.get(componentTypeId);
        Class<? extends AbstractComponent> c;
        if (type == null) {
            DefaultComponentProvider provider = PlatformAccess.getPlatform().getDefaultComponentProvider();
            c = provider.getBrokenComponent();
            LOGGER.error("unable to find class for component " + componentTypeId + " this is likely caused by a missing bundle");
        } else {
            c = type.getComponentClass();
        }
        AbstractComponent component =  c.newInstance();
        component.getCapability(ComponentInitializer.class).initialize();
        
        return component;
    }

    /**
     * Implements an extended component provider that can wrap a component provider
     * supplied by a plugin bundle.
     */
    public static class ExtendedComponentProvider implements ComponentProvider {
        private final ComponentProvider provider;
        private final String bundleSymbolicName;

        /**
         * Creates a new extended component provider based on a base component
         * provider.
         * 
         * @param aProvider the base provider
         * @param bundleSymName the symbolic name for the bundle containing the base provider
         */
        public ExtendedComponentProvider(ComponentProvider aProvider, String bundleSymName) {
            provider = aProvider;
            bundleSymbolicName = bundleSymName;
        }

        @Override
        public Collection<ComponentTypeInfo> getComponentTypes() {
            return provider.getComponentTypes();
        }

        @Override
        public Collection<ViewInfo> getViews(String componentId) {
            return provider.getViews(componentId);
        }

        /**
         * Gets the symbolic name of the bundle containing the base provider.
         * 
         * @return the bundle symbolic name
         */
        public String getBundleSymbolicName() {
            return bundleSymbolicName;
        }

        @Override
        public Collection<MenuItemInfo> getMenuItemInfos() {
            return provider.getMenuItemInfos();
        }

        @Override
        public Collection<PolicyInfo> getPolicyInfos() {
            return provider.getPolicyInfos();
        }

        @Override
        public ProviderDelegate getProviderDelegate() {
            return provider.getProviderDelegate();
        }

        @Override
        public Collection<StatusAreaWidgetInfo> getStatusAreaWidgetInfos() {
            return provider.getStatusAreaWidgetInfos();
        }

        @Override
        public SearchProvider getSearchProvider() {
            return provider.getSearchProvider();
        }

        @Override
        public <T> T getAsset(TypeInfo<?> objectType, Class<T> assetType) {
            return provider.getAsset(objectType, assetType);
        }

    }
    
    /**
     * Implements an extended component type information. This class
     * is used to wrap a component type information from a plugin bundle.
     * It keeps track of the bundle ID, but otherwise delegates to the
     * plugin bundle.
     */
    public static class ExtendedComponentTypeInfo extends ComponentTypeInfo {
        private final String symbolicName;

        /**
         * Creates a new extended component type.
         * 
         * @param info the component type information
         * @param bundleSymName the OSGi bundle symbolic name
         */
        public ExtendedComponentTypeInfo(ComponentTypeInfo info, String bundleSymName) {
            super(info.getDisplayName(), info.getShortDescription(), info.getComponentClass(), info.getId(), info.isCreatable());
            assert bundleSymName != null: "bundleSymbolicName should not be null";
            symbolicName = bundleSymName;
        }
        
        /**
         * Gets the symbolic name of the bundle we're wrapping.
         * 
         * @return the bundle symbolic name
         */
        public String getBundleSymbolicName() {
            return symbolicName;
        }
    }
    
    /**
     * Refresh the set of components maintained by the registry.
     * First, all component types are forgotten. Then, all component
     * type providers are queried to get their component type information.
     * 
     * @param providers the ordered list of component type providers
     */
    public void refreshComponents(List<ExtendedComponentProvider> providers) {
        LOGGER.debug("providers refreshed {0}", providers);
        assert providers != null : "providers list should not be null";
        synchronized(availableComponents) {
            activeProviders.set(providers);

            availableComponents.clear();
            for (ExtendedComponentProvider provider:providers) {
                try {
                    if (provider.getComponentTypes() != null) {
                        for (ComponentTypeInfo info:provider.getComponentTypes()) {
                            ExtendedComponentTypeInfo eInfo = new ExtendedComponentTypeInfo(info, provider.getBundleSymbolicName());
                            ExtendedComponentTypeInfo existingInfo = availableComponents.put(info.getId(), eInfo);
                            assert existingInfo == null : "Component type already registered by " + existingInfo.getBundleSymbolicName() + " for " + info.getDisplayName() +
                                                          " trying to register again by " + eInfo.getBundleSymbolicName();
                        }
                    }
                } catch (Exception e) {
                    // if an exception occurs, log an error for the provider to resolve but
                    // continue through the rest of the providers
                    LOGGER.error("Error occurred while invoking provider: " + provider.getClass().getName() +
                            " from bundle: " + provider.getBundleSymbolicName(), e);
                }
            }
        }
    }
    
    @Override
    public AbstractComponent getComponent(String id) {
        return PlatformAccess.getPlatform().getPersistenceProvider().getComponent(id);
    }
    
    /**
     * This method checks if the class can be created. If the class is creatable, it will
     * appear under the "Create" menu.
     * 
     * @param clazz the class to check if it is creatable.
     * @return true if clazz is creatable, false otherwise
     */
    @Override
    public boolean isCreatable(Class<?> clazz) {
        ComponentTypeInfo info = availableComponents.get(clazz.getName());
        return info != null && info.isCreatable();
    }

    @Override
    public AbstractComponent newInstance(String componentType) {
        try {
            AbstractComponent comp = createComponent(componentType);
            ComponentInitializer ci = comp.getCapability(ComponentInitializer.class);
            String user = getDefaultUser();
            ci.setCreator(user);
            comp.setOwner(user);
            return comp;
            
        } catch (InstantiationException e) {
            LOGGER.info(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

}
