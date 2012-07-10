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
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;

import java.util.GregorianCalendar;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JToggleButtonFixture;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class PlotSettingsControlPanelRobotTest {

	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(PlotSettingsControlPanel.class.getName().substring(0, 
                                 PlotSettingsControlPanel.class.getName().lastIndexOf("."))+".Bundle");
	
	private Robot robot;
	    private static final String TITLE = "Plot Settings Control Panel Test Frame";
	    @Mock
	    PlotViewManifestation mockPlotMan;
	    
	    private PlotSettingsControlPanel controlPanel;
	    
	    PlotView qcPlot;	    
	    GregorianCalendar currentTime = new GregorianCalendar();
	    
	    MyPlotSettingController controller;
	   
	  @BeforeSuite
	    public void setUp() {
	        MockitoAnnotations.initMocks(this);
	        currentTime.setTimeInMillis(100);

	        qcPlot = new PlotView.Builder(PlotterPlot.class).build();

	        Mockito.when(mockPlotMan.getCurrentMCTTime()).thenReturn(100L);
	        Mockito.when(mockPlotMan.getPlot()).thenReturn(qcPlot);
	        robot = BasicRobot.robotWithCurrentAwtHierarchy();
	       
	        GuiActionRunner.execute(new GuiTask() {
	            @Override
	            protected void executeInEDT() throws Throwable {
	                controlPanel = new PlotSettingsControlPanel(mockPlotMan);
	             
	                // replace the control panel;s controller with our test controller
	                controller = new  MyPlotSettingController(controlPanel);
	                controlPanel.controller = controller;
	                
	                JFrame frame = new JFrame(TITLE);
	                frame.setName(TITLE);
	                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                controlPanel.setOpaque(true); // content panes must be opaque
	                frame.setContentPane(controlPanel);
	                frame.pack();
	                frame.setVisible(true);
	            }
	        });
	    }
	  
	  @AfterSuite
	  @Test (enabled = false)
	  public void tearDown() {
		  robot.cleanUp();
	  }
	  
	  @Test (enabled = false)
	  public void testEnableAndDisableOfResetAndApplyButtons() {
		  FrameFixture window = WindowFinder.findFrame(TITLE).using(robot);
		  
		  // locate the Apply and cancel buttons
		  JButtonFixture applyButton = window.button(new ButtonMatcher(BUNDLE.getString("Apply.label")));
		  JButtonFixture resetButton = window.button(new ButtonMatcher(BUNDLE.getString("Reset.label")));
		  
		  applyButton.requireDisabled();
		  resetButton.requireDisabled();
		  
		  JToggleButtonFixture yAxisButton = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("YAxisAsTime.label")));
		  yAxisButton.click();
		  
		  applyButton.requireEnabled();
		  resetButton.requireEnabled();
		 
		  JToggleButtonFixture xAxisButton = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("XAxisAsTime.label")));
		  xAxisButton.click();
		  
		  applyButton.requireDisabled();
		  resetButton.requireDisabled();	
		 
		  yAxisButton.click();
		  resetButton.click();
		  
		  applyButton.requireDisabled();
		  resetButton.requireDisabled();
		  
		  xAxisButton.requireSelected();
	  }
	  
	  @Test (enabled = false)
	  public void testTimeOnXAndYAxis() {
		  FrameFixture window = WindowFinder.findFrame(TITLE).using(robot);
		  
		  // locate the Apply and cancel buttons
		  JButtonFixture applyButton = window.button(new ButtonMatcher(BUNDLE.getString("Apply.label")));
		  JButtonFixture resetButton = window.button(new ButtonMatcher(BUNDLE.getString("Reset.label")));
		  
		  applyButton.requireDisabled();
		  resetButton.requireDisabled();
		 
		  JToggleButtonFixture xAxisButton = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("XAxisAsTime.label")));
		  xAxisButton.requireSelected();
		  
		  JToggleButtonFixture yAxisButton = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("YAxisAsTime.label")));
		  yAxisButton.click();
		  applyButton.click();
		  
		  Assert.assertEquals(controller.timeAxisSetting, AxisOrientationSetting.Y_AXIS_AS_TIME);
	  }
	  
	  @Test (enabled = false)
	  public void testXAxisMaxAtLeftAndRight() {
		  FrameFixture window = WindowFinder.findFrame(TITLE).using(robot);
		  
		  // locate the Apply and cancel buttons
		  JButtonFixture applyButton = window.button(new ButtonMatcher(BUNDLE.getString("Apply.label")));
		  
		  JToggleButtonFixture maxAtRight = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("MaxAtRight.label")));
		  maxAtRight.requireSelected();
		  
		  JToggleButtonFixture maxAtLeft = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("MaxAtLeft.label")));
		  maxAtLeft.click();
		  applyButton.click();
		  
		  Assert.assertEquals(controller.xAxisMaximumLocation, XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
	  }

	  @Test (enabled = false)
	  public void testYAxisMaxAtTopAndBottom() {
          FrameFixture window = WindowFinder.findFrame(TITLE).using(robot);
		  
		  // locate the Apply and cancel buttons
		  JButtonFixture applyButton = window.button(new ButtonMatcher(BUNDLE.getString("Apply.label")));
		  
		  JToggleButtonFixture maxAtTop = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("MaxAtTop.label")));
		  maxAtTop.requireSelected();
		  
		  JToggleButtonFixture maxAtBottom = window.toggleButton(new ToggleButtonMatcher(BUNDLE.getString("MaxAtBottom.label")));
		  maxAtBottom.click();
		  applyButton.click();
		  
		  Assert.assertEquals(controller.yAxisMaximumLocation, YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);  
	  }
	  
	  
     class MyPlotSettingController extends PlotSettingController {
		  
		  public MyPlotSettingController(PlotSettingsControlPanel controlPanel) {
               super(controlPanel);  
		  }
		  
		  @Override
		  public void createPlot() {
			  //do nothing.
		  }
		  
	  }
	  
	  private static class ButtonMatcher extends GenericTypeMatcher<JButton> {
	        private final String label;
	        
	        public ButtonMatcher(String label) {
	            super(JButton.class, true);
	            this.label = label;
	        }
	        
	        @Override
	        protected boolean isMatching(JButton cb) {
	            return label.equals(cb.getAccessibleContext().getAccessibleName()) ||
	                   label.equals(cb.getToolTipText());
	        }
	        
	    }
	  
	  private static class ToggleButtonMatcher extends GenericTypeMatcher<JToggleButton> {
	        private final String label;
	        
	        public ToggleButtonMatcher(String label) {
	            super(JToggleButton.class, true);
	            this.label = label;
	        }
	        
	        @Override
	        protected boolean isMatching(JToggleButton cb) {
	            return label.equals(cb.getAccessibleContext().getAccessibleName()) ||
	                   label.equals(cb.getToolTipText());
	        }
	        
	    }
	 	 
}