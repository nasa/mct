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

import java.util.EventListener;

import javax.swing.event.EventListenerList;

/**
 * Implements a thread-safe manager of an event listener list. Classes
 * can use this. 
 */
public class ListenerManager {

	/** The listeners that have been added, possibly of multiple types. */
	private EventListenerList listeners = new EventListenerList();

	/**
	 * Add a listener of a specific class to the listener list.
	 * 
	 * @param <T> the listener type
	 * @param clazz the class of the listener
	 * @param l the listener to add
	 */
	public <T extends EventListener> void addListener(Class<T> clazz, T l) {
		synchronized (listeners) {
			// Remove the listener first, so we don't add it twice.
			listeners.remove(clazz, l);
			listeners.add(clazz, l);
		}
	}
	
	/**
	 * Remove a listener of a specific class from the listener list.
	 * 
	 * @param <T> the listener type
	 * @param clazz the class of the listener
	 * @param l the listener to remove
	 */
	public <T extends EventListener> void removeListener(Class<T> clazz, T l) {
		synchronized (listeners) {
			listeners.remove(clazz, l);
		}
	}

	/**
	 * Gets the listeners of a specified class.
	 * 
	 * @param <T> the class of the desired listeners
	 * @param clazz the class of the desired listeners
	 * @return an array of listeners of the desired class, which will be of zero length if there are no listeners
	 *   of the desired type
	 */
	public <T extends EventListener> T[] getListenersOfClass(Class<T> clazz) {
		synchronized (listeners) {
			return listeners.getListeners(clazz);
		}
	}

	/**
	 * Notifiers all listeners of a given class using the specified
	 * callback object.
	 * 
	 * @param <T> the type of the event listeners 
	 * @param clazz the class of the event listeners
	 * @param notifier the notifier class that will call the event method in the listener interface
	 */
	public <T extends EventListener> void fireEvent(Class<T> clazz, ListenerNotifier<T> notifier) {
		for (T listener : getListenersOfClass(clazz)) {
			notifier.notifyEvent(listener);
		}
	}
	
}
