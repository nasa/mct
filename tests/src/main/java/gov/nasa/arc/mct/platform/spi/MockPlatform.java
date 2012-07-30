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
package gov.nasa.arc.mct.platform.spi;

import gov.nasa.arc.mct.api.feed.FeedAggregator;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.activity.TimeService;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.MenuManager;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ProviderDelegateService;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;
import gov.nasa.arc.mct.services.internal.component.User;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockPlatform implements Platform {
	
	@Override
	public PolicyManager getPolicyManager() {
		return new PolicyManager() {
			
			@Override
			public ExecutionResult execute(String categoryKey, PolicyContext context) {
				return new ExecutionResult(context, true, "");
			}
		};
	}

	@Override
	public WindowManager getWindowManager() {
		return new WindowManager() {
			
			@Override
			public void openInNewWindow(AbstractComponent component) {
			}

			@Override
			public AbstractComponent getWindowRootComponent(Component component) {
				return null;
			}

			@Override
			public View getWindowRootManifestation(
					Component component) {
				return null;
			}
			
			@Override
			public void refreshWindows() {
				//
			}
			
			@Override
			public void closeWindows(String componentId) {
				// 
			}

			@Override
			public void openInNewWindow(AbstractComponent component,
					GraphicsConfiguration graphicsConfig) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public CoreComponentRegistry getComponentRegistry() {
		return new CoreComponentRegistry() {
			
			@Override
			public <T extends AbstractComponent> T newInstance(Class<T> componentClass, AbstractComponent parent) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public AbstractComponent newInstance(ComponentTypeInfo componentTypeInfo) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public AbstractComponent getComponent(String id) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AbstractComponent newCollection(
					Collection<AbstractComponent> components) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<ViewInfo> getViewInfos(String componentTypeId,
					ViewType type) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AbstractComponent newInstance(String componentType) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean isCreatable(Class<?> clazz) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		
	}

	@Override
	public User getCurrentUser() {
		return GlobalContext.getGlobalContext().getUser();
	}
	
	@Override
	public DefaultComponentProvider getDefaultComponentProvider() {
		return null;
	}
	
	@Override
	public SubscriptionManager getSubscriptionManager() {
		return null;
	}

	@Override
	public TimeService getTimeService() {
		return null;
	}

	@Override
	public MenuManager getMenuManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProviderDelegateService getProviderDelegateService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FeedAggregator getFeedAggregator() {
		return null;
	}
	
	@Override
	public PersistenceProvider getPersistenceProvider() {
		return new PersistenceProvider() {

			@Override
			public void startRelatedOperations() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void tagComponents(String tag,
					Collection<AbstractComponent> components) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void completeRelatedOperations(boolean save) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public Map<String, ExtendedProperties> getAllProperties(
					String componentId) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean hasComponentsTaggedBy(String tagId) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public <T extends AbstractComponent> T getComponent(
					String externalKey, Class<T> componentType) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public User getUser(String userId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AbstractComponent getComponent(String componentId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AbstractComponent getComponentFromStore(String componentId) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<AbstractComponent> getReferences(
					AbstractComponent component) {
				return Collections.emptyList();
			}

			@Override
			public void persist(
					Collection<AbstractComponent> componentsToPersist) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(Collection<AbstractComponent> componentsToDelete) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public List<AbstractComponent> getReferencedComponents(
					AbstractComponent component) {
				return Collections.emptyList();
			}

			@Override
			public Set<String> getAllUsers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<String> getUsersInGroup(String group) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void addComponentToWorkUnit(AbstractComponent component) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void updateComponentsFromDatabase() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public List<AbstractComponent> getBootstrapComponents() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void addNewUser(String userId, String groupId,
					AbstractComponent mysandbox, AbstractComponent dropbox) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public AbstractComponent getComponent(String externalKey,
					String componentType) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	@Override
	public AbstractComponent getRootComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AbstractComponent> getBootstrapComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractComponent getMySandbox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractComponent getUserDropboxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refreshAllMCTHousedContent() {
		// TODO Auto-generated method stub
		
	}
}
