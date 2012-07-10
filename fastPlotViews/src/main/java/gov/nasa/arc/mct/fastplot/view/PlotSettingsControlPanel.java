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
package gov.nasa.arc.mct.fastplot.view;

import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisBounds;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisOrientationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.AxisType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.NonTimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineConnectionType;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.PlotLineDrawingFlags;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.TimeAxisSubsequentBoundsSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.XAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants.YAxisMaximumLocationSetting;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;
import gov.nasa.arc.mct.fastplot.utils.TimeFormatUtils;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.util.LafColor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the UI for the Plot Configuration Panel
 */

public class PlotSettingsControlPanel extends JPanel {
	private static final long serialVersionUID = 6158825155688815494L;
	
	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
                               ResourceBundle.getBundle(PlotSettingsControlPanel.class.getName().substring(0, 
        		                                        PlotSettingsControlPanel.class.getName().lastIndexOf("."))+".Bundle");
    
    private final static Logger logger = LoggerFactory.getLogger(PlotSettingsControlPanel.class);

	private static final String MANUAL_LABEL = BUNDLE.getString("Manual.label");

    private static final int INTERCONTROL_HORIZONTAL_SPACING = 0; 
    private static final int INDENTATION_SEMI_FIXED_CHECKBOX = 16;

	private static final int NONTIME_TITLE_SPACING = 0;
	private static final int Y_SPAN_SPACING = 50;


    private static final int INNER_PADDING = 5;
    private static final int INNER_PADDING_TOP = 5;

    private static final int X_AXIS_TYPE_VERTICAL_SPACING = 2;
	private static final Border TOP_PADDED_MARGINS = BorderFactory.createEmptyBorder(5, 0, 0, 0);
	private static final Border BOTTOM_PADDED_MARGINS = BorderFactory.createEmptyBorder(0, 0, 2, 0);
	private static final int BEHAVIOR_CELLS_X_PADDING = 18;

	private static final Border CONTROL_PANEL_MARGINS = BorderFactory.createEmptyBorder(INNER_PADDING_TOP, INNER_PADDING, INNER_PADDING, INNER_PADDING);
	private static final Border SETUP_AND_BEHAVIOR_MARGINS = BorderFactory.createEmptyBorder(0, INNER_PADDING, INNER_PADDING, INNER_PADDING);

	// Stabilize width of panel on left of the static plot image
	private static final Dimension Y_AXIS_BUTTONS_PANEL_PREFERRED_SIZE = new Dimension(250, 0);

	private static final Double NONTIME_AXIS_SPAN_INIT_VALUE = Double.valueOf(30);

	private static final int JTEXTFIELD_COLS = 8;
	private static final int NUMERIC_TEXTFIELD_COLS1 = 12;

	private static final DecimalFormat nonTimePaddingFormat = new DecimalFormat("###.###");
	private static final DecimalFormat timePaddingFormat = new DecimalFormat("##.###");
	private static final String DATE_FORMAT = "D/HH:mm:ss";

	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

