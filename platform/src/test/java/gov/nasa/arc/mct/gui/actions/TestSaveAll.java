package gov.nasa.arc.mct.gui.actions;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestSaveAll {
    private Platform platform;
    private Platform mockPlatform;
    
    private Set<AbstractComponent> goodComponents = new HashSet<AbstractComponent>();
    
    @BeforeClass
    public void setup() {
        platform = PlatformAccess.getPlatform();

        
        mockPlatform = Mockito.mock(Platform.class);
        new PlatformAccess().setPlatform(mockPlatform);
        
        PolicyManager mockPolicyManager = Mockito.mock(PolicyManager.class);
        
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicyManager);
        
        Answer<ExecutionResult> answer = new Answer<ExecutionResult> () {
            @Override
            public ExecutionResult answer(InvocationOnMock invocation) throws Throwable {
                PolicyContext context = (PolicyContext) invocation.getArguments()[1];
                AbstractComponent comp = context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class);
                return new ExecutionResult(context, goodComponents.contains(comp), "" );
            }            
        };
        
        Mockito.when(mockPolicyManager.execute(Mockito.eq(PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey()), Mockito.<PolicyContext>any())).thenAnswer(answer);
        
        
    }
    
    @AfterClass
    public void teardown() {
        new PlatformAccess().setPlatform(platform);
    }

    
    @Test (dataProvider = "generateSavesComponentCases")
    public void testSaveAllSavesComponent(ContextAwareAction action, boolean isDirty) {
        // Save All should save the parent component, as well as any children 
        // returned by getModifiedObjects. Even when parent has not 'changed', 
        // save all implies that changes to children are relevant to parent. 
        // This verifies that parent is saved even when parent is not dirty.
        
        // Set up parent/child component such that SaveAll should be available
        final AbstractComponent child = generateComponent(true, true, null);
        final AbstractComponent comp = generateComponent(true, isDirty, child);

        // Elaborate mocking to simulate context menu activation
        MCTHousing mockHousing = Mockito.mock(MCTHousing.class);
        MCTContentArea mockContentArea = Mockito.mock(MCTContentArea.class);
        View mockView = Mockito.mock(View.class);
        ActionContextImpl mockContext = Mockito.mock(ActionContextImpl.class);

        Mockito.when(mockContext.getInspectorComponent()).thenReturn(comp);
        Mockito.when(mockContext.getTargetComponent()).thenReturn(comp);
        Mockito.when(mockContext.getTargetHousing()).thenReturn(mockHousing);
        Mockito.when(mockHousing.getContentArea()).thenReturn(mockContentArea);
        Mockito.when(mockContentArea.getHousedViewManifestation()).thenReturn(mockView);
        Mockito.when(mockView.getManifestedComponent()).thenReturn(comp);

        // Generate a new persistence provider each time
        PersistenceProvider persistence = Mockito.mock(PersistenceProvider.class);
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(persistence);

        // Simulate menu activation cycle; also, verify expectations (even
        // though redundant to test below)
        Assert.assertTrue(action.canHandle(mockContext));
        Assert.assertTrue(action.isEnabled());

        // Perform the action
        action.actionPerformed(Mockito.mock(ActionEvent.class));

        // Finally, verify that the appropriate objects (both parent and child)
        // went to persistence
        Mockito.verify(persistence, Mockito.times(1)).persist(
                Mockito.argThat(new ArgumentMatcher<Collection<AbstractComponent>>() {
                    @Override
                    public boolean matches(Object argument) {
                        return (argument instanceof Collection<?>) && ((Collection<?>) argument).contains(child)
                                && ((Collection<?>) argument).contains(comp);
                    }
                })
        );

        
    }

    @DataProvider
    public Object[][] generateSavesComponentCases() {
        Object[][] cases = new Object[4][];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                cases[i*2+j] = new Object[] { 
                        i == 0 ? new ThisSaveAllAction() : new ObjectsSaveAllAction(),
                        j == 0 // isDirty
                };
            }
        }
        return cases;
    }
    
    
    @Test (dataProvider="generateTestCases")
    public void testSaveAllEnabled(ContextAwareAction action, AbstractComponent comp, boolean shouldHandle, boolean shouldBeEnabled) {
        // Elaborate mocking to simulate context menu activation
        MCTHousing mockHousing = Mockito.mock(MCTHousing.class);
        MCTContentArea mockContentArea = Mockito.mock(MCTContentArea.class);
        View mockView = Mockito.mock(View.class);
        ActionContextImpl mockContext = Mockito.mock(ActionContextImpl.class);
        
        Mockito.when(mockContext.getInspectorComponent()).thenReturn(comp);
        Mockito.when(mockContext.getTargetComponent()).thenReturn(comp);
        Mockito.when(mockContext.getTargetHousing()).thenReturn(mockHousing);
        Mockito.when(mockHousing.getContentArea()).thenReturn(mockContentArea);
        Mockito.when(mockContentArea.getHousedViewManifestation()).thenReturn(mockView);
        Mockito.when(mockView.getManifestedComponent()).thenReturn(comp);
        
        // Verify that enabled/disabled states match expectations
        Assert.assertEquals(action.canHandle(mockContext), shouldHandle);
        Assert.assertEquals(action.isEnabled(), shouldBeEnabled);
    }
    
    @DataProvider
    public Object[][] generateTestCases() {
        Object[][] testCases = new Object[32][];
        boolean truths[] = {false, true};
        int i = 0;

        // Generate a variety of test cases based on expected response to policy
        for (boolean action : truths) {
            for (boolean validParent : truths) {
                for (boolean validChild : truths) {
                    for (boolean isDirty : truths) {            
                        for (boolean hasChild : truths) {
                            Object[] testCase = {
                                action ? new ThisSaveAllAction() : new ObjectsSaveAllAction(),
                                generateComponent(validParent, isDirty, hasChild ? generateComponent(validChild, isDirty, null) : null),
                                action || (validParent && hasChild && validChild) || (validParent && !hasChild && isDirty), 
                                (validParent && hasChild && validChild) || (validParent && !hasChild && isDirty)
                            };
                            testCases[i++] = testCase;
                        }
                    }
                }
            }
        }
        
        return testCases;
    }
    
    private AbstractComponent generateComponent(boolean good, boolean dirty, AbstractComponent child) {
        AbstractComponent mockComponent = Mockito.mock(AbstractComponent.class);
        Mockito.when(mockComponent.isDirty()).thenReturn(dirty);
        if (child != null) {
            Mockito.when(mockComponent.getAllModifiedObjects()).thenReturn(Collections.singleton(child));
        }
        if (good) {
            goodComponents.add(mockComponent);
        }
        return mockComponent;
    }
    
}
