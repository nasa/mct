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
package plotter;

import java.util.Arrays;

import junit.framework.TestCase;

public class JUnitIntegerTickMarkCalculator extends TestCase {
	public void testExact() {
		double[][] data = ticks(1, 2);
		check(data[0], 1.0, 2.0);
		check(data[1]);
	}


	public void testLess() {
		double[][] data = ticks(1.01, 1.99);
		check(data[0]);
		check(data[1]);
	}


	public void testMore() {
		double[][] data = ticks(0.99, 2.01);
		check(data[0], 1.0, 2.0);
		check(data[1]);
	}


	public void testExactNegative() {
		double[][] data = ticks(-2, -1);
		check(data[0], -1.0, -2.0);
		check(data[1]);
	}


	public void testLessNegative() {
		double[][] data = ticks(-1.99, -1.01);
		check(data[0]);
		check(data[1]);
	}


	public void testMoreNegative() {
		double[][] data = ticks(-2.01, -0.99);
		check(data[0], -1.0, -2.0);
		check(data[1]);
	}


	public void testExactInverted() {
		double[][] data = ticks(2, 1);
		check(data[0], 1.0, 2.0);
		check(data[1]);
	}


	public void testLessInverted() {
		double[][] data = ticks(1.99, 1.01);
		check(data[0]);
		check(data[1]);
	}


	public void testMoreInverted() {
		double[][] data = ticks(2.01, 0.99);
		check(data[0], 1.0, 2.0);
		check(data[1]);
	}


	public void testExactNegativeInverted() {
		double[][] data = ticks(-1, -2);
		check(data[0], -1.0, -2.0);
		check(data[1]);
	}


	public void testLessNegativeInverted() {
		double[][] data = ticks(-1.01, -1.99);
		check(data[0]);
		check(data[1]);
	}


	public void testMoreNegativeInverted() {
		double[][] data = ticks(-0.99, -2.01);
		check(data[0], -1.0, -2.0);
		check(data[1]);
	}


	public void testExactPosNeg() {
		double[][] data = ticks(-1, 1);
		check(data[0], -1.0, 0.0, 1.0);
		check(data[1]);
	}


	public void testLessPosNeg() {
		double[][] data = ticks(-0.99, 0.99);
		check(data[0], 0.0);
		check(data[1]);
	}


	public void testMorePosNeg() {
		double[][] data = ticks(-1.01, 1.01);
		check(data[0], -1.0, 0.0, 1.0);
		check(data[1]);
	}


	public void testExactPosNegInverted() {
		double[][] data = ticks(1, -1);
		check(data[0], -1.0, 0.0, 1.0);
		check(data[1]);
	}


	public void testLessPosNegInverted() {
		double[][] data = ticks(0.99, -0.99);
		check(data[0], 0.0);
		check(data[1]);
	}


	public void testMorePosNegInverted() {
		double[][] data = ticks(1.01, -1.01);
		check(data[0], -1.0, 0.0, 1.0);
		check(data[1]);
	}


	private void check(double[] actual, double... expected) {
		Arrays.sort(actual);
		Arrays.sort(expected);
		String msg = "Expected " + Arrays.toString(expected) + ", but got " + Arrays.toString(actual);
		assertEquals(msg, expected.length, actual.length);
		for(int i = 0; i < expected.length; i++) {
			assertTrue(msg, Math.abs(expected[i] - actual[i]) < .0000001);
		}
	}


	private double[][] ticks(double start, double end) {
		IntegerTickMarkCalculator c = new IntegerTickMarkCalculator();
		Axis axis = new Axis() {
			private static final long serialVersionUID = 1L;
		};
		axis.setStart(start);
		axis.setEnd(end);
		return c.calculateTickMarks(axis);
	}
}
