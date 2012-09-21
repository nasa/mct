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
package gov.nasa.arc.mct.abbreviation;

import java.util.List;

/**
 * Defines a data structure that represents the set of abbreviations available
 * for words and phrases in a string.
 */
public interface Abbreviations {

	/**
	 * Gets the original string for which the available abbreviations have
	 * been calculated.
	 *  
	 * @return the original string
	 */
	String getValue();
	
	/**
	 * Gets the phrases into which the original string has been divided as
	 * possible abbreviations were found. The phrases, in order, comprise
	 * all words of the original string.
	 * 
	 * @return a list of phrases that can be abbreviated
	 */
	List<String> getPhrases();
	
	/**
	 * Gets the available abbreviations for a phrase. The list is always
	 * nonempty, since the first element is the phrase unchanged.
	 * 
	 * @param phrase the phrase to abbreviate, which may be a single word
	 * @return a list of possible abbreviations for the phrase
	 */
	List<String> getAbbreviations(String phrase);
	
}
