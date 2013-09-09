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

/**
 * A ViewType represents where the view is appropriate for usage within the platform. For example, some views are appropriate only in
 * a tree style display, which others are designed to edit specific aspects of an object. 
 *
 */
public enum ViewType {
    /**
     * A view that can be presented as the main view of an object. The layout type defines the placement and types 
     * of views so this view may be placed differently depending on the available layout types. The center view is 
     * expected to publish selection events to drive instances of the inspector. 
     */
    CENTER, 
    /**
     * A view that is placed in the right pane of the window. 
     */
    RIGHT,
    /**
     * A view that shows context sensitive views based on the current selection. An inspector is driven by the current selection within the 
     * window and thus is required to use the <code>SelectionProvider</code> associated with
     * the housing to determine which inspector views to show. This view will display views that are appropriate for INSPECTOR. 
     * 
     * @see gov.nasa.arc.mct.gui.SelectionProvider
     */
    INSPECTOR, 
    /**
     * The actual inspector.
     * A view that shows context sensitive views based on the current selection in the view showing the center pane. 
     * An inspector is driven by the current selection within the 
     * window and thus is required to use the <code>SelectionProvider</code> associated with
     * the housing to determine which inspector views to show. This view will display views that are appropriate for INSPECTOR. 
     * 
     * @see gov.nasa.arc.mct.gui.SelectionProvider
     */
    CENTER_OWNED_INSPECTOR,
    /**
    * A view that can shown as a navigation aide (currently this is the directory area in MCT). A navigator
    * may publish selection events, so an inspector view can present the appropriate views based on the selection. 
    * This view must also provide a way to manipulate delegate components, for example using drag and drop to allow. This
    * type is provided by the platform and component authors generally will not need to add views of this type. 
    * 
    * @see gov.nasa.arc.mct.gui.SelectionProvider
    */
    NAVIGATOR,
    /** an OBJECT inspection
     * A view that can be displayed within an <code>INSPECTOR</code>. 
     * */
    OBJECT, 
    
    /** 
     * 
     * Identifies a view that can organize other views. For example, the MCT platform provides a layout view to produce the 
     * three section view for a non leaf component. 
     * 
     */
    LAYOUT,
    
    /**
     * This defines a node view when a component is represented in a tree. This view will likely be eliminated in the future as this information
     * can be derived from the current APIs. 
     */
    NODE,
    
    /**
     * A title view role can be used when displaying a human readable name of a component. This view will likely be eliminated in the future as this
     * information can be derived from the current APIs. 
     */
    TITLE,
    
    /**
     * An embedded view is suitable for nesting within other views. A nested view's properties may be owned and controlled
     * by another view and should not mutate properties of the underlying component. For example, editing model state as there is
     * no guarantee that the referenced object will be directly controlled. Thus any view which mutates properties of
     * the component should not use this view type; however, other view which support the <code>OBJECT</code> type that
     * do not mutate component properties should define a view info with this type. 
     */
    EMBEDDED,
    
    /**
     * An embedded view suitable for switching components, such as the 
     * drop-down in the Inspector area.
     */
    VIEW_SWITCHER
}