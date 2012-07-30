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
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.gui.util.UniqueNameGenerator;

import java.awt.Container;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Implements a factory that creates arbitrarily nested split panes.
 * This class is used to divide up the area of a parent component
 * into multiple splittable areas to fit any number of interior
 * panels. 
 * 
 * @author nshi
 *
 */
public class MCTSplitPaneFactory {
	
    private static final String SPLIT_PANE_NAME = "splitPane";

    /**
     * Creates a series of {@link javax.swing.JSplitPane} objects to hold the
     * given panels, in a horizontal or vertical orientation. If the number
     * of panels is <em>n</em>, we create <em>n-1</em> instances of <code>JSplitPane</code>.
     * The first holds the first panel and all the other <code>JSplitPane</code> instances,
     * and so on recursively.
     * 
     * @param container the container in which we will place the split panes
     * @param panels the list of panels to split the container among
     * @param orientation the orientation, horizontal or vertical
     * @return a component that contains all the panels, organized into split panes
     */
	public static JComponent createSplitPanes (Container container, List<JPanel> panels, int orientation) {
	    // TODO: Fix this workaround: Ran into a divide by zero condition; so, temporarily
	    // added return statement for new JSplitPane..
	    if (panels.size() == 0) {
	        JSplitPane splitter = new JSplitPane();
	        splitter.setOneTouchExpandable(true);
            instrumentNames(splitter);
	        return splitter;
	    }
	    
        return createSplitPanes(container, panels, 0, panels.size() - 1, container.getWidth() / panels.size(), orientation);
	}

	private static JComponent createSplitPanes(Container container, List<? extends JComponent> components, int start, int end, int divide, int orientation) {
		if (start == end)
			return components.get(start);
		else if (end - start == 1) {
			JSplitPane splitPane = new JSplitPane(orientation, components.get(start), components.get(end));
			splitPane.setDividerLocation(divide);
			splitPane.setContinuousLayout(true);
			splitPane.setOneTouchExpandable(true);
			splitPane.setBorder(null);
			instrumentNames(splitPane);
			return splitPane;
		}
		else {
			int middle = (start + end) / 2;
			JSplitPane splitPane = new JSplitPane(orientation, 
								  				  createSplitPanes(container, components, start, middle, divide, orientation),
								  				  createSplitPanes(container, components, middle + 1, end, divide, orientation));
			
			splitPane.setDividerLocation(divide * (middle + 1));
			splitPane.setContinuousLayout(true);
			splitPane.setOneTouchExpandable(true);
			splitPane.setBorder(null);
            instrumentNames(splitPane);

			return splitPane;
		}
	}

    private static void instrumentNames(JSplitPane splitter) {
        splitter.setName(UniqueNameGenerator.get(SPLIT_PANE_NAME));
    }
}
