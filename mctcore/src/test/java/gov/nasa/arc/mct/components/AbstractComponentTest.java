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
package gov.nasa.arc.mct.components;

import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AbstractComponentTest {

    public static class BaseComponentSub1 extends AbstractComponent {
        public BaseComponentSub1(int a){
        }
    }
    
    public static class BaseComponentSub3 extends AbstractComponent {
        BaseComponentSub3(){
        }
    }
    
    public static class BaseComponentSub2 extends AbstractComponent {
        public BaseComponentSub2(){
        }
    }
    
    public static class TestingView3 extends View {
        private static final long serialVersionUID = 1L;
        public TestingView3(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
        
    }
    
    @Mock
    private Platform mockPlatform;
    
    @Mock
    private WindowManager mockWindowManager;
    
    @Mock
    private PersistenceProvider mockPersistenceService;
    
    @BeforeMethod
    public void setup() {
        PolicyManager mockManager = new PolicyManager() {
            
            @Override
            public ExecutionResult execute(String categoryKey, PolicyContext context) {
                return new ExecutionResult(null, true, null);
            }
        };
        
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockManager);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistenceService);
        Mockito.when(mockPersistenceService.getReferencedComponents(Mockito.any(AbstractComponent.class))).thenReturn(Collections.<AbstractComponent>emptyList());

        (new PlatformAccess()).setPlatform(mockPlatform);
    }
    
    @Test
    public void testCheckBaseComponentRequirements() {        
        AbstractComponent.checkBaseComponentRequirements(BaseComponentSub2.class);
        checkConstructor(BaseComponentSub1.class);
        checkConstructor(BaseComponentSub3.class);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @DataProvider(name="viewInfoData")
    public Object[][] viewInfoData() {
        View v1 = Mockito.mock(View.class);
        View v2 = Mockito.mock(TestingView3.class);
        ViewInfo vi = new ViewInfo(v1.getClass(),"v1", ViewType.CENTER);
        //ViewInfo vi2 = new ViewInfo(v2.getClass(), "v2", ViewType.INSPECTION);
        ViewInfo vi3 = new ViewInfo(v2.getClass(), "v2", ViewType.CENTER);
        
        return new Object[][] {
                        new Object[] {
                           new LinkedHashSet(Arrays.asList(vi,vi3)), Collections.emptySet(), ViewType.CENTER, new LinkedHashSet(Arrays.asList(vi,vi3))            
                        },
                        new Object[] {
                           new LinkedHashSet(Arrays.asList(vi,vi3)), Collections.singleton(vi), ViewType.CENTER, Collections.singleton(vi3)            
                        },
                        
                        new Object[] {
                                        Collections.emptySet(), Collections.emptySet(), ViewType.LAYOUT, Collections.emptySet()
                                     } 
        };
    }
    
    //Harleigh108: this removes the warning received when we build with respect to java 7: deprecation is for PREFERRED_VIEW
    @SuppressWarnings({ "rawtypes", "deprecation" })
    @Test(dataProvider="viewInfoData")
    public void testGetViewInfos(Set<ViewInfo> viewInfos, final Set<ViewInfo> filterOut, ViewType type, Set<ViewInfo> expected) {
        AbstractComponent ac = new BaseComponentSub2();
        CoreComponentRegistry mockRegistry = Mockito.mock(CoreComponentRegistry.class);
        PolicyManager mockPolicyManager = Mockito.mock(PolicyManager.class);
        Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockRegistry);
        Mockito.when(mockRegistry.getViewInfos(Mockito.anyString(), Mockito.same(type))).thenReturn(viewInfos);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        Mockito.when(mockPolicyManager.execute(Mockito.matches(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey()), Mockito.any(PolicyContext.class))).thenAnswer(
                        new Answer() {
                            public Object answer(InvocationOnMock invocation) {
                                Object[] args = invocation.getArguments();
                                PolicyContext pc = (PolicyContext) args[1];
                                ViewInfo vi = pc.getProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), ViewInfo.class);
                                return new ExecutionResult(pc, !filterOut.contains(vi), "");
                            }
                        }
        );
        Mockito.when(mockPolicyManager.execute(Mockito.matches(PolicyInfo.CategoryType.PREFERRED_VIEW.getKey()), Mockito.any(PolicyContext.class))).thenReturn(new ExecutionResult(new PolicyContext(),true, ""));
        Set<ViewInfo> infos = ac.getViewInfos(type);
        Assert.assertEquals(infos, expected);
    }
    
    @Test
    public void testInitialize() {
        AbstractComponent comp = new BaseComponentSub2();
        Assert.assertNull(comp.getId());
        comp.initialize();
        Assert.assertTrue(comp.getCapability(ComponentInitializer.class).isInitialized());
        Assert.assertNotNull(comp.getId());
    }
    
    @Test
    public void testClone() {
        BaseComponentSub2 comp = new BaseComponentSub2();
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("value", "value");
        Mockito.when(mockPersistenceService.getAllProperties(comp.getComponentId())).thenReturn(Collections.singletonMap("view1", props));

        BaseComponentSub2 compClone = BaseComponentSub2.class.cast(comp.clone());
        Map<String,ExtendedProperties> clonedProps = compClone.getCapability(ComponentInitializer.class).getAllViewRoleProperties();
        Assert.assertTrue(clonedProps.size() == 1);
        ExtendedProperties clonedEp = clonedProps.get("view1");
        Assert.assertTrue(clonedEp.getAllProperties().size() == 1);
        Assert.assertEquals(clonedEp.getProperty("value",String.class),"value");
    }
    
    @Test
    public void addDelegateComponentsTest() {
        (new PlatformAccess()).setPlatform(mockPlatform);
        BaseComponentSub2 comp = new BaseComponentSub2();
        BaseComponentSub2 comp2 = new BaseComponentSub2();
        comp.addDelegateComponent(comp2);
        
        Assert.assertEquals(comp.getComponents().size(), 1);
        Assert.assertEquals(comp.getComponents().iterator().next(), comp2);
    }
    
    private void checkConstructor(Class<? extends AbstractComponent> clazz) {
        try {
            AbstractComponent.checkBaseComponentRequirements(clazz);
            Assert.fail("only public no argument constructors should be allowed " + clazz.getName());
        } catch (IllegalArgumentException e) {
            //
        }
    }

}
