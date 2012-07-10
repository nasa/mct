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
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;

import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge between the PlostSettingsControlPanel which provides the GUI widgets and callbacks and
 * MCT. 
 * 
 */
public class PlotSettingController {
	
	    AxisOrientationSetting  timeAxisSetting;
	    XAxisMaximumLocationSetting xAxisMaximumLocation;
	    YAxisMaximumLocationSetting yAxisMaximumLocation;
	    TimeAxisSubsequentBoundsSetting timeAxisSubsequentSetting;
	    NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting;
	    NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting;
	    double nonTimeMax;
	    double nonTimeMin;
	    GregorianCalendar minTime;
	    GregorianCalendar maxTime;
	    double timePadding = 0;
	    double nonTimeMinPadding = 0;
	    double nonTimeMaxPadding = 0;
	    boolean useOrdinalPositionForSubplots;
	    String timeSystemSetting;
	    String timeFormatSetting;
	    boolean timeAxisPinned;
		PlotLineDrawingFlags plotLineDraw;
	    PlotLineConnectionType plotLineConnectionType;
	
	private static Logger logger = LoggerFactory.getLogger(PlotSettingController.class);
    
	    // Panel controller is controlling
		private PlotSettingsControlPanel panel;
	        
	    /**
	     * Construct controller defining the panel to connect to. 
	     * @param panel the panel to control.
	     * @throws IllegalArgumentExcpetion if panel is null.
	     */
	    PlotSettingController(PlotSettingsControlPanel inputPanel) {
	        if (inputPanel == null) {
	            throw new IllegalArgumentException();
	        }
	        panel = inputPanel;
	    }
	    	    
	    public void setTimeAxis(AxisOrientationSetting setting) {
	    	timeAxisSetting = setting;
	    }
	    
	    public void setTimeSystem(String setting) {
	    	timeSystemSetting = setting;
	    }
 	    
	    public void setTimeFormat(String setting) {
	    	timeFormatSetting = setting;
	    }
	    
	    public void setXAxisMaximumLocation(XAxisMaximumLocationSetting setting) {
	         xAxisMaximumLocation = setting;	
	    }
	    
	    public void setYAxisMaximumLocation(YAxisMaximumLocationSetting setting) {
	        yAxisMaximumLocation = setting;	
	    }
	
	    public void setTimeAxisPinned(boolean pinned) {
	    	timeAxisPinned = pinned;
	    }
	    
	    public void setTimeAxisSubsequentBounds(TimeAxisSubsequentBoundsSetting setting) {
	        timeAxisSubsequentSetting = setting;    	
	    }
	    	
	    public void setNonTimeAxisSubsequentMinBounds(NonTimeAxisSubsequentBoundsSetting setting) {
	    	nonTimeAxisSubsequentMinSetting = setting;
	    }
	    
	    public void setNonTimeAxisSubsequentMaxBounds(NonTimeAxisSubsequentBoundsSetting setting) {
	    	nonTimeAxisSubsequentMaxSetting = setting;
	    }
	    
	    public void setNonTimeMinMaxValues(double minValue, double maxValue) {
	        nonTimeMin = minValue;
	        nonTimeMax = maxValue;
	    }
	    
	    public void setTimeMinMaxValues(GregorianCalendar lowerTime, GregorianCalendar upperTime) {
	    	minTime = lowerTime;
	    	maxTime = upperTime;
	    }
	    
	    public void setTimePadding(Double padding) {
	       timePadding = padding;
        }
	    
	    public void setNonTimeMaxPadding(Double padding) {
		       nonTimeMaxPadding = padding;
	        }
		
	    public void setNonTimeMinPadding(Double padding) {
		       nonTimeMinPadding = padding;
	    }
	    
	    public void setUseOrdinalPositionToGroupSubplots(boolean value) {
	    	useOrdinalPositionForSubplots = value;
	    }
	    
	    public void setPlotLineDraw(PlotLineDrawingFlags plotLineDraw) {
			this.plotLineDraw = plotLineDraw;
		}

