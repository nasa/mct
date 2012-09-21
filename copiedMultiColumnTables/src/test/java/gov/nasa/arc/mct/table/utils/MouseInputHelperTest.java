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
package gov.nasa.arc.mct.table.utils;

import static java.awt.Event.ALT_MASK;
import static java.awt.Event.CTRL_MASK;
import static java.awt.Event.META_MASK;
import static java.awt.Event.SHIFT_MASK;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON2;
import static java.awt.event.MouseEvent.BUTTON3;
import static org.testng.Assert.assertEquals;

import java.awt.Component;
import java.awt.event.MouseEvent;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MouseInputHelperTest {

	private @Mock Component component;
	private MyInputHelper helper;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		helper = new MyInputHelper();
	}
	
	// Test the clicks that we handle, with various modifiers, to make
	// sure the right handler is called.
	@Test(dataProvider="mouseClickTests")
	public void testMouseInput(String desiredResult, int button, int clickCount, int modifiers) {
		MouseEvent e = createMouseEvent(button, clickCount, modifiers);		
		helper.mouseClicked(e);
		assertEquals(helper.getResult(), desiredResult);
	}

	private MouseEvent createMouseEvent(int button, int clickCount, int modifiers) {
		return new MouseEvent(
				component, // source
				1, // event ID
				System.currentTimeMillis(), // when
				modifiers,
				0, 0, 0, 0, // x, y, xAbs, yAbs
				clickCount,
				(button==MouseEvent.BUTTON3 && modifiers==0), // popupTrigger
				button);
	}
	
	@DataProvider(name="mouseClickTests")
	public Object[][] getMouseClickTests() {
		return new Object[][] {
				// Simple clicks with no modifiers.
				new Object[] { "mouseLeftClicked", BUTTON1, 1, 0 },
				new Object[] { "mouseRightClicked", BUTTON3, 1, 0 },
				new Object[] { "mouseLeftDoubleClicked", BUTTON1, 2, 0 },
				
				// Left single-clicks with shift or meta.
				new Object[] { "mouseLeftClickedWithShift", BUTTON1, 1, SHIFT_MASK },
				new Object[] { "mouseLeftClickedWithMeta", BUTTON1, 1, META_MASK },
				
				// Left single-click with other modifiers should do nothing.
				new Object[] { "no result", BUTTON1, 1, CTRL_MASK },
				new Object[] { "no result", BUTTON1, 1, ALT_MASK },
				
				// Left double-click should do nothing if any modifiers are present.
				new Object[] { "no result", BUTTON1, 2, ALT_MASK },
				new Object[] { "no result", BUTTON1, 2, CTRL_MASK },
				new Object[] { "no result", BUTTON1, 2, META_MASK },
				new Object[] { "no result", BUTTON1, 2, SHIFT_MASK },
				
				// Right single-click should do nothing if any modifiers are present.
				new Object[] { "no result", BUTTON3, 1, ALT_MASK },
				new Object[] { "no result", BUTTON3, 1, CTRL_MASK },
				new Object[] { "no result", BUTTON3, 1, META_MASK },
				new Object[] { "no result", BUTTON3, 1, SHIFT_MASK },
		};
	}
	
	// Tests of mouse clicks that should be ignored, no matter what
	// the modifier state.
	@Test(dataProvider="ignoredClickTests")
	public void testIgnoredClicks(int button, int clickCount) {
		MouseEvent e;
		
		e = createMouseEvent(button, clickCount, 0);
		helper.mouseClicked(e);
		assertEquals(helper.getResult(), "no result");
		
		e = createMouseEvent(button, clickCount, ALT_MASK);
		helper.mouseClicked(e);
		assertEquals(helper.getResult(), "no result");
		
		e = createMouseEvent(button, clickCount, CTRL_MASK);
		helper.mouseClicked(e);
		assertEquals(helper.getResult(), "no result");
		
		e = createMouseEvent(button, clickCount, META_MASK);
		helper.mouseClicked(e);
		assertEquals(helper.getResult(), "no result");
		
		e = createMouseEvent(button, clickCount, SHIFT_MASK);
		helper.mouseClicked(e);
		assertEquals(helper.getResult(), "no result");
	}
	
	@DataProvider(name="ignoredClickTests")
	public Object[][] getIgnoredClickTests() {
		return new Object[][] {
				// Middle button should always be ignored.
				new Object[] { BUTTON2, 1 },
				new Object[] { BUTTON2, 2 },
				
				// Right double-click should be ignored.
				new Object[] { BUTTON3, 2 },
		};
	}
	
	private static class MyInputHelper extends MouseInputHelper {
		
		private String result = "no result";
		
		public String getResult() {
			return result;
		}
		
		public void setResult(String newResult) {
			result = newResult;
		}

		@Override
		public void mouseLeftClicked(MouseEvent e) {
			setResult("mouseLeftClicked");
		}

		@Override
		public void mouseLeftDoubleClicked(MouseEvent e) {
			setResult("mouseLeftDoubleClicked");
		}

		@Override
		public void mouseRightClicked(MouseEvent e) {
			setResult("mouseRightClicked");
		}

		@Override
		public void mouseLeftClickedWithMeta(MouseEvent e) {
			setResult("mouseLeftClickedWithMeta");
		}

		@Override
		public void mouseLeftClickedWithShift(MouseEvent e) {
			setResult("mouseLeftClickedWithShift");
		}
		
	}
	
}
