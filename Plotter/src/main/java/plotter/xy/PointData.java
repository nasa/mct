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
package plotter.xy;

import plotter.DoubleData;
import plotter.DoubleDataDouble;

/**
 * Stores X and Y data together for convenience.
 * @author Adam Crume
 */
public class PointData implements CompressionOutput {
	private DoubleData x;

	private DoubleData y;


	/**
	 * Creates point data with new default {@link DoubleData} objects for X and Y.
	 */
	public PointData() {
		this(new DoubleDataDouble(), new DoubleDataDouble());
	}


	/**
	 * Creates point data using the given X and Y data.
	 * @param x X data
	 * @param y Y data
	 */
	public PointData(DoubleData x, DoubleData y) {
		this.x = x;
		this.y = y;
	}


	/**
	 * Returns the X data.
	 * @return the X data
	 */
	public DoubleData getX() {
		return x;
	}


	/**
	 * Sets the X data.
	 * @param x the X data
	 */
	public void setX(DoubleData x) {
		this.x = x;
	}


	/**
	 * Returns the Y data.
	 * @return the Y data
	 */
	public DoubleData getY() {
		return y;
	}


	/**
	 * Sets the Y data.
	 * @param y the Y data
	 */
	public void setY(DoubleData y) {
		this.y = y;
	}


	/**
	 * Convenience method for adding a point.
	 * Equivalent to:
	 * <pre>
	 * PointData d = ...;
	 * d.getX().add(x);
	 * d.getY().add(y);
	 * </pre>
	 * @param x X coordinate of the point to add
	 * @param y Y coordinate of the point to add
	 */
	@Override
	public void add(double x, double y) {
		this.x.add(x);
		this.y.add(y);
	}


	/**
	 * Removes all points.
	 */
	public void removeAll() {
		x.removeAll();
		y.removeAll();
	}


	/**
	 * Removes <code>count</code> points from the end.
	 * @param count number of points to remove
	 */
	@Override
	public void removeLast(int count) {
		x.removeLast(count);
		y.removeLast(count);
	}


	@Override
	public int getPointCount() {
		return x.getLength();
	}
}
