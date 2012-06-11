package plotter.examples;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;

import plotter.xy.ScatterXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYAxis;

public class ScatterPointShapes {
	public static void main(String[] args) {
		XYPlotFrame frame = new XYPlotFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setup();

		XYAxis xAxis = frame.getXAxis();
		XYAxis yAxis = frame.getYAxis();
		final ScatterXYPlotLine line = new ScatterXYPlotLine(xAxis, yAxis);
		GeneralPath pointShape = new GeneralPath();
		pointShape.moveTo(-5, -5);
		pointShape.lineTo(-5, 5);
		pointShape.lineTo(5, 5);
		pointShape.lineTo(5, -5);
		pointShape.lineTo(-5, -5);
		line.setPointFill(pointShape);
		line.setForeground(Color.white);
		final SimpleXYDataset d = new SimpleXYDataset(line);
		d.setMaxCapacity(1000);
		d.setXData(line.getXData());
		d.setYData(line.getYData());
		frame.addPlotLine(line);

		yAxis.setStart(-10);
		yAxis.setEnd(10);
		xAxis.setStart(-10);
		xAxis.setEnd(10);

		for(int p = 0; p <= 10; p++) {
			double r = p;
			double theta = p;
			double x = r * Math.cos(theta);
			double y = r * Math.sin(theta);
			d.add(x, y);
		}

		frame.setSize(400, 300);
		frame.setVisible(true);
	}
}
