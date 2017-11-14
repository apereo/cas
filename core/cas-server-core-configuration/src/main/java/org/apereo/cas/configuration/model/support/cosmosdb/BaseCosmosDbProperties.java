package org.apereo.cas.configuration.model.support.cosmosdb;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link BaseCosmosDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cosmosdb-core")
public abstract class BaseCosmosDbProperties implements Serializable {
    private static final long serialVersionUID = 2528153816791719898L;

    /**
     * Database throughput usually between 400 or 100,000.
     */
    private int throughput = 10_000;

    /**
     * Document Db host address (i.e. https://localhost:8081).
     */
    @RequiredProperty
    private String uri;

    /**
     * Document Db master key (i.e. https://localhost:8081).
     */
    @RequiredProperty
    private String key;

    /**
     * Document Db consistency level.
     * Accepted values are {@code Strong,BoundedStaleness,Session,Eventual,ConsistentPrefix}.
     */
    private String consistencyLevel = "Session";

    /**
     * Database name.
     */
    @RequiredProperty
    private String database;

    /**
     * Whether telemetry should be enabled by default.
     */
    private boolean allowTelemetry = true;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(final String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(final String database) {
        this.database = database;
    }

    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    public void setAllowTelemetry(final boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    public int getThroughput() {
        return throughput;
    }

    public void setThroughput(final int throughput) {
        this.throughput = throughput;
    }
}
