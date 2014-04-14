/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
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
package gov.nasa.arc.mct.dbpersistence.service;

import gov.nasa.arc.mct.dbpersistence.service.StepBehindCache.Lookup;

import java.util.concurrent.atomic.AtomicLong;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestStepBehindCache {
	// Magic number to check for in refresh tests
	private static final long MAGIC = 0xBee; 
	
	// Value to count up during successive lookups
	private AtomicLong value = new AtomicLong(0L);
	
	@BeforeMethod
	public void setup() {
		// Reset the counter
		value.set(0L);
	}
	
	@Test (timeOut = 5000)
	public void testGet() {
		// Create an a counter to test with
		Lookup<Long> counter = new Lookup<Long>() {
			public synchronized Long lookup() {
				return value.getAndIncrement();
			}
		};
		
		// Create a step-behind cache to check
		StepBehindCache<Long> cache = new StepBehindCache<Long>(counter, 50L);
		
		// Should return 0 the first time
		Assert.assertEquals(cache.get().longValue(), 0L);
		
		// Should thereafter be one step behind
		for (long v = 0; v < 10; v++) {
			// Should eventually catch up
			// Test will time out otherwise
			while (cache.get().longValue() != v) {}
			// Actual counter should be exactly one step ahead
			while (value.get() != v + 1) {}
		}
	}

	@Test
	public void testRefresh() {
		// Create an a plain lookup to test with
		Lookup<Long> counter = new Lookup<Long>() {
			public synchronized Long lookup() {
				return value.get();
			}
		};
		
		// Create a step-behind cache to check
		StepBehindCache<Long> cache = new StepBehindCache<Long>(counter, 0L);
		
		// Should return 0 the first time
		Assert.assertEquals(cache.get().longValue(), 0L);
		
		// Simulate an external change
		value.set(MAGIC);
		
		// Refresh, verify that cache is up-to-date
		cache.refresh();
		Assert.assertEquals(cache.get().longValue(), MAGIC);
	}

	@Test
	public void testWithoutRefresh() {
		// Create an a plain lookup to test with
		Lookup<Long> counter = new Lookup<Long>() {
			public synchronized Long lookup() {
				return value.get();
			}
		};
		
		// Create a step-behind cache to check
		StepBehindCache<Long> cache = new StepBehindCache<Long>(counter, 0L);
		
		// Should return 0 the first time
		Assert.assertEquals(cache.get().longValue(), 0L);
		
		// Simulate an external change
		value.set(MAGIC);
		
		// Don't refresh, verify that cache is not up-to-date	
		Assert.assertEquals(cache.get().longValue(), 0L);
	}

}
