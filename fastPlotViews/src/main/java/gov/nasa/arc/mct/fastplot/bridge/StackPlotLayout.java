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
package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.utils.ComponentTraverser;
import gov.nasa.arc.mct.fastplot.utils.ComponentTraverser.ComponentProcedure;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import plotter.xy.SlopeLineDisplay;
import plotter.xy.XYAxis;
import plotter.xy.XYLocationDisplay;
import plotter.xy.XYPlot;

/**
 * Manages layout for stack plots. Primarily functions as a grid bag layout, but 
 * also forces alignment on y axes by monitoring / adjusting y axis width and 
 * legend manager width.
 * 
 * @author vwoeltje
 *
 */
public class StackPlotLayout extends GridBagLayout {
	private static final long serialVersionUID = 1L;

	private PlotView plotView;
	
	public StackPlotLayout (PlotView plotView) {
		this.plotView = plotView;
	}
	
	@Override
	public void layoutContainer(Container parent) {
		final List<XYPlotComponents> componentList = new ArrayList<XYPlotComponents>();
		
		/* Find all XYPlots and create mappings to their internal elements */
		ComponentTraverser.traverse(parent, new ComponentProcedure() {
			@Override
			public void run(Component c) {
				if (c instanceof XYPlot) componentList.add(new XYPlotComponents((XYPlot) c));			
			}			
		});
		
		/* Desired widths */
		int yAxisWidth   = 1;
		int legendWidth  = 1;

		/* Desired height (in case legend gets tall) */
		int legendHeight = 1;
	
		/* Swap minimum width and height if we are a sideways stackplot */
		int minWidth, minHeight;		
		if (plotView.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			minWidth = PlotConstants.MINIMUM_PLOT_WIDTH;
			minHeight = PlotConstants.MINIMUM_PLOT_HEIGHT;				
		} else if (plotView.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME){
			minWidth = PlotConstants.MINIMUM_PLOT_HEIGHT;
			minHeight = PlotConstants.MINIMUM_PLOT_WIDTH;			
		} else {
			minWidth = PlotConstants.MINIMUM_PLOT_WIDTH;
			minHeight = PlotConstants.MINIMUM_PLOT_HEIGHT;
		}
	
		
		/* Find the largest "desired" width/height for the components */
		for (XYPlotComponents xyPlotComps : componentList) {
			int textMargin = xyPlotComps.getYAxis().getTextMargin();
			
			yAxisWidth = Math.max(xyPlotComps.getYAxis().getWidth(), yAxisWidth);
			for (Component c : xyPlotComps.getYAxis().getComponents()) {
				yAxisWidth = Math.max(c.getWidth() + textMargin, yAxisWidth);
			}
			for (LegendEntry c : xyPlotComps.getLegend().getLegendEntryList()) {	
				legendWidth = Math.max(c.getLabelWidth(), legendWidth);								
			}			
			legendHeight = Math.max(xyPlotComps.getLegend().getHeight(), legendHeight);		
			
			if (plotView.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME) {
				legendWidth = constrainLegendWidth(legendWidth, yAxisWidth, minWidth, parent.getWidth());
				realign(xyPlotComps, yAxisWidth, legendWidth,
						yAxisWidth + minWidth + (componentList.size() > 1 ? legendWidth : 0),
						Math.max(legendHeight, minHeight + xyPlotComps.getSlopeLineDisplay().getHeight()) +
						xyPlotComps.getXAxis().getHeight());
				yAxisWidth = legendWidth = 1; // reset widths
			}			
		}	
		
		
		/* Set all PreferredSizes to the desired widths we've discovered */
		if (plotView.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			legendWidth = constrainLegendWidth(legendWidth, yAxisWidth, minWidth, parent.getWidth());
			for (XYPlotComponents xyPlotComps : componentList) {
				realign(xyPlotComps, yAxisWidth, legendWidth,
						yAxisWidth + minWidth, 
						Math.max(legendHeight, minHeight + xyPlotComps.getSlopeLineDisplay().getHeight()) +
						xyPlotComps.getXAxis().getHeight());			
			}		
		}
		
		super.layoutContainer(parent);
	}
	
