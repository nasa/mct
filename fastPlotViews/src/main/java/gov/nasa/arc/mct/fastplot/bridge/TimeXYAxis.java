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

import plotter.TimeTickMarkCalculator;
import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;

@SuppressWarnings("serial")
public class TimeXYAxis extends LinearXYAxis implements AbstractAxis {
	private long startLong;
	private long endLong;


	public TimeXYAxis(XYDimension d) {
		super(d);
		setTickMarkCalculator(new TimeTickMarkCalculator());
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

	@Override
	public AxisVisibleOrientation getVisibleOrientation() {
		switch (this.getPlotDimension()) {
		case X: return AxisVisibleOrientation.HORIZONTAL;
		case Y: return AxisVisibleOrientation.VERTICAL;
		}
		return null;
	}
}
