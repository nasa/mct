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
package gov.nasa.arc.mct.table.access;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AccessorTest {

	private interface MyServiceInterface {}
	private static class MyService implements MyServiceInterface {}
	private static class MyService2 {}
	
	private ServiceAccess access;
	private MyService service;
	private MyService2 service2;
	
	@BeforeMethod
	public void init() {
		access = new ServiceAccess();
		service = new MyService();
		service2 = new MyService2();
	}
	
	@Test
	public void testGettersSetters() {
		assertNull(ServiceAccess.getService(MyService.class));
		
		access.bind(service);
		assertSame(ServiceAccess.getService(MyService.class), service);
		
		access.unbind(service);
		assertNull(ServiceAccess.getService(MyService.class));
	}
	
	@Test
	public void testBindMoreThanOneService() {
		access.bind(service);
		access.bind(service2);
		
		assertSame(ServiceAccess.getService(MyService.class), service);
		assertSame(ServiceAccess.getService(MyService2.class), service2);
	}
	
	@Test
	public void testFindMatchingService() {
		access.bind(service);
		assertSame(ServiceAccess.getService(MyServiceInterface.class), service);
	}
	
}
