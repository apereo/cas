package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-generic")
@Getter
@Setter
@Accessors(chain = true)
public class GroovyAuthenticationProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 2179027841236526083L;

    /**
     * Authentication handler name used to verify credentials in the file.
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
