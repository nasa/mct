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
package plotter.xy;

import junit.framework.TestSuite;

public class JUnitXY extends TestSuite {
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Plotter XY");
		suite.addTestSuite(JUnitCompressingXYDataset.class);
		suite.addTestSuite(JUnitDefaultCompressor.class);
		suite.addTestSuite(JUnitDefaultXYLayoutGenerator.class);
		suite.addTestSuite(JUnitLinearXYAxis.class);
		suite.addTestSuite(JUnitLinearXYPlotLine.class);
		suite.addTestSuite(JUnitLinearXYPlotLineYIndependent.class);
		suite.addTestSuite(JUnitScatterXYPlotLine.class);
		suite.addTestSuite(JUnitSimpleXYDataset.class);
		suite.addTestSuite(JUnitSlopeLine.class);
		suite.addTestSuite(JUnitSlopeLineDisplay.class);
		suite.addTestSuite(JUnitXYAxis.class);
		suite.addTestSuite(JUnitXYGrid.class);
		suite.addTestSuite(JUnitXYLocationDisplay.class);
		suite.addTestSuite(JUnitXYMarkerLine.class);
		suite.addTestSuite(JUnitXYPlot.class);
		suite.addTestSuite(JUnitXYPlotLine.class);
		suite.addTestSuite(JUnitXYReversingDataset.class);
		return suite;
	}
}
