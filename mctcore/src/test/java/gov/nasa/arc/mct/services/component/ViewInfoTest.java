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
package gov.nasa.arc.mct.services.component;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.gui.View;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ViewInfoTest {
    
    
    @Mock
    private AbstractComponent component;
    
    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidViewClass() {
        new ViewInfo(InvalidConstructorView.class, "testview", ViewType.CENTER);
    }
    
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testInvalidViewTypes() {
        new ViewInfo(TestView.class, "testview", null);
    }
    
    @Test
    public void testMethods() {
        ViewInfo vi = new ViewInfo(TestView.class, "testview", ViewType.CENTER);
        Assert.assertEquals(vi.getViewType(),ViewType.CENTER);
        Assert.assertEquals(vi.getViewClass(), TestView.class);
        Assert.assertEquals(vi.getViewName(),"testview");
        View v = vi.createView(component);
        Assert.assertEquals(v.getClass(),TestView.class);
        Mockito.verify(component).addViewManifestation(v);
    }
    
    @Test
    public void testHashCode() {
        ViewInfo vi = new ViewInfo(TestView.class, "tv", ViewType.INSPECTOR);
        Assert.assertEquals(vi.hashCode(), TestView.class.getName().hashCode() ^ ViewType.INSPECTOR.hashCode());
    }
    
    @Test
    public void testEquals() {
        ViewInfo vi = new ViewInfo(TestView.class, "tv", ViewType.INSPECTOR);
        ViewInfo vi2 = new ViewInfo(TestView.class, "tv", ViewType.INSPECTOR);
        View v = Mockito.mock(View.class);
        ViewInfo vi3 = new ViewInfo(v.getClass(), "tv", TestView.class.getName(),ViewType.LAYOUT);

        
        Assert.assertFalse(vi.equals(null));
        Assert.assertTrue (vi.equals(vi2));
        Assert.assertFalse(vi.equals(vi3));
        Assert.assertFalse(vi.equals(Integer.valueOf(7)));
    }
    
    private static class InvalidConstructorView extends View {
        private static final long serialVersionUID = 1L;
        
    }
    
    public static class TestView extends View {
        private static final long serialVersionUID = 1L;

        public TestView(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }
    }
}
