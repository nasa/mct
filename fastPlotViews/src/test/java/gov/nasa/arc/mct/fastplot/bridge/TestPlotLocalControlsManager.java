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

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PanDirection;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.ZoomDirection;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.IconLoader;
import gov.nasa.arc.mct.fastplot.view.PinSupport;
import gov.nasa.arc.mct.fastplot.view.Pinnable;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.GregorianCalendar;

import javax.swing.SpringLayout;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import plotter.xy.XYPlot;
import plotter.xy.XYPlotContents;

public class TestPlotLocalControlsManager {

	@Mock 
	private PlotterPlot mockPlot;
	
	@Mock
	private XYPlot plotView;
	
	@Mock
	private  PlotCornerResetButtonManager cornerResetButtonManager;
	
	private MyPanAndZoomManager panAndZoomManager;
	
	PlotLocalControlsManager testLCM;
	
	Rectangle2D rectangle;

	@Mock
	private PlotView plotAbstraction;

	@Mock
	private AbstractAxis mockAxis;
	
	@Mock
	private Axis otherAxis;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPlot.getPlotView()).thenReturn(plotView);
		Mockito.when(mockPlot.getTimeAxis()).thenReturn(mockAxis);
		Mockito.when(mockPlot.getPlotAbstraction()).thenReturn(plotAbstraction);
		Mockito.when(plotAbstraction.getTimeAxis()).thenReturn(otherAxis);
		panAndZoomManager = new MyPanAndZoomManager(mockPlot);
		mockPlot.panAndZoomManager = panAndZoomManager;
		mockPlot.setPlotView(plotView);
		mockPlot.cornerResetButtonManager =  cornerResetButtonManager;
		mockPlot.setPlotAbstraction(plotAbstraction);
		mockPlot.setMaxTime(100);
		mockPlot.setMinTime(0);
		mockPlot.isLocalControlsEnabled = true;
		mockPlot.isTimeLabelEnabled = true;
		
		GregorianCalendar oneHundredCal = new GregorianCalendar();
		oneHundredCal.setTimeInMillis(100);
		GregorianCalendar zeroCal = new GregorianCalendar();
		zeroCal.setTimeInMillis(0);
		
		Mockito.when(mockPlot.isUserOperationsLocked()).thenReturn(false);
		rectangle = new Rectangle(0,0,100,100);
		final Axis timeAxis = new Axis();
		final PinSupport pins = new PinSupport() {
			Pinnable timeAxisPin = timeAxis.createPin();


			@Override
			protected void informPinned(boolean pinned) {
				timeAxisPin.setPinned(pinned);
			}
		};
		Mockito.when(plotAbstraction.getTimeAxis()).thenReturn(timeAxis);
    	Mockito.when(plotAbstraction.getTimeAxisUserPin()).thenReturn(pins.createPin());
    	Mockito.when(plotAbstraction.createPin()).thenAnswer(new Answer<Pinnable>() {
    		@Override
    		public Pinnable answer(InvocationOnMock invocation) throws Throwable {
    			return pins.createPin();
    		}
		});
    	Mockito.when(plotView.getContents()).thenReturn(new XYPlotContents());
    	Mockito.when(plotView.getLayout()).thenReturn(new SpringLayout());
		setTestLCM();
		
	}
	
	private void setTestLCM(){
		testLCM = new PlotLocalControlsManager(mockPlot);
		testLCM.setupLocalControlManager();	
	}
	
	@Test
	public void testPlotZoomButtons() {
		// make sure the plot zoom buttons are connected to the right action.
		Assert.assertNull(panAndZoomManager.lastZoomDirection);
		
		ActionEvent e = new ActionEvent(testLCM, 0, "test action");
		
		e.setSource(testLCM.xAxisCenterZoomInButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastZoomDirection, PlotConstants.ZoomDirection.ZOOM_IN_CENTER_X_AXIS);
		
		e.setSource(testLCM.xAxisCenterZoomOutButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastZoomDirection, PlotConstants.ZoomDirection.ZOOM_OUT_CENTER_X_AXIS);
			
		e.setSource(testLCM.yAxisCenterZoomInButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastZoomDirection, PlotConstants.ZoomDirection.ZOOM_IN_CENTER_Y_AXIS);
		
		e.setSource(testLCM.yAxisCenterZoomOutButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastZoomDirection, PlotConstants.ZoomDirection.ZOOM_OUT_CENTER_Y_AXIS);

	}
	
	@Test
	public void testPlotPanButtons() {
	  // make sure the pan buttons are connected to the right action. 
		
		Assert.assertNull(panAndZoomManager.lastPanDirection);
		
		ActionEvent e = new ActionEvent(testLCM, 0, "test action");
		e.setSource(testLCM.yAxisPanUpButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastPanDirection, PlotConstants.PanDirection.PAN_HIGHER_Y_AXIS);
		
		e.setSource(testLCM.yAxisPanDownButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastPanDirection, PlotConstants.PanDirection.PAN_LOWER_Y_AXIS);
		
		e.setSource(testLCM.xAxisPanLeftButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastPanDirection, PlotConstants.PanDirection.PAN_LOWER_X_AXIS);
		
		e.setSource(testLCM.xAxisPanRightButton);
		testLCM.actionPerformed(e);
		Assert.assertEquals(panAndZoomManager.lastPanDirection, PlotConstants.PanDirection.PAN_HIGHER_X_AXIS);
	}
	
	@Test
	public void testPauseButton() {
		Axis timeAxis = mockPlot.getPlotAbstraction().getTimeAxis();
		ActionEvent e = new ActionEvent(testLCM, 0, "test action");
		e.setSource(testLCM.pauseButton);
		testLCM.actionPerformed(e);
		Assert.assertTrue(timeAxis.isPinned());

		testLCM.actionPerformed(e);
		Assert.assertFalse(timeAxis.isPinned());
	}
	
	@Test
	public void testInformAltKeyState() {
			
		panAndZoomManager.enteredZoomMode = false;
		testLCM.informAltKeyState(true);
		Assert.assertTrue(panAndZoomManager.enteredZoomMode);
		Assert.assertTrue(testLCM.xAxisZoomButtonCenterPanel.isVisible());
		Assert.assertTrue(testLCM.yAxisZoomButtonMiddlePanel.isVisible());
		
		testLCM.informAltKeyState(false);
		Assert.assertFalse(panAndZoomManager.enteredZoomMode);
		Assert.assertFalse(testLCM.xAxisZoomButtonCenterPanel.isVisible());
		Assert.assertFalse(testLCM.yAxisZoomButtonMiddlePanel.isVisible());
		
		Mockito.when(mockPlot.isUserOperationsLocked()).thenReturn(true);
		testLCM.informAltKeyState(true);
		Assert.assertFalse(panAndZoomManager.enteredZoomMode);
		Assert.assertFalse(testLCM.xAxisZoomButtonCenterPanel.isVisible());
		Assert.assertFalse(testLCM.yAxisZoomButtonMiddlePanel.isVisible());
		
		Mockito.when(mockPlot.isUserOperationsLocked()).thenReturn(false);
	}
	
	@Test
	public void testInformCtlKeyState() {
			
		panAndZoomManager.enteredPanMode = false;
		testLCM.informCtlKeyState(true);
		Assert.assertTrue(panAndZoomManager.enteredPanMode);
		Assert.assertTrue(testLCM.xAxisPanButtonPanel.isVisible());
		Assert.assertTrue(testLCM.yAxisPanButtonPanel.isVisible());
		
		testLCM.informCtlKeyState(false);
		Assert.assertFalse(panAndZoomManager.enteredPanMode);
		Assert.assertFalse(testLCM.xAxisPanButtonPanel.isVisible());
		Assert.assertFalse(testLCM.yAxisPanButtonPanel.isVisible());
		
		Mockito.when(mockPlot.isUserOperationsLocked()).thenReturn(true);
		testLCM.informCtlKeyState(true);
		Assert.assertFalse(panAndZoomManager.enteredPanMode);
		Assert.assertFalse(testLCM.xAxisPanButtonPanel.isVisible());
		Assert.assertFalse(testLCM.yAxisPanButtonPanel.isVisible());
		
		Mockito.when(mockPlot.isUserOperationsLocked()).thenReturn(false);
	}
	
	
	@Test
	public void testMouseEnterAndExit() {
		testLCM.pauseButton.setVisible(false);
		Mockito.when(mockPlot.isPaused()).thenReturn(false);
		testLCM.informMouseEntered();
		Assert.assertTrue(testLCM.pauseButton.isVisible());
		
		testLCM.informMouseExited();
		Assert.assertFalse(testLCM.pauseButton.isVisible());
		
		testLCM.informMouseEntered();
		Assert.assertTrue(testLCM.pauseButton.isVisible());
		
		Mockito.when(mockPlot.getPlotAbstraction().isPinned()).thenReturn(true);
		testLCM.informMouseExited();
		Assert.assertTrue(testLCM.pauseButton.isVisible());
	}
	
	@Test
	public void testCornerResetButtons() {
		ActionEvent e = new ActionEvent(testLCM, 0, "test action");
		e.setSource(testLCM.topRightCornerResetButton);
		testLCM.actionPerformed(e);
		verify(cornerResetButtonManager).informJumpToCurrentTimeSelected();
		verify(cornerResetButtonManager, never()).informResetYAxisActionSelected();
		verify(cornerResetButtonManager, never()).informResetXAxisActionSelected();
		verify(cornerResetButtonManager, never()).informResetXAndYActionSelected();
		
		e.setSource(testLCM.topLeftCornerResetButton);
		testLCM.actionPerformed(e);
		verify(cornerResetButtonManager, atMost(1)).informJumpToCurrentTimeSelected();
		verify(cornerResetButtonManager).informResetYAxisActionSelected();
		verify(cornerResetButtonManager, never()).informResetXAxisActionSelected();
		verify(cornerResetButtonManager, never()).informResetXAndYActionSelected();
		
		e.setSource(testLCM.bottomRightCornerResetButton);
		testLCM.actionPerformed(e);
		verify(cornerResetButtonManager, atMost(1)).informJumpToCurrentTimeSelected();
		verify(cornerResetButtonManager, atMost(1)).informResetYAxisActionSelected();
		verify(cornerResetButtonManager).informResetXAxisActionSelected();
		verify(cornerResetButtonManager, never()).informResetXAndYActionSelected();
		
		e.setSource(testLCM. bottomLeftCornerResetButton);
		testLCM.actionPerformed(e);
		verify(cornerResetButtonManager, atMost(1)).informJumpToCurrentTimeSelected();
		verify(cornerResetButtonManager, atMost(1)).informResetYAxisActionSelected();
		verify(cornerResetButtonManager, atMost(1)).informResetXAxisActionSelected();
		verify(cornerResetButtonManager).informResetXAndYActionSelected();
		
	}
	
	@Test
	public void testSetButtonsVisible() {
		testLCM. setJumpToCurrentTimeButtonVisible(true);
		Assert.assertTrue(testLCM.topRightCornerResetButton.isVisible());
		testLCM. setJumpToCurrentTimeButtonVisible(false);
		Assert.assertFalse(testLCM.topRightCornerResetButton.isVisible());
		
		testLCM.setXAxisCornerResetButtonVisible(true);
		Assert.assertTrue(testLCM.bottomRightCornerResetButton.isVisible());
		testLCM.setXAxisCornerResetButtonVisible(false);
		Assert.assertFalse(testLCM.bottomRightCornerResetButton.isVisible());
		
		testLCM.setYAxisCornerResetButtonVisible(true);
		Assert.assertTrue(testLCM.topLeftCornerResetButton.isVisible());
		testLCM.setYAxisCornerResetButtonVisible(false);
		Assert.assertFalse(testLCM.topLeftCornerResetButton.isVisible());
		
		testLCM.setXAndYAxisCornerResetButtonVisible(true);
		Assert.assertTrue(testLCM.bottomLeftCornerResetButton.isVisible());
		testLCM.setXAndYAxisCornerResetButtonVisible(false);
		Assert.assertFalse(testLCM.bottomLeftCornerResetButton.isVisible());
	}
	
	@Test
	public void testSetupUnPauseAndResetButtonVisible() {
		testLCM.topRightCornerResetButton.setVisible(false);
		testLCM.setJumpToCurrentTimeButtonVisible(true);
		Assert.assertTrue(testLCM.topRightCornerResetButton.isVisible());
		Assert.assertEquals(testLCM.topRightCornerResetButton.getIcon(),IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY));
		testLCM.setJumpToCurrentTimeButtonVisible(false);
		Assert.assertFalse(testLCM.topRightCornerResetButton.isVisible());
		Assert.assertEquals(testLCM.topRightCornerResetButton.getIcon(),IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_GREY));
		
		testLCM.setJumpToCurrentTimeButtonAlarm(true);
		Assert.assertTrue(testLCM.topRightCornerResetButton.isVisible());
		Assert.assertEquals(testLCM.topRightCornerResetButton.getIcon(),IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_CORNER_RESET_BUTTON_TOP_RIGHT_ORANGE));
	}
	
	static private class MyPanAndZoomManager extends PanAndZoomManager {

		// Accessed by test code.
		PanDirection lastPanDirection = null;
		ZoomDirection lastZoomDirection = null;
		
		boolean enteredZoomMode = false;
		boolean enteredPanMode = false;
		
		public MyPanAndZoomManager(PlotterPlot quinnCurtisPlot) {
			super(quinnCurtisPlot);
		}
		
		@Override
		public void panAction(PanDirection panningAction) {
			lastPanDirection = panningAction;
		}
		
		@Override
		public void zoomAction(ZoomDirection zoomAction) { 
			lastZoomDirection = zoomAction;
		}
		
		@Override
		public void enteredZoomMode() {
			enteredZoomMode = true;
		}
		
		@Override
		public void exitedZoomMode() {
			enteredZoomMode = false;
		}
		
		@Override
		public void enteredPanMode() {
			enteredPanMode = true;
		}
		
		@Override
		public void exitedPanMode() {
			enteredPanMode = false;
		}
			

		@Override
		public boolean isInPanMode() {
			return enteredPanMode;
		}


		@Override
		public boolean isInZoomMode() {
			return enteredZoomMode;
		}
		}
	
}
