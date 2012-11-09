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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;

import plotter.xy.LinearXYAxis;
import plotter.xy.XYDimension;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

public class TestPlotDataSeriesWithCompression {

	
	@Mock 
	private PlotterPlot mockPlot;
	
	@Mock
	private PlotDataManager mockDataManager;
	@Mock
	private XYPlot mockPlotView;
	@Mock
	private PlotLimitManager mockLimitManager;

	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockPlot.setPlotDataManager(mockDataManager);
		mockPlot.compressionIsEnabled = true;
		mockPlot.setPlotView(mockPlotView);
		mockPlot.setLimitManager(mockLimitManager);
		Mockito.when(mockPlot.isCompressionEnabled()).thenReturn(true);
		Mockito.when(mockPlotView.getContents()).thenReturn(new XYPlotContents());
		Mockito.when(mockPlotView.getXAxis()).thenReturn(new LinearXYAxis(XYDimension.X));
		Mockito.when(mockPlotView.getYAxis()).thenReturn(new LinearXYAxis(XYDimension.Y));
	}

}
