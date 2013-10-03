package org.acme.example;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;

import org.acme.example.policy.MultiColViewPolicy;
import org.acme.example.view.MultiColView;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * We test the execution of the policy associated with the multi-column view
 * 
 * @author Harleigh
 *
 */
public class MultiColPolicyTest {

	private Policy policy;			//This policy will be a MultiColView policy
	private PolicyContext context;	//Note: PolicyContext is 'final' and so can not be mocked
	
	@BeforeTest
	public void setup() {
		MockitoAnnotations.initMocks(this);
		policy = new MultiColViewPolicy();
		context = new PolicyContext();
	}
	
	/*Testing the execution of the MultiColView policy.  We are testing for the following behavior:
	 * *When the viewClass context is MultiColView.class:
	 *      -Components which are leaves may not be viewed by MultiColView; hence
	 *       the policy should return false.
	 *      -Components which are not leaves are viewable by MultiColView; hence
	 *       the policy should return true.
	 * *When the viewClass context is not MultiColView.class:
	 *      -The policy should always return true (regardless of whether the component
	 *       is or is not a leaf.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testExecute() {
		ExecutionResult result = null;
		AbstractComponent mockComp = Mockito.mock(AbstractComponent.class);
		ViewInfo mockViewInfo = Mockito.mock(ViewInfo.class);
		
		/*
		 * From (the below two lines), calling getProperty with the key 'PolicyContext.PropertyName.TARGET_COMPONENT.getName()'
		 * returns our mocked component. Similarly calling getProperty with the key
		 * 'PolicyContext.PropertyName.TARGET_VIEW_INFO.getName()' returns our mocked viewInfo.
		 */
		context.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), mockComp);
		context.setProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), mockViewInfo);

		//Now we test:

		/*Two tests (for leaf and non-leaf components) when the ViewInfo is from the MultiColView class*/
		Mockito.when((Class)mockViewInfo.getViewClass()).thenReturn(MultiColView.class);
			//(1) Test for leaf-components: 
		    Mockito.when(mockComp.isLeaf()).thenReturn(true);	//mocked component is a leaf
			result = policy.execute(context);	//context is: the component is a leaf, and the viewInfo is from MultiColView
			Assert.assertEquals(result.getStatus(), false);		//components which are leaves may not be viewed wrt MultiColView
			//---end test on leaf-components
			
			//(2) Test for non-leaf components:
		    Mockito.when(mockComp.isLeaf()).thenReturn(false);	//mocked component is not a leaf
			result = policy.execute(context);					//context is: the component is a leaf, and the viewInfo is from MultiColView
			Assert.assertEquals(result.getStatus(), true);		//components which are leaves are viewable wrt MultiColView
			//---end test on non-leaf components
			
		/*Two tests (for leaf and non-leaf components) when the ViewInfo is not from the MultiColView class*/
		Mockito.when((Class)mockViewInfo.getViewClass()).thenReturn(OnStiltsView.class);
			//(1) Test for leaf-components: 
		    Mockito.when(mockComp.isLeaf()).thenReturn(true);	//mocked component is a leaf
			result = policy.execute(context);	//context is: the component is a leaf, and the viewInfo is not from MultiColView
			Assert.assertEquals(result.getStatus(), true);		//policy should return true for views that are not MultiCol views
			//---end test on leaf-components
			
			//(2) Test for non-leaf components:
		    Mockito.when(mockComp.isLeaf()).thenReturn(false);	//mocked component is not a leaf
			result = policy.execute(context);					//context is: the component is a leaf, and the viewInfo is not from MultiColView
			Assert.assertEquals(result.getStatus(), true);		//policy should return true for views that are not MultiCol views
			//---end test on non-leaf components
	}
	
	/*
	 * This exists so we can test the MultiColView policy when executing the policy when the context has the viewInfo not being-from
	 * the MultiCol class.  That is  we just needed a view which is not the view of the MultiColView class (Also, when you are on stilts,
	 * your view is extended)
	 */
	@SuppressWarnings("serial")
	private class OnStiltsView extends View {}
}
