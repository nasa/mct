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
package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.fastplot.settings.LineSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.LegendEntryPopupMenuFactory;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestLegendEntryPopup {
	private static final int COLOR_SUBMENU       = 0;
	private static final int THICKNESS_SUBMENU   = 1;
	private static final int PREDICTION_CHECKBOX = 3;
	private static final int PREDICTION_SUBMENU  = 4;
	
	@Mock AbbreviatingPlotLabelingAlgorithm mockLabelingAlgorithm;
	@Mock LegendEntryPopupMenuFactory       mockPopupManager; 
	@Mock JPopupMenu                        mockPopup;
	@Mock LegendEntry                       mockLegendEntry;
	@Mock PlotViewManifestation             mockPlotViewManifestation;
	@Mock PlotView                          mockPlotView;
	
	@Mock Platform                          mockPlatform;
	@Mock PolicyManager                     mockPolicyManager;
	
	Platform oldPlatform;
	
	private static final ResourceBundle BUNDLE = 
	        ResourceBundle.getBundle(LegendEntryPopupMenuFactory.class.getName().substring(0, 
	        		LegendEntryPopupMenuFactory.class.getName().lastIndexOf("."))+".Bundle");
	
	@BeforeClass
	public void setupClass() {
		oldPlatform = PlatformAccess.getPlatform();
		
		new PlatformAccess().setPlatform(mockPlatform);		
	}
	
	@AfterClass
	public void teardownClass() {
		new PlatformAccess().setPlatform(oldPlatform);		
	}
	
	@BeforeTest
	public void setupTest() {
		
		MockitoAnnotations.initMocks(this);
		Mockito.when(mockPopupManager.getPopup(Mockito.<LegendEntry> any())).thenReturn(mockPopup);
				
		/* View's isLocked() method is final - it can't be mocked & needs to reference the platform */
		/* So, provide a mock platform */
		Mockito.when(mockPlatform.getPolicyManager())
			.thenReturn(mockPolicyManager);
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
			.thenReturn(new ExecutionResult(null, false, null));
		
		Mockito.when(mockLegendEntry.getComputedBaseDisplayName()).thenReturn("test");
		Mockito.when(mockLegendEntry.getFullBaseDisplayName()).thenReturn("test");
		Mockito.when(mockLegendEntry.getLineSettings()).thenReturn(new LineSettings());
		Mockito.when(mockLegendEntry.getNumberRegressionPoints()).thenReturn(15);

		
	}
	
	private JPopupMenu getSubMenu(JPopupMenu menu, int index) {
		return ((JMenu) (menu.getComponent(index))).getPopupMenu();
	}
	
	/* Tests for LegendEntry's triggering of popup manager behaviors */	
	@Test
	public void testLegendEntryTriggersPopup() {		
		LegendEntry entry = new LegendEntry(Color.WHITE, Color.BLACK, new JLabel().getFont(), mockLabelingAlgorithm);
		entry.setPopup(mockPopupManager);
		entry.mousePressed(new MouseEvent(entry, 0, 0, 0, 0, 0, 0, true ));
		Mockito.verify(mockPopupManager).getPopup(entry);
		Mockito.verify(mockPopup).show(entry, 0, 0);
	}
	
	@Test
	public void testLegendEntryTriggersPopupAtLocation() {
		int x = 100; int y = 120;
		LegendEntry entry = new LegendEntry(Color.WHITE, Color.BLACK, new JLabel().getFont(), mockLabelingAlgorithm);
		entry.setPopup(mockPopupManager);
		entry.mousePressed(new MouseEvent(entry, 0, 0, 0, x, y, 0, true ));
		Mockito.verify(mockPopupManager).getPopup(entry);
		Mockito.verify(mockPopup).show(entry, x, y);
	}

	@Test
	public void testLegendEntryIgnoresNonPopupTriggers () {		
		LegendEntry entry = new LegendEntry(Color.WHITE, Color.BLACK, new JLabel().getFont(), mockLabelingAlgorithm);
		entry.setPopup(mockPopupManager);
		entry.mousePressed(new MouseEvent(entry, 0, 0, 0, 0, 0, 0, false ));		
		Mockito.verify(mockPopupManager, Mockito.never()).getPopup(entry);		
	}
	
	
	/* Tests for LegendEntryPopupMenuManager behavior */
	@Test
	public void testLegendEntryPopupMenuSize() {
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
			.thenReturn(new ExecutionResult(null, false, null));	
		
		LegendEntryPopupMenuFactory manager = new LegendEntryPopupMenuFactory(mockPlotViewManifestation);
		JPopupMenu menu = getSubMenu(manager.getPopup(mockLegendEntry), COLOR_SUBMENU);
		
		Assert.assertEquals(menu.getComponentCount(), PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT);		
	}
	
	@Test
	public void testLegendEntryRegressionMenu() {
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
			.thenReturn(new ExecutionResult(null, false, null));	
		
		LegendEntryPopupMenuFactory manager = new LegendEntryPopupMenuFactory(mockPlotViewManifestation);
		JMenuItem regressionLineCheckbox = ((JMenuItem) manager.getPopup(mockLegendEntry).getComponent(PREDICTION_CHECKBOX));
		Assert.assertEquals(regressionLineCheckbox.getText(), BUNDLE.getString("RegressionLineLabel"));
		Assert.assertFalse(regressionLineCheckbox.isSelected());
		JMenu regressionPointsMenu = ((JMenu) manager.getPopup(mockLegendEntry).getComponent(PREDICTION_SUBMENU));		
		Assert.assertEquals(regressionPointsMenu.getText(), BUNDLE.getString("RegressionPointsLabel"));	
		JSpinner regressionPointsSpinner = (JSpinner) regressionPointsMenu.getMenuComponent(0);
		Assert.assertEquals(regressionPointsSpinner.getModel().getValue(), mockLegendEntry.getNumberRegressionPoints());
	}

	@Test 
	public void testLegendEntryPopupMenuColors() {
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
		.thenReturn(new ExecutionResult(null, false, null));	
	
		LegendEntryPopupMenuFactory manager = new LegendEntryPopupMenuFactory(mockPlotViewManifestation);
		JPopupMenu menu = getSubMenu(manager.getPopup(mockLegendEntry), COLOR_SUBMENU);
		
		/* Draw icons to this to test for color correctness */
		BufferedImage image = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
		Graphics      graphics = image.getGraphics();
		
		for (int i = 0; i < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT; i++) {
			JMenuItem item = (JMenuItem) menu.getComponent(i);
			Icon      icon = item.getIcon();
			icon.paintIcon(item, graphics, 0, 0);
			int drawnRGB = image.getRGB(2, 2); // Go a couple pixels in, in case icon has border
			Assert.assertEquals(PlotLineColorPalette.getColor(i).getRGB(), drawnRGB);
		}
	}
	
	@Test 
	public void testLegendEntryPopupMenuSelection() {
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
		.thenReturn(new ExecutionResult(null, false, null));	
	
		LegendEntryPopupMenuFactory manager = new LegendEntryPopupMenuFactory(mockPlotViewManifestation);
		for (int i = 0; i < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT; i++) {
			Mockito.when(mockLegendEntry.getForeground()).thenReturn(PlotLineColorPalette.getColor(i));
	
			JPopupMenu menu = getSubMenu(manager.getPopup(mockLegendEntry), COLOR_SUBMENU);
			
			JRadioButtonMenuItem  item = (JRadioButtonMenuItem) menu.getComponent(i);
			Assert.assertTrue(item.isSelected());
			for (int j = 0; j < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT; j++) {
				if (j != i) {
					item = (JRadioButtonMenuItem) menu.getComponent(j);
					Assert.assertFalse(item.isSelected());
				}
			}
		}
		
		
	}

	@Test 
	public void testLegendEntryPopupMenuThicknesses() {
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
		.thenReturn(new ExecutionResult(null, false, null));	
	
		LegendEntryPopupMenuFactory manager = new LegendEntryPopupMenuFactory(mockPlotViewManifestation);
		JPopupMenu menu = getSubMenu(manager.getPopup(mockLegendEntry), THICKNESS_SUBMENU );
		
		Assert.assertEquals(menu.getComponentCount(), PlotConstants.MAX_LINE_THICKNESS);
		
		for (int i = 1; i < PlotConstants.MAX_LINE_THICKNESS; i++) {
			JMenuItem item = (JMenuItem) menu.getComponent(i - 1);
			Assert.assertTrue(item.getText().contains(Integer.toString(i)));
		}
	}

	@Test 
	public void testLegendEntryPopupMenuThicknessSelection() {
		Mockito.when(mockPolicyManager.execute(Mockito.anyString(), Mockito.<PolicyContext> any()))
		.thenReturn(new ExecutionResult(null, false, null));	
	
		LegendEntryPopupMenuFactory manager = new LegendEntryPopupMenuFactory(mockPlotViewManifestation);
		for (int i = 0; i < PlotConstants.MAX_LINE_THICKNESS; i++) {
			LineSettings settings = new LineSettings();
			settings.setThickness(i + 1);
			Mockito.when(mockLegendEntry.getLineSettings()).thenReturn(settings);
	
			JPopupMenu menu = getSubMenu(manager.getPopup(mockLegendEntry), THICKNESS_SUBMENU );
			
			JRadioButtonMenuItem  item = (JRadioButtonMenuItem) menu.getComponent(i);
			Assert.assertTrue(item.isSelected());
			for (int j = 0; j < PlotConstants.MAX_LINE_THICKNESS; j++) {
				if (j != i) {
					item = (JRadioButtonMenuItem) menu.getComponent(j);
					Assert.assertFalse(item.isSelected());
				}
			}
		}

	}
	
}
