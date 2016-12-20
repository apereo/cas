package org.apereo.cas.configuration.model.support.jpa;

import org.apereo.cas.configuration.support.ConnectionPoolingProperties;

/**
 * Common properties for all jpa configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public abstract class AbstractJpaProperties {

    private String dialect = "org.hibernate.dialect.HSQLDialect";
    private String ddlAuto = "create-drop";
    private String batchSize = "1";
    private String driverClass = "org.hsqldb.jdbcDriver";
    private String url = "jdbc:hsqldb:mem:cas-hsql-database";
    private String healthQuery = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
    private String user = "sa";
    private String password = "";
    private String defaultCatalog;
    private String defaultSchema;

    private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

    private int idleTimeout = 5000;
    private int leakThreshold = 10;

    private boolean failFast = true;
    private boolean isolateInternalQueries;
    private boolean autocommit;

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

    public String getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final String batchSize) {
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

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(final int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getLeakThreshold() {
        return leakThreshold;
    }

    public void setLeakThreshold(final int leakThreshold) {
        this.leakThreshold = leakThreshold;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
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
}
