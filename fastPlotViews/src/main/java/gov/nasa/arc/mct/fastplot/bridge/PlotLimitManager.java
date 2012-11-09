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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.LimitAlarmState;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.view.IconLoader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plotter.xy.CompressingXYDataset;
import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

/**
 * Manage data items going out of bounds, displaying warning indicators to user, and responding to user
 * actions on those warning indicators. 
 */
public class PlotLimitManager implements ActionListener {
	
	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(PlotLocalControlsManager.class.getName().substring(0, 
        			 	         PlotLocalControlsManager.class.getName().lastIndexOf("."))+".Bundle");
	
	private PlotterPlot plot;
	
	private final static Logger logger = LoggerFactory.getLogger(PlotLimitManager.class);
	
	// Limit Buttons
	// min and max limit arrow panels
	JButton nonTimeMaxLimitButton;
	JButton nonTimeMinLimitButton;   
	
	// Record the times at which the most recent out of range data items were
	// experienced. These are used to identify when the value has scrolled off of the plot
	// window. 
	private long maxAlarmMostRecentTime = 0;
	private long minAlarmMostRecentTime = 0;
	
	private double cachedNonTimeMinValue = 0.0;
	private double cachedNonTimeMaxValue = 0.0;
		
	// Alarm states
	LimitAlarmState nonTimeMinAlarm = LimitAlarmState.NO_ALARM;
	LimitAlarmState nonTimeMaxAlarm = LimitAlarmState.NO_ALARM;
	LimitAlarmState untranslatedNonTimeMaxAlarm = LimitAlarmState.NO_ALARM;
	LimitAlarmState untranslatedNonTimeMinAlarm = LimitAlarmState.NO_ALARM;
	
	
	LimitAlarmState cachedNonTimeMinAlarm = LimitAlarmState.NO_ALARM;
	LimitAlarmState cachedNonTimeMaxAlarm = LimitAlarmState.NO_ALARM;

	// Icons
	ImageIcon nonTimeMaxLimitAlarmRaisedIcon;
	ImageIcon nonTimeMaxLimitAlarmOpenedByUserIcon;
	ImageIcon nonTimeMaxLimitAlarmClosedByUserIcon;
	
	ImageIcon nonTimeMinLimitAlarmRaisedIcon;
	ImageIcon nonTimeMinLimitAlarmOpenedByUserIcon;
	ImageIcon nonTimeMinLimitAlarmClosedByUserIcon;
	
	private boolean untranslated = true;

	PlotLimitManager(PlotterPlot thePlot) {
		plot = thePlot;
	}

	/**
	 * Set the enabled state of the manager.
	 * @param state true if manager is enabled, false otherwise. 
	 */
	void setModeUntranslated(boolean state) {
		if (state) {
			//Github Issue #27 08/15/12 limit buttons are always visible
			//and are always active
			//except when in auto adjust and non-time and time are not pinned
			untranslated = true;
			resetLimitAlarms();
			if (plot.getNonTimeAxisSubsequentMaxSetting() == NonTimeAxisSubsequentBoundsSetting.AUTO) {	
				nonTimeMaxLimitButton.setVisible(false);		
			}
			if (plot.getNonTimeAxisSubsequentMinSetting() == NonTimeAxisSubsequentBoundsSetting.AUTO) {
				nonTimeMinLimitButton.setVisible(false);
			}
			updateLimitButtons();

		} else {
			untranslated = false;
			nonTimeMinAlarm = LimitAlarmState.NO_ALARM;
			nonTimeMaxAlarm = LimitAlarmState.NO_ALARM;
			updateLimitButtons();
		}
	}
	
	/**
	 * Return the enabled state of the limit manager.
	 * @return true is manager enabled, false otherwise. 
	 */
	boolean isUntranslated() {
		return untranslated; 
	}
	
	/**
	 * Examine the non-time limit settings and place alarms on the RTProcessVariable as required. This 
	 * is the underlying mechanism which drives the out of limit triangles. 
	 * @param dataset
	 */
	void addLimitAlarms(CompressingXYDataset dataset) {
	}

