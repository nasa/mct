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
package gov.nasa.arc.mct.abbreviation.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import gov.nasa.arc.mct.abbreviation.Abbreviations;

import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AbbreviationsManagerTest {
	
	private Properties defaultProperties;
	
	@BeforeMethod
	public void init() {
		defaultProperties = new Properties();
		defaultProperties.setProperty("fiber optic", "F/O");
		defaultProperties.setProperty("system", "Sys");
	}
	
	@Test
	public void testGetAlternatives() {
		AbbreviationsManager manager;
		Properties props;
		List<String> alternatives;
		
		props = new Properties();
		manager = new AbbreviationsManager(props);
		alternatives = manager.getAlternatives("volts");
		assertEquals(alternatives.size(), 1);
		assertEquals(alternatives.get(0), "volts");
		
		props = new Properties();
		props.setProperty("Volts", "V"); // Note that lookup should be case insensitive.
		manager = new AbbreviationsManager(props);
		alternatives = manager.getAlternatives("volts");
		assertEquals(alternatives.size(), 2);
		assertEquals(alternatives.get(0), "volts"); // Matches the case of getAbbreviations() argument.
		assertEquals(alternatives.get(1), "V");
		
		props = new Properties();
		props.setProperty("Amperes", "Amps | A | aa | bbbb | a | aaaa ");
		manager = new AbbreviationsManager(props);
		alternatives = manager.getAlternatives("amperes");
		assertEquals(alternatives.size(), 7);
		assertEquals(alternatives.get(0), "amperes"); // Must match in case to getAbbreviations() argument.
		assertEquals(alternatives.get(1), "A");
		assertEquals(alternatives.get(2), "a");
		assertEquals(alternatives.get(3), "aa");
		assertEquals(alternatives.get(4), "Amps"); // same length items are in left to right specified order
		assertEquals(alternatives.get(5), "bbbb");
		assertEquals(alternatives.get(6), "aaaa");
	}
	
	@Test(dataProvider="getAbbreviationsTests")
	public void testGetAbbreviations(String s, String[] expectedPhrases) {
		AbbreviationsManager manager = new AbbreviationsManager(defaultProperties);
		Abbreviations abbrev = manager.getAbbreviations(s);
		
		assertEquals(abbrev.getValue(), s);
		assertEquals(abbrev.getPhrases().size(), expectedPhrases.length);
		for (int i=0; i<abbrev.getPhrases().size(); ++i) {
			String phrase = abbrev.getPhrases().get(i);
			assertEquals(phrase, expectedPhrases[i]);
			
			List<String> alternatives = abbrev.getAbbreviations(phrase);
			List<String> expectedAlternatives = manager.getAlternatives(abbrev.getPhrases().get(i));
			assertTrue(alternatives.size() >= 1);
			assertEquals(alternatives.size(), expectedAlternatives.size());
			assertEquals(alternatives.get(0), abbrev.getPhrases().get(i));
		}
	}
	
	@DataProvider(name="getAbbreviationsTests")
	private Object[][] getGetAbbreviationsTests() {
		return new Object[][] {
				{ "System", new String[] { "System" } }, // One word in abbreviations map
				{ "MDM", new String[] { "MDM" } }, // One word not in abbreviations map
				{ "Fiber Optic", new String[] { "Fiber Optic" } }, // Exact phrase in abbreviations map

				// Some longer tests.
				{ "Junk1 Junk2 Junk3", new String[] { "Junk1", "Junk2", "Junk3" } }, // No matches
				{ "Fiber Optic MDM System", new String[] { "Fiber Optic", "MDM", "System" } },
		};
	}
	
}
