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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import plotter.Axis;
import plotter.xy.DefaultXYLayoutGenerator;
import plotter.xy.LinearXYAxis;
import plotter.xy.LinearXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.SlopeLine;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYGrid;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

public class StressTest {
	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
		int plotsx = 32;
		int plotsy = 32;
		final int linesPerPlot = 2;
		JPanel container = new JPanel();
		container.setLayout(new GridLayout(plotsx, plotsy));
		contentPane.add(new JScrollPane(container));
		Timer timer = new Timer();
		final int numPlots = plotsx * plotsy;
		final Axis[] xAxes = new Axis[numPlots];
		final Axis[] yAxes = new Axis[numPlots];
		final SimpleXYDataset[][] datasets = new SimpleXYDataset[numPlots][linesPerPlot];
		SlopeLine slopeLine = new SlopeLine();
		slopeLine.setForeground(Color.white);
		for(int i = 0; i < numPlots; i++) {
			final XYPlot plot = new XYPlot();
			final XYAxis xAxis = new LinearXYAxis(XYDimension.X);
			final XYAxis yAxis = new LinearXYAxis(XYDimension.Y);
			xAxis.setPreferredSize(new Dimension(1, 30));
			yAxis.setPreferredSize(new Dimension(40, 1));
			xAxis.setForeground(Color.white);
			yAxis.setForeground(Color.white);
			xAxis.setTextMargin(10);
			yAxis.setTextMargin(10);
			plot.add(xAxis);
			plot.add(yAxis);
			plot.setXAxis(xAxis);
			plot.setYAxis(yAxis);
			plot.setBackground(Color.darkGray);
			XYGrid grid = new XYGrid(xAxis, yAxis);
			grid.setForeground(Color.lightGray);
			XYPlotContents contents = new XYPlotContents();
			contents.setBackground(Color.black);
			plot.add(contents);
			contents.add(grid);
			plot.setPreferredSize(new Dimension(150, 100));

			new DefaultXYLayoutGenerator().generateLayout(plot);

			for(int j = 0; j < linesPerPlot; j++) {
				final LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
				line.setForeground(Color.white);
				final SimpleXYDataset d = new SimpleXYDataset(line);
				d.setMaxCapacity(1000);
				d.setXData(line.getXData());
				d.setYData(line.getYData());
				contents.add(line);

				for(int x = 0; x < 900; x++) {
					double x2 = x / 10.0;
					double y2 = Math.sin(x2 / 10.0 + Math.PI * j / (double) linesPerPlot);
					d.add(x2, y2);
				}
				datasets[i][j] = d;
			}
			slopeLine.attach(plot);

			yAxis.setStart(-1.2);
			yAxis.setEnd(1.2);
			xAxis.setStart(0);
			xAxis.setEnd(10);

			container.add(plot);

			xAxes[i] = xAxis;
			yAxes[i] = yAxis;

			System.out.println("Plot " + (i + 1) + " of " + numPlots + " created");
		}
		timer.schedule(new TimerTask() {
			int x = 0;


			@Override
			public void run() {
				x++;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						for(int i = 0; i < numPlots; i++) {
							xAxes[i].setStart(x / 10);
							xAxes[i].setEnd(x / 10 + 100);
							for(int j = 0; j < linesPerPlot; j++) {
								double x2 = x / 10.0 + 90;
								double y2 = Math.sin(x2 / 10.0 + Math.PI * j / (double) linesPerPlot);
								datasets[i][j].add(x2, y2);
							}
						}
					}
				});
			}
		}, 1000, 1000);
		frame.setSize(400, 300);
		frame.setVisible(true);
	}
}
