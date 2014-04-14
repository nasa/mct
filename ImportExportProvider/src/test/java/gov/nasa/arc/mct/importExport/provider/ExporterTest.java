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
package gov.nasa.arc.mct.importExport.provider;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.components.JAXBModelStatePersistence;
import gov.nasa.arc.mct.components.ModelStatePersistence;
import gov.nasa.arc.mct.importExport.access.ComponentRegistryAccess;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ComponentRegistry;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExporterTest {

    public static TemporaryFolder tmpDir = new TemporaryFolder();

	@Mock private PersistenceProvider persistenceProvider;
	@Mock private ComponentRegistry mockRegistry;
	@Mock private Platform mockPlatform;
	@Mock private PolicyManager mockPolicyManager;
	
	private Exporter exporter;
	private Importer importer;
	private ComponentRegistryAccess access;
	private TestAbstractComponent componentA;
	private TestAbstractComponent componentB;
	private TestAbstractComponent componentC;
    private TestAbstractComponent componentD;
    private TestAbstractComponent componentE;
    private TestAbstractComponent componentF;
    private TestIEComponent ieComponentDate;
    private TestIEComponent ieComponentFileName1;
    private TestIEComponent ieComponentFileName2;
    private TestIEComponent ieComponentFileName3;
    private TestIEComponent ieComponentFileName4;
    private TestIEComponent ieComponentFileName5;
    private TestIEComponent ieComponentFileName6;
    private int count = 0;

  @BeforeMethod
  public void setup() throws Exception {
      MockitoAnnotations.initMocks(this);
      tmpDir.create();
      access = new ComponentRegistryAccess();
      
      (new PlatformAccess()).setPlatform(mockPlatform);
      when(mockRegistry
                   .isCreatable(Mockito.any(AbstractComponent.class.getClass())))
             .thenReturn(true);
      when(mockPlatform.getPersistenceProvider()).thenReturn(persistenceProvider);
      when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
      when(mockPolicyManager
                  .execute(Mockito.anyString(), Mockito.any(PolicyContext.class)))
             .thenReturn(new ExecutionResult(null,true,""));
      
      access.setRegistry(mockRegistry);
      
      
      // For importer
      int base = count * 12;
      
      ieComponentDate = new TestIEComponent();
      ieComponentDate.setId(String.valueOf(base + 1));
      ieComponentFileName1 = new TestIEComponent();
      ieComponentFileName1.setId(String.valueOf(base + 2));
      ieComponentFileName2 = new TestIEComponent();
      ieComponentFileName2.setId(String.valueOf(base + 3));
      ieComponentFileName3 = new TestIEComponent();
      ieComponentFileName3.setId(String.valueOf(base + 4));
      ieComponentFileName4 = new TestIEComponent();
      ieComponentFileName4.setId(String.valueOf(base + 5));
      ieComponentFileName5 = new TestIEComponent();
      ieComponentFileName5.setId(String.valueOf(base + 6));
      ieComponentFileName6 = new TestIEComponent();
      ieComponentFileName6.setId(String.valueOf(base + 7));
      
      componentB = createAbstractComponent("compB", String.valueOf(base + 8));
      componentC = createAbstractComponent("compC", String.valueOf(base + 9));
      componentD = createAbstractComponent("compD", String.valueOf(base + 10));
      componentE = createAbstractComponent("compE", String.valueOf(base + 11));
      componentF = createAbstractComponent("compF", String.valueOf(base + 12));
      
      when(mockRegistry
             .newInstance(Mockito.eq(ImportExportComponent.class), 
                           Mockito.any(AbstractComponent.class)))
             .thenReturn(ieComponentDate)
             .thenReturn(ieComponentFileName1)
             .thenReturn(ieComponentFileName2)
             .thenReturn(ieComponentFileName3)
             .thenReturn(ieComponentFileName4)
             .thenReturn(ieComponentFileName5)
             .thenReturn(ieComponentFileName6);
      when(mockRegistry.newInstance(Mockito.anyString()))
             .thenReturn(componentB)
             .thenReturn(componentC)
             .thenReturn(componentD)
             .thenReturn(componentE)
             .thenReturn(componentF);

      count++;
  }
  
  @AfterMethod
  public void tearDown() {
      access.releaseRegistry(mockRegistry);
      tmpDir.destroy();
  }

  @Test
  public void testExportWithoutChildren() throws Exception {
      File export = tmpDir.newFile("oneComponentWithoutChildren.xml");
      File expected = new File("src/test/resources/testOutput/oneComponentWithoutChildren.xml");
	  AbstractComponent componentWithoutChildren = newCompWOChildren(componentA, 
			  "NOCHILDREN","rootOneComponentWithoutChildren");

	  AbstractComponent parent = exportAndVerify(Arrays.asList(componentWithoutChildren), export, expected);

	  // component with date
	  assertEquals(parent.getComponents().size(), 1);
	  // comp with filename
	  assertEquals(parent.getComponents().get(0)
              .getComponents().size(), 1);
	  // top level comp
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
      // no child
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 0);
  }
  
  @Test
  public void testExportWithChildren() throws Exception {
      File export = tmpDir.newFile("componentWithChildren.xml");
	  File expected = new File("src/test/resources/testOutput/componentWithChildren.xml");
	  AbstractComponent componentWithChildren = newCompWithChildren();

	  AbstractComponent parent = exportAndVerify(Arrays.asList(componentWithChildren), export, expected);
	  
	  // component with date
	  assertEquals(parent.getComponents().size(), 1);
	  // comp with filename
      assertEquals(parent.getComponents().get(0)
              .getComponents().size(), 1);
      // top level comp
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
      // child
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
  }

  @Test
  public void testExportWithMultiLevelChildren() throws Exception {
	  File export = tmpDir.newFile("componentMultiLevelChildren.xml");
	  File expected = new File("src/test/resources/testOutput/componentMultiLevelChildren.xml");
	  AbstractComponent component = newCompWithMultiLevelChildren();
	  
	  AbstractComponent parent = exportAndVerify(Arrays.asList(component), export, expected);
	  
	  // component with date
	  assertEquals(parent.getComponents().size(), 1);
	  // comp with filename
      assertEquals(parent.getComponents().get(0)
              .getComponents().size(), 1);
      // top level comp
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
      // children
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 2);
      // grandchild
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
  }

  @Test
  public void testExportWithRecursiveChildren() throws Exception {
	  File export = tmpDir.newFile("componentRecursiveChildren.xml");
	  File expected = new File("src/test/resources/testOutput/componentRecursiveChildren.xml");
	  AbstractComponent component = newCompWithRecursiveChildren();
	  
	  AbstractComponent parent = exportAndVerify(Arrays.asList(component), export, expected);
	  
	  // component with date
	  assertEquals(parent.getComponents().size(), 1);
	  // comp with filename
      assertEquals(parent.getComponents().get(0)
              .getComponents().size(), 1);
      // top level comp
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
      // child
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
      // grandchild (which is same as top level comp)
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
  }
  
  @Test
  public void testExportMultipleComponents() throws Exception {
      File export = tmpDir.newFile("multComp.xml");
      File expected = new File("src/test/resources/testOutput/multComp.xml");
      AbstractComponent component1 = newCompWithRecursiveChildren();
      AbstractComponent component2 = newCompWithChildren();
      List<AbstractComponent> comps = new ArrayList<AbstractComponent>();
      comps.add(component1);
      comps.add(component2);
      
      AbstractComponent parent = exportAndVerify(comps, export, expected);
      
      // component with date
      assertEquals(parent.getComponents().size(), 1);
      // comp with filename
      assertEquals(parent.getComponents().get(0)
              .getComponents().size(), 1);
      // 2 top level comps
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 2);
      // child of 1st top level comp
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(0)
              .getComponents().size(), 1);
      // child of 2nd top level comp
      assertEquals(parent.getComponents().get(0)
              .getComponents().get(0)
              .getComponents().get(1)
              .getComponents().size(), 1);
  }
  
  
  ////////////////////////////////////////////////////////////////////////////////
  
  
  private AbstractComponent exportAndVerify(List<AbstractComponent> components, File export, File expected) {
	  exporter = new Exporter(export, components);
	  try {
	      exporter.doInBackground();
	      FileReader reader = new FileReader(export);
	      if (reader.read() == -1) {
	          Assert.fail("Output file is empty.");
	      }
	  } catch (Exception e) {
	      e.printStackTrace();
	      Assert.fail();
	  }

	  AbstractComponent parent = new TestAbstractComponent();
	  importer = new Importer(Arrays.asList(expected), "Tonto", parent);
	  importer.doInBackground();
	  return parent;
  }
  
  private AbstractComponent newCompWOChildren(AbstractComponent component, String id,
		                         String externalKey) {
      component = new TestAbstractComponent();
      ComponentInitializer ci = component.getCapability(ComponentInitializer.class);
      ci.setId(id);
      ci.setCreator("Ginger");
      ci.setCreationDate(new Date());
      component.setDisplayName("Test Name");
      component.setOwner("Fred");
	  
	  // Add view state
	  Map<String, ExtendedProperties> viewRoleProperties = 
			                 new HashMap<String, ExtendedProperties>();
	  ExtendedProperties prop = new ExtendedProperties();
	  prop.setProperty("viewType1", "properties1");
	  viewRoleProperties.put("test1", prop);
	  prop.setProperty("viewType2", "properties2");
	  viewRoleProperties.put("test2", prop);
	  when(persistenceProvider.getAllProperties(id)).thenReturn(viewRoleProperties);
	  
	  return component;
  }
  
  private AbstractComponent newCompWithChildren() {
	  componentA = (TestAbstractComponent) newCompWOChildren(componentA, "PARENT", 
	                                                         "componentWithChildren");
	  componentB = (TestAbstractComponent) newCompWOChildren(componentB, "CHILD", 
	                                                         "componentWithChildren");
	  componentA.addDelegateComponent(componentB);
	  when(persistenceProvider.getReferencedComponents(componentA))
             .thenReturn(Arrays.asList((AbstractComponent) componentB));
	  
	  return componentA;
  }
  
  private AbstractComponent newCompWithMultiLevelChildren() {
	  componentA = (TestAbstractComponent) newCompWOChildren(componentA, "PARENT", 
	                                                    "componentMultilevelChildren");
	  componentB = (TestAbstractComponent) newCompWOChildren(componentB, "CHILD", 
	                                                    "componentMultilevelChildren");
	  componentC = (TestAbstractComponent) newCompWOChildren(componentC, "GRANDCHILD", 
	                                                    "componentMultilevelChildren");
	  
	  ArrayList<AbstractComponent> list = new ArrayList<AbstractComponent>();
	  list.add(componentB);
	  list.add(componentC);
	  componentA.addDelegateComponents(list);
	  componentB.addDelegateComponent(componentC);
	  
	  when(persistenceProvider.getReferencedComponents(componentA))
             .thenReturn(list);
	  when(persistenceProvider.getReferencedComponents(componentB))
             .thenReturn(Arrays.asList((AbstractComponent) componentC));

	  return componentA;
  }
  
  private AbstractComponent newCompWithRecursiveChildren() {
	  componentA = (TestAbstractComponent) newCompWOChildren(componentA, "PARENT", 
	                                                       "componentRecursiveChildren");
	  componentB = (TestAbstractComponent) newCompWOChildren(componentB, "CHILD", 
	                                                       "componentRecursiveChildren");
	  componentA.addDelegateComponent(componentB);
	  componentB.addDelegateComponent(componentA);
	  
	  when(persistenceProvider.getReferencedComponents(componentA))
             .thenReturn(Arrays.asList((AbstractComponent) componentB));
	  when(persistenceProvider.getReferencedComponents(componentB))
             .thenReturn(Arrays.asList((AbstractComponent) componentA));

	  return componentA;
  }
  
  private TestAbstractComponent createAbstractComponent(String componentName, String id) {
      TestAbstractComponent comp = new TestAbstractComponent();
      ComponentInitializer ci = comp.getCapability(ComponentInitializer.class);
      ci.setId(id);
      return comp;
  }
  
  ////////////////////////////////////////////////////////////////////////////

  public static class TestAbstractComponent extends AbstractComponent {
      private AtomicReference<TestModel> model = 
              new AtomicReference<TestModel>(new TestModel());

      public void setModelValue(String v) {
          model.get().setValue(v);
      }

      public String getModelValue() {
          return model.get().getValue();
      }

      @Override
      protected <T> T handleGetCapability(Class<T> capability) {
          if (ModelStatePersistence.class.isAssignableFrom(capability)) {
              JAXBModelStatePersistence<TestModel> persistence = 
                      new JAXBModelStatePersistence<TestModel>() {

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
  
  public static class TestIEComponent extends ImportExportComponent {
      private AtomicReference<TestModel> model = 
              new AtomicReference<TestModel>(new TestModel());

      public void setModelValue(String v) {
          model.get().setValue(v);
      }

      public String getModelValue() {
          return model.get().getValue();
      }

      @Override
      protected <T> T handleGetCapability(Class<T> capability) {
          if (ModelStatePersistence.class.isAssignableFrom(capability)) {
              JAXBModelStatePersistence<TestModel> persistence = 
                      new JAXBModelStatePersistence<TestModel>() {

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
}
