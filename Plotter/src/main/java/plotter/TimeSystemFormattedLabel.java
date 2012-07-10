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

import java.text.MessageFormat;

import javax.swing.JLabel;

/**
 * Time system formatted label names.
 */
public class TimeSystemFormattedLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	/** Displays the time system formatted label name. Defaults to GMT. */
	private String timeSystemLabelName = Axis.DEFAULT_TIME_SYSTEM_NAME;
	
	/** 
	 * Formats the label for display as HTML content.
	 */
	private MessageFormat format = new MessageFormat("");
	
	/**
	 * Sets the time system formatted label name.
	 * @param labelName for time system formatted label.
	 */
	public void setTimeSystemAxisLabelName(String labelName) {
		setText(labelName);
		this.timeSystemLabelName = labelName;
	}
	
	/**
	 * Returns the label name for this particular time system format.
	 * @return timeSystemLabelName - time system formatted label name.
	 */
	public String getTimeSystemAxisLabelName() {
		return this.timeSystemLabelName;
	}
	
	/**
	 * Returns the message text format used to display the label.
	 * @return the format used to display the label.
	 */
	public MessageFormat getFormat() {
		return format;
	}


	/**
	 * Sets the format used to display the label.
	 * @param format used to set the label.
	 */
	public void setFormat(MessageFormat format) {
		this.format = format;
	}
}