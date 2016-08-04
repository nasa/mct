package plotter.examples;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.JFrame;

import plotter.Rotation;
import plotter.xy.LinearXYAxis;
import plotter.xy.LinearXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;

public class BigLabels {
	public static void main(String[] args) {
		XYPlotFrame frame = new XYPlotFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setup();

		XYAxis xAxis = frame.getXAxis();
		XYAxis yAxis = frame.getYAxis();
		final LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
		line.setForeground(Color.white);
		final SimpleXYDataset d = new SimpleXYDataset(line);
		d.setMaxCapacity(1000);
		d.setXData(line.getXData());
		d.setYData(line.getYData());
		frame.addPlotLine(line);

		yAxis.setStart(-1.2);
		yAxis.setEnd(1.2);
		xAxis.setStart(0);
		xAxis.setEnd(2 * Math.PI);
		xAxis.setFormat(new DecimalFormat("0.0000000"));
		yAxis.setFormat(new DecimalFormat("0.0000000"));
		((LinearXYAxis)yAxis).setLabelRotation(Rotation.CCW);

		for(int x = 0; x <= 100; x++) {
			double x2 = x / 100.0 * 2 * Math.PI;
			double y2 = Math.sin(x2);
			d.add(x2, y2);
		}

		frame.setSize(400, 300);
		frame.setVisible(true);
	}
}
