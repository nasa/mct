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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ListenerManagerTest {

	private ListenerManager manager;
	
	@BeforeMethod
	public void init() {
		manager = new ListenerManager();
	}
	
	@Test
	public void testNotification() {
		Listener1 listener1 = new Listener1();
		manager.addListener(Listener1.class, listener1);
		
		assertEquals(listener1.getNotifyCount(), 0);
		manager.fireEvent(Listener1.class, new ListenerNotifier<Listener1> () {
			@Override
			public void notifyEvent(Listener1 listener) {
				listener.notifyEvent();
			}
		});
		assertEquals(listener1.getNotifyCount(), 1);
		
		// Removing should cause notification to cease.
		manager.removeListener(Listener1.class, listener1);
		
		assertEquals(listener1.getNotifyCount(), 1);
		manager.fireEvent(Listener1.class, new ListenerNotifier<Listener1> () {
			@Override
			public void notifyEvent(Listener1 listener) {
				listener.notifyEvent();
			}
		});
		assertEquals(listener1.getNotifyCount(), 1);
	}
	
	@Test
	public void testMultipleAdd() {
		Listener1 listener1 = new Listener1();
		manager.addListener(Listener1.class, listener1);

		// Adding twice should not generate multiple notifications.
		manager.addListener(Listener1.class, listener1);
		
		assertEquals(listener1.getNotifyCount(), 0);
		manager.fireEvent(Listener1.class, new ListenerNotifier<Listener1> () {
			@Override
			public void notifyEvent(Listener1 listener) {
				listener.notifyEvent();
			}
		});
		assertEquals(listener1.getNotifyCount(), 1);
	}
	
	@Test
	public void testMultipleListenerTypes() {
		Listener1 listener1 = new Listener1();
		manager.addListener(Listener1.class, listener1);

		Listener2 listener2 = new Listener2();
		manager.addListener(Listener2.class, listener2);
		
		assertEquals(listener1.getNotifyCount(), 0);
		assertEquals(listener2.getNotifyCount(), 0);
		manager.fireEvent(Listener1.class, new ListenerNotifier<Listener1> () {
			@Override
			public void notifyEvent(Listener1 listener) {
				listener.notifyEvent();
			}
		});
		assertEquals(listener1.getNotifyCount(), 1);
		assertEquals(listener2.getNotifyCount(), 0);

		manager.fireEvent(Listener2.class, new ListenerNotifier<Listener2> () {
			@Override
			public void notifyEvent(Listener2 listener) {
				listener.notifyEvent();
			}
		});
		assertEquals(listener1.getNotifyCount(), 1);
		assertEquals(listener2.getNotifyCount(), 1);
	}
	
	@Test
	public void testMultipleListeners() {
		Listener1 listenera = new Listener1();
		manager.addListener(Listener1.class, listenera);

		Listener1 listenerb = new Listener1();
		manager.addListener(Listener1.class, listenerb);
		
		assertEquals(listenera.getNotifyCount(), 0);
		assertEquals(listenerb.getNotifyCount(), 0);
		manager.fireEvent(Listener1.class, new ListenerNotifier<Listener1> () {
			@Override
			public void notifyEvent(Listener1 listener) {
				listener.notifyEvent();
			}
		});
		assertEquals(listenera.getNotifyCount(), 1);
		assertEquals(listenerb.getNotifyCount(), 1);
	}
	
	private static class MockListener implements EventListener {
	
		private int notifyCount = 0;
		
		public void notifyEvent() {
			++notifyCount;
		}
		
		public int getNotifyCount() {
			return notifyCount;
		}
		
	}
	
	private static class Listener1 extends MockListener {}
	private static class Listener2 extends MockListener {}
		
}
