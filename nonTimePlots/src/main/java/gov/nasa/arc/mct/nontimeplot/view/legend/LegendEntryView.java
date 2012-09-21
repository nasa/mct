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
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.gui.FeedView;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import plotter.xy.ScatterXYPlotLine;

public class LegendEntryView extends FeedView {
	private static final long serialVersionUID = -2885137579175013142L;
	public static final ViewInfo VIEW_INFO = new ViewInfo(LegendEntryView.class, "Legend Entry", ViewType.EMBEDDED);
	
	private Collection<FeedProvider> feedProviders;
	private JLabel label = new JLabel();
	private Font baseFont;
	private Font strikeThroughFont;
	private ScatterXYPlotLine line = null; // TODO: Make more generic	
	
	
	public LegendEntryView(AbstractComponent ac, ViewInfo vi) {
		super(ac, vi);
		
		setOpaque(false);
		
		label.setText(ac.getDisplayName());
		label.setFont(updateBaseFont(Font.ITALIC));
		label.setForeground(Color.LIGHT_GRAY);
		
		add(label);
		
		FeedProvider fp = ac.getCapability(FeedProvider.class);
		feedProviders = fp==null ? Collections.<FeedProvider>emptyList() : Collections.singleton(fp);
		
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}
	
	public void setPlotLine(ScatterXYPlotLine line) {
		this.line = line;
		label.setForeground(line.getForeground());
		label.setFont(updateBaseFont(Font.PLAIN));
		label.setIcon(new Icon() {

			@Override
			public int getIconHeight() {
				return 9; //TODO: if line has icon...
			}

			@Override
			public int getIconWidth() {
				return 9;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				
			}
			
		});
		// TODO: Attach popup!
		
	}

	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		for (FeedProvider fp : feedProviders) {
			if (data.containsKey(fp.getSubscriptionId())) {
				List<Map<String, String>> series = data.get(fp.getSubscriptionId());
				if (!series.isEmpty()){
					String display = getManifestedComponent().getDisplayName();
					RenderingInfo ri = fp.getRenderingInfo(series.get(series.size() - 1));
					String status = ri.getStatusText();
					if (!status.trim().isEmpty()) display = "(" + status + ") " + display;
					label.setText(display);
					label.setFont(ri.isPlottable() ? baseFont : strikeThroughFont); 
				}
			}
		}
		
	}

	@Override
	public void synchronizeTime(Map<String, List<Map<String, String>>> data,
			long syncTime) {
		updateFromFeed(data);
	}

	@Override
	public Collection<FeedProvider> getVisibleFeedProviders() {
		return feedProviders;
	}
	
	private Font updateBaseFont(int style) {
		baseFont = label.getFont().deriveFont(9f).deriveFont(style);
		
		Map attrs = baseFont.getAttributes();
		attrs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		strikeThroughFont = baseFont.deriveFont(attrs);
		
		return baseFont;
	}
	
	
}
