package gov.nasa.arc.mct.fastplot.util;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.utils.TimeFormatUtils;
import gov.nasa.arc.mct.services.activity.TimeService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestTimeFormatUtils {
	// Prepare test date (Jan 8, 1947 01:02:03)
	private static final Date testDate = new Date(-725237877000l);
 
	
	@DataProvider (name = "testCases")
	public Object[][] createTestCases() {		
		// Use a default date format for reference
		DateFormat defaultDateFormat = new SimpleDateFormat(TimeService.DEFAULT_TIME_FORMAT);
		defaultDateFormat.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		String def = defaultDateFormat.format(testDate);
		
		return new Object[][] {
				{ null,               def },            // Null arg - should use default
				{ "oo DDD HH mm ss" , def },            // Illegal args to date format - should use default
				{ "yyyy DDD HH",      "1947 008 01"  }, // With year
				{ "yyyy DDD HH eeee", "1947 008 01 " }  // Should ignore nanoseconds
		};
	}

	
	@Test (dataProvider = "testCases")
	public void testDateFormats(String format, String expected) {
		DateFormat testFormat = TimeFormatUtils.makeDataFormat(format);
		Assert.assertEquals(testFormat.format(testDate), expected);
	}
	
}
