package gov.nasa.arc.mct.fastplot.settings;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotLineGlobalConfiguration;
import gov.nasa.arc.mct.gui.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.Properties;

public class PlotSettings extends GenericSettings implements PlotConfiguration {
	
	private static final String DEFAULT_CHANGES = "properties/plot.properties";
	
	public boolean isNull() {
		return getAxisOrientationSetting() == null;
	}
	
	private void createDefaults() {
		long now = new GregorianCalendar().getTimeInMillis();
		this.create(PlotConstants.TIME_AXIS_SETTING, AxisOrientationSetting.X_AXIS_AS_TIME, AxisOrientationSetting.class);
		this.create(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, TimeAxisSubsequentBoundsSetting.JUMP, TimeAxisSubsequentBoundsSetting.class);
		this.create(PlotConstants.TIME_SYSTEM_SETTING, "", String.class);
		this.create(PlotConstants.TIME_FORMAT_SETTING, PlotConstants.DEFAULT_TIME_FORMAT, String.class);
		this.create(PlotConstants.X_AXIS_MAXIMUM_LOCATION_SETTING, XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT, XAxisMaximumLocationSetting.class);
		this.create(PlotConstants.Y_AXIS_MAXIMUM_LOCATION_SETTING, PlotConstants.DEFAULT_Y_AXIS_MAX_LOCATION_SETTING, YAxisMaximumLocationSetting.class);
		this.create(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MIN_SETTING, PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_SUBSEQUENT_SETTING, NonTimeAxisSubsequentBoundsSetting.class);
		this.create(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MAX_SETTING, PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_SUBSEQUENT_SETTING, NonTimeAxisSubsequentBoundsSetting.class);
		
		this.create(PlotConstants.TIME_MAX, now, Long.class);
		this.create(PlotConstants.TIME_MIN, now - PlotConstants.DEFAULT_PLOT_SPAN, Long.class);
		this.create(PlotConstants.NON_TIME_MAX, PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_VALUE, Double.class);
		this.create(PlotConstants.NON_TIME_MIN, PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_VALUE, Double.class);
		this.create(PlotConstants.DEPENDENT_MAX, PlotConstants.DEFAULT_NON_TIME_AXIS_MAX_VALUE, Double.class);
		this.create(PlotConstants.DEPENDENT_MIN, PlotConstants.DEFAULT_NON_TIME_AXIS_MIN_VALUE, Double.class);
		this.create(PlotConstants.TIME_PADDING, PlotConstants.DEFAULT_TIME_AXIS_PADDING, Double.class);
		this.create(PlotConstants.NON_TIME_MAX_PADDING, PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MAX, Double.class);
		this.create(PlotConstants.NON_TIME_MIN_PADDING, PlotConstants.DEFAULT_NON_TIME_AXIS_PADDING_MIN, Double.class);
		
		this.create(PlotConstants.GROUP_BY_ORDINAL_POSITION, true, Boolean.class);
		this.create(PlotConstants.PIN_TIME_AXIS, false, Boolean.class);
		this.create(PlotConstants.DRAW_LINES, PlotConstants.DEFAULT_PLOT_LINE_DRAW.drawLine(), Boolean.class);
		this.create(PlotConstants.DRAW_MARKERS, PlotConstants.DEFAULT_PLOT_LINE_DRAW.drawMarkers(), Boolean.class);
		this.create(PlotConstants.CONNECTION_TYPE, PlotLineGlobalConfiguration.getDefaultConnectionType(), PlotLineConnectionType.class);
		
		// adjust default plot values according to plot.properties
		Properties properties = getPlotDefaultProperties();
		if (properties != null) { // if there is no plot.properties, does nothing
			for(String propertyName : properties.stringPropertyNames()) {
				  String value = properties.getProperty(propertyName);
				  if (propertyName.equals("DefaultPlotSpan")) { // change default plot span 
					  this.create(PlotConstants.TIME_MIN, now - Long.parseLong(value), Long.class);
				  }
				  else if (propertyName.equals("NonTimeMax")) { // change default non-time axis max value
					  this.create(PlotConstants.NON_TIME_MAX, Double.parseDouble(value), Double.class);
				  }
				  else if (propertyName.equals("NonTimeMin")) { // change default non-time axis min value
					  this.create(PlotConstants.NON_TIME_MIN, Double.parseDouble(value), Double.class);
				  }
				  else if (propertyName.equals("DependentMax")) { // change default dependent max value
					  this.create(PlotConstants.DEPENDENT_MAX, Double.parseDouble(value), Double.class);
				  }
				  else if (propertyName.equals("DependentMin")) { // change default dependent min value
					  this.create(PlotConstants.DEPENDENT_MIN, Double.parseDouble(value), Double.class);
				  }
				  else if (propertyName.equals("TimePadding")) { // change time axis padding value
					  this.create(PlotConstants.TIME_PADDING, Double.parseDouble(value), Double.class);
				  }
				  else if (propertyName.equals("NonTimeMinPadding")) { // change default non time max padding
					  this.create(PlotConstants.NON_TIME_MAX_PADDING, Double.parseDouble(value), Double.class);
				  }
				  else if (propertyName.equals("NonTimeMaxPadding")) { // change default non time min padding
					  this.create(PlotConstants.NON_TIME_MIN_PADDING, Double.parseDouble(value), Double.class);
				  }
				}
			}
	}
	
