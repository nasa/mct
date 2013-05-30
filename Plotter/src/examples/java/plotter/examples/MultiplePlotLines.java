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

import javax.swing.JFrame;

import plotter.xy.LinearXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;

public class MultiplePlotLines {
	public static void main(String[] args) {
		XYPlotFrame frame = new XYPlotFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUseLegend(true);
		frame.setTitle(MultiplePlotLines.class.getSimpleName());
		frame.setup();

		XYAxis xAxis = frame.getXAxis();
		XYAxis yAxis = frame.getYAxis();

		final LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line.setForeground(Color.red);
		final SimpleXYDataset d = new SimpleXYDataset(line);
		d.setMaxCapacity(1000);
		d.setXData(line.getXData());
		d.setYData(line.getYData());
		frame.addPlotLine("one", line);

		final LinearXYPlotLine line2 = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line2.setForeground(Color.green);
		final SimpleXYDataset d2 = new SimpleXYDataset(line2);
		d2.setMaxCapacity(1000);
		d2.setXData(line2.getXData());
		d2.setYData(line2.getYData());
		frame.addPlotLine("two", line2);

		final LinearXYPlotLine line3 = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line3.setForeground(Color.blue);
		final SimpleXYDataset d3 = new SimpleXYDataset(line3);
		d3.setMaxCapacity(1000);
		d3.setXData(line3.getXData());
		d3.setYData(line3.getYData());
		frame.addPlotLine("three", line3);

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
}
