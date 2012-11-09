package gov.nasa.arc.mct.fastplot.settings;

import gov.nasa.arc.mct.fastplot.view.PlotControlsLayout;
import gov.nasa.arc.mct.fastplot.view.PlotControlsLayout.ResizersScrollPane;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class PlotSettingsControlContainer extends JPanel {
	private static final long serialVersionUID = 8930533587430928800L;

	private PlotViewManifestation managedView;
	
	public PlotSettingsControlContainer(PlotViewManifestation managedView) {
		this.managedView = managedView;
		
		final PlotSettingsSubPanel panel = new PlotSettingsControlArea(managedView);
		panel.reset(managedView.getPlot(), true);
		
		setLayout(new GridLayout(1,1));
		
		PlotControlsLayout controlsLayout = new PlotControlsLayout();
		PlotControlsLayout.ResizersScrollPane scroller =
			new ResizersScrollPane(panel);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel paddableOverallPanel = new JPanel(controlsLayout);
		paddableOverallPanel.add(scroller, PlotControlsLayout.MIDDLE);
		paddableOverallPanel.add(createApplyButtonPanel(panel), PlotControlsLayout.LOWER);
		add(paddableOverallPanel);
	}
	
	// Apply and Reset buttons at bottom of the Plot Settings Control Panel
	private JPanel createApplyButtonPanel(final PlotSettingsSubPanel panel) {
		final JButton okButton = new JButton("Apply");//BUNDLE.getString("Apply.label"));
		final JButton resetButton = new JButton("Reset");//BUNDLE.getString("Reset.label"));
		final Runnable callback = new Runnable() {
			@Override
			public void run() {
				PlotSettings settings = new PlotSettings();
				settings.loadFrom(managedView);
				panel.populate(settings);
				panel.reset(settings, false);
				
				okButton.setEnabled(panel.isDirty() && panel.isValidated()); 
				resetButton.setEnabled(panel.isDirty());	
			}						
		};
		
		okButton.setEnabled(false);
		resetButton.setEnabled(false);
		
		panel.addCallback(callback);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlotSettings settings = new PlotSettings();
				settings.loadFrom(managedView);
				panel.populate(settings);
				settings.persist(managedView);
				managedView.setupPlot(settings);
				panel.reset(settings, true);
				callback.run();
			}
		});
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlotSettings settings = new PlotSettings();
				settings.loadFrom(managedView);
				panel.reset(settings, true);
				callback.run(); // Disable apply reset
			}
		});
		JPanel okButtonPadded = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 7));
		okButtonPadded.add(okButton);
		okButtonPadded.add(resetButton);

		JPanel okPanel = new JPanel();
		okPanel.setLayout(new BorderLayout());
		okPanel.add(okButtonPadded, BorderLayout.EAST);
		// Instrument
		okPanel.setName("okPanel");
		okButton.setName("okButton");
		okButtonPadded.setName("okButtonPadded");

		// Set the panel color to a nice shade of gray
		okButtonPadded.setOpaque(false);
		okPanel.setBackground(Color.DARK_GRAY); //TODO: Externalize

		return okPanel;
	}

}
