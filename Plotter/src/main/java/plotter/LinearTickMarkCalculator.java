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

import java.util.Arrays;



/**
 * Default implementation of {@link TickMarkCalculator} for a linear axis.
 * @author Adam Crume
 */
public class LinearTickMarkCalculator implements TickMarkCalculator {
	@Override
	public double[][] calculateTickMarks(Axis axis) {
		double start = axis.getStart();
		double end = axis.getEnd();
		double min = start;
		double max = end;
		if(min > max) {
			double tmp = max;
			max = min;
			min = tmp;
		}
		double diff = max - min;

		// d should be diff scaled to between 1 and 10
		double d = diff / Math.pow(10, Math.floor(Math.log10(diff)));
		double adj = .1;
		while(d < 2) {
			d *= 2;
			adj *= 2;
		}
		while(d > 5) {
			d /= 2;
			adj *= 5;
		}
		double spacing = diff / d;
		double spacing2 = spacing * adj;

		double[] minorVals;
		double[] majorVals;
		if(min >= 0) {
			// In this case, both ends of the axis are non-negative.  Go from min to max.
			double startCount = Math.ceil(min / spacing);
			double endCount = Math.floor(max / spacing);
			majorVals = new double[(int)(endCount - startCount) + 1];
			double value = startCount * spacing;
			for(int i = 0; i < majorVals.length; i++) {
				majorVals[i] = value;
				value += spacing;
			}

			startCount = Math.ceil(min / spacing2);
			endCount = Math.floor(max / spacing2);
			minorVals = new double[(int) (endCount - startCount) + 1];
			value = startCount * spacing2;
			for(int i = 0; i < minorVals.length; i++) {
				minorVals[i] = value;
				value += spacing2;
			}
		} else if(max <= 0) {
			// In this case, both ends of the axis are non-positive.  Go from max to min.
			double startCount = Math.floor(max / spacing);
			double endCount = Math.ceil(min / spacing);
			majorVals = new double[(int) (startCount - endCount) + 1];
			double value = startCount * spacing;
			for(int i = 0; i < majorVals.length; i++) {
				majorVals[i] = value;
				value -= spacing;
			}

			startCount = Math.floor(max / spacing2);
			endCount = Math.ceil(min / spacing2);
			minorVals = new double[(int) (startCount - endCount) + 1];
			value = startCount * spacing2;
			for(int i = 0; i < minorVals.length; i++) {
				minorVals[i] = value;
				value -= spacing2;
			}
		} else {
			// In this case, one end is positive and one is negative.
			// Start from 0 and go in both directions to minimize rounding error.

			// Calculate the number of major ticks.  There should be a better way.
			int majorCount = 0;
			double value = 0;
			while(value <= max) {
				value += spacing;
				majorCount++;
			}
			value = -spacing;
			while(value >= min) {
				value -= spacing;
				majorCount++;
			}

			majorVals = new double[majorCount];
			value = 0;
			int i = 0;
			while(value <= max) {
				majorVals[i] = value;
				i++;
				value += spacing;
			}
			value = -spacing;
			while(value >= min) {
				majorVals[i] = value;
				i++;
				value -= spacing;
			}

			// Calculate the number of minor ticks.  There should be a better way.
			int minorCount = 0;
			value = 0;
			while(value <= max) {
				value += spacing2;
				minorCount++;
			}
			value = -spacing2;
			while(value >= min) {
				value -= spacing2;
				minorCount++;
			}

			minorVals = new double[minorCount];
			value = 0;
			i = 0;
			while(value <= max) {
				minorVals[i] = value;
				i++;
				value += spacing2;
			}
			value = -spacing2;
			while(value >= min) {
				minorVals[i] = value;
				i++;
				value -= spacing2;
			}
		}

		Arrays.sort(majorVals);
		return new double[][] {majorVals, minorVals};
	}
}
