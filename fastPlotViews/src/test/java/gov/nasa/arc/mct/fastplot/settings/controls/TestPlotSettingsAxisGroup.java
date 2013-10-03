package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.settings.controls.TestPlotSettingsAxisGroup.PlotSettingsAxisGroupTester.BoundOption;
import gov.nasa.arc.mct.fastplot.view.NumericTextField;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.fastplot.view.TimeDuration;
import gov.nasa.arc.mct.fastplot.view.TimeSpanTextField;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotSettingsAxisGroup {
	private static final String TEST_NAME = "TestName";
	
	private static final double EPSILON   = 0.01;
	private static final int SETTINGS_MIN = -100;
	private static final int SETTINGS_MAX =  100;
	private static final int PLOT_MIN     = -50;
	private static final int PLOT_MAX     =  50;
	private static final int SPAN         =  33;
	

	private static final long PLOT_MIN_TIME =  1000;
	private static final long PLOT_MAX_TIME = 31000;
	
	private static final String[] TEST_TITLES = { "Title0", "Title1" };
	
	private PlotSettings settings;
	
	@Mock PlotViewManifestation view;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(view.getCurrentMCTTime()).thenReturn(PLOT_MIN_TIME);
		settings = new PlotSettings();
		settings.setMinNonTime(SETTINGS_MIN);
		settings.setMaxNonTime(SETTINGS_MAX);
	}
	
	@Test
	public void testNonTimeManualMatchesSettings() {
		PlotSettingsAxisGroupTester axisGroup = new PlotSettingsAxisGroupTester(false);
		axisGroup.reset(settings, true);

		JComponent minManual = axisGroup.getControl(false, BoundOption.MANUAL, true);
		JComponent maxManual = axisGroup.getControl(true , BoundOption.MANUAL, true);

		Assert.assertEquals(Double.parseDouble(((JTextField) minManual).getText()), 
				SETTINGS_MIN, EPSILON);
		Assert.assertEquals(Double.parseDouble(((JTextField) maxManual).getText()), 
				SETTINGS_MAX, EPSILON);		
	}

	@Test
	public void testNonTimeCurrentMatchesPlot() {
		PlotSettingsAxisGroupTester axisGroup = new PlotSettingsAxisGroupTester(false);
		axisGroup.reset(settings, true);
		axisGroup.updateFrom(view);

		JLabel min = (JLabel) axisGroup.getControl(false, BoundOption.CURRENT, true);
		JLabel max = (JLabel) axisGroup.getControl(true , BoundOption.CURRENT, true);

		Assert.assertEquals(Double.parseDouble(min.getText().replaceAll("[\\(\\)]", "")), 
				PLOT_MIN, EPSILON);
		Assert.assertEquals(Double.parseDouble(max.getText().replaceAll("[\\(\\)]", "")), 
				PLOT_MAX, EPSILON);		
	}
	
	@Test
	public void testNonTimeAutoMatchesSpan() {
		PlotSettingsAxisGroupTester axisGroup = new PlotSettingsAxisGroupTester(false);
		axisGroup.reset(settings, true);
		axisGroup.updateFrom(view);
		
		NumericTextField span =  (NumericTextField) axisGroup.getSpanControl(true);
		span.setValue(SPAN);		
		
		JLabel min = (JLabel) axisGroup.getControl(false, BoundOption.AUTO, true);
		JLabel max = (JLabel) axisGroup.getControl(true , BoundOption.AUTO, true);
		
		((JRadioButton) axisGroup.getControl(false, BoundOption.MANUAL, false)).doClick();
		span.setValue(SPAN);	
		((JRadioButton) axisGroup.getControl(true , BoundOption.AUTO, false)).doClick();			
		Assert.assertEquals(Double.parseDouble(max.getText().replaceAll("[\\(\\)]", "")), 
				SETTINGS_MIN + SPAN, EPSILON);		

		((JRadioButton) axisGroup.getControl(true , BoundOption.MANUAL, false)).doClick();
		span.setValue(SPAN);	
		((JRadioButton) axisGroup.getControl(false, BoundOption.AUTO, false)).doClick();			
		Assert.assertEquals(Double.parseDouble(min.getText().replaceAll("[\\(\\)]", "")), 
				SETTINGS_MAX - SPAN, EPSILON);		

	}
	

	@Test
	public void testTimeCurrentMatchesPlot() {
		PlotSettingsAxisGroupTester axisGroup = new PlotSettingsAxisGroupTester(true);
		axisGroup.reset(settings, true);
		axisGroup.updateFrom(view);

		JLabel min = (JLabel) axisGroup.getControl(false, BoundOption.CURRENT, true);
		JLabel max = (JLabel) axisGroup.getControl(true , BoundOption.CURRENT, true);

		Assert.assertEquals(min.getText().replaceAll("[\\(\\)]", ""), "001/00:00:01 1969");
		Assert.assertEquals(max.getText().replaceAll("[\\(\\)]", ""), "001/00:00:31 1969");		
	}

	@Test
	public void testTimeAutoMatchesSpan() {
		PlotSettingsAxisGroupTester axisGroup = new PlotSettingsAxisGroupTester(true);
		axisGroup.reset(settings, true);
		axisGroup.updateFrom(view);
		
		JLabel max = (JLabel) axisGroup.getControl(true , BoundOption.AUTO, true);
		
		TimeSpanTextField span =  (TimeSpanTextField) axisGroup.getSpanControl(true);
		
		((JRadioButton) axisGroup.getControl(false, BoundOption.CURRENT, false)).doClick();
		span.setTime(new TimeDuration(15000l));
		((JRadioButton) axisGroup.getControl(true , BoundOption.AUTO, false)).doClick();			
		Assert.assertEquals(max.getText().replaceAll("[\\(\\)]", ""), "001/00:00:16 1969");
	}

	
	@Test
	public void testGetSetTitle() {		
		PlotSettingsAxisGroupTester axisGroup = new PlotSettingsAxisGroupTester(true);
		for (String s : TEST_TITLES) {
			axisGroup.setTitle(s);
			Assert.assertEquals(axisGroup.getTitle(), s);
		}
	}
	
	public static class PlotSettingsAxisGroupTester extends PlotSettingsAxisGroup {
		private static final long serialVersionUID = -7947400808583335533L;
		private boolean temporal;
		
		public PlotSettingsAxisGroupTester(boolean temporal) {
			super(temporal);
			this.temporal = temporal;
			setName(TEST_NAME);
			add(getMinControls());
			add(getMaxControls());
			add(getSpanControls());
		}

		@Override
		public void setBounds(PlotConfiguration settings, double min, double max) {
			settings.setMinNonTime(min);
			settings.setMaxNonTime(max);
		}

		@Override
		public double getBoundMinimum(PlotConfiguration settings) {
			return temporal ? settings.getMinTime() : settings.getMinNonTime();
		}

		@Override
		public double getBoundMaximum(PlotConfiguration settings) {
			return temporal ? settings.getMaxTime() : settings.getMaxNonTime();
		}

		@Override
		public double getActualMinimum(PlotViewManifestation view) {
			return temporal ? PLOT_MIN_TIME : PLOT_MIN;
		}

		@Override
		public double getActualMaximum(PlotViewManifestation view) {
			return temporal ? PLOT_MAX_TIME : PLOT_MAX;
		}
		
		public JComponent getControl (boolean maximal, BoundOption boundOpt, boolean isValue) {
			return find(this, TEST_NAME +
					(maximal ? MAX_SUFFIX : MIN_SUFFIX) +
					boundOpt.getSuffix() +
					(isValue ? VALUE_SUFFIX : "")
					);
		}
		
		public JComponent getSpanControl (boolean isValue) {
			return find(this, TEST_NAME +
					SPAN_SUFFIX +
					(isValue ? VALUE_SUFFIX : "")
					);
		}
		
		private JComponent find(JComponent parent, String name) {
			if (parent.getName() != null && parent.getName().equals(name)) return parent;
			for (Component c : parent.getComponents()) {
				if (c != null && c instanceof JComponent) {
					JComponent child = find((JComponent) c, name);
					if (child != null) {
						return child;
					}
				}
			}
			return null;
		}
		
		public enum BoundOption {
			AUTO()    { public String getSuffix() { return AUTO_SUFFIX;    } } ,
			CURRENT() { public String getSuffix() { return CURRENT_SUFFIX; } } ,
			MANUAL()  { public String getSuffix() { return MANUAL_SUFFIX;  } } ;
			public abstract String getSuffix();
		}
		
	}
}
