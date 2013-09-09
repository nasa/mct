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

import gov.nasa.arc.mct.gui.housing.InspectionArea;
import gov.nasa.arc.mct.gui.housing.Inspector;
import gov.nasa.arc.mct.gui.housing.MCTDirectoryArea;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

/**
 * This class provides default views for those
 * components that do not provide a view. 
 *
 */
public class DefaultViewProvider extends AbstractComponentProvider {

	private final List<ViewInfo> myViewInfos;
		
	public DefaultViewProvider() {
	    ResourceBundle bundle = ResourceBundle.getBundle("DefaultView"); 
	    
	    List<ViewInfo> viewInfos = new ArrayList<ViewInfo>();
	    viewInfos.add(new ViewInfo(NodeViewManifestation.class, NodeViewManifestation.DEFAULT_NODE_VIEW_ROLE_NAME, ViewType.NODE));
        viewInfos.add(new ViewInfo(MCTHousingViewManifestation.class, MCTHousingViewManifestation.VIEW_ROLE_NAME, ViewType.LAYOUT));
        viewInfos.add(new ViewInfo(Inspector.class, Inspector.DEFAULT_INSPECTOR_VIEW_PROP_KEY, ViewType.INSPECTOR));
        viewInfos.add(new ViewInfo(MCTDirectoryArea.class, MCTDirectoryArea.VIEW_NAME, ViewType.NAVIGATOR));
        viewInfos.add(new ViewInfo(LabelViewManifestation.class, bundle.getString("LabelViewRoleName"), ViewType.TITLE));
        viewInfos.add(new ViewInfo(InspectionArea.class, InspectionArea.INSPECTION_AREA_VIEW_PROP_KEY, ViewType.RIGHT));
        viewInfos.add(new ViewInfo(SwitcherView.class, bundle.getString("SwitcherViewName"), ViewType.VIEW_SWITCHER));
        viewInfos.add(new ViewInfo(InfoView.class, bundle.getString("InfoViewName"), ViewType.OBJECT, 
                new ImageIcon(getClass().getResource("/icons/mct_icon_menu_info.png"))));

        myViewInfos = viewInfos;
	}
	
	@Override
	public Collection<ViewInfo> getViews(String componentTypeId) {
	    return myViewInfos;
	}

}
