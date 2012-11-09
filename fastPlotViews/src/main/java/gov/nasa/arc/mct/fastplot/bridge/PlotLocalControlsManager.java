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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PanDirection;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.ZoomDirection;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.IconLoader;
import gov.nasa.arc.mct.fastplot.view.Pinnable;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plotter.xy.XYPlotContents;


/**
 * Manages the hiding and show of the local controls on the plot to support panning, zooming, and pausing. 
 */
public class PlotLocalControlsManager implements ActionListener {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(PlotLocalControlsManager.class);
	
	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(PlotLocalControlsManager.class.getName().substring(0, 
        			 	         PlotLocalControlsManager.class.getName().lastIndexOf("."))+".Bundle");
	
	PlotterPlot plot;
	JButton pauseButton;
	
	// Pan controls
	JPanel yAxisPanButtonPanel;
	JPanel xAxisPanButtonPanel;
	JButton yAxisPanUpButton;
	JButton yAxisPanDownButton;
	JButton xAxisPanLeftButton;
	JButton xAxisPanRightButton;
	
	// Zoom controls
    JPanel xAxisZoomButtonCenterPanel; 
    JButton xAxisCenterZoomInButton;
    JButton xAxisCenterZoomOutButton;
    
   JPanel yAxisZoomButtonMiddlePanel; 
    
    JButton yAxisCenterZoomInButton;
    JButton yAxisCenterZoomOutButton;
    
    // Corner reset buttons
    JButton topRightCornerResetButton;
    JButton topLeftCornerResetButton;
    JButton bottomRightCornerResetButton;
    JButton bottomLeftCornerResetButton;
    
	private Pinnable ctrlPin;
	private Pinnable altPin;

	public PlotLocalControlsManager(PlotterPlot thePlot) {
	   plot = thePlot;
	}
	
	public void setupLocalControlManager() {
		createLocalControlButtons();
		updatePinButton();
	}
	
