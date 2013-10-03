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

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
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
        // Hash codes should match for new instance with same parameters
        Assert.assertEquals(vi.hashCode(), new ViewInfo(TestView.class, "tv", ViewType.INSPECTOR).hashCode());
        // Hash codes should match for like view types, too
        // (Set operations elsewhere in MCT depend on this behavior)
        Assert.assertEquals(vi.hashCode(), new ViewInfo(TestView.class, "tv2", ViewType.CENTER).hashCode());
    }
    
    @Test
    public void testEquals() {
        ViewInfo vi = new ViewInfo(TestView.class, "tv", ViewType.INSPECTOR);
        ViewInfo vi2 = new ViewInfo(OtherTestView.class, "tv", ViewType.LAYOUT);

        // Should equal with same arguments
        Assert.assertTrue (vi.equals(new ViewInfo(TestView.class, "tv", ViewType.INSPECTOR)));
        // Should equal with same class
        // (Set operations elsewhere in MCT depend on this behavior)
        Assert.assertTrue (vi.equals(new ViewInfo(TestView.class, "tv", ViewType.CENTER)));
                
        // Should not equal another view class
        Assert.assertFalse(vi.equals(vi2));
        
        // Should be false for null or other object types
        Assert.assertFalse(vi.equals(null));
        Assert.assertFalse(vi.equals(Integer.valueOf(7)));
    }
    
    @Test (dataProvider = "hashCodeMatchesEqualsCases")
    public void testHashCodeMatchesEquals(ViewInfo a, ViewInfo b) {
        // Test a decent-sized batch of miscellaneous view info pairs,
        // ensure that hashCode is equal if equals is true
        if (a.hashCode() != b.hashCode()) {
            Assert.assertFalse(a.equals(b));            
            Assert.assertFalse(b.equals(a));            
        } 
        // Note: Hash codes can be the same without implying equality, so
        // nothing to test where a.hashCode == b.hashCode
    }
    
    @SuppressWarnings("unchecked")
    @DataProvider
    public Object[][] hashCodeMatchesEqualsCases() {
        List<Object> objects = new ArrayList<Object>();
                
        for (Class<? extends View> viewClass : new Class[] { TestView.class, OtherTestView.class }) {
            for (ViewType vt : ViewType.values()) {
                for (String name : new String[] {"tv", "vt"}) {
                    objects.add(new ViewInfo(viewClass, name, vt));
                }
            }
        }
        
        Object[][] permutations = new Object[(objects.size() * objects.size() + objects.size()) / 2][];
        int i = 0;
        for (int j = 0; j < objects.size(); j++) {
            for (int k = j; k < objects.size(); k++) {
                permutations[i++] = new Object[] { objects.get(j) , objects.get(k) };
            }
        }
     
        return permutations;
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
    
    public static class OtherTestView extends View {
        private static final long serialVersionUID = 1L;

        public OtherTestView(AbstractComponent ac, ViewInfo vi) {
            super(ac,vi);
        }        
    }
}
