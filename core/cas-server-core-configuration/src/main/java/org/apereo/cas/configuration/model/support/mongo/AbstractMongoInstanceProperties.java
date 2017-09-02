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
    private MongoConnections conns = new MongoConnections();

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
     * Multiple host addresses may be defined, separated by comma.
     * If more than one host is defined, it is assumed that each host contains the port as well, if any.
     * Otherwise the configuration may fallback onto {@link #getPort()}.
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
     * MongoDb database instance name.
     */
    private String databaseName = StringUtils.EMPTY;

    /**
     * Whether the database socket connection should be tagged with keep-alive.
     */
    private boolean socketKeepAlive;
    
    /**
     * Name of the database to use for authentication.
     */
    private String authenticationDatabaseName;

    /**
     * A replica set in MongoDB is a group of {@code mongod} processes that maintain
     * the same data set. Replica sets provide redundancy and high availability, and are the basis for all production deployments.
     */
    private String replicaSet;

    /**
     * Whether connections require SSL.
     */
    private boolean sslEnabled;

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

    public MongoConnections getConns() {
        return conns;
    }

    public void setConns(final MongoConnections conns) {
        this.conns = conns;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    public String getAuthenticationDatabaseName() {
        return authenticationDatabaseName;
    }

    public void setAuthenticationDatabaseName(final String authenticationDatabaseName) {
        this.authenticationDatabaseName = authenticationDatabaseName;
    }

    public String getReplicaSet() {
        return replicaSet;
    }

    public void setReplicaSet(final String replicaSet) {
        this.replicaSet = replicaSet;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(final boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public static class MongoConnections implements Serializable {

        private static final long serialVersionUID = -2398415870062168474L;
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
