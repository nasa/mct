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
package gov.nasa.arc.mct.table.utils;

import java.util.ArrayList;

/**
 * Implements a list that doesn't throw {@link IndexOutOfBoundsException} when
 * getting or setting elements past the end of the list. Instead, on a get, a
 * null is returned whenever the index is out of bounds. On a set, nulls are
 * inserted to boost the list capacity before setting the new element.
 *
 * @param <T> the element type stored in the list
 */
public class NoSizeList<T> extends ArrayList<T> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public T get(int index) {
		if (index >= size()) {
			return null;
		} else {
			return super.get(index);
		}
	}

	@Override
	public T set(int index, T element) {
		while (index >= size()) {
			add(null);
		}
		
		// OK, index < size()
		return super.set(index, element);
	}
	
	/**
	 * Truncates the list to the indicated size. Elements from the end
	 * of the list are removed, in turn, until the size is equal or less
	 * than the desired size.
	 * 
	 * @param newSize the desired size of the list
	 */
	public void truncate(int newSize) {
		if (newSize >= 0) {
			while (size() > newSize) {
				remove(size() - 1);
			}
		}
	}

}
