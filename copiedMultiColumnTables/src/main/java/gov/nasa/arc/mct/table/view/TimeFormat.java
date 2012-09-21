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

import gov.nasa.arc.mct.components.TimeConversion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Coordinates formatting between simple date formats and 
 * time conversions to create displayable times.
 * 
 * @author vwoeltje
 *
 */
public class TimeFormat {
    
    /**
     * Apply a time conversion, as well as a date format. This utility 
     * will determine whether or not to display the result as a clock  
     * or a timer based on the properties of the supplied time conversion. 
     * (Clock assumes date relative to some epoch and adjusts; timer  
     * is purely relative, and can display negative values et cetera)
     * 
     * Note that formatting for Timer attempts to mimic the supplied 
     * SimpleDateFormat by replacing instances of HH, ss, mm, DDD, 
     * and yyyy. Other elements are not currently supported.
     * 
     * @param sdf the date formatter to apply
     * @param tc the time conversion to apply
     * @param time the time to be converted
     * @return a string representing the time in the chosen format
     */
    public static String applySimpleDateFormat(SimpleDateFormat sdf,
                                               TimeConversion   tc,
                                               String           time) {
        
        if (tc == null) {
            long timeMillis = (long) Double.parseDouble(time.toString());
            return sdf.format(new Date(timeMillis));
        }
        
        long timeMillis = tc.convertToUNIXTime(time);
        if (!tc.isTimer()) { // We're a clock - SimpleDateFormat is fine 
            return sdf.format(new Date(timeMillis));
        } else { // We need to represent countdown data
                       
            long seconds = Math.abs(timeMillis) / 1000;
            long minutes = seconds              / 60;
            long hours   = minutes              / 60;
            long days    = hours                / 24;
            long years   = days                 / 365;
            
            seconds %= 60;
            minutes %= 60;
            hours   %= 24;
            days    %= 365;
            
            /* Note: This is not very versatile! */
            String pattern = sdf.toLocalizedPattern();
            pattern = pattern.replaceAll("HH",   String.format("%02d", hours));
            pattern = pattern.replaceAll("mm",   String.format("%02d", minutes));
            pattern = pattern.replaceAll("ss",   String.format("%02d", seconds));
            pattern = pattern.replaceAll("DDD",  String.format("%03d", days));
            pattern = pattern.replaceAll("yyyy", String.format("%04d", years));
            
            return ((timeMillis < 0) ? "-" : "+") + pattern;
        }
        
    }
    

    /** 
	 * Defines the supported date types, with their labels and DateFormat formatter object.
	 */
  enum DateFormatItem {None(new SimpleDateFormat(),"None"), 
	   YYYYDDD_HHMMSS(new SimpleDateFormat("yyyy/DDD/HH:mm:ss"),"YYYY/DDD/HH:MM:SS"), 
	   DDD_HHMMSS(new SimpleDateFormat("DDD/HH:mm:ss"), "DDD/HH:MM:SS"), 
	   DDD_HHMM(new SimpleDateFormat("DDD/HH:mm"),  "DDD/HH:MM"), 
	   HHMMSS(new SimpleDateFormat("HH:mm:ss"), "HH:MM:SS"),
       HHMM( new SimpleDateFormat("HH:mm"), "HH:MM");

	private SimpleDateFormat dateFormatter;
	private String guiLabel;

	DateFormatItem(SimpleDateFormat fmt, String label) {
     this.dateFormatter = fmt;
     this.dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
     this.guiLabel = label;          
	}
	
	public SimpleDateFormat getFormatter() {
		return dateFormatter;			
	}
	
	public String getGuiLabel() {
		return guiLabel;
	}
	
  }
}
