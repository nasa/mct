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
/**
 * TelemetryElementComponent.java Aug 18, 2008
 * 
 * This code is the property of the National Aeronautics and Space
 * Administration and was produced for the Mission Control Technologies (MCT)
 * project.
 * 
 */
package gov.nasa.arc.mct.core.components;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.Bootstrap;

public class MineTaxonomyComponent extends AbstractComponent implements Bootstrap {
    
    public MineTaxonomyComponent() {
    }

    @Override
    protected <T> T handleGetCapability(Class<T> capability) {
        return capability.isAssignableFrom(getClass()) ?
                capability.cast(this) : super.handleGetCapability(capability);
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public boolean isSandbox() {
        return true;
    }

    @Override
    public int categoryIndex() {
        return Integer.MAX_VALUE; // Always should appear at bottom
    }

    @Override
    public int componentIndex() {
        return Integer.MAX_VALUE; // Always should appear at bottom
    }
    
}
