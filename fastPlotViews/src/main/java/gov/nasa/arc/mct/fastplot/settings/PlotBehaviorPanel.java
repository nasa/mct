package gov.nasa.arc.mct.fastplot.settings;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisBounds;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.settings.controls.PlotSettingsCheckBox;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;

public class PlotBehaviorPanel extends PlotSettingsPanel {

	private static final long serialVersionUID = 3317532599862428211L;

	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
                               ResourceBundle.getBundle("gov.nasa.arc.mct.fastplot.view.Bundle");
	
	private static final int BEHAVIOR_CELLS_X_PADDING = 18;
    private static final int INDENTATION_SEMI_FIXED_CHECKBOX = 16;
	private static final int NONTIME_TITLE_SPACING = 0;
	private static final int PADDING_COLUMNS = 3;

	/*
	 * Plot Behavior panel controls
	 */
	private JRadioButton nonTimeMinAutoAdjustMode;
	private JRadioButton nonTimeMaxAutoAdjustMode;
	private JRadioButton nonTimeMinFixedMode;
	private JRadioButton nonTimeMaxFixedMode;
	private JCheckBox nonTimeMinSemiFixedMode;
	private JCheckBox nonTimeMaxSemiFixedMode;
	private JTextField nonTimeMinPadding;
	private JTextField nonTimeMaxPadding;


	private PlotSettingsCheckBox pinTimeAxis;
	private JRadioButton timeJumpMode;
	private JRadioButton timeScrunchMode;
	private JTextField timeJumpPadding;
	private JTextField timeScrunchPadding;

	private JLabel behaviorTimeAxisLetter;
	private JLabel behaviorNonTimeAxisLetter;
	
