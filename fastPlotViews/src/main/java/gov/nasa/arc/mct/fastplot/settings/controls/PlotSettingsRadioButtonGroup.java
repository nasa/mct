package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotSettingsSubPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

public abstract class PlotSettingsRadioButtonGroup<T> extends PlotSettingsSubPanel {
	private static final long serialVersionUID = -3064217413423344296L;

	private Map<T, JRadioButton> buttons = new HashMap<T, JRadioButton>();
	
	private ButtonGroup buttonGroup = new ButtonGroup();
	
	private JRadioButton selection;
	
	public PlotSettingsRadioButtonGroup(T... options) {
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
				
		for (T option : options) {				
			JRadioButton button = new JRadioButton(option.toString());
			buttonGroup.add(button);			
			button.addActionListener(this); // Hook into apply/reset callbacks
			add(button, gbc);		
			gbc.gridy++;
			
			buttons.put(option, button);
		}
		
		if (options.length > 0) {
			setSelection(options[0]);
		}
			
	}
	
	public JRadioButton getButton(T object) {
		return buttons.get(object);
	}
	
	/**
	 * Set the selection for this group. Note that this will be considered 
	 * the "clean" selection - user changes will flag this as dirty.
	 * @param object
	 */
	public void setSelection(T object) {
		JRadioButton selection = buttons.get(object);
		if (selection != null) {
			buttonGroup.setSelected(selection.getModel(), true);
			this.selection = selection;
		}
	}
	
	public T getSelection() {
		for (Entry<T, JRadioButton> button : buttons.entrySet()) {
			if (button.getValue().isSelected()) {
				return button.getKey();
			}
		}
		return null;
	}
	
	public void setText(T object, String text) {
		JRadioButton button = buttons.get(object);
		if (button != null) {
			button.setText(text);
		}
	}

	@Override
	public boolean isDirty() {
		if (selection == null) return false;
		return !buttonGroup.isSelected(selection.getModel());
	}

	@Override
	public boolean isValidated() {
		return true;
	}
}
