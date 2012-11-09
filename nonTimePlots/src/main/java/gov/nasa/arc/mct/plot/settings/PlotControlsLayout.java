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
package gov.nasa.arc.mct.plot.settings;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * This layout manager allows a lower panel to remain visible and on top of a middle panel
 * when the parent cannot accommodate the heights of both. It also allows the lower panel to
 * remain adjacent to the middle panel when the parent has more height than its children require.
 */

public class PlotControlsLayout implements LayoutManager2 {

	/**
	 * This scroll pane class provides a convenient way to access the resizeable components
	 * that reside in this scroll pane, eliminating a search.
	 */
	@SuppressWarnings("serial")
	public static class ResizersScrollPane extends JScrollPane {

		private JComponent[] resizers;

		public ResizersScrollPane(JComponent component, JComponent... resizerWidget) {
			super(component);
			int numResizers = resizerWidget.length;
			resizers = new JComponent[numResizers];
			for (int k = 0; k < numResizers; k++) {
				resizers[k] = resizerWidget[k];
			}
		}

		public JComponent[] getResizers() {
			return resizers;
		}
	}

	private static final int DEFAULT_PADDING = 0;
	public static final String MIDDLE = "Middle";
	public static final String LOWER = "Lower";

	private int minWidth = 0;
	private int minHeight = 0;
	private boolean sizeUnknown = true;
	private int preferredWidth;
	private int preferredHeight;
	/*
	 * The padding between the middle panel and the lower panel
	 */
	private int innerPadding = 0;
	private Component middleComponent;
	private Component lowerComponent;


	public PlotControlsLayout() {
		this(DEFAULT_PADDING);
	}

	public PlotControlsLayout(int padding) {
		innerPadding = padding;
	}

	/*
	 * LayoutManager2
	 * NOTE: there is another method with same name in LayoutManager
	 */
	@Override
	public void addLayoutComponent(final Component comp, Object constraints) {
		if (constraints instanceof String) {
			String name = (String) constraints;
			if (MIDDLE.equals(name)) {
				middleComponent = comp;
				if (comp instanceof ResizersScrollPane) {
					final JComponent parent = (JComponent) comp.getParent();
					JComponent[] children = ((ResizersScrollPane) comp).getResizers();
					for (JComponent child : children) {
						if (child.getComponentListeners().length == 0) {
							child.addComponentListener(new ComponentListener() {
								@Override
								public void componentResized(ComponentEvent e) {
									parent.revalidate();
								}
	
								@Override
								public void componentShown(ComponentEvent e) {
								}
								@Override
								public void componentMoved(ComponentEvent e) {
								}
								@Override
								public void componentHidden(ComponentEvent e) {
								}
							});
						}
					}
				}
			} else
				if (LOWER.equals(name)) {
					lowerComponent = comp;
				} else {
					throw new IllegalArgumentException("Cannot use unknown constraint in layout: " + name);
				}
		}
	}
    
	/* LayoutManager2 */
	@Override
    public float getLayoutAlignmentX(Container target) {
	    return 0.5f;
    }

	/* LayoutManager2 */
	@Override
    public float getLayoutAlignmentY(Container target) {
	    return 0.5f;
    }

	/* LayoutManager2 */
	@Override
    public void invalidateLayout(Container target) {
    }

	/* LayoutManager2 */
	@Override
    public Dimension maximumLayoutSize(Container target) {
		if (sizeUnknown) {
			setSizes(target);
		}
	    return new Dimension(preferredWidth, preferredHeight);
    }

	/*
	 * LayoutManager
	 * NOTE: another method with same name in LayoutManager2
	 */
	@Override
    public void addLayoutComponent(String name, Component comp) {
    }

