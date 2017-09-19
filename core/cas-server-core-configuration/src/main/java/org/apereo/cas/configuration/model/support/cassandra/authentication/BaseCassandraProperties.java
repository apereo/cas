package org.apereo.cas.configuration.model.support.cassandra.authentication;

import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;

/**
 * This is {@link BaseCassandraProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseCassandraProperties implements Serializable {
    private static final long serialVersionUID = 3708645268337674572L;
    /**
     * Username to bind and establish a connection to cassandra.
     */
    @RequiredProperty
    private String username;
    /**
     * Password to bind and establish a connection to cassandra.
     */
    @RequiredProperty
    private String password;

    /**
     * Cassandra protocol version.
     */
    private String protocolVersion = "V3";
    /**
     * Keyspace address to use where the cluster would connect.
     */
    @RequiredProperty
    private String keyspace;
    /**
     * The list of contact points to use for the new cluster.
     */
    @RequiredProperty
    private String contactPoints = "localhost";
    /**
     * Used by a DC-ware round-robin load balancing policy.
     * This policy provides round-robin queries over the node of the local data center. It also includes in the query plans returned a
     * configurable number of hosts in the remote data centers, but those are always tried after the local nodes.
     * In other words, this policy guarantees that no host in a remote data center will be queried unless no host in the local data center can be reached.
     */
    private String localDc;
    /**
     * The DC policy is wrapped inside a token-aware policy that may be set to shuffle replicas.
     * This indicates whether to shuffle the replicas returned by getRoutingKey. Note that setting this parameter to true might decrease the effectiveness of
     * caching (especially at consistency level ONE), since the same row will be retrieved from any
     * replica (instead of only the "primary" replica without shuffling).
     * On the other hand, shuffling will better distribute writes, and can alleviate hotspots caused by "fat" partitions.
     */
    private boolean shuffleReplicas = true;
    /**
     * A policy that defines a default behavior to adopt when a request fails.
     * Such policy allows to centralize the handling of query retries, allowing to minimize the need for exception catching/handling in business code.
     * Accepted options are {@code DEFAULT_RETRY_POLICY, DOWNGRADING_CONSISTENCY_RETRY_POLICY, FALLTHROUGH_RETRY_POLICY}.
     * <p>
     * The default policy retries queries in only two cases:
     * <ul>
     * <li>On a read timeout, if enough replicas replied but data was not retrieved.</li>
     * <li>On a write timeout, if we timeout while writing the distributed log used by batch statements.
     * This retry policy is conservative in that it will never retry with a different consistency level than the one of the initial operation.
     * </li>
     * </ul>
     */
    private String retryPolicy = "DEFAULT_RETRY_POLICY";
    /**
     * Protocol compression options.
     * Accepted options are {@code NONE, SNAPPY, LZ4}.
     */
    private String compression = "LZ4";
    /**
     * Query option consistency level.
     * The consistency level set through this method will be use for queries that don't explicitly have a consistency level.
     * Accepted values are:{@code ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO}.
     */
    private String consistencyLevel = "LOCAL_QUORUM";
    /**
     * Query option serial consistency level.
     * The serial consistency level set through this method will be use for queries that don't explicitly have a serial consistency level.
     * Accepted values are:{@code ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO}.
     */
    private String serialConsistencyLevel = "LOCAL_SERIAL";

    /**
     * Sets the maximum number of connections.
     */
    private int maxConnections = 10;
    /**
     * Sets the core number of connections per host.
     */
    private int coreConnections = 1;
    /**
     * Sets the maximum number of connection requests per host.
     */
    private int maxRequestsPerConnection = 1024;
    /**
     * The connection timeout in milliseconds.
     * As the name implies, the connection timeout defines how long the driver waits to establish a new connection to a Cassandra node before giving up.
     */
    private int connectTimeoutMillis = 5000;
    /**
     * The per-host read timeout in milliseconds.
     * This defines how long the driver will wait for a given Cassandra node to answer a query.
     */
    private int readTimeoutMillis = 5000;

    /**
     * Cassandra instance port.
     */
    @RequiredProperty
    private int port = 9042;

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

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(final String protocolVersion) {
        this.protocolVersion = protocolVersion;
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

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(final String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
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
}
