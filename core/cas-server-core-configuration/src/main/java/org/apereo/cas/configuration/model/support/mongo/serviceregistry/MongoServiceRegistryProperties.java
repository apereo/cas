package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

/**
 * Configuration properties class mongodb service registry.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class MongoServiceRegistryProperties {

    private String host = "localhost";

    private int port = 27017;

    private String userId = "";

    private String userPassword = "";

    private String serviceRegistryCollection = "cas-service-registry";

    private boolean dropCollection;

    private int timeout = 5000;

    private int idleTimeout = 30000;

    private String writeConcern = "NORMAL";

    private boolean socketKeepAlive;

    private Conns conns = new Conns();

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

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(final String userPassword) {
        this.userPassword = userPassword;
    }

    public String getServiceRegistryCollection() {
        return serviceRegistryCollection;
    }

    public void setServiceRegistryCollection(final String serviceRegistryCollection) {
        this.serviceRegistryCollection = serviceRegistryCollection;
    }

    public boolean isDropCollection() {
        return dropCollection;
    }

    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(final int idleTimeout) {
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

    public static class Conns {
        private int lifetime = 60000;
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
