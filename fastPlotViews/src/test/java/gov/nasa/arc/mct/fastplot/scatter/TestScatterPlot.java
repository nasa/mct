package gov.nasa.arc.mct.fastplot.scatter;

import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotView;
import gov.nasa.arc.mct.fastplot.settings.PlotSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;

import java.awt.Color;
import java.awt.Font;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestScatterPlot {
	private static final double EPSILON = 0.001d;
	
	private ScatterPlot  testPlot;
	private PlotSettings settings;
	private PlotAbstraction abstraction;
	
	@BeforeMethod
	public void setup() {
		settings    = new PlotSettings();
		testPlot    = new ScatterPlot(settings);
		abstraction = new PlotView.Builder(ScatterPlot.class).plotSettings(settings).build();
	}
	
	@Test
	public void testScatterPlotMatchesSettingsViaPlotView() {
		PlotSettings s = new PlotSettings();
		PlotView basePlot = new PlotView.Builder(ScatterPlot.class).plotSettings(s).build();		
		Assert.assertTrue(basePlot.plotMatchesSetting(s));
	}
	
	@Test
	public void testScatterPlotMatchesSettingsDirectly() {
		Assert.assertEquals(testPlot.getOrdinalPositionForStackedPlots(),
				settings.getOrdinalPositionForStackedPlots());
		Assert.assertEquals(testPlot.getPinTimeAxis(),
				settings.getPinTimeAxis());
		Assert.assertEquals(testPlot.getAxisOrientationSetting(),
				settings.getAxisOrientationSetting());
		Assert.assertEquals(testPlot.getMaxDependent(),
				settings.getMaxDependent());
		Assert.assertEquals(testPlot.getMaxNonTime(),
				settings.getMaxNonTime());
		Assert.assertEquals(testPlot.getMaxTime(),
				settings.getMaxTime());
		Assert.assertEquals(testPlot.getMinDependent(),
				settings.getMinDependent());
		Assert.assertEquals(testPlot.getMinNonTime(),
				settings.getMinNonTime());
		Assert.assertEquals(testPlot.getMinTime(),
				settings.getMinTime());
		Assert.assertEquals(testPlot.getNonTimeAxisSubsequentMaxSetting(),
				settings.getNonTimeAxisSubsequentMaxSetting());
		Assert.assertEquals(testPlot.getNonTimeAxisSubsequentMinSetting(),
				settings.getNonTimeAxisSubsequentMinSetting());
		Assert.assertEquals(testPlot.getNonTimeMaxPadding(),
				settings.getNonTimeMaxPadding());
		Assert.assertEquals(testPlot.getNonTimeMinPadding(),
				settings.getNonTimeMinPadding());
		Assert.assertEquals(testPlot.getTimeAxisSubsequentSetting(),
				settings.getTimeAxisSubsequentSetting());
		Assert.assertEquals(testPlot.getTimePadding(),
				settings.getTimePadding());
		Assert.assertEquals(testPlot.getXAxisMaximumLocation(),
				settings.getXAxisMaximumLocation());
		Assert.assertEquals(testPlot.getYAxisMaximumLocation(),
				settings.getYAxisMaximumLocation());
		// TODO: Connection, line style, etc (not currently featured in non-time)
	}

	
	@Test
	public void testGetPlotAbstraction() {
		PlotSettings s = new PlotSettings();
		PlotView basePlot = new PlotView.Builder(ScatterPlot.class).plotSettings(s).build();		
		ScatterPlot p = new ScatterPlot();
		p.setPlotAbstraction(basePlot);
		Assert.assertEquals(p.getPlotAbstraction(), basePlot);

		// Should also pull recognize PlotAbstraction from constructor
		p = new ScatterPlot(basePlot);		
		Assert.assertEquals(p.getPlotAbstraction(), basePlot);
		
		// Finally, should also take abstraction used by createChart
		invokeCreateChart(p);
		Assert.assertEquals(p.getPlotAbstraction(), abstraction);
	}
	
	@Test
	public void testSetTimeAxisStartAndStop() {
		testPlot.setTimeAxisStartAndStop(1000L, 64000L);
		Assert.assertEquals(testPlot.getCurrentTimeAxisMin().getTimeInMillis(), 1000L);
		Assert.assertEquals(testPlot.getCurrentTimeAxisMax().getTimeInMillis(), 64000L);
		testPlot.setTimeAxisStartAndStop(128000L, 300000L);
		Assert.assertEquals(testPlot.getCurrentTimeAxisMin().getTimeInMillis(), 128000L);
		Assert.assertEquals(testPlot.getCurrentTimeAxisMax().getTimeInMillis(), 300000L);
		
		// Should still be able to retrieve initial time axis setting
		Assert.assertEquals(testPlot.getInitialTimeMaxSetting(), settings.getMaxTime());
		Assert.assertEquals(testPlot.getInitialTimeMinSetting(), settings.getMinTime());
	}
		
	@Test
	public void testNonTimeAxisAutoExpands() {
		settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		settings.setNonTimeMinPadding(0);
		settings.setNonTimeMaxPadding(0);
		settings.setMinNonTime(-1);
		settings.setMaxNonTime( 1);
		settings.setMinDependent(-1);
		settings.setMaxDependent( 1);
		settings.setMinTime(0);
		settings.setMaxTime(14000);
		
		testPlot = new ScatterPlot(settings);
		invokeCreateChart(testPlot);
		Assert.assertEquals(testPlot.getNonTimeMinDataValueCurrentlyDisplayed(), -1, EPSILON);
		Assert.assertEquals(testPlot.getNonTimeMaxDataValueCurrentlyDisplayed(),  1, EPSILON);

		Assert.assertEquals(testPlot.getDataSetSize(), 0);
		testPlot.addDataSet("xData", Color.WHITE);
		testPlot.addDataSet("xData" + PlotConstants.NON_TIME_FEED_SEPARATOR + "yData", Color.WHITE, "Test Data");
		Assert.assertEquals(testPlot.getDataSetSize(), 1); // Should only count plottable data set
		
		Assert.assertTrue(testPlot.isKnownDataSet("xData"));
		Assert.assertTrue(testPlot.isKnownDataSet("yData"));
		
		// Shouldn't start marching yet...
		for (double t = 0; t < Math.PI * 2; t += Math.PI/100.0) {
			testPlot.addData("xData", (long) (t * 1000), Math.sin(t));
			testPlot.addData("yData", (long) (t * 1000), Math.cos(t));
			Assert.assertEquals(testPlot.getNonTimeMinDataValueCurrentlyDisplayed(), -1, EPSILON);
			Assert.assertEquals(testPlot.getNonTimeMaxDataValueCurrentlyDisplayed(),  1, EPSILON);
		}
		
		// But now, we should auto-expand
		for (double t = 0; t < Math.PI * 2; t += Math.PI/100.0) {
			// Make sure new data is inserted after last timestamp of old data
			// (insertion of data into middle of existing series is unsupported)
			testPlot.addData("xData", 7000 + (long) (t * 1000), 2*Math.sin(t));
			testPlot.addData("yData", 7000 + (long) (t * 1000), 2*Math.cos(t));
		}
		Assert.assertEquals(testPlot.getNonTimeMinDataValueCurrentlyDisplayed(), -2, EPSILON);
		Assert.assertEquals(testPlot.getNonTimeMaxDataValueCurrentlyDisplayed(),  2, EPSILON);

		// Initial setting should still be retrievable
		Assert.assertEquals(testPlot.getInitialNonTimeMinSetting(), -1, EPSILON);
		Assert.assertEquals(testPlot.getInitialNonTimeMaxSetting(),  1, EPSILON);
		
	}
	
	private void invokeCreateChart(ScatterPlot plot) {
		plot.createChart(
				Font.decode(Font.SERIF), 1,
				Color.BLACK, Color.BLACK,
				0, Color.BLACK,
				Color.BLACK, Color.BLACK,
				"", Color.BLACK,
				Color.BLACK, 10,
				false, false,
				false, abstraction,
				new AbbreviatingPlotLabelingAlgorithm());
	}
}
