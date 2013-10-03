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
 * AbstractComponent.java Aug 18, 2008
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.components;

import gov.nasa.arc.mct.db.util.IdGenerator;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.services.internal.component.Updatable;
import gov.nasa.arc.mct.util.WeakHashSet;
import gov.nasa.arc.mct.util.exception.MCTRuntimeException;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;


/**
 * The superclass of every component type. A component is uniquely identified by its id.  
 * Subclasses must be created by the platform before usage within the system, this is done using the
 * <code>ComponentRegistry</code>. A component indicates that it has persistent state by providing an instance of 
 * {@link ModelStatePersistence} through the <code>getCapabilities</code> method. 
 * 
 * <em>Important</em> subclasses must have a public no argument constructor.
 * 
 */
public abstract class AbstractComponent implements Cloneable {

    /** A dummy component, used as a sentinel value. */
    public final static AbstractComponent NULL_COMPONENT;

    static {
        AbstractComponent tmpComp = new AbstractComponent() {
        };

        tmpComp.id = "0";
        tmpComp.getCapability(ComponentInitializer.class).initialize();
        NULL_COMPONENT = tmpComp;
    }

    /** The unique ID of the component, filled in by the framework. */
    private String id;

    private ComponentTypeInfo typeInfo;
    private String owner;
    private String originalOwner;
    private String creator;
    private Date creationDate;
    private AbstractComponent workUnitDelegate = null;
    private String displayName = null; // human readable name for the component.
    private String externalKey = null; // reference that can be used to contain external keys
    private Map<String, ExtendedProperties> viewRoleProperties;
    private ComponentInitializer initializer;
    private int version;
    private final AtomicBoolean isDirty = new AtomicBoolean(false);
    private boolean isStale = false;
    /** The existing manifestations of this component. */
    private final WeakHashSet<View> viewManifestations = new WeakHashSet<View>();
    
    private SoftReference<List<AbstractComponent>> cachedComponentReferences = new SoftReference<List<AbstractComponent>>(null);
    private List<AbstractComponent> mutatedComponentReferences;
    
    /**
     * Initialize the component by registering it with the component registry
     * and creating a new, empty set of view roles.
     */
    protected void initialize() {
        performInitialization();
        getCapability(Initializer.class).setInitialized();
    }

    private void performInitialization() {
        if (this.id == null) {
            this.id = IdGenerator.nextComponentId();
        }
    }
    
    /**
     * Verifies the class requirements are met for this component class.
     * 
     * @param componentClass
     *            class to verify, must not be null
     * @throws IllegalArgumentException
     *             if the class does not meet the requirements
     */
    public static void checkBaseComponentRequirements(Class<? extends AbstractComponent> componentClass)
                    throws IllegalArgumentException {
        try {
            // ensure that a public no argument constructor exists. If this is
            // an inner non static class
            // there is an implicit argument of the enclosing class so this
            // scenario should be covered
            componentClass.getConstructor(new Class[0]);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(componentClass.getName()
                            + " must provide a public no argument constructor");
        }
    }
    
    /**
     * Returns the work unit delegate if any for this component. A work unit delegate will be persisted instead of this
     * component. This is generally only useful to views that are owning properties of other views or components.
     * @return work unit delegate or null if there is no delegate
     */
    public AbstractComponent getWorkUnitDelegate() {
        return workUnitDelegate;
    }
    
    /**
     * Get the type ID string for the type of the component. Component type IDs
     * are unique for each registered component type.
     * 
     * @return the component type ID
     */
    public String getComponentTypeID() {
        return this.getClass().getName();
    }
    
