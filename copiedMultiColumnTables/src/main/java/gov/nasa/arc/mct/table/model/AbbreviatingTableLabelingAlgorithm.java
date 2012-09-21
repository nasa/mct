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
package gov.nasa.arc.mct.table.model;

import gov.nasa.arc.mct.table.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implements a table labeling algorithm that tries to abbreviate row,
 * column, and cell labels based on not repeating words that appear in
 * other labels.
 */
public class AbbreviatingTableLabelingAlgorithm extends TableLabelingAlgorithm {
	
	private static final Set<String> EMPTY_WORD_SET = new HashSet<String>();
	
	/**
	 * The regular expression defining the delimiter pattern between words.
	 * Words are delimited by a sequence of one or more spaces or underscores.
	 */
	private static final String WORD_DELIMITERS = "[ _]+";
	
	/**
	 * The compiled regular expression defining the delimiter pattern between
	 * words.
	 */
	private static final Pattern WORD_DELIMITER_PATTERN = Pattern.compile(WORD_DELIMITERS);
	
	/**
	 * The separator to use when concatenating words together to form labels.
	 */
	private static final String WORD_SEPARATOR = " ";

	/** The labels from the surrounding context. Words from those labels
	 * will never appear in the resulting row, column, or cell labels.
	 */
	private List<String> globalContextLabels = new ArrayList<String>();
	
	private Set<String> globalContextWords = new HashSet<String>();
	
	/**
	 * Creates a new instance of the labeling algorithm.
	 */
	public AbbreviatingTableLabelingAlgorithm() {
		super(TableOrientation.ROW_MAJOR);
	}
	
	@Override
	public void computeLabels(LabeledTableModel model) {
		Set<String> commonContextWords;
		
		if (model.getRowCount()==1 && model.getColumnCount() > 1) {
			commonContextWords = computeLabelsRowMajor(model);
		} else if (model.getRowCount() > 1 && model.getColumnCount()==1) {
			commonContextWords = computeLabelsColumnMajor(model);
		} else if (getOrientation() == TableOrientation.ROW_MAJOR) {
			commonContextWords = computeLabelsRowMajor(model);
		} else {
			commonContextWords = computeLabelsColumnMajor(model);
		}
		
		computeCellLabels(model, commonContextWords);
	}
	
	/**
	 * Computes the row and column labels in row-major order. That is,
	 * the row labels are computed first, then the column labels. This means
	 * that any words used in the row labels will not be present in the
	 * column labels. Calculates and return the set of words from the surrounding
	 * context that are common with all cells.
	 * 
	 * @param model the table label model
	 * @return the set of words from the surrounding context that are common among all cells
	 */
	private Set<String> computeLabelsRowMajor(LabeledTableModel model) {
		Set<String>commonContextWords = new HashSet<String>(globalContextWords);
		Set<String> contextLabelWords = new HashSet<String>();
		
		List<List<String>> rowLabels = new ArrayList<List<String>>();
		for (int row=0; row < model.getRowCount(); ++row) {
			List<String> rowLabel = computeLabel(getRowIdentifiers(row, model), EMPTY_WORD_SET);
			rowLabels.add(rowLabel);
			contextLabelWords.addAll(rowLabel);
			commonContextWords.retainAll(rowLabel);
		}
		
		for (int row=0; row < model.getRowCount(); ++row) {
			// Only remove context from the labels if the table isn't a single cell.
			if (model.getRowCount() > 1 || model.getColumnCount() > 1) {
				rowLabels.get(row).removeAll(commonContextWords);
			}
			model.setRowName(row, StringUtils.join(rowLabels.get(row), WORD_SEPARATOR));
		}
		
		contextLabelWords.addAll(commonContextWords);

		for (int col=0; col < model.getColumnCount(); ++col) {
			List<String> colLabel = computeLabel(getColumnIdentifiers(col, model), contextLabelWords);
			model.setColumnName(col, StringUtils.join(colLabel, WORD_SEPARATOR));
		}
		
		return commonContextWords;
	}
	
	/**
	 * Computes the row and column labels in column-major order. That is,
	 * the column labels are computed first, then the row labels. This means
	 * that any words used in the column labels will not be present in the
	 * row labels. Calculates and return the set of words from the surrounding
	 * context that are common with all cells.
	 * 
	 * @param model the table label model
	 * @return the set of words from the surrounding context that are common among all cells
	 */
	private Set<String> computeLabelsColumnMajor(LabeledTableModel model) {
		Set<String>commonContextWords = new HashSet<String>(globalContextWords);
		Set<String> contextLabelWords = new HashSet<String>();
		
		List<List<String>> colLabels = new ArrayList<List<String>>();
		for (int col=0; col < model.getColumnCount(); ++col) {
			List<String> colLabel = computeLabel(getColumnIdentifiers(col, model), EMPTY_WORD_SET);
			colLabels.add(colLabel);
			contextLabelWords.addAll(colLabel);
			commonContextWords.retainAll(colLabel);
		}
		
		for (int col=0; col < model.getColumnCount(); ++col) {
			// Only remove context from the labels if the table isn't a single cell.
			if (model.getRowCount() > 1 || model.getColumnCount() > 1) {
				colLabels.get(col).removeAll(commonContextWords);
			}
			model.setColumnName(col, StringUtils.join(colLabels.get(col), WORD_SEPARATOR));
		}
		
		contextLabelWords.addAll(commonContextWords);

		for (int row=0; row < model.getRowCount(); ++row) {
			List<String> rowLabel = computeLabel(getRowIdentifiers(row, model), contextLabelWords);
			model.setRowName(row, StringUtils.join(rowLabel, WORD_SEPARATOR));
		}
		
		return commonContextWords;
	}

