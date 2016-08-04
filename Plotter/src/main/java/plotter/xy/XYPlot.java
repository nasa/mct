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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.geom.Point2D;

import plotter.Plot;


/**
 * Plots points using Cartesian coordinates.
 * @author Adam Crume
 */
public class XYPlot extends Plot {
	private static final long serialVersionUID = 1L;

	/** The X axis. */
	private XYAxis xAxis;

	/** The Y axis. */
	private XYAxis yAxis;

	/** The X2 axis. */
	private XYAxis x2Axis;

	/** The Y2 axis. */
	private XYAxis y2Axis;


	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Color background = getBackground();
		if(background != null) {
			g.setColor(background);
			g.fillRect(0, 0, width, height);
		}
	}


	@Override
	public void toLogical(Point2D dest, Point2D src) {
		double x = xAxis.toLogical((int) (src.getX() + .5) - xAxis.getX());
		double y = yAxis.toLogical((int) (src.getY() + .5) - yAxis.getY());
		dest.setLocation(x, y);
	}


	@Override
	public void toPhysical(Point2D dest, Point2D src) {
		int x = xAxis.toPhysical(src.getX()) + xAxis.getX();
		int y = yAxis.toPhysical(src.getY()) + yAxis.getY();
		dest.setLocation(x, y);
	}


	/**
	 * Overridden to return false since subcomponents are transparent.
	 */
	@Override
	public boolean isOptimizedDrawingEnabled() {
		return false;
	}


	/**
	 * Returns the X axis.
	 * @return the X axis
	 */
	public XYAxis getXAxis() {
		return xAxis;
	}


	/**
	 * Sets the X axis.
	 * @param xAxis the X axis
	 */
	public void setXAxis(XYAxis xAxis) {
		this.xAxis = xAxis;
	}


	/**
	 * Returns the Y axis.
	 * @return the Y axis
	 */
	public XYAxis getYAxis() {
		return yAxis;
	}


	/**
	 * Sets the Y axis.
	 * @param yAxis the Y axis
	 */
	public void setYAxis(XYAxis yAxis) {
		this.yAxis = yAxis;
	}


	/**
	 * Returns the X2 axis.
	 * @return the X2 axis
	 */
	public XYAxis getX2Axis() {
		return x2Axis;
	}


	/**
	 * Sets the X2 axis
	 * @param x2Axis the X2 axis
	 */
	public void setX2Axis(XYAxis x2Axis) {
		this.x2Axis = x2Axis;
	}


	/**
	 * Returns the Y2 axis
	 * @return the Y2 axis
	 */
	public XYAxis getY2Axis() {
		return y2Axis;
	}


	/**
	 * Sets the Y2 axis
	 * @param y2Axis the Y2 axis
	 */
	public void setY2Axis(XYAxis y2Axis) {
		this.y2Axis = y2Axis;
	}


	/**
	 * Returns the content area of the plot.
	 * @return the content area of the plot
	 */
	public XYPlotContents getContents() {
		XYPlotContents contents = null;
		for(Component c : getComponents()) {
			if(c instanceof XYPlotContents) {
				contents = (XYPlotContents) c;
			}
		}
		return contents;
	}
}
