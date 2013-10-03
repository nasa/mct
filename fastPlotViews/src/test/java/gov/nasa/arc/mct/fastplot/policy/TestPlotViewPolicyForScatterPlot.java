package gov.nasa.arc.mct.fastplot.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class TestPlotViewPolicyForScatterPlot {
	
	
	private AbstractComponent[] nonFeeds = new AbstractComponent[10]; 
	private AbstractComponent[] feeds    = new AbstractComponent[10];
	
	private AbstractComponent[] parents  = new AbstractComponent[10];
	
	private AbstractComponent grandparent;
	
	private List<AbstractComponent> grandchildren;
	private List<AbstractComponent> children;
	
	@Mock FeedProvider mockProvider;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this); 
		
		grandchildren = new ArrayList<AbstractComponent>();
		children = new ArrayList<AbstractComponent>();
		
		grandparent = Mockito.mock(AbstractComponent.class);
		
		for (int i = 0; i < 10; i++) {
			nonFeeds[i] = Mockito.mock(AbstractComponent.class);
			feeds[i] = Mockito.mock(AbstractComponent.class);
			parents[i] = Mockito.mock(AbstractComponent.class);
			
			Mockito.when(nonFeeds[i].isLeaf()).thenReturn(true);
			Mockito.when(nonFeeds[i].getCapability(FeedProvider.class)).thenReturn(null);
			
			Mockito.when(feeds[i].isLeaf()).thenReturn(true);
			Mockito.when(feeds[i].getCapability(FeedProvider.class)).thenReturn(mockProvider);
			
			Mockito.when(parents[i].isLeaf()).thenReturn(false);
			Mockito.when(parents[i].getComponents()).thenReturn(grandchildren);
		}
		
		Mockito.when(grandparent.isLeaf()).thenReturn(false);
		Mockito.when(grandparent.getComponents()).thenReturn(children);
	}

	@Test
	public void testEmptyCollection() {
		//children array is empty
		Assert.assertFalse(PlotViewPolicy.isScatterPlottable(grandparent));
		Assert.assertFalse(PlotViewPolicy.isOverlaidScatterPlottable(grandparent));
	}
	
	@Test
	public void testNonFeeds() {		
		Collections.addAll(children, nonFeeds);
		Assert.assertFalse(PlotViewPolicy.isScatterPlottable(grandparent));
		Assert.assertFalse(PlotViewPolicy.isOverlaidScatterPlottable(grandparent));		
	}
	
	@Test
	public void testOneLayerFeeds() {
		Collections.addAll(children, feeds);
		Assert.assertTrue (PlotViewPolicy.isScatterPlottable(grandparent));
		Assert.assertFalse(PlotViewPolicy.isOverlaidScatterPlottable(grandparent));				
	}

	@Test
	public void testTwoLayerFeeds() {
		Collections.addAll(children, parents);
		Collections.addAll(grandchildren, feeds);
		Assert.assertFalse(PlotViewPolicy.isScatterPlottable(grandparent));
		Assert.assertTrue (PlotViewPolicy.isOverlaidScatterPlottable(grandparent));				
	}

	@Test
	public void testTwoLayerNonFeeds() {
		Collections.addAll(children, parents);
		Collections.addAll(grandchildren, nonFeeds);
		Assert.assertFalse(PlotViewPolicy.isScatterPlottable(grandparent));
		Assert.assertFalse(PlotViewPolicy.isOverlaidScatterPlottable(grandparent));				
	}
}
