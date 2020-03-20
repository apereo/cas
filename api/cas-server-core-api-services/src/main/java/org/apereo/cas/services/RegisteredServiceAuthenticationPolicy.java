package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationPolicy}.
 * The authentication policy can be assigned to a service definition
 * to indicate how CAS should respond to authentication requests
 * when processing the assigned service.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAuthenticationPolicy extends Serializable {

    /**
     * Gets required authentication handlers.
     *
     * @return the required authentication handlers
     */
    Set<String> getRequiredAuthenticationHandlers();

    /**
     * Sets required authentication handlers.
     *
     * @param handlers the handlers
     */
    void setRequiredAuthenticationHandlers(Set<String> handlers);
}
