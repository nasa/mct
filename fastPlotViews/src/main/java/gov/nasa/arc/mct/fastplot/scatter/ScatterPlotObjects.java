package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;
import plotter.xy.XYGrid;

public class ScatterPlotObjects extends JPanel {
	private static final long serialVersionUID = 3465361001952456712L;

	private ScatterPlot plotPackage;
	
	private XYPlot         plot     = new XYPlot();
	private XYPlotContents contents = new XYPlotContents();
	private LinearXYAxis   xAxis = new LinearXYAxis(XYDimension.X);
	private LinearXYAxis   yAxis = new LinearXYAxis(XYDimension.Y);
	private XYGrid         grid  = new XYGrid(xAxis, yAxis);
	
	public ScatterPlotObjects (ScatterPlot scatterPlot) {
		this.plotPackage = scatterPlot;
		
		XYGrid grid = new XYGrid(xAxis, yAxis);
		grid.setForeground(Color.WHITE); //TODO: Get this from somewhere reasonable
		
		contents.setBackground(Color.BLACK);
		contents.add(grid);
		
		plot.setXAxis(xAxis);
		plot.setYAxis(yAxis);
		
		plot.add(contents);
		plot.add(xAxis);
		plot.add(yAxis);
		plot.add(scatterPlot.getLegendManager());
		setupAxes();
		setupLayout();
		
		this.add(plot);
	}
	
	public XYPlot getXYPlot() {
		return plot;
	}
	
	private boolean isAxisInverted(XYDimension dimension) {
		switch (dimension) {
		case X:
			return plotPackage.getXAxisMaximumLocation() ==
				   XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
		case Y:
			return plotPackage.getYAxisMaximumLocation() ==
				   YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
		}
		assert false : "Unknown dimension";
		return false; // Should not reach
	}
	
	private void setupAxes() {
		double min = plotPackage.getMinNonTime();//.getInitialNonTimeMinSetting();
		double max = plotPackage.getMaxNonTime();//.getInitialNonTimeMaxSetting();
		xAxis.setFormat(PlotConstants.NON_TIME_FORMAT);
		yAxis.setFormat(PlotConstants.NON_TIME_FORMAT);
				
		for (XYDimension d : XYDimension.values()) {
			LinearXYAxis axis = ((d == XYDimension.X) ? xAxis : yAxis);
			boolean inv = isAxisInverted(d);
			axis.setStart(inv ? max : min);
			axis.setEnd(inv ? min : max);
		}
		
		xAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
		xAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
		xAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 2);
		yAxis.setMinorTickLength(PlotConstants.MINOR_TICK_MARK_LENGTH);
		yAxis.setMajorTickLength(PlotConstants.MAJOR_TICK_MARK_LENGTH);
		yAxis.setTextMargin(PlotConstants.MAJOR_TICK_MARK_LENGTH + 5);
		yAxis.setStartMargin(24);
	}

	private void setupLayout() {
		SpringLayout layout = new SpringLayout();
		plot.setLayout(layout);
		JComponent legend = plotPackage.getLegendManager();
		
		layout.putConstraint(SpringLayout.WEST, xAxis, 64, SpringLayout.EAST, legend);
		layout.putConstraint(SpringLayout.EAST, xAxis, 0, SpringLayout.EAST, plot);
		
		layout.putConstraint(SpringLayout.SOUTH, yAxis, 0, SpringLayout.SOUTH, plot);
		layout.putConstraint(SpringLayout.NORTH, yAxis, 0, SpringLayout.NORTH, plot);
		
		layout.putConstraint(SpringLayout.SOUTH, xAxis, 0, SpringLayout.SOUTH, plot);
		layout.putConstraint(SpringLayout.NORTH, xAxis, -24, SpringLayout.SOUTH, yAxis);
		
		layout.putConstraint(SpringLayout.WEST, yAxis, 0, SpringLayout.EAST, legend);
		layout.putConstraint(SpringLayout.EAST, yAxis, 0, SpringLayout.WEST, xAxis);
		
		layout.putConstraint(SpringLayout.SOUTH, contents, 0, SpringLayout.NORTH, xAxis);
		layout.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.NORTH, plot);
		
		layout.putConstraint(SpringLayout.WEST, contents, 0, SpringLayout.EAST, yAxis);
		layout.putConstraint(SpringLayout.EAST, contents, 0, SpringLayout.EAST, plot);
		
		layout.putConstraint(SpringLayout.WEST, legend, PlotConstants.PLOT_LEGEND_OFFSET_FROM_LEFT_HAND_SIDE, SpringLayout.WEST, plot);
		layout.putConstraint(SpringLayout.NORTH, legend, 0, SpringLayout.NORTH, contents);

	}
	
	public void setAxisRepresentation(Font f, Color c) {
		grid.setForeground(c);
		xAxis.setForeground(c);
		yAxis.setForeground(c);
		xAxis.setFont(f);
		yAxis.setFont(f);
	}
	
	
}
