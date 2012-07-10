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


import gov.nasa.arc.mct.fastplot.bridge.LegendEntry;
import gov.nasa.arc.mct.fastplot.bridge.PlotAbstraction.LineSettings;
import gov.nasa.arc.mct.fastplot.bridge.PlotConstants;
import gov.nasa.arc.mct.fastplot.bridge.PlotLineColorPalette;
import gov.nasa.arc.mct.fastplot.bridge.PlotLineShapePalette;
import gov.nasa.arc.mct.fastplot.bridge.PlotMarkerIcon;
import gov.nasa.arc.mct.fastplot.utils.MenuItemSpinner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Provides popup menus to legend entries upon request. 
 * @author vwoeltje
 */
public class LegendEntryPopupMenuFactory {
	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(LegendEntryPopupMenuFactory.class.getName().substring(0, 
        		LegendEntryPopupMenuFactory.class.getName().lastIndexOf("."))+".Bundle");
	
	private PlotViewManifestation manifestation;

	public LegendEntryPopupMenuFactory(PlotViewManifestation targetPlotManifestation) {
		manifestation = targetPlotManifestation;
	}
	
	/**
	 * Get a popup menu for a specified legend entry
	 * @param entry the legend entry to produce a popup menu
	 * @return a popup menu with options appropriate to the specified legend entry
	 */
	public JPopupMenu getPopup(LegendEntry entry) {
		LegendEntryPopup popup = new LegendEntryPopup(manifestation, entry);
		return popup;
	}
	
	private class LegendEntryPopup extends JPopupMenu {
		private static final long serialVersionUID = -4846098785335776279L;
		
		private int spinnerValue;
		
		public LegendEntryPopup(final PlotViewManifestation manifestation, final LegendEntry legendEntry) {
			super();
			if (!manifestation.isLocked()) {						
				String name = legendEntry.getComputedBaseDisplayName();
				spinnerValue = legendEntry.getNumberRegressionPoints();
				if (name.isEmpty()) name = legendEntry.getFullBaseDisplayName();

				final LineSettings settings = legendEntry.getLineSettings();
				
				// Color submenu
				String subMenuText = String.format(BUNDLE.getString("SelectColor.label"), name);
				JMenu subMenu = new JMenu(subMenuText);
				Color currentColor = legendEntry.getForeground();

				for (int i = 0; i < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT; i++) {
					JMenuItem item = new JRadioButtonMenuItem("", 
							new SolidColorIcon(PlotLineColorPalette.getColor(i)),
							(currentColor == PlotLineColorPalette.getColor(i))
							);
					final int colorIndex = i;
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {				
							legendEntry.setForeground(PlotLineColorPalette.getColor(colorIndex));
							manifestation.persistPlotLineSettings();
						}					
					});
					subMenu.add(item);
				}

				add(subMenu);
				
