package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import org.testng.Assert;
import java.util.Properties;

public class TestPlotDefaults {
	
	public void testValues()
	{
		// Creating a new plotSettings object calls the createDefaults method
		// this method loads values from plot.properties
		PlotSettings plotSettings = new PlotSettings();
		Properties properties = plotSettings.getPlotDefaultProperties();
		
		if (properties != null) checkProperties(plotSettings, properties);
	}
	
	// assert equal mappings in plotSettings to what is specified in plot.properties
	private void checkProperties(PlotSettings plotSettings, Properties properties)
	{
		for(String propertyName : properties.stringPropertyNames()) {
			/* DefaultPlotSpan not a value on the plot, rather, there's timeMin and timeMax;
			 * timeMin is a function of DefaultPlotSpan and the current time, hence we must
			 * rename the string to ensure we get the right value from the map */
			if (propertyName.equals("DefaultPlotSpan")) propertyName = "TimeMin";
			String value = properties.getProperty(propertyName);
			Assert.assertEquals(plotSettings.get(propertyName, String.class).toString(), value);
		}
	}
	
}
