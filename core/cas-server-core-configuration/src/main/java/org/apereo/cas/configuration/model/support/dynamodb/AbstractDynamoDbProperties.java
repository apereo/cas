package org.apereo.cas.configuration.model.support.dynamodb;

import org.springframework.core.io.Resource;

/**
 * This is {@link AbstractDynamoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractDynamoDbProperties {
    private Resource credentialsPropertiesFile;
            
    private String credentialAccessKey;
    private String credentialSecretKey;
    
    private String endpoint;
    private String region;
    private String regionOverride;
    private String serviceNameIntern;
    
    private boolean dropTablesOnStartup;
    private int timeOffset;
    
    private long readCapacity = 10;
    private long writeCapacity = 10;
    private int connectionTimeout = 5000;
    private int requestTimeout = 5000;
    private int socketTimeout = 5000;
    private boolean useGzip;
    private boolean useReaper;
    private boolean useThrottleRetries;
    private boolean useTcpKeepAlive;
    private String protocol = "HTTPS";
    private int clientExecutionTimeout = 10000;
    private boolean cacheResponseMetadata;
    private String localAddress;
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