	// this method extracts the "properties" from a "plot.properties" file
	// assumes the file is located in the /.../resources/properties directory
	public Properties getPlotDefaultProperties() {
		Properties properties = new Properties();
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(DEFAULT_CHANGES);
		if (is != null) {
			try {
				properties.load(is);
			} catch (IOException e) {}
			finally {				
				try { is.close(); }
				catch (IOException e) {}
			}
			return properties;
		}
		else return null; // returns null if there is no such file
	}
	
	public PlotSettings() {
		createDefaults();
	}
	
	public PlotSettings(PlotConfiguration configuration) {
		this(configuration.getAxisOrientationSetting(), 
				configuration.getTimeSystemSetting(),
				configuration.getTimeFormatSetting(),
				configuration.getXAxisMaximumLocation(), 
				configuration.getYAxisMaximumLocation(), 
				configuration.getTimeAxisSubsequentSetting(), 
				configuration.getNonTimeAxisSubsequentMinSetting(), 
				configuration.getNonTimeAxisSubsequentMaxSetting(), 
				configuration.getMaxTime(), 
				configuration.getMinTime(), 
				configuration.getMaxNonTime(), 
				configuration.getMinNonTime(), 
				configuration.getTimePadding(), 
				configuration.getNonTimeMaxPadding(), 
				configuration.getNonTimeMinPadding(), 
				configuration.getOrdinalPositionForStackedPlots(), 
				configuration.getPinTimeAxis(), 
				configuration.getPlotLineDraw(), 
				configuration.getPlotLineConnectionType());
	}
	
	public PlotSettings(AxisOrientationSetting timeAxisSetting,
			String timeSystemSetting, String timeFormatSetting,
			XAxisMaximumLocationSetting xAxisMaximumLocation,
			YAxisMaximumLocationSetting yAxisMaximumLocation,
			TimeAxisSubsequentBoundsSetting timeAxisSubsequent,
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting,
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting,
			long maxTime, long minTime, double maxNonTime, double minNonTime,
			double timePadding, double nonTimeMaxPadding,
			double nonTimeMinPadding, boolean ordinalPositionForStackedPlots,
			boolean pinTimeAxis, PlotLineDrawingFlags plotLineDraw,
			PlotLineConnectionType plotLineConnectionType) {
		createDefaults();
		this.setAxisOrientationSetting(timeAxisSetting);
		this.setTimeSystemSetting(timeSystemSetting);
		this.setTimeFormatSetting(timeFormatSetting);
		this.setXAxisMaximumLocation(xAxisMaximumLocation);
		this.setYAxisMaximumLocation(yAxisMaximumLocation);
		this.setTimeAxisSubsequentSetting(timeAxisSubsequent);
		this.setNonTimeAxisSubsequentMinSetting(nonTimeAxisSubsequentMinSetting);
		this.setNonTimeAxisSubsequentMaxSetting(nonTimeAxisSubsequentMaxSetting);
		this.setMaxTime(maxTime);
		this.setMinTime(minTime);
		this.setMaxNonTime(maxNonTime);
		this.setMinNonTime(minNonTime);
		this.setTimePadding(timePadding);
		this.setNonTimeMaxPadding(nonTimeMaxPadding);
		this.setNonTimeMinPadding(nonTimeMinPadding);
		this.setOrdinalPositionForStackedPlots(ordinalPositionForStackedPlots);
		this.setPinTimeAxis(pinTimeAxis);
		this.setPlotLineDraw(plotLineDraw);
		this.setPlotLineConnectionType(plotLineConnectionType);
	}
	
