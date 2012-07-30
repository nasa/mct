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

import gov.nasa.arc.mct.gui.ActionContext;
import gov.nasa.arc.mct.gui.GroupAction.RadioAction;
import gov.nasa.arc.mct.gui.housing.MCTAbstractHousing;
import gov.nasa.arc.mct.gui.impl.ActionContextImpl;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.Action;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestListWindows {

    private ListWindowsAction action;
    
    @Mock private MCTAbstractHousing housing1;
    @Mock private MCTAbstractHousing housing2;
    @Mock private MCTAbstractHousing housing3;
    
    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);        
    }
    
    @SuppressWarnings("serial")
    @Test
    public void tetNoOpenHousings() {
        action = new ListWindowsAction() {
            @Override
            Collection<MCTAbstractHousing> getAllHousings() {
                return Collections.emptyList();
            }
        };
        
        Assert.assertFalse(action.canHandle(Mockito.mock(ActionContext.class)));
        Assert.assertFalse(action.isEnabled());
        action.actionPerformed(Mockito.mock(ActionEvent.class)); // Cover no-op
    }
    
    @SuppressWarnings("serial")
    @Test
    public void testOpenHousings() {
        action = new ListWindowsAction() {
            @Override
            Collection<MCTAbstractHousing> getAllHousings() {
                Collection<MCTAbstractHousing> housings = new ArrayList<MCTAbstractHousing>();
                Mockito.when(housing1.getTitle()).thenReturn("A");
                Mockito.when(housing2.getTitle()).thenReturn("A");
                Mockito.when(housing3.getTitle()).thenReturn("B");
                
                housings.add(housing1);
                housings.add(housing2);
                housings.add(housing3);
                return housings;
            }
        };

        ActionContextImpl context = new ActionContextImpl();
        context.setTargetHousing(housing1);
        Assert.assertTrue(action.canHandle(context));
        RadioAction[] radioActions = action.getActions();
        Assert.assertEquals(radioActions.length, 3);
        Assert.assertSame(radioActions[0].getValue(Action.NAME), "A");
        Assert.assertSame(radioActions[1].getValue(Action.NAME), "A");
        Assert.assertSame(radioActions[2].getValue(Action.NAME), "B");
        
        Mockito.when(housing1.isFocused()).thenReturn(true);
        Mockito.doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Mockito.when(housing3.isFocused()).thenReturn(true);
                Mockito.when(housing1.isFocused()).thenReturn(false);
                return null;
            }
        }).when(housing3).toFront();
        Assert.assertTrue(radioActions[0].isSelected());
        Assert.assertFalse(radioActions[1].isSelected());
        Assert.assertFalse(radioActions[2].isSelected());
        
        radioActions[2].actionPerformed(Mockito.mock(ActionEvent.class));
        Assert.assertFalse(housing1.isFocused());
        Assert.assertFalse(housing2.isFocused());
        Assert.assertTrue(housing3.isFocused());
    }
}
