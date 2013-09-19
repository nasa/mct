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

import gov.nasa.arc.mct.component.MockComponent;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl;
import gov.nasa.arc.mct.registry.ExternalComponentRegistryImpl.ExtendedComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;

import java.util.Collections;

import javax.swing.ImageIcon;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestComponentIcon {
    private MockComponent component;
    @Mock 
    private ComponentProvider mockProvider;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        component = new MockComponent();
    }

    @SuppressWarnings("deprecation") // getIcon deprecated but may still be used, so test
    @Test
    public void testCreateManifestation() {
        ImageIcon icon = new ImageIcon();
        ExtendedComponentProvider extendedProvider = new ExtendedComponentProvider(mockProvider, "mock bundle");
        ComponentTypeInfo info = new ComponentTypeInfo("displayname", "desc", MockComponent.class, null, icon);
        Mockito.when(mockProvider.getComponentTypes()).thenReturn(Collections.singleton(info));        
        ExternalComponentRegistryImpl.getInstance().refreshComponents(Collections.singletonList(extendedProvider));        
        Assert.assertSame(component.getIcon(), info.getIcon());
        ExternalComponentRegistryImpl.getInstance().refreshComponents(Collections.<ExtendedComponentProvider>emptyList());
    }

}
