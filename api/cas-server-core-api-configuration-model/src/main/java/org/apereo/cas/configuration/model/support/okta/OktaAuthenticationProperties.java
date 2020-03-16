package org.apereo.cas.configuration.model.support.okta;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OktaAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-okta-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class OktaAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = -13245764438426360L;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * The order of this authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Password encoding properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

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
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate<Credential>}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    private String credentialCriteria;
    
    /**
     * Okta domain.
     */
    @RequiredProperty
    private String organizationUrl;
}
