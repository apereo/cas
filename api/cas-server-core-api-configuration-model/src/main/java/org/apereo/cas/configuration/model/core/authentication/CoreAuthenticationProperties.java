package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link CoreAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CoreAuthenticationProperties")
public class CoreAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = -2244126985007049516L;

    /**
     * Attempt to resolve/filter authentication handlers
     * for the current transaction based on what is globally
     * defined via the definition of a registered service
     * and how it filters the required authentication handlers.
     */
    @NestedConfigurationProperty
    private RegisteredServiceAuthenticationHandlerResolutionProperties serviceAuthenticationResolution =
        new RegisteredServiceAuthenticationHandlerResolutionProperties();

    /**
     * Attempt to resolve/filter authentication handlers
     * for the current transaction based on what is globally
     * defined via an external groovy script.
     */
    @NestedConfigurationProperty
    private GroovyAuthenticationHandlerResolutionProperties groovyAuthenticationResolution =
        new GroovyAuthenticationHandlerResolutionProperties();

    /**
     * Customization of authentication engine and pre/post processing.
     */
    @NestedConfigurationProperty
    private AuthenticationEngineProperties engine = new AuthenticationEngineProperties();

}