	static GregorianCalendar ZERO_TIME_SPAN_CALENDAR = new GregorianCalendar();
	static {
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.DAY_OF_YEAR, 1);
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.HOUR_OF_DAY, 0);
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.MINUTE, 0);
		ZERO_TIME_SPAN_CALENDAR.set(Calendar.SECOND, 0);
	}
	private static Date ZERO_TIME_SPAN_DATE = new Date();
	static {
		// Sets value to current Year and time zone
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		ZERO_TIME_SPAN_DATE.setTime(cal.getTimeInMillis());
	}

	// Space between paired controls (e.g., label followed by text field)
	private static final int SPACE_BETWEEN_ROW_ITEMS = 3;

	private static final Color OK_PANEL_BACKGROUND_COLOR = LafColor.WINDOW_BORDER.darker();

	private static final int PADDING_COLUMNS = 3;

	// Maintain link to the plot view this panel is supporting.
	private PlotViewManifestation plotViewManifestion;
	
	// Maintain link to the controller this panel is calling to persist setting and create the plot
	PlotSettingController controller;

	// Non-Time Axis Maximums panel
	NonTimeAxisMaximumsPanel nonTimeAxisMaximumsPanel;

	// Non-Time Axis Minimums panel
	NonTimeAxisMinimumsPanel nonTimeAxisMinimumsPanel;

	//Time Axis Minimums panel
	public TimeAxisMinimumsPanel timeAxisMinimumsPanel;

	//Time Axis Maximums panel
	public TimeAxisMaximumsPanel timeAxisMaximumsPanel;

	// Top panel controls: Manipulate controls around static plot image
	JComboBox timeSystemDropdown;
    private JComboBox timeFormatDropdown;
	JRadioButton xAxisAsTimeRadioButton;
    JRadioButton yAxisAsTimeRadioButton;
    private JRadioButton yMaxAtTop;
    private JRadioButton yMaxAtBottom;
    private JRadioButton xMaxAtRight;
    private JRadioButton xMaxAtLeft;
    private JCheckBox groupByCollection;
   
	//=========================================================================
	/*
	 * X Axis panels
	 */
	XAxisSpanCluster xAxisSpanCluster;

	XAxisAdjacentPanel xAxisAdjacentPanel;

	// Join minimums and maximums panels
	XAxisButtonsPanel xAxisButtonsPanel;

    JLabel xMinLabel;
    JLabel xMaxLabel;

    private JLabel xAxisType;

	//=========================================================================
	/*
	 * Y Axis panels
	 */
    YMaximumsPlusPanel yMaximumsPlusPanel;
    YAxisSpanPanel yAxisSpanPanel;
    YMinimumsPlusPanel yMinimumsPlusPanel;
	private YAxisButtonsPanel yAxisButtonsPanel;

    private JLabel yAxisType;

	/*
	 * Time Axis fields
	 */
    JRadioButton timeAxisMaxAuto;
    ParenthesizedTimeLabel timeAxisMaxAutoValue;
    JRadioButton timeAxisMaxManual;
    TimeTextField timeAxisMaxManualValue;

    JRadioButton timeAxisMinAuto;
    ParenthesizedTimeLabel timeAxisMinAutoValue;
    JRadioButton timeAxisMinManual;
    TimeTextField timeAxisMinManualValue;
    
    TimeSpanTextField timeSpanValue;

	public JRadioButton timeAxisMinCurrent;
	public JRadioButton timeAxisMaxCurrent;
	public ParenthesizedTimeLabel timeAxisMinCurrentValue;
	public ParenthesizedTimeLabel timeAxisMaxCurrentValue;

    /*
     * Non-time Axis fields
     */
    JRadioButton nonTimeAxisMaxCurrent;
    ParenthesizedNumericLabel nonTimeAxisMaxCurrentValue;
    JRadioButton nonTimeAxisMaxManual;
    NumericTextField nonTimeAxisMaxManualValue;
    JRadioButton nonTimeAxisMaxAutoAdjust;
    ParenthesizedNumericLabel nonTimeAxisMaxAutoAdjustValue;

    JRadioButton nonTimeAxisMinCurrent;
    ParenthesizedNumericLabel nonTimeAxisMinCurrentValue;
    JRadioButton nonTimeAxisMinManual;
    NumericTextField nonTimeAxisMinManualValue;
    JRadioButton nonTimeAxisMinAutoAdjust;
	ParenthesizedNumericLabel nonTimeAxisMinAutoAdjustValue;

    NumericTextField nonTimeSpanValue;

	/*
	 * Plot Behavior panel controls
	 */
	JRadioButton nonTimeMinAutoAdjustMode;
	JRadioButton nonTimeMaxAutoAdjustMode;
	JRadioButton nonTimeMinFixedMode;
	JRadioButton nonTimeMaxFixedMode;
	JCheckBox nonTimeMinSemiFixedMode;
	JCheckBox nonTimeMaxSemiFixedMode;
	JTextField nonTimeMinPadding;
	JTextField nonTimeMaxPadding;


	JCheckBox pinTimeAxis;
	JRadioButton timeJumpMode;
	JRadioButton timeScrunchMode;
	JTextField timeJumpPadding;
	JTextField timeScrunchPadding;
	

	/*
	 * Plot line setup panel controls
	 */
	private JLabel       drawLabel;
	private JRadioButton linesOnly;
	private JRadioButton markersAndLines;
	private JRadioButton markersOnly;
	
	private JLabel       connectionLineTypeLabel;
	private JRadioButton direct;
	private JRadioButton step;
	
	
	
	private StillPlotImagePanel imagePanel;

	private JLabel behaviorTimeAxisLetter;
	private JLabel behaviorNonTimeAxisLetter;

	private GregorianCalendar recycledCalendarA = new GregorianCalendar();
	private GregorianCalendar recycledCalendarB = new GregorianCalendar();

	private JButton okButton;
	private JButton resetButton;

	// Saved Settings of Plot Settings Panel controls. Used to affect Apply and Reset buttons
	private Object ssWhichAxisAsTime;
	private String ssTimeSystemSelection;
    private String ssTimeFormatSelection;
	private Object ssXMaxAtWhich;
	private Object ssYMaxAtWhich;
	private Object ssTimeMin;
	private Object ssTimeMax;
	private Object ssNonTimeMin;
	private Object ssNonTimeMax;

	private Object ssTimeAxisMode;
	private Object ssPinTimeAxis;
	private List<JToggleButton> ssNonTimeMinAxisMode = new ArrayList<JToggleButton>(2);
	private List<JToggleButton> ssNonTimeMaxAxisMode = new ArrayList<JToggleButton>(2);
	private String ssNonTimeMinPadding;
	private String ssNonTimeMaxPadding;
	private String ssTimeAxisJumpMaxPadding;
	private String ssTimeAxisScrunchMaxPadding;
	
	private Object ssDraw;
	private Object ssConnectionLineType;
	
	// Initialize this to avoid nulls on persistence
	private long ssTimeMinManualValue = 0L;
	private long ssTimeMaxManualValue = 0L;
	private String ssNonTimeMinManualValue = "0.0";
	private String ssNonTimeMaxManualValue = "1.0";

	private boolean timeMinManualHasBeenSelected = false;
	private boolean timeMaxManualHasBeenSelected = false;

	private boolean ssGroupByCollection = false;
	
	private SimpleDateFormat staticAreaTimeFormat = null;

	//===================================================================================

	public static class CalendarDump {
		public static String dumpDateAndTime(GregorianCalendar calendar) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(calendar.get(Calendar.YEAR) + " - ");
			buffer.append(calendar.get(Calendar.DAY_OF_YEAR) + "/");
			buffer.append(calendar.get(Calendar.HOUR_OF_DAY) + ":");
			buffer.append(calendar.get(Calendar.MINUTE) + ":");
			buffer.append(calendar.get(Calendar.SECOND));
			buffer.append(", Zone " + (calendar.get(Calendar.ZONE_OFFSET) / (3600 * 1000)));
			return buffer.toString();
		}

		public static String dumpMillis(long timeInMillis) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(timeInMillis);
			return dumpDateAndTime(cal);
		}
	}

	class ParenthesizedTimeLabel extends JLabel {
		private static final long serialVersionUID = -6004293775277749905L;

		private GregorianCalendar timeInMillis;

		private JRadioButton companionButton;
		
		public ParenthesizedTimeLabel(JRadioButton button) {
						
			companionButton = button;
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					companionButton.setSelected(true);
					companionButton.requestFocusInWindow();
					updateMainButtons();
				}
			});
		}

		public void setTime(GregorianCalendar inputTime) {
			timeInMillis = inputTime;
			setText("(" + getStaticAreaTimeFormat().format(inputTime.getTime()) + ")");		
		}

		public GregorianCalendar getCalendar() {
			return timeInMillis;
		}

		public long getTimeInMillis() {
			return timeInMillis.getTimeInMillis();
		}

		public String getSavedTime() {
			return CalendarDump.dumpDateAndTime(timeInMillis);
		}

		public int getSecond() {
			return timeInMillis.get(Calendar.SECOND);
		}

		public int getMinute() {
			return timeInMillis.get(Calendar.MINUTE);
		}

		public int getHourOfDay() {
			return timeInMillis.get(Calendar.HOUR_OF_DAY);
		}
	}

	class ParenthesizedNumericLabel extends JLabel {
		private static final long serialVersionUID = 3403375470853249483L;
		private JRadioButton companionButton;

		public ParenthesizedNumericLabel(JRadioButton button) {
			
			companionButton = button;
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					companionButton.setSelected(true);
					companionButton.requestFocusInWindow();
					updateMainButtons();
				}
			});
		}

		public Double getValue() {
			String data = getText();
			if (data == null) {
				return null;
			}
			if (data.length() < 3) {
				logger.error("Numeric label in plot settings contained invalid content [" + data + "]");
				return null;
			}
			Double result = null;
			try {
				result = Double.parseDouble(data.substring(1, data.length() - 1));
				
			} catch(NumberFormatException e) {
				logger.error("Could not parse numeric value from ["+ data.substring(1, data.length() - 1) + "]");
			}
			return result;
		}

		public void setValue(Double input) {
			
			String formatNum = nonTimePaddingFormat.format(input);
			
			if ( (input.doubleValue() >= PlotConstants.MILLION_VALUES) || (input.doubleValue() <= PlotConstants.NEGATIVE_MILLION_VALUES) ) {
                formatNum = PlotterPlot.getNumberFormatter(input.doubleValue()).format(input.doubleValue());
			}

			if (formatNum.equals("0"))
				formatNum = "0.0";
			
			if (formatNum.equals("1"))
				formatNum = "1.0";
			
			setText("(" + formatNum + ")");
		}
	}

	/*
	 * Focus listener for the Time axis Manual and Span fields
	 */
	class TimeFieldFocusListener extends FocusAdapter {
		// This class can be used with a null button
		private JRadioButton companionButton;

		public TimeFieldFocusListener(JRadioButton assocButton) {			
			companionButton = assocButton;
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (e.isTemporary())
				return;
			if (companionButton != null) {
				companionButton.setSelected(true);
			}
			updateMainButtons();
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (e.isTemporary())
				return;
			try {
				timeSpanValue.commitEdit(); 
			} catch (ParseException exception) {
				exception.printStackTrace();
			}

			updateTimeAxisControls();
		}
	}

	/*
	 * Common action listener for the Time axis Mode buttons
	 */
	class TimeAxisModeListener implements ActionListener {
		private JTextComponent companionField;

		public TimeAxisModeListener(JTextComponent field) {
			companionField = field;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			updateMainButtons();
			String content = companionField.getText();
			companionField.requestFocusInWindow();
			companionField.setSelectionStart(0);
			if (content != null) {
				companionField.setSelectionEnd(content.length());
			}
		}
	}

	/*
	 * Focus listener for the Time Padding fields
	 */
	class TimePaddingFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			if (e.isTemporary())
				return;
			updateTimeAxisControls();
		}
	}

	class NonTimeFieldFocusListener extends FocusAdapter {
		private JRadioButton companionButton;

		public NonTimeFieldFocusListener(JRadioButton assocButton) {
			companionButton = assocButton;
		}

		@Override
		public void focusGained(FocusEvent e) {
			if (e.isTemporary())
				return;
			companionButton.setSelected(true);
			updateNonTimeAxisControls();
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (e.isTemporary())
				return;

			updateNonTimeAxisControls();
		}
	}

	/*
	 * Guide to the inner classes implementing the movable panels next to the static plot image
	 * 
	 * XAxisAdjacentPanel - Narrow panel just below X axis
	 *     XAxisSpanCluster - child panel
	 * XAxisButtonsPanel - Main panel below X axis
	 *     minimumsPanel - child
	 *     maximumsPanel - child
	 * 
	 * YAxisButtonsPanel - Main panel to left of Y axis
	 *     YMaximumsPlusPanel - child panel
	 *     YSpanPanel - child
	 *     YMinimumsPlusPanel - child
	 *
	 */


	// Panel holding the Y Axis Span controls
	class YAxisSpanPanel extends JPanel {
		private static final long serialVersionUID = 6888092349514542052L;
		private JLabel ySpanTag;
		private JComponent spanValue;
		private Component boxGlue;
		private Component boxOnRight;

		public YAxisSpanPanel() {
	        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			nonTimePaddingFormat.setParseIntegerOnly(false);
			nonTimeSpanValue = new NumericTextField(NUMERIC_TEXTFIELD_COLS1, nonTimePaddingFormat);
			nonTimeSpanValue.setColumns(JTEXTFIELD_COLS);
			nonTimeSpanValue.setValue(NONTIME_AXIS_SPAN_INIT_VALUE);

	        spanValue = nonTimeSpanValue;

	        ySpanTag = new JLabel("Span: ");
	        boxGlue = Box.createHorizontalGlue();
	        boxOnRight = Box.createRigidArea(new Dimension(Y_SPAN_SPACING, 0));

	        layoutPanel();

	        // Instrument
	        ySpanTag.setName("ySpanTag");
		}

		void layoutPanel() {
			removeAll();
			add(boxGlue);
	        add(ySpanTag);
            add(spanValue);
	        add(boxOnRight);
		}

		void setSpanField(JComponent field) {
			spanValue = field;
			layoutPanel();
		}
	}

	// Panel holding the combined "Min" label and Y Axis minimums panel
	class YMinimumsPlusPanel extends JPanel {
		private static final long serialVersionUID = 2995723041366974233L;

		private JPanel coreMinimumsPanel;
		private JLabel minJLabel = new JLabel("Min");

		public YMinimumsPlusPanel() {
	        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	        minJLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            minJLabel.setFont(minJLabel.getFont().deriveFont(Font.BOLD));

            nonTimeAxisMinimumsPanel = new NonTimeAxisMinimumsPanel();
            nonTimeAxisMinimumsPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            minJLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);

            add(nonTimeAxisMinimumsPanel);
	        add(minJLabel);

	        // Instrument
	        minJLabel.setName("minJLabel");
		}

		public void setPanel(JPanel minPanel) {
			coreMinimumsPanel = minPanel;
			removeAll();
	        add(coreMinimumsPanel);
	        add(minJLabel);
	        revalidate();
		}

		public void setAxisTagAlignment(float componentAlignment) {
			coreMinimumsPanel.setAlignmentY(componentAlignment);
			minJLabel.setAlignmentY(componentAlignment);
		}
	}

	// Panel holding the combined "Max" label and Y Axis maximums panel
	class YMaximumsPlusPanel extends JPanel {
		private static final long serialVersionUID = -7611052255395258026L;

		private JPanel coreMaximumsPanel;
		private JLabel maxJLabel = new JLabel("Max");

		public YMaximumsPlusPanel() {
	        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	        maxJLabel.setAlignmentY(Component.TOP_ALIGNMENT);
	        maxJLabel.setFont(maxJLabel.getFont().deriveFont(Font.BOLD));

	        nonTimeAxisMaximumsPanel = new NonTimeAxisMaximumsPanel();
	        nonTimeAxisMaximumsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
	        maxJLabel.setAlignmentY(Component.TOP_ALIGNMENT);
	        add(nonTimeAxisMaximumsPanel);
	        add(maxJLabel);

	        // Instrument
	        maxJLabel.setName("maxJLabel");
		}

		public void setPanel(JPanel maxPanel) {
			coreMaximumsPanel = maxPanel;
			removeAll();
	        add(coreMaximumsPanel);
	        add(maxJLabel);
	        revalidate();
		}

		public void setAxisTagAlignment(float componentAlignment) {
			coreMaximumsPanel.setAlignmentY(componentAlignment);
			maxJLabel.setAlignmentY(componentAlignment);
			revalidate();
		}
	}

	// Panel holding the Time Axis minimum controls
	class TimeAxisMinimumsPanel extends JPanel {
		private static final long serialVersionUID = 3651502189841560982L;

		public TimeAxisMinimumsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			timeAxisMinCurrent = new JRadioButton(BUNDLE.getString("Currentmin.label"));
			timeAxisMinCurrentValue = new ParenthesizedTimeLabel(timeAxisMinCurrent);
			timeAxisMinCurrentValue.setTime(new GregorianCalendar());

		    timeAxisMinManual = new JRadioButton(MANUAL_LABEL);
	        MaskFormatter formatter = null;
			try {
				formatter = new MaskFormatter("###/##:##:##");
				formatter.setPlaceholderCharacter('0');
			} catch (ParseException e) {
				logger.error("Parse error in creating time field", e);
			}

		    timeAxisMinManualValue = new TimeTextField(formatter);

		    timeAxisMinAuto = new JRadioButton(BUNDLE.getString("Now.label"));
		    timeAxisMinAutoValue = new ParenthesizedTimeLabel(timeAxisMinAuto);
			timeAxisMinAutoValue.setTime(new GregorianCalendar());
		    timeAxisMinAutoValue.setText("should update every second");

		    timeAxisMinAuto.setSelected(true);

		    JPanel yAxisMinRow1 = createMultiItemRow(timeAxisMinCurrent, timeAxisMinCurrentValue);
	        JPanel yAxisMinRow2 = createMultiItemRow(timeAxisMinManual, timeAxisMinManualValue);
		    JPanel yAxisMinRow3 = createMultiItemRow(timeAxisMinAuto, timeAxisMinAutoValue);

	        add(yAxisMinRow1);
	        add(yAxisMinRow2);
	        add(yAxisMinRow3);
	        add(Box.createVerticalGlue());

	        ButtonGroup minButtonGroup = new ButtonGroup();
	        minButtonGroup.add(timeAxisMinCurrent);
	        minButtonGroup.add(timeAxisMinManual);
	        minButtonGroup.add(timeAxisMinAuto);

	        timeAxisMinAuto.setToolTipText(BUNDLE.getString("TimeAxisMins.label"));

		}
	}

	// Panel holding the Time Axis maximum controls
	class TimeAxisMaximumsPanel extends JPanel {
		private static final long serialVersionUID = 6105026690366452860L;

		public TimeAxisMaximumsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			timeAxisMaxCurrent = new JRadioButton(BUNDLE.getString("CurrentMax.label"));
			timeAxisMaxCurrentValue = new ParenthesizedTimeLabel(timeAxisMaxCurrent);
			GregorianCalendar initCalendar = new GregorianCalendar();
			initCalendar.add(Calendar.MINUTE, 10);
			timeAxisMaxCurrentValue.setTime(initCalendar);

			timeAxisMaxManual = new JRadioButton(MANUAL_LABEL);

			MaskFormatter formatter = null;
			try {
				formatter = new MaskFormatter("###/##:##:##");
				formatter.setPlaceholderCharacter('0');
			} catch (ParseException e) {
				e.printStackTrace();
			}
		    timeAxisMaxManualValue = new TimeTextField(formatter);
		    initCalendar.setTimeInMillis(timeAxisMaxManualValue.getValueInMillis() + 1000);
		    timeAxisMaxManualValue.setTime(initCalendar);
		    
			timeAxisMaxAuto = new JRadioButton(BUNDLE.getString("MinPlusSpan.label"));
			timeAxisMaxAutoValue = new ParenthesizedTimeLabel(timeAxisMaxAuto);
            timeAxisMaxAutoValue.setTime(initCalendar);

			JPanel yAxisMaxRow1 = createMultiItemRow(timeAxisMaxCurrent, timeAxisMaxCurrentValue);
	        JPanel yAxisMaxRow2 = createMultiItemRow(timeAxisMaxManual, timeAxisMaxManualValue);
			JPanel yAxisMaxRow3 = createMultiItemRow(timeAxisMaxAuto, timeAxisMaxAutoValue);

	        timeAxisMaxAuto.setSelected(true);

	        add(yAxisMaxRow1);
	        add(yAxisMaxRow2);
	        add(yAxisMaxRow3);
	        add(Box.createVerticalGlue());

	        ButtonGroup maxButtonGroup = new ButtonGroup();
	        maxButtonGroup.add(timeAxisMaxCurrent);
	        maxButtonGroup.add(timeAxisMaxManual);
	        maxButtonGroup.add(timeAxisMaxAuto);

	        timeAxisMaxAuto.setToolTipText(BUNDLE.getString("TimeAxisMins.label"));
		}
	}

	// Panel holding the min, max and span controls
	class YAxisButtonsPanel extends JPanel {
		private static final long serialVersionUID = 3430980575280458813L;
		private GridBagConstraints gbcForMax;
		private GridBagConstraints gbcForMin;
		private GridBagConstraints gbcForSpan;

		public YAxisButtonsPanel() {
			setLayout(new GridBagLayout());
			setPreferredSize(Y_AXIS_BUTTONS_PANEL_PREFERRED_SIZE);

			// Align radio buttons for Max and Min on the left
			gbcForMax = new GridBagConstraints();
			gbcForMax.gridx = 0;
			gbcForMax.fill = GridBagConstraints.HORIZONTAL;
			gbcForMax.anchor = GridBagConstraints.WEST;
			gbcForMax.weightx = 1;
			gbcForMax.weighty = 0;

			gbcForMin = new GridBagConstraints();
			gbcForMin.gridx = 0;
			gbcForMin.fill = GridBagConstraints.HORIZONTAL;
			gbcForMin.anchor = GridBagConstraints.WEST;
			gbcForMin.weightx = 1;
			gbcForMin.weighty = 0;

			// Align Span controls on the right
			gbcForSpan = new GridBagConstraints();
			gbcForSpan.gridx = 0;
			// Let fill default to NONE and weightx default to 0
			gbcForSpan.anchor = GridBagConstraints.EAST;
			gbcForSpan.weighty = 1;
		}

		public void setNormalOrder(boolean normalDirection) {
			removeAll();
			if (normalDirection) {
				gbcForMax.gridy = 0;
				gbcForMax.anchor = GridBagConstraints.NORTHWEST;
				add(yMaximumsPlusPanel, gbcForMax);
				gbcForSpan.gridy = 1;
				add(yAxisSpanPanel, gbcForSpan);
				gbcForMin.gridy = 2;
				gbcForMin.anchor = GridBagConstraints.SOUTHWEST;
				add(yMinimumsPlusPanel, gbcForMin);
				yMaximumsPlusPanel.setAxisTagAlignment(Component.TOP_ALIGNMENT);
				yMinimumsPlusPanel.setAxisTagAlignment(Component.BOTTOM_ALIGNMENT);
			} else {
				gbcForMin.gridy = 0;
				gbcForMin.anchor = GridBagConstraints.NORTHWEST;
				add(yMinimumsPlusPanel, gbcForMin);
				gbcForSpan.gridy = 1;
				add(yAxisSpanPanel, gbcForSpan);
				gbcForMax.gridy = 2;
				gbcForMax.anchor = GridBagConstraints.SOUTHWEST;
				add(yMaximumsPlusPanel, gbcForMax);
				yMaximumsPlusPanel.setAxisTagAlignment(Component.BOTTOM_ALIGNMENT);
				yMinimumsPlusPanel.setAxisTagAlignment(Component.TOP_ALIGNMENT);
			}
			revalidate();
		}

		public void insertMinMaxPanels(JPanel minPanel, JPanel maxPanel) {
			yMinimumsPlusPanel.setPanel(minPanel);
			yMaximumsPlusPanel.setPanel(maxPanel);
			revalidate();
		}
	}

	// Panel holding the X axis Minimums panel and Maximums panel
	class XAxisButtonsPanel extends JPanel {
		private static final long serialVersionUID = -5671943216161507045L;

		private JPanel minimumsPanel;
		private JPanel maximumsPanel;

		private GridBagConstraints gbcLeft;
		private GridBagConstraints gbcRight;

		public XAxisButtonsPanel() {
			this.setLayout(new GridBagLayout());

			gbcLeft = new GridBagConstraints();
			gbcLeft.gridx = 0;
			gbcLeft.gridy = 0;
			gbcLeft.fill = GridBagConstraints.BOTH;
			gbcLeft.anchor = GridBagConstraints.WEST;
			gbcLeft.weightx = 1;

			gbcRight = new GridBagConstraints();
			gbcRight.gridx = 1;
			gbcRight.gridy = 0;
			gbcRight.fill = GridBagConstraints.BOTH;
			gbcLeft.anchor = GridBagConstraints.EAST;
			gbcRight.weightx = 1;
		}

		public void setNormalOrder(boolean normalDirection) {
			removeAll();
			if (normalDirection) {
				add(minimumsPanel, gbcLeft);
				add(maximumsPanel, gbcRight);
			} else {
				add(maximumsPanel, gbcLeft);
				add(minimumsPanel, gbcRight);
			}
			revalidate();
		}

		public void insertMinMaxPanels(JPanel minPanel, JPanel maxPanel) {
			minimumsPanel = minPanel;
			maximumsPanel = maxPanel;
		}
	}

	// Non-time axis Minimums panel
	class NonTimeAxisMinimumsPanel extends JPanel {
		private static final long serialVersionUID = -2619634570876465687L;

		public NonTimeAxisMinimumsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			nonTimeAxisMinCurrent = new JRadioButton(BUNDLE.getString("CurrentSmallestDatum.label"), true);
			
			nonTimeAxisMinCurrentValue = new ParenthesizedNumericLabel(nonTimeAxisMinCurrent);
			nonTimeAxisMinCurrentValue.setValue(0.0);

			nonTimeAxisMinManual = new JRadioButton(MANUAL_LABEL, false);
			
			nonTimeAxisMinManualValue = new NumericTextField(NUMERIC_TEXTFIELD_COLS1, nonTimePaddingFormat);
			nonTimeAxisMinManualValue.setColumns(JTEXTFIELD_COLS);
			
			nonTimeAxisMinManualValue.setText(nonTimeAxisMinCurrentValue.getValue().toString()); 
			
			nonTimeAxisMinAutoAdjust = new JRadioButton(BUNDLE.getString("MaxMinusSpan.label"), false);
			nonTimeAxisMinAutoAdjustValue = new ParenthesizedNumericLabel(nonTimeAxisMinAutoAdjust);
			nonTimeAxisMinAutoAdjustValue.setValue(0.0); 

			
			JPanel xAxisMinRow1 = createMultiItemRow(nonTimeAxisMinCurrent, nonTimeAxisMinCurrentValue);
			JPanel xAxisMinRow2 = createMultiItemRow(nonTimeAxisMinManual, nonTimeAxisMinManualValue);
			JPanel xAxisMinRow3 = createMultiItemRow(nonTimeAxisMinAutoAdjust, nonTimeAxisMinAutoAdjustValue);

			ButtonGroup minimumsGroup = new ButtonGroup();
			minimumsGroup.add(nonTimeAxisMinCurrent);
			minimumsGroup.add(nonTimeAxisMinManual);
			minimumsGroup.add(nonTimeAxisMinAutoAdjust);

			// Layout
			add(xAxisMinRow1);
			add(xAxisMinRow2);
			add(xAxisMinRow3);
			nonTimeAxisMinCurrent.setToolTipText(BUNDLE.getString("NonTimeAxisMin.label"));

			// Instrument
			xAxisMinRow1.setName("xAxisMinRow1");
			xAxisMinRow2.setName("xAxisMinRow2");
			xAxisMinRow3.setName("xAxisMinRow3");
		}
	}

	// Non-time axis Maximums panel
	class NonTimeAxisMaximumsPanel extends JPanel {
		private static final long serialVersionUID = -768623994853270825L;

		public NonTimeAxisMaximumsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			nonTimeAxisMaxCurrent = new JRadioButton(BUNDLE.getString("CurrentLargestDatum.label"), true);
			
			nonTimeAxisMaxCurrentValue = new ParenthesizedNumericLabel(nonTimeAxisMaxCurrent);
			nonTimeAxisMaxCurrentValue.setValue(1.0);

			nonTimeAxisMaxManual = new JRadioButton(MANUAL_LABEL, false);
			
			DecimalFormat format = new DecimalFormat("###.######");
			format.setParseIntegerOnly(false);
			nonTimeAxisMaxManualValue = new NumericTextField(NUMERIC_TEXTFIELD_COLS1, format);
			nonTimeAxisMaxManualValue.setColumns(JTEXTFIELD_COLS);
			
			nonTimeAxisMaxManualValue.setText(nonTimeAxisMaxCurrentValue.getValue().toString());

			nonTimeAxisMaxAutoAdjust = new JRadioButton(BUNDLE.getString("MinPlusSpan.label"), false);
			nonTimeAxisMaxAutoAdjustValue = new ParenthesizedNumericLabel(nonTimeAxisMaxAutoAdjust);
			nonTimeAxisMaxAutoAdjustValue.setValue(1.0);

			JPanel xAxisMaxRow1 = createMultiItemRow(nonTimeAxisMaxCurrent, nonTimeAxisMaxCurrentValue);
			JPanel xAxisMaxRow2 = createMultiItemRow(nonTimeAxisMaxManual, nonTimeAxisMaxManualValue);
			JPanel xAxisMaxRow3 = createMultiItemRow(nonTimeAxisMaxAutoAdjust, nonTimeAxisMaxAutoAdjustValue);

			ButtonGroup maximumsGroup = new ButtonGroup();
			maximumsGroup.add(nonTimeAxisMaxManual);
			maximumsGroup.add(nonTimeAxisMaxCurrent);
			maximumsGroup.add(nonTimeAxisMaxAutoAdjust);

			// Layout
			add(xAxisMaxRow1);
			add(xAxisMaxRow2);
			add(xAxisMaxRow3);
			nonTimeAxisMaxCurrent.setToolTipText(BUNDLE.getString("NonTimeAxisMax.label"));

			// Instrument
			xAxisMaxRow1.setName("xAxisMaxRow1");
			xAxisMaxRow2.setName("xAxisMaxRow2");
			xAxisMaxRow3.setName("xAxisMaxRow3");
		}
	}

	// Panel holding X axis tags for Min and Max, and the Span field
	class XAxisAdjacentPanel extends JPanel {
		private static final long serialVersionUID = 4160271246055659710L;
        GridBagConstraints gbcLeft = new GridBagConstraints();
        GridBagConstraints gbcRight = new GridBagConstraints();
        GridBagConstraints gbcCenter = new GridBagConstraints();

		public XAxisAdjacentPanel() {
		    this.setLayout(new GridBagLayout());
			xMinLabel = new JLabel(BUNDLE.getString("Min.label"));
			xMaxLabel = new JLabel(BUNDLE.getString("Max.label"));
            xMinLabel.setFont(xMinLabel.getFont().deriveFont(Font.BOLD));
            xMaxLabel.setFont(xMaxLabel.getFont().deriveFont(Font.BOLD));

			setBorder(BOTTOM_PADDED_MARGINS);

            gbcLeft.anchor = GridBagConstraints.WEST;
            gbcLeft.gridx = 0;
            gbcLeft.gridy = 0;
            gbcLeft.weightx = 0.5;
            gbcCenter.anchor = GridBagConstraints.NORTH;
            gbcCenter.gridx = 1;
            gbcCenter.gridy = 0;
            gbcRight.anchor = GridBagConstraints.EAST;
            gbcRight.gridx = 2;
            gbcRight.gridy = 0;
            gbcRight.weightx = 0.5;
		}

		public void setNormalOrder(boolean normalDirection) {
			removeAll();
			if (normalDirection) {
			    add(xMinLabel, gbcLeft);
			    add(xAxisSpanCluster, gbcCenter);
			    add(xMaxLabel, gbcRight);
			} else {
                add(xMaxLabel, gbcLeft);
                add(xAxisSpanCluster, gbcCenter);
                add(xMinLabel, gbcRight);
			}
			revalidate();
		}
	}

	// Panel holding X axis Span controls
	class XAxisSpanCluster extends JPanel {
		private static final long serialVersionUID = -3947426156383446643L;
		private JComponent spanValue;
		private JLabel spanTag;
		private Component boxStrut;

		public XAxisSpanCluster() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	        spanTag = new JLabel("Span: ");

	        // Create a text field with a ddd/hh:mm:ss format for time
	        MaskFormatter formatter = null;
			try {
				formatter = new MaskFormatter("###/##:##:##");
				formatter.setPlaceholderCharacter('0');
			} catch (ParseException e) {
				logger.error("Error in creating a mask formatter", e);
			}
	        timeSpanValue = new TimeSpanTextField(formatter);
	        spanValue = timeSpanValue;
	        boxStrut = Box.createHorizontalStrut(INTERCONTROL_HORIZONTAL_SPACING);

			layoutPanel();

            // Instrument
            spanTag.setName("spanTag");
		}

		void layoutPanel() {
			removeAll();
			add(spanTag);
            add(boxStrut);
            add(spanValue);
		}

		void setSpanField(JComponent field) {
			spanValue = field;
			layoutPanel();
		}
	}

	// Panel that holds the still image of a plot in the Initial Settings area
    public class StillPlotImagePanel extends JPanel {
		private static final long serialVersionUID = 8645833372400367908L;
		private JLabel timeOnXAxisNormalPicture;
		private JLabel timeOnYAxisNormalPicture;
		private JLabel timeOnXAxisReversedPicture;
		private JLabel timeOnYAxisReversedPicture;

		public StillPlotImagePanel() {
			timeOnXAxisNormalPicture = new JLabel("", IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_TIME_ON_X_NORMAL), JLabel.CENTER);
			timeOnYAxisNormalPicture = new JLabel("", IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_TIME_ON_Y_NORMAL), JLabel.CENTER);
			timeOnXAxisReversedPicture = new JLabel("", IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_TIME_ON_X_REVERSED), JLabel.CENTER);
			timeOnYAxisReversedPicture = new JLabel("", IconLoader.INSTANCE.getIcon(IconLoader.Icons.PLOT_TIME_ON_Y_REVERSED), JLabel.CENTER);
			add(timeOnXAxisNormalPicture); // default

			// Instrument
			timeOnXAxisNormalPicture.setName("timeOnXAxisNormalPicture");
			timeOnYAxisNormalPicture.setName("timeOnYAxisNormalPicture");
			timeOnXAxisReversedPicture.setName("timeOnXAxisReversedPicture");
			timeOnYAxisReversedPicture.setName("timeOnYAxisReversedPicture");
		}

		public void setImageToTimeOnXAxis(boolean normalDirection) {
			removeAll();
			if (normalDirection) {
				add(timeOnXAxisNormalPicture);
			} else {
				add(timeOnXAxisReversedPicture);
			}
			revalidate();
		}

		public void setImageToTimeOnYAxis(boolean normalDirection) {
			removeAll();
			if (normalDirection) {
				add(timeOnYAxisNormalPicture);
			} else {
				add(timeOnYAxisReversedPicture);
			}
			revalidate();
		}
	}


    /* ================================================
	 * Main Constructor for Plot Settings Control panel
	 * ================================================
	 */
	public PlotSettingsControlPanel(PlotViewManifestation plotMan) {
		// This sets the display of date/time fields that use this format object to use GMT
		dateFormat.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		
		// store reference to the plot
		plotViewManifestion = plotMan;	
		// Create controller for this panel.
		controller = new PlotSettingController(this);

		setLayout(new BorderLayout());
		setBorder(CONTROL_PANEL_MARGINS);
		
		addAncestorListener(new AncestorListener () {

			@Override
			public void ancestorAdded(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				// this could be changed to use resetButton.doClick();
				ActionListener[] resetListeners = resetButton.getActionListeners();
				if (resetListeners.length == 1) {
					resetListeners[0].actionPerformed(null);
				} else {
					logger.error("Reset button has unexpected listeners.");
				}
			}

		});

		// Assemble the panel contents - two collapsible panels
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));

		JPanel controlsAPanel = new SectionPanel(BUNDLE.getString("PlotSetup.label"), getInitialSetupPanel());
		JPanel controlsBPanel = new SectionPanel(BUNDLE.getString("WhenSpaceRunsOut.label"), getPlotBehaviorPanel());
		JPanel controlsCPanel = new SectionPanel(BUNDLE.getString("LineSetup.label"), getLineSetupPanel());
		
		overallPanel.add(controlsAPanel);
		overallPanel.add(Box.createRigidArea(new Dimension(0, 7)));
		overallPanel.add(controlsBPanel);
		overallPanel.add(Box.createRigidArea(new Dimension(0, 7)));
		overallPanel.add(controlsCPanel);

		// Use panels and layouts to achieve desired spacing
		JPanel squeezeBox = new JPanel(new BorderLayout());
		squeezeBox.add(overallPanel, BorderLayout.NORTH);

		PlotControlsLayout controlsLayout = new PlotControlsLayout();
		PlotControlsLayout.ResizersScrollPane scroller =
			controlsLayout.new ResizersScrollPane(squeezeBox, controlsAPanel, controlsBPanel);
		scroller.setBorder(BorderFactory.createEmptyBorder());

		JPanel paddableOverallPanel = new JPanel(controlsLayout);
		paddableOverallPanel.add(scroller, PlotControlsLayout.MIDDLE);
		paddableOverallPanel.add(createApplyButtonPanel(), PlotControlsLayout.LOWER);

		add(paddableOverallPanel, BorderLayout.CENTER);

		behaviorTimeAxisLetter.setText("X");
		behaviorNonTimeAxisLetter.setText("Y");

		// Set the initial value of the Time Min Auto value ("Now")
		GregorianCalendar nextTime = new GregorianCalendar();
		nextTime.setTimeInMillis(plotViewManifestion.getCurrentMCTTime());
		nextTime.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		if (nextTime.getTimeInMillis() == 0.0) {
			nextTime = plotViewManifestion.getPlot().getMinTime();
			nextTime.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
		}
		timeAxisMinAutoValue.setTime(nextTime);

		instrumentNames();

		// Initialize state of control panel to match that of the plot.
		PlotAbstraction plot = plotViewManifestion.getPlot();
		
		if (plot!=null){
		  setControlPanelState(plot.getAxisOrientationSetting(),
                  			 plot.getTimeSystem(),
                  			 plot.getTimeFormat(),
				  			 plot.getXAxisMaximumLocation(),
				             plot.getYAxisMaximumLocation(),
				             plot.getTimeAxisSubsequentSetting(),
				             plot.getNonTimeAxisSubsequentMinSetting(),
				             plot.getNonTimeAxisSubsequentMaxSetting(),
				             plot.getNonTimeMax(),
				             plot.getNonTimeMin(),
				             plot.getTimeMin(),
				             plot.getTimeMax(),
				             plot.getTimePadding(),
				             plot.getNonTimeMaxPadding(),
				             plot.getNonTimeMinPadding(),
				             plot.useOrdinalPositionForSubplots(),
				             plot.getTimeAxisUserPin().isPinned(),
				             plot.getPlotLineDraw(),
				             plot.getPlotLineConnectionType());		
		}

		// Save the panel controls' initial settings to control the Apply and Reset buttons'
		// enabled/disabled states.
		saveUIControlsSettings();
		setupApplyResetListeners();
			
		okButton.setEnabled(false);
		
		refreshDisplay();
	}

	@SuppressWarnings("serial")
	static public class SectionPanel extends JPanel {

		public SectionPanel(String titleText, JPanel inputPanel) {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = GridBagConstraints.REMAINDER;

			setBorder(BorderFactory.createTitledBorder(titleText));
			add(inputPanel, gbc);

			JLabel padding = new JLabel();
			gbc.weighty = 1.0;
			add(padding, gbc);
		}

	}

	private String getSelectedTimeSystem() {
        return timeSystemDropdown.getSelectedItem() != null ?
                        (String) timeSystemDropdown.getSelectedItem() : BUNDLE.getString("Time.label");
	}

	
	/*
	 * Take a snapshot of the UI controls' settings. ("ss" = saved setting)
	 * For button groups, only the selected button needs to be recorded.
	 * For spinners, the displayed text is recorded.
	 */
	private void saveUIControlsSettings() {
		// Time system drop-down box 

		// Choice of axis for Time
		ssWhichAxisAsTime = findSelectedButton(xAxisAsTimeRadioButton, yAxisAsTimeRadioButton);

        // Time system drop-down box
        ssTimeSystemSelection = getSelectedTimeSystem();
		
        // Time format drop-down box
        ssTimeFormatSelection = (String) timeFormatDropdown.getSelectedItem();

		// X-axis orientation
		ssXMaxAtWhich = findSelectedButton(xMaxAtRight, xMaxAtLeft);

		// Y axis orientation
		ssYMaxAtWhich = findSelectedButton(yMaxAtTop, yMaxAtBottom);

		// Time Axis Minimums
		ssTimeMin = findSelectedButton(timeAxisMinCurrent, timeAxisMinManual, timeAxisMinAuto);

		// Time Axis Maximums
		ssTimeMax = findSelectedButton(timeAxisMaxCurrent, timeAxisMaxManual, timeAxisMaxAuto);

		// Non-Time Axis Minimums
		ssNonTimeMin = findSelectedButton(nonTimeAxisMinCurrent, nonTimeAxisMinManual, nonTimeAxisMinAutoAdjust);
		
		// Non-Time Axis Maximums
		ssNonTimeMax = findSelectedButton(nonTimeAxisMaxCurrent, nonTimeAxisMaxManual, nonTimeAxisMaxAutoAdjust);
		
		// Panel - Plot Behavior When Space Runs Out 
		// Time Axis Table
		ssTimeAxisMode = findSelectedButton(timeJumpMode, timeScrunchMode);
		ssTimeAxisJumpMaxPadding = timeJumpPadding.getText();
		ssTimeAxisScrunchMaxPadding = timeScrunchPadding.getText();
		
		ssPinTimeAxis = pinTimeAxis.isSelected();

		// Non-Time Axis Table
		ssNonTimeMinAxisMode = findSelectedButtons(nonTimeMinAutoAdjustMode, nonTimeMinSemiFixedMode, nonTimeMinFixedMode);
		ssNonTimeMaxAxisMode = findSelectedButtons(nonTimeMaxAutoAdjustMode, nonTimeMaxSemiFixedMode, nonTimeMaxFixedMode);
		
		ssNonTimeMinPadding = nonTimeMinPadding.getText();
		ssNonTimeMaxPadding = nonTimeMaxPadding.getText();

		// Time Axis Manual fields
		ssTimeMinManualValue = timeAxisMinManualValue.getValueInMillis();
		ssTimeMaxManualValue = timeAxisMaxManualValue.getValueInMillis();

		// Non-Time Axis Manual fields
		ssNonTimeMinManualValue = nonTimeAxisMinManualValue.getText();
		ssNonTimeMaxManualValue = nonTimeAxisMaxManualValue.getText();
		
		// stacked plot grouping
		ssGroupByCollection = groupByCollection.isSelected();
		
		// Line drawing options
		ssDraw = findSelectedButton(linesOnly, markersAndLines, markersOnly);
		ssConnectionLineType = findSelectedButton(direct, step);
	}

	/*
	 * Does the Plot Settings Control Panel have pending changes ?
	 * Compare the values in the ss-variables to the current settings
	 */
	boolean isPanelDirty() {
		 JToggleButton selectedButton = null;

		 // Time system drop-down.
		 if (timeSystemDropdown.isEnabled() && !((String)timeSystemDropdown.getSelectedItem()).equals(ssTimeSystemSelection)) {
			 return true;
		 }

		 if (timeFormatDropdown.isEnabled() && !((String)timeFormatDropdown.getSelectedItem()).equals(ssTimeFormatSelection)) {
			 return true;
		 }
 
		// X or Y Axis As Time
		selectedButton = findSelectedButton(xAxisAsTimeRadioButton, yAxisAsTimeRadioButton);
		if (ssWhichAxisAsTime != selectedButton) {
			return true;
		}

		// X Axis orientation
		selectedButton = findSelectedButton(xMaxAtRight, xMaxAtLeft);
		if (ssXMaxAtWhich != selectedButton) {
			return true;
		}

		// Y Axis orientation
		selectedButton = findSelectedButton(yMaxAtTop, yMaxAtBottom);
		if (ssYMaxAtWhich != selectedButton) {
			return true;
		}

		// Time Axis Minimums
		selectedButton = findSelectedButton(timeAxisMinCurrent, timeAxisMinManual, timeAxisMinAuto);
		if (ssTimeMin != selectedButton) {
			return true;
		}
		// If the Manual setting was initially selected, check if the Manual value changed
		// Note that we convert our time value back to a string to avoid differing evaluations of milliseconds
		recycledCalendarA.setTimeInMillis(ssTimeMinManualValue);
		if ( (ssTimeMin == timeAxisMinManual && ssTimeMin.getClass().isInstance(timeAxisMinManual)) &&
				!getStaticAreaTimeFormat().format(recycledCalendarA.getTime()).equals(timeAxisMinManualValue.getValue()) ){
			return true;
		}

		// Time Axis Maximums
		selectedButton = findSelectedButton(timeAxisMaxCurrent, timeAxisMaxManual, timeAxisMaxAuto);
		if (ssTimeMax != selectedButton) {
			return true;
		}
		// If the Manual setting was initially selected, check if the Manual value changed
		// Note that we convert our time value back to a string to avoid differing evaluations of milliseconds
		recycledCalendarA.setTimeInMillis(ssTimeMaxManualValue);
		if ( (ssTimeMax == timeAxisMaxManual && ssTimeMax.getClass().isInstance(timeAxisMaxManual)) &&
				!dateFormat.format(recycledCalendarA.getTime()).equals(timeAxisMaxManualValue.getValue()) ) {
			return true;
		}

		// Non-Time Axis Minimums
		selectedButton = findSelectedButton(nonTimeAxisMinCurrent, nonTimeAxisMinManual, nonTimeAxisMinAutoAdjust);
		if (ssNonTimeMin != selectedButton) {
			return true;
		}
		// If the Manual setting was initially selected, check if the Manual value changed
		if (ssNonTimeMin == nonTimeAxisMinManual &&
				! ssNonTimeMinManualValue.equals(nonTimeAxisMinManualValue.getText())) {
			return true;
		}

		// Non-Time Axis Maximums
		selectedButton = findSelectedButton(nonTimeAxisMaxCurrent, nonTimeAxisMaxManual, nonTimeAxisMaxAutoAdjust);
		if (ssNonTimeMax != selectedButton) {
			return true;
		}
		// If the Manual setting was initially selected, check if the Manual value changed
		if (ssNonTimeMax == nonTimeAxisMaxManual &&
				! ssNonTimeMaxManualValue.equals(nonTimeAxisMaxManualValue.getText())) {
			return true;
		}

		// Panel - Plot Behavior When Space Runs Out 
		// Time Axis Table
		selectedButton = findSelectedButton(timeJumpMode, timeScrunchMode);
		if (ssTimeAxisMode != selectedButton) {
			return true;
		}
		if (! ssTimeAxisJumpMaxPadding.equals(timeJumpPadding.getText())) {
			return true;
		}
		if (! ssTimeAxisScrunchMaxPadding.equals(timeScrunchPadding.getText())) {
			return true;
		}
		
		if (!ssPinTimeAxis.equals(pinTimeAxis.isSelected())) {
			return true;
		}
	
		// Non-Time Axis Table
		if (!buttonStateMatch(findSelectedButtons(nonTimeMinAutoAdjustMode, nonTimeMinSemiFixedMode, nonTimeMinFixedMode),
	              ssNonTimeMinAxisMode)) {			
           return true;
        }	

		if (!buttonStateMatch(findSelectedButtons(nonTimeMaxAutoAdjustMode, nonTimeMaxSemiFixedMode, nonTimeMaxFixedMode),
				              ssNonTimeMaxAxisMode)) { 
			return true;
		}

		if (! ssNonTimeMinPadding.equals(nonTimeMinPadding.getText())) {
			return true;
		}
		
		if (! ssNonTimeMaxPadding.equals(nonTimeMaxPadding.getText())) {
			return true;
		}
		
		if (ssGroupByCollection != groupByCollection.isSelected()) {
			return true;
		}
		
		// Line Setup panel: Draw options
		selectedButton = findSelectedButton(linesOnly, markersAndLines, markersOnly);
		if (ssDraw != selectedButton) {
			return true;
		}
		// Line Setup panel: Connection line type options
		selectedButton = findSelectedButton(direct, step);
		if (ssConnectionLineType != selectedButton) {
			return true;
		}
		
		
		return false;
	}

	private  JToggleButton findSelectedButton( JToggleButton... buttons) {
		for ( JToggleButton button : buttons) {
			
			if (button.isSelected()) {
				return button;
			} 
		}
		logger.error("Unexpected, no selected button in subject group in Plot Settings Control Panel.");
		return null;
	}
	
	static List<JToggleButton> findSelectedButtons( JToggleButton... buttons) {
		List<JToggleButton> selectedButtons = new ArrayList<JToggleButton>(2);
		for ( JToggleButton button : buttons) {
			
			if (button.isSelected()) {
		      selectedButtons.add(button);
			}
		}
		return selectedButtons;
	}
	
	/**
	 * Return true if tw
	 * @param buttonSet1
	 * @param buttonSet2
	 * @return
	 */
	static boolean buttonStateMatch(List<JToggleButton> buttonSet1, List<JToggleButton> buttonSet2) {
	   return buttonSet1.size() == buttonSet2.size() &&
		    buttonSet1.containsAll(buttonSet2);
	}
	

	/*
	 * Add listeners to the UI controls to connect the logic for enabling the
	 * Apply and Reset buttons
	 */
	private void setupApplyResetListeners() {
		timeSystemDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMainButtons();
				updateStaticAreaWithTimeSystem();
            }
		});

		timeFormatDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMainButtons();
				setStaticAreaTimeFormat( (String) timeFormatDropdown.getSelectedItem());
			}
		});

		
		// Add listener to radio buttons not on the axes
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMainButtons();
			}
		};
		xAxisAsTimeRadioButton.addActionListener(buttonListener);
		yAxisAsTimeRadioButton.addActionListener(buttonListener);
		xMaxAtRight.addActionListener(buttonListener);
		xMaxAtLeft.addActionListener(buttonListener);
		yMaxAtTop.addActionListener(buttonListener);
		yMaxAtBottom.addActionListener(buttonListener);

		timeJumpMode.addActionListener(new TimeAxisModeListener(timeJumpPadding));
		timeScrunchMode.addActionListener(new TimeAxisModeListener(timeScrunchPadding));
		pinTimeAxis.addActionListener(buttonListener);

		nonTimeMinAutoAdjustMode.addActionListener(buttonListener);
		nonTimeMinFixedMode.addActionListener(buttonListener);
		nonTimeMinSemiFixedMode.addActionListener(buttonListener);
		nonTimeMaxAutoAdjustMode.addActionListener(buttonListener);
		nonTimeMaxFixedMode.addActionListener(buttonListener);
		nonTimeMaxSemiFixedMode.addActionListener(buttonListener);

		// Add listeners to the Time axis buttons
		ActionListener timeAxisListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimeAxisControls();
			}
		};
		timeAxisMinCurrent.addActionListener(timeAxisListener);
		timeAxisMinAuto.addActionListener(timeAxisListener);
		timeAxisMaxCurrent.addActionListener(timeAxisListener);
		timeAxisMaxAuto.addActionListener(timeAxisListener);
		timeAxisMinManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeMinManualHasBeenSelected = true;
				updateTimeAxisControls();
			}
		});
		timeAxisMaxManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeMaxManualHasBeenSelected = true;
				updateTimeAxisControls();
			}
		});

		// Add listeners to the Non-Time axis buttons
		ActionListener nonTimeAxisListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateNonTimeAxisControls();
			}
		};
		nonTimeAxisMinCurrent.addActionListener(nonTimeAxisListener);
		nonTimeAxisMinAutoAdjust.addActionListener(nonTimeAxisListener);
		nonTimeAxisMaxCurrent.addActionListener(nonTimeAxisListener);
		nonTimeAxisMaxAutoAdjust.addActionListener(nonTimeAxisListener);
		nonTimeAxisMinManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nonTimeAxisMinManual.isSelected()) {
					nonTimeAxisMinManualValue.requestFocusInWindow();
					nonTimeAxisMinManualValue.setSelectionStart(0);
					String content = nonTimeAxisMinManualValue.getText();
					
					
					if (content != null) {
						nonTimeAxisMinManualValue.setSelectionEnd(content.length());
					}
					updateNonTimeAxisControls();
				}
			}
		});
		nonTimeAxisMaxManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nonTimeAxisMaxManual.isSelected()) {
					nonTimeAxisMaxManualValue.requestFocusInWindow();
					nonTimeAxisMaxManualValue.setSelectionStart(0);
					String content = nonTimeAxisMaxManualValue.getText();
										
					if (content != null) {
						nonTimeAxisMaxManualValue.setSelectionEnd(content.length());
					}
					updateNonTimeAxisControls();
				}
			}
		});

		// Add listeners to Non-Time axis text fields
		nonTimeSpanValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.isTemporary())
					return;
				updateNonTimeAxisControls();
			}
		});

		// Add listeners to Time axis text fields
		timeAxisMinManualValue.addFocusListener(new TimeFieldFocusListener(timeAxisMinManual) {
			@Override
			public void focusGained(FocusEvent e) {
				timeMinManualHasBeenSelected = true;
				super.focusGained(e);
			}			
		});
		timeAxisMaxManualValue.addFocusListener(new TimeFieldFocusListener(timeAxisMaxManual)  {
			@Override
			public void focusGained(FocusEvent e) {
				timeMaxManualHasBeenSelected = true;
				super.focusGained(e);
			}
		});
		timeSpanValue.addFocusListener(new TimeFieldFocusListener(null));

		// Plot Behavior section: Add listeners to Padding text fields
		timeJumpPadding.addFocusListener(new TimePaddingFocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				timeJumpMode.setSelected(true);
			}
		});
		timeScrunchPadding.addFocusListener(new TimePaddingFocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				timeScrunchMode.setSelected(true);
			}
		});

		FocusListener nontimePaddingListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.isTemporary())
					return;
				updateNonTimeAxisControls();
			}
		};
		nonTimeMinPadding.addFocusListener(nontimePaddingListener);
		nonTimeMaxPadding.addFocusListener(nontimePaddingListener);
	}

	// Apply and Reset buttons at bottom of the Plot Settings Control Panel
	private JPanel createApplyButtonPanel() {
		okButton = new JButton(BUNDLE.getString("Apply.label"));
		resetButton = new JButton(BUNDLE.getString("Reset.label"));

		okButton.setEnabled(false);
		resetButton.setEnabled(false);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setupPlot();
				PlotAbstraction plot = plotViewManifestion.getPlot();	
				if (plot!=null){
					setControlPanelState(plot.getAxisOrientationSetting(),
							plot.getTimeSystem(),
                            plot.getTimeFormat(),
							plot.getXAxisMaximumLocation(),
							plot.getYAxisMaximumLocation(),
							plot.getTimeAxisSubsequentSetting(),
							plot.getNonTimeAxisSubsequentMinSetting(),
							plot.getNonTimeAxisSubsequentMaxSetting(),
							plot.getNonTimeMax(),
							plot.getNonTimeMin(),
							plot.getTimeMin(),
							plot.getTimeMax(),
							plot.getTimePadding(),
							plot.getNonTimeMaxPadding(),
							plot.getNonTimeMinPadding(),
							plot.useOrdinalPositionForSubplots(), 
							plot.getTimeAxisUserPin().isPinned(),
							plot.getPlotLineDraw(),
							plot.getPlotLineConnectionType());		
				}
				okButton.setEnabled(false);
				saveUIControlsSettings();
				plotViewManifestion.getManifestedComponent().save();
			}
		});
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                PlotAbstraction plot = plotViewManifestion.getPlot();    
                if (plot!=null){
                   setControlPanelState(plot.getAxisOrientationSetting(),
                		   			 plot.getTimeSystem(),
                		   			 plot.getTimeFormat(),
                		   			 plot.getXAxisMaximumLocation(),
                                     plot.getYAxisMaximumLocation(),
                                     plot.getTimeAxisSubsequentSetting(),
                                     plot.getNonTimeAxisSubsequentMinSetting(),
                                     plot.getNonTimeAxisSubsequentMaxSetting(),
                                     plot.getNonTimeMax(),
                                     plot.getNonTimeMin(),
                                     plot.getTimeMin(),
                                     plot.getTimeMax(),
                                     plot.getTimePadding(),
                                     plot.getNonTimeMaxPadding(),
                                     plot.getNonTimeMinPadding(),
                                     plot.useOrdinalPositionForSubplots(), 
                                     plot.getTimeAxisUserPin().isPinned(),
                                     plot.getPlotLineDraw(),
                                     plot.getPlotLineConnectionType());        
                }
                okButton.setEnabled(false);
                resetButton.setEnabled(false);
                saveUIControlsSettings();
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
		okPanel.setBackground(OK_PANEL_BACKGROUND_COLOR);

		return okPanel;
	}

	// Assign internal names to the top level class variables
	private void instrumentNames() {
        timeAxisMinimumsPanel.setName("timeAxisMinimumsPanel");
        timeAxisMaximumsPanel.setName("timeAxisMaximumsPanel");
		nonTimeAxisMaximumsPanel.setName("nonTimeAxisMaximumsPanel");
		nonTimeAxisMinimumsPanel.setName("nonTimeAxisMinimumsPanel");

        timeAxisMinAuto.setName("timeAxisMinAuto");
        timeAxisMinAutoValue.setName("timeAxisMinAutoValue");
        timeAxisMinManual.setName("timeAxisMinManual");
        timeAxisMinManualValue.setName("timeAxisMinManualValue");

        timeAxisMaxAuto.setName("timeAxisMaxAuto");
        timeAxisMaxAutoValue.setName("timeAxisMaxAutoValue");
        timeAxisMaxManual.setName("timeAxisMaxManual");
        timeAxisMaxManualValue.setName("timeAxisMaxManualValue");

		nonTimeAxisMinCurrent.setName("nonTimeAxisMinCurrent");
		nonTimeAxisMinCurrentValue.setName("nonTimeAxisMinCurrentValue");
		nonTimeAxisMinManual.setName("nonTimeAxisMinManual");
		nonTimeAxisMinManualValue.setName("nonTimeAxisMinManualValue");
		nonTimeAxisMinAutoAdjust.setName("nonTimeAxisMinAutoAdjust");

		nonTimeAxisMaxCurrent.setName("nonTimeAxisMaxCurrent");
		nonTimeAxisMaxCurrentValue.setName("nonTimeAxisMaxCurrentValue");
		nonTimeAxisMaxManual.setName("nonTimeAxisMaxManual");
		nonTimeAxisMaxManualValue.setName("nonTimeAxisMaxManualValue");
		nonTimeAxisMaxAutoAdjust.setName("nonTimeAxisMaxAutoAdjust");

    	timeJumpMode.setName("timeJumpMode");
    	timeScrunchMode.setName("timeScrunchMode");
    	timeJumpPadding.setName("timeJumpPadding");
    	timeScrunchPadding.setName("timeScrunchPadding");

		nonTimeMinAutoAdjustMode.setName("nonTimeMinAutoAdjustMode");
    	nonTimeMaxAutoAdjustMode.setName("nonTimeMaxAutoAdjustMode");
    	nonTimeMinFixedMode.setName("nonTimeMinFixedMode");
    	nonTimeMaxFixedMode.setName("nonTimeMaxFixedMode");
    	nonTimeMinSemiFixedMode.setName("nonTimeMinSemiFixedMode");
    	nonTimeMaxSemiFixedMode.setName("nonTimeMaxSemiFixedMode");

        imagePanel.setName("imagePanel");
        timeSystemDropdown.setName("timeSystemDropdown");
        timeFormatDropdown.setName("timeFormatDropdown");

        timeSpanValue.setName("timeSpanValue");
        nonTimeSpanValue.setName("nonTimeSpanValue");
		
        xMinLabel.setName("xMinLabel");
        xMaxLabel.setName("xMaxLabel");

        xAxisAsTimeRadioButton.setName("xAxisAsTimeRadioButton");
        yAxisAsTimeRadioButton.setName("yAxisAsTimeRadioButton");
        xMaxAtRight.setName("xMaxAtRight");
        xMaxAtLeft.setName("xMaxAtLeft");
        yMaxAtTop.setName("yMaxAtTop");
        yMaxAtBottom.setName("yMaxAtBottom");
        yAxisType.setName("yAxisType");
		xAxisType.setName("xAxisType");

		xAxisSpanCluster.setName("xAxisSpanCluster");
		xAxisAdjacentPanel.setName("xAxisAdjacentPanel");
		xAxisButtonsPanel.setName("xAxisButtonsPanel");

        yAxisSpanPanel.setName("ySpanPanel");
        yMaximumsPlusPanel.setName("yMaximumsPlusPanel");
        yMinimumsPlusPanel.setName("yMinimumsPlusPanel");
        yAxisButtonsPanel.setName("yAxisButtonsPanel");
	}

	/**
	 * This method scans and sets the Time Axis controls next to the static plot image.
	 * Triggered by time axis button selection.
	 */
	GregorianCalendar scratchCalendar = new GregorianCalendar();
	GregorianCalendar workCalendar = new GregorianCalendar();
	{
		workCalendar.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
	}
	

	void updateTimeAxisControls() {
		// Enable/disable the Span control
		timeSpanValue.setEnabled(timeAxisMaxAuto.isSelected());

		// Set the value of the Time Span field and the various Min and Max Time fields
		if (timeAxisMinAuto.isSelected() && timeAxisMaxAuto.isSelected()) {
			// If both Auto buttons are selected, Span value is used
			scratchCalendar.setTimeInMillis(timeAxisMinAutoValue.getTimeInMillis());
			scratchCalendar.add(Calendar.SECOND, timeSpanValue.getSecond());
			scratchCalendar.add(Calendar.MINUTE, timeSpanValue.getMinute());
			scratchCalendar.add(Calendar.HOUR_OF_DAY, timeSpanValue.getHourOfDay());
			scratchCalendar.add(Calendar.DAY_OF_YEAR, timeSpanValue.getDayOfYear());
			timeAxisMaxAutoValue.setTime(scratchCalendar);
		} else if (timeAxisMinAuto.isSelected() && timeAxisMaxManual.isSelected()) {
			/*
			 * Min Auto ("Now"), and Max Manual
			 */
			timeSpanValue.setTime(subtractTimes(timeAxisMinAutoValue.getTimeInMillis(),
					timeAxisMaxManualValue.getValueInMillis()));
		} else if (timeAxisMinAuto.isSelected() && timeAxisMaxCurrent.isSelected()) {
			/*
			 * Min Auto ("Now"), and Current Max
			 */
			timeSpanValue.setTime(subtractTimes(timeAxisMinAutoValue.getTimeInMillis(),
					timeAxisMaxCurrentValue.getTimeInMillis()));
		} else if (timeAxisMinManual.isSelected() && timeAxisMaxAuto.isSelected()) {
			/*
			 * Min Manual, and Max Auto ("Min+Span")
			 */
			scratchCalendar.setTimeInMillis(timeAxisMinManualValue.getValueInMillis());
			scratchCalendar.add(Calendar.SECOND, timeSpanValue.getSecond());
			scratchCalendar.add(Calendar.MINUTE, timeSpanValue.getMinute());
			scratchCalendar.add(Calendar.HOUR_OF_DAY, timeSpanValue.getHourOfDay());
			scratchCalendar.add(Calendar.DAY_OF_YEAR, timeSpanValue.getDayOfYear());
			timeAxisMaxAutoValue.setTime(scratchCalendar);
		} else if (timeAxisMinManual.isSelected() && timeAxisMaxManual.isSelected()) {
			/*
			 * Min Manual, and Max Manual
			 * - subtract the Min Manual from the Max Manual to get the new Span value
			 */
			timeSpanValue.setTime(subtractTimes(timeAxisMinManualValue.getValueInMillis(),
					timeAxisMaxManualValue.getValueInMillis()));
		} else if (timeAxisMinManual.isSelected() && timeAxisMaxCurrent.isSelected()) {
			/*
			 * Min Manual, and Current Max
			 * - subtract the Min Manual from the Current Max to get the new Span value
			 */
			timeSpanValue.setTime(subtractTimes(timeAxisMinManualValue.getValueInMillis(),
					timeAxisMaxCurrentValue.getTimeInMillis()));
		} else if (timeAxisMinCurrent.isSelected() && timeAxisMaxAuto.isSelected()) {
			/*
			 * Current Min, and Max Auto ("Min+Span")
			 * - set the Max Auto value to the sum of Current Min and the Span value
			 */
			scratchCalendar.setTimeInMillis(timeAxisMinCurrentValue.getTimeInMillis());
			scratchCalendar.add(Calendar.SECOND, timeSpanValue.getSecond());
			scratchCalendar.add(Calendar.MINUTE, timeSpanValue.getMinute());
			scratchCalendar.add(Calendar.HOUR_OF_DAY, timeSpanValue.getHourOfDay());
			scratchCalendar.add(Calendar.DAY_OF_YEAR, timeSpanValue.getDayOfYear());
			timeAxisMaxAutoValue.setTime(scratchCalendar);
		} else if (timeAxisMinCurrent.isSelected() && timeAxisMaxManual.isSelected()) {
			/*
			 * Current Min, and Max Manual
			 * - subtract the Current Min from Max Manual to get the new Span value 
			 */
			timeSpanValue.setTime(subtractTimes(timeAxisMinCurrentValue.getTimeInMillis(),
					timeAxisMaxManualValue.getValueInMillis()));
		} else if (timeAxisMinCurrent.isSelected() && timeAxisMaxCurrent.isSelected()) {
			/*
			 * Current Min, and Current Max
			 * - subtract the Current Min from the Current Max to get the new Span value
			 */
			timeSpanValue.setTime(subtractTimes(timeAxisMinCurrentValue.getTimeInMillis(),
					timeAxisMaxCurrentValue.getTimeInMillis()));
		} else {
			logger.error("Program issue: if-else cases are missing one use case.");
		}

		if (timeAxisMinCurrent.isSelected()) {
			scratchCalendar.setTimeInMillis(timeAxisMinCurrentValue.getTimeInMillis());
		} else
			if (timeAxisMinManual.isSelected()) {
				scratchCalendar.setTimeInMillis(timeAxisMinManualValue.getValueInMillis());
			} else
				if (timeAxisMinAuto.isSelected()) {
					scratchCalendar.setTimeInMillis(timeAxisMinAutoValue.getTimeInMillis());
				}

		scratchCalendar.add(Calendar.SECOND, timeSpanValue.getSecond());
		scratchCalendar.add(Calendar.MINUTE, timeSpanValue.getMinute());
		scratchCalendar.add(Calendar.HOUR_OF_DAY, timeSpanValue.getHourOfDay());
		scratchCalendar.add(Calendar.DAY_OF_YEAR, timeSpanValue.getDayOfYear());
		timeAxisMaxAutoValue.setTime(scratchCalendar);

		// Update the Time axis Current Min and Max values
		GregorianCalendar plotMinTime = plotViewManifestion.getPlot().getMinTime();
		GregorianCalendar plotMaxTime = plotViewManifestion.getPlot().getMaxTime();
		
		timeAxisMinCurrentValue.setTime(plotMinTime);
		timeAxisMaxCurrentValue.setTime(plotMaxTime);

		// If the Manual (Min and Max) fields have NOT been selected up to now, update them with the
		// plot's current Min and Max values
		if (! timeMinManualHasBeenSelected) {
    		workCalendar.setTime(plotMinTime.getTime());
			timeAxisMinManualValue.setTime(workCalendar);
		}
		if (! timeMaxManualHasBeenSelected) {
    		workCalendar.setTime(plotMaxTime.getTime());
			timeAxisMaxManualValue.setTime(workCalendar);
		}
		updateMainButtons();
	}

	/**
	 * Returns the difference between two times as a time Duration
	 * @param begin
	 * @param end
	 * @return
	 */
	private TimeDuration subtractTimes(long begin, long end) {
	    if (begin < end) {
			long difference = end - begin;
			long days = difference / (24 * 60 * 60 * 1000);
			long remainder = difference - (days * 24 * 60 * 60 * 1000);
			long hours = remainder / (60 * 60 * 1000);
			remainder = remainder - (hours * 60 * 60 * 1000);
			long minutes = remainder / (60 * 1000);
			remainder = remainder - (minutes * 60 * 1000);
			long seconds = remainder / (1000);
			return new TimeDuration((int) days, (int) hours, (int) minutes, (int) seconds);
		} else {
			return new TimeDuration(0, 0, 0, 0);
		}
	}

	/**
	 * This method scans and sets the Non-Time Axis controls next to the static plot image.
	 * Triggered when a non-time radio button is selected or on update tick
	 */
	void updateNonTimeAxisControls() {
		assert !(nonTimeAxisMinAutoAdjust.isSelected() && nonTimeAxisMaxAutoAdjust.isSelected()) : "Illegal condition: Both span radio buttons are selected.";
		// Enable/disable the non-time Span value
		if (nonTimeAxisMinAutoAdjust.isSelected() || nonTimeAxisMaxAutoAdjust.isSelected()) {
			nonTimeSpanValue.setEnabled(true);
		} else {
			nonTimeSpanValue.setEnabled(false);
		}
		// Enable/disable the non-time Auto-Adjust (Span-dependent) controls
		if (nonTimeAxisMaxAutoAdjust.isSelected()) {
			nonTimeAxisMinAutoAdjust.setEnabled(false);
			nonTimeAxisMinAutoAdjustValue.setEnabled(false);
		} else
		if (nonTimeAxisMinAutoAdjust.isSelected()) {
			nonTimeAxisMaxAutoAdjust.setEnabled(false);
			nonTimeAxisMaxAutoAdjustValue.setEnabled(false);
		} else
			// If neither of the buttons using Span is selected, enable both
			if (!nonTimeAxisMinAutoAdjust.isSelected() && !nonTimeAxisMaxAutoAdjust.isSelected()) {
			nonTimeAxisMinAutoAdjust.setEnabled(true);
			nonTimeAxisMaxAutoAdjust.setEnabled(true);					
			nonTimeAxisMinAutoAdjustValue.setEnabled(true);
			nonTimeAxisMaxAutoAdjustValue.setEnabled(true);					
		}

		// Update the Span-dependent controls
		//     nonTimeAxisMinAutoAdjustValue: (Max - Span)
		double maxValue = getNonTimeMaxValue();
		//     nonTimeAxisMaxAutoAdjustValue: (Min + Span)
		double minValue = getNonTimeMinValue();
		
	
		try {
			String span = nonTimeSpanValue.getText();
			if (! span.isEmpty()) {
				double spanValue = nonTimeSpanValue.getDoubleValue();
				nonTimeAxisMinAutoAdjustValue.setValue(maxValue - spanValue);
				nonTimeAxisMaxAutoAdjustValue.setValue(minValue + spanValue);
			}
		} catch (ParseException e) {
			logger.error("Plot control panel: Could not parse non-time span value.");
		}

		if (!(nonTimeAxisMinAutoAdjust.isSelected() || nonTimeAxisMaxAutoAdjust.isSelected())) {
			double difference = getNonTimeMaxValue() - getNonTimeMinValue();
			nonTimeSpanValue.setValue(difference);
		}

		updateMainButtons();
	}

	private boolean isValidTimeAxisValues() {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(plotViewManifestion.getCurrentMCTTime());
		// If the time from MCT is invalid, then we do not evaluate Time Span
		if (gc.get(Calendar.YEAR) <= 1970) {
			return true;
		}
		
		if (timeAxisMinCurrent.isSelected() && timeAxisMaxCurrent.isSelected()
				&& timeSpanValue.getText().equals("000/00:00:00") ) {
			return true;
		}
		
		// For valid MCT times, evaluate the value in Time Span
		return timeSpanValue.getSubYearValue() > 0;
	}

	private boolean isValidNonTimeAxisValues() {
		try {
			if (nonTimeSpanValue.getText().isEmpty()) {
				return false;
			} else if (!nonTimeMaxPadding.getText().isEmpty() && !nonTimeMinPadding.getText().isEmpty() && 
					Integer.parseInt(nonTimeMaxPadding.getText()) + Integer.parseInt(nonTimeMinPadding.getText()) > 99) {
				return false;
			}
			
			// When the plot has no data, the current min and max may be the same value,
			// usually zero. This should not disable the Apply and Reset buttons.
			if (nonTimeAxisMinCurrent.isSelected() && nonTimeAxisMaxCurrent.isSelected()
					&& nonTimeSpanValue.getDoubleValue() == 0.0 ) {
				return true;
			}
			// If the plot has data, then check that the Span value is greater than zero.
			if (nonTimeSpanValue.getDoubleValue() <= 0.0) {
				return false;
			}
		} catch (ParseException e) {
			logger.error("Could not parse the non-time span's value");
		}
		return true;
	}

	/**
	 * This method checks the values in the axis controls and then enables/disables the
	 * Apply button.
	 * @param isPanelDirty 
	 */
	private void updateMainButtons() {
		
		// Apply button enable/disable.
		if (isPanelDirty() && isValidTimeAxisValues() && isValidNonTimeAxisValues()) {
			okButton.setEnabled(true);
			okButton.setToolTipText(BUNDLE.getString("ApplyPanelSettingToPlot.label"));
			
			resetButton.setEnabled(true);
			resetButton.setToolTipText(BUNDLE.getString("ResetPanelSettingToMatchPlot.label"));
		} else {
			okButton.setEnabled(false);
			
			
			// Set tool tip to explain why the control is disabled.
			StringBuilder toolTip = new StringBuilder("<HTML>" + BUNDLE.getString("ApplyButtonIsInActive.label") + ":<BR>");
	
			if (!isPanelDirty()) {
				toolTip.append(BUNDLE.getString("NoChangedMadeToPanel.label") + "<BR>");
			}
		
			if(!isValidTimeAxisValues()) {
				toolTip.append(BUNDLE.getString("TimeAxisValuesInvalid.label") + "<BR>");
			}
			
			if(!isValidNonTimeAxisValues()) {
				toolTip.append(BUNDLE.getString("NonTimeAxisValuesInvalid.label") + "<BR>");
			}
	        toolTip.append("</HTML>");
			okButton.setToolTipText(toolTip.toString());
			
			resetButton.setEnabled(false);
			resetButton.setToolTipText(BUNDLE.getString("ResetButtonInactiveBecauseNoChangesMade.label"));
		}
	}


	/**
	 * Update the label representing the time axis' Min + Span value
	 * Selections are: Min Manual button, Max Auto ("Min + Span") button
	 */
	public void refreshDisplay() {
		// Update the MCT time ("Now")
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(plotViewManifestion.getCurrentMCTTime());
		timeAxisMinAutoValue.setTime(gc);

		// Update the time min/max values			
		nonTimeAxisMinCurrentValue.setValue(plotViewManifestion.getMinFeedValue());
		nonTimeAxisMaxCurrentValue.setValue(plotViewManifestion.getMaxFeedValue());
		
		updateTimeAxisControls();
		updateNonTimeAxisControls();
	}
	
	// Initially returns float; but shouldn't this be double precision (?)
	double getNonTimeMaxValue() {
		if (nonTimeAxisMaxCurrent.isSelected()) {
			return nonTimeAxisMaxCurrentValue.getValue().floatValue();
		} else if (nonTimeAxisMaxManual.isSelected()) {
			//float result = 0;
			double result = 1.0;
			try {
				result = nonTimeAxisMaxManualValue.getDoubleValue().floatValue();
			} catch (ParseException e) {
				logger.error("Plot control panel: Could not read the non-time axis' maximum manual value");
			}
			
			return result;
		} else if (nonTimeAxisMaxAutoAdjust.isSelected()) {
			
			return nonTimeAxisMaxAutoAdjustValue.getValue().floatValue();
		}
		return 1.0;
	}

	double getNonTimeMinValue() {
		if (nonTimeAxisMinCurrent.isSelected()) {
			return nonTimeAxisMinCurrentValue.getValue().floatValue();
		} else if (nonTimeAxisMinManual.isSelected()) {
			double result = 0.0;
			try {
				result = nonTimeAxisMinManualValue.getDoubleValue().floatValue();
				
			} catch (ParseException e) {
				logger.error("Plot control panel: Could not read the non-time axis' minimum manual value");
			}
			return result;
		} else if (nonTimeAxisMinAutoAdjust.isSelected()) {
			
			return nonTimeAxisMinAutoAdjustValue.getValue().floatValue();
		}
		return 0.0;
	}

	/**
	 * Gets the PlotViewManifestation associated with this control panel.
	 * @return the PlotViewManifesation
	 */
	public PlotViewManifestation getPlot() {
		return plotViewManifestion;
	}

	// The Initial Settings panel
	// Name change: Initial Settings is now labeled Min/Max Setup
	private JPanel getInitialSetupPanel() {
		JPanel initialSetup = new JPanel();
		initialSetup.setLayout(new BoxLayout(initialSetup, BoxLayout.Y_AXIS));
		initialSetup.setBorder(SETUP_AND_BEHAVIOR_MARGINS);

		yAxisType = new JLabel("(NON-TIME)");
        JPanel yAxisTypePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        yAxisTypePanel.add(yAxisType);

        imagePanel = new StillPlotImagePanel();

        // Start defining the top panel
		JPanel initTopPanel = createTopPanel();

        // Assemble the bottom panel
        JPanel initBottomPanel = new JPanel();
        initBottomPanel.setLayout(new GridBagLayout());
		initBottomPanel.setBorder(TOP_PADDED_MARGINS);

		JPanel yAxisPanelSet = createYAxisPanelSet();

        JPanel xAxisPanelSet = createXAxisPanelSet();
        
        JPanel yAxisControlsPanel = new JPanel();
        yAxisControlsPanel.setLayout(new BoxLayout(yAxisControlsPanel, BoxLayout.Y_AXIS));

        yAxisPanelSet.setAlignmentX(Component.CENTER_ALIGNMENT);
        yAxisControlsPanel.add(yAxisPanelSet);

        JPanel xAxisControlsPanel = new JPanel(new GridLayout(1, 1));
        xAxisControlsPanel.add(xAxisPanelSet);

        // The title label for (TIME) or (NON-TIME)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        initBottomPanel.add(yAxisTypePanel, gbc);

        // The Y Axis controls panel
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 1;
        gbc1.gridwidth = 1;
        gbc1.gridheight = 3;
        gbc1.fill = GridBagConstraints.BOTH;
        // To align the "Min" or "Max" label with the bottom of the static plot image,
        // add a vertical shim under the Y Axis bottom button set and "Min"/"Max" label.
        gbc1.insets = new Insets(2, 0, 10, 2); 
        initBottomPanel.add(yAxisControlsPanel, gbc1);

        // The static plot image
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 1;
        gbc2.gridwidth = 3;
        gbc2.gridheight = 3;
        initBottomPanel.add(imagePanel, gbc2);

        // The X Axis controls panel
        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 1;
        gbc3.gridy = 4;
        gbc3.gridwidth = 3;
        gbc3.gridheight = 1;
        gbc3.fill = GridBagConstraints.BOTH;
        gbc3.insets = new Insets(0, 8, 0, 0);
        initBottomPanel.add(xAxisControlsPanel, gbc3);

		// Assemble the major panel: Initial Settings
        JPanel topClamp = new JPanel(new BorderLayout());
        topClamp.add(initTopPanel, BorderLayout.NORTH);
        JPanel bottomClamp = new JPanel(new BorderLayout());
        bottomClamp.add(initBottomPanel, BorderLayout.NORTH);
        JPanel sideClamp = new JPanel(new BorderLayout());
        sideClamp.add(bottomClamp, BorderLayout.WEST);

        initialSetup.add(Box.createRigidArea(new Dimension(0, INNER_PADDING)));
		initialSetup.add(topClamp);
		initialSetup.add(Box.createRigidArea(new Dimension(0, INNER_PADDING)));
		initialSetup.add(new JSeparator());
        initialSetup.add(sideClamp);

        // Instrument
        initialSetup.setName("initialSetup");
        initTopPanel.setName("initTopPanel");
        initBottomPanel.setName("initBottomPanel");
        yAxisPanelSet.setName("yAxisPanelSet");
        xAxisPanelSet.setName("xAxisPanelSet");
        yAxisControlsPanel.setName("yAxisInnerPanel");
        xAxisControlsPanel.setName("nontimeSidePanel");
        topClamp.setName("topClamp");
        bottomClamp.setName("bottomClamp");

		return initialSetup;
	}

	// Top panel that controls which axis plots Time and the direction of each axis
	private JPanel createTopPanel() {
		JPanel initTopPanel = new JPanel();
		initTopPanel.setLayout(new GridBagLayout());

		// Time Systems and Formats
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS)); 
		timePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		JPanel timeSystemPanel = new JPanel();
		timeSystemPanel.setLayout(new BoxLayout(timeSystemPanel, BoxLayout.X_AXIS)); 
		timeSystemPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 0));
		
		JPanel timeFormatsPanel = new JPanel();
		timeFormatsPanel.setLayout(new BoxLayout(timeFormatsPanel, BoxLayout.X_AXIS)); 
		timeFormatsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 0));
		
		timePanel.add(timeSystemPanel);
		timePanel.add(timeFormatsPanel);
		
		String notApplicable = BUNDLE.getString("Time.label");
		String [] choices = getComponentSpecifiedTimeSystemChoices();
		if (choices == null || choices.length <= 0) {
			timeSystemDropdown = new JComboBox(new String[]{TimeService.DEFAULT_TIME_SYSTEM});
			timeSystemDropdown.setFocusable(false);
			timeSystemDropdown.setEnabled(false);
		} else {
			timeSystemDropdown = new JComboBox(choices);
		}
		timeSystemDropdown.setSelectedItem(plotViewManifestion.getPlot().getTimeSystem());
		ssTimeSystemSelection = (String) timeSystemDropdown.getSelectedItem();
		timeSystemPanel.add(new JLabel(BUNDLE.getString("TimeSystem.label")));
		timeSystemPanel.add(timeSystemDropdown);
		
		choices = getComponentSpecifiedTimeFormatChoices();
		if (choices == null || choices.length <= 0) {
			timeFormatDropdown = new JComboBox(new String[]{TimeService.DEFAULT_TIME_FORMAT});
			timeFormatDropdown.setFocusable(false);
			timeFormatDropdown.setEnabled(false);
		} else {
			timeFormatDropdown = new JComboBox(choices);
		}
		timeFormatDropdown.setSelectedItem(plotViewManifestion.getPlot().getTimeFormat());
		ssTimeFormatSelection = (String) timeFormatDropdown.getSelectedItem();
		timeFormatsPanel.add(new JLabel(BUNDLE.getString("TimeFormat.label")));
		timeFormatsPanel.add(timeFormatDropdown);
		
        xAxisAsTimeRadioButton = new JRadioButton(BUNDLE.getString("XAxisAsTime.label"));
    	yAxisAsTimeRadioButton = new JRadioButton(BUNDLE.getString("YAxisAsTime.label"));

        // Middle column
        JLabel xDirTitle = new JLabel(BUNDLE.getString("XAxis.label"));
        xMaxAtRight = new JRadioButton(BUNDLE.getString("MaxAtRight.label"));
        xMaxAtLeft = new JRadioButton(BUNDLE.getString("MaxAtLeft.label"));

        // Right column
        JLabel yDirTitle = new JLabel(BUNDLE.getString("YAxis.label"));
        yMaxAtTop = new JRadioButton(BUNDLE.getString("MaxAtTop.label"));
        yMaxAtBottom = new JRadioButton(BUNDLE.getString("MaxAtBottom.label"));

        // Separator lines for the top panel
        JSeparator separator1 = new JSeparator(SwingConstants.VERTICAL);
        JSeparator separator2 = new JSeparator(SwingConstants.VERTICAL);

        // Assemble the top row of cells (combo box, 2 separators and 2 titles)
        GridBagConstraints gbcTopA = new GridBagConstraints();
        gbcTopA.anchor = GridBagConstraints.WEST;
        initTopPanel.add(timePanel, gbcTopA);

        GridBagConstraints gbcSep1 = new GridBagConstraints();
        gbcSep1.gridheight = 3;
        gbcSep1.fill = GridBagConstraints.BOTH;
        gbcSep1.insets = new Insets(0, 5, 0, 5);
        initTopPanel.add(separator1, gbcSep1);

        GridBagConstraints gbcTopB = new GridBagConstraints();
        gbcTopB.anchor = GridBagConstraints.WEST;
        gbcTopB.insets = new Insets(0, 4, 0, 0);
        initTopPanel.add(xDirTitle, gbcTopB);

        initTopPanel.add(separator2, gbcSep1);
        initTopPanel.add(yDirTitle, gbcTopB);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 1;
        gbc.gridx = 0;
        initTopPanel.add(xAxisAsTimeRadioButton, gbc);
        gbc.gridx = 2;
        initTopPanel.add(xMaxAtRight, gbc);
        gbc.gridx = 4;
        initTopPanel.add(yMaxAtTop, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 0, 0, 0);
        initTopPanel.add(yAxisAsTimeRadioButton, gbc);
        gbc.gridx = 2;
        initTopPanel.add(xMaxAtLeft, gbc);
        gbc.gridx = 4;
        initTopPanel.add(yMaxAtBottom, gbc);
        
        // add stacked plot grouping
        GridBagConstraints groupingGbc = new GridBagConstraints();
        groupingGbc.gridheight = 3;
        groupingGbc.fill = GridBagConstraints.BOTH;
        groupingGbc.insets = new Insets(0, 5, 0, 5);
        initTopPanel.add(new JSeparator(JSeparator.VERTICAL), groupingGbc);
        // Stacked Plot Grouping Label
        groupingGbc.gridheight = 1;
        groupingGbc.gridy = 0;
        initTopPanel.add(new JLabel(BUNDLE.getString("StackedPlotGroping.label")),groupingGbc);
        groupingGbc.gridy = 1;
        groupByCollection = new JCheckBox(BUNDLE.getString("GroupByCollection.label"));
        groupByCollection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateMainButtons();
			}
        });
        initTopPanel.add(groupByCollection,groupingGbc);
        
        // Add listeners and set initial state
        addTopPanelListenersAndState();

        // Instrument
        initTopPanel.setName("initTopPanel");
        timePanel.setName("timePanel");

        JPanel horizontalSqueeze = new JPanel(new BorderLayout());
        horizontalSqueeze.add(initTopPanel, BorderLayout.WEST);
        return horizontalSqueeze;
	}
	
	String[] getComponentSpecifiedTimeSystemChoices() {
		return (plotViewManifestion != null) ?
				plotViewManifestion.getTimeSystemChoices() :  new String[] { PlotConstants.DEFAULT_TIME_SYSTEM };
	}

	String[] getComponentSpecifiedTimeFormatChoices() {
		return (plotViewManifestion != null) ?
				plotViewManifestion.getTimeFormatChoices() : new String[] { PlotConstants.DEFAULT_TIME_FORMAT };
	}


	private void addTopPanelListenersAndState() {
		xAxisAsTimeRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xAxisAsTimeRadioButtonActionPerformed();
			}
		});

		yAxisAsTimeRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yAxisAsTimeRadioButtonActionPerformed();

			}
		});

        xMaxAtRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xMaxAtRightActionPerformed();
			}
		});
        xMaxAtLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				xMaxAtLeftActionPerformed(); 
			}
		});

        yMaxAtTop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yMaxAtTopActionPerformed();
			}
		});
        yMaxAtBottom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				yMaxAtBottomActionPerformed();
			}
		});

		xAxisAsTimeRadioButton.setSelected(true);
		xMaxAtRight.setSelected(true);
        yMaxAtTop.setSelected(true);

        // Button Groups
        ButtonGroup timenessGroup = new ButtonGroup();
        timenessGroup.add(xAxisAsTimeRadioButton);
        timenessGroup.add(yAxisAsTimeRadioButton);
        ButtonGroup xDirectionGroup = new ButtonGroup();
        xDirectionGroup.add(xMaxAtRight);
        xDirectionGroup.add(xMaxAtLeft);
        ButtonGroup yDirectionGroup = new ButtonGroup();
        yDirectionGroup.add(yMaxAtTop);
        yDirectionGroup.add(yMaxAtBottom);
	}

	private void updateStaticAreaWithTimeSystem() {
		xAxisType.setText("(" + getSelectedTimeSystem() + ")");
	}
	
	private void xAxisAsTimeRadioButtonActionPerformed() {
		 // Move the time axis panels to the x axis class
		 // Move the nontime axis panels to the y axis class
		 xAxisButtonsPanel.insertMinMaxPanels(timeAxisMinimumsPanel, timeAxisMaximumsPanel);
		 yAxisButtonsPanel.insertMinMaxPanels(nonTimeAxisMinimumsPanel, nonTimeAxisMaximumsPanel);
		 xAxisSpanCluster.setSpanField(timeSpanValue); //Panel);
		 yAxisSpanPanel.setSpanField(nonTimeSpanValue);
		 if (yMaxAtTop.isSelected()) {
			 yAxisButtonsPanel.setNormalOrder(true);
		 } else {
			 yAxisButtonsPanel.setNormalOrder(false);
		 }

		 if (xMaxAtRight.isSelected()) {
			 xMaxAtRight.doClick();
		 } else {
			 xMaxAtLeft.doClick();
		 }
		 xAxisType.setText("(" + ssTimeSystemSelection + ")");
		 yAxisType.setText("(" + BUNDLE.getString("NonTime.label") + ")");
		 imagePanel.setImageToTimeOnXAxis(xMaxAtRight.isSelected());
		 behaviorTimeAxisLetter.setText(BUNDLE.getString("X.label"));
		 behaviorNonTimeAxisLetter.setText(BUNDLE.getString("Y.label"));	
     }
	
	private void yAxisAsTimeRadioButtonActionPerformed() {
		xAxisButtonsPanel.insertMinMaxPanels(nonTimeAxisMinimumsPanel, nonTimeAxisMaximumsPanel);
		yAxisButtonsPanel.insertMinMaxPanels(timeAxisMinimumsPanel, timeAxisMaximumsPanel);
		xAxisSpanCluster.setSpanField(nonTimeSpanValue);
		yAxisSpanPanel.setSpanField(timeSpanValue); //Panel);
		if (yMaxAtTop.isSelected()) {
			yAxisButtonsPanel.setNormalOrder(true);
		} else {
			yAxisButtonsPanel.setNormalOrder(false);
		}

        if (xMaxAtRight.isSelected()) {
            xMaxAtRight.doClick();
        } else {
            xMaxAtLeft.doClick();
        }
		xAxisType.setText("(" + BUNDLE.getString("NonTime.label") + ")");
		yAxisType.setText("(" + BUNDLE.getString("Time.label") + ")");
		imagePanel.setImageToTimeOnYAxis(yMaxAtTop.isSelected());
		behaviorTimeAxisLetter.setText(BUNDLE.getString("Y.label"));
		behaviorNonTimeAxisLetter.setText(BUNDLE.getString("X.label"));
	}
	
	private void xMaxAtRightActionPerformed() {
		xAxisAdjacentPanel.setNormalOrder(true);
		xAxisButtonsPanel.setNormalOrder(true);
		if (xAxisAsTimeRadioButton.isSelected()) {
			imagePanel.setImageToTimeOnXAxis(true); // normal direction
		}
	}
	
	private void xMaxAtLeftActionPerformed() {
		xAxisAdjacentPanel.setNormalOrder(false);
		xAxisButtonsPanel.setNormalOrder(false);
		if (xAxisAsTimeRadioButton.isSelected()) {
			imagePanel.setImageToTimeOnXAxis(false); // reverse direction
		}
	}
	
	private void yMaxAtTopActionPerformed() {
		yAxisButtonsPanel.setNormalOrder(true);
		if (yAxisAsTimeRadioButton.isSelected()) {
			imagePanel.setImageToTimeOnYAxis(true); // normal direction
		}
	}
	
	private void yMaxAtBottomActionPerformed() {
		yAxisButtonsPanel.setNormalOrder(false);
		if (yAxisAsTimeRadioButton.isSelected()) {
			imagePanel.setImageToTimeOnYAxis(false); // reverse direction
		}
	}
	
    // The controls that exactly align with the X axis
	private JPanel createXAxisPanelSet() {
		xAxisSpanCluster = new XAxisSpanCluster();
		xAxisAdjacentPanel = new XAxisAdjacentPanel();
		xAxisAdjacentPanel.setNormalOrder(true);
		xAxisButtonsPanel = new XAxisButtonsPanel();
		timeAxisMinimumsPanel = new TimeAxisMinimumsPanel();
		timeAxisMaximumsPanel = new TimeAxisMaximumsPanel();
	

		xAxisButtonsPanel.insertMinMaxPanels(timeAxisMinimumsPanel, timeAxisMaximumsPanel);
		xAxisButtonsPanel.setNormalOrder(true);

		JPanel belowXAxisPanel = new JPanel();
		belowXAxisPanel.setLayout(new BoxLayout(belowXAxisPanel, BoxLayout.Y_AXIS));
		belowXAxisPanel.add(xAxisAdjacentPanel);
		belowXAxisPanel.add(xAxisButtonsPanel);
        belowXAxisPanel.add(Box.createVerticalStrut(X_AXIS_TYPE_VERTICAL_SPACING));
        xAxisType = new JLabel("("+ ssTimeSystemSelection +")");
		belowXAxisPanel.add(xAxisType);

		xAxisType.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Instrument
		belowXAxisPanel.setName("belowXAxisPanel");

		return belowXAxisPanel;
	}

    // The controls that exactly align with the Y axis
	private JPanel createYAxisPanelSet() {
	    yMaximumsPlusPanel = new YMaximumsPlusPanel();
	    yAxisSpanPanel = new YAxisSpanPanel();
	    yMinimumsPlusPanel = new YMinimumsPlusPanel();

        yAxisButtonsPanel = new YAxisButtonsPanel();
        yAxisButtonsPanel.insertMinMaxPanels(nonTimeAxisMinimumsPanel, nonTimeAxisMaximumsPanel);
        yAxisButtonsPanel.setNormalOrder(true);

        return yAxisButtonsPanel;
	}

	// Convenience method for populating and applying a standard layout for multi-item rows
    private JPanel createMultiItemRow(JRadioButton button, JComponent secondItem) {
    	JPanel panel = new JPanel();
    	panel.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.anchor = GridBagConstraints.BASELINE_LEADING;
    	if (button != null) {
    		button.setSelected(false);
    		panel.add(button, gbc);
    	}
    	if (secondItem != null) {
    		gbc.gridx = 1;
    		gbc.insets = new Insets(0, SPACE_BETWEEN_ROW_ITEMS, 0, 0);
    		gbc.weightx = 1;
    		panel.add(secondItem, gbc);
    	}
    	panel.setName("multiItemRow");
		return panel;
	}

    // The Plot Behavior panel
	private JPanel getPlotBehaviorPanel() {
        JPanel plotBehavior = new JPanel();
        plotBehavior.setLayout(new GridBagLayout());
        plotBehavior.setBorder(SETUP_AND_BEHAVIOR_MARGINS);

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
    	pinTimeAxis = new JCheckBox(BUNDLE.getString("PinTimeAxis.label"));
    	behaviorTimeTitlePanel.add(pinTimeAxis);

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
    	plotBehavior.add(behaviorTimeTitlePanel, gbc);
    	gbc.gridy++;
    	gbc.insets = new Insets(0, 0, 0, 0);
    	plotBehavior.add(timeAxisPanel, gbc);
    	gbc.gridy++;
    	gbc.insets = new Insets(6, 0, 0, 0);
    	plotBehavior.add(behaviorNonTimeTitlePanel, gbc);
    	gbc.gridy++;
    	gbc.insets = new Insets(0, 0, 0, 0);
    	plotBehavior.add(nonTimeAxisPanel, gbc);

    	// Instrument
    	plotBehavior.setName("plotBehavior");
    	modePanel.setName("modePanel");
    	bMode.setName("bMode");
    	minPanel.setName("minPanel");
    	bMin.setName("bMin");
    	maxPanel.setName("maxPanel");    	
    	timeAxisPanel.setName("timeAxisPanel");
    	nonTimeAxisPanel.setName("nonTimeAxisPanel");
    	behaviorTimeAxisLetter.setName("behaviorTimeAxisLetter");
    	behaviorNonTimeAxisLetter.setName("behaviorNonTimeAxisLetter");

    	JPanel stillBehavior = new JPanel(new BorderLayout());
    	stillBehavior.add(plotBehavior, BorderLayout.WEST);
    	return stillBehavior;
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
			if (insertBuilder.length() + fb.getDocument().getLength() < 3) {
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
			if (replaceBuilder.length() - length + fb.getDocument().getLength() < 3) {
				super.replace(fb, offset, length, replaceBuilder.toString(), attr);
			}
		}

		StringBuilder getInsertBuilder() {
			return insertBuilder;
		}

		StringBuilder getReplaceBuilder() {
			return replaceBuilder;
		}
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

	static class GridLinedPanel extends JPanel {
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
    
	private JPanel getLineSetupPanel() {
		JPanel panel = new JPanel();
		
		drawLabel = new JLabel(BUNDLE.getString("Draw.label"));
		linesOnly = new JRadioButton(BUNDLE.getString("LinesOnly.label"));
		markersAndLines = new JRadioButton(BUNDLE.getString("MarkersAndLines.label"));
		markersOnly = new JRadioButton(BUNDLE.getString("MarkersOnly.label"));
		
		connectionLineTypeLabel = new JLabel(BUNDLE.getString("ConnectionLineType.label"));
		direct = new JRadioButton(BUNDLE.getString("Direct.label"));
		step = new JRadioButton(BUNDLE.getString("Step.label"));
		direct.setToolTipText(BUNDLE.getString("Direct.tooltip"));
		step.setToolTipText(BUNDLE.getString("Step.tooltip"));
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipady = 4;
		gbc.ipadx = BEHAVIOR_CELLS_X_PADDING;
		gbc.anchor = GridBagConstraints.WEST;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth  = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(drawLabel, gbc);
		gbc.gridy++;
		panel.add(linesOnly, gbc);
		gbc.gridy++;
		panel.add(markersAndLines, gbc);
		gbc.gridy++;
		panel.add(markersOnly, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 4;
		gbc.fill = GridBagConstraints.VERTICAL;
		panel.add(new JSeparator(JSeparator.VERTICAL), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth  = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(connectionLineTypeLabel, gbc);
		gbc.gridy++;
		panel.add(direct, gbc);
		gbc.gridy++;
		panel.add(step, gbc);
		
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.add(panel, BorderLayout.WEST);
		parent.setBorder(SETUP_AND_BEHAVIOR_MARGINS);
		
		ButtonGroup drawGroup = new ButtonGroup();
		drawGroup.add(linesOnly);
		drawGroup.add(markersAndLines);
		drawGroup.add(markersOnly);

		ButtonGroup connectionGroup = new ButtonGroup();
		connectionGroup.add(direct);
		connectionGroup.add(step);

		ActionListener disabler = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 boolean linesShowing = !markersOnly.isSelected();
				 connectionLineTypeLabel.setEnabled(linesShowing);
				 direct.setEnabled(linesShowing);
				 step.setEnabled(linesShowing);
			}			
		};
		
		linesOnly.addActionListener(disabler);
		markersAndLines.addActionListener(disabler);
		markersOnly.addActionListener(disabler);
		
		return parent;
	}
	
	
	
	
    /**
     * Set the state of the widgets in the setting panel according to those specified in the parameters.
     * @param timeAxisSetting
     * @param timeSystem
     * @param timeFormat
     * @param xAxisMaximumLocation
     * @param yAxisMaximumLocation
     * @param timeAxisSubsequentSetting
     * @param nonTimeAxisSubsequentMinSetting
     * @param nonTimeAxisSubsequentMaxSetting
     * @param nonTimeMax
     * @param nonTimeMin
     * @param minTime
     * @param maxTime
     * @param timePadding
     * @param plotLineDraw indicates how to draw the plot (whether to include lines, markers, etc)
	 * @param plotLineConnectionType the method of connecting lines on the plot
     */
    public void setControlPanelState(AxisOrientationSetting timeAxisSetting,
    		String timeSystem,
            String timeFormat,
    		XAxisMaximumLocationSetting xAxisMaximumLocation,
			YAxisMaximumLocationSetting yAxisMaximumLocation,
			TimeAxisSubsequentBoundsSetting timeAxisSubsequentSetting,
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMinSetting,
			NonTimeAxisSubsequentBoundsSetting nonTimeAxisSubsequentMaxSetting,
			double nonTimeMax, double nonTimeMin, long minTime,
		    long maxTime, 
			double timePadding,
			double nonTimePaddingMax,
			double nonTimePaddingMin, 
			boolean groupStackPlotsByOrdinalPosition, boolean timeAxisPinned,
			PlotLineDrawingFlags plotLineDraw,
			PlotLineConnectionType plotLineConnectionType) {
    	
    	if (plotViewManifestion.getPlot() == null) {
			throw new IllegalArgumentException("Plot Setting control Panel cannot be setup if the PltViewManifestation's plot is null");
		}
       	
    	pinTimeAxis.setSelected(timeAxisPinned);
    	
    	assert nonTimeMin < nonTimeMax : "Non Time min >= Non Time Max";
    	assert minTime < maxTime : "Time min >= Time Max " + minTime + " " + maxTime;
    	
    	timeSystemDropdown.setSelectedItem(timeSystem == null ? TimeService.DEFAULT_TIME_SYSTEM : timeSystem);
        timeFormatDropdown.setSelectedItem(timeFormat == null ? TimeService.DEFAULT_TIME_FORMAT : timeFormat);
	
    	// Setup time axis setting. 
    	if (timeAxisSetting ==  AxisOrientationSetting.X_AXIS_AS_TIME) {
    		xAxisAsTimeRadioButton.setSelected(true);
    		yAxisAsTimeRadioButton.setSelected(false);
    		xAxisAsTimeRadioButtonActionPerformed();
    	} else if (timeAxisSetting ==  AxisOrientationSetting.Y_AXIS_AS_TIME) {
    		xAxisAsTimeRadioButton.setSelected(false);
    		yAxisAsTimeRadioButton.setSelected(true);
    		yAxisAsTimeRadioButtonActionPerformed();
    	} else {
    		assert false :"Time must be specified as being on either the X or Y axis.";
    	}	
    	
    	// X Max setting
    	if (xAxisMaximumLocation == XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT) {
    		xMaxAtRight.setSelected(true);
    		xMaxAtLeft.setSelected(false);
    		xMaxAtRightActionPerformed();
        } else if (xAxisMaximumLocation == XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT) {
        	xMaxAtRight.setSelected(false);
    		xMaxAtLeft.setSelected(true);
    		xMaxAtLeftActionPerformed();
    	} else {
    		assert false: "X max location must be set.";
	 	}
    	  
    	// Y Max setting
    	if (yAxisMaximumLocation == YAxisMaximumLocationSetting.MAXIMUM_AT_TOP) {
    		yMaxAtTop.setSelected(true);
    		yMaxAtBottom.setSelected(false);
    		yMaxAtTopActionPerformed();
    	} else if (yAxisMaximumLocation == YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM) {
    		yMaxAtTop.setSelected(false);
    		yMaxAtBottom.setSelected(true);
    		yMaxAtBottomActionPerformed();
    	} else {
    		assert false: "Y max location must be set.";
    	}
    	
    	if (timeAxisSubsequentSetting == TimeAxisSubsequentBoundsSetting.JUMP) {
    		timeJumpMode.setSelected(true);
    		timeScrunchMode.setSelected(false);	

    		timeAxisMaxAuto.setSelected(false);
    		timeAxisMaxManual.setSelected(false);
    		timeAxisMaxCurrent.setSelected(true);
    		timeAxisMinAuto.setSelected(false);
    		timeAxisMinManual.setSelected(false);
    		timeAxisMinCurrent.setSelected(true);

    		workCalendar.setTime(plotViewManifestion.getPlot().getMaxTime().getTime());
    		timeAxisMaxManualValue.setTime(workCalendar);

    		workCalendar.setTime(plotViewManifestion.getPlot().getMinTime().getTime());
    		timeAxisMinManualValue.setTime(workCalendar);
    	    
    	} else if (timeAxisSubsequentSetting ==  TimeAxisSubsequentBoundsSetting.SCRUNCH) {
    		timeJumpMode.setSelected(false);
    		timeScrunchMode.setSelected(true);
    	 		
    		timeAxisMaxAuto.setSelected(false);
    		timeAxisMaxManual.setSelected(false);
    		timeAxisMaxCurrent.setSelected(true);
    		timeAxisMinAuto.setSelected(false);
    		timeAxisMinManual.setSelected(false);
    		timeAxisMinCurrent.setSelected(true); 
    		
    		GregorianCalendar minCalendar = new GregorianCalendar();
    		minCalendar.setTimeZone(getStaticAreaTimeFormat().getTimeZone());
     		minCalendar.setTimeInMillis(minTime);
     		timeAxisMinManualValue.setTime(minCalendar);	
    		
    	} else {
           assert false : "No time subsequent mode selected"; 
    	}
    	timeScrunchPadding.setText(timePaddingFormat.format(timePadding * 100));
    	timeJumpPadding.setText(timePaddingFormat.format(timePadding * 100)); 
    	
    	// Set the Current Min and Max values
		timeAxisMinCurrentValue.setTime(plotViewManifestion.getPlot().getMinTime());
		timeAxisMaxCurrentValue.setTime(plotViewManifestion.getPlot().getMaxTime());

    	// Non Time Subsequent Settings
    	
    	// Min
    	if (NonTimeAxisSubsequentBoundsSetting.AUTO == nonTimeAxisSubsequentMinSetting) {
    		nonTimeMinAutoAdjustMode.setSelected(true);
    		nonTimeMinFixedMode.setSelected(false);
    		nonTimeMinSemiFixedMode.setSelected(false);
    		nonTimeMinSemiFixedMode.setEnabled(false);
    	} else if (NonTimeAxisSubsequentBoundsSetting.FIXED == nonTimeAxisSubsequentMinSetting) {
    		nonTimeMinAutoAdjustMode.setSelected(false);
			nonTimeMinFixedMode.setSelected(true);
			nonTimeMinSemiFixedMode.setSelected(false);
			nonTimeMinSemiFixedMode.setEnabled(true);
    	} else if (NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED == nonTimeAxisSubsequentMinSetting) {
    		nonTimeMinAutoAdjustMode.setSelected(false);
			nonTimeMinFixedMode.setSelected(true);
			nonTimeMinSemiFixedMode.setSelected(true);
			nonTimeMinSemiFixedMode.setEnabled(true);
        } else {
    		assert false : "No non time min subsequent setting specified";
    	}
    		
    	// Max
    	if (NonTimeAxisSubsequentBoundsSetting.AUTO == nonTimeAxisSubsequentMaxSetting) {
			nonTimeMaxAutoAdjustMode.setSelected(true);
			nonTimeMaxFixedMode.setSelected(false);
			nonTimeMaxSemiFixedMode.setSelected(false);
			nonTimeMaxSemiFixedMode.setEnabled(false);
	    } else if (NonTimeAxisSubsequentBoundsSetting.FIXED == nonTimeAxisSubsequentMaxSetting) {
	    	nonTimeMaxAutoAdjustMode.setSelected(false);
		    nonTimeMaxFixedMode.setSelected(true);
		    nonTimeMaxSemiFixedMode.setSelected(false);
		    nonTimeMaxSemiFixedMode.setEnabled(true);
	    }  else if (NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED == nonTimeAxisSubsequentMaxSetting) {
	    		nonTimeMaxAutoAdjustMode.setSelected(false);
				nonTimeMaxFixedMode.setSelected(true);
				nonTimeMaxSemiFixedMode.setSelected(true);
				nonTimeMaxSemiFixedMode.setEnabled(true);
	    } else {
		    assert false : "No non time max subsequent setting specified";
	    }
      
    	// Non time Axis Settings.
    	// Non-Time Min. Control Panel
    	if (NonTimeAxisSubsequentBoundsSetting.AUTO == nonTimeAxisSubsequentMinSetting) {
    		nonTimeAxisMinCurrent.setSelected(true);
    		nonTimeAxisMinManual.setSelected(false);
    		nonTimeAxisMinAutoAdjust.setSelected(false);
    	
    	} else if (NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED == nonTimeAxisSubsequentMinSetting
    				|| NonTimeAxisSubsequentBoundsSetting.FIXED == nonTimeAxisSubsequentMinSetting) {
					
			nonTimeAxisMinCurrent.setSelected(false);
			if (!nonTimeAxisMinManual.isSelected()) {
				nonTimeAxisMinManual.setSelected(true);
			}	
    		nonTimeAxisMinAutoAdjust.setSelected(false);
    	} 
    	nonTimeMinPadding.setText(nonTimePaddingFormat.format(nonTimePaddingMin * 100));
    	nonTimeAxisMinManualValue.setText(nonTimePaddingFormat.format(nonTimeMin));
    	
    	// Non-Time Max. Control Panel
    	if (NonTimeAxisSubsequentBoundsSetting.AUTO == nonTimeAxisSubsequentMaxSetting) {
    		nonTimeAxisMaxCurrent.setSelected(true);
    		nonTimeAxisMaxManual.setSelected(false);
    		nonTimeAxisMaxAutoAdjust.setSelected(false);
        	
    	} else if (NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED == nonTimeAxisSubsequentMaxSetting
	    				|| NonTimeAxisSubsequentBoundsSetting.FIXED == nonTimeAxisSubsequentMaxSetting) {
			nonTimeAxisMaxCurrent.setSelected(false);
			if (!nonTimeAxisMaxManual.isSelected()) {
				nonTimeAxisMaxManual.setSelected(true);
			}
    		nonTimeAxisMaxAutoAdjust.setSelected(false);
    	} 
    	nonTimeMaxPadding.setText(nonTimePaddingFormat.format(nonTimePaddingMax * 100));
    	nonTimeAxisMaxManualValue.setText(nonTimePaddingFormat.format(nonTimeMax));
    	
    	// Draw
    	if (plotLineDraw.drawLine() && plotLineDraw.drawMarkers()) {
    		markersAndLines.setSelected(true);
    	} else if (plotLineDraw.drawLine()) {
    		linesOnly.setSelected(true);
    	} else if (plotLineDraw.drawMarkers()) {
    		markersOnly.setSelected(true);
    	} else {
    		logger.warn("Plot line drawing configuration is unset.");
    	}
    	
    	// Connection line type
    	if (plotLineConnectionType == PlotLineConnectionType.DIRECT) {
    		direct.setSelected(true);
    	} else if (plotLineConnectionType == PlotLineConnectionType.STEP_X_THEN_Y) {
    		step.setSelected(true);
    	}
    	
    	updateTimeAxisControls();
    	groupByCollection.setSelected(!groupStackPlotsByOrdinalPosition);
    }
    
    // Get the plot setting from the GUI widgets, inform the plot controller and request a new plot.
    void setupPlot() {
    	// Axis on which time will be displayed
    	if ( xAxisAsTimeRadioButton.isSelected()  ) {
    		assert !yAxisAsTimeRadioButton.isSelected() : "Both axis location boxes are selected!";
    		controller.setTimeAxis(AxisOrientationSetting.X_AXIS_AS_TIME);
    	} else if (yAxisAsTimeRadioButton.isSelected()) {
    		controller.setTimeAxis(AxisOrientationSetting.Y_AXIS_AS_TIME);
    	} else {
    		assert false: "Time must be specified as being on either the X or Y axis.";
    	    controller.setTimeAxis(AxisOrientationSetting.X_AXIS_AS_TIME);
    	}	
    	
    	// Time System setting
        controller.setTimeSystem(getSelectedTimeSystem());

        // Time Format setting
        controller.setTimeFormat((String)timeFormatDropdown.getSelectedItem());
    	
    	// X Max Location setting
    	if (xMaxAtRight.isSelected()) {
    		assert !xMaxAtLeft.isSelected(): "Both x max location settings are selected!";
    		controller.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
    	} else if (xMaxAtLeft.isSelected()) {
    		controller.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_LEFT);
    	} else {
    		assert false: "X max location must be set.";
	        controller.setXAxisMaximumLocation(XAxisMaximumLocationSetting.MAXIMUM_AT_RIGHT);
    	}
    	  
    	// Y Max Location setting
    	if (yMaxAtTop.isSelected()) {
    		assert !yMaxAtBottom.isSelected(): "Both y max location settings are selected!";
    		controller.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
    	} else if (yMaxAtBottom.isSelected()) {
    		controller.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_BOTTOM);
    	} else {
    		assert false: "Y max location must be set.";
    		controller.setYAxisMaximumLocation(YAxisMaximumLocationSetting.MAXIMUM_AT_TOP);
    	}
    	
    	controller.setTimeAxisPinned(pinTimeAxis.isSelected());
    	
    	// Time Subsequent settings	 	
    	if (timeJumpMode.isSelected()) {
    		assert !timeScrunchMode.isSelected() : "Both jump and scruch are set!";
    		controller.setTimeAxisSubsequentBounds(TimeAxisSubsequentBoundsSetting.JUMP);
    		controller.setTimePadding(Double.valueOf(timeJumpPadding.getText()).doubleValue() / 100.);
    	} else if (timeScrunchMode.isSelected()) {
    		assert !timeJumpMode.isSelected() : "Both scrunch and jump are set!";
    		controller.setTimeAxisSubsequentBounds(TimeAxisSubsequentBoundsSetting.SCRUNCH);
    		controller.setTimePadding(Double.valueOf(timeScrunchPadding.getText()).doubleValue() / 100.);
    	} else {
           assert false : "No time subsequent mode selected"; 
    	   controller.setTimeAxisSubsequentBounds(TimeAxisSubsequentBoundsSetting.JUMP);
    	}
     	
    	// Non Time Subsequent Settings
    	
    	// Min
    	if (nonTimeMinAutoAdjustMode.isSelected()) {
    		assert !nonTimeMinFixedMode.isSelected() : "Both non time min subsequent modes are selected!";
    		controller.setNonTimeAxisSubsequentMinBounds(NonTimeAxisSubsequentBoundsSetting.AUTO);
    	} else if (nonTimeMinFixedMode.isSelected() && !nonTimeMinSemiFixedMode.isSelected()) {
    		assert !nonTimeMinAutoAdjustMode.isSelected() : "Both non time min subsequent modes are selected!";
    		controller.setNonTimeAxisSubsequentMinBounds(NonTimeAxisSubsequentBoundsSetting.FIXED);
    	} else if (nonTimeMinFixedMode.isSelected() && nonTimeMinSemiFixedMode.isSelected()) {
    		assert !nonTimeMinAutoAdjustMode.isSelected() : "Both non time min subsequent modes are selected!";
    		controller.setNonTimeAxisSubsequentMinBounds(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
    	} else {
    		assert false : "No non time min subsequent setting specified";
    	    controller.setNonTimeAxisSubsequentMinBounds(NonTimeAxisSubsequentBoundsSetting.AUTO);
    	}
		controller.setNonTimeMinPadding(Double.parseDouble(nonTimeMinPadding.getText()) / 100.);

    	// Max
      	if (nonTimeMaxAutoAdjustMode.isSelected()) {
    		assert !nonTimeMaxFixedMode.isSelected() : "Both non time max subsequent modes are selected!";
    		controller.setNonTimeAxisSubsequentMaxBounds(NonTimeAxisSubsequentBoundsSetting.AUTO);
    	} else if (nonTimeMaxFixedMode.isSelected() && !nonTimeMaxSemiFixedMode.isSelected()) {
    		assert !nonTimeMaxAutoAdjustMode.isSelected() : "Both non time max subsequent modes are selected!";
    		controller.setNonTimeAxisSubsequentMaxBounds(NonTimeAxisSubsequentBoundsSetting.FIXED);
    	} else if (nonTimeMaxFixedMode.isSelected() && nonTimeMaxSemiFixedMode.isSelected()) {
    		assert !nonTimeMaxAutoAdjustMode.isSelected() : "Both non time max subsequent modes are selected!";
    		controller.setNonTimeAxisSubsequentMaxBounds(NonTimeAxisSubsequentBoundsSetting.SEMI_FIXED);
    	} else {
    		assert false : "No non time ax subsequent setting specified";
    	    controller.setNonTimeAxisSubsequentMaxBounds(NonTimeAxisSubsequentBoundsSetting.AUTO);
    	}
		controller.setNonTimeMaxPadding(Double.valueOf(nonTimeMaxPadding.getText()).doubleValue() / 100.);

      	// Time
      	GregorianCalendar timeMin = recycledCalendarA;
      	GregorianCalendar timeMax = recycledCalendarB;

      	if (timeAxisMinAuto.isSelected()) {
      		GregorianCalendar gc = new GregorianCalendar();
      		gc.setTimeInMillis(plotViewManifestion.getCurrentMCTTime());
      		timeMin = gc;
      	} else if (timeAxisMinManual.isSelected()) {
      			timeMin.setTimeInMillis(timeAxisMinManualValue.getValueInMillis());
      	} else if (timeAxisMinCurrent.isSelected()) {
      		   timeMin.setTimeInMillis(timeAxisMinCurrentValue.getTimeInMillis());
        } else throw new IllegalArgumentException("No time setting button is selected.");

      	if (timeAxisMaxAuto.isSelected()) {
      		timeMax.setTime(timeMin.getTime());
    		timeMax.add(Calendar.SECOND, timeSpanValue.getSecond());
    		timeMax.add(Calendar.MINUTE, timeSpanValue.getMinute());
    		timeMax.add(Calendar.HOUR_OF_DAY, timeSpanValue.getHourOfDay());
    		timeMax.add(Calendar.DAY_OF_YEAR, timeSpanValue.getDayOfYear());
     	} else if (timeAxisMaxManual.isSelected()) {
      		timeMax.setTimeInMillis(timeAxisMaxManualValue.getValueInMillis());
    	} else if (timeAxisMaxCurrent.isSelected()) {
   		   timeMax.setTimeInMillis(timeAxisMaxCurrentValue.getTimeInMillis());
        } else throw new IllegalArgumentException("No time setting button is selected.");
      	
      	// Check that values are valid.
      	assert timeMin.getTimeInMillis() < timeMax.getTimeInMillis() : "Time min is > timeMax. Min = "
      		+ CalendarDump.dumpDateAndTime(timeMin) + ", Max = " + CalendarDump.dumpDateAndTime(timeMax);

      	if (timeMin.getTimeInMillis() < timeMax.getTimeInMillis()) {
      		controller.setTimeMinMaxValues(timeMin, timeMax);
      	} else {
      		logger.warn("User error - The minimum time was not less than the maximum time");
      	}

      	double nonTimeMin = 0.0;
      	double nonTimeMax = 1.0;
      	
      	// Non time
      	if (nonTimeAxisMinCurrent.isSelected()) { 
      		nonTimeMin = plotViewManifestion.getMinFeedValue();
      		
      	} else if (nonTimeAxisMinManual.isSelected()) {
      		nonTimeMin = Double.valueOf(nonTimeAxisMinManualValue.getText()).doubleValue();
      		
      	} else if (nonTimeAxisMinAutoAdjust.isSelected()) {
      		nonTimeMin = nonTimeAxisMinAutoAdjustValue.getValue();

      	} else {
      		logger.error("Non Time min axis setting not yet supported.");
      	}

        if (nonTimeAxisMaxCurrent.isSelected()) {
        	nonTimeMax = plotViewManifestion.getMaxFeedValue();
        	
    	} else if (nonTimeAxisMaxManual.isSelected()) {
      		nonTimeMax = Double.valueOf(nonTimeAxisMaxManualValue.getText()).doubleValue();
 
    	} else if (nonTimeAxisMaxAutoAdjust.isSelected()) {
    		nonTimeMax = nonTimeAxisMaxAutoAdjustValue.getValue();
 
      	} else {
     		logger.error("Non Time Max axis setting not yet supported.");
      	}

  
       if (nonTimeMin >= nonTimeMax) {
    	   nonTimeMin = 0.0;
    	   nonTimeMax = 1.0;
       }
       
       // Draw
       controller.setPlotLineDraw(new PlotLineDrawingFlags(
    		   linesOnly.isSelected()   || markersAndLines.isSelected(),
    		   markersOnly.isSelected() || markersAndLines.isSelected()
    		   ));
       
       // Connection line type
       if (direct.isSelected()) {
    	   controller.setPlotLineConnectionType(PlotLineConnectionType.DIRECT);
       } else if (step.isSelected()) {
    	   controller.setPlotLineConnectionType(PlotLineConnectionType.STEP_X_THEN_Y);
       }
    	   
       
       controller.setUseOrdinalPositionToGroupSubplots(!groupByCollection.isSelected());
       
       controller.setNonTimeMinMaxValues(nonTimeMin, nonTimeMax);
        // Settings complete. Create plot.
       controller.createPlot();
    	
    }
    
    /**
     * Return the controller associated with this settings panel. Usage intent is for testing only,
     * hence this is package private.
     * @return
     */
    PlotSettingController getController() {
    	return controller;
    }
    
    public void updateControlsToMatchPlot() {
    	resetButton.setEnabled(true);
    	resetButton.doClick(0);
    }
    
    public void setStaticAreaTimeFormat(String dateFormatPattern) {
    	staticAreaTimeFormat = TimeFormatUtils.makeDataFormat(dateFormatPattern == null ? TimeService.DEFAULT_TIME_FORMAT : dateFormatPattern);
    }

    public SimpleDateFormat getStaticAreaTimeFormat() {
    	if (staticAreaTimeFormat == null) {
    		setStaticAreaTimeFormat(ssTimeFormatSelection);
    	}
    	return staticAreaTimeFormat;
    }
}
