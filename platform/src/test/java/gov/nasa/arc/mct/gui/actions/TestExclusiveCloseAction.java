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
package gov.nasa.arc.mct.gui.actions;

import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestExclusiveCloseAction {

    private WindowsExclusiveCloseAction action;
    
    @Mock MCTAbstractHousing housing1;
    @Mock MCTAbstractHousing housing2;
    
    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);        
    }
    
    @SuppressWarnings("serial")
    @Test
    public void testOpenHousings() {
        action = new WindowsExclusiveCloseAction() {
            @Override
            Collection<MCTAbstractHousing> getAllHousings() {
                Collection<MCTAbstractHousing> housings = new ArrayList<MCTAbstractHousing>();
                housings.add(housing1);
                housings.add(housing2);
                return housings;
            }
        };

        ActionContextImpl context = new ActionContextImpl();
        context.setTargetHousing(housing1);
        Assert.assertTrue(action.canHandle(context));
        Assert.assertTrue(action.isEnabled());

        Mockito.doNothing().when(housing2).closeHousing();
        action.actionPerformed(Mockito.mock(ActionEvent.class));
        Mockito.verify(housing2, Mockito.times(1)).closeHousing();
        Mockito.verify(housing1, Mockito.times(0)).closeHousing();
    }
    
    @SuppressWarnings("serial")
    @Test
    public void testNoOpenHousings() {
        action = new WindowsExclusiveCloseAction() {
            @Override
            Collection<MCTAbstractHousing> getAllHousings() {
                return Collections.emptyList();
            }
        };

        ActionContextImpl context = new ActionContextImpl();
        context.setTargetHousing(null);
        Assert.assertFalse(action.canHandle(context));
    }
    
    @SuppressWarnings("serial")
    @Test
    public void testOneOpenHousing() {
        action = new WindowsExclusiveCloseAction() {
            @Override
            Collection<MCTAbstractHousing> getAllHousings() {
                Collection<MCTAbstractHousing> housings = new ArrayList<MCTAbstractHousing>();
                housings.add(housing1);
                return housings;
            }
        };

        ActionContextImpl context = new ActionContextImpl();
        context.setTargetHousing(housing1);
        Assert.assertTrue(action.canHandle(context));
        Assert.assertFalse(action.isEnabled());
    }


}
