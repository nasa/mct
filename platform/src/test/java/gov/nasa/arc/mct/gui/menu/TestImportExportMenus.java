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
package gov.nasa.arc.mct.gui.menu;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareMenu;
import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuSection;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.menu.ExportMenu.ObjectsExportMenu;
import gov.nasa.arc.mct.gui.menu.ExportMenu.ThisExportMenu;
import gov.nasa.arc.mct.gui.menu.ImportMenu.ObjectsImportMenu;
import gov.nasa.arc.mct.gui.menu.ImportMenu.ThisImportMenu;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.PolicyManager;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestImportExportMenus {

    private Platform oldPlatform;
    private Platform mockPlatform;
    
    @BeforeClass
    public void setup() {
        oldPlatform = PlatformAccess.getPlatform();        
        mockPlatform = Mockito.mock(Platform.class);
        new PlatformAccess().setPlatform(mockPlatform);
    }
    
    @AfterClass
    public void teardown() {
        new PlatformAccess().setPlatform(oldPlatform);
    }
    
    @Test
    public void testExportMenus() {
        // Create test objects
        ContextAwareMenu thisMenu = new ThisExportMenu();
        ContextAwareMenu objsMenu = new ObjectsExportMenu();
        thisMenu.initialize();
        objsMenu.initialize();        
        
        // Name is specified in code, so verify it
        Assert.assertEquals(thisMenu.getText(), "Export");
        Assert.assertEquals(objsMenu.getText(), "Export");
        
        // Should always return true to canHandle
        Assert.assertTrue(thisMenu.canHandle(null));
        Assert.assertTrue(objsMenu.canHandle(null));
        
        // Verify that expected extension points are published
        String[] thisExts = thisMenu.getExtensionMenubarPaths();
        String[] objsExts = objsMenu.getExtensionMenubarPaths();
        Assert.assertEquals(thisExts[0], "/this/export.ext");
        Assert.assertEquals(objsExts[0], "/objects/export.ext");
        
        // Verify that export to image is included
        Assert.assertTrue(find("EXPORT_THIS_TO_IMAGE", thisMenu));
        Assert.assertTrue(find("EXPORT_VIEW_TO_IMAGE", objsMenu));
    }
    
    private boolean find (String commandKey, ContextAwareMenu menu) {
        boolean found = false;
        
        for (MenuSection section : menu.getMenuSections()) {
            for (MenuItemInfo info : section.getMenuItemInfoList()) {
                found |= commandKey.equals(info.getCommandKey());
            }
        }
        
        return found;
    }

    @Test
    public void testImportMenus() {
        // Create test objects
        ContextAwareMenu thisMenu = new ThisImportMenu();
        ContextAwareMenu objsMenu = new ObjectsImportMenu();
        thisMenu.initialize();
        objsMenu.initialize();
        
        // Name is specified in code, so verify it
        Assert.assertEquals(thisMenu.getText(), "Import");
        Assert.assertEquals(objsMenu.getText(), "Import");
        
        // Verify that expected extension points are published
        String[] thisExts = thisMenu.getExtensionMenubarPaths();
        String[] objsExts = objsMenu.getExtensionMenubarPaths();
        Assert.assertEquals(thisExts[0], "/this/import.ext");
        Assert.assertEquals(objsExts[0], "/objects/import.ext");        

        
        // Should only handle single, writable targets...
        
        // Set up mocks
        ActionContext mockContext = Mockito.mock(ActionContext.class);
        PolicyManager mockPolicy = Mockito.mock(PolicyManager.class);
        AbstractComponent mockComponent = Mockito.mock(AbstractComponent.class);
        View mockView = Mockito.mock(View.class);
        String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY.getKey();
        
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicy);
        
        // First, This's Import menu
        // No window active - should disallow
        Mockito.when(mockContext.getWindowManifestation()).thenReturn(null);
        Assert.assertFalse(thisMenu.canHandle(mockContext));
        
        // Active window but component is null - should disallow
        Mockito.when(mockContext.getWindowManifestation()).thenReturn(mockView);
        Mockito.when(mockView.getManifestedComponent()).thenReturn(null);
        Assert.assertFalse(thisMenu.canHandle(mockContext));
        
        // Has a window, but policy says no - should disallow
        Mockito.when(mockContext.getWindowManifestation()).thenReturn(mockView);
        Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);
        Mockito.when(mockPolicy.execute(Mockito.eq(compositionKey), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null, false, ""));
        Assert.assertFalse(thisMenu.canHandle(mockContext));
        
        // If policy allows, then canHandle should be true
        Mockito.when(mockPolicy.execute(Mockito.eq(compositionKey), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null, true, ""));
        Assert.assertTrue(thisMenu.canHandle(mockContext));
        
        
        // Second, Objects's Import menu
        // Null selections - should disallow
        Mockito.when(mockContext.getSelectedManifestations()).thenReturn(null);
        Assert.assertFalse(objsMenu.canHandle(mockContext));

        // Empty selections - should disallow
        Mockito.when(mockContext.getSelectedManifestations())
            .thenReturn(Collections.<View>emptyList());
        Assert.assertFalse(objsMenu.canHandle(mockContext));
        
        // One selection but policy says no - should disallow
        Mockito.when(mockContext.getSelectedManifestations())
            .thenReturn(Arrays.asList(mockView));
        Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);        
        Mockito.when(mockPolicy.execute(Mockito.eq(compositionKey), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null, false, ""));               
        Assert.assertFalse(objsMenu.canHandle(mockContext));
        
        // One selection but policy says yes - should allow!
        Mockito.when(mockContext.getSelectedManifestations())
            .thenReturn(Arrays.asList(mockView));
        Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);        
        Mockito.when(mockPolicy.execute(Mockito.eq(compositionKey), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null, true, ""));               
        Assert.assertTrue(objsMenu.canHandle(mockContext));
        
        // Multiple selections, even though policy says yes - should disallow
        Mockito.when(mockContext.getSelectedManifestations())
            .thenReturn(Arrays.asList(mockView, mockView));
        Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);        
        Mockito.when(mockPolicy.execute(Mockito.eq(compositionKey), Mockito.<PolicyContext>any()))
            .thenReturn(new ExecutionResult(null, true, ""));               
        Assert.assertFalse(objsMenu.canHandle(mockContext));

    }
}
