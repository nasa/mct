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
package gov.nasa.arc.mct.canvas.view;

import gov.nasa.arc.mct.canvas.MenuManagerAccess;
import gov.nasa.arc.mct.canvas.panel.Panel;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.services.component.MenuManager;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class Augmentation extends JComponent {

    private static final int d = 8;
    private static final int h = d / 2;
    private static final float s = h / 2;
    private Set<Panel> highlightedPanels = new LinkedHashSet<Panel>();
    private static final Color ORIG_COLOR = UIManager.getColor("textHighlight").darker();
    private static final Color HIGHLIGHT_COLOR = new Color(ORIG_COLOR.getRed(), ORIG_COLOR
                    .getGreen(), ORIG_COLOR.getBlue(), 200);
    
    private CanvasManifestation canvasManifestation;
    private JPanel augmentedPanel;
    private boolean hasPanelChanged = false;

    private Component lastMouseEnteredWidget;
    /**
     * This variable represents the widget the mouse was pressed in. The matching mouse released and mouse dragged
     * must be dispatched to the same event. 
     */
    private Component mousePressedWidget;
    
    private Point oldLocation;

    private Point selectedPointIfNothingHappens;
    
    boolean spotlightMode = false;
    String spotlightText;

    public Augmentation(JPanel augmentedPanel, CanvasManifestation canvasManifestation) {
        this.augmentedPanel = augmentedPanel;
        this.canvasManifestation = canvasManifestation;
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (highlightedPanels.isEmpty()) {
                    redispatchEvent(e);
                    return;
                }
                
                if (Augmentation.this.canvasManifestation.isLocked()) {
                    redispatchEvent(e);
                    return;
                }

                selectedPointIfNothingHappens = null;
                Augmentation augmentation = (Augmentation) e.getSource();
                int cursorType = augmentation.getCursor().getType();
                Point newLocation = e.getPoint();
                switch (cursorType) {
                case Cursor.MOVE_CURSOR:
                    newLocation = marshalNewLocation(newLocation, e.getLocationOnScreen());

                    int diffX = newLocation.x - oldLocation.x,
                    diffY = newLocation.y - oldLocation.y;
                    for (Panel panel : highlightedPanels) {
                        Rectangle panelOldBounds = panel.getBounds();
                        panel.setBounds(panelOldBounds.x + diffX, panelOldBounds.y + diffY,
                                        panelOldBounds.width, panelOldBounds.height);
                    }
                    oldLocation = newLocation;
                    break;

                case Cursor.NE_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldNECornerX = panelOldBounds.x + panelOldBounds.width, oldNECornerY = panelOldBounds.y;
                        int diffWidth = newLocation.x - oldNECornerX, diffHeight = newLocation.y
                                        - oldNECornerY;
                        int newWidth = panelOldBounds.width + diffWidth;
                        int newHeight = panelOldBounds.height - diffHeight;
                        if (newWidth > 0 && newHeight > 0)
                            panel.setBounds(panelOldBounds.x, panelOldBounds.y + diffHeight,
                                            newWidth, newHeight);
                    }
                    break;

                case Cursor.SE_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldSECornerX = panelOldBounds.x + panelOldBounds.width, oldSECornerY = panelOldBounds.y
                                        + panelOldBounds.height;
                        int diffWidth = newLocation.x - oldSECornerX, diffHeight = newLocation.y
                                        - oldSECornerY;
                        int newWidth = panelOldBounds.width + diffWidth;
                        int newHeight = panelOldBounds.height + diffHeight;
                        if (newWidth > 0 && newHeight > 0)
                            panel
                                            .setBounds(panelOldBounds.x, panelOldBounds.y,
                                                            newWidth, newHeight);
                    }
                    break;

                case Cursor.SW_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldSWCornerX = panelOldBounds.x, oldSWCornerY = panelOldBounds.y
                                        + panelOldBounds.height;
                        int diffWidth = newLocation.x - oldSWCornerX, diffHeight = newLocation.y
                                        - oldSWCornerY;
                        int newWidth = panelOldBounds.width - diffWidth;
                        int newHeight = panelOldBounds.height + diffHeight;
                        if (newWidth > 0 && newHeight > 0)
                            panel.setBounds(panelOldBounds.x + diffWidth, panelOldBounds.y,
                                            newWidth, newHeight);
                    }
                    break;

                case Cursor.NW_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldSWCornerX = panelOldBounds.x, oldSWCornerY = panelOldBounds.y;
                        int diffWidth = newLocation.x - oldSWCornerX, diffHeight = newLocation.y
                                        - oldSWCornerY;
                        int newWidth = panelOldBounds.width - diffWidth;
                        int newHeight = panelOldBounds.height - diffHeight;
                        if (newWidth > 0 && newHeight > 0)
                            panel.setBounds(panelOldBounds.x + diffWidth, panelOldBounds.y
                                            + diffHeight, newWidth, newHeight);
                    }
                    break;
                case Cursor.E_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldEEdgeX = panelOldBounds.x + panelOldBounds.width;
                        int diffWidth = newLocation.x - oldEEdgeX;
                        int newWidth = panelOldBounds.width + diffWidth;
                        if (newWidth > 0)
                            panel.setBounds(panelOldBounds.x, panelOldBounds.y, newWidth,
                                            panelOldBounds.height);
                    }
                    break;

                case Cursor.S_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldSEdgeY = panelOldBounds.y + panelOldBounds.height;
                        int diffHeight = newLocation.y - oldSEdgeY;
                        int newHeight = panelOldBounds.height + diffHeight;
                        if (newHeight > 0)
                            panel.setBounds(panelOldBounds.x, panelOldBounds.y,
                                            panelOldBounds.width, newHeight);
                    }
                    break;

                case Cursor.W_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldWEdgeX = panelOldBounds.x;
                        int diffWidth = newLocation.x - oldWEdgeX;
                        int newWidth = panelOldBounds.width - diffWidth;
                        if (newWidth > 0)
                            panel.setBounds(panelOldBounds.x + diffWidth, panelOldBounds.y,
                                            newWidth, panelOldBounds.height);
                    }
                    break;

                case Cursor.N_RESIZE_CURSOR:
                    if (highlightedPanels.size() == 1) {
                        Panel panel = highlightedPanels.iterator().next();
                        Rectangle panelOldBounds = panel.getBounds();
                        int oldNEdgeY = panelOldBounds.y;
                        int diffHeight = newLocation.y - oldNEdgeY;
                        int newHeight = panelOldBounds.height - diffHeight;
                        if (newHeight > 0)
                            panel.setBounds(panelOldBounds.x, panelOldBounds.y + diffHeight,
                                            panelOldBounds.width, newHeight);
                    }
                    break;

                default:
                    redispatchEvent(e);
                    return;
                }
                redispatchEvent(e);
                hasPanelChanged = true;
            }

            private Point marshalNewLocation(Point mouseLocation, Point mouseLocationOnScreen) {
                if (highlightedPanels.isEmpty()) {
                    return mouseLocation;
                }

                int smallestX;
                int smallestY;
                Iterator<Panel> it = highlightedPanels.iterator();
                Panel panel = it.next();
                Point bound = panel.getLocationOnScreen();
                smallestX = bound.x;
                smallestY = bound.y;
                while (it.hasNext()) {
                    panel = it.next();
                    bound = panel.getLocationOnScreen();
                    if (bound.x < smallestX) {
                        smallestX = bound.x;
                    }
                    if (bound.y < smallestY) {
                        smallestY = bound.y;
                    }
                }
                Point smallestPoint = new Point(smallestX, smallestY);
                Point returnLocation = new Point(mouseLocation.x, mouseLocation.y);
                if (invalidHorizontalMovement(smallestPoint, mouseLocationOnScreen,
                                Augmentation.this.canvasManifestation,
                                mouseLocation.x < oldLocation.x)) {
                    returnLocation.x = oldLocation.x
                                    + Augmentation.this.canvasManifestation.getLocationOnScreen().x
                                    - smallestX;
                }
                if (invalidVerticalMovement(smallestPoint, mouseLocationOnScreen,
                                Augmentation.this.canvasManifestation,
                                mouseLocation.y < oldLocation.y)) {
                    returnLocation.y = oldLocation.y
                                    + Augmentation.this.canvasManifestation.getLocationOnScreen().y
                                    - smallestY;
                }
                return returnLocation;
            }

            private boolean invalidHorizontalMovement(Point checkPoint, Point mouseLocation,
                            Container parent, boolean leftMoving) {
                Point parentLoc = parent.getLocationOnScreen();
                if ((checkPoint.x <= parentLoc.x) && leftMoving) {
                    return true;
                }
                return false;
            }

            private boolean invalidVerticalMovement(Point checkPoint, Point mouseLocation,
                            Container parent, boolean upMoving) {
                Point parentLoc = parent.getLocationOnScreen();
                if ((checkPoint.y <= parentLoc.y) && upMoving) {
                    return true;
                }
                return false;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Augmentation augmentation = (Augmentation) e.getSource();
                if (highlightedPanels.isEmpty()) {
                    augmentation.setAugmentationCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } else {
                	for (Panel panel : highlightedPanels) {                
                	    int currentCursorType = augmentation.getCursor().getType();
                	    setCursorType(augmentation, panel, e.getPoint());
                	    int newCursorType = augmentation.getCursor().getType();
                	    oldLocation = e.getPoint();
                	    if (currentCursorType != newCursorType) {
                	        return;
                	    }
                	}
                }
                redispatchEvent(e);
            }
        });

        // if popup trigger is in title bar, then show popup otherwise redispatchEvent
        // 
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                redispatchEvent(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                redispatchEvent(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedPointIfNothingHappens = null;
                // point is not in title area, pass the underlying event to the contents of the panel
                Augmentation augmentation = (Augmentation) e.getSource();
                Point p = e.getLocationOnScreen();
                CanvasManifestation canvas = Augmentation.this.canvasManifestation;
                Panel panel = canvas.findImmediatePanel(p);
                
                // Detects if cursor is within the move or resize region. This region includes
                // d pixels (see field declaration) beyond a panel's width and height.
                if (panel == null && augmentation.getCursor().getType() != Cursor.DEFAULT_CURSOR) {
                    redispatchEvent(e);
                    return;
                }
                    
                if (panel != null && !panel.pointOnBorder(p)) {
                    redispatchEvent(e);
                    return;
                }
                
                if (isPopupTrigger(e)) {
                    showPopupMenu(e);
                    return;
                }
                if (e.getClickCount() == 1) {
                    oldLocation = e.getPoint();
                    if (isDiscontinuousMultiSelection(e)) {
                        canvas.addSingleSelection(p);
                    } else if(canvas.getSelectedPanels().contains(panel)) {
                        canvas.addSingleSelection(p);
                        selectedPointIfNothingHappens = p;
                    } else {
                        canvas.setSingleSelection(p);
                    }
                    if(panel != null) {
                        panel.setTitleBounds();
                        setCursorType(augmentation, panel, e.getPoint());
                    }
                    augmentation.repaint();
                } else if (isDoubleClick(e)) {
                    panel = augmentation.canvasManifestation.setSingleSelection(p);
                    if (panel != null) {
                        panel.getWrappedManifestation().getManifestedComponent().open();
                    }
                }
                redispatchEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isPopupTrigger(e)) {
                    showPopupMenu(e);
                    return;
                }
                for (Panel panel : highlightedPanels) {
                    Rectangle r = panel.getBounds();
                    r = panel.marshalBound(r);
                    panel.setBounds(r);
                }
                if(selectedPointIfNothingHappens != null) {
                    Augmentation.this.canvasManifestation.setSingleSelection(selectedPointIfNothingHappens);
                    hasPanelChanged = true;
                }
                if (hasPanelChanged) {
                    Augmentation.this.canvasManifestation.fireFocusPersist();
                    Augmentation.this.repaint();
                    Augmentation.this.canvasManifestation.computePreferredSize();
                    hasPanelChanged = false;
                    Augmentation.this.canvasManifestation.updateController(highlightedPanels);
                }
                redispatchEvent(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                redispatchEvent(e);
            }

            private boolean isDiscontinuousMultiSelection(MouseEvent e) {
                return e.isControlDown() || e.isMetaDown();
            }

            private boolean isPopupTrigger(MouseEvent e) {
                return e.isPopupTrigger();
            }

            private boolean isDoubleClick(MouseEvent e) {
                return (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e));
            }

            private void showPopupMenu(MouseEvent e) {
                Augmentation augmentation = (Augmentation) e.getSource();
                Point p = e.getLocationOnScreen();
                Panel panel = augmentation.canvasManifestation.findImmediatePanel(p);
                
                if (panel != null && !panel.pointInTitle(p)) {
                    redispatchEvent(e);
                    return;
                }
                
                if (!augmentation.canvasManifestation.getSelectedPanels().contains(panel)) {
                    panel = augmentation.canvasManifestation.setSingleSelection(p);
                }
                
                // Don't show the popup if the canvas is in the inspector.
                // This is because the actions don't use the right canvas.
                // This causes bug MCT-2250.
                // This should be re-enabled once the actions use the correct canvas.
                View m = augmentation.canvasManifestation;
                while (m != null && m.getInfo() != null) {
                    if (m.getInfo().getViewType() == ViewType.INSPECTOR || m.getInfo().getViewType() == ViewType.CENTER_OWNED_INSPECTOR) {
                        return;
                    }
                    m = (View) SwingUtilities.getAncestorOfClass(View.class, m);
                }

                if (panel == null) {
                    MenuManager menuManager = MenuManagerAccess.getMenuManager();
                    JPopupMenu popupMenu = menuManager.getViewPopupMenu(augmentation.canvasManifestation);
                    if (popupMenu != null)
                        popupMenu.show(augmentation.canvasManifestation, e.getX(), e.getY());                    
                } else {
                    Point panelPoint = panel.getLocationOnScreen();
                    JPopupMenu popupMenu = panel.getWrappedManifestation()
                                    .getManifestationPopupMenu();
                    if (popupMenu != null)
                        popupMenu.show(panel, p.x - panelPoint.x, p.y - panelPoint.y);
                }
                augmentation.repaint();
            }
        });

        MarqueSelectionListener marqueSelectionListener = new MarqueSelectionListener(
                        augmentedPanel, new MarqueSelectionListener.MultipleSelectionProvider() {

                            @Override
                            public void selectPanels(Collection<Panel> selection) {
                                Augmentation.this.canvasManifestation.setSelection(selection);
                                Augmentation.this.repaint();
                            }

                            @Override
                            public boolean pointInTopLevelPanel(Point p) {
                                return !Augmentation.this.canvasManifestation
                                                .isPointinaPanel(p);
                            }
                        });

        addMouseListener(marqueSelectionListener);
        addMouseMotionListener(marqueSelectionListener);        
    }

    private void setCursorType(Augmentation augmentation, Panel panel, Point cursorPoint) {
        Point panelLocaion = panel.getLocation();
        /*
         * (x0, y0) (x1, y0) (x2, y0) 
         * (x0, y1)          (x2, y1) 
         * (x0, y2) (x1, y2) (x2, y2)
         */
        int x0 = panelLocaion.x, x1 = panelLocaion.x + panel.getWidth() / 2, x2 = panelLocaion.x
                        + panel.getWidth();
        int y0 = panelLocaion.y, y1 = panelLocaion.y + panel.getHeight() / 2, y2 = panelLocaion.y
                        + panel.getHeight();

        // Check if cursorPoint falls on the highlight area.
        int x0_left = x0 - h;
        int x0_right = x0 + h;
        int x2_left = x2 - h;
        int x2_right = x2 + h;
        int y0_top = y0 - h;
        int y0_bottom = y0 + h;
        int y2_top = y2 - h;
        int y2_bottom = y2 + h;
        if (cursorPoint.x >= x0_left
                        && cursorPoint.x <= x2_right
                        && cursorPoint.y >= y0_top
                        && cursorPoint.y <= y2_bottom
                        && (!(cursorPoint.x >= x0_right && cursorPoint.x <= x2_left
                                        && cursorPoint.y >= y0_bottom && cursorPoint.y <= y2_top))) {
            if (cursorPoint.x >= x0_left && cursorPoint.x <= x0_right
                            && cursorPoint.y >= y0_top && cursorPoint.y <= y0_bottom) {
                augmentation.setAugmentationCursor(Cursor
                                .getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
            } else {
                int y1_top = y1 - h;
                int y1_bottom = y1 + h;
                if (cursorPoint.x >= x0_left && cursorPoint.x <= x0_right
                                && cursorPoint.y >= y1_top && cursorPoint.y <= y1_bottom) {
                    augmentation.setAugmentationCursor(Cursor
                                    .getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                } else if (cursorPoint.x >= x0_left && cursorPoint.x <= x0_right
                                && cursorPoint.y >= y2_top && cursorPoint.y <= y2_bottom) {
                    augmentation.setAugmentationCursor(Cursor
                                    .getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                } else {
                    int x1_left = x1 - h;
                    int x1_right = x1 + h;
                    if (cursorPoint.x >= x1_left && cursorPoint.x <= x1_right
                                    && cursorPoint.y >= y0_top
                                    && cursorPoint.y <= y0_bottom) {
                        augmentation.setAugmentationCursor(Cursor
                                        .getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    } else if (cursorPoint.x >= x1_left && cursorPoint.x <= x1_right
                                    && cursorPoint.y >= y2_top
                                    && cursorPoint.y <= y2_bottom) {
                        augmentation.setAugmentationCursor(Cursor
                                        .getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                    } else if (cursorPoint.x >= x2_left && cursorPoint.x <= x2_right
                                    && cursorPoint.y >= y0_top
                                    && cursorPoint.y <= y0_bottom) {
                        augmentation.setAugmentationCursor(Cursor
                                        .getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    } else if (cursorPoint.x >= x2_left && cursorPoint.x <= x2_right
                                    && cursorPoint.y >= y1_top
                                    && cursorPoint.y <= y1_bottom) {
                        augmentation.setAugmentationCursor(Cursor
                                        .getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    } else if (cursorPoint.x >= x2_left && cursorPoint.x <= x2_right
                                    && cursorPoint.y >= y2_top
                                    && cursorPoint.y <= y2_bottom) {
                        augmentation.setAugmentationCursor(Cursor
                                        .getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    } else
                        augmentation.setAugmentationCursor(Cursor
                                        .getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
        } else
            augmentation.setAugmentationCursor(Cursor
                            .getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        // Check if cursor falls into the panel title area
        if (panel.getTitleBounds().contains(cursorPoint)) {
            if(panel.getIconBounds().contains(cursorPoint)) {
                augmentation.setAugmentationCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else {
                augmentation.setAugmentationCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }
            
    }

    private void redispatchEvent(MouseEvent e) {
        Component destination; 
        if (e.getID() == MouseEvent.MOUSE_DRAGGED || e.getID() == MouseEvent.MOUSE_RELEASED) {
            destination = mousePressedWidget; 
        } else  {
            destination = getDeepestComponentWithMouseListeners(e);
        }
        if (destination != null) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                mousePressedWidget = destination;
            }
            MouseEvent newEvent;
            MouseEvent parentNewEvent;
            
            Point newPoint = SwingUtilities.convertPoint(Augmentation.this.augmentedPanel, e.getPoint(), destination);
            
            if (destination != lastMouseEnteredWidget) {
                newEvent = new MouseEvent(destination, MouseEvent.MOUSE_ENTERED, e.getWhen(), e
                                .getModifiers(), newPoint.x, newPoint.y, e.getXOnScreen(), e
                                .getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e
                                .getButton());
                
                if (destination.getParent() != null) {
                
                    newPoint = SwingUtilities.convertPoint(Augmentation.this.augmentedPanel, e.getPoint(), destination.getParent());
                    
                    parentNewEvent = new MouseEvent(destination.getParent(), MouseEvent.MOUSE_ENTERED, e.getWhen(), e
                                .getModifiers(), newPoint.x, newPoint.y, e.getXOnScreen(), e
                                .getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e
                                .getButton());
                    destination.getParent().dispatchEvent(parentNewEvent);
                    
                }
                
                destination.dispatchEvent(newEvent);
                
              
                if (lastMouseEnteredWidget != null) {
                    newPoint = SwingUtilities.convertPoint(Augmentation.this.augmentedPanel, e
                                    .getPoint(), lastMouseEnteredWidget);
                    newEvent = new MouseEvent(lastMouseEnteredWidget, MouseEvent.MOUSE_EXITED, e
                                    .getWhen(), e.getModifiers(), newPoint.x, newPoint.y, e
                                    .getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e
                                    .isPopupTrigger(), e.getButton());
                    
                    if (lastMouseEnteredWidget.getParent() != null) {
                        parentNewEvent = new MouseEvent(lastMouseEnteredWidget.getParent(), MouseEvent.MOUSE_EXITED, e.getWhen(), e
                                    .getModifiers(), newPoint.x, newPoint.y, e.getXOnScreen(), e
                                    .getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e
                                    .getButton());
                        lastMouseEnteredWidget.getParent().dispatchEvent(parentNewEvent);

                    }
                    
                    
                    lastMouseEnteredWidget.dispatchEvent(newEvent);
                   
                }
                lastMouseEnteredWidget = destination;
            }
                      
                newEvent = new MouseEvent(destination, e.getID(), e.getWhen(), e.getModifiersEx(),
                            newPoint.x, newPoint.y, e.getXOnScreen(), e.getYOnScreen(), e
                                            .getClickCount(), e.isPopupTrigger(), e.getButton());
            
            if (destination.getParent() != null) {
                newPoint = SwingUtilities.convertPoint(Augmentation.this.augmentedPanel, e.getPoint(), destination.getParent());
                
                parentNewEvent = new MouseEvent(destination.getParent(), e.getID(), e.getWhen(), e
                            .getModifiersEx(), newPoint.x, newPoint.y, e.getXOnScreen(), e
                            .getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e
                            .getButton());
                destination.getParent().dispatchEvent(parentNewEvent);
                
            }
            
            destination.dispatchEvent(newEvent);
            
 
            
        }
    }

    private Component getDeepestComponentWithMouseListeners(MouseEvent e) {
        Component widget = SwingUtilities.getDeepestComponentAt(Augmentation.this.augmentedPanel, e
                        .getX(), e.getY());
        while (widget != null && widget != Augmentation.this.augmentedPanel
                        && widget.getMouseListeners().length == 0
                        && widget.getMouseMotionListeners().length == 0)
            widget = widget.getParent();
                
        return widget;
    }

    public void addHighlights(Collection<Panel> panels) {
        for (Panel panel : panels) {
            highlightedPanels.add(panel);
        }
    }

    private Shape createSpotlight(Component c, Rectangle rect, String labelText, FontMetrics fontMetrics, int xOffset) {
        Point convertedPoint = SwingUtilities.convertPoint(c, rect.getLocation(), augmentedPanel);
        int x = convertedPoint.x;
        int y = convertedPoint.y;
        int substringStart = labelText.toLowerCase().indexOf(spotlightText.toLowerCase());
        int substringEnd = substringStart + spotlightText.length();
        RoundRectangle2D roundRectangle2D = new RoundRectangle2D.Double(xOffset + x + fontMetrics.charsWidth(labelText.substring(0, substringStart).toCharArray(), 0, substringStart), y,
                        fontMetrics.charsWidth(labelText.substring(substringStart, substringEnd).toCharArray(), 0, spotlightText.length()),
                        rect.getHeight(), rect.getHeight()/2, rect.getHeight()/2);
        return roundRectangle2D;
    }
    
    private void spotlightText(Container container, Area mask) {        
        for (Component c : container.getComponents()) {
            if (c.isShowing() && c instanceof JLabel) {
                String labelText = ((JLabel) c).getText();
                if (labelText != null && !labelText.isEmpty()) {
                    if (labelText.toLowerCase().contains(spotlightText.toLowerCase())) {
                        Shape spotlight = createSpotlight(c, new Rectangle(0, 0, c.getWidth(), c.getHeight()), labelText, c.getFontMetrics(c.getFont()), 0);
                        mask.subtract(new Area(spotlight));                        
                    }
                }
            } else if (c instanceof JList) {
                JList list = (JList) c;
                ListModel listModel = list.getModel();
                for (int i = 0; i < listModel.getSize(); i++) {
                    Object elementAt = listModel.getElementAt(i);
                    if (elementAt instanceof String) {
                        if (((String) elementAt).toLowerCase().contains(spotlightText.toLowerCase())) {
                            String labelText = (String) elementAt;
                            Rectangle cellBounds = list.getCellBounds(i, i);
                            JLabel label = (JLabel) list.getCellRenderer().getListCellRendererComponent(list, elementAt, i, false, false);                            
                            Shape shape = createSpotlight(c, cellBounds, labelText, label.getFontMetrics(label.getFont()), label.getInsets().left);
                            mask.subtract(new Area(shape));
                        }
                    }
                }
            } else if (c instanceof Container) {
                spotlightText((Container) c, mask);                
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (isInSpotlightMode()) {
            spotlight((Graphics2D) g);
            return;
        }
        
        for (Panel panel : highlightedPanels)
            highlight(panel, (Graphics2D) g);
    }
    
    private void spotlight(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(Color.black);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        Rectangle2D screen = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
        Area mask = new Area(screen);
        
        spotlightText(augmentedPanel, mask);
        
        g2.fill(mask);        
    }
    
    private boolean isInSpotlightMode() {
        return spotlightText != null && !spotlightText.isEmpty();
    }

    private void highlight(Panel panel, Graphics2D g) {
        highlightedPanels.add(panel);
        panel.setTitleBounds();

        g.setColor(HIGHLIGHT_COLOR);
        g.setStroke(new BasicStroke(s));
        Point location = panel.getLocation();
        g.drawRect(location.x, location.y, panel.getWidth(), panel.getHeight());

        /*
         * (x0, y0) (x1, y0) (x2, y0) (x0, y1) (x2, y1) (x0, y2) (x1, y2) (x2,
         * y2)
         */

        int x0 = location.x, x1 = location.x + panel.getWidth() / 2, x2 = location.x
                        + panel.getWidth();
        int y0 = location.y, y1 = location.y + panel.getHeight() / 2, y2 = location.y
                        + panel.getHeight();

        g.drawRect(x0 - h, y0 - h, d, d);
        g.drawRect(x1 - h, y0 - h, d, d);
        g.drawRect(x2 - h, y0 - h, d, d);

        g.drawRect(x0 - h, y1 - h, d, d);
        g.drawRect(x2 - h, y1 - h, d, d);

        g.drawRect(x0 - h, y2 - h, d, d);
        g.drawRect(x1 - h, y2 - h, d, d);
        g.drawRect(x2 - h, y2 - h, d, d);

        g.setColor(HIGHLIGHT_COLOR.brighter());

        g.fill3DRect(x0 - h, y0 - h, d, d, true);
        g.fill3DRect(x1 - h, y0 - h, d, d, true);
        g.fill3DRect(x2 - h, y0 - h, d, d, true);

        g.fill3DRect(x0 - h, y1 - h, d, d, true);
        g.fill3DRect(x2 - h, y1 - h, d, d, true);

        g.fill3DRect(x0 - h, y2 - h, d, d, true);
        g.fill3DRect(x1 - h, y2 - h, d, d, true);
        g.fill3DRect(x2 - h, y2 - h, d, d, true);
    }

    public void removeHighlights(Collection<Panel> panels) {
        for (Panel panel : panels) {
            highlightedPanels.remove(panel);

            Rectangle bounds = panel.getBounds();
            repaint(new Rectangle(Math.max(bounds.x - h, 0), Math.max(bounds.y - h, 0),
                            bounds.width + d, bounds.height + d));

        }
        setAugmentationCursor(Cursor.getPredefinedCursor(Cursor.getDefaultCursor().getType()));
    }
    
    private void setAugmentationCursor(Cursor cursor) {
        // Get the top level Augmentation
        Augmentation augmentation = this, originator = this;
        Container parent = augmentation.getParent();
        while (parent != null) {
            if (parent instanceof CanvasManifestation)
                augmentation = ((CanvasManifestation) parent).augmentation;
            parent = parent.getParent();
        }
        originator.setCursor(cursor);
        augmentation.setCursor(cursor);
    }

}
