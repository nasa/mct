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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import plotter.AxisLabel;
import plotter.LinearTickMarkCalculator;
import plotter.MultiLineLabelUI;
import plotter.Rotation;
import plotter.TickMarkCalculator;

/**
 * A linear XY axis is the normal case where points on the screen are linearly related to their corresponding data values.
 * @author Adam Crume
 */
public class LinearXYAxis extends XYAxis {
	private static final long serialVersionUID = 1L;

	/** Start value of the axis at the time the labels were cached. */
	private double labelCacheStart = Double.NaN;

	/** End value of the axis at the time the labels were cached. */
	private double labelCacheEnd = Double.NaN;

	/** Values of the major tick marks. */
	private double[] majorVals;

	/** Values of the minor tick marks. */
	private double[] minorVals;

	/** Calculates tick marks and labels for the axis. */
	private TickMarkCalculator tickMarkCalculator = new LinearTickMarkCalculator();

	/** Format used to display values in labels. */
	private NumberFormat format = NumberFormat.getInstance();

	/**
	 * Creates an axis.
	 * @param d dimension the axis represents
	 */
	public LinearXYAxis(XYDimension d) {
		super(d);
	}


	@Override
	public void doLayout() {
		super.doLayout();
		double start = getStart();
		double end = getEnd();
		double diff = end - start;
		XYDimension plotDimension = getPlotDimension();
		int startMargin = getStartMargin();
		int width = getWidth();
		int height = getHeight();
		int size = (plotDimension == XYDimension.X ? width : height) - startMargin - getEndMargin();

		// Labels only need to be recreated if the min or max changes.
		// Otherwise, they simply need to be repositioned.
		if(start != labelCacheStart || end != labelCacheEnd) {
			double[][] ticks = tickMarkCalculator.calculateTickMarks(this);
			majorVals = ticks[0];
			minorVals = ticks[1];
			labelCacheStart = start;
			labelCacheEnd = end;
		}

		// Calculate the physical locations of the tick marks from the logical locations.
		int[] major = new int[majorVals.length];
		int[] minor = new int[minorVals.length];
		for(int i = 0; i < major.length; i++) {
			major[i] = (int) ((majorVals[i] - start) / diff * size + .5) - 1;
		}
		for(int i = 0; i < minor.length; i++) {
			minor[i] = (int) ((minorVals[i] - start) / diff * size + .5) - 1;
		}
		setMajorTicks(major);
		setMinorTicks(minor);

		if(isShowLabels()) {
			updateLabels();
		} else {
			removeAll();
		}
	}


	/**
	 * Updates labels to match the tick marks.
	 * Labels may be added, removed, or repositioned.
	 */
	private void updateLabels() {
		int[] major = getMajorTicks();
		int width = getWidth();
		int height = getHeight();
		int startMargin = getStartMargin();
		XYDimension plotDimension = getPlotDimension();
		double diff = getEnd() - getStart();
		Component[] components = getComponents();

		// oldLabels contains the original labels that have not been reused.
		Set<AxisLabel> oldLabels = new HashSet<AxisLabel>(components.length);
		for(Component c : components) {
			oldLabels.add((AxisLabel) c);
		}

		// maxLabelError defines the largest absolute difference between a label's value and the needed value.
		// If the error is greater than this, the label is not reused.
		double maxLabelError = .01 * Math.abs(diff);
		AxisLabel[] labels = new AxisLabel[majorVals.length];
		for(int i = 0; i < labels.length; i++) {
			labels[i] = createLabel(majorVals[i], oldLabels, maxLabelError);
		}

		// Remove labels that have not been reused.
		for(AxisLabel oldLabel : oldLabels) {
			remove(oldLabel);
		}

		// Position the labels to line up with the corresponding tick marks.
		int textMargin = getTextMargin();
		boolean inverted = getEnd() < getStart();
		if(plotDimension == XYDimension.X) {
			for(int i = 0; i < major.length; i++) {
				Component label = labels[i];
				double preferredSize = label.getPreferredSize().getWidth();
				double end = preferredSize / 2;
				double start = -end;
				if(inverted) {
					if(i < major.length - 1) {
						start = Math.max(start, (major[i + 1] - major[i]) / 2);
					}
					if(i > 0) {
						end = Math.min(end, (major[i - 1] - major[i]) / 2);
					}
				} else {
					if(i > 0) {
						start = Math.max(start, (major[i - 1] - major[i]) / 2);
					}
					if(i < major.length - 1) {
						end = Math.min(end, (major[i + 1] - major[i]) / 2);
					}
				}
				double labelWidth = end - start;
				label.setSize((int) labelWidth, label.getHeight());
				label.setLocation((int) (start + startMargin + major[i]), textMargin);
			}
		} else {
			int height2 = height - startMargin;
			int width2 = width - textMargin;
			for(int i = 0; i < major.length; i++) {
				Component label = labels[i];
				double preferredSize = label.getPreferredSize().getHeight();
				double end = preferredSize / 2;
				double start = -end;
				if(inverted) {
					if(i < major.length - 1) {
						start = Math.max(start, (major[i + 1] - major[i]) / 2);
					}
					if(i > 0) {
						end = Math.min(end, (major[i - 1] - major[i]) / 2);
					}
				} else {
					if(i > 0) {
						start = Math.max(start, (major[i - 1] - major[i]) / 2);
					}
					if(i < major.length - 1) {
						end = Math.min(end, (major[i + 1] - major[i]) / 2);
					}
				}
				double labelHeight = end - start;
				int labelWidth = label.getWidth();
				label.setSize(labelWidth, (int) labelHeight);
				label.setLocation(width2 - labelWidth, (int) (height2 - major[i] - end));
			}
		}
	}


