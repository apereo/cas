package org.apereo.cas.configuration.model.support.cassandra.authentication;

/**
 * This is {@link CassandraAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CassandraAuthenticationProperties {
    private String name;
    private Integer order;

    private String username;
    private String password;
        
    private String protocolVersion = "V3";
    private String keyspace;
    private String contactPoints = "localhost";
    private String localDc;
    private boolean shuffleReplicas = true;
    private String retryPolicy = "DEFAULT_RETRY_POLICY";
    private String compression = "LZ4";
    private String consistencyLevel = "LOCAL_QUORUM";
    private String serialConsistencyLevel = "LOCAL_SERIAL";

    private int maxConnections = 10;
    private int coreConnections = 1;
    private int maxRequestsPerConnection = 1024;
    private int connectTimeoutMillis = 5000;
    private int readTimeoutMillis = 5000;

    private int port = 9042;

    private String usernameAttribute;
    private String passwordAttribute;
    private String tableName;

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(final String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isShuffleReplicas() {
        return shuffleReplicas;
    }

    public void setShuffleReplicas(final boolean shuffleReplicas) {
        this.shuffleReplicas = shuffleReplicas;
    }

    public String getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(final String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(final String compression) {
        this.compression = compression;
    }

    public String getSerialConsistencyLevel() {
        return serialConsistencyLevel;
    }

    public void setSerialConsistencyLevel(final String serialConsistencyLevel) {
        this.serialConsistencyLevel = serialConsistencyLevel;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(final int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getCoreConnections() {
        return coreConnections;
    }

    public void setCoreConnections(final int coreConnections) {
        this.coreConnections = coreConnections;
    }

    public int getMaxRequestsPerConnection() {
        return maxRequestsPerConnection;
    }

    public void setMaxRequestsPerConnection(final int maxRequestsPerConnection) {
        this.maxRequestsPerConnection = maxRequestsPerConnection;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(final int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(final int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public void setPasswordAttribute(final String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(final String keyspace) {
        this.keyspace = keyspace;
    }

    public String getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(final String contactPoints) {
        this.contactPoints = contactPoints;
    }

    public String getLocalDc() {
        return localDc;
    }

    public void setLocalDc(final String localDc) {
        this.localDc = localDc;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(final String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }
}
