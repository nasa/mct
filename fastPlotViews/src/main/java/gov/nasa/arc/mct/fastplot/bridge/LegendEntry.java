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
package gov.nasa.arc.mct.fastplot.bridge;

import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.fastplot.settings.LineSettings;
import gov.nasa.arc.mct.fastplot.utils.AbbreviatingPlotLabelingAlgorithm;
import gov.nasa.arc.mct.fastplot.utils.TruncatingLabel;
import gov.nasa.arc.mct.fastplot.view.LegendEntryPopupMenuFactory;
import gov.nasa.arc.mct.fastplot.view.legend.AbstractLegendEntry;
import gov.nasa.arc.mct.util.LafColor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import plotter.xy.LinearXYPlotLine;

/**
 *  Legend entry for a plot line. The class responds to mouse entered events by increasing the brightness of the text labels.
 */
@SuppressWarnings("serial")
public class LegendEntry extends JPanel implements MouseListener, AbstractLegendEntry {

	private final static Logger logger = LoggerFactory.getLogger(LegendEntry.class);
	
 
	
	// Padding around labels to create space between the label text and its outside edge
	// Add a little spacing from the left-hand side
	private static final int    LEFT_PADDING  = 5;
	private static final Border PANEL_PADDING = BorderFactory.createEmptyBorder(0, LEFT_PADDING, 0, 0);
	
