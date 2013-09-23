package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;

import javax.swing.JRadioButton;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotSettingsRadioButtonGroup {
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
				JRadioButton b1 = t.getButton(choices[i]);
				JRadioButton b2 = t.getButton(choices[(i+1)%3]);
				Assert.assertTrue(b1.isSelected());
				Assert.assertFalse(b2.isSelected());
								
				// Select a different item
				b2.doClick();
				Assert.assertFalse(b1.isSelected());
				Assert.assertTrue(b2.isSelected());			
				Assert.assertTrue(t.isDirty());
				Assert.assertTrue(t.isValidated()); // Can't be invalid
				
				// Restore the original item
				b1.doClick();
				Assert.assertTrue(b1.isSelected());
				Assert.assertFalse(b2.isSelected());			
				Assert.assertFalse(t.isDirty());
				Assert.assertTrue(t.isValidated()); // Can't be invalid

			}		
			
		}
	}
	
	@Test
	public void testSetText() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Tester<Integer> t = new Tester(INTEGER_CHOICES, Integer.class);
		
		// By default, should appear as toString
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(t.getButton(INTEGER_CHOICES[i]).getText(), 
					INTEGER_CHOICES[i].toString());
		}
		
		// Set choices to alternate text
		for (int i = 0; i < 3; i++) {
			t.setText(INTEGER_CHOICES[i], STRING_CHOICES[i]);
		}
		
		// Now, alternate text sahould appear in buttons
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(t.getButton(INTEGER_CHOICES[i]).getText(), 
					STRING_CHOICES[i].toString());
		}
	}
	
	private static class Tester<T> extends PlotSettingsRadioButtonGroup<T> {
		private static final long serialVersionUID = 2491883458409785019L;
		private Class<T> c;
		
		public Tester (T[] choices, Class<T> cls) {
			super(choices);
			c     = cls;
		}
		
		@SuppressWarnings("unchecked")
		public JRadioButton getButton(Object object) {
			return super.getButton((T) object);
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
