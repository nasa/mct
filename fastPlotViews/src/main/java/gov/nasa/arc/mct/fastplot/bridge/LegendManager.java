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

import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Provides the panel holding the individual legend entries for a plot.
 */
@SuppressWarnings("serial")
public class LegendManager extends JPanel implements MouseListener {

	public static final int MAX_NUMBER_LEGEND_COLUMNS = 1;
	
	// Panel holding the legend items.
	private JPanel innerPanel;
	private Color backgroundColor;
	private LegendEntry legendEntry;
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	private List<LegendEntry> legendEntryList = new ArrayList<LegendEntry> ();
	
	/**
	 * Included only to support unit testing.
	 */
	protected LegendManager() {}
	
	/**
	 * Construct the legend panel for a plot
	 * @param legendBackgroundColor the background color of the legend
	 */
	public LegendManager(AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm) {
		this.plotLabelingAlgorithm = plotLabelingAlgorithm;
				
		//backgroundColor = legendBackgroundColor;
		//setBackground(legendBackgroundColor);
		setLayout(new BorderLayout());

		innerPanel = new JPanel();	
		//innerPanel.setBackground(legendBackgroundColor);	
		
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setOpaque(false);
		setOpaque(false);
		add(innerPanel, BorderLayout.NORTH);	
		setVisible(false);
	}	

	/**
	 * Add new entry to the legend.
	 * @param entry to add
	 */
	public void addLegendEntry(AbstractLegendEntry entry) {
		if (entry instanceof JComponent) {
			((JComponent) entry).setBackground(backgroundColor);
			if (entry instanceof LegendEntry) {		
				legendEntry = (LegendEntry) entry;
				legendEntry.setPlotLabelingAlgorithm(this.plotLabelingAlgorithm);
		
				legendEntryList.add((LegendEntry) entry);
			}			
			innerPanel.add((Component) entry);
		}
	}

	
	public List<LegendEntry> getLegendEntryList() {
		return legendEntryList;
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		//do nothing
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
		String toolTipText = legendEntry.getToolTipText();
		legendEntry.setToolTipText(toolTipText);
		this.setToolTipText(toolTipText);
			
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//do nothing
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//do nothing
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//do nothing
	}
	
	@Override
	public String getToolTipText() {
		return legendEntry.getToolTipText();
	}
}
