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

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.GregorianCalendar;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSpanTextField extends JFormattedTextField {
	private static final long serialVersionUID = -4115671788373208673L;
    private final static Logger logger = LoggerFactory.getLogger(TimeSpanTextField.class);

    private static final String DEFAULT_VALUE = "000/00:00:00";
	private static final int NUM_COLUMNS = DEFAULT_VALUE.length() - 3; // Don't need columns for colons
	static final private int DAYS_POSITION = 0;
	static final private int HOURS_POSITION = 4;
	static final private int MINUTES_POSITION = 7;
	static final private int SECONDS_POSITION = 10;
	private static final DecimalFormat dayFormat = new DecimalFormat("000");
	private static final DecimalFormat hhmmssFormat = new DecimalFormat("00");
	private MaskFormatter yearFormatter;
	
	private final YearSpanTextField yearSpanValue;

	public TimeSpanTextField(AbstractFormatter formatter) {
		super(formatter);
		setInputVerifier(new DurationVerifier());
		setColumns(NUM_COLUMNS);
		setValue(DEFAULT_VALUE);
		setHorizontalAlignment(JFormattedTextField.RIGHT);
		yearSpanValue = createYearSpanTextField();
	}
	
	@Override public void setEnabled(boolean state) {
		super.setEnabled(state);
		yearSpanValue.setEnabled(state);
	}

	public int getDayOfYear() {
		String work = (String) getValue();
		return Integer.parseInt(work.substring(0, 3));
	}

	public int getHourOfDay() {
		String work = (String) getValue();
		return Integer.parseInt(work.substring(4, 6));
	}

	public int getMinute() {
		String work = (String) getValue();
		return Integer.parseInt(work.substring(7, 9));
	}

	public int getSecond() {
		String work = (String) getValue();
		return Integer.parseInt(work.substring(10, 12));
	}

	public int getSubYearValue() {
		return getDayOfYear() + getHourOfDay() + getMinute() + getSecond();
	}
	
	public long getDurationInMillis() {
		return (long) PlotConstants.MILLISECONDS_IN_SECOND * getSecond() +
		       (long) PlotConstants.MILLISECONDS_IN_MIN * getMinute() +
		       (long) PlotConstants.MILLISECONDS_IN_HOUR * getHourOfDay() +
		       (long) PlotConstants.MILLISECONDS_IN_DAY * getDayOfYear() +
		       (long) PlotConstants.MILLISECONDS_IN_YEAR * yearSpanValue.getYears();
		       
		       
	}

	public void setTime(TimeDuration duration) {
		StringBuilder builder = new StringBuilder();
		builder.append(dayFormat.format(duration.getDays()) + "/");
		builder.append(hhmmssFormat.format(duration.getHours()) + ":");
		builder.append(hhmmssFormat.format(duration.getMinutes()) + ":");
		builder.append(hhmmssFormat.format(duration.getSeconds()));
		setValue(builder.toString());
		yearSpanValue.setValue(duration.getYears());
	}
	
	
	@Override
	public synchronized void addActionListener(ActionListener l) {
		super.addActionListener(l);
		if (yearSpanValue != null) yearSpanValue.addActionListener(l);
	}

	@Override
	public synchronized void removeActionListener(ActionListener l) {
		super.removeActionListener(l);
		if (yearSpanValue != null) yearSpanValue.removeActionListener(l);
	}
 
	@Override
	public synchronized void addFocusListener(FocusListener l) {
		super.addFocusListener(l);
		if (yearSpanValue != null) yearSpanValue.addFocusListener(l);
	}

	@Override
	public synchronized void removeFocusListener(FocusListener l) {
		super.removeFocusListener(l);
		if (yearSpanValue != null) yearSpanValue.removeFocusListener(l);
	}

	YearSpanTextField createYearSpanTextField() {
		try {
			yearFormatter = new MaskFormatter("#####") {
				/**
				 * 
				 */
				private static final long serialVersionUID = -6395586629439379363L;

				@Override
				public String valueToString(Object value) {
					if (value == null) 
						return "00000";
					return String.format("%05d", Integer.parseInt(value.toString()));
				}
			};
			yearFormatter.setPlaceholderCharacter('0');
		} catch (ParseException e) {
			logger.error("Error in creating a mask formatter", e);
		}
        return new YearSpanTextField(yearFormatter);
	}
	
	public YearSpanTextField getYearSpanValue() {
		return yearSpanValue;
	}

	/**
	 * Used for a field with a Time Duration
	 */
	class DurationVerifier extends InputVerifier {
		
		private void setTimeValue(int dayOfYear, int hourOfDay, int minute, int second) {
			StringBuilder builder = new StringBuilder();
			builder.append(dayFormat.format(dayOfYear) + "/");
			builder.append(hhmmssFormat.format(hourOfDay) + ":");
			builder.append(hhmmssFormat.format(minute) + ":");
			builder.append(hhmmssFormat.format(second));
			setValue(builder.toString());
		}
		
		@Override
		public boolean verify(JComponent component) {
			TimeSpanTextField field = (TimeSpanTextField) component;
			String days = field.getText().substring(DAYS_POSITION, 3);
			String hours = field.getText().substring(HOURS_POSITION, 6);
			String minutes = field.getText().substring(MINUTES_POSITION, 9);
			String seconds = field.getText().substring(SECONDS_POSITION, 12);

			int yearsValue = Integer.parseInt(yearSpanValue.getValue().toString());
			int daysValue = Integer.parseInt(days);
			int hoursValue = Integer.parseInt(hours);
			int minutesValue = Integer.parseInt(minutes);
			int secondsValue = Integer.parseInt(seconds);

			// Check seconds field. Carry over values > 59
			if (secondsValue >= 60) {
				minutesValue += secondsValue/60;
				secondsValue = secondsValue % 60;
			}
			// Check minutes field. Carry over values > 59
			if (minutesValue >= 60) {
				hoursValue += minutesValue/60;
				minutesValue = minutesValue % 60;
			}
			// Check hour of day field. Carry over values > 23
			if (hoursValue >= 24) {
				daysValue += hoursValue/24; 
				hoursValue = hoursValue % 24;
			}
			//Carry over values > 365 to year
			if (daysValue > 365) { 
				yearSpanValue.setText(String.format("%05d", (Integer.parseInt(yearSpanValue.getValue().toString()) + (daysValue / 365))));
				daysValue = daysValue % 365;
			}
			
			setTimeValue(daysValue, hoursValue, minutesValue, 
					secondsValue);
			
			return field.isEditValid();
		}
	}
	
	public static class YearSpanTextField extends JFormattedTextField {

		private static final long serialVersionUID = -2520968397371605584L;
		private static final String DEFAULT_VALUE = "00000";
		private static final int NUM_COLUMNS = DEFAULT_VALUE.length();

		public YearSpanTextField(AbstractFormatter formatter) {
			super(formatter);
			setColumns(NUM_COLUMNS);
			setValue(DEFAULT_VALUE);
			setHorizontalAlignment(JFormattedTextField.RIGHT);
		}
		
		public int getYears() {
			return Integer.parseInt(getText());
		}
		
	}
}