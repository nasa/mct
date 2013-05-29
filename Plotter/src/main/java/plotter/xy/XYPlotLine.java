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
import plotter.PlotLine;

/**
 * Base class for all XY plot lines.
 * @author Adam Crume
 */
public abstract class XYPlotLine extends PlotLine {
	private static final long serialVersionUID = 2L;

	/**
	 * Returns the X data.
	 * This should not be modified directly.
	 * @return X data
	 */
	public abstract DoubleData getXData();


	/**
	 * Returns the Y data.
	 * This should not be modified directly.
	 * @return Y data
	 */
	public abstract DoubleData getYData();


	/**
	 * Adds a data point.
	 * @param x X-coordinate of the data point
	 * @param y Y-coordinate of the data point
	 */
	public abstract void add(double x, double y);


	/**
	 * Returns the independent dimension.  May be null.
	 * @return the independent dimension
	 */
	public abstract XYDimension getIndependentDimension();


	/**
	 * Adds data to the beginning.
	 * @param x X data to add
	 * @param y Y data to add
	 */
	public abstract void prepend(DoubleData x, DoubleData y);


	/**
	 * Adds data to the beginning.
	 * @param x X data to add
	 * @param xoff index within <code>x</code> to start copying from
	 * @param y Y data to add
	 * @param yoff index within <code>y</code> to start copying from
	 * @param len number of points to add
	 */
	public abstract void prepend(double[] x, int xoff, double[] y, int yoff, int len);
}
