package gov.nasa.arc.mct.platform.spi;

import gov.nasa.arc.mct.api.persistence.PersistenceService;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines the interface for the platform. A persistence provider supports the ability to retrieve
 * and mutate components.
 *
 */
public interface PersistenceProvider extends PersistenceService {
    
    /**
     * Gets the specified user from the database.
     * @param userId of the user
     * @return user
     */
    User getUser(String userId);
    
    /**
     * Gets the component based on the component id. 
     * @param componentId of the component.
     * @return the component or null if no component with the id exists.
     */
    AbstractComponent getComponent(String componentId);
    
    /**
     * Gets the component based on the component id and will ignore cache if any.
     * @param componentId of the component.
     * @return the component or null if no component with the id exists.
     */
    AbstractComponent getComponentFromStore(String componentId);
    
    /**
     * Gets the references to this component. Currently, this is only based on relationships.
     * @param component to find references to
     * @return Collection that can be empty but never null of components referencing this component.
     */
    Collection<AbstractComponent> getReferences(AbstractComponent component);
    
    /**
     * Gets all the persistent state for the component.
     * @param componentId to retrieve properties for
     */
    Map<String, ExtendedProperties> getAllProperties(String componentId);
    
    /**
     * Update the persistent representation with the specified components.
     * @param componentsToPersist as a unit
     */
    void persist(Collection<AbstractComponent> componentsToPersist);
    
    /**
     * Deletes the components from the persistent store.
     * @param componentsToDelete as a unit
     */
    void delete(Collection<AbstractComponent> componentsToDelete);

    /**
     * Returns the references from this component.
     * @param component to find references from
     * @return Collection that can be empty but never null of components that are referenced by this component.
     */
    List<AbstractComponent> getReferencedComponents(AbstractComponent component);
    
    /**
     * Gets all users.
     * @return collection of users.
     */
    Set<String> getAllUsers();
    
    /**
     * Gets all users in a group
     * @param group to find users in
     * @return a collection of User IDs
     */
    Collection<String> getUsersInGroup(String group);
    
    /**
     * Adds this component to the work unit.
     * @param component to persist when the work unit is completed.
     */
    void addComponentToWorkUnit(AbstractComponent component);
    
    /**
     * Polls component changes from database and updates those changes to the 
     * corresponding components. Changes include: component properties, view
     * properties, models, and model data.
     */
    void updateComponentsFromDatabase();
    
    /**
     * Returns the initial list of components to be populated in the UserEnvironment upon MCT startup. 
     * @return the initial list of components upon MCT startup.
     */    
    List<AbstractComponent> getBootstrapComponents();    
    
    /**
     * This method handles user creation.
     * @param userId the userId
     * @param groupId the groupId
     * @param mysandbox the My Sandbox component
     * @param dropbox the user's dropbox
     */
    void addNewUser(String userId, String groupId, AbstractComponent mysandbox, AbstractComponent dropbox);
    
    /**
     * Add a tag to the specified components
     * @param tag the specified components
     * @param components to tag, must already be persistent
     */
    void tagComponents(String tag, Collection<AbstractComponent> components);
}
