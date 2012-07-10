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

import javax.swing.JComponent;

/**
 * An axis is a line on a plot that is associated with a plot dimension.
 * An axis may have tick marks and labels.
 * 
 * Note that the start is usually smaller than the end, but this is not necessarily the case.
 * @author Adam Crume
 */
public abstract class Axis extends JComponent {
	private static final long serialVersionUID = 1L;

	/** Displays the time system axis label name. Defaults to GMT. */
	protected static final String DEFAULT_TIME_SYSTEM_NAME = "GMT";

	/** Start value. */
	private double start;

	/** End value. */
	private double end;

	/** Length of major tick marks. */
	private int majorTickLength = 10;

	/** Length of minor tick marks. */
	private int minorTickLength = 5;

	/** Distance between the axis and text labels. */
	private int textMargin = 15;

	/** Whether or not to display labels. */
	private boolean showLabels = true;


	/**
	 * Returns the start value.
	 * @return the start value
	 */
	public double getStart() {
		return start;
	}


	/**
	 * Sets the start value.
	 * @param start the start value
	 */
	public void setStart(double start) {
		if(start != this.start) {
			this.start = start;
			revalidate();
			repaint();
		}
	}


	/**
	 * Returns the end value.
	 * @return the end value
	 */
	public double getEnd() {
		return end;
	}


	/**
	 * Sets the end value.
	 * @param end the end value
	 */
	public void setEnd(double end) {
		if(end != this.end) {
			this.end = end;
			revalidate();
			repaint();
		}
	}


	/**
	 * Adds <code>offset</code> to both the start and end.  May be more efficient than setting them separately.
	 * @param offset amount to shift the axis
	 */
	public void shift(double offset) {
		if(offset != 0) {
			end += offset;
			start += offset;
			revalidate();
			repaint();
		}
	}


	/**
	 * Returns the length of major tick marks.
	 * @return the length of major tick marks
	 */
	public int getMajorTickLength() {
		return majorTickLength;
	}


	/**
	 * Sets the length of major tick marks.
	 * @param majorTickLength the length of major tick marks
	 */
	public void setMajorTickLength(int majorTickLength) {
		this.majorTickLength = majorTickLength;
	}


	/**
	 * Returns the length of minor tick marks.
	 * @return the length of minor tick marks
	 */
	public int getMinorTickLength() {
		return minorTickLength;
	}


	/**
	 * Sets the length of minor tick marks.
	 * @param minorTickLength the length of minor tick marks
	 */
	public void setMinorTickLength(int minorTickLength) {
		this.minorTickLength = minorTickLength;
	}


	/**
	 * Returns the distance between the axis and text labels.
	 * @return the distance between the axis and text labels
	 */
	public int getTextMargin() {
		return textMargin;
	}


	/**
	 * Sets the distance between the axis and text labels.
	 * @param textMargin the distance between the axis and text labels
	 */
	public void setTextMargin(int textMargin) {
		this.textMargin = textMargin;
	}


	/**
	 * Returns true if this axis displays labels.
	 * @return true if this axis displays labels
	 */
	public boolean isShowLabels() {
		return showLabels;
	}


	/**
	 * Sets whether or not this axis should display labels.
	 * @param showLabels true to display labels
	 */
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}
	
	/**
	 * Returns the time system axis label name.
	 * @return time system axis label name.
	 */
	public abstract String getTimeSystemAxisLabelName();

	/**
	 * Sets the time system axis label name.
	 * @param labelName time system.
	 */
	public abstract void setTimeSystemAxisLabelName(String labelName);
}
