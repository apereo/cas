package org.apereo.cas.configuration.model.support.okta;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link BaseOktaApiProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-okta-authentication")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseOktaApiProperties extends BaseOktaProperties {
    @Serial
    private static final long serialVersionUID = -11245764438426360L;

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
     * Private key resource used for oauth20 api calls
     * with a client id. When using this approach, you won't need an API Token because
     * the Okta SDK will request an access token for you.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties privateKey = new SpringResourceProperties();

    /**
     * Okta API token.
     */
    private String apiToken;

    /**
     * Okta client id used in combination with the private key.
     */
    @RequiredProperty
    private String clientId;

    /**
     * Okta allows you to interact with Okta APIs using scoped OAuth 2.0 access tokens. Each access token
     * enables the bearer to perform specific actions on specific Okta endpoints, with that
     * ability controlled by which scopes the access token contains. Scopes are only used
     * when using client id and private-key.
     */
    @RequiredProperty
    private List<String> scopes = Stream.of("okta.users.read", "okta.apps.read").toList();
}
