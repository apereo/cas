package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AuthenticationExceptionsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AuthenticationExceptionsProperties")
public class AuthenticationExceptionsProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -2385347572099983874L;

    /**
     * Define custom exceptions that can then be mapped to message bundles for custom error handling.
     * <p>
     * By default CAS is configured to recognize and handle a number
     * of exceptions for during authentication. Each exception has the specific message bundle
     * mapping so that a specific message could be presented to end users on the login form.
     * Any un-recognized or un-mapped exceptions results in a generic message.
     * To map custom exceptions, one would need map the exception, they can be defined here
     * and then linked to custom messages.
     */
    private List<Class<? extends Throwable>> exceptions = new ArrayList<>(0);

    /**
     * Handle exceptions using a groovy script.
     */
    @NestedConfigurationProperty
    private GroovyAuthenticationExceptionsProperties groovy = new GroovyAuthenticationExceptionsProperties();
}
