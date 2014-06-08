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
import java.awt.Dimension;
import java.text.ChoiceFormat;

import javax.swing.JFrame;

import plotter.IntegerTickMarkCalculator;
import plotter.xy.LinearXYPlotLine;
import plotter.xy.LinearXYPlotLine.LineMode;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;

public class Enumeration {
	public static void main(String[] args) {
		XYPlotFrame frame = new XYPlotFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setup();

		XYAxis xAxis = frame.getXAxis();
		XYAxis yAxis = frame.getYAxis();
		yAxis.setPreferredSize(new Dimension(75, 50));
		xAxis.setStartMargin(75);
		yAxis.setFormat(new ChoiceFormat(new double[] {-Double.POSITIVE_INFINITY, 0, 1, 2, 2.000001}, new String[] {"", "red",
				"green", "blue", ""}));
		yAxis.setTickMarkCalculator(new IntegerTickMarkCalculator());
		final LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line.setForeground(Color.white);
		line.setLineMode(LineMode.STEP_YX);
		final SimpleXYDataset d = new SimpleXYDataset(line);
		d.setMaxCapacity(1000);
		d.setXData(line.getXData());
		d.setYData(line.getYData());
		frame.addPlotLine(line);

		yAxis.setStart(-1);
		yAxis.setEnd(3);
		xAxis.setStart(0);
		xAxis.setEnd(10);

		d.add(2, 0);
		d.add(3, 1);
		d.add(4, 2);

		frame.setSize(400, 300);
		frame.setVisible(true);
	}
}
