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
/**
 * MCTGUIResourceBundle.java Aug 18, 2008
 * 
 * This code is property of the National Aeronautics and Space Administration
 * and was produced for the Mission Control Technologies (MCT) Project.
 * 
 */
package gov.nasa.arc.mct.gui.impl;

import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * This class contains the static GUI constants.
 * 
 * @author nshi
 * 
 */
public final class MCTGUIResourceBundle {

    /** The color for the title bar on a selected view manifestation. */
    public static final Color ACTIVE_COLOR = SystemColor.textHighlight;
    /** The color for the title bar on an unselected view manifestation. */
    public static final Color INACTIVE_COLOR = new JPanel().getBackground();

    /** The width of the thick border on a selected view manifestation. */
    public static final int BORDERLINE_SIZE = 5;
    /** The border to use for a selected view manifestation. */
    public static final Border ACTIVE_BORDER = BorderFactory.createLineBorder(MCTGUIResourceBundle.ACTIVE_COLOR, BORDERLINE_SIZE);
    /** The border to use for an unselected view manifestation. */
    public static final Border INACTIVE_BORDER = BorderFactory.createLineBorder(INACTIVE_COLOR, BORDERLINE_SIZE);

}
