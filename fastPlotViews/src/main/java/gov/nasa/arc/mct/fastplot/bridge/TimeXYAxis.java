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
package gov.nasa.arc.mct.fastplot.bridge;

import plotter.Axis;
import plotter.TimeTickMarkCalculator;
import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;

@SuppressWarnings("serial")
public class TimeXYAxis extends LinearXYAxis {
	private long startLong;
	private long endLong;
	
	/** Displays the time system axis label name. Defaults to GMT. */
    private String timeSystemLabelName = Axis.DEFAULT_TIME_SYSTEM_NAME;

	public TimeXYAxis(XYDimension d) {
		super(d);
		setTickMarkCalculator(new TimeTickMarkCalculator());
	}

	@Override
    public String getTimeSystemAxisLabelName() {
		return timeSystemLabelName;
    }

    @Override
    public void setTimeSystemAxisLabelName(String labelName) {
    	super.setTimeSystemAxisLabelName(labelName);
    	timeSystemLabelName = labelName;
    }
	
	public long getStartAsLong() {
		return startLong;
	}


	public long getEndAsLong() {
		return endLong;
	}


	@Override
	public void setStart(double min) {
		super.setStart(min);
		startLong = (long) min;
	}


	@Override
	public void setEnd(double max) {
		super.setEnd(max);
		endLong = (long) max;
	}


	public void setStart(long min) {
		super.setStart(min);
		startLong = min;
	}


	public void setEnd(long max) {
		super.setEnd(max);
		endLong = max;
	}


	@Override
	public void shift(double offset) {
		super.shift(offset);
		long off = (long) offset;
		startLong += off;
		endLong += off;
	}
}
