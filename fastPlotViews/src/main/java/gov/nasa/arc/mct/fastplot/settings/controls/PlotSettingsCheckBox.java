package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettingsSubPanel;

import javax.swing.JCheckBox;

public abstract class PlotSettingsCheckBox extends PlotSettingsSubPanel {
	private static final long serialVersionUID = 5485293797086854968L;
	
	private JCheckBox checkbox = new JCheckBox();
	private boolean   initial  = false;
	
	public PlotSettingsCheckBox(String text) {
		checkbox.setText(text);
		add(checkbox);
		checkbox.addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsPopulator#reset(gov.nasa.arc.mct.fastplot.settings.PlotSettings)
	 */
	@Override
	public void reset(PlotConfiguration settings, boolean hard) {
		if (hard) checkbox.setSelected(initial = getFrom(settings));
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsSubPanel#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return initial != checkbox.isSelected();
	}

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsSubPanel#isValidated()
	 */
	@Override
	public boolean isValidated() {
		return true;
	}

	public boolean isSelected() {
		return checkbox.isSelected();
	}
	
	public abstract boolean getFrom(PlotConfiguration settings);
}
