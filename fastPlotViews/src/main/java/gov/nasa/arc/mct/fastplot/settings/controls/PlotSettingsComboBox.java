package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.settings.PlotSettingsSubPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;

public abstract class PlotSettingsComboBox<T> extends PlotSettingsSubPanel {
	private static final long serialVersionUID = -5666070801819602174L;

	private T selection;
	
	private Map<T, ItemWrapper<T>> items = new HashMap<T, ItemWrapper<T>>();
	private JComboBox comboBox;
	
	public PlotSettingsComboBox(T... choices) {
		List<ItemWrapper<T>> wrappedChoices = new ArrayList<ItemWrapper<T>>();
		for (T item : choices) {
			ItemWrapper<T> wrapper = new ItemWrapper<T>(item);
			wrappedChoices.add(wrapper);
			items.put(item, wrapper);
		}
		comboBox = new JComboBox(wrappedChoices.toArray());
		if (choices.length > 0) setSelection(choices[0]);
		add(comboBox);
	}

	/**
	 * Set the selection for this group. Note that this will be considered 
	 * the "clean" selection - user changes will flag this as dirty.
	 * @param object
	 */
	public void setSelection(T object) {
		this.selection = (T) object;
		ItemWrapper<T> w = items.get(object);
		comboBox.setSelectedItem(w);
	}
	
	@SuppressWarnings("unchecked")
	public T getSelection() {
		ItemWrapper<T> wrapper = ((ItemWrapper<T>) comboBox.getSelectedItem());
		return wrapper == null ? null : wrapper.item;
	}

	@Override
	public boolean isDirty() {
		return selection != null && !selection.equals(getSelection());
	}

	@Override
	public boolean isValidated() {
		return true;
	}

	public void setText(T object, String text) {
		ItemWrapper<T> item = items.get(object);
		if (item != null) {
			item.text = text;
		}
	}
	
	private static class ItemWrapper<T> {
		private T item;
		private String text;
		
		public ItemWrapper(T item) {
			this.item = item;
			this.text = item.toString();
		}
		
		public String toString() {
			return text;
		}
	}
}
