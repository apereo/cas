package org.apereo.cas.configuration.model.support.jpa.ticketregistry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for ticketreg.database.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "ticketreg.database", ignoreUnknownFields = false)
public class JpaTicketRegistryProperties {

    private String dialect = "org.hibernate.dialect.HSQLDialect";

    private String ddlAuto = "create-drop";

    private String batchSize = "1";

    private String driverClass = "org.hsqldb.jdbcDriver";

    private String url = "jdbc:hsqldb:mem:cas-ticket-registry";

    private String user = "sa";

    private String password = "";

    private Pool pool = new Pool();

    private int idleTimeout = 5000;

    private int leakThreshold = 10;

    private boolean failFast = true;

    private boolean isolateInternalQueries = false;

    private String healthQuery = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";

    private boolean autocommit = false;

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

    public Pool getPool() {
        return pool;
    }

    public void setPool(final Pool pool) {
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

    public void setLeakThreshold(int leakThreshold) {
        this.leakThreshold = leakThreshold;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isolateInternalQueries() {
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

    /**
     * Pool props.
     */
    public static class Pool {
        private int minSize = 6;
        private int maxSize = 18;
        private int maxIdleTime = 1000;
        private int maxWait = 2000;
        private boolean suspension = false;

        public boolean isSuspension() {
            return suspension;
        }

        public void setSuspension(final boolean suspension) {
            this.suspension = suspension;
        }

        public int getMinSize() {
            return minSize;
        }

        public void setMinSize(final int minSize) {
            this.minSize = minSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(final int maxSize) {
            this.maxSize = maxSize;
        }

        public int getMaxIdleTime() {
            return maxIdleTime;
        }

        public void setMaxIdleTime(final int maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        public int getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(final int maxWait) {
            this.maxWait = maxWait;
        }
    }
}
