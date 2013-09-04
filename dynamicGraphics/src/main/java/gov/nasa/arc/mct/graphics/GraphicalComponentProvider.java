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
package gov.nasa.arc.mct.graphics;

import gov.nasa.arc.mct.graphics.component.GraphicalComponent;
import gov.nasa.arc.mct.graphics.component.GraphicalComponentWizardUI;
import gov.nasa.arc.mct.graphics.view.GraphicalManifestation;
import gov.nasa.arc.mct.graphics.view.StaticGraphicalView;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;



public class GraphicalComponentProvider extends AbstractComponentProvider {
    private static ResourceBundle bundle = ResourceBundle.getBundle("GraphicsResourceBundle");
    final Collection<PolicyInfo> policyInfos;
    
    private static final List<ViewInfo> VIEW_INFOS = Arrays.asList(
    		new ViewInfo(GraphicalManifestation.class, GraphicalManifestation.VIEW_ROLE_NAME, GraphicalManifestation.class.getName(), ViewType.OBJECT, 
					new ImageIcon(GraphicalManifestation.class.getResource("/icons/graphicsViewButton-OFF.png")),
					new ImageIcon(GraphicalManifestation.class.getResource("/icons/graphicsViewButton-ON.png"))),
    		new ViewInfo(GraphicalManifestation.class, GraphicalManifestation.VIEW_ROLE_NAME, GraphicalManifestation.class.getName(), ViewType.EMBEDDED,
					new ImageIcon(GraphicalManifestation.class.getResource("/icons/graphicsViewButton-OFF.png")),
					new ImageIcon(GraphicalManifestation.class.getResource("/icons/graphicsViewButton-ON.png"))),
    		new ViewInfo(StaticGraphicalView.class, GraphicalManifestation.VIEW_ROLE_NAME, ViewType.OBJECT),
    		new ViewInfo(StaticGraphicalView.class, GraphicalManifestation.VIEW_ROLE_NAME, ViewType.EMBEDDED)
    		);     
    
    public GraphicalComponentProvider(){
    	policyInfos = Arrays.asList(new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), GraphicalStringPolicy.class),
    								new PolicyInfo(PolicyInfo.CategoryType.FILTER_VIEW_ROLE.getKey(), GraphicalViewPolicy.class));
    }
   
    @Override
    public Collection<ViewInfo> getViews(String componentTypeId) {
    	return VIEW_INFOS;
    }
    
	@Override
	public Collection<PolicyInfo> getPolicyInfos() {
        return policyInfos;
	}
	
    @Override
	public Collection<ComponentTypeInfo> getComponentTypes() {
		return Collections.singleton(
				new ComponentTypeInfo(bundle.getString("Component_Name"),
						bundle.getString("Component_Description"), 
						GraphicalComponent.class, 
						new GraphicalComponentWizardUI(),
						null));//new ImageIcon(GraphicalComponentProvider.class.getResource("/icons/importSVG.png"))));
	}
	
	
}
