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
package gov.nasa.arc.mct.gui.impl;

import gov.nasa.arc.mct.gui.Twistie;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestTwistie {
    
    private MyTwistie twistie;
    private int invocationCt = 0;
    
    @Mock
    private MouseEvent mouseEvent;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        twistie = new MyTwistie();
        checkState(twistie, false);
    }
    
    @AfterMethod
    public void reset() {
        invocationCt = 0;
    }
        
    @Test
    public void testClick() throws Exception {
        MouseListener[] mouseListeners = twistie.getMouseListeners();
        Assert.assertNotNull(mouseListeners);
        Assert.assertEquals(mouseListeners.length, 1);
        MouseListener mouseListener = mouseListeners[0];
        mouseEvent = Mockito.mock(MouseEvent.class);
        mouseListener.mouseClicked(mouseEvent);
        checkState(twistie, true);
        Assert.assertEquals(invocationCt, 1);
    }
    
    @Test(dependsOnMethods = {"testClick"})
    public void testMethod() throws Exception {
        twistie.changeState(false);
        checkState(twistie, false);
        Assert.assertEquals(invocationCt, 0);
    }

    
    private void checkState(Twistie twistie, boolean expectedValue) {
        try {
            Field field = Twistie.class.getDeclaredField("state");
            field.setAccessible(true);
            Boolean state = (Boolean) field.get(twistie);
            Assert.assertEquals(state.booleanValue(), expectedValue);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }
    
    @SuppressWarnings("serial")
    private class MyTwistie extends Twistie {

        public MyTwistie() {
            super();
        }
        
        @Override
        protected void changeStateAction(boolean state) {
            invocationCt++;
        }
        
    }
    
}
