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

import java.awt.MouseInfo;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Responsible for detecting key, mouse and component events on the QuinCurtisPlot and
 * passing those events to the appropriate plot component for handling. 
 */
public class PlotViewActionListener implements MouseListener, ComponentListener {

	// The plot for which we're managing action events. 
	PlotterPlot plot;
	
	// Used to prevent mouse entered events being fired while the mouse is still inside 
	// the plot area. 
	boolean mouseOutsideOfPlotArea = false;

   
	public PlotViewActionListener (PlotterPlot thePlot) {
		plot = thePlot;	
		// register this class as a listener for key, component, and mouse events
		plot.getPlotView().addComponentListener(this);
		plot.getPlotView().addMouseListener(this);
		if(!plot.getPlotView().contains(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y)) {
			mouseOutsideOfPlotArea = true; 
		} else {
			mouseOutsideOfPlotArea = false; 
		}
		
	}
		
	@Override
	public void componentHidden(ComponentEvent e) {
		// do nothing.	
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// Size has changed so the plot needs to be laid out again. 
		plot.calculatePlotAreaLayout();
		plot.getPlotDataManager().informResizeEvent();
	}
	
	@Override
	public void componentShown(ComponentEvent e) {
		plot.calculatePlotAreaLayout();	
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// do nothing.	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (mouseOutsideOfPlotArea) {
		  plot.localControlsManager.informMouseEntered();
		  mouseOutsideOfPlotArea = false;
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Suppress if mouse has not exited plotview window but rather just
		// entered another JComponent on the window. 
		
		if(!plot.getPlotView().contains(e.getX(), e.getY())) {
		  plot.localControlsManager.informMouseExited();
		  mouseOutsideOfPlotArea = true; 
		}
		
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// do nothing.
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// do nothing. 
	}

	/**
	 * @return the mouseOutsideOfPlotArea
	 */
	public boolean isMouseOutsideOfPlotArea() {
		return mouseOutsideOfPlotArea;
	}

}
