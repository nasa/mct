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
package gov.nasa.arc.mct.subscribe.manager.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.mockito.MockitoAnnotations;
import org.osgi.service.cm.ConfigurationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationServiceTest {
	private ConfigurationService configService;
	@SuppressWarnings("unchecked")
	private Dictionary dict;
	
    @SuppressWarnings("unchecked")
	@BeforeMethod
    public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		dict = new Hashtable();
		configService = ConfigurationService.getInstance();
    }
    
    @SuppressWarnings("unchecked")
	@Test(expectedExceptions=ConfigurationException.class,dependsOnMethods="testDefaults")
    public void testUpdatedMissingGracePeriod() throws Exception {
       configService.updated(dict);
    }
    
    @SuppressWarnings("unchecked")
	@Test()
    public void testDefaults() throws Exception {
    	configService.updated(null);
    	Assert.assertEquals(1000,configService.getTimerSleepTime());
    	Assert.assertEquals(1000,configService.getUnSubscriptionGracePeriod());
    	
    	final int grace = 123;
    	dict.put(ConfigurationService.UNSUBSCRIPTION_GRACE_PERIOD, grace);
    	configService.updated(dict);
    	Assert.assertEquals(configService.getUnSubscriptionGracePeriod(), grace);
    }
}
