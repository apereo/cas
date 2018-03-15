package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link AbstractDynamoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractDynamoDbProperties implements Serializable {
    private static final long serialVersionUID = -8349917272283787550L;

    /**
     * File containing credentials properties.
     */
    private Resource credentialsPropertiesFile;

    /**
     * Credentials access key.
     */
    @RequiredProperty
    private String credentialAccessKey;

    /**
     * Credentials secret key.
     */
    @RequiredProperty
    private String credentialSecretKey;

    /**
     * Dynamo DB endpoint.
     */
    @RequiredProperty
    private String endpoint;

    /**
     * EC2 region.
     */
    @RequiredProperty
    private String region;

    /**
     * EC2 region override.
     */
    private String regionOverride;

    /**
     * Service name pattern.
     */
    private String serviceNameIntern;

    /**
     * Flag that indicates whether to drop tables on start up.
     */
    private boolean dropTablesOnStartup;

    /**
     * Flag that indicates whether to prevent CAS from creating tables.
     */
    private boolean preventTableCreationOnStartup;

    /**
     * Time offset.
     */
    private int timeOffset;

    /**
     * Read capacity.
     */
    private long readCapacity = 10;

    /**
     * Write capacity.
     */
    private long writeCapacity = 10;

    /**
     * Connection timeout.
     */
    private int connectionTimeout = 5000;

    /**
     * Request timeout.
     */
    private int requestTimeout = 5000;

    /**
     * Socket timeout.
     */
    private int socketTimeout = 5000;

    /**
     * Flag that indicates whether to use Gzip compression.
     */
    private boolean useGzip;

    /**
     * Flag that indicates whether to use reaper.
     */
    private boolean useReaper;

    /**
     * Flag that indicates whether to throttle retries.
     */
    private boolean useThrottleRetries;

    /**
     * Flag that indicates whether to keep TCP connection alive.
     */
    private boolean useTcpKeepAlive;

    /**
     * Protocol setting.
     */
    private String protocol = "HTTPS";

    /**
     * Client execution timeout.
     */
    private int clientExecutionTimeout = 10000;

    /**
     * Flag that indicates whether to cache response metadata.
     */
    private boolean cacheResponseMetadata;

    /**
     * Local address.
     */
    private String localAddress;

    /**
     * Maximum connections setting.
     */
    private int maxConnections = 10;

    public String getServiceNameIntern() {
        return serviceNameIntern;
    }

    public void setServiceNameIntern(final String serviceNameIntern) {
        this.serviceNameIntern = serviceNameIntern;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(final int timeOffset) {
        this.timeOffset = timeOffset;
    }

    public boolean isDropTablesOnStartup() {
        return dropTablesOnStartup;
    }

    public void setDropTablesOnStartup(final boolean dropTablesOnStartup) {
        this.dropTablesOnStartup = dropTablesOnStartup;
    }

    public boolean isPreventTableCreationOnStartup() {
        return preventTableCreationOnStartup;
    }

    public void setPreventTableCreationOnStartup(final boolean preventTableCreationOnStartup) {
        this.preventTableCreationOnStartup = preventTableCreationOnStartup;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getRegionOverride() {
        return regionOverride;
    }

    public void setRegionOverride(final String regionOverride) {
        this.regionOverride = regionOverride;
    }

    public Resource getCredentialsPropertiesFile() {
        return credentialsPropertiesFile;
    }

    public void setCredentialsPropertiesFile(final Resource credentialsPropertiesFile) {
        this.credentialsPropertiesFile = credentialsPropertiesFile;
    }

    public String getCredentialAccessKey() {
        return credentialAccessKey;
    }

    public void setCredentialAccessKey(final String credentialAccessKey) {
        this.credentialAccessKey = credentialAccessKey;
    }

    public String getCredentialSecretKey() {
        return credentialSecretKey;
    }

    public void setCredentialSecretKey(final String credentialSecretKey) {
        this.credentialSecretKey = credentialSecretKey;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(final int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(final int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(final int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(final int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public boolean isUseGzip() {
        return useGzip;
    }

    public void setUseGzip(final boolean useGzip) {
        this.useGzip = useGzip;
    }

    public boolean isUseReaper() {
        return useReaper;
    }

    public void setUseReaper(final boolean useReaper) {
        this.useReaper = useReaper;
    }

    public boolean isUseThrottleRetries() {
        return useThrottleRetries;
    }

    public void setUseThrottleRetries(final boolean useThrottleRetries) {
        this.useThrottleRetries = useThrottleRetries;
    }

    public boolean isUseTcpKeepAlive() {
        return useTcpKeepAlive;
    }

    public void setUseTcpKeepAlive(final boolean useTcpKeepAlive) {
        this.useTcpKeepAlive = useTcpKeepAlive;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public int getClientExecutionTimeout() {
        return clientExecutionTimeout;
    }

    public void setClientExecutionTimeout(final int clientExecutionTimeout) {
        this.clientExecutionTimeout = clientExecutionTimeout;
    }

    public boolean isCacheResponseMetadata() {
        return cacheResponseMetadata;
    }

    public void setCacheResponseMetadata(final boolean cacheResponseMetadata) {
        this.cacheResponseMetadata = cacheResponseMetadata;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(final String localAddress) {
        this.localAddress = localAddress;
    }

    public long getReadCapacity() {
        return readCapacity;
    }

    public void setReadCapacity(final long readCapacity) {
        this.readCapacity = readCapacity;
    }

    public long getWriteCapacity() {
        return writeCapacity;
    }

    public void setWriteCapacity(final long writeCapacity) {
        this.writeCapacity = writeCapacity;
    }
}
