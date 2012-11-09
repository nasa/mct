package gov.nasa.arc.mct.plot.settings;

import gov.nasa.arc.mct.plot.settings.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.plot.settings.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.plot.settings.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.plot.settings.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.plot.settings.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.plot.settings.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.plot.settings.PlotConstants.YAxisMaximumLocationSetting;

public interface PlotConfiguration {

	public abstract void setAxisOrientationSetting(
			AxisOrientationSetting timeAxisSetting);

	public abstract AxisOrientationSetting getAxisOrientationSetting();

	public abstract void setTimeSystemSetting(String timeSystemSetting);

	public abstract String getTimeSystemSetting();

	public abstract void setTimeFormatSetting(String timeFormatSetting);

	public abstract String getTimeFormatSetting();

	public abstract void setXAxisMaximumLocation(
			XAxisMaximumLocationSetting xAxisMaximumLocation);

	public abstract XAxisMaximumLocationSetting getXAxisMaximumLocation();

	public abstract void setYAxisMaximumLocation(
			YAxisMaximumLocationSetting yAxisMaximumLocation);

	public abstract YAxisMaximumLocationSetting getYAxisMaximumLocation();

	public abstract void setTimeAxisSubsequentSetting(
			TimeAxisSubsequentBoundsSetting timeAxisSubsequent);

	public abstract TimeAxisSubsequentBoundsSetting getTimeAxisSubsequentSetting();

	public abstract void setNonTimeAxisSubsequentMinSetting(
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting);

	public abstract NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMinSetting();

	public abstract void setNonTimeAxisSubsequentMaxSetting(
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting);

	public abstract NonTimeAxisSubsequentBoundsSetting getNonTimeAxisSubsequentMaxSetting();

	public abstract void setMaxTime(long maxTime);

	public abstract long getMaxTime();

	public abstract void setMinTime(long minTime);

	public abstract long getMinTime();

	public abstract void setMaxNonTime(double maxNonTime);

	public abstract double getMaxNonTime();

	public abstract void setMinNonTime(double minNonTime);

	public abstract double getMinNonTime();

	public abstract void setTimePadding(double timePadding);

	public abstract double getTimePadding();

	public abstract void setNonTimeMaxPadding(double nonTimeMaxPadding);

	public abstract double getNonTimeMaxPadding();

	public abstract void setNonTimeMinPadding(double nonTimeMinPadding);

	public abstract double getNonTimeMinPadding();

	public abstract void setOrdinalPositionForStackedPlots(
			boolean ordinalPositionForStackedPlots);

	public abstract boolean getOrdinalPositionForStackedPlots();

	public abstract void setPinTimeAxis(boolean pinTimeAxis);

	public abstract boolean getPinTimeAxis();

	public abstract void setPlotLineDraw(PlotLineDrawingFlags plotLineDraw);

	public abstract PlotLineDrawingFlags getPlotLineDraw();

	public abstract void setPlotLineConnectionType(
			PlotLineConnectionType plotLineConnectionType);

	public abstract PlotLineConnectionType getPlotLineConnectionType();

}