/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.fastplot.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.Placeholder;
import gov.nasa.arc.mct.evaluator.api.Evaluator;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.util.ArrayList;
import java.util.List;


public class PlotViewPolicy implements Policy {
	
	static boolean hasFeed(AbstractComponent component) {
		return component.getCapability(FeedProvider.class)  != null || (component.getCapability(Placeholder.class) != null);
	}
	
	@Override
	public ExecutionResult execute(PolicyContext context) {
		boolean result = true;
		
		ViewInfo viewInfo = context.getProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), ViewInfo.class);
	
		if (viewInfo.getViewClass().equals(PlotViewManifestation.class)) {
			AbstractComponent targetComponent = context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class);
			
			if (targetComponent.isLeaf() && !hasFeed(targetComponent)) {
				String message = "Leaf is not a feed.";
				return new ExecutionResult(context, false, message);
			}
			
			result = !rejectCanvasView(context,targetComponent);
		}
		
		String message;
		if (result) {
           message = "Requested Plot View has children with data feeds.";
		} else {
		   message = "Requested Plot View did not have children with data feeds.";
		}
		return new ExecutionResult(context, result, message);
	}
	
	/**
	 * Only allow the plot view to be visible in a canvas view if the component is a leaf, this allow the plot view to be the default canvas
	 * for a FeedProvider. However, since the plot view is now also a canvas view always allow the canvas view role if the plot view role is
	 * asked for directly. 
	 * @param context
	 * @param component
	 * @return
	 */
	private boolean rejectCanvasView(PolicyContext context, AbstractComponent component) {
		 return ViewType.CENTER.equals(context.getProperty(PolicyContext.PropertyName.VIEW_TYPE.getName())) &&
		        !component.isLeaf();
	}
	
	private final static AbstractComponent[][] COMPONENT_MATRIX_TYPE = new AbstractComponent[0][];
	private final static AbstractComponent[] COMPONENT_VECTOR_TYPE = new AbstractComponent[0];
	
	/**
	 * Gets the 2-dimensional matrix of components where the children are collections
	 * and the grand children are data feeds.
	 *  
	 * @param targetComponent the parent component of the matrix 
	 * @param ordinalPosition if the ordinal position in the list should determine the stacked plot
	 * @return a 2-dimensional array of components making up the matrix
	 */
	public static AbstractComponent[][] getPlotComponents(AbstractComponent targetComponent, boolean ordinalPosition) {
		List<AbstractComponent[]> subPlots = new ArrayList<AbstractComponent[]>();
		if (targetComponent!=null){
			
			if (hasFeed(targetComponent)) {
			    // We need only add this component to the plot. 
				List<AbstractComponent> phantomChildren = new ArrayList<AbstractComponent>();
				phantomChildren.add(targetComponent);
			    subPlots.add(phantomChildren.toArray(COMPONENT_VECTOR_TYPE));
			    
			} else if (isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(targetComponent)) {
				List<AbstractComponent> children = new ArrayList<AbstractComponent>();
				for (AbstractComponent component : targetComponent.getComponents()) {
					if (hasFeed(component)) {
						children.add(component);
					}
				}
				subPlots.add(children.toArray(COMPONENT_VECTOR_TYPE));
				
			} else if (isCompoundComponentWithCompoundChildrenThatRequirePlots(targetComponent)) {
				List<List<AbstractComponent>> stackedPlots = new ArrayList<List<AbstractComponent>>();
				if (ordinalPosition) {
					// match based on ordinal position in the list
					for (AbstractComponent component : targetComponent.getComponents()) {
						if (!component.isLeaf() && !isEvaluator(component)) {
							int childCount = 0;
							for (AbstractComponent childComponent : component.getComponents()) {
								if (hasFeed(childComponent)) {
									if (stackedPlots.size() < ++childCount) {
										stackedPlots.add(new ArrayList<AbstractComponent>());
									}
									stackedPlots.get(childCount-1).add(childComponent);
									if (childCount == PlotConstants.MAX_NUMBER_SUBPLOTS) {
										break;
									}
								}
							}
						}
					}
				} else {
					int currentStackedPlots = 0;
					for (AbstractComponent component : targetComponent.getComponents()) {
						if (!component.isLeaf() && !isEvaluator(component)) {
							if (++currentStackedPlots == PlotConstants.MAX_NUMBER_SUBPLOTS ) {
								break;
							}
							List<AbstractComponent> feeds = new ArrayList<AbstractComponent>();
							for (AbstractComponent childComponent : component.getComponents()) {
								if (hasFeed(childComponent)) {
									feeds.add(childComponent);
								}
							}
							stackedPlots.add(feeds);
						}
					}
				}
				for (List<AbstractComponent> stackedPlot : stackedPlots) {
					subPlots.add(stackedPlot.toArray(COMPONENT_VECTOR_TYPE));
				}
			}
		} 
		
		return subPlots.toArray(COMPONENT_MATRIX_TYPE);
	}
	
	/**
	 * Returns true if the component is an evaluator component.
	 * @param component to determine whether it is an evaluator
	 * @return true if the component is an evaluator, false otherwise
	 */
	private static boolean isEvaluator(AbstractComponent component) {
		return !component.isLeaf() && component.getCapability(Evaluator.class) != null;
	}
	
	/**
	 * Returns true if component is a compound component (has children) and at least one of those children
	 * isALeafComponentThatRequiresAPlot. False otherwise.
	 * 
	 * @param component
	 * @return
	 */
    static boolean isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(AbstractComponent component) {
    	if (component.isLeaf()) {
    		return false;
    	}
    	
    	for (AbstractComponent childComponent : component.getComponents()) {
    		if (hasFeed(childComponent)) {
    			return true;
    		}
    	}
    	return false;
    }
	
    /**
	 * Returns true if component is a compound component and all its children are compound components with
	 * at least one grand child component that isALeafComponentThatRequiresAPlot.
	 * 
	 * @param component
	 * @return
	 */
    static boolean isCompoundComponentWithCompoundChildrenThatRequirePlots(AbstractComponent component) {
    	if (component.isLeaf() ||
    	    isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(component)) {
    		return false;
    	}
    	
    	// We no know that all children of component are not leafs that require a plot. 
    	for (AbstractComponent childComponent : component.getComponents()) {
			if (!childComponent.isLeaf() && isCompoundComponentWithAtLeastOneChildThatIsALeafAndThatRequiresAPlot(childComponent)) {
				return true;
			}
		}
	    return false;
    }
    
    public static boolean isScatterPlottable(AbstractComponent comp) {
    	int count = 0;
    	for (AbstractComponent child : comp.getComponents()) {
    		if (!child.isLeaf()) {
    			return false;
    		}
    		if (child.getCapability(FeedProvider.class) != null) {
    			count++;
    		}
    	}
    	return count >= 2;
    }
    
    public static boolean isOverlaidScatterPlottable(AbstractComponent comp) {
    	for (AbstractComponent child : comp.getComponents()) {
    		if (!isScatterPlottable(child)) {
    			return false;
    		}
    	}
    	return comp.getComponents().size() > 0;
    }
}