	private int constrainLegendWidth(int legendWidth, int yAxisWidth, int minWidth, int parentWidth) {
		/* If there's not enough room for minimum-sized plot, shrink legends */
		if (yAxisWidth + legendWidth + minWidth > parentWidth) {
			int reducedWidth = parentWidth - minWidth - yAxisWidth;
			return (reducedWidth < 0) ? 0 : reducedWidth;
		} else {
			return legendWidth;
		}
	}
	
	private void realign (XYPlotComponents xyPlotComps, int yAxisWidth, int legendWidth, int fullWidth, int fullHeight) {
		XYPlot plot  = xyPlotComps.getPlot();
		XYAxis yAxis = xyPlotComps.getYAxis();
		LegendManager legend = xyPlotComps.getLegend();
				
		yAxis.setPreferredSize(new Dimension ( yAxisWidth, 
			yAxis.getPreferredSize().height));			
		
		/* X Axis starts at left edge of Y Axis, so margin must equal Y Axis width */
		xyPlotComps.getXAxis().setStartMargin(yAxisWidth);
			
		legend.setPreferredSize(new Dimension(legendWidth,
				legend.getPreferredSize().height));
			
		plot.setPreferredSize(new Dimension(fullWidth, fullHeight));	
		
		/* Initially, let the slope & location displays be as big as they want */
		JLabel  slopeDisplay    = xyPlotComps.getSlopeLineDisplay();
		JLabel  locationDisplay = xyPlotComps.getLocationDisplay();
		letSizeFreely(slopeDisplay);
		letSizeFreely(locationDisplay);

		/* Then clamp them to content width */
		int contentWidth = plot.getContents().getWidth(); 
		if (slopeDisplay.getPreferredSize().width    > contentWidth) {
			slopeDisplay.setPreferredSize(new Dimension (contentWidth, slopeDisplay.getPreferredSize().height));
		} 
		if (locationDisplay.getPreferredSize().width > contentWidth) {
			locationDisplay.setPreferredSize(new Dimension (contentWidth, locationDisplay.getPreferredSize().height));
		} 
		
		/* Hide the location display if there is insufficient room */
		if (!slopeDisplay.getText().isEmpty() && locationDisplay.getPreferredSize().width + slopeDisplay.getPreferredSize().width > contentWidth) {
			locationDisplay.setPreferredSize(new Dimension (1,locationDisplay.getPreferredSize().height));
		} 
		
		/* Finally, request that these new preferred sizes get enforced */
		plot.revalidate();		
	}

	/**
	 * Prevent JLabels from shrinking when empty, but size freely when filled
	 * (originally from PlotDataCursor)
	 */
	private void letSizeFreely(JLabel label) {  
		if (label.getText().isEmpty() || !label.isVisible()) {
			label.setText("Ag");
			Dimension size = label.getPreferredSize();
			size.width = 1;
			label.setText("");
			label.setPreferredSize(size);
		} else {
			label.setPreferredSize(null);
		}
	}
	

	/*
	 * Provide access to components in an XYPlot. Helpful insofar as getLegend() is 
	 * not available in XYPlot itself (legends are tacked on external to Plotter package)
	 */
	private class XYPlotComponents {
		private XYPlot            plot;
		private LegendManager     legend = null;
		private SlopeLineDisplay  slope  = null;
		private XYLocationDisplay location = null;
		
		public XYPlotComponents(XYPlot xyPlot) {
			plot = xyPlot;
			findLegend();
		}	
		
		public XYPlot getPlot() {
			return plot;
		}
		
		public XYAxis  getYAxis()  {
			return plot.getYAxis();
		}
		
		public XYAxis  getXAxis()  {
			return plot.getXAxis();
		}
		
		public JLabel getSlopeLineDisplay() {
			return slope;
		}
		
		
		public JLabel getLocationDisplay() {
			return location;
		}
		
		public LegendManager getLegend() {
			/* It may be the case that a legend has been added since 
			 * this object was created, so look for it */			 
			if (legend == null) findLegend();
			return legend;
		}
		
		private void findLegend() {
			for (Component c : plot.getComponents()) {
				if (c instanceof LegendManager) {
					legend = (LegendManager) c;
				}
				if (c instanceof SlopeLineDisplay) {
					slope  = (SlopeLineDisplay) c;
				}
				if (c instanceof XYLocationDisplay) {
					location = (XYLocationDisplay) c;					
				}
			}
		}
	}
}
