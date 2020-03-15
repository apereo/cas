package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

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
public class CoreAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = -2244126985007049516L;

    /**
     * Attempt to resolve/filter authentication handlers
     * for the current transaction based on what is globally
     * defined via the definition of a registered service
     * and how it filters the required authentication handlers.
     */
    private RegisteredServiceAuthenticationHandlerResolution serviceAuthenticationResolution = new RegisteredServiceAuthenticationHandlerResolution();

    /**
     * Attempt to resolve/filter authentication handlers
     * for the current transaction based on what is globally
     * defined via an external groovy script.
     */
    private GroovyAuthenticationHandlerResolution groovyAuthenticationResolution = new GroovyAuthenticationHandlerResolution();

    /**
     * Customization of authentication engine and pre/post processing.
     */
    @NestedConfigurationProperty
    private AuthenticationEngineProperties engine = new AuthenticationEngineProperties();
    
    @RequiresModule(name = "cas-server-support-authentication", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class GroovyAuthenticationHandlerResolution extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;

        /**
         * The execution order of this resolver in the chain of authentication handler resolvers.
         */
        private int order;
    }

    @RequiresModule(name = "cas-server-support-authentication", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class RegisteredServiceAuthenticationHandlerResolution implements Serializable {
        private static final long serialVersionUID = 8079027843747126083L;

        /**
         * The execution order of this resolver in the chain of authentication handler resolvers.
         */
        private int order;
    }
}
