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
package gov.nasa.arc.mct.table.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConstraintBuilderTest {

	private JPanel panel;
	private ConstraintBuilder builder;
	
	@BeforeMethod
	public void init() {
		panel = new JPanel();
		builder = new ConstraintBuilder(panel);
	}
	
	@Test
	public void testInitialization() {
		GridBagConstraints defaults = new GridBagConstraints();
		
		// We default to grid position 0,0.
		defaults.gridx = 0;
		defaults.gridy = 0;
		
		GridBagConstraints newConstraints = builder.getConstraints();
		
		assertConstraintsEqual(newConstraints, defaults);
	}

	private void assertConstraintsEqual(GridBagConstraints actual, GridBagConstraints expected) {
		assertEquals(actual.anchor, expected.anchor);
		assertEquals(actual.fill, expected.fill);
		assertEquals(actual.gridheight, expected.gridheight);
		assertEquals(actual.gridwidth, expected.gridwidth);
		assertEquals(actual.gridx, expected.gridx);
		assertEquals(actual.gridy, expected.gridy);
		assertEquals(actual.insets, expected.insets);
		assertEquals(actual.ipadx, expected.ipadx);
		assertEquals(actual.ipady, expected.ipady);
		assertEquals(actual.weightx, expected.weightx);
		assertEquals(actual.weighty, expected.weighty);
	}
	
	@Test
	public void testReset() {
		GridBagConstraints defaults = builder.getConstraints();
		
		builder.span(2,3);
		builder.reset();
		assertConstraintsEqual(builder.getConstraints(), defaults);
		
		JPanel dummy = new JPanel();
		builder.at(0,0).add(dummy);
		
		// We've moved to a new column.
		defaults.gridx = 1;
		defaults.gridy = 0;
		assertConstraintsEqual(builder.getConstraints(), defaults);

		// Test moving to a new row.
		defaults.gridx = 0;
		++defaults.gridy;
		assertConstraintsEqual(builder.nextRow().getConstraints(), defaults);
	}
	
	@Test
	public void testMakeDefault() {
		GridBagConstraints defaults = builder.getConstraints();
		assertEquals(defaults.fill, GridBagConstraints.NONE);
		
		builder.vfill().makeDefault();
		builder.reset();
		
		GridBagConstraints newConstraints = builder.getConstraints();
		assertEquals(newConstraints.fill, GridBagConstraints.VERTICAL);
	}
	
	@Test
	public void testInsets() {
		GridBagConstraints defaults = builder.getConstraints();
		assertEquals(defaults.insets, new Insets(0,0,0,0));
		
		builder.insets(1, 2, 3, 4);
		GridBagConstraints newConstraints = builder.getConstraints();
		assertEquals(newConstraints.insets, new Insets(1,2,3,4));
	}
	
	@Test
	public void testgetConstraints() {
		GridBagConstraints constraints = builder.getConstraints();
		
		// Another call should give us a unique object.
		assertNotSame(builder.getConstraints(), constraints);
	}
	
	@Test
	public void testAt() {
		GridBagConstraints c;
		
		c = builder.at(2,1).getConstraints();
		assertEquals(c.gridy, 2);
		assertEquals(c.gridx, 1);
		
		c = builder.at(4, 3).getConstraints();
		assertEquals(c.gridy, 4);
		assertEquals(c.gridx, 3);
	}
	
	@Test(enabled=false)
	public void testSpan() {
		GridBagConstraints c;
		
		c = builder.span(2,1).getConstraints();
		assertEquals(c.gridheight, 2);
		assertEquals(c.gridwidth, 1);
		
		c = builder.span(4, 3).getConstraints();
		assertEquals(c.gridheight, 4);
		assertEquals(c.gridwidth, 3);
		
		// getConstraints() should have reset to a 1x1 span.
		c = builder.getConstraints();
		assertEquals(c.gridheight, 1);
		assertEquals(c.gridwidth, 1);
	}
	
	@Test
	public void testHpad() {
		GridBagConstraints c;
		
		c = builder.hpad(100).getConstraints();
		assertEquals(c.ipadx, 100);
		
		c = builder.hpad(5).getConstraints();
		assertEquals(c.ipadx, 5);
	}
	
	@Test
	public void testVpad() {
		GridBagConstraints c;
		
		c = builder.vpad(100).getConstraints();
		assertEquals(c.ipady, 100);
		
		c = builder.vpad(5).getConstraints();
		assertEquals(c.ipady, 5);
	}
	
	@Test
	public void testBaseline_w() {
		GridBagConstraints c;
		
		c = builder.baseline_w().getConstraints();
		assertEquals(c.anchor, GridBagConstraints.BASELINE_LEADING);
	}

	@Test
	public void testBaseline_e() {
		GridBagConstraints c;
		
		c = builder.baseline_e().getConstraints();
		assertEquals(c.anchor, GridBagConstraints.BASELINE_TRAILING);
	}

	@Test
	public void testBaseline_centered() {
		GridBagConstraints c;
		
		c = builder.baseline_centered().getConstraints();
		assertEquals(c.anchor, GridBagConstraints.BASELINE);
	}

	@Test
	public void testNw() {
		GridBagConstraints c;
		
		c = builder.nw().getConstraints();
		assertEquals(c.anchor, GridBagConstraints.NORTHWEST);
	}

	@Test
	public void testSw() {
		GridBagConstraints c;
		
		c = builder.sw().getConstraints();
		assertEquals(c.anchor, GridBagConstraints.SOUTHWEST);
	}

	@Test
	public void w() {
		GridBagConstraints c;
		
		c = builder.w().getConstraints();
		assertEquals(c.anchor, GridBagConstraints.WEST);
	}

	@Test
	public void testHfill() {
		GridBagConstraints c;
		
		c = builder.hfill().getConstraints();
		assertEquals(c.fill, GridBagConstraints.HORIZONTAL);
	}

	@Test
	public void testVfill() {
		GridBagConstraints c;
		
		c = builder.vfill().getConstraints();
		assertEquals(c.fill, GridBagConstraints.VERTICAL);
	}

	@Test
	public void testHvfill() {
		GridBagConstraints c;
		
		c = builder.hvfill().getConstraints();
		assertEquals(c.fill, GridBagConstraints.BOTH);
	}
	
	@Test
	public void testAdd() {
		builder.nw().makeDefault();

		JPanel dummy = new JPanel();
		GridBagConstraints constraints = builder.getConstraints();
		builder.add(dummy);
		
		GridBagLayout layout = (GridBagLayout) panel.getLayout();
		GridBagConstraints actual = layout.getConstraints(dummy);
		
		assertConstraintsEqual(actual, constraints);
	}
	
	@Test
	public void testAddMultiple() {
		JPanel dummy1 = new JPanel();
		JPanel dummy2 = new JPanel();
		
		builder.add(dummy1, dummy2);
		
		// Should have 1 child for grid location 0,0.
		assertEquals(panel.getComponentCount(), 1);
		JPanel c = (JPanel) panel.getComponents()[0];
		
		// That child should have 2 children for the dummy panels.
		assertEquals(c.getComponentCount(), 2);
		assertSame(c.getComponents()[0], dummy1);
		assertSame(c.getComponents()[1], dummy2);
	}

	@Test
	public void testNextRow() {
		GridBagConstraints defaults = builder.getConstraints();
		
		builder.nextRow();
		assertEquals(builder.getConstraints().gridy, defaults.gridy + 1);
	}

	@Test
	public void testNextColumn() {
		GridBagConstraints defaults = builder.getConstraints();
		
		builder.nextColumn();
		assertEquals(builder.getConstraints().gridx, defaults.gridx + 1);
	}
	
	@Test
	public void testHbox() {
		Component c = ConstraintBuilder.hbox(5);
		assertEquals(c.getMinimumSize().width, 5);
		assertEquals(c.getMinimumSize().height, 0);
		assertEquals(c.getPreferredSize().width, 5);
		assertEquals(c.getPreferredSize().height, 0);
		assertEquals(c.getMaximumSize().width, 5);
		assertEquals(c.getMaximumSize().height, 0);
		
		Component c2 = ConstraintBuilder.hbox(5, 10, 15);
		assertEquals(c2.getMinimumSize().width, 5);
		assertEquals(c2.getMinimumSize().height, 0);
		assertEquals(c2.getPreferredSize().width, 10);
		assertEquals(c2.getPreferredSize().height, 0);
		assertEquals(c2.getMaximumSize().width, 15);
		assertEquals(c2.getMaximumSize().height, 0);
	}

}
