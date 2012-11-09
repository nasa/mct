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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.view.Axis;
import gov.nasa.arc.mct.fastplot.view.PinSupport;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotCornerResetButtonManager {
	@Mock 
	private PlotterPlot plot;
	@Mock
	private PlotLocalControlsManager plotControlManager;
	@Mock
	private QCPlotObjects qcPlotObjects;
    @Mock
    private PanAndZoomManager panAndZoomManager;
    @Mock
    private PlotAbstraction plotAbstraction;
    @Mock
    private PlotLimitManager limitManager;
    @Mock
    private PlotDataManager dataManager;
	
	PlotCornerResetButtonManager pcm;
	private boolean topLeftVisible;
	private boolean topRightVisible;
	private boolean bottomLeftVisible;
	private boolean bottomRightVisible;


	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(plot.getLocalControlsManager()).thenReturn(plotControlManager);
		Mockito.when(plot.getPlotAbstraction()).thenReturn(plotAbstraction);
		Mockito.when(plot.getLimitManager()).thenReturn(limitManager);
		Mockito.when(plot.getPlotDataManager()).thenReturn(dataManager);
		plot.localControlsManager = plotControlManager;
		plot.panAndZoomManager = panAndZoomManager;

		pcm = new PlotCornerResetButtonManager(plot);
    	
    	PinSupport pins = new PinSupport();
    	Mockito.when(plotAbstraction.getCurrentMCTTime()).thenReturn(new GregorianCalendar().getTimeInMillis());
		Mockito.when(plotAbstraction.getSubPlots()).thenReturn(Arrays.asList((AbstractPlottingPackage) plot));
    	Mockito.when(plotAbstraction.getTimeAxis()).thenReturn(new Axis());
    	Mockito.doAnswer(new Answer<Object>() {
    		@Override
    		public Object answer(InvocationOnMock invocation) throws Throwable {
    			plot.updateResetButtons();
    			return null;
    		}
		}).when(plotAbstraction).updateResetButtons();
    	Mockito.when(plotAbstraction.getTimeAxisUserPin()).thenReturn(pins.createPin());
    	Mockito.when(plot.getNonTimeAxis()).thenReturn(new Axis());
    	PinSupport pins2 = new PinSupport();
    	Mockito.when(plot.getNonTimeAxisUserPin()).thenReturn(pins2.createPin());
    	Mockito.when(plot.getCurrentTimeAxisMax()).thenReturn(new GregorianCalendar());
    	Mockito.when(plot.getCurrentTimeAxisMin()).thenReturn(new GregorianCalendar());
    	Mockito.doAnswer(new Answer<Object>() {
    		@Override
    		public Object answer(InvocationOnMock invocation) throws Throwable {
    			pcm.updateButtons();
    			return null;
    		}
		}).when(plot).updateResetButtons();
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				topRightVisible = ((Boolean) invocation.getArguments()[0]).booleanValue();
				return null;
			}
		}).when(plotControlManager).setJumpToCurrentTimeButtonVisible(true);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				bottomRightVisible = ((Boolean) invocation.getArguments()[0]).booleanValue();
				return null;
			}
		}).when(plotControlManager).setXAxisCornerResetButtonVisible(true);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				topLeftVisible = ((Boolean) invocation.getArguments()[0]).booleanValue();
				return null;
			}
		}).when(plotControlManager).setYAxisCornerResetButtonVisible(true);
		Mockito.doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				bottomRightVisible = ((Boolean) invocation.getArguments()[0]).booleanValue();
				return null;
			}
		}).when(plotControlManager).setXAndYAxisCornerResetButtonVisible(true);
	}
	
	@Test
	public void testInformResetYAxisSelcted() {
		plot.setAxisOrientationSetting( AxisOrientationSetting.X_AXIS_AS_TIME );
		Mockito.when(plot.isPaused()).thenReturn(true);
		plot.isTimeLabelEnabled = true;
		pcm.informResetYAxisActionSelected();
		assertFalse(topRightVisible);
		assertFalse(bottomLeftVisible);
		
		verify(plot).refreshDisplay();
		
		// Flip axis orientation and recall. Make sure the right scroll frame is called.
		plot.setAxisOrientationSetting( AxisOrientationSetting.Y_AXIS_AS_TIME );
		pcm.informResetYAxisActionSelected();
	} 
	
	@Test
	public void testInformResetXAxisSelcted() {
		plot.setAxisOrientationSetting( AxisOrientationSetting.X_AXIS_AS_TIME );
		Mockito.when(plot.isPaused()).thenReturn(true);
		plot.isTimeLabelEnabled = true;
		pcm.informResetXAxisActionSelected();
		assertFalse(bottomRightVisible);
		assertFalse(bottomLeftVisible);
		
		verify(plot).refreshDisplay();
		
		// Flip axis orientation and recall. Make sure the right scroll frame is called.
		plot.setAxisOrientationSetting( AxisOrientationSetting.Y_AXIS_AS_TIME );
		pcm.informResetYAxisActionSelected();
	} 
	
	@Test
	public void testInformResetXAndYAxisSelcted() {
		plot.setAxisOrientationSetting( AxisOrientationSetting.X_AXIS_AS_TIME );
		Mockito.when(plot.isPaused()).thenReturn(true);
		plot.isTimeLabelEnabled = true;
		pcm.informResetXAndYActionSelected();
		assertFalse(bottomRightVisible);
		assertFalse(topLeftVisible);
		assertFalse(bottomLeftVisible);
		
		verify(plot).refreshDisplay();
		
		// Flip axis orientation and recall. Make sure the right scroll frame is called.
		plot.setAxisOrientationSetting( AxisOrientationSetting.Y_AXIS_AS_TIME );
		pcm.informResetYAxisActionSelected();
	}
	
	@Test
	public void resetX() {
		Mockito.when(plot.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		plot.setNonTimeMinFixedByPlotSettings(false);
		plot.setNonTimeMaxFixedByPlotSettings(false);
		pcm.resetX();
		verify(plot, never()).setNonTimeMinFixed(false);
		verify(plot, never()).setNonTimeMaxFixed(false);
		
		Mockito.when(plot.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.Y_AXIS_AS_TIME);
		Mockito.when(plot.isPaused()).thenReturn(true);
		pcm.resetX();
		verify(plot).setNonTimeMinFixed(false);
		verify(plot).setNonTimeMaxFixed(false);
		
		Mockito.when(plot.isPaused()).thenReturn(false);
		pcm.resetX();
		verify(plot, atLeastOnce()).setNonTimeMinFixed(false);
		verify(plot, atLeastOnce()).setNonTimeMaxFixed(false);
	}
	
	@Test
	public void resetY() {
		Mockito.when(plot.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.Y_AXIS_AS_TIME);
		plot.setNonTimeMinFixedByPlotSettings(false);
		plot.setNonTimeMaxFixedByPlotSettings(false);
		
		pcm.resetY();
		verify(plot, never()).setNonTimeMinFixed(false);
		verify(plot, never()).setNonTimeMaxFixed(false);
		
		Mockito.when(plot.getAxisOrientationSetting()).thenReturn(AxisOrientationSetting.X_AXIS_AS_TIME);
		Mockito.when(plot.isPaused()).thenReturn(true);
		pcm.resetY();
		verify(plot, atLeastOnce()).setNonTimeMinFixed(false);
		verify(plot, atLeastOnce()).setNonTimeMaxFixed(false);
		
		Mockito.when(plot.isPaused()).thenReturn(false);
		pcm.resetY();
		verify(plot, atLeastOnce()).setNonTimeMinFixed(false);
		verify(plot, atLeastOnce()).setNonTimeMaxFixed(false);
	}
	
	@Test
	public void testUpdateButtonsAfterPanZoomAction() {
		MockitoAnnotations.initMocks(this);
		
		GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar startTime = new GregorianCalendar();
		GregorianCalendar endTime = new GregorianCalendar();
			
		Mockito.when(plotAbstraction.getCurrentMCTTime()).thenReturn(now.getTimeInMillis());
    	Mockito.when(plot.getCurrentTimeAxisMax()).thenReturn(endTime);
    	Mockito.when(plot.getCurrentTimeAxisMin()).thenReturn(startTime);
    	
    	// set end time after now
    	endTime.add(Calendar.MINUTE, 1);
    	
    	pcm.updateButtons();
    	verify(plotControlManager, atMost(1)).setJumpToCurrentTimeButtonVisible(true);
    	verify(plotControlManager, never()).setJumpToCurrentTimeButtonVisible(false);
    	
    	now.add(Calendar.MINUTE, -1);
    	endTime.setTimeInMillis(now.getTimeInMillis());
    	pcm.updateButtons();
    	verify(plotControlManager, atMost(2)).setJumpToCurrentTimeButtonVisible(true);
    	verify(plotControlManager, atMost(1)).setJumpToCurrentTimeButtonVisible(false);
	}
}
