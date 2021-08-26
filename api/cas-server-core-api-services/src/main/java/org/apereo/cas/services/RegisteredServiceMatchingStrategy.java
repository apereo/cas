package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Represents a strategy on how a registered service
 * could be matched against an incoming request
 * using its service id, etc.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@FunctionalInterface
public interface RegisteredServiceMatchingStrategy extends Serializable {

    /**
     * Indicate if this strategy can be matched against the given service id.
     *
     * @param registeredService the registered service
     * @param serviceId         the service id
     * @return true /false
     */
    boolean matches(RegisteredService registeredService, String serviceId);
}