	/**
	 * Creates a label for the given value.
	 * A label may instead be reused (and removed) from the <code>oldLabels</code> set.
	 * @param value value the label displays
	 * @param oldLabels labels that may be reused
	 * @param maxLabelError largest absolute difference between a label's value and <code>value</code> that allows a label to be reused
	 * @return the label, which may be new or reused
	 */
	private AxisLabel createLabel(double value, Set<AxisLabel> oldLabels, double maxLabelError) {
		for(AxisLabel oldLabel : oldLabels) {
			// Value is the same, reuse the label.
			// It is currently assumed that if the value is close enough, the text doesn't change.
			// For well-behaved tick mark calculators and label formats, this should be true.  (Use rounding!)
			// The new text could be calculated, but that is expensive.
			if(Math.abs(value - oldLabel.getValue()) < maxLabelError) {
				oldLabels.remove(oldLabel);
				return oldLabel;
			}
		}

		AxisLabel label = new AxisLabel(value, format.format(value));
		Rotation labelRotation = getLabelRotation();
		if(labelRotation != null) {
			label.putClientProperty(Rotation.class.getName(), labelRotation);
		}
		label.setUI(MultiLineLabelUI.labelUI);
		label.setForeground(getForeground());
		label.setFont(getFont());
		add(label);
		label.setSize(label.getPreferredSize());
		return label;
	}


	@Override
	public int toPhysical(double d) {
		double min = getStart();
		double max = getEnd();
		int endMargin = getEndMargin();
		if(getPlotDimension() == XYDimension.X) {
			int width = getWidth();
			int startMargin = getStartMargin();
			return (int) ((d - min) / (max - min) * (width - startMargin - endMargin) + .5) + getStartMargin() - 1;
		} else {
			int height = getHeight() - getStartMargin();
			return height - (int) ((d - min) / (max - min) * (height - endMargin) + .5);
		}
	}


	@Override
	public double toLogical(int n) {
		double min = getStart();
		double max = getEnd();
		int endMargin = getEndMargin();
		if(getPlotDimension() == XYDimension.X) {
			int width = getWidth();
			int startMargin = getStartMargin();
			return (n - startMargin + 1) * 1.0 / (width - startMargin - endMargin) * (max - min) + min;
		} else {
			int height = getHeight() - getStartMargin() - endMargin;
			return (height - n + endMargin) * 1.0 / height * (max - min) + min;
		}
	}


	/**
	 * Sets the foreground color.
	 * Overridden to update the color of the labels.
	 * @param color the foreground color
	 */
	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		for(Component c : getComponents()) {
			c.setForeground(color);
		}
	}


	/**
	 * Sets the font.
	 * Overridden to update the font of the labels.
	 * @param font the font
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		for(Component c : getComponents()) {
			c.setFont(font);
		}
	}


	/**
	 * Returns the tick mark calculator.
	 * @return the tick mark calculator
	 */
	public TickMarkCalculator getTickMarkCalculator() {
		return tickMarkCalculator;
	}


	/**
	 * Sets the tick mark calculator.
	 * @param tickMarkCalculator the tick mark calculator
	 */
	public void setTickMarkCalculator(TickMarkCalculator tickMarkCalculator) {
		this.tickMarkCalculator = tickMarkCalculator;
	}


	/**
	 * Returns the format for the labels.
	 * @return the format for the labels
	 */
	public NumberFormat getFormat() {
		return format;
	}


	/**
	 * Sets the format for the labels.
	 * @param format the format for the labels
	 */
	public void setFormat(NumberFormat format) {
		this.format = format;
	}
}
