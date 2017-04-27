package org.apereo.cas.configuration.model.support.jpa;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.support.Beans;

/**
 * Common properties for all jpa configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public abstract class AbstractJpaProperties {

    private String dialect = "org.hibernate.dialect.HSQLDialect";
    private String ddlAuto = "create-drop";
    private String driverClass = "org.hsqldb.jdbcDriver";
    private String url = "jdbc:hsqldb:mem:cas-hsql-database";
    private String user = "sa";
    private String password = StringUtils.EMPTY;
    private String defaultCatalog;
    private String defaultSchema;
    private String healthQuery = StringUtils.EMPTY;
    private String idleTimeout = "PT10M";
    private String dataSourceName;

    private ConnectionPoolingProperties pool = new ConnectionPoolingProperties();

    private int leakThreshold = 3_000;
    private int batchSize = 1;

    private boolean failFast = true;
    private boolean isolateInternalQueries;
    private boolean autocommit;
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
}
