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

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import plotter.Axis;
import plotter.Rotation;

/**
 * An XY axis is an axis for an XYPlot.
 * @author Adam Crume
 */
public abstract class XYAxis extends Axis {
	private static final long serialVersionUID = 1L;

	private static final int[] EMPTY = new int[0];

	/** Specifies which dimension this axis controls. */
	private final XYDimension plotDimension;

	/** Distance in pixels of the major ticks from the min location. */
	private int[] majorTicks = EMPTY;

	/** Distance in pixels of the minor ticks from the min location. */
	private int[] minorTicks = EMPTY;

	/** Number of pixels this axis overlaps the other axis. */
	private int startMargin;

	/** Number of pixels at the end that serve as margin. */
	private int endMargin;

	/** Listeners that get notified when the tick marks change. */
	private Set<TicksChangedListener> tickListeners = new HashSet<TicksChangedListener>();

	/** Specifies how the labels are oriented. */
	private Rotation labelRotation;

	/** True if the axis has its tick marks on the opposite side from the default. */
	private boolean mirrored;

	
	/**
	 * Creates an axis.
	 * @param d dimension controlled by this axis
	 */
	public XYAxis(XYDimension d) {
		assert d != null;
		this.plotDimension = d;
		setOpaque(false);
	}


	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		g = g.create();
		if(plotDimension == XYDimension.X) {
			width -= startMargin + endMargin;
			g.translate(startMargin, 0);
		} else {
			height -= startMargin + endMargin;
			g.translate(0, endMargin);
		}

		g.setColor(getForeground());

		int majorTickLength = getMajorTickLength();
		int minorTickLength = getMinorTickLength();
		if(plotDimension == XYDimension.X) {
			if(mirrored) {
				g.drawLine(0, height - 1, width - 1, height - 1);
				for(int x : getMajorTicks()) {
					g.drawLine(x, height - 1, x, height - 1 - majorTickLength);
				}
				for(int x : getMinorTicks()) {
					g.drawLine(x, height - 1, x, height - 1 - minorTickLength);
				}
			} else {
				g.drawLine(0, 0, width - 1, 0);
				for(int x : getMajorTicks()) {
					g.drawLine(x, 0, x, majorTickLength);
				}
				for(int x : getMinorTicks()) {
					g.drawLine(x, 0, x, minorTickLength);
				}
			}
		} else {
			if(mirrored) {
				g.drawLine(0, height - 1, 0, 0);
				for(int y : getMajorTicks()) {
					g.drawLine(majorTickLength, height - 1 - y, 0, height - 1 - y);
				}
				for(int y : getMinorTicks()) {
					g.drawLine(minorTickLength, height - 1 - y, 0, height - 1 - y);
				}
			} else {
				g.drawLine(width - 1, height - 1, width - 1, 0);
				for(int y : getMajorTicks()) {
					g.drawLine(width - 1 - majorTickLength, height - 1 - y, width - 1, height - 1 - y);
				}
				for(int y : getMinorTicks()) {
					g.drawLine(width - 1 - minorTickLength, height - 1 - y, width - 1, height - 1 - y);
				}
			}
		}
	}


	/**
	 * Converts a logical coordinate to a physical one.
	 * @param d logical coordinate
	 * @return physical coordinate
	 */
	public abstract int toPhysical(double d);


	/**
	 * Converts a physical coordinate to a logical one.
	 * @param n physical coordinate
	 * @return logical coordinate
	 */
	public abstract double toLogical(int n);


	/**
	 * Returns the dimension this axis controls.
	 * @return the dimension this axis controls
	 */
	public XYDimension getPlotDimension() {
		return plotDimension;
	}


	/**
	 * Returns the number of pixels at the start that serve as margin.
	 * @return margin, in pixels
	 */
	public int getStartMargin() {
		return startMargin;
	}


	/**
	 * Sets the number of pixels at the start that serve as margin.
	 * @param startMargin margin, in pixels
	 */
	public void setStartMargin(int startMargin) {
		this.startMargin = startMargin;
	}


	/**
	 * Returns the number of pixels at the end that serve as margin.
	 * @return margin, in pixels
	 */
	public int getEndMargin() {
		return endMargin;
	}


	/**
	 * Sets the number of pixels at the end that serve as margin.
	 * @param endMargin margin, in pixels
	 */
	public void setEndMargin(int endMargin) {
		this.endMargin = endMargin;
	}


	/**
	 * Do not modify the returned array.
	 * @return distance of the major tick marks from the beginning of the axis, in pixels
	 */
	public int[] getMajorTicks() {
		return majorTicks;
	}


	/**
	 * Sets the major tick marks.
	 * All {@link TicksChangedListener}s are notified.
	 * @param majorTicks physical locations of the major tick marks
	 */
	protected void setMajorTicks(int[] majorTicks) {
		assert majorTicks != null;
		this.majorTicks = majorTicks;
		for(TicksChangedListener listener : tickListeners) {
			listener.ticksChanged(this);
		}
	}


	/**
	 * Do not modify the returned array.
	 * @return distance of the minor ticks from the beginning of the axis, in pixels
	 */
	public int[] getMinorTicks() {
		return minorTicks;
	}


	/**
	 * Sets the locations of the minor ticks.
	 * @param minorTicks physical locations of the minor tick marks
	 */
	protected void setMinorTicks(int[] minorTicks) {
		assert minorTicks != null;
		this.minorTicks = minorTicks;
	}


	/**
	 * Adds a tick listener.
	 * @param listener listener to add
	 */
	public void addTickListener(TicksChangedListener listener) {
		tickListeners.add(listener);
	}


	/**
	 * Returns the label rotation.
	 * @return the label rotation
	 */
	public Rotation getLabelRotation() {
		return labelRotation;
	}


	/**
	 * Sets the label rotation.
	 * @param labelRotation the label rotation
	 */
	public void setLabelRotation(Rotation labelRotation) {
		this.labelRotation = labelRotation;
	}


	/**
	 * Returns true if the axis is mirrored.
	 * @return true if the axis is mirrored
	 */
	public boolean isMirrored() {
		return mirrored;
	}


	/**
	 * Sets whether or not the axis is mirrored.
	 * @param mirrored true if the axis should be mirrored
	 */
	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
	}



	/**
	 * A TicksChangedListener gets notified when tick marks change.
	 * @author Adam Crume
	 */
	public static interface TicksChangedListener {
		/**
		 * Notifies this listener that an axis's tick marks changed.
		 * @param a axis whose tick marks changed
		 */
		public void ticksChanged(XYAxis a);
	}
}
