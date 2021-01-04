package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyAuthenticationHandlerResolutionProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GroovyAuthenticationHandlerResolutionProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 8079027843747126083L;

    /**
     * The execution order of this resolver in the chain of authentication handler resolvers.
     */
    private int order;
}
