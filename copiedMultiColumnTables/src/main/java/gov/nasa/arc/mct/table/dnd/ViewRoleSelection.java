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

import gov.nasa.arc.mct.gui.View;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * This class implements the transferrable class consisting of an array of ViewRole objects.
 */
public class ViewRoleSelection implements Transferable {
    private View[] transferredViewRoles;
    private DataFlavor[] supportedDataFlavors = new DataFlavor[] { View.DATA_FLAVOR };

    /**
     * Creates a new transferrable based on the array of view roles that can be transferred.
     * 
     * @param transferredViewRoles the view roles corresponding to the selection
     */
    public ViewRoleSelection(View[] transferredViewRoles) {
        this.transferredViewRoles = transferredViewRoles;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return transferredViewRoles;
        }
        return null;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedDataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor == null) {
            return false;
        }
        return flavor.equals(supportedDataFlavors[0]);
    }
}
