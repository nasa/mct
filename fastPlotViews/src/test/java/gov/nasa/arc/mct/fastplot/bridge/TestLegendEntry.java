package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.settings.LineSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JWindow;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import plotter.xy.LinearXYPlotLine;

public class TestLegendEntry {
	private AbbreviatingPlotLabelingAlgorithm algorithm = new AbbreviatingPlotLabelingAlgorithm();
	
	private LegendEntry legendEntry;
	
	@Mock private LinearXYPlotLine mockPlotLine;
	@Mock private LinearXYPlotLine mockRegressionLine;
	@Mock private LineSettings mockSettings;
	@Mock private Icon mockIcon;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		legendEntry = new LegendEntry(Color.BLACK, Color.WHITE, Font.decode(Font.SANS_SERIF), algorithm);
		legendEntry.setPlot(mockPlotLine);
	}
	
	@Test
	public void testGetterSetterForLineSettings() {
		legendEntry.setLineSettings(mockSettings);
		Assert.assertEquals(legendEntry.getLineSettings(), mockSettings);
	}
	
	@Test
	public void testGetterSetterForRegression() {
		legendEntry.setLineSettings(mockSettings);
		
		// Toggle regression
		boolean b = !legendEntry.hasRegressionLine();
		legendEntry.setHasRegressionLine(b);
		Mockito.verify(mockSettings, Mockito.atLeastOnce()).setHasRegression(b);			
		
		// Increase number of regression points
		int n = legendEntry.getNumberRegressionPoints() + 1;		
		legendEntry.setNumberRegressionPoints(n);
		Mockito.verify(mockSettings, Mockito.atLeastOnce()).setRegressionPoints(n);
		
		// Set / unset regression line component
		Assert.assertNull(legendEntry.getRegressionLine());
		legendEntry.setRegressionLine(mockRegressionLine);
		Assert.assertNotNull(legendEntry.getRegressionLine());
		Mockito.verify(mockRegressionLine, Mockito.atLeastOnce()).setForeground(Mockito.<Color>any());
	}
	
	@Test
	public void testUpdatesIcon() {
		// Note: Font rendering context is needed for character shapes, so show legend entry on screen
		
		JWindow w = new JWindow();
		w.getContentPane().add(legendEntry);
		w.setVisible(true);
		w.pack();
		
		Mockito.when(mockPlotLine.getPointIcon()).thenReturn(mockIcon);
		int i = 0;
		for (boolean b : new boolean[]{false, true}) {
			Mockito.verify(mockPlotLine, Mockito.times(i)).setPointIcon(Mockito.<Icon>any());
			Mockito.when(mockSettings.getUseCharacter()).thenReturn(b);
			Mockito.when(mockSettings.getMarker()).thenReturn(0);
			Mockito.when(mockSettings.getCharacter()).thenReturn("X");
			legendEntry.setLineSettings(mockSettings);
			Mockito.verify(mockPlotLine, Mockito.times(++i)).setPointIcon(Mockito.<Icon>any());			
		}		
		
		w.setVisible(false);
		w.dispose();
	}
}