	public PlotBehaviorPanel() {
        setLayout(new GridBagLayout());
        
        JPanel modePanel = new JPanel(new GridLayout(1, 1));
        JButton bMode = new JButton(BUNDLE.getString("Mode.label"));
        bMode.setAlignmentY(CENTER_ALIGNMENT);
        modePanel.add(bMode);
        JPanel minPanel = new JPanel(new GridLayout(1, 1));
        JLabel bMin = new JLabel(BUNDLE.getString("Min.label"));
        bMin.setHorizontalAlignment(JLabel.CENTER);
        minPanel.add(bMin);
        JPanel maxPanel = new JPanel(new GridLayout(1, 1));
        maxPanel.add(new JLabel(BUNDLE.getString("Max.label")));

        GridLinedPanel timeAxisPanel = createGriddedTimeAxisPanel();
    	GridLinedPanel nonTimeAxisPanel = createGriddedNonTimeAxisPanel();

    	behaviorTimeAxisLetter = new JLabel("_");
    	JPanel behaviorTimeTitlePanel = new JPanel();
    	behaviorTimeTitlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, NONTIME_TITLE_SPACING));
    	behaviorTimeTitlePanel.add(new JLabel(BUNDLE.getString("TimeAxis.label")  + " ("));
    	behaviorTimeTitlePanel.add(behaviorTimeAxisLetter);
    	behaviorTimeTitlePanel.add(new JLabel("):"));
    	pinTimeAxis = new PlotSettingsCheckBox(BUNDLE.getString("PinTimeAxis.label")) {
			private static final long serialVersionUID = 4533604843417685876L;

			@Override
			public boolean getFrom(PlotConfiguration settings) {
				return settings.getPinTimeAxis();
			}

			@Override
			public void populate(PlotConfiguration settings) {
				settings.setPinTimeAxis(isSelected());
			}
    	};
    	behaviorTimeTitlePanel.add(pinTimeAxis);
    	addSubPanel(pinTimeAxis);

    	behaviorNonTimeAxisLetter = new JLabel("_");
    	JPanel behaviorNonTimeTitlePanel = new JPanel();
    	behaviorNonTimeTitlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, NONTIME_TITLE_SPACING));
    	behaviorNonTimeTitlePanel.add(new JLabel(BUNDLE.getString("NonTimeAxis.label")  + " ("));
    	behaviorNonTimeTitlePanel.add(behaviorNonTimeAxisLetter);
    	behaviorNonTimeTitlePanel.add(new JLabel("):"));

    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.anchor = GridBagConstraints.WEST;
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.insets = new Insets(6, 0, 0, 0);
    	add(behaviorTimeTitlePanel, gbc);
    	gbc.gridy++;
    	gbc.insets = new Insets(0, 0, 0, 0);
    	add(timeAxisPanel, gbc);
    	gbc.gridy++;
    	gbc.insets = new Insets(6, 0, 0, 0);
    	add(behaviorNonTimeTitlePanel, gbc);
    	gbc.gridy++;
    	gbc.insets = new Insets(0, 0, 0, 0);
    	add(nonTimeAxisPanel, gbc);

    	// Listen
    	nonTimeMinAutoAdjustMode.addActionListener(this);
    	nonTimeMaxAutoAdjustMode.addActionListener(this);
    	nonTimeMinFixedMode.addActionListener(this);
    	nonTimeMaxFixedMode.addActionListener(this);
    	nonTimeMinSemiFixedMode.addActionListener(this);
    	nonTimeMaxSemiFixedMode.addActionListener(this);
    	nonTimeMinPadding.addActionListener(this);
    	nonTimeMaxPadding.addActionListener(this);    	
    	timeJumpMode.addActionListener(this);
    	timeScrunchMode.addActionListener(this);
    	timeJumpPadding.addActionListener(this);
    	timeScrunchPadding.addActionListener(this);
    	timeJumpPadding.addFocusListener(focusActivator);
    	timeScrunchPadding.addFocusListener(focusActivator);
    	nonTimeMinPadding.addFocusListener(focusActivator);
    	nonTimeMaxPadding.addFocusListener(focusActivator);
    	nonTimeMinPadding.getDocument().addDocumentListener(documentListener); // Apply needs to disable if min+max>99, 
    	nonTimeMaxPadding.getDocument().addDocumentListener(documentListener); // so listen to all changes to both
    	
    	// Instrument
    	setName("plotBehavior");
    	modePanel.setName("modePanel");
    	bMode.setName("bMode");
    	minPanel.setName("minPanel");
    	bMin.setName("bMin");
    	maxPanel.setName("maxPanel");    	
    	timeAxisPanel.setName("timeAxisPanel");
    	nonTimeAxisPanel.setName("nonTimeAxisPanel");
    	behaviorTimeAxisLetter.setName("behaviorTimeAxisLetter");
    	behaviorNonTimeAxisLetter.setName("behaviorNonTimeAxisLetter");
	}
	
	
	
	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsPanel#populate(gov.nasa.arc.mct.fastplot.settings.PlotConfiguration)
	 */
	@Override
	public void populate(PlotConfiguration settings) {
		super.populate(settings);
		
		// TODO: Break other parts down into PlotSettingsSubPanels, too
		settings.setNonTimeMinPadding(Double.parseDouble(nonTimeMinPadding.getText()) / 100d);
		settings.setNonTimeMaxPadding(Double.parseDouble(nonTimeMaxPadding.getText()) / 100d);
		
		if (timeJumpMode.isSelected()) {
			settings.setTimePadding(Double.parseDouble(timeJumpPadding.getText()) / 100d);
			settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.JUMP);
		} else if (timeScrunchMode.isSelected()) {
			settings.setTimePadding(Double.parseDouble(timeScrunchPadding.getText()) / 100d);
			settings.setTimeAxisSubsequentSetting(TimeAxisSubsequentBoundsSetting.SCRUNCH);
		}
		
		if (nonTimeMinSemiFixedMode.isSelected()) {
			settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		} else if (nonTimeMinFixedMode.isSelected()) { 
			settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);			
		} else if (nonTimeMinAutoAdjustMode.isSelected()) {
			settings.setNonTimeAxisSubsequentMinSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		}
		
		if (nonTimeMaxSemiFixedMode.isSelected()) {
			settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
		} else if (nonTimeMaxFixedMode.isSelected()) { 
			settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.FIXED);			
		} else if (nonTimeMaxAutoAdjustMode.isSelected()) {
			settings.setNonTimeAxisSubsequentMaxSetting(NonTimeAxisSubsequentBoundsSetting.AUTO);
		}
	}

	private JToggleButton cachedTimeMode;
	private JToggleButton cachedNonTimeMin;
	private JToggleButton cachedNonTimeMax;
	private double cachedTimePadding;
	private double cachedNonTimeMinPadding;
	private double cachedNonTimeMaxPadding;

	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsPanel#reset(gov.nasa.arc.mct.fastplot.settings.PlotConfiguration, boolean)
	 */
	@Override
	public void reset(PlotConfiguration settings, boolean hard) {
		super.reset(settings, hard);
			
		switch (settings.getAxisOrientationSetting()) {
		case X_AXIS_AS_TIME:
			behaviorTimeAxisLetter.setText("x");
			behaviorNonTimeAxisLetter.setText("y");
			break;
		case Y_AXIS_AS_TIME:
			behaviorTimeAxisLetter.setText("y");
			behaviorNonTimeAxisLetter.setText("x");
			break;
		case Z_AXIS_AS_TIME:
			behaviorTimeAxisLetter.setText("z");
			behaviorNonTimeAxisLetter.setText("x/y");
		}
		
		if (hard) {
			
			switch(settings.getTimeAxisSubsequentSetting()) {
			case JUMP:    timeJumpMode   .setSelected(true); break;
			case SCRUNCH: timeScrunchMode.setSelected(true); break;
			}
			
			switch(settings.getNonTimeAxisSubsequentMinSetting()) {
			case AUTO: 
				nonTimeMinAutoAdjustMode.setSelected(true);
				nonTimeMinSemiFixedMode.setSelected(false);
				nonTimeMinSemiFixedMode.setEnabled(false);
				break;
			case FIXED:
				nonTimeMinFixedMode.setSelected(true);
				nonTimeMinSemiFixedMode.setSelected(false);
				nonTimeMinSemiFixedMode.setEnabled(true);
				break;
			case SEMI_FIXED:
				nonTimeMinFixedMode.setSelected(true);
				nonTimeMinSemiFixedMode.setSelected(true);
				nonTimeMinSemiFixedMode.setEnabled(true);
				break;
			}
			
			switch(settings.getNonTimeAxisSubsequentMaxSetting()) {
			case AUTO: 
				nonTimeMaxAutoAdjustMode.setSelected(true);
				nonTimeMaxSemiFixedMode.setSelected(false);
				nonTimeMaxSemiFixedMode.setEnabled(false);
				break;
			case FIXED:
				nonTimeMaxFixedMode.setSelected(true);
				nonTimeMaxSemiFixedMode.setSelected(false);
				nonTimeMaxSemiFixedMode.setEnabled(true);
				break;
			case SEMI_FIXED:
				nonTimeMaxFixedMode.setSelected(true);
				nonTimeMaxSemiFixedMode.setSelected(true);
				nonTimeMaxSemiFixedMode.setEnabled(true);
				break;
			}

			timeJumpPadding.setText(String.valueOf((int) (settings.getTimePadding() * 100)));
			timeScrunchPadding.setText(String.valueOf((int) (settings.getTimePadding() * 100)));
			nonTimeMinPadding.setText(String.valueOf((int) (settings.getNonTimeMinPadding() * 100)));
			nonTimeMaxPadding.setText(String.valueOf((int) (settings.getNonTimeMaxPadding() * 100)));
			
			
			cacheState();
		}
	}



	/* (non-Javadoc)
	 * @see gov.nasa.arc.mct.fastplot.settings.PlotSettingsPanel#isDirty()
	 */
	@Override
	public boolean isDirty() {
		
		return 		super.isDirty() ||
		            cachedTimeMode    != findSelection(timeJumpMode, timeScrunchMode) ||
		            cachedNonTimeMin  != findSelection(nonTimeMinAutoAdjustMode, nonTimeMinSemiFixedMode, nonTimeMinFixedMode) ||
		            cachedNonTimeMax  != findSelection(nonTimeMaxAutoAdjustMode, nonTimeMaxSemiFixedMode, nonTimeMaxFixedMode) ||
		            cachedTimePadding != Double.parseDouble(
				                   ((cachedTimeMode == timeJumpMode) ? timeJumpPadding : timeScrunchPadding).getText()
		            ) ||
		            cachedNonTimeMinPadding != Double.parseDouble(nonTimeMinPadding.getText()) ||
		            cachedNonTimeMaxPadding != Double.parseDouble(nonTimeMaxPadding.getText());
	}
	
	private void cacheState() {
		cachedTimeMode = findSelection(timeJumpMode, timeScrunchMode);
		cachedNonTimeMin = findSelection(nonTimeMinAutoAdjustMode, nonTimeMinSemiFixedMode, nonTimeMinFixedMode);
		cachedNonTimeMax = findSelection(nonTimeMaxAutoAdjustMode, nonTimeMaxSemiFixedMode, nonTimeMaxFixedMode);
		cachedTimePadding = Double.parseDouble(
				((cachedTimeMode == timeJumpMode) ? timeJumpPadding : timeScrunchPadding).getText()
		);
		cachedNonTimeMinPadding = Double.parseDouble(nonTimeMinPadding.getText());
		cachedNonTimeMaxPadding = Double.parseDouble(nonTimeMaxPadding.getText());
	}
	
	private JToggleButton findSelection (JToggleButton... buttons) {
		for (JToggleButton b : buttons) if (b.isSelected()) return b;
		return null; // TODO: Assert something here?
	}



	// The Time Axis table within the Plot Behavior area
	private GridLinedPanel createGriddedTimeAxisPanel() {
    	JLabel titleMode = new JLabel(BUNDLE.getString("Mode.label"));
    	JLabel titleMin = new JLabel(BUNDLE.getString("Min.label"));
    	JLabel titleMinPadding = new JLabel(BUNDLE.getString("Min.label"));
    	JLabel titleMax = new JLabel(BUNDLE.getString("Max.label"));
    	JLabel titleMaxPadding = new JLabel(BUNDLE.getString("Max.label"));
    	JLabel titleSpan = new JLabel(BUNDLE.getString("Span.label"));
    	JLabel titleMax_Min = new JLabel("(" + BUNDLE.getString("MaxMinusMin.label") +")");
    	JPanel titlePanelSpan = new JPanel();
    	titlePanelSpan.setLayout(new BoxLayout(titlePanelSpan, BoxLayout.Y_AXIS));
    	titlePanelSpan.add(titleSpan);
    	titlePanelSpan.add(titleMax_Min);
    	titleSpan.setAlignmentX(Component.CENTER_ALIGNMENT);
    	titleMax_Min.setAlignmentX(Component.CENTER_ALIGNMENT);
    	JLabel titlePaddingOnRedraw = new JLabel(BUNDLE.getString("PaddingOnRedraw.label"));
        setFontToBold(titleMode);
        setFontToBold(titleMin);
        setFontToBold(titleMax);
        setFontToBold(titleMinPadding);
        setFontToBold(titleMaxPadding);
        setFontToBold(titlePaddingOnRedraw);
        setFontToBold(titleSpan);
        setFontToBold(titleMax_Min);
        
    	timeJumpMode = new JRadioButton(BUNDLE.getString("Jump.label"));
    	timeScrunchMode = new JRadioButton(BUNDLE.getString("Scrunch.label"));
    	JPanel timeJumpModePanel = new JPanel();
    	timeJumpModePanel.add(timeJumpMode);
    	JPanel timeScrunchModePanel = new JPanel();
    	timeScrunchModePanel.add(timeScrunchMode);

    	ButtonGroup modeGroup = new ButtonGroup();
    	modeGroup.add(timeJumpMode);
    	modeGroup.add(timeScrunchMode);

    	timeJumpMode.setSelected(true);

    	timeJumpPadding = createPaddingTextField(AxisType.TIME_IN_JUMP_MODE, AxisBounds.MAX);
    	timeScrunchPadding = createPaddingTextField(AxisType.TIME_IN_SCRUNCH_MODE, AxisBounds.MAX);

    	timeJumpPadding.addFocusListener(new FocusBasedSelector(timeJumpMode));
    	timeScrunchPadding.addFocusListener(new FocusBasedSelector(timeScrunchMode));
    	
    	JPanel timeJumpPaddingPanel = new JPanel();
    	timeJumpPaddingPanel.add(timeJumpPadding);
    	timeJumpPaddingPanel.add(new JLabel(BUNDLE.getString("Percent.label")));

    	JPanel timeScrunchPaddingPanel = new JPanel();
    	timeScrunchPaddingPanel.add(timeScrunchPadding);
    	timeScrunchPaddingPanel.add(new JLabel(BUNDLE.getString("Percent.label")));

    	GridLinedPanel griddedPanel = new GridLinedPanel();
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridwidth = 1;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1;
    	gbc.weighty = 1;
    	gbc.ipadx = BEHAVIOR_CELLS_X_PADDING;
    	gbc.gridheight = 2;
    	griddedPanel.setGBC(gbc);

    	// Title row A
    	int row = 0;
    	griddedPanel.addCell(titleMode, 0, row);
    	griddedPanel.addCell(titleMin, 1, row);
    	griddedPanel.addCell(titleMax, 2, row);
    	gbc.gridheight = 2;
    	griddedPanel.addCell(titlePanelSpan, 3, row);
    	gbc.gridheight = 1;
    	gbc.gridwidth = 2;
    	griddedPanel.addCell(titlePaddingOnRedraw, 4, row);
    	gbc.gridwidth = 1;

    	// Title row B - only two entries
    	row++;
    	griddedPanel.addCell(titleMinPadding, 4, row);
    	griddedPanel.addCell(titleMaxPadding, 5, row);
    	
    	// Row 1
    	row++;
    	griddedPanel.addCell(timeJumpModePanel, 0, row, GridBagConstraints.WEST);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("AutoAdjusts.label")), 1, row);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("AutoAdjusts.label")), 2, row);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("Fixed.label")), 3, row);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("Dash.label")), 4, row);
    	griddedPanel.addCell(timeJumpPaddingPanel, 5, row);

    	// Row 2
    	row++;
    	griddedPanel.addCell(timeScrunchModePanel, 0, row, GridBagConstraints.WEST);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("Fixed.label")), 1, row);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("AutoAdjusts.label")), 2, row);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("AutoAdjusts.label")), 3, row);
    	griddedPanel.addCell(new JLabel(BUNDLE.getString("Dash.label")), 4, row);
    	griddedPanel.addCell(timeScrunchPaddingPanel, 5, row);

		return griddedPanel;
	}
	
	// The Non-Time Axis table within the Plot Behavior panel
	private GridLinedPanel createGriddedNonTimeAxisPanel() {
    	JLabel titleMin = new JLabel(BUNDLE.getString("Min.label"));
    	JLabel titleMax = new JLabel(BUNDLE.getString("Max.label"));
    	JLabel titlePadding = new JLabel(BUNDLE.getString("Padding.label"));
    	JLabel titleMinPadding = new JLabel(BUNDLE.getString("Min.label"));
    	JLabel titleMaxPadding = new JLabel(BUNDLE.getString("Max.label"));
        setFontToBold(titleMin);
        setFontToBold(titleMax);
        setFontToBold(titlePadding);
        setFontToBold(titleMinPadding);
        setFontToBold(titleMaxPadding);

    	nonTimeMinAutoAdjustMode = new JRadioButton(BUNDLE.getString("AutoAdjusts.label"));
    	nonTimeMaxAutoAdjustMode = new JRadioButton(BUNDLE.getString("AutoAdjusts.label"));
    	nonTimeMinFixedMode = new JRadioButton(BUNDLE.getString("Fixed.label"));
    	nonTimeMaxFixedMode = new JRadioButton(BUNDLE.getString("Fixed.label"));

    	JPanel nonTimeMinAutoAdjustModePanel = new JPanel();
    	nonTimeMinAutoAdjustModePanel.add(nonTimeMinAutoAdjustMode);
    	JPanel nonTimeMaxAutoAdjustModePanel = new JPanel();
    	nonTimeMaxAutoAdjustModePanel.add(nonTimeMaxAutoAdjustMode);
    	JPanel nonTimeMinFixedModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    	nonTimeMinFixedModePanel.add(nonTimeMinFixedMode);
    	JPanel nonTimeMaxFixedModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    	nonTimeMaxFixedModePanel.add(nonTimeMaxFixedMode);

    	nonTimeMinAutoAdjustMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nonTimeMinSemiFixedMode.setEnabled(false);
				nonTimeMinSemiFixedMode.setSelected(false);
			}
    	});

    	nonTimeMinFixedMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			   if(nonTimeMinFixedMode.isSelected()) {
				nonTimeMinSemiFixedMode.setEnabled(true); 
			   } else {
				   nonTimeMinSemiFixedMode.setEnabled(false);
				   nonTimeMinSemiFixedMode.setSelected(false);
			   }
			}
		});

    	nonTimeMaxAutoAdjustMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nonTimeMaxSemiFixedMode.setEnabled(false);
				nonTimeMaxSemiFixedMode.setSelected(false);
			}
    	});

    	nonTimeMaxFixedMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				   if(nonTimeMaxFixedMode.isSelected()) {
						nonTimeMaxSemiFixedMode.setEnabled(true); 
					} else {
						   nonTimeMaxSemiFixedMode.setEnabled(false);
						   nonTimeMaxSemiFixedMode.setSelected(false);
					}
			}
		});

    	nonTimeMinAutoAdjustMode.setSelected(true);
    	nonTimeMaxAutoAdjustMode.setSelected(true);
    	
    	ButtonGroup minGroup = new ButtonGroup();
    	minGroup.add(nonTimeMinAutoAdjustMode);
    	minGroup.add(nonTimeMinFixedMode);
    	ButtonGroup maxGroup = new ButtonGroup();
    	maxGroup.add(nonTimeMaxAutoAdjustMode);
    	maxGroup.add(nonTimeMaxFixedMode);

    	nonTimeMinSemiFixedMode = new JCheckBox(BUNDLE.getString("SemiFixed.label"));
    	nonTimeMaxSemiFixedMode = new JCheckBox(BUNDLE.getString("SemiFixed.label"));
    	JPanel nonTimeMinSemiFixedModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
    	JPanel nonTimeMaxSemiFixedModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
    	nonTimeMinSemiFixedModePanel.add(nonTimeMinSemiFixedMode);
    	nonTimeMaxSemiFixedModePanel.add(nonTimeMaxSemiFixedMode);

    	nonTimeMinSemiFixedMode.setEnabled(false);
    	nonTimeMaxSemiFixedMode.setEnabled(false);
    	
    	nonTimeMinPadding = createPaddingTextField(AxisType.NON_TIME, AxisBounds.MIN);
    	nonTimeMaxPadding = createPaddingTextField(AxisType.NON_TIME, AxisBounds.MAX);    	
    	
    	JPanel nonTimeMinPaddingPanel = new JPanel();
    	nonTimeMinPaddingPanel.add(nonTimeMinPadding);
    	nonTimeMinPaddingPanel.add(new JLabel(BUNDLE.getString("Percent.label")));

    	JPanel nonTimeMaxPaddingPanel = new JPanel();
    	nonTimeMaxPaddingPanel.add(nonTimeMaxPadding);
    	nonTimeMaxPaddingPanel.add(new JLabel(BUNDLE.getString("Percent.label")));

    	JPanel nonTimeMins = new JPanel();
    	nonTimeMins.setLayout(new GridBagLayout());
    	GridBagConstraints gbc0 = new GridBagConstraints();
    	gbc0.gridy = 0;
    	gbc0.anchor = GridBagConstraints.WEST;
    	nonTimeMins.add(nonTimeMinAutoAdjustModePanel, gbc0);

    	gbc0.gridy = 1;
    	nonTimeMins.add(nonTimeMinFixedModePanel, gbc0);
    	gbc0.gridy = 2;
		gbc0.insets = new Insets(0, INDENTATION_SEMI_FIXED_CHECKBOX, 0, 0);
    	nonTimeMins.add(nonTimeMinSemiFixedModePanel, gbc0);

    	JPanel nonTimeMaxs = new JPanel();
    	nonTimeMaxs.setLayout(new GridBagLayout());
    	GridBagConstraints gbc1 = new GridBagConstraints();
    	gbc1.gridy = 0;
    	gbc1.anchor = GridBagConstraints.WEST;
    	nonTimeMaxs.add(nonTimeMaxAutoAdjustModePanel, gbc1);
    	gbc1.gridy = 1;
    	nonTimeMaxs.add(nonTimeMaxFixedModePanel, gbc1);
    	gbc1.gridy = 2;
    	gbc1.insets = new Insets(0, INDENTATION_SEMI_FIXED_CHECKBOX, 0, 0);
    	nonTimeMaxs.add(nonTimeMaxSemiFixedModePanel, gbc1);

    	GridLinedPanel griddedPanel = new GridLinedPanel();
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1;
    	gbc.weighty = 1;
    	gbc.ipadx = BEHAVIOR_CELLS_X_PADDING;
    	griddedPanel.setGBC(gbc);

    	// Title row A
    	int row = 0;
    	gbc.gridwidth = 1;
    	gbc.gridheight = 2; // First 2 titles are 2 rows high
    	griddedPanel.addCell(titleMin, 1, row);
    	griddedPanel.addCell(titleMax, 2, row);
    	gbc.gridwidth = 2; // "Padding" spans 2 columns, 1 row high
    	gbc.gridheight = 1;
    	griddedPanel.addCell(titlePadding, 3, row);
    	gbc.gridwidth = 1;

    	// Title row B - only 2 cells occupied
    	row++;
    	griddedPanel.addCell(titleMinPadding, 3, row);
    	griddedPanel.addCell(titleMaxPadding, 4, row);

    	// Row 1
    	row++;
    	griddedPanel.addCell(nonTimeMins, 1, row, GridBagConstraints.WEST);
    	griddedPanel.addCell(nonTimeMaxs, 2, row, GridBagConstraints.WEST);
    	griddedPanel.addCell(nonTimeMinPaddingPanel, 3, row);
    	griddedPanel.addCell(nonTimeMaxPaddingPanel, 4, row);

    	// Instrument
    	nonTimeMins.setName("nonTimeMins");
    	nonTimeMaxs.setName("nonTimeMaxs");

    	return griddedPanel;
	}
	
    @SuppressWarnings("serial")
	private JTextField createPaddingTextField(AxisType axisType, AxisBounds bound) {
    	final JFormattedTextField tField = new JFormattedTextField(new InternationalFormatter(
    			NumberFormat.getIntegerInstance()) {
    				protected DocumentFilter getDocumentFilter() {
    					return filter;
    				}
    				private DocumentFilter filter = new PaddingFilter();
    			});
    	tField.setColumns(PADDING_COLUMNS);
    	tField.setHorizontalAlignment(JTextField.RIGHT);
    	if (bound.equals(AxisBounds.MIN)) {
    		tField.setText(axisType.getMinimumDefaultPaddingAsText());
    	} else {
    		tField.setText(axisType.getMaximumDefaultPaddingAsText());
    	}
    	
    	tField.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
			  tField.selectAll();
			  tField.removeAncestorListener(this);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
				
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				
			}
    		
    	});
		return tField;
	}
	
	private void setFontToBold(JLabel item) {
        item.setFont(item.getFont().deriveFont(Font.BOLD));
    }

	class FocusBasedSelector implements FocusListener {
		private JRadioButton button;
		
		private FocusBasedSelector(JRadioButton button) {
			this.button = button;
		}

		@Override
		public void focusGained(FocusEvent arg0) {
			button.doClick();
		}

		@Override
		public void focusLost(FocusEvent arg0) {			
		}		
	}
	
	private final FocusListener focusActivator = new FocusListener() {
		@Override
		public void focusGained(FocusEvent arg0) {
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			Object o = arg0.getSource();
			if (o instanceof JTextField) {
				actionPerformed(null);				
			}
		}		
	};       
	
	private final DocumentListener documentListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			fireCallbacks();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			fireCallbacks();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			if (arg0.getDocument().getLength() > 0) fireCallbacks();
		}
    };
	
	/*
	 * This filter blocks non-numeric characters from being entered in the padding fields
	 */
	class PaddingFilter extends DocumentFilter {
		private StringBuilder insertBuilder;
		private StringBuilder replaceBuilder;

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			insertBuilder = new StringBuilder(string);
			for (int k = insertBuilder.length() - 1; k >= 0; k--) {
				int cp = insertBuilder.codePointAt(k);
				if (! Character.isDigit(cp)) {
					insertBuilder.deleteCharAt(k);
					if (Character.isSupplementaryCodePoint(cp)) {
						k--;
						insertBuilder.deleteCharAt(k);
					}
				}
			}
			if (insertBuilder.length() + fb.getDocument().getLength() < 3 &&
					insertBuilder.length() + fb.getDocument().getLength() > 0) {
				super.insertString(fb, offset, insertBuilder.toString(), attr);
			}
		}


		
		@Override
		public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attr)
				throws BadLocationException {
			replaceBuilder = new StringBuilder(string);
			for (int k = replaceBuilder.length() - 1; k >= 0; k--) {
				int cp = replaceBuilder.codePointAt(k);
				if (! Character.isDigit(cp)) {
					replaceBuilder.deleteCharAt(k);
					if (Character.isSupplementaryCodePoint(cp)) {
						k--;
						replaceBuilder.deleteCharAt(k);
					}
				}
			}
			if ((replaceBuilder.length() - length + fb.getDocument().getLength()) < 3) {
				if ((replaceBuilder.length() - length + fb.getDocument().getLength()) == 0) {
					super.replace(fb, offset, length, "0", attr);
				} else {
					super.replace(fb, offset, length, replaceBuilder.toString(), attr);
				}
			}
		}
		
		@Override
		public void remove(FilterBypass fb,
                int offset,
                int length)
         throws BadLocationException {
			if (fb.getDocument().getLength() - length >= 1) {
				super.remove(fb, offset, length);
			} else {
				super.replace(fb, 0, fb.getDocument().getLength(), "0", null);
			}
		}

		StringBuilder getInsertBuilder() {
			return insertBuilder;
		}

		StringBuilder getReplaceBuilder() {
			return replaceBuilder;
		}
	}
	
	private static class GridLinedPanel extends JPanel {
		private static final long serialVersionUID = -1227455333903006294L;
		private GridBagConstraints wrapGbc;

    	public GridLinedPanel() {
    		setLayout(new GridBagLayout());
    		setBorder(BorderFactory.createLineBorder(Color.gray));
    	}

    	void setGBC(GridBagConstraints inputGbc) {
    		wrapGbc = inputGbc;
    	}

    	// Wrap each added ui control in a JPanel with a border
    	void addCell(JLabel uiControl, int xPosition, int yPosition) {
        	uiControl.setHorizontalAlignment(JLabel.CENTER);
        	wrapControlInPanel(uiControl, xPosition, yPosition);
    	}

    	// Wrap each added ui control in a JPanel with a border
    	void addCell(JPanel uiControl, int xPosition, int yPosition) {
        	wrapControlInPanel(uiControl, xPosition, yPosition);
    	}

		private void wrapControlInPanel(JComponent uiControl, int xPosition,
				int yPosition) {
			JPanel wrapperPanel = new JPanel();

			wrapperPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			wrapperPanel.add(uiControl, gbc);

			wrapGbc.gridx = xPosition;
        	wrapGbc.gridy = yPosition;
        	wrapperPanel.setBorder(new LineBorder(Color.lightGray));
        	add(wrapperPanel, wrapGbc);
		}

		private void addCell(JComponent uiControl, int xPosition,
				int yPosition, int alignment) {
			JPanel wrapperPanel = new JPanel(new GridBagLayout());
        	wrapperPanel.setBorder(new LineBorder(Color.lightGray));

        	GridBagConstraints gbc = new GridBagConstraints();
			if (alignment == GridBagConstraints.WEST) {
				gbc.weightx = 1;
				gbc.anchor = GridBagConstraints.WEST;
			}
			wrapperPanel.add(uiControl, gbc);

			wrapGbc.gridx = xPosition;
        	wrapGbc.gridy = yPosition;
        	add(wrapperPanel, wrapGbc);
		}
    }


	@Override
	public boolean isValidated() {
		return Double.parseDouble(nonTimeMinPadding.getText()) + Double.parseDouble(nonTimeMaxPadding.getText()) < 100.0 &&
			super.isValidated();
	}
}