				// Thickness submenu
				subMenuText = String.format(BUNDLE.getString("SelectThickness.label"), name);
				subMenu = new JMenu(subMenuText);
				for (int i = 1; i <= PlotConstants.MAX_LINE_THICKNESS; i++) {
					JMenuItem item = new JRadioButtonMenuItem("" + i, 
							(settings.getThickness() == i));
					final int thickness = i;
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {				
							settings.setThickness(thickness);
							legendEntry.setLineSettings(settings);
							manifestation.persistPlotLineSettings();
						}					
					});
					subMenu.add(item);

				}
				add(subMenu);
				
				// Marker submenu
				if (manifestation.getPlot() != null && 
					manifestation.getPlot().getPlotLineDraw().drawMarkers()) {
					subMenuText = String.format(BUNDLE.getString("SelectMarker.label"), name);
					subMenu = new JMenu(subMenuText);
					for (int i = 0; i < PlotConstants.MAX_NUMBER_OF_DATA_ITEMS_ON_A_PLOT; i++) {
						JMenuItem item = new JRadioButtonMenuItem("",
								new PlotMarkerIcon(PlotLineShapePalette.getShape(i), false),
								(settings.getMarker() == i && !settings.getUseCharacter()));
						item.setForeground(legendEntry.getForeground());
						final int marker = i;
						item.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {				
								settings.setMarker(marker);
								settings.setUseCharacter(false);
								legendEntry.setLineSettings(settings);
								manifestation.persistPlotLineSettings();
							}					
						});
						subMenu.add(item);
					}
					JMenuItem other = new JRadioButtonMenuItem(BUNDLE.getString("SelectCharacter.label"), 
							settings.getUseCharacter());
					if (!settings.getCharacter().isEmpty()) {
						FontRenderContext frc = ((Graphics2D) manifestation.getGraphics()).getFontRenderContext();
						other.setIcon(new PlotMarkerIcon(
								PlotLineShapePalette.getShape(settings.getCharacter(), frc),
								PlotLineColorPalette.getColor(settings.getColorIndex()),
								false));
					}
					other.addActionListener( new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							final CharacterDialog dialog = new CharacterDialog();
							dialog.setInitialString(settings.getCharacter());
							dialog.ok.addActionListener( new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									settings.setCharacter(dialog.field.getText().trim());
									settings.setUseCharacter(true);
									legendEntry.setLineSettings(settings);
									manifestation.persistPlotLineSettings();
								}							
							});
							dialog.setVisible(true);
						}					
					});
					subMenu.add(other);
					add(subMenu);
				}

				// Regression line
				addSeparator();
				String subMenuText2 = String.format(BUNDLE.getString("RegressionPointsLabel"), 
						name);

				final JMenuItem regressionLineCheckBox = new JCheckBoxMenuItem(BUNDLE.getString("RegressionLineLabel"),false);
				final JMenu regressionMenu = new JMenu(subMenuText2);

				SpinnerModel pointsModel = new SpinnerNumberModel(legendEntry.getNumberRegressionPoints(), 2, 100, 1);
				final JSpinner spinner = new MenuItemSpinner(pointsModel, regressionMenu);
				spinner.setPreferredSize(new Dimension(50, 20));
				spinner.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						legendEntry.setNumberRegressionPoints(Integer.parseInt(((JSpinner)e.getSource()).getValue().toString()));
					}

				});	
				
				addPopupMenuListener(new PopupMenuListener() {

					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						// TODO Auto-generated method stub
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
						if (spinnerValue != Integer.parseInt(spinner.getValue().toString())) {
							manifestation.persistPlotLineSettings();
						}
						
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
						// TODO Auto-generated method stub
						
					}

					
				});
			
				regressionLineCheckBox.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						AbstractButton abstractButton = (AbstractButton) e.getSource();
						if (abstractButton.getModel().isSelected()) {
							legendEntry.setHasRegressionLine(true);
						} else {
							legendEntry.setHasRegressionLine(false);
						}
						manifestation.persistPlotLineSettings();
						
					}
					
				});
				if (legendEntry.hasRegressionLine()) {
					regressionLineCheckBox.setSelected(true);
				} else {
					regressionLineCheckBox.setSelected(false);
				}
				add(regressionLineCheckBox);
				regressionMenu.add(spinner);
				add(regressionMenu);

			}
			
		}	
		
		private class SolidColorIcon implements Icon {
			private Color iconColor;
						
			public SolidColorIcon (Color c) {
				iconColor = c;
			}

			@Override
			public int getIconHeight() {
				return 12;
			}

			@Override
			public int getIconWidth() {
				return 48;
			}

			@Override
			public void paintIcon(Component arg0, Graphics g, int x,
					int y) {
				g.setColor(iconColor);
				g.fillRect(x, y, getIconWidth(), getIconHeight() - 1);
				g.setColor(iconColor.darker());
				g.drawRect(x, y, getIconWidth(), getIconHeight() - 1);				
			}
			
		}
		
		private class CharacterDialog extends JDialog {
			private static final long serialVersionUID = 1644116578638815212L;
			private JTextField field = new JTextField(1);
			private JButton    ok, cancel;
			
			public CharacterDialog() {
				super((Frame) SwingUtilities.windowForComponent(manifestation));
				Frame f = (Frame) SwingUtilities.windowForComponent(manifestation);
				setTitle(BUNDLE.getString("SelectCharacter.title") + " - " + 
						 f.getTitle() + " - " +
						 manifestation.getInfo().getViewName());
				
				setLocationRelativeTo((Frame) SwingUtilities.windowForComponent(manifestation));
				
				ActionListener closer = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
						dispose();
					}					
				};
				
				ok     = new JButton("OK");
				cancel = new JButton("Cancel");
				
				ok    .addActionListener(closer);
				cancel.addActionListener(closer);			
				
				getRootPane().setDefaultButton(ok);
				
				final Document doc = field.getDocument();
				if (doc instanceof AbstractDocument) {
					AbstractDocument ad = (AbstractDocument) doc;
					ad.setDocumentFilter(new DocumentFilter() {
						@Override
						public void insertString(FilterBypass bypass, int offset,
								String str, AttributeSet attr)
								throws BadLocationException {
							if (doc.getLength() + str.length() < 2 && !str.trim().isEmpty()) {
								super.insertString(bypass, offset, str, attr);
							}
						}

						@Override
						public void replace(FilterBypass bypass, int start,
								int length, String str, AttributeSet attr)
								throws BadLocationException {
							if (doc.getLength() + str.length() < 2  && !str.trim().isEmpty()) {
								super.replace(bypass, start, length, str, attr);
							}
						}
						
					});
				}
				doc.addDocumentListener(new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent arg0) {
						ok.setEnabled(doc.getLength() == 1);
					}
					@Override
					public void insertUpdate(DocumentEvent arg0) {
						ok.setEnabled(doc.getLength() == 1);						
					}
					@Override
					public void removeUpdate(DocumentEvent arg0) {
						ok.setEnabled(doc.getLength() == 1);
					}					
				});

				JPanel panel = new JPanel();
				JLabel label = new JLabel(BUNDLE.getString("SelectCharacterDescription.label"));
				
				SpringLayout layout = new SpringLayout();
				panel.setLayout(layout);
				
				panel .add(label);
				panel .add(field);
				panel .add(ok);
				panel .add(cancel);		
				
				layout.putConstraint(SpringLayout.WEST , label ,  0, SpringLayout.WEST , panel );
				layout.putConstraint(SpringLayout.WEST , field , 12, SpringLayout.EAST , label );
				layout.putConstraint(SpringLayout.EAST , panel ,  0, SpringLayout.EAST , field );

				layout.putConstraint(SpringLayout.EAST , cancel,  0, SpringLayout.EAST , panel );
				layout.putConstraint(SpringLayout.EAST , ok    , -5, SpringLayout.WEST , cancel);
				
				layout.putConstraint(SpringLayout.NORTH, label ,  0, SpringLayout.NORTH, panel );
				layout.putConstraint(SpringLayout.NORTH, field ,  0, SpringLayout.NORTH, panel );
				layout.putConstraint(SpringLayout.NORTH, cancel, 17, SpringLayout.SOUTH, field );
				layout.putConstraint(SpringLayout.NORTH, ok    , 17, SpringLayout.SOUTH, field );
				layout.putConstraint(SpringLayout.SOUTH, panel,   0, SpringLayout.SOUTH, cancel);
				layout.putConstraint(SpringLayout.SOUTH, panel ,  0, SpringLayout.SOUTH, ok    );				
				
				panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
				
				getContentPane().add(panel);
				pack();
			}
			
			private void setInitialString (String s) {
				field.setText(s.trim());
				ok.setEnabled(!s.trim().isEmpty());
			}
			
		}
		
	}

}
