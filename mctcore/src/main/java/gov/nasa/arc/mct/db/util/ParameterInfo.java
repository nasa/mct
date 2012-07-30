/*******************************************************************************
 * Mission Control Technologies is Copyright 2007-2012 NASA Ames Research Center
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use 
 * this file except in compliance with the License. See the MCT Open Source 
 * Licenses file distributed with this work for additional information regarding copyright 
 * ownership. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for 
 * the specific language governing permissions and limitations under the License.
 *******************************************************************************/

package gov.nasa.arc.mct.db.util;

/**
 * This class describes an component info. 
 */
public class ParameterInfo {
    private String symbolName;
    private String displayName;

    /**
     * Specifies a component.
     * @param id the unique symbol name
     * @param name human readable string describing the component type
     * @throws Exception if error
     */
    public ParameterInfo(String id, String name) throws Exception {

        if (id == null || id.isEmpty()) {
            throw new Exception("symbol id must be non empty.");
        }
        if (name == null) {
            throw new Exception("display name must be non null.");
        }
        this.symbolName = id;
        this.displayName = name;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof ParameterInfo &&
        ((ParameterInfo)obj).getSymbolName().equals(getSymbolName());
    }

    @Override
    public final int hashCode() {
        return getSymbolName().hashCode();
    }

    /**
     * Get the symbol name.
     * @return the symbol name
     */
    public String getSymbolName() {
        return symbolName;
    }

    /**
     * Get the display name.
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

}
