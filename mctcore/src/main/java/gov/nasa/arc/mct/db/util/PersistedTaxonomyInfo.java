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
 * Info class for taxonomies.
 */
public class PersistedTaxonomyInfo {
    private String component_id;
    private boolean puiParent;
    private String taxoPath;
    private int obj_version;
    private int seq_no;

    /**
     * Info class for taxonomies.
     * @param component_id the database id
     * @param taxoPath the taxonomy path ID
     * @param puiParent the taxonomy's parent
     * @param obj_version database object version
     * @param seq_no database sequence number
     */
	public PersistedTaxonomyInfo(String component_id, String taxoPath, boolean puiParent, int obj_version, int seq_no) {
		super();
		this.component_id = component_id;
		this.puiParent = puiParent;
		this.taxoPath = taxoPath;
		this.obj_version = obj_version;
		this.seq_no = seq_no;
	}
	
	/**
	 * Get component ID.
	 * @return component ID
	 */
    public String getComponent_id() {
        return component_id;
    }

    /**
     * Set the component ID.
     * @param component_id component ID
     */
    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    /** 
     * Returns true if this taxonomy is a parent.
     * @return true if this taxonomy is a parent
     */
    public boolean isPuiParent() {
        return puiParent;
    }

    /**
     * Set the parent attribute.
     * @param puiParent the parent
     */
    public void setPuiParent(boolean puiParent) {
        this.puiParent = puiParent;
    }

    /**
     * Get the taxonomy path ID.
     * @return taxo path ID
     */
    public String getTaxoPath() {
        return taxoPath;
    }

    /**
     * Set the taxonomy path ID.
     * @param taxoPath the path
     */
    public void setTaxoPath(String taxoPath) {
        this.taxoPath = taxoPath;
    }

    /** 
     * Get the object version.
     * @return object version
     */
    public int getObj_version() {
        return obj_version;
    }

    /**
     * Set the object version.
     * @param obj_version  the object version
     */
    public void setObj_version(int obj_version) {
        this.obj_version = obj_version;
    }

    /**
     * Get the sequence number.
     * @return sequence number
     */
    public int getSeq_no() {
        return seq_no;
    }

    /**
     * Set the sequence number.
     * @param seq_no the sequence number
     */
    public void setSeq_no(int seq_no) {
        this.seq_no = seq_no;
    }

}