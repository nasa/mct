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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.components.TimeConversion;
import gov.nasa.arc.mct.evaluator.api.Evaluator;
import gov.nasa.arc.mct.gui.FeedView;
import gov.nasa.arc.mct.gui.FeedView.RenderingCallback;
import gov.nasa.arc.mct.gui.NamingContext;
//import gov.nasa.arc.mct.gui.OptionBox;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.roles.events.AddChildEvent;
import gov.nasa.arc.mct.roles.events.PropertyChangeEvent;
import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.table.access.ServiceAccess;
import gov.nasa.arc.mct.table.dnd.TableTransferHandler;
import gov.nasa.arc.mct.table.gui.LabeledTable;
import gov.nasa.arc.mct.table.gui.TableLayoutListener;
import gov.nasa.arc.mct.table.model.AbbreviatingTableLabelingAlgorithm;
import gov.nasa.arc.mct.table.model.ComponentTableModel;
import gov.nasa.arc.mct.table.model.MultiColTableModel;
import gov.nasa.arc.mct.table.model.TableOrientation;
import gov.nasa.arc.mct.table.model.TableStructure;
//import gov.nasa.arc.mct.table.policy.TableViewPolicy;
import gov.nasa.arc.mct.table.view.TableFormattingConstants.JVMFontFamily;
import gov.nasa.arc.mct.table.view.TimeFormat.DateFormatItem;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JComponent;
//import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a visible manifestation of the the view role.
 */
