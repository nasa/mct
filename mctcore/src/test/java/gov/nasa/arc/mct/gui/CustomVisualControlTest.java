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
package gov.nasa.arc.mct.gui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class CustomVisualControlTest {
    @Test
    public void testCustomVisualControl() {
        // Only thing to verify is that listeners get called
        final CustomVisualControl control = new CustomVisualControl() {
            private static final long serialVersionUID = -7026155085259171397L;

            @Override
            public void setValue(Object value) {}

            @Override
            public Object getValue() { return null; }

            @Override
            public void setMutable(boolean mutable) {}
        };
        
        ChangeListener mockListeners[] = {
                        Mockito.mock(ChangeListener.class),
                        Mockito.mock(ChangeListener.class)                
        };
        
        ArgumentMatcher<ChangeEvent> controlChanged = new ArgumentMatcher<ChangeEvent>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof ChangeEvent &&
                        ((ChangeEvent)argument).getSource() == control;
            }
        };
        
        control.addChangeListener(mockListeners[0]);
        control.addChangeListener(mockListeners[1]);
        
        control.fireChange();
       
        Mockito.verify(mockListeners[0], Mockito.times(1)).stateChanged(Mockito.argThat(controlChanged));
        Mockito.verify(mockListeners[1], Mockito.times(1)).stateChanged(Mockito.argThat(controlChanged));
        
        control.removeChangeListener(mockListeners[1]);
        control.fireChange();
        
        Mockito.verify(mockListeners[0], Mockito.times(2)).stateChanged(Mockito.argThat(controlChanged));
        Mockito.verify(mockListeners[1], Mockito.times(1)).stateChanged(Mockito.argThat(controlChanged));

        control.removeChangeListener(mockListeners[0]);
        control.addChangeListener(mockListeners[1]);
        control.fireChange();
        
        Mockito.verify(mockListeners[0], Mockito.times(2)).stateChanged(Mockito.argThat(controlChanged));
        Mockito.verify(mockListeners[1], Mockito.times(2)).stateChanged(Mockito.argThat(controlChanged));
    }
}
