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
package gov.nasa.arc.mct.nontimeplot.view.legend;

import gov.nasa.arc.mct.components.AbstractComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class LegendManager extends JPanel {
	private static final long serialVersionUID = 8260092802125531988L;
	private Map<String, LegendEntryView> legendEntries = new HashMap<String, LegendEntryView>();
	
	public LegendManager(Collection<AbstractComponent> children) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for (AbstractComponent child : children) {
			LegendEntryView v = (LegendEntryView) LegendEntryView.VIEW_INFO.createView(child);			
			legendEntries.put(child.getComponentId(), v);
			v.setAlignmentX(LEFT_ALIGNMENT);
			add (v);
		}
		setOpaque(false);
	}
	
	public LegendEntryView getLegendEntry(AbstractComponent comp) {
		return legendEntries.get(comp.getComponentId());
	}
	
}
