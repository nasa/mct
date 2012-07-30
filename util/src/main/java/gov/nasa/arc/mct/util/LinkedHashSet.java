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
package gov.nasa.arc.mct.util;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implements a linked hash set based upon an abstract set. 
 * @param <E> - entry.
 */
public class LinkedHashSet<E> extends AbstractSet<E> {
    private LinkedList<Reference<E>> orderedList = new LinkedList<Reference<E>>();
    private Map<E, Reference<E>> managedElements = new HashMap<E, Reference<E>>();
    
    /**
     * Default constructor.
     */
    public LinkedHashSet() {
        super();
    }
    
    /**
     * Constructor with set of entry.
     * @param entries - set of entry.
     */
    public LinkedHashSet(Set<E> entries) {
        for (E entry: entries) {
            offerLast(entry);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new SetIterator(orderedList.iterator());
    }

    @Override
    public synchronized int size() {
        return managedElements.size();
    }

    /**
     * Adds to the hash set.
     * @return boolean - flag.
     * @param e - Entry.
     */
    public synchronized boolean add(E e) {
        Reference<E> listElement = managedElements.remove(e);
        boolean hasData = listElement != null && listElement.get() != null;

        Reference<E> newElement = new Reference<E>(e);
        if (listElement != null) {
            listElement.clear();
        }
        orderedList.offerFirst(newElement);
        managedElements.put(e, newElement);

        return !hasData;
    }

    /**
     * Offers the last entry.
     * @param e - Entry.
     * @return boolean - flag.
     */
    public synchronized boolean offerLast(E e) {
        Reference<E> listElement = managedElements.remove(e);
        boolean hasData = listElement != null && listElement.get() != null;

        Reference<E> newElement = new Reference<E>(e);
        if (listElement != null) {
            listElement.clear();
        }
        orderedList.offerLast(newElement);
        managedElements.put(e, newElement);

        return !hasData;
    }

    @Override
    public synchronized boolean remove(Object e) {
        Reference<E> listElement = managedElements.remove(e);
        boolean hasData = listElement != null && listElement.get() != null;
        if (listElement != null) {
            listElement.clear();
        }
        return !hasData;
    }
    
    private static final class Reference<E> {
        private E referent;
        
        Reference(E element) {
            this.referent = element;
        }
        
        void clear() {
            this.referent = null;
        }
        
        E get() {
            return this.referent;
        }
    }

    private final class SetIterator implements Iterator<E> {
        private Iterator<Reference<E>> parentIterator;
        private E nextElement;

        public SetIterator(Iterator<Reference<E>> parentIterator) {
            this.parentIterator = parentIterator;
        }

        @Override
        public boolean hasNext() {
            if (nextElement != null) {
                return true;
            }

            boolean hasNext = false;
            while (!hasNext) {
                if (!parentIterator.hasNext()) {
                    break;
                }
                Reference<E> nextRef = parentIterator.next();
                nextElement = nextRef.get();
                if (nextElement != null) {
                    hasNext = true;
                } else {
                    remove();
                }
            }

            return hasNext;
        }

        @Override
        public E next() {
            if (nextElement != null) {
                E returnValue = nextElement;
                nextElement = null;
                return returnValue;
            } else if (hasNext()) {
                E returnValue = nextElement;
                nextElement = null;
                return returnValue;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            synchronized (LinkedHashSet.this) {
                parentIterator.remove();
            }
        }
    }

}
