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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * A pane that can be split into two parts or not. This extends <code>JSplitPane</code>
 * by adding API calls to show and hide the split. Also, the state of the divider is
 * preserved when showing or hiding the split.
 * 
 * @author mrose
 *
 */
@SuppressWarnings("serial")
public class SplittablePane extends JPanel {
	
	/** Constant indicating the splittable pane should be split horizontally in two,
	 * left and right. */
	public static final int HORIZONTAL_SPLIT = JSplitPane.HORIZONTAL_SPLIT;
	
	/** Constant indicating the splittable pane should be split vertically in two,
	 * top and bottom. */
	public static final int VERTICAL_SPLIT = JSplitPane.VERTICAL_SPLIT;

	/** The divider is, by default, 40% of the way across or down. */
	public static final double DEFAULT_DIVIDER_LOCATION = 0.4;
	
	/** If true, we're currently shown in split state. */
	private boolean isSplit;
	
	/** The primary component, always shown. */
	private Component mainComponent;
	
	/** The split pane component, if we're shown divided. */
	private JSplitPane splitPane;
	
	/** The split divider position. */
	private int dividerLocation;
	
    /**
     * Creates a new splittable pane with the default orientation and no components.
     */
    public SplittablePane() {
        this(HORIZONTAL_SPLIT, new JPanel());
    }

	/**
	 * Creates a new splittable pane with the given orientation and main component (top or left).
	 * 
	 * @param orientation the orientation of the split divider, when shown
	 * @param mainComponent the top or left component, or the only component, when not split
	 */
	public SplittablePane(int orientation, Component mainComponent) {
		this(orientation, mainComponent, (Component) null);
	}

	/**
	 * Creates a new splittable pane with the given orientation, main component (top or left),
	 * and secondary component (right or bottom).
	 * 
	 * @param orientation the orientation of the split divider, when shown
	 * @param mainComponent the top or left component, or the only component, when not split
	 * @param secondaryComponent the right or bottom component, when split
	 */
	public SplittablePane(int orientation, Component mainComponent, Component secondaryComponent) {
		setLayout(new BorderLayout());
		
		splitPane = new JSplitPane(orientation);
		splitPane.setBorder(null);
		splitPane.setResizeWeight(0.5); // Divide extra or removed space evenly when resizing.
		
		this.mainComponent = mainComponent;
		this.isSplit = false;
		if (this.mainComponent != null) {
		    add(this.mainComponent, BorderLayout.CENTER);
		}
		
		setSecondaryComponent(secondaryComponent);
	}
	
	/**
	 * Sets the main component. This is either the left or top component,
	 * depending on the orientation.
	 * 
	 * @param component the left or top component
	 */
	public void setMainComponent(Component component) {
	    if (!isSplit()) {
	        if (mainComponent != null) {
	            remove(mainComponent);
	        }
	        if (component != null) {
                add(component, BorderLayout.CENTER);
	        }
	    } else {
            if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                splitPane.setLeftComponent(component);
            } else {
                splitPane.setTopComponent(component);               
            }
	    }
	    mainComponent = component;
	}

	/**
	 * Sets the secondary component. This is either the right or bottom component,
	 * depending on the orientation.
	 * 
	 * @param secondaryComponent the right or bottom component
	 */
    public void setSecondaryComponent(Component secondaryComponent) {
        if (splitPane.getOrientation() == HORIZONTAL_SPLIT) {
			splitPane.setRightComponent(secondaryComponent);
		} else {
			splitPane.setBottomComponent(secondaryComponent);
		}
    }

    /**
	 * Hides the split divider, showing only the main component. If not split, do nothing.
	 */
	public void hideSplit() {
		if (isSplit) {
			dividerLocation = splitPane.getDividerLocation();
			remove(splitPane);
			splitPane.remove(mainComponent);
			add(mainComponent, BorderLayout.CENTER);
			isSplit = false;
			validate();
		}
	}

	/**
	 * Shows the split divider, main component and secondary component. If already split,
	 * do nothing.
	 */
	public void showSplit() {
		if (!isSplit) {
		    if (mainComponent != null) {
		        remove(mainComponent);
    			if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
    				splitPane.setLeftComponent(mainComponent);
    			} else {
    				splitPane.setTopComponent(mainComponent);				
    			}
            }
			add(splitPane, BorderLayout.CENTER);
			isSplit = true;
			validate();
            adjustDivider();
		}
	}

	/**
	 * Restores a previous divider location, if we remembered one, else set
	 * divider location to the default position. We have a valid divider
	 * location whenever <code>dividerLocation</code> is greater than zero.
	 */
    protected void adjustDivider() {
        if (dividerLocation > 0) {
            splitPane.setDividerLocation(dividerLocation);
        } else {
            splitPane.setDividerLocation(DEFAULT_DIVIDER_LOCATION);
        }
    }
    
    /**
     * Remembers the current divider location, if we are split.
     */
    public void saveDividerLocation() {
        if (isSplit()) {
            dividerLocation = splitPane.getDividerLocation();
        }
    }
	
	/**
	 * Checks whether the splittable pane is shown with the split divider.
	 * 
	 * @return true, if the pane is shown with the split divider
	 */
	public boolean isSplit() {
		return isSplit;
	}

    /**
     * Gets the main component, left or top, depending on the split orientation.
     * 
     * @return the main component
     */
    public Component getMainComponent() {
        return mainComponent;
    }

    /**
     * Gets the secondary component, right or bottom, depending on the split orientation.
     * 
     * @return the main component
     */
    public Component getSecondaryComponent() {
        if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            return splitPane.getRightComponent();
        } else {
            return splitPane.getBottomComponent();               
        }
    }
    
    /**
     * Sets the fractional position of the divider. 0 means all the way to the left or top, hiding
     * the main component, 1 means all the way to the right or bottom. Halfway is 0.5, for example.
     * 
     * Because of limitations in <code>JSplitPane</code>, this method does not correctly set
     * the divider location unless the pane is split and visible on the screen.
     * 
     * @param fraction the fractional position of the divider
     */
    public void setDividerFraction(double fraction) {
        splitPane.setDividerLocation(fraction);
    }
    
    /**
     * Gets the current fractional position of the divider.
     * 
     * @return the divider position, as a fractional value from 0 to 1
     */
    public double getDividerFraction() {
        return (double) splitPane.getDividerLocation() / (double) splitPane.getWidth();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        splitPane.setSize(width, height);
        doLayout();
    }

}
