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
package plotter.examples;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import plotter.Legend;
import plotter.LegendItem;
import plotter.LegendItem.DescriptionPosition;
import plotter.xy.LinearXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

public class LegendStyles {
	public static void main(String[] args) {
		XYPlotFrame frame = new XYPlotFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(LegendStyles.class.getSimpleName());
		frame.setup();
		XYPlot plot = frame.getPlot();
		XYAxis xAxis = frame.getXAxis();
		XYAxis yAxis = frame.getYAxis();
		SpringLayout layout = (SpringLayout) plot.getLayout();
		XYPlotContents contents = plot.getContents();
		List<Legend> legends=new ArrayList<Legend>();

		Legend legend1 = createLegend(plot, 0, 1, 1, 0, null, SwingConstants.LEFT);
		layout.putConstraint(SpringLayout.NORTH, legend1, 0, SpringLayout.NORTH, contents);
		layout.putConstraint(SpringLayout.EAST, legend1, 0, SpringLayout.EAST, contents);
		legends.add(legend1);

		Legend legend2 = createLegend(plot, 1, 1, 0, 0, null, SwingConstants.RIGHT);
		layout.putConstraint(SpringLayout.SOUTH, legend2, 0, SpringLayout.SOUTH, contents);
		layout.putConstraint(SpringLayout.EAST, legend2, 0, SpringLayout.EAST, contents);
		legends.add(legend2);

		Legend legend3 = createLegend(plot, 1, 0, 0, 1, LegendItem.DescriptionPosition.LEFT, SwingConstants.RIGHT);
		layout.putConstraint(SpringLayout.SOUTH, legend3, 0, SpringLayout.SOUTH, contents);
		layout.putConstraint(SpringLayout.WEST, legend3, 0, SpringLayout.WEST, contents);
		legends.add(legend3);

		Legend legend4 = createLegend(plot, 0, 0, 1, 1, LegendItem.DescriptionPosition.LEFT, SwingConstants.LEFT);
		layout.putConstraint(SpringLayout.NORTH, legend4, 0, SpringLayout.NORTH, contents);
		layout.putConstraint(SpringLayout.WEST, legend4, 0, SpringLayout.WEST, contents);
		legends.add(legend4);

		Legend legend5 = createLegend(plot, 1, 1, 1, 1, null, SwingConstants.CENTER);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, legend5, 0, SpringLayout.VERTICAL_CENTER, contents);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, legend5, 0, SpringLayout.HORIZONTAL_CENTER, contents);
		legends.add(legend5);

		Legend legend6 = createLegend(plot, 0, 0, 0, 0, null, SwingConstants.LEFT);
		layout.putConstraint(SpringLayout.NORTH, legend6, 0, SpringLayout.NORTH, contents);
		layout.putConstraint(SpringLayout.WEST, legend6, 0, SpringLayout.WEST, plot);
		layout.putConstraint(SpringLayout.WEST, plot.getYAxis(), 0, SpringLayout.EAST, legend6);
		legend6.setBorder(new MatteBorder(1, 0, 1, 1, Color.lightGray));
		legends.add(legend6);

		final LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line.setForeground(Color.red);
		final SimpleXYDataset d = new SimpleXYDataset(line);
		d.setMaxCapacity(1000);
		d.setXData(line.getXData());
		d.setYData(line.getYData());
		frame.addPlotLine("one", line);
		for(Legend legend : legends) {
			legend.addLine("one", line);
		}

		final LinearXYPlotLine line2 = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line2.setForeground(Color.green);
		final SimpleXYDataset d2 = new SimpleXYDataset(line2);
		d2.setMaxCapacity(1000);
		d2.setXData(line2.getXData());
		d2.setYData(line2.getYData());
		frame.addPlotLine("two", line2);
		for(Legend legend : legends) {
			legend.addLine("two", line2);
		}

		final LinearXYPlotLine line3 = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line3.setForeground(Color.blue);
		final SimpleXYDataset d3 = new SimpleXYDataset(line3);
		d3.setMaxCapacity(1000);
		d3.setXData(line3.getXData());
		d3.setYData(line3.getYData());
		frame.addPlotLine("three", line3);
		for(Legend legend : legends) {
			legend.addLine("three", line3);
		}

		yAxis.setStart(-1.2);
		yAxis.setEnd(1.2);
		xAxis.setStart(0);
		xAxis.setEnd(2 * Math.PI);

		for(int x = 0; x <= 100; x++) {
			double x2 = x / 100.0 * 2 * Math.PI;
			d.add(x2, Math.sin(x2));
			d2.add(x2, Math.sin(x2 + Math.PI / 4));
			d3.add(x2, Math.sin(x2 + Math.PI / 2));
		}

		frame.setSize(400, 300);
		frame.setVisible(true);
	}

	private static Legend createLegend(XYPlot plot, int top, int left, int bottom, int right, DescriptionPosition descPos, int textAlignment) {
		Legend legend = new Legend(descPos, textAlignment);
		legend.setForeground(Color.white);
		legend.setBackground(Color.black);
		legend.setFont(new Font("Arial", 0, 12));
		legend.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.darkGray));
		plot.add(legend, 0);
		return legend;
	}
}
