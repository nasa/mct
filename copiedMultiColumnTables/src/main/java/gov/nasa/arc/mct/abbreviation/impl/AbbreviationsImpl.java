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

import gov.nasa.arc.mct.abbreviation.Abbreviations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements a partition of a string into phrases, each of which has
 * one or more alternative abbreviations. The original text of the
 * section is always the first of the alternatives. A phrase in the
 * original string is a sequence of one or more words, separated by
 * whitespace.
 */
public class AbbreviationsImpl implements Abbreviations {
	
	private String value;
	private List<String> phrases = new ArrayList<String>();
	private Map<String, List<String>> abbreviations = new HashMap<String, List<String>>();

	/**
	 * Create an abbreviation object.
	 * @param value original text
	 */
	protected AbbreviationsImpl(String value) {
		this.value = value;
	}
	
	@Override
	public List<String> getAbbreviations(String phrase) {
		return abbreviations.get(phrase);
	}

	@Override
	public List<String> getPhrases() {
		return phrases;
	}

	@Override
	public String getValue() {
		return value;
	}
	
	/**
	 * Add a set of abbreviations to a phrase.
	 * @param phrase the phrase to be abbreviated
	 * @param alternatives the phrase's abbreviations
	 */
	protected void addPhrase(String phrase, List<String> alternatives) {
		phrases.add(phrase);
		abbreviations.put(phrase, alternatives);
	}

}
