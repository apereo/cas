package org.apereo.cas.configuration.model.support.aws;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("BaseAmazonWebServicesProperties")
public abstract class BaseAmazonWebServicesProperties implements Serializable {
    private static final long serialVersionUID = 6426637051495147084L;

    /**
     * Use access-key provided by AWS to authenticate.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String credentialAccessKey;

    /**
     * Use secret key provided by AWS to authenticate.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
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
    @DurationCapable
    private String connectionTimeout = "5000";

    /**
     * Socket timeout.
     */
    @DurationCapable
    private String socketTimeout = "5000";

    /**
     * Client execution timeout.
     */
    @DurationCapable
    private String clientExecutionTimeout = "10000";

    /**
     * Flag that indicates whether to use reaper.
     */
    private boolean useReaper;

    /**
     * Optionally specifies the proxy host to connect through.
     */
    private String proxyHost;

    /**
     * Optionally specifies the proxy password to connect through.
     */
    private String proxyPassword;

    /**
     * Optionally specifies the proxy username to connect through.
     */
    private String proxyUsername;

    /**
     * Outline the requested retry mode.
     * Accepted values are {@code STANDARD, LEGACY}.
     */
    private String retryMode = "STANDARD";
    
    /**
     * Local address.
     */
    private String localAddress;
}
