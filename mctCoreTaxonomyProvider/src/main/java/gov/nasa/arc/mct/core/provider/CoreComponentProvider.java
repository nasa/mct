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
package gov.nasa.arc.mct.core.provider;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.core.components.BrokenComponent;
import gov.nasa.arc.mct.core.components.BrokenInfoPanel;
import gov.nasa.arc.mct.core.components.MineTaxonomyComponent;
import gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent;
import gov.nasa.arc.mct.core.components.TelemetryDisciplineComponent;
import gov.nasa.arc.mct.core.components.TelemetryUserDropBoxComponent;
import gov.nasa.arc.mct.core.policy.AllCannotBeInspectedPolicy;
import gov.nasa.arc.mct.core.policy.CanDeleteComponentPolicy;
import gov.nasa.arc.mct.core.policy.CanRemoveComponentPolicy;
import gov.nasa.arc.mct.core.policy.CannotDragOrDropMySandbox;
import gov.nasa.arc.mct.core.policy.CantDuplicateDropBoxesPolicy;
import gov.nasa.arc.mct.core.policy.ChangeOwnershipPolicy;
import gov.nasa.arc.mct.core.policy.CheckBuiltinComponentPolicy;
import gov.nasa.arc.mct.core.policy.CheckComponentOwnerIsUserPolicy;
import gov.nasa.arc.mct.core.policy.DisciplineUsersViewControlPolicy;
import gov.nasa.arc.mct.core.policy.DropboxFilterViewPolicy;
import gov.nasa.arc.mct.core.policy.LeafCannotAddChildDetectionPolicy;
import gov.nasa.arc.mct.core.policy.ObjectPermissionPolicy;
import gov.nasa.arc.mct.core.policy.ReservedWordsNamingPolicy;
import gov.nasa.arc.mct.core.policy.SameComponentsCannotBeLinkedPolicy;
import gov.nasa.arc.mct.core.roles.DropboxCanvasView;
import gov.nasa.arc.mct.core.roles.UsersManifestation;
import gov.nasa.arc.mct.platform.spi.DefaultComponentProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.policy.PolicyInfo.CategoryType;
import gov.nasa.arc.mct.services.component.AbstractComponentProvider;
import gov.nasa.arc.mct.services.component.ComponentTypeInfo;
import gov.nasa.arc.mct.services.component.ProviderDelegate;
import gov.nasa.arc.mct.services.component.TypeInfo;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.ComponentInitializer;
import gov.nasa.arc.mct.services.internal.component.CoreComponentRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class CoreComponentProvider extends AbstractComponentProvider implements DefaultComponentProvider {
    private static final ResourceBundle resource = ResourceBundle.getBundle("CoreTaxonomyResourceBundle"); // NO18N

    private ImageIcon GROUPS_ICON = new ImageIcon(getClass().getResource("/icons/mct_icon_groups.png"));
    private ImageIcon DROPBOX_ICON = new ImageIcon(getClass().getResource("/icons/mct_icon_dropbox.png"));
    
    @Override
    public Collection<ComponentTypeInfo> getComponentTypes() {
        List<ComponentTypeInfo> compInfos = new ArrayList<ComponentTypeInfo>();
        ComponentTypeInfo typeInfo = new ComponentTypeInfo(resource.getString("discipline_component_display_name"), resource
                .getString("discipline_component_description"), TelemetryDisciplineComponent.class, false);
        compInfos.add(typeInfo);
        typeInfo = new ComponentTypeInfo(resource.getString("user_dropbox_component_display_name"), resource
                .getString("user_dropbox_component_description"), TelemetryUserDropBoxComponent.class, false);
        compInfos.add(typeInfo);
        typeInfo = new ComponentTypeInfo(resource.getString("mine_component_display_name"), resource
                .getString("mine_component_description"), MineTaxonomyComponent.class, false);
        compInfos.add(typeInfo);
        typeInfo = new ComponentTypeInfo(resource.getString("broken_component_display_name"), resource
                .getString("broken_component_description"), BrokenComponent.class, false);
        compInfos.add(typeInfo);
        typeInfo = new ComponentTypeInfo(resource.getString("data_taxonomy_component_type_display_name"), resource.getString("data_taxonomy_component_type_description"), TelemetryDataTaxonomyComponent.class, false);
        compInfos.add(typeInfo);

        
        return compInfos;
    }

    @Override
    public Collection<ViewInfo> getViews(String componentTypeId) {
        if (BrokenComponent.class.getName().equals(componentTypeId)) {
            return Collections.singleton(new ViewInfo(BrokenInfoPanel.class, resource.getString("BrokenInspectorViewName"),ViewType.OBJECT)); //NOI18N
        } else if (TelemetryUserDropBoxComponent.class.getName().equals(componentTypeId)) {
            return Arrays.asList(
                    new ViewInfo(DropboxCanvasView.class, resource.getString("DropBoxViewName"),ViewType.OBJECT),
                    new ViewInfo(DropboxCanvasView.class, resource.getString("DropBoxViewName"),ViewType.CENTER));
        } else if (TelemetryDisciplineComponent.class.getName().equals(componentTypeId)) {
            return Collections.singleton(new ViewInfo(UsersManifestation.class, "Users", ViewType.OBJECT));
        }
        
        return Collections.emptyList();
    }    
            
    @Override
    public Collection<PolicyInfo> getPolicyInfos() {
        return Arrays.asList(
                new PolicyInfo(CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey(),
                               ObjectPermissionPolicy.class),
                new PolicyInfo(CategoryType.COMPOSITION_POLICY_CATEGORY.getKey(), 
                               LeafCannotAddChildDetectionPolicy.class,
                               CannotDragOrDropMySandbox.class,
                               SameComponentsCannotBeLinkedPolicy.class),
                new PolicyInfo(CategoryType.ACCEPT_DELEGATE_MODEL_CATEGORY.getKey(),
                               LeafCannotAddChildDetectionPolicy.class,
                               SameComponentsCannotBeLinkedPolicy.class),
                new PolicyInfo(CategoryType.COMPONENT_NAMING_POLICY_CATEGORY.getKey(),
                               ReservedWordsNamingPolicy.class),
                new PolicyInfo(CategoryType.ALLOW_COMPONENT_RENAME_POLICY_CATEGORY.getKey(),
                               ChangeOwnershipPolicy.class,
                               CheckBuiltinComponentPolicy.class,
                               ReservedWordsNamingPolicy.class,
                               ObjectPermissionPolicy.class),                            
                new PolicyInfo(CategoryType.FILTER_VIEW_ROLE.getKey(),
                               DropboxFilterViewPolicy.class,
                               AllCannotBeInspectedPolicy.class),
                new PolicyInfo(CategoryType.CAN_DELETE_COMPONENT_POLICY_CATEGORY.getKey(),
                               CanDeleteComponentPolicy.class),
                new PolicyInfo(CategoryType.CAN_REMOVE_MANIFESTATION_CATEGORY.getKey(),
                        CanRemoveComponentPolicy.class),
                new PolicyInfo(CategoryType.CAN_DUPLICATE_OBJECT.getKey(), 
                                CantDuplicateDropBoxesPolicy.class,
                               CheckComponentOwnerIsUserPolicy.class),
                new PolicyInfo(CategoryType.COMPOSITION_POLICY_CATEGORY.getKey(), 
                               CheckComponentOwnerIsUserPolicy.class),                
                new PolicyInfo(CategoryType.CAN_OBJECT_BE_CONTAINED_CATEGORY.getKey(),
                                CannotDragOrDropMySandbox.class),
                new PolicyInfo(CategoryType.SHOW_HIDE_CTRL_MANIFESTATION.getKey(),
                               DisciplineUsersViewControlPolicy.class));
    }
    
    @Override
    public Class<? extends AbstractComponent> getBrokenComponent() {
       return BrokenComponent.class;
    }

    @Override
    public AbstractComponent createDropbox(String userId) {
        Platform platform = PlatformAccess.getPlatform();
        AbstractComponent dropbox = platform.getComponentRegistry().newInstance(TelemetryUserDropBoxComponent.class.getName());
        ComponentInitializer dropboxCapability = dropbox.getCapability(ComponentInitializer.class);
        dropboxCapability.setCreator("admin");
        dropboxCapability.setOwner(userId);
        dropbox.setDisplayName(userId + "\'s drop box");
        
        return dropbox;
    }
    
    @Override
    public AbstractComponent createSandbox(String userId) {
        Platform platform = PlatformAccess.getPlatform();
        CoreComponentRegistry componentRegistry = platform.getComponentRegistry();
        AbstractComponent mySandbox = componentRegistry.newInstance(MineTaxonomyComponent.class.getName());
        ComponentInitializer mysandboxCapability = mySandbox.getCapability(ComponentInitializer.class);
        mysandboxCapability.setCreator(userId);
        mysandboxCapability.setOwner(userId);
        mySandbox.setDisplayName("My Sandbox");
        
        return mySandbox;
    }
    
    @Override
    public void createDefaultComponents() {
        Platform platform = PlatformAccess.getPlatform();
        CoreComponentRegistry componentRegistry = platform.getComponentRegistry();
        
        AbstractComponent dropBoxes = componentRegistry.newInstance(TelemetryDataTaxonomyComponent.class.getName());
        dropBoxes.setDisplayName("User Drop Boxes");
        dropBoxes.setExternalKey("/UserDropBoxes");
        ComponentInitializer dropBoxesCapability = dropBoxes.getCapability(ComponentInitializer.class);
        dropBoxesCapability.setCreator("admin");
        dropBoxesCapability.setOwner("admin");
        
        platform.getPersistenceProvider().persist(Collections.singleton(dropBoxes));
        platform.getPersistenceProvider().tagComponents("bootstrap:admin", Collections.singleton(dropBoxes));
    }
    
    @Override
    public ProviderDelegate getProviderDelegate() {
        return new CoreComponentProviderDelegate();
    }

    @Override
    public <T> T getAsset(TypeInfo<?> typeInfo, Class<T> assetClass) {
        if (assetClass.isAssignableFrom(ImageIcon.class)) {
            if (typeInfo.getTypeClass().equals(TelemetryDisciplineComponent.class)) {
                return assetClass.cast(GROUPS_ICON);
            }
            if (typeInfo.getTypeClass().equals(TelemetryUserDropBoxComponent.class)) {
                return assetClass.cast(DROPBOX_ICON);
            }
        }
        return super.getAsset(typeInfo, assetClass);
    }
    
}
