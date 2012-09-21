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
package gov.nasa.arc.mct.table.gui;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implements a manager of sets of components grouped by a
 * string tag. The visibility of the components for a tag
 * may be jointly set. This manager is designed to be used
 * by control panels where some of the controls need to be
 * hidden or shown depending on the state of the object they
 * are controlling.
 */
public class TaggedComponentManager {

	/** A set of all components that have tags. */
	private Set<Component> allComponents = new HashSet<Component>();
	
	/** A map between tags and sets of components. */
	private Map<String, Set<Component>> taggedComponents = new HashMap<String, Set<Component>>();

	/**
	 * Adds components to the set for the given tag.
	 * 
	 * @param tag the tag
	 * @param componentsToTag the components to add to the tag
	 */
	public void tagComponents(String tag, Component... componentsToTag) {
		Set<Component> components = taggedComponents.get(tag);
		if (components == null) {
			components = new HashSet<Component>();
			taggedComponents.put(tag, components);
		}
		components.addAll(Arrays.asList(componentsToTag));
		allComponents.addAll(Arrays.asList(componentsToTag));
	}
	
	/**
	 * Gets the set of components corresponding to a tag.
	 * 
	 * @param tag the tag
	 * @return the set of components for the tag, or an empty collection if there
	 *   are no components for that tag
	 */
	public Collection<Component> getComponentsForTag(String tag) {
		Set<Component> components = taggedComponents.get(tag);
		if (components == null) {
			return Collections.emptyList();
		} else {
			return components;
		}
	}
	
	/**
	 * Shows all components for a tag if a condition is true. Does nothing
	 * if there are no components for the tag or if the condition is false.
	 * 
	 * @param tag the tag
	 * @param condition the condition to test
	 */
	public void show(String tag, boolean condition) {
		if (condition) {
			for (Component c : getComponentsForTag(tag)) {
				c.setVisible(true);
			}
		}
	}
	
	/**
	 * Hides all components for a tag, if a condition is true. Does nothing
	 * if there are no components for the tag or the condition is false.
	 * 
	 * @param tag the tag
	 * @param condition the condition to test
	 */
	public void hide(String tag, boolean condition) {
		if (condition) {
			for (Component c : getComponentsForTag(tag)) {
				c.setVisible(false);
			}
		}
	}
	
	/**
	 *  Disables components for a tag if condition is true
	 * @param tag tag component
	 * @param condition test condition
	 */
	public void disable(String tag, boolean condition) {
		if (condition) {
			for (Component c : getComponentsForTag(tag)) {
				c.setEnabled(false);
			}
		} 
	}
	
	/**
	 * Enables components for a tag if condition is true
	 * @param tag tag component
	 * @param condition test condition
	 */
	public void enable(String tag, boolean condition) {
		if (condition) {
			for (Component c : getComponentsForTag(tag)) {
				c.setEnabled(true);
			}
		} 
	}
	
	
	/**
	 * Sets all components to visible.
	 */
	public void showAll() {
		for (Component c : allComponents) {
			c.setVisible(true);
		}
	}

	/**
	 * Hides a component if a set of others is all hidden.
	 * 
	 * @param component the component to hide if the others are all hidden
	 * @param others an array of components to test
	 */
	public void hideIfOthersHidden(Component component, Component... others) {
		boolean shouldShow = false;
		
		for (Component other : others) {
			if (other.isVisible()) {
				shouldShow = true;
				break;
			}
		}
		
		// If we get here, all others are hidden
		component.setVisible(shouldShow);
	}
	
}
