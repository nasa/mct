package gov.nasa.arc.mct.fastplot.view.legend;

import gov.nasa.arc.mct.components.AbstractComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class PlotLegendManager extends JPanel {
	private static final long serialVersionUID = 8260092802125531988L;
	private Map<String, LegendEntryView> legendEntries = new HashMap<String, LegendEntryView>();
	
	public PlotLegendManager(Collection<AbstractComponent> children) {
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