	private void createPauseButton() {
		// we only have a pause button if time is enabled on the plot.
		if (plot.isTimeLabelEnabled){
			pauseButton = makeButton(IconLoader.Icons.PLOT_PAUSE_ICON, 
					BUNDLE.getString("PinTimeAxisButton.Tooltip"));
			   plot.getPlotView().add(pauseButton);
			plot.getPlotView().setComponentZOrder(pauseButton, 0);
			SpringLayout layout = (SpringLayout) plot.getPlotView().getLayout();
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, pauseButton, 0, SpringLayout.HORIZONTAL_CENTER, yAxisZoomButtonMiddlePanel);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, pauseButton, 0, SpringLayout.VERTICAL_CENTER, xAxisZoomButtonCenterPanel);
		}
	}
	
	private void createPanControls() {
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			 if (plot.isTimeLabelEnabled) {
				 createXAxisPanControls();       
			 }
			 createYAxisPanControls();
		} else {
			if (plot.isTimeLabelEnabled) {
				 createYAxisPanControls();       
			 }
			 createXAxisPanControls();
		}
	}
	
	private void createZoomControls() {
		if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
			 if (plot.isTimeLabelEnabled) {
				 createXAxisZoomControls();       
			 }
			 createYAxisZoomControls();
		} else {
			if (plot.isTimeLabelEnabled) {
				 createYAxisZoomControls();       
			 }
			 createXAxisZoomControls();
		} 
	}
	
	
	private void createYAxisPanControls() {
	    yAxisPanUpButton = makeButton(IconLoader.Icons.PLOT_PAN_UP_ARROW_ICON,
		                  BUNDLE.getString("PanUp.Tooltip"));
		yAxisPanUpButton.setVisible(true);
		yAxisPanDownButton = makeButton(IconLoader.Icons.PLOT_PAN_DOWN_ARROW_ICON,
		                    BUNDLE.getString("PanDown.Tooltip"));
		yAxisPanDownButton.setVisible(true);
		yAxisPanButtonPanel = new JPanel();
		yAxisPanButtonPanel.setVisible(false);
		yAxisPanButtonPanel.setLayout(new GridLayout(2,1,2,2));
		yAxisPanButtonPanel.add(yAxisPanUpButton);
		yAxisPanButtonPanel.add(yAxisPanDownButton);
		yAxisPanButtonPanel.setOpaque(false);
		plot.getPlotView().add(yAxisPanButtonPanel);
		plot.getPlotView().setComponentZOrder(yAxisPanButtonPanel, 0);
		SpringLayout layout = (SpringLayout) plot.getPlotView().getLayout();
		XYPlotContents contents = plot.getPlotView().getContents();
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, yAxisPanButtonPanel, 0, SpringLayout.VERTICAL_CENTER, contents);
		layout.putConstraint(SpringLayout.EAST, yAxisPanButtonPanel, -1, SpringLayout.EAST, plot.getPlotView().getYAxis());
	}
	
	private void createXAxisPanControls() {
		xAxisPanLeftButton = makeButton(IconLoader.Icons.PLOT_PAN_LEFT_ARROW_ICON,
			             BUNDLE.getString("PanLeft.Tooltip"));
		xAxisPanLeftButton.setVisible(true);
		xAxisPanRightButton = makeButton(IconLoader.Icons.PLOT_PAN_RIGHT_ARROW_ICON,
			              BUNDLE.getString("PanRight.Tooltip"));
		xAxisPanRightButton.setVisible(true);
		
		xAxisPanButtonPanel = new JPanel();
		xAxisPanButtonPanel.setVisible(false);
		xAxisPanButtonPanel.setLayout(new GridLayout(1,2,2,2));
		xAxisPanButtonPanel.add(xAxisPanLeftButton);
		xAxisPanButtonPanel.add(xAxisPanRightButton);
		xAxisPanButtonPanel.setOpaque(false);
		plot.getPlotView().add(xAxisPanButtonPanel);
		plot.getPlotView().setComponentZOrder(xAxisPanButtonPanel, 0);
		SpringLayout layout = (SpringLayout) plot.getPlotView().getLayout();
		XYPlotContents contents = plot.getPlotView().getContents();
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, xAxisPanButtonPanel, 0, SpringLayout.HORIZONTAL_CENTER, contents);
		layout.putConstraint(SpringLayout.NORTH, xAxisPanButtonPanel, 1, SpringLayout.NORTH, plot.getPlotView().getXAxis());
	}
	
	
	private void createCornerResetButtons() {
		 topRightCornerResetButton = makeButton(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY,
				                                BUNDLE.getString("TopRightCornerButton.Tooltip"));
		 topLeftCornerResetButton = makeButton(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_LEFT_GREY,
				                                BUNDLE.getString("TopLeftCornerButton.Tooltip"));
		 bottomRightCornerResetButton = makeButton(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_BOTTOM_RIGHT_GREY,
				                                BUNDLE.getString("BottomRightCornerButton.Tooltip"));
		 bottomLeftCornerResetButton = makeButton(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_BOTTOM_LEFT_GREY,
				                                BUNDLE.getString("BottomLeftCornerButton.Tooltip"));
		 
		  plot.getPlotView().add(topRightCornerResetButton);
		  plot.getPlotView().add(topLeftCornerResetButton);
		  plot.getPlotView().add(bottomRightCornerResetButton);
		  plot.getPlotView().add(bottomLeftCornerResetButton);
		plot.getPlotView().setComponentZOrder(topRightCornerResetButton, 0);
		plot.getPlotView().setComponentZOrder(topLeftCornerResetButton, 0);
		plot.getPlotView().setComponentZOrder(bottomRightCornerResetButton, 0);
		plot.getPlotView().setComponentZOrder(bottomLeftCornerResetButton, 0);
		SpringLayout layout = (SpringLayout) plot.getPlotView().getLayout();
		XYPlotContents contents = plot.getPlotView().getContents();
		layout.putConstraint(SpringLayout.NORTH, topRightCornerResetButton, 0, SpringLayout.NORTH, contents);
		layout.putConstraint(SpringLayout.EAST, topRightCornerResetButton, 0, SpringLayout.EAST, contents);
		layout.putConstraint(SpringLayout.NORTH, topLeftCornerResetButton, 0, SpringLayout.NORTH, contents);
		layout.putConstraint(SpringLayout.WEST, topLeftCornerResetButton, 0, SpringLayout.WEST, contents);
		layout.putConstraint(SpringLayout.SOUTH, bottomRightCornerResetButton, 0, SpringLayout.SOUTH, contents);
		layout.putConstraint(SpringLayout.EAST, bottomRightCornerResetButton, 0, SpringLayout.EAST, contents);
		layout.putConstraint(SpringLayout.SOUTH, bottomLeftCornerResetButton, 0, SpringLayout.SOUTH, contents);
		layout.putConstraint(SpringLayout.WEST, bottomLeftCornerResetButton, 0, SpringLayout.WEST, contents);
	}
	
	
	
	private void createXAxisZoomControls() {
		 xAxisZoomButtonCenterPanel = new JPanel();
		 xAxisCenterZoomInButton = makeButton(IconLoader.Icons.PLOT_ZOOM_IN_X_ICON,
				  BUNDLE.getString("ZoomInX.Tooltip"));
		  xAxisCenterZoomOutButton = makeButton(IconLoader.Icons.PLOT_ZOOM_OUT_X_ICON,
				  BUNDLE.getString("ZoomOutX.Tooltip"));
		  
 
		  xAxisCenterZoomInButton.setVisible(true);
		  xAxisCenterZoomOutButton.setVisible(true);
		 
		  xAxisZoomButtonCenterPanel.setLayout(new GridLayout(1,2,2,2));
		  xAxisZoomButtonCenterPanel.add(xAxisCenterZoomOutButton);
		  xAxisZoomButtonCenterPanel.add(xAxisCenterZoomInButton);
		  
		  xAxisZoomButtonCenterPanel.setVisible(false);
		  plot.getPlotView().add(xAxisZoomButtonCenterPanel);
		  plot.getPlotView().setComponentZOrder(xAxisZoomButtonCenterPanel, 0);
		  xAxisZoomButtonCenterPanel.setOpaque(false);
		SpringLayout layout = (SpringLayout) plot.getPlotView().getLayout();
		XYPlotContents contents = plot.getPlotView().getContents();
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, xAxisZoomButtonCenterPanel, 0, SpringLayout.HORIZONTAL_CENTER, contents);
		layout.putConstraint(SpringLayout.NORTH, xAxisZoomButtonCenterPanel, 1, SpringLayout.NORTH, plot.getPlotView().getXAxis());
	}
	
	private void createYAxisZoomControls() {
		yAxisZoomButtonMiddlePanel = new JPanel();
		
		  yAxisCenterZoomInButton = makeButton(IconLoader.Icons.PLOT_ZOOM_IN_Y_ICON,
				  BUNDLE.getString("ZoomInY.Tooltip"));
		  yAxisCenterZoomOutButton = makeButton(IconLoader.Icons.PLOT_ZOOM_OUT_Y_ICON,
				  BUNDLE.getString("ZoomOutY.Tooltip"));	    
		  
		
		  yAxisCenterZoomInButton.setVisible(true);
		  yAxisCenterZoomOutButton.setVisible(true);
		 
		  yAxisZoomButtonMiddlePanel.setLayout(new GridLayout(2,1,2,2));
		  
		  yAxisZoomButtonMiddlePanel.add(yAxisCenterZoomOutButton);
		  yAxisZoomButtonMiddlePanel.add(yAxisCenterZoomInButton);
		  
		  yAxisZoomButtonMiddlePanel.setVisible(false);
		  plot.getPlotView().add(yAxisZoomButtonMiddlePanel);
		  plot.getPlotView().setComponentZOrder(yAxisZoomButtonMiddlePanel, 0);
		  yAxisZoomButtonMiddlePanel.setOpaque(false);  
		SpringLayout layout = (SpringLayout) plot.getPlotView().getLayout();
		XYPlotContents contents = plot.getPlotView().getContents();
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, yAxisZoomButtonMiddlePanel, 0, SpringLayout.VERTICAL_CENTER, contents);
		layout.putConstraint(SpringLayout.EAST, yAxisZoomButtonMiddlePanel, -1, SpringLayout.EAST, plot.getPlotView().getYAxis());
	}
	
	protected void createLocalControlButtons() {
        createPanControls();
        createZoomControls();
		createPauseButton();
        createCornerResetButtons();
	}
	
	public int getYAxisLocalControlWidth() {
		return PlotConstants.LOCAL_CONTROL_WIDTH;
	}
	
	public int getXAxisLocalControlHeight() {
		return PlotConstants.LOCAL_CONTROL_HEIGHT;
	}
	
	// Key event notifications.
	public void informShiftKeyState(boolean state) {
		plot.timeSyncLine.informShiftKeyState(state);
	}
	
	public void informAltKeyState(boolean state) {
		if(altPin == null) {
			altPin = plot.getPlotAbstraction().createPin();
		}
		altPin.setPinned(state);
		if (state && !plot.isUserOperationsLocked()) {
			plot.setUserOperationLockedState(true);
		    showZoomControls();
		    plot.panAndZoomManager.enteredZoomMode();
		} else if(!state && plot.panAndZoomManager.isInZoomMode()) {
			hideZoomControls();
			plot.setUserOperationLockedState(false);
			plot.panAndZoomManager.exitedZoomMode();
		}
	}
	
    public void informCtlKeyState(boolean state) {
		if(ctrlPin == null) {
			ctrlPin = plot.getPlotAbstraction().createPin();
		}
    	ctrlPin.setPinned(state);
    	if (state && !plot.isUserOperationsLocked()) {
    		plot.setUserOperationLockedState(true);
		    showPanControls();
		    plot.panAndZoomManager.enteredPanMode();
    	} else if(!state && plot.panAndZoomManager.isInPanMode()) {
    		hidePanControls();
    		plot.setUserOperationLockedState(false);
    		plot.panAndZoomManager.exitedPanMode();
    	}
	}
    
    private void setPauseButtonEnabled(boolean state) {
    	if (plot.isTimeLabelEnabled) {
    		pauseButton.setEnabled(state);
    	}
    }
	
    private void showZoomControls() {
    	//disable pause button when zoom controls showing. 
    	setPauseButtonEnabled(false);  
    	if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
    		yAxisZoomButtonMiddlePanel.setVisible(true);
	    	if (plot.isTimeLabelEnabled) {
	    		xAxisZoomButtonCenterPanel.setVisible(true);
	    	}
    	} else {
    		xAxisZoomButtonCenterPanel.setVisible(true);
	    	if (plot.isTimeLabelEnabled) {
	    		yAxisZoomButtonMiddlePanel.setVisible(true);
	    	}
    	}
    }
    
    private void hideZoomControls() {
    	// enable the pause button. 
    	setPauseButtonEnabled(true);
    
    	
    	if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
	    	if (plot.isTimeLabelEnabled) {
	    	  xAxisZoomButtonCenterPanel.setVisible(false);
	    	}
	    	yAxisZoomButtonMiddlePanel.setVisible(false);
    	} else {
    		if (plot.isTimeLabelEnabled) {
  	    	  yAxisZoomButtonMiddlePanel.setVisible(false);
  	    	}
  	    	xAxisZoomButtonCenterPanel.setVisible(false);
    	}
    }
    
    private void showPanControls() {
    	// disable the pause button. 
    	setPauseButtonEnabled(false);
    	if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
	    	if (plot.isTimeLabelEnabled) {
	    		xAxisPanButtonPanel.setVisible(true);
	    		xAxisPanButtonPanel.revalidate();
	    	}
	      	yAxisPanButtonPanel.setVisible(true);
	      	yAxisPanButtonPanel.revalidate();
    	} else {
    		if (plot.isTimeLabelEnabled) {
	    		yAxisPanButtonPanel.setVisible(true);
	    		yAxisPanButtonPanel.revalidate();
	    	}
	      	xAxisPanButtonPanel.setVisible(true);
	      	xAxisPanButtonPanel.revalidate();
    	}
    }
    
    private void hidePanControls() {
    	
    	// enable the pause button. 
    	setPauseButtonEnabled(true);
    	if (plot.getAxisOrientationSetting() == AxisOrientationSetting.X_AXIS_AS_TIME) {
	    	yAxisPanButtonPanel.setVisible(false);
	    	yAxisPanButtonPanel.revalidate();
	    	if (plot.isTimeLabelEnabled) {
	    		xAxisPanButtonPanel.setVisible(false);
	    		xAxisPanButtonPanel.revalidate();
	    	}
    	} else {
    		xAxisPanButtonPanel.setVisible(false);
	    	xAxisPanButtonPanel.revalidate();
	    	if (plot.isTimeLabelEnabled) {
	    		yAxisPanButtonPanel.setVisible(false);
	    		yAxisPanButtonPanel.revalidate();
	    	}
    	}
    }
    
    void setJumpToCurrentTimeButtonVisible(boolean state) {
    	topRightCornerResetButton.setVisible(state);
    }
    
    void setXAxisCornerResetButtonVisible(boolean state) {
    	bottomRightCornerResetButton.setVisible(state);
    }
  
    void setYAxisCornerResetButtonVisible(boolean state) {
    	topLeftCornerResetButton.setVisible(state);
    }
  
    void setXAndYAxisCornerResetButtonVisible(boolean state) {
    	bottomLeftCornerResetButton.setVisible(state);
    }
  
    void setJumpToCurrentTimeButtonAlarm(boolean state) {
    	if (state) {
    		topRightCornerResetButton.setVisible(true);
    		topRightCornerResetButton.setIcon(IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_ORANGE));
    	} else {
    		topRightCornerResetButton.setVisible(true);
    		topRightCornerResetButton.setIcon(IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY));
    	}
    }
    
    
    // Control display methods. 
    
    private JButton makeButton(IconLoader.Icons icon,  String toolTip) {
    	JButton returnButton =  new JButton(IconLoader.INSTANCE.getIcon(icon));
        returnButton.setToolTipText(toolTip);
        returnButton.setOpaque(false);
        returnButton.setContentAreaFilled(false);
        returnButton.setFocusPainted(true);
        returnButton.setBorder(BorderFactory.createEmptyBorder(PlotConstants.ARROW_BUTTON_BORDER_STYLE_BOTTOM, 
                               PlotConstants.ARROW_BUTTON_BORDER_STYLE_LEFT, 
                               PlotConstants.ARROW_BUTTON_BORDER_STYLE_BOTTOM,
                               PlotConstants.ARROW_BUTTON_BORDER_STYLE_RIGHT));
        returnButton.setVisible(false);
        returnButton.addActionListener(this);
        return returnButton;
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		if (pauseButton!=null && e.getSource() == pauseButton) {
			Axis timeAxis = plot.getPlotAbstraction().getTimeAxis();
			Pinnable timePin = plot.getPlotAbstraction().getTimeAxisUserPin();
			timePin.setPinned(!timePin.isPinned());
			plot.getPlotAbstraction().updateResetButtons();
			updatePinButton();
			// make the pause button behave as if the unpin time axis button was pressed
			if (!timeAxis.isPinned()) {
		          plot.cornerResetButtonManager.informJumpToCurrentTimeSelected();	
			}

		// Pan Controls
		} else if (e.getSource() == yAxisPanUpButton) {
			plot.panAndZoomManager.panAction(PanDirection.PAN_HIGHER_Y_AXIS);
		} else if (e.getSource() == yAxisPanDownButton) {
			plot.panAndZoomManager.panAction(PanDirection.PAN_LOWER_Y_AXIS);
		} else if (e.getSource() == xAxisPanLeftButton) {
			plot.panAndZoomManager.panAction(PanDirection.PAN_LOWER_X_AXIS);
		} else if (e.getSource() == xAxisPanRightButton) {
			plot.panAndZoomManager.panAction(PanDirection.PAN_HIGHER_X_AXIS);
		// Zoom Controls
		} else if (e.getSource() == xAxisCenterZoomInButton) {
			plot.panAndZoomManager.zoomAction(ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		} else if (e.getSource() == xAxisCenterZoomOutButton) {
			plot.panAndZoomManager.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_X_AXIS);
		} else if (e.getSource() == yAxisCenterZoomInButton) {
			plot.panAndZoomManager.zoomAction(ZoomDirection.ZOOM_IN_CENTER_Y_AXIS);
		} else if (e.getSource() == yAxisCenterZoomOutButton) {
			plot.panAndZoomManager.zoomAction(ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS);
		} else if (e.getSource() == topRightCornerResetButton) {
			if (!plot.isUserOperationsLocked()) {
	          plot.cornerResetButtonManager.informJumpToCurrentTimeSelected();	
			}
			updatePinButton();
		} else if (e.getSource() ==  topLeftCornerResetButton) {
			 plot.cornerResetButtonManager.informResetYAxisActionSelected();	
			updatePinButton();
		} else if (e.getSource() ==  bottomRightCornerResetButton) {
			 plot.cornerResetButtonManager.informResetXAxisActionSelected();	
			updatePinButton();
		} else if (e.getSource() == bottomLeftCornerResetButton) {
			 plot.cornerResetButtonManager.informResetXAndYActionSelected();	
			updatePinButton();
		} else {
			assert false: "Unknown button pushed on plot local controls.";
		}

		plot.getPlotView().requestFocus();
	}
 
	void setPauseButtonVisible(boolean state) {
		if (plot.isTimeLabelEnabled) {
			pauseButton.setVisible(state);
		}
	}
	
	void informMouseEntered() {
		// show the pause button.
		if (!isPinned()) {
			setPauseButtonVisible(true);
		} else {
			if (plot.panAndZoomManager.isInZoomMode()) {
				showZoomControls();
			}
			if (plot.panAndZoomManager.isInPanMode()) {
				showPanControls();
			}
		}
	}
	
	private boolean isPinned() {
		PlotAbstraction plotAbstraction = plot.getPlotAbstraction();
		Axis timeAxis = plotAbstraction.getTimeAxis();
		return timeAxis.isPinned() || plotAbstraction.isPinned();
	}
	
    void informMouseExited() {
		// hide the pause button. 
    	if (!isPinned()) {
    		setPauseButtonVisible(false);
    	} else {
    		if (plot.panAndZoomManager.isInPanMode()) {
    			hidePanControls();
    		}
    		if (plot.panAndZoomManager.isInZoomMode()) {
    			hideZoomControls();
    		}
    	}
	}


	public void updatePinButton() {
		if(plot.isTimeLabelEnabled) {
			ImageIcon icon = null;
			if (isPinned()) {
				icon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_PLAY_ICON);
				pauseButton.setToolTipText(BUNDLE.getString("UnpinTimeAxisButton.Tooltip"));
			} else {
				icon = IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_PAUSE_ICON);
				pauseButton.setToolTipText(BUNDLE.getString("PinTimeAxisButton.Tooltip"));
			}
			pauseButton.setIcon(icon);
			pauseButton.setSize(icon.getIconWidth(), icon.getIconHeight());
			pauseButton.revalidate();
		}
	}
	
	public void pinXYAxesAfterZoomedIn() {
		plot.getPlotAbstraction().getTimeAxisUserPin().setPinned(true);
		plot.getPlotAbstraction().getTimeAxis().setZoomed(true);
		
		plot.getNonTimeAxisUserPin().setPinned(true);
		plot.getNonTimeAxis().setZoomed(true);
		
		plot.getPlotAbstraction().updateResetButtons();
		plot.refreshDisplay();
		plot.notifyObserversTimeChange();
		plot.getPlotDataManager().resizeAndReloadPlotBuffer();
	}
}
