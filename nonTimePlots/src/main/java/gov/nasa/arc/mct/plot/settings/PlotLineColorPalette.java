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
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.UIManager;

/**
 * Defines set of colors to use for plotting lines. 
 *
 */
public class PlotLineColorPalette {
	private static final ResourceBundle BUNDLE = 
        ResourceBundle.getBundle(PlotLineColorPalette.class.getName().substring(0, 
        		PlotLineColorPalette.class.getName().lastIndexOf("."))+".Bundle");

	public final static Color COLOR1 = new Color(032, 178, 170);
	public final static Color COLOR2 = new Color(154, 205, 050);
	public final static Color COLOR3 = new Color(255, 140, 000);
	public final static Color COLOR4 = new Color(210, 180, 140);
	public final static Color COLOR5 = new Color(064, 224, 208);
	public final static Color COLOR6 = new Color(065, 105, 255);
	public final static Color COLOR7 = new Color(255, 215, 000);
	public final static Color COLOR8 = new Color(106, 90, 205);
	public final static Color COLOR9 = new Color(238, 130, 238);
	public final static Color COLOR10 = new Color(204, 153, 102);
	public final static Color COLOR11 = new Color(153, 204, 204);
	public final static Color COLOR12 = new Color(102, 204, 051);
	public final static Color COLOR13 = new Color(255, 204, 000);
	public final static Color COLOR14 = new Color(255, 102, 051);
	public final static Color COLOR15 = new Color(204, 102, 255);
	public final static Color COLOR16 = new Color(255, 000, 102);
	public final static Color COLOR17 = new Color(255, 255, 000);
	public final static Color COLOR18 = new Color(128, 000, 128);
	public final static Color COLOR19 = new Color(000, 134, 139);
	public final static Color COLOR20 = new Color(000, 138, 000);
	public final static Color COLOR21 = new Color(255, 000, 000);
	public final static Color COLOR22 = new Color(000, 000, 255);
	public final static Color COLOR23 = new Color(245, 222, 179);	
	public final static Color COLOR24 = new Color(188, 143, 143);
	public final static Color COLOR25 = new Color( 70, 130, 180);
	public final static Color COLOR26 = new Color(255, 175, 175);
	public final static Color COLOR27 = new Color(67, 205, 128);
	public final static Color COLOR28 = new Color(205, 193, 197);
	public final static Color COLOR29 = new Color(160,  82,  45);
	public final static Color COLOR30 = new Color(100, 149, 237);
	public final static Color COLOR31 = new Color(000, 000, 000);
	

	private static List<Color> colorSet = new ArrayList<Color>();

	private static void loadColors() {
		colorSet.add(COLOR1);
		colorSet.add(COLOR2);
		colorSet.add(COLOR3);
		colorSet.add(COLOR4);
		colorSet.add(COLOR5);
		colorSet.add(COLOR6);
		colorSet.add(COLOR7);
		colorSet.add(COLOR8);
		colorSet.add(COLOR9);
		colorSet.add(COLOR10);
		colorSet.add(COLOR11);
		colorSet.add(COLOR12);
		colorSet.add(COLOR13);
		colorSet.add(COLOR14);
		colorSet.add(COLOR15);
		colorSet.add(COLOR16);
		colorSet.add(COLOR17);
		colorSet.add(COLOR18);
		colorSet.add(COLOR19);
		colorSet.add(COLOR20);
		colorSet.add(COLOR21);
		colorSet.add(COLOR22);
		colorSet.add(COLOR23);
		colorSet.add(COLOR24);
		colorSet.add(COLOR25);
		colorSet.add(COLOR26);
		colorSet.add(COLOR27);
		colorSet.add(COLOR28);
		colorSet.add(COLOR29);
		colorSet.add(COLOR30);
		colorSet.add(COLOR31);	// Add last - we don't want to use black

	}
	
	/**
	 * Return the ith color in the palette. If color is specified in the 
	 * UIManager (by way of viewColor.properties), use that; otherwise, fall 
	 * back to defaults.
	 * 
	 * @param i the index of the color to use
	 * @return the ith color
	 */
	public static Color getColor(int i) {
		
		// Try to find a scheme-specified color first
		Color c = UIManager.getColor(String.format(
				BUNDLE.getString("PlotColor.format"), (i+1)));
		if (c != null) return c;
		
		if (colorSet.size() == 0) {
			loadColors();
		}
		
		if (i >= colorSet.size()){
			throw new IllegalArgumentException("Requested color " + i + " out of range [ 0 .. "+ colorSet.size() + "]");
		}
		
		return colorSet.get(i);
	}

	/**
	 * Return the number of colors in the pallet
	 * @return the number of colors
	 */
	public static int getColorCount() {
		if (colorSet.size() == 0) {
			loadColors();
		}

		return colorSet.size();
	}
}