@SuppressWarnings("serial")
public class MultiColView extends FeedView
		implements RenderingCallback, SelectionProvider {
	private static final Logger logger = LoggerFactory.getLogger(MultiColView.class);
	
	private LabeledTable table;
	private MultiColTableModel model;
	private AbbreviatingTableLabelingAlgorithm labelingAlgorithm;

	private Map<String, TimeConversion> timeConversionMap = new HashMap<String, TimeConversion>();
	private final AtomicReference<Collection<FeedProvider>> feedProvidersRef = new AtomicReference<Collection<FeedProvider>>(Collections.<FeedProvider>emptyList());
	private List<AbstractComponent> multipleEvaluators = new ArrayList<AbstractComponent>();
	private static final DecimalFormat[] formats;
	private boolean receivedData = false;
	private boolean updating = false;
	private TableSelectionListener selectionListener;

	static {
		formats = new DecimalFormat[11];
		formats[0] = new DecimalFormat("#");
		String formatString = "#.";
		for (int i = 1; i < formats.length; i++) {
			formatString += "0";
			DecimalFormat format = new DecimalFormat(formatString);
			formats[i] = format;
		}
	}
	
	/**
	 * Creates a view manifestation for a component.
	 * 
	 * If this is the first manifestation for a component, a new
	 * manifestation will be created with default settings. Subsequent
	 * manifestations will use the persisted properties.
	 * 
	 * @param component
	 *            the component for this manifestation
	 * @param vi the view information for this view
	 */
	public MultiColView(AbstractComponent component, ViewInfo vi) {
		super(component,vi);
		
		labelingAlgorithm = new AbbreviatingTableLabelingAlgorithm();
		setLabelingContext(labelingAlgorithm, getNamingContext());
		//TableStructure structure = new TableStructure(getManifestedComponent());
		model = new MultiColTableModel(labelingAlgorithm, this);
		model.updateLabels();
		
		TableViewCellRenderer renderer = new TableViewCellRenderer();
		
		table = new LabeledTable(model);
		table.getTable().setShowGrid(false);
		table.getTable().getColumnModel().setColumnMargin(0);
		table.getTable().setRowMargin(0);
		
		table.getTable().setDefaultRenderer(Object.class,
				renderer);
		table.getTable().setTransferHandler(
				new TableTransferHandler(this, table));
		table.getTable().setDragEnabled(true);
		table.getTable().setFillsViewportHeight(true);
			table.getTable().setDropMode(DropMode.ON_OR_INSERT);

		Color bg = getColor("background");
		setBackground(bg);
		table.setBackground(bg);
		table.getTable().setBackground(bg);
		bg = getColor("header.background");
		if (bg != null) {
			table.getTable().getTableHeader().setBackground(bg);
			table.getRowHeaders().setBackground(bg);
		}
		
		Color grid = getColor("grid");
		if (grid != null) table.setHeaderBorder(BorderFactory.createLineBorder(grid, 0));

		Color defaultValueColor = getColor("defaultValueColor");
		if (defaultValueColor != null) {
			renderer.setForeground(defaultValueColor);
			table.getRowHeaders().setForeground(defaultValueColor);
			table.getTable().getTableHeader().setForeground(defaultValueColor);
		}
		
		Color bgSelectionColor = getColor("selection.background");
		Color fgSelectionColor = getColor("selection.foreground");
		
		if (bgSelectionColor != null) {
			table.getTable().setSelectionBackground(bgSelectionColor);
		}
		if (fgSelectionColor != null) {
			table.getTable().setSelectionForeground(fgSelectionColor);
		}			
		
		TableSettings tableSettings = loadSettingsFromPersistence();
		table.setRestoringSettings(true);
		table.updateColumnsFromModel(tableSettings!=null ? tableSettings.getColumnWidths() : null);
		if (tableSettings != null) {
			setTableSettings(tableSettings, table);
		}
		
		System.out.println("debug MultiColView() +++ Right after table settings +++++++++++++++++++++++++++++++++++++++++++");
		
		updateFeedProviders();
		add(table);
		table.setRestoringSettings(false);
		table.updateColumnsHeaderValuesOnly();
		table.addTableLayoutListener(new TableLayoutListener() {
			@Override
			public void tableChanged(Object source) {
				model.updateColumnCount();
			}
		});
		
		selectionListener = new TableSelectionListener();
		
		System.out.println("debug MultiColView() +++ Right after selection listener +++++++++++++++++++++++++++++++++++++++++++");
		
		// JTable has different listeners for row and column selections so doing one or the other will miss some change events. 
		table.getTable().getTableHeader().getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
		table.getTable().getSelectionModel().addListSelectionListener(selectionListener);
		
		table.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getComponent().isEnabled()
						&& e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() == 2) {
					Point p = e.getPoint();
					int row = table.getTable().rowAtPoint(p);
					int column = table.getTable().columnAtPoint(p);
					AbstractComponent ac = (AbstractComponent) model
							.getStoredValueAt(row, column);
					if (ac != null) {
						ac.open();
					}
				}
			}
		});

		System.out.println("debug MultiColView() +++ Right after mouse listener +++++++++++++++++++++++++++++++++++++++++++");
		
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				final MultiColView table = MultiColView.this;
				Timer t = new Timer(1000, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						long startTime = Long.MIN_VALUE;
						for (FeedProvider fp : getVisibleFeedProviders()) {
							startTime = Math.max(startTime, fp
									.getTimeService().getCurrentTime());
						}
						requestData(null, startTime, startTime, null, table, true);
					}
				});
				t.setRepeats(false);
				t.start();
				removeAncestorListener(this);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
		});
		
		System.out.println("debug MultiColView() +++ end of constructor +++++++++++++++++++++++++++++++++++++++++++");

	}

	private Color getColor(String name) {
        return UIManager.getColor(name);        
    }
	
	@Override
	protected void handleNamingContextChange() {
		updateMonitoredGUI();
	}
	
	/**
	 * Set the surrounding context labels that should be removed when
	 * calculating labels for the rows, columns, and cells of the table. The
	 * surrounding context consists of the panel name in which the table is
	 * embedded. This may be set in the manifest information or may be the
	 * collection object name, if the panel title is not customized.
	 * Specifically, if the panel title is visible and non-empty, that is
	 * used as the labeling context (even if just blanks). If the panel
	 * title is invisible, the labeling context is empty. Otherwise, the
	 * labeling context is the component's display name.
	 * 
	 * @param algorithm
	 *            the table labeling algorithm
	 * @param context
	 *            to derive labels from
	 */
	private void setLabelingContext(
			AbbreviatingTableLabelingAlgorithm algorithm,
			NamingContext context) {
		String surroundingName = "";
		if (context != null) {
			/* Is some name being shown by the labeling context? */
			if (context.getContextualName() != null) {
				surroundingName = context.getContextualName(); /* Get that name. */			
				if (surroundingName.isEmpty()) {
					/* A title bar or similar is displayed, but it's not overriding our * 
					 * base displayed name */
					surroundingName = getManifestedComponent().getDisplayName();
				} 
			}
		} else {
			/* Labeling context is null, so we are in our own window or inspector */
			surroundingName = getManifestedComponent().getDisplayName();
		}
		algorithm.setContextLabels(surroundingName);
	}

	@Override
	protected JComponent initializeControlManifestation() {
		TableControlPanelController controller = new TableControlPanelController(
				this, table, model);
		return new TableSettingsControlPanel(controller);
	}

	@Override
	public SelectionProvider getSelectionProvider() {
		return this;
	}

	@Override
	public void removeSelectionChangeListener(
			PropertyChangeListener listener) {
		removePropertyChangeListener(
				SelectionProvider.SELECTION_CHANGED_PROP, listener);
	}

	@Override
	public void addSelectionChangeListener(PropertyChangeListener listener) {
		addPropertyChangeListener(SelectionProvider.SELECTION_CHANGED_PROP,
				listener);
	}

	@Override
	public Collection<View> getSelectedManifestations() {
		List<View> selectedManifestations = new ArrayList<View>();
		for (int row : table.getTable().getSelectedRows()) {
			for (int col : table.getTable().getSelectedColumns()) {
				AbstractComponent component = (AbstractComponent) model
						.getStoredValueAt(row, col);
				if (component != null
						&& component != AbstractComponent.NULL_COMPONENT) {
					selectedManifestations.add(component.getViewInfos(ViewType.NODE).iterator().next().createView(component));
				}
			}
		}
		return selectedManifestations;
	}

	private boolean isClearingSelection = false;
	private boolean orientationChanged = false;
	
	public static final String VIEW_ROLE_NAME = "AlexCopied";
	@Override
	public void clearCurrentSelections() {
		isClearingSelection = true;
		try {
			table.getTable().clearSelection();
			selectionListener.clearLastSelectedComponents();
		} finally {
			isClearingSelection = false;
		}
	}

	@Override
	public void updateMonitoredGUI(AddChildEvent event) {
		recreateTable();
	}

	/**
	 * Recreates the columns and rows of the table so the on-screen
	 * representation matches the current state of the model. First
	 * notifies the model that the underlying components may have
	 * changed (so that the model will interrogate the components for
	 * the current number of rows and columns). Then updates the set
	 * of feed providers to match the model and updates the labels by
	 * running the labeling algorithm. Afterwards, the columns in the
	 * table are recreated, in case the number of columns has changed.
	 * Finally, a table-structure-changed event is fired to force
	 * complete redisplay of the table on-screen.
	 */
	private void recreateTable() {
		//TODO: Recreate without clobbering user selections (except where appropriate)
		model.updateColumnCount();
		updateFeedProviders();
		model.updateLabels();
		table.updateColumnsFromModel(null);
		table.updateColumnsHeaderValuesOnly();
	}

	@Override
	public void updateMonitoredGUI(RemoveChildEvent event) {
		recreateTable();
	}

	@Override
	public void updateMonitoredGUI(PropertyChangeEvent event) {
		// Display name or panel title has changed. Must recalculate labels.
		setLabelingContext(labelingAlgorithm, getNamingContext());
		recreateTable();
	}

	@Override
	public Collection<FeedProvider> getVisibleFeedProviders() {
		return feedProvidersRef.get();
	}

	@Override
	public void synchronizeTime(
			Map<String, List<Map<String, String>>> data, long syncTime) {
		updateFromFeed(data);
	}

	@Override
	public void render(Map<String, List<Map<String, String>>> data) {
		if (!receivedData) {
			updateFromFeed(data);
		}
	}

	/**
	 * Extract data from the feed and push it to the table.
	 */
	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		receivedData = true;
		if (data != null) {
			Collection<FeedProvider> feeds = getVisibleFeedProviders();

			for (FeedProvider provider : feeds) {
				String feedId = provider.getSubscriptionId();
				List<Map<String, String>> dataForThisFeed = data
						.get(feedId);
				if (dataForThisFeed != null && !dataForThisFeed.isEmpty()) {
					// Process the first value for this feed.
					Map<String, String> entry = dataForThisFeed
							.get(dataForThisFeed.size() - 1);

					try {
						Object value = entry
								.get(FeedProvider.NORMALIZED_VALUE_KEY);
						RenderingInfo ri = provider.getRenderingInfo(entry);
						TableCellSettings settings = model
								.getCellSettings(provider
										.getSubscriptionId());
						
						if (settings.getEvaluator() != null) {
							ri = settings
									.getEvaluator()
									.getCapability(Evaluator.class)
									.evaluate(
											data,
											Collections
													.singletonList(provider));
							value = ri.getValueText();							
						} else {
							if (provider.getFeedType() != FeedType.STRING) {
								if (settings.getDateFormat() != null && settings.getDateFormat() !=  DateFormatItem.None) {
									TimeConversion tc = timeConversionMap.get(provider.getSubscriptionId());								
									value = TimeFormat.applySimpleDateFormat(settings.getDateFormatter(),
											tc, value.toString());
								}  else {
									value = executeDecimalFormatter(provider,
											value.toString(), data, settings);
								}
							}
						}
						
						DisplayedValue displayedValue = new DisplayedValue();
						displayedValue.setStatusText(ri.getStatusText());
						displayedValue.setValueColor(ri.getValueColor());
						if (ri.getStatusText().isEmpty() || ri.getStatusText().equals(" ")) {
							if (settings.getFontColor() != null) {
								displayedValue.setValueColor(settings.getFontColor());
							}
						}
//						Set color according to font color settings, as long as value is valid
						displayedValue.setValue(ri.isValid() ? value
								.toString() : "");
						displayedValue.setNumberOfDecimals(settings
								.getNumberOfDecimals());
						displayedValue
								.setAlignment(settings.getAlignment());

						model.setValue(provider.getSubscriptionId(),
								displayedValue);
						
					} catch (ClassCastException ex) {
						logger.error("Feed data entry of unexpected type",
								ex);
					} catch (NumberFormatException ex) {
						logger.error(
								"Feed data entry does not contain parsable value",
								ex);
					}
				}
			}

			// execute multiple evaluators to ensure their values are
			// updated also
			for (AbstractComponent multi : multipleEvaluators) {
				Evaluator evaluator = multi.getCapability(Evaluator.class);
				FeedProvider.RenderingInfo info = evaluator.evaluate(data,
						getFeedProviders(multi));
				if (info != null && info.getValueText() != null) {
					TableCellSettings settings = model.getCellSettings(model
							.getKey(multi));
					DisplayedValue displayedValue = new DisplayedValue();
					displayedValue.setValue(info.getValueText());
					displayedValue.setStatusText(info.getStatusText());
					displayedValue.setValueColor(info.getValueColor());
					displayedValue.setNumberOfDecimals(settings
							.getNumberOfDecimals());
					displayedValue.setAlignment(settings.getAlignment());
	
					assert !info.getValueColor().equals(Color.white) : "attempting to rendering white text on white foreground";
					if (info.getStatusText().isEmpty() || info.getStatusText().equals(" ")) {
						if (settings.getFontColor() != null) {
							displayedValue.setValueColor(settings.getFontColor());
						}
					}
					model.setValue(model.getKey(multi), displayedValue);
				}
			}
		} else {
			logger.debug("Data was null");
		}
	}

	private List<FeedProvider> getFeedProviders(AbstractComponent component) {
		List<FeedProvider> feedProviders = new ArrayList<FeedProvider>(
				component.getComponents().size());
		for (AbstractComponent referencedComponent : component.getComponents()) {
			FeedProvider fp = referencedComponent.getCapability(
					FeedProvider.class);
			if (fp != null) {
				feedProviders.add(fp);
			}
		}
		return feedProviders;
	}

	/**
	 * Formats decimal places for the given value.
	 * 
	 * @param value
	 *            current value for the cell
	 * @return evaluated value
	 */
	private String executeDecimalFormatter(final FeedProvider provider,
			final String feedValue,
			final Map<String, List<Map<String, String>>> data,
			TableCellSettings cellSettings) {
		String rv = feedValue;


		// apply decimal places formatting if appropriate
		FeedType feedType = provider.getFeedType();
		int decimalPlaces = cellSettings.getNumberOfDecimals();
		if (feedType == FeedType.FLOATING_POINT
				|| feedType == FeedType.INTEGER) {
			if (decimalPlaces == -1) {
				decimalPlaces = (feedType == FeedType.FLOATING_POINT) ? TableCellSettings.DEFAULT_DECIMALS
						: 0;
			}
			try {
				rv = formats[decimalPlaces]
				             .format(FeedType.FLOATING_POINT
				            		 .convert(feedValue));
			} catch (IllegalFormatException ife) {
				logger.error("unable to format", ife);
			} catch (NumberFormatException nfe) {
				logger.error(
						"unable to convert value to expected feed value",
						nfe);
			}

			
		}
		return rv;
	}

	private void updateFeedProviders() {
		MultiColTableModel model = new MultiColTableModel(labelingAlgorithm, this);
		updateFeedProviders(model);
		updateEvalutors();
		loadHeaderSettings();
		loadCellSettings();
	}

	private void loadHeaderSettings() {
		for (int row = 0; row < model.getRowCount(); ++row) {
			String labelAbbreviations = getViewProperties()
					.getProperty("ROW_LABEL_ABBREVIATIONS_" + row, String.class);
			if (labelAbbreviations != null) {
				LabelAbbreviations abbrevs = new LabelAbbreviations();
				abbrevs.addAbbreviationsFromString(labelAbbreviations);
				model.setRowLabelAbbreviations(row, abbrevs);
			}
		}

		for (int col = 0; col < model.getColumnCount(); ++col) {
			String labelAbbreviations = getViewProperties()
					.getProperty("COLUMN_LABEL_ABBREVIATIONS_" + col, String.class);
			if (labelAbbreviations != null) {
				LabelAbbreviations abbrevs = new LabelAbbreviations();
				abbrevs.addAbbreviationsFromString(labelAbbreviations);
				model.setColumnLabelAbbreviations(col, abbrevs);
			}
		}
	}

	private boolean saveHeaderSettings(boolean orientationChanged) {
		boolean settingsChanged = orientationChanged;
		ExtendedProperties viewProperties = getViewProperties();
		for (int row = 0; row < model.getRowCount(); ++row) {
			String oldLabelAbbreviations = getViewProperties()
					.getProperty("ROW_LABEL_ABBREVIATIONS_" + row, String.class);
			String newLabelAbbreviations = model.getRowLabelAbbreviations(
					row).toString();
			if (orientationChanged || !newLabelAbbreviations.equals(oldLabelAbbreviations)) {
				viewProperties.setProperty(
						"ROW_LABEL_ABBREVIATIONS_" + row,
						newLabelAbbreviations);
				settingsChanged = true;
			}
		}

		for (int col = 0; col < model.getColumnCount(); ++col) {
			String oldLabelAbbreviations = getViewProperties()
					.getProperty("COLUMN_LABEL_ABBREVIATIONS_" + col, String.class);
			String newLabelAbbreviations = model
					.getColumnLabelAbbreviations(col).toString();
			if (orientationChanged || !newLabelAbbreviations.equals(oldLabelAbbreviations)) {
				viewProperties.setProperty(
						"COLUMN_LABEL_ABBREVIATIONS_" + col,
						newLabelAbbreviations);
				settingsChanged = true;
			}
		}
		orientationChanged = false;
		return settingsChanged;
	}

	private void loadCellSettings() {
		for (int row = 0; row < model.getRowCount(); ++row) {
			for (int col = 0; col < model.getColumnCount(); ++col) {
				AbstractComponent component = (AbstractComponent) model
						.getStoredValueAt(row, col);
				if (component != null) {
					loadCellSettings(component);
				}
			}
		}
	}

	/**
	 * Extracts the evaluators from the set of components. An evaluator is
	 * associated with a component if the evaluator references only that
	 * component.
	 */
	private void updateEvalutors() {
		multipleEvaluators.clear();
		for (AbstractComponent component : getManifestedComponent().getComponents()) {
			addMultisOneLevel(component);
			addMultis(multipleEvaluators, component);
		}
		addMultis(multipleEvaluators, getManifestedComponent());
	}

	private void addMultis(List<AbstractComponent> multis,
			AbstractComponent component) {
		if (isEvaluator(component)
				&& component.getCapability(Evaluator.class)
						.requiresMultipleInputs()) {
			multis.add(component);
		}
	}

	private void addMultisOneLevel(AbstractComponent root) {
		for (AbstractComponent component : root.getComponents()) {
			if (isEvaluator(component) && component.getCapability(Evaluator.class).requiresMultipleInputs()) {
				addMultis(multipleEvaluators, component);
			}
		}
	}

	private boolean isEvaluator(AbstractComponent comp) {
		return comp.getCapability(Evaluator.class) != null;
	}

	private void updateFeedProviders(MultiColTableModel model) {
		ArrayList<FeedProvider> feedProviders = new ArrayList<FeedProvider>();
		timeConversionMap.clear();
		for (int rowIndex = 0; rowIndex < model.getRowCount(); ++rowIndex) {
			for (int columnIndex = 0; columnIndex < model
					.updateColumnCount(); ++columnIndex) {
				AbstractComponent component = model.getComponent(rowIndex);
				if (component != null) {
					FeedProvider fp = getFeedProvider(component);
					if (fp != null) {
						feedProviders.add(fp);
						TimeConversion tc = component.getCapability(TimeConversion.class);
						if (tc != null) {
							timeConversionMap.put(fp.getSubscriptionId(), tc);
						}							
					} else {
						if (component.getCapability(Evaluator.class) != null) {
							for (AbstractComponent referencedComponent : component.getComponents()) {
								fp = getFeedProvider(referencedComponent);
								if (fp != null) {
									feedProviders.add(fp);
								}
							}
						}
					}
				}
			}
		}
		feedProviders.trimToSize();
		feedProvidersRef.set(feedProviders);
	}

	// Update the view manifestation when the manifestation info has
	// changed in the database.
	@Override
	public void updateMonitoredGUI() {
	    if (!updating) {
	        setTableSettings(loadSettingsFromPersistence(), table);
	    	recreateTable();
	    } 
	}

	@Override
	public void clear(Collection<FeedProvider> feedProviders) {
		for (FeedProvider provider : feedProviders) {
			model.setValue(provider.getSubscriptionId(), "");
		}
	}

	/**
	 * Load the settings for the manifestation from persistence.
	 * 
	 * @return
	 */
	TableSettings loadSettingsFromPersistence() {
		TableSettings settings = new TableSettings();

		for (TableSettings.AvailableSettings setting : TableSettings.AvailableSettings
				.values()) {
			try {
				String name = setting.name();
				String value = getViewProperties().getProperty(name, String.class);
				settings.setValue(setting, value);
			} catch (Exception ex) {
				logger.error("exception when loading persistent settings", ex);
				// ignore - no persisted setting
			}
		}

		return settings;
	}

	private void loadCellSettings(AbstractComponent component) {
		String id = model.getKey(component);
		TableCellSettings cellSettings = model.getCellSettings(id);
		String value = getViewProperties().getProperty(
				"EVALUATOR_" + id, String.class);
		if (value == null || value.isEmpty()) {
			cellSettings.setEvaluator(null);
		} else {
			cellSettings.setEvaluator(findEvaluator(component, value));
		}

		LabelAbbreviations abbreviations = new LabelAbbreviations();
		value = getViewProperties().getProperty(
				"LABEL_ABBREVIATIONS_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			abbreviations.addAbbreviationsFromString(value);
		}
		model.setCellLabelAbbreviations(id, abbreviations);

		value = getViewProperties().getProperty("DECIMAL_PLACES_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setNumberOfDecimals(Integer.valueOf(value));
		}
		
		value = getViewProperties().getProperty("FONT_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setCellFont(Enum.valueOf(TableFormattingConstants.JVMFontFamily.class, value));
		}
		
		value = getViewProperties().getProperty("FONT_COLOR_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setFontColor(new Color(Integer.valueOf(value).intValue()));
		}
		
		value = getViewProperties().getProperty("BG_COLOR_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setBackgroundColor(new Color(Integer.valueOf(value).intValue()));
		}
		
		value = getViewProperties().getProperty("FONT_SIZE_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setFontSize(Integer.valueOf(value).intValue());
		}
		
		value = getViewProperties().getProperty("FONT_STYLE_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setFontStyle(Integer.valueOf(value).intValue());
		}

		value = getViewProperties().getProperty("AS_DATE_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setDateFormat(DateFormatItem.valueOf(value));
		}
		
		value = getViewProperties().getProperty("ALIGNMENT_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setAlignment(ContentAlignment.valueOf(value));
		}
		
		value = getViewProperties().getProperty("BORDER_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setCellBorderState(new BorderState(value));
		}
		
		value = getViewProperties().getProperty("FONT_UNDERLINE_" + id, String.class);
		if (value != null && !value.isEmpty()) {
			cellSettings.setTextAttributeUnderline(Integer.valueOf(value).intValue());
		}
	}

	private AbstractComponent findEvaluator(AbstractComponent component,
			String id) {
		// The component may be its own evaluator.
		if (id.equals(component.getId())) {
			return component;
		}

		AbstractComponent parent = AbstractComponent.getComponentById(id);
		Evaluator e = parent == null ? null : parent.getCapability(Evaluator.class);
		if (e != null && !e.requiresMultipleInputs()
				&& parent.getId().equals(id)) {

			return parent;
		}

		return null;
	}

	private void saveSettingsToPersistence() {
		boolean settingsChanged = false;
		TableSettings settings = getCurrentTableSettings(table);
		ExtendedProperties viewProperties = getViewProperties();
		for (TableSettings.AvailableSettings setting : TableSettings.AvailableSettings
				.values()) {
			String currentValue = getViewProperties().getProperty(
					setting.name(), String.class);
			String newValue = settings.getValue(setting);
			assert (newValue != null) : "Table setting for "
					+ setting.toString() + " has null value";
			if (!newValue.equals(currentValue)) {
				viewProperties.setProperty(setting.name(),
						newValue);
				settingsChanged = true;
			}
		}

		// Save label settings.
		settingsChanged = (saveHeaderSettings(orientationChanged) || settingsChanged);

		// Also save all cell settings.
		for (int row = 0; row < model.getRowCount(); ++row) {
			for (int col = 0; col < model.getColumnCount(); ++col) {
				AbstractComponent component = (AbstractComponent) model
						.getStoredValueAt(row, col);
				if (component != null) {
					settingsChanged = (saveCellSettings(component) || settingsChanged);
				}
			}
		}

		if (settingsChanged) {
		    try {
		        updating = true;
		        table.updateColumnsHeaderValuesOnly();
		    	getManifestedComponent().save();
		    } finally {
		        updating = false;
		    }
		}
	}

	private boolean saveCellSettings(AbstractComponent component) {
		boolean settingsChanged = false;

		String id = model.getKey(component);
		TableCellSettings cellSettings = model.getCellSettings(id);
		String propertyName = "EVALUATOR_" + id;
		String currentValue = getViewProperties().getProperty(
				propertyName, String.class);
		ExtendedProperties viewProperties = getViewProperties();
		if (currentValue == null) {
			currentValue = "";
		}
		String newValue = (cellSettings.getEvaluator() == null) ? ""
				: cellSettings.getEvaluator().getId();
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}

		LabelAbbreviations abbrevs = model.getCellLabelAbbreviations(id);
		propertyName = "LABEL_ABBREVIATIONS_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = abbrevs.toString();
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}

		propertyName = "DECIMAL_PLACES_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = Integer.toString(cellSettings.getNumberOfDecimals());
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}

		propertyName = "ALIGNMENT_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = cellSettings.getAlignment().toString();
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}

		propertyName = "BORDER_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = cellSettings.getCellBorderState().toString();
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "AS_DATE_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		
		newValue = cellSettings.getDateFormat().toString();
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "FONT_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = cellSettings.getCellFont().name();
		newValue = (newValue == null ? "" : newValue);
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "FONT_SIZE_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = String.valueOf(cellSettings.getFontSize());
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "FONT_COLOR_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = (cellSettings.getForegroundColor() != null ? 
				String.valueOf(cellSettings.getForegroundColor().getRGB()) : "");
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "BG_COLOR_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = (cellSettings.getBackgroundColor() != null ? 
				String.valueOf(cellSettings.getBackgroundColor().getRGB()) : "");
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "FONT_STYLE_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = String.valueOf(cellSettings.getFontStyle());
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		propertyName = "FONT_UNDERLINE_" + id;
		currentValue = getViewProperties().getProperty(propertyName, String.class);
		if (currentValue == null) {
			currentValue = "";
		}
		newValue = String.valueOf(cellSettings.getTextAttributeUnderline());
		if (!currentValue.equals(newValue)) {
			viewProperties.setProperty(propertyName, newValue);
			settingsChanged = true;
		}
		
		return settingsChanged;

	}

	private void setTableSettings(TableSettings settings, LabeledTable table) {
		TableOrientation orientation = settings.getOrientation();
		int[] columnWidths = settings.getColumnWidths();
		int[] columnOrder = settings.getColumnOrder();
		int[] rowHeights = settings.getRowHeights();
		ContentAlignment[] rowHeaderAlignments = settings
				.getRowHeaderAlignments();
		ContentAlignment[] columnHeaderAlignments = settings
				.getColumnHeaderAlignments();
		JVMFontFamily[] rowFontNames = settings.getRowFontNames();
		int[] rowFontColors = settings.getRowFontColors();
		int[] rowBackgroundColors = settings.getRowBackgroundColors();
		int[] rowHeaderBorderColors = settings.getRowHeaderBorderColors();
		int[] rowFontSizes = settings.getRowFontSizes();
		int[] rowFontStyles = settings.getRowFontStyles();
		int[] rowTextAttributes = settings.getRowHeaderTextAttributes();
		BorderState[] rowHeaderBorderStates = settings.getRowHeaderBorderStates();
		BorderState[] columnHeaderBorderStates = settings.getColumnHeaderBorderStates();
		JVMFontFamily[] colFontNames = settings.getColumnFontNames();
		int[] columnFontColors = settings.getColumnFontColors();
		int[] columnBackgroundColors = settings.getColumnBackgroundColors();
		int[] columnHeaderBorderColors = settings.getColumnHeaderBorderColors();
		int[] colFontSizes = settings.getColumnFontSizes();
		int[] colFontStyles = settings.getColumnFontStyles();
		int[] columnTextAttributes = settings.getColumnHeaderTextAttributes();
		

		if (orientation != null) {
			labelingAlgorithm.setOrientation(orientation);
			model.setOrientation(orientation);
			table.updateDropMode();
		}
		// Set the column order first, so that the right values get the
		// right
		// width once we set the widths.
		if (columnOrder != null) {
			table.setColumnOrder(columnOrder);

		}
		if (columnWidths != null) {
			table.setColumnWidths(columnWidths);
		}
		if (rowHeights != null) {
			table.setRowHeights(rowHeights);
		}
		if (rowHeaderAlignments != null) {
			table.setRowHeaderAlignments(rowHeaderAlignments);
		}
		if (columnHeaderAlignments != null) {
			table.setColumnHeaderAlignments(columnHeaderAlignments);
		}
		if (rowFontNames != null) {
			table.setRowHeaderFontNames(rowFontNames);
		}
		if (rowFontColors != null) {
			table.setRowHeaderFontColors(intToColorArray(rowFontColors));
		}
		if (rowBackgroundColors != null) {
			table.setRowHeaderBackgroundColors(intToColorArray(rowBackgroundColors));
		}
		if (rowHeaderBorderColors != null) {
			table.setRowHeaderBorderColors(intToColorArray(rowHeaderBorderColors));
		}
		if (rowFontSizes != null) {
			table.setRowHeaderFontSizes(intToIntegerArray(rowFontSizes));
		}
		if (rowFontStyles != null) {
			table.setRowHeaderFontStyles(intToIntegerArray(rowFontStyles));
		}
		if (rowTextAttributes != null) {
			table.setRowHeaderTextAttributes(intToIntegerArray(rowTextAttributes));
		}
		if (rowHeaderBorderStates != null) {
			table.setRowHeaderBorderStates(rowHeaderBorderStates);
		}
		if (colFontNames != null) {
			table.setColumnHeaderFontNames(colFontNames);
		}
		if (columnFontColors != null) {
			table.setColumnHeaderFontColors(intToColorArray(columnFontColors));
		}
		if (columnBackgroundColors != null) {
			table.setColumnHeaderBackgroundColors(intToColorArray(columnBackgroundColors));
		}
		if (columnHeaderBorderColors != null) {
			table.setColumnHeaderBorderColors(intToColorArray(columnHeaderBorderColors));
		}
		if (colFontSizes != null) {
			table.setColumnHeaderFontSizes(intToIntegerArray(colFontSizes));
		}
		if (colFontStyles != null) {
			table.setColumnHeaderFontStyles(intToIntegerArray(colFontStyles));
		}
		
		if (columnTextAttributes != null) {
			table.setColumnHeaderTextAttributes(intToIntegerArray(columnTextAttributes));
		}
		if (columnHeaderBorderStates != null) {
			table.setColumnHeaderBorderStates(columnHeaderBorderStates);
		}
		table.getTable().setShowGrid(settings.isShowGrid());
	}

	private TableSettings getCurrentTableSettings(LabeledTable table) {
		TableSettings settings = new TableSettings();
		settings.setOrientation(model.getOrientation());
		settings.setColumnWidths(table.getColumnWidths());
		settings.setColumnOrder(table.getColumnOrder());
		settings.setShowGrid(table.getShowGrid());
		settings.setRowHeights(integerToIntArray(table.getRowHeights()));
		settings.setRowHeaderAlignments(table.getRowHeaderAlignments());
		settings.setColumnHeaderAlignments(table
				.getColummnHeaderAlignments());
		settings.setRowFontNames(table.getRowHeaderFontNames());
		settings.setRowFontColors(colorToIntArray(table.getRowHeaderFontColors()));
		settings.setRowHeaderBorderColors(colorToIntArray(table.getRowHeaderBorderColors()));
		settings.setRowBackgroundColors(colorToIntArray(table.getRowHeaderBackgroundColors()));
		settings.setRowFontSizes(integerToIntArray(table.getRowHeaderFontSizes()));
		settings.setRowFontStyles(integerToIntArray(table.getRowHeaderFontStyles()));
		settings.setRowTextAttributes(integerToIntArray(table.getRowHeaderTextAttributes()));
		settings.setColumnFontNames(table.getColumnHeaderFontNames());
		settings.setColumnFontColors(colorToIntArray(table.getColumnHeaderFontColors()));
		settings.setColumnBackgroundColors(colorToIntArray(table.getColumnHeaderBackgroundColors()));
		settings.setColumnHeaderBorderColors(colorToIntArray(table.getColumnHeaderBorderColors()));
		settings.setColumnFontSizes(integerToIntArray(table.getColumnHeaderFontSizes()));
		settings.setColumnFontStyles(integerToIntArray(table.getColumnHeaderFontStyles()));
		settings.setColumnTextAttributes(integerToIntArray(table.getColumnHeaderTextAttributes()));
		settings.setRowHeaderBorderStates(table.getRowHeaderBorderStates());
		settings.setColumnHeaderBorderStates(table.getColumnHeaderBorderStates());
		

		return settings;
	}
	
	private Color[] intToColorArray(int[] a) {
		Color[] newArray = new Color[a.length];
		for (int i = 0; i < a.length ; i++) {
			newArray[i] = new Color(a[i]);
		}
		return newArray;
	}
	
	private int[] colorToIntArray(Color[] a) {
		int[] newArray = new int[a.length];
		for (int i = 0; i < a.length ; i++) {
			newArray[i] = a[i].getRGB();
		}
		return newArray;
	}
	
	private int[] integerToIntArray(Integer[] a) {
		int[] newArray = new int[a.length];
		for (int i = 0; i < a.length ; i++) {
			newArray[i] = a[i].intValue();
		}
		return newArray;
	}
	
	private Integer[] intToIntegerArray(int[] a) {
		Integer[] newArray = new Integer[a.length];
		for (int i = 0; i < a.length ; i++) {
			newArray[i] = Integer.valueOf(a[i]);
		}
		return newArray;
	}

	/**
	 * Save the cell settings when they are changed in the formatting panel.
	 */
	public void saveCellSettings() {
		saveSettingsToPersistence();
	}

	/**
	 * Save the cell settings when table orientation is changed in the formatting panel.
	 */
	public void saveCellSettingsUponTableOrientationChange() {
		orientationChanged = true;
		saveSettingsToPersistence();
	}
	
	/**
	 * Performs the object interactions required to drop an object into the
	 * table.
	 * 
	 * @param sourceViews
	 *            an array of view roles for the components dropped onto the
	 *            table
	 * @param row
	 *            the row at which the drop occurred
	 * @param column
	 *            the column at which the drop occurred
	 * @param isInsertRow
	 *            true, if we should insert a row at the drop position
	 * @param isInsertColumn
	 *            true, if we should insert a column at the drop position
	 * @return true, if the drop was successful
	 */
	//commented out as part of the TableStructure setValue() debacle
	/*public boolean handleDrop(View[] sourceViews, int row,
			int column, boolean isInsertRow, boolean isInsertColumn) {

		if (sourceViews.length > 0) {
			AbstractComponent component = sourceViews[0].getManifestedComponent(); 
			Collection<AbstractComponent> sourceComponents = Collections
					.singleton(component);
			AbstractComponent targetComponent = model
					.getModifiedComponentAt(row, column, isInsertRow,
							isInsertColumn);

			final ExecutionResult result = checkDropPolicy(targetComponent,
					sourceComponents, this);

			if (result.getStatus()) {
				model.setValueAt(component, row, column, isInsertRow,
						isInsertColumn);
				recreateTable();
			} else {
				// Action is _not_ permitted under policy constraint
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Inform the user that policy prohibited the
						// operation.
						OptionBox.showMessageDialog(
								MultiColView.this,
								result.getMessage(),
								"Composition Error - ",
								OptionBox.ERROR_MESSAGE);
					}
				});
			}
		}

		return true;
	}*/

	private ExecutionResult checkDropPolicy(
			AbstractComponent targetComponent,
			Collection<AbstractComponent> sourceComponents,
			View targetViewManifesation) {
		// Establish policy context.
		PolicyContext context = new PolicyContext();
		context.setProperty(
				PolicyContext.PropertyName.TARGET_COMPONENT.getName(),
				targetComponent);
		context.setProperty(
				PolicyContext.PropertyName.SOURCE_COMPONENTS.getName(),
				sourceComponents);
		context.setProperty(PolicyContext.PropertyName.ACTION.getName(),
				Character.valueOf('w'));
		context.setProperty(
				PolicyContext.PropertyName.VIEW_MANIFESTATION_PROVIDER
						.getName(), targetViewManifesation);
		String compositionKey = PolicyInfo.CategoryType.COMPOSITION_POLICY_CATEGORY
				.getKey();
		// Execute policy
		return ServiceAccess.getService(PolicyManager.class).execute(
				compositionKey, context);
	}
	
	/**
     * Gets abbreviation view properties from persistence. Does a swap of row/col upon a table orientation 
     * and persists.  Also updates the the new properties in the model. Uses the inclusive
     * square so that persisted settings for non-square tables are removed.
     * @param orientation the new table orientation
     */
	public void swapAbbreviationsUponTableOrientationChange(TableOrientation orientation) {

		int inclusiveSquare = Math.max(model.getRowCount(), model.getColumnCount());

		String[] rowAbbrevProperties = new String[inclusiveSquare];
		String[] colAbbrevProperties = new String[inclusiveSquare];

		// get all the row and col headers
		for (int i = 0; i < inclusiveSquare; i++) {
			rowAbbrevProperties[i] = getViewProperties().getProperty("ROW_LABEL_ABBREVIATIONS_" + i, String.class);
		}
		for (int j = 0; j < inclusiveSquare; j++) {
			colAbbrevProperties[j] = getViewProperties().getProperty("COLUMN_LABEL_ABBREVIATIONS_" + j, String.class);
		}

		//swap for persistence.  Also update the model.
		String labelAbbreviations = null;

		for (int i = 0; i < inclusiveSquare; i++) {
			labelAbbreviations = rowAbbrevProperties[i];
			if (labelAbbreviations == null) labelAbbreviations = "";
			// rather  than setting view properties here, we let saveHeaderSettings() set view properties.
			LabelAbbreviations abbrevs = new LabelAbbreviations();
            abbrevs.addAbbreviationsFromString(labelAbbreviations);
			model.setColumnLabelAbbreviations(i, abbrevs);
		}
		for (int j = 0; j < inclusiveSquare; j++) {
			labelAbbreviations = colAbbrevProperties[j];
			if (labelAbbreviations == null) labelAbbreviations = "";
			LabelAbbreviations abbrevs = new LabelAbbreviations();
			abbrevs.addAbbreviationsFromString(labelAbbreviations);
			model.setRowLabelAbbreviations(j, abbrevs);
		}
	}

	/**
	 * @author dcberrio
	 * A ListSelectionListener that caches the components selected in a table, in 
	 * order to conflate row and column change events
	 */
	public class TableSelectionListener implements ListSelectionListener {

		/**
		 * keep track of the last selected set of components this is done to eliminate duplicate events when 
		 * both the row and column have changed, since this would result in two events. 
		 */
		private Set<AbstractComponent> lastSelectedComponents = new HashSet<AbstractComponent>();
		
		private boolean lastSelectedComponentsAreSame(Collection<View> selectedManifestations) {
			Set<AbstractComponent> currentSelectedComponents = new HashSet<AbstractComponent>();
			for (View view:selectedManifestations) {
				currentSelectedComponents.add(view.getManifestedComponent());
			}
			boolean sameList = currentSelectedComponents.size() == lastSelectedComponents.size() &&
							   lastSelectedComponents.containsAll(currentSelectedComponents);
			lastSelectedComponents = currentSelectedComponents;
			return sameList;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			
			Collection<View> selectedManifestations = getSelectedManifestations();
			if (selectedManifestations.isEmpty())
				return;
			
			if (!lastSelectedComponentsAreSame(selectedManifestations) && !isClearingSelection) {
				MultiColView.this.firePropertyChange(
						SelectionProvider.SELECTION_CHANGED_PROP,
						null, selectedManifestations);
			}
		}
		
		public void clearLastSelectedComponents() {
			lastSelectedComponents.clear();
		}

	}
}