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
import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class DefaultCursorSetterTest {

	@Mock private Cursor currentCursor;
	@Mock private Component component;
	@Mock private MouseEvent e;
	
	private DefaultCursorSetter cursorSetter;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		cursorSetter = new DefaultCursorSetter(component);
	}
	
	@Test
	public void testMouseEnterLeave() {
		Cursor defaultCursor = Cursor.getDefaultCursor();
		
		when(component.getCursor()).thenReturn(currentCursor);
		cursorSetter.mouseEntered(e);
		verify(component).setCursor(defaultCursor);
		
		cursorSetter.mouseExited(e);
		verify(component).setCursor(currentCursor);
	}

}
