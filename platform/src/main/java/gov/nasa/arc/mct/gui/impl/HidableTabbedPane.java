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
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.gui.util.UniqueNameGenerator;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

/**
 * A tabbed pane that hides the tabs when there is only one tab. Since <code>JTabbedPane</code>
 * always shows the tabs, we implement a <code>JPanel</code> instead, with two possible
 * Components inside: either the component that would be shown in the first tab, or an
 * actual <code>JTabbedPane</code> with all components and tab titles.
 * 
 * We implement a portion of the <code>JTabbedPane</code> protocol, intended to be the
 * most useful methods.
 */
@SuppressWarnings("serial")
public class HidableTabbedPane extends JPanel {
	
	/** The component in the first tab. We have to keep track of it here,
	 * because we can't insert it simultaneously into the first tab in
	 * the <code>JTabbedPane</code> and into the top-level panel. We'll
	 * remove it from one and place into the other as needed. */
	private Component firstComponent;
	
	/** The tabbed pane holding all tabs. */
	private JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

	/** A dummy component used as the first tab component when there is only one tab,
	 * since Java doesn't like the same object as a child of two different containers.
	 */
	private JPanel placeholder = new JPanel();
	
	/**
	 * Create a new hideable tabbed pane with no tabs.
	 */
	public HidableTabbedPane() {
	    this.firstComponent = null;
        setLayout(new BorderLayout());
        instrumentNames();
	}

    private void instrumentNames() {
        setName(UniqueNameGenerator.get("hidableTabbedPane"));
        tabs.setName("tabbedPaneWithin");
    }
	
	/**
	 * Create a new hideable tabbed pane with the given title and component for the
	 * first tab (which will be hidden until we add more tabs).
	 * 
	 * @param firstTabTitle the textual title of the first tab
	 * @param firstComponent the component to show in the first tab
	 */
	public HidableTabbedPane(String firstTabTitle, Component firstComponent) {
	    this();
	    
		this.firstComponent = firstComponent;
		addTab(firstTabTitle, placeholder);
		
		add(firstComponent, BorderLayout.CENTER);
	}
	
	/**
	 * Get the number of tabs in the pane.
	 * 
	 * @return the number of tabs
	 */
	public int getTabCount() {
		return tabs.getTabCount();
	}
	
	/**
	 * Get the index of the selected tab, 0-relative.
	 * 
	 * @return the selected tab index
	 */
	public int getSelectedIndex() {
		if (tabs.getTabCount() <= 1) {
			return 0;
		} else {
			return tabs.getSelectedIndex();
		}
	}
	
	/**
	 * Set the selected tab index. The index must be a valid tab index or -1, which
	 * indicates that no tab should be selected (can also be used when there are no
	 * tabs in the tabbedpane).
	 * 
	 * @param index the new selected tab index
	 */
	public void setSelectedIndex(int index) {
        tabs.setSelectedIndex(index);
	}
	
	/**
	 * Get the component shown in the context area of the given tab.
	 * 
	 * @param index the index of the tab, 0-relative
	 * @return the component in the content area of the tab
	 */
	public Component getComponentAt(int index) {
		if (index == 0) {
			return firstComponent;
		} else {
			return tabs.getComponentAt(index);
		}
	}
	
	/**
	 * Get the component in the tab label at the given index.
	 * 
	 * @param index the tab whose tab component we want, 0-relative
	 * @return the component for the tab label
	 */
	public Component getTabComponentAt(int index) {
        return tabs.getTabComponentAt(index);
	}
	
	/**
	 * Change the component in the content area for a tab.
	 * 
	 * @param index the index of the tab to change
	 * @param component the new component for the content area of the tab
	 */
	public void setComponentAt(int index, Component component) {
		if (isTabsShown()) {
			tabs.setComponentAt(index, component);
		}
		if (index == 0) {
			if (!isTabsShown()) {
			    if (firstComponent != null) {
			        remove(firstComponent);
			    }
			    if (component != null) {
			        add(component, BorderLayout.CENTER);
			    }
			}
			
			firstComponent = component;
		}
		
		validate();
	}
	
	/**
	 * Set the tab label component for a tab.
	 * 
	 * @param index the index of the tab
	 * @param component the component for the tab label
	 */
	public void setTabComponentAt(int index, Component component) {
		tabs.setTabComponentAt(index, component);
	}

	/**
	 * Check whether the tabs are shown. We show the tabs whenever the tab count
	 * is greater than one.
	 * 
	 * @return true, if the tabs are shown
	 */
	public boolean isTabsShown() {
		return (tabs.getTabCount() > 1);
	}
	
	/**
	 * Add a new tab with a textual title and content area component. Show the tabs,
	 * if we then have more than one tab.
	 * 
	 * @param title the title for the new tab
	 * @param component the component for the content area
	 */
	public void addTab(String title, Component component) {
		tabs.add(title, component);
		if (tabs.getTabCount() == 2) {
			showTabs();
		}
	}
	
	/**
	 * Remove a tab. Hide the tabs, if we then have only one tab.
	 * 
	 * @param index the index of the tab to remove
	 */
	public void removeTabAt(int index) {
		tabs.removeTabAt(index);
		if (tabs.getTabCount() >= 1) {
			firstComponent = tabs.getComponentAt(0);
		}
		if (tabs.getTabCount() == 1) {
			hideTabs();
		}
	}
	
	/**
	 * Show the tabs. Move the first component into the first tab.
	 */
	protected void showTabs() {
	    if (firstComponent != null) {
	        remove(firstComponent);
	    }
		tabs.setComponentAt(0, firstComponent);
		add(tabs, BorderLayout.CENTER);
		validate();
	}
	
	/**
	 * Hide the tabs. Move the first component from the first tab
	 * to be the entire content area.
	 */
	protected void hideTabs() {
		remove(tabs);
		tabs.setComponentAt(0, placeholder);
		add(firstComponent, BorderLayout.CENTER);
		validate();
	}

	/**
	 * Add a listener to notify when the currently selected tab changes.
	 * 
	 * @param listener the listener
	 */
    public void addChangeListener(ChangeListener listener) {
        tabs.addChangeListener(listener);
    }

}
