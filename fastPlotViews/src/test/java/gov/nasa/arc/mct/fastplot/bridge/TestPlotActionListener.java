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

import gov.nasa.arc.mct.fastplot.view.Axis;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.SpringLayout;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import plotter.xy.XYPlot;

public class TestPlotActionListener {
	
	PlotterPlot plot;
   
	XYPlot chartView;
	PlotViewActionListener listener;
	@Mock
	TestLocalControlsManager localControlsManager;
	@Mock
	PlotDataCursor dataCursor;
	@Mock
	PlotAbstraction plotAbstraction;
	
	@BeforeSuite
	public void setup() {
		MockitoAnnotations.initMocks(this);
		plot = new PlotterPlot();
		chartView = new XYPlot();
		chartView.setLayout(new SpringLayout());
		plot.setPlotView(chartView);
		listener = new PlotViewActionListener(plot);
		localControlsManager = new TestLocalControlsManager(plot);
		localControlsManager.setupLocalControlManager();
		plot.localControlsManager = localControlsManager;
		plot.dataCursor = dataCursor;
		plot.setPlotAbstraction(plotAbstraction);
		Mockito.when(plotAbstraction.getTimeAxis()).thenReturn(new Axis());
	}


	@Test
	public void testMouseEnteredMouseExited() {
		  Rectangle plotRectangle = new Rectangle(0,0,100,100);
		  plot.getPlotView().setBounds(plotRectangle);
		  
		  MouseEvent mouseEvent = new MouseEvent(chartView, 0, 0, 0, (int) plotRectangle.getMaxX() + 1, 0, 0, false);
		  listener.mouseExited(mouseEvent);
		  Assert.assertTrue(listener.mouseOutsideOfPlotArea);
		  	  
		  mouseEvent = new MouseEvent(chartView, 0, 0, 0, (int) plotRectangle.getMinX(), (int) plotRectangle.getMinY(), 0, false);
		  listener.mouseEntered(mouseEvent);
		  Assert.assertFalse(listener.mouseOutsideOfPlotArea);
		  
		  // Really do not exit (simulates going over a button etc). 
		  mouseEvent = new MouseEvent(chartView, 0, 0, 0, (int) plotRectangle.getMinX() + 1, (int) plotRectangle.getMinY() + 1, 0, false);
		  listener.mouseExited(mouseEvent);
		  Assert.assertFalse(listener.mouseOutsideOfPlotArea);
		  
		  // Now really exit.
		  mouseEvent = new MouseEvent(chartView, 0, 0, 0, (int) plotRectangle.getMaxX() + 1, (int) plotRectangle.getMinY() + 1, 0, false);
		  listener.mouseExited(mouseEvent);
		  Assert.assertTrue(listener.mouseOutsideOfPlotArea);
		  
	}
	   
	   @Test
	   public void testMouseClicked() {
		   // for coverage as this is a do nothing operation. 
		   MouseEvent mouseEvent = new MouseEvent(chartView, 0, 0, 0, 0, 0, 0, false);
		   listener.mouseClicked(mouseEvent);
	   }
	   
	   @Test
	   public void testMousePressed() {
		   // for coverage as this is a do nothing operation. 
		   MouseEvent mouseEvent = new MouseEvent(chartView, 0, 0, 0, 0, 0, 0, false);
		   listener.mousePressed(mouseEvent);
	   }
	   
	   @Test
	   public void  testMouseReleased() {
		   // for coverage as this is a do nothing operation. 
		   MouseEvent mouseEvent = new MouseEvent(chartView, 0, 0, 0, 0, 0, 0, false);
		   listener.mouseReleased(mouseEvent);
	   }
	
	static class TestLocalControlsManager extends PlotLocalControlsManager {

		boolean shiftState = false;
		boolean altState = false;
		boolean ctlState = false;
		
		public TestLocalControlsManager(PlotterPlot thePlot) {
			super(thePlot);
		}
		
		@Override
		public void informShiftKeyState(boolean state) {
			shiftState = state;
		}
		
		@Override
		public void informAltKeyState(boolean state) {
			altState = state;
		}
		
		@Override
		public void informCtlKeyState(boolean state) {
			ctlState = state;
		}
		
		@Override
		public void setupLocalControlManager() {
			createLocalControlButtons();
		}
		
		
	}
}
