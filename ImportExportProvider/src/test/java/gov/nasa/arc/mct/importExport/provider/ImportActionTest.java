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
package gov.nasa.arc.mct.importExport.provider;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.jsc.mct.importExport.utilities.Utilities;
import gov.nasa.jsc.mct.importExport.utilities.XMLFileFilter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ImportActionTest {
	ImportAction importAction;
	ImportThisAction importThisAction;
	List<File> okFiles;
	
	private Platform oldPlatform;
	private Platform mockPlatform;
	private PolicyManager mockPolicy;
	
	@BeforeClass
	public void setupPlatform() {
		oldPlatform = PlatformAccess.getPlatform();
		mockPlatform = Mockito.mock(Platform.class);
		mockPolicy = Mockito.mock(PolicyManager.class);
		Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicy);
		new PlatformAccess().setPlatform(mockPlatform);
	}
	
	@AfterClass
	public void teardownPlatform() {
		new PlatformAccess().setPlatform(oldPlatform);		
	}
	
	@BeforeMethod
	void setup()  { 
		importAction = new ImportAction();
		importThisAction = new ImportThisAction();
	}
	
	@Test 
	public void testActionPerformedFilter(){
   	    okFiles = Utilities.filterSelectedFiles(
   	    		   Arrays.asList(new File("src/test/resources/testOutput/multComp.xml")));
		Assert.assertEquals(okFiles.size(), 1);
		
		okFiles = Utilities.filterSelectedFiles(
				             Arrays.asList(new File("src/test/resources/testOutput/")));
		Assert.assertEquals(okFiles.size(), 5);
	}
	
	@Test 
	public void testXMLFilter(){
		XMLFileFilter xmlfilter = new XMLFileFilter();
		Assert.assertFalse(xmlfilter.accept(new File("")));		
		Assert.assertTrue(xmlfilter.accept(new File("src/test/resources/testOutput/")));
		Assert.assertTrue(xmlfilter.accept(new File("src/test/resources/testOutput/multComp.xml")));
	}
	
	@Test (expectedExceptions = IllegalArgumentException.class)
	public void testActionPerformedFilterNotafile(){
		okFiles = Utilities.filterSelectedFiles(Arrays.asList(new File("UNIT-Test-not-a-file")));
	}

	@Test 
	public void testCanHandle() {
		// Setup mocks
		ActionContext mockContext = Mockito.mock(ActionContext.class);
		View mockView = Mockito.mock(View.class);
		AbstractComponent mockComponent = Mockito.mock(AbstractComponent.class);
		Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);
		
		// This-style import needs a window manifestation
		Mockito.when(mockContext.getWindowManifestation()).thenReturn(null);
		Assert.assertFalse(importThisAction.canHandle(mockContext));
		Mockito.when(mockContext.getWindowManifestation()).thenReturn(mockView);
		Assert.assertTrue(importThisAction.canHandle(mockContext));
		
		// Objects-style import needs exactly one selection
		Mockito.when(mockContext.getSelectedManifestations())
			.thenReturn(Arrays.<View>asList());
		Assert.assertFalse(importAction.canHandle(mockContext));
		Mockito.when(mockContext.getSelectedManifestations())
			.thenReturn(Arrays.<View>asList(mockView, mockView));
		Assert.assertFalse(importAction.canHandle(mockContext));
		Mockito.when(mockContext.getSelectedManifestations())
			.thenReturn(Arrays.<View>asList(mockView));
		Assert.assertTrue(importAction.canHandle(mockContext));		
	}
	
	@Test
	public void testIsEnabled() {
		// Setup mocks (need to invoke canHandle for Action lifecycle)
		ActionContext mockContext = Mockito.mock(ActionContext.class);
		View mockView = Mockito.mock(View.class);
		AbstractComponent mockComponent = Mockito.mock(AbstractComponent.class);
		Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);

		// Call canHandle (for lifecycle)
		Mockito.when(mockContext.getWindowManifestation()).thenReturn(mockView);
		Assert.assertTrue(importThisAction.canHandle(mockContext));
		Mockito.when(mockContext.getSelectedManifestations())
			.thenReturn(Arrays.<View>asList(mockView));
		Assert.assertTrue(importAction.canHandle(mockContext));		

		// Check isEnabled - should depend on policy
		for (boolean truth : new boolean[] {true, false}) {
			Mockito.when(mockPolicy.execute(Mockito.anyString(), Mockito.<PolicyContext>any()))
				.thenReturn(new ExecutionResult(null, truth, ""));
			Assert.assertEquals(importAction.isEnabled(), truth);
			Assert.assertEquals(importThisAction.isEnabled(), truth);
		}
		
	}
}

