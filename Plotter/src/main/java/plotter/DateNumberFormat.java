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
package plotter;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;

/**
 * Wraps a {@link DateFormat} in a {@link NumberFormat}.
 * @author Adam Crume
 */
public class DateNumberFormat extends NumberFormat {
	private static final long serialVersionUID = 1L;

	private final DateFormat format;

	private final long offset;


	/**
	 * Creates a number format based on <code>format</code>
	 * @param format date format to view as a number format
	 */
	public DateNumberFormat(DateFormat format) {
		this(format, 0);
	}


	/**
	 * Creates a number format based on <code>format</code>
	 * @param format date format to view as a number format
	 */
	public DateNumberFormat(DateFormat format, long offset) {
		this.format = format;
		this.offset = offset;
	}


	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		return format.parse(source, parsePosition).getTime();
	}


	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		toAppendTo.append(format.format(new Date(number + offset)));
		return toAppendTo;
	}


	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		toAppendTo.append(format.format(new Date((long) number + offset)));
		return toAppendTo;
	}


	/**
	 * Returns the base format.
	 * @return the base format
	 */
	public DateFormat getBaseFormat() {
		return format;
	}
}
