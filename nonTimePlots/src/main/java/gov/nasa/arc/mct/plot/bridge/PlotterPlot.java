package gov.nasa.arc.mct.plot.bridge;

import gov.nasa.arc.mct.plot.adapter.Plot;
import gov.nasa.arc.mct.plot.adapter.PlotAppearance;
import gov.nasa.arc.mct.plot.adapter.PlotAxis;
import gov.nasa.arc.mct.plot.adapter.PlotContents;
import gov.nasa.arc.mct.plot.adapter.PlotLine;
import gov.nasa.arc.mct.plot.settings.PlotConfiguration;
import gov.nasa.arc.mct.plot.settings.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.plot.settings.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.plot.settings.PlotConstants.YAxisMaximumLocationSetting;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SpringLayout;

import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYGrid;
import plotter.xy.XYPlot;

public class PlotterPlot extends XYPlot implements Plot, PlotAppearance {
	private static final long serialVersionUID = -879851368944464375L;

	private static final int PLOT_MARGIN = 16;
	
	private List<PlotLine>  plotLines = new ArrayList<PlotLine>();
	private PlotAxis        plotAxes[] = new PlotAxis[PlotAxisType.values().length];
	private PlotLineFactory plotLineFactory = new TemporalPlotLineFactory(this, false);	
	
	private PlotterPlotContents contents = new PlotterPlotContents();
	private XYGrid              grid;

	public PlotterPlot() {
		setXAxis(new LinearXYAxis(XYDimension.X));
		setYAxis(new LinearXYAxis(XYDimension.Y));
		grid = new XYGrid(getXAxis(), getYAxis());
		
		contents.add(grid);
		
		add(getXAxis());
		add(getYAxis());
		add(contents  );
		
		setupLayout();
	}
	
	
	@Override
	public JComponent getRepresentation() {
		return this;
	}

	@Override
	public void configure(PlotConfiguration configuration) {
		boolean rotated = configuration.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME;
		
		// Create some wrappers
		plotLineFactory = (configuration.getAxisOrientationSetting() == AxisOrientationSetting.Z_AXIS_AS_TIME) ?
				new ScatterPlotLineFactory(this, rotated) : new TemporalPlotLineFactory(this, rotated);
				
		/* Configure plot axis wrappers */
		PlotAxis xAxisWrapper = new PlotterPlotAxis(getXAxis(), 
				configuration.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
		PlotAxis yAxisWrapper = new PlotterPlotAxis(getXAxis(), 
				configuration.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
		
		plotAxes[PlotAxisType.X.ordinal()] = xAxisWrapper;
		plotAxes[PlotAxisType.Y.ordinal()] = yAxisWrapper;
		plotAxes[PlotAxisType.INDEPENDENT.ordinal()] = 
			(configuration.getAxisOrientationSetting() != AxisOrientationSetting.Y_AXIS_AS_TIME) ?
					xAxisWrapper : yAxisWrapper;
		plotAxes[PlotAxisType.DEPENDENT.ordinal()] = 
			(configuration.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME) ?
					xAxisWrapper : yAxisWrapper;
		plotAxes[PlotAxisType.TEMPORAL.ordinal()] =
			(configuration.getAxisOrientationSetting() == AxisOrientationSetting.Z_AXIS_AS_TIME) ?
			null : // TODO: Create an invisible time axis
			plotAxes[PlotAxisType.INDEPENDENT.ordinal()];
			   
		
	}

	@Override
	public PlotLine createPlotLine() {
		PlotLine line = plotLineFactory.createLine();
		plotLines.add(line);
		return line;
	}

	@Override
	public List<PlotLine> getPlotLines() {
		return plotLines;
	}

	@Override
	public PlotAppearance getAppearance() {
		return this;
	}

	@Override
	public PlotAxis getAxis(PlotAxisType axisType) {
		return plotAxes[axisType.ordinal()];
	}
	
	@Override
	public PlotContents getPlotContents() {
		return contents;
	}

	@Override
	public void setAppearance(PlotAppearance appearance) {
		setBackground(appearance.getBackground());
		getXAxis().setBackground(appearance.getFrameBackground());
		getYAxis().setBackground(appearance.getFrameBackground());
		grid.setForeground(appearance.getGridColor());
		for (PlotAxisType axisType : PlotAxisType.values()) {
			Font     f = appearance.getFont(axisType);
			PlotAxis a = this.getAxis(axisType);
			if (a != null && f != null) {
				a.getRepresentation().setFont(f);
			}
		}
	}

	private void setupLayout() {
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		
		layout.putConstraint(SpringLayout.WEST, getXAxis(), PLOT_MARGIN*2, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, getXAxis(), 0, SpringLayout.EAST, this);
		
		layout.putConstraint(SpringLayout.SOUTH, getYAxis(), -PLOT_MARGIN, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.NORTH, getYAxis(), 0, SpringLayout.NORTH, this);
		
		layout.putConstraint(SpringLayout.SOUTH, getXAxis(), 0, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.NORTH, getXAxis(), 0, SpringLayout.SOUTH, getYAxis());
		
		layout.putConstraint(SpringLayout.WEST, getYAxis(), 0, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, getYAxis(), 0, SpringLayout.WEST, getXAxis());
		
		layout.putConstraint(SpringLayout.SOUTH, contents, 0, SpringLayout.NORTH, getXAxis());
		layout.putConstraint(SpringLayout.NORTH, contents, 0, SpringLayout.NORTH, this);
		
		layout.putConstraint(SpringLayout.WEST, contents, 0, SpringLayout.EAST, getYAxis());
		layout.putConstraint(SpringLayout.EAST, contents, 0, SpringLayout.EAST, this);		
	}
	
	/* Plot Appearance */
	@Override
	public Font getFont(PlotAxisType axis) {
		return getAxis(axis).getRepresentation().getFont();
	}

	@Override
	public Color getFrameBackground() {
		return this.getXAxis().getBackground();
	}

	@Override
	public Color getGridColor() {
		return grid.getForeground();
	}

	@Override
	public Color getAxisColor() {
		return this.getXAxis().getForeground();
	}



}
