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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Telemetry batch loader.
 */
public class BatchLoaderImpl implements BatchLoader {
    private static final Logger logger = LoggerFactory.getLogger(BatchLoaderImpl.class);
    private static final String containerComponentClassName = "gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent";
    private static boolean tolerateDuplicates = true; // may expose this to API
    private  Class <? extends AbstractComponent> componentClass = null;
    private String tag = "";
    private String tagDescription;
    private String owner = "admin";
    
    /**
     * Instantiate a batch loader.
     * @param componentClass the component type of the MCT telemetry component to be loaded
     */
    public BatchLoaderImpl(Class<? extends AbstractComponent> componentClass) {
        super();
        this.componentClass = componentClass;
    }
    
    @Override
    public Connection createConnection(String dbname, String host, String port, String user, String passwd, boolean profileSQL) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        Class.forName("com.mysql.jdbc.Driver");
        String url = makeURL(dbname, host, port, user, passwd, profileSQL);
        connection = DriverManager.getConnection(url);
        logger.info("opening ConnectionURL: " + url);

        return connection;
    }

    @Override
    public void persistTaxonomies(Connection connection, Collection<String> taxonomyPaths) throws Exception {

        String compID = null;
        PreparedStatement relationshipStatement = null;
        PreparedStatement csStatement = null;
        PreparedStatement parentObjVersionStatement = null;
        int distinctTaxoCount = 0;
        long startTime = System.currentTimeMillis();
        Map<String, PersistedTaxonomyInfo> persistedTaxonomies = new HashMap<String, PersistedTaxonomyInfo>();

        logger.info("Before persisting taxonomies, component_spec count: " + countRows(connection, "component_spec"));
        populatePersistedTaxonomiesCache(connection, persistedTaxonomies);

        try {
            relationshipStatement = connection
            .prepareStatement("insert into component_relationship  (component_id, associated_component_id, seq_no) values(?, ?, ?)");
            csStatement = connection.prepareStatement("insert into  component_spec "
                            + "(component_id, component_name, owner,  component_type, external_key, creator_user_id, date_created) "
                            + "values (?,  ?,  ?, ?,  ?,  ?, NOW())");
            csStatement.setString(3, owner);
            csStatement.setString(4, containerComponentClassName);
            csStatement.setString(6, owner);

            parentObjVersionStatement = connection
            .prepareStatement("update component_spec set obj_version = ? where component_id = ?");

            connection.setAutoCommit(false);
            for (String taxoPath : taxonomyPaths) {
                distinctTaxoCount++;

                PersistedTaxonomyInfo taxoInfo = persistedTaxonomies.get(taxoPath);  

                if (taxoInfo == null) {
                    TaxoWorkOrder workOrder = makeWorkOrder(connection, taxoPath, persistedTaxonomies);
                    if (workOrder.nodesToCreate.size() == 0) {
                        logger.warn("Make work order returned no work: " + taxoPath);
                    } else {
                        int maxSeqNo = 0;
                        String currentParentCompID = workOrder.parentNode.getComponent_id();
                        int currentParentObjVersion = workOrder.parentNode.getObj_version();
                        String pathToCreate = workOrder.parentNode.getTaxoPath() == ROOT_PATH ? "" : workOrder.parentNode.getTaxoPath();
                        int pathCount = 0;
                        for (String nodeToCreate : workOrder.nodesToCreate) {

                            compID = IdGenerator.nextComponentId();
                            pathToCreate = pathToCreate + "/" + nodeToCreate;

                            // Root parent is always pre existing, with children possibly. So its initial
                            // sequence number needs to be known. Since non top level nodes will be parented by a newly
                            // created node and have one child eventually, their seq no can be one.
                            maxSeqNo = (++pathCount == 1) ? workOrder.parentNode.getSeq_no() + 1 : 1;

                            relationshipStatement.setString(1, currentParentCompID);
                            relationshipStatement.setString(2, compID);
                            relationshipStatement.setInt(3, maxSeqNo);
                            relationshipStatement.addBatch();

                            csStatement.setString(1, compID);
                            String opsname = nodeToCreate;
                            csStatement.setString(2, opsname);
                            csStatement.setString(5, pathToCreate);
                            csStatement.addBatch();

                            parentObjVersionStatement.setInt(1, currentParentObjVersion + 1);
                            parentObjVersionStatement.setString(2, currentParentCompID);
                            parentObjVersionStatement.addBatch();

                            // update cache state: a newly created node and an updated obj version for its parent
                            persistedTaxonomies.put(pathToCreate, new PersistedTaxonomyInfo(compID, pathToCreate,
                                            false /* init as sub node */, 0 /* new node */, 0 /* new child has no children */));
                            currentParentCompID = compID;
                        }

                        // the last item in the work order is marked as pui parent.
                        PersistedTaxonomyInfo puiParent = persistedTaxonomies.get(pathToCreate);
                        assert puiParent != null : "could not find most recently created node path in persisted taxonomy cache";
                        assert pathToCreate.equals(taxoPath);
                        puiParent.setPuiParent(true);
                        puiParent.setObj_version(0); // newly created
                        puiParent.setSeq_no(0); // no children
                        puiParent.setTaxoPath(pathToCreate);

                        int[] csCounts = csStatement.executeBatch();
                        int[] parentCounts = relationshipStatement.executeBatch();
                        int[] objVersionCounts = parentObjVersionStatement.executeBatch();

                        if (!validateUpdate(csCounts, parentCounts, objVersionCounts)) {
                            connection.rollback();
                        } else {
                            connection.commit();
                        }
                    }
                } 
            }
        } finally {
            closeAll(new Statement[] { relationshipStatement, csStatement },  null);
        }

        logger.info("After persist all taxonomies component_spec count: " + countRows(connection, "component_spec")
                        + " Number of Taxo Paths added: " + (distinctTaxoCount - 1) + " batch time: "
                        + (System.currentTimeMillis() - startTime) + " persistedTaxonomies size "
                        + persistedTaxonomies.size());
        connection.setAutoCommit(true);
    }


    @Override
    public void persistParameters(Connection connection,Class<? extends AbstractComponent> componentType,
                    String parentKey, Collection<ParameterInfo> newSymbols) throws Exception {

        Map<String, PersistedTaxonomyInfo> persistedTaxonomies = new HashMap<String, PersistedTaxonomyInfo>();
        Map<String,String> persistedSymbols = null;
        PreparedStatement relationshipStatement = null;
        PreparedStatement newChildSpecStatement = null;
        PreparedStatement updateParentStatement = null;
        PreparedStatement tagStatement = null;
        int[] csCounts = new int[0];
        int[] crCounts = new int[0];
        int[] tagCounts = new int[0];
        int[] objVersionCounts = new int[0];
        long startTime = System.currentTimeMillis();
        boolean shouldTag = shouldTag(connection, tag);
        
        logger.info("Before persisting parameters, component_spec count: " + countRows(connection, "component_spec"));
        if (newSymbols == null || newSymbols.isEmpty()) {
            logger.warn("Empty of null symbol list.");  
            return;
        }
        AbstractComponent.checkBaseComponentRequirements(this.componentClass);
        populatePersistedTaxonomiesCache(connection, persistedTaxonomies);
        // get symbols that are both in the database and in the input parameter file.
        persistedSymbols = fetchPersistedSymbols(connection, newSymbols);

        if (shouldTag) {
            addTag(connection, tag, tagDescription);
        }

        PersistedTaxonomyInfo parent = persistedTaxonomies.get(parentKey);
        if (parent == null) {
            throw new Exception ("The parent taxonomy path must exist: "+parentKey);  
        }

        try {

            updateParentStatement = connection
            .prepareStatement("update component_spec set obj_version = ? where component_id = ?");

            relationshipStatement = connection
            .prepareStatement("insert into component_relationship  (component_id, associated_component_id, seq_no) values(?, ?, ?)");
            newChildSpecStatement = connection
            .prepareStatement("insert into  component_spec "
                            + "(component_id, component_name, owner, external_key, component_type, creator_user_id, date_created) "
                            + "values (?,     ?,              ?,             ?,    ?,       ?, NOW())");
            newChildSpecStatement.setString(3, owner);
            newChildSpecStatement.setString(5, componentType.getName());
            newChildSpecStatement.setString(6, owner);

            tagStatement = connection
            .prepareStatement("insert into tag_association (component_id, tag_id) values(?, ?)");

            int runningPuiCount = 0;
            int puiParentCount = 0;
            connection.setAutoCommit(false);

            if (shouldTag) {
                for (String comp_external_key: persistedSymbols.keySet()) {
                    if (!isComponentTagged(connection, comp_external_key, tag)) {
                        tagStatement.setString(1, persistedSymbols.get(comp_external_key));
                        tagStatement.setString(2, tag);
                        tagStatement.addBatch();
                    }
                }
            }

            int directDescendentCount = 0;
            for (ParameterInfo parameterInfo : newSymbols) {
                if (tolerateDuplicates && persistedSymbols.containsKey(parameterInfo.getSymbolName())) {
                    logger.debug("Symbol already is persisted: "+parameterInfo.getSymbolName());
                    continue;
                }
                directDescendentCount++;
                runningPuiCount++;

                puiParentCount++;
                String parentCompID = parent.getComponent_id();
                int parentObjectVersion = parent.getObj_version() + 1;// refresh GUI

                updateParentStatement.setInt(1, parentObjectVersion);
                updateParentStatement.setString(2, parentCompID);
                updateParentStatement.addBatch();

                String compID = IdGenerator.nextComponentId();
                int maxSeqNo = parent.getSeq_no();  
                parent.setSeq_no(++maxSeqNo); // update cache
                relationshipStatement.setString(1, parentCompID);
                relationshipStatement.setString(2, compID);
                relationshipStatement.setInt(3, maxSeqNo);
                relationshipStatement.addBatch();

                newChildSpecStatement.setString(1, compID);
                newChildSpecStatement.setString(2, parameterInfo.getDisplayName()); // component name
                newChildSpecStatement.setString(4, parameterInfo.getSymbolName()); // external key
                newChildSpecStatement.addBatch();

                if (shouldTag) {
                    tagStatement.setString(1, compID);
                    tagStatement.setString(2, tag);
                    tagStatement.addBatch();
                }

                if ((runningPuiCount % 1000) == 0) {
                    logger.debug("excute at runningCount "+runningPuiCount + " puiParentCount " +puiParentCount+ " parentpath "+parentKey + " max seqno: " + maxSeqNo + " parentObjectVersion: " + parentObjectVersion);
                    csCounts = newChildSpecStatement.executeBatch();
                    crCounts = relationshipStatement.executeBatch();
                    if (shouldTag) tagCounts = tagStatement.executeBatch();
                    objVersionCounts = updateParentStatement.executeBatch();
                    if (!validateUpdate(csCounts, crCounts, tagCounts, objVersionCounts)) {
                        logger.error("Rollback persisting puis");
                        connection.rollback();
                        throw new Exception("Rollback persisting puis"); 
                    } 
                }
            }

            csCounts = newChildSpecStatement.executeBatch();
            crCounts = relationshipStatement.executeBatch();
            if (shouldTag) tagCounts = tagStatement.executeBatch();
            objVersionCounts = updateParentStatement.executeBatch(); 
            if (!validateUpdate(csCounts, crCounts, tagCounts, objVersionCounts)) {
                logger.error("Rollback persisting puis");
                connection.rollback();
                throw new Exception("Rollback persisting puis");
            } else {
                connection.commit();
            } 

            logger.info("After persisting parameters, component_spec count: " + countRows(connection, "component_spec")
                            + " Parent path: " + parentKey 
                            + " Number of component_spec added: " + csCounts.length + " batch time: "
                            + (System.currentTimeMillis() - startTime));

        }  finally {
            closeAll(new Statement[] { newChildSpecStatement,  relationshipStatement }, null);
        }
        connection.setAutoCommit(true);
    }

    /**
     * Validate each update in the batch.
     * http://leejeok.wordpress.com/2008/05/21/java-mysql-mass-update-using-batch-updating Return values are > 1 we are good.  
     * Else determine why command[i] was bad. A value of SUCCESS_NO_INFO means update
     * was successfully executed but MySQL server unable to determine the number of rows affected. A value of
     * EXECUTE_FAILED means MySQL server rejected the query for error.
     */
    private boolean validateUpdate(int[]... countsList) {

        assert countsList.length > 0;
        for (int[] counts : countsList) {
            boolean OK = true;
            for (int i = 1; i < counts.length; i++) {
                if ((counts[i] == Statement.SUCCESS_NO_INFO) || (counts[i] == Statement.EXECUTE_FAILED)) {
                    OK = false;
                }
            }
            if (! OK) {
                logger.error("Validating batch update counts");
                return false;
            }
        }
        return true;
    }

    private void closeAll(Statement[] statements, ResultSet[] rs) {
        try {
            if (statements != null) {
                for (int i = 0; i < statements.length; i++) {
                    if (statements[i] != null) {
                        statements[i].close();
                    }
                }
            }
            if (rs != null) {
                for (int i = 0; i < rs.length; i++) {
                    if (rs[i] != null) {
                        rs[i].close();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Closing All, perhaps csCount is zero.",e);
        }
    }

    /** Returns row count for tableName. */
    private static int countRows(Connection conn, String tableName) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int rowCount = -1;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            rs.next();
            rowCount = rs.getInt(1);
        } finally {
            rs.close();
            stmt.close();
        }
        return rowCount;
    }

    /** Fetches a taxo node given its path. Throws exception upon error. */
    private PersistedTaxonomyInfo fetchTaxoInfoByTaxoID(Connection conn, String taxoPath) throws Exception {

        Statement stmt = null;
        ResultSet rs = null;
        String parentID = null;
        int objVersion = -1;
        int maxSeqNo = -1;
        try {
            stmt = conn.createStatement();
            rs = stmt
            .executeQuery(
                            "select cs.component_id, cs.obj_version from component_spec cs " +
                            "where cs.component_type='gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent' " +
                            "and cs.external_key = '"   + taxoPath + "'");
            rs.next();
            parentID = rs.getString(1);
            objVersion = rs.getInt(2);
            if (parentID == null) {
                throw new Exception("comp ID is null.");
            }
            maxSeqNo = fetchMaxSequenceByCompID(conn, parentID);
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return new PersistedTaxonomyInfo(parentID, taxoPath, false, objVersion, maxSeqNo);
    }

    /** 
     * Returns the subset of symbols that are in the symbolsToBeLoaded list, and in the database.
     * Also fetches 
     * @param symbolsToBeLoaded 
     */
    private Map<String,String> fetchPersistedSymbols(Connection conn, 
                    Collection<ParameterInfo> symbolsToBeLoaded) throws Exception {

        Map<String,String> alreadyPersistedSymbols = new HashMap<String,String>();

       
        ResultSet rs2 = null;
        PreparedStatement stmt2 = null;
        for (ParameterInfo p: symbolsToBeLoaded) {
            try {
                stmt2 = conn.prepareStatement(
                                "select external_key, component_id from component_spec where component_type=? and external_key=?");
                stmt2.setString(1, componentClass.getName());
                stmt2.setString(2, p.getSymbolName());
                rs2 = stmt2.executeQuery();
                if (rs2.next()) {
                    alreadyPersistedSymbols.put(rs2.getString(1), rs2.getString(2));
                }

            }  finally {
                if (stmt2 != null)
                    stmt2.close();
                if (rs2 != null)
                    rs2.close();
            }
        }
        return alreadyPersistedSymbols;
    }

    /** Returns true if this component exists, untagged with tag.*/
    private boolean isComponentTagged(Connection conn, String external_key, String tag) throws Exception {

        boolean exists = false;
        PreparedStatement stmt2 = conn.prepareStatement(
        "select external_key from component_spec cs join tag_association ta  on cs.component_id=ta.component_id where cs.component_type=? and cs.external_key=? and tag_id=?");
        stmt2.setString(1, componentClass.getName());
        stmt2.setString(2, external_key);
        stmt2.setString(3, tag);
        ResultSet rs2 = null;

        try {
            rs2 = stmt2.executeQuery();
            exists = rs2.next();
        }  finally {
            if (stmt2 != null)
                stmt2.close();
            if (rs2 != null)
                rs2.close();
        }
        return exists;
    }
    
    /** Fetches max sequence number for a parent component. */
    private int fetchMaxSequenceByCompID(Connection conn, String compID) throws Exception {

        Statement stmt2 = null;
        ResultSet rs2 = null;
        int maxSeqNo = -1;
        try {
            // If parent has no children, MAX() returns NULL if there were no matching rows; and if SQL NULL, getInt
            // returns 0.
            stmt2 = conn.createStatement();
            rs2 = stmt2.executeQuery("select  max(seq_no) from component_relationship where component_id = '" + compID
                            + "'");
            rs2.next();
            maxSeqNo = rs2.getInt(1);

        }  finally {
            if (stmt2 != null)
                stmt2.close();
            if (rs2 != null)
                rs2.close();
        }
        return maxSeqNo;
    }

 


    /** Specifies a taxonomy node path creation task. */
    static class TaxoWorkOrder {
        private List<String> nodesToCreate;
        private PersistedTaxonomyInfo parentNode;

        public TaxoWorkOrder(List<String> nodesToCreate, PersistedTaxonomyInfo parentNode) {
            super();
            this.nodesToCreate = nodesToCreate;
            this.parentNode = parentNode;
        }

    }

    @Override
    public void validateDatabaseCreationState(Connection connection) throws Exception {
        try {
            fetchTaxoInfoByTaxoID(connection, ROOT_PATH);
        } catch (Exception e) {
            throw new Exception ("The top level (root) taxonomy nodes have not been added. Run the sql script that loads root taxonomies.");      
        }
    }

    /** Fetches list of already existing taxo nodes. */
    void populatePersistedTaxonomiesCache(Connection conn,
                    Map<String, PersistedTaxonomyInfo> persistedTaxonomies) throws Exception {

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt
            .executeQuery("select external_key, component_id, obj_version from component_spec where component_type = 'gov.nasa.arc.mct.core.components.TelemetryDataTaxonomyComponent'");
            while (rs.next()) {
                String taxoPathValue = rs.getString("external_key");
                String compID = rs.getString("component_id");
                if (taxoPathValue != null && taxoPathValue.startsWith(ROOT_PATH)) {
                    int objVersion = rs.getInt("obj_version");
                    
                    int maxSeqNo = fetchMaxSequenceByCompID(conn, compID) ;; // max seq needed to add child to pre existing node
                    PersistedTaxonomyInfo info = new PersistedTaxonomyInfo(compID, taxoPathValue,
                                    false, objVersion, maxSeqNo);
                    persistedTaxonomies.put(taxoPathValue, info);
                }
            }
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return;
    }


    /**
     * Given a path name to create, identifies the parent to start building the new node(s), as well as a work order of
     * nodes that are to be created under that parent. It inspects the cache of currently persisted taxonomies, to
     * determine the highest node that already exists (this is the parent).
     * 
     * All top level nodes are pre existing, and a pre exising parent may have children so we need to know its a max
     * sequence number.
     * 
     * @param conn the connection
     * @param input fqTaxoPathToCreate fully qualified path to be created
     * @param persistedTaxonomies cache of currently persisted taxonomies
     * @param completedSubpaths
     * @return a work order with info about the parent that will get the new node(s), and the ordered list of new nodes
     * @throws Exception
     */
    private TaxoWorkOrder makeWorkOrder(Connection conn, String fqTaxoPathToCreate,
                    Map<String, PersistedTaxonomyInfo> persistedTaxonomies) throws Exception {

        List<String> nodesToCreate;
        PersistedTaxonomyInfo highestPersistedNode;

        try {
            highestPersistedNode = fetchTaxoInfoByTaxoID(conn, ROOT_PATH);

            String pathToCreateMinusHighestPersisted = fqTaxoPathToCreate.substring(highestPersistedNode.getTaxoPath().length());

            String nextPath = highestPersistedNode.getTaxoPath();

            while (true) {
                if (pathToCreateMinusHighestPersisted == null) {
                    continue;
                }
                int nextSeparator = pathToCreateMinusHighestPersisted.indexOf('/');

                String nextNodeToCreate = nextSeparator > 0 ? pathToCreateMinusHighestPersisted.substring(0,
                                nextSeparator) : pathToCreateMinusHighestPersisted;

                nextPath = nextPath + nextNodeToCreate;

                if (persistedTaxonomies.containsKey(nextPath)) {

                    highestPersistedNode = fetchTaxoInfoByTaxoID(conn, nextPath);
                    pathToCreateMinusHighestPersisted = fqTaxoPathToCreate.substring(highestPersistedNode.getTaxoPath()
                                    .length() + 1);

                } else {
                    break;
                }
            }
            nodesToCreate = Arrays.asList(pathToCreateMinusHighestPersisted.split("/"));

        } catch (Exception e) {
            logger.warn("Could not parse persisted taxo path. Ensure it is fully qualified." + fqTaxoPathToCreate);
            e.printStackTrace();
            return new TaxoWorkOrder(Collections.<String> emptyList(), null);
        }

        if (nodesToCreate.size() == 0) {
            logger.warn("Work order produced no work: " + fqTaxoPathToCreate);
        }

        return new TaxoWorkOrder(nodesToCreate, highestPersistedNode); // ensure highest persisted is incremented
    }

    private String makeURL(String dbname, String host, String port, String user, String passwd, boolean profileSQL) {
        String partURL = "jdbc:mysql://" + host + ":" + new Integer(port) + "/";
        if (profileSQL) {
            return partURL + dbname + "?user=" + user + "&password=" + passwd + "&profileSQL=true";
        } else {
            return partURL + dbname + "?user=" + user + "&password=" + passwd;
        }
    }

    /**
     * Returns true if the component should be tagged.
     * 
     * @throws Exception upon invalid query
     * @return true if a tagID does not already exist and if the tag is valid
     */
    private static boolean shouldTag(Connection conn, String tag) throws Exception {
        if (tag == null || tag.isEmpty()) {
            return false;
        }
        Statement stmt = null;
        ResultSet rs = null;
        int count = -1;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select count(*) from tag where tag_id = '" + tag + "'");
            rs.next();
            count = rs.getInt(1);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return count != 0;
    }

    /**
     * Adds a tag for this recon ID. The tag_property first subfield is used to specify order so that the recon ID
     * sequence and date are recorded.
     * @param conn the db 
     * @param tag the tag
     * @param the tag description
     * @throws Exception upon error
     */
    private static void addTag(Connection conn, String tag, String tagDescription) throws Exception {

        String mostRecentTag = fetchNextReconOrdinal(conn);
        String nextOrdinal = mostRecentTag != null ? mostRecentTag : "1";
        DateFormat dfm = new SimpleDateFormat("MMM d HH:mm:ss z yyyy");
        dfm.setTimeZone(TimeZone.getDefault());
        Date now = new Date();
        String newTagProperty = nextOrdinal + ":recon:" + dfm.format(now) + ":" +  tagDescription;
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("insert into tag (tag_id, tag_property) values ('" + tag + "'  , '"
                            + newTagProperty + "')");

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (stmt != null)
                stmt.close();
        }
        return;
    }

    /**
     * Fetches the most recently added recon tag's ordinal value. eg) defaultReconId | 1:recon:Sep 19 15:04:18 PDT 2011
     * @param conn the db
     * @return ordinal for most recent tag + 1, else null if there are no tags
     * @throws Exception upon error
     */
    private static String fetchNextReconOrdinal(Connection conn) throws Exception {

        Statement stmt = null;
        ResultSet rs = null;
        int next = -1;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select tag_property from tag where tag_property like '%:recon:%'");
            String tprop = null;
            int nextCandidate = -1;
            while (rs.next()) {
                tprop = rs.getString(1);
                String mostRecentOrdinal = tprop.substring(0, tprop.indexOf(':'));
                nextCandidate = Integer.parseInt(mostRecentOrdinal);
                if (nextCandidate > next) {
                    next = nextCandidate;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
        return next == -1 ? null : String.valueOf(next + 1);
    }

    @Override
    public void setTaggingInfo(String componentTag, String description) {
        this.tag = componentTag;
        this.tagDescription = description;
    }

    @Override
    public void setOwner(String owner) {
       this.owner = owner;
    }

}
