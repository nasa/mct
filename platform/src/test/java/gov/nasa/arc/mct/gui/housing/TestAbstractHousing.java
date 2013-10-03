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
package gov.nasa.arc.mct.gui.housing;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.SelectionProvider;
import gov.nasa.arc.mct.gui.View;
import gov.nasa.arc.mct.gui.ViewProvider;
import gov.nasa.arc.mct.gui.util.TestUtilities;
import gov.nasa.arc.mct.services.component.ViewInfo;
import gov.nasa.arc.mct.util.condition.Condition;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Collections;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestAbstractHousing {

    @SuppressWarnings("serial")
    class ModernistHousing extends MCTAbstractHousing {

        private WindowListener windowListener;

        public ModernistHousing() {
            super("0");
            windowListener = new WindowAdapter(){
                
            };
        }

        // Getter
        public WindowListener getWindowListener() {
            return windowListener;
        }

        @Override
        public MCTContentArea getContentArea() {
            // TODO Auto-generated method stub
            return null;
        }
        
        @Override
        public void addControlArea(ControlProvider provider) {
            
        }
        
        @Override
        public void toggleControlAreas(boolean showing) {
            
        }

        @Override
        public MCTControlArea getControlArea() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public View getCurrentManifestation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MCTDirectoryArea getDirectoryArea() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public View getInspectionArea() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isControlAreaVisible() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setContentArea(MCTContentArea contentArea) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setControlArea(MCTControlArea controlArea) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public SelectionProvider getSelectionProvider() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setControlAreaVisible(boolean flag) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setDirectoryArea(View directoryArea) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setInspectionArea(View inspectionArea) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public View getHousedViewManifestation() {
            // TODO Auto-generated method stub
            return null;
        }
        
        @Override
        public Collection<ViewProvider> getHousedManifestationProviders() {
            return Collections.emptySet();
        }

        @Override
        public AbstractComponent getWindowComponent() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setStatusArea(MCTStatusArea statusArea) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public MCTStatusArea getStatusArea() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private ModernistHousing myHousing;
    
    @BeforeMethod
    protected void getUser() {
        MockitoAnnotations.initMocks(this);
        
        myHousing = new ModernistHousing();
    }
    
    @Test
    public void testConstructor() {
        assertNotNull(myHousing);
        assertNotNull(myHousing.getHostedFrame());
    }

    @Test
    public void testListener() {
        myHousing.setVisible(true);
        Condition.waitForCondition(5000, new Condition() {
            public boolean getValue() {return myHousing.isVisible();}
        });
        assertTrue(myHousing.isVisible());
        WindowListener listener = myHousing.getWindowListener();
        WindowEvent event = new WindowEvent(myHousing, WindowEvent.WINDOW_CLOSING);
        listener.windowClosing(event);
        myHousing.setVisible(false);
        Condition.waitForCondition(10000, new Condition() {
            public boolean getValue() {return !myHousing.isVisible();}
        });
        assertFalse(myHousing.isVisible());
    }
    
    @Test
    public void testSetHousedView() {
        // Should not allow housed view to be set at the abstract level
        Assert.assertFalse(myHousing.setHousedViewManifestation(Mockito.mock(ViewInfo.class)));
    }

    @AfterClass
    public void cleanup() throws Exception {
        TestUtilities.killWindow(myHousing);
    }

}
