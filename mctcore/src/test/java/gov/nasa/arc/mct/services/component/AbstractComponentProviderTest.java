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

import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractComponentProviderTest {
    @Test
    public void testAbstractComponentProvider() {
        // Verify that:
        // - Collection methods return empty (non-null) by default
        // - Non-collection methods return null
        AbstractComponentProvider provider = new AbstractComponentProvider(){};
        
        Assert.assertTrue(provider.getComponentTypes().isEmpty());
        Assert.assertTrue(provider.getMenuItemInfos().isEmpty());
        Assert.assertTrue(provider.getPolicyInfos().isEmpty());
        Assert.assertTrue(provider.getStatusAreaWidgetInfos().isEmpty());
        Assert.assertTrue(provider.getViews("").isEmpty());
        Assert.assertNull(provider.getSearchProvider());
        Assert.assertNull(provider.getProviderDelegate());        
        Assert.assertNull(provider.getAsset(null, null));
    }
}
