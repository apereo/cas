package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationPolicy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAuthenticationPolicyCriteria extends Serializable {

    /**
     * To authentication policy.
     *
     * @param registeredService the registered service
     * @return the authentication policy
     */
    @JsonIgnore
    AuthenticationPolicy toAuthenticationPolicy(RegisteredService registeredService);
}
