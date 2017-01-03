package org.apereo.cas.configuration.model.support.mongo;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.Beans;

/**
 * This is {@link AbstractMongoInstanceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractMongoInstanceProperties {
    private Conns conns = new Conns();
    
    private int port = 27017;

    private String userId = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;
    private String host = "localhost";
    private String timeout = "PT5S";
    private String idleTimeout = "PT30S";
    private String writeConcern = "NORMAL";
    private String collectionName;
    private String databaseName = StringUtils.EMPTY;
    
    private boolean socketKeepAlive;
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
        private int lifetime = 60_000;
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