	public boolean matches(PlotConfiguration settings) {
		return
			settings.getAxisOrientationSetting().equals(getAxisOrientationSetting()) &&
			settings.getTimeSystemSetting().equals(getTimeSystemSetting()) &&
			settings.getTimeFormatSetting().equals(getTimeFormatSetting()) &&
			settings.getXAxisMaximumLocation().equals(getXAxisMaximumLocation()) &&
			settings.getYAxisMaximumLocation().equals(getYAxisMaximumLocation()) &&
			settings.getTimeAxisSubsequentSetting().equals(getTimeAxisSubsequentSetting()) &&
			settings.getNonTimeAxisSubsequentMinSetting().equals(getNonTimeAxisSubsequentMinSetting()) &&
			settings.getNonTimeAxisSubsequentMaxSetting().equals(getNonTimeAxisSubsequentMaxSetting()) &&
			settings.getMaxTime() == getMaxTime() &&
			settings.getMinTime() == getMinTime() &&
			settings.getMaxNonTime() == getMaxNonTime() &&
			settings.getMinNonTime() == getMinNonTime() &&
			settings.getTimePadding() == getTimePadding() &&
			settings.getNonTimeMaxPadding() == getNonTimeMaxPadding() &&
			settings.getNonTimeMinPadding() == getNonTimeMinPadding() &&
			settings.getOrdinalPositionForStackedPlots() == getOrdinalPositionForStackedPlots() &&
			settings.getPinTimeAxis() == getPinTimeAxis() &&
			settings.getPlotLineDraw() == getPlotLineDraw() &&
			settings.getPlotLineConnectionType() == getPlotLineConnectionType();
	}
	
