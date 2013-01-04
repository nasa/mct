package gov.nasa.arc.mct.fastplot.settings.controls;

import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotterPlot;
import gov.nasa.arc.mct.fastplot.settings.PlotConfiguration;
import gov.nasa.arc.mct.fastplot.settings.PlotSettingsPanel;
import gov.nasa.arc.mct.fastplot.settings.PlotSettingsSubPanel;
import gov.nasa.arc.mct.fastplot.view.NumericTextField;
import gov.nasa.arc.mct.fastplot.view.PlotViewManifestation;
import gov.nasa.arc.mct.fastplot.view.TimeDuration;
import gov.nasa.arc.mct.fastplot.view.TimeSpanTextField;
import gov.nasa.arc.mct.fastplot.view.TimeTextField;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PlotSettingsAxisGroup extends PlotSettingsPanel implements ActionListener {
	private static final long serialVersionUID = -6810586939806488596L;


	// Access bundle file where externalized strings are defined.
	private static final ResourceBundle BUNDLE = 
                               ResourceBundle.getBundle("gov.nasa.arc.mct.fastplot.view.Bundle");

	// Logger
    private final static Logger logger = LoggerFactory.getLogger(PlotSettingsAxisGroup.class);

	
	// Other constants
	private static final Double NONTIME_AXIS_SPAN_INIT_VALUE = Double.valueOf(30);
	private static final int JTEXTFIELD_COLS = 8;
	private static final int NUMERIC_TEXTFIELD_COLS1 = 12;
	private static final String MANUAL_LABEL = BUNDLE.getString("Manual.label");
    private static final int INTERCONTROL_HORIZONTAL_SPACING = 0; 
	private static final DecimalFormat PARENTHESIZED_LABEL_FORMAT = new DecimalFormat("###.###");
	private static final int SPACE_BETWEEN_ROW_ITEMS = 3;


	// Fields
	private AxisBoundsPanel minControls;
	private AxisBoundsPanel maxControls;
	private AxisSpanCluster spanControls;
	private String minText = "Min";
	private String maxText = "Max";
	private String title = "Axis";
	private boolean temporal;
	private boolean manualUpdates = true; // Does manual update with current value?
	
	private Runnable autoControlsCallback = new Runnable() {
		@Override
		public void run() {
			minControls.auto.setEnabled(!maxControls.auto.isSelected());
			minControls.autoValue.setEnabled(!maxControls.auto.isSelected());
			maxControls.auto.setEnabled(!minControls.auto.isSelected());
			maxControls.autoValue.setEnabled(!minControls.auto.isSelected());
			if (!maxControls.auto.isSelected() && !minControls.auto.isSelected()) {
				spanControls.setSpanValue(getValue(maxControls) - getValue(minControls));
			}
			if (!maxControls.auto.isSelected()) {
				minControls.autoValue.setValue(getValue(maxControls) - spanControls.getSpanValue());
			}
			if (!minControls.auto.isSelected()) {
				maxControls.autoValue.setValue(getValue(minControls) + spanControls.getSpanValue());
			}
		}
	};
	
	public PlotSettingsAxisGroup(boolean temporal) {
		this.temporal = temporal;
		this.minControls  = new AxisBoundsPanel(false);
		this.maxControls  = new AxisBoundsPanel(true);
		this.spanControls = new AxisSpanCluster();
		addSubPanel(minControls);
		addSubPanel(maxControls);
		addSubPanel(spanControls);
		if (!temporal) {
			minControls.addCallback(autoControlsCallback);
			maxControls.addCallback(autoControlsCallback);
		}
	} 
	
	public JPanel getMinControls() {
		return minControls;
	}

	public JPanel getMaxControls() {
		return maxControls;
	}

	public JPanel getSpanControls() {
		return spanControls;
	}

	public String getMinText() {
		return minText;
	}

	public String getMaxText() {
		return maxText;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public abstract void setBounds(PlotConfiguration settings, double min, double max);	
	public abstract double getBoundMinimum(PlotConfiguration settings);
	public abstract double getBoundMaximum(PlotConfiguration settings);
	public abstract double getActualMinimum(PlotViewManifestation view);
	public abstract double getActualMaximum(PlotViewManifestation view);

	public void updateFrom(PlotViewManifestation view) {		
		if (temporal) {
			minControls.updateCurrent(getActualMinimum(view));
			maxControls.updateCurrent(getActualMaximum(view));
			minControls.updateAuto((double) view.getCurrentMCTTime());
			maxControls.updateAuto((double) getValue(minControls) + spanControls.getSpanValue());
			if (!maxControls.auto.isSelected()) {
				spanControls.setSpanValue(getValue(maxControls) - getValue(minControls));
			}
		} else {
			minControls.currentValue.setValue(getActualMinimum(view));
			maxControls.currentValue.setValue(getActualMaximum(view));
		}
	}
	
	private double getValue(AxisBoundsPanel panel)  {
		try {
			boolean maximum = panel == maxControls;
			AxisBoundsPanel other = maximum ? minControls : maxControls;
			if (temporal) {
				TimeSpanTextField field = (TimeSpanTextField) spanControls.spanValue;
				if (panel.auto.isSelected()) {
					return maximum ? (field.getDurationInMillis() + getValue(other)) : 
						             panel.autoValue.getValue(); // Now
				} else if (panel.current.isSelected()) {
					return panel.currentValue.getValue();					
				} else if (panel.manual.isSelected()) {
					return ((ManualTimeEntryArea)panel.manualValue).getValue();
				}
			} else {
				NumericTextField field = (NumericTextField) spanControls.spanValue;
				if (panel.auto.isSelected()) {
					return getValue(other) + (maximum ? 1 : -1) * field.getDoubleValue();
				} else if (panel.current.isSelected()) {
					return panel.currentValue.getValue();
				} else if (panel.manual.isSelected()) {
					field = (NumericTextField) panel.manualValue;
					return field.getDoubleValue();
				}
			}
		} catch (ParseException pe) {
			logger.error("Parse exception in axis bounds panel.");	
		}
		logger.error("Could not interpret user input from axis bounds panel.");
		return Double.NaN;
	}

	// Non-time axis Maximums panel
	class AxisBoundsPanel extends PlotSettingsSubPanel {
		private static final long serialVersionUID = -768623994853270825L;

		private JRadioButton         current;		
		private ParenthesizedLabel   currentValue;
		private JRadioButton         manual;		
		private JComponent           manualValue;
		private JRadioButton         auto;		
		private ParenthesizedLabel   autoValue;		
		
		private double               cachedManualValue;
		
		private boolean maximal;
		
		public AxisBoundsPanel(boolean maximal) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			current = new JRadioButton(BUNDLE.getString( temporal ?
					(maximal ? "CurrentMax.label" : "Currentmin.label") :
					(maximal ? "CurrentLargestDatum.label" : "CurrentSmallestDatum.label")
			), true);		
			currentValue = temporal ? new ParenthesizedTimeLabel(current) : new ParenthesizedNumericLabel(current);
			
			manual = new JRadioButton( MANUAL_LABEL, false);
			manualValue = temporal ? getTimeManualValue() : getNonTimeManualValue();
			
			auto = new JRadioButton(BUNDLE.getString(temporal ?
					(maximal ? "MinPlusSpan.label" : "Now.label"        ) :
					(maximal ? "MinPlusSpan.label" : "MaxMinusSpan.label")
			), false);
			autoValue = temporal ? new ParenthesizedTimeLabel(current) : new ParenthesizedNumericLabel(auto);

			ButtonGroup maximumsGroup = new ButtonGroup();
			maximumsGroup.add(manual);
			maximumsGroup.add(current);
			maximumsGroup.add(auto);

			manual.addActionListener(this);
			current.addActionListener(this);
			auto.addActionListener(this);
			
			// Layout
			add(createMultiItemRow(current, currentValue));
			add(createMultiItemRow(manual,  manualValue) );
			add(createMultiItemRow(auto,    autoValue)   );

			// Note the maximality
			this.maximal = maximal;
			
			//TODO: Tooltips, instrumentation
		}
		
		private JComponent getTimeManualValue() {
			GregorianCalendar calendar = new GregorianCalendar();			
		    ManualTimeEntryArea entryArea = new ManualTimeEntryArea(calendar);
		    entryArea.addFocusListener(this);
		    entryArea.addActionListener(this);
		    return entryArea;
		}
		
		private JComponent getNonTimeManualValue() {
			DecimalFormat format = new DecimalFormat("###.######");
			format.setParseIntegerOnly(false);
			manualValue = new NumericTextField(NUMERIC_TEXTFIELD_COLS1, format);
			((NumericTextField)manualValue).addFocusListener(this);
			((NumericTextField)manualValue).addActionListener(this);
			((JTextField)manualValue).setColumns(JTEXTFIELD_COLS);
			return manualValue;
		}
		
		public void updateCurrent(double value) {
			currentValue.setValue(value);
			if (temporal && manualUpdates) {
				updateManual(value);
			}
		}
		
		public void updateManual(double value) {
			if (temporal) ((ManualTimeEntryArea) manualValue).setValue(value);
			else          ((NumericTextField)    manualValue).setValue(value);
		}
		
		public void updateAuto(double value) {
			autoValue.setValue(value);
		}
		
		public void addActionListener(ActionListener actionListener) {
			current.addActionListener(actionListener);
			manual.addActionListener(actionListener);
			auto.addActionListener(actionListener);
		}

		public void removeActionListener(ActionListener actionListener) {
			current.removeActionListener(actionListener);
			manual.removeActionListener(actionListener);
			auto.removeActionListener(actionListener);
		}

		@Override
		public void populate(PlotConfiguration settings) {
			// All handled in the span control - nothing to do here
		}

		@Override
		public void reset(PlotConfiguration settings, boolean hard) {
			if (temporal) {
				if (hard) {
					current.setSelected(true);
					manualUpdates = true;
				} else {
					if (maximal && !auto.isSelected()) {
						spanControls.setSpanValue(getValue(maxControls) - getValue(minControls));
					}
					manualUpdates &= !manual.isSelected();
				}
			} else {
				NumericTextField field = (NumericTextField) manualValue;
				if (hard) {
					manual.setSelected(true);						
					cachedManualValue = maximal ? getBoundMaximum(settings) : getBoundMinimum(settings); 
					field.setValue(cachedManualValue);
				}
				// Prevent empty manual value field
				if (field.getText().isEmpty()) {
					field.setValue(cachedManualValue);
				}
				autoControlsCallback.run(); // Update enabled state
			}
		}

		@Override
		public boolean isDirty() {		
			try {
			return !(temporal ? current : manual).isSelected() || 
			        (temporal ? false :
			        	((NumericTextField) manualValue).getDoubleValue() != cachedManualValue
			        	 );
			} catch (ParseException pe) {
				return false;
			}
		}

		@Override
		public boolean isValidated() {
			return true;
		}
		
	}



	// Panel holding span controls
	class AxisSpanCluster extends PlotSettingsSubPanel {
		private static final long serialVersionUID = -3947426156383446643L;
		private JTextField spanValue;
		private JLabel spanTag;

		public AxisSpanCluster() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	        spanTag = new JLabel(BUNDLE.getString("SpanTextField.label"));

	        // Create a text field with a ddd/hh:mm:ss format for time
	        spanValue = temporal ? getTimeSpanTextFieldFormat() : getNonTimeTextField();
	        
	        spanValue.addActionListener(this);
	        spanValue.addFocusListener(this);
		
	        add(spanTag);
            add(Box.createHorizontalStrut(INTERCONTROL_HORIZONTAL_SPACING));
            add(spanValue);
            if (temporal) {
	            add(Box.createHorizontalStrut(INTERCONTROL_HORIZONTAL_SPACING + 3));
	            add(new JLabel(BUNDLE.getString("YearSpan")));
	            add(Box.createHorizontalStrut(INTERCONTROL_HORIZONTAL_SPACING + 3));
	            add(((TimeSpanTextField)spanValue).getYearSpanValue());
            }
            
            // Instrument
            spanTag.setName("spanTag");
		}

		JTextField getNonTimeTextField() {
			NumericTextField nonTimeSpanValue = new NumericTextField(NUMERIC_TEXTFIELD_COLS1, PARENTHESIZED_LABEL_FORMAT);
			nonTimeSpanValue.setColumns(JTEXTFIELD_COLS);
			nonTimeSpanValue.setValue(NONTIME_AXIS_SPAN_INIT_VALUE);
			return nonTimeSpanValue;
		}
		
		JTextField getTimeSpanTextFieldFormat() {
			MaskFormatter formatter = null;
			try {
				formatter = new MaskFormatter("###/##:##:##");
				formatter.setPlaceholderCharacter('0');
			} catch (ParseException e) {
				logger.error("Error in creating a mask formatter", e);
			}
	        return new TimeSpanTextField(formatter);
		}
		
		public double getSpanValue() {
			try {
				if (temporal) {
					TimeSpanTextField field = (TimeSpanTextField) spanValue;
					return field.getDurationInMillis();					
				} else {
					NumericTextField field = (NumericTextField) spanValue;
					return field.getDoubleValue();
				}
			} catch (ParseException pe) {
				return Double.NaN;
			}
		}
		
		public void setSpanValue(double value) {
			if (value > 0) {
				if (temporal) {
					TimeSpanTextField field = (TimeSpanTextField) spanValue;
					field.setTime(new TimeDuration((long)value));
				} else {
					NumericTextField field = (NumericTextField) spanValue;
					field.setValue(value);			
				}
			}
		}

		@Override
		public void populate(PlotConfiguration settings) {
			PlotSettingsAxisGroup.this.setBounds(settings, getValue(minControls), getValue(maxControls));
		}
		


		@Override
		public void reset(PlotConfiguration settings, boolean hard) {
			if (temporal) {
				spanValue.setEnabled(maxControls.auto.isSelected());
			} else {
				spanValue.setEnabled(minControls.auto.isSelected() || maxControls.auto.isSelected());
				NumericTextField field = (NumericTextField) spanValue;
				field.setValue(getBoundMaximum(settings) - getBoundMinimum(settings));
			}
		}

		@Override
		public boolean isDirty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isValidated() {
			return getValue(maxControls) > getValue(minControls);
		}
		
	}
	
	private class ManualTimeEntryArea extends JPanel {
		private static final long serialVersionUID = 5096738756047815979L;

		private JComboBox yearBox;
		private TimeTextField timeField;
		
		public ManualTimeEntryArea(Calendar calendar) {
			Integer[] years = new Integer[10];
			for (int i = 0 ; i < 10; i++ ) {
				years[i] = new Integer(calendar.get(Calendar.YEAR) - i);
			}
			yearBox = new JComboBox(years);
			yearBox.setEditable(true);
			
	        MaskFormatter formatter = null;
			try {
				formatter = new MaskFormatter("###/##:##:##");
				formatter.setPlaceholderCharacter('0');
			} catch (ParseException e) {
				logger.error("Parse error in creating time field", e);
			}
			
			timeField = new TimeTextField(formatter, calendar.get(Calendar.YEAR));
		    	
		    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		    add(timeField);
		    add(yearBox);
		    
		    yearBox.setPreferredSize(new Dimension(60,timeField.getPreferredSize().height - 1));
		}
		
		public void addActionListener(ActionListener al) {
			timeField.addActionListener(al);
			yearBox.addActionListener(al);
		}
		
		public void addFocusListener(FocusListener fl) {
			timeField.addFocusListener(fl);
			yearBox.addFocusListener(fl);
		}
		
		public void setValue(double d) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
			cal.setTimeInMillis((long) d);
			int year = cal.get(Calendar.YEAR);
			timeField.setTime(cal);
			yearBox.getModel().setSelectedItem(Integer.valueOf(year));
			yearBox.revalidate();
		}
		
		public long getValue() {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(timeField.getValueInMillis());
			cal.set(Calendar.YEAR, (Integer) yearBox.getSelectedItem());
			return cal.getTimeInMillis();
		}
	}

	
    private abstract class ParenthesizedLabel extends JLabel {
		private static final long serialVersionUID = 4908562204337928432L;

		abstract void setValue(Double value);
		abstract double getValue();
    }

	private class ParenthesizedTimeLabel extends ParenthesizedLabel {
		private static final long serialVersionUID = -6004293775277749905L;

		private GregorianCalendar timeInMillis = new GregorianCalendar();

		private JRadioButton companionButton;
		
		// TODO: This should be the user-selected date
		private DateFormat dateFormat = new SimpleDateFormat(PlotConstants.DEFAULT_TIME_FORMAT);
		
		public ParenthesizedTimeLabel(JRadioButton button) {
			dateFormat.setTimeZone(TimeZone.getTimeZone(PlotConstants.DEFAULT_TIME_ZONE));
			companionButton = button;
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					companionButton.setSelected(true);
					companionButton.requestFocusInWindow();
					//TODO : Fire callbacks!
				}
			});
		}

		public void setValue (Double value) {
			timeInMillis.setTimeInMillis(value.longValue());
			setText("(" + dateFormat.format(timeInMillis.getTime()) + " "
					+ timeInMillis.get(Calendar.YEAR) + ")");		
		}
		
		public double getValue() {
			return (double) timeInMillis.getTimeInMillis();
		}

	}

	private class ParenthesizedNumericLabel extends ParenthesizedLabel {
		private static final long serialVersionUID = 3403375470853249483L;
		private JRadioButton companionButton;

		public ParenthesizedNumericLabel(JRadioButton button) {
			
			companionButton = button;
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					companionButton.setSelected(true);
					companionButton.requestFocusInWindow();
					//TODO: Fire callbacks!
				}
			});
		}

		public double getValue() {
			String data = getText();
			if (data == null) {
				return Double.NaN;
			}
			if (data.length() < 3) {
				logger.error("Numeric label in plot settings contained invalid content [" + data + "]");
				return Double.NaN;
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
			
			String formatNum = PARENTHESIZED_LABEL_FORMAT.format(input);
			
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
	
	// Convenience method for populating and applying a standard layout for multi-item rows
    private JPanel createMultiItemRow(final JComponent button, JComponent secondItem) {
    	JPanel panel = new JPanel();
    	panel.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.anchor = GridBagConstraints.BASELINE_LEADING;
    	if (button != null && button instanceof JRadioButton) {
    		((AbstractButton) button).setSelected(false);
    		panel.add(button, gbc);
    	} else if (button != null && button instanceof JComboBox){
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
}