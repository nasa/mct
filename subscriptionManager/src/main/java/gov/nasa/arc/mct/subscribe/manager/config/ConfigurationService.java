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

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * This class is a service used to retrieve configuration values for Subscription Manager. The configuration
 * will be read from the services.xml file. 
 * 
 */
public class ConfigurationService implements ManagedService {

    public static final String PID = ConfigurationService.class.getName();
    
    private static final ConfigurationService INSTANCE = new ConfigurationService();
    
    public static final String UNSUBSCRIPTION_GRACE_PERIOD = "unsubscriptionGracePeriod";
    
    private final int timerSleepTime = 1000;
    private Integer unsubscriptionGracePeriod = Integer.valueOf(1000);
    
    private ConfigurationService() {
    }

    public static ConfigurationService getInstance() {
        return INSTANCE;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void updated(Dictionary dict) throws ConfigurationException {
        if (dict != null) {
	        unsubscriptionGracePeriod = (Integer) dict.get(UNSUBSCRIPTION_GRACE_PERIOD);
	        if (unsubscriptionGracePeriod == null) {
	            throw new ConfigurationException(UNSUBSCRIPTION_GRACE_PERIOD, "property not provided");
	        }
        }
    }
    
    public int getTimerSleepTime() {
        return timerSleepTime;
    }
    
    public int getUnSubscriptionGracePeriod() {
        return unsubscriptionGracePeriod;
    }
}
