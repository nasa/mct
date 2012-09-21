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
package gov.nasa.arc.mct.table.dnd;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import gov.nasa.arc.mct.gui.View;

import java.awt.datatransfer.DataFlavor;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestViewRoleSelection {

    private ViewRoleSelection subjectClass;
    private View[] viewRoleCollection;

    @BeforeClass
    public void setup() {
        viewRoleCollection = getViewRoleCollection();
        subjectClass = new ViewRoleSelection(viewRoleCollection);
    }

    private View[] getViewRoleCollection() {
        View[] viewColl = new View[] {
        		Mockito.mock(View.class),
        		Mockito.mock(View.class)
        };
        return viewColl;
    }

    @Test
    public void testGetTransferData() throws Exception {
        DataFlavor flavor = View.DATA_FLAVOR;
        Object returnee = subjectClass.getTransferData(flavor);
        assertNotNull(returnee);

        Object nullReturnee = subjectClass.getTransferData(null);
        assertNull(nullReturnee);
    }

    @Test
    public void testGetDataTransferFlavors() {
        assertNotNull(subjectClass.getTransferDataFlavors());
    }

    @Test
    public void testIsDataFlavorSupported() throws ClassNotFoundException {
        assertTrue(subjectClass.isDataFlavorSupported(View.DATA_FLAVOR));
    }
}
