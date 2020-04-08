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
     * Gets required authentication handlers by their name/id.
     *
     * @return the required authentication handlers
     */
    Set<String> getRequiredAuthenticationHandlers();

    /**
     * Gets required authentication policy criteria.
     *
     * @return the required authentication policies
     */
    RegisteredServiceAuthenticationPolicyCriteria getCriteria();

}
