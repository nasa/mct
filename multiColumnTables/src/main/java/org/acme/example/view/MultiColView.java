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

package org.acme.example.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.components.FeedProvider.FeedType;
import gov.nasa.arc.mct.components.FeedProvider.RenderingInfo;
import gov.nasa.arc.mct.components.TimeConversion;
import gov.nasa.arc.mct.gui.FeedView;
import gov.nasa.arc.mct.gui.FeedView.RenderingCallback;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class MultiColView extends FeedView implements RenderingCallback {
	private static final int DEFAULT_DECIMALS = 2;
	private static final ContentAlignment DEFAULT_ALIGN = ContentAlignment.RIGHT;

	private JTable table;
	private MultiColTableModel model;
	private TableSettingsControlPanel tableSettingsControlPanel;
	
	private Map<String, TimeConversion> timeConversionMap = new HashMap<String, TimeConversion>();
	private final AtomicReference<Collection<FeedProvider>> feedProvidersRef = new AtomicReference<Collection<FeedProvider>>(Collections.<FeedProvider>emptyList());
	private static final DecimalFormat[] formats;
	public static final String HIDDEN_COLUMNS_PROP = "HIDDEN_COLUMNS_PROP";
	private static final Logger logger = LoggerFactory.getLogger(MultiColView.class);
    
	//used in a depth-first-search to retrieve all nodes of a graph
	private HashSet<Integer> visited = new HashSet<Integer>();
	
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
	 * Given a root-node, perform a depth-first-search and return a list containing that root-node
	 * as well as all components connected to the root node.  This method can handle arbitrary connected
	 * graphs, that is, connected graphs that may or may not contain a cycle.
	 * 
	 *     Note: We expect this function will not be called on very large graphs (no more than a few
	 *           hundred--even I have not made than many tidbits in MCT) so we chose a recursive
	 *           solution.  
	 * 
	 * @param root the start of the depth-first-search
	 * @param list at the end of the function, this list will contain root as well as
	 *        all components connected to root
	 */
	private void getNodes(AbstractComponent root, List<AbstractComponent> list ) {
		list.add(root);
		visited.add(root.hashCode());
		for(AbstractComponent child : root.getComponents() ) {
			if( !visited.contains(child.hashCode() ) )
				getNodes(child, list);				
		}
	}

	public MultiColView(AbstractComponent ac, ViewInfo vi) {
		super(ac,vi);
		JPanel viewPanel = new JPanel(new BorderLayout());

		ViewSettings settings = new ViewSettings();
		
		AbstractComponent component = getManifestedComponent();
		List<AbstractComponent> childrenList = new ArrayList<AbstractComponent>();

		getNodes(component, childrenList);
		
		
		//If no children, we display the selectedComponent. 
		if(childrenList.size()==0) {
			childrenList = new ArrayList<AbstractComponent>();
			childrenList.add(component);
		}
		//We ignore any components without feed providers
		List<AbstractComponent> tempList = new ArrayList<AbstractComponent>();
		for(AbstractComponent child : childrenList) {
			if(child.getCapability(FeedProvider.class)!=null) {
				tempList.add(child);
				component.addViewManifestation(this);
			}
		}
		childrenList = tempList;
		model = new MultiColTableModel(childrenList, settings);
		
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		table.setShowGrid(false);
		table.setFillsViewportHeight(true);
		table.setBorder(BorderFactory.createEmptyBorder());
		viewPanel.setBorder(BorderFactory.createEmptyBorder());
		
		//We set up the cell and header renderers for each column.
		MultiColColumnRenderer colHeaderRender = new MultiColColumnRenderer();
		DynamicValueCellRender dynamicValueCellRender = new DynamicValueCellRender();
		TimeCellRender timeCellRender = new TimeCellRender();
		MultiColCellRenderer cellRender = new MultiColCellRenderer();
		TableColumnModel colModel = table.getColumnModel();
		ArrayList<ColumnType> colList = settings.getColumnTypes();
		for(ColumnType colType : colList) {
			colModel.getColumn(settings.getIndexForColumn(colType)).setHeaderRenderer(colHeaderRender);
			if(colType==ColumnType.VALUE) {
				colModel.getColumn(settings.getIndexForColumn(colType)).setCellRenderer(dynamicValueCellRender);
			} else if(colType==ColumnType.TIME) {
				colModel.getColumn(settings.getIndexForColumn(colType)).setCellRenderer(timeCellRender);
			} else {
				colModel.getColumn(settings.getIndexForColumn(colType)).setCellRenderer(cellRender);
			}
		}
		
		viewPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
		viewPanel.add(table, BorderLayout.CENTER);
		
		setColorsToDefaults();
		table.getColumnModel().setColumnMargin(1);
		
		add(viewPanel, BorderLayout.NORTH);
		updateFeedProviders();
		
		tableSettingsControlPanel = new TableSettingsControlPanel(settings, table, this);
		
		// Apply column show/hide states from view properties
		Set<Object> hiddenColIds = getViewProperties().getProperty(HIDDEN_COLUMNS_PROP);
		if (hiddenColIds != null && !hiddenColIds.isEmpty()) {
			List<String> hiddenColIdList = new ArrayList<String>();
			for (Object id : hiddenColIds) {
				tableSettingsControlPanel.removeTableColumn(ColumnType.valueOf((String) id));
				hiddenColIdList.add((String) id);
			}
			tableSettingsControlPanel.updateColumnVisibilityStates(hiddenColIdList);
		}
	}
	
	private void setColorsToDefaults() {
		Color bg = UIManager.getColor("TableViewManifestation.background");
		setBackground(bg);
		table.setBackground(bg);
		bg = UIManager.getColor("TableViewManifestation.foreground");
		table.setForeground(bg);
		bg = UIManager.getColor("TableViewManifestation.header.background");
		if(bg!=null) {
			table.getTableHeader().setBackground(bg);
		}
		table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
		Color defaultValueColor = UIManager.getColor("TableViewManifestation.defaultValueColor");
		if(defaultValueColor!=null) {
			table.getTableHeader().setForeground(defaultValueColor);
		}
		Color bgSelectionColor = UIManager.getColor("TableViewManifestation.selection.background");
		if(bgSelectionColor!=null) {
			table.setSelectionBackground(bgSelectionColor);
		}
		Color fgSelectionColor = UIManager.getColor("TableViewManifestation.selection.foreground");
		if(fgSelectionColor!=null) {
			table.setSelectionForeground(fgSelectionColor);
		}
	}

	@Override
	public void render(Map<String, List<Map<String, String>>> data) {
		updateFromFeed(data);
	}

	@Override
	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
		if (data != null) {
			Collection<FeedProvider> feeds = getVisibleFeedProviders();
			for (FeedProvider provider : feeds) {
				String feedId = provider.getSubscriptionId();
				List<Map<String, String>> dataForThisFeed = data
						.get(feedId);
				if (dataForThisFeed != null && !dataForThisFeed.isEmpty()) {
					Map<String, String> entry = dataForThisFeed
							.get(dataForThisFeed.size() - 1);
					try {
						Object value = entry
								.get(FeedProvider.NORMALIZED_VALUE_KEY);
						RenderingInfo ri = provider.getRenderingInfo(entry);
						if (provider.getFeedType() != FeedType.STRING) {
							value = executeDecimalFormatter(provider,
									value.toString(), data);
						}
						DisplayedValue displayedValue = new DisplayedValue();
						displayedValue.setStatusText(ri.getStatusText());
						displayedValue.setValueColor(ri.getValueColor());
						displayedValue.setValue(ri.isValid() ? value
								.toString() : "");
						displayedValue.setNumberOfDecimals(DEFAULT_DECIMALS);
						displayedValue.setAlignment(DEFAULT_ALIGN);
						model.setValue(provider.getSubscriptionId(),displayedValue);
					} catch (ClassCastException ex) {
						logger.error("Feed data entry of unexpected type",ex);
					} catch (NumberFormatException ex) {
						logger.error("Feed data entry does not contain parsable value",ex);
					}
				}
			}
		} else {
			logger.debug("Data was null");
		}
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
			final Map<String, List<Map<String, String>>> data) {
		String rv = feedValue;
		// Apply decimal places formatting if appropriate
		FeedType feedType = provider.getFeedType();
		int decimalPlaces = DEFAULT_DECIMALS;
		if (feedType == FeedType.FLOATING_POINT
				|| feedType == FeedType.INTEGER) {
			if (decimalPlaces == -1) {
				decimalPlaces = (feedType == FeedType.FLOATING_POINT) ? DEFAULT_DECIMALS: 0;
			}
			try {
				rv = formats[decimalPlaces]
						.format(FeedType.FLOATING_POINT
								.convert(feedValue));
			} catch (IllegalFormatException ife) {
				logger.error("unable to format", ife);
			} catch (NumberFormatException nfe) {
				logger.error("unable to convert value to expected feed value",nfe);
			}
		}
		return rv;
	}

	@Override
	public void synchronizeTime(Map<String, List<Map<String, String>>> data,
			long syncTime) {
		updateFromFeed(data);
	}

	private void updateFeedProviders() {
		ArrayList<FeedProvider> feedProviders = new ArrayList<FeedProvider>();
		timeConversionMap.clear();
		for (int rowIndex = 0; rowIndex < model.getRowCount(); ++rowIndex) {
			AbstractComponent component = model.getComponentOfRow(rowIndex);
			if(component!=null) {
				FeedProvider fp = getFeedProvider(component);
				if (fp != null) {
					feedProviders.add(fp);
					TimeConversion tc = component.getCapability(TimeConversion.class);
					if (tc != null) {
						timeConversionMap.put(fp.getSubscriptionId(), tc);
					}							
				}
			}
		}
		feedProviders.trimToSize();
		feedProvidersRef.set(feedProviders);
	}
	
	@Override
	public Collection<FeedProvider> getVisibleFeedProviders() {
		return feedProvidersRef.get();
	}

	@Override
	protected JComponent initializeControlManifestation() {
		return tableSettingsControlPanel;
	}
	
	@Override
	public void updateMonitoredGUI() {
		// Update column visibility states
		Set<String> colIdsToBeRemoved = getColumnIdsToBeRemoved();
		if (!colIdsToBeRemoved.isEmpty()) {
			for (String id : colIdsToBeRemoved) {
				tableSettingsControlPanel.removeTableColumn(ColumnType.valueOf(id));
			}
		}

		Set<String> colIdsToBeAdded = getColumnIdsToBeAdded();
		if (!colIdsToBeAdded.isEmpty()) {
			for (String id : colIdsToBeAdded) {
				tableSettingsControlPanel.addTableColumn(ColumnType.valueOf(id));
			}
		}

		Set<Object> hiddenColIds = getViewProperties().getProperty(HIDDEN_COLUMNS_PROP);		
		List<String> hiddenColIdList = new ArrayList<String>();
		for (Object id : hiddenColIds)
			hiddenColIdList.add((String) id);
		tableSettingsControlPanel.updateColumnVisibilityStates(hiddenColIdList);
	}
	
	/**
	 * Returns the column ids to be removed from the current column model. 
	 */
	private Set<String> getColumnIdsToBeRemoved() {
		Set<String> colIdsToBeRemoved = new HashSet<String>();
		Set<Object> hiddenColIds = getViewProperties().getProperty(HIDDEN_COLUMNS_PROP);		
		TableColumnModel columnModel = table.getColumnModel();
		Enumeration<TableColumn> columns = columnModel.getColumns();
		
		// Get the column ids to hide
		while (columns.hasMoreElements()) {
			TableColumn c = columns.nextElement();
			for (Object hiddenColId : hiddenColIds) {
				if (hiddenColId.equals(c.getIdentifier())) {
					colIdsToBeRemoved.add((String) hiddenColId);
				}
			}
		}
		return colIdsToBeRemoved;
	}
	
	/**
	 * Returns the column ids to be added to the current column model. 
	 */
	private Set<String> getColumnIdsToBeAdded() {
		Set<String> colIdsToBeAdded = new HashSet<String>();
		
		// Get the set of column ids that are visible 
		Set<Object> hiddenColIds = getViewProperties().getProperty(HIDDEN_COLUMNS_PROP);
		for (ColumnType type : ColumnType.values()) {
			boolean found = false;
			for (Object id : hiddenColIds) {
				if (id.equals(type.name()))
					found = true;
			}
			if (!found)
				colIdsToBeAdded.add(type.name());
		}
		
		// Remove the column ids that are already visible
		TableColumnModel columnModel = table.getColumnModel();
		Enumeration<TableColumn> columns = columnModel.getColumns();
		while (columns.hasMoreElements()) {
			TableColumn c = columns.nextElement();
			if (colIdsToBeAdded.contains(c.getIdentifier()))
				colIdsToBeAdded.remove(c.getIdentifier());
		}
		return colIdsToBeAdded;		
	}
	
	
}
