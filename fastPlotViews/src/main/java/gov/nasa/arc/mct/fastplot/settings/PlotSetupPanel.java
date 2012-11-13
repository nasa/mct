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
package gov.nasa.arc.mct.fastplot.settings;

import gov.nasa.arc.mct.fastplot.bridge.AbstractAxis;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.policy.PlotViewPolicy;
import gov.nasa.arc.mct.fastplot.settings.controls.PlotSettingsAxisGroup;
import gov.nasa.arc.mct.fastplot.settings.controls.PlotSettingsCheckBox;
import gov.nasa.arc.mct.fastplot.settings.controls.PlotSettingsComboBox;
import gov.nasa.arc.mct.fastplot.settings.controls.PlotSettingsRadioButtonGroup;
import gov.nasa.arc.mct.fastplot.view.IconLoader;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the UI for the Plot Configuration Panel
 */

public class PlotSetupPanel extends PlotSettingsPanel {
	private static final long serialVersionUID = 6158825155688815494L;
	
	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
                               ResourceBundle.getBundle("gov.nasa.arc.mct.fastplot.view.Bundle");
    
    private final static Logger logger = LoggerFactory.getLogger(PlotSetupPanel.class);



    private static final int INNER_PADDING = 5;
    private static final int INNER_PADDING_TOP = 5;

	private static final Border TOP_PADDED_MARGINS = BorderFactory.createEmptyBorder(5, 0, 0, 0);

	private static final Border CONTROL_PANEL_MARGINS = BorderFactory.createEmptyBorder(INNER_PADDING_TOP, INNER_PADDING, INNER_PADDING, INNER_PADDING);
	private static final Border SETUP_AND_BEHAVIOR_MARGINS = BorderFactory.createEmptyBorder(0, INNER_PADDING, INNER_PADDING, INNER_PADDING);

	
	private static final String DATE_FORMAT = "D/HH:mm:ss";

	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

