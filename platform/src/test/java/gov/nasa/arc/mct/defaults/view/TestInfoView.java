package gov.nasa.arc.mct.defaults.view;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.context.GlobalContext;
import gov.nasa.arc.mct.platform.spi.RoleAccess;
import gov.nasa.arc.mct.platform.spi.RoleService;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;
import gov.nasa.arc.mct.services.internal.component.User;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestInfoView {
    @Test
    public void testUpdateMonitoredGUI() {
        /* This test ensures that updateMonitoredGUI triggers revalidate/repaint */
        
        final AtomicInteger repaintCalls = new AtomicInteger(0);
        final AtomicInteger revalidateCalls = new AtomicInteger(0);
        
        // This is just elaborate mocking to avoid NPEs
        User mockUser = Mockito.mock(User.class);
        RoleService mockRoleService = Mockito.mock(RoleService.class);
        Mockito.when(mockUser.getUserId()).thenReturn("");
        Mockito.when(mockRoleService.getAllUsers()).thenReturn(Collections.singleton(""));
        GlobalContext.getGlobalContext().switchUser(mockUser, Mockito.mock(Runnable.class));
        new RoleAccess().addRoleService(mockRoleService);
        
        AbstractComponent comp = Mockito.mock(AbstractComponent.class);
        ViewInfo info = new ViewInfo(InfoView.class, "Info", ViewType.CENTER);
        
        Mockito.when(comp.getOwner()).thenReturn("*");
        Mockito.when(comp.getComponentTypeID()).thenReturn("");
        
        // We can't mock InfoView because we want to test its code, but we 
        // do want to verify that methods of its Swing superclass are invoked. 
        InfoView infoView = new InfoView(comp, info) {
            private static final long serialVersionUID = 1L;
            public void repaint() {
                super.repaint();
                repaintCalls.incrementAndGet();
            }
            public void revalidate() {
                super.revalidate();
                revalidateCalls.incrementAndGet();
            }
        };
        
        // The real part of the test: Check that repaint and revalidate really do get called.
        repaintCalls.set(0);
        revalidateCalls.set(0);
        infoView.updateMonitoredGUI();
        Assert.assertEquals(repaintCalls.get(), 1);
        Assert.assertEquals(revalidateCalls.get(), 1);        
    }
}
