package org.apereo.cas.configuration.model.support.rest;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link RestAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest-authentication")
@Slf4j
@Getter
@Setter
public class RestAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -6122859176355467060L;

    /**
     * Endpoint URI to use for verification of credentials.
     */
    @RequiredProperty
    private String uri;

    /**
     * Password encoder settings for REST authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;
}
