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

import gov.nasa.arc.mct.table.view.AbbreviationSettings;
import gov.nasa.arc.mct.table.view.LabelAbbreviations;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LabelAbbreviationsTest {

	@Test
	public void getAbbreviation() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		AbbreviationsImpl availableAbbreviations = new AbbreviationsImpl("value");
		availableAbbreviations.addPhrase("Amps", Collections.singletonList("A"));
		availableAbbreviations.addPhrase("BCA1", Collections.<String>emptyList());
		availableAbbreviations.addPhrase("Ch1", Collections.<String>emptyList());
		availableAbbreviations.addPhrase("Serial", Collections.<String>emptyList());
		AbbreviationSettings aSettings = new AbbreviationSettings("fullLabel", availableAbbreviations, new LabelAbbreviations());
		String abbreviatedLabel = aSettings.getAbbreviatedLabel();
		Assert.assertEquals(abbreviatedLabel, "Amps BCA1 Ch1 Serial");
		LabelAbbreviations available2 = aSettings.getAbbreviations();
		Assert.assertEquals(available2.getAbbreviation("BCA1"), "BCA1");
		Assert.assertEquals(available2.getAbbreviation("Amps"), "Amps");

		// Change the state of the control panel via currentAbbreviations
		LabelAbbreviations currentAbbreviations = new LabelAbbreviations();
		currentAbbreviations.addAbbreviation("Amps", "A | a | Amp");
		currentAbbreviations.addAbbreviation("BCA1", "B | bca1");
		currentAbbreviations.addAbbreviation("CAT", "C");
		currentAbbreviations.addAbbreviation("DOG", "D");
		currentAbbreviations.addAbbreviation("Ace", "ace");
		currentAbbreviations.addAbbreviation("Abb", "a");
		currentAbbreviations.addAbbreviation("Rabbit", "R");

		AbbreviationSettings a2Settings = new AbbreviationSettings("fullLabel", availableAbbreviations, currentAbbreviations);
		LabelAbbreviations available2afterSelect = a2Settings.getAbbreviations();
		Assert.assertEquals(available2afterSelect.getAbbreviation("BCA1"), "B | bca1");
		Assert.assertEquals(available2afterSelect.getAbbreviation("Amps"), "A | a | Amp");
		Map<String, String> map = getAbbreviations(currentAbbreviations);
		Assert.assertEquals(map.size(), 7);

	}

	private Map<String, String> getAbbreviations(
			LabelAbbreviations currentAbbreviations) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException  {
		Field f = currentAbbreviations.getClass().getDeclaredField("abbreviations"); //NoSuchFieldException
		f.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, String> map = (HashMap<String,String>) f.get(currentAbbreviations); //IllegalAccessException
		return map;
	}
}
