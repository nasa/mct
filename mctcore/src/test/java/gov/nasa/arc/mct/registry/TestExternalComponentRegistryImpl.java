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
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestExternalComponentRegistryImpl {
    private ExternalComponentRegistryImpl registry;
    private Platform mockPlatform;
    private PolicyManager mockPolicyManager;
    private PersistenceProvider mockPersistence;
    private AbstractComponent mySandbox = new AbstractComponent() {
    };
    
    private ExtendedComponentProvider createProvider(final Collection<ComponentTypeInfo> infos, final Collection<ViewInfo> viewInfos) {
        ComponentProvider provider = new TestComponentProvider(infos, viewInfos);
        
        return new ExtendedComponentProvider(provider, "test");
    }
    
    
    @BeforeMethod
    public void clearRegistry() throws Exception {
        registry = new ExternalComponentRegistryImpl() {
            
            @Override
            protected String getDefaultUser() {
                return "defaultUser";
            }            
        };
        
        mockPlatform = Mockito.mock(Platform.class);
        mockPolicyManager = Mockito.mock(PolicyManager.class);
        mockPersistence = Mockito.mock(PersistenceProvider.class);
        (new PlatformAccess()).setPlatform(mockPlatform);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getMySandbox()).thenReturn(mySandbox);
        ExecutionResult er = new ExecutionResult(null, true, null);
        Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.any(PolicyContext.class))).thenReturn(er);
    }
    
    @Test
    public void testGetComponentType() {
        Assert.assertNull(registry.getComponentType("someType"), "components should be empty prior to populating registry");
        ComponentTypeInfo info = new ComponentTypeInfo("displayName", "desc", TestBaseComponent.class);
        ExtendedComponentProvider provider = createProvider(Collections.singletonList(info), null);
        registry.refreshComponents(Collections.singletonList(provider));
        Assert.assertEquals(TestBaseComponent.class, registry.getComponentType(TestBaseComponent.class.getName()),"supplied info must be equal");
        registry.refreshComponents(Collections.<ExtendedComponentProvider>emptyList());
        Assert.assertNull(registry.getComponentType(TestBaseComponent.class.getName()),"removing provider must also remove component types");

    }
    
    private static class TestComponentProvider extends AbstractComponentProvider {
        private final Collection<ComponentTypeInfo> infos;
        private final Collection<ViewInfo> viewInfos;

        private TestComponentProvider(Collection<ComponentTypeInfo> infos, Collection<ViewInfo> viewInfos) {
            this.infos = infos;
            this.viewInfos = viewInfos;
        }

        @Override
        public Collection<ComponentTypeInfo> getComponentTypes() {
            return infos;
        }

        @Override
        public Collection<ViewInfo> getViews(String componentTypeId) {
            return viewInfos;
        }

    }
    public static class TestBaseComponent extends AbstractComponent {
    }
    
    
    public static class TestingView extends View {
        private static final long serialVersionUID = 1L;
        public TestingView(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
        
    }
    
    public static class OtherTestingView extends View {
        private static final long serialVersionUID = 1L;
        public OtherTestingView(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
        
    }
    
    public static class TestingView2 extends View {
        private static final long serialVersionUID = 1L;
        public TestingView2(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
        
    }

    public static class TestingView3 extends View {
        private static final long serialVersionUID = 1L;
        public TestingView3(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
        
    }
    
    @Test
    public void testGetViews() {
        Assert.assertTrue(registry.getViewInfos("someType", ViewType.CENTER).isEmpty(), "views should be empty prior to populating registry");
        ViewInfo vi1 = new ViewInfo(TestingView.class, "abc", ViewType.OBJECT);
        ViewInfo vi3 = new ViewInfo(TestingView2.class, "abc", ViewType.CENTER);
        ViewInfo vi2 = new ViewInfo(TestingView3.class, "def", ViewType.CENTER);
        ExtendedComponentProvider provider = createProvider(Collections.<ComponentTypeInfo>emptyList(), Arrays.asList(vi1, vi2, vi3));
        registry.refreshComponents(Collections.singletonList(provider));
        Collection<ViewInfo> infos = registry.getViewInfos("abc", ViewType.CENTER);
        Assert.assertEquals(infos.size(),2);
        Assert.assertTrue(infos.containsAll(Arrays.asList(vi2,vi3)));
        infos = registry.getViewInfos("abc", ViewType.OBJECT);
        Assert.assertEquals(infos.size(),1);
        Assert.assertTrue(infos.containsAll(Arrays.asList(vi1)));
        Assert.assertTrue(registry.getViewInfos("abc", ViewType.LAYOUT).isEmpty());
        registry.refreshComponents(Collections.<ExtendedComponentProvider>emptyList());
        Assert.assertTrue(registry.getViewInfos("someType", ViewType.CENTER).isEmpty(), "views should be empty prior to populating registry");
        
    }
    
    @Test
    public void testDefaultViews() {
        ExtendedComponentProvider provider = createProvider(Collections.<ComponentTypeInfo>singleton(new ComponentTypeInfo("displayName", "desc", TestBaseComponent.class)), Collections.<ViewInfo>emptyList());
        registry.refreshComponents(Collections.singletonList(provider));
        Assert.assertTrue(registry.getViewInfos("someType", ViewType.CENTER).isEmpty(), "views should be empty prior to populating registry");
        ExtendedComponentProvider defaultProvider = createProvider(Collections.<ComponentTypeInfo>emptyList(), Collections.<ViewInfo>singleton(new ViewInfo(TestingView.class,"",ViewType.CENTER)));
        registry.setDefaultViewProvider(defaultProvider);
        Assert.assertEquals(Collections.singletonList(new ViewInfo(TestingView.class,"", ViewType.CENTER)), registry.getViewInfos(TestBaseComponent.class.getName(), ViewType.CENTER), "default test view should be provided");
        // TODO add additional tests to verify the multiplicity of the ViewType is considered
    }
    
    
    @Test
    public void testNewInstance() throws Exception {
        ComponentTypeInfo info = new ComponentTypeInfo("displayName", "desc", TestBaseComponent.class);
        ExtendedComponentProvider provider = createProvider(Collections.singletonList(info), null);
        registry.refreshComponents(Collections.singletonList(provider));
        TestBaseComponent newComponent = registry.newInstance(TestBaseComponent.class,null);
        
        Assert.assertTrue(newComponent.getClass().equals(TestBaseComponent.class));
        
        // now pass a parent which is shared
        AbstractComponent parentComponent = new MockComponent() {
            @Override
            public synchronized List<AbstractComponent> getComponents() {
                return Collections.<AbstractComponent> emptyList();
            }
        };
        TestBaseComponent newComponent2 = registry.newInstance(TestBaseComponent.class,parentComponent);
        Assert.assertTrue(newComponent2.getClass().equals(TestBaseComponent.class));
    }
    
    @Test(enabled=false)
    public void testNewCollection() {
        // Environment setup: platform, collection provider, lock manager, and component registry.
        Platform mockPlatform = Mockito.mock(Platform.class);        

        
        MockComponentRegistry registry = new MockComponentRegistry();
        
        // Set the platform SPI
        PlatformAccess platformAccess = new PlatformAccess();
        platformAccess.setPlatform(mockPlatform);
        
        // Case #1: test returned collection when adding selectedComponents to the new collection is successful
        
        // Setup
        TestBaseComponent collection = Mockito.mock(TestBaseComponent.class);                
        registry.setDefaultCollection(collection);
        List<AbstractComponent> selectedComponents = Collections.singletonList(Mockito.mock(AbstractComponent.class));
        registry.setExpectedResultForAddComponents(true);

        // The test
        AbstractComponent newCollection = registry.newCollection(selectedComponents);
        Assert.assertSame(newCollection, collection);
        
        // Case #2: test returned collection when adding selectedComponents to the new collection fails
        
        // Setup
        registry.clearRegistry();
        registry.setDefaultCollection(collection);
        registry.setExpectedResultForAddComponents(false);
        
        // The test
        newCollection = registry.newCollection(selectedComponents);
        Assert.assertNotNull(newCollection);

        // Tear down
        platformAccess.setPlatform(null);
    }
    
    @Test
    public void testGetComponentInfos() {
        ComponentTypeInfo info = new ComponentTypeInfo("displayName", "desc", TestBaseComponent.class);
        ExtendedComponentProvider provider = createProvider(Collections.singletonList(info), null);
        registry.refreshComponents(Collections.singletonList(provider));
        Assert.assertEquals(Collections.singleton(info), registry.getComponentInfos(),"component infos must be the same");
    }
    
    @Test
    public void testNullReturnFromComponentProvider() {
        ExtendedComponentProvider provider = createProvider(null, null);
        registry.refreshComponents(Collections.singletonList(provider));
        Assert.assertTrue(registry.getComponentInfos().isEmpty());
    }
    
    @Test
    public void testMultipleReturnsFromGetInfos() {
        ComponentTypeInfo info = new ComponentTypeInfo("displayName", "desc", TestBaseComponent.class);
        ViewInfo providerViewInfo = new ViewInfo(TestingView.class,"", ViewType.CENTER);
        ViewInfo defaultViewInfo = new ViewInfo(OtherTestingView.class,"", ViewType.CENTER);
        ExtendedComponentProvider cp = createProvider(Collections.singleton(info), Collections.<ViewInfo>singleton(providerViewInfo));
        registry.refreshComponents(Collections.singletonList(cp));
        
        ExtendedComponentProvider defaultProvider = createProvider(Collections.<ComponentTypeInfo>emptyList(), Collections.singleton(defaultViewInfo));
        registry.setDefaultViewProvider(defaultProvider);
        List<ViewInfo> expected = Arrays.asList(providerViewInfo,defaultViewInfo);
        Collection<ViewInfo> viewInfos = registry.getViewInfos(TestBaseComponent.class.getName(), ViewType.CENTER);
        Assert.assertEquals(2, viewInfos.size());
        Assert.assertTrue(viewInfos.containsAll(expected));
    
    }
    
    @Test
    public void testExceptionWhileInvokingGetInfo() {
        ComponentTypeInfo info = new ComponentTypeInfo("displayName", "desc", TestBaseComponent.class);
        ExtendedComponentProvider goodProvider = createProvider(Collections.singletonList(info), null);
        ComponentProvider exceptionProvider = new TestComponentProvider(null, null) {
            @Override
            public Collection<ComponentTypeInfo> getComponentTypes() {
                throw new ClassCastException();
            }
        };
        ExtendedComponentProvider badProvider = new ExtendedComponentProvider(exceptionProvider, "symName");
            
        registry.refreshComponents(Arrays.asList(badProvider, goodProvider));
        Assert.assertEquals(Collections.singleton(info), registry.getComponentInfos(),"component infos must be the same");
    
    }    
}
