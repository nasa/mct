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
package gov.nasa.arc.mct.table.policy;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.Placeholder;
import gov.nasa.arc.mct.evaluator.api.Evaluator;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.Policy;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
//import gov.nasa.arc.mct.table.model.TableStructure;
import gov.nasa.arc.mct.table.view.MultiColView;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Implements the policy defining whether the alpha view is available for
 * a component.
 */
public class TableViewPolicy implements Policy {
	private static final ResourceBundle bundle = ResourceBundle.getBundle("TablePolicy");
	private static final Pattern        suppressComponents = Pattern.compile(bundle.getString("Suppress_Components"));
	
	private static boolean hasFeed(AbstractComponent component) {
		return component.getCapability(FeedProvider.class)  != null;
	}

	private static boolean hasEvaluator(AbstractComponent component) {
		return component.getCapability(Evaluator.class) != null;
	}
	
	private static boolean isViewableEvaluator(AbstractComponent component) {
		return hasEvaluator(component) &&
		component.getCapability(Evaluator.class).requiresMultipleInputs();
	}
	
	private static boolean isEvaluatorComponent(AbstractComponent component) {
		return hasEvaluator(component) && !hasFeed(component);
	}

	@Override
	public ExecutionResult execute(PolicyContext context) {
		boolean result = true;

		ViewInfo viewInfo = context.getProperty(PolicyContext.PropertyName.TARGET_VIEW_INFO.getName(), ViewInfo.class);
		if (viewInfo.getViewClass().equals(MultiColView.class)) {
			AbstractComponent targetComponent = context.getProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), AbstractComponent.class);
			result = !targetComponent.isLeaf() || hasFeed(targetComponent) || (targetComponent instanceof Placeholder); 
		}
		return new ExecutionResult(context, result, "Requested Table View did not have children with data feeds.");
	}

	/**
	 * Gets a 2-dimensional matrix of components to show in a tabular view. A 2-dimensional
	 * matrix is always returned, although there may be only 1 row or column, if the component
	 * to display is a leaf, or if there are direct children that are leaves.
	 * 
	 * <p>There are 3 cases:</p>
	 * <ul>
	 *   <li>Component is a leaf or has a data feed (0-dimensional table)
	 *   <li>Component is not a leaf, and 1 or more children are leaves or have data feeds (1-dimensional table)
	 *   <li>Component is not a leaf, and if it has children they are all non-leaves without data feeds (2-dimensional table)
	 * </ul>
	 *  
	 * @param targetComponent the parent component of the matrix
	 * @return the table structure, or null if the component cannot be viewed in a tabular view
	 */
	//commented out in tabletype purge 21aug12
	/*public static TableStructure getTableStructure(AbstractComponent targetComponent) {
		if (targetComponent.isLeaf() || hasFeed(targetComponent) || isEvaluatorComponent(targetComponent)) {
			// 0-dimensional table
			return new TableStructure(TableType.ZERO_DIMENSIONAL, targetComponent);
		}
		// The default case: a 2-dimensional structure. //modified by Alex from tableviews code
		return new TableStructure(TableType.TWO_DIMENSIONAL, targetComponent);

		
		// Try to create a 2-dimensional structure.
		// Each child of the component is an object to display as a row
		// or column. The child must have children of its own in order
		// to have 2 dimensions to the table. Each of those grandchildren
		// must have a data feed.
		boolean isTwoDimensional = true;
		childLoop: for (AbstractComponent component : targetComponent.getComponents()) {
			if ((component.isLeaf() || hasEvaluator(component)) && canEmbedInTable(component)) {
				isTwoDimensional = false;
				break childLoop;
			} else {
				for (AbstractComponent gcComponent : component.getComponents()) {
					if (!gcComponent.isLeaf() && !hasFeed(gcComponent) && !isViewableEvaluator(gcComponent)) {
						// We found a nonleaf that doesn't have a value, so we aren't two-dimensional.
						isTwoDimensional = false;
						break childLoop;
					}
				}
			}
		}
		if (isTwoDimensional) {
			return new TableStructure(TableType.TWO_DIMENSIONAL, targetComponent);
		}
	}*/
	
	/**
	 * Determine if this component is displayable as a cell within a table
	 * @param comp the component which might be displayed
	 * @return true if the component can be displayed in a table, false if it should be skipped
	 */
	public static boolean canEmbedInTable(AbstractComponent comp) {
		/* Suppress certain specific components - note that this should be removed once 
		 * rendered redundant to an external policy manager */
		if (suppressComponents.matcher(comp.getComponentTypeID()).matches()) return false;
		
		for (ViewInfo vi : comp.getViewInfos(ViewType.EMBEDDED)) {
			if (MultiColView.class.isAssignableFrom(vi.getViewClass())) {
				return true;
			}
		}
		return false;
	}

}
