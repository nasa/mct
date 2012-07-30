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
package gov.nasa.arc.mct.platform.spi;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The <code>RoleAccess</code> class is used to inject instances of <code>RoleService</code> using declarative 
 * services. It also provides helper method to determine if a user has a specified role.
 *
 */
public class RoleAccess {
    private static final ConcurrentLinkedQueue<RoleService>  roleServices = new ConcurrentLinkedQueue<RoleService>();

    /**
     * Add a role service 
     * @param roleService registered roleService 
     */
    public void addRoleService(RoleService roleService) {
        roleServices.add(roleService);
    }
    
    /**
     * Determine if a user has a particular role.
     * @param user user
     * @param role role
     * @return
     */
    public static boolean hasRole(User user, String role) {
        for (RoleService roleService: roleServices) {
            if (roleService.hasRole(user, role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if the runtime user can change the owner of a component.  Determines this based on the current
     * runtime user and the roles associated with the runtime user and the component's owner.
     * 
     * @param component the component
     * @param runtimeUser the user
     * @return true if this components owner can be changed.
     */
    public static boolean canChangeOwner(AbstractComponent component, User runtimeUser) {
        
        String componentOwner = component.getOriginalOwner() == null ? component.getOwner() : component.getOriginalOwner();
        if (componentOwner.equals(runtimeUser.getUserId()) || "admin".equals(runtimeUser.getUserId())) {
            return true;
        } else {
            return RoleAccess.hasRole(runtimeUser, componentOwner);
        }
    }

     /**
     * Get the primary role for a user.  Get the list of roles for this user from 
     * Role Data.  If this user has an entry in Role Data, the primary role is the first listed. 
     * If the user has no entry in Role Data, return the default role for all users.
     * @param user the user
     * @return primary role of user or default role if this user does not exist in the role data.
     */
    public static String getPrimaryRole(User user) {
    	String r = null;
    	 for (RoleService roleService: roleServices) {
             if ((r = roleService.getPrimaryRole(user)) != null) {
                 return r;
             }
         }
    	 return null;
    }
    
    /**
     * Get all roles for the first role service.  
     * 
     * @return all roles defined in the first service. If no services are defined,  returns an empty array.
     */
    public static String[] getAllRoles() {
        Set<String> allRoles = new HashSet<String>();
        for (RoleService roleService: roleServices) {
            if ((allRoles = roleService.getAllRoles()) != null) {
                break;
            }
        }
        return allRoles.toArray(new String[allRoles.size()]);    
    }
    
    /**
     * Get all roles for a user, for the first-found role service.  
     * @param user the user
     * @return all user's role defined in the first service. If no services are defined,  returns an empty array.
     */
    public static String[] getAllRoles(String user) {
        Set<String> allRoles = new HashSet<String>();
        for (RoleService roleService: roleServices) {
            if ((allRoles = roleService.getAllRoles(user)) != null) {
                break;
            }
        }
        return allRoles.toArray(new String[allRoles.size()]);
    }
    
    /**
     * Get all users for the first role service.  
     * @param user the current user
     * @return all users defined in the first-found service. If no services are defined,  returns an array containing the current user.
     */
    public static String[] getAllUsers() {
        Set<String> allUsers = new HashSet<String>();
        
        if ( (roleServices.size() == 0) && (PlatformAccess.getPlatform() != null) ) {
            allUsers.addAll(PlatformAccess.getPlatform().getPersistenceProvider().getAllUsers());
            return allUsers.toArray(new String[allUsers.size()]);
        }
        
        for (RoleService roleService: roleServices) {
            if ((allUsers = roleService.getAllUsers()) != null) {
                break;
            }
        }
        return allUsers.toArray(new String[allUsers.size()]);
   }
    
    /**
     * release all registered role services.
     */
    public static void releaseRoleServices() {
        roleServices.clear();
    }
    
    public void removeRoleService(RoleService roleService) {
        roleServices.remove(roleService);
    }
}
