package gov.nasa.arc.mct.plot.settings;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class PlotSettingsPanel extends PlotSettingsSubPanel {
	private static final long serialVersionUID = -7171700637608907253L;

	private List<PlotSettingsSubPanel> subPanels = new ArrayList<PlotSettingsSubPanel>();

	
	private boolean dirty = false;
	private boolean valid = true;
	
	private final Runnable callback = new Runnable() {
		@Override
		public void run() {
			dirty = false;
			valid = true;
			for (PlotSettingsSubPanel subPanel : subPanels) {
				dirty |= subPanel.isDirty();
				valid &= subPanel.isValidated();
			}			
			fireCallbacks();
		}		
	};
	
	/**
	 * Add a sub panel which is used to formulate settings.
	 * Note that this does not add the subPanel visually
	 * @param subPanel
	 */
	public void addSubPanel(PlotSettingsSubPanel subPanel) {
		if (!subPanels.contains(subPanel)) {
			subPanels.add(subPanel);		
			subPanel.addCallback(callback);
		}
	}
	
	@Override
	public Component add(Component comp) {
		if (comp instanceof PlotSettingsSubPanel) {
			addSubPanel((PlotSettingsSubPanel) comp);
		}
		return super.add(comp);
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component, int)
	 */
	@Override
	public Component add(Component comp, int arg1) {
		if (comp instanceof PlotSettingsSubPanel) {
			addSubPanel((PlotSettingsSubPanel) comp);
		}
		return super.add(comp, arg1);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component, java.lang.Object, int)
	 */
	@Override
	public void add(Component comp, Object arg1, int arg2) {
		if (comp instanceof PlotSettingsSubPanel) {
			addSubPanel((PlotSettingsSubPanel) comp);
		}
		super.add(comp, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
	 */
	@Override
	public void add(Component comp, Object arg1) {
		if (comp instanceof PlotSettingsSubPanel) {
			addSubPanel((PlotSettingsSubPanel) comp);
		}
		super.add(comp, arg1);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#add(java.lang.String, java.awt.Component)
	 */
	@Override
	public Component add(String arg0, Component comp) {
		if (comp instanceof PlotSettingsSubPanel) {
			addSubPanel((PlotSettingsSubPanel) comp);
		}
		return super.add(arg0, comp);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsPopulator#populate(gov.nasa.arc.mct.fastplot.settings.PlotSettings)
	 */
	@Override
	public void populate(PlotConfiguration settings) {
		for (PlotSettingsSubPanel subPanel : subPanels) {
			subPanel.populate(settings);
		}
		dirty = false;
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsPopulator#reset(gov.nasa.arc.mct.fastplot.settings.PlotSettings)
	 */
	@Override
	public void reset(PlotConfiguration settings, boolean hard) {
		for (PlotSettingsSubPanel subPanel : subPanels) {
			subPanel.reset(settings, hard);			
		}
		if (hard) dirty = false;
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public boolean isValidated() {
		return valid;
	}

 
}
