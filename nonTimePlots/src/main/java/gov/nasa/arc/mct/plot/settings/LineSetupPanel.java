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
package gov.nasa.arc.mct.plot.settings;

import gov.nasa.arc.mct.plot.settings.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.plot.settings.PlotConstants.PlotLineDrawingFlags;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;


/**
 * This class defines the UI for the Plot Configuration Panel
 */

public class LineSetupPanel extends PlotSettingsSubPanel {
	private static final long serialVersionUID = 4960389368454892861L;

	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
                               ResourceBundle.getBundle("gov.nasa.arc.mct.fastplot.view.Bundle");

	private static final int BEHAVIOR_CELLS_X_PADDING = 18;
	
	/*
	 * Plot line setup panel controls
	 */
	private JLabel       drawLabel;
	private JRadioButton linesOnly;
	private JRadioButton markersAndLines;
	private JRadioButton markersOnly;
	
	private JLabel       connectionLineTypeLabel;
	private JRadioButton direct;
	private JRadioButton step;
	
	private PlotLineDrawingFlags cachedFlags;
	private PlotLineConnectionType cachedConnectionType;
	
	public LineSetupPanel() {
		drawLabel = new JLabel(BUNDLE.getString("Draw.label"));
		linesOnly = new JRadioButton(BUNDLE.getString("LinesOnly.label"));
		markersAndLines = new JRadioButton(BUNDLE.getString("MarkersAndLines.label"));
		markersOnly = new JRadioButton(BUNDLE.getString("MarkersOnly.label"));
		
		connectionLineTypeLabel = new JLabel(BUNDLE.getString("ConnectionLineType.label"));
		direct = new JRadioButton(BUNDLE.getString("Direct.label"));
		step = new JRadioButton(BUNDLE.getString("Step.label"));
		direct.setToolTipText(BUNDLE.getString("Direct.tooltip"));
		step.setToolTipText(BUNDLE.getString("Step.tooltip"));
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipady = 4;
		gbc.ipadx = BEHAVIOR_CELLS_X_PADDING;
		gbc.anchor = GridBagConstraints.WEST;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth  = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		add(drawLabel, gbc);
		gbc.gridy++;
		add(linesOnly, gbc);
		gbc.gridy++;
		add(markersAndLines, gbc);
		gbc.gridy++;
		add(markersOnly, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 4;
		gbc.fill = GridBagConstraints.VERTICAL;
		add(new JSeparator(JSeparator.VERTICAL), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth  = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		add(connectionLineTypeLabel, gbc);
		// Order direct / step such that defined default goes first
		for (JComponent option : 
			(PlotLineGlobalConfiguration.getDefaultConnectionType().equals(PlotLineConnectionType.DIRECT) ? 
					new JComponent[] {direct,step} : new JComponent[] {step, direct})) {
			gbc.gridy++;
			add(option, gbc);
		}
				
		ButtonGroup drawGroup = new ButtonGroup();
		drawGroup.add(linesOnly);
		drawGroup.add(markersAndLines);
		drawGroup.add(markersOnly);

		ButtonGroup connectionGroup = new ButtonGroup();
		connectionGroup.add(direct);
		connectionGroup.add(step);

		ActionListener disabler = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 updateConnectionLineControls();
			}			
		};
		
		linesOnly.addActionListener(disabler);
		markersAndLines.addActionListener(disabler);
		markersOnly.addActionListener(disabler);

		for (Component c : getComponents()) {
			if (c instanceof AbstractButton) {
				((AbstractButton) c).addActionListener(this); // Trigger callbacks for any action
			}
		}
	}
	
	
	
	private void updateConnectionLineControls() {
		boolean linesShowing = !markersOnly.isSelected();
		 connectionLineTypeLabel.setEnabled(linesShowing);
		 direct.setEnabled(linesShowing);
		 step.setEnabled(linesShowing);
	}

	private PlotLineDrawingFlags getSelectedDrawingFlags() {
		boolean drawLines   = linesOnly.isSelected()   || markersAndLines.isSelected();
		boolean drawMarkers = markersOnly.isSelected() || markersAndLines.isSelected();
		return new PlotLineDrawingFlags(drawLines, drawMarkers);
	}
	
	private PlotLineConnectionType getSelectedConnectionType() {
		if (direct.isSelected()) {
			return PlotLineConnectionType.DIRECT;
		} else if (step.isSelected()) {
			return PlotLineConnectionType.STEP_X_THEN_Y;
		} else {
			return null; // TODO: Log the impossible state?
		}
	}


	@Override
	public void populate(PlotConfiguration settings) {
		cachedFlags = getSelectedDrawingFlags();
		settings.setPlotLineDraw(cachedFlags);
		cachedConnectionType = getSelectedConnectionType();
		settings.setPlotLineConnectionType(cachedConnectionType);
	}



	@Override
	public void reset(PlotConfiguration settings, boolean hard) {
		if (hard) {
			boolean drawLines   = settings.getPlotLineDraw().drawLine();
			boolean drawMarkers = settings.getPlotLineDraw().drawMarkers();
			
			linesOnly.setSelected(drawLines && !drawMarkers);
			markersOnly.setSelected(!drawLines && drawMarkers);
			markersAndLines.setSelected(drawLines && drawMarkers);
			
			cachedFlags = new PlotLineDrawingFlags(drawLines, drawMarkers);
			cachedConnectionType = settings.getPlotLineConnectionType();
			
			direct.setSelected(cachedConnectionType == PlotLineConnectionType.DIRECT);
			step.setSelected(cachedConnectionType == PlotLineConnectionType.STEP_X_THEN_Y);
		}		
	}



	@Override
	public boolean isDirty() {
		PlotLineDrawingFlags selected = getSelectedDrawingFlags();
		return cachedFlags.drawLine()    != selected.drawLine()    ||
		       cachedFlags.drawMarkers() != selected.drawMarkers() ||
		       cachedConnectionType      != getSelectedConnectionType();
	}

	@Override
	public boolean isValidated() {
		return true;
	}
}
