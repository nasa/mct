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
package gov.nasa.arc.mct.table.view;

import gov.nasa.arc.mct.abbreviation.Abbreviations;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 * Holds the current abbreviation settings for a row, column, or cell label.
 */
public class AbbreviationSettings {
	
	private String fullLabel;
	private Abbreviations availableAbbreviations;
	private LabelAbbreviations currentAbbreviations;
	private List<ComboBoxModel> models = new ArrayList<ComboBoxModel>(); 

	/**
	 * Create an object representing the state of the settings for abbreviations.
	 * @param fullLabel the unabbreviated label
	 * @param availableAbbreviations the externally defined abbreviations 
	 * @param currentAbbreviations the current selected abbreviations
	 */
	public AbbreviationSettings(String fullLabel, Abbreviations availableAbbreviations, LabelAbbreviations currentAbbreviations) {
		this.fullLabel = fullLabel;
		this.availableAbbreviations = availableAbbreviations;
		this.currentAbbreviations = currentAbbreviations;

		for (String phrase : availableAbbreviations.getPhrases()) {
			models.add(getModel(phrase));
		}
	}
	
	/**
	 * Builds a new combo box model for a phrase. Builds the data using available abbreviations, and
	 * then sets the selection using the state of the table settings controller.
	 */
	private ComboBoxModel getModel(String phrase) {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (String abbreviation : availableAbbreviations.getAbbreviations(phrase)) {
			model.addElement(abbreviation);
		}
		model.setSelectedItem(currentAbbreviations.getAbbreviation(phrase));
		return model;
	}
	
	/**
	 * Determines if any of the available abbreviations can be applied.
	 * @return true if at least one abbreviation can be applied
	 */
	public boolean canAbbreviate() {
		for (String phrase : availableAbbreviations.getPhrases()) {
			if (availableAbbreviations.getAbbreviations(phrase).size() > 1) {
				return true;
			}
		}
		
		// All abbreviations lists were size <= 1;
		return false;
	}
	
	/**
	 * Gets the unabbreviated (full) label.
	 * @return full label
	 */
	public String getFullLabel() {
		return fullLabel;
	}
	
	/**
	 * Get the jcomponent models associated with abbreviation settings. These models have state,
	 * according to the state of the combo box selection.
	 * @return list of models
	 */
	public ComboBoxModel[] getAbbreviationModels() {
		return models.toArray(new ComboBoxModel[models.size()]);
	}
	
	/**
	 * Gets the available abbreviations, converting from Abbreviations to sorted LabelAbbreviations.
	 * The returned data structure stores the state of the controller using comboBoxModels.
	 * @return available abbreviations
	 */
	public LabelAbbreviations getAbbreviations() {
		LabelAbbreviations abbrevs = new LabelAbbreviations();
		int i = 0;
		for (String phrase : availableAbbreviations.getPhrases()) {
			String phraseAbbreviation = (String) models.get(i).getSelectedItem();
			if (!phrase.equals(phraseAbbreviation)) {
				abbrevs.addAbbreviation(phrase, phraseAbbreviation);
			}
			++i;
		}
		return abbrevs;
	}
	
	/**
	 * Examines the comboBoxModel state and calculates the prospective value if abbreviations were applied.
	 * @return prospective value
	 */
	public String getAbbreviatedLabel() {
		StringBuilder result = new StringBuilder();
		for (ComboBoxModel model : models) {
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(model.getSelectedItem());
		}
		return result.toString();
	}
	
}
