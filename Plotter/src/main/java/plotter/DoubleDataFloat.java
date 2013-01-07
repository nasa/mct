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
 * Stores floats in a double-ended circular buffer.
 * @author Adam Crume
 */
public class DoubleDataFloat extends DoubleData {
	/** Default initial capacity. */
	private static final int DEFAULT_CAPACITY = 8;

	/** Contains the data. */
	private float[] data;


	/**
	 * Creates a buffer with the specified capacity.
	 * @param capacity capacity of the buffer
	 */
	public DoubleDataFloat(int capacity) {
		data = new float[capacity];
	}


	/**
	 * Creates a buffer with default capacity.
	 */
	public DoubleDataFloat() {
		this(DEFAULT_CAPACITY);
	}


	/**
	 * Adds an element to the buffer.
	 * @param d element to add
	 */
	public void add(double d) {
		if(length == data.length) {
			// If we don't have enough space, allocate a larger array
			setCapacity(data.length * 2);
		}
		data[wrap(offset + length, data.length)] = (float)d;
		length++;
	}


	/**
	 * Adds elements to the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to copy
	 */
	public void add(double[] d, int off, int len) {
		if(off < 0 || len < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("d.length = " + d.length + ", off = " + off + ", len = " + len);
		}
		if(length + len > data.length) {
			// If we don't have enough space, allocate a larger array
			int newlen = data.length;
			while(newlen < length + len) {
				newlen *= 2;
			}
			setCapacity(newlen);
		}
		int start1 = wrap(offset + length, data.length);
		int end1 = Math.min(start1 + len, data.length);
		arraycopy(d, off, data, start1, end1 - start1);
		if(end1 - start1 < len) {
			arraycopy(d, off + end1 - start1, data, 0, len - end1 + start1);
		}
		length += len;
	}


	private void arraycopy(double[] src, int srcPos, float[] dest, int destPos, int length) {
		int i = destPos;
		int j = srcPos;
		int max = destPos + length;
		while(i < max) {
			dest[i++] = (float) src[j++];
		}
	}


	/**
	 * Adds elements to the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to copy
	 */
	public void add(float[] d, int off, int len) {
		if(off < 0 || len < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("d.length = " + d.length + ", off = " + off + ", len = " + len);
		}
		if(length + len > data.length) {
			// If we don't have enough space, allocate a larger array
			int newlen = data.length;
			while(newlen < length + len) {
				newlen *= 2;
			}
			setCapacity(newlen);
		}
		int start1 = wrap(offset + length, data.length);
		int end1 = Math.min(start1 + len, data.length);
		System.arraycopy(d, off, data, start1, end1 - start1);
		if(end1 - start1 < len) {
			System.arraycopy(d, off + end1 - start1, data, 0, len - end1 + start1);
		}
		length += len;
	}


	/**
	 * Adds elements to the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to copy
	 */
	public void add(DoubleData d, int off, int len) {
		if(off < 0 || len < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("d.getLength() = " + d.length + ", off = " + off + ", len = " + len);
		}
		DoubleDataFloat d2 = (DoubleDataFloat) d; // TODO: What if it is a different type?
		int off2 = wrap(d.offset + off, d2.data.length);
		int available = d2.data.length - off2;
		if(available < len) {
			add(d2.data, off2, available);
			add(d2.data, 0, len - available);
		} else {
			add(d2.data, off2, len);
		}
	}


