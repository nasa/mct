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
package gov.nasa.arc.mct.services.component;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ComponentTypeInfoTest {

    // Deprecated constructors are exercised for coverage
    @SuppressWarnings("deprecation")
    @Test
    public void testIsCreatable() {
        ComponentTypeInfo info;
        info = new ComponentTypeInfo("","",AbstractComponent.class,true);
        Assert.assertTrue(info.isCreatable());
        info = new ComponentTypeInfo("","",AbstractComponent.class,false);
        Assert.assertFalse(info.isCreatable());
        info = new ComponentTypeInfo("","",AbstractComponent.class,true,null);
        Assert.assertTrue(info.isCreatable());
        info = new ComponentTypeInfo("","",AbstractComponent.class,false,null);
        Assert.assertFalse(info.isCreatable());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedMethods() {
        // Verify that deprecated methods are redirected to getAsset
        Platform oldPlatform = PlatformAccess.getPlatform();
        Platform mockPlatform = Mockito.mock(Platform.class);
        CoreComponentRegistry mockRegistry = Mockito.mock(CoreComponentRegistry.class);
        Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockRegistry);
        new PlatformAccess().setPlatform(mockPlatform);
        
        ComponentTypeInfo info;
        info = new ComponentTypeInfo("","",AbstractComponent.class);
        info.getIcon();
        Mockito.verify(mockRegistry).getAsset(info, javax.swing.ImageIcon.class);
        
        info = new ComponentTypeInfo("","",AbstractComponent.class);
        info.getWizardUI();
        Mockito.verify(mockRegistry).getAsset(info, CreateWizardUI.class);
        
        
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
}
