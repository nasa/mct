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
package gov.nasa.arc.mct.fastplot.utils;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.services.activity.TimeService;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeFormatUtils {
	private final static Logger logger = LoggerFactory.getLogger(TimeFormatUtils.class);

	private static final String ignoreNanoSeconds = "eeee";
	
	public static SimpleDateFormat makeDataFormat(String formatString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(TimeService.DEFAULT_TIME_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		
		if (formatString != null && !formatString.isEmpty()) {
			try {
				if (formatString.endsWith(ignoreNanoSeconds)) {
					formatString = formatString.substring(0, formatString.indexOf(ignoreNanoSeconds));
				}

				dateFormat = new SimpleDateFormat(formatString);
			} catch (IllegalArgumentException e) { 
				logger.error("Unable to format date time format: "+ formatString+ ". Instead using default: "+ TimeService.DEFAULT_TIME_FORMAT);
			}
		}
		return dateFormat;

	}
}
