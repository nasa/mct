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
package gov.nasa.arc.mct.gui.util;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.MCTMutableTreeNode;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.ViewInfo;

/**
 * GUI utility implementation. 
 *
 */
                
public class GUIUtil {

    
    /**
     * Clones the node from the view role's model role by lazily loading only
     * its first level children.
     * 
     * @param component - The AbstractComponent to be cloned.
     * @param viewInfo - root view info be cloned.
     * @return cloned subtree
     */
    public static final MCTMutableTreeNode cloneTreeNode(AbstractComponent component, ViewInfo viewInfo) {
        return cloneFirstLevelViewRoles(component,viewInfo);
    }
    
    /**
     * Clones the 1st level children.
     * @param component - The AbstractComponent to be cloned.
     * @param viewInfo
     * @return cloned subtree rooted from view role's model role.
     */
    private static final MCTMutableTreeNode cloneFirstLevelViewRoles(AbstractComponent component, ViewInfo info) {
        MCTMutableTreeNode cloned = new MCTMutableTreeNode(info.createView(component));
        if (!component.isLeaf()) {
            cloned.setProxy(true);
            MCTMutableTreeNode proxyChildNode = new MCTMutableTreeNode(View.NULL_VIEW_MANIFESTATION);
            cloned.add(proxyChildNode);
        }

        return cloned;
    }

}