	static GregorianCalendar ZERO_TIME_SPAN_CALENDAR = new GregorianCalendar();
	static {
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.DAY_OF_YEAR, 1);
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.MINUTE, 0);
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.SECOND, 0);
	}
	private static Date ZERO_TIME_SPAN_DATE = new Date();
	static {
		// Sets value to current Year and time zone
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		ZERO_TIME_SPAN_DATE.setTime(cal.getTimeInMillis());
	}


	// Maintain link to the plot view this panel is supporting.
	private PlotViewManifestation plotViewManifestation;
	
    /*
     * Non-time Axis fields
     */
	private StillPlotImagePanel imagePanel;




    /* ================================================
	 * Main Constructor for Plot Settings Control panel
	 * ================================================
	 */
	public PlotSetupPanel(PlotViewManifestation plotMan) {
		// This sets the display of date/time fields that use this format object to use GMT
		dateFormat.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		
		// store reference to the plot
		plotViewManifestation = plotMan;	
		// Create controller for this panel.

		setLayout(new BorderLayout());
		setBorder(CONTROL_PANEL_MARGINS);		

		add(getInitialSetupPanel(), BorderLayout.WEST);

		// Set the initial value of the Time Min Auto value ("Now")
		GregorianCalendar nextTime = new GregorianCalendar();
		nextTime.setTimeInMillis(plotViewManifestation.getCurrentMCTTime());
		nextTime.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		if (nextTime.getTimeInMillis() == 0.0) {
			nextTime.setTimeInMillis(plotViewManifestation.getPlot().getMinTime());
			nextTime.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		}

		instrumentNames();
		
	}



	// Assign internal names to the top level class variables
	private void instrumentNames() {
//        timeAxisMinimumsPanel.setName("timeAxisMinimumsPanel");
//        timeAxisMaximumsPanel.setName("timeAxisMaximumsPanel");
//		nonTimeAxisMaximumsPanel.setName("nonTimeAxisMaximumsPanel");
//		nonTimeAxisMinimumsPanel.setName("nonTimeAxisMinimumsPanel");
//
//        timeAxisMinAuto.setName("timeAxisMinAuto");
//        timeAxisMinAutoValue.setName("timeAxisMinAutoValue");
//        timeAxisMinManual.setName("timeAxisMinManual");
//        timeAxisMinManualValue.setName("timeAxisMinManualValue");
//
//        timeAxisMaxAuto.setName("timeAxisMaxAuto");
//        timeAxisMaxAutoValue.setName("timeAxisMaxAutoValue");
//        timeAxisMaxManual.setName("timeAxisMaxManual");
//        timeAxisMaxManualValue.setName("timeAxisMaxManualValue");
//
//		nonTimeAxisMinCurrent.setName("nonTimeAxisMinCurrent");
//		nonTimeAxisMinCurrentValue.setName("nonTimeAxisMinCurrentValue");
//		nonTimeAxisMinManual.setName("nonTimeAxisMinManual");
//		nonTimeAxisMinManualValue.setName("nonTimeAxisMinManualValue");
//		nonTimeAxisMinAutoAdjust.setName("nonTimeAxisMinAutoAdjust");
//
//		nonTimeAxisMaxCurrent.setName("nonTimeAxisMaxCurrent");
//		nonTimeAxisMaxCurrentValue.setName("nonTimeAxisMaxCurrentValue");
//		nonTimeAxisMaxManual.setName("nonTimeAxisMaxManual");
//		nonTimeAxisMaxManualValue.setName("nonTimeAxisMaxManualValue");
//		nonTimeAxisMaxAutoAdjust.setName("nonTimeAxisMaxAutoAdjust");
//
//        imagePanel.setName("imagePanel");
//        timeSystemDropdown.setName("timeSystemDropdown");
//        timeFormatDropdown.setName("timeFormatDropdown");
//
//        timeSpanValue.setName("timeSpanValue");
//        nonTimeSpanValue.setName("nonTimeSpanValue");
//		
//        xMinLabel.setName("xMinLabel");
//        xMaxLabel.setName("xMaxLabel");
//
//        xAxisAsTimeRadioButton.setName("xAxisAsTimeRadioButton");
//        yAxisAsTimeRadioButton.setName("yAxisAsTimeRadioButton");
//        xMaxAtRight.setName("xMaxAtRight");
//        xMaxAtLeft.setName("xMaxAtLeft");
//        yMaxAtTop.setName("yMaxAtTop");
//        yMaxAtBottom.setName("yMaxAtBottom");
//
//		xAxisSpanCluster.setName("xAxisSpanCluster");
//		xAxisAdjacentPanel.setName("xAxisAdjacentPanel");
//		xAxisButtonsPanel.setName("xAxisButtonsPanel");
//
//        yAxisSpanPanel.setName("ySpanPanel");
//        yMaximumsPlusPanel.setName("yMaximumsPlusPanel");
//        yMinimumsPlusPanel.setName("yMinimumsPlusPanel");
//        yAxisButtonsPanel.setName("yAxisButtonsPanel");
	}





	

	// The Initial Settings panel
	// Name change: Initial Settings is now labeled Plot Setup
	private PlotSettingsPanel getInitialSetupPanel() {
		PlotSettingsPanel initialSetup = new PlotSettingsPanel();
		initialSetup.setLayout(new BoxLayout(initialSetup, BoxLayout.Y_AXIS));
		initialSetup.setBorder(SETUP_AND_BEHAVIOR_MARGINS);

        imagePanel = new StillPlotImagePanel();

        // Start defining the top panel
		PlotSettingsSubPanel initTopPanel = getAlternateTopPanel();
        PlotSettingsSubPanel initBottomPanel = getAlternateBottomPanel();

		// Assemble the major panel: Initial Settings
        JPanel topClamp = new JPanel(new BorderLayout());
        topClamp.add(initTopPanel, BorderLayout.NORTH);
        JPanel bottomClamp = new JPanel(new BorderLayout());
        bottomClamp.add(initBottomPanel, BorderLayout.NORTH);
        JPanel sideClamp = new JPanel(new BorderLayout());
        sideClamp.add(bottomClamp, BorderLayout.WEST);

        initialSetup.add(Box.createRigidArea(new Dimension(0, INNER_PADDING)));
		initialSetup.add(topClamp);
		initialSetup.add(Box.createRigidArea(new Dimension(0, INNER_PADDING)));
		initialSetup.add(new JSeparator());
        initialSetup.add(sideClamp);

        // Instrument
        initialSetup.setName("initialSetup");
        initTopPanel.setName("initTopPanel");
        initBottomPanel.setName("initBottomPanel");
        
        topClamp.setName("topClamp");
        bottomClamp.setName("bottomClamp");

        initialSetup.addSubPanel(initTopPanel);
        initialSetup.addSubPanel(initBottomPanel);
        
		return initialSetup;
	}

	
	private PlotSettingsSubPanel getAlternateBottomPanel() {

		final AxisPanel xAxisPanel = new AxisPanel() {			
			private static final long serialVersionUID = 7880175726915727283L;

			@Override
			public void initialLayout() {
				layout.putConstraint(SpringLayout.WEST, minMaxPanel[0], 0, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.NORTH, minMaxPanel[0], 0, SpringLayout.SOUTH, spanPanel);
				
				layout.putConstraint(SpringLayout.EAST, this, 0, SpringLayout.EAST, minMaxPanel[1]);
				layout.putConstraint(SpringLayout.WEST, minMaxPanel[1], 0, SpringLayout.EAST, minMaxPanel[0]);
				layout.putConstraint(SpringLayout.NORTH, minMaxPanel[1], 0, SpringLayout.SOUTH, spanPanel);
						
				layout.putConstraint(SpringLayout.WEST, minMaxLabel[0], 0, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.NORTH, minMaxLabel[0], 0, SpringLayout.NORTH, this);
				
				layout.putConstraint(SpringLayout.EAST, minMaxLabel[1], 0, SpringLayout.EAST, this);
				layout.putConstraint(SpringLayout.NORTH, minMaxLabel[1], 0, SpringLayout.NORTH, this);
				
				layout.putConstraint(SpringLayout.SOUTH, minMaxPanel[1], 0, SpringLayout.SOUTH, minMaxPanel[0]);
				layout.putConstraint(SpringLayout.NORTH, axisTitle, 0, SpringLayout.SOUTH, minMaxPanel[0]);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, axisTitle, 0, SpringLayout.EAST, minMaxPanel[0]);
				
				layout.putConstraint(SpringLayout.NORTH, spanPanel, 0, SpringLayout.NORTH, this);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, spanPanel, 0, SpringLayout.EAST, minMaxPanel[0]);
				
				layout.putConstraint(SpringLayout.SOUTH, this, Spring.sum(
						Spring.sum(Spring.height(axisTitle), Spring.height(spanPanel)),
						Spring.max(Spring.height(minMaxPanel[0]), Spring.height(minMaxPanel[1]))
				), SpringLayout.NORTH, this);
				
			}
		};
		
		final AxisPanel yAxisPanel = new AxisPanel() {			
			private static final long serialVersionUID = 7880175726915727283L;

			@Override
			public void initialLayout() {
				layout.putConstraint(SpringLayout.WEST, minMaxPanel[0], 0, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.EAST, minMaxLabel[0], 0, SpringLayout.EAST, this);

				layout.putConstraint(SpringLayout.WEST, minMaxPanel[1], 0, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.EAST, minMaxLabel[1], 0, SpringLayout.EAST, this);

				layout.putConstraint(SpringLayout.EAST, spanPanel, 0, SpringLayout.EAST, this);
				layout.putConstraint(SpringLayout.EAST, this, Spring.max(
						Spring.sum(Spring.width(minMaxPanel[0]), Spring.width(minMaxLabel[0])),
						Spring.sum(Spring.width(minMaxPanel[1]), Spring.width(minMaxLabel[1])))
			    , SpringLayout.WEST, this);
				
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, axisTitle, 0, SpringLayout.HORIZONTAL_CENTER, this);

				layout.putConstraint(SpringLayout.NORTH, axisTitle, 0, SpringLayout.NORTH, this);
				layout.putConstraint(SpringLayout.NORTH, minMaxPanel[1], 0, SpringLayout.SOUTH, axisTitle);				
				layout.putConstraint(SpringLayout.NORTH, spanPanel, 0, SpringLayout.SOUTH, minMaxPanel[1]);
				layout.putConstraint(SpringLayout.NORTH, minMaxPanel[0], 0, SpringLayout.SOUTH, spanPanel);
				layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, minMaxPanel[0]);
				
				layout.putConstraint(SpringLayout.SOUTH, minMaxLabel[0], 0, SpringLayout.SOUTH, this);
				
			}
		};
				
		final AxisPanel zAxisPanel = new AxisPanel() {			
			private static final long serialVersionUID = 7880175726915727283L;

			@Override
			public void initialLayout() {
				layout.putConstraint(SpringLayout.WEST, minMaxPanel[0], 0, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.WEST, minMaxLabel[0], 0, SpringLayout.EAST, minMaxPanel[0]);

				layout.putConstraint(SpringLayout.WEST, minMaxPanel[1], 0, SpringLayout.WEST, minMaxPanel[0]);
				layout.putConstraint(SpringLayout.WEST, minMaxLabel[1], 0, SpringLayout.EAST, minMaxPanel[1]);
				
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, spanPanel, 0, SpringLayout.HORIZONTAL_CENTER, this);
				layout.putConstraint(SpringLayout.EAST, this, 0, SpringLayout.EAST, minMaxLabel[0]);
				
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, axisTitle, 0, SpringLayout.HORIZONTAL_CENTER, minMaxPanel[0]);

				
				layout.putConstraint(SpringLayout.NORTH, minMaxPanel[1], 0, SpringLayout.NORTH, this);				
				layout.putConstraint(SpringLayout.NORTH, spanPanel, 0, SpringLayout.SOUTH, minMaxPanel[1]);
				layout.putConstraint(SpringLayout.NORTH, minMaxPanel[0], 0, SpringLayout.SOUTH, spanPanel);
				layout.putConstraint(SpringLayout.NORTH, axisTitle, 0, SpringLayout.SOUTH, minMaxPanel[0]);
				layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, axisTitle);
				
				layout.putConstraint(SpringLayout.SOUTH, minMaxLabel[0], 0, SpringLayout.SOUTH, this);
				
			}
		};
		zAxisPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		JPanel p1 = new JPanel(); p1.add(new JLabel("Min panel"));
		JPanel p2 = new JPanel(); p2.add(new JLabel("Max panel"));

		final PlotSettingsAxisGroup timeGroup = new PlotSettingsAxisGroup(true) {
			private static final long serialVersionUID = -7135071044865840006L;

			@Override
			public void setBounds(PlotConfiguration settings, double min,
					double max) {
				settings.setMinTime((long) min);
				settings.setMaxTime((long) max);
			}

			@Override
			public double getBoundMinimum(PlotConfiguration settings) {
				return settings.getMinTime();
			}

			@Override
			public double getBoundMaximum(PlotConfiguration settings) {
				return settings.getMaxTime();
			}

			@Override
			public double getActualMinimum(PlotViewManifestation view) {
				AbstractAxis axis = view.getPlot().getPlotTimeAxis();
				return Math.min(axis.getStart(), axis.getEnd());
			}

			@Override
			public double getActualMaximum(PlotViewManifestation view) {
				AbstractAxis axis = view.getPlot().getPlotTimeAxis();
				return Math.max(axis.getStart(), axis.getEnd());
			}
		};
		
		final PlotSettingsAxisGroup nonTimeGroup = new PlotSettingsAxisGroup(false) {
			private static final long serialVersionUID = 506645956367162100L;
			@Override
			public void setBounds(PlotConfiguration settings, double min,
					double max) {
				settings.setMinNonTime(min);
				settings.setMaxNonTime(max);
			}
			@Override
			public double getBoundMinimum(PlotConfiguration settings) {
				return settings.getMinNonTime();
			}

			@Override
			public double getBoundMaximum(PlotConfiguration settings) {
				return settings.getMaxNonTime();
			}
			@Override
			public double getActualMinimum(PlotViewManifestation view) {
				return view.getMinFeedValue();
			}
			@Override
			public double getActualMaximum(PlotViewManifestation view) {
				return view.getMaxFeedValue();
			}
		};	
		final PlotSettingsAxisGroup otherNonTimeGroup = new PlotSettingsAxisGroup(false) {
			private static final long serialVersionUID = -635180790760328411L;

			@Override
			public void setBounds(PlotConfiguration settings, double min,
					double max) {
				settings.setMinDependent(min);
				settings.setMaxDependent(max);
			}		
			@Override
			public double getBoundMinimum(PlotConfiguration settings) {
				return settings.getMinDependent();
			}

			@Override
			public double getBoundMaximum(PlotConfiguration settings) {
				return settings.getMaxDependent();
			}
			
			@Override
			public double getActualMinimum(PlotViewManifestation view) {
				return view.getMinFeedValue();
			}
			@Override
			public double getActualMaximum(PlotViewManifestation view) {
				return view.getMaxFeedValue();
			}

		};
	
		timeGroup.updateFrom(plotViewManifestation);
		nonTimeGroup.updateFrom(plotViewManifestation);
		otherNonTimeGroup.updateFrom(plotViewManifestation);
		this.plotViewManifestation.addFeedCallback(new Runnable() {
			@Override
			public void run() {
				timeGroup.updateFrom(plotViewManifestation);
				nonTimeGroup.updateFrom(plotViewManifestation);
				otherNonTimeGroup.updateFrom(plotViewManifestation);
			}
		});		
		addSubPanel(timeGroup);
		addSubPanel(nonTimeGroup);
		addSubPanel(otherNonTimeGroup);
		

		timeGroup.setTitle("TIME");
		nonTimeGroup.setTitle("NON-TIME");
		otherNonTimeGroup.setTitle("DEPENDENT");
		
		PlotSettingsPanel panel = new PlotSettingsPanel() {
			private static final long serialVersionUID = 1730870211575829997L;			
			
			@Override			
			public void reset(PlotConfiguration settings, boolean hard) {
				boolean xInverted = settings.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT;
				boolean yInverted = settings.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM;
				switch (settings.getAxisOrientationSetting()) {
				case X_AXIS_AS_TIME:
					xAxisPanel.setFrom(timeGroup, xInverted);
					yAxisPanel.setFrom(nonTimeGroup, yInverted);
					break;
				case Y_AXIS_AS_TIME:
					yAxisPanel.setFrom(timeGroup, yInverted);
					xAxisPanel.setFrom(nonTimeGroup, xInverted);					
					break;
				case Z_AXIS_AS_TIME:
					zAxisPanel.setFrom(timeGroup, true);
					yAxisPanel.setFrom(otherNonTimeGroup, yInverted);
					xAxisPanel.setFrom(nonTimeGroup, xInverted);					
					break;					
				}
				zAxisPanel.setVisible(settings.getAxisOrientationSetting() == AxisOrientationSetting.Z_AXIS_AS_TIME);
				super.reset(settings, hard);
				revalidate();
			}			
		};
		panel.setLayout(new GridBagLayout());
		panel.setBorder(TOP_PADDED_MARGINS);
		        

        // The title label for (TIME) or (NON-TIME)
        GridBagConstraints gbc = new GridBagConstraints();        

        // The Y Axis controls panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        // To align the "Min" or "Max" label with the bottom of the static plot image,
        // add a vertical shim under the Y Axis bottom button set and "Min"/"Max" label.
        gbc.insets = new Insets(2, 0, 10, 2); 
        panel.add(yAxisPanel, gbc);

        // The Z Axis controls panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        // To align the "Min" or "Max" label with the bottom of the static plot image,
        // add a vertical shim under the Y Axis bottom button set and "Min"/"Max" label.
        gbc.insets = new Insets(2, 0, 10, 2); 
        panel.add(zAxisPanel, gbc);

        
        // The static plot image
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 3;
        panel.add(imagePanel, gbc);

        // The X Axis controls panel
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 8, 0, 0);
        panel.add(xAxisPanel, gbc);
        
        // Instrument
        zAxisPanel.setName("zAxisPanelSet");
        yAxisPanel.setName("yAxisPanelSet");
        xAxisPanel.setName("xAxisPanelSet");
		
        panel.revalidate();
        
        return panel;
	}
	
	private PlotSettingsSubPanel getAlternateTopPanel() {
		PlotSettingsPanel panel = new PlotSettingsPanel();
		
		PlotSettingsSubPanel subPanels[] = {
			new TimeSetupPanel(),
			new XYSetupPanel(false),
			new XYSetupPanel(true),
			new GroupingSetupPanel()			
		};
				
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 0.0;
        
		for (JComponent c : subPanels) {
			gbc.insets = new Insets(0, 0, 0, 0);			
			gbc.fill = GridBagConstraints.NONE;	
			if (c == subPanels[subPanels.length - 1]) gbc.weightx = 1;
			gbc.anchor = GridBagConstraints.NORTHWEST;
			panel.add(c, gbc);
			gbc.gridx++;
			
			if (c == subPanels[subPanels.length - 1]) break; // No final separator
			gbc.insets = new Insets(0, 5, 0, 5);
			gbc.fill = GridBagConstraints.VERTICAL;
			panel.add(new JSeparator(JSeparator.VERTICAL), gbc);
			gbc.gridx++;
		}
		
		return panel;
	}
	
	private class TimeSetupPanel extends PlotSettingsPanel {
		private static final long serialVersionUID = -2628686516154128194L;
		
		public TimeSetupPanel() {
			// Time Systems and Formats
			JPanel timePanel = new PlotSettingsPanel();
			timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS)); 
			timePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			
			JPanel timeSystemPanel = new PlotSettingsPanel();
			timeSystemPanel.setLayout(new BoxLayout(timeSystemPanel, BoxLayout.X_AXIS)); 
			timeSystemPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 0));
			
			JPanel timeFormatsPanel = new PlotSettingsPanel();
			timeFormatsPanel.setLayout(new BoxLayout(timeFormatsPanel, BoxLayout.X_AXIS)); 
			timeFormatsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 0));
			
			timePanel.add(timeSystemPanel);
			timePanel.add(timeFormatsPanel);
			
			final String [] systemChoices = getComponentSpecifiedTimeSystemChoices();
			PlotSettingsComboBox<String> timeSystemComboBox = new PlotSettingsComboBox<String>(systemChoices) {
				private static final long serialVersionUID = 1765748658974789785L;

				@Override
				public void populate(PlotConfiguration settings) {
					settings.setTimeSystemSetting(getSelection());
				}

				@Override
				public void reset(PlotConfiguration settings, boolean hard) {
					if (hard && systemChoices.length > 0) {
						String system = settings.getTimeSystemSetting();					
						setSelection(!system.isEmpty() ? system : systemChoices[0]);					
					}
				}				
			};
			if (systemChoices.length < 1) {
				timeSystemComboBox.setFocusable(false);
				timeSystemComboBox.setEnabled(false);
			}
			
			timeSystemPanel.add(new JLabel(BUNDLE.getString("TimeSystem.label")));
			timeSystemPanel.add(timeSystemComboBox);
			addSubPanel(timeSystemComboBox);
						
			String[] formatChoices = getComponentSpecifiedTimeFormatChoices();
			for (int i = 0; i < formatChoices.length; i++) { // Remove formatting marks
				formatChoices[i] = formatChoices[i].replaceAll("'", "");
			}
			PlotSettingsComboBox<String> timeFormatComboBox = new PlotSettingsComboBox<String>(formatChoices) {				
				private static final long serialVersionUID = 4893624658379045632L;

				@Override
				public void populate(PlotConfiguration settings) {
					settings.setTimeFormatSetting(getSelection());
				}

				@Override
				public void reset(PlotConfiguration settings, boolean hard) {
					if (hard) setSelection(settings.getTimeFormatSetting());					
				}				
			};
			if (systemChoices.length < 1) {
				timeFormatComboBox.setFocusable(false);
				timeFormatComboBox.setEnabled(false);
			}
			timeFormatComboBox.setSelection(plotViewManifestation.getPlot().getTimeFormatSetting());
			timeFormatsPanel.add(new JLabel(BUNDLE.getString("TimeFormat.label")));
			timeFormatsPanel.add(timeFormatComboBox);
			addSubPanel(timeFormatComboBox);
			
	        AxisOrientationSetting[] orientationOptions = 
	        	PlotViewPolicy.isScatterPlottable(plotViewManifestation.getManifestedComponent()) ||
	        	PlotViewPolicy.isOverlaidScatterPlottable(plotViewManifestation.getManifestedComponent()) ?
	        			AxisOrientationSetting.values() : 
	        			new AxisOrientationSetting[] {AxisOrientationSetting.X_AXIS_AS_TIME, AxisOrientationSetting.Y_AXIS_AS_TIME};
	    	PlotSettingsRadioButtonGroup<AxisOrientationSetting> timeAxisButtons = 
	    		new PlotSettingsRadioButtonGroup<AxisOrientationSetting>(orientationOptions) {
	    		private static final long serialVersionUID = 1L;

				@Override
				public void populate(PlotConfiguration settings) {
					settings.setAxisOrientationSetting(getSelection());					
				}

				@Override
				public void reset(PlotConfiguration settings, boolean hard) {
					if (hard) setSelection(settings.getAxisOrientationSetting());
				}	    		
	    	};
	    	timeAxisButtons.setText(AxisOrientationSetting.X_AXIS_AS_TIME, BUNDLE.getString("XAxisAsTime.label"));
	    	timeAxisButtons.setText(AxisOrientationSetting.Y_AXIS_AS_TIME, BUNDLE.getString("YAxisAsTime.label"));
	    	timeAxisButtons.setText(AxisOrientationSetting.Z_AXIS_AS_TIME, BUNDLE.getString("ZAxisAsTime.label"));
	    	
	    	setLayout(new GridBagLayout());
	    	GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(0,0,2,0);
			gbc.gridy = 0;
			add(timeSystemPanel, gbc);
			gbc.gridy = 1;			
			add(timeFormatsPanel, gbc);
			gbc.gridy = 2;
			add(timeAxisButtons, gbc);	
		}
	}
	
	/**
	 * Panel for selecting "Max at Top", "Max at Bottom", et cetera
	 * Used for both X and Y axes; boolean value in constructor call distinguishes
	 */
	private class XYSetupPanel extends PlotSettingsPanel {
		private static final long serialVersionUID = -4105387384633330667L;

		private boolean yAxis; //Otherwise X!		
		
		public XYSetupPanel(boolean isYAxis) {
			yAxis = isYAxis;
			
			PlotSettingsRadioButtonGroup<?> group = yAxis ? new PlotSettingsRadioButtonGroup<YAxisMaximumLocationSetting>(
					YAxisMaximumLocationSetting.values()) {
				private static final long serialVersionUID = -3941213585469079585L;

				@Override
				public void populate(PlotConfiguration settings) {
					settings.setYAxisMaximumLocation(getSelection());
				}

				@Override
				public void reset(PlotConfiguration settings, boolean hard) {
					setText(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP, BUNDLE.getString("MaxAtTop.label"));
					setText(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM, BUNDLE.getString("MaxAtBottom.label"));
					if (hard) setSelection(settings.getYAxisMaximumLocation());
				}
			} : new PlotSettingsRadioButtonGroup<XAxisMaximumLocationSetting>(
					XAxisMaximumLocationSetting.values()) {
				private static final long serialVersionUID = 2946219924543543496L;

				@Override
				public void populate(PlotConfiguration settings) {
					settings.setXAxisMaximumLocation(getSelection());
				}

				@Override
				public void reset(PlotConfiguration settings, boolean hard) {
					setText(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT, BUNDLE.getString("MaxAtLeft.label"));
					setText(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, BUNDLE.getString("MaxAtRight.label"));
					if (hard) setSelection(settings.getXAxisMaximumLocation());
				}
			};
			
			JLabel xDirTitle = new JLabel(BUNDLE.getString(yAxis ? "YAxis.label" : "XAxis.label"));

			
			
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(8,0,16,0);
			gbc.gridy = 0;
			add(xDirTitle, gbc);
			gbc.insets = new Insets(0,0,2,0);
			gbc.gridy = 1;			
			add(group, gbc);
		}
		
	}
	
	private class GroupingSetupPanel extends PlotSettingsPanel {
		private static final long serialVersionUID = 4465726647984136821L;

		public GroupingSetupPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
	        gbc.gridy = 0;
	        gbc.anchor = GridBagConstraints.NORTHWEST;
	        gbc.insets = new Insets(8,0,14,0);
	        add(new JLabel(BUNDLE.getString("StackedPlotGrouping.label")),gbc);
	        gbc.gridy = 1;
	        gbc.insets = new Insets(0,0,0,0);
	        JPanel groupCheckBox = new PlotSettingsCheckBox(BUNDLE.getString("GroupByCollection.label")) {
				private static final long serialVersionUID = 3172215382592976671L;
				@Override
				public void populate(PlotConfiguration settings) {
					settings.setOrdinalPositionForStackedPlots(!isSelected());
				}
				@Override
				public boolean getFrom(PlotConfiguration settings) {
					return !settings.getOrdinalPositionForStackedPlots();
				}	        	
	        };
	        add(groupCheckBox, gbc);
		}
	}
	
	String[] getComponentSpecifiedTimeSystemChoices() {
		return (plotViewManifestation != null) ?
				plotViewManifestation.getTimeSystemChoices() :  new String[] { PlotConstants.DEFAULT_TIME_SYSTEM };
	}

	String[] getComponentSpecifiedTimeFormatChoices() {
		return (plotViewManifestation != null) ?
				plotViewManifestation.getTimeFormatChoices() : new String[] { PlotConstants.DEFAULT_TIME_FORMAT };
	}


			
	private abstract class AxisPanel extends JPanel { 
		private static final long serialVersionUID = -6277438566623696715L;
		
		protected JPanel minMaxPanel[] = { new JPanel(), new JPanel() };
		protected JLabel minMaxLabel[] = { new JLabel(), new JLabel() };		
		protected JPanel spanPanel     = new JPanel();
		protected JLabel axisTitle     = new JLabel();
		
		protected SpringLayout layout  = new SpringLayout();
		
		public AxisPanel() {
			for (JLabel l : minMaxLabel) l.setFont(l.getFont().deriveFont(Font.BOLD));
			setLayout(layout);
			add(minMaxPanel[0]);
			add(minMaxPanel[1]);
			add(minMaxLabel[0]);
			add(minMaxLabel[1]);
			add(spanPanel);
			add(axisTitle);

		}
		
		public void clear() {
			for (JPanel p : minMaxPanel) p.removeAll();
			for (JLabel l : minMaxLabel) l.setText("");
			spanPanel.removeAll();
			axisTitle.setText("");
		}
		
		public void setFrom(PlotSettingsAxisGroup group, boolean inverted) {
			clear();
			int min = inverted ? 1 : 0;
			int max = inverted ? 0 : 1;
			minMaxPanel[min].add(group.getMinControls());
			minMaxPanel[max].add(group.getMaxControls());
			minMaxLabel[min].setText(group.getMinText());
			minMaxLabel[max].setText(group.getMaxText());
			spanPanel.add(group.getSpanControls());
			axisTitle.setText("(" + group.getTitle() + ")");
			layout = new SpringLayout();
			setLayout(layout);
			initialLayout();
			revalidate();
		}
		
		public abstract void initialLayout();
	}

    
	


	/*
	 * Focus listener for the Time axis Manual and Span fields
	 */
	class TimeFieldFocusListener extends FocusAdapter {
		// This class can be used with a null button
		private JRadioButton companionButton;

		public TimeFieldFocusListener(JRadioButton assocButton) {			
			companionButton = assocButton;
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (e.isTemporary())
				return;
			if (companionButton != null) {
				companionButton.setSelected(true);
			}
			PlotSetupPanel.this.focusGained(e);
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (e.isTemporary())
				return;
		}
	}

	/*
	 * Common action listener for the Time axis Mode buttons
	 */
	class TimeAxisModeListener implements ActionListener {
		private JTextComponent companionField;

		public TimeAxisModeListener(JTextComponent field) {
			companionField = field;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			this.actionPerformed(e);
			String content = companionField.getText();
			companionField.requestFocusInWindow();
			companionField.setSelectionStart(0);
			if (content != null) {
				companionField.setSelectionEnd(content.length());
			}
		}
	}


	class NonTimeFieldFocusListener extends FocusAdapter {
		private JRadioButton companionButton;

		public NonTimeFieldFocusListener(JRadioButton assocButton) {
			companionButton = assocButton;
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (e.isTemporary())
				return;
			companionButton.setSelected(true);
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (e.isTemporary())
				return;
		}
	}




	
	// Panel that holds the still image of a plot in the Initial Settings area
    private class StillPlotImagePanel extends PlotSettingsSubPanel {
		private static final long serialVersionUID = 8645833372400367908L;
		
		private JLabel timePicture;
		
		public StillPlotImagePanel() {
			timePicture = new JLabel("", IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_TIME_ON_X_NORMAL), JLabel.CENTER);
			add(timePicture); // default

			// Instrument
			timePicture.setName("timePicture");
		}

		@Override
		public void populate(PlotConfiguration settings) {
			// Passive - only respond to settings
		}

		@Override
		public void reset(PlotConfiguration settings, boolean hard) {
			switch (settings.getAxisOrientationSetting()) {
			case X_AXIS_AS_TIME:
				timePicture.setIcon(IconLoader.INSTANCE.getIcon(
						settings.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT ? 
						IconLoader.Icons.PLOT_TIME_ON_X_NORMAL : IconLoader.Icons.PLOT_TIME_ON_X_REVERSED ) );
				break;
			case Y_AXIS_AS_TIME:
				timePicture.setIcon(IconLoader.INSTANCE.getIcon(
						settings.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP ? 
						IconLoader.Icons.PLOT_TIME_ON_Y_NORMAL : IconLoader.Icons.PLOT_TIME_ON_Y_REVERSED ) );
				break;
			case Z_AXIS_AS_TIME:
				timePicture.setIcon(IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_TIME_ON_Z));
			}
			revalidate();
		}

		@Override
		public boolean isDirty() {
			return false;
		}

		@Override
		public boolean isValidated() {
			return true;
		}
	}
}
