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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Manages a set of abbreviations for a label in a table. The abbreviations
 * are stored in an ordered map, sorted by their length, longest to shortest.
 * That way the most specific abbreviations are applies first.
 */
public class LabelAbbreviations {

	
	private Map<String, String> abbreviations = new HashMap<String, String>();

	/** Clears abbrevations. */
	public void clear() {
		abbreviations.clear();
	}
	
	/**
	 * Adds a new abbreviation to the set.
	 * 
	 * @param phrase the phrase for which there is an abbreviation
	 * @param abbreviation the abbreviation for the phrase
	 */
	public void addAbbreviation(String phrase, String abbreviation) {
		abbreviations.put(phrase, abbreviation);
	}
	
	/**
	 * Gets the abbreviation for a phrase. Returns the original phrase
	 * if there is no abbreviation for the phrase.
	 * 
	 * @param phrase the phrase for which to get the abbreviation
	 * @return the abbreviation, or the original phrase if there is no abbreviation
	 */
	public String getAbbreviation(String phrase) {
		String abbreviation = abbreviations.get(phrase);
		if (abbreviation == null) {
			return phrase;
		} else {
			return abbreviation;
		}
	}
	
	/**
	 * Applies all the abbreviations to a string.
	 * 
	 * @param s the string to abbreviate
	 * @return the result after substituting the abbreviations
	 */
	public String applyAbbreviations(String s) {
		String result = s;
		
		for (Entry<String, String> entry : abbreviations.entrySet()) {
			result = result.replace(entry.getKey(), entry.getValue());
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Entry<String,String> entry : abbreviations.entrySet()) {
			if (result.length() > 0) {
				result.append('\\');
			}
			result.append(entry.getKey());
			result.append('=');
			result.append(entry.getValue());
		}
		return result.toString();
	}
	
	/** 
	 * Add an abbreviation, givent its string representation.
	 * @param s the string
	 */
	public void addAbbreviationsFromString(String s) {
		String[] abbreviationStrings = s.split("\\\\");
		for (String abbrevString : abbreviationStrings) {
			String[] valueStrings = abbrevString.split("=");
			if (valueStrings.length == 2) {
				addAbbreviation(valueStrings[0], valueStrings[1]);
			}
		}
	}
	
}
