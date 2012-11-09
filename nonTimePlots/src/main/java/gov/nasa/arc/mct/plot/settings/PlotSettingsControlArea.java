package gov.nasa.arc.mct.plot.settings;

import gov.nasa.arc.mct.plot.view.PlotView;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class PlotSettingsControlArea extends PlotSettingsPanel {
	private static final long serialVersionUID = -7171700637608907253L;
    private static final Border INNER_BORDER = BorderFactory.createEmptyBorder(0, 5, 5, 5);

	public PlotSettingsControlArea(PlotView managedView) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		//add(new SectionPanel("Plot Setup", new PlotSetupPanel()));
		//add(new SectionPanel("Plot Setup", new PlotSetupControlPanel(managedView)));
		add(new SectionPanel("Plot Behavior When Space Runs Out", new PlotBehaviorPanel()));		
		add(new SectionPanel("Line Setup", new LineSetupPanel()));
	}
	
	private class SectionPanel extends JPanel {
		private static final long serialVersionUID = -3867465087891823132L;

		public SectionPanel(String titleText, PlotSettingsSubPanel subPanel) {
			addSubPanel(subPanel);			
			
			setLayout  (new BorderLayout());
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(titleText), 
					INNER_BORDER));
			add(subPanel, BorderLayout.WEST);//, gbc);
		}

	}

}
