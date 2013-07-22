package org.openl.rules.db.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;
import org.openl.rules.db.utils.DBUtils;

import com.googlecode.flyway.core.Flyway;

public class DBMigrationBean  {
    private static final String SCHEMA_SEPARATOR = ".";
    private static final String MIGRATE_WITHOUT_FLYWAY_SCRIPT = "/db/without_flyway/Migrate_mysql_first_time.sql";
    private static final String INITIAL_VERSION_OF_MIGRATION = "2";

    private final Log log = LogFactory.getLog(DBMigrationBean.class);
    
    private String dbDriver;
    private String dbLogin;
    private String dbPassword;
    private String dbPrefix;
    private String dbUrl;
    private String dbSchema;
    private String dbUrlSeparator;
    private DataSource dataSource;
    
    public String init() {
        String prefix = dbUrl.split(dbUrlSeparator)[0] + dbUrlSeparator;
        String url = dbUrl.split(dbUrlSeparator)[1];
        DBUtils dbUtils = new DBUtils();
        Connection dbConnection = dbUtils.createConnection(dbDriver, prefix, url, dbLogin, dbPassword);
        try {
            dbConnection.setAutoCommit(false);
            Dialect dialect = new StandardDialectResolver().resolveDialect(dbConnection.getMetaData());

            Flyway flyway = flywayInit(dialect);
            if (isExistMySQLWithoutFlyway(dbConnection, dbUtils, dialect)) {
                log.info("Migrate DB to newer version and initialize flyway");
                migrateMySQLToNewVersion(dbConnection, dbUtils);
                flyway.setInitVersion(INITIAL_VERSION_OF_MIGRATION);
                flyway.setInitDescription("Migrated from existed WebStudio without flyway");
                flyway.init();
                log.info("Migration successful");
            } else {
                flyway.migrate();
            }
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            try {
                dbConnection.rollback();
            } catch (SQLException e1) {
                log.error(e.getMessage(), e);
            }
        } finally {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        return "";
    }

    /**
     * Creates and initializes the Flyway metadata table.
     */
    private Flyway flywayInit(Dialect dialect) {
        // Set path to V1_Base_version.sql script
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        String schemaPrefix = StringUtils.isBlank(dbSchema) ? "" : StringUtils.trim(dbSchema) + SCHEMA_SEPARATOR;

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schemaPrefix", schemaPrefix);
        placeholders.put("identity_column", getIdentityColumn(dialect));
        placeholders.put("create_hibernate_sequence", getCreateHibernateSequence(dialect));
        placeholders.put("bigint", dialect.getTypeName(Types.BIGINT));
        placeholders.put("longtext", dialect.getTypeName(Types.VARCHAR, 1000, 0, 0));
        flyway.setPlaceholders(placeholders);

        flyway.setLocations(getScriptLocations(dialect));

        return flyway;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbLogin() {
        return dbLogin;
    }

    public void setDbLogin(String dbLogin) {
        this.dbLogin = dbLogin;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbPrefix() {
        return dbPrefix;
    }

    public void setDbPrefix(String dbPrefix) {
        this.dbPrefix = dbPrefix;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public String getDbUrlSeparator() {
        return dbUrlSeparator;
    }

    public void setDbUrlSeparator(String dbUrlSeparator) {
        this.dbUrlSeparator = dbUrlSeparator;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private boolean isExistMySQLWithoutFlyway(Connection dbConnection, DBUtils dbUtils, Dialect dialect) throws SQLException {
        return dialect instanceof MySQLDialect
                && dbUtils.isDatabaseExists(dbConnection)
                && !dbUtils.isTableSchemaVersionExists(dbConnection)
                && dbUtils.isTableUsersExists(dbConnection);
    }

    private void migrateMySQLToNewVersion(Connection dbConnection, DBUtils dbUtils) throws SQLException {
        dbUtils.executeSQL(MIGRATE_WITHOUT_FLYWAY_SCRIPT, dbConnection);
    }

    private String getIdentityColumn(Dialect dialect) {
        if (dialect.supportsIdentityColumns()) {
            String dataType = dialect.hasDataTypeInIdentityColumn() ? dialect.getTypeName(Types.BIGINT) : "";
            return dataType + " " + dialect.getIdentityColumnString(Types.BIGINT);
        } else {
            return dialect.getTypeName(Types.BIGINT) + " not null";
        }
    }

    private String getCreateHibernateSequence(Dialect dialect) {
        if (dialect.supportsIdentityColumns()) {
            return "";
        } else {
            String[] strings = dialect.getCreateSequenceStrings("hibernate_sequence", 1, 1);
            StringBuilder sb = new StringBuilder();
            for (String s : strings) {
                sb.append(s);
            }
            sb.append(";");
            return sb.toString();
        }
    }

    private String[] getScriptLocations(Dialect dialect) {
        List<String> locations = new ArrayList<String>();
        locations.add("db/migration/common");

        // DB-specific scripts can be added here:
        if (dialect instanceof Oracle8iDialect) {
            locations.add("db/migration/oracle");
        }
//        if (dialect instanceof MySQLDialect) {
//            locations.add("db/migration/mysql");
//        }
//        if (dialect instanceof SQLServerDialect) {
//            locations.add("db/migration/mssqlserver");
//        }

        return locations.toArray(new String[locations.size()]);
    }

}
