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

import junit.framework.TestCase;

public class JUnitDoubleDataFloat extends TestCase {
	public void testGrow() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(1);
		assertEquals(2, data.getLength());
		data.add(2);
		assertEquals(3, data.getLength());
		data.add(3);
		assertEquals(4, data.getLength());
		data.add(4);
		assertEquals(5, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(1.0, data.get(1));
		assertEquals(2.0, data.get(2));
		assertEquals(3.0, data.get(3));
		assertEquals(4.0, data.get(4));
	}


	public void testCycle() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(1);
		assertEquals(2, data.getLength());
		data.add(2);
		assertEquals(3, data.getLength());
		data.add(3);
		assertEquals(4, data.getLength());
		data.removeFirst(1);
		assertEquals(3, data.getLength());
		data.add(4);
		assertEquals(4, data.getLength());
		assertEquals(1.0, data.get(0));
		assertEquals(2.0, data.get(1));
		assertEquals(3.0, data.get(2));
		assertEquals(4.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testAddMultiple() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(new double[] {2, 3, 4}, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(2.0, data.get(1));
		assertEquals(3.0, data.get(2));
		assertEquals(4.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testAddHuge() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		double[] d = new double[100];
		for(int i = 0; i < d.length; i++) {
			d[i] = i + 1;
		}
		data.add(d, 0, d.length);
		assertEquals(1 + d.length, data.getLength());
		for(int i = 0; i < d.length + 1; i++) {
			assertEquals((double) i, data.get(i));
		}
		assertEquals(128, data.getCapacity());
	}


	public void testAddOutOfRange() {
		DoubleData data = new DoubleDataFloat();
		double[] d = new double[10];

		try {
			data.add(d, -1, 1);
			fail("Should have thrown an IndexOutOfBoundsException");
		} catch(IndexOutOfBoundsException e) {
		}

		try {
			data.add(d, 10, 1);
			fail("Should have thrown an IndexOutOfBoundsException");
		} catch(IndexOutOfBoundsException e) {
		}

		try {
			data.add(d, 5, 6);
			fail("Should have thrown an IndexOutOfBoundsException");
		} catch(IndexOutOfBoundsException e) {
		}

		// should all work
		data.add(d, 0, 1);
		data.add(d, 9, 1);
		data.add(d, 5, 5);
	}


	public void testAddOutOfRangeFloat() {
		DoubleDataFloat data = new DoubleDataFloat();
		float[] d = new float[10];

		try {
			data.add(d, -1, 1);
			fail("Should have thrown an IndexOutOfBoundsException");
		} catch(IndexOutOfBoundsException e) {
		}

		try {
			data.add(d, 10, 1);
			fail("Should have thrown an IndexOutOfBoundsException");
		} catch(IndexOutOfBoundsException e) {
		}

		try {
			data.add(d, 5, 6);
			fail("Should have thrown an IndexOutOfBoundsException");
		} catch(IndexOutOfBoundsException e) {
		}

		// should all work
		data.add(d, 0, 1);
		data.add(d, 9, 1);
		data.add(d, 5, 5);
	}


	public void testAddDoubleData() {
		DoubleDataFloat data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		DoubleDataFloat data2 = new DoubleDataFloat();
		data2.add(new double[] { 2, 3, 4 }, 0, 3);
		data.add(data2, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(2.0, data.get(1));
		assertEquals(3.0, data.get(2));
		assertEquals(4.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testAddDoubleData2() {
		DoubleDataFloat data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		DoubleDataFloat data2 = new DoubleDataFloat();
		data2.add(new double[] { 2, 3, 4 }, 0, 3);
		data.add(data2, 1, 2);
		assertEquals(3, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(4, data.getCapacity());
	}


	public void testAddDoubleDataWithCycle() {
		DoubleDataFloat data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		DoubleDataFloat data2 = new DoubleDataFloat(4);
		data2.add(1);
		data2.add(2);
		data2.add(3);
		data2.removeFirst(2);
		data2.add(4);
		data2.add(5);
		assertEquals(3, data2.getLength());
		data.add(data2, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(5.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testAddDoubleDataOutOfRange() {
		DoubleDataFloat data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		DoubleDataFloat data2 = new DoubleDataFloat();
		data2.add(new double[] { 2, 3, 4 }, 0, 3);

		try {
			data.add(data2, 1, 3);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.add(data2, -1, 1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.add(data2, 1, -1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		// should work
		data.add(data2, 0, 3);
		data.add(data2, 1, 2);
	}


	public void testAddMultipleWithCycle() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(1);
		assertEquals(2, data.getLength());
		data.removeFirst(1);
		assertEquals(1, data.getLength());
		data.add(new double[] {2, 3, 4}, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(1.0, data.get(0));
		assertEquals(2.0, data.get(1));
		assertEquals(3.0, data.get(2));
		assertEquals(4.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testPrependMultiple() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.prepend(new double[] {2, 3, 4}, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(2.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(0.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testPrependMultipleFloat() {
		DoubleDataFloat data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.prepend(new float[] {2, 3, 4}, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(2.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(0.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testPrependMultipleWithCycle() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(1);
		assertEquals(2, data.getLength());
		data.removeFirst(1);
		assertEquals(1, data.getLength());
		data.prepend(new double[] {2, 3, 4}, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(2.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(1.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testPrependMultipleFloatWithCycle() {
		DoubleDataFloat data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(1);
		assertEquals(2, data.getLength());
		data.removeFirst(1);
		assertEquals(1, data.getLength());
		data.prepend(new float[] {2, 3, 4}, 0, 3);
		assertEquals(4, data.getLength());
		assertEquals(2.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(1.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testPrependMultipleWithCycle2() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		assertEquals(1, data.getLength());
		data.add(1);
		assertEquals(2, data.getLength());
		data.removeFirst(1);
		assertEquals(1, data.getLength());

		DoubleData data2 = new DoubleDataFloat(4);
		data2.add(0);
		data2.add(1);
		data2.add(2);
		data2.add(3);
		data2.removeFirst(2);
		data2.add(4);
		assertEquals(3, data2.getLength());
		assertEquals(4, data2.getCapacity());
		data.prepend(data2, 0, 3);

		assertEquals(4, data.getLength());
		assertEquals(2.0, data.get(0));
		assertEquals(3.0, data.get(1));
		assertEquals(4.0, data.get(2));
		assertEquals(1.0, data.get(3));
		assertEquals(4, data.getCapacity());
	}


	public void testPrependOutOfRange() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		double[] d = new double[] { 2, 3, 4 };

		try {
			data.prepend(d, -1, 1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(1, data.getLength());

		try {
			data.prepend(d, 0, -1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(1, data.getLength());

		try {
			data.prepend(d, 1, 3);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(1, data.getLength());

		// should work
		data.prepend(d, 1, 2);
	}


	public void testPrependDoubleDataOutOfRange() {
		DoubleData data = new DoubleDataFloat(4);
		data.add(0);
		DoubleData d = new DoubleDataFloat();
		d.add(2);
		d.add(3);
		d.add(4);

		try {
			data.prepend(d, -1, 1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(1, data.getLength());

		try {
			data.prepend(d, 0, -1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(1, data.getLength());

		try {
			data.prepend(d, 1, 3);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(1, data.getLength());

		// should work
		data.prepend(d, 1, 2);
	}


	public void testBinarySearch() {
		testBinarySearch(new double[] {}, .5, -1);

		testBinarySearch(new double[] {1}, .5, -1);
		testBinarySearch(new double[] {1}, 1, 0);
		testBinarySearch(new double[] {1}, 1.5, -2);

		testBinarySearch(new double[] {1, 2}, .5, -1);
		testBinarySearch(new double[] {1, 2}, 1, 0);
		testBinarySearch(new double[] {1, 2}, 1.5, -2);
		testBinarySearch(new double[] {1, 2}, 2, 1);
		testBinarySearch(new double[] {1, 2}, 2.5, -3);

		testBinarySearch(new double[] {1, 2, 3}, .5, -1);
		testBinarySearch(new double[] {1, 2, 3}, 1, 0);
		testBinarySearch(new double[] {1, 2, 3}, 1.5, -2);
		testBinarySearch(new double[] {1, 2, 3}, 2, 1);
		testBinarySearch(new double[] {1, 2, 3}, 2.5, -3);
		testBinarySearch(new double[] {1, 2, 3}, 3, 2);
		testBinarySearch(new double[] {1, 2, 3}, 3.5, -4);
	}


	private void testBinarySearch(double[] values, double d, int index) {
		DoubleData data = new DoubleDataFloat(4);
		data.add(values, 0, values.length);
		assertEquals(index, data.binarySearch(d));
	}


	public void testDictionarySearch() {
		testDictionarySearch(new double[] {}, .5, -1);

		testDictionarySearch(new double[] {1}, .5, -1);
		testDictionarySearch(new double[] {1}, 1, 0);
		testDictionarySearch(new double[] {1}, 1.5, -2);

		testDictionarySearch(new double[] {1, 2}, .5, -1);
		testDictionarySearch(new double[] {1, 2}, 1, 0);
		testDictionarySearch(new double[] {1, 2}, 1.5, -2);
		testDictionarySearch(new double[] {1, 2}, 2, 1);
		testDictionarySearch(new double[] {1, 2}, 2.5, -3);

		testDictionarySearch(new double[] {1, 2, 3}, .5, -1);
		testDictionarySearch(new double[] {1, 2, 3}, 1, 0);
		testDictionarySearch(new double[] {1, 2, 3}, 1.5, -2);
		testDictionarySearch(new double[] {1, 2, 3}, 2, 1);
		testDictionarySearch(new double[] {1, 2, 3}, 2.5, -3);
		testDictionarySearch(new double[] {1, 2, 3}, 3, 2);
		testDictionarySearch(new double[] {1, 2, 3}, 3.5, -4);

		testDictionarySearch(new double[] {1, 2, 3, 4}, .5, -1);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 1, 0);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 1.5, -2);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 2, 1);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 2.5, -3);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 3, 2);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 3.5, -4);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 4, 3);
		testDictionarySearch(new double[] {1, 2, 3, 4}, 4.5, -5);
	}


	private void testDictionarySearch(double[] values, double d, int index) {
		DoubleData data = new DoubleDataFloat(4);
		data.add(values, 0, values.length);
		assertEquals(index, data.dictionarySearch(d));
	}


	public void testInsertCloseToHead() {
		for(int pad = 0; pad < 8; pad++) {
			DoubleData data = new DoubleDataFloat(8);
			for(int i = 0; i < pad; i++) {
				data.add(-1);
			}
			data.removeFirst(pad);
			data.add(7);
			data.add(8);
			data.add(9);
			data.add(10);
			data.add(11);
			data.add(12);
			assertEquals(6, data.getLength());
			data.insert(2, 8.5);
			assertEquals(7, data.getLength());
			assertEquals(7.0, data.get(0));
			assertEquals(8.0, data.get(1));
			assertEquals(8.5, data.get(2));
			assertEquals(9.0, data.get(3));
			assertEquals(10.0, data.get(4));
			assertEquals(11.0, data.get(5));
			assertEquals(12.0, data.get(6));
		}
	}


	public void testInsertCloseToTail() {
		for(int pad = 0; pad < 8; pad++) {
			DoubleData data = new DoubleDataFloat(8);
			for(int i = 0; i < pad; i++) {
				data.add(-1);
			}
			data.removeFirst(pad);
			data.add(5);
			data.add(6);
			data.add(7);
			data.add(8);
			data.add(9);
			data.add(10);
			assertEquals(6, data.getLength());
			data.insert(5, 9.5);
			assertEquals(7, data.getLength());
			assertEquals(5.0, data.get(0));
			assertEquals(6.0, data.get(1));
			assertEquals(7.0, data.get(2));
			assertEquals(8.0, data.get(3));
			assertEquals(9.0, data.get(4));
			assertEquals(9.5, data.get(5));
			assertEquals(10.0, data.get(6));
		}
	}


	public void testInsertGrow() {
		DoubleData data = new DoubleDataFloat(4);
		for(int i = 0; i < 4; i++) {
			data.add(i);
		}
		data.insert(1, 5);
		assertEquals(5, data.getLength());
		assertEquals(8, data.getCapacity());
		assertEquals(0.0, data.get(0));
		assertEquals(5.0, data.get(1));
		assertEquals(1.0, data.get(2));
		assertEquals(2.0, data.get(3));
		assertEquals(3.0, data.get(4));
	}


	public void testInsertOutOfRange() {
		DoubleData data = new DoubleDataFloat(4);
		for(int i = 0; i < 4; i++) {
			data.add(i);
		}

		try {
			data.insert(-1, 0);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(4, data.getLength());

		try {
			data.insert(5, 0);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(4, data.getLength());

		// should work
		data.insert(4, 0);
	}


	public void testInsertDoubleData() {
		DoubleData data = new DoubleDataFloat(4);
		DoubleData data2 = new DoubleDataFloat(4);
		for(int i = 0; i < 4; i++) {
			data.add(i);
			data2.add(i + 4);
		}
		data.insert(1, data2, 1, 3);
		assertEquals(7, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(5.0, data.get(1));
		assertEquals(6.0, data.get(2));
		assertEquals(7.0, data.get(3));
		assertEquals(1.0, data.get(4));
		assertEquals(2.0, data.get(5));
		assertEquals(3.0, data.get(6));
	}


	public void testInsertDoubleDataNearTail() {
		DoubleData data = new DoubleDataFloat(4);
		DoubleData data2 = new DoubleDataFloat(4);
		for(int i = 0; i < 4; i++) {
			data.add(i);
			data2.add(i + 4);
		}
		data.insert(3, data2, 1, 3);
		assertEquals(7, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(1.0, data.get(1));
		assertEquals(2.0, data.get(2));
		assertEquals(5.0, data.get(3));
		assertEquals(6.0, data.get(4));
		assertEquals(7.0, data.get(5));
		assertEquals(3.0, data.get(6));
	}


	public void testInsertDoubleDataOutOfRange() {
		DoubleData data = new DoubleDataFloat(4);
		DoubleData data2 = new DoubleDataFloat(4);
		for(int i = 0; i < 4; i++) {
			data.add(i);
			data2.add(i + 4);
		}

		try {
			data.insert(-1, data2, 0, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(4, data.getLength());

		try {
			data.insert(0, data2, -1, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(4, data.getLength());

		try {
			data.insert(0, data2, 2, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(4, data.getLength());

		try {
			data.insert(5, data2, 0, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}
		assertEquals(4, data.getLength());

		// should work
		data.insert(4, data2, 0, 1);
	}


	public void testCopyFrom() {
		DoubleData data = new DoubleDataFloat(8);
		for(int i = 0; i < 4; i++) {
			data.add(i);
		}
		assertEquals(4, data.getLength());
		double[] d = new double[] {5, 6, 7, 8};
		data.copyFrom(d, 1, 1, 2);
		assertEquals(4, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(6.0, data.get(1));
		assertEquals(7.0, data.get(2));
		assertEquals(3.0, data.get(3));
	}


	public void testCopyFromFloat() {
		DoubleDataFloat data = new DoubleDataFloat(8);
		for(int i = 0; i < 4; i++) {
			data.add(i);
		}
		assertEquals(4, data.getLength());
		float[] d = new float[] {5, 6, 7, 8};
		data.copyFrom(d, 1, 1, 2);
		assertEquals(4, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(6.0, data.get(1));
		assertEquals(7.0, data.get(2));
		assertEquals(3.0, data.get(3));
	}


	public void testCopyFromWithCycle() {
		DoubleData data = new DoubleDataFloat(8);
		for(int i = 0; i < 8; i++) {
			data.add(i);
		}
		data.removeFirst(4);
		data.add(8);
		data.add(9);
		data.add(10);
		data.add(11);
		assertEquals(8, data.getLength());
		double[] d = new double[] {13, 14, 15, 16, 17, 18, 19, 20, 21};
		data.copyFrom(d, 2, 1, 5);
		assertEquals(8, data.getLength());
		assertEquals(4.0, data.get(0));
		assertEquals(15.0, data.get(1));
		assertEquals(16.0, data.get(2));
		assertEquals(17.0, data.get(3));
		assertEquals(18.0, data.get(4));
		assertEquals(19.0, data.get(5));
		assertEquals(10.0, data.get(6));
		assertEquals(11.0, data.get(7));
	}


	public void testCopyFromFloatWithCycle() {
		DoubleDataFloat data = new DoubleDataFloat(8);
		for(int i = 0; i < 8; i++) {
			data.add(i);
		}
		data.removeFirst(4);
		data.add(8);
		data.add(9);
		data.add(10);
		data.add(11);
		assertEquals(8, data.getLength());
		float[] d = new float[] {13, 14, 15, 16, 17, 18, 19, 20, 21};
		data.copyFrom(d, 2, 1, 5);
		assertEquals(8, data.getLength());
		assertEquals(4.0, data.get(0));
		assertEquals(15.0, data.get(1));
		assertEquals(16.0, data.get(2));
		assertEquals(17.0, data.get(3));
		assertEquals(18.0, data.get(4));
		assertEquals(19.0, data.get(5));
		assertEquals(10.0, data.get(6));
		assertEquals(11.0, data.get(7));
	}


	public void testCopyFromOutOfRange() {
		DoubleData data = new DoubleDataFloat(8);
		for(int i = 0; i < 4; i++) {
			data.add(i);
		}
		assertEquals(4, data.getLength());
		double[] d = new double[] { 5, 6, 7, 8 };

		try {
			data.copyFrom(d, 0, 0, -1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, -1, 0, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, 0, -1, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, 2, 0, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, 0, 2, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		// should work
		data.copyFrom(d, 2, 0, 2);
		data.copyFrom(d, 0, 2, 2);
		data.copyFrom(d, 0, 0, 1);
	}


	public void testCopyFromFloatOutOfRange() {
		DoubleDataFloat data = new DoubleDataFloat(8);
		for(int i = 0; i < 4; i++) {
			data.add(i);
		}
		assertEquals(4, data.getLength());
		float[] d = new float[] { 5, 6, 7, 8 };

		try {
			data.copyFrom(d, 0, 0, -1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, -1, 0, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, 0, -1, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, 2, 0, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(d, 0, 2, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		// should work
		data.copyFrom(d, 2, 0, 2);
		data.copyFrom(d, 0, 2, 2);
		data.copyFrom(d, 0, 0, 1);
	}


	private DoubleData init(int cap, int off, int len) {
		DoubleData data = new DoubleDataFloat(cap);
		for(int i = 0; i < off; i++) {
			data.add(0);
		}
		data.removeFirst(off);
		for(int i = 0; i < len; i++) {
			data.add(i);
		}
		return data;
	}


	private void assertRange(DoubleData data, int off, int len, int start) {
		for(int i = 0; i < len; i++) {
			assertEquals((double)start + i, data.get(off + i));
		}
	}


	// () = src
	// [] = dest
	// ' = offset
	// | = array ends

	// | ' [] () |
	public void testCopyFromSelf0() {
		DoubleData data = init(100, 0, 90);
		data.copyFrom(data, 70, 10, 5);
		assertRange(data, 0, 10, 0);
		assertRange(data, 10, 5, 70);
		assertRange(data, 15, 75, 15);
	}

	// | [] ' () |
	public void testCopyFromSelf1() {
		DoubleData data = init(100, 50, 90);
		data.copyFrom(data, 20, 60, 5);
		assertRange(data, 0, 60, 0);
		assertRange(data, 60, 5, 20);
		assertRange(data, 65, 15, 65);
	}

	// | [] () ' |
	public void testCopyFromSelf2() {
		DoubleData data = init(100, 90, 90);
		data.copyFrom(data, 70, 0, 5);
		assertRange(data, 0, 5, 70);
		assertRange(data, 5, 85, 5);
	}

	// | ' [ ( ] )  |
	public void testCopyFromSelf3() {
		DoubleData data = init(100, 1, 90);
		data.copyFrom(data, 12, 10, 5);
		assertRange(data, 0, 10, 0);
		assertRange(data, 10, 5, 12);
		assertRange(data, 15, 75, 15);
	}

	// | [ ( ] ) ' |
	public void testCopyFromSelf4() {
		DoubleData data = init(100, 90, 90);
		data.copyFrom(data, 22, 20, 5);
		assertRange(data, 0, 20, 0);
		assertRange(data, 20, 5, 22);
		assertRange(data, 25, 65, 25);
	}

	// | ' ( [ ) ] |
	public void testCopyFromSelf5() {
		DoubleData data = init(100, 1, 90);
		data.copyFrom(data, 20, 22, 5);
		assertRange(data, 0, 22, 0);
		assertRange(data, 22, 5, 20);
		assertRange(data, 27, 63, 27);
	}

	// | ( [ ) ] ' |
	public void testCopyFromSelf6() {
		DoubleData data = init(100, 90, 90);
		data.copyFrom(data, 20, 22, 5);
		assertRange(data, 0, 22, 0);
		assertRange(data, 22, 5, 20);
		assertRange(data, 27, 63, 27);
	}

	// | ' () [] |
	public void testCopyFromSelf7() {
		DoubleData data = init(100, 1, 90);
		data.copyFrom(data, 20, 40, 5);
		assertRange(data, 0, 40, 0);
		assertRange(data, 40, 5, 20);
		assertRange(data, 45, 45, 45);
	}

	// | () ' [] |
	public void testCopyFromSelf8() {
		DoubleData data = init(100, 50, 90);
		data.copyFrom(data, 70, 20, 5);
		assertRange(data, 0, 20, 0);
		assertRange(data, 20, 5, 70);
		assertRange(data, 25, 65, 25);
	}

	// | () [] ' |
	public void testCopyFromSelf9() {
		DoubleData data = init(100, 90, 90);
		data.copyFrom(data, 20, 70, 5);
		assertRange(data, 0, 70, 0);
		assertRange(data, 70, 5, 20);
		assertRange(data, 75, 15, 75);
	}

	// | ] ' () [ |
	public void testCopyFromSelf10() {
		DoubleData data = init(100, 30, 90);
		data.copyFrom(data, 10, 68, 5);
		assertRange(data, 0, 68, 0);
		assertRange(data, 68, 5, 10);
		assertRange(data, 73, 17, 73);
	}

	// | ] () ' [ |
	public void testCopyFromSelf11() {
		DoubleData data = init(100, 80, 90);
		data.copyFrom(data, 70, 18, 5);
		assertRange(data, 0, 18, 0);
		assertRange(data, 18, 5, 70);
		assertRange(data, 23, 67, 23);
	}

	// | ] ' ( [ ) |
	public void testCopyFromSelf12() {
		DoubleData data = init(100, 30, 90);
		data.copyFrom(data, 64, 67, 5);
		assertRange(data, 0, 67, 0);
		assertRange(data, 67, 5, 64);
		assertRange(data, 72, 18, 72);
	}

	// | ( ] ) ' [ |
	public void testCopyFromSelf13() {
		DoubleData data = init(100, 80, 90);
		data.copyFrom(data, 21, 18, 5);
		assertRange(data, 0, 18, 0);
		assertRange(data, 18, 5, 21);
		assertRange(data, 23, 67, 23);
	}

	// | [ ) ] ' ( |
	public void testCopyFromSelf14() {
		DoubleData data = init(100, 80, 90);
		data.copyFrom(data, 18, 21, 5);
		assertRange(data, 0, 21, 0);
		assertRange(data, 21, 5, 18);
		assertRange(data, 26, 64, 26);
	}

	// | ) ' [] ( |
	public void testCopyFromSelf15() {
		DoubleData data = init(100, 30, 90);
		data.copyFrom(data, 68, 10, 5);
		assertRange(data, 0, 10, 0);
		assertRange(data, 10, 5, 68);
		assertRange(data, 15, 75, 15);
	}

	// | ) [] ' ( |
	public void testCopyFromSelf16() {
		DoubleData data = init(100, 80, 90);
		data.copyFrom(data, 18, 70, 5);
		assertRange(data, 0, 70, 0);
		assertRange(data, 70, 5, 18);
		assertRange(data, 75, 15, 75);
	}

	// | ) ' [ ( ] |
	public void testCopyFromSelf17() {
		DoubleData data = init(100, 30, 90);
		data.copyFrom(data, 67, 64, 5);
		assertRange(data, 0, 64, 0);
		assertRange(data, 64, 5, 67);
		assertRange(data, 69, 11, 69);
	}

	// | ] ) ' [ ( |
	public void testCopyFromSelf18() {
		DoubleData data = init(100, 60, 90);
		data.copyFrom(data, 38, 37, 5);
		assertRange(data, 0, 37, 0);
		assertRange(data, 37, 5, 38);
		assertRange(data, 42, 48, 42);
	}

	// | ) ] ' ( [ |
	public void testCopyFromSelf19() {
		DoubleData data = init(100, 60, 90);
		data.copyFrom(data, 37, 38, 5);
		assertRange(data, 0, 38, 0);
		assertRange(data, 38, 5, 37);
		assertRange(data, 43, 47, 43);
	}

	public void testCopyFromDoubleData() {
		DoubleData data = new DoubleDataFloat(8);
		DoubleData data2 = new DoubleDataFloat(8);
		for(int i = 0; i < 8; i++) {
			data.add(i);
			data2.add(i + 8);
		}
		data.copyFrom(data2, 1, 2, 4);
		assertEquals(8, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(1.0, data.get(1));
		assertEquals(9.0, data.get(2));
		assertEquals(10.0, data.get(3));
		assertEquals(11.0, data.get(4));
		assertEquals(12.0, data.get(5));
		assertEquals(6.0, data.get(6));
		assertEquals(7.0, data.get(7));
	}


	public void testCopyFromDoubleDataWithCycle() {
		DoubleData data = new DoubleDataFloat(8);
		DoubleData data2 = new DoubleDataFloat(8);
		for(int i = 0; i < 8; i++) {
			data.add(i);
			data2.add(i + 8);
		}
		data2.removeFirst(4);
		for(int i = 0; i < 4; i++) {
			data2.add(i + 20);
		}
		data.copyFrom(data2, 1, 2, 4);
		assertEquals(8, data.getLength());
		assertEquals(0.0, data.get(0));
		assertEquals(1.0, data.get(1));
		assertEquals(13.0, data.get(2));
		assertEquals(14.0, data.get(3));
		assertEquals(15.0, data.get(4));
		assertEquals(20.0, data.get(5));
		assertEquals(6.0, data.get(6));
		assertEquals(7.0, data.get(7));
	}


	public void testCopyFromDoubleDataOutOfRange() {
		DoubleData data = new DoubleDataFloat(8);
		DoubleData data2 = new DoubleDataFloat(8);
		for(int i = 0; i < 8; i++) {
			data.add(i);
			data2.add(i + 8);
		}

		try {
			data.copyFrom(data2, -1, 0, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(data2, 0, -1, 1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(data2, 6, 0, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(data2, 0, 6, 3);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			data.copyFrom(data2, 0, 0, -1);
			fail("Should throw an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		// should work
		data.copyFrom(data2, 0, 0, 1);
		data.copyFrom(data2, 6, 0, 2);
		data.copyFrom(data2, 0, 6, 2);
	}


	public void testClone() {
		DoubleData d = init(8, 1, 3);
		DoubleData d2 = d.clone();
		assertRange(d2, 0, d2.getLength(), 0);
		d.set(0, 1);
		d.add(-1);
		assertRange(d2, 0, d2.getLength(), 0);
	}


	public void testGetOutOfRange() {
		DoubleData d = init(8, 1, 4);

		try {
			d.get(-1);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			d.get(4);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		// should work
		d.get(3);
	}


	public void testSetOutOfRange() {
		DoubleData d = init(8, 1, 4);

		try {
			d.set(-1, 0);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		try {
			d.set(4, 0);
			fail("Should have thrown an exception");
		} catch(IndexOutOfBoundsException e) {
			// should happen
		}

		// should work
		d.set(3, 0);
	}


	public void testSetCapacity() {
		DoubleData d = init(8, 1, 4);
		assertEquals(8, d.getCapacity());

		try {
			d.setCapacity(-1);
			fail("Should have thrown an exception");
		} catch(IllegalArgumentException e) {
			// should happen
		}

		// Shouldn't be able to shrink it less than the length
		try {
			d.setCapacity(3);
			fail("Should have thrown an exception");
		} catch(IllegalArgumentException e) {
			// should happen
		}

		// No change, should do nothing
		d.setCapacity(8);
		assertEquals(8, d.getCapacity());
		assertEquals(4, d.getLength());

		d.setCapacity(16);
		assertEquals(16, d.getCapacity());
		assertEquals(4, d.getLength());

		d.setCapacity(4);
		assertEquals(4, d.getCapacity());
		assertEquals(4, d.getLength());

		for(int i = 0; i < 4; i++) {
			assertEquals((double)i, d.get(i));
		}
	}


	public void testRemoveFirst() {
		DoubleData data = new DoubleDataFloat();
		data.add(0);
		data.add(1);
		data.add(2);

		data.removeFirst(1);
		assertEquals(2, data.getLength());
		assertEquals(1.0, data.get(0));

		try {
			data.removeFirst(-1);
			fail("Should have thrown an exception");
		} catch(IllegalArgumentException e) {
			// should happen
		}
		assertEquals(2, data.getLength());

		try {
			data.removeFirst(3);
			fail("Should have thrown an exception");
		} catch(IllegalArgumentException e) {
			// should happen
		}
		assertEquals(2, data.getLength());

		data.removeFirst(2);
		assertEquals(0, data.getLength());
	}


	public void testRemoveLast() {
		DoubleData data = new DoubleDataFloat();
		data.add(0);
		data.add(1);
		data.add(2);

		data.removeLast(1);
		assertEquals(2, data.getLength());
		assertEquals(0.0, data.get(0));

		try {
			data.removeLast(-1);
			fail("Should have thrown an exception");
		} catch(IllegalArgumentException e) {
			// should happen
		}
		assertEquals(2, data.getLength());

		try {
			data.removeLast(3);
			fail("Should have thrown an exception");
		} catch(IllegalArgumentException e) {
			// should happen
		}
		assertEquals(2, data.getLength());

		data.removeLast(2);
		assertEquals(0, data.getLength());
	}


	private String dump(DoubleData d) {
		StringBuffer b = new StringBuffer();
		b.append("{");
		int n = d.getLength();
		for(int i = 0; i < n; i++) {
			if(i > 0) {
				b.append(", ");
			}
			b.append(d.get(i));
		}
		b.append("}");
		return b.toString();
	}
}
