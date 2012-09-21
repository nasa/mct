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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StringUtilsTest {
	
	/** Word delimiters for the split tests. */
	private static final String DELIMITERS = "[ _]*";
	
	@Test(dataProvider="splitTests")
	public void testSplit(String s, String delimiterPattern, String[] words) {
		
	}
	
	@DataProvider(name="splitTests")
	public Object[][] getSplitTests() {
		return new Object[][] {
				new Object[] { "", "", new String[0] },
				new Object[] { "", DELIMITERS, new String[0] },
				new Object[] { "oneword", DELIMITERS, new String[] {"oneword"} },
				new Object[] { "a b_c", DELIMITERS, new String[] {"a", "b", "c"} },
		};
	}

	@Test(dataProvider="joinTests")
	public void testJoin(String[] parts, String separator, String result) {
		assertEquals(StringUtils.join(Arrays.asList(parts), separator), result);
	}
	
	@Test(dataProvider="joinTests")
	public void testJoinArray(String[] parts, String separator, String result) {
		assertEquals(StringUtils.join(parts, separator), result);
	}

	@DataProvider(name="joinTests")
	public Object[][] getJoinTests() {
		return new Object[][] {
				// No parts to join
				new Object[] { new String[0], "-", "" },

				// A single part to join
				new Object[] { new String[] { "hello" }, "-", "hello" },

				// 2 parts to join
				new Object[] { new String[] { "a", "b" }, "-", "a-b", },

				// 3 parts to join
				new Object[] { new String[] { "a", "b", "c" }, "-", "a-b-c", }
		};
	}

}