	@Override
	public void loadFrom(View v) {
		super.loadFrom(v);
		
		// Special case: Support "FIXED" time axis setting for backwards compat.
		String subsequent = v.getViewProperties().getProperty(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, String.class);
		if (subsequent != null && subsequent.equals("FIXED")) {
			this.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
			this.setPinTimeAxis(true);
		}
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setTimeAxisSetting(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting)
	 */
	@Override
	public void setAxisOrientationSetting(AxisOrientationSetting timeAxisSetting) {
		this.set(PlotConstants.TIME_AXIS_SETTING, timeAxisSetting);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getTimeAxisSetting()
	 */
	@Override
	public AxisOrientationSetting getAxisOrientationSetting() {
		return this.get(PlotConstants.TIME_AXIS_SETTING, AxisOrientationSetting.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setTimeSystemSetting(java.lang.String)
	 */
	@Override
	public void setTimeSystemSetting(String timeSystemSetting) {
		this.set(PlotConstants.TIME_SYSTEM_SETTING, timeSystemSetting);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getTimeSystemSetting()
	 */
	@Override
	public String getTimeSystemSetting() {
		return this.get(PlotConstants.TIME_SYSTEM_SETTING, String.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setTimeFormatSetting(java.lang.String)
	 */
	@Override
	public void setTimeFormatSetting(String timeFormatSetting) {
		this.set(PlotConstants.TIME_FORMAT_SETTING, timeFormatSetting);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getTimeFormatSetting()
	 */
	@Override
	public String getTimeFormatSetting() {
		return this.get(PlotConstants.TIME_FORMAT_SETTING, String.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setXAxisMaximumLocation(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting)
	 */
	@Override
	public void setXAxisMaximumLocation(XAxisMaximumLocationSetting xAxisMaximumLocation) {
		this.set(PlotConstants.X_AXIS_MAXIMUM_LOCATION_SETTING, xAxisMaximumLocation);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getXAxisMaximumLocation()
	 */
	@Override
	public XAxisMaximumLocationSetting getXAxisMaximumLocation() {
		return this.get(PlotConstants.X_AXIS_MAXIMUM_LOCATION_SETTING, XAxisMaximumLocationSetting.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setYAxisMaximumLocation(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting)
	 */
	@Override
	public void setYAxisMaximumLocation(YAxisMaximumLocationSetting yAxisMaximumLocation) {
		this.set(PlotConstants.Y_AXIS_MAXIMUM_LOCATION_SETTING, yAxisMaximumLocation);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getYAxisMaximumLocation()
	 */
	@Override
	public YAxisMaximumLocationSetting getYAxisMaximumLocation() {
		return this.get(PlotConstants.Y_AXIS_MAXIMUM_LOCATION_SETTING, YAxisMaximumLocationSetting.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setTimeAxisSubsequent(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting)
	 */
	@Override
	public void setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting timeAxisSubsequent) {
		this.set(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, timeAxisSubsequent);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getTimeAxisSubsequent()
	 */
	@Override
	public TimeAxisSubsequentBoundsSetting getTimeAxisSubsequentSetting() {
		return this.get(PlotConstants.TIME_AXIS_SUBSEQUENT_SETTING, TimeAxisSubsequentBoundsSetting.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setNonTimeAxisSubsequentMinSetting(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting)
	 */
	@Override
	public void setNonTimeAxisSubsequentMinSetting(
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting) {
		this.set(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MIN_SETTING, nonTimeAxisSubsequentMinSetting);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getNonTimeAxisSubsequentMinSetting()
	 */
	@Override
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMinSetting() {
		return this.get(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MIN_SETTING, NonTimeAxisSubsequentBoundsSetting.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setNonTimeAxisSubsequentMaxSetting(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting)
	 */
	@Override
	public void setNonTimeAxisSubsequentMaxSetting(
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting) {
		this.set(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MAX_SETTING, nonTimeAxisSubsequentMaxSetting);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getNonTimeAxisSubsequentMaxSetting()
	 */
	@Override
	public NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMaxSetting() {
		return this.get(PlotConstants.NON_TIME_AXIS_SUBSEQUENT_MAX_SETTING, NonTimeAxisSubsequentBoundsSetting.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setMaxTime(long)
	 */
	@Override
	public void setMaxTime(long maxTime) {
		this.set(PlotConstants.TIME_MAX, (Long) maxTime);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getMaxTime()
	 */
	@Override
	public long getMaxTime() {
		return this.get(PlotConstants.TIME_MAX, Long.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setMinTime(long)
	 */
	@Override
	public void setMinTime(long minTime) {
		this.set(PlotConstants.TIME_MIN, (Long) minTime);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getMinTime()
	 */
	@Override
	public long getMinTime() {
		return this.get(PlotConstants.TIME_MIN, Long.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setMaxNonTime(double)
	 */
	@Override
	public void setMaxNonTime(double maxNonTime) {
		this.set(PlotConstants.NON_TIME_MAX, (Double) maxNonTime);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getMaxNonTime()
	 */
	@Override
	public double getMaxNonTime() {
		return this.get(PlotConstants.NON_TIME_MAX, Double.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setMinNonTime(double)
	 */
	@Override
	public void setMinNonTime(double minNonTime) {
		this.set(PlotConstants.NON_TIME_MIN, (Double) minNonTime);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getMinNonTime()
	 */
	@Override
	public double getMinNonTime() {
		return this.get(PlotConstants.NON_TIME_MIN, Double.class);
	}
	
		/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getMinDependent()
	 */
	@Override
	public double getMinDependent() {
		return this.get(PlotConstants.DEPENDENT_MIN, Double.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setMinDependent(double)
	 */
	@Override
	public void setMinDependent(double min) {
		this.set(PlotConstants.DEPENDENT_MIN, min);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getMaxDependent()
	 */
	@Override
	public double getMaxDependent() {
		return this.get(PlotConstants.DEPENDENT_MAX, Double.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setMaxDependent(double)
	 */
	@Override
	public void setMaxDependent(double max) {
		this.set(PlotConstants.DEPENDENT_MAX, max);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setTimePadding(double)
	 */
	@Override
	public void setTimePadding(double timePadding) {
		this.set(PlotConstants.TIME_PADDING, (Double) timePadding);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getTimePadding()
	 */
	@Override
	public double getTimePadding() {
		return this.get(PlotConstants.TIME_PADDING, Double.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setNonTimeMaxPadding(double)
	 */
	@Override
	public void setNonTimeMaxPadding(double nonTimeMaxPadding) {
		this.set(PlotConstants.NON_TIME_MAX_PADDING, (Double) nonTimeMaxPadding);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getNonTimeMaxPadding()
	 */
	@Override
	public double getNonTimeMaxPadding() {
		return this.get(PlotConstants.NON_TIME_MAX_PADDING, Double.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setNonTimeMinPadding(double)
	 */
	@Override
	public void setNonTimeMinPadding(double nonTimeMinPadding) {
		this.set(PlotConstants.NON_TIME_MIN_PADDING, (Double) nonTimeMinPadding);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getNonTimeMinPadding()
	 */
	@Override
	public double getNonTimeMinPadding() {
		return this.get(PlotConstants.NON_TIME_MIN_PADDING, Double.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setOrdinalPositionForStackedPlots(boolean)
	 */
	@Override
	public void setOrdinalPositionForStackedPlots(
			boolean ordinalPositionForStackedPlots) {
		this.set(PlotConstants.GROUP_BY_ORDINAL_POSITION, (Boolean) ordinalPositionForStackedPlots);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getOrdinalPositionForStackedPlots()
	 */
	@Override
	public boolean getOrdinalPositionForStackedPlots() {
		return this.get(PlotConstants.GROUP_BY_ORDINAL_POSITION, Boolean.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setPinTimeAxis(boolean)
	 */
	@Override
	public void setPinTimeAxis(boolean pinTimeAxis) {
		this.set(PlotConstants.PIN_TIME_AXIS, (Boolean) pinTimeAxis);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getPinTimeAxis()
	 */
	@Override
	public boolean getPinTimeAxis() {
		return this.get(PlotConstants.PIN_TIME_AXIS, Boolean.class);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setPlotLineDraw(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags)
	 */
	@Override
	public void setPlotLineDraw(PlotLineDrawingFlags plotLineDraw) {
		this.set(PlotConstants.DRAW_LINES, plotLineDraw.drawLine());
		this.set(PlotConstants.DRAW_MARKERS, plotLineDraw.drawMarkers());
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getPlotLineDraw()
	 */
	@Override
	public PlotLineDrawingFlags getPlotLineDraw() {
		return new PlotLineDrawingFlags(this.get(PlotConstants.DRAW_LINES,   Boolean.class), 
				                        this.get(PlotConstants.DRAW_MARKERS, Boolean.class)); 
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#setPlotLineConnectionType(gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType)
	 */
	@Override
	public void setPlotLineConnectionType(PlotLineConnectionType plotLineConnectionType) {
		this.set(PlotConstants.CONNECTION_TYPE, plotLineConnectionType);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotConfiguration#getPlotLineConnectionType()
	 */
	@Override
	public PlotLineConnectionType getPlotLineConnectionType() {
		return this.get(PlotConstants.CONNECTION_TYPE, PlotLineConnectionType.class);
	}
}