	/**
	 * Copies data from the source object.
	 * @param src object to copy data from
	 * @param srcoff index within src to copy data from
	 * @param dstoff index within this to copy data to
	 * @param len number of elements to copy
	 */
	public void copyFrom(DoubleData src, int srcoff, int dstoff, int len) {
		if(srcoff < 0 || len < 0 || srcoff + len > src.length) {
			throw new IndexOutOfBoundsException("src.getLength() = " + src.length + ", srcoff = " + srcoff + ", len = " + len);
		}
		if(dstoff < 0 || dstoff + len > length) {
			throw new IndexOutOfBoundsException("dstoff = " + dstoff + ", len = " + len + ", getLength() = " + length);
		}
		DoubleDataFloat src2 = (DoubleDataFloat) src; // TODO: What if it is a different type?
		int off2 = wrap(src.offset + srcoff, src2.data.length);
		int available = src2.data.length - off2;
		if(available < len) {
			// Which order depends only if src == data.
			int dstoff2 = wrap(dstoff + offset, data.length);
			if(dstoff2 < off2 && dstoff2 > wrap(src.offset + srcoff + len, src2.data.length)) {
				copyFrom(src2.data, off2, dstoff, available);
				copyFrom(src2.data, 0, dstoff + available, len - available);
			} else {
				copyFrom(src2.data, 0, dstoff + available, len - available);
				copyFrom(src2.data, off2, dstoff, available);
			}
		} else {
			copyFrom(src2.data, off2, dstoff, len);
		}
	}


	/**
	 * Copies data from the source object.
	 * @param src object to copy data from
	 * @param srcoff index within src to copy data from
	 * @param dstoff index within this to copy data to
	 * @param len number of elements to copy
	 */
	public void copyFrom(double[] src, int srcoff, int dstoff, int len) {
		if(srcoff < 0 || len < 0 || srcoff + len > src.length) {
			throw new IndexOutOfBoundsException("d.length = " + src.length + ", srcoff = " + srcoff + ", len = " + len);
		}
		if(dstoff < 0 || dstoff + len > length) {
			throw new IndexOutOfBoundsException("dstoff = " + dstoff + ", len = " + len + ", getLength() = " + length);
		}
		int off2 = wrap(offset + dstoff, data.length);
		int available = data.length - off2;
		if(available < len) {
			// Which order depends only if src == data.
			if(off2 < srcoff + len) {
				arraycopy(src, srcoff + available, data, 0, len - available);
				arraycopy(src, srcoff, data, off2, available);
			} else {
				arraycopy(src, srcoff, data, off2, available);
				arraycopy(src, srcoff + available, data, 0, len - available);
			}
		} else {
			arraycopy(src, srcoff, data, off2, len);
		}
	}


	/**
	 * Copies data from the source object.
	 * @param src object to copy data from
	 * @param srcoff index within src to copy data from
	 * @param dstoff index within this to copy data to
	 * @param len number of elements to copy
	 */
	public void copyFrom(float[] src, int srcoff, int dstoff, int len) {
		if(srcoff < 0 || len < 0 || srcoff + len > src.length) {
			throw new IndexOutOfBoundsException("d.length = " + src.length + ", srcoff = " + srcoff + ", len = " + len);
		}
		if(dstoff < 0 || dstoff + len > length) {
			throw new IndexOutOfBoundsException("dstoff = " + dstoff + ", len = " + len + ", getLength() = " + length);
		}
		int off2 = wrap(offset + dstoff, data.length);
		int available = data.length - off2;
		if(available < len) {
			// Which order depends only if src == data.
			if(off2 < srcoff + len) {
				System.arraycopy(src, srcoff + available, data, 0, len - available);
				System.arraycopy(src, srcoff, data, off2, available);
			} else {
				System.arraycopy(src, srcoff, data, off2, available);
				System.arraycopy(src, srcoff + available, data, 0, len - available);
			}
		} else {
			System.arraycopy(src, srcoff, data, off2, len);
		}
	}


