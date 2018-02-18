package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;

import java.awt.Component;

import javax.swing.AbstractButton;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotSettingsCheckBox {
	private PlotSettings settings;
		
	private static final boolean[] STATES = { false, true };
	
	@BeforeMethod
	public void setup() {
		settings = new PlotSettings();
	}
	
	@Test
	public void testInitialSetting() {
		for (boolean b : STATES) {
			Tester t = new Tester();
			settings.setOrdinalPositionForStackedPlots(b);
			t.reset(settings, true);
			Assert.assertEquals(t.isSelected(), b);
		}
	}
	
	@Test
	public void testSoftReset() {
		for (boolean b : STATES) {
			Tester t = new Tester();
			settings.setOrdinalPositionForStackedPlots(b);
			t.reset(settings, true);
			Assert.assertEquals(t.isSelected(), b);
			settings.setOrdinalPositionForStackedPlots(!b);
			t.reset(settings, false);
			Assert.assertEquals(t.isSelected(), b); // Should not change
		}
	}

	@Test
	public void testHardReset() {
		for (boolean b : STATES) {
			Tester t = new Tester();
			settings.setOrdinalPositionForStackedPlots(b);
			t.reset(settings, true);
			Assert.assertEquals(t.isSelected(), b);
			settings.setOrdinalPositionForStackedPlots(!b);
			t.reset(settings, true);
			Assert.assertEquals(t.isSelected(), !b); // Should change
		}
	}
	
	@Test
	public void testValidAndDirtyFlags() {
		for (boolean b : STATES) {
			Tester t = new Tester();
			settings.setOrdinalPositionForStackedPlots(b);
			t.reset(settings, true);
			for (int i = 0; i < 10; i++) {
				// Should always be valid
				Assert.assertTrue(t.isValidated());
				// Should be dirty every other click
				
				Assert.assertEquals(t.isDirty(), i % 2 == 1);
				t.click();
			}
		}		
	}
	
	private static class Tester extends PlotSettingsCheckBox {
		private static final long serialVersionUID = 1019496761275527323L;

		public Tester() {
			super("Tester");
		}
		@Override
		public boolean getFrom(PlotConfiguration settings) {
			return settings.getOrdinalPositionForStackedPlots();
		}
		@Override
		public void populate(PlotConfiguration settings) {
			settings.setOrdinalPositionForStackedPlots(isSelected());
		}		
		
		public void click() {
			for (Component c : getComponents()) {
				if (c instanceof AbstractButton) {
					((AbstractButton) c).doClick();
				}
			}
		}
	}
}