	private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1,1,1,1);
	private Border focusBorder = BorderFactory.createLineBorder(LafColor.TEXT_HIGHLIGHT);
	
	// Associated plot.
	private LinearXYPlotLine linePlot;

	// Gui widgets
	protected JLabel baseDisplayNameLabel= new TruncatingLabel();
	private Color backgroundColor;
	private Color foregroundColor;
	private Color originalPlotLineColor;
	private Stroke originalPlotLineStroke;
	private Stroke originalRegressionLineStroke;
	private Font originalFont;
	private Font boldFont;
	private Font strikeThruFont;
	private Font boldStrikeThruFont;

	private String baseDisplayName = "";
	
	boolean selected=false;
	
	private String currentToolTipTxt = "";
	private ToolTipManager toolTipManager;
	
	private String dataSetName = "";

	private String thisBaseDisplayName = "";	
	private String valueString = "";
	private AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm = new AbbreviatingPlotLabelingAlgorithm();
	private String computedBaseDisplayName = "";
	private FeedProvider.RenderingInfo renderingInfo;
	
	private LegendEntryPopupMenuFactory popupManager = null;

	private LineSettings lineSettings = new LineSettings();
	
	// Default width - will be adjusted to match base display name
	private int baseWidth = PlotConstants.PLOT_LEGEND_WIDTH;
	
	private LinearXYPlotLine regressionLine;

	/**
	 * Construct a legend entry
	 * @param theBackgroundColor background color of the entry
	 * @param theForegroundColor text color
	 * @param font text font
	 */
	public LegendEntry(Color theBackgroundColor, Color theForegroundColor, Font font, AbbreviatingPlotLabelingAlgorithm thisPlotLabelingAlgorithm) { 
		setBorder(EMPTY_BORDER);
		setOpaque(false);
		
		plotLabelingAlgorithm = thisPlotLabelingAlgorithm;
		
		backgroundColor = theBackgroundColor;	
		foregroundColor =  theForegroundColor;
		setForeground(foregroundColor);
		lineSettings.setMarker(lineSettings.getColorIndex());
		// Default to using same marker index as color index; may be later overridden if user-specified
		
		focusBorder = BorderFactory.createLineBorder(theForegroundColor);
		
		// NOTE: Original font size is 10. Decrease by 1 to size 9. 
		// Need to explicitly cast to float from int on derived font size
		// Need to explicitly set to FontName=ArialMT/Arial-BoldMT and FontFamily=Arial to be
		// cross OS platforms L&F between MacOSX and Linux.
		// MacOSX defaults to Arial and Linux defaults to Dialog FontFamily.
		originalFont = font;
		originalFont = originalFont.deriveFont((float)(originalFont.getSize()-1));	
		boldFont = originalFont.deriveFont(Font.BOLD);
		Map<TextAttribute, Object> attributes = new Hashtable<TextAttribute, Object>();
		attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
		strikeThruFont = originalFont.deriveFont(attributes);
		boldStrikeThruFont = boldFont.deriveFont(attributes);
	
		// Setup the look of the labels.
		baseDisplayNameLabel.setBackground(backgroundColor);
		baseDisplayNameLabel.setForeground(foregroundColor);
		baseDisplayNameLabel.setFont(originalFont);
		baseDisplayNameLabel.setOpaque(true);
		
		// Sets as the default ToolTipManager
		toolTipManager = ToolTipManager.sharedInstance();
		toolTipManager.setEnabled(true);
		toolTipManager.setLightWeightPopupEnabled(true);
		
		// Defaults: toolTipManager.getDismissDelay()=4000ms, 
		// toolTipManager.getInitialDelay()=750ms, 
		// toolTipManager.getReshowDelay()=500ms 
		toolTipManager.setDismissDelay(PlotConstants.MILLISECONDS_IN_SECOND * 3);
		toolTipManager.setInitialDelay(PlotConstants.MILLISECONDS_IN_SECOND / 2);
		toolTipManager.setReshowDelay(PlotConstants.MILLISECONDS_IN_SECOND / 2);
		
		// Place the labels according to the format specified in the UE spec.
		layoutLabels();

		// Listen to mouse events to drive the highlighting of legends when mouse enters. 
		addMouseListener(this);
	}

	// Data getter and and setters
	void setPlot(LinearXYPlotLine thePlot) {
		linePlot = thePlot;
		updateLinePlotFromSettings();
	}


	private List<String> getPanelOrWindowContextTitleList() {
		List<String> panelOrWindowContextTitleList = new ArrayList<String>();
		panelOrWindowContextTitleList.clear();
		
		if (plotLabelingAlgorithm != null) {

			if (plotLabelingAlgorithm.getPanelContextTitleList().size() > 0) {
				panelOrWindowContextTitleList.addAll(this.plotLabelingAlgorithm.getPanelContextTitleList());
			}
			
			if (plotLabelingAlgorithm.getCanvasContextTitleList().size() > 0) {
				panelOrWindowContextTitleList.addAll(this.plotLabelingAlgorithm.getCanvasContextTitleList());
			}
			
		} else {
			logger.error("Plot labeling algorithm object is NULL!");
		}
				
		return panelOrWindowContextTitleList;
	}
	
	public void setBaseDisplayName(String theBaseDisplayName) {
		
		thisBaseDisplayName = theBaseDisplayName;
		
		if (thisBaseDisplayName != null) {
			thisBaseDisplayName = thisBaseDisplayName.trim();
		}
		
		baseDisplayName  = thisBaseDisplayName;
					
		// Format the base display name 
		// Split string around newline character.
		 String[] strings = baseDisplayName.split(PlotConstants.LEGEND_NEWLINE_CHARACTER); 
				 
	     if (strings.length <= 1) {
			// Determine if first or second string is null
	    	 if (baseDisplayName.indexOf(PlotConstants.LEGEND_NEWLINE_CHARACTER) == -1) {
				// first string is null.
				baseDisplayNameLabel.setText(baseDisplayName); 
			} else if (theBaseDisplayName.equals(PlotConstants.LEGEND_NEWLINE_CHARACTER)) {
				baseDisplayNameLabel.setText("");	
			} else {
				// second string is empty. Truncate first.
				baseDisplayNameLabel.setText(PlotConstants.LEGEND_ELLIPSES);	
			}
		 } else {
			 
			 // Use table labeling algorithm to display base display name.
			 // line1 is base display name; while line2 is PUI name
			 String line1 = strings[0];
			 
			 if (line1 != null) {
				 baseDisplayName = line1.trim();
				 thisBaseDisplayName = baseDisplayName;
			 }
			 
			 List<String> baseDisplayNameList = new ArrayList<String>();
			 baseDisplayNameList.add(line1);
			
			 assert plotLabelingAlgorithm != null : "Plot labeling algorithm should NOT be NULL at this point.";
			 
			 baseDisplayName = plotLabelingAlgorithm.computeLabel(baseDisplayNameList, getPanelOrWindowContextTitleList());
			 
			 // since this name will be used in a legend, it must not be empty so use the initial display name if it would have been empty
			 if (baseDisplayName != null && baseDisplayName.isEmpty()) {
				 baseDisplayName = thisBaseDisplayName;
			 }
			 computedBaseDisplayName = baseDisplayName;

			 updateLabelText();
		 }
		
	     valueString = formatNumericStringToScientificNotation(valueString);
	     
	     thisBaseDisplayName = theBaseDisplayName.replaceAll(PlotConstants.WORD_DELIMITERS, " ");
	     currentToolTipTxt = "<HTML>" + thisBaseDisplayName.replaceAll(PlotConstants.LEGEND_NEWLINE_CHARACTER, "<BR>") + "<BR>" + valueString + "<HTML>";
	     this.setToolTipText(currentToolTipTxt);	
	}

	private String formatNumericStringToScientificNotation(String valueString) {
		if (valueString != null && !valueString.isEmpty()) {
            valueString = PlotterPlot.getNumberFormatter(Double.parseDouble(valueString)).format(Double.parseDouble(valueString));
        }
		return valueString;
	}
	
	void setData(FeedProvider.RenderingInfo info) {
		this.renderingInfo = info;
		String valueText = info.getValueText();
		if (!"".equals(valueText)) {
			valueString = PlotConstants.DECIMAL_FORMAT.format(Double.parseDouble(valueText));
			valueString = formatNumericStringToScientificNotation(valueString);
		}
		updateLabelFont();
		updateLabelText();
		thisBaseDisplayName = thisBaseDisplayName.replaceAll(PlotConstants.WORD_DELIMITERS, " ");
	    currentToolTipTxt = "<HTML>" + thisBaseDisplayName.replaceAll(PlotConstants.LEGEND_NEWLINE_CHARACTER, "<BR>") + "<BR>" + valueString + "<HTML>";
	    this.setToolTipText(currentToolTipTxt);
	     
	}


	private void updateLabelFont() {
		if(selected) {
			if(renderingInfo == null || renderingInfo.isPlottable()) {
				baseDisplayNameLabel.setFont(boldFont);
			} else {
				baseDisplayNameLabel.setFont(boldStrikeThruFont);
			}
		} else {
			if(renderingInfo == null || renderingInfo.isPlottable()) {
				baseDisplayNameLabel.setFont(originalFont);
			} else {
				baseDisplayNameLabel.setFont(strikeThruFont);
			}
		}
	}


	private void updateLabelText() {
		String statusText = renderingInfo == null ? null : renderingInfo.getStatusText();
		if(statusText == null) {
			statusText = "";
		}
		statusText = statusText.trim();
		if(!"".equals(statusText)) {
			baseDisplayNameLabel.setText("(" + statusText + ") " + baseDisplayName);
		} else {
			baseDisplayNameLabel.setText(baseDisplayName);
		}
	}
	
	private void updateLabelWidth() {
		/* Record font & string to restore*/
		Font   f = baseDisplayNameLabel.getFont();
		String s = baseDisplayNameLabel.getText();
		
		if (f == originalFont && s.equals(baseDisplayName) && baseDisplayNameLabel.isValid()) {
			baseWidth = baseDisplayNameLabel.getWidth();
		}
		
	}
	
	/**
	 * Layout the labels within a legend in line with the UE specification.
	 */
	void layoutLabels() {
		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		panel.setBorder(PANEL_PADDING);
		panel.setBackground(backgroundColor);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel displayNamePanel = new JPanel();
		displayNamePanel.setLayout(new BoxLayout(displayNamePanel, BoxLayout.LINE_AXIS));
		displayNamePanel.add(baseDisplayNameLabel);
		displayNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		panel.add(displayNamePanel);

		add(panel, BorderLayout.CENTER);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// do nothing	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
		toolTipManager.registerComponent(this);

		if (!selected) {
			// Highlight this entry on the plot.
			originalPlotLineColor  = linePlot.getForeground();
			originalPlotLineStroke = linePlot.getStroke();
		}
		
		selected = true;
		// Highlight this legend entry
		baseDisplayNameLabel.setForeground(foregroundColor.brighter());
		updateLabelFont();

		linePlot.setForeground(originalPlotLineColor.brighter().brighter());
		if(originalPlotLineStroke == null) {
			linePlot.setStroke(new BasicStroke(PlotConstants.SELECTED_LINE_THICKNESS));
		} else if (originalPlotLineStroke instanceof BasicStroke) {
			BasicStroke stroke = (BasicStroke) originalPlotLineStroke;
			linePlot.setStroke(new BasicStroke(stroke.getLineWidth() * PlotConstants.SELECTED_LINE_THICKNESS, stroke.getEndCap(), stroke
					.getLineJoin(), stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()));

		} //Otherwise, it's a stroke we can't change (ie EMPTY_STROKE)

		if (regressionLine != null) {
			originalRegressionLineStroke = regressionLine.getStroke();
			regressionLine.setForeground(originalPlotLineColor.brighter().brighter());
			Stroke stroke = (BasicStroke) regressionLine.getStroke();
			//TODO synch with plot thickness feature changes
			if(stroke == null) {
				regressionLine.setStroke(new BasicStroke(PlotConstants.SLOPE_LINE_WIDTH*2,
		                BasicStroke.CAP_BUTT,
		                BasicStroke.JOIN_MITER,
		                10.0f, PlotConstants.dash1, 0.0f));
			} else {
				regressionLine.setStroke(new BasicStroke(PlotConstants.SLOPE_LINE_WIDTH*2,
		                BasicStroke.CAP_BUTT,
		                BasicStroke.JOIN_MITER,
		                10.0f, PlotConstants.dash1, 0.0f));
			}
		}
				

		this.setToolTipText(currentToolTipTxt);
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
		toolTipManager.unregisterComponent(this);
		
		selected = false;
		// Return this legend entry to its original look. 
		baseDisplayNameLabel.setForeground(foregroundColor);
		updateLabelFont();
		
		// Return this entry on the plot to its original look. 
		linePlot.setForeground(originalPlotLineColor);
		linePlot.setStroke(originalPlotLineStroke);
		if (regressionLine != null) {
			regressionLine.setForeground(originalPlotLineColor);
			regressionLine.setStroke(originalRegressionLineStroke);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// open the color changing popup	
		if (popupManager != null && e.isPopupTrigger()) {
			setBorder(focusBorder); //TODO: Externalize the color of this?
			JPopupMenu popup = popupManager.getPopup(this);
			popup.show(this, e.getX(), e.getY());
			popup.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuCanceled(PopupMenuEvent arg0) {
					setBorder(EMPTY_BORDER);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
					setBorder(EMPTY_BORDER);
				}

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
					
				}
				
			});
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (popupManager != null && e.isPopupTrigger()) {
			popupManager.getPopup(this).show(this, e.getX(), e.getY());
		}
	}
	
	public String getToolTipText() {
		return currentToolTipTxt;
	}

	// Retrieves whatever is set in the label text field
	public String getBaseDisplayNameLabel() {
		return baseDisplayNameLabel.getText();
	}
		
	// Retrieves base display name + PUI name
	public String getFullBaseDisplayName() {
		return thisBaseDisplayName;
	}
	
	// Retrieves only base display name (w/o PUI name)
	// after running thru labeling algorithm
	public String getComputedBaseDisplayName() {
		return computedBaseDisplayName;
	}
	
	// Retrieves truncated with ellipse 
	public String getTruncatedBaseDisplayName() {
		return baseDisplayName;
	}
	
	public void setPlotLabelingAlgorithm(AbbreviatingPlotLabelingAlgorithm plotLabelingAlgorithm) {
		this.plotLabelingAlgorithm = plotLabelingAlgorithm;
	}
	
	public AbbreviatingPlotLabelingAlgorithm getPlotLabelingAlgorithm() {
		return this.plotLabelingAlgorithm;
	}
	
	public int getLabelWidth() {
		updateLabelWidth();
		return baseWidth + LEFT_PADDING;
	}

	@Override
	public void setForeground(Color fg) {
		Color lineColor = fg;
		Color labelColor = fg;
		if (linePlot != null) {
			if (linePlot.getForeground() != foregroundColor) lineColor = fg;
			linePlot.setForeground(lineColor);
		}
		
		if (regressionLine != null) {
			if (regressionLine.getForeground() != foregroundColor) lineColor = fg.brighter().brighter();
			regressionLine.setForeground(lineColor);
		}
		
		if (baseDisplayNameLabel != null) {
			if (baseDisplayNameLabel.getForeground() != foregroundColor) labelColor = fg.brighter();
			baseDisplayNameLabel.setForeground(labelColor);
		}
		
		foregroundColor = fg;		
		focusBorder = BorderFactory.createLineBorder(fg);
		
		// Infer the appropriate index for this color
		for (int i = 0; i < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT; i++) {
			if (PlotLineColorPalette.getColor(i).getRGB() == fg.getRGB()) {
				lineSettings.setColorIndex(i);
			}
		}
		
		super.setForeground(fg);
	}

	public LegendEntryPopupMenuFactory getPopup() {
		return popupManager;
	}

	public void setPopup(LegendEntryPopupMenuFactory popup) {
		this.popupManager = popup;
	}
	
	public void setLineSettings(LineSettings settings) {
		lineSettings = settings;
		updateLinePlotFromSettings();
	}
	
	public LineSettings getLineSettings() {
		return lineSettings;
	}
	
	private void updateLinePlotFromSettings() {
		/* Color */
		int index = lineSettings.getColorIndex();
		Color c = PlotLineColorPalette.getColor(index);
		setForeground(c);
		
		/* Thickness */
		Stroke s = linePlot.getStroke();
		if (s == null || s instanceof BasicStroke) {
			int t = lineSettings.getThickness();
			linePlot.setStroke(t == 1 ? null : new BasicStroke(t));
			originalPlotLineStroke = linePlot.getStroke();
		} // We only want to modify known strokes
		
		/* Marker */
		if (linePlot.getPointIcon() != null) {
			Shape shape = null;
			if (lineSettings.getUseCharacter()) {
				Graphics g = (Graphics) getGraphics();
				if (g != null && g instanceof Graphics2D) {
					FontRenderContext frc = ((Graphics2D)g).getFontRenderContext();
					shape = PlotLineShapePalette.getShape(lineSettings.getCharacter(), frc);
				}
			} else {
				int marker = lineSettings.getMarker();			
				shape = PlotLineShapePalette.getShape(marker);
			}
			if (shape != null) {
				linePlot.setPointIcon(new PlotMarkerIcon(shape));				
				baseDisplayNameLabel.setIcon(new PlotMarkerIcon(shape, false, 12, 12));
			}
		}
		
		linePlot.repaint();	
		repaint();
	}

	/** Get whether a regression line is displayed or not.
	 * @return regressionLine
	 */
	public boolean hasRegressionLine() {
		return lineSettings.getHasRegression();
	}

	/** Set whether a regression line is displayed or not.
	 * @param regressionLine boolean indicator
	 */
	public void setHasRegressionLine(boolean regressionLine) {
		lineSettings.setHasRegression(regressionLine);
	}

	/** Get the number of regression points to use.
	 * @return numberRegressionPoints the number of regression points to use
	 */
	public int getNumberRegressionPoints() {
		return lineSettings.getRegressionPoints();
	}

	/** Set the number of regression points to use.
	 * @param numberRegressionPoints
	 */
	public void setNumberRegressionPoints(int numberRegressionPoints) {
		lineSettings.setRegressionPoints(numberRegressionPoints);
	}

	/** Get the regression line for this legend entry.
	 * @return regressionLine a LinearXYPlotLine
	 */
	public LinearXYPlotLine getRegressionLine() {
		return regressionLine;
	}

	/** Set the regression line for this legend entry.
	 * @param regressionLine a LinearXYPlotLine
	 */
	public void setRegressionLine(LinearXYPlotLine regressionLine) {
		this.regressionLine = regressionLine;
		if (regressionLine != null)
			regressionLine.setForeground(foregroundColor);
	}
	/**
	 * @return the dataSetName
	 */
	public String getDataSetName() {
		return dataSetName;
	}

	/**
	 * @param dataSetName the dataSetName to set
	 */
	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	@Override
	public void attachPlotLine(AbstractPlotLine plotLine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDisplayedName() {
		return this.baseDisplayName;
	}

}
