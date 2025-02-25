package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link RegisteredServiceAuthenticationHandlerResolutionProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class RegisteredServiceAuthenticationHandlerResolutionProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 8079027843747126083L;

    /**
     * The execution order of this resolver in the chain of authentication handler resolvers.
     */
    private int order;
}
