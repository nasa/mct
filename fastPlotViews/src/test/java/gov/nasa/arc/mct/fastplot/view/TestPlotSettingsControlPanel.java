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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.CalendarDump;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.NonTimeFieldFocusListener;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.PaddingFilter;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.ParenthesizedNumericLabel;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.ParenthesizedTimeLabel;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.TimeAxisModeListener;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.TimeFieldFocusListener;
import gov.nasa.arc.mct.fastplot.view.PlotSettingsControlPanel.TimePaddingFocusListener;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.PlainDocument;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotSettingsControlPanel {

	
	private AbstractComponent mockComponent;
	@Mock private PersistenceProvider mockProvider;
	@Mock private Platform mockPlatform;
	
	@BeforeMethod
	public void setup() {
		mockComponent = new DummyComponent();
		MockitoAnnotations.initMocks(this);
		new PlatformAccess().setPlatform(mockPlatform);
		Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockProvider);
	}
	
	@AfterMethod
	public void teardown() {
		new PlatformAccess().setPlatform(null);
	}
	
	@Test
	public void testInitialization() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.thePlot = new PlotView.Builder(PlotterPlot.class).build();

		PlotSettingsControlPanel controlPanel = new PlotSettingsControlPanel(panel);
		Assert.assertTrue(controlPanel.getLayout().getClass().equals(BorderLayout.class));

		PlotSettingsControlPanel.XAxisButtonsPanel xButtonsPanel = controlPanel.new XAxisButtonsPanel();
		Assert.assertEquals(xButtonsPanel.getComponentCount(), 0);
		xButtonsPanel.insertMinMaxPanels(new JPanel(new CardLayout()), new JPanel(new GridBagLayout()));
		xButtonsPanel.setNormalOrder(false);
		Assert.assertEquals(xButtonsPanel.getComponentCount(), 2);

		PlotSettingsControlPanel.YAxisButtonsPanel yButtonsPanel = controlPanel.new YAxisButtonsPanel();
		Assert.assertEquals(yButtonsPanel.getComponentCount(), 0);
		yButtonsPanel.insertMinMaxPanels(new JPanel(new CardLayout()), new JPanel(new GridBagLayout()));
		yButtonsPanel.setNormalOrder(false);
		Assert.assertEquals(yButtonsPanel.getComponentCount(), 3);

		PlotSettingsControlPanel.XAxisAdjacentPanel xAdjacentPanel = controlPanel.new XAxisAdjacentPanel();
		Assert.assertEquals(xAdjacentPanel.getComponentCount(), 0);
		xAdjacentPanel.setNormalOrder(false);
		Assert.assertEquals(xAdjacentPanel.getComponentCount(), 3);

		PlotSettingsControlPanel.StillPlotImagePanel imagePanel = controlPanel.new StillPlotImagePanel();
		imagePanel.setImageToTimeOnXAxis(true);
		Assert.assertEquals(imagePanel.getComponentCount(), 1);
		imagePanel.setImageToTimeOnYAxis(true);
		Assert.assertEquals(imagePanel.getComponentCount(), 1);

		controlPanel.updateNonTimeAxisControls();
		NumericTextField span = controlPanel.nonTimeSpanValue;
		Assert.assertFalse(span.isEnabled());

		controlPanel.updateTimeAxisControls();
		JComponent timeSpan = controlPanel.timeSpanValue;
		Assert.assertFalse(timeSpan.isEnabled());

		JSpinner checkComp = new JSpinner();
		PlotSettingsControlPanel.XAxisSpanCluster cluster = controlPanel.xAxisSpanCluster;
		cluster.setSpanField(checkComp);
		Component[] components = cluster.getComponents();
		List<Component> list = Arrays.asList(components);
		Assert.assertTrue(list.contains(checkComp));

		JTextField checkField = new JTextField();
		PlotSettingsControlPanel.YAxisSpanPanel spanPanel = controlPanel.yAxisSpanPanel;
		spanPanel.setSpanField(checkField);
		Component[] innerComps = spanPanel.getComponents();
		List<Component> list2 = Arrays.asList(innerComps);
		Assert.assertTrue(list2.contains(checkField));

	}

	/* Test Time axis setup */

	
	@Test
	/**
	 * This is a crude and limited test in that it insures only that valid setting can be pushed through
	 * for all permutations of axisOrientaiotn, xmax loc, ymax loc, time axis subsequent, nontime subsequent etc.
	 */
	public void testSetupPlot() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.thePlot = new PlotView.Builder(PlotterPlot.class).build();

		PlotSettingsControlPanel controlPanel = new PlotSettingsControlPanel(panel);

		GregorianCalendar minTime = new GregorianCalendar();
		GregorianCalendar maxTime = new GregorianCalendar();
		maxTime.add(Calendar.MINUTE, 10);

		// Push all options through the setup routines to expose if any have not been implemented. 
		for(AxisOrientationSetting axisO : AxisOrientationSetting.values()) {
			for (XAxisMaximumLocationSetting xAxisMax: XAxisMaximumLocationSetting.values()) {
				for (YAxisMaximumLocationSetting  yAxisMax: YAxisMaximumLocationSetting.values()) {
					for (TimeAxisSubsequentBoundsSetting timeSubsequent: TimeAxisSubsequentBoundsSetting.values()) {
						for (NonTimeAxisSubsequentBoundsSetting nonTimeMinSubsequent: NonTimeAxisSubsequentBoundsSetting.values()) {
							for (NonTimeAxisSubsequentBoundsSetting nonTimeMaxSubsequent: NonTimeAxisSubsequentBoundsSetting.values()) {

								controlPanel.setControlPanelState(axisO, 
										PlotConstants.DEFAULT_TIME_SYSTEM,
                                        PlotConstants.DEFAULT_TIME_FORMAT,
										xAxisMax, 
										yAxisMax, 
										timeSubsequent,										                  
										nonTimeMinSubsequent,
										nonTimeMaxSubsequent, 
										10, // non time max
										9, // non time min
										minTime.getTimeInMillis(), // min time
										maxTime.getTimeInMillis(), // max time
										0.5, // time padding
										0.5, // non time padding max
										0.49, // non time padding min
										true,
										true,
										PlotConstants.DEFAULT_PLOT_LINE_DRAW,
										PlotLineConnectionType.STEP_X_THEN_Y
								);							
								controlPanel.setupPlot();	
							}
						}
					}
				}
			}
		}		
	}


	@Test
	public void testParenthesizedLabel() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.thePlot = new PlotView.Builder(PlotterPlot.class).build();

		PlotSettingsControlPanel controlPanel = new PlotSettingsControlPanel(panel);
		JRadioButton button = new JRadioButton();
		ParenthesizedTimeLabel label = controlPanel.new ParenthesizedTimeLabel(button);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		int hour = 14;
		int minute = 46;
		int second = 57;
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		long markerTime = calendar.getTimeInMillis();
		label.setTime(calendar);
		Assert.assertEquals(label.getCalendar(), calendar);
		Assert.assertEquals(label.getTimeInMillis(), markerTime);
		Assert.assertEquals(label.getSavedTime(), CalendarDump.dumpDateAndTime(calendar));
		Assert.assertEquals(label.getHourOfDay(), hour);
		Assert.assertEquals(label.getMinute(), minute);
		Assert.assertEquals(label.getSecond(), second);
		Assert.assertEquals(label.getSavedTime(), CalendarDump.dumpMillis(calendar.getTimeInMillis()));
		

		// Supplemental tests for ParenthesizedNumericLabel
		ParenthesizedNumericLabel numericLabel = controlPanel.new ParenthesizedNumericLabel(button);
		numericLabel.setText(null);
		Assert.assertNull(numericLabel.getValue());
		numericLabel.setText("x");
		Assert.assertNull(numericLabel.getValue());

		// Focus listener tests
		JTextField textField = new JTextField();
		FocusEvent event1 = new FocusEvent(textField, FocusEvent.FOCUS_GAINED);
		FocusEvent event2 = new FocusEvent(textField, FocusEvent.FOCUS_LAST);
		FocusEvent tempEvent1 = new FocusEvent(textField, FocusEvent.FOCUS_GAINED) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isTemporary() {
				return true;
			}
		};
		FocusEvent tempEvent2 = new FocusEvent(textField, FocusEvent.FOCUS_GAINED) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isTemporary() {
				return true;
			}
		};
		JRadioButton assocButton = new JRadioButton();
		NonTimeFieldFocusListener nontimeListener = controlPanel.new NonTimeFieldFocusListener(assocButton);
		nontimeListener.focusGained(event1);
		nontimeListener.focusLost(event2);
		nontimeListener.focusGained(tempEvent1);
		nontimeListener.focusLost(tempEvent2);
		Assert.assertFalse(assocButton.hasFocus());

		assocButton.setSelected(false);
		TimeFieldFocusListener timeListener = controlPanel.new TimeFieldFocusListener(assocButton);
		timeListener.focusGained(event1);
		Assert.assertTrue(assocButton.isSelected());
		timeListener.focusLost(event2);
		Assert.assertFalse(assocButton.hasFocus());
		assocButton.setSelected(false);
		timeListener.focusGained(tempEvent1);
		Assert.assertFalse(assocButton.isSelected());
		timeListener.focusLost(tempEvent2);
		Assert.assertFalse(assocButton.hasFocus());
		//
		TimePaddingFocusListener paddingListener = controlPanel.new TimePaddingFocusListener();
		paddingListener.focusLost(tempEvent1);
		paddingListener.focusLost(event1);
		textField.setText("zzz");
		TimeAxisModeListener modeListener = controlPanel.new TimeAxisModeListener(textField);
		modeListener.actionPerformed(null);
		Assert.assertEquals(textField.getSelectedText(), textField.getText());
		//
		PaddingFilter paddingFilter = controlPanel.new PaddingFilter();
		FilterBypass fb = new FilterBypass() {
			@Override
			public void replace(int offset, int length, String string,
					AttributeSet attrs) throws BadLocationException {
			}
			@Override
			public void remove(int offset, int length) throws BadLocationException {
			}
			@Override
			public void insertString(int offset, String string, AttributeSet attr)
					throws BadLocationException {
			}
			@Override
			public Document getDocument() {
				return new PlainDocument();
			}
		};
		try {
			paddingFilter.insertString(fb, 0, "xxx", null);
			Assert.assertEquals(paddingFilter.getInsertBuilder().toString(), "");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		try {
			paddingFilter.replace(fb, 0, 1, "xxx", null);
			Assert.assertEquals(paddingFilter.getReplaceBuilder().toString(), "");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	@Test 
	void testFindSelectedButtons() {
		JToggleButton selectedButton = new JToggleButton();
		selectedButton.setSelected(true);
		JToggleButton notSelectedButton = new JToggleButton();
		notSelectedButton.setSelected(false);	
		List<JToggleButton> selectedButtons = PlotSettingsControlPanel.findSelectedButtons (selectedButton, notSelectedButton);
		Assert.assertEquals(selectedButtons.size(), 1);
		Assert.assertTrue(selectedButtons.contains(selectedButton));
	}
	
	@Test
	void testbuttonStateMatch() {
		JToggleButton button1= new JToggleButton();
		JToggleButton button2 = new JToggleButton();
		JToggleButton button3= new JToggleButton();
		
		
		ArrayList<JToggleButton> list1 = new ArrayList<JToggleButton>();
		list1.add(button2);
		list1.add(button3);
		
		ArrayList<JToggleButton> list2 = new ArrayList<JToggleButton>();
		list2.add(button3);
		list2.add(button2);
		
		ArrayList<JToggleButton> list3 = new ArrayList<JToggleButton>();
		list3.add(button1);
		list3.add(button3);
		
		ArrayList<JToggleButton> list4 = new ArrayList<JToggleButton>();
		list4.add(button3);
		
	 	Assert.assertFalse(PlotSettingsControlPanel.buttonStateMatch(list1, list3));
	 	Assert.assertFalse(PlotSettingsControlPanel.buttonStateMatch(list1, list4));
	 	Assert.assertTrue(PlotSettingsControlPanel.buttonStateMatch(list1, list2));
	}
	
	@Test
	public void testIsDirtyNonTimeSubsequentSettings() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.thePlot = new PlotView.Builder(PlotterPlot.class).build();
		
		PlotSettingsControlPanel controlPanel = new PlotSettingsControlPanel(panel);
		Assert.assertFalse(controlPanel.isPanelDirty());
		
		// Uncheck semifixed mode.
		controlPanel.xAxisAsTimeRadioButton.setSelected(false);
		controlPanel.yAxisAsTimeRadioButton.setSelected(true);
		//controlPanel.nonTimeMinSemiFixedMode.setSelected(false);
		
		// panel should now be dirty.
		Assert.assertTrue(controlPanel.isPanelDirty());
		
		// Set back to the original state.
		//controlPanel.nonTimeMinSemiFixedMode.setSelected(true);
		controlPanel.xAxisAsTimeRadioButton.setSelected(true);
		Assert.assertFalse(controlPanel.isPanelDirty());
	}
	
	@Test
	public void testApplyButtonState() {
		PlotViewManifestation panel = new PlotViewManifestation(mockComponent, new ViewInfo(PlotViewManifestation.class,"",ViewType.OBJECT));
		panel.thePlot = new PlotView.Builder(PlotterPlot.class).build();
		
		PlotSettingsControlPanel controlPanel = new PlotSettingsControlPanel(panel);
		Assert.assertFalse(controlPanel.isPanelDirty());
		
		// Switch to scrunch mode
		controlPanel.timeJumpMode.setSelected(false);
		controlPanel.timeScrunchMode.setSelected(true);
		controlPanel.timeScrunchMode.doClick();
		
		// panel should now be dirty.
		Assert.assertTrue(controlPanel.isPanelDirty());
		
		JButton okButton = (JButton) findNamedComponent(controlPanel, "okButton");
		
		// we should have an ok button, and it should be enabled
		Assert.assertNotNull(okButton);
		Assert.assertTrue(okButton.isEnabled());
		
		// clicking should disable the ok button
		okButton.doClick();
		Assert.assertFalse(okButton.isEnabled());		
	}
	
	private JComponent findNamedComponent(JComponent comp, String name) {
		if (comp.getName() != null && comp.getName().equals(name)) return comp;
		for (Component c : comp.getComponents()) {
			if (c instanceof JComponent) {
				JComponent found = findNamedComponent((JComponent) c, name);
				if (found != null) return found;
			}
		}
		return null;
	}
	
	@Test
	public void testXMaxAtLeftOrRightButtonState() {
		
	}
	
	private static class DummyComponent extends AbstractComponent {
		public DummyComponent() {
		}
	}
}
