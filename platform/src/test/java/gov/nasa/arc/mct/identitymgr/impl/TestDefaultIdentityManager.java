package gov.nasa.arc.mct.identitymgr.impl;

import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TestDefaultIdentityManager {

    // Some sample users & corresponding groups 
    private String[] users = {
            "mockUser", "mockAdmin", "mockOther"
    };    
    private String[] groups = {
            "users", "admin", "other"
    };
    
    private Platform oldPlatform;
    @Mock Platform mockPlatform;
    @Mock PersistenceProvider mockPersistence;
    @Mock WindowManager mockWindowing;
    @Mock Properties mockProperties;
    
    @BeforeClass
    public void setup() {
        oldPlatform = PlatformAccess.getPlatform();
    }
    
    @AfterClass
    public void teardown() {
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
    @BeforeMethod
    public void setupMethod() {
        MockitoAnnotations.initMocks(this);
        
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getWindowManager()).thenReturn(mockWindowing);

        // Expose sample users/groups through persistence
        for (int i = 0; i < 3; i++) {
            User mockUser = Mockito.mock(User.class);
            Mockito.when(mockUser.getUserId()).thenReturn(users[i]);
            Mockito.when(mockUser.getDisciplineId()).thenReturn(groups[i]);
            Mockito.when(mockPersistence.getUser(users[i])).thenReturn(mockUser);
        }
        Set<String> userSet = new HashSet<String>();
        userSet.addAll(Arrays.<String>asList(users));        
        Mockito.when(mockPersistence.getAllUsers()).thenReturn(userSet);
        
        new PlatformAccess().setPlatform(mockPlatform);
    }
    
    @Test
    public void testMonitorDisabled() {
        // The default identity manager should have no monitor
        Assert.assertFalse(new DefaultIdentityManager(mockProperties).isMonitorRunning());
    }
        
    @Test
    public void testNoDialogForNoUsers() {
        // No input dialog should be shown if there are no users in the DB
        Mockito.when(mockPersistence.getAllUsers()).thenReturn(Collections.<String>emptySet());        
        new DefaultIdentityManager(mockProperties);        
        Mockito.verify(mockWindowing, Mockito.never()).showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any());
    }
    
    @Test
    public void testDialogForUsers() {
        // If multiple users are present, input dialog should be shown                     
        new DefaultIdentityManager(mockProperties);        
        Mockito.verify(mockWindowing, Mockito.times(1)).showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any());
    }
    
    @Test
    public void testSelectionForUsers() {
        // Verify that IdentityManager matches selection from shown dialog               
        for (int i = 0; i < 3; i++) {
            Mockito.when(mockWindowing.showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any())).thenReturn(users[i]);
            IdentityManager identityManager = new DefaultIdentityManager(mockProperties);        
            Assert.assertEquals(identityManager.getCurrentUser(), users[i]);
            Assert.assertEquals(identityManager.getCurrentGroup(), groups[i]);
        }
    }
    
    @Test
    public void testMatchesMCTProperties() {
        // Here, we make sure that specifying mct.user still works with this identity manager
        for (int i = 0; i < 3; i++) {
            Mockito.when(mockProperties.getProperty("mct.user")).thenReturn(users[i]);
            IdentityManager identityManager = new DefaultIdentityManager(mockProperties);   
            // Input dialog should not be shown - mct.user was set!
            Mockito.verify(mockWindowing, Mockito.never()).showInputDialog(Mockito.anyString(), Mockito.anyString(), Mockito.<Object[]>any(), Mockito.any());
            
            // But we should still have "loaded" the requested user
            Assert.assertEquals(identityManager.getCurrentUser(), users[i]);
            Assert.assertEquals(identityManager.getCurrentGroup(), groups[i]);
            
            // Finally, ensure there's still no monitor
            Assert.assertFalse(identityManager.isMonitorRunning());
        }
    }
    
}
