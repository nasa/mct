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

/**
 * Stores doubles in a double-ended circular buffer.
 * @author Adam Crume
 */
public abstract class DoubleData implements Cloneable {
	/** Default initial capacity. */
	protected static final int DEFAULT_CAPACITY = 8;

	/** Offset within 'data' where usable data starts. */
	protected int offset;

	/** Number of elements in the buffer. */
	protected int length;


	/**
	 * Returns the number of elements in the buffer.
	 * @return the number of elements in the buffer.
	 */
	public int getLength() {
		return length;
	}


	/**
	 * Adds an element to the buffer.
	 * @param d element to add
	 */
	public abstract void add(double d);


	/**
	 * Adds elements to the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to copy
	 */
	public abstract void add(double[] d, int off, int len);


	/**
	 * Adds elements to the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to copy
	 */
	public abstract void add(DoubleData d, int off, int len);


	/**
	 * Copies data from the source object.
	 * @param src object to copy data from
	 * @param srcoff index within src to copy data from
	 * @param dstoff index within this to copy data to
	 * @param len number of elements to copy
	 */
	public abstract void copyFrom(DoubleData src, int srcoff, int dstoff, int len);


	/**
	 * Copies data from the source object.
	 * @param src object to copy data from
	 * @param srcoff index within src to copy data from
	 * @param dstoff index within this to copy data to
	 * @param len number of elements to copy
	 */
	public abstract void copyFrom(double[] src, int srcoff, int dstoff, int len);


	/**
	 * Inserts a value into the buffer.
	 * @param index position for the new value
	 * @param d value to add
	 */
	public abstract void insert(int index, double d);


	/**
	 * Inserts elements into the buffer.
	 * @param index position for the new value
	 * @param d data to add
	 * @param off offset within d to start copying data from
	 * @param len number of elements to insert
	 */
	public abstract void insert(int index, DoubleData d, int off, int len);


	/**
	 * Adds elements to the beginning of the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to add
	 */
	public abstract void prepend(double[] d, int off, int len);


	/**
	 * Adds elements to the beginning of the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to add
	 */
	public abstract void prepend(DoubleData d, int off, int len);


	/**
	 * Returns the element at the given index.
	 * @param index index of the element
	 * @return value at that index
	 */
	public abstract double get(int index);


	/**
	 * Sets the element at the given index
	 * @param index index of the element
	 * @param d value to set at that index
	 */
	public abstract void set(int index, double d);


	/**
	 * Returns the capacity, or the maximum length the buffer can grow to without resizing.
	 * @return the capacity
	 */
	public abstract int getCapacity();


	/**
	 * Sets the buffer's capacity.
	 * The new capacity cannot be less than the amount of data currently in the buffer.
	 * @param capacity new capacity
	 * @throws IllegalArgumentException if the requested capacity is less than the length
	 */
	public abstract void setCapacity(int capacity);


	/**
	 * Removes elements from the front of the buffer.
	 * @param count number of elements to remove
	 */
	public abstract void removeFirst(int count);


	/**
	 * Removes elements from the end of the buffer.
	 * @param count number of elements to remove
	 */
	public void removeLast(int count) {
		if(count < 0) {
			throw new IllegalArgumentException("Count cannot be negative: " + count);
		}
		if(count > length) {
			throw new IllegalArgumentException("Trying to remove " + count + " elements, but only contains " + length);
		}
		length -= count;
	}


	/**
	 * Searches the data for an insertion point.
	 * Assumes the data is sorted.
	 * Assumes the data does not contain NaNs and that the argument is not NaN.
	 * Runs in O(log(n)) time, where n is the length (as defined by {@link #getLength()}).
	 * @param d value to search for
	 * @return index of the search key, if it is contained in the array; otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
	 * The <i>insertion point</i> is defined as the point at which the key would be inserted into the array:
	 * the index of the first element greater than the key, or <tt>a.length</tt> if all elements in the array are less than the specified key.
	 * Note that this guarantees that the return value will be &gt;= 0 if and only if the key is found.
	 */
	public abstract int binarySearch(double d);


	/**
	 * Searches the data for an insertion point.
	 * Assumes the data is sorted.
	 * Assumes the data does not contain NaNs and that the argument is not NaN.
	 * Runs on average in O(log(log(n))) time, where n is the length (as defined by {@link #getLength()}).
	 * However, in the worst case (where the values are exponentially distributed), may run in O(n) time.
	 * @param d value to search for
	 * @return index of the search key, if it is contained in the array; otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.
	 * The <i>insertion point</i> is defined as the point at which the key would be inserted into the array:
	 * the index of the first element greater than the key, or <tt>a.length</tt> if all elements in the array are less than the specified key.
	 * Note that this guarantees that the return value will be &gt;= 0 if and only if the key is found.
	 */
	public abstract int dictionarySearch(double d);


	/**
	 * Removes everything from the buffer.
	 */
	public void removeAll() {
		length = 0;
	}


	@Override
	public DoubleData clone() {
		try {
			return (DoubleData) super.clone();
		} catch(CloneNotSupportedException e) {
			throw new RuntimeException(e); // should never happen
		}
	}


	protected static int wrap(int x, int len) {
        while(x >= len) {
            x -= len;
        }
        return x;
    }
}
