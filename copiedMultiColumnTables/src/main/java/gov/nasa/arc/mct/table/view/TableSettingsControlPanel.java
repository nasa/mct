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

import static gov.nasa.arc.mct.table.gui.ConstraintBuilder.hbox;
import gov.nasa.arc.mct.table.gui.ConstraintBuilder;
import gov.nasa.arc.mct.table.gui.TaggedComponentManager;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.view.BorderState.BorderEdge;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
//import java.util.HashMap;
import java.util.HashSet;
//import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
//import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Implements a dialog box for manipulating the settings of a
 * table.
 */
public class TableSettingsControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** The width of am empty border around the control panel. */
	private static final int PANEL_BORDER = 5;

	//	private static final int TITLE_BORDER_SPACE = 10;
	private static final int LABEL_VALUE_SPACE = 5;
	private static final int COLUMN_SPACE = 20;
	private static final int THIN_SPACE = 0;

	// Component tags for hiding/showing controls.
	private static final String SELECTION_NOT_EMPTY = "selection-not-empty";
	private static final String HEADERS_HIDABLE = "headers-hidable";
	private static final String SINGLE_CELL_SELECTION = "single-cell-selection";
	private static final String ENUMERATION_CONTROLS = "enumeration";
	private static final String DECIMAL_CONTROLS = "decimalControls";
	private static final String DATE_CONTROLS = "dateFormattingControls";
	private static final String DECIMAL_ALIGNMENT_BUTTON = "cellAlignButton";
	private static final String TABLE_ORIENTATION = "table-orientation";
	private static final String TRANSPOSE = "transpose-table";
	private static final String ROW_HEADER_LABEL = "row-header-label";
	private static final String COLUMN_HEADER_LABEL = "column-header-label";
	private static final String CELL_LABEL = "cell-label";
	private static final String COLUMN_IS_NOT_SELECTED = "column-not-selected";
	private static final String ROW_IS_NOT_SELECTED = "row-not-selected";

	private static final String NOT_FULLY_IMPLEMENTED = "not-fully-implemented";

	/** The resource bundle we should use for getting strings. */
	private static final ResourceBundle bundle = ResourceBundle.getBundle("TableSettingsControlPanel"); //NOI18N

	/** A list of all control panels of this type. */
	private static Collection<TableSettingsControlPanel> allPanels = new HashSet<TableSettingsControlPanel>();

	/** The controller object that mediates interaction between the control panel
	 * and the actual table.
	 */
	private TableSettingsController controller;

	// Row and column formatting controls.
	private JSpinner rowHeightSpinner;
	private JSpinner columnWidthSpinner;

	private JRadioButton tableRowOrientation;
	private JRadioButton tableColOrientation;

	private JButton transposeTableButton;
	private JRadioButton rowHeaderAlignLeft;
	private JRadioButton rowHeaderAlignCenter;
	private JRadioButton rowHeaderAlignRight;
	private ButtonGroup rowHeaderAlignGroup;
	private JRadioButton columnHeaderAlignLeft;
	private JRadioButton columnHeaderAlignCenter;
	private JRadioButton columnHeaderAlignRight;
	private ButtonGroup columnHeaderAlignGroup;
	private JCheckBox showRowHeaders;
	private JCheckBox showColumnHeaders;
	private JComboBox rowHeaderFontName;
	private JSpinner rowHeaderFontSize;
	private JComboBox rowForegroundColorComboBox = null;
	private JComboBox rowBackgroundColorComboBox = null;
	private JComboBox rowHeaderBorderColorComboBox = null;
	private JComboBox columnForegroundColorComboBox = null;
	private JComboBox columnBackgroundColorComboBox = null;
	private JComboBox columnHeaderBorderColorComboBox = null;
	private JToggleButton rowHeaderFontStyleBold;
	private JToggleButton rowHeaderFontStyleItalic;
	private JToggleButton rowHeaderFontUnderline;
	private JComboBox columnHeaderFontName;
	private JSpinner columnHeaderFontSize;
	private JToggleButton columnHeaderFontStyleBold;
	private JToggleButton columnHeaderFontStyleItalic;
	private JToggleButton columnHeaderFontStyleUnderline;

	//	private JCheckBox showGridCheckbox;

	// Row header label controls.
	private JLabel rowHeaderFullLabel;
	private JPanel rowHeaderAbbreviations;
	private JLabel rowHeaderAbbreviatedLabel;

	// Column header label controls.
	private JLabel columnHeaderFullLabel;
	private JPanel columnHeaderAbbreviations;
	private JLabel columnHeaderAbbreviatedLabel;

	// Cell label controls.
	private JLabel cellFullLabel;
	private JPanel cellAbbreviations;
	private JLabel cellAbbreviatedLabel;

	// Cell formatting controls.
	private JComboBox propertyToShow;
	private JComboBox enumeration;
	private JComboBox numberOfDecimals;
	private JComboBox dateFormatChooser;
	private JComboBox cellFontName;
	private JSpinner cellFontSize;
	private JComboBox cellFontColorComboBox;
	private JComboBox cellBackgroundColorComboBox;
	private JToggleButton cellFontStyleBold;
	private JToggleButton cellFontStyleItalic;
	private JToggleButton cellFontUnderline;
	private JRadioButton cellAlignLeft;
	private JRadioButton cellAlignCenter;
	private JRadioButton cellAlignRight;
	private JRadioButton cellAlignDecimal;
	private ButtonGroup cellAlignGroup;
	
	/** border controller. */
	
	JToggleButton rowHeaderBorderOnLeft = null;
	JToggleButton rowHeaderBorderOnTop = null;
	JToggleButton rowHeaderBorderOnRight = null;
	JToggleButton rowHeaderBorderOnBottom = null;
	JToggleButton columnHeaderBorderOnLeft = null;
	JToggleButton columnHeaderBorderOnTop = null;
	JToggleButton columnHeaderBorderOnRight = null;
	JToggleButton columnHeaderBorderOnBottom = null;
	JToggleButton cellBorderOnLeft = null;
	JToggleButton cellBorderOnTop = null;
	JToggleButton cellBorderOnRight = null;
	JToggleButton cellBorderOnBottom = null;

	// Component visibility manager, for showing and hiding controls.
	private TaggedComponentManager mgr = new TaggedComponentManager();

	private TitledPanel rowColumnFormattingPanel;
	private JPanel rowColumnFontFormattingPanel;

	private TitledPanel columnHeaderLabelPanel;

	private TitledPanel cellLabelPanel;

	private TitledPanel rowHeaderLabelPanel;

	private TitledPanel cellFormattingPanel;
	
	private final ChangeListener rowHeightListener;
	private final ChangeListener columnWidthListener;
	private final ChangeListener rowHeaderFontSizeListener;
	private final ChangeListener columnHeaderFontSizeListener;
	private final ChangeListener cellFontSizeListener;

	/**
	 * Creates a new control panel, interacting with a designated controller
	 * to get the default settings. All controls will call the controller to
	 * update settings in the table as the user makes changes.
	 * 
	 * @param panelController the controller that handles the interactions between the control panel
	 *   and the underlying table
	 */
	public TableSettingsControlPanel(TableSettingsController panelController) {
		controller = panelController;

		SpinnerModel rowDimensionModel = new SpinnerNumberModel(12, 5, 100, 1);
		rowHeightSpinner = new JSpinner(rowDimensionModel);
		setAccessibleName(rowHeightSpinner, "ROW_HEIGHT");
		rowHeightSpinner.addChangeListener(rowHeightListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setRowHeight(Integer.class.cast(rowHeightSpinner.getValue()).intValue());
			}
		});

		SpinnerModel columnDimensionModel = new SpinnerNumberModel(12, 5, 3000, 1);
		columnWidthSpinner = new JSpinner(columnDimensionModel);
		setAccessibleName(columnWidthSpinner, "COLUMN_WIDTH");
		columnWidthSpinner.addChangeListener(columnWidthListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setColumnWidth(Integer.class.cast(columnWidthSpinner.getValue()).intValue());
			}
		});

		tableRowOrientation = new JRadioButton(bundle.getString("ROW_MAJOR"));
		setAccessibleName(tableRowOrientation, "ROW_MAJOR");
		tableRowOrientation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setTableOrientation(TableOrientation.ROW_MAJOR);
				settingsChanged();
			}
		});
		tableColOrientation = new JRadioButton(bundle.getString("COLUMN_MAJOR"));
		setAccessibleName(tableColOrientation, "COLUMN_MAJOR");
		tableColOrientation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setTableOrientation(TableOrientation.COLUMN_MAJOR);
				settingsChanged();
			}
		});
		tableRowOrientation.setSelected(true);
		groupButtons(tableRowOrientation, tableColOrientation);

		transposeTableButton = new JButton(bundle.getString("TRANSPOSE_TABLE"));
		setAccessibleName(transposeTableButton, "TRANSPOSE_TABLE");
		transposeTableButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.transposeTable();
			}
		});
		
		rowHeaderFontName = setUpFontControl();
		setAccessibleName(rowHeaderFontName, "ROW_FONT");
		rowHeaderFontName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setRowHeaderFontName((rowHeaderFontName.getModel()));
			}
		});
		
		columnHeaderFontName = setUpFontControl();
		setAccessibleName(columnHeaderFontName, "COL_FONT");
		columnHeaderFontName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setColumnHeaderFontName((columnHeaderFontName.getModel()));
			}
		});
		
		SpinnerModel rowFontSizeModel = new SpinnerNumberModel(12, 8, 36, 1);
		rowHeaderFontSize = new JSpinner(rowFontSizeModel);
		setAccessibleName(rowHeaderFontSize, "ROW_FONT_SIZE");
		rowHeaderFontSize.addChangeListener(rowHeaderFontSizeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setRowHeaderFontSize(Integer.class.cast(rowHeaderFontSize.getValue()).intValue());
				
			}
		});
		
		
		SpinnerModel columnFontSizeModel = new SpinnerNumberModel(12, 8, 36, 1);
		columnHeaderFontSize = new JSpinner(columnFontSizeModel);
		setAccessibleName(columnHeaderFontSize, "COL_FONT_SIZE");
		columnHeaderFontSize.addChangeListener(columnHeaderFontSizeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setColumnHeaderFontSize(Integer.class.cast(columnHeaderFontSize.getValue()).intValue());
				
			}
		});
		
		SpinnerModel cellFontSizeModel = new SpinnerNumberModel(12, 8, 36, 1);
		cellFontSize = new JSpinner(cellFontSizeModel);
		setAccessibleName(cellFontSize, "CELL_FONT_SIZE");
		cellFontSize.addChangeListener(cellFontSizeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				controller.setCellFontSize(Integer.class.cast(cellFontSize.getValue()).intValue());
				
			}
		});

		rowHeaderAlignLeft = getIconRadioButton("textAlignLeft_off.png", "textAlignLeft_on.png", bundle.getString("ROW_HEADER_ALIGN_LEFT"));
		setAccessibleName(rowHeaderAlignLeft, "ROW_HEADER_ALIGN_LEFT");
        Insets buttonInsets = rowHeaderAlignLeft.getInsets();
        buttonInsets.set(0, 0, 0, 
                        2);
        rowHeaderAlignLeft.setMargin(buttonInsets);
		rowHeaderAlignLeft.setActionCommand(ContentAlignment.LEFT.toString());
		rowHeaderAlignCenter = getIconRadioButton("textAlignCenter_off.png", "textAlignCenter_on.png", bundle.getString("ROW_HEADER_ALIGN_CENTER"));
		rowHeaderAlignCenter.setMargin(buttonInsets);
		setAccessibleName(rowHeaderAlignCenter, "ROW_HEADER_ALIGN_CENTER");
		rowHeaderAlignCenter.setActionCommand(ContentAlignment.CENTER.toString());
		rowHeaderAlignRight = getIconRadioButton("textAlignRight_off.png", "textAlignRight_on.png", bundle.getString("ROW_HEADER_ALIGN_RIGHT"));
		rowHeaderAlignRight.setMargin(buttonInsets);
		setAccessibleName(rowHeaderAlignRight, "ROW_HEADER_ALIGN_RIGHT");
		rowHeaderAlignRight.setActionCommand(ContentAlignment.RIGHT.toString());
		rowHeaderAlignLeft.setSelected(true);
		rowHeaderAlignGroup = groupButtons(rowHeaderAlignLeft, rowHeaderAlignCenter, rowHeaderAlignRight);

		ActionListener rowHeaderAlignmentListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setRowHeaderAlignment(ContentAlignment.valueOf(e.getActionCommand()));
			}

		};
		
		rowHeaderAlignLeft.addActionListener(rowHeaderAlignmentListener);
		rowHeaderAlignCenter.addActionListener(rowHeaderAlignmentListener);
		rowHeaderAlignRight.addActionListener(rowHeaderAlignmentListener);
		
		setUpRowFontStyleButtons();
		setUpColumnFontStyleButtons();
		setUpCellFontStyleButtons();
		setUpHeaderBorderButtons();

		columnHeaderAlignLeft = getIconRadioButton("textAlignLeft_off.png", "textAlignLeft_on.png", bundle.getString("COLUMN_HEADER_ALIGN_LEFT"));
        columnHeaderAlignLeft.setMargin(buttonInsets);
		setAccessibleName(columnHeaderAlignLeft, "COLUMN_HEADER_ALIGN_LEFT");
		columnHeaderAlignLeft.setActionCommand(ContentAlignment.LEFT.toString());
		columnHeaderAlignCenter = getIconRadioButton("textAlignCenter_off.png", "textAlignCenter_on.png", bundle.getString("COLUMN_HEADER_ALIGN_CENTER"));
		columnHeaderAlignCenter.setMargin(buttonInsets);
		setAccessibleName(columnHeaderAlignCenter, "COLUMN_HEADER_ALIGN_CENTER");
		columnHeaderAlignCenter.setActionCommand(ContentAlignment.CENTER.toString());
		columnHeaderAlignRight = getIconRadioButton("textAlignRight_off.png", "textAlignRight_on.png", bundle.getString("COLUMN_HEADER_ALIGN_RIGHT"));
		columnHeaderAlignRight.setMargin(buttonInsets);
		setAccessibleName(columnHeaderAlignRight, "COLUMN_HEADER_ALIGN_RIGHT");
		columnHeaderAlignRight.setActionCommand(ContentAlignment.RIGHT.toString());
		columnHeaderAlignLeft.setSelected(true);
		columnHeaderAlignGroup = groupButtons(columnHeaderAlignLeft, columnHeaderAlignCenter, columnHeaderAlignRight);

		ActionListener columnHeaderAlignmentListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setColumnHeaderAlignment(ContentAlignment.valueOf(e.getActionCommand()));
			}

		};

		columnHeaderAlignLeft.addActionListener(columnHeaderAlignmentListener);
		columnHeaderAlignCenter.addActionListener(columnHeaderAlignmentListener);
		columnHeaderAlignRight.addActionListener(columnHeaderAlignmentListener);

		showRowHeaders = new JCheckBox(bundle.getString("SHOW_ROW_HEADERS"));
		setAccessibleName(showRowHeaders, "SHOW_ROW_HEADERS");
		showRowHeaders.setSelected(true);
		showColumnHeaders = new JCheckBox(bundle.getString("SHOW_COLUMN_HEADERS"));
		setAccessibleName(showColumnHeaders, "SHOW_COLUMN_HEADERS");
		showColumnHeaders.setSelected(true);

		mgr.tagComponents(NOT_FULLY_IMPLEMENTED, showRowHeaders, showColumnHeaders);

		rowHeaderFullLabel = new JLabel();
		setAccessibleName(rowHeaderFullLabel, "ROW_HEADER_FULL_LABEL");
		rowHeaderAbbreviations = new JPanel();
		setAccessibleName(rowHeaderAbbreviations, "ROW_HEADER_ABBREVIATIONS");
		rowHeaderAbbreviatedLabel = new JLabel();
		setAccessibleName(rowHeaderAbbreviatedLabel, "ROW_HEADER_ABBREVIATED_LABEL");

		columnHeaderFullLabel = new JLabel();
		setAccessibleName(columnHeaderFullLabel, "COLUMN_HEADER_FULL_LABEL");
		columnHeaderAbbreviations = new JPanel();
		setAccessibleName(columnHeaderAbbreviations, "COLUMN_HEADER_ABBREVIATIONS");
		columnHeaderAbbreviatedLabel = new JLabel();
		setAccessibleName(columnHeaderAbbreviatedLabel, "COLUMN_HEADER_ABBREVIATED_LABEL");

		cellFullLabel = new JLabel();
		setAccessibleName(cellFullLabel, "CELL_FULL_LABEL");
		cellAbbreviations = new JPanel();
		setAccessibleName(cellAbbreviations, "CELL_ABBREVIATIONS");
		cellAbbreviatedLabel = new JLabel();
		setAccessibleName(cellAbbreviatedLabel, "CELL_ABBREVIATED_LABEL");

		propertyToShow = new JComboBox(new String[] { "Value" });
		setAccessibleName(propertyToShow, "PROPERTY_TO_SHOW");
		enumeration = new JComboBox();
		setAccessibleName(enumeration, "ENUMERATION");
		enumeration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setEnumeration(enumeration.getModel());
				
				if (!controller.enumerationIsNone(enumeration.getModel())) {
					if (!controller.dateIsNone(dateFormatChooser.getModel())) {
						dateFormatChooser.setSelectedItem(DateFormatItem.None); 
						controller.setDateFormat(dateFormatChooser.getModel()); 		
					}
					mgr.disable(DATE_CONTROLS, true);
					mgr.disable(DECIMAL_CONTROLS, true);
					mgr.disable(DECIMAL_ALIGNMENT_BUTTON, true);            
				} else {
					mgr.enable(DATE_CONTROLS, true);
					mgr.enable(DECIMAL_CONTROLS, true);
					mgr.enable(DECIMAL_ALIGNMENT_BUTTON, true);
				}
			}
		});

		numberOfDecimals = new JComboBox(new Object[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
		numberOfDecimals.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setDecimalPlaces(numberOfDecimals.getModel());
			}

		});
		setAccessibleName(numberOfDecimals, "NUMBER_OF_DECIMALS");
		
		cellFontName = setUpFontControl();
		setAccessibleName(cellFontName, "CELL_FONT");
		cellFontName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setCellFont(cellFontName.getModel());
			}
		});

		dateFormatChooser = new JComboBox(new Object[] {DateFormatItem.None, DateFormatItem.HHMM, DateFormatItem.HHMMSS,  DateFormatItem.DDD_HHMM, DateFormatItem.DDD_HHMMSS,  DateFormatItem.YYYYDDD_HHMMSS});
		DateComboBoxRenderer renderer= new DateComboBoxRenderer();
		dateFormatChooser.setRenderer(renderer);
	    
		dateFormatChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setDateFormat(dateFormatChooser.getModel());

				if (! controller.dateIsNone(dateFormatChooser.getModel())) {
					mgr.disable(DECIMAL_CONTROLS, true);
					mgr.disable(DECIMAL_ALIGNMENT_BUTTON, true);
				} else {
					mgr.enable(DECIMAL_CONTROLS, true);
					mgr.enable(DECIMAL_ALIGNMENT_BUTTON, true);
				}

			}
		});
		setAccessibleName(dateFormatChooser, "FORMAT_AS_DATE"); 
		
		/* Border controls are ungrouped toggle buttons with independent state, however, create radio buttons to get smaller button size. */
        cellBorderOnLeft = getIconRadioButton("LeftBorder_off.png", "LeftBorder_on.png", bundle.getString("CELL_BORDER_LEFT_DESCRIPTION"));
        cellBorderOnLeft.setMargin(buttonInsets);
        setAccessibleName(cellBorderOnLeft, "CELL_BORDER_LEFT");
		cellBorderOnTop = getIconRadioButton("TopBorder_off.png", "TopBorder_on.png", bundle.getString("CELL_BORDER_TOP_DESCRIPTION"));
		setAccessibleName(cellBorderOnTop, "CELL_BORDER_TOP");
		cellBorderOnRight = getIconRadioButton("RightBorder_off.png", "RightBorder_on.png", bundle.getString("CELL_BORDER_RIGHT_DESCRIPTION"));
		setAccessibleName(cellBorderOnRight, "CELL_BORDER_RIGHT");
		cellBorderOnBottom = getIconRadioButton("BottomBorder_off.png", "BottomBorder_on.png", bundle.getString("CELL_BORDER_BOTTOM_DESCRIPTION"));
		setAccessibleName(cellBorderOnBottom, "CELL_BORDER_BOTTOM");
		ActionListener cellBorderListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BorderState compositeControllerState = new BorderState(cellBorderOnLeft.getModel(),cellBorderOnTop.getModel(), cellBorderOnRight.getModel(),  cellBorderOnBottom.getModel());
				controller.mergeBorderState(compositeControllerState); 
			}
		};
		cellBorderOnLeft.addActionListener(cellBorderListener);
		cellBorderOnTop.addActionListener(cellBorderListener);
		cellBorderOnRight.addActionListener(cellBorderListener);
		cellBorderOnBottom.addActionListener(cellBorderListener);

		cellAlignLeft = getIconRadioButton("textAlignLeft_off.png", "textAlignLeft_on.png", bundle.getString("CELL_ALIGN_LEFT"));
		cellAlignLeft.setMargin(buttonInsets);
		setAccessibleName(cellAlignLeft, "CELL_ALIGN_LEFT");
		cellAlignLeft.setActionCommand(ContentAlignment.LEFT.toString());
		cellAlignCenter = getIconRadioButton("textAlignCenter_off.png", "textAlignCenter_on.png", bundle.getString("CELL_ALIGN_CENTER"));
		setAccessibleName(cellAlignCenter, "CELL_ALIGN_CENTER");
		cellAlignCenter.setActionCommand(ContentAlignment.CENTER.toString());
		cellAlignRight = getIconRadioButton("textAlignRight_off.png", "textAlignRight_on.png", bundle.getString("CELL_ALIGN_RIGHT"));
		setAccessibleName(cellAlignRight, "CELL_ALIGN_RIGHT");
		cellAlignRight.setActionCommand(ContentAlignment.RIGHT.toString());
		cellAlignDecimal = getIconRadioButton("TextAlignDecimal_off.png", "TextAlignDecimal_on.png", bundle.getString("CELL_ALIGN_DECIMAL"));
		setAccessibleName(cellAlignDecimal, "CELL_ALIGN_DECIMAL");
		cellAlignDecimal.setActionCommand(ContentAlignment.DECIMAL.toString());
		cellAlignLeft.setSelected(true);
		cellAlignGroup = groupButtons(cellAlignLeft, cellAlignCenter, cellAlignRight, cellAlignDecimal);

		ActionListener cellAlignmentListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.setCellAlignment(ContentAlignment.valueOf(e.getActionCommand()));
			}
			
		};

		cellAlignLeft.addActionListener(cellAlignmentListener);
		cellAlignCenter.addActionListener(cellAlignmentListener);
		cellAlignRight.addActionListener(cellAlignmentListener);
		cellAlignDecimal.addActionListener(cellAlignmentListener);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(PANEL_BORDER, PANEL_BORDER, PANEL_BORDER, PANEL_BORDER));

		JScrollPane scrollPane = new JScrollPane(contentPanel);

		setLayout(new GridLayout(1,1));
		add(scrollPane);

		ConstraintBuilder builder;

		rowColumnFormattingPanel = new TitledPanel("ROW_COLUMN_FORMATTING_PANEL");
		setAccessibleName(rowColumnFormattingPanel, "ROW_COLUMN_FORMATTING_PANEL");
		contentPanel.add(rowColumnFormattingPanel);
		builder = rowColumnFormattingPanel.getBuilder();

		JLabel rowHeightLabel = new JLabel(bundle.getString("ROW_HEIGHT_LABEL"));
		builder.add(rowHeightLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		JLabel columnWidthLabel = new JLabel(bundle.getString("COLUMN_WIDTH_LABEL"));
		builder.add(rowHeightSpinner, hbox(COLUMN_SPACE), columnWidthLabel,
				hbox(LABEL_VALUE_SPACE), columnWidthSpinner);

		JLabel tableOrientationLabel = new JLabel(bundle.getString("TABLE_ORIENTATION_LABEL"));
		builder.nextRow().add(tableOrientationLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		builder.add(tableRowOrientation, hbox(COLUMN_SPACE), tableColOrientation);

		mgr.tagComponents(TABLE_ORIENTATION, tableOrientationLabel, tableRowOrientation, tableColOrientation);

//		builder.nextRow();
//		builder.nextColumn().insets(0,LABEL_VALUE_SPACE,0,0).add(transposeTableButton);

		mgr.tagComponents(TRANSPOSE, transposeTableButton);
		mgr.tagComponents(NOT_FULLY_IMPLEMENTED, transposeTableButton);
		
		setUpColorComboBoxes();
		rowColumnFontFormattingPanel = createHeaderFormattingPanel();
		contentPanel.add(rowColumnFontFormattingPanel);

		mgr.tagComponents(SELECTION_NOT_EMPTY, rowHeightLabel, rowHeightSpinner, columnWidthLabel, columnWidthSpinner);
		mgr.tagComponents(SELECTION_NOT_EMPTY, rowHeaderAlignLeft, rowHeaderAlignCenter, rowHeaderAlignRight);
		mgr.tagComponents(SELECTION_NOT_EMPTY, columnHeaderAlignLeft, columnHeaderAlignCenter, columnHeaderAlignRight);
		mgr.tagComponents(SELECTION_NOT_EMPTY, rowColumnFontFormattingPanel);
		mgr.tagComponents(HEADERS_HIDABLE, showRowHeaders, showColumnHeaders);

		//		layout.addRow().done();
		//		layout.inColumn(1).add(showGridCheckbox).done();

		rowHeaderLabelPanel = new TitledPanel("ROW_HEADER_LABEL_PANEL");
		setAccessibleName(rowHeaderLabelPanel, "ROW_HEADER_LABEL_PANEL");
		contentPanel.add(rowHeaderLabelPanel);
		mgr.tagComponents(ROW_HEADER_LABEL, rowHeaderLabelPanel);

		builder = rowHeaderLabelPanel.getBuilder();

		builder.add(new JLabel(bundle.getString("ROW_HEADER_FULL_LABEL_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(rowHeaderFullLabel);
		builder.nextRow().add(new JLabel(bundle.getString("ROW_HEADER_ABBREVIATIONS_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(rowHeaderAbbreviations);
		builder.nextRow().add(new JLabel(bundle.getString("ROW_HEADER_ABBREVIATED_LABEL_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(rowHeaderAbbreviatedLabel);

		columnHeaderLabelPanel = new TitledPanel("COLUMN_HEADER_LABEL_PANEL");
		setAccessibleName(columnHeaderLabelPanel, "COLUMN_HEADER_LABEL_PANEL");
		contentPanel.add(columnHeaderLabelPanel);
		mgr.tagComponents(COLUMN_HEADER_LABEL, columnHeaderLabelPanel);
		builder = columnHeaderLabelPanel.getBuilder();

		builder.add(new JLabel(bundle.getString("COLUMN_HEADER_FULL_LABEL_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(columnHeaderFullLabel);
		builder.nextRow().add(new JLabel(bundle.getString("COLUMN_HEADER_ABBREVIATIONS_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(columnHeaderAbbreviations);
		builder.nextRow().add(new JLabel(bundle.getString("COLUMN_HEADER_ABBREVIATED_LABEL_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(columnHeaderAbbreviatedLabel);

		cellLabelPanel = new TitledPanel("CELL_LABEL_PANEL");
		setAccessibleName(cellLabelPanel, "CELL_LABEL_PANEL");
		contentPanel.add(cellLabelPanel);
		mgr.tagComponents(CELL_LABEL, cellLabelPanel);
		builder = cellLabelPanel.getBuilder();

		builder.add(new JLabel(bundle.getString("CELL_FULL_LABEL_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(cellFullLabel);
		builder.nextRow().add(new JLabel(bundle.getString("CELL_ABBREVIATIONS_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(cellAbbreviations);
		builder.nextRow().add(new JLabel(bundle.getString("CELL_ABBREVIATED_LABEL_LABEL")));
		builder.insets(0, LABEL_VALUE_SPACE, 0, 0);
		builder.add(cellAbbreviatedLabel);

		cellFormattingPanel = new TitledPanel("CELL_FORMATTING_PANEL");
		setAccessibleName(cellFormattingPanel, "CELL_FORMATTING_PANEL");
		contentPanel.add(cellFormattingPanel);
		mgr.tagComponents(SELECTION_NOT_EMPTY, cellFormattingPanel);
		builder = cellFormattingPanel.getBuilder();

		JLabel propertyToShowLabel = new JLabel(bundle.getString("PROPERTY_TO_SHOW_LABEL"));
//		builder.add(propertyToShowLabel);
//		builder.insets(0,LABEL_VALUE_SPACE,0,0);
//		builder.add(propertyToShow);

	
		JLabel enumerationLabel = new JLabel(bundle.getString("ENUMERATION_LABEL"));
		builder.nextRow().add(enumerationLabel);
		builder.insets(0,LABEL_VALUE_SPACE,LABEL_VALUE_SPACE,0);
		builder.add(enumeration);

		JLabel dateLabel = new JLabel(bundle.getString("FORMAT_AS_DATE_LABEL"));
		builder.nextRow().add(dateLabel);
		builder.insets(0,LABEL_VALUE_SPACE,LABEL_VALUE_SPACE,0);
		builder.add(dateFormatChooser);

		JLabel numberOfDecimalsLabel = new JLabel(bundle.getString("NUMBER_OF_DECIMALS_LABEL"));
		builder.nextRow().add(numberOfDecimalsLabel);
		builder.insets(0,LABEL_VALUE_SPACE,LABEL_VALUE_SPACE,0);
		builder.add(numberOfDecimals);
		
		JLabel cellFontNameLabel = new JLabel("Font Name:");
		builder.nextRow().add(cellFontNameLabel);
		builder.insets(0,LABEL_VALUE_SPACE,LABEL_VALUE_SPACE,0);
		builder.add(cellFontName);
		
		JLabel cellFontSizeLabel = new JLabel("Font Size:");
		builder.nextRow().add(cellFontSizeLabel);
		builder.insets(0,LABEL_VALUE_SPACE,LABEL_VALUE_SPACE,0);
		builder.add(cellFontSize);
		
		JLabel cellFontColorLabel = new JLabel("Font Color:");
		builder.nextRow().add(cellFontColorLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		builder.add(cellFontColorComboBox);
		
		JLabel cellAlignmentLabel = new JLabel(bundle.getString("CELL_ALIGNMENT"));
		builder.nextRow().add(cellAlignmentLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		builder.add(cellAlignLeft, hbox(THIN_SPACE), cellAlignCenter, hbox(THIN_SPACE), cellAlignRight, hbox(THIN_SPACE), cellAlignDecimal);

		JLabel cellFontStyleLabel = new JLabel("Font Style:");
		builder.nextRow().add(cellFontStyleLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		builder.add(cellFontStyleBold,cellFontStyleItalic, cellFontUnderline);
		
		JLabel cellBackgroundColorLabel = new JLabel("Background Color:");
		builder.nextRow().add(cellBackgroundColorLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		builder.add(cellBackgroundColorComboBox);
		
		JLabel cellBordersLabel = new JLabel(bundle.getString("BORDERS"));
		builder.nextRow().add(cellBordersLabel);
		builder.insets(0,LABEL_VALUE_SPACE,0,0);
		builder.add(cellBorderOnLeft,  hbox(THIN_SPACE), cellBorderOnRight,  hbox(THIN_SPACE), cellBorderOnTop,  hbox(THIN_SPACE), cellBorderOnBottom);
		
		mgr.tagComponents(SINGLE_CELL_SELECTION, propertyToShowLabel, propertyToShow);
		mgr.tagComponents(ENUMERATION_CONTROLS, enumerationLabel, enumeration);
		mgr.tagComponents(SELECTION_NOT_EMPTY, cellAlignmentLabel, cellAlignLeft, cellAlignCenter, cellAlignRight, enumerationLabel, enumeration);
		mgr.tagComponents(DECIMAL_CONTROLS, numberOfDecimalsLabel, numberOfDecimals);
		mgr.tagComponents(SELECTION_NOT_EMPTY, numberOfDecimalsLabel, numberOfDecimals);
		mgr.tagComponents(DATE_CONTROLS, dateLabel, dateFormatChooser);
		mgr.tagComponents(DECIMAL_ALIGNMENT_BUTTON, cellAlignDecimal);
		mgr.tagComponents(SELECTION_NOT_EMPTY, cellBordersLabel, cellBorderOnLeft, cellBorderOnTop, cellBorderOnRight, cellBorderOnBottom);


		mgr.tagComponents(NOT_FULLY_IMPLEMENTED, propertyToShowLabel, propertyToShow);

		loadSettings();

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				allPanels.add(TableSettingsControlPanel.this);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
				// ignore
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				allPanels.remove(TableSettingsControlPanel.this);
			}

		});

		// Reload the settings when the table selection changes.
		controller.addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged() {
				loadSettings();
			}
		});
	}

	private ButtonGroup groupButtons(AbstractButton... buttons) {
		ButtonGroup group = new ButtonGroup();
		for (AbstractButton b : buttons) {
			group.add(b);
		}
		return group;
	}

	private static class TitledPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		ConstraintBuilder builder;

		public TitledPanel(String titleKey) {
			setLayout(new FlowLayout(FlowLayout.LEADING));
			JPanel panel = new JPanel();
			builder = new ConstraintBuilder(panel);
			builder.w().makeDefault();
			add(panel);

			setBorder(new TitledBorder(bundle.getString(titleKey)));
		}

		public ConstraintBuilder getBuilder() {
			return builder;
		}

	}

	/**
	 * Notify all other panels that the settings have changed.
	 */
	private void settingsChanged() {
		for (TableSettingsControlPanel panel : allPanels) {
			if (panel != this) {
				panel.loadSettings();
			}
		}
	}

	private void setValueWithoutDispatchingEvents(JSpinner spinner, Object value, ChangeListener cl) {
		spinner.removeChangeListener(cl);
		spinner.setValue(value);
		spinner.addChangeListener(cl);
	}
	
	private void setSelectedWithoutDispatchingEvents(JRadioButton button, boolean selected) {
		ActionListener[]  listeners = button.getActionListeners();
		for (ActionListener al:listeners) {
			button.removeActionListener(al);
		}
		button.setSelected(selected);
		for (ActionListener al:listeners) {
			button.addActionListener(al);
		}
	}
	
	private void setSelectedWithoutDispatchingEvents(JComboBox comboBox, int selectedIndex) {
		ActionListener[]  listeners = comboBox.getActionListeners();
		for (ActionListener al:listeners) {
			comboBox.removeActionListener(al);
		}
		comboBox.setSelectedIndex(selectedIndex);
		for (ActionListener al:listeners) {
			comboBox.addActionListener(al);
		}
	}
	
	/**
	 * Loads settings from the controller and sets the control states.
	 */
	public void loadSettings() {
		// Set initial values for the controls.
		if (controller.getTableOrientation() == TableOrientation.ROW_MAJOR) {
			setSelectedWithoutDispatchingEvents(tableRowOrientation, true);
		} else {
			setSelectedWithoutDispatchingEvents(tableColOrientation, true);
		}
		if ( controller.getRowHeight() > 0) {
			setValueWithoutDispatchingEvents(rowHeightSpinner, controller.getRowHeight(), rowHeightListener);
		}
		setValueWithoutDispatchingEvents(columnWidthSpinner, controller.getColumnWidth(), columnWidthListener);

		
		
		//		showGridCheckbox.setSelected(controller.getShowGrid());

		mgr.showAll();

		configureAbbreviationSettings(
				LabelType.ROW,
				ROW_HEADER_LABEL,
				rowHeaderFullLabel,
				rowHeaderAbbreviatedLabel,
				rowHeaderAbbreviations,
				controller.getRowLabelAbbreviationSettings()
		);
		configureAbbreviationSettings(
				LabelType.COLUMN,
				COLUMN_HEADER_LABEL,
				columnHeaderFullLabel,
				columnHeaderAbbreviatedLabel,
				columnHeaderAbbreviations,
				controller.getColumnLabelAbbreviationSettings()
		);
		configureAbbreviationSettings(
				LabelType.CELL,
				CELL_LABEL,
				cellFullLabel,
				cellAbbreviatedLabel,
				cellAbbreviations,
				controller.getCellLabelAbbreviationSettings()
		);

		ComboBoxModel enumerationModel = controller.getEnumerationModel();
		enumeration.setModel(enumerationModel);
		if (enumerationModel.getSize() <= 1) { // i.e., only "None"
			enumeration.setEnabled(false);
		} else {
			enumeration.setEnabled(true);
		}
		
		if (controller.selectedCellsHaveMixedEnumerations()) {
			ActionListener[] listeners = enumeration.getActionListeners();
			for (ActionListener al : listeners) {
				enumeration.removeActionListener(al);
			}
			enumeration.setSelectedIndex(-1);
			for (ActionListener al : listeners) {
				enumeration.addActionListener(al);
			}
		}

		ActionListener[] listeners = numberOfDecimals.getActionListeners();
		for (ActionListener al : listeners) {
			numberOfDecimals.removeActionListener(al);
		}
		int decimals = controller.getDecimalPlaces() == null ? -1 : controller.getDecimalPlaces();
		numberOfDecimals.setSelectedIndex(decimals);
		for (ActionListener al : listeners) {
			numberOfDecimals.addActionListener(al);
		}
		
		//Row and Column Formatting controls
		//Font Name
		listeners = rowHeaderFontName.getActionListeners();
		for (ActionListener al : listeners) {
			rowHeaderFontName.removeActionListener(al);
		}
		if (controller.getRowHeaderFontName() != null) {
			rowHeaderFontName.setSelectedItem(controller.getRowHeaderFontName());
		} else {
			rowHeaderFontName.setSelectedIndex(-1);
		}
		for (ActionListener al : listeners) {
			rowHeaderFontName.addActionListener(al);
		}
		listeners = columnHeaderFontName.getActionListeners();
		for (ActionListener al : listeners) {
			columnHeaderFontName.removeActionListener(al);
		}
		if (controller.getRowHeaderFontName() != null) {
			columnHeaderFontName.setSelectedItem(controller.getColumnHeaderFontName());
		} else {
			columnHeaderFontName.setSelectedIndex(-1);
		}
		for (ActionListener al : listeners) {
			columnHeaderFontName.addActionListener(al);
		}
		//Font Size
		if (controller.getRowHeaderFontSize() != null) {
			setValueWithoutDispatchingEvents(rowHeaderFontSize,
					controller.getRowHeaderFontSize().intValue(),
					rowHeaderFontSizeListener);
		}
		if (controller.getColumnHeaderFontSize() != null) {
			setValueWithoutDispatchingEvents(columnHeaderFontSize,
					controller.getColumnHeaderFontSize().intValue(),
					columnHeaderFontSizeListener);
		}
		if (controller.getCellFontSize() != null) {
			setValueWithoutDispatchingEvents(cellFontSize,
					controller.getCellFontSize().intValue(),
					cellFontSizeListener);
		}
		
		//Font Style
		if (controller.getRowFontStyle() != null) {
			if (controller.getRowFontStyle().equals(Font.BOLD)) {
				rowHeaderFontStyleBold.setSelected(true);
				rowHeaderFontStyleItalic.setSelected(false);	
			} else if (controller.getRowFontStyle().equals(Font.ITALIC)) {
				rowHeaderFontStyleItalic.setSelected(true);	
				rowHeaderFontStyleBold.setSelected(false);
			} else if (controller.getRowFontStyle().equals(Font.BOLD+Font.ITALIC)) {
				rowHeaderFontStyleBold.setSelected(true);
				rowHeaderFontStyleItalic.setSelected(true);
			} else {
				rowHeaderFontStyleBold.setSelected(false);
				rowHeaderFontStyleItalic.setSelected(false);
			}
		} else {
			rowHeaderFontStyleBold.setSelected(false);
			rowHeaderFontStyleItalic.setSelected(false);
		}
		if (controller.getCellFontTextAttribute() != null) {
			if (controller.getCellFontTextAttribute().equals(TextAttribute.UNDERLINE_ON)) {
				cellFontUnderline.setSelected(true);
			} else {
				cellFontUnderline.setSelected(false);
			}
		}
		if (controller.getRowHeaderTextAttribute() != null) {
			if (controller.getRowHeaderTextAttribute().equals(TextAttribute.UNDERLINE_ON)) {
				rowHeaderFontUnderline.setSelected(true);
			} else {
				rowHeaderFontUnderline.setSelected(false);
			}
		}
		
		if (controller.getColumnHeaderFontStyle() != null) {
			if (controller.getColumnHeaderFontStyle().equals(Font.BOLD)) {
				columnHeaderFontStyleBold.setSelected(true);
				columnHeaderFontStyleItalic.setSelected(false);
			} else if (controller.getColumnHeaderFontStyle().equals(Font.ITALIC)) {
				columnHeaderFontStyleItalic.setSelected(true);
				columnHeaderFontStyleBold.setSelected(false);
			} else if (controller.getColumnHeaderFontStyle().equals(Font.BOLD+Font.ITALIC)) {
				columnHeaderFontStyleBold.setSelected(true);
				columnHeaderFontStyleItalic.setSelected(true);
			} else {
				columnHeaderFontStyleBold.setSelected(false);
				columnHeaderFontStyleItalic.setSelected(false);
			}
		} else {
			columnHeaderFontStyleBold.setSelected(false);
			columnHeaderFontStyleItalic.setSelected(false);
		}
		
		if (controller.getColumnHeaderTextAttribute() != null) {
			if (controller.getColumnHeaderTextAttribute().equals(TextAttribute.UNDERLINE_ON)) {
				columnHeaderFontStyleUnderline.setSelected(true);
			} else {
				columnHeaderFontStyleUnderline.setSelected(false);
			}
		}
		
		if (controller.getCellFontStyle() != null) {
			if (controller.getCellFontStyle().equals(Font.BOLD)) {
				cellFontStyleBold.setSelected(true);
				cellFontStyleItalic.setSelected(false);
			} else if (controller.getCellFontStyle().equals(Font.ITALIC)) {
				cellFontStyleItalic.setSelected(true);
				cellFontStyleBold.setSelected(false);
			} else if (controller.getCellFontStyle().equals(Font.BOLD+Font.ITALIC)) {
				cellFontStyleBold.setSelected(true);
				cellFontStyleItalic.setSelected(true);
			} else {
				cellFontStyleBold.setSelected(false);
				cellFontStyleItalic.setSelected(false);
			}
		} else {
			cellFontStyleBold.setSelected(false);
			cellFontStyleItalic.setSelected(false);
		}
		
		//  Color
		if (controller.getRowHeaderFontColor() != null) {
			listeners = rowForegroundColorComboBox.getActionListeners();
			for (ActionListener al : listeners) {
				rowForegroundColorComboBox.removeActionListener(al);
			}
			rowForegroundColorComboBox.setSelectedItem(controller.getRowHeaderFontColor());
			for (ActionListener al : listeners) {
				rowForegroundColorComboBox.addActionListener(al);
			}
		} else {
			setSelectedWithoutDispatchingEvents(rowForegroundColorComboBox, 0);
		}
		
		if (controller.getColumnHeaderFontColor() != null) {
			listeners = columnForegroundColorComboBox.getActionListeners();
			for (ActionListener al : listeners) {
				columnForegroundColorComboBox.removeActionListener(al);
			}
			columnForegroundColorComboBox.setSelectedItem(controller.getColumnHeaderFontColor());
			for (ActionListener al : listeners) {
				columnForegroundColorComboBox.addActionListener(al);
			}
		} else {
			setSelectedWithoutDispatchingEvents(columnForegroundColorComboBox, 0);
		}
		
		
		
		if (controller.getRowHeaderBorderColor() != null) {
			listeners = rowHeaderBorderColorComboBox.getActionListeners();
			for (ActionListener al : listeners) {
				rowHeaderBorderColorComboBox.removeActionListener(al);
			}
			rowHeaderBorderColorComboBox.setSelectedItem(controller.getRowHeaderBorderColor());
			for (ActionListener al : listeners) {
				rowHeaderBorderColorComboBox.addActionListener(al);
			}
		} else {
			setSelectedWithoutDispatchingEvents(rowHeaderBorderColorComboBox, 0);
		}
		
		if (controller.getColumnHeaderBorderColor() != null) {
			columnHeaderBorderColorComboBox.setSelectedItem(controller.getColumnHeaderBorderColor());
		} else {
			setSelectedWithoutDispatchingEvents(columnHeaderBorderColorComboBox, 0);
		}
		
	
		
		if (controller.getRowHeaderBackgroundColor() != null) {
			listeners = rowBackgroundColorComboBox.getActionListeners();
			for (ActionListener al : listeners) {
				rowBackgroundColorComboBox.removeActionListener(al);
			}
			rowBackgroundColorComboBox.setSelectedItem(controller.getRowHeaderBackgroundColor());
			for (ActionListener al : listeners) {
				rowBackgroundColorComboBox.addActionListener(al);
			}
		} else {
			setSelectedWithoutDispatchingEvents(rowBackgroundColorComboBox, 0);
		}
		if (controller.getColumnHeaderBackgroundColor() != null) {
			columnBackgroundColorComboBox.setSelectedItem(controller.getColumnHeaderBackgroundColor());
		} else {
			setSelectedWithoutDispatchingEvents(columnBackgroundColorComboBox, 0);
		}
		
		if (controller.getCellFontColor() != null) {
			listeners = cellFontColorComboBox.getActionListeners();
			for (ActionListener al : listeners) {
				cellFontColorComboBox.removeActionListener(al);
			}
			cellFontColorComboBox.setSelectedItem(controller.getCellFontColor());
			for (ActionListener al : listeners) {
				cellFontColorComboBox.addActionListener(al);
			}
		} else {
			setSelectedWithoutDispatchingEvents(cellFontColorComboBox, 0);
		}
		if (controller.getCellBackgroundColor() != null) {
			listeners = cellBackgroundColorComboBox.getActionListeners();
			for (ActionListener al : listeners) {
				cellBackgroundColorComboBox.removeActionListener(al);
			}
			cellBackgroundColorComboBox.setSelectedItem(controller.getCellBackgroundColor());
			for (ActionListener al : listeners) {
				cellBackgroundColorComboBox.addActionListener(al);
			}
		} else {
			setSelectedWithoutDispatchingEvents(cellBackgroundColorComboBox, 0);
		}
		
		//Cell Formatting Controls
		listeners = cellFontName.getActionListeners();
		for (ActionListener al : listeners) {
			cellFontName.removeActionListener(al);
		}

		if (controller.getCellFontName() != null) {
			cellFontName.setSelectedItem(controller.getCellFontName());
		} else {
			cellFontName.setSelectedIndex(-1);
		}
		for (ActionListener al : listeners) {
			cellFontName.addActionListener(al);
		}
		
		

		listeners = dateFormatChooser.getActionListeners();
		for (ActionListener al : listeners) {
			dateFormatChooser.removeActionListener(al);
		}

		DateFormatItem dateFormatState = controller.getDateFormat() == null ? DateFormatItem.None : controller.getDateFormat();
		dateFormatChooser.setSelectedItem(dateFormatState);
		for (ActionListener al : listeners) {
			dateFormatChooser.addActionListener(al);
		}

		// Load cell border settings, but don't let action listeners fire.
		BorderState borderState = controller.getBorderState();
		toggleWithoutAction(cellBorderOnLeft,  borderState.hasWestBorder());
		toggleWithoutAction(cellBorderOnTop, borderState.hasNorthBorder()); 
		toggleWithoutAction(cellBorderOnRight, borderState.hasEastBorder());
		toggleWithoutAction(cellBorderOnBottom, borderState.hasSouthBorder());
		
		// Load row header border settings, but don't let action listeners fire.
		BorderState rowHeaderborderState = controller.getRowHeaderBorderState();
		if (rowHeaderborderState == null) {
			rowHeaderborderState = new BorderState(BorderEdge.NONE.value());
		}
		toggleWithoutAction(rowHeaderBorderOnLeft,  rowHeaderborderState.hasWestBorder());
		toggleWithoutAction(rowHeaderBorderOnTop, rowHeaderborderState.hasNorthBorder()); 
		toggleWithoutAction(rowHeaderBorderOnRight, rowHeaderborderState.hasEastBorder());
		toggleWithoutAction(rowHeaderBorderOnBottom, rowHeaderborderState.hasSouthBorder());
		
		// Load column header border settings, but don't let action listeners fire.
		BorderState columnHeaderborderState = controller.getColumnHeaderBorderState();
		if (columnHeaderborderState == null) {
			columnHeaderborderState = new BorderState(BorderEdge.NONE.value());
		}
		toggleWithoutAction(columnHeaderBorderOnLeft,  columnHeaderborderState.hasWestBorder());
		toggleWithoutAction(columnHeaderBorderOnTop, columnHeaderborderState.hasNorthBorder()); 
		toggleWithoutAction(columnHeaderBorderOnRight, columnHeaderborderState.hasEastBorder());
		toggleWithoutAction(columnHeaderBorderOnBottom, columnHeaderborderState.hasSouthBorder());
		
		// Load the alignment settings, but don't let action listeners fire.
		ContentAlignment alignment = controller.getCellAlignment();
		selectButtonWithoutAction(cellAlignGroup, alignment);
		alignment = controller.getRowHeaderAlignment();
		selectButtonWithoutAction(rowHeaderAlignGroup, alignment);
		alignment = controller.getColumnHeaderAlignment();
		selectButtonWithoutAction(columnHeaderAlignGroup, alignment);

		/*
		 * Disable/Enable the table orientation radio buttons when:
		 * 1) No rows or columns are selected (default condition), then "Display collections as Rows or Columns" radio button is enabled 
		 * 2) When one or more rows or columns are selected, then "Display collections as Rows or Columns" radio button is disabled. 
		 * 3) When all rows or columns are selected, then "Display collections as Rows or Columns" radio button is enabled 
		 */
		if (controller.getSelectedCellCount() == (controller.getTableColumnCount() * controller.getTableRowCount()) 
				|| ((controller.getSelectedCellCount() == 0) && controller.canSetOrientation())) {
			mgr.enable(TABLE_ORIENTATION, true);
		} else if (controller.getSelectedCellCount() > 0) {
			mgr.disable(TABLE_ORIENTATION, true);
		} 
		
		// Show/hide controls based on the selection.
		mgr.hide(SELECTION_NOT_EMPTY, controller.getSelectedCellCount() == 0);
		mgr.hide(HEADERS_HIDABLE, !controller.isCanHideHeaders());
		mgr.hide(SINGLE_CELL_SELECTION, controller.getSelectedCellCount()!=1);
		mgr.hide(TABLE_ORIENTATION, !controller.canSetOrientation());
		mgr.hide(TRANSPOSE, !controller.canTranspose());
		mgr.hide(NOT_FULLY_IMPLEMENTED, true);

		// Disable/Enable numerics depending on the state of the enumeration combo box and date chooser
		mgr.enable(DATE_CONTROLS, true);
		if ((!controller.enumerationIsNone(enumerationModel)  )) {  
			mgr.disable(DECIMAL_CONTROLS, true);
			mgr.disable(DECIMAL_ALIGNMENT_BUTTON, true);
			mgr.disable(DATE_CONTROLS, true);
		} else if (!controller.dateIsNone(dateFormatChooser.getModel())) {
			mgr.disable(DECIMAL_CONTROLS, true);
			mgr.disable(DECIMAL_ALIGNMENT_BUTTON, true);
		} else {
			mgr.enable(DECIMAL_CONTROLS, true);
			mgr.enable(DECIMAL_ALIGNMENT_BUTTON, true);
			mgr.enable(DATE_CONTROLS, true);
		}

		// Hide controls if they will do nothing.  R6It1 No longer hiding 
//		mgr.hide(ENUMERATION_CONTROLS, enumeration.getModel().getSize()==0);

		mgr.hide(DECIMAL_CONTROLS, !controller.showDecimalPlaces());
		mgr.hide(DATE_CONTROLS, !controller.showDecimalPlaces()); 
		mgr.hideIfOthersHidden(rowColumnFormattingPanel, rowHeightSpinner, columnWidthSpinner, 
				tableRowOrientation, transposeTableButton, rowHeaderAlignLeft, 
				columnHeaderAlignLeft, showRowHeaders); 
		mgr.tagComponents(COLUMN_IS_NOT_SELECTED, columnHeaderFontName, columnHeaderFontSize, 
				columnHeaderFontStyleBold, columnHeaderFontStyleItalic,
				columnForegroundColorComboBox);
		mgr.hide(COLUMN_IS_NOT_SELECTED, controller.getSelectedColumnCount() == 0);
		mgr.tagComponents(ROW_IS_NOT_SELECTED, rowColumnFontFormattingPanel,
				rowHeaderFontName, rowHeaderFontSize, rowHeaderFontStyleBold, 
				rowHeaderFontStyleItalic,rowForegroundColorComboBox); 				
		mgr.hide(ROW_IS_NOT_SELECTED, controller.getSelectedRowCount() == 0);	
		mgr.hideIfOthersHidden(cellFormattingPanel, propertyToShow, enumeration, numberOfDecimals, dateFormatChooser, cellBorderOnLeft, cellBorderOnTop, cellBorderOnRight, cellBorderOnBottom);
	}
	
	/**
	 * Toggles a button. Ensures that no listeners will be called by first removing all
	 * listeners for the button and then adding them back.
	 * 
	 * @param button the button 
	 * @param hasEdge true if the cell controlled by this jbutton has this edge drawn.  
	 */
	private void toggleWithoutAction(JToggleButton button, boolean hasEdge) {

		ActionListener[] listeners = button.getActionListeners();
		for (ActionListener listener : listeners) {
			button.removeActionListener(listener);
		}
		if (hasEdge) {
			button.setSelected(true);
		} else {
			button.setSelected(false);
		}
		for (ActionListener listener : listeners) {
			button.addActionListener(listener);
		}
	}
	

	/**
	 * Selects a radio button within a button group based on its action command
	 * string. Ensures that no listeners will be called by first removing all
	 * command listeners for the button and then adding them back.
	 * 
	 * @param group the button group
	 * @param actionCommandObject an object whose string representation is the action command of the
	 *   button to select, or null if no button should be selected
	 */
	private void selectButtonWithoutAction(ButtonGroup group, Object actionCommandObject) {
		group.clearSelection();

		if (actionCommandObject != null) {
			String actionCommand = actionCommandObject.toString();
			for (Enumeration<AbstractButton> e=group.getElements(); e.hasMoreElements(); ) {
				AbstractButton b = e.nextElement();
				if (b.getActionCommand().equals(actionCommand)) {
					ActionListener[] listeners = b.getActionListeners();
					for (ActionListener listener : listeners) {
						b.removeActionListener(listener);
					}

					b.setSelected(true);

					for (ActionListener listener : listeners) {
						b.addActionListener(listener);
					}
				}
			}
		}
	}

	private static enum LabelType { CELL, COLUMN, ROW }

	private void configureAbbreviationSettings(
			final LabelType type,
			String tag,
			JLabel fullLabel,
			final JLabel abbreviatedLabel,
			JPanel abbreviationsPanel,
			final AbbreviationSettings abbreviationSettings
	) {
		abbreviationsPanel.removeAll();

		if (abbreviationSettings==null || !abbreviationSettings.canAbbreviate()) {
			mgr.hide(tag, true);
		} else {
			fullLabel.setText(abbreviationSettings.getFullLabel());
			abbreviatedLabel.setText(abbreviationSettings.getAbbreviatedLabel());
			abbreviationsPanel.setLayout(new FlowLayout());
			for (ComboBoxModel model : abbreviationSettings.getAbbreviationModels()) {
				if (model.getSize() > 1) {
					JComboBox dropDown = new JComboBox(model);
					dropDown.getAccessibleContext().setAccessibleName(model.getElementAt(0)+"comboBox");
					dropDown.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							abbreviatedLabel.setText(abbreviationSettings.getAbbreviatedLabel());
							if (type == LabelType.CELL) {
								controller.setCellLabelAbbreviations(abbreviationSettings);
							} else if (type == LabelType.ROW) {
								controller.setRowLabelAbbreviations(abbreviationSettings);
							} else {
								controller.setColumnLabelAbbreviations(abbreviationSettings);
							}
						}
					});
					abbreviationsPanel.add(dropDown);
				} else {
					abbreviationsPanel.add(new JLabel((String) model.getElementAt(0)));
				}
			}
		}
	}

	private JRadioButton getIconRadioButton(String offName, String onName, String description) {
		JRadioButton button = new JRadioButton(loadIcon(offName, description));
		button.setSelectedIcon(loadIcon(onName, description));
		return button;
	}

	private Icon loadIcon(String name, String description) {
		URL url = getClass().getClassLoader().getResource("images/" + name);
		return new ImageIcon(url, description);
	}

	private void setAccessibleName(JComponent component, String keyPrefix) {
		String nameKey = keyPrefix + "_NAME";
		String descriptionKey = keyPrefix + "_DESCRIPTION";

		assert bundle.containsKey(nameKey);
		assert bundle.containsKey(descriptionKey);
		component.getAccessibleContext().setAccessibleName(bundle.getString(nameKey));
		component.getAccessibleContext().setAccessibleDescription(bundle.getString(descriptionKey));
	}

	/**
	 * Returns a component whose paintComponent method is used to display the combo box items.
	 * Sets the label string for the comboBox.  In the default combo box model,  the object's toString is used
	 * but the date object's guiString is required, rather than its toString java value.
	 */
	class DateComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		
		/**
		 * This method finds the text corresponding
		 * to the selected value and returns the label, set up
		 * to display the text.
		 * @param list a list object used to display the items. 
		 * @param value - the object to render
		 * @param index -- the index of the object to render
		 * @param isSelected - indicates whether the object to render is selected.
		 * @param cellHasFocus - indicates whether the object to render has the focus     
		 */
		@Override
		public Component getListCellRendererComponent(
				JList list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {
			
			    JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			    l.setText(DateFormatItem.class.cast(value).getGuiLabel());
			    return l;
		}
	}
	
	private JPanel createHeaderFormattingPanel() {
		
		//Alignment Controls 
		JPanel rowHeaderAlignmentControlPanel = new JPanel(new FlowLayout());
		rowHeaderAlignmentControlPanel.add(rowHeaderAlignLeft);
		rowHeaderAlignmentControlPanel.add(rowHeaderAlignCenter);
		rowHeaderAlignmentControlPanel.add(rowHeaderAlignRight);
//		thePanel.add(rowHeaderAlignmentControlPanel, gbcCenter2);
		
		JPanel columnHeaderAlignmentControlPanel = new JPanel(new FlowLayout());
		columnHeaderAlignmentControlPanel.add(columnHeaderAlignLeft);
		columnHeaderAlignmentControlPanel.add(columnHeaderAlignCenter);
		columnHeaderAlignmentControlPanel.add(columnHeaderAlignRight);
		
		// Font Size Controls
        
		TitledPanel theHFPanel = new TitledPanel("HEADER_LABEL_FORMATTING");
		ConstraintBuilder builder = theHFPanel.getBuilder();
		builder.at(0,1).baseline_centered().add(new JLabel(bundle.getString("ROW")));
		builder.at(0,2).baseline_centered().add(new JLabel(bundle.getString("COLUMN")));
		builder.at(1,0).baseline_w().add(new JLabel(bundle.getString("FONT_NAME_LABEL"))
										,hbox(10));
		builder.at(1,1).baseline_w().add(rowHeaderFontName, hbox(20));
		builder.at(1,2).baseline_w().add(columnHeaderFontName);
		builder.insets(LABEL_VALUE_SPACE,0,0,0);
		builder.at(2,0).baseline_w().add(new JLabel(bundle.getString("FONT_SIZE_LABEL")));
		builder.at(2,1).baseline_w().add(rowHeaderFontSize);
		builder.at(2,2).baseline_w().add(columnHeaderFontSize);
		builder.at(3,0).w().add(new JLabel(bundle.getString("FONT_COLOR_LABEL")));
		builder.at(3,1).insets(LABEL_VALUE_SPACE,0,0,0).baseline_w().add(rowForegroundColorComboBox);
		builder.at(3,2).insets(LABEL_VALUE_SPACE,0,0,0).baseline_w().add(columnForegroundColorComboBox);
		builder.at(4,0).w().add(new JLabel(bundle.getString("ALIGNMENT")));
		builder.at(4,1).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(rowHeaderAlignLeft, 
				rowHeaderAlignCenter, rowHeaderAlignRight);
		builder.at(4,2).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(columnHeaderAlignLeft,
				columnHeaderAlignCenter, columnHeaderAlignRight);
		builder.at(5,0).w().add(new JLabel(bundle.getString("FONT_STYLE_LABEL")));
		builder.at(5,1).insets(LABEL_VALUE_SPACE,0,0,0).baseline_w().add(rowHeaderFontStyleBold, 
				rowHeaderFontStyleItalic,rowHeaderFontUnderline);
		builder.at(5,2).insets(LABEL_VALUE_SPACE,0,0,0).baseline_w().add(columnHeaderFontStyleBold,
				columnHeaderFontStyleItalic,columnHeaderFontStyleUnderline);
		builder.at(6,0).baseline_w().add(new JLabel(bundle.getString("BACKGROUND_COLOR_LABEL")),hbox(10));
		builder.at(6,1).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(rowBackgroundColorComboBox);
		builder.at(6,2).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(columnBackgroundColorComboBox);
		builder.at(7,0).w().add(new JLabel(bundle.getString("ROW_COL_BORDER")));
		builder.at(7,1).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(rowHeaderBorderOnLeft,
				rowHeaderBorderOnRight, rowHeaderBorderOnTop, rowHeaderBorderOnBottom, hbox(10));
		builder.at(7,2).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(columnHeaderBorderOnLeft,
				columnHeaderBorderOnRight, columnHeaderBorderOnTop, columnHeaderBorderOnBottom);
		builder.at(8,0).w().add(new JLabel(bundle.getString("BORDER_COLOR")));
		builder.at(8,1).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(rowHeaderBorderColorComboBox);
		builder.at(8,2).insets(LABEL_VALUE_SPACE,0,0,0).nw().add(columnHeaderBorderColorComboBox);
		
		
		
		return theHFPanel;
	}
	
	private JComboBox setUpFontControl() {
//		DefaultComboBoxModel fontModel = new DefaultComboBoxModel();
		return new JComboBox(TableFormattingConstants.JVMFontFamily.values()
//				new String[] { 
//				Font.DIALOG, 
//				Font.MONOSPACED,
//				Font.SANS_SERIF,
//				Font.SERIF}
				 );
	}
	
	private void setUpHeaderBorderButtons() {
		//Row Headers
        rowHeaderBorderOnLeft = getIconRadioButton("LeftBorder_off.png", "LeftBorder_on.png", bundle.getString("ROW_BORDER_LEFT_DESCRIPTION"));
		setAccessibleName(rowHeaderBorderOnLeft, "ROW_BORDER_LEFT");
        Insets buttonInsets = rowHeaderBorderOnLeft.getInsets();
        buttonInsets.set(0, 0, 0, 
                        2);
        rowHeaderBorderOnLeft.setMargin(buttonInsets);
		rowHeaderBorderOnTop = getIconRadioButton("TopBorder_off.png", "TopBorder_on.png", bundle.getString("ROW_BORDER_TOP_DESCRIPTION"));
		rowHeaderBorderOnTop.setMargin(buttonInsets);
		setAccessibleName(rowHeaderBorderOnTop, "ROW_BORDER_TOP");
		rowHeaderBorderOnRight = getIconRadioButton("RightBorder_off.png", "RightBorder_on.png", bundle.getString("ROW_BORDER_RIGHT_DESCRIPTION"));
		rowHeaderBorderOnRight.setMargin(buttonInsets);
		setAccessibleName(rowHeaderBorderOnRight, "ROW_BORDER_RIGHT");
		rowHeaderBorderOnBottom = getIconRadioButton("BottomBorder_off.png", "BottomBorder_on.png", bundle.getString("ROW_BORDER_BOTTOM_DESCRIPTION"));
		rowHeaderBorderOnBottom.setMargin(buttonInsets);
		setAccessibleName(rowHeaderBorderOnBottom, "ROW_BORDER_BOTTOM");
		ActionListener rowBorderListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BorderState compositeControllerState = new BorderState(rowHeaderBorderOnLeft.getModel(),
						rowHeaderBorderOnTop.getModel(), rowHeaderBorderOnRight.getModel(),  rowHeaderBorderOnBottom.getModel());
				controller.mergeRowHeaderBorderState(compositeControllerState); 
			}
		};
		rowHeaderBorderOnLeft.addActionListener(rowBorderListener);
		rowHeaderBorderOnTop.addActionListener(rowBorderListener);
		rowHeaderBorderOnRight.addActionListener(rowBorderListener);
		rowHeaderBorderOnBottom.addActionListener(rowBorderListener);
		
		//Col Headers
		
        columnHeaderBorderOnLeft = getIconRadioButton("LeftBorder_off.png", "LeftBorder_on.png", bundle.getString("COL_BORDER_LEFT_DESCRIPTION"));
        columnHeaderBorderOnLeft.setMargin(buttonInsets);
        setAccessibleName(columnHeaderBorderOnLeft, "COL_BORDER_LEFT");
		columnHeaderBorderOnTop = getIconRadioButton("TopBorder_off.png", "TopBorder_on.png", bundle.getString("COL_BORDER_TOP_DESCRIPTION"));
		columnHeaderBorderOnTop.setMargin(buttonInsets);
		setAccessibleName(columnHeaderBorderOnTop, "COL_BORDER_TOP");
		columnHeaderBorderOnRight = getIconRadioButton("RightBorder_off.png", "RightBorder_on.png", bundle.getString("COL_BORDER_RIGHT_DESCRIPTION"));
		columnHeaderBorderOnRight.setMargin(buttonInsets);
		setAccessibleName(columnHeaderBorderOnRight, "COL_BORDER_RIGHT");
		columnHeaderBorderOnBottom = getIconRadioButton("BottomBorder_off.png", "BottomBorder_on.png", bundle.getString("COL_BORDER_BOTTOM_DESCRIPTION"));
		columnHeaderBorderOnBottom.setMargin(buttonInsets);
		setAccessibleName(columnHeaderBorderOnBottom, "COL_BORDER_BOTTOM");
		ActionListener columnBorderListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BorderState compositeControllerState = new BorderState(columnHeaderBorderOnLeft.getModel(),
						columnHeaderBorderOnTop.getModel(), columnHeaderBorderOnRight.getModel(),  columnHeaderBorderOnBottom.getModel());
				controller.mergeColumnHeaderBorderState(compositeControllerState); 
			}
		};
		columnHeaderBorderOnLeft.addActionListener(columnBorderListener);
		columnHeaderBorderOnTop.addActionListener(columnBorderListener);
		columnHeaderBorderOnRight.addActionListener(columnBorderListener);
		columnHeaderBorderOnBottom.addActionListener(columnBorderListener);
		
	}
	
	private void setUpRowFontStyleButtons() {
		rowHeaderFontStyleBold = getIconRadioButton("bold_off.png","bold_on.png", 
				bundle.getString("FONT_BOLD"));
        Insets boldButtonInsets = rowHeaderFontStyleBold.getInsets();
        boldButtonInsets.set(0, 0, 0, 
                        2);
        rowHeaderFontStyleBold.setMargin(boldButtonInsets);
		setAccessibleName(rowHeaderFontStyleBold, "ROW_FONT_BOLD");
		rowHeaderFontStyleItalic = getIconRadioButton("italics_off.png","italics_on.png", 
				bundle.getString("FONT_ITALIC"));	
		rowHeaderFontStyleItalic.setMargin(boldButtonInsets);
		setAccessibleName(rowHeaderFontStyleItalic, "ROW_FONT_ITALIC");
		rowHeaderFontUnderline = getIconRadioButton("underline_off.png","underline_on.png", 
				bundle.getString("FONT_UNDERLINE"));	
		rowHeaderFontUnderline.setMargin(boldButtonInsets);
		setAccessibleName(rowHeaderFontUnderline, "ROW_FONT_UNDERLINE");
		
		ActionListener rowFontStyleListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int fontStyle = Font.PLAIN;
				if (rowHeaderFontStyleBold.getModel().isSelected()) {
					fontStyle = Font.BOLD;
					if (rowHeaderFontStyleItalic.getModel().isSelected()) {
						fontStyle += Font.ITALIC;
					}
				} else if (rowHeaderFontStyleItalic.getModel().isSelected()) {
					fontStyle = Font.ITALIC;
				}
				controller.setRowHeaderFontStyle(fontStyle);
			}
		}; 
		rowHeaderFontStyleBold.addActionListener(rowFontStyleListener);
		rowHeaderFontStyleItalic.addActionListener(rowFontStyleListener);
		
		ActionListener rowHeaderUnderlineListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (rowHeaderFontUnderline.getModel().isSelected()) {
					controller.setRowHeaderTextAttribute(TextAttribute.UNDERLINE_ON);
				} else {
					controller.setRowHeaderTextAttribute(TableFormattingConstants.UNDERLINE_OFF);
				}
			}
		}; 
		rowHeaderFontUnderline.addActionListener(rowHeaderUnderlineListener);
		
	}
	
	private void setUpColumnFontStyleButtons() {
		columnHeaderFontStyleBold = getIconRadioButton("bold_off.png","bold_on.png", 
				bundle.getString("FONT_BOLD"));	
        Insets boldButtonInsets = columnHeaderFontStyleBold.getInsets();
        boldButtonInsets.set(0, 0, 0, 
                        2);
        columnHeaderFontStyleBold.setMargin(boldButtonInsets);
		setAccessibleName(columnHeaderFontStyleBold, "COL_FONT_BOLD");
		columnHeaderFontStyleItalic = getIconRadioButton("italics_off.png","italics_on.png", 
				bundle.getString("FONT_ITALIC"));	
		columnHeaderFontStyleItalic.setMargin(boldButtonInsets);
		setAccessibleName(columnHeaderFontStyleItalic, "COL_FONT_ITALIC");
		columnHeaderFontStyleUnderline = getIconRadioButton("underline_off.png","underline_on.png", 
				bundle.getString("FONT_UNDERLINE"));	
		columnHeaderFontStyleUnderline.setMargin(boldButtonInsets);
		setAccessibleName(columnHeaderFontStyleUnderline, "COL_FONT_UNDERLINE");
		
		ActionListener columnFontStyleListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int fontStyle = Font.PLAIN;
				if (columnHeaderFontStyleBold.getModel().isSelected()) {
					fontStyle = Font.BOLD;
					if (columnHeaderFontStyleItalic.getModel().isSelected()) {
						fontStyle += Font.ITALIC;
					}
				} else if (columnHeaderFontStyleItalic.getModel().isSelected()) {
					fontStyle = Font.ITALIC;
				}
				controller.setColumnHeaderFontStyle(fontStyle);
			}
		}; 
		columnHeaderFontStyleBold.addActionListener(columnFontStyleListener);
		columnHeaderFontStyleItalic.addActionListener(columnFontStyleListener);
		
		ActionListener columnHeaderUnderlineListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (columnHeaderFontStyleUnderline.getModel().isSelected()) {
					controller.setColumnHeaderTextAttribute(TextAttribute.UNDERLINE_ON);
				} else {
					controller.setColumnHeaderTextAttribute(TableFormattingConstants.UNDERLINE_OFF);
				}
			}
		}; 
		columnHeaderFontStyleUnderline.addActionListener(columnHeaderUnderlineListener);
		
	}
	
	private void setUpCellFontStyleButtons() {
		cellFontStyleBold = getIconRadioButton("bold_off.png","bold_on.png", 
				bundle.getString("FONT_BOLD"));	
        Insets buttonInsets = cellFontStyleBold.getInsets();
        buttonInsets.set(buttonInsets.top, 0, buttonInsets.bottom, 
                        buttonInsets.right);
        cellFontStyleBold.setMargin(buttonInsets);
		setAccessibleName(cellFontStyleBold, "CELL_FONT_BOLD");
		cellFontStyleItalic = getIconRadioButton("italics_off.png","italics_on.png", 
				bundle.getString("FONT_ITALIC"));	
		setAccessibleName(cellFontStyleItalic, "CELL_FONT_ITALIC");
		cellFontUnderline = getIconRadioButton("underline_off.png","underline_on.png", 
				bundle.getString("CELL_FONT_UNDERLINE"));	
		setAccessibleName(cellFontUnderline, "CELL_FONT_UNDERLINE");
		
		ActionListener cellFontStyleListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int fontStyle = Font.PLAIN;
				if (cellFontStyleBold.getModel().isSelected()) {
					fontStyle = Font.BOLD;
					if (cellFontStyleItalic.getModel().isSelected()) {
						fontStyle += Font.ITALIC;
					}
				} else if (cellFontStyleItalic.getModel().isSelected()) {
					fontStyle = Font.ITALIC;
				}
				controller.setCellFontStyle(fontStyle);
			}
		}; 
		cellFontStyleBold.addActionListener(cellFontStyleListener);
		cellFontStyleItalic.addActionListener(cellFontStyleListener);
		
		ActionListener cellFontUnderlineListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cellFontUnderline.getModel().isSelected()) {
					controller.setCellFontTextAttribute(TextAttribute.UNDERLINE_ON);
				} else {
					controller.setCellFontTextAttribute(TableFormattingConstants.UNDERLINE_OFF);
				}
			}
		}; 
		
		cellFontUnderline.addActionListener(cellFontUnderlineListener);
	}
	
	private void setUpColorComboBoxes() {
        // Build Color choosers
		rowForegroundColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		setAccessibleName(rowForegroundColorComboBox,"ROW_FONT_COLOR");
		rowForegroundColorComboBox.setName("Row_colorComboBox");
		rowForegroundColorComboBox.setToolTipText("Font color");
		rowForegroundColorComboBox.setMaximumRowCount(5);
		rowForegroundColorComboBox.setPreferredSize(new Dimension(50, 20));
		rowForegroundColorComboBox.setSelectedIndex(0);
		
        rowForegroundColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
			
		});

        // Attach listener to show colors in combo box.
        rowForegroundColorComboBox.addActionListener(new ActionListener() {
        	
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setRowHeaderFontColor(Color.class.cast(rowForegroundColorComboBox.getSelectedItem()));
            }
        });
        
		columnForegroundColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		columnForegroundColorComboBox.setName("Column_colorComboBox");
		setAccessibleName(columnForegroundColorComboBox,"COL_FONT_COLOR");
		columnForegroundColorComboBox.setToolTipText("Font color");
		columnForegroundColorComboBox.setMaximumRowCount(5);
		columnForegroundColorComboBox.setPreferredSize(new Dimension(50, 20));
		columnForegroundColorComboBox.setSelectedIndex(0);

        columnForegroundColorComboBox.setRenderer(new ListCellRenderer() {
    	
    	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
    	
		@Override
		public Component getListCellRendererComponent(JList list,
				Object obj, int arg2, boolean arg3, boolean arg4) {	
			
			if (obj instanceof Color) { 
				myColorPanel.setColor((Color) obj);
				return myColorPanel;
			}
			return new JPanel();
		}
				
		});
        
        // Attach listener to show border styles in combo box.
		columnForegroundColorComboBox.addActionListener(new ActionListener() {
        	
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setColumnHeaderFontColor(Color.class.cast(columnForegroundColorComboBox.getSelectedItem()));
            }
        });
		
		rowBackgroundColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		rowBackgroundColorComboBox.setName("Row_background_colorComboBox");
		setAccessibleName(rowBackgroundColorComboBox,"ROW_BACKGROUND_COLOR");
		rowBackgroundColorComboBox.setToolTipText("Background color");
		rowBackgroundColorComboBox.setMaximumRowCount(5);
		rowBackgroundColorComboBox.setPreferredSize(new Dimension(50, 20));
		rowBackgroundColorComboBox.setSelectedIndex(0);
		
        rowBackgroundColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
					
		});

        // Attach listener to show colors in combo box.
        rowBackgroundColorComboBox.addActionListener(new ActionListener() {
        	
        
        	
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setRowHeaderBackgroundColor(Color.class.cast(rowBackgroundColorComboBox.getSelectedItem()));
            }
        });
        
		rowHeaderBorderColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		rowHeaderBorderColorComboBox.setName("Row_header_border_colorComboBox");
		setAccessibleName(rowHeaderBorderColorComboBox,"ROW_HEADER_BORDER_COLOR");
		rowHeaderBorderColorComboBox.setToolTipText("Border color");
		rowHeaderBorderColorComboBox.setMaximumRowCount(5);
		rowHeaderBorderColorComboBox.setPreferredSize(new Dimension(50, 20));
		rowHeaderBorderColorComboBox.setSelectedIndex(0);
		
		rowHeaderBorderColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
					
		});

        // Attach listener to show colors in combo box.
		rowHeaderBorderColorComboBox.addActionListener(new ActionListener() {
        
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setRowHeaderBorderColor(Color.class.cast(rowHeaderBorderColorComboBox.getSelectedItem()));
            }
        });
        
		columnBackgroundColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		columnBackgroundColorComboBox.setName("Column_background_colorComboBox");
		setAccessibleName(columnBackgroundColorComboBox,"COL_BACKGROUND_COLOR");
		columnBackgroundColorComboBox.setToolTipText("Background color");
		columnBackgroundColorComboBox.setMaximumRowCount(5);
		columnBackgroundColorComboBox.setPreferredSize(new Dimension(50, 20));
		columnBackgroundColorComboBox.setSelectedIndex(0);

		columnBackgroundColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
					
		});
        
        // Attach listener to show border styles in combo box.
		columnBackgroundColorComboBox.addActionListener(new ActionListener() {
        	
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setColumnHeaderBackgroundColor(Color.class.cast(columnBackgroundColorComboBox.getSelectedItem()));
            }
        });
		
		columnHeaderBorderColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		columnHeaderBorderColorComboBox.setName("Col_header_border_colorComboBox");
		setAccessibleName(columnHeaderBorderColorComboBox,"COL_HEADER_BORDER_COLOR");
		columnHeaderBorderColorComboBox.setToolTipText("Border color");
		columnHeaderBorderColorComboBox.setMaximumRowCount(5);
		columnHeaderBorderColorComboBox.setPreferredSize(new Dimension(50, 20));
		columnHeaderBorderColorComboBox.setSelectedIndex(0);
		
		columnHeaderBorderColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
					
		});

        // Attach listener to show colors in combo box.
		columnHeaderBorderColorComboBox.addActionListener(new ActionListener() {
        
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setColumnHeaderBorderColor(Color.class.cast(columnHeaderBorderColorComboBox.getSelectedItem()));
            }
        });
		
		cellFontColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		setAccessibleName(cellFontColorComboBox,"CELL_FONT_COLOR");
		cellFontColorComboBox.setName("Cell_colorComboBox");
		cellFontColorComboBox.setToolTipText("Font color");
		cellFontColorComboBox.setMaximumRowCount(5);
		cellFontColorComboBox.setPreferredSize(new Dimension(50, 20));
		cellFontColorComboBox.setSelectedIndex(0);

		cellFontColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
				
		});
        
        // Attach listener to show border styles in combo box.
		cellFontColorComboBox.addActionListener(new ActionListener() {
        	
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setCellFontColor(Color.class.cast(cellFontColorComboBox.getSelectedItem()));
            }
        });
		
		cellBackgroundColorComboBox = new JComboBox(TableFormattingConstants.ForegroundColors);
		setAccessibleName(cellBackgroundColorComboBox,"CELL_BACKGROUND_COLOR");
		cellBackgroundColorComboBox.setName("Cell_BackgroundColorComboBox");
		cellBackgroundColorComboBox.setToolTipText("Background color");
		cellBackgroundColorComboBox.setMaximumRowCount(5);
		cellBackgroundColorComboBox.setPreferredSize(new Dimension(50, 20));
		cellBackgroundColorComboBox.setSelectedIndex(0);

		cellBackgroundColorComboBox.setRenderer(new ListCellRenderer() {
        	
        	private ColorPanel myColorPanel = new ColorPanel(new Color(0));
        	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object obj, int arg2, boolean arg3, boolean arg4) {	
				
				if (obj instanceof Color) { 
					myColorPanel.setColor((Color) obj);
					return myColorPanel;
				}
				return new JPanel();
			}
						
		});
        
        // Attach listener to show border styles in combo box.
		cellBackgroundColorComboBox.addActionListener(new ActionListener() {
        	
            @Override
			public void actionPerformed(ActionEvent e) {
            	controller.setCellBackgroundColor(Color.class.cast(cellBackgroundColorComboBox.getSelectedItem()));
            }
        });
	}
	
    
	/**
	 * A JPanel that draws a color, for color dropdowns.
	 * @author vwoeltje	 
	 */
	private static class ColorPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5931786628055358422L;

		private static final Dimension COMBO_BOX_DIMENSION = new Dimension(50, 20);

		Color color;
		public ColorPanel(Color c) {
			color = c;
			setBackground(c);
			this.setPreferredSize(COMBO_BOX_DIMENSION);			
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(color);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		
		protected void setColor(Color aColor) {
			this.color = aColor;
		}

		
	}

}
