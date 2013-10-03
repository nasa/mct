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
package gov.nasa.arc.mct.core.provider;

import gov.nasa.arc.mct.core.components.BrokenComponent;
import gov.nasa.arc.mct.core.components.BrokenInfoPanel;
import gov.nasa.arc.mct.core.components.MineTaxonomyComponent;
import gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent;
import gov.nasa.arc.mct.core.components.TelemetryDisciplineComponent;
import gov.nasa.arc.mct.core.components.TelemetryUserDropBoxComponent;
import gov.nasa.arc.mct.core.roles.DropboxCanvasView;
import gov.nasa.arc.mct.core.roles.UsersManifestation;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CoreComponentProviderTest {
    private CoreComponentProvider provider;
    
    @BeforeMethod
    public void setup() {
        provider = new CoreComponentProvider();
    }
    
    @Test (dataProvider="componentTypesTestCases")
    public void testComponentTypes(Class<?> expectedType) {
        // Verify that all expected type are in getComponentTypes
        Collection<ComponentTypeInfo> typeInfos = provider.getComponentTypes();
        boolean found = false;
        for (TypeInfo<?> t : typeInfos) {
            found |= t.getTypeClass().equals(expectedType);
        }
        Assert.assertTrue(found);
    }
    
    @DataProvider
    public Object[][] componentTypesTestCases() {
        return new Object[][] {
                { TelemetryDisciplineComponent.class },
                { TelemetryUserDropBoxComponent.class },
                { MineTaxonomyComponent.class },
                { BrokenComponent.class },
                { TelemetryDataTaxonomyComponent.class }
        };
    }
    
    @Test (dataProvider="viewTestCases")
    public void testViews(Class<?> compClass, Class<?> viewClass, int size) {
        Collection<ViewInfo> views =
                provider.getViews(compClass.getName());
        Assert.assertEquals(views.size(), size);
        if (size > 0) {
            boolean found = false;
            for (TypeInfo<?> t : views) {
                found |= t.getTypeClass().equals(viewClass);
            }
            Assert.assertTrue(found);
        }
    }
    
    @DataProvider
    public Object[][] viewTestCases() {
        return new Object[][] {
                { TelemetryDisciplineComponent.class, UsersManifestation.class, 1 },
                { TelemetryUserDropBoxComponent.class, DropboxCanvasView.class, 2 },
                { MineTaxonomyComponent.class , null, 0},
                { BrokenComponent.class, BrokenInfoPanel.class, 1 },
                { TelemetryDataTaxonomyComponent.class, null, 0 }
        };
    }
    
    @Test
    public void testBrokenComponent() {
        Assert.assertEquals(provider.getBrokenComponent(), BrokenComponent.class);
    }
    
    @Test (dataProvider="assetTestCases")
    public void testAssets(Class<?> compClass, Class<?> assetClass, boolean expected) {
        TypeInfo<?> typeInfo = new TypeInfo<Object>(compClass){};        
        if (expected) {
            Assert.assertNotNull(provider.getAsset(typeInfo, assetClass));
        } else {
            Assert.assertNull(provider.getAsset(typeInfo, assetClass));
        }
    }
    
    @DataProvider
    public Object[][] assetTestCases() {
        return new Object[][] {
                // Verify that disciplines and drop boxes have icons
                { TelemetryDisciplineComponent.class, ImageIcon.class, true },
                { TelemetryUserDropBoxComponent.class, ImageIcon.class, true },
                { MineTaxonomyComponent.class, ImageIcon.class, false },
                { BrokenComponent.class, ImageIcon.class, false },
                { TelemetryDataTaxonomyComponent.class, ImageIcon.class, false },
                // Should also return for icon (assignable)
                { TelemetryDisciplineComponent.class, Icon.class, true },
                { TelemetryUserDropBoxComponent.class, Icon.class, true },
                { MineTaxonomyComponent.class, Icon.class, false },
                { BrokenComponent.class, Icon.class, false },
                { TelemetryDataTaxonomyComponent.class, Icon.class, false },
                // No create wizards
                { TelemetryDisciplineComponent.class, CreateWizardUI.class, false },
                { TelemetryUserDropBoxComponent.class, CreateWizardUI.class, false },
                { MineTaxonomyComponent.class, CreateWizardUI.class, false },
                { BrokenComponent.class, CreateWizardUI.class, false },
                { TelemetryDataTaxonomyComponent.class, CreateWizardUI.class, false },
        };
    }
    
}
