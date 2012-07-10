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
package gov.nasa.arc.mct.subscribe.manager;

import gov.nasa.arc.mct.event.services.EventProvider;
import gov.nasa.arc.mct.subscribe.manager.config.ConfigurationService;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SubscriptionManagerServiceTest {
	private SubscriptionManagerService subManager;
	
	@Mock
	private LogService logService;
	@Mock
	private ComponentContext compContext;
	@Mock
	private BundleContext bc;
	@Mock
	private ServiceReference providerSR;
	@Mock
	private EventProvider eventProvider;
	
	private String feedID = "testFeed";
	
	@BeforeMethod
	public void setup() throws ConfigurationException {
		ConfigurationService cs = ConfigurationService.getInstance();
		Dictionary<String, Integer> dict = new Hashtable<String, Integer>();
		dict.put(ConfigurationService.UNSUBSCRIPTION_GRACE_PERIOD, 0);
		cs.updated(dict);
		
		subManager = new SubscriptionManagerService();
		
		MockitoAnnotations.initMocks(this);
		subManager.unsetLogger(null);
		subManager.setLogger(logService);

		Mockito.when(compContext.getBundleContext()).thenReturn(bc);
		Mockito.when(eventProvider.subscribeTopics(feedID)).thenReturn(Collections.<String>emptyList());
	}
	
	/**
	 * Verify the subscription and unsubscriptions that make it to ISP work also
	 */
	@Test
	public void checkSubscriptionLifeCycleThroughService() throws Exception {
		final String feedID2 = "testFeedId2";
		subManager.activate(compContext);
		subManager.addProvider(eventProvider);
		Mockito.when(eventProvider.subscribeTopics(feedID)).thenReturn(Collections.singleton(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID));
		subManager.subscribe(feedID);
		Thread.sleep(100);
		Mockito.when(eventProvider.subscribeTopics(feedID2)).thenReturn(Collections.singleton(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID2));
		subManager.subscribe(feedID2);
		Mockito.verify(eventProvider, Mockito.times(1)).subscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
		Mockito.verify(eventProvider, Mockito.times(1)).subscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID2);

		// should be added to the list, now verify that this will get removed once the cycle has completed
		subManager.unsubscribe(feedID);
		subManager.unsubscribe(feedID2);
		// wait until the unsubscription timer event fires to verify that the unsubscription event was sent
		Thread.yield();
		Thread.sleep(1400);
		Mockito.verify(eventProvider, Mockito.times(1)).unsubscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
		Mockito.verify(eventProvider, Mockito.times(1)).unsubscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID2);
	}
	
	
	@Test
	public void addProviderTest() {
		subManager.subscribe(feedID);
		Mockito.verify(eventProvider, Mockito.never()).subscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
		
		subManager.activate(compContext);
		subManager.addProvider(eventProvider);
		
		Mockito.verify(eventProvider, Mockito.times(1)).subscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
		
		subManager.unsubscribe(feedID);
		Mockito.verify(eventProvider, Mockito.never()).unsubscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
	}
	
	@Test
	public void removeProviderTest() {
		subManager.addProvider(eventProvider);
		subManager.removeProvider(eventProvider);
		
		subManager.subscribe(feedID);
		Mockito.verify(eventProvider, Mockito.never()).subscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
		
		subManager.activate(compContext);
		Mockito.verify(eventProvider, Mockito.never()).subscribeTopics(EventProvider.TELEMETRY_TOPIC_PREFIX+feedID);
		
	}
	
}
