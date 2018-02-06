package org.apereo.cas.configuration.model.support.dynamodb;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.core.io.Resource;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AbstractDynamoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
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
}
