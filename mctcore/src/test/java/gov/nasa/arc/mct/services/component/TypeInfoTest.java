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

import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;

import javax.swing.Icon;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TypeInfoTest {
    @Test
    public void testGetTypeClass() {
        Assert.assertEquals(new TypeInfo<String>(String.class){}.getTypeClass(), String.class);
        Assert.assertEquals(new TypeInfo<TypeInfoTest>(TypeInfoTest.class){}.getTypeClass(), TypeInfoTest.class);
    }
    
    @Test
    public void testGetAsset() {
        // Verify that getAsset is robust to null platform, etc
        Platform oldPlatform = PlatformAccess.getPlatform();
        Platform mockPlatform = Mockito.mock(Platform.class);
        CoreComponentRegistry mockRegistry = Mockito.mock(CoreComponentRegistry.class);
        Icon mockIcon = Mockito.mock(Icon.class);
        
        TypeInfo<?> info = new TypeInfo<TypeInfoTest>(TypeInfoTest.class){};
        
        // Try with a null platform - should fail to retrieve asset
        new PlatformAccess().setPlatform(null);
        Assert.assertNull(info.getAsset(Icon.class));        
        
        // Try with a platform but no registry - should fail to retrieve asset
        new PlatformAccess().setPlatform(mockPlatform);
        Assert.assertNull(info.getAsset(Icon.class));        
               
        // Try with a mock registry (but no assets) - should fail to retrieve
        Mockito.when(mockPlatform.getComponentRegistry()).thenReturn(mockRegistry);
        Assert.assertNull(info.getAsset(Icon.class));        
                
        // Try with a mock registry that has the requested asset - should retrieve it
        Mockito.when(mockRegistry.getAsset(info, Icon.class)).thenReturn(mockIcon);
        Assert.assertEquals(info.getAsset(Icon.class), mockIcon);        
        
        // Restore old platform
        new PlatformAccess().setPlatform(oldPlatform);
    }
}