	/**
	 * Inserts a value into the buffer.
	 * @param index position for the new value
	 * @param d value to add
	 */
	public void insert(int index, double d) {
		if(index < 0 || index > length) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", length is " + length);
		}
		if(length == data.length) {
			setCapacity(data.length * 2);
		}

		// TODO: See if this mess can be simplified
		int split = data.length - offset;
		if(index < length / 2) {
			// Insert near head; shift early elements left
			if(index <= split) {
				if(offset == 0) {
					data[data.length - 1] = data[0];
					System.arraycopy(data, offset + 1, data, offset, index);
				} else {
					System.arraycopy(data, offset, data, offset - 1, index);
				}
			} else {
				System.arraycopy(data, offset, data, offset - 1, split);
				data[data.length - 1] = data[0];
				System.arraycopy(data, 1, data, 0, index - split - 1);
			}
			offset--;
			while(offset < 0) {
				offset += data.length;
			}
		} else {
			// Insert near tail; shift late elements right
			if(index < split) {
				if(split <= length) {
					System.arraycopy(data, 0, data, 1, length - split);
					data[0] = data[data.length - 1];
					System.arraycopy(data, index + offset, data, index + offset + 1, split - index - 1);
				} else {
					System.arraycopy(data, index + offset, data, index + offset + 1, length - index);
				}
			} else {
				System.arraycopy(data, index - split, data, index - split + 1, length - index);
			}
		}
		length++;
		data[wrap(offset + index, data.length)] = (float)d;
	}


	/**
	 * Inserts elements into the buffer.
	 * @param index position for the new value
	 * @param d data to add
	 * @param off offset within d to start copying data from
	 * @param len number of elements to insert
	 */
	public void insert(int index, DoubleData d, int off, int len) {
		if(index < 0 || index > length) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", length is " + length);
		}
		if(off < 0 || len < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("Index out of bounds: off = " + off + ", len = " + len + ", d.length = " + d.length);
		}
		int newlen = length + len;
		int cap = data.length;
		while(newlen > cap) {
			cap *= 2;
		}
		if(cap != data.length) {
			setCapacity(cap);
		}

		if(index < length / 2) {
			// Insert near head; shift early elements left
			length += len;
			offset -= len; // implicitly shifts elements right
			while(offset < 0) {
				offset += data.length;
			}
			copyFrom(this, len, 0, index);
		} else {
			// Insert near tail; shift late elements right
			length += len;
			copyFrom(this, index, index + len, getLength() - index - len);
		}
		copyFrom(d, off, index, len);
	}


	/**
	 * Adds elements to the beginning of the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to add
	 */
	public void prepend(double[] d, int off, int len) {
		if(len < 0 || off < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("d.length = " + d.length + ", off = " + off + ", len = " + len);
		}
		if(length + len > data.length) {
			int newlen = data.length;
			while(newlen < length + len) {
				newlen *= 2;
			}
			setCapacity(newlen);
		}
		int start1 = offset - len;
		while(start1 < 0) {
			start1 += data.length;
		}
		int end1 = Math.min(start1 + len, data.length);
		arraycopy(d, off, data, start1, end1 - start1);
		if(end1 - start1 < len) {
			arraycopy(d, off + end1 - start1, data, 0, len - end1 + start1);
		}
		length += len;
		offset = start1;
	}


	/**
	 * Adds elements to the beginning of the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to add
	 */
	public void prepend(float[] d, int off, int len) {
		if(len < 0 || off < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("d.length = " + d.length + ", off = " + off + ", len = " + len);
		}
		if(length + len > data.length) {
			int newlen = data.length;
			while(newlen < length + len) {
				newlen *= 2;
			}
			setCapacity(newlen);
		}
		int start1 = offset - len;
		while(start1 < 0) {
			start1 += data.length;
		}
		int end1 = Math.min(start1 + len, data.length);
		System.arraycopy(d, off, data, start1, end1 - start1);
		if(end1 - start1 < len) {
			System.arraycopy(d, off + end1 - start1, data, 0, len - end1 + start1);
		}
		length += len;
		offset = start1;
	}


	/**
	 * Adds elements to the beginning of the buffer.
	 * @param d data to add
	 * @param off offset within <code>d</code> to start copying from
	 * @param len number of elements to add
	 */
	public void prepend(DoubleData d, int off, int len) {
		if(len < 0 || off < 0 || off + len > d.length) {
			throw new IndexOutOfBoundsException("d.getLength() = " + d.length + ", off = " + off + ", len = " + len);
		}
		DoubleDataFloat d2 = (DoubleDataFloat) d; // TODO: What if it is a different type?
		int off2 = wrap(d.offset + off, d2.data.length);
		int available = d2.data.length - off2;
		if(available < len) {
			prepend(d2.data, 0, len - available);
			prepend(d2.data, off2, available);
		} else {
			prepend(d2.data, off2, len);
		}
	}


	/**
	 * Returns the element at the given index.
	 * @param index index of the element
	 * @return value at that index
	 */
	public double get(int index) {
		if(index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", length is " + length);
		}
		return data[wrap(offset + index, data.length)];
	}


	/**
	 * Sets the element at the given index
	 * @param index index of the element
	 * @param d value to set at that index
	 */
	public void set(int index, double d) {
		if(index < 0 || index >= length) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index + ", length is " + length);
		}
		data[wrap(offset + index, data.length)] = (float)d;
	}


	/**
	 * Returns the capacity, or the maximum length the buffer can grow to without resizing.
	 * @return the capacity
	 */
	public int getCapacity() {
		return data.length;
	}


	/**
	 * Sets the buffer's capacity.
	 * The new capacity cannot be less than the amount of data currently in the buffer.
	 * @param capacity new capacity
	 * @throws IllegalArgumentException if the requested capacity is less than the length
	 */
	public void setCapacity(int capacity) {
		if(capacity < length) {
			throw new IllegalArgumentException("Cannot set capacity less than the current length.  Remove elements first.  length = " + length
					+ ", new capacity = " + capacity);
		}
		if(capacity == data.length) {
			return;
		}

		float[] data2 = new float[capacity];
		int available = data.length - offset;
		if(length <= available) {
			System.arraycopy(data, offset, data2, 0, length);
		} else {
			System.arraycopy(data, offset, data2, 0, available);
			System.arraycopy(data, 0, data2, available, length - available);
		}

		data = data2;
		offset = 0;
	}


	/**
	 * Removes elements from the front of the buffer.
	 * @param count number of elements to remove
	 */
	public void removeFirst(int count) {
		if(count < 0) {
			throw new IllegalArgumentException("Count cannot be negative: " + count);
		}
		if(count > length) {
			throw new IllegalArgumentException("Trying to remove " + count + " elements, but only contains " + length);
		}
		offset = wrap(offset + count, data.length);
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
	public int binarySearch(double d) {
		if(length == 0) {
			return -1;
		}
		float f = (float) d;
		int min = 0;
		int max = length;
		while(max - min > 1) {
			int mid = (min + max) / 2;
			float x = data[wrap(offset + mid, data.length)];
			if(x < f) {
				min = mid;
			} else if(x > f) {
				max = mid;
			} else { // assume x==f
				return mid;
			}
		}
		float x = data[wrap(offset + min, data.length)];
		if(x == f) {
			return min;
		} else if(x < f) {
			return -min - 2;
		} else {
			return -min - 1;
		}
	}


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
	public int dictionarySearch(double d) {
		if(length == 0) {
			return -1;
		}

		int min = 0;
		int max = length - 1;
		while(true) {
			double minval = get(min);
			if(minval > d) {
				return -min - 1;
			}
			double maxval = get(max);
			if(maxval < d) {
				return -max - 2;
			}
			int mid = min + (int) ((d - minval) * (max - min) / (maxval - minval));
			double midval = get(mid);
			if(midval < d) {
				min = mid + 1;
			} else if(midval > d) {
				max = mid - 1;
			} else {
				return mid;
			}
		}
	}


	@Override
	public DoubleDataFloat clone() {
		DoubleDataFloat d = (DoubleDataFloat) super.clone();
		d.data = data.clone();
		return d;
	}
}
