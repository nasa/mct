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
package gov.nasa.arc.mct.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BootstrapTest {

    @Test (dataProvider = "comparatorTestCases")
    public void testComparator(
                    Bootstrap bootstrapA, Bootstrap bootstrapB,
                    long creationA, long creationB,
                    int expectedSign) {
        // Set up mocks
        AbstractComponent a = Mockito.mock(AbstractComponent.class);
        AbstractComponent b = Mockito.mock(AbstractComponent.class);
        Mockito.when(a.handleGetCapability(Bootstrap.class))
            .thenReturn(bootstrapA);
        Mockito.when(b.handleGetCapability(Bootstrap.class))
            .thenReturn(bootstrapB);
        Mockito.when(a.getCreationDate()).thenReturn(new Date(creationA));
        Mockito.when(b.getCreationDate()).thenReturn(new Date(creationB));
        
        // Run the comparator
        int actualSign = (int) (Math.signum(Bootstrap.COMPARATOR.compare(a, b)));
        
        // Verify that sign is as expected
        Assert.assertEquals(actualSign, expectedSign);
    }
    
    @DataProvider
    public Object[][] comparatorTestCases() {
        long times[] = { 100L, 1000000L };
        int indexes[] = { Integer.MIN_VALUE/2, 0, Integer.MAX_VALUE/2 };
        
        List<Object[]> cases = new ArrayList<Object[]>();
        
        // Category should decide the difference, if it is different
        for (long ta : times) {
            for (long tb : times) {
                for (int ia : indexes) {
                    for (int ib : indexes) {
                        for (int i : indexes) {
                            cases.add(new Object[] {
                                mockBootstrap(i, ia), mockBootstrap(i+100, ib),
                                ta, tb,
                                -1
                            });
                            cases.add(new Object[] {
                                mockBootstrap(i, ia), mockBootstrap(i-100, ib),
                                ta, tb,
                                1
                            });
                        }
                    }
                }                
            }
        }

        // Otherwise, should depend on component index
        for (long ta : times) {
            for (long tb : times) {
                for (int i : indexes) {
                    for (int j : indexes) {
                        cases.add(new Object[] {
                            mockBootstrap(i, j), mockBootstrap(i, j+100),
                            ta, tb,
                            -1
                        });
                        cases.add(new Object[] {
                            mockBootstrap(i, j), mockBootstrap(i, j-100),
                            ta, tb,
                            1
                        });
                        cases.add(new Object[] {
                            mockBootstrap(i, j), mockBootstrap(i, j),
                            ta, tb,
                            0
                        });
                    }
                }                
            }
        }        

        // When Bootstrap capability is null, should be less
        for (long ta : times) {
            for (long tb : times) {
                for (int i : indexes) {
                    cases.add(new Object[] {
                        null, mockBootstrap(i, i),
                        ta, tb,
                        -1
                    });
                    cases.add(new Object[] {
                        mockBootstrap(i, i), null,
                        ta, tb,
                        1
                    });
                }                
            }
        }        

        // Finally, just use time stamp if neither has Bootstrap
        for (long ta : times) {
            for (long tb : times) {
                cases.add(new Object[] {
                    null, null,
                    ta, tb,
                    (int) (Math.signum(ta-tb))
                });
            }
        }        

        
        Object[][] c = new Object[cases.size()][];
        for (int i = 0; i < cases.size(); i++) {
            c[i] = cases.get(i);
        }
        
        return c;
    }
    
    private Bootstrap mockBootstrap(int categoryIndex, int componentIndex) {
        Bootstrap mockBootstrap = Mockito.mock(Bootstrap.class);
        
        Mockito.when(mockBootstrap.categoryIndex()).thenReturn(categoryIndex);
        Mockito.when(mockBootstrap.componentIndex()).thenReturn(componentIndex);
        
        return mockBootstrap;
    }
       
}
