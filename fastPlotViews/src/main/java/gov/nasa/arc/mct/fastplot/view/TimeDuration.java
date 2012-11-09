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
package gov.nasa.arc.mct.fastplot.view;

public class TimeDuration {
	private int years;
	private int days;
	private int hours;
	private int minutes;
	private int seconds;

	public TimeDuration(long ms) {
		ms /= 1000;
		seconds = (int) (ms % 60);
		ms /= 60;
		minutes = (int) (ms % 60);
		ms /= 60;
		hours   = (int) (ms % 24);
		ms /= 24;
		days    = (int) (ms % 365); // Note: May give unexpected values during leap year
		ms /= 365;
		years   = (int) (ms);
	}
	
	public TimeDuration(int y, int d, int h, int m, int s) {
	// Check for invalid values
		years = y;
		days = d;
		hours = h;
		minutes = m;
		seconds = s;
	}
	
	public int getYears() {
		return years;
	}
	public void setYears(int years) {
		// Check for invalid values
		this.years = years;
	}

	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		// Check for invalid values
		this.days = days;
	}
	public int getHours() {
		return hours;
	}
	public void setHours(int hours) {
		this.hours = hours;
	}
	public int getMinutes() {
		return minutes;
	}
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	public int getSeconds() {
		return seconds;
	}
	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}
}
