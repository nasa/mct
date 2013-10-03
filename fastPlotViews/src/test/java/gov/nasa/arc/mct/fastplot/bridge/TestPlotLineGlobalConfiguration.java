package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;

import java.awt.Font;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestPlotLineGlobalConfiguration {
	private Map<String, Object> originalMappings;
	
	@BeforeMethod
	public void setup() throws Exception {
		originalMappings = new HashMap<String, Object>();
		
		// Uninitialize global configuration
		Field f = PlotLineGlobalConfiguration.class.getDeclaredField("initialized");
		f.setAccessible(true);
		f.setBoolean(null, false);
	}
	
	@AfterMethod
	public void teardown() {
		for (String key : originalMappings.keySet()) {
			UIManager.put(key, originalMappings.get(key));
		}
	}
	
	@Test
	public void testWithoutMappings() {
		Font marker = PlotLineGlobalConfiguration.getMarkerFont();
		Assert.assertNotNull(marker);
		Assert.assertTrue(marker.getSize() >= 8);
	}
	
	
	@DataProvider(name = "knownTypes")
	public Object[][] createKnownTypes() {
		Object[][] params = new Object[PlotLineConnectionType.values().length][];
		int i = 0;
		for (PlotLineConnectionType value : PlotLineConnectionType.values()) {
			params[i++] = new Object[]{ value.name() };
		}
		return params;
	}
	
	@Test (dataProvider = "knownTypes")
	public void testKnownConnectionTypes(String connection) {
		recordMapping("PlotLine.connection", connection);
		PlotLineConnectionType type = PlotLineGlobalConfiguration.getDefaultConnectionType();
		Assert.assertNotNull(type);
		Assert.assertEquals(type.name(), connection);
	}
	
	@Test
	public void testUnkownConnectionType() {
		recordMapping("PlotLine.connection", "NOT_A_CONNECTION_TYPE");
		PlotLineConnectionType type = PlotLineGlobalConfiguration.getDefaultConnectionType();
		Assert.assertNotNull(type);
		Assert.assertNotSame(type.name(), "NOT_A_CONNECTION_TYPE");	
	}
	
	
	@DataProvider(name = "fontStyles")
	public Object[][] createFontStyles() {
		return new Object[][] {
				{"PLAIN", Font.PLAIN},
				{"BOLD", Font.BOLD}, 
				{"ITALIC", Font.ITALIC}, 
				{"BOLDITALIC", Font.BOLD + Font.ITALIC}	
		};
	}
	
	@Test (dataProvider = "fontStyles")
	public void testFontStyles(String style, Integer expected) {
		recordMapping("PlotLine.fontStyle", style);
		Font marker = PlotLineGlobalConfiguration.getMarkerFont();
		int actual = marker.getStyle();
		Assert.assertEquals(actual, expected.intValue());
	}
	
	
	@DataProvider(name = "fontSizes")
	public Object[][] createFontSizes() {
		Object[][] params = new Object[16][];
		for (int i = 0; i < 16; i++) {
			params[i] = new Object[]{ Integer.valueOf(4 + i*4) };
		}
		return params;
	}
	
	@Test (dataProvider = "fontSizes")
	public void testFontStyles(Integer size) {
		recordMapping("PlotLine.fontSize", size.toString());
		Font marker = PlotLineGlobalConfiguration.getMarkerFont();
		int actual = marker.getSize();
		Assert.assertEquals(actual, size.intValue());
	}
	
	private void recordMapping(String key, String value) {
		if (!originalMappings.containsKey(key)) {
			originalMappings.put(key, UIManager.get(key));
		}
		UIManager.put(key, value);
	}
}
