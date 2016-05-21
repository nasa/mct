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
/**
 * DataValidation.java Aug 18, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.util;

/**
 * Data validation for length input string.
 *
 */
public final class DataValidation {

    /**
     * Validates length for input string based on the minimum and maximum
     * values.
     * 
     * @param input
     *            the input String
     * @param minimum
     *            minimum length
     * @param maximum
     *            maximum length
     * @return Returns true, if minimum less than or equal AND length less than or equal maximum; otherwise returns
     *         false.
     */
    public static boolean validateLength(String input, int minimum, int maximum) {
        return input.length() >= minimum && input.length() <= maximum;
    }
}
