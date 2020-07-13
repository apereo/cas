package org.apereo.cas.configuration.model.support.aws;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link BaseAmazonWebServicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-aws")
@Accessors(chain = true)
public abstract class BaseAmazonWebServicesProperties implements Serializable {
    private static final long serialVersionUID = 6426637051495147084L;

    /**
     * Authenticate and bind into the instance via a credentials properties file.
     */
    @RequiredProperty
    private transient Resource credentialsPropertiesFile;

    /**
     * Use access-key provided by AWS to authenticate.
     */
    @RequiredProperty
    private String credentialAccessKey;

    /**
     * Use secret key provided by AWS to authenticate.
     */
    @RequiredProperty
    private String credentialSecretKey;

    /**
     * AWS region used.
     */
    @RequiredProperty
    private String region;

    /**
     * Profile name to use.
     */
    private String profileName;

    /**
     * Profile path.
     */
    private String profilePath;

    /**
     * EC2 region override.
     */
    private String regionOverride;

    /**
     * Service name pattern.
     */
    private String serviceNameIntern;

    /**
     * AWS custom endpoint.
     */
    @RequiredProperty
    private String endpoint;

    /**
     * Maximum connections setting.
     */
    private int maxConnections = 10;

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
     * The maximum number of times that a retryable failed request (ex: a 5xx response from a
     * service) will be retried. Or -1 if the user has not explicitly set this value, in which case
     * the configured RetryPolicy will be used to control the retry count.
     */
    private int maxErrorRetry = -1;

    /**
     * Client execution timeout.
     */
    private int clientExecutionTimeout = 10000;

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
     *  Optionally specifies the proxy host to connect through.
     */
    private String proxyHost;

    /**
     *  Optionally specifies the proxy password to connect through.
     */
    private String proxyPassword;

    /**
     *  Optionally specifies the proxy username to connect through.
     */
    private String proxyUsername;

    /**
     *  Optionally specifies the proxy port to connect through.
     */
    private int proxyPort = -1;

    /**
     * Flag that indicates whether to cache response metadata.
     */
    private boolean cacheResponseMetadata;

    /**
     * Local address.
     */
    private String localAddress;
}
