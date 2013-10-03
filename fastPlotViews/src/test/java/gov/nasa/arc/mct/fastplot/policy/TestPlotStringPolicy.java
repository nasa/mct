package gov.nasa.arc.mct.fastplot.policy;

import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPlotStringPolicy {
	private Policy policy;
	
	private ViewInfo viewInfo;
	@Mock PlotViewManifestation mockView;
	PolicyContext context;
	@Mock FeedProvider  mockProvider;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		policy = new PlotStringPolicy();
		context = new PolicyContext();
		viewInfo = new ViewInfo(PlotViewManifestation.class, "Plot", ViewType.OBJECT);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(),
				viewInfo);
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(),
				mockProvider);
	}
	
	@Test
	public void testFeedTypes() {
		// Should return true for all FeedType options, except string
		for (FeedType ft : FeedType.values()) {
			Mockito.when(mockProvider.getFeedType()).thenReturn(ft);
			Assert.assertEquals(policy.execute(context).getStatus(), ft != FeedType.STRING);
		}		
	}
	
}