		public void setPlotLineConnectionType(
				PlotLineConnectionType plotLineConnectionType) {
			this.plotLineConnectionType = plotLineConnectionType;
		}

		
	    /**
	     * Run tests to check that the plot settings panel has feed a valid state for a plot to be created.
	     * @return true if state is valid. False otherwise. 
	     */
	    String stateIsValid() {
	    	if (nonTimeMin > nonTimeMax) {
	    		return "PlotSettingsPanel passed a nonTimeMin  (" + nonTimeMin + 
	    				      ") >=  nonTimeMax (" + nonTimeMax + ") to the PlotSettingsController. Panel needs to validate this.";
	     	}
	    	
	    	if (minTime == null || maxTime == null) {
	    		return "PlotSettingsPanel passed a null min or max time to the PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (minTime.getTimeInMillis() >= maxTime.getTimeInMillis()) {
	            return "PlotSettingsPanel passed a timeMin (" + minTime.getTimeInMillis() + ") >= timeMax (" 
	                                                          + maxTime.getTimeInMillis() + ") to the PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (timePadding > 1.0 || timePadding < 0.0) {
	    		return "PlotSettingsPanel of "+ timePadding + " passed a timePadding outside the range 0.0 .. 1.0 to PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (nonTimeMinPadding > 1.0 || nonTimeMaxPadding < 0.0) {
	    		return "PlotSettingsPanel of "+ nonTimeMinPadding + " passed a nonTimeMinPadding outside the range 0.0 .. 1.0 to PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (nonTimeMinPadding + nonTimeMaxPadding >= 1.0) {
	    		return "The minimum and maximum non-Time axis padding must total less than 1";
	    	}
	    	
	    	if (nonTimeMaxPadding > 1.0 || nonTimeMaxPadding < 0.0) {
	    		return "PlotSettingsPanel of "+ nonTimeMaxPadding + " passed a nonTimeMaxPadding outside the range 0.0 .. 1.0 to PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	
	    	if (timeAxisSetting == null) {
	    		return "PlotSettingsPanel passed a null timeAxisSetting to the PlotSettingsController. Panel needs to validate this.";
	      	}
	    	
	    	if (xAxisMaximumLocation == null) {
	    		return "PlotSettingsPanel passed a null xAxisMaximumLocation to the PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (timeAxisSubsequentSetting == null) {
	    		return "PlotSettingsPanel passed a null timeAxisSubsequentSetting to the PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (nonTimeAxisSubsequentMinSetting == null) {
	    		return "PlotSettingsPanel passed a null nonTimeAxisSubsequentMinSetting to the PlotSettingsController. Panel needs to validate this.";
	    	}
	    	
	    	if (nonTimeAxisSubsequentMaxSetting == null) {
	    		return "PlotSettingsPanel passed a null nonTimeAxisSubsequentMaxSetting to the PlotSettingsController. Panel needs to validate this.";
	    		
	    	}
	    	
	    	return null;
	    }
	    
	    /*
	     * Call when user presses create chart button
	     */
	    public void createPlot() {
	    	// Only create a new plot if the state passed from plot settings panel is valid.
	    	String badStateMessage = stateIsValid();

	    	// Cause a hard assertion failure when running in development environment. 
	    	assert (badStateMessage == null) : "Plot setting panel passed a bad state to the plot " + badStateMessage; 

	    	// Display an error message in production environment. 
	    	if (badStateMessage != null) {
	    		logger.error(badStateMessage);
	    	} else {
	    		// The state is good so that we can create the plot. 		
	    		panel.getPlot().setupPlot(timeAxisSetting,
	    				timeSystemSetting,
                        timeFormatSetting,
	    				xAxisMaximumLocation,
	    				yAxisMaximumLocation,
	    				timeAxisSubsequentSetting,
	    				nonTimeAxisSubsequentMinSetting,
	    				nonTimeAxisSubsequentMaxSetting,
	    				nonTimeMax,
	    				nonTimeMin,
	    				minTime,
	    				maxTime,
	    				timePadding,
	    				nonTimeMaxPadding,
	    				nonTimeMinPadding,
	    				useOrdinalPositionForSubplots,
	    				timeAxisPinned,
	    				plotLineDraw,
	    				plotLineConnectionType
	    				);
	    	}
	    }
}
