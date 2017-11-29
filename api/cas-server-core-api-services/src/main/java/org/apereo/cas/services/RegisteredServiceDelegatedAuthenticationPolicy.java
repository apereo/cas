package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Collection;

/**
 * This is {@link RegisteredServiceDelegatedAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceDelegatedAuthenticationPolicy extends Serializable {

    /**
     * Indicate the collection of allowed authentication providers
     * that this service may choose to delegate.
     *
     * @return allowed authn providers
     */
    Collection<String> getAllowedProviders();

    /**
     * Is provider allowed to process the request for this service.
     *
     * @param provider          the provider
     * @param registeredService the registered service
     * @return the boolean
     */
    @JsonIgnore
    default boolean isProviderAllowed(final String provider, final RegisteredService registeredService) {
        return true;
    }
}
