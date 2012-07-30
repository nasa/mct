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

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.services.component.ViewInfo;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestPopupOpener {

    private static final int ONE_CLICK = 1;
    private static final int X_LOC = 0;
    private static final int Y_LOC = 0;
    private static final int MODS = 0;
    private static final int WHEN = 0;

    private AbstractComponent comp;
    private MCTPopupOpener firstPopupOpener;
    private MyMCTViewManifestation manif1;
    private MyMCTViewManifestation manif2;
    private MCTPopupOpener secondPopupOpener;

    class MyFirstPopupOpener extends MCTPopupOpener {
        public MyFirstPopupOpener(AbstractComponent comp, Set<View> viewManifSet) {
            super(comp, viewManifSet);
        }

        @Override
        JPopupMenu generatePopupMenu(ActionContextImpl context) {
            JPopupMenu newMenu = new JPopupMenu();
            newMenu.setVisible(true);
            return newMenu;
        }
    }

    class MySecondPopupOpener extends MCTPopupOpener {
        public MySecondPopupOpener(AbstractComponent comp, Set<View> viewManifSet) {
            super(comp, viewManifSet);
        }

        @Override
        JPopupMenu generatePopupMenu(ActionContextImpl context) {
            return null;
        }
    }

    public class MyMCTViewManifestation extends View {
        private static final long serialVersionUID = 1L;

        public MyMCTViewManifestation(AbstractComponent comp, ViewInfo vi) {
            super(comp,vi);
            // TODO Auto-generated constructor stub
       }
    }

    @BeforeClass
    public void setup() {
        Set<View> viewManifSet = new HashSet<View>();
        // Load the set of view manifestations
        manif1 = new MyMCTViewManifestation(comp,null);
        manif2 = new MyMCTViewManifestation(comp,null);
        viewManifSet.add(manif1);
        viewManifSet.add(manif2);

        firstPopupOpener = new MyFirstPopupOpener(comp, viewManifSet);
        secondPopupOpener = new MySecondPopupOpener(comp, viewManifSet);
        JFrame frame = new JFrame();
        frame.add(manif1);
        frame.add(manif2);
        frame.setVisible(true);
    }

    @Test
    public void testShowingPopupMenu() {
        MouseEvent event = new MouseEvent(manif1, MouseEvent.MOUSE_CLICKED,
                WHEN, MODS, X_LOC, Y_LOC, ONE_CLICK, false, MouseEvent.BUTTON3);

        // Assertion that the number of Frames has not changed
        Frame[] framesBefore = Frame.getFrames();
        secondPopupOpener.mousePressed(event);
        Frame[] framesAfter = Frame.getFrames();
        Assert.assertTrue(framesBefore.length == framesAfter.length);

        // Assertion that the count of open Frames has increased by one
        framesBefore = Frame.getFrames();
        firstPopupOpener.mousePressed(event);
        framesAfter = Frame.getFrames();
        Assert.assertTrue(framesBefore.length + 1 == framesAfter.length);
    }

}