	/**
	 * Gets the cell identifiers for an entire row, as a list.
	 * 
	 * @param row the row for which to get the cell identifiers
	 * @param model the table labeling model
	 * @return a list of cell identifiers for the row
	 */
	List<String> getRowIdentifiers(int row, LabeledTableModel model) {
		List<String> identifiers = new ArrayList<String>();
		
		for (int col=0; col < model.getColumnCount(); ++col) {
			String identifier = model.getIdentifierAt(row, col);
			if (identifier!=null && !identifier.isEmpty()) {
				identifiers.add(identifier);
			}
		}
		return identifiers;
	}

	/**
	 * Gets the cell identifiers for an entire column, as a list.
	 * 
	 * @param col the column for which to get the cell identifiers
	 * @param model the table labeling model
	 * @return a list of cell identifiers for the column
	 */
	List<String> getColumnIdentifiers(int col, LabeledTableModel model) {
		List<String> identifiers = new ArrayList<String>();
		
		for (int row=0; row < model.getRowCount(); ++row) {
			String identifier = model.getIdentifierAt(row, col);
			if (identifier!=null && !identifier.isEmpty()) {
				identifiers.add(identifier);
			}
		}
		return identifiers;
	}

	/**
	 * Computes a label for a row or column with a set of cell identifiers.
	 * The resulting label should have all words that are common among the
	 * cells, excluding any words that are in the surrounding context labels.
	 * Labels from the surrounding context could be from an existing
	 * row or column label or from a surrounding panel or window, for example.
	 * 
	 * @param identifiers the cell identifiers for the row or column
	 * @param contextWords the words to remove because they exist in the surrounding context
	 * @return the sequence of words for the label of the row or column
	 */
	private List<String> computeLabel(List<String> identifiers, Set<String> contextWords) {
		List<String> labelWords = null;
		
		for (int i=0; i < identifiers.size(); ++i) {
			List<String> cellLabelWords = StringUtils.split(identifiers.get(i), WORD_DELIMITER_PATTERN);
			if (cellLabelWords.size() > 0) {
				if (labelWords == null) {
					labelWords = cellLabelWords;
				} else {
					labelWords.retainAll(cellLabelWords);
				}
			}
		}
		
		if (labelWords == null) {
			return new ArrayList<String>();
		} else {
			labelWords.removeAll(contextWords);
			return labelWords;
		}
	}

	/**
	 * Calculates the labels for each cell in the table.
	 * 
	 * @param model the table label model
	 * @param contextWords the words from surrounding context that should not appear in the cell labels
	 */
	void computeCellLabels(LabeledTableModel model, Set<String> contextWords) {
		for (int row=0; row < model.getRowCount(); ++row) {
			for (int col=0; col < model.getColumnCount(); ++col) {
				String identifier = model.getIdentifierAt(row, col);
				String rowLabel = model.getFullRowName(row);
				String colLabel = model.getFullColumnName(col);
				
				model.setCellName(row, col, computeCellLabel(identifier, rowLabel, colLabel, contextWords));
			}
		}
	}

	/**
	 * Calculates the label for a single cell. The cell label is
	 * the words in the identifier for the cell, minus any words
	 * that appear in the row and column labels associated with
	 * the cell.
	 * 
	 * @param identifier the cell identifier
	 * @param rowLabel the label for the row containing the cell
	 * @param colLabel the label for the column containing the cell
	 * @param contextWords any words from surrounding context labels that should be removed 
	 * @return
	 */
	String computeCellLabel(String identifier, String rowLabel, String colLabel, Set<String> contextWords) {
		if (identifier == null) {
			return "";
		}
		
		List<String> labelWords = StringUtils.split(identifier, WORD_DELIMITER_PATTERN);
		labelWords.removeAll(StringUtils.split(rowLabel, WORD_DELIMITER_PATTERN));
		labelWords.removeAll(StringUtils.split(colLabel, WORD_DELIMITER_PATTERN));
		labelWords.removeAll(contextWords);

		return StringUtils.join(labelWords, WORD_SEPARATOR);
	}

	/** The array return type of {@link #getContextLabels()}, used to
	 * convert a list into an array. 
	 */
	private static final String[] CONTEXT_LABEL_ARRAY_TYPE = new String[0];
	
	/**
	 * Gets the labels describing the surrounding context of the table.
	 * The words in these labels will be stripped during the label
	 * computation, so that they never appear as row, column, nor cell
	 * labels.
	 * 
	 * @return an array containing the context labels, which may be empty
	 */
	public String[] getContextLabels() {
		return globalContextLabels.toArray(CONTEXT_LABEL_ARRAY_TYPE);
	}

	/**
	 * Sets the labels describing the surrounding context of the table.
	 * The words in these labels will be stripped during the label
	 * computation, so that they never appear as row, column, nor cell
	 * labels.
	 * 
	 * @param labels zero or more labels from the surrounding context
	 */
	public void setContextLabels(String... labels) {
		globalContextLabels.clear();
		globalContextLabels.addAll(Arrays.asList(labels));
		
		globalContextWords.clear();
		for (String label : labels) {
			globalContextWords.addAll(Arrays.asList(label.split(WORD_DELIMITERS)));
		}
	}
	
}
