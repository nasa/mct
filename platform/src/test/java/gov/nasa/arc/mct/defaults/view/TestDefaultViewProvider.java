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
package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDefaultViewProvider {

    
    @Test
    public void testGetViews() {
        // Ensure platform is providing necessary view types
        Set<ViewType> viewTypes = new HashSet<ViewType>();
              
        for (ViewInfo vi : new DefaultViewProvider().getViews("component")) {
            viewTypes.add(vi.getViewType());
        }
        
        // Needed for browse trees, directory areas
        Assert.assertTrue(viewTypes.contains(ViewType.NODE));
        
        // Needed as top-level housing
        Assert.assertTrue(viewTypes.contains(ViewType.LAYOUT));
        
        // Needed for inspecting selections
        Assert.assertTrue(viewTypes.contains(ViewType.INSPECTOR));
        
        // Needed for adding common view switchers to custom inspectors
        Assert.assertTrue(viewTypes.contains(ViewType.VIEW_SWITCHER));
        
        // Needed for browsing
        Assert.assertTrue(viewTypes.contains(ViewType.NAVIGATOR));
        
        // Needed for titling inspectors
        Assert.assertTrue(viewTypes.contains(ViewType.TITLE));
        
        // Needed for containing inspectors
        Assert.assertTrue(viewTypes.contains(ViewType.RIGHT));        
    }
    
}
