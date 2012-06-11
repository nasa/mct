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
package plotter;


/**
 * Calculates tick marks and labels for an axis.
 * @author Adam Crume
 */
public interface TickMarkCalculator {
	/**
	 * Calculates tick marks.
	 * The values of major and minor tick marks are returned.
	 * Major values <b>must</b> be in sorted order.
	 * @param axis axis that needs tick marks and labels
	 * @return values of the major tick marks at index 0, and the minor tick marks at index 1
	 */
	public double[][] calculateTickMarks(Axis axis);
}