	// Create the limit buttons to the correct size and format and add action this manager as
	// the listener to their events. 
	public void setupLimitButtons() {
	
		// Select icons based on time axis location and if the non time max is on the left, right, top, or bottom of plot.		
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			   // time is on the x-axis
				if (plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
					nonTimeMaxLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_UP_ARROW_SOLID_ICON);
					nonTimeMaxLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_DOWN_ARROW_TRANSLUCENT_ICON);
					nonTimeMaxLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_UP_ARROW_HOLLOW_ICON);
					
					nonTimeMinLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_DOWN_ARROW_SOLID_ICON);
					nonTimeMinLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_UP_ARROW_TRANSLUCENT_ICON);
					nonTimeMinLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_DOWN_ARROW_HOLLOW_ICON);
					
				} else if (plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM) {
					nonTimeMaxLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_DOWN_ARROW_SOLID_ICON);
					nonTimeMaxLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_UP_ARROW_TRANSLUCENT_ICON);
					nonTimeMaxLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_DOWN_ARROW_HOLLOW_ICON);
					
					nonTimeMinLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_UP_ARROW_SOLID_ICON);
					nonTimeMinLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_DOWN_ARROW_TRANSLUCENT_ICON);
					nonTimeMinLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_UP_ARROW_HOLLOW_ICON);
					
				} else {
					assert false: "Unknown axis maximum location setting.";
				}
		} else if (plot.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME) {
			// time is on the y-axis
			if (plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT) {
				nonTimeMaxLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_LEFT_ARROW_SOLID_ICON);
				nonTimeMaxLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_RIGHT_ARROW_TRANSLUCENT_ICON);
				nonTimeMaxLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_LEFT_ARROW_HOLLOW_ICON);
				
				nonTimeMinLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_RIGHT_ARROW_SOLID_ICON);
				nonTimeMinLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_LEFT_ARROW_TRANSLUCENT_ICON);
				nonTimeMinLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_RIGHT_ARROW_HOLLOW_ICON);
				
			} else if (plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {
				nonTimeMaxLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_RIGHT_ARROW_SOLID_ICON);
				nonTimeMaxLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_LEFT_ARROW_TRANSLUCENT_ICON);
				nonTimeMaxLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_RIGHT_ARROW_HOLLOW_ICON);
				
				nonTimeMinLimitAlarmRaisedIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_LEFT_ARROW_SOLID_ICON);
				nonTimeMinLimitAlarmOpenedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_RIGHT_ARROW_TRANSLUCENT_ICON);
				nonTimeMinLimitAlarmClosedByUserIcon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_LEFT_ARROW_HOLLOW_ICON);
			} else {
				assert false: "Unknown axis maximum location setting.";
			}
			
		} else {
			assert false: "Unknown axis orientation setting.";
		}
		
		assert nonTimeMaxLimitAlarmRaisedIcon != null;
		assert nonTimeMaxLimitAlarmOpenedByUserIcon !=null;
		assert nonTimeMaxLimitAlarmClosedByUserIcon !=null;
		
		assert nonTimeMinLimitAlarmRaisedIcon !=null;
		assert nonTimeMinLimitAlarmOpenedByUserIcon !=null;
		assert nonTimeMinLimitAlarmClosedByUserIcon !=null;
		
		// create limit buttons	
		nonTimeMaxLimitButton = new JButton(nonTimeMaxLimitAlarmRaisedIcon);
		nonTimeMaxLimitButton.setVisible(false);
		nonTimeMaxLimitButton.setContentAreaFilled( false );
		nonTimeMaxLimitButton.setBorder(BorderFactory.createEmptyBorder(PlotConstants.ARROW_BUTTON_BORDER_STYLE_TOP, 
                PlotConstants.ARROW_BUTTON_BORDER_STYLE_LEFT, 
                PlotConstants.ARROW_BUTTON_BORDER_STYLE_BOTTOM,
                PlotConstants.ARROW_BUTTON_BORDER_STYLE_RIGHT));
		nonTimeMaxLimitButton.setFocusPainted(false);
		nonTimeMaxLimitButton.setToolTipText(BUNDLE.getString("ShowAllData"));
		XYPlot plotView = plot.getPlotView();
		plotView.add(nonTimeMaxLimitButton);
		plotView.setComponentZOrder(nonTimeMaxLimitButton, 0);
		XYPlotContents contents = plotView.getContents();
		SpringLayout layout = (SpringLayout) plotView.getLayout();
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			if (plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				layout.putConstraint(SpringLayout.NORTH, nonTimeMaxLimitButton, 0, SpringLayout.NORTH, contents);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nonTimeMaxLimitButton, 0, SpringLayout.HORIZONTAL_CENTER, contents);
			} else if (plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM) {
				layout.putConstraint(SpringLayout.SOUTH, nonTimeMaxLimitButton, 0, SpringLayout.SOUTH, contents);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nonTimeMaxLimitButton, 0, SpringLayout.HORIZONTAL_CENTER, contents);
			}
		} else if (plot.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME){
			if (plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT) {
				layout.putConstraint(SpringLayout.WEST, nonTimeMaxLimitButton, 0, SpringLayout.WEST, contents);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, nonTimeMaxLimitButton, 0, SpringLayout.VERTICAL_CENTER, contents);
			} else if (plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT){
				layout.putConstraint(SpringLayout.EAST, nonTimeMaxLimitButton, 0, SpringLayout.EAST, contents);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, nonTimeMaxLimitButton, 0, SpringLayout.VERTICAL_CENTER, contents);
			}
		}

		nonTimeMinLimitButton = new JButton(nonTimeMinLimitAlarmRaisedIcon );
		nonTimeMinLimitButton.setVisible(false);
		nonTimeMinLimitButton.setContentAreaFilled( false );
		nonTimeMinLimitButton.setBorder(BorderFactory.createEmptyBorder(PlotConstants.ARROW_BUTTON_BORDER_STYLE_TOP, 
                                             PlotConstants.ARROW_BUTTON_BORDER_STYLE_LEFT, 
                                             PlotConstants.ARROW_BUTTON_BORDER_STYLE_BOTTOM,
                                             PlotConstants.ARROW_BUTTON_BORDER_STYLE_RIGHT));
		nonTimeMinLimitButton.setFocusPainted(false);
		nonTimeMinLimitButton.setToolTipText(BUNDLE.getString("ShowAllData"));
		nonTimeMaxLimitButton.addActionListener(this);
		nonTimeMinLimitButton.addActionListener(this);		
		plotView.add(nonTimeMinLimitButton);
		plotView.setComponentZOrder(nonTimeMinLimitButton, 0);
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			if (plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
				layout.putConstraint(SpringLayout.SOUTH, nonTimeMinLimitButton, 0, SpringLayout.SOUTH, contents);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nonTimeMinLimitButton, 0, SpringLayout.HORIZONTAL_CENTER, contents);
			} else if (plot.getYAxisMaximumLocation() == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM) {
				layout.putConstraint(SpringLayout.NORTH, nonTimeMinLimitButton, 0, SpringLayout.NORTH, contents);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nonTimeMinLimitButton, 0, SpringLayout.HORIZONTAL_CENTER, contents);
			}
		} else if (plot.getAxisOrientationSetting() == AxisOrientationSetting.Y_AXIS_AS_TIME){
			if (plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT) {
				layout.putConstraint(SpringLayout.EAST, nonTimeMinLimitButton, 0, SpringLayout.EAST, contents);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, nonTimeMinLimitButton, 0, SpringLayout.VERTICAL_CENTER, contents);
			} else if (plot.getXAxisMaximumLocation() == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT){
				layout.putConstraint(SpringLayout.WEST, nonTimeMinLimitButton, 0, SpringLayout.WEST, contents);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, nonTimeMinLimitButton, 0, SpringLayout.VERTICAL_CENTER, contents);
			}
		}
	}


	// Listener for button events.
	public void actionPerformed(ActionEvent e) {
		// Handlers transition alarms through states. 
		if (e.getSource() == nonTimeMaxLimitButton) {
			processMaxAlertButtonPress();
		} else if (e.getSource() == nonTimeMinLimitButton) {
			processMinAlertButtonPress();
		} else {
			logger.error("Unknown button selected on plot");
		}
		if (plot.isPaused()) {
			// We need to rescale the plot as a button has been pressed and we are in paused mode.
			// When we are not paused the regular updating of the plot will cause it to be resacled.
			plot.refreshDisplay();
		}
	}	
	
	private void updateLimitButtons() {
		if (nonTimeMaxAlarm == LimitAlarmState.ALARM_RAISED	) {
			setMaxAlarmIconToAlarmRaised();
		} else if (nonTimeMaxAlarm == LimitAlarmState.ALARM_OPENED_BY_USER) {
			setMaxAlarmIconToAlarmOpendedByUser();
		} else if (nonTimeMaxAlarm == LimitAlarmState.ALARM_CLOSED_BY_USER) {
			setMaxAlarmIconToAlarmClosedByUser();
		} else if (nonTimeMaxAlarm == LimitAlarmState.NO_ALARM) {
			nonTimeMaxLimitButton.setVisible(false);
		}
		if (nonTimeMinAlarm == LimitAlarmState.ALARM_RAISED	) {
			setMinAlarmIconToAlarmRaised();
		} else if (nonTimeMinAlarm == LimitAlarmState.ALARM_OPENED_BY_USER) {
			setMinAlarmIconToAlarmOpendedByUser();
		} else if (nonTimeMinAlarm == LimitAlarmState.ALARM_CLOSED_BY_USER) {
			setMinAlarmIconToAlarmClosedByUser();
		}  else if (nonTimeMinAlarm == LimitAlarmState.NO_ALARM) {
			nonTimeMinLimitButton.setVisible(false);
		}
	}
	
	/**
	 * Transition through the alarm states as user presses max alert button. 
	 */
	void processMaxAlertButtonPress() {
			if (nonTimeMaxAlarm == LimitAlarmState.ALARM_CLOSED_BY_USER || 
					nonTimeMaxAlarm == LimitAlarmState.ALARM_RAISED	) {
				setCachedNonTimeMaxValue(plot.getMaxNonTime());
				setMaxAlarmIconToAlarmOpendedByUser();
				nonTimeMaxAlarm = LimitAlarmState.ALARM_OPENED_BY_USER;
				plot.setNonTimeMaxFixed(false);
			} else if (nonTimeMaxAlarm == LimitAlarmState.ALARM_OPENED_BY_USER) {
				setMaxAlarmIconToAlarmClosedByUser();
				nonTimeMaxAlarm = LimitAlarmState.ALARM_CLOSED_BY_USER;
				setNotTimeMaxToFixedMode(); 		

			} else if (nonTimeMaxAlarm == LimitAlarmState.NO_ALARM) {
				assert false : "No alarm state means that no button should be visible for user to click on";
			} else {
				assert false : "Unknown alarm state";
			}
	}
	
	/**
	 * Transition through the alarm states as user presses min alert button. 
	 */
	void processMinAlertButtonPress(){
			if (nonTimeMinAlarm == LimitAlarmState.ALARM_CLOSED_BY_USER || 
					nonTimeMinAlarm == LimitAlarmState.ALARM_RAISED	) {
				setCachedNonTimeMinValue(plot.getMinNonTime());
				setMinAlarmIconToAlarmOpendedByUser();
				nonTimeMinAlarm = LimitAlarmState.ALARM_OPENED_BY_USER;
				plot.setNonTimeMinFixed(false);
			} else if (nonTimeMinAlarm == LimitAlarmState.ALARM_OPENED_BY_USER) {
				setMinAlarmIconToAlarmClosedByUser(); 
				nonTimeMinAlarm = LimitAlarmState.ALARM_CLOSED_BY_USER;
				setNotTimeMinToFixedMode(); 
			} else if (nonTimeMinAlarm == LimitAlarmState.NO_ALARM) {
				assert false : "No alarm state means that no button should be visible for user to click on";
			} else {
				assert false : "Unknown alarm state";
			}
	}
	
	private void setNotTimeMaxToFixedMode() {
		plot.setNonTimeMaxFixed(true);
		// reset the bounds on the nontime max.
		plot.resetNonTimeMax();
	}
	
	private void setNotTimeMinToFixedMode() {
		plot.setNonTimeMinFixed(true);
		// reset the bounds on the nontime min.
		plot.resetNonTimeMin();
	}
	
	private void addMaxAlertButton() {			
		setMaxAlarmIconToAlarmRaised();
		nonTimeMaxLimitButton.setVisible(true);
	}

	private void addMinAlertButton() {	
		setMinAlarmIconToAlarmRaised();
		nonTimeMinLimitButton.setVisible(true);
	}

	public void resetLimitAlarms() {
		// Reset alarms after zoom or pan
		nonTimeMinAlarm = LimitAlarmState.NO_ALARM;
		nonTimeMaxAlarm = LimitAlarmState.NO_ALARM;

	}
	
	/**
	 * Transform an input value to a physical pixel point (use as Y point coord)
	 * and compare that point's Y coord to an input physical Y value.
	 * If less than or equal to 1, return true.
	 * @param value
	 * @param nonTimeLimitPhysicalValue
	 * @return
	 */
	private boolean nonTimeValueWithin1PixelOfLimit(double value, double nonTimeLimitPhysicalValue) {
		if (Double.valueOf(value).equals(Double.valueOf(Double.NaN))) {
			return false;
		}
		// Don't flag limit as being reached if plot is just non-visible
		if (plot.getPlotView().getWidth() < 1 || plot.getPlotView().getHeight() < 1) {
			return false;
		}
		
		Point2D valuePointPhysical = new Point2D.Double(0,value);  // source value point
		plot.getPlotView().toPhysical(valuePointPhysical, valuePointPhysical);
		if (Math.abs(valuePointPhysical.getY()-nonTimeLimitPhysicalValue) <= 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Inform limit manager of the most recently plotted time. 
	 * @param atTime time at which point was plotted
	 */
	public void informPointPlottedAtTime(long atTime, double value) {

		boolean checkMax = plot.getNonTimeAxisSubsequentMinSetting() == NonTimeAxisSubsequentBoundsSetting.FIXED
				|| plot.getNonTimeAxisSubsequentMaxSetting() == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED 
				|| plot.getNonTimeAxis().isPinned() 
				|| plot.getPlotAbstraction().getTimeAxisUserPin().isPinned()
				|| plot.getPlotAbstraction().getTimeAxis().isPinned()
				|| plot.getPlotAbstraction().getTimeAxis().isZoomed();
		if(checkMax && 
				(value >= plot.getMaxNonTime()
				|| nonTimeValueWithin1PixelOfLimit(value, plot.nonTimeAxisMaxPhysicalValue)) 
				&& atTime >= plot.getMinTime() && atTime <= plot.getMaxTime()) {
			if (nonTimeMaxAlarm != LimitAlarmState.ALARM_OPENED_BY_USER ) { 
				boolean wasOpen = nonTimeMaxAlarm == LimitAlarmState.ALARM_RAISED;
				nonTimeMaxAlarm = LimitAlarmState.ALARM_RAISED;	
				maxAlarmMostRecentTime = atTime;
				if(!wasOpen) {
					addMaxAlertButton();

					if(plot.getNonTimeAxisSubsequentMaxSetting() == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED &&
							!plot.getNonTimeAxis().isPinned()) {

						processMaxAlertButtonPress();
					}
				}
			}
		}
		
		boolean checkMin = plot.getNonTimeAxisSubsequentMinSetting() == NonTimeAxisSubsequentBoundsSetting.FIXED
				|| plot.getNonTimeAxisSubsequentMinSetting() == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED 
				|| plot.getNonTimeAxis().isPinned() 
				|| plot.getPlotAbstraction().getTimeAxisUserPin().isPinned() 
				|| plot.getPlotAbstraction().getTimeAxis().isPinned()
				|| plot.getPlotAbstraction().getTimeAxis().isZoomed();
		if(checkMin && (value <= plot.getMinNonTime() ||
				nonTimeValueWithin1PixelOfLimit(value, plot.nonTimeAxisMinPhysicalValue)) &&
				atTime >= plot.getMinTime() && atTime <= plot.getMaxTime()) {
			if (nonTimeMinAlarm != LimitAlarmState.ALARM_OPENED_BY_USER ) { 
					
				boolean wasOpen = nonTimeMinAlarm == LimitAlarmState.ALARM_RAISED;
				nonTimeMinAlarm = LimitAlarmState.ALARM_RAISED;	
				minAlarmMostRecentTime = atTime;
				if(!wasOpen) {
					addMinAlertButton();

					if(plot.getNonTimeAxisSubsequentMinSetting() == NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED &&
							!plot.getNonTimeAxis().isPinned()) {
						processMinAlertButtonPress();
					}
				}
			}
		}

		// Check upper alarm still valid
		
        // Only check if an alarm a max alarm is raised and limit indicators showing.
		
		if (checkMax && nonTimeMaxAlarm != LimitAlarmState.NO_ALARM) {
			if (plot.getMinTime() > maxAlarmMostRecentTime) {
				// alarm has scrolled off. 
				nonTimeMaxAlarm = LimitAlarmState.NO_ALARM;
				nonTimeMaxLimitButton.setVisible(false);			
			}
		}
			
		// Check lower alarm still valid
		// Only check if an alarm a max alarm is raised.
        if (checkMin && nonTimeMinAlarm != LimitAlarmState.NO_ALARM) {		
            if (plot.getMinTime() > minAlarmMostRecentTime) {
            	nonTimeMinAlarm = LimitAlarmState.NO_ALARM;
            	nonTimeMinLimitButton.setVisible(false); 	
			}
		}
        
        if (checkMin || checkMax) {
        	plot.newPointPlotted(atTime, value);
        }
        
	}
	
	private void changeButtonIcon(JButton button, ImageIcon newIcon, String tooltip) {
		button.setIcon(newIcon);
		button.setToolTipText(tooltip);
	}
	
	private void setMaxAlarmIconToAlarmRaised() {
		changeButtonIcon(nonTimeMaxLimitButton, nonTimeMaxLimitAlarmRaisedIcon, 
				BUNDLE.getString("ShowAllData"));
	}

	private void setMaxAlarmIconToAlarmOpendedByUser() {
		changeButtonIcon(nonTimeMaxLimitButton, nonTimeMaxLimitAlarmOpenedByUserIcon,
				BUNDLE.getString("HideOOBData"));
	}

	private void setMaxAlarmIconToAlarmClosedByUser() {
		changeButtonIcon(nonTimeMaxLimitButton,nonTimeMaxLimitAlarmClosedByUserIcon,
				BUNDLE.getString("ShowAllDataAgain"));
	}

	private void setMinAlarmIconToAlarmRaised() {
		changeButtonIcon(nonTimeMinLimitButton, nonTimeMinLimitAlarmRaisedIcon, 
				BUNDLE.getString("ShowAllData"));
	}

	private void setMinAlarmIconToAlarmOpendedByUser() {
		changeButtonIcon(nonTimeMinLimitButton, nonTimeMinLimitAlarmOpenedByUserIcon,
				BUNDLE.getString("HideOOBData"));
	}
	
	private void setMinAlarmIconToAlarmClosedByUser() {
		changeButtonIcon(nonTimeMinLimitButton, nonTimeMinLimitAlarmClosedByUserIcon,
				BUNDLE.getString("ShowAllDataAgain"));
    }

	/**
	 * @return the cachedNonTimeMinValue
	 */
	public double getCachedNonTimeMinValue() {
		return cachedNonTimeMinValue;
	}

	/**
	 * @param cachedNonTimeMinValue the cachedNonTimeMinValue to set
	 */
	public void setCachedNonTimeMinValue(double cachedNonTimeMinValue) {
		this.cachedNonTimeMinValue = cachedNonTimeMinValue;
	}

	/**
	 * @return the cachedNonTimeMaxValue
	 */
	public double getCachedNonTimeMaxValue() {
		return cachedNonTimeMaxValue;
	}

	/**
	 * @param cachedNonTimeMaxValue the cachedNonTimeMaxValue to set
	 */
	public void setCachedNonTimeMaxValue(double cachedNonTimeMaxValue) {
		this.cachedNonTimeMaxValue = cachedNonTimeMaxValue;
	}
}		