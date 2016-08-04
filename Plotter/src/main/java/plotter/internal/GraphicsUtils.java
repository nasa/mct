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
package plotter.internal;

import java.awt.BasicStroke;
import java.awt.Graphics;


/**
 * Common graphics code.
 * @author Adam Crume
 */
public class GraphicsUtils {
	/**
	 * Equivalent to calling {@link Graphics#drawLine(int, int, int, int)} with a {@link BasicStroke} with width 1 and dash array of length 2, but faster. 
	 * @param g graphics context
	 * @param x1 first X coordinate
	 * @param y1 first Y coordinate
	 * @param x2 second X coordinate
	 * @param y2 second Y coordinate
	 * @param dashlength length of the drawn portion
	 * @param spacelength length of the undrawn portion
	 */
	public static void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2, double dashlength, double spacelength) {
		int dx = x2 - x1;
		int dy = y2 - y1;
		double linelength = Math.sqrt(dx * dx + dy * dy);
		double strokeLength = dashlength + spacelength;
		double s1 = strokeLength / linelength;
		double xincdashspace = dx * s1;
		double yincdashspace = dy * s1;
		double s2 = (dashlength - 1) / linelength;
		double xincdash = dx * s2;
		double yincdash = dy * s2;
		int counter = 0;
		double x = x1 + .5;
		double y = y1 + .5;
		double max = linelength - dashlength;
		for(double i = 0; i < max; i += strokeLength) {
			g.drawLine((int) x, (int) y, (int) (x + xincdash), (int) (y + yincdash));
			x += xincdashspace;
			y += yincdashspace;
			counter++;
		}
		if(strokeLength * counter < linelength) {
			g.drawLine((int) (x1 + xincdashspace * counter + .5), (int) (y1 + yincdashspace * counter + .5), x2, y2);
		}
	}
}
