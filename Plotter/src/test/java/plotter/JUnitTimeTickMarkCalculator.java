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

public class JUnitTimeTickMarkCalculator extends TestCase {
	private static final long SECOND = 1000;

	private static final long MINUTE = 60 * SECOND;

	private static final long HOUR = 60 * MINUTE;

	private static final long DAY = 24 * HOUR;


	public void testExact() {
		double[][] data = ticks(60 * 1000, 120 * 1000);
		checkMajor(data[0], 60000, 75000, 90000, 105000, 120000);
		check(data[1], 60000, 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000, 105000, 110000, 115000, 120000);
	}


	public void testLess() {
		double[][] data = ticks(60 * 1000 + 1, 120 * 1000 - 1);
		checkMajor(data[0], 75000, 90000, 105000);
		check(data[1], 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000, 105000, 110000, 115000);
	}


	public void testMore() {
		double[][] data = ticks(60 * 1000 - 1, 120 * 1000 + 1);
		checkMajor(data[0], 60000, 90000, 120000);
		check(data[1], 60000, 70000, 80000, 90000, 100000, 110000, 120000);
	}


	public void testExactNegative() {
		double[][] data = ticks(-60 * 1000, -120 * 1000);
		checkMajor(data[0], -120000, -105000, -90000, -75000, -60000);
		check(data[1], -60000, -65000, -70000, -75000, -80000, -85000, -90000, -95000, -100000, -105000, -110000, -115000, -120000);
	}


	public void testLessNegative() {
		double[][] data = ticks(-60 * 1000 - 1, -120 * 1000 + 1);
		checkMajor(data[0], -105000, -90000, -75000);
		check(data[1], -65000, -70000, -75000, -80000, -85000, -90000, -95000, -100000, -105000, -110000, -115000);
	}


	public void testMoreNegative() {
		double[][] data = ticks(-60 * 1000 + 1, -120 * 1000 - 1);
		checkMajor(data[0], -120000, -90000, -60000);
		check(data[1], -60000, -70000, -80000, -90000, -100000, -110000, -120000);
	}


	public void testExactInverted() {
		double[][] data = ticks(120 * 1000, 60 * 1000);
		checkMajor(data[0], 60000, 75000, 90000, 105000, 120000);
		check(data[1], 60000, 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000, 105000, 110000, 115000, 120000);
	}


	public void testLessInverted() {
		double[][] data = ticks(120 * 1000 - 1, 60 * 1000 + 1);
		checkMajor(data[0], 75000, 90000, 105000);
		check(data[1], 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000, 105000, 110000, 115000);
	}


	public void testMoreInverted() {
		double[][] data = ticks(120 * 1000 + 1, 60 * 1000 - 1);
		checkMajor(data[0], 60000, 90000, 120000);
		check(data[1], 60000, 70000, 80000, 90000, 100000, 110000, 120000);
	}


	public void testExactNegativeInverted() {
		double[][] data = ticks(-120 * 1000, -60 * 1000);
		checkMajor(data[0], -120000, -105000, -90000, -75000, -60000);
		check(data[1], -60000, -65000, -70000, -75000, -80000, -85000, -90000, -95000, -100000, -105000, -110000, -115000, -120000);
	}


	public void testLessNegativeInverted() {
		double[][] data = ticks(-120 * 1000 + 1, -60 * 1000 - 1);
		checkMajor(data[0], -105000, -90000, -75000);
		check(data[1], -65000, -70000, -75000, -80000, -85000, -90000, -95000, -100000, -105000, -110000, -115000);
	}


	public void testMoreNegativeInverted() {
		double[][] data = ticks(-120 * 1000 - 1, -60 * 1000 + 1);
		checkMajor(data[0], -120000, -90000, -60000);
		check(data[1], -60000, -70000, -80000, -90000, -100000, -110000, -120000);
	}


	public void testExactPosNeg() {
		double[][] data = ticks(-60 * 1000, 60 * 1000);
		checkMajor(data[0], -60000, -30000, 0.0, 30000, 60000);
		check(data[1], -60000, -50000, -40000, -30000, -20000, -10000, 0.0, 10000, 20000, 30000, 40000, 50000, 60000);
	}


	public void testLessPosNeg() {
		double[][] data = ticks(-60 * 1000 + 1, 60 * 1000 - 1);
		checkMajor(data[0], -30000, 0.0, 30000);
		check(data[1], -50000, -40000, -30000, -20000, -10000, 0.0, 10000, 20000, 30000, 40000, 50000);
	}


	public void testMorePosNeg() {
		double[][] data = ticks(-60 * 1000 - 1, 60 * 1000 + 1);
		checkMajor(data[0], 0.0);
		check(data[1], -60000, 0.0, 60000);
	}


