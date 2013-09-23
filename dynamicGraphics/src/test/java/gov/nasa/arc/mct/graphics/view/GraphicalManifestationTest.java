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
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.graphics.brush.Brush;
import gov.nasa.arc.mct.graphics.state.StateSensitive;
import gov.nasa.arc.mct.gui.NamingContext;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class GraphicalManifestationTest {
	
	private static final int    SIZE = 300;	
	private static final String SUBSCRIPTION_ID = "mock";
	private static final Color  BACKGROUND = Color.pink;
	
	private GraphicalManifestation view;
	
	Map<String, List<Map<String, String>>> feedData;
	

	@Mock private AbstractComponent  mockComponent;
	@Mock private FeedProvider       mockProvider;
	@Mock private ViewInfo           mockViewInfo;
	@Mock private RenderingInfo      mockRenderingInfo;
	@Mock private ExtendedProperties mockViewProperties;
	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setupTest() {
		MockitoAnnotations.initMocks(this);
				
		Mockito.when(mockComponent.getCapability(FeedProvider.class)).thenReturn(mockProvider);
		Mockito.when(mockComponent.getDisplayName()).thenReturn("test");
		
		
		Mockito.when(mockProvider.getSubscriptionId()).thenReturn(SUBSCRIPTION_ID);
		Mockito.when(mockProvider.getRenderingInfo(Mockito.anyMap())).thenReturn(mockRenderingInfo);
		
		Mockito.when(mockRenderingInfo.isPlottable()).thenReturn(true);
		Mockito.when(mockRenderingInfo.getValueText()).thenReturn("50");
					
		view        = new GraphicalManifestation(mockComponent, mockViewInfo) {
			private static final long serialVersionUID = -3323166317731283322L;

			public ExtendedProperties getViewProperties() {
				return mockViewProperties;
			}
		};
		
		feedData = 
			new HashMap<String, List<Map<String, String>>>();
		Map<String, String> dummyMap = new HashMap<String,String>();
		feedData.put(SUBSCRIPTION_ID, Collections.singletonList(dummyMap));
	}
	
	@Test 
	public void testManifestationPaint() {
		Shape shape = (Shape) view.getSettings().getSetting(GraphicalSettings.GRAPHICAL_SHAPE);
		List<Brush> brushList = view.getSettings().getLayers();
		
		view.setSize(SIZE, SIZE);				
		view.setBackground(BACKGROUND);
		
		final BufferedImage viewImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		final BufferedImage manualImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		
		
		view.updateFromFeed(feedData);
		view.paint(viewImage.getGraphics());
		
		Graphics g = manualImage.getGraphics();
		g.setColor(BACKGROUND);
		g.fillRect(0,0,300,300);
		for (Brush brush : brushList) {
			if (brush instanceof StateSensitive) {
				((StateSensitive) brush).setInterval(0, 100);
				((StateSensitive) brush).setState("50");
			}			
			// Bounds shrunken by 5%
			brush.draw(shape, g, new Rectangle(15,15,270,270));
		}

		for (int x = 0; x < 300; x++) {
			for (int y = 0; y < 300; y++) {				
				Assert.assertEquals(viewImage.getRGB(x, y), manualImage.getRGB(x, y));
			}
		}
		
		
	}
	 
	@Test 
	public void testManifestationNamingContext() {
		Shape shape = (Shape) view.getSettings().getSetting(GraphicalSettings.GRAPHICAL_SHAPE);
		List<Brush> brushList = view.getSettings().getLayers();
		
		view.setSize(SIZE, SIZE);				
		view.setBackground(BACKGROUND);
		view.setNamingContext(new NamingContext() {
			@Override
			public NamingContext getParentContext() {
				return null;
			}
			@Override
			public String getContextualName() {
				return null;
			}			
		});
		
		final BufferedImage viewImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		final BufferedImage manualImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		
		
		view.updateFromFeed(feedData);
		view.paint(viewImage.getGraphics());		
		
		Graphics g = manualImage.getGraphics();
		g.setColor(BACKGROUND);
		g.fillRect(0,0,300,300);
		for (Brush brush : brushList) {
			if (brush instanceof StateSensitive) {
				((StateSensitive) brush).setInterval(0, 100);
				((StateSensitive) brush).setState("50");
			}			
			// Bounds shrunken by 5%
			brush.draw(shape, g, new Rectangle(15,15,270,270));
		}

		// Check to see if something has been drawn differently now that a naming context is set
		// (separate test ensures that drawing is same when no naming context set).
		boolean differenceFound = false;
		for (int x = 0; x < 300; x++) {
			for (int y = 0; y < 300; y++) {				
				differenceFound |= viewImage.getRGB(x, y) != manualImage.getRGB(x, y);
			}
		}
		Assert.assertTrue(differenceFound);
		
	}
	
	@Test 
	public void testControlManifestation() {
		JComponent c = view.initializeControlManifestation();
		Assert.assertTrue(findControlPanel(c));
	}
	
	private boolean findControlPanel(JComponent component) {
		boolean result = component instanceof GraphicalControlPanel;
		for (Component c : component.getComponents()) {
			if (c instanceof JComponent) {
				result |= findControlPanel((JComponent) c);
			}
		}
		return result;
	}
	
	@Test 
	public void testResponseToSettingChange() {
		Shape shape = (Shape) view.getSettings().getSetting(GraphicalSettings.GRAPHICAL_SHAPE);
		List<Brush> originalList = view.getSettings().getLayers();
		
		view.setSize(SIZE, SIZE);				
		view.setBackground(BACKGROUND);
		
		final BufferedImage viewImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		final BufferedImage manualImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		
		
		Graphics g = manualImage.getGraphics();
		g.setColor(BACKGROUND);
		g.fillRect(0,0,300,300);
		for (Brush brush : originalList) {
			if (brush instanceof StateSensitive) {
				((StateSensitive) brush).setInterval(0, 100);
				((StateSensitive) brush).setState("50");
			}			
			// Bounds shrunken by 5%
			brush.draw(shape, g, new Rectangle(15,15,270,270));
		}
		
		/* Try a different outline color */
		Mockito.when(mockViewProperties.getProperty(GraphicalSettings.GRAPHICAL_OUTLINE_COLOR, String.class))
	    	.thenReturn("Color4");
		Color color = (Color) view.getSettings().getNamedObject("Color4");
		
		view.updateMonitoredGUI();
		view.updateFromFeed(feedData);
		view.paint(viewImage.getGraphics());		

		for (int x = 0; x < 300; x++) {
			for (int y = 0; y < 300; y++) {
				
				if (viewImage.getRGB(x, y) == color.getRGB()) {
					/* Outline should be different */
					Assert.assertFalse(viewImage.getRGB(x, y) == manualImage.getRGB(x, y));
				} else {
					/* Everyting else should still be the same */
					Assert.assertTrue (viewImage.getRGB(x, y) == manualImage.getRGB(x, y));
				}
			}
		}	
	}
	
	@Test
	public void testVisibleFeedProvider() {
		Collection<FeedProvider> providers = view.getVisibleFeedProviders();
		Assert.assertEquals(providers.size(), 1);
		Assert.assertEquals(providers.iterator().next(), mockProvider);
	}
}