    /**
     * Returns the views for the desired view type. This method will apply the <code>PolicyInfo.CategoryType.FILTER_VIEW_ROLE</code> policy
     * and the <code>PolicyInfo.CategoryType.PREFERRED_VIEW</code> policy before returning the appropriate list of views.
     * @param type of view to discover.
     * @return views that are appropriate for this component. 
     */
    public Set<ViewInfo> getViewInfos(ViewType type) {
        Set<ViewInfo> possibleViewInfos =  PlatformAccess.getPlatform().getComponentRegistry().getViewInfos(getComponentTypeID(), type);
        Set<ViewInfo> filteredViewInfos = new LinkedHashSet<ViewInfo>();
        
        Platform platform = PlatformAccess.getPlatform();
        PolicyManager policyManager = platform.getPolicyManager();
        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), this);
        context.setProperty(PolicyContext.PropertyName.VIEW_TYPE.getName(), type);
        for (ViewInfo viewInfo : possibleViewInfos) {
            context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), viewInfo);
            if (policyManager.execute(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), context)
                            .getStatus()) {
                filteredViewInfos.add(viewInfo);
            }
        }

        // if there is a preferredView then make sure this is added first in the
        // list
        for (ViewInfo viewRole : filteredViewInfos) {
            if (this.getClass().equals(viewRole.getPreferredComponentType())) {
                Set<ViewInfo> setWithPreferredViewFirst = new LinkedHashSet<ViewInfo>();
                setWithPreferredViewFirst.add(viewRole);
                setWithPreferredViewFirst.addAll(filteredViewInfos);
                filteredViewInfos = setWithPreferredViewFirst;
                break;
            }
        }
        
        return filteredViewInfos;
    }
    
    private synchronized void ensureViewPropertiesLoaded() {
        if (viewRoleProperties == null) {
            viewRoleProperties = PlatformAccess.getPlatform().getPersistenceProvider().getAllProperties(getComponentId());
        }
        assert viewRoleProperties != null;
    }
    
    private synchronized void addViewProperty(String viewRoleType, ExtendedProperties properties) {
        ensureViewPropertiesLoaded();
        if (!viewRoleProperties.containsKey(viewRoleType)) {
            this.viewRoleProperties.put(viewRoleType, properties);
        }        
    }

    /**
     * Return the unique ID for this component.
     * 
     * @return the ID for the component
     */
    public String getId() {
        return this.id;
    }

    /**
     * Return the unique ID for this component. This is a synonym for
     * {@link #getId()}.
     * 
     * @return the ID for the component
     */
    public String getComponentId() {
        return this.id;
    }

    /**
     * Sets the ID of this component. The framework calls this method
     * automatically. It should never be called by plugin code.
     * 
     * @param id
     *            the new ID for the component
     */
    public void setId(String id) {
        if (id != null && initializer != null && initializer.isInitialized()) {
            throw new IllegalStateException("id must be set before component is initialized");
        }
        this.id = id;
    }

    /**
     * Tests whether the component is currently a leaf.
     * 
     * <p>
     * A leaf component is defined as a component that will never have any
     * children. This is an <i>immutable</i> property of a component instance
     * and must not change during its lifetime. The default implementation
     * returns false (thus the default component will be a container), a
     * subclass that should identify itself as a leaf should override this
     * method and return true.
     * 
     * @return true, if the component is currently a leaf. False otherwise.
     */
    public boolean isLeaf() {
        return false;
    }
    
    /**
     * Sets the user ID of the owner of the component.
     * 
     * @param owner
     *            the user ID of the owner of the component
     */
    public synchronized void setOwner(String owner) {
        if (originalOwner == null) {
            originalOwner = this.owner;
        }
        this.owner = owner;
    }

    /**
     * Gets the user ID of the owner of the component prior to the owner being modified.
     * @return the user Id of the owner
     */
    public final synchronized String getOriginalOwner() {
        return originalOwner;
    }
    
    private synchronized void resetOriginalOwner() {
        originalOwner = null;
    }
    
    /**
     * Gets the user ID of the owner of the component.
     * 
     * @return the component owner user ID
     */
    public synchronized String getOwner() {
        return this.owner;
    }
    
    /**
     * Gets the creator of this component. 
     * @return the creator of this component
     */
    public synchronized String getCreator() {
        return creator;
    }
    
    /**
     * Gets the creation time of this component.
     * @return when this component was created
     */
    public synchronized Date getCreationDate() {
        return creationDate;
    }

    /**
     * Adds a new delegate (child) component. The model of the new delegate
     * component will be added as a new delegate model, and all view role
     * instances will be updated. The new child will be added after all existing
     * children. If the child is already present, this will move the child to
     * the end.
     * 
     * @param childComponent
     *            the new child component
     */
    public final void addDelegateComponent(AbstractComponent childComponent) {
        addDelegateComponents(Collections.singleton(childComponent));
    }

    /**
     * This method is invoked by the persistence provider when the component is saved to the underlying persistence store. The 
     * implementation delegates to the viewPersisted method. 
     */
    public final void componentSaved() {
        final Set<View> guiComponents = getAllViewManifestations();
        if (guiComponents != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (View gui : guiComponents) {
                        gui.viewPersisted();
                    }
                }
            });
        }
    }
    
    /**
     * Gets the components which are referencing this component. Currently, only delegate components are considered.
     * The result of this method is transient and thus requires overhead for each execution. 
     * @return collection which can be empty but never null of components which reference this component. 
     */
    public Collection<AbstractComponent> getReferencingComponents() {
        PersistenceProvider persistenceService = PlatformAccess.getPlatform().getPersistenceProvider();
        return persistenceService.getReferences(this);
    }

    /**
     * Adds a delegate (child) component to this component. The model of the
     * delegate component will be added as a delegate model, if not already
     * present, and all the view roles will be updated by sending an
     * {@link AddChildEvent} to the view role listeners. If the model is already
     * present, the child model will be moved to the given position.
     * 
     * @param childIndex
     *            the index within the children to add the new component, or -1
     *            to add at the end
     * @param childComponent
     *            the new delegate component
     */
    private void processAddDelegateComponent(int childIndex, AbstractComponent childComponent) {
        List<AbstractComponent> list = getComponents();
        if (childIndex < 0) {
            childIndex = list.size();
        }

        // If the child already exists, remove it, and adjust the insert index
        // if needed.
        int existingIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getComponentId().equals(childComponent.getComponentId())) {
                existingIndex = i;
            }
        }
        if (existingIndex >= 0) {
            removeComponent(list.get(existingIndex));
            if (existingIndex < childIndex) {
                --childIndex;
            }
        }

        // Now add it again.
        addComponentAt(childIndex, childComponent);
    }

    /**
     * Determine by policy if this component can be deleted.
     * 
     * @return true if this component can be deleted, false otherwise.
     */
    public final boolean canBeDeleted() {
        Platform platform = PlatformAccess.getPlatform();
        PolicyManager policyManager = platform.getPolicyManager();

        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), this);
        context.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String compositionKey = PolicyInfo.CategoryType.CAN_DELETE_COMPONENT_POLICY_CATEGORY
                        .getKey();
        return policyManager.execute(compositionKey, context).getStatus();
    }

    /**
     * Adds new delegate (child) components to this component. The model for
     * each component will be added as a delegate model, if not already present.
     * Then, all view roles will be updated by sending an {@link AddChildEvent}
     * for each new delegate added. The child will be added at the end of any
     * existing children. If the child was already present, this will move the
     * child to the end.
     * 
     * <p>
     * This method is called when a drop of one or more components happens in
     * the directory tree. The canvas area of this component is unaffected.
     * 
     * @param childComponents
     *            the collection of delegate components to add
     * @return true if <code>childComponents</code> are added successfully;
     *         otherwise false.
     */
    public final boolean addDelegateComponents(Collection<AbstractComponent> childComponents) {
        return addDelegateComponents(-1, childComponents);
    }

    /**
     * Adds new delegate (child) components to this component. The model for
     * each component will be added as a delegate model, if not already present.
     * Then, all view roles will be updated by sending an {@link AddChildEvent}
     * for each new delegate added. The child will be added at a given index
     * among the existing children. If the child was already present, this will
     * move the child to the new position.
     * 
     * <p>
     * This method is called when a drop of one or more components happens in
     * the directory tree. 
     * 
     * @param childIndex
     *            the index with the children to add the new component, or -1 to
     *            add at the end
     * @param childComponents
     *            the collection of delegate components to add
     * @return true if <code>childComponents</code> are added successfully;
     *         otherwise false.
     */
    public final boolean addDelegateComponents(int childIndex,
                    Collection<AbstractComponent> childComponents) {

        if (isLeaf()) {
            throw new UnsupportedOperationException("components declared as leaf cannot be mutated");
        }
        Platform platform = PlatformAccess.getPlatform();
        PolicyManager policyManager = platform.getPolicyManager();
        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), this);
        context.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        context
                        .setProperty(PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),
                                        childComponents);
        if (policyManager.execute(PolicyInfo.CategoryType.ACCEPT_DELEGATE_MODEL_CATEGORY.getKey(),
                        context).getStatus()) {
          
            if (!childComponents.isEmpty()) {
                for (AbstractComponent childComponent : childComponents) {
                    processAddDelegateComponent(childIndex, childComponent);
                }
            }
            
            addDelegateComponentsCallback(childComponents);
            return true;

        }
        return false;
    }
    
    /**
     * Invoked after any of the <code>addDelegateComponents</code> methods are invoked. The default 
     * implementation does nothing. 
     * @param childComponents that were added.
     */
    protected void addDelegateComponentsCallback(Collection<AbstractComponent> childComponents) {
        
    }
    
    /**
     * Removes a delegate (child) component from this component. The model of
     * the delegate component is removed as a delegate model. Then, all view
     * roles are updated by sending a {@link RemoveChildEvent}.
     * 
     * @param childComponent
     *            the delegate component to remove
     */
    public final void removeDelegateComponent(AbstractComponent childComponent) {
        removeDelegateComponents(Collections.singleton(childComponent));
    }

    /**
     * Removes delegate (child) components from this component.
     * 
     * @param childComponents
     *            the delegate components to remove
     */
    public synchronized void removeDelegateComponents(Collection<AbstractComponent> childComponents) {
        if (isLeaf()) {
            throw new UnsupportedOperationException("components declared as leaf cannot be mutated");
        }
        for (AbstractComponent comp : childComponents) {
            removeComponent(comp);
        }

    }

    /**
     * Get the human readable name for this component that is suitable for
     * displaying to the user in a GUI.
     * 
     * If the display name has not been set, the component ID will be returned.
     * 
     * Note: this method should <b>not</b> be relied on to retrieve component
     * IDs (or other component type specific identifiers such as PUI IDs).
     * 
     * @return the display name of the component.
     */
    public synchronized String getDisplayName() {
        if (displayName == null) {
            return getId();
        } else {
            return displayName;
        }
    }
    
    /**
     * Get the human readable name for this component that is suitable for user display. This method differs from
     * {@link #getDisplayName()} as this method will be invoked when many components are being presented together and
     * having additional information may help the user distinguish components whose display name varies by only a small amount.
     * For example, this method may be used in presenting search results that may have the same display name so this method will be
     * invoked to help differentiate the results in the interface. The default implementation of this method will invoke {@link #getDisplayName()}.
     * @return a string representing the extended display name
     */
    public String getExtendedDisplayName() {
        return getDisplayName();
    }

    /**
     * Gets the external key for this component if it exists.
     * @return key used outside MCT if it exists, null otherwise
     */
    public synchronized String getExternalKey() {
        return externalKey;
    }
    
    /**
     * Sets the external key that allow MCT components to generically reference external entities. How this is
     * used outside MCT is considered behavior of the component and will be described further by the component author. 
     * @param key used outside MCT
     */
    public synchronized void setExternalKey(String key) {
        externalKey = key;
    }
    
    /**
     * Sets the human readable display name for this component.
     * 
     * @param name
     *            the new display name
     */
    public synchronized void setDisplayName(String name) {
        this.displayName = name;
    }

    /**
     * Sets the display name of the component and updates all view
     * manifestations to match, by sending a {@link PropertyChangeEvent} to each
     * view role listener.
     * 
     * @param name
     *            the new display name
     */
    public void setAndUpdateDisplayName(String name) {
        this.setDisplayName(name);
        PropertyChangeEvent event = new PropertyChangeEvent(this);
        event.setProperty(PropertyChangeEvent.DISPLAY_NAME, this.displayName);
        firePropertyChangeEvent(event);
    }
    
    
    /**
     * Sets the coomponent's owner and updates all views.
     * 
     * @param name the owner name
     */
    public void setAndUpdateOwner(String name) {
        this.setOwner(name);
        PropertyChangeEvent event = new PropertyChangeEvent(this);
        event.setProperty(PropertyChangeEvent.OWNER, this.owner);
        firePropertyChangeEvent(event);
    }
    
    private void firePropertyChangeEvent(final PropertyChangeEvent event) {
        final Set<View> guiComponents = getAllViewManifestations();
        if (guiComponents != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    for (View gui : guiComponents) {
                        gui.updateMonitoredGUI(event);
                    }
                }
            });
        }
    }

    
    /**
     * Open this component in a new top level window.
     */
    public final void open() {
        Platform platform = PlatformAccess.getPlatform();
        assert platform != null;
        
        if (DetectGraphicsDevices.getInstance().getNumberGraphicsDevices() > DetectGraphicsDevices.MINIMUM_MONITOR_CHECK) {
            Frame frame = null;
            for (Frame f: Frame.getFrames()) {
                if (f.isActive() || f.isFocused()) {
                    frame = f;
                    break;
                }
            }
            
            if (frame != null) {            
                GraphicsConfiguration graphicsConfig = frame.getGraphicsConfiguration();
                open(graphicsConfig);
            } else {
                // Need this when MCT first startups and when there's no active window available.
                openInNewWindow(platform);
            }
            
        } else {
            openInNewWindow(platform);
        }
    }
    
    private void openInNewWindow(Platform platform) {
        assert platform != null : "Platform should not be null.";
        AbstractComponent ac = getComponentId() == null ? this : getNewComponentFromPersistence();
        if (ac != null) {
            platform.getWindowManager().openInNewWindow(ac);
        }
    }
    
    private AbstractComponent getNewComponentFromPersistence() {
        return PlatformAccess.getPlatform().getPersistenceProvider().getComponent(getComponentId());
    }
    
    /**
     * Detect multiple monitor displays and allow menu item to open this component in a new top level window.
     * @param graphicsConfig - Detect multiple display monitor devices
     */
    public final void open(GraphicsConfiguration graphicsConfig) {
        Platform platform = PlatformAccess.getPlatform();
        assert platform != null;
        WindowManager windowManager = platform.getWindowManager();
        if (platform.getRootComponent() == this)
            windowManager.openInNewWindow(this, graphicsConfig);
        else
            windowManager.openInNewWindow(PlatformAccess.getPlatform().getPersistenceProvider().getComponent(getComponentId()), graphicsConfig);
    }
    
    /**
     * Gets an instance of the capability. A capability is functionality that
     * can be provided by a component dynamically. For example, functionality
     * that can be provided only before a component has been initialized, doing this
     * using inheritance would require introducing an exception into the method
     * signatures and additional semantic documentation. The
     * capabilities provided are specific to the component type and are not
     * constrained by the platform.
     * 
     * @param <T>
     *            Class of the capability
     * @param capability
     *            requested from the component
     * @return an instance of the capability requested or null if not provided
     */
    public final <T> T getCapability(Class<T> capability) {
        if (ComponentInitializer.class.isAssignableFrom(capability)) {
            if (initializer == null) {
                initializer = new Initializer();
            }
            return capability.cast(initializer);
        } else if (Updatable.class.isAssignableFrom(capability)) {
            if (initializer == null) {
                initializer = new Initializer();
            }
            return capability.cast(initializer);
        } else if (capability.isAssignableFrom(ComponentTypeInfo.class)) {
            if (typeInfo == null) {
                for (ComponentTypeInfo info : 
                    ExternalComponentRegistryImpl.getInstance().getComponentInfos()) {
                    if (info != null && info.getComponentClass().equals(getClass())) {
                        typeInfo = info;
                        break;
                    }
                }
                if (typeInfo == null) {
                    typeInfo = new ComponentTypeInfo(getClass().getSimpleName(), getClass().getSimpleName(), getClass()); 
                }
            }
            return capability.cast(typeInfo);
        }
       
        return handleGetCapability(capability);
    }

    /**
     * Provides subclasses a chance to inject capabilities. The default
     * implementation does nothing by returning null. There are no requirements
     * on component providers to add capabilities.
     * 
     * @param <T>
     *            Class of the capability
     * @param capability
     *            requested from the component
     * @see AbstractComponent#getCapability(Class)
     * @return an instance of the capability requested for null if not available
     */
    protected <T> T handleGetCapability(Class<T> capability) {
        return null;
    }

    /**
     * Provide multiple capabilities for a capability class. 
     * @param <T> Class of the capability
     * @param capability requested from the component
     * @return list of capabilities
     */
     public final <T>List<T> getCapabilities(Class<T> capability) {
        return handleGetCapabilities(capability);
    }
    

    /**
     * Provides subclasses a chance to inject capabilities.
     * @param <T> the class of the capability
     * @param capability requested from the component
     * @return list of capabilities 
     */
    protected <T>List<T> handleGetCapabilities(Class<T> capability) {
        T t = getCapability(capability);
        return t == null ? Collections.<T>emptyList() : Collections.singletonList(t);
    }
    
    private AbstractComponent getWorkUnitComponent() {
        return workUnitDelegate != null ? workUnitDelegate : this;
    }
    
    private void addComponentToWorkUnit() {
        PlatformAccess.getPlatform().getPersistenceProvider().addComponentToWorkUnit(getWorkUnitComponent());
    }
    
    /**
     * Determines if the underlying component has changes that only exist in memory.
     * @return true if the component needs to be saved false otherwise. 
     */
    public boolean isDirty() {
        return getWorkUnitComponent().isDirty.get();
    }

    /**
     * Determines if the version of the underlying component is older than the component
     * persisted in the database.
     * @return true this version is older
     */
    public synchronized boolean isStale() {
        return isStale;
    }
    /**
     * Mark the component as having outstanding changes in memory.
     */
    public void save() {
        getWorkUnitComponent().isDirty.set(true);
        addComponentToWorkUnit();
        if (getWorkUnitComponent() != this) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (View v : viewManifestations)
                        v.updateMonitoredGUI();
                }
                
            });
        }
    }
    
    @Override
    public AbstractComponent clone() {
        try {
            Class<? extends AbstractComponent> componentClassType = this.getClass();
            
            String newID = IdGenerator.nextComponentId();
            AbstractComponent clonedComponent = componentClassType.newInstance();
            clonedComponent.setId(newID);
            
            ModelStatePersistence persistence = getCapability(ModelStatePersistence.class);
            if (persistence != null) {
                String modelState = persistence.getModelState();
                ModelStatePersistence clonedPersistence = clonedComponent.getCapability(ModelStatePersistence.class);
                assert clonedPersistence != null;
                clonedPersistence.setModelState(modelState);
                
            }

            for (AbstractComponent child : getComponents()) {
                clonedComponent.addComponent(child);
            }
            
            
            ensureViewPropertiesLoaded();
            clonedComponent.ensureViewPropertiesLoaded();
            for (Entry<String, ExtendedProperties> e : viewRoleProperties.entrySet()) {
                clonedComponent.viewRoleProperties.put(e.getKey(), e.getValue().clone());
            }

            clonedComponent.owner = owner;
            clonedComponent.creator = creator;
            clonedComponent.displayName = displayName;
            clonedComponent.externalKey = externalKey;
            clonedComponent.version = version;

            ComponentInitializer clonedCapability = clonedComponent.getCapability(ComponentInitializer.class);
            
            clonedCapability.initialize();
            return clonedComponent;
        } catch (SecurityException e) {
            throw new MCTRuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new MCTRuntimeException(e);
        } catch (InstantiationException e) {
            throw new MCTRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new MCTRuntimeException(e);
        } 
    }
   
    private synchronized void setViewProperty(String viewType, ExtendedProperties properties) {
        ensureViewPropertiesLoaded();
        this.viewRoleProperties.put(viewType, properties);
    }

    private synchronized Map<String,ExtendedProperties> getRawViewProperties() {
        return viewRoleProperties;
    }
    
    private synchronized Map<String,ExtendedProperties> getViewProperties() {
        ensureViewPropertiesLoaded();
        return viewRoleProperties;
    }
    
    private synchronized ExtendedProperties getViewProperties(String viewType) {
        ensureViewPropertiesLoaded();
        return this.viewRoleProperties.get(viewType);
    }

    /**
     * Returns the version of the component.
     * @return the version of the component
     */
    public int getVersion() {
        return version;
    }
    
    /**
     * Get an asset of a specified type. For instance, an 
     * Icon may be retrieved using getAsset(Icon.class).
     * @param <T> the type of asset desired
     * @param assetClass the desired type of asset
     * @return an asset of the desired type (or null if there is none)
     */
    public <T> T getAsset(Class<T> assetClass) {
        return getCapability(ComponentTypeInfo.class).getAsset(assetClass);
    }
    
    /**
     * Returns the icon image for this component.
     * @return an icon image
     * @deprecated use getAsset(Icon.class) instead
     */
    @Deprecated
    public final javax.swing.ImageIcon getIcon() {
        return getAsset(javax.swing.ImageIcon.class);
    }
    
    /**
     * Returns the icon based on the component type.
     * @param className of the component type
     * @return an image icon
     */
    @Deprecated
    public static javax.swing.ImageIcon getIconForComponentType(String className) {
        for (ComponentTypeInfo componentTypeInfo : 
            ExternalComponentRegistryImpl.getInstance().getComponentInfos()) {
            if (componentTypeInfo.getTypeClass().getName().equals(className)) {
                return componentTypeInfo.getAsset(javax.swing.ImageIcon.class);
            }
        }
        return null;
    }
    
    /**
     * Resets component properties.
     * @param txn the transaction to be performed atomically
     */
    public synchronized void resetComponentProperties(ResetPropertiesTransaction txn) {
        txn.perform();
    }
    
    /**
     * Adds a view manifestation that should be alerted to changes in this component.
     * @param viewManifestation to notify when changes occur.
     */
    public void addViewManifestation(View viewManifestation) {
        viewManifestations.add(viewManifestation);
    }
    
    /**
     * Gets all the currently monitored manifestations. 
     * @return all the current views of this manifestation
     */
    public Set<View> getAllViewManifestations() {
        return new HashSet<View>(viewManifestations);
    }
    
    private final class Initializer implements ComponentInitializer, Updatable {
        private boolean initialized;
        
        @Override
        public void setId(String id) {
            AbstractComponent.this.id = id;
        }

        @Override
        public void setOwner(String owner) {
            AbstractComponent.this.owner = owner;
        }
        
        @Override
        public void setWorkUnitDelegate(AbstractComponent delegate) {
            AbstractComponent.this.workUnitDelegate = delegate;   
        }
        
        @Override
        public void componentSaved() {
            AbstractComponent.this.isDirty.set(false);
            AbstractComponent.this.resetOriginalOwner();
            AbstractComponent.this.releaseChildrenList();
        }
        
        @Override
        public void setViewRoleProperty(String viewRoleType, ExtendedProperties properties) {
            setViewProperty(viewRoleType, properties);
        }

        @Override
        public ExtendedProperties getViewRoleProperties(String viewType) {
            return getViewProperties(viewType);
        }

        @Override
        public void setCreationDate(Date creationDate) {
            AbstractComponent.this.creationDate = creationDate;
        }
        
        @Override
        public void setCreator(String creator) {
           AbstractComponent.this.creator = creator;
        }
        
        @Override
        public void addViewRoleProperties(String viewRoleType, ExtendedProperties properties) {
            addViewProperty(viewRoleType, properties);
        }
        
        @Override
        public void initialize() {
            checkInitialized();
            AbstractComponent.this.initialize();
        }

        public void setInitialized() {
            checkInitialized();
            initialized = true;
        }

        private void checkInitialized() {
            if (isInitialized()) {
                throw new IllegalStateException("component already initialized");
            }
        }

        @Override
        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public Map<String, ExtendedProperties> getMutatedViewRoleProperties() {
            return AbstractComponent.this.getRawViewProperties();
        }
        
        @Override
        public Map<String, ExtendedProperties> getAllViewRoleProperties() {
            return Collections.unmodifiableMap(getViewProperties());
        }

        @Override
        public synchronized void setVersion(int version) {
            AbstractComponent.this.version = version;
            cachedComponentReferences.clear();
        }

        @Override
        public void setBaseDisplayedName(String baseDisaplyedName) {
            AbstractComponent.this.displayName = baseDisaplyedName;
        }

        @Override
        public void addReferences(List<AbstractComponent> references) {
            for (AbstractComponent c : references) {
                processAddDelegateComponent(-1, c);
            }
        }

        @Override
        public void removeReferences(List<AbstractComponent> references) {
            for (AbstractComponent c : references) {
                removeComponent(c);
            }
        }

        @Override
        public void notifyStale() {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (View v : viewManifestations) {
                        v.notifyStaleState(true);
                    }
                }                
            });
        }

        @Override
        public synchronized void setStaleByVersion(int version) {
            if (getVersion() < version) {
                AbstractComponent.this.isStale = true ;
            }
        }
        
        @Override
        public synchronized void setStaleByVersion(String componentId, int version) {
            AbstractComponent component = findComponentById(componentId);
            if (component != null && component.getVersion() < version) {
                AbstractComponent.this.isStale = true ;
            }
        }        

        /*  
         * Search recursively for a component with a specific id. This is used 
         * to support setStaleByVersion for work unit delegates. In principle this 
         * search could take a long time, but in practice this will probably not be 
         * the case as this should only be triggered when incoming changes occur 
         * in a view which contains the specified components. The component's presence 
         * in that view should generally imply that the component is not particularly 
         * deep within the graph (hence the choice of a breadth-first search)
         */
        private AbstractComponent findComponentById(String id) {
            Set<String> ignore = new HashSet<String>();
            Queue<AbstractComponent> queue = new LinkedList<AbstractComponent>();
            queue.add(AbstractComponent.this);
            AbstractComponent toCheck;
            while ( (toCheck = queue.poll()) != null) {
                String checkedId = toCheck.getComponentId();
                if (checkedId.equals(id)) {
                    return toCheck;
                } else if (!ignore.contains(checkedId)) {
                    ignore.add(checkedId);
                    queue.addAll(toCheck.getComponents());
                }
            }
            return null;            
        }
    }    
    
    /**
     * Defines a transaction to reset component properties. 
     */
    public interface ResetPropertiesTransaction {
        /**
         * Defines the steps to reset certain component properties.
         */
        public void perform();
    }
        
    /**
     * Get a list of all components to which this component refers.
     * Generally, a referenced component may be thought of as a child 
     * of the referencing component, but the precise interpretation of the 
     * relationship may vary among component types and view types.
     * @return a list of all referenced components
     */
    public synchronized List<AbstractComponent> getComponents() {
        return getOrLoadComponents();
    }

    /**
     * Returns the component by the specified id.
     * @param id of the component to find
     * @return component with the given id or null if no component currently has the id.
     */
    public static AbstractComponent getComponentById(String id) {
        Platform platform = PlatformAccess.getPlatform();
        return platform.getPersistenceProvider().getComponent(id);    
    }
    
    private void referencedComponentsMutated() {
        mutatedComponentReferences = cachedComponentReferences.get();
        assert mutatedComponentReferences != null : "method must be invoked while holding a strong reference to the mutated list of children";
    }
    
    private synchronized void releaseChildrenList() {
        mutatedComponentReferences = null;
    }
    
    private List<AbstractComponent> getOrLoadComponents() {
        if (isLeaf()) {
            return Collections.<AbstractComponent>emptyList();
        }
        List<AbstractComponent> currentlyReferencedComponents = cachedComponentReferences.get();
        if (currentlyReferencedComponents == null) {
            currentlyReferencedComponents = new ArrayList<AbstractComponent>(PlatformAccess.getPlatform().getPersistenceProvider().getReferencedComponents(this));
            cachedComponentReferences = new SoftReference<List<AbstractComponent>>(currentlyReferencedComponents);
        }
        
        return currentlyReferencedComponents;
    }
    
    /**
     * Add a reference to the specified component.
     * Generally, a referenced component may be thought of as a child 
     * of the referencing component, but the precise interpretation of the 
     * relationship may vary among component types and view types.
     * @param component the component to which to refer
     */
    private synchronized void addComponent(AbstractComponent component) {
        List<AbstractComponent> referencedComponents = getOrLoadComponents();
        referencedComponents.add(component);
        referencedComponentsMutated();
    }
    
    /**
     * Add a reference to the specified component, at the specified index.
     * Generally, a referenced component may be thought of as a child 
     * of the referencing component, but the precise interpretation of the 
     * relationship may vary among component types and view types.
     * @param index the index at which to reference
     * @param component the component to which to refer
     */
    private synchronized void addComponentAt(int index, AbstractComponent component) {
        List<AbstractComponent> referencedComponents = getOrLoadComponents();
        referencedComponents.add(index, component);
        referencedComponentsMutated();
    }
    
    /**
     * Remove a reference to the specified component
     * Generally, a referenced component may be thought of as a child 
     * of the referencing component, but the precise interpretation of the 
     * relationship may vary among component types and view types.
     * @param component the component to dereference
     */
    private synchronized void removeComponent(AbstractComponent component) {
        List<AbstractComponent> referencedComponents = getOrLoadComponents();
        for (AbstractComponent ac : referencedComponents) {
            if (ac.getComponentId().equals(component.getComponentId())) {
                referencedComponents.remove(ac);
                break;
            }
        }
        referencedComponentsMutated();
    }
    
    /** 
     * Returns a description of nuclear data that is inspectable. 
     * This ordered list of fields is rendered in MCT Platform's InfoView.
     * 
     * @return ordered list of property descriptors
     */
    public List<PropertyDescriptor> getFieldDescriptors() {
        return null;
    }
    
    /**
     * For Save All action. This method should return a set of modified objects 
     * to be saved. These modified objects are not required to be related to this object.
     * The save all action automatically includes saving changes for this object, so this 
     * method does not need to include this object to be saved.
     * 
     * @return a set of <code>AbstractComponent</code> that have been modified and are ready to be saved
     */
    public Set<AbstractComponent> getAllModifiedObjects() {
        return Collections.emptySet();
    }
    
    /**
     * This is a template method, which is called when the save all action was successful.
     */
    public void notifiedSaveAllSuccessful() {        
    }
}
