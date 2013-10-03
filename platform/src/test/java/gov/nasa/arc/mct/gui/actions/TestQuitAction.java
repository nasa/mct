package gov.nasa.arc.mct.gui.actions;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.housing.registry.UserEnvironmentRegistry;
import gov.nasa.arc.mct.osgi.platform.OSGIRuntimeImpl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;

import java.awt.event.ActionEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestQuitAction {
    private OSGIRuntimeImpl originalRuntime;        
    private Platform originalPlatform;

    @Mock Platform mockPlatform;
    @Mock OSGIRuntimeImpl mockRuntime;
    @Mock ActionContext mockActionContext;
    @Mock ActionEvent mockEvent;

    private boolean doRemoveHousings = true; 
    
    
    @BeforeClass
    public void setupRuntime() throws Exception {
        // We want to verify interactions with OSGIRuntime & Platform, 
        // so we'll need to mock them.
        // Store the originals so it can be restored after the test
        originalRuntime = getSingleton(OSGIRuntimeImpl.class);        
        originalPlatform = PlatformAccess.getPlatform();
    }
    
    @AfterClass
    public void restoreRuntime() throws Exception {
        // Restore original runtime/platform
        setSingleton(OSGIRuntimeImpl.class, originalRuntime);       
        new PlatformAccess().setPlatform(originalPlatform);
        
        // Flush out the registry, since we can't mock it
        clearRegistry(); 
    }
    
    @BeforeMethod
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        // Mock OSGIRuntime and Platform
        setSingleton(OSGIRuntimeImpl.class, mockRuntime);
        new PlatformAccess().setPlatform(mockPlatform);

        // Some tests want housings to refuse to be closed,
        // but most presume that they are removed by closeHousing call.
        doRemoveHousings = true;
        clearRegistry();
    }
    
   
    @Test (dataProvider = "generateTestCases")
    public void testQuitEnabled(boolean confirmed, int housings) {
        // Verifies that QuitAction is available in appropriate cases
        initializeRegistry(housings); 
        ContextAwareAction quit = new QuitAction();
    
        // Should only handle or be enabled when there are windows open
        Assert.assertEquals(quit.canHandle(mockActionContext), housings > 0);
        Assert.assertEquals(quit.isEnabled(), housings > 0);
    }
    
    @Test (dataProvider = "generateTestCases")
    public void testActionPerformed(final boolean confirmed, int housings) throws Exception {
        // Verifies that performing a QuitAction closes all windows, stops OSGI
        // (except where user input indicates otherwise)
        MCTAbstractHousing[] mockHousings = initializeRegistry(housings); 
        ContextAwareAction quit = new QuitAction();
        Mockito.reset(mockRuntime);
        
        // Set up window manager to support dialog call 
        // Act as though the user clicked "OK" or "Cancel" (depending on argument "confirmed")
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        Mockito.when(mockWindowManager.showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] options = (Object[]) invocation.getArguments()[2];
                return confirmed ? options[0] : options[1]; // options[0] presumed to mean "OK"
            }
        });
        
        // Already tested in testQuitEnabled, but also need to obey action's life cycle
        Assert.assertEquals(quit.canHandle(mockActionContext), housings > 0);
        
        // Trigger the action - this is the method we are testing
        quit.actionPerformed(mockEvent);
        
        // A dialog should have been requested
        Mockito.verify(mockWindowManager, Mockito.times(1)).showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any());
        
        // All housings should be closed, iff dialog was confirmed
        for (MCTAbstractHousing mockHousing : mockHousings) {
            Mockito.verify(mockHousing, confirmed ? Mockito.times(1) : Mockito.never()).closeHousing();
        }
        
        // Verify an assumption of the test (that all housings are gone if confirmed, or there if not)
        Assert.assertEquals(UserEnvironmentRegistry.getHousingCount(), confirmed ? 0 : housings);
        
        // OSGI should have been stopped, iff dialog was confirmed
        Mockito.verify(mockRuntime, confirmed ? Mockito.times(1) : Mockito.never()).stopOSGI();
    }

    @Test (dataProvider = "generateTestCases")
    public void testActionPerformedWindowsNotClosed(final boolean confirmed, int housings) throws Exception {
        // A user may interrupt a QuitAction by stopping a window closing
        // (for instance, by hitting cancel if a "Save" "Discard" "Cancel" dialog appears)
        // If this happens, we should NOT stop OSGI (which effectively stops MCT)
        // This is verified by this test.
        
        // Suppress housing removal (as though user had kept window open)
        doRemoveHousings = false;
        
        MCTAbstractHousing[] mockHousings = initializeRegistry(housings); 
        ContextAwareAction quit = new QuitAction();
        Mockito.reset(mockRuntime);
        
        // Set up window manager to support dialog call 
        // Act as though the user clicked "OK" or "Cancel" (depending on argument "confirmed")
        WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowManager);
        Mockito.when(mockWindowManager.showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] options = (Object[]) invocation.getArguments()[2];
                return confirmed ? options[0] : options[1]; // options[0] presumed to mean "OK"
            }
        });
        
        // Already tested in testQuitEnabled, but also need to obey action's life cycle
        Assert.assertEquals(quit.canHandle(mockActionContext), housings > 0);
        
        // Trigger the action - this is the method we are testing
        quit.actionPerformed(mockEvent);
        
        // A dialog should have been requested
        Mockito.verify(mockWindowManager, Mockito.times(1)).showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any(), Mockito.<Map<String,Object>>any());
        
        // All housings should be closed, iff dialog was confirmed
        for (MCTAbstractHousing mockHousing : mockHousings) {
            Mockito.verify(mockHousing, confirmed ? Mockito.times(1) : Mockito.never()).closeHousing();
        }
        
        // Verify an assumption of the test (that all housings are still "there")
        Assert.assertEquals(UserEnvironmentRegistry.getHousingCount(), housings);
        
        // OSGI should NOT have been stopped (except for corner case where 
        // action was confirmed and there were no housings to begin with)
        Mockito.verify(mockRuntime, (!confirmed || housings > 0) ? Mockito.never() : Mockito.times(1)).stopOSGI();
    }

    
    private void clearRegistry() {
        // Flush all housings from the UserEnvironmentRegistry
        // (it can't be mocked, so test must rely on its actual behavior)
        Collection<MCTAbstractHousing> housings = UserEnvironmentRegistry.getAllHousings();
        for (MCTAbstractHousing housing : housings) {
            UserEnvironmentRegistry.removeHousing(housing);
        }
    }
    
    private MCTAbstractHousing[]  initializeRegistry (int numberOfHousings) {
        // Initialize the UserEnvironmentRegistry with mock housings
        // UserEnvironmentRegistry can't be effectively mocked 
        // (internal instance is static and final), so instead we populate 
        // it directly. 
        clearRegistry();
        MCTAbstractHousing[] mockHousings = mockArray(numberOfHousings, MCTAbstractHousing.class);

        // Verify precondition (ensure UserEnvironmentRegistry is appropriately clear) 
        Assert.assertEquals(UserEnvironmentRegistry.getHousingCount(), 0);
        
        for (MCTAbstractHousing h : mockHousings) {
            // UserEnvironmentRegistry keys on component id, so provide one
            AbstractComponent mockComp = Mockito.mock(AbstractComponent.class);
            Mockito.when(mockComp.getId()).thenReturn("component");
            Mockito.when(h.getWindowComponent()).thenReturn(mockComp);
            
            // Also, 
            Mockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {          
                    // Actually remove from the UserEnvironmentRegistry, since 
                    // QuitAction looks there to determine behavior. But allow this 
                    // to be suppressed by changing doRemoveHousings (to permit
                    // testing of QuitAction's behavior when housings don't close.)
                    if (doRemoveHousings) {
                        UserEnvironmentRegistry.removeHousing((MCTAbstractHousing) invocation.getMock());
                    }
                    return null;
                }                
            }).when(h).closeHousing();
            UserEnvironmentRegistry.registerHousing(h);
        }        
        
        // Verify new precondition for tests, that UserEnvironmentRegistry contains this many housings
        Assert.assertEquals(UserEnvironmentRegistry.getHousingCount(), numberOfHousings);
        
        return mockHousings;
    }
    
    @DataProvider
    public Object[][] generateTestCases() {
        // We want to test along two axes:
        // - Whether or not the user confirms the shutdown when the dialog appears
        // - How many housings (windows) are open when the action is invoked
        int housingVariations = 5;
        Object[][] cases = new Object[2 * housingVariations][2];
        int count = 0;
        for (boolean confirmed : new boolean[]{false,true}) {
            for (int housings = 0; housings < housingVariations; housings++) {
                cases[count][0] = confirmed;
                cases[count][1] = (housings == 0) ? 0 : (1 << (housings-1));
                count++;
            }
        }
        return cases;        
    }
    
    // Create an array of mocks of a given type
    private <T> T[] mockArray(int size, Class<T> classToMock) {
        @SuppressWarnings("unchecked")
        T[] mocks = (T[]) Array.newInstance(classToMock, size);               
        for (int i = 0; i < size; i++) {
            mocks[i] = Mockito.mock(classToMock, Mockito.RETURNS_MOCKS);
        }
        return mocks;
    }
   
    // Use reflection to "break into" singletons so they can be mocked/spied   
    private <T> T getSingleton(Class<T> singletonClass) throws Exception {
        for (Field f : singletonClass.getDeclaredFields()) {
            if (singletonClass.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers()) ) {
                f.setAccessible(true);
                return singletonClass.cast(f.get(null));
            }
        }
        return null;
    }
    
    private <T> void setSingleton(Class<T> singletonClass, T value) throws Exception {
        for (Field f : singletonClass.getDeclaredFields()) {
            if (singletonClass.isAssignableFrom(f.getType()) && Modifier.isStatic(f.getModifiers()) ) {
                f.setAccessible(true);
                if (Modifier.isFinal(f.getModifiers())) {
                    int mods = f.getModifiers();
                    Field modField = Field.class.getDeclaredField("modifiers");
                    modField.setAccessible(true);
                    modField.set(f, mods & ~Modifier.FINAL);
                }
                f.set(null, value);
                return;
            }
        }
    }
    
}
