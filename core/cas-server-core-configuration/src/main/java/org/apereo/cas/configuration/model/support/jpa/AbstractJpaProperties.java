package org.apereo.cas.configuration.model.support.jpa;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Common properties for all jpa configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public abstract class AbstractJpaProperties implements Serializable {

    private static final long serialVersionUID = 761486823496930920L;

    /**
     * The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc)
     * which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
     */
    private String dialect = "org.hibernate.dialect.HSQLDialect";

    /**
     * Hibernate feature to automatically validate and exports DDL to the schema.
     * By default, creates and drops the schema automatically when a session is starts and ends
     */
    private String ddlAuto = "create-drop";

    /**
     * The JDBC driver used to connect to the database.
     */
    @RequiredProperty
    private String driverClass = "org.hsqldb.jdbcDriver";

    /**
     * The database connection URL.
     */
    @RequiredProperty
    private String url = "jdbc:hsqldb:mem:cas-hsql-database";

    /**
     * The database user.
     * 
     * The database user must have sufficient permissions to be able to handle
     * schema changes and updates, when needed.
     */
    @RequiredProperty
    private String user = "sa";

    /**
     * The database connection password.
     */
    @RequiredProperty
    private String password = StringUtils.EMPTY;

    /**
     * Qualifies unqualified table names with the given catalog in generated SQL.
     */
    private String defaultCatalog;

    /**
     * Qualify unqualified table names with the given schema/tablespace in generated SQL.
     */
    private String defaultSchema;

    /**
     * The SQL query to be executed to test the validity of connections.
     */
    private String healthQuery = StringUtils.EMPTY;

    /**
     * Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
     */
    private String idleTimeout = "PT10M";

    /**
     * Attempts to do a JNDI data source look up for the data source name specified.
     * Will attempt to locate the data source object as is, or will try to return a proxy
     * instance of it, in the event that {@link #dataSourceProxy} is used.
     */
    private String dataSourceName;

    /**
     * Additional settings provided by Hibernate in form of key-value pairs.
     *
     * @see org.hibernate.cfg.AvailableSettings
     */
    private Map<String, String> properties = new HashMap<>();

    /**
     * Database connection pooling settings.
     */
    private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

    /**
     * Controls the amount of time that a connection can be out of the pool before a message
     * is logged indicating a possible connection leak.
     */
    private int leakThreshold = 3_000;

    /**
     * A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
     */
    private int batchSize = 5;

    /**
     * Set the pool initialization failure timeout.
     * <ul>
     *   <li>Any value greater than zero will be treated as a timeout for pool initialization.
     *       The calling thread will be blocked from continuing until a successful connection
     *       to the database, or until the timeout is reached.  If the timeout is reached, then
     *       a {@code PoolInitializationException} will be thrown. </li>
     *   <li>A value of zero will <i>not</i>  prevent the pool from starting in the
     *       case that a connection cannot be obtained. However, upon start the pool will
     *       attempt to obtain a connection and validate that the {@code connectionTestQuery}
     *       and {@code connectionInitSql} are valid.  If those validations fail, an exception
     *       will be thrown.  If a connection cannot be obtained, the validation is skipped
     *       and the the pool will start and continue to try to obtain connections in the
     *       background.  This can mean that callers to {@code DataSource#getConnection()} may
     *       encounter exceptions. </li>
     *   <li>A value less than zero will <i>not</i> bypass any connection attempt and
     *       validation during startup, and therefore the pool will start immediately.  The
     *       pool will continue to try to obtain connections in the background. This can mean
     *       that callers to {@code DataSource#getConnection()} may encounter exceptions. </li>
     * </ul>
     * Note that if this timeout value is greater than or equal to zero (0), and therefore an
     * initial connection validation is performed, this timeout does not override the
     * {@code connectionTimeout} or {@code validationTimeout}; they will be honored before this
     * timeout is applied.  The default value is one millisecond.
     */
    private long failFastTimeout = 1;

    /**
     * This property determines whether data source isolates internal pool queries, such as the connection alive test,
     * in their own transaction.
     * <p>
     * Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction.
     * This property only applies if {@link #autocommit} is disabled.
     */
    private boolean isolateInternalQueries;

    /**
     * The default auto-commit behavior of connections in the pool.
     * Determined whether queries such as update/insert should be immediately executed
     * without waiting for an underlying transaction.
     */
    private boolean autocommit;

    /**
     * Indicates whether JNDI data sources retrieved should be proxied
     * or returned back verbatim.
     */
    private boolean dataSourceProxy;

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(final String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(final String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(final String dialect) {
        this.dialect = dialect;
    }

    public String getDdlAuto() {
        return ddlAuto;
    }

    public void setDdlAuto(final String ddlAuto) {
        this.ddlAuto = ddlAuto;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(final String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public ConnectionPoolingProperties getPool() {
        return pool;
    }

    public void setPool(final ConnectionPoolingProperties pool) {
        this.pool = pool;
    }

    public long getIdleTimeout() {
        return Beans.newDuration(idleTimeout).toMillis();
    }

    public void setIdleTimeout(final String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getLeakThreshold() {
        return leakThreshold;
    }

    public void setLeakThreshold(final int leakThreshold) {
        this.leakThreshold = leakThreshold;
    }
    
    public boolean isIsolateInternalQueries() {
        return isolateInternalQueries;
    }

    public void setIsolateInternalQueries(final boolean isolateInternalQueries) {
        this.isolateInternalQueries = isolateInternalQueries;
    }

    public String getHealthQuery() {
        return healthQuery;
    }

    public void setHealthQuery(final String healthQuery) {
        this.healthQuery = healthQuery;
    }

    public boolean isAutocommit() {
        return autocommit;
    }

    public void setAutocommit(final boolean autocommit) {
        this.autocommit = autocommit;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(final String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public boolean isDataSourceProxy() {
        return dataSourceProxy;
    }

    public void setDataSourceProxy(final boolean dataSourceProxy) {
        this.dataSourceProxy = dataSourceProxy;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public long getFailFastTimeout() {
        return failFastTimeout;
    }

    public void setFailFastTimeout(final long failFastTimeout) {
        this.failFastTimeout = failFastTimeout;
    }
}
