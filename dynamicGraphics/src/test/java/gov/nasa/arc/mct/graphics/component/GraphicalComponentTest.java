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
package gov.nasa.arc.mct.graphics.component;

import gov.nasa.arc.mct.components.ModelStatePersistence;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphicalComponentTest {
	private static final String uriOne = "abc";
	private static final String uriTwo = "XYZ";
	
	
	@Test
	public void testGraphicalModel() {
		GraphicalModel model = new GraphicalModel();
		
		Assert.assertNull( model.getGraphicURI() );
		
		model.setGraphicURI(uriOne);
		Assert.assertEquals(model.getGraphicURI(), uriOne);
		model.setGraphicURI(uriTwo);
		Assert.assertEquals(model.getGraphicURI(), uriTwo);
	}
	
	@Test
	public void testGraphicalComponent() {
		// Ensure that graphical component is a leaf
		GraphicalComponent comp = new GraphicalComponent();
		Assert.assertTrue(comp.isLeaf());
	}
	
	@Test
	public void testGraphicalComponentModelPersistence() {
		GraphicalComponent a = new GraphicalComponent();
		GraphicalComponent b = new GraphicalComponent();
		a.getModelRole().setGraphicURI(uriOne);
		b.getModelRole().setGraphicURI(uriTwo);
		
		ModelStatePersistence persisterA = a.getCapability(ModelStatePersistence.class);
		ModelStatePersistence persisterB = b.getCapability(ModelStatePersistence.class);
		Assert.assertNotNull(persisterA);
		Assert.assertNotNull(persisterB);
		
		// Swap models
		String stateA = persisterA.getModelState();
		String stateB = persisterB.getModelState();
		persisterA.setModelState(stateB);
		persisterB.setModelState(stateA);

		// Ensure they have switched
		Assert.assertEquals(b.getModelRole().getGraphicURI(), uriOne);
		Assert.assertEquals(a.getModelRole().getGraphicURI(), uriTwo);
	}
}
