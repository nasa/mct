package gov.nasa.arc.mct.identitymgr.impl;

import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.WindowManager;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * A minimal implementation of an IdentityManager suitable for demonstrations or 
 * evaluation environments. Prompts the user to enter their name if no user name 
 * (mct.user) has been specified.
 * 
 * @author vwoeltje
 *
 */
public class DefaultIdentityManager extends IdentityManager {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("DefaultIdentityManager"); //NOI18N
    private boolean initialized = false;    
    
    /**
     * Create a default identity manager
     * @param properties input properties
     */
    public DefaultIdentityManager(Properties properties) {
        this.currentUser = properties.getProperty("mct.user"); //NOI18N
    }
    
    @Override
    public String getCurrentUser() {
        ensureInitialized();
        return super.getCurrentUser();
    }

    @Override
    public String getCurrentGroup() {
        ensureInitialized();
        return super.getCurrentGroup();
    }

    private void ensureInitialized() {
        if (!initialized) {
            String username = this.currentUser;
            // If PersistenceProvider & WindowManager are available, we can create a dialog with available users
            Platform platform = PlatformAccess.getPlatform();
            PersistenceProvider persistence = platform != null ? platform.getPersistenceProvider() : null;
            WindowManager windowing = platform != null ? platform.getWindowManager() : null;
            
            if (username == null && persistence != null && windowing != null) {
                Object[] users = persistence.getAllUsers().toArray();
                if (users.length > 0) {
                    Arrays.sort(users);
                    username = (String) windowing.showInputDialog(bundle.getString("TITLE"), bundle.getString("QUESTION"), users, users[0], null); //NOI18N
                }
            }        
            
            this.currentUser = username;
            
            // If a user was selected, initialize with group info as well from Persistence
            if (persistence != null && username != null) {
                User user = persistence.getUser(username);
                if (user != null) {        
                    this.currentGroup = user.getDisciplineId();
                }
            }
        }
    }
    
    @Override
    public boolean isMonitorRunning() {
        // There is no need for a monitor here.
        return false;
    }

}
