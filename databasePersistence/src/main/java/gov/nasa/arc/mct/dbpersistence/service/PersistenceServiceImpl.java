/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
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
package gov.nasa.arc.mct.dbpersistence.service;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.dbpersistence.access.InternalDBPersistenceAccess;
import gov.nasa.arc.mct.dbpersistence.dao.ComponentSpec;
import gov.nasa.arc.mct.dbpersistence.dao.DatabaseIdentification;
import gov.nasa.arc.mct.dbpersistence.dao.Disciplines;
import gov.nasa.arc.mct.dbpersistence.dao.MctUsers;
import gov.nasa.arc.mct.dbpersistence.dao.Tag;
import gov.nasa.arc.mct.dbpersistence.dao.TagAssociation;
import gov.nasa.arc.mct.dbpersistence.dao.TagAssociationPK;
import gov.nasa.arc.mct.dbpersistence.dao.ViewState;
import gov.nasa.arc.mct.dbpersistence.dao.ViewStatePK;
import gov.nasa.arc.mct.dbpersistence.search.QueryResult;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.services.internal.component.Updatable;
import gov.nasa.arc.mct.services.internal.component.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceServiceImpl implements PersistenceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceServiceImpl.class);
	private static final String PERSISTENCE_PROPS = "properties/persistence.properties";
	private static final String VERSION_PROPS = "properties/version.properties";
	private static final JAXBContext propContext;
	private static final ComponentIdComparator COMPONENT_ID_COMPARATOR = new ComponentIdComparator();
	
	private final ConcurrentHashMap<String, List<WeakReference<AbstractComponent>>> cache = new ConcurrentHashMap<String, List<WeakReference<AbstractComponent>>>(); 
	
	static {
		try {
			propContext = JAXBContext.newInstance(ExtendedProperties.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
	static class ComponentIdComparator implements Comparator<AbstractComponent>, Serializable{
		private static final long serialVersionUID = 1834331905999908621L;

		public int compare(AbstractComponent o1, AbstractComponent o2) {
			return o1.getComponentId().compareTo(o2.getComponentId());
		}
	}
	
	private static final ThreadLocal<Set<AbstractComponent>> components = new ThreadLocal<Set<AbstractComponent>>(){
		@Override
		protected Set<AbstractComponent> initialValue() {
			return Collections.emptySet();
		}
	};
	
	private EntityManagerFactory entityManagerFactory;
		
	private Date lastPollTime;
	public void setEntityManagerProperties(Properties p) {
		ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		try {
			entityManagerFactory = Persistence.createEntityManagerFactory("MissionControlTechnologies", p);
		} finally {
			Thread.currentThread().setContextClassLoader(originalCL);
		}
	}

	public void activate(ComponentContext context) throws IOException {
		setEntityManagerProperties(getPersistenceProperties());
		new InternalDBPersistenceAccess().setPersistenceService(this);
		checkDatabaseVersion();
		
        Timer databasePollingTimer = new Timer();
        databasePollingTimer.schedule(new TimerTask() {

            @Override
            public void run() {
            	InternalDBPersistenceAccess.getService().updateComponentsFromDatabase();
            }
            
        }, Calendar.getInstance().getTime(), 3000);

	}
	
	private Properties getPersistenceProperties() throws IOException {
		Properties properties = new Properties();
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(PERSISTENCE_PROPS);
		if (is == null)
			throw new IOException("Unable to get mct property file: " + PERSISTENCE_PROPS);
		
		try {
			properties.load(is);
		} finally {
			is.close();
		}
		
		// adjust user name and connection string properties for JPA
		final String dbUser = "mct.database_userName";
		final String dbPassword = "mct.database_password";
		final String dbConnectionURL = "mct.database_connectionUrl";
		final String dbName = "mct.database_name";
		final String dbProperties = "mct.database_properties";
		final String jdbcUrlProperty = "javax.persistence.jdbc.url";
		if (System.getProperty(jdbcUrlProperty) != null) {
			properties.put(jdbcUrlProperty, System.getProperty(jdbcUrlProperty));
		}
		properties.put("javax.persistence.jdbc.user", System.getProperty(dbUser, properties.getProperty(dbUser)));
		properties.put("javax.persistence.jdbc.password",System.getProperty(dbPassword,properties.getProperty(dbPassword)));
		String connectionURL = System.getProperty(dbConnectionURL, properties.getProperty(dbConnectionURL)) + 
							   System.getProperty(dbName, properties.getProperty(dbName)) + "?" +
							   System.getProperty(dbProperties, properties.getProperty(dbProperties));
		if (!properties.containsKey(jdbcUrlProperty)) {
			properties.put(jdbcUrlProperty,connectionURL);
		}
		
		return properties;
	}
	
	private void checkDatabaseVersion() {
		// Don't check schema versions if a special JVM parameter is set.
        if (System.getProperty("mct.db.check-schema-version", Boolean.TRUE.toString()).equals(Boolean.TRUE.toString())) {
        	Properties versionProperties = new Properties(); 
        	 try {
                 InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(VERSION_PROPS);
         		if (is == null)
         			throw new IOException("Unable to get mct property file: " + VERSION_PROPS);
         		try {
         			versionProperties.load(is);
         		} finally {
         			is.close();
         		}
             } catch (IOException e) {
                 throw new RuntimeException("Cannot load version properties (properties/version.properties)", e);
             }
             
        	 String schemaId;
             if ((schemaId = versionProperties.getProperty("mct.db.schema_id")) == null) {
                 throw new RuntimeException("required property mct.db.schema_id is undefined");
             }
             
             EntityManager em = entityManagerFactory.createEntityManager();
             try {
            	 
            	 TypedQuery<DatabaseIdentification> q = em.createNamedQuery("DatabaseIdentification.findSchemaId", DatabaseIdentification.class);
            	 DatabaseIdentification di = q.getSingleResult();
            	 if (!schemaId.equals(di.getValue())) {
            		 throw new RuntimeException ("Mismatched schemaID.\nDeployed schema ID is " + di.getValue() + 
            				 "\nbut MCT requires schema ID " + schemaId);
            	 } 
             } finally {
            	 em.close();
             }
       }
	}
	
	@Override
	public AbstractComponent getComponentFromStore(String componentId) {
		Cache c = entityManagerFactory.getCache();
		c.evict(ComponentSpec.class, componentId);
		
		return getComponent(componentId);
	}
	
	@Override
	public AbstractComponent getComponent(String componentId) {
		EntityManager em = entityManagerFactory.createEntityManager();
		ComponentSpec cs = null;
		try {
			cs = em.find(ComponentSpec.class, componentId);
		} finally {
			em.close();
		}
		if (cs != null) {
			return createAbstractComponent(cs);
		}
		return null;
	}

	@Override
	public Collection<AbstractComponent> getReferences(AbstractComponent component) {
		Collection<AbstractComponent> references = new ArrayList<AbstractComponent>();
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<ComponentSpec> q = em.createNamedQuery("ComponentSpec.findReferencingComponents", ComponentSpec.class);
			q.setParameter("component", component.getComponentId());
			List<ComponentSpec> referencingComponents = q.getResultList();
			for (ComponentSpec cs:referencingComponents) {
				references.add(createAbstractComponent(cs));
			}
		} finally {
			em.close();
		}
		
		return references;
	}

	private boolean hasWorkUnitBeenStarted() {
		return getCurrentComponents() != Collections.<AbstractComponent>emptySet();
	}
	
	@Override
	public void startRelatedOperations() {
		assert entityManagerFactory != null;
		if (hasWorkUnitBeenStarted()) {
			throw new IllegalStateException("startRelatedOperations cannot be invoked until the last operations has been closed");
		}
		components.set(new TreeSet<AbstractComponent>(COMPONENT_ID_COMPARATOR));
	}

	@Override
	public void completeRelatedOperations(boolean save) {
		try {
			if (save) {
				persist(components.get());
			}
		} finally {
			components.remove();
		}
	}
	
	@Override
	public void addComponentToWorkUnit(AbstractComponent component) {
		if (hasWorkUnitBeenStarted()) { 
			getCurrentComponents().add(component);
		}
	}
	
	public Collection<AbstractComponent> getCurrentComponents() {
		return components.get();
	}

	@Override
	public Set<String> getAllUsers() {
		EntityManager em = entityManagerFactory.createEntityManager();
		Set<String> userNames = null;
		try {
			TypedQuery<MctUsers> q = em.createNamedQuery("MctUsers.findAll", MctUsers.class);
			List<MctUsers> users = q.getResultList();
			userNames = new HashSet<String>(users.size());
			for (MctUsers u:users) {
				userNames.add(u.getUserId());
			}
		} finally {
			em.close();
		}
		
		return userNames;
	}
	
	@Override
	public Collection<String> getUsersInGroup(String group) {
		EntityManager em = entityManagerFactory.createEntityManager();
		List<String> userNames = null;
		try {
			TypedQuery<MctUsers> q = em.createNamedQuery("MctUsers.findByGroup", MctUsers.class);
			q.setParameter("group", group);
			List<MctUsers> users = q.getResultList();
			userNames = new ArrayList<String>(users.size());
			for (MctUsers u:users) {
				userNames.add(u.getUserId());
			}
		} finally {
			em.close();
		}
		
		return userNames;
	}
	
	private ExtendedProperties createExtendedProperties(String props) {
			Unmarshaller unmarshaller;
			try {
				unmarshaller = propContext.createUnmarshaller();
				InputStream is = new ByteArrayInputStream(props.getBytes("ASCII"));
				return ExtendedProperties.class.cast(unmarshaller.unmarshal(is));
			} catch (JAXBException je) {
				throw new RuntimeException(je);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
	}
	
	private String generateStringFromView(ExtendedProperties viewState) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Marshaller marshaller = propContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "ASCII");
			marshaller.marshal(viewState, out);
			return out.toString("ASCII");
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ViewState createViewState(String viewType, String componentId, ExtendedProperties viewData, EntityManager em, ComponentSpec cs) {
		ViewStatePK viewStatePK = new ViewStatePK();
		viewStatePK.setComponentId(componentId);
		viewStatePK.setViewType(viewType);
		ViewState vs = em.find(ViewState.class, viewStatePK);
		if (vs == null) {
			vs = new ViewState();
			vs.setViewStatePK(viewStatePK);
			if (cs.getViewStateCollection() == null) {
				cs.setViewStateCollection(new ArrayList<ViewState>());
			}
			cs.getViewStateCollection().add(vs);
		} 
		vs.setViewInfo(generateStringFromView(viewData));
		return vs;
	}
	
	private void updateComponentSpec(AbstractComponent ac,ComponentSpec cs,EntityManager em, boolean fullSave) {
		cs.setComponentId(ac.getComponentId());
		cs.setComponentName(ac.getDisplayName());
		cs.setOwner(ac.getOwner());
		cs.setCreatorUserId(ac.getCreator());
		cs.setComponentType(ac.getClass().getName());
		cs.setExternalKey(ac.getExternalKey());
		if (!fullSave) {
			return;
		}
		cs.setObjVersion(ac.getVersion());
		ModelStatePersistence persistence = ac.getCapability(ModelStatePersistence.class);
		if (persistence != null) {
			cs.setModelInfo(persistence.getModelState());
		}
		
		// save relationships
		// non optimal implementation as this will require a query to get the relationships
		cs.setReferencedComponents(new ArrayList<ComponentSpec>(ac.getComponents().size()));
		for (AbstractComponent c : ac.getComponents()) {
			ComponentSpec refCs = em.find(ComponentSpec.class, c.getComponentId());
			if (refCs == null) {
				// this can be null if the component has been deleted
				continue;
			}
			cs.getReferencedComponents().add(refCs);
		}
		
		// save views
		ComponentInitializer ci = ac.getCapability(ComponentInitializer.class);
		if (ci.getMutatedViewRoleProperties() != null) {
			for (Entry<String,ExtendedProperties> viewEntry : ci.getAllViewRoleProperties().entrySet()) {
				createViewState(viewEntry.getKey(), cs.getComponentId(), viewEntry.getValue(),em,cs);
			}
		}
	}
	
	@Override
	public void persist(Collection<AbstractComponent> componentsToPersist) {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();
			// first persist all new components, without relationships, model, and view states 
			for (AbstractComponent nc : componentsToPersist) {
				if (nc.getCreationDate() == null) {
					ComponentSpec cs = new ComponentSpec();
					updateComponentSpec(nc, cs, em, false);
					em.persist(cs);
				}
			}
			
			// now persist the data
			for (AbstractComponent c : componentsToPersist) {
				updateComponentSpec(c, em.find(ComponentSpec.class, c.getComponentId()), em, true);
			}
			em.flush();
			em.getTransaction().commit();
			for (AbstractComponent c : componentsToPersist) {
				ComponentInitializer ci = c.getCapability(ComponentInitializer.class);
				ci.componentSaved();
				if (c.getCreationDate() == null) {
					ci.setCreationDate(em.find(ComponentSpec.class, c.getComponentId()).getDateCreated());
				}
				c.getCapability(Updatable.class).setVersion(em.find(ComponentSpec.class, c.getComponentId()).getObjVersion());
				c.componentSaved();
				List<WeakReference<AbstractComponent>> list = cache.get(c.getComponentId());
				if (list == null) {
					list = Collections.synchronizedList(new LinkedList<WeakReference<AbstractComponent>>());
					cache.put(c.getComponentId(), list);
				}
				list.add(new WeakReference<AbstractComponent>(c));
			}
		} catch(OptimisticLockException ole) {
			throw new gov.nasa.arc.mct.api.persistence.OptimisticLockException(ole);
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@Override
	public void delete(Collection<AbstractComponent> componentsToDelete) {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();
			for (AbstractComponent component:componentsToDelete) {
				ComponentSpec componentToDelete = em.find(ComponentSpec.class, component.getComponentId());
				if (componentToDelete == null) {
					// component has already been deleted from database but not refreshed in the user interface
					continue;
				}
				TypedQuery<ComponentSpec> q = em.createNamedQuery("ComponentSpec.findReferencingComponents", ComponentSpec.class);
				q.setParameter("component", component.getComponentId());
				List<ComponentSpec> referencingComponents = q.getResultList();
				for (ComponentSpec cs:referencingComponents) {
					cs.getReferencedComponents().remove(componentToDelete);
				}
				em.remove(componentToDelete);
			}
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
		
	}
	
	@Override
	public boolean hasComponentsTaggedBy(String tagId) {
		EntityManager em = null;
		try {
			em = entityManagerFactory.createEntityManager();
			TypedQuery<TagAssociation> q = em.createNamedQuery("TagAssociation.getComponentsByTag", TagAssociation.class);
			q.setParameter("tagId", tagId);
			q.setMaxResults(1);
			return !q.getResultList().isEmpty();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}
	
	@Override
	public List<AbstractComponent> getReferencedComponents(
			AbstractComponent component) {
		List<AbstractComponent> references = new ArrayList<AbstractComponent>();
		EntityManager em = entityManagerFactory.createEntityManager();
		if (component.getComponentId() != null) {
			try {
				ComponentSpec reference = em.find(ComponentSpec.class, component.getComponentId());
					if (reference != null) {
					List<ComponentSpec> referencedComponents = reference.getReferencedComponents();
					for (ComponentSpec cs:referencedComponents) {
						if (cs != null) {
							AbstractComponent ac = createAbstractComponent(cs);
							references.add(ac);
						}
					}
				}
			} finally {
				em.close();
			}
		}
		
		return references;
	}
	
	@Override
	public User getUser(String userId) {
		EntityManager em = entityManagerFactory.createEntityManager();
		User u = null;
		try {
			MctUsers users = em.find(MctUsers.class, userId);
			if (users != null) {
				final String discipline = users.getDisciplineId().getDisciplineId();
				final String uId = users.getUserId();
				
				u = new User() {
					@Override
					public String getDisciplineId() {
						return discipline;
					}
					
					@Override
					public String getUserId() {
						return uId;
					}
					
					@Override
					public User getValidUser(String userID) {
						return getUser(userID);
					}
				};
			}
		} finally {
			em.close();
		}
		return u;
	}
	
    /**
     * Finds all the components by base display name regex pattern.
     * @param pattern - regex.
     * @param props <code>Properties</code> set arguments for SQL query.
     * @return QueryResult - search results.
     */
    @SuppressWarnings("unchecked")
    public QueryResult findComponentsByBaseDisplayedNamePattern(String pattern, Properties props) {
        String username = PlatformAccess.getPlatform().getCurrentUser().getUserId();
        
        pattern = pattern.isEmpty() ? "%" : pattern.replace('*', '%');
        
        String countQuery = "select count(*) from component_spec c "
                        + "where c.creator_user_id like :creator "
                        + "and c.component_id not in (select component_id from component_spec where component_type = 'gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent' and component_name = 'All')"
                        + "and (c.component_type != 'gov.nasa.arc.mct.core.components.MineTaxonomyComponent' or c.owner = :owner) "
                        + "and c.component_name like :pattern ;";
                
        String entitiesQuery = "select c.* from component_spec c "
            + "where c.creator_user_id like :creator "
            + "and c.component_id not in (select component_id from component_spec where component_type = 'gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent' and component_name = 'All')"
            + "and (c.component_type != 'gov.nasa.arc.mct.core.components.MineTaxonomyComponent' or c.owner = :owner) "
            + "and c.component_name like :pattern "
            + "limit 100";
        
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
        	Query q = em.createNativeQuery(countQuery);
        	           
            q.setParameter("pattern", pattern);
            q.setParameter("owner", username);
            q.setParameter("creator", (props != null && props.get("creator") != null) ? props.get("creator") : "%" );    
            int count = ((Number) q.getSingleResult()).intValue();

            q = em.createNativeQuery(entitiesQuery, ComponentSpec.class);
            q.setParameter("pattern", pattern);
            q.setParameter("owner", username);
            q.setParameter("creator", (props != null && props.get("creator") != null) ? props.get("creator") : "%" );    
            List<ComponentSpec> daoObjects = q.getResultList();
            return new QueryResult(count, daoObjects);
        } catch (Exception t) {
            LOGGER.error("error executing query", t);
            return null;
        } finally {
        	em.close();
        }        
    }
    
    AbstractComponent newAbstractComponent(ComponentSpec cs) {
    	return PlatformAccess.getPlatform().getComponentRegistry().newInstance(cs.getComponentType());
    }

    private AbstractComponent createAbstractComponent(ComponentSpec cs) {
    	AbstractComponent ac = newAbstractComponent(cs);
		ComponentInitializer initializer = ac.getCapability(ComponentInitializer.class);
		initializer.setCreationDate(cs.getDateCreated());
		initializer.setCreator(cs.getCreatorUserId());
		initializer.setOwner(cs.getOwner());
		initializer.setId(cs.getComponentId());
		ac.setExternalKey(cs.getExternalKey());
		ac.setDisplayName(cs.getComponentName());
		ac.getCapability(Updatable.class).setVersion(cs.getObjVersion());
        ModelStatePersistence persister = ac.getCapability(ModelStatePersistence.class);
        if (persister != null) {
            persister.setModelState(cs.getModelInfo());
        }
		
		// Add ac to cache
		List<WeakReference<AbstractComponent>> list = cache.get(cs.getComponentId());
		if (list == null) {
			list = Collections.synchronizedList(new LinkedList<WeakReference<AbstractComponent>>());
		}
		list.add(new WeakReference<AbstractComponent>(ac));
		cache.put(cs.getComponentId(), list);
		return ac;
    }
    
	@Override
	public <T extends AbstractComponent> T getComponent(String externalKey, Class<T> componentType) {
		EntityManager em = entityManagerFactory.createEntityManager();
		T comp = null;
		try {
			TypedQuery<ComponentSpec> q = em.createQuery("SELECT c FROM ComponentSpec c WHERE c.externalKey = :externalKey and c.componentType = :componentType", ComponentSpec.class);
			q.setParameter("externalKey", externalKey);
			q.setParameter("componentType", componentType.getName());
			List<ComponentSpec> cs = q.getResultList();
			if (!cs.isEmpty()) {
				AbstractComponent ac = createAbstractComponent(cs.get(0));
				comp = componentType.cast(ac);
			}
		} finally {
			em.close();
		}
		return comp;
	}
	
	private Date getCurrentTimeFromDatabase() {
		Platform p = PlatformAccess.getPlatform();
		if (p==null) {
			return null;
		}
		User currentUser = PlatformAccess.getPlatform().getCurrentUser();
		if (currentUser == null)
			return null;
		
		String userId = currentUser.getUserId();
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			TypedQuery<Date> q = em.createQuery("SELECT CURRENT_TIMESTAMP FROM ComponentSpec c WHERE c.owner = :owner", Date.class);
			q.setParameter("owner", userId);
			return q.getSingleResult();
		} finally {
			em.close();
		}
	}

	private void iterateOverChangedComponents(ChangedComponentVisitor v) {		
		if (lastPollTime == null) {
			lastPollTime = getCurrentTimeFromDatabase();
			if (lastPollTime == null)
				return;
		}
        String query = "SELECT CURRENT_TIMESTAMP, c FROM ComponentSpec c WHERE c.lastModified BETWEEN ?1 AND CURRENT_TIMESTAMP";
        EntityManager em = entityManagerFactory.createEntityManager();        
        try {
            Query q = em.createQuery(query);
            q.setParameter(1, lastPollTime, TemporalType.TIMESTAMP);
            final int MAX_CACHE_SIZE = 500;
            int iteration = 0;
            boolean done = false;
            while(!done) {
	            q.setFirstResult(MAX_CACHE_SIZE * iteration);
	            q.setMaxResults(MAX_CACHE_SIZE);
	            @SuppressWarnings("rawtypes")
				List resultList = q.getResultList();
	            for (int i=0; i<resultList.size(); i++) {
	            	for (Object obj : (Object[]) resultList.get(i)) {
	            		if (obj instanceof ComponentSpec)
	            			v.operateOnComponent((ComponentSpec) obj);
	            		if (obj instanceof Date)
	            			lastPollTime = (Date) obj;
	            	}
	            }    	
            	done = resultList.size() < MAX_CACHE_SIZE;
            	iteration++;
            	em.clear();
            }
        } catch (Exception t) {
            LOGGER.error("error executing query", t);
        } finally {
            em.close();
        }        

	}
	
	private void cleanCacheIfNecessary(String componentId, int latestVersion) {
		Cache c = entityManagerFactory.getCache();
		if (c.contains(ComponentSpec.class, componentId)) {
			EntityManager em = entityManagerFactory.createEntityManager();
			ComponentSpec cs = em.find(ComponentSpec.class, componentId);
			if (cs != null && cs.getObjVersion() < latestVersion) {
				c.evict(ComponentSpec.class, componentId);
			}
			em.close();
		}
	}
	
    private void updateComponentIfNecessary(final ComponentSpec c, final Collection<AbstractComponent> cachedComponents) {
    	Collection<AbstractComponent> delegateComponets = new ArrayList<AbstractComponent>();
    	for (final AbstractComponent ac : cachedComponents) {
    		Updatable updatable = ac.getCapability(Updatable.class);
    		updatable.setStaleByVersion(c.getObjVersion());
    		cleanCacheIfNecessary(c.getComponentId(), c.getObjVersion());
    		if (ac.getWorkUnitDelegate() != null) {
    			ac.getWorkUnitDelegate().getCapability(Updatable.class).setStaleByVersion(Integer.MAX_VALUE);
    			delegateComponets.add(ac.getWorkUnitDelegate());
    		}
    	}
    	cachedComponents.addAll(delegateComponets);
    	for (final AbstractComponent ac: cachedComponents) {
    		if (ac.isStale()) {
	            ac.resetComponentProperties(new AbstractComponent.ResetPropertiesTransaction() {
	                
	                @Override
	                public void perform() {
	                    Updatable updatable = ac.getCapability(Updatable.class);
	                    updatable.notifyStale();
	                }
	            });
	            LOGGER.debug("{} updated", c.getComponentName());
	        }
    	}
    }

    private int pollCounter = 0;
    
	@Override
	public void updateComponentsFromDatabase() {
		pollCounter++;
        iterateOverChangedComponents(
                new ChangedComponentVisitor() {
                    @Override
                    public void operateOnComponent(ComponentSpec c) {
                    	List<WeakReference<AbstractComponent>> list = cache.get(c.getComponentId());
                        if (list != null && !list.isEmpty()) {
                        	Collection<AbstractComponent> cachedComponents = new ArrayList<AbstractComponent>(list.size());
                        	synchronized(list) {
                        		Iterator<WeakReference<AbstractComponent>> it = list.iterator();
                        		while (it.hasNext()) {
                        			AbstractComponent ac = it.next().get();
                        			if (ac != null) {
                        				cachedComponents.add(ac);
                        			} else {
                        				it.remove();
                        			}
                        		}
                        	}
                        	if (!cachedComponents.isEmpty())
                        		updateComponentIfNecessary(c, cachedComponents);
                        }
                    }
                }
            );
        if (pollCounter % 1000 == 0)
        	cleanCache();
	}
	
    public interface ChangedComponentVisitor {
        /**
         * This method is invoked for each component that has changed. 
         * @param component that has changed 
         */
        void operateOnComponent(ComponentSpec component);
    }

    @Override
	public List<AbstractComponent> getBootstrapComponents() {
		List<ComponentSpec> cslist = null;
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			String userId = PlatformAccess.getPlatform().getCurrentUser() == null ? null : PlatformAccess.getPlatform().getCurrentUser().getUserId();
			TypedQuery<ComponentSpec> q = em.createQuery("SELECT t.componentSpec FROM TagAssociation t where t.tag.tagId = 'bootstrap:admin' or (t.tag.tagId = 'bootstrap:creator' and t.componentSpec.creatorUserId = :user)", ComponentSpec.class);
			q.setParameter("user", userId);
			cslist = q.getResultList();
		} finally {
			em.close();
		}
		
		if (cslist != null) {
			List<AbstractComponent> aclist = new ArrayList<AbstractComponent>();
			for (ComponentSpec cs : cslist) {
				AbstractComponent ac = createAbstractComponent(cs);
				aclist.add(ac);
			}
			return aclist;
		}
			
		return null;				
	}
    
    private void cleanCache() {
    	Iterator<String> iterator = cache.keySet().iterator();
    	while(iterator.hasNext()) {
    		String componentId = iterator.next();
    		List<WeakReference<AbstractComponent>> list = cache.get(componentId);
    		boolean canBeDeleted = true;
    		synchronized(list) {
	    		for (WeakReference<AbstractComponent> r : list) {
	    			if (r.get() != null) {
	    				 canBeDeleted = false;
	    			}
	    		}
    		}
    		if (canBeDeleted)
    			iterator.remove();
    	}
    }

	@Override
	public void addNewUser(String userId, String groupId, AbstractComponent mysandbox, AbstractComponent dropbox) {
		String userDropboxesId = PlatformAccess.getPlatform().getUserDropboxes().getComponentId();
	 	EntityManager em = entityManagerFactory.createEntityManager();
	 	try {
	 		em.getTransaction().begin();
	 		Disciplines group = em.find(Disciplines.class, groupId);
	 		if (group == null) {
	 			group = new Disciplines();
	 			group.setDisciplineId(groupId);
	 			em.persist(group);
	 		}
	 		MctUsers user = new MctUsers();
	 		user.setUserId(userId);
	 		user.setDisciplineId(group);
	 		em.persist(user);
	
	 		ComponentSpec mysandboxComponentSpec = new ComponentSpec();
	 		updateComponentSpec(mysandbox, mysandboxComponentSpec, em, true);
	 		em.persist(mysandboxComponentSpec);

	 		TagAssociationPK tagAssociationPK = new TagAssociationPK();
	 		tagAssociationPK.setComponentId(mysandbox.getComponentId());
	 		tagAssociationPK.setTagId("bootstrap:creator");
	 		TagAssociation tagAssociation = new TagAssociation();
	 		tagAssociation.setTagAssociationPK(tagAssociationPK);
	 		em.persist(tagAssociation);

	 		ComponentSpec dropboxComponentSpec = new ComponentSpec();
	 		updateComponentSpec(dropbox, dropboxComponentSpec, em, true);
	 		em.persist(dropboxComponentSpec);
		
	 		ComponentSpec userDropboxes = em.find(ComponentSpec.class, userDropboxesId);
	 		userDropboxes.getReferencedComponents().add(dropboxComponentSpec);    		
	 		mysandboxComponentSpec.getReferencedComponents().add(dropboxComponentSpec);
		
	 		em.persist(userDropboxes);
	 		em.persist(mysandboxComponentSpec);
	 		em.getTransaction().commit();
	 	} finally {
	 		if(em.getTransaction().isActive())
	 			em.getTransaction().rollback();
 			em.close();
	 	}
	}

	@Override
	public Map<String, ExtendedProperties> getAllProperties(String componentId) {
		EntityManager em = entityManagerFactory.createEntityManager();
		Map<String, ExtendedProperties> properties = new HashMap<String, ExtendedProperties>();
		try {
			ComponentSpec cs = em.find(ComponentSpec.class, componentId);
			if (cs != null) {
				for (ViewState vs: cs.getViewStateCollection()) {
					properties.put(vs.getViewStatePK().getViewType(), createExtendedProperties(vs.getViewInfo()));
				}
			}
			return properties;
		} finally {
			em.close();
		}
	}
	
	@Override
	public AbstractComponent getComponent(String externalKey,
			String componentType) {
		EntityManager em = entityManagerFactory.createEntityManager();
		AbstractComponent ac = null;
		try {
			TypedQuery<ComponentSpec> q = em
					.createQuery(
							"SELECT c FROM ComponentSpec c "
									+ "WHERE c.externalKey = :externalKey and c.componentType = :componentType",
							ComponentSpec.class);
			q.setParameter("externalKey", externalKey);
			q.setParameter("componentType", componentType);
			List<ComponentSpec> cs = q.getResultList();
			if (!cs.isEmpty()) {
			    ac = createAbstractComponent(cs.get(0));
			} 

		} finally {
			em.close();
		}
		return ac;
	}
	
	@Override
	public void tagComponents(String tag,
			Collection<AbstractComponent> components) {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();
			Tag t = em.find(Tag.class, tag);
			if (t == null) {
				t = new Tag();
				t.setTagId(tag);
				em.persist(t);
			}

			for (AbstractComponent component:components) {
				ComponentSpec cs = em.find(ComponentSpec.class, component.getComponentId());
				TagAssociation association = new TagAssociation();
				TagAssociationPK tagPK = new TagAssociationPK();
				tagPK.setComponentId(cs.getComponentId());
				tagPK.setTagId(t.getTagId());
				association.setTagAssociationPK(tagPK);
				cs.getTagAssociationCollection().add(association);
			}
			
			em.getTransaction().commit();
		} finally {
			if(em.getTransaction().isActive())
	 			em.getTransaction().rollback();
			em.close();
		}
		
	}
}
