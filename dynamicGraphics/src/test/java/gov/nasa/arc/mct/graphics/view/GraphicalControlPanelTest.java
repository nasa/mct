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
package gov.nasa.arc.mct.graphics.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.KeyPressInfo;
import org.fest.swing.core.Robot;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class GraphicalControlPanelTest {
	private static final String WINDOW_TITLE = "GraphicalControlPanel test";
	
	private Robot robot;
	
	private final Map<String, String> defaultMap = new HashMap<String, String>();		
	
	private JFrame testFrame;
	private GraphicalSettings settings;
	private GraphicalControlPanel controls;

	private AbstractComponent mockComponent = new AbstractComponent() {
		
		@Override
		public Collection<AbstractComponent> getReferencingComponents() {
			return Collections.<AbstractComponent> emptyList();
		} 
		
	};
	@Mock private GraphicalManifestation mockView;
	private ExtendedProperties           viewProperties;
	
	
	@BeforeClass
	public void setupClass() {

		defaultMap.put(GraphicalSettings.GRAPHICAL_SHAPE,            GraphicalSettings.DEFAULT_SHAPE);
		defaultMap.put(GraphicalSettings.GRAPHICAL_BACKGROUND_COLOR, GraphicalSettings.DEFAULT_BACKGROUND_COLOR);
		defaultMap.put(GraphicalSettings.GRAPHICAL_OUTLINE_COLOR,    GraphicalSettings.DEFAULT_OUTLINE_COLOR);
		defaultMap.put(GraphicalSettings.GRAPHICAL_OUTLINE_WEIGHT,   GraphicalSettings.DEFAULT_OUTLINE_WEIGHT);
		defaultMap.put(GraphicalSettings.GRAPHICAL_FOREGROUND_FILL,  GraphicalSettings.DEFAULT_FOREGROUND_FILL);
		defaultMap.put(GraphicalSettings.GRAPHICAL_FOREGROUND_COLOR, GraphicalSettings.DEFAULT_FOREGROUND_COLOR);
		defaultMap.put(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN,   GraphicalSettings.DEFAULT_FOREGROUND_MIN);
		defaultMap.put(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX,   GraphicalSettings.DEFAULT_FOREGROUND_MAX);
		defaultMap.put(GraphicalSettings.GRAPHICAL_EVALUATOR,        GraphicalSettings.DEFAULT_EVALUATOR);
		defaultMap.put(GraphicalSettings.GRAPHICAL_EVALUATOR_MAP,    GraphicalSettings.DEFAULT_EVALUATOR_MAP);
	}
	

	@BeforeMethod
	public void setupTest() {
		
		testFrame = new JFrame(WINDOW_TITLE);

		MockitoAnnotations.initMocks(this);
		
		viewProperties = new ExtendedProperties();
		Mockito.when(mockView.getManifestedComponent()).thenReturn(mockComponent);
		Mockito.when(mockView.getViewProperties()).thenReturn(viewProperties);	
		
		mockComponent.setDisplayName("Test Component");
		
		//Mockito.when(mockComponent.getCapability(Evaluator.class)).thenReturn(null);
		//Mockito.when(mockComponent.getMasterComponent()).thenReturn(null);
		//Mockito.when(mockComponent.getReferencingComponents()).thenReturn(Collections.<AbstractComponent>emptyList());
		
		settings  = new GraphicalSettings(mockView) {

			@Override
			public Collection<String> getSupportedEnumerations() {
				return Collections.<String> emptyList();
			}


			@Override
			public Collection<Object> getSupportedEvaluators() {
				return Collections.<Object> singleton(mockComponent);
			}	
			

			@Override
			public Object getSetting(String name) {				
				return this.getNamedObject(defaultMap.get(name));
			}
			
		};
	
		Mockito.when(mockView.getSettings()).thenReturn(settings);
		controls =  new GraphicalControlPanel(mockView);
		
		GuiActionRunner.execute( new GuiTask() {

			@Override
			protected void executeInEDT() throws Throwable {		 
		
				testFrame.setName(WINDOW_TITLE);
				testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				testFrame.getContentPane().add(controls);
				testFrame.setSize(800, 600);
				testFrame.pack();
				testFrame.setVisible(true);
				
			}
		});
		
		robot = BasicRobot.robotWithCurrentAwtHierarchy();	
	
	}
	
	@AfterMethod
	public void cleanUpTest() {
		robot.cleanUp();
	}
	
	
	@Test
	public void testDropDowns() {
		String[] dropDowns = { GraphicalSettings.GRAPHICAL_SHAPE,
				               GraphicalSettings.GRAPHICAL_OUTLINE_COLOR,
				               GraphicalSettings.GRAPHICAL_BACKGROUND_COLOR,
				               GraphicalSettings.GRAPHICAL_FOREGROUND_FILL,
				               GraphicalSettings.GRAPHICAL_FOREGROUND_COLOR };
		
		FrameFixture window = WindowFinder.findFrame(WINDOW_TITLE).using(robot);
			
		for (String propertyName : dropDowns) {
			Assert.assertNull   (viewProperties.getProperty(propertyName, String.class));
			// Note, this is sensitive to defaults (only calls on change)
			// So, we do it at least twice every time
			window.comboBox(propertyName).selectItem(1); 
			window.comboBox(propertyName).selectItem(2); 
			Assert.assertNotNull(viewProperties.getProperty(propertyName, String.class));					
		}

	}
	
	@Test
	public void testMinMax() {
		FrameFixture window = WindowFinder.findFrame(WINDOW_TITLE).using(robot);

		verifyMinMax(null, null);		
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).enterText("10");
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).enterText("20");
		// Just changing text shouldn't change property
		verifyMinMax(null, null); 
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));		
		verifyMinMax("10", "20");		

		// Bad data should not make changes
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).deleteText();
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).enterText("cat");
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).deleteText();
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).enterText("dog");		
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));
		verifyMinMax("10", "20"); 
		
		// Min > Max should not change settings
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).deleteText();
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).enterText("30");
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).deleteText();
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).enterText("20");		
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));
		verifyMinMax("10", "20"); 
		
		// But now settings change should work
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).deleteText();
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).enterText("30");
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).deleteText();
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX).enterText("40");		
		window.textBox(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN).pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_ENTER));
		verifyMinMax("30", "40"); 		
	}
	
	/**
	 * Min and Max should always have the same number of setViewProperty calls
	 * @param times
	 */
	private void verifyMinMax(String min, String max) {
		Assert.assertEquals(viewProperties.getProperty(GraphicalSettings.GRAPHICAL_FOREGROUND_MIN, String.class), min);
		Assert.assertEquals(viewProperties.getProperty(GraphicalSettings.GRAPHICAL_FOREGROUND_MAX, String.class), max);
	}
	

}


