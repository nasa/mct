/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
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
package gov.nasa.arc.mct.dbpersistence.dao;


import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "component_spec",uniqueConstraints=@UniqueConstraint(columnNames={"external_key", "component_type"}))
@Cacheable
@NamedQueries({
    @NamedQuery(name = "ComponentSpec.findAll", query = "SELECT c FROM ComponentSpec c"),
    @NamedQuery(name = "ComponentSpec.findReferencingComponents", query = "SELECT c FROM ComponentSpec c JOIN c.referencedComponents refs WHERE refs.componentId = :component")})
public class ComponentSpec implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "component_id",length=32)
    private String componentId;
    
    @Basic(optional = false)
    @Column(name = "component_name",length=200)
    private String componentName;
    
    @Basic(optional = false)
    @Column(name = "creator_user_id",updatable=false,length=20)
    private String creatorUserId;
    
    @Basic(optional = false)
    @Column(name = "owner",length=40)
    private String owner;
    
    @Column(name = "date_created",updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    
    @Column(name = "external_key",length=64)
    private String externalKey;
    
    @Basic(optional = false)
    @Column(name = "last_modified", nullable=false)    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;
    
    @Basic(optional = false)
    @Column(name = "component_type",length=150)
    private String componentType;
    
    @Lob
    @Column(name = "model_info",length=Integer.MAX_VALUE)
    private String modelInfo;
    
    @Basic(optional = false)
    @Version()
    @Column(name = "obj_version")
    private int objVersion;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "componentSpec",fetch=FetchType.LAZY)
    private Collection<ViewState> viewStateCollection;
    
    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE,CascadeType.DETACH, CascadeType.PERSIST},fetch=FetchType.LAZY)
    @JoinTable(name="component_relationship",inverseJoinColumns=@JoinColumn(name="associated_component_id"), joinColumns=@JoinColumn(name="component_id"))
    @OrderColumn(name="seq_no")
    private List<ComponentSpec> referencedComponents;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "componentSpec",fetch=FetchType.LAZY)
    private Collection<TagAssociation> tagAssociationCollection;

    public ComponentSpec() {
    }

    @PrePersist
    public void initializePriorToDatabaseAdd() {
    	dateCreated = new Date();
    	lastModified = dateCreated; // May be wrong, but won't result in missed updates 
    }
    
    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(String modelInfo) {
        this.modelInfo = modelInfo;
    }

    public void setObjVersion(int versionNumber) {
    	objVersion = versionNumber;
    }
    
    public int getObjVersion() {
        return objVersion;
    }

    public Collection<ViewState> getViewStateCollection() {
        return viewStateCollection;
    }

    public void setViewStateCollection(Collection<ViewState> viewStateCollection) {
        this.viewStateCollection = viewStateCollection;
    }

    public List<ComponentSpec> getReferencedComponents() {
        return referencedComponents;
    }

    public void setReferencedComponents(List<ComponentSpec> componentRelationshipCollection) {
        this.referencedComponents = componentRelationshipCollection;
    }

    public Collection<TagAssociation> getTagAssociationCollection() {
        return tagAssociationCollection;
    }

    public void setTagAssociationCollection(Collection<TagAssociation> tagAssociationCollection) {
        this.tagAssociationCollection = tagAssociationCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (componentId != null ? componentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ComponentSpec)) {
            return false;
        }
        ComponentSpec other = (ComponentSpec) object;
        if ((this.componentId == null && other.componentId != null) || (this.componentId != null && !this.componentId.equals(other.componentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "test.ComponentSpec[ componentId=" + componentId + " ]";
    }
    
}