	public void testExactPosNegInverted() {
		double[][] data = ticks(60 * 1000, -60 * 1000);
		checkMajor(data[0], -60000, -30000, 0.0, 30000, 60000);
		check(data[1], -60000, -50000, -40000, -30000, -20000, -10000, 0.0, 10000, 20000, 30000, 40000, 50000, 60000);
	}


	public void testLessPosNegInverted() {
		double[][] data = ticks(60 * 1000 - 1, -60 * 1000 + 1);
		checkMajor(data[0], -30000, 0.0, 30000);
		check(data[1], -50000, -40000, -30000, -20000, -10000, 0.0, 10000, 20000, 30000, 40000, 50000);
	}


	public void testMorePosNegInverted() {
		double[][] data = ticks(60 * 1000 + 1, -60 * 1000 - 1);
		checkMajor(data[0], 0.0);
		check(data[1], -60000, 0.0, 60000);
	}


	public void test11Days() {
		double[][] data = ticks(0, 11 * DAY);
		checkMajor(data[0], 0, 5 * DAY, 10 * DAY);
		check(data[1], 0, 1 * DAY, 2 * DAY, 3 * DAY, 4 * DAY, 5 * DAY, 6 * DAY, 7 * DAY, 8 * DAY, 9 * DAY, 10 * DAY, 11 * DAY);
	}


	public void test7Days() {
		double[][] data = ticks(0, 7 * DAY);
		checkMajor(data[0], 0, 2 * DAY, 4 * DAY, 6 * DAY);
		check(data[1], 0, 1 * DAY, 2 * DAY, 3 * DAY, 4 * DAY, 5 * DAY, 6 * DAY, 7 * DAY);
	}


	public void test4Days() {
		double[][] data = ticks(0, 4 * DAY);
		checkMajor(data[0], 0, 1 * DAY, 2 * DAY, 3 * DAY, 4 * DAY);
		check(data[1], 0, .5 * DAY, 1 * DAY, 1.5 * DAY, 2 * DAY, 2.5 * DAY, 3 * DAY, 3.5 * DAY, 4 * DAY);
	}


	public void test36Hours() {
		double[][] data = ticks(0, 36 * HOUR);
		checkMajor(data[0], 0, 12 * HOUR, 24 * HOUR, 36 * HOUR);
		check(data[1], 0, 6 * HOUR, 12 * HOUR, 18 * HOUR, 24 * HOUR, 30 * HOUR, 36 * HOUR);
	}


	public void test18Hours() {
		double[][] data = ticks(0, 18 * HOUR);
		checkMajor(data[0], 0, 6 * HOUR, 12 * HOUR, 18 * HOUR);
		check(data[1], 0, 2 * HOUR, 4 * HOUR, 6 * HOUR, 8 * HOUR, 10 * HOUR, 12 * HOUR, 14 * HOUR, 16 * HOUR, 18 * HOUR);
	}


	public void test4Hours() {
		double[][] data = ticks(0, 4 * HOUR);
		checkMajor(data[0], 0, 2 * HOUR, 4 * HOUR);
		check(data[1], 0, 1 * HOUR, 2 * HOUR, 3 * HOUR, 4 * HOUR);
	}


	public void test90Minutes() {
		double[][] data = ticks(0, 90 * MINUTE);
		checkMajor(data[0], 0, 30 * MINUTE, 60 * MINUTE, 90 * MINUTE);
		check(data[1], 0, 10 * MINUTE, 20 * MINUTE, 30 * MINUTE, 40 * MINUTE, 50 * MINUTE, 60 * MINUTE, 70 * MINUTE, 80 * MINUTE, 90 * MINUTE);
	}


	public void test45Minutes() {
		double[][] data = ticks(0, 45 * MINUTE);
		checkMajor(data[0], 0, 15 * MINUTE, 30 * MINUTE, 45 * MINUTE);
		check(data[1], 0, 5 * MINUTE, 10 * MINUTE, 15 * MINUTE, 20 * MINUTE, 25 * MINUTE, 30 * MINUTE, 35 * MINUTE, 40 * MINUTE, 45 * MINUTE);
	}


	public void test25Minutes() {
		double[][] data = ticks(0, 25 * MINUTE);
		checkMajor(data[0], 0, 10 * MINUTE, 20 * MINUTE);
		check(data[1], 0, 5 * MINUTE, 10 * MINUTE, 15 * MINUTE, 20 * MINUTE, 25 * MINUTE);
	}


	public void test15Minutes() {
		double[][] data = ticks(0, 15 * MINUTE);
		checkMajor(data[0], 0, 5 * MINUTE, 10 * MINUTE, 15 * MINUTE);
		check(data[1], 0, 1 * MINUTE, 2 * MINUTE, 3 * MINUTE, 4 * MINUTE, 5 * MINUTE, 6 * MINUTE, 7 * MINUTE, 8 * MINUTE, 9 * MINUTE, 10 * MINUTE,
				11 * MINUTE, 12 * MINUTE, 13 * MINUTE, 14 * MINUTE, 15 * MINUTE);
	}


	public void test5Minutes() {
		double[][] data = ticks(0, 5 * MINUTE);
		checkMajor(data[0], 0, 2 * MINUTE, 4 * MINUTE);
		check(data[1], 0, 1 * MINUTE, 2 * MINUTE, 3 * MINUTE, 4 * MINUTE, 5 * MINUTE);
	}


	public void test90Seconds() {
		double[][] data = ticks(0, 90 * SECOND);
		checkMajor(data[0], 0, 30 * SECOND, 60 * SECOND, 90 * SECOND);
		check(data[1], 0, 10 * SECOND, 20 * SECOND, 30 * SECOND, 40 * SECOND, 50 * SECOND, 60 * SECOND, 70 * SECOND, 80 * SECOND, 90 * SECOND);
	}


	public void test45Seconds() {
		double[][] data = ticks(0, 45 * SECOND);
		checkMajor(data[0], 0, 15 * SECOND, 30 * SECOND, 45 * SECOND);
		check(data[1], 0, 5 * SECOND, 10 * SECOND, 15 * SECOND, 20 * SECOND, 25 * SECOND, 30 * SECOND, 35 * SECOND, 40 * SECOND, 45 * SECOND);
	}


	public void test25Seconds() {
		double[][] data = ticks(0, 25 * SECOND);
		checkMajor(data[0], 0, 10 * SECOND, 20 * SECOND);
		check(data[1], 0, 5 * SECOND, 10 * SECOND, 15 * SECOND, 20 * SECOND, 25 * SECOND);
	}


	public void test15Seconds() {
		double[][] data = ticks(0, 15 * SECOND);
		checkMajor(data[0], 0, 5 * SECOND, 10 * SECOND, 15 * SECOND);
		check(data[1], 0, 1 * SECOND, 2 * SECOND, 3 * SECOND, 4 * SECOND, 5 * SECOND, 6 * SECOND, 7 * SECOND, 8 * SECOND, 9 * SECOND, 10 * SECOND,
				11 * SECOND, 12 * SECOND, 13 * SECOND, 14 * SECOND, 15 * SECOND);
	}


	public void test5Seconds() {
		double[][] data = ticks(0, 5 * SECOND);
		checkMajor(data[0], 0, 2 * SECOND, 4 * SECOND);
		check(data[1], 0, 1 * SECOND, 2 * SECOND, 3 * SECOND, 4 * SECOND, 5 * SECOND);
	}


	public void test600Millis() {
		double[][] data = ticks(0, 600);
		checkMajor(data[0], 0, 200, 400, 600);
		check(data[1], 0, 100, 200, 300, 400, 500, 600);
	}


	public void test300Millis() {
		double[][] data = ticks(0, 300);
		checkMajor(data[0], 0, 100, 200, 300);
		check(data[1], 0, 50, 100, 150, 200, 250, 300);
	}


	public void test100Millis() {
		double[][] data = ticks(0, 100);
		checkMajor(data[0], 0, 50, 100);
		check(data[1], 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
	}


	private void checkMajor(double[] actual, double... expected) {
		String msg = "Expected " + Arrays.toString(expected) + ", but got " + Arrays.toString(actual);
		assertEquals(msg, expected.length, actual.length);
		for(int i = 0; i < expected.length; i++) {
			assertTrue(msg, Math.abs(expected[i] - actual[i]) <= .0000001 * Math.abs(expected[i]));
		}
	}


	private void check(double[] actual, double... expected) {
		Arrays.sort(actual);
		Arrays.sort(expected);
		String msg = "Expected " + Arrays.toString(expected) + ", but got " + Arrays.toString(actual);
		assertEquals(msg, expected.length, actual.length);
		for(int i = 0; i < expected.length; i++) {
			assertTrue(msg, Math.abs(expected[i] - actual[i]) <= .0000001 * Math.abs(expected[i]));
		}
	}


	private double[][] ticks(double start, double end) {
		TimeTickMarkCalculator c = new TimeTickMarkCalculator();
		Axis axis = new Axis() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getTimeSystemAxisLabelName() {
				// TODO Auto-generated method stub
				return "GMT";
			}

			@Override
			public void setTimeSystemAxisLabelName(String labelName) {
				// TODO Auto-generated method stub
				
			}
		};
		axis.setStart(start);
		axis.setEnd(end);
		double[][] data = c.calculateTickMarks(axis);
		return data;
	}
}
