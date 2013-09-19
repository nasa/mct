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
package gov.nasa.jsc.mct.executable.buttons;

import gov.nasa.arc.mct.gui.MenuItemInfo;
import gov.nasa.arc.mct.gui.MenuItemInfo.MenuItemType;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.CreateWizardUI;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.jsc.mct.executable.buttons.view.ExecutableButtonManifestation;
import gov.nasa.jsc.mct.executables.buttons.actions.ExecutableButtonAction;
import gov.nasa.jsc.mct.executables.buttons.actions.ExecutableButtonThisAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;


public class ExecutableButtonComponentProvider extends AbstractComponentProvider {

	private static final ResourceBundle bundle = ResourceBundle.getBundle("ResourceBundle"); 
	private final AtomicReference<ComponentTypeInfo> executableButtonComponentType = new AtomicReference<ComponentTypeInfo>();
	
//	private static final ImageIcon VIEW_ICON = 
//			new ImageIcon(ExecutableButtonComponent.class.getResource("/icons/executableIcon.png"));
	
    private static final String OBJECTS_CREATE_EXT_PATH = "/objects/creation.ext";
    private static final String THIS_EXECUTE_PATH = "/this/additions";
    private static final String EXECUTABLE_BUTTON_ACTION_MENU = "EXECUTABLE_BUTTON_ACTION";
    private static final String EXECUTABLE_BUTTON_THIS_MENU = "EXECUTABLE_BUTTON_THIS";
	
    public ExecutableButtonComponentProvider() {
		executableButtonComponentType.set(createTypeInfo());
    }
	private ComponentTypeInfo createTypeInfo() {
		return new ComponentTypeInfo(
				bundle.getString("display_name"),  
				bundle.getString("description"),
				ExecutableButtonComponent.class
				);
	}
	
	@Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		return Collections.singleton(executableButtonComponentType.get());
	}

	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
		if (componentTypeId.equals(ExecutableButtonComponent.class.getName())) {
			Collection<ViewInfo> views = new ArrayList<ViewInfo>();
			views.add(new ViewInfo(ExecutableButtonManifestation.class, ExecutableButtonManifestation.VIEW_NAME, ExecutableButtonManifestation.class.getName(), 
					ViewType.OBJECT, false, ExecutableButtonComponent.class));
			views.add(new ViewInfo(ExecutableButtonManifestation.class, ExecutableButtonManifestation.VIEW_NAME, ExecutableButtonManifestation.class.getName(),
					ViewType.EMBEDDED, false, ExecutableButtonComponent.class));
			return views;
		}
		
		return Collections.emptyList();
	}

	@Override
	public Collection<MenuItemInfo> getMenuItemInfos() {
		return Arrays.asList(                
						new MenuItemInfo(
								OBJECTS_CREATE_EXT_PATH, 
								EXECUTABLE_BUTTON_ACTION_MENU, 
								MenuItemType.NORMAL,
								ExecutableButtonAction.class),
		                new MenuItemInfo(
		                		 THIS_EXECUTE_PATH,
		                		 EXECUTABLE_BUTTON_THIS_MENU,
		                	     MenuItemType.NORMAL,
		                	     ExecutableButtonThisAction.class)
		);
	}
	
    @Override
    public <T> T getAsset(TypeInfo<?> typeInfo, Class<T> assetClass) {
        if (assetClass.isAssignableFrom(ImageIcon.class)) {
            if (typeInfo.getTypeClass().equals(ExecutableButtonManifestation.class)) {
            	// TODO: Needs new icon
                // return assetClass.cast(VIEW_ICON);
            }
        }
        if (assetClass.isAssignableFrom(CreateWizardUI.class)) {
        	if (typeInfo.getTypeClass().equals(ExecutableButtonComponent.class)) {
        		return assetClass.cast(new CreateExecutableButtonComponentWizardUI());
        	}
        }
        return super.getAsset(typeInfo, assetClass);
    }

	
}
