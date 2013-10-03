package org.acme.example.policy;

import org.acme.example.view.MultiColView;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;


/*
 * This class tells MCT what types of components that can be viewed by the MultiColumn plug-ion. If the
 * selected component is not a leaf (ref: isLeaf in AbstractComponent) then it and all of its children 
 * may be viewed by the MultiColumnView
 * 
 * MultiColComponentProvider tells MCT that this is the class that is handling the policies for
 * the multi-column plug-in.
 */
public class MultiColViewPolicy implements Policy {

	/*
	 * Tells MCT what components may be viewed by the MultiColView:  any component that is not a leaf
	 * may be viewed by the MultiColView 
	 */
	@Override
	public ExecutionResult execute(PolicyContext context) {
		boolean result = true;
		ViewInfo viewInfo = context.getProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), ViewInfo.class);

		if (MultiColView.class.isAssignableFrom(viewInfo.getViewClass())) {
			result = canView(context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class));
		}

		return new ExecutionResult(context, result, null);
	}
	
	/*
	 * Any component that is not a leaf may be viewed in the multi-column view of MCT
	 */
	private boolean canView(AbstractComponent component) {
		if( component.isLeaf() )
			return false;
		else
			return true;
	}
	
}