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
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.fastplot.view.PlotControlsLayout.ResizersScrollPane;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestPlotControlsLayout {

	@Test
	public void testLayout() throws Exception {
        final JFrame frame = new JFrame("CustomLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content panel.
        final PlotControlsLayout layout = new PlotControlsLayout(0);
        final JPanel panel = new JPanel(layout);

        JPanel innerPanel = new JPanel();
        innerPanel.add(new JLabel("Inner Label"));
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
        upperPanel.add(new JButton("Button 2"));
        upperPanel.add(new JButton("Button 3"));
        upperPanel.add(new JButton("Button 4"));
        upperPanel.add(new JButton("Button 5"));
        upperPanel.add(innerPanel);

        JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        lowerPanel.add(new JButton("Apply"));
        lowerPanel.add(new JButton("Reset"));

        ResizersScrollPane scroller = new ResizersScrollPane(upperPanel, innerPanel);
        panel.add(scroller, PlotControlsLayout.MIDDLE);
        panel.add(lowerPanel, PlotControlsLayout.LOWER);
        frame.add(panel);

        //Display the window.
        SwingUtilities.invokeAndWait(new Runnable() {
        	public void run() {
        		frame.pack();
        		frame.setVisible(true);
        	}
        });

        // Test
        final int panelHeight = 150;
        SwingUtilities.invokeAndWait(new Runnable() {
        	public void run() {
        		panel.setSize(200, panelHeight);
        		panel.repaint();
        		layout.layoutContainer(panel);
        	}
        });
        
        Assert.assertEquals(lowerPanel.getLocation().y, panelHeight - lowerPanel.getSize().height - 1);

        final int panelHeight2 = 100;
        SwingUtilities.invokeAndWait(new Runnable() {
        	public void run() {
        		panel.setSize(200, panelHeight2);
        		panel.repaint();
        		layout.layoutContainer(panel);
        	}
        });
        Assert.assertEquals(lowerPanel.getLocation().y, panelHeight2 - lowerPanel.getSize().height - 1);

        layout.resetSizeFlag();
        Assert.assertTrue(layout.minimumLayoutSize(panel).height > lowerPanel.getSize().height);
        layout.resetSizeFlag();
        Assert.assertTrue(layout.maximumLayoutSize(panel).height > lowerPanel.getSize().height);
        layout.invalidateLayout(null);
        layout.addLayoutComponent("", null);
        layout.removeLayoutComponent(scroller);
        layout.removeLayoutComponent(lowerPanel);
        Assert.assertEquals(layout.getLayoutAlignmentX(null), 0.5f);
        Assert.assertEquals(layout.getLayoutAlignmentY(null), 0.5f);
	}

}
