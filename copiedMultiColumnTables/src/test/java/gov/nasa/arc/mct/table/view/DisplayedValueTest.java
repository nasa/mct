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
package gov.nasa.arc.mct.table.view;

import java.awt.Color;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class DisplayedValueTest {

	@Test
	public void testInstantiation() {
		DisplayedValue a = new DisplayedValue();
		a.setValue("");
		a.setValueColor(Color.BLACK);
		
		assertEquals(a.getValue(), "");
		assertEquals(a.getColor(), Color.BLACK);
		assertEquals(a.toString(), a.getValue());
		
		DisplayedValue b = new DisplayedValue();
		b.setValue("hello, world");
		b.setValueColor(Color.RED);

		assertEquals(b.getValue(), "hello, world");
		assertEquals(b.getColor(), Color.RED);
		assertEquals(b.toString(), b.getValue());
	}
	
	@Test
	public void testSetLabel() {
		DisplayedValue value = new DisplayedValue();
		value.setValue("abc");
		value.setValueColor(Color.GREEN);

		assertEquals(value.toString(), "abc");
		
		value.setLabel("def");
		assertEquals(value.toString(), "abc def");
	}
	
}
