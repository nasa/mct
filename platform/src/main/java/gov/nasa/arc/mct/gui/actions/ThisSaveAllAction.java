package gov.nasa.arc.mct.gui.actions;

import gov.nasa.arc.mct.api.persistence.OptimisticLockException;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.ContextAwareAction;
import gov.nasa.arc.mct.gui.housing.MCTContentArea;
import gov.nasa.arc.mct.gui.housing.MCTHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.policy.PolicyInfo;
import gov.nasa.arc.mct.services.internal.component.Updatable;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.Set;

public class ThisSaveAllAction extends ContextAwareAction{
    private static final long serialVersionUID = 3940626077815919451L;
    private static final ResourceBundle BUNDLE = 
            ResourceBundle.getBundle(
                    ThisSaveAllAction.class.getName().substring(0, 
                            ThisSaveAllAction.class.getName().lastIndexOf("."))+".Bundle");
    private ActionContextImpl actionContext;
        

    public ThisSaveAllAction() {
        super(BUNDLE.getString("SaveAllAction.label"));
    }
    
    @Override
    public boolean canHandle(ActionContext context) {
        actionContext = (ActionContextImpl) context;
        return getCenterPaneComponent() != null;
    }

    private boolean isComponentWriteableByUser(AbstractComponent component) {
        Platform p = PlatformAccess.getPlatform();
        PolicyContext policyContext = new PolicyContext();
        policyContext.setProperty(PolicyContext.PropertyName.TARGET_COMPONENT.getName(), component);
        policyContext.setProperty(PolicyContext.PropertyName.ACTION.getName(), 'w');
        String inspectionKey = PolicyInfo.CategoryType.OBJECT_INSPECTION_POLICY_CATEGORY.getKey();
        return p.getPolicyManager().execute(inspectionKey, policyContext).getStatus();
    }
    
    private AbstractComponent getCenterPaneComponent() {
        MCTHousing housing = actionContext.getTargetHousing();
        MCTContentArea contentArea = housing.getContentArea();
        return contentArea == null ? null : contentArea.getHousedViewManifestation().getManifestedComponent();
    }
    
    @Override
    public boolean isEnabled() {
        AbstractComponent ac = getCenterPaneComponent();
        Set<AbstractComponent> modified = ac.getAllModifiedObjects();
        
        // Ensure that policy permits saving ALL these components
        boolean hasOnlyWriteableComponents = isComponentWriteableByUser(ac);
        if (hasOnlyWriteableComponents) {
            for (AbstractComponent mod : modified) {
                if (!isComponentWriteableByUser(mod)) {
                    hasOnlyWriteableComponents = false;
                    break;
                }
            }
        }
                
        return (!ac.isStale() && ac.isDirty() || !modified.isEmpty()) && hasOnlyWriteableComponents;
    }

    /**
     * This method is invoked when the client side object is stale. This can occur when another client 
     * even another window in the same application instance has saved the component after it has been loaded. 
     * This implementation will try again, which will overwrite the previous change; however, this is where 
     * configuration could be added to display a message instead. 
     */
    private void handleStaleObject(AbstractComponent ac) {
        overwritePreviousChanges(ac);
    }
    
    private void overwritePreviousChanges(AbstractComponent ac) {
        AbstractComponent updatedComp = PlatformAccess.getPlatform().getPersistenceProvider().getComponentFromStore(ac.getComponentId());
        ac.getCapability(Updatable.class).setVersion(updatedComp.getVersion());
        actionPerformed(null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractComponent ac = getCenterPaneComponent();
        Set<AbstractComponent> allModifiedObjects = ac.getAllModifiedObjects();
        if (ac.isDirty())
            allModifiedObjects.add(ac);
        try {
            PlatformAccess.getPlatform().getPersistenceProvider().persist(allModifiedObjects);
        } catch (OptimisticLockException ole) {
            handleStaleObject(ac);
        }
    }

}
