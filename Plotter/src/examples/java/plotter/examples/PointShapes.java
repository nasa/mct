package plotter.examples;

import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import plotter.Shapes;
import plotter.xy.LinearXYPlotLine;
import plotter.xy.SimpleXYDataset;
import plotter.xy.XYAxis;
import plotter.xy.XYDimension;

public class PointShapes {
	public static void main(String[] args) {
		XYPlotFrame frame = new XYPlotFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setup();

		XYAxis xAxis = frame.getXAxis();
		XYAxis yAxis = frame.getYAxis();
		List<Shape> shapes = new ArrayList<Shape>();
		shapes.add(Shapes.square(5));
		shapes.add(Shapes.circle(5));
		shapes.add(Shapes.triangleUp(5));
		shapes.add(Shapes.triangleDown(5));
		shapes.add(Shapes.shuttle(5));
		shapes.add(Shapes.star(5));
		shapes.add(Shapes.plus(5));
		shapes.add(Shapes.x(5));
		LinearXYPlotLine[] lines = new LinearXYPlotLine[shapes.size() * 2];
		SimpleXYDataset[] datasets = new SimpleXYDataset[lines.length];
		for(int i = 0; i < lines.length; i++) {
			LinearXYPlotLine line = new LinearXYPlotLine(xAxis, yAxis, XYDimension.X);
			line.setForeground(Color.white);
			SimpleXYDataset d = new SimpleXYDataset(line);
			d.setMaxCapacity(1000);
			d.setXData(line.getXData());
			d.setYData(line.getYData());
			frame.addPlotLine(line);
			if(i % 2 == 0) {
				line.setPointOutline(shapes.get(i / 2));
			} else {
				line.setPointFill(shapes.get(i / 2));
			}
			lines[i] = line;
			datasets[i] = d;
		}

		yAxis.setStart(-1.2);
		yAxis.setEnd(1.2);
		xAxis.setStart(0);
		xAxis.setEnd(2 * Math.PI);

		for(int x = 0; x <= 10; x++) {
			double x2 = x / 10.0 * 2 * Math.PI;
			for(int i = 0; i < datasets.length; i++) {
				datasets[i].add(x2, Math.sin(x2 - Math.PI * i / datasets.length));
			}
		}

		frame.setSize(400, 300);
		frame.setVisible(true);
	}
}
