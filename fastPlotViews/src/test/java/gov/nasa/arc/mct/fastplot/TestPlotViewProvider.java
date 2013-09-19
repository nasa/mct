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
package gov.nasa.arc.mct.fastplot;

import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotViewProvider {

    private PlotViewProvider provider;

    @BeforeMethod
    public void setup() {
        provider = new PlotViewProvider();
    }

    @Test
    public void testGetters() {
        Assert.assertEquals(provider.getComponentTypes(), Collections.emptyList());
        Assert.assertEquals(provider.getMenuItemInfos(), Collections.emptyList());
    }

    @Test
    public void testGetViewRoles() {
        Assert.assertTrue(provider.getViews("").contains(new ViewInfo(PlotViewManifestation.class,"","gov.nasa.arc.mct.fastplot.view.PlotViewRole", ViewType.CENTER)));
    }

    @Test
    public void testGetPolicyInfos() {
        Collection<PolicyInfo> infos = provider.getPolicyInfos();
        Assert.assertEquals(infos.size(), 2);
    }
    
    @Test
    public void testGetAsset() {       
        @SuppressWarnings("serial")
		class UnknownType extends ImageIcon {};
        
    	for (ViewInfo viewInfo : provider.getViews("")) {
            // Should have an icon
    		Assert.assertNotNull(provider.getAsset(viewInfo, ImageIcon.class));
    		// Should obey assignable rules for icon
    		Assert.assertNotNull(provider.getAsset(viewInfo, Icon.class));
    		Assert.assertNull(provider.getAsset(viewInfo, UnknownType.class));
    	}

    	// Should not have assets for unknown types
    	TypeInfo<?> unknownInfo = new TypeInfo<UnknownType>(UnknownType.class){};
    	for (Class<?> type : new Class<?>[] {ImageIcon.class,Icon.class,UnknownType.class}) {
    		Assert.assertNull(provider.getAsset(unknownInfo, type));
    	}
    	
    }

}
