package org.apereo.cas.configuration.model.support.okta;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseOktaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-okta-authentication")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseOktaProperties implements Serializable {
    private static final long serialVersionUID = -23245764438426360L;

    /**
     * The order of this authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Send requests via a proxy; define the hostname.
     */
    private String proxyHost;

    /**
     * Send requests via a proxy; define the proxy port.
     * Negative/zero values should deactivate the proxy configuration
     * for the http client.
     */
    private int proxyPort;

    /**
     * Send requests via a proxy; define the proxy username.
     */
    private String proxyUsername;

    /**
     * Send requests via a proxy; define the proxy password.
     */
    private String proxyPassword;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = 5000;

    /**
     * Okta domain.
     */
    @RequiredProperty
    private String organizationUrl;
}
