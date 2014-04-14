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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ObjectManagerTest {

    @Test
    public void testDirtyObjectManager() {
        // Setup test
        AbstractComponent mockParent = mockComponent();
        AbstractComponent dirtyChild = mockComponent();
        AbstractComponent otherDirtyChild = mockComponent();
        AbstractComponent cleanChild = mockComponent();
        Mockito.when(mockParent.getComponents()).thenReturn(Arrays.asList(dirtyChild, otherDirtyChild, cleanChild));
        Mockito.when(dirtyChild.isDirty()).thenReturn(true);
        Mockito.when(otherDirtyChild.isDirty()).thenReturn(true);
        Mockito.when(cleanChild.isDirty()).thenReturn(false);
        
        // Create the object manager
        ObjectManager dirtyObjectManager = new ObjectManager.DirtyObjectManager(mockParent);
        Set<AbstractComponent> modified = dirtyObjectManager.getAllModifiedObjects();
        
        // Should contain dirty, but not clean child
        Assert.assertTrue(modified.contains(dirtyChild));
        Assert.assertTrue(modified.contains(otherDirtyChild));
        Assert.assertFalse(modified.contains(cleanChild));
        
        // Should not allow explicitly adding objects
        Assert.assertFalse(dirtyObjectManager.addModifiedObject(cleanChild));
        Assert.assertFalse(dirtyObjectManager.addModifiedObject(dirtyChild));
        
        // Saved notification should do nothing - exercise anyway
        dirtyObjectManager.notifySaved(modified);
    }
    
    @Test (timeOut=10000)
    public void testDirtyCycleDetection() {
        // Setup test
        AbstractComponent mockHead = mockComponent();
        AbstractComponent mockTail = mockComponent();
        Mockito.when(mockHead.handleGetCapability(ObjectManager.class)).thenReturn(new ObjectManager.DirtyObjectManager(mockHead));
        Mockito.when(mockTail.handleGetCapability(ObjectManager.class)).thenReturn(new ObjectManager.DirtyObjectManager(mockTail));
        Mockito.when(mockHead.getComponents()).thenReturn(Arrays.asList(mockTail));
        Mockito.when(mockTail.getComponents()).thenReturn(Arrays.asList(mockHead));
        
        // If cycle detection fails, this will get stuck in a loop and the test will time out
        mockHead.getCapability(ObjectManager.class).getAllModifiedObjects();        
    }
    
    @Test
    public void testDirtyObjectManagerAggregates() {
        // Setup test
        AbstractComponent mockParent = mockComponent();
        AbstractComponent dirtyChild = mockComponent();
        AbstractComponent cleanChild = mockComponent();
        AbstractComponent firstGrandchild = mockComponent();
        AbstractComponent secondGrandchild = mockComponent();
        ObjectManager mockDirtyManager = Mockito.mock(ObjectManager.class);
        ObjectManager mockCleanManager = Mockito.mock(ObjectManager.class);
        Mockito.when(mockParent.getComponents()).thenReturn(Arrays.asList(dirtyChild, cleanChild));
        Mockito.when(dirtyChild.isDirty()).thenReturn(true);
        Mockito.when(cleanChild.isDirty()).thenReturn(false);
        Mockito.when(dirtyChild.handleGetCapability(ObjectManager.class))
            .thenReturn(mockDirtyManager);
        Mockito.when(cleanChild.handleGetCapability(ObjectManager.class))
            .thenReturn(mockCleanManager);
        Mockito.when(mockDirtyManager.getAllModifiedObjects())
            .thenReturn(Collections.singleton(firstGrandchild));
        Mockito.when(mockCleanManager.getAllModifiedObjects())
            .thenReturn(Collections.singleton(secondGrandchild));
        
        // Dirty object manager should aggregate from other object managers,
        // even if their parents are clean.
        ObjectManager dirtyObjectManager = new ObjectManager.DirtyObjectManager(mockParent);
        Set<AbstractComponent> modified = dirtyObjectManager.getAllModifiedObjects();

        // Verify that expected components are all there
        Assert.assertTrue(modified.contains(dirtyChild));
        Assert.assertFalse(modified.contains(cleanChild));
        Assert.assertTrue(modified.contains(firstGrandchild));
        Assert.assertTrue(modified.contains(secondGrandchild));
    }
    
    @Test
    public void testExplicitObjectManager() {
        // Create some test components
        AbstractComponent toAdd[] = {
                        mockComponent(),
                        mockComponent(),
                        mockComponent(),
                        mockComponent(),
                        mockComponent()
        };                        
                        
        // Create the manager and verify its initial state
        ObjectManager manager = new ObjectManager.ExplicitObjectManager();
        Assert.assertTrue(manager.getAllModifiedObjects().isEmpty());
        
        // Should be able to add and then find every object
        for (AbstractComponent c: toAdd) {
            Assert.assertTrue(manager.addModifiedObject(c));
            Assert.assertTrue(manager.getAllModifiedObjects().contains(c));
        }
        
        // And verify that they're all still there
        for (AbstractComponent c: toAdd) {
            Assert.assertTrue(manager.getAllModifiedObjects().contains(c));
        }
        
        // Now, generate a set to save
        Set<AbstractComponent> toSave = new HashSet<AbstractComponent>();
        boolean flag = false; // Only save every other component
        for (AbstractComponent c : toAdd) {
            if (flag) {
                toSave.add(c);
            }
            flag = !flag;
        }
        
        // Notify of the save
        manager.notifySaved(toSave);
        
        // Verify that saved objects are no longer found
        // but that unsaved objects are
        for (AbstractComponent c : toAdd) {
            Assert.assertEquals(manager.getAllModifiedObjects().contains(c), !toSave.contains(c));
        }
       
    }    
    
    private AbstractComponent mockComponent() {
        AbstractComponent mockComponent = Mockito.mock(AbstractComponent.class);
        Mockito.when(mockComponent.getComponentId()).thenReturn(UUID.randomUUID().toString());
        return mockComponent;
    }
}
