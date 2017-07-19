package org.apereo.cas.configuration.model.support.mongo;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.Beans;

import java.io.Serializable;

/**
 * This is {@link AbstractMongoInstanceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractMongoInstanceProperties implements Serializable {
    private static final long serialVersionUID = -2471243083598934186L;

    /**
     * core connection-related settings.
     */
    private Conns conns = new Conns();

    /**
     * MongoDb database port.
     */
    private int port = 27017;

    /**
     * MongoDb database user for authentication.
     */
    private String userId = StringUtils.EMPTY;

    /**
     * MongoDb database password for authentication.
     */
    private String password = StringUtils.EMPTY;

    /**
     * MongoDb database host for authentication.
     */
    private String host = "localhost";

    /**
     * MongoDb database connection timeout.
     */
    private String timeout = "PT5S";

    /**
     * MongoDb database connection idle timeout.
     */
    private String idleTimeout = "PT30S";

    /**
     * Write concern describes the level of acknowledgement requested from
     * MongoDB for write operations to a standalone
     * mongo db or to replica sets or to sharded clusters. In sharded clusters,
     * mongo db instances will pass the write concern on to the shards.
     */
    private String writeConcern = "NORMAL";

    /**
     * MongoDb database collection name to fetch and/or create.
     */
    private String collectionName;

    /**
     * MongoDb database instance name.
     */
    private String databaseName = StringUtils.EMPTY;

    /**
     * Whether the database socket connection should be tagged with keep-alive.
     */
    private boolean socketKeepAlive;

    /**
     * Whether collections should be dropped on startup and re-created.
     */
    private boolean dropCollection;
    
    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

    public long getTimeout() {
        return Beans.newDuration(timeout).toMillis();
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    public long getIdleTimeout() {
        return Beans.newDuration(idleTimeout).toMillis();
    }

    public void setIdleTimeout(final String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(final String writeConcern) {
        this.writeConcern = writeConcern;
    }

    public boolean isSocketKeepAlive() {
        return socketKeepAlive;
    }

    public void setSocketKeepAlive(final boolean socketKeepAlive) {
        this.socketKeepAlive = socketKeepAlive;
    }

    public Conns getConns() {
        return conns;
    }

    public void setConns(final Conns conns) {
        this.conns = conns;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    public static class Conns {

        /**
         * Maximum number of connections to keep around.
         */
        private int lifetime = 60_000;

        /**
         * Total number of connections allowed per host.
         */
        private int perHost = 10;

        public int getLifetime() {
            return lifetime;
        }

        public void setLifetime(final int lifetime) {
            this.lifetime = lifetime;
        }

        public int getPerHost() {
            return perHost;
        }

        public void setPerHost(final int perHost) {
            this.perHost = perHost;
        }
    }
}
