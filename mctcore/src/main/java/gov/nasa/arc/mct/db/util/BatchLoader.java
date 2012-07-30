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

import gov.nasa.arc.mct.components.AbstractComponent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Interface for loading telemetry into MCT database.
 */
public interface BatchLoader {
    
    /** Root of taxonomy ID. */
    public static final String ROOT_PATH = "/";

    /**
     * Opens a database connection.
     * @param dbname dbname
     * @param host host
     * @param port port
     * @param user dbuser
     * @param passwd dbuser's password
     * @param profileSQL optional for debugging
     * @return the connection
     * @throws SQLException upon db error
     * @throws ClassNotFoundException upon jdbc class not found error
     */
    public Connection createConnection(String dbname, String host, String port, 
                    String user, String passwd, boolean profileSQL) throws SQLException, ClassNotFoundException;
    
    
    /**
     * Validates the database state.
     * @param connection the database connection
     * @throws Exception if invalid
     */
    public void validateDatabaseCreationState(Connection connection) throws Exception;
        
    /**
     * Given a collection of taxonomy paths, persists each new path if it has not already been persisted.
     * The pathnames are fully qualified.
     * @param connection the database connection
     * @param taxonomyPaths collection of taxonomy paths
     * @throws Exception upon error
     */
    public void persistTaxonomies(Connection connection, Collection<String> taxonomyPaths) throws Exception;
    
    
    /** 
     * Given a collection of symbols, persists each symbol to its parent taxonomy node.
     * @param connection the database connection
     * @param componentType the MCT component type to persist
     * @param parentKey the parent node
     * @param newSymbols the collection of symbols
     * @throws Exception upon error
     */
    public void persistParameters(Connection connection,  Class<? extends AbstractComponent> componentType,
                    String parentKey, Collection<ParameterInfo> newSymbols) throws Exception;


    /**
     * Set the component tagging info.
     * @param componentTag the tag
     * @param description the tag description
     */
    public void setTaggingInfo(String componentTag, String description);

    /**
     * Set the owner for components and taxonomy nodes.
     * @param owner the owner
     */
    public void setOwner(String owner);

}