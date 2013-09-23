package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.services.activity.TimeService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class TestPlotDataAssigner {

	@DataProvider (name = "feedProviders")
	private Object[][] createFeedProviderMatrices() {
		
		List<Object[]> testCases = new ArrayList<Object[]>();
		
		// Test a variety of sizes
		boolean[] truths = { false , true };
		for (boolean multiTS : truths) {
			for (boolean multiTF : truths) {
				for (int ts = 1; ts < 10; ts += 3) {
					for (int tf = 1; tf < 10; tf += 3) {
						Set<String> timeSystems = new HashSet<String>();
						Set<String> timeFormats = new HashSet<String>();
						for (int i = 0; i < ts; i++) timeSystems.add("TS"+i);
						for (int i = 0; i < tf; i++) timeFormats.add("TF"+i);
						AbstractComponent[][] comps = makeComponentsFor(timeSystems.toArray(new String[ts]), timeFormats.toArray(new String[tf]), multiTS, multiTF);
						testCases.add(new Object[] { comps, timeSystems, timeFormats });
					}
				}			
			}
		}
		
		return testCases.toArray(new Object[testCases.size()][]);
	}
	
	private AbstractComponent[][] makeComponentsFor(String[] timeSystems, String[] timeFormats, boolean multiTS, boolean multiTF) {
		int rows = multiTS ? 1 : timeSystems.length;
		int cols = multiTF ? 1 : timeFormats.length;
		
		AbstractComponent[][] result = new AbstractComponent[rows][];
		
		for (int r = 0 ; r < rows ; r++) {
			result[r] = new AbstractComponent[cols];
			for (int c = 0 ; c < cols ; c++) {
				FeedProvider fp      = Mockito.mock(FeedProvider.class);
				TimeService  ts      = Mockito.mock(TimeService.class);
				AbstractComponent ac = Mockito.mock(AbstractComponent.class);
				Mockito.when(ac.isLeaf()).thenReturn(true);
				Mockito.when(ac.getCapability(FeedProvider.class)).thenReturn(fp);
				Mockito.when(fp.getTimeService()).thenReturn(ts);
				Mockito.when(fp.isPrediction()).thenReturn(false);
				Mockito.when(ts.getTimeSystems()).thenReturn(multiTS ? timeSystems : new String[]{timeSystems[r]});
				Mockito.when(ts.getTimeFormats()).thenReturn(multiTF ? timeFormats : new String[]{timeFormats[c]});
				result[r][c] = ac;
			}
		}
		
		return result;
	}

	private AbstractComponent generateMockGrandparent(AbstractComponent[][] comps) {
		AbstractComponent grandparent = Mockito.mock(AbstractComponent.class);
		List<AbstractComponent> children = new ArrayList<AbstractComponent>();
		
		for (int i = 0; i < comps.length; i++) {
			AbstractComponent child = Mockito.mock(AbstractComponent.class);
			List<AbstractComponent> grandchildren = new ArrayList<AbstractComponent>();
			for (int j = 0; j < comps[0].length; j++) {
				grandchildren.add(comps[i][j]);				
			}
			Mockito.when(child.getComponents()).thenReturn(grandchildren);
			Mockito.when(child.isLeaf()).thenReturn(false);
			children.add(child);			
		}
		
		Mockito.when(grandparent.getComponents()).thenReturn(children);
		Mockito.when(grandparent.isLeaf()).thenReturn(false);
		
		return grandparent;
	}
	
	
	@Test (dataProvider = "feedProviders")
	public void testAggregateChoices(AbstractComponent[][] comps, Set<String> expectedTS, Set<String> expectedTF) {
		Set<String> actual = PlotDataAssigner.aggregateTimeSystemChoices(comps);
		
		Assert.assertEquals(actual.size(), expectedTS.size());
		for (String a : expectedTS) {
			Assert.assertTrue(actual.contains(a));
		}
	
		actual = PlotDataAssigner.aggregateTimeFormatChoices(comps);
		
		Assert.assertEquals(actual.size(), expectedTF.size());
		for (String a : expectedTF) {
			Assert.assertTrue(actual.contains(a));
		}		
	}

	@Test (dataProvider = "feedProviders")
	public void testUpdateFeedProviders(AbstractComponent[][] comps, Set<String> expectedTS, Set<String> expectedTF) {
		AbstractComponent comp = generateMockGrandparent(comps);
		PlotViewManifestation view = Mockito.mock(PlotViewManifestation.class);
		Mockito.when(view.getManifestedComponent()).thenReturn(comp);
		Mockito.when(view.getViewProperties()).thenReturn(new ExtendedProperties());
		
		int compCount = 0;
		for (AbstractComponent[] compArray : comps) {
			compCount += compArray.length;
			for (AbstractComponent c : compArray) {
				FeedProvider fp = c.getCapability(FeedProvider.class);
				Mockito.when(view.getFeedProvider(c)).thenReturn(fp);
			}
		}

		PlotDataAssigner pda = new PlotDataAssigner(view);
		pda.informFeedProvidersHaveChanged();
		
	
		// Should have gotten one feed provider per component
		Assert.assertEquals(pda.getVisibleFeedProviders().size(), compCount);
	
		
	}
}
