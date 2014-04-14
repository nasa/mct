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
package gov.nasa.arc.mct.canvas.view;

import gov.nasa.arc.mct.canvas.PolicyManagerAccess;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.platform.spi.PersistenceProvider;
import gov.nasa.arc.mct.platform.spi.Platform;
import gov.nasa.arc.mct.platform.spi.PlatformAccess;
import gov.nasa.arc.mct.policy.ExecutionResult;
import gov.nasa.arc.mct.policy.PolicyContext;
import gov.nasa.arc.mct.services.component.PolicyManager;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.services.component.ViewType;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CanvasDropTargetTest {

    private Platform oldPlatform;
    private PolicyManager oldPolicy;
    
    @Mock
    private PolicyManager mockPolicy;
    
    @Mock
    private PersistenceProvider mockPersistence;
    
    @Mock
    private Platform mockPlatform;
    
    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        oldPlatform = PlatformAccess.getPlatform();
        oldPolicy = PolicyManagerAccess.getPolicyManager();
        Mockito.when(mockPlatform.getPersistenceProvider()).thenReturn(mockPersistence);
        Mockito.when(mockPlatform.getPolicyManager()).thenReturn(mockPolicy);
        new PlatformAccess().setPlatform(mockPlatform);
        new PolicyManagerAccess().setPolciyManager(mockPolicy);
    }
    
    @AfterClass
    public void teardown() {
        new PlatformAccess().setPlatform(oldPlatform);
        new PolicyManagerAccess().setPolciyManager(oldPolicy);
    }
    
    /*
     * Regression test for mct #220
     * https://github.com/nasa/mct/issues/220
     */
    @Test
    public void testWithoutEmbeddedView() throws Exception {
        Mockito.when(mockPersistence.getAllProperties(Mockito.anyString())).thenReturn(new HashMap<String, ExtendedProperties>());
        Mockito.when(mockPolicy.execute(Mockito.anyString(), Mockito.<PolicyContext>any())).thenReturn(new ExecutionResult(null, true, ""));
        
        ViewInfo mockCanvasViewInfo = Mockito.mock(ViewInfo.class);
        AbstractComponent mockCanvasComponent = new TestComponent();
        AbstractComponent mockDropComponent = new TestComponent();
        CanvasManifestation canvas = new CanvasManifestation(mockCanvasComponent, mockCanvasViewInfo);
        DropTarget dt = canvas.getDropTarget();
        
        DropTargetDropEvent mockDTDE = Mockito.mock(DropTargetDropEvent.class);
        Transferable mockTransferable = Mockito.mock(Transferable.class);
        View[] mockViews = { Mockito.mock(View.class) };
        
        Mockito.when(mockViews[0].getManifestedComponent()).thenReturn(mockDropComponent);
        Mockito.when(mockViews[0].getInfo()).thenReturn(mockCanvasViewInfo);
        Mockito.when(mockPersistence.getComponent(Mockito.anyString())).thenReturn(mockDropComponent);
        Mockito.when(mockDTDE.getTransferable()).thenReturn(mockTransferable);
        Mockito.when(mockTransferable.isDataFlavorSupported(View.DATA_FLAVOR)).thenReturn(true);
        Mockito.when(mockDTDE.getLocation()).thenReturn(new Point(0,0));
        Mockito.when(mockTransferable.getTransferData(View.DATA_FLAVOR)).thenReturn(mockViews);
        
        dt.drop(mockDTDE);
    }
    
    // Can't mock for this test due to calls to addDelegateComponent,
    // which is final and eventually references a field which needs to be 
    // initialized by a real constructor call.
    public static class TestComponent extends AbstractComponent {
        private ViewInfo titleViewInfo = Mockito.mock(ViewInfo.class);
        private View mockView = Mockito.mock(View.class);
        
        public Set<ViewInfo> getViewInfos(ViewType type) {            
            Mockito.when(titleViewInfo.createView(Mockito.<AbstractComponent>any())).thenReturn(mockView);
            Mockito.when(mockView.getManifestedComponent()).thenReturn(this);
            return type.equals(ViewType.EMBEDDED) ? Collections.<ViewInfo>emptySet() : Collections.<ViewInfo>singleton(titleViewInfo);
        }
        
        @Override
        public String getDisplayName() {
            return "test component";
        }
    }
    
}
