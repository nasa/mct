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
 * WeakHashSet.java Aug 29, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.util;

import gov.nasa.arc.mct.util.logging.MCTLogger;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class uses {@link java.lang.ref.WeakReference} so that the contained
 * objects could be garbage collected.
 * 
 * @author asi
 * 
 * @param <T> WeakHashSet
 */
public class WeakHashSet<T> extends AbstractSet<T> {
	private static final long serialVersionUID = -7806849276006714321L;

	private static MCTLogger logger = MCTLogger.getLogger(WeakHashSet.class);

	private final Map<Integer, WeakEntry<T>> map;
	private final ReferenceQueue<T> queue = new ReferenceQueue<T>();

	/**
	 * Constructor initialization.
	 */
	public WeakHashSet() {
		super();
		map = new HashMap<Integer, WeakEntry<T>>();
	}

	/**
	 * Constructor with set.
	 * @param set - the set.
	 */
	public WeakHashSet(Set<T> set) {
		map = new HashMap<Integer, WeakEntry<T>>();
		if (set instanceof WeakHashSet<?>) {
			((WeakHashSet<T>) set).cleanQueue();
		}
		for (T t : set) {
			if (t == null) {
				continue;
			}
			map.put(t.hashCode(), new WeakEntry<T>(t, queue));
		}
	}

	/**
	 * Cleans the queue.
	 */
	@SuppressWarnings("unchecked")
	protected void cleanQueue() {
		WeakEntry<T> wt;
		while ((wt = (WeakEntry<T>) queue.poll()) != null) {
			int hash = wt.hashCode;
			logger.debug("Garbage collecting: {0}", wt.idString);
			map.remove(hash);
		}
	}

	/**
	 * Iterates the weak hashset.
	 * @return Iterator
	 */
	public Iterator<T> iterator() {
		cleanQueue();
		return new WeakHashSetIterator(map.values().iterator());
	}

	/**
	 * Returns the size of the weak hash set.
	 * @return the size - number.
	 */
	public int size() {
		cleanQueue();
		return map.size();
	}

	/**
	 * Checks whether the weak hash set is empty or not.
	 * @return boolean - empty or not.
	 */
	public boolean isEmpty() {
		cleanQueue();
		return map.isEmpty();
	}

	/**
	 * Checks whether an object is contained in the hash set.
	 * @param o - the object to check on.
	 * @return boolean - whether the object is contained on the hash set.
	 */
	public boolean contains(Object o) {
		cleanQueue();
		return map.containsKey(o.hashCode());
	}

	/**
	 * Adds the designated object to the hash set.
	 * @param o - the object.
	 * @return boolean - returns true if added; otherwise false.
	 */
	public boolean add(T o) {
		cleanQueue();
		if (o == null) { return false; }
		return map.put(o.hashCode(), new WeakEntry<T>(o, queue)) == null;
	}

	/**
	 * Removes the designated object from the hash set.
	 * @param o - the object to remove.
	 * @return true if removed; otherwise false.
	 */
	public boolean remove(Object o) {
		return map.remove(o.hashCode()) != null;
	}

	/**
	 * Clears the hash set.
	 */
	public void clear() {
		cleanQueue();
		map.clear();
	}

	private final class WeakHashSetIterator implements Iterator<T> {
		private Iterator<WeakEntry<T>> parentIterator;
		private T nextElement;

		public WeakHashSetIterator(Iterator<WeakEntry<T>> parentIterator) {
			this.parentIterator = parentIterator;
		}

		@Override
		public boolean hasNext() {
			if (nextElement != null) { return true; }
			
			boolean hasNext = false;
			while (!hasNext) {
				if (!parentIterator.hasNext()) {
					break;
				}
				WeakReference<T> nextRef = parentIterator.next();
				nextElement = nextRef.get();
				if (nextElement != null) {
					hasNext = true;
				} else {
					parentIterator.remove();
				}
			}

			return hasNext;
		}

		@Override
		public T next() {
			if (nextElement != null) {
				T returnValue = nextElement;
				nextElement = null;
				return returnValue;
			} else if (hasNext()) {
				T returnValue = nextElement;
				nextElement = null;
				return returnValue;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			parentIterator.remove();
		}
	}

	private static class WeakEntry<T> extends
			WeakReference<T> {
		private final int hashCode;
		private final String idString;

		public WeakEntry(T t, ReferenceQueue<T> queue) {
			super(t, queue);
			this.hashCode = t.hashCode();
			this.idString = t.toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof WeakEntry)) {
				return false;
			}
			return (this.hashCode == ((WeakEntry<T>) obj).hashCode);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}

}
