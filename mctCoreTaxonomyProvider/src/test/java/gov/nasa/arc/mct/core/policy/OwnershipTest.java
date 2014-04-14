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
package gov.nasa.arc.mct.core.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.collection.Group;
import gov.nasa.arc.mct.platform.core.access.PlatformAccess;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.RoleAccess;
import gov.nasa.arc.mct.platform.spi.RoleService;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OwnershipTest {
   
    private AbstractComponent pseudoComponent;
    
    @Mock
    private Group mockGroup;
    
    @Mock
    private User mockUser;
    
    @Mock
    private Platform mockPlatform;
    
    
    private Policy ownershipPolicy = new CheckComponentOwnerIsUserPolicy();
    
    private Platform originalPlatform;
    
    @BeforeClass
    public void setPlatform() {
        originalPlatform = PlatformAccess.getPlatform();
    }
    
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);       
        new PlatformAccess().setPlatform(mockPlatform);        
    }
    
    @AfterClass
    public void resetPlatform() {
        new PlatformAccess().setPlatform(originalPlatform);
    }
    
    /**
     * Test for ownership. Should a given user be able to modify a 
     * component, based on the userID/group of the user as well 
     * as the ownership/group of the component?
     * This is driven by the data set "testCases".
     * @param userID the user name
     * @param groupID the user's group
     * @param compOwner the component's owner
     * @param compGroup the component's group
     * @param expectedResult true if this should be allowed; otherwise false
     */
    @Test (dataProvider="testData")
    public void testOwnership(
            String userID, 
            String groupID, 
            final String compOwner,
            final String compGroup, 
            final boolean hasGroupCapability, 
            boolean expectedResult) {
        PolicyContext context = new PolicyContext();
        
        // getCapability is final, so we can't just mock....
        pseudoComponent = new AbstractComponent() {
            public String getOwner() {
                return compOwner;
            }
            
            public <T> T handleGetCapability(Class<T> capability) {
                if (capability.isAssignableFrom(Group.class) && hasGroupCapability) {
                    return capability.cast(mockGroup);
                }
                return super.handleGetCapability(capability);
            };
        };
        
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), pseudoComponent);
        
        Mockito.when(mockPlatform.getCurrentUser()).thenReturn(mockUser);
        Mockito.when(mockGroup.getDiscipline()).thenReturn(compGroup);
        Mockito.when(mockUser.getUserId()).thenReturn(userID);
        Mockito.when(mockUser.getDisciplineId()).thenReturn(groupID);
        
        ExecutionResult result = ownershipPolicy.execute(context);
        Assert.assertEquals(result.getStatus(), expectedResult);
    }
    
    
    @DataProvider
    public Object[][] testData() {
        List<Object[]> result = new ArrayList<Object[]>();
        String[] users  = { "testUser1", "testUser2" };
        String[] groups = { "testGroup1", "testGroup2", null };
        boolean[] truths = { true, false };
        
        for (String userID : users) {
            for (String groupID : groups) {
                // Don't let the user's group be null
                // (object's group may be)
                if (groupID == null) continue;               
                for (String compOwner : users) {
                    for (String compGroup : groups) {
                        for (boolean hasGroupCapability : truths) {
                            result.add(new Object[] {
                                    userID,
                                    groupID,
                                    compOwner,
                                    compGroup,
                                    hasGroupCapability,
                                    userID.equals(compOwner) || (hasGroupCapability && groupID.equals(compGroup))
                            });
                        }
                    }
                }
            }
        }
        
        return result.toArray(new Object[result.size()][]);
    }
    
    @Test
    public void testUsesRoleService() {
        RoleService mockRoleService = Mockito.mock(RoleService.class);
        new RoleAccess().addRoleService(mockRoleService);
        
        AbstractComponent mockComponent = Mockito.mock(AbstractComponent.class);
        
        Mockito.when(mockPlatform.getCurrentUser()).thenReturn(mockUser);
        Mockito.when(mockUser.getUserId()).thenReturn("testUser1");
        Mockito.when(mockUser.getDisciplineId()).thenReturn("testGroup1");
        Mockito.when(mockComponent.getOwner()).thenReturn("testRole1");
        
        Mockito.verifyZeroInteractions(mockRoleService);
                
        PolicyContext context = new PolicyContext();
        context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), pseudoComponent);
        
        Mockito.when(mockRoleService.hasRole(Mockito.<User>any(), Mockito.anyString())).thenReturn(false);
        Assert.assertFalse(ownershipPolicy.execute(context).getStatus());
        
        Mockito.when(mockRoleService.hasRole(Mockito.<User>any(), Mockito.anyString())).thenReturn(true);              
        Assert.assertTrue(ownershipPolicy.execute(context).getStatus());
        
        new RoleAccess().removeRoleService(mockRoleService);
    }
   

}
