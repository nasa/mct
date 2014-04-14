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
import gov.nasa.arc.mct.components.JAXBModelStatePersistence;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.dbpersistence.dao.ComponentSpec;
import gov.nasa.arc.mct.dbpersistence.dao.Disciplines;
import gov.nasa.arc.mct.dbpersistence.dao.MctUsers;
import gov.nasa.arc.mct.dbpersistence.dao.Tag;
import gov.nasa.arc.mct.dbpersistence.dao.TagAssociation;
import gov.nasa.arc.mct.dbpersistence.dao.TagAssociationPK;
import gov.nasa.arc.mct.dbpersistence.dao.ViewState;
import gov.nasa.arc.mct.dbpersistence.dao.ViewStatePK;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.services.internal.component.User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestPersistenceServiceImpl {
	
	private PersistenceServiceImpl serviceImpl;
	private EntityManagerFactory factory;
	private EntityManager em;
	@Mock private Platform mockPlatform;
	@Mock private PolicyManager mockPolicyManager;
	
	@BeforeMethod
	protected void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		Properties properties = new Properties();
		properties.put("javax.persistence.jdbc.driver","org.apache.derby.jdbc.EmbeddedDriver");
		properties.put("javax.persistence.jdbc.url","jdbc:derby:memory:testdb;create=true");
		properties.put("hibernate.hbm2ddl.auto","create");
		properties.put("hibernate.show_sql" ,"false"); 
		properties.put("hibernate.format_sql", "true");
		
		serviceImpl = new PersistenceServiceImpl() {
			@Override
			AbstractComponent newAbstractComponent(ComponentSpec cs) {
				if (TestAbstractComponent.class.getName().equals(cs.getComponentType())) {
					return new TestAbstractComponent();
				}
				return new AbstractComponent() {
					
				};
			}
		};
		
		serviceImpl.setEntityManagerProperties(properties);
		
		Field f = PersistenceServiceImpl.class.getDeclaredField("entityManagerFactory");
		f.setAccessible(true);
		factory = (EntityManagerFactory) f.get(serviceImpl);
		em = factory.createEntityManager();
		Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(new ExecutionResult(null,true,""));
		(new PlatformAccess()).setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(serviceImpl);
	}
	
	@AfterMethod
	protected void tearDown() throws Exception {
		factory.close();
		(new PlatformAccess()).setPlatform(null);
	}
	
	private void createUsers(List<String> users, String group) {
		em.getTransaction().begin();
		Disciplines d = new Disciplines();
		d.setDescription("test discipline");
		d.setDisciplineId(group);
		em.persist(d);
		for (String user:users) {
			MctUsers userDao = new MctUsers();
			userDao.setUserId(user);
			userDao.setDisciplineId(d);
			em.persist(userDao);
		}
		em.getTransaction().commit();
	}
	
	@Test
	public void testDoubleCloseOperations() {
		serviceImpl.startRelatedOperations();
		serviceImpl.completeRelatedOperations(true);
		serviceImpl.completeRelatedOperations(true);
	}
	
	@Test
	public void testAddNewUser() {
		String userId = "testUser";
		String sandboxId = "mysandbox";
		
		User mockUser = Mockito.mock(User.class);
		AbstractComponent mockBoxes = createAbstractComponent("Drop Boxes", "dropboxes");
		Mockito.when(mockPlatform.getUserDropboxes()).thenReturn(mockBoxes);
		Mockito.when(mockPlatform.getCurrentUser()).thenReturn(mockUser);
		Mockito.when(mockUser.getUserId()).thenReturn(userId);
		
		em.getTransaction().begin();
		em.persist(createComponentSpec(
				"dropboxes", "xyz",	"123", "123", "xyz", 
				new ArrayList<Tag>(), Collections.<String,String>emptyMap()));
		em.getTransaction().commit();		
		
		// Verify pre-condition, risk pre-populating bootstrap cache
		// See https://github.com/nasa/mct/issues/245
		Assert.assertEquals(serviceImpl.getBootstrapComponents().size(), 0);
		
		serviceImpl.bind(mockPlatform);
		serviceImpl.addNewUser(
				userId, 
				"testGroup", 
				createAbstractComponent("My Sandbox", sandboxId  , userId), 
				createAbstractComponent("My Dropbox", "mydropbox", userId));
 
		Assert.assertEquals(serviceImpl.getBootstrapComponents().get(0).getComponentId(), sandboxId);
	}
	
	@Test
	public void testFindComponentsByBaseDisplayedNamePattern() {
		User mockUser = Mockito.mock(User.class);
		Mockito.when(mockPlatform.getCurrentUser()).thenReturn(mockUser);
		Mockito.when(mockUser.getUserId()).thenReturn("testUser");
		
		String[] bdns = {
				"hit 0",
				"miss 0",
				"hit 1",
				"miss 0",
				"hit 2"				
		};
		em.getTransaction().begin();
		for (int i = 0 ; i < bdns.length; i++) {
			em.persist(createComponentSpec(
					"search" + i,
					(i==0) ? "testUser" : "otherUser",
					bdns[i],	
					"123", "xyz", 
					new ArrayList<Tag>(), Collections.<String,String>emptyMap()));
		}
		em.getTransaction().commit();
		
		serviceImpl.bind(mockPlatform);
	

		Assert.assertEquals(
				serviceImpl.findComponentsByBaseDisplayedNamePattern("", null).getCount(),
				5);
		
		Assert.assertEquals(
				serviceImpl.findComponentsByBaseDisplayedNamePattern("hit*", null).getCount(),
				3);	
		
		Assert.assertEquals(
				serviceImpl.findComponentsByBaseDisplayedNamePattern("hit*", new Properties()).getCount(),
				3);
	
		Properties creatorProps = new Properties();
		creatorProps.put("creator", "testUser");
		Assert.assertEquals(
				serviceImpl.findComponentsByBaseDisplayedNamePattern("hit*", creatorProps).getCount(),
				1);
		
		creatorProps.put("creator", "otherUser");
		Assert.assertEquals(
				serviceImpl.findComponentsByBaseDisplayedNamePattern("hit*", creatorProps).getCount(),
				2);
	}
	
	@Test
	public void testGetAllUsers() {
		// setup some users
		List<String> users = Arrays.asList("user1","user2","user3");
		createUsers(users,"group1");
		
		// make sure they can be retrieved
		Set<String> allUsers = serviceImpl.getAllUsers();
		Assert.assertEquals(allUsers.size(), users.size());
		Assert.assertTrue(allUsers.containsAll(users));
	}
	
	@Test
	public void testGetUsersInGroups() {
		// setup some users
		List<String> users = Arrays.asList("user1","user2","user3");
		createUsers(users,"group1");
		
		List<String> users2 = Arrays.asList("user12","user22");
		createUsers(users2,"group2");
		
		// make sure they can be retrieved
		Collection<String> allUsers = serviceImpl.getUsersInGroup("group1");
		Assert.assertEquals(allUsers.size(), users.size());
		Assert.assertTrue(allUsers.containsAll(users));
		
		allUsers = serviceImpl.getUsersInGroup("group2");
		Assert.assertEquals(allUsers.size(), users2.size());
		Assert.assertTrue(allUsers.containsAll(users2));
	}
	
	private ComponentSpec createComponentSpec(String id, String userId, String name, String type, String owner, List<Tag> tags, Map<String,String> views) {
		ComponentSpec cs = new ComponentSpec();
		cs.setComponentId(id);
		cs.setCreatorUserId(userId);
		cs.setComponentName(name);
		cs.setComponentType(type);
		cs.setOwner(owner);
		cs.setReferencedComponents(new ArrayList<ComponentSpec>());
		List<ViewState> viewStates = new ArrayList<ViewState>();
		for (Entry<String,String> e:views.entrySet()) {
			ViewState vs = new ViewState();
			ViewStatePK viewStatePK = new ViewStatePK();
			viewStatePK.setComponentId(cs.getComponentId());
			viewStatePK.setViewType(e.getKey());
			vs.setViewStatePK(viewStatePK);
			vs.setViewInfo(e.getValue());
			viewStates.add(vs);
		}
		cs.setViewStateCollection(viewStates);
		
		cs.setTagAssociationCollection(new ArrayList<TagAssociation>());
		for (Tag aTag:tags) {
			TagAssociation association = new TagAssociation();
			TagAssociationPK tagPK = new TagAssociationPK();
			tagPK.setComponentId(cs.getComponentId());
			tagPK.setTagId(aTag.getTagId());
			association.setTagAssociationPK(tagPK);
			association.setTagProperty(aTag.getTagProperty());
			cs.getTagAssociationCollection().add(association);
		}
		
		return cs;
	}
	
	@DataProvider(name="taggedComponentTests")
	Object[][] hasTaggedComponentsTestCases() {
		return new Object[][] {
			new Object[] {"validTag", true},
			new Object[] {"invalidTag", false}
		};
	}
	
	@Test(dataProvider="taggedComponentTests")
	public void testHasTaggedComponents(String tag, boolean expectedResult) {
		List<Tag> taggedComponents = Collections.<Tag>emptyList();
		if (expectedResult) {
			Tag t = new Tag();
			t.setTagId(tag);
			t.setTagProperty("property");
			taggedComponents = Collections.singletonList(t);
		}
		em.getTransaction().begin();
		for (Tag t:taggedComponents) {
			em.persist(t);
		}
		em.persist(createComponentSpec("123", "xyz", "123", "123", "xyz", taggedComponents, Collections.<String,String>emptyMap()));
		em.getTransaction().commit();
		em.clear();
		
		Assert.assertEquals(serviceImpl.hasComponentsTaggedBy(tag),expectedResult);
	}
	
	@Test
	public void testTagComponents() {
		em.getTransaction().begin();
		em.persist(createComponentSpec("123", "xyz", "123", "123", "xyz", Collections.<Tag>emptyList(), Collections.<String,String>emptyMap()));
		em.getTransaction().commit();
		
		Assert.assertFalse(serviceImpl.hasComponentsTaggedBy("testTag"));
		serviceImpl.tagComponents("testTag", Collections.singleton(serviceImpl.getComponent("123")));
		Assert.assertTrue(serviceImpl.hasComponentsTaggedBy("testTag"));
	}
	
	public static class TestAbstractComponent extends AbstractComponent {
		private AtomicReference<TestModel> model = new AtomicReference<TestModel>(new TestModel());
		
		public void setModelValue(String v) {
			model.get().setValue(v);
		}
		
		public String getModelValue() {
			return model.get().getValue();
		}
		
		@Override
		public String toString() {
			return "compId = " + getComponentId() + "  name = " + getDisplayName(); 
		}
		
		@Override
		protected <T> T handleGetCapability(Class<T> capability) {
			if (ModelStatePersistence.class.isAssignableFrom(capability)) {
				JAXBModelStatePersistence<TestModel> persistence = new JAXBModelStatePersistence<TestModel>() {

					@Override
					protected TestModel getStateToPersist() {
						return model.get();
					}

					@Override
					protected void setPersistentState(TestModel modelState) {
						model.set(modelState);
					}

					@Override
					protected Class<TestModel> getJAXBClass() {
						return TestModel.class;
					}
			        
				};
				
				return capability.cast(persistence);
			}
			return super.handleGetCapability(capability);
		}
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class TestModel {
		private String value;
		
		public String getValue()  {
			return value;
		}

		public void setValue(String v) {
			value = v;
		}
	}
	
	private TestAbstractComponent createAbstractComponent(String componentName, String id) {
		return createAbstractComponent(componentName, id, "creator");
	}
	
	private TestAbstractComponent createAbstractComponent(String componentName, String id, String creator) {
		TestAbstractComponent comp = new TestAbstractComponent();
		ComponentInitializer ci = comp.getCapability(ComponentInitializer.class);
		ci.setId(id);
		comp.setDisplayName(componentName);
		comp.setModelValue(componentName);
		comp.setOwner("test");
		ci.setCreator(creator);
		comp.save();
		
		return comp;
	}
	
	@Test
	public void testViewStateLoading() {
		final TestAbstractComponent testComp = createAbstractComponent("test","0");
		ExtendedProperties ep = new ExtendedProperties(); 
		ep.addProperty("test", "abc");
		testComp.getCapability(ComponentInitializer.class).setViewRoleProperty("test", ep);
		serviceImpl.persist(Arrays.<AbstractComponent>asList(testComp));
		Map<String, ExtendedProperties> returnedEp = serviceImpl.getAllProperties(testComp.getComponentId());
		Assert.assertTrue(returnedEp.size() == 1);
		Assert.assertEquals(returnedEp.get("test").getProperty("test", String.class), "abc");
	}
	
	@Test
	public void testPersist() {
		final TestAbstractComponent grandparent = createAbstractComponent("grandparent", "0");
		final TestAbstractComponent parent = createAbstractComponent("parent", "1");
		final TestAbstractComponent child = createAbstractComponent("child", "2");
		List<AbstractComponent> comps = Arrays.<AbstractComponent>asList(grandparent,parent,child);
		grandparent.addDelegateComponent(parent);
		parent.addDelegateComponent(child);
		for (AbstractComponent ac:comps) {	
			Assert.assertTrue(ac.isDirty());
			Assert.assertNull(ac.getCreationDate());
		}
		
		serviceImpl.persist(Arrays.<AbstractComponent>asList(grandparent,parent,child));
		for (AbstractComponent ac:comps) {	
			Assert.assertFalse(ac.isDirty());
			Assert.assertNotNull(ac.getCreationDate());
		}
		
		TestAbstractComponent persistedGrandparent = (TestAbstractComponent) serviceImpl.getComponent(grandparent.getComponentId());
		Assert.assertEquals(persistedGrandparent.getDisplayName(), grandparent.getDisplayName());
		Assert.assertEquals(persistedGrandparent.getComponents().size(), 1, persistedGrandparent.getComponents().toString());
		Assert.assertEquals(persistedGrandparent.getModelValue(), grandparent.getDisplayName());
		Assert.assertEquals(persistedGrandparent.getComponents().iterator().next().getComponentId(), parent.getComponentId());
		
		TestAbstractComponent persistedParent = (TestAbstractComponent) serviceImpl.getComponent(parent.getComponentId());
		Assert.assertEquals(persistedParent.getDisplayName(), parent.getDisplayName());
		Assert.assertEquals(persistedParent.getComponents().size(), 1);
		Assert.assertEquals(persistedParent.getDisplayName(), persistedParent.getDisplayName());
		Assert.assertEquals(persistedParent.getComponents().iterator().next().getComponentId(), child.getComponentId());
	}
	
	@Test
	public void testDelete() {
		em.getTransaction().begin();
		final String childId = "3";
		Tag t3 = new Tag();
		t3.setTagId("t3");
		em.persist(t3);
		ComponentSpec child = createComponentSpec(childId, "xyz", "deleted", "123", "xyz", Collections.<Tag>emptyList(), Collections.<String,String>emptyMap());
		ComponentSpec parent1 = createComponentSpec("1", "xyz", "parent1", "123", "xyz", Collections.<Tag>emptyList(), Collections.<String,String>emptyMap());
		ComponentSpec parent2 = createComponentSpec("2", "xyz", "parent2", "123", "xyz", Collections.<Tag>emptyList(), Collections.<String,String>emptyMap());
		ComponentSpec parent3 = createComponentSpec("p2", "xyz", "parent2", "123", "xyz", Collections.<Tag>singletonList(t3), Collections.<String,String>emptyMap());
		
		List<ComponentSpec> parents = Arrays.asList(parent1,parent2);
		for (ComponentSpec cs : parents) {
			cs.getReferencedComponents().add(child);
		}
		parent3.getReferencedComponents().add(child);
		em.persist(child);
		em.persist(parent1);
		em.persist(parent2);
		em.persist(parent3);
		em.getTransaction().commit();
		em.clear();
		
		int objVersion = em.find(ComponentSpec.class, "1").getObjVersion();
		em.clear();
		
		Assert.assertNotNull(em.find(Tag.class, "t3"));
		AbstractComponent parent2Component = serviceImpl.getComponent("p2");
		serviceImpl.delete(Collections.singleton(parent2Component));
		Assert.assertNotNull(em.find(Tag.class, "t3"));
		
		AbstractComponent deleted = serviceImpl.getComponent(child.getComponentId());
		serviceImpl.delete(Collections.singleton(deleted));
		
		Assert.assertNull(em.find(ComponentSpec.class, childId));
		for (ComponentSpec cs: parents) {
			ComponentSpec p = em.find(ComponentSpec.class, cs.getComponentId());
			Assert.assertEquals(p.getObjVersion(), objVersion+1);
		}
		
		AbstractComponent unsavedObj = Mockito.mock(AbstractComponent.class);
		Mockito.when(unsavedObj.getComponentId()).thenReturn("thisObjUnsaved");
		serviceImpl.delete(Collections.singleton(unsavedObj));
	}
	
	@DataProvider(name="referencingComponents")
	Object[][] referencingComponentTestCases() {
		return new Object[][] {
			new Object[] {Arrays.asList("1","2","3"),new Map[]{Collections.singletonMap("1", "2"), Collections.singletonMap("2", "3")}},
			new Object[] {Arrays.asList("1","2","3"),new Map[0]}
		};
	}
	
	@Test(dataProvider="referencingComponents")
	public void testGetReferencingComponents(List<String> componentIds, Map<String,String>[] relationships) {
		Map<String, List<String>> expectedRelationships = new HashMap<String, List<String>>();
		
		// bootstrap several components
		em.getTransaction().begin();
		for (String id:componentIds) {
			ComponentSpec cs = createComponentSpec(id, "xyz", id, "123", "xyz", Collections.<Tag>emptyList(), Collections.<String,String>emptyMap());
			em.persist(cs);
		}
		for (Map<String,String> relation:relationships) {
			for (Entry<String,String> entry:relation.entrySet()) {
				ComponentSpec parent = em.find(ComponentSpec.class, entry.getKey());
				ComponentSpec child = em.find(ComponentSpec.class, entry.getValue());
				List<String> mappedRelationships = expectedRelationships.get(entry.getValue());
				if (mappedRelationships == null) {
					mappedRelationships = new ArrayList<String>();
					mappedRelationships.add(entry.getKey());
					expectedRelationships.put(entry.getValue(), mappedRelationships);
				}
				parent.getReferencedComponents().add(child);
			}
		}
		em.getTransaction().commit();
		em.clear();
		
		// verify relationships are queried correctly
		for (Entry<String,List<String>> relationshipEntries:expectedRelationships.entrySet()) {
			List<String> refs = relationshipEntries.getValue();
			AbstractComponent parent = Mockito.mock(AbstractComponent.class);
			Mockito.when(parent.getComponentId()).thenReturn(relationshipEntries.getKey());
			Collection<AbstractComponent> referencedComponents = serviceImpl.getReferences(parent);
			Assert.assertEquals(referencedComponents.size(), refs.size(), "referencedComponents " + referencedComponents + "expected refs " + refs);
			for (String id : refs) {
				boolean found = false;
				for (AbstractComponent cs:referencedComponents) {
					if (cs.getComponentId().equals(id)) {
						found = true;
						break;
					}
				}
				Assert.assertTrue(found, "did not find expected component in reference list");
			}
		}
		
	}
}
