package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AbstractDynamoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Setter
public abstract class AbstractDynamoDbProperties extends BaseAmazonWebServicesProperties {
    private static final long serialVersionUID = -8349917272283787550L;

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

    /**
     * Indicates that the database instance is local to the deployment
     * that does not require or use any credentials or other configuration
     * other than host and region. This is mostly used during development
     * and testing.
     */
    private boolean localInstance;
}
