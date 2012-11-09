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

import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.utils.ComponentTraverser;
import gov.nasa.arc.mct.fastplot.utils.ComponentTraverser.ComponentProcedure;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import plotter.xy.XYPlot;


public class TestStackPlotLayout {
	private JFrame frame;
	
	private PlotView thePlot;
	private SortedMap<Long, Double> smallData;
	private SortedMap<Long, Double> midData;
	private SortedMap<Long, Double> bigData;
	
	private static final String NAME = "Test display name";
	
	private static final int    PLOT_COUNT = 3;
	
	@Mock PlotViewManifestation mockPlotUser;

	@BeforeClass
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		PlotSettings settings = new PlotSettings();
		settings.setMinTime(0);
		settings.setMaxTime(1000);
		
		thePlot = new PlotView.Builder(PlotterPlot.class).
        	numberOfSubPlots(PLOT_COUNT).
        	plotSettings(settings).
        	plotLabelingAlgorithm(new AbbreviatingPlotLabelingAlgorithm()).build();
		
		thePlot.setManifestation(mockPlotUser);
		
		smallData = new TreeMap<Long, Double>();
		midData = new TreeMap<Long, Double>();
		bigData = new TreeMap<Long, Double>();
		
		for (long i = 0; i < 1000; i++) {
			smallData.put(i, 1.0 + (double) i / 1000);
			midData.put(i, 1.0 + (double) i);
			bigData.put(i, 1.0 + (double) i * 1000);
		}

		String suffix = " extra data";
		for (int i = 0; i < PLOT_COUNT; i++) {
			getSubPlot(i).addDataSet("test" + i, Color.PINK, NAME + suffix);
			suffix += suffix; //Keep getting longer
		}
			
		getSubPlot(0).addData("test0", smallData);
		getSubPlot(1).addData("test1", midData);
		getSubPlot(2).addData("test2", bigData);
		

		SwingUtilities.invokeAndWait( new Runnable() {
			public void run() {
				frame = new JFrame("Test StackPlotLayout");
				
				frame.getContentPane().add(thePlot.plotPanel);
				frame.setSize(600, 600);
				frame.setVisible(true);
				
				frame.validate();
			}
		});

		
	}
	
	@AfterClass
	public void tearDown() {
		frame.dispose();
	}
	
	private AbstractPlottingPackage getSubPlot(int n) {
		return thePlot.getSubPlots().get(n);
	}
	
	@Test
	public void testYAxisSize() throws Exception {
		final ArrayList<Integer> widths = new ArrayList<Integer>();
		
		SwingUtilities.invokeAndWait( new Runnable() {
			public void run() {
				frame.validate();
				ComponentTraverser.traverse(frame, new ComponentProcedure() {
					@Override
					public void run(Component c) {				
						if (c instanceof XYPlot) {
							widths.add(((XYPlot) c).getYAxis().getWidth());
							widths.add(((XYPlot) c).getXAxis().getStartMargin());
						}
					}		
				});
			}
		});
		
		/* All Y axes should be the same width as each other, and as 
		 * all x axis start margins */
		Assert.assertEquals(widths.size(), PLOT_COUNT * 2);
		for (Integer w : widths) {
			Assert.assertEquals(w, widths.get(0));
		}		
	}
	
	@Test
	public void testContentsWidth() throws Exception {
		final ArrayList<Integer> widths = new ArrayList<Integer>();
				
		SwingUtilities.invokeAndWait( new Runnable() {
			public void run() {
				frame.validate();
				ComponentTraverser.traverse(frame, new ComponentProcedure() {
					@Override
					public void run(Component c) {				
						if (c instanceof XYPlot) {
							widths.add(((XYPlot) c).getContents().getWidth());
						}
					}		
				});
			}
		});
		
		Assert.assertEquals(widths.size(), PLOT_COUNT);
		for (Integer w : widths) {
			Assert.assertEquals(w, widths.get(0));
		}		
	}
	
	@Test
	public void testLegendSize() throws Exception {
		final ArrayList<Integer> widths = new ArrayList<Integer>();
				
		SwingUtilities.invokeAndWait( new Runnable() {
			public void run() {
				frame.validate();
				ComponentTraverser.traverse(frame, new ComponentProcedure() {
					@Override
					public void run(Component c) {			
						if (c instanceof LegendManager) {
							widths.add(c.getWidth());
						}
					}		
				});
			}
		});
		
		Assert.assertEquals(widths.size(), PLOT_COUNT);
		for (Integer w : widths) {
			Assert.assertEquals(w, widths.get(0));
		}		
	}
	
	@Test
	public void testSmaller() throws Exception {		
		frame.setSize(500 , 600);
		frame.validate();
	
		testYAxisSize();
		testContentsWidth();
		testLegendSize();
	}
}
