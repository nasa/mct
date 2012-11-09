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
import gov.nasa.arc.mct.fastplot.view.Pinnable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.GregorianCalendar;

import plotter.xy.XYAxis;
import plotter.xy.XYMarkerLine;
import plotter.xy.XYPlotContents;

/**
 * Manages the time sync line that appears on plots. 
 * Each instance of this class manages the sync line on a single plot or, in the case of stack plots, a single subplot.
 */
public class PlotTimeSyncLine implements MouseListener, MouseMotionListener{
	private PlotterPlot plot;
	private XYMarkerLine timeSyncLinePlot;

	private GregorianCalendar syncTime;

	private boolean mouseDown;

	public PlotTimeSyncLine(PlotterPlot thePlot) {
		plot = thePlot;
		plot.getPlotView().addMouseListener(this);	
		plot.getPlotView().addMouseMotionListener(this);
	}
	
	public boolean inTimeSyncMode() {
		return timeSyncLinePlot != null;
	}

	private GregorianCalendar getTime(int x, int y) {
		// Adjust for left edge of YAxis - otherwise, legends throw off reference point
 		Point2D location = new Point2D.Double(x, y);

		// convert from location within the JPanel to location within the plot axis
		plot.getPlotView().toLogical(location, location);

		GregorianCalendar clickTime;
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			clickTime = new GregorianCalendar();
			clickTime.setTimeInMillis((long) location.getX());
		} else {
			clickTime = new GregorianCalendar();
			clickTime.setTimeInMillis((long) location.getY());
		}
		return clickTime;
	}

	/**
	 * Returns true if a time sync line is visible on the plot. False otherwise.
	 * @return
	 */
	boolean timeSyncLineVisible() {
		if (timeSyncLinePlot!=null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Draws a time sync line on the plot at the specified time. We take the approach of plotting the time
	 * sync line like another data series in preference to using Quinn-Curtis' line drawing function. 
	 * @param time at which to draw the time sync line.
	 */
	void drawTimeSyncLineAtTime(GregorianCalendar time) {
		assert time != null;

		// Peg timesync line to the plot area by setting the time
		// parameter to be on the plot max or plot min.
        if (time.before(plot.getCurrentTimeAxisMin())) {
        	time = plot.getCurrentTimeAxisMin();
        } else if (time.after(plot.getCurrentTimeAxisMax())) {
        	time = plot.getCurrentTimeAxisMax();
        }
	
		syncTime = time;

		if(timeSyncLinePlot == null) {
			AbstractAxis timeAxis = plot.getTimeAxis();
			if (timeAxis instanceof XYAxis) { // Only decorate time-like axes; TODO: move to AbstractAxis?
				timeSyncLinePlot = new XYMarkerLine((XYAxis) timeAxis, time.getTimeInMillis());
				timeSyncLinePlot.setForeground(PlotConstants.TIME_SYNC_LINE_COLOR);
				XYPlotContents contents = plot.getPlotView().getContents();
				contents.add(timeSyncLinePlot);
				contents.revalidate();
			}
		} else {
			timeSyncLinePlot.setValue(time.getTimeInMillis());
		}
	}
	
	/**
	 * Remove the time sync line from the plot.
	 */
	void removeTimeSyncLine() {
		if (timeSyncLinePlot != null) {
			plot.setUserOperationLockedState(false);
			XYPlotContents contents = plot.getPlotView().getContents();
			contents.remove(timeSyncLinePlot);
			contents.repaint(); // TODO: Only repaint the relevant portion
//			plot.refreshDisplay();
			timeSyncLinePlot = null;
			syncTime = null;
		}
	}
	
	/**
	 * Let the timesync line know that the shift key was pressed.
	 */
	public void informShiftKeyState(boolean state) {
		// This mouseDown stuff is a hack so that only one time sync line in a PlotView will change the global time sync state.
		if(timeSyncLinePlot != null && mouseDown) {
			assert syncTime != null;
			if(state) {
				plot.getPlotAbstraction().initiateGlobalTimeSync(syncTime);
			} else {
				GregorianCalendar time = syncTime;
				Pinnable pin = plot.getPlotAbstraction().createPin();
				pin.setPinned(true);
				plot.notifyGlobalTimeSyncFinished();
				syncTime = time;
				plot.getPlotAbstraction().showTimeSyncLine(syncTime);
				pin.setPinned(false);
			}
		}
	}
	
	/**
	 * Respond to a mouse drag event by moving the time sync line.
	 * @param e mouse drag event.
	 */
	private void dragTimeSyncLine(MouseEvent e) {
		if (plot.isInitialized()) {
			int x = e.getX();
			int y = e.getY();

			syncTime = getTime(x, y);
			plot.setUserOperationLockedState(true);
	
			// If the shift key modifier is present, initiate the time sync mode on the workstation.
			if (e.isShiftDown()) {
				plot.getPlotAbstraction().updateGlobalTimeSync(syncTime);
			} else {
				plot.getPlotAbstraction().showTimeSyncLine(syncTime);
			}
		}
	}

	private void processEndTimeSyncLineMouseEvent() {
		if (plot.isInitialized()) {
			plot.notifyGlobalTimeSyncFinished();
		}
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// do nothing.
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	   // do nothing.
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		plot.getPlotView().requestFocus();

		if (plot.isInitialized() && !plot.isUserOperationsLocked() ) {
			// Test event is inside the time axis area.
			int x = e.getX();
			int y = e.getY();
			Rectangle2D plotRect = plot.getPlotView().getContents().getBounds();
			boolean drawLine = false;
			if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
				double labelHeight = plot.getXAxisLabelHeight();
				if (x >= (int) plotRect.getMinX() &&
						x <= (int) plotRect.getMaxX() &&
						y >= (int) plotRect.getMaxY() &&
						y <= (int) (plotRect.getMaxY() + labelHeight)) {
					drawLine = true;
				}
			} else {
				double labelWidth = plot.getYAxisLabelWidth();
				if (y >= (int) plotRect.getMinY() &&
						y <= (int) plotRect.getMaxY() &&
						x <= (int) plotRect.getMinX() &&
						x >= (int) (plotRect.getMinX() - labelWidth)) {
					drawLine = true;
				}
			}	
			if(drawLine) {
				mouseDown = true;
				syncTime = getTime(x, y);
				plot.setUserOperationLockedState(true);
		
				// If the shift key modifier is present, initiate the time sync mode on the workstation. 
				if (e.isShiftDown()) {
					plot.initiateGlobalTimeSync(syncTime);
				} else {
					plot.getPlotAbstraction().showTimeSyncLine(syncTime);
				}
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
		processEndTimeSyncLineMouseEvent();
	}
  
	@Override
	public void mouseDragged(MouseEvent e) {
		if (timeSyncLinePlot != null) {
		  dragTimeSyncLine(e);
     	}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// do nothing
	}
}
