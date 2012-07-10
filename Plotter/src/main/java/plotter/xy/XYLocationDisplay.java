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
package plotter.xy;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.MessageFormat;

import javax.swing.JLabel;

/**
 * Displays the X and Y coordinates of the mouse cursor on a plot.
 * @author Adam Crume
 */
public class XYLocationDisplay extends JLabel implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 1L;

	/** Formats the point for display.  The format is given two arguments, the X and Y coordinates. */
	private MessageFormat format = new MessageFormat("(X: {0}  Y: {1})");

	/** True if the mouse cursor is within the plot contents area. */
	private boolean haveMouse;


	@Override
	public void mouseDragged(MouseEvent e) {
		if(haveMouse) {
			XYPlotContents contents = (XYPlotContents) e.getSource();
			Point2D p = new Point2D.Double();
			contents.toLogical(p, e.getPoint());
			setText(format.format(new Object[] {p.getX(), p.getY()}));
		}
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		if(haveMouse) {
			XYPlotContents contents = (XYPlotContents) e.getSource();
			Point2D p = new Point2D.Double();
			contents.toLogical(p, e.getPoint());
			setText(format.format(new Object[] {p.getX(), p.getY()}));
		}
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// ignore
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		haveMouse = true;
	}


	@Override
	public void mouseExited(MouseEvent e) {
		haveMouse = false;
		setText("");
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// ignore
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// ignore
	}


	/**
	 * Attaches the display to a plot.
	 * @param plot plot to display the coordinates of
	 */
	public void attach(XYPlot plot) {
		XYPlotContents contents = plot.getContents();
		if(contents == null) {
			throw new IllegalArgumentException("Plot does not contain an XYPlotContents component");
		}
		contents.addMouseListener(this);
		contents.addMouseMotionListener(this);
	}


	/**
	 * Returns the format used to display the point.
	 * @return the format used to display the point
	 */
	public MessageFormat getFormat() {
		return format;
	}


	/**
	 * Sets the format used to display the point.
	 * @param format the format used to display the point
	 */
	public void setFormat(MessageFormat format) {
		this.format = format;
	}
}
