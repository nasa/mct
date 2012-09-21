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

import static org.testng.Assert.*;

import javax.swing.JLabel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TaggedComponentManagerTest {
	
	private static final String TAG1 = "Tag1";
	private static final String TAG2 = "Tag2";
	
	private JLabel component1;
	private JLabel component2;

	private TaggedComponentManager manager;

	@BeforeMethod
	public void init() {
		manager = new TaggedComponentManager();
		component1 = new JLabel("hello");
		component1.setVisible(true);
		component2 = new JLabel("goodbye");
		component2.setVisible(true);
	}
	
	@Test
	public void testInitialization() {
		assertTrue(manager.getComponentsForTag(TAG1).isEmpty());
		assertTrue(manager.getComponentsForTag(TAG2).isEmpty());
	}
	
	@Test
	public void testAddTaggedComponents() {
		manager.tagComponents(TAG1, component1);
		assertEquals(manager.getComponentsForTag(TAG1).size(), 1);
		assertTrue(manager.getComponentsForTag(TAG1).contains(component1));
		assertFalse(manager.getComponentsForTag(TAG1).contains(component2));
		
		manager.tagComponents(TAG1, component2);
		assertEquals(manager.getComponentsForTag(TAG1).size(), 2);
		assertTrue(manager.getComponentsForTag(TAG1).contains(component1));
		assertTrue(manager.getComponentsForTag(TAG1).contains(component2));
	}
	
	@Test
	public void testSetVisible() {
		manager.tagComponents(TAG1, component1);
		manager.tagComponents(TAG2, component2);
		assertTrue(component1.isVisible());
		assertTrue(component2.isVisible());
		
		manager.hide(TAG1, true);
		assertFalse(component1.isVisible());
		assertTrue(component2.isVisible());
		
		manager.hide(TAG2, true);
		assertFalse(component1.isVisible());
		assertFalse(component2.isVisible());
		
		manager.show(TAG1, true);
		assertTrue(component1.isVisible());
		assertFalse(component2.isVisible());
	}
	
	@Test
	public void testHideIfOthersHidden() {
		manager.hideIfOthersHidden(component1, component2);
		assertTrue(component1.isVisible());
		
		component2.setVisible(false);
		manager.hideIfOthersHidden(component1, component2);
		assertFalse(component1.isVisible());
	}
	
}
