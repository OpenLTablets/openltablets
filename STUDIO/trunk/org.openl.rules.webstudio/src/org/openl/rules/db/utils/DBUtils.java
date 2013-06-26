package org.openl.rules.db.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.config.ConfigurationManager;
import org.openl.rules.webstudio.web.install.InstallWizard;

public class DBUtils {
    private static final String USERS_TABLE = "openluser";
    private final Log log = LogFactory.getLog(InstallWizard.class);

    private Map<String, Object> dbErrors;
    private ConfigurationManager sqlErrorsConfig;
    private String sqlErrorsFilePath = "db/sql-errors.properties";
    private  String tableForValidation = "schema_version";

    public DBUtils() {
        sqlErrorsConfig = new ConfigurationManager(false, null, System.getProperty("webapp.root") + "/WEB-INF/conf/" + sqlErrorsFilePath);
        dbErrors = sqlErrorsConfig.getProperties();
    }

    /**
     * Returns connection (session) to a specific database.
     * 
     * @param dbDriver - database driver
     * @param dbPrefix - database prefix
     * @param dbUrl - database url
     * @param login - database login
     * @param password - database password
     * @return connection (session) to a specific database.
     */
    public Connection createConnection(String dbDriver, String dbPrefix, String dbUrl, String login, String password) {
        Connection conn = null;
        int errorCode = 0;

        try {
            Class.forName(dbDriver);
            conn = DriverManager.getConnection((dbPrefix + dbUrl), login, password);
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.getMessage(), cnfe);
            throw new ValidatorException(new FacesMessage("Incorrectd database driver"));
        } catch (SQLException sqle) {
            errorCode = sqle.getErrorCode();
            String errorMessage = (String) dbErrors.get("" + errorCode);

            if (errorMessage != null) {
                log.error(sqle.getMessage(), sqle);
                throw new ValidatorException(new FacesMessage(errorMessage));
            } else {
                log.error(sqle.getMessage(), sqle);
                throw new ValidatorException(new FacesMessage("Incorrect database URL, login or password"));
            }
        }

        return conn;
    }

    /**
     * Validates database exists or not.
     * 
     * @param conn is a connection (session) with a specific database.
     * @return true if database exists
     */
    public boolean isDatabaseExists(Connection conn) {
        String schemaName = "";
        try {
            schemaName = conn.getCatalog();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return getDbSchemaList(conn).contains(schemaName);
    }

    /**
     * Validates flyway table 'schema_version' exists or not.
     * 
     * @param conn is a connection (session) with a specific database.
     * @return true if table 'schema_version' exists into DB
     */
    public boolean isTableSchemaVersionExists(Connection conn) throws SQLException {
        return getDBOpenlTables(conn).contains(tableForValidation);
    }

    public boolean isTableUsersExists(Connection conn) throws SQLException {
        return getDBOpenlTables(conn).contains(USERS_TABLE);
    }

    /**
     * Returns a list of schemas form OpenL database
     * 
     * @param conn is a connection (session) with a specific database.
     * @return a list of database schemas
     */
    private List<String> getDbSchemaList(Connection conn) {
        List<String> dbSchemasList = new ArrayList<String>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getCatalogs();

            while (rs.next()) {
                String databaseName = rs.getString("TABLE_CAT");
                dbSchemasList.add(databaseName);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return dbSchemasList;
    }

    /**
     * Returns a list of tables from OpenL database
     * 
     * @param conn is a connection (session) with a specific database.
     * @return a list of tables from OpenL database
     */
    private List<String> getDBOpenlTables(Connection conn) {
        List<String> dbOpenlTablesList = new ArrayList<String>();

        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getTables(null, null, "%", null);

            while (rs.next()) {
                String dbTableName = rs.getString("TABLE_NAME");
                dbOpenlTablesList.add(dbTableName);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return dbOpenlTablesList;
    }

    /**
     * Executes SQL script for changing columns and colums datatypes into
     * existing MySQL database
     * 
     * @param sqlFilePath a path to SQL script
     * @param connection - used for executing a static SQL
     *            statement and returning the results it produces.
     */
    public void executeSQL(String sqlFilePath, Connection connection) {
        List<String> queries = new ArrayList<String>();
        BufferedReader buffReader = null;
        try {
            buffReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(sqlFilePath)));
            StringBuilder sb = new StringBuilder();
            String str;

            while ((str = buffReader.readLine()) != null) {
                sb.append(str);
                if (str.isEmpty()) {
                    queries.add(sb.toString());
                    // clear the StringBuilder content
                    sb.delete(0, sb.length());
                }
            }
            
            if (sb.length() > 0) {
                queries.add(sb.toString());
            }

            buffReader.close();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (buffReader != null) {
                try {
                    buffReader.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        Statement st = null;
        try {
            st = connection.createStatement();
            for (String query : queries) {
                st.addBatch(query);
            }
            st.executeBatch();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}
