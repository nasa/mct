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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Step-Behind Cache is always "one step behind" 
 * the ground truth - when a read is requested, it returns 
 * the last version of the value it read immediately, 
 * then asynchronously looks up the next version of the 
 * value.
 * 
 * This is useful when:
 * - You want up-to-date values to be recognized at 
 *   some point, but not within any fixed time period.
 * - You must get a useful return value quickly, if 
 *   possible.
 * - You do not want to spend time polling in the 
 *   background.
 * 
 * In the special case where there is no prior 
 * lookup to fall back on, the Step-Behind Cache 
 * will trigger an explicit lookup (which may 
 * take a while)
 * 
 * @author vwoeltje
 *
 */
public class StepBehindCache<T> {
	private static final int THREAD_POOL_SIZE = 4;
	private AtomicReference<T> cache = 
			new AtomicReference<T>(null);
	private static final ExecutorService THREAD_POOL =
			Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	private Lookup<T> lookup;
	private long lastLookup = Long.MIN_VALUE;
	private long period = 1000L;

	/**
	 * Create a new step-behind cache wrapping the 
	 * specified lookup procedure, not to be executed 
	 * more often than the specified interval.
	 * @param lookup the actual lookup procedure
	 * @param delay the delay, in milliseconds, between lookups
	 */
	public StepBehindCache (Lookup<T> lookup, long delay) {
		this.lookup = lookup;
		this.period = delay;
	}
	
	/**
	 * Create a new step-behind cache wrapping the 
	 * specified lookup procedure.
	 * @param lookup the actual lookup procedure
	 */
	public StepBehindCache (Lookup<T> lookup) {
		this.lookup = lookup;
	}
	
	/**
	 * Get the currently cached value, and trigger a lookup 
	 * for the next value on a background thread, assuming 
	 * the current value is sufficiently out-dated (default 
	 * delay between lookups is one second, but this can be 
	 * overriden in the constructor.)
	 *
	 * The first call to this method will invoke the actual 
	 * lookup immediately, so it is not quite always expected 
	 * to return quickly.
	 *
	 * @return the last value looked up
	 */
	public T get() {
		T cached = cache.get();
		if (cached == null) {
			cached = lookup.lookup();
			cache.set(cached);
		} else {
			long now = System.currentTimeMillis();
			if (lastLookup <= now - period) {
				lastLookup = now;
				backgroundLookup();
			}
		}
		return cached;
	}
	
	private void backgroundLookup() {
		THREAD_POOL.submit(new Runnable() {
			public void run() {
				cache.set(lookup.lookup());
			}
		});
	}
	
	/**
	 * Refresh the cache. This clears the cache and 
	 * initiates a new lookup in the background. If 
	 * this lookup has not completed by the time the 
	 * next get() is called, that get() will issue a 
	 * new lookup and block until it has completed.
	 */
	public void refresh() {
		cache.set(null);
		backgroundLookup();
	}

	/**
	 * Describes a method for looking up a value. Typically 
	 * this lookup is not timely to perform (some delay 
	 * before the response is anticipated.)
	 *
	 * @param <S> the type of value to be looked up
	 */
	public static interface Lookup<S> {
		public S lookup();
	}
}
