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
package gov.nasa.arc.mct.gui.housing;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Implements a drop target listener that autoscrolls a component if the mouse
 * is in a border strip around the edges of the component. Once the drag-and-drop
 * starts, a timer is used to generate timer ticks. At each tick, if the mouse
 * is in an area just inside the edges of the component, then we scroll in that
 * direction. (We can scroll in two directions simultaneously if the mouse is in
 * a corner.)
 * 
 * We must ensure the timer is eventually stopped. This happens whenever
 * there is a call to {{@link #dragExit(DropTargetEvent)} or
 * {{@link #drop(DropTargetDropEvent)}. We make the following assumptions about
 * the order of calls. (Sun's code in {@link javax.swing.TransferHandler.DropHandler}
 * appears to make the same assumptions.)
 * 
 * <ol>
 *   <li>dragEnter()
 *   <li>Any number of calls to dragOver() (possibly interspersed at times
 *   with a dragExit() followed by dragEnter(), if the mouse leaves the
 *   directory tree and reenters)
 *   <li>Either dragExit() or drop()
 * </ol>
 * This assumption guarantees that the timer will be stopped at the end
 * of the drag-and-drop operation.
 */
class AutoscrollDropTargetListener implements DropTargetListener, ActionListener {

    /**
     * How near the mouse must be to the edge of the control to
     * cause autoscrolling during drag-and-drop.
     */
    public static final int AUTOSCROLL_BORDER_SIZE = 10;
    
    /** How many milliseconds before autoscrolling commences. */
    public static final int AUTOSCROLL_INITIAL_DELAY = 50;
    
    /** How many milliseconds between autoscroll events. */
    public static final int AUTOSCROLL_INTERVAL = 100;
    
    /** A timer used to perform autoscrolling at a fixed interval. */
    private Timer timer;
    
    /** The tree to scroll. */
    private JComponent comp;
    
    /** Mouse coordinates, or null if not under mouse */
    private Point mouse = null;
    
    /**
     * Creates a new autoscrolling drop target listener.
     * 
     * @param comp the component to autoscroll
     * @throws IllegalArgumentException if the component does not implement {@link javax.swing.Scrollable}
     */
    public AutoscrollDropTargetListener(JComponent comp) throws IllegalArgumentException {
        if (!(comp instanceof Scrollable)) {
            throw new IllegalArgumentException("Component must implement javax.swing.Scrollable");
        }

        this.comp = comp;
        this.timer = new Timer(AUTOSCROLL_INTERVAL, this);
        timer.setInitialDelay(AUTOSCROLL_INITIAL_DELAY);
    }
    
    /**
     * Package-internal constructor for unit testing.
     * 
     * @param tree the tree to autoscroll
     * @param timer the timer to use for generating autoscroll events
     * @throws IllegalArgumentException if the component does not implement {@link javax.swing.Scrollable}
     */
    AutoscrollDropTargetListener(JComponent comp, Timer timer) throws IllegalArgumentException {
        if (!(comp instanceof Scrollable)) {
            throw new IllegalArgumentException("Component must implement javax.swing.Scrollable");
        }

        this.comp = comp;
        this.timer = timer;
        timer.setInitialDelay(50);
    }

    @Override
    public synchronized void dragEnter(DropTargetDragEvent dtde) {
        if (!timer.isRunning()) {
            timer.start();
            mouse = dtde.getLocation();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        mouse = dtde.getLocation();
    }

    @Override
    public synchronized void dragExit(DropTargetEvent dte) {
        if (timer.isRunning()) {
            timer.stop();
            mouse = null;
        }
    }

    @Override
    public synchronized void drop(DropTargetDropEvent dtde) {
        if (timer.isRunning()) {
            timer.stop();
            mouse = null;
        }
    }

    // This method is called upon a timer tick. If the mouse is in
    // the autoscroll border region, in either axis, then scroll
    // the tree appropriately, if not already at the end of the
    // range for that scroll direction.
    //
    // Note that we retain mouse position as reported by 
    // DropTargetDropEvent. Earlier implementation of this method 
    // retrieved mouse position explicitly (via Component.getMousePosition())
    // but this occasionally caused deadlocks on Mac OS X due to
    // concurrency issues with AppKit thread
    @Override
    public void actionPerformed(ActionEvent e) {
        Rectangle bounds = comp.getBounds();
        Rectangle visible = comp.getVisibleRect();
        Rectangle newVisible = new Rectangle(visible.x, visible.y, visible.width, visible.height);
        
        // We know that this cast must succeed, since the constructors
        // throw IllegalArgumentException if the component is not a
        // Scrollable.
        Scrollable s = (Scrollable) comp;
        
        // If the mouse is not over the tree at this time, ignore
        // the timer event.
        if (mouse == null) {
            return;
        }

        // If we're in the autoscroll strip at the left or the right, adjust
        // the x position. We increase the x-amount to scroll, because the amount
        // that JTree tells us to scroll is too small. (Per conversation with
        // Tom Dayton, 2010-06-15. The factor to increase by was empirically
        // arrived at by Mark Rose on 2010-06-15.)
        if (mouse.x < visible.x + AUTOSCROLL_BORDER_SIZE) {
            newVisible.x = max(0, visible.x - 3*s.getScrollableUnitIncrement(visible, SwingConstants.HORIZONTAL, -1));
        } else if (mouse.x > visible.x + visible.width - AUTOSCROLL_BORDER_SIZE) {
            newVisible.x = min(bounds.width - visible.width, visible.x + 3*s.getScrollableUnitIncrement(visible, SwingConstants.HORIZONTAL, 1));
        }

        // If we're in the autoscroll strip at the top or the bottom, adjust
        // the y position.
        if (mouse.y < visible.y + AUTOSCROLL_BORDER_SIZE) {
            newVisible.y = max(0, visible.y - s.getScrollableUnitIncrement(visible, SwingConstants.VERTICAL, -1));
        } else if (mouse.y > visible.y + visible.height - AUTOSCROLL_BORDER_SIZE) {
            newVisible.y = min(bounds.height - visible.height, visible.y + s.getScrollableUnitIncrement(visible, SwingConstants.VERTICAL, 1));
        }
        
        // If we changed either x or y, then scroll the tree.
        if (!newVisible.equals(visible)) {
            comp.scrollRectToVisible(newVisible);
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // ignore
    }
   
}