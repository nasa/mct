package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;

import java.awt.Component;

import javax.swing.JComboBox;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotSettingsComboBox {
	private static final String[] STRING_CHOICES = {
		"Thing one", "thing two", "thing three"
	};
	
	private static final Integer[] INTEGER_CHOICES = {
		1, 2, 3
	};
	
	private PlotConfiguration settings;
	
	@BeforeMethod
	public void setup() {
		settings = new PlotSettings();
	}
	
	
	@Test
	public void testHardAndSoftReset() {
		for (Object[] choices : new Object[][]{STRING_CHOICES, INTEGER_CHOICES}) {
			for (int i = 0; i < 3; i++) {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Tester<?> t = new Tester(choices, choices[i].getClass());
				
				if (choices[i] instanceof Integer) {
					settings.setMinTime((Integer) choices[i]);
				} else if (choices[i] instanceof String) {
					settings.setTimeSystemSetting((String) choices[i]);
				}
				t.reset(settings, true);
				Assert.assertEquals(t.getSelection(), choices[i]);
				
				if (choices[i] instanceof Integer) {
					settings.setMinTime((Integer) choices[(i+1)%3]);
				} else if (choices[i] instanceof String) {
					settings.setTimeSystemSetting((String) choices[(i+1)%3]);
				}
				t.reset(settings, false);
				Assert.assertEquals(t.getSelection(), choices[i]);
				
				if (choices[i] instanceof Integer) {
					settings.setMinTime((Integer) choices[(i+2)%3]);
				} else if (choices[i] instanceof String) {
					settings.setTimeSystemSetting((String) choices[(i+2)%3]);
				}
				t.reset(settings, true);
				Assert.assertEquals(t.getSelection(), choices[(i+2)%3]);
			}		
		}
	}
	
	@Test
	public void testPopulate() {
		for (Object[] choices : new Object[][]{STRING_CHOICES, INTEGER_CHOICES}) {
			for (int i = 0; i < 3; i++) {				 
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Tester<?> t = new Tester(choices, choices[i].getClass());
				
				if (choices[i] instanceof Integer) {
					settings.setMinTime((Integer) choices[i]);
				} else if (choices[i] instanceof String) {
					settings.setTimeSystemSetting((String) choices[i]);
				}
				t.reset(settings, true);
				Assert.assertEquals(t.getSelection(), choices[i]);
				
				t.setSelection(choices[(i+1)%3]);
				t.populate(settings);

				if (choices[i] instanceof Integer) {
					Assert.assertEquals((Integer) (int) settings.getMinTime(), choices[(i+1)%3]);
				} else if (choices[i] instanceof String) {
					Assert.assertEquals(settings.getTimeSystemSetting(), choices[(i+1)%3]);
				}
			}		
		}
	}
	
	@Test
	public void testDirty() {
		for (Object[] choices : new Object[][]{STRING_CHOICES, INTEGER_CHOICES}) {
			for (int i = 0; i < 3; i++) {	
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Tester<?> t = new Tester(choices, choices[i].getClass());
				
				if (choices[i] instanceof Integer) {
					settings.setMinTime((Integer) choices[i]);
				} else if (choices[i] instanceof String) {
					settings.setTimeSystemSetting((String) choices[i]);
				}
				
				// Reset - should not be dirty
				t.reset(settings, true);
				Assert.assertFalse(t.isDirty());
				Assert.assertTrue(t.isValidated()); // Can't be invalid
				
				// Find a combo box to test with
				JComboBox b = null;
				for (Component c : t.getComponents()) {
					if (c instanceof JComboBox) {
						b = (JComboBox) c;
					}
				}
				Assert.assertNotNull(b);
				
				// Select a different item
				Assert.assertEquals(b.getSelectedIndex(), i);
				b.setSelectedIndex((i+1) % 3);				
				Assert.assertTrue(t.isDirty());
				Assert.assertTrue(t.isValidated()); // Can't be invalid
				
				// Restore the original item
				b.setSelectedIndex(i);				
				Assert.assertFalse(t.isDirty());
				Assert.assertTrue(t.isValidated()); // Can't be invalid

			}		
			
		}
	}
	
	@Test
	public void testSetText() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Tester<Integer> t = new Tester(INTEGER_CHOICES, Integer.class);
		
		// Find a combo box to test with
		JComboBox b = null;
		for (Component c : t.getComponents()) {
			if (c instanceof JComboBox) {
				b = (JComboBox) c;
			}
		}
		Assert.assertNotNull(b);
		
		// By default, should appear as toString
		for (int i = 0; i < 3; i++) {			
			Assert.assertEquals(b.getItemAt(i).toString(),
					INTEGER_CHOICES[i].toString());
		}
		
		// Set choices to alternate text
		for (int i = 0; i < 3; i++) {
			t.setText(INTEGER_CHOICES[i], STRING_CHOICES[i]);
		}
		
		// Now, alternate text sahould appear in buttons
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(b.getItemAt(i).toString(), 
					STRING_CHOICES[i].toString());
		}
	}
	
	private static class Tester<T> extends PlotSettingsComboBox<T> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9214443775684345868L;
		private Class<T> c;
		
		public Tester (T[] choices, Class<T> cls) {
			super(choices);
			c     = cls;
		}
		
		@SuppressWarnings("unchecked")
		public void setSelection(Object object) {
			super.setSelection((T) object);
		}

		@Override
		public void populate(PlotConfiguration settings) {
			if (Integer.class.isAssignableFrom(c)) {
				settings.setMinTime((Integer) getSelection());
			} else {
				settings.setTimeSystemSetting((String) getSelection());
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void reset(PlotConfiguration settings, boolean hard) {
			if (hard) setSelection((T) (Integer.class.isAssignableFrom(c) ?
					(int) settings.getMinTime() : settings.getTimeSystemSetting()));
		}
		
	}
}
