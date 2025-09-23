package org.apereo.cas.configuration.model.support.rest;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link RestAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class RestAuthenticationProperties implements Serializable, CasFeatureModule {

    @Serial
    private static final long serialVersionUID = -6122859176355467060L;

    /**
     * Endpoint URI to use for verification of credentials.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String uri;

    /**
     * HTTP method to use when contacting the REST API for authentication.
     */
    private String method = "POST";

    /**
     * Password encoder settings for REST authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;
}
