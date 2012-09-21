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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a manager of an abbreviations list.
 */
public class AbbreviationsManager {
	
	/** A regular expression used to separate alternative abbreviations. (\s == any whitespace) */
	private static final Pattern ABBREVIATION_SEPARATOR = Pattern.compile("\\s*\\|\\s*");
	
	/** A regular expression used to separate words. */
	private static final Pattern WORD_SEPARATOR = Pattern.compile("\\s+");
	
	private Map<String, List<String>> abbreviations = new HashMap<String, List<String>>();

	/**
	 * Creates a new abbreviations manager configured with a set of abbreviation
	 * properties. Abbreviation properties are of the form:
	 * <pre>
	 * phrase = alt1 | alt2 | ...
	 * </pre>
	 * Whitespace around the "=" and "|" separators is removed. The phrase is
	 * converted to lower case, but the alternatives are used verbatim.
	 * 
	 * @param abbreviationProperties the abbreviation properties
	 */
	public AbbreviationsManager(Properties abbreviationProperties) {
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>) abbreviationProperties.propertyNames();
		while (e.hasMoreElements()) {
			String phrase = e.nextElement();
			String lcPhrase = phrase.toLowerCase();
			String[] alternatives = ABBREVIATION_SEPARATOR.split(abbreviationProperties.getProperty(phrase).trim());

			List<String> abbreviationsForPhrase = new ArrayList<String>(Arrays.asList(alternatives));
			Collections.sort(abbreviationsForPhrase, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
						return o1.length() - o2.length();
				}
			});
			abbreviations.put(lcPhrase, abbreviationsForPhrase);
		}
	}
	
	/**
	 * Gets the alternative abbreviations for a phrase. The original phrase is always the
	 * the first alternative returned. If no abbreviations are found for the phrase, returns
	 * a list with one element, the original phrase. The phrase is converted to lower case
	 * before looking up its alternatives.
	 * 
	 * @param phrase the phrase to abbreviate
	 * @return a list of alternative abbreviations, with the original phrase as the first element
	 */
	public List<String> getAlternatives(String phrase) {
		List<String> result = new ArrayList<String>();
		result.add(phrase);
		
		List<String> alternatives = abbreviations.get(phrase.toLowerCase());
		if (alternatives != null) {
			result.addAll(alternatives);
		}
		
		return result;
	}

	/**
	 * Finds the phrases within a string that can be abbreviated, and returns
	 * a structure with those phrases and the alternatives for each phrase.
	 * A phrase is a sequence of one or more words in the original string, where
	 * words are delimited by whitespace. At each point in the original string,
	 * the longest phrase for which there are abbreviations is found.
	 * 
	 * @param s the string to find abbreviations for
	 * @return a structure describing the available abbreviations
	 */
	public Abbreviations getAbbreviations(String s) {
		AbbreviationsImpl abbrev = new AbbreviationsImpl(s);
		List<String> phrases = getPhrasesWithAbbreviations(s);
		for (String phrase : phrases) {
			abbrev.addPhrase(phrase, getAlternatives(phrase));
		}
		
		return abbrev;
	}

	/**
	 * Constructs a partition of a string into phrases, along word boundaries,
	 * where each phrase has one or more alternative abbreviations, and each
	 * phrase is the longest match against the abbreviations at that position
	 * in the original string.
	 * 
	 * @param s the original string to partition into phrases
	 * @return a list of phrases
	 */
	private List<String> getPhrasesWithAbbreviations(String s) {
		int phraseStart = 0;
		List<String> phrasesWithAbbreviations = new ArrayList<String>();
		Matcher wordBoundary = WORD_SEPARATOR.matcher(s);
		
		while (phraseStart < s.length()) {
			int phraseLength = getLongestPhraseLength(s.substring(phraseStart));
			phrasesWithAbbreviations.add(s.substring(phraseStart, phraseStart + phraseLength));
			if (wordBoundary.find(phraseStart + phraseLength)) {
				phraseStart = wordBoundary.end();
			} else {
				phraseStart = s.length();
			}
		}
		
		return phrasesWithAbbreviations;
	}
	
	/**
	 * Finds the longest phrase within a string that has abbreviations. The first word
	 * is always a possibility, even if no alternatives exist to that word.
	 * 
	 * @param s the string for which to find the longest phrase with alternatives
	 * @return the length of the longest phrase with alternative abbreviations
	 */
	private int getLongestPhraseLength(String s) {
		// If the entire string matches, then it is obviously the longest matching phrase.
		if (abbreviations.containsKey(s.toLowerCase())) {
			return s.length();
		}
		
		Matcher wordBoundary = WORD_SEPARATOR.matcher(s);
		if (!wordBoundary.find()) {
			// No word boundaries found. Entire string is only possible phrase.
			return s.length();
		}
		
		// First word is always an abbreviation candidate, perhaps with no
		// alternatives but itself.
		int longestMatchLength = wordBoundary.start();
		
		while (wordBoundary.find()) {
			if (abbreviations.containsKey(s.substring(0, wordBoundary.start()).toLowerCase())) {
				longestMatchLength = wordBoundary.start();
			}
		}
		
		return longestMatchLength;
	}
		
}
