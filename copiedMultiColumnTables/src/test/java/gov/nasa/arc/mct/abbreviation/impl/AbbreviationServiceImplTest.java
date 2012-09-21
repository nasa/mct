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

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import gov.nasa.arc.mct.abbreviation.Abbreviations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.service.component.ComponentContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AbbreviationServiceImplTest {

	@Mock private ComponentContext context;

	AbbreviationServiceImpl abbrev;
	Dictionary<String, String> properties;
	
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		abbrev = new AbbreviationServiceImpl();
		properties = new Hashtable<String, String>();
		properties.put("component.name", "AbbreviationService");
		
		when(context.getProperties()).thenReturn(properties);
	}
	
	@Test
	public void testActivateGoodFile() {
		properties.put("abbreviations-file", "/default-abbreviations.properties");
		abbrev.activate(context);

		Abbreviations abbreviations = abbrev.getAbbreviations("Fiber Optic MDM System");
		assertEquals(abbreviations.getPhrases().size(), 3);
		assertEquals(abbreviations.getPhrases().get(0), "Fiber Optic");
		assertEquals(abbreviations.getPhrases().get(1), "MDM");
		assertEquals(abbreviations.getPhrases().get(2), "System");
	}
	
	@Test
	public void testActivateNoFileProperty() {
		abbrev.activate(context);

		Abbreviations abbreviations = abbrev.getAbbreviations("Fiber Optic MDM System");
		assertEquals(abbreviations.getPhrases().size(), 4);
		assertEquals(abbreviations.getPhrases().get(0), "Fiber");
		assertEquals(abbreviations.getPhrases().get(1), "Optic");
		assertEquals(abbreviations.getPhrases().get(2), "MDM");
		assertEquals(abbreviations.getPhrases().get(3), "System");
	}
	
	@Test
	public void testActivateNonexistentAbbreviationsFile() {
		properties.put("abbreviations-file", "/file-does-not-exist.properties");
		abbrev.activate(context);

		Abbreviations abbreviations = abbrev.getAbbreviations("Fiber Optic MDM System");
		assertEquals(abbreviations.getPhrases().size(), 4);
		assertEquals(abbreviations.getPhrases().get(0), "Fiber");
		assertEquals(abbreviations.getPhrases().get(1), "Optic");
		assertEquals(abbreviations.getPhrases().get(2), "MDM");
		assertEquals(abbreviations.getPhrases().get(3), "System");
	}
	
	@Test(dataProvider="findFileTests")
	public void testFindFile(String path, String fileProperty) throws IOException {
		InputStream in;
		Properties p;
		
		p = new Properties();
		in = abbrev.findFile(path);
		assertNotNull(in);
		p.load(in);
		assertEquals(p.getProperty("file"), fileProperty);
	}
	
	@DataProvider(name = "findFileTests")
	public Object[][] getFindFileTests() {
		return new Object[][] {
				// A file path
				{ "src/test/data/abbreviations.properties", "in file system" },
				
				// A resource in the bundle using an absolute name
				{ "/test-abbreviations.properties", "root of bundle" },
				
				// A resource in the bundle using a relative name
				{ "package-abbreviations.properties", "in bundle package" },
		};
	}
	
}
