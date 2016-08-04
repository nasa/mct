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

import java.awt.Component;

import javax.swing.SpringLayout;

import plotter.Legend;

/**
 * Sets up the default layout for an {@link XYPlot}.
 * This layout installs a {@link SpringLayout} on the plot
 * and adds constraints on its children.
 * Recognized children include:
 * <ul>
 * <li>{@link XYPlotContents}
 * <li>{@link XYAxis}
 * <li>{@link XYLocationDisplay}
 * <li>{@link SlopeLineDisplay}
 * </ul>
 * Other children will be ignored.
 * Children added after the layout is installed will not be set up.
 * @author Adam Crume
 */
public class DefaultXYLayoutGenerator {
	/**
	 * Sets up the layout for the plot.
	 * Note that the constraints for some children may be affected by the existence of certain other children.
	 * @param plot plot to lay out
	 */
	public void generateLayout(XYPlot plot) {
		SpringLayout layout = new SpringLayout();
		plot.setLayout(layout);

		XYAxis xAxis = null;
		XYAxis yAxis = null;
		XYAxis x2Axis = null;
		XYAxis y2Axis = null;
		XYPlotContents contents = null;
		XYLocationDisplay locationDisplay = null;
		SlopeLineDisplay slopeDisplay = null;
		Legend legend = null;
		for(Component c : plot.getComponents()) {
			if(c instanceof XYPlotContents) {
				contents = (XYPlotContents) c;
				layout.putConstraint(SpringLayout.EAST, c, 0, SpringLayout.EAST, plot);
			} else if(c instanceof XYAxis) {
				XYAxis a = (XYAxis) c;
				if(a.getPlotDimension() == XYDimension.X) {
					if(a.isMirrored()) {
						x2Axis = a;
						layout.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, plot);
						layout.putConstraint(SpringLayout.EAST, c, 0, SpringLayout.EAST, plot);
						layout.putConstraint(SpringLayout.WEST, c, 0, SpringLayout.WEST, plot);
					} else {
						xAxis = a;
						layout.putConstraint(SpringLayout.SOUTH, c, 0, SpringLayout.SOUTH, plot);
						layout.putConstraint(SpringLayout.EAST, c, 0, SpringLayout.EAST, plot);
						layout.putConstraint(SpringLayout.WEST, c, 0, SpringLayout.WEST, plot);
					}
				} else {
					if(a.isMirrored()) {
						y2Axis = a;
						layout.putConstraint(SpringLayout.EAST, c, 0, SpringLayout.EAST, plot);
						layout.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, plot);
						layout.putConstraint(SpringLayout.SOUTH, c, 0, SpringLayout.SOUTH, plot);
					} else {
						yAxis = a;
						layout.putConstraint(SpringLayout.WEST, c, 0, SpringLayout.WEST, plot);
						layout.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, plot);
						layout.putConstraint(SpringLayout.SOUTH, c, 0, SpringLayout.SOUTH, plot);
					}
				}
			} else if(c instanceof XYLocationDisplay) {
				locationDisplay = (XYLocationDisplay) c;
				layout.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, plot);
			} else if(c instanceof SlopeLineDisplay) {
				slopeDisplay = (SlopeLineDisplay) c;
				layout.putConstraint(SpringLayout.NORTH, c, 0, SpringLayout.NORTH, plot);
			} else if(c instanceof Legend) {
				legend = (Legend) c;
			}
		}

		if(contents != null) {
			if(locationDisplay != null) {
				layout.putConstraint(SpringLayout.WEST, locationDisplay, 0, SpringLayout.WEST, contents);
				layout.putConstraint(SpringLayout.EAST, locationDisplay, 0, SpringLayout.HORIZONTAL_CENTER, contents);
			}

			if(slopeDisplay != null) {
				layout.putConstraint(SpringLayout.WEST, slopeDisplay, 0, SpringLayout.HORIZONTAL_CENTER, contents);
				layout.putConstraint(SpringLayout.EAST, slopeDisplay, 0, SpringLayout.EAST, contents);
			}

			int margin;
			if(x2Axis != null) {
				layout.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.SOUTH, x2Axis);
				margin = (int) x2Axis.getPreferredSize().getHeight();
				if(locationDisplay != null) {
					margin += (int) locationDisplay.getPreferredSize().getHeight();
				} else if(slopeDisplay != null) {
					margin += (int) slopeDisplay.getPreferredSize().getHeight();
				}
			} else if(locationDisplay != null) {
				layout.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.SOUTH, locationDisplay);
				margin = (int) locationDisplay.getPreferredSize().getHeight();
			} else if(slopeDisplay != null) {
				layout.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.SOUTH, slopeDisplay);
				margin = (int) slopeDisplay.getPreferredSize().getHeight();
			} else {
				layout.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.NORTH, plot);
				margin = 0;
			}

			if(xAxis != null) {
				layout.putConstraint(SpringLayout.SOUTH, contents, 0, SpringLayout.NORTH, xAxis);
			} else {
				layout.putConstraint(SpringLayout.SOUTH, contents, 0, SpringLayout.SOUTH, plot);
			}

			if(yAxis != null) {
				layout.putConstraint(SpringLayout.WEST, contents, 0, SpringLayout.EAST, yAxis);
				yAxis.setEndMargin(margin);
			} else {
				layout.putConstraint(SpringLayout.WEST, contents, 0, SpringLayout.WEST, plot);
			}

			if(legend != null) {
				layout.putConstraint(SpringLayout.NORTH, legend, 0, SpringLayout.NORTH, contents);
				layout.putConstraint(SpringLayout.EAST, legend, 0, SpringLayout.EAST, contents);
			}
			if(y2Axis != null) {
				layout.putConstraint(SpringLayout.EAST, contents, 0, SpringLayout.WEST, y2Axis);
				y2Axis.setEndMargin(margin);
			} else {
				layout.putConstraint(SpringLayout.EAST, contents, 0, SpringLayout.EAST, plot);
			}
		}

		if(xAxis != null && yAxis != null) {
			xAxis.setStartMargin((int) yAxis.getPreferredSize().getWidth());
			yAxis.setStartMargin((int) xAxis.getPreferredSize().getHeight());
		}

		if(xAxis != null && y2Axis != null) {
			xAxis.setEndMargin((int) y2Axis.getPreferredSize().getWidth());
			y2Axis.setStartMargin((int) xAxis.getPreferredSize().getHeight());
		}

		if(x2Axis != null && yAxis != null) {
			x2Axis.setStartMargin((int) yAxis.getPreferredSize().getWidth());
			yAxis.setEndMargin((int) x2Axis.getPreferredSize().getHeight());
		}

		if(x2Axis != null && y2Axis != null) {
			x2Axis.setEndMargin((int) y2Axis.getPreferredSize().getWidth());
			y2Axis.setEndMargin((int) x2Axis.getPreferredSize().getHeight());
		}
	}
}
