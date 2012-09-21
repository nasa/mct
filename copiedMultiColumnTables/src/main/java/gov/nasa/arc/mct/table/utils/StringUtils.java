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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implements utilities for string manipulation.
 */
public class StringUtils {

	/**
	 * Splits a string into words and returns the result as a list.
	 * Empty words are never returned. This is slightly different
	 * behavior than that of {@link String#split(String)}, which will
	 * return an array of one element, an empty string, if no words
	 * are found.
	 * 
	 * @param s the string to split
	 * @param delimiterPattern the regular expression representing the word delimiters
	 * @return the words found, as a list, or an empty list if no words found
	 */
	public static List<String> split(String s, Pattern delimiterPattern) {
		String[] words = delimiterPattern.split(s);
		
		// Add all nonempty words to the result.
		List<String> result = new ArrayList<String>();
		for (String w : words) {
			if (w.length() > 0) {
				result.add(w);
			}
		}
		
		return result;
	}
	
	/**
	 * Joins a list of words together, separated by a separator string.
	 * 
	 * @param words the list of words to join, which may be empty
	 * @param separator the string to insert between the words
	 * @return the joined string
	 */
	public static String join(List<String> words, String separator) {
		StringBuilder s = new StringBuilder();
		
		for (String w : words) {
			if (s.length() > 0) {
				s.append(separator);
			}
			s.append(w);
		}
		
		return s.toString();
	}

	/**
	 * Joins an array of words together, separated by a separator string.
	 * 
	 * @param words the array of words to join, which may be empty
	 * @param separator the string to insert between the words
	 * @return the joined string
	 */
	public static String join(String[] words, String separator) {
		StringBuilder s = new StringBuilder();
		
		for (String w : words) {
			if (s.length() > 0) {
				s.append(separator);
			}
			s.append(w);
		}
		
		return s.toString();
	}

}