	/* LayoutManager */
    /*
     * This is called when the panel is first displayed, and every time its size changes.
     * Note: You can't assume preferredLayoutSize or minimumLayoutSize will be called.
     */
	@Override
    public void layoutContainer(Container parent) {
        if (sizeUnknown) {
        	setSizes(parent);
        }

        if (middleComponent != null && lowerComponent != null ) {
        	doLayout(parent, middleComponent, lowerComponent);
        } else
        	if (middleComponent != null) {
        		middleComponent.setBounds(0, 0, parent.getSize().width, middleComponent.getPreferredSize().height);
        	} else
        		if (lowerComponent != null) {
        			lowerComponent.setBounds(0, 0, parent.getSize().width, lowerComponent.getPreferredSize().height);
        		}
    }

	private void doLayout(Container parent, Component middleComp, Component lowerComp) {
	    Dimension parentDim = parent.getSize();
	    Dimension middleDim = middleComp.getPreferredSize();
	    Dimension lowerDim = lowerComp.getPreferredSize();

	    // In the following, the middle component is a scroll pane and the lower component is a panel.
    	// a) The widths of both the scroll pane and the panel are expanded to the width of the parent
	    // b) Maintain a buffer (INNER_PADDING) between the scroll pane and the panel
	    // c) The panel is positioned at the bottom of the parent, and is allowed its preferred height
	    // d) The scroll pane shrinks to fit the remaining space after the panel and buffer are handled

	    // Does the parent have enough space for both components ?
	    if (middleDim.height + lowerDim.height + innerPadding < parentDim.height) {
		    // If so, position the scroll pane at the top of the parent
		    middleComp.setBounds(0, 0, parentDim.width, middleDim.height);
	    	lowerComp.setBounds(0, middleDim.height + innerPadding - 1, parentDim.width, lowerDim.height);
	    } else {
	    	// If not, shrink the height of the scroll pane, and ensure the panel is completely visible
	    	// as long as possible while parent shrinks.
	    	int overlapY = middleDim.height + lowerDim.height + innerPadding - parentDim.height;
		    middleComp.setBounds(0, 0, parentDim.width, middleDim.height - overlapY);
	    	lowerComp.setBounds(0, middleDim.height + innerPadding - overlapY - 1, parentDim.width, lowerDim.height);
	    	parent.setComponentZOrder(lowerComp, 0);
	    }
    }

	/* LayoutManager */
	@Override
    public Dimension minimumLayoutSize(Container parent) {
		if (sizeUnknown) {
			setSizes(parent);
		}
        Dimension dim = new Dimension(0, 0);

        // Always add the container's insets
        Insets insets = parent.getInsets();
        dim.width = minWidth + insets.left + insets.right;
        dim.height = minHeight + insets.top + insets.bottom;
        return dim;
    }

	/* LayoutManager */
	@Override
    public Dimension preferredLayoutSize(Container parent) {
		if (sizeUnknown) {
			setSizes(parent);
		}
        Dimension dim = new Dimension(0, 0);

        // Always add the container's insets
        Insets insets = parent.getInsets();
        dim.width = preferredWidth + insets.left + insets.right;
        dim.height = preferredHeight + insets.top + insets.bottom;
        return dim;
    }

	private void setSizes(Container parent) {
	    int nComps = parent.getComponentCount();

        // Reset preferred/minimum width and height.
        preferredWidth = 0;
        preferredHeight = 0;
        minWidth = 0;
        minHeight = 0;

        for (int i = 0; i < nComps; i++) {
            Component comp = parent.getComponent(i);
            if (comp.isVisible()) {
                Dimension prefSize = comp.getPreferredSize();

                preferredWidth = (prefSize.width > preferredWidth) ? prefSize.width : preferredWidth;
                preferredHeight += prefSize.height;

                minWidth = Math.max(comp.getMinimumSize().width, minWidth);
                minHeight += comp.getMinimumSize().height;
            }
        }
        sizeUnknown = false;
    }

	/* LayoutManager */
	@Override
    public void removeLayoutComponent(Component comp) {
		if (comp == middleComponent) {
			middleComponent = null;
		} else
			if (comp == lowerComponent) {
				lowerComponent = null;
			}
    }

	void resetSizeFlag() {
		sizeUnknown = true;
	}

}
