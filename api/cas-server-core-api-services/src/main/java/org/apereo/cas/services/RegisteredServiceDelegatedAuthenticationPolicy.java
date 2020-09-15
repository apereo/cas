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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceDelegatedAuthenticationPolicy extends Serializable {

    /**
     * Indicate the collection of allowed authentication providers
     * that this service may choose to delegate.
     *
     * @return allowed authn providers
     */
    Collection<String> getAllowedProviders();

    /**
     * Indicate whether authentication should be exclusively
     * limited to allowed providers, disabling other forms of
     * authentication such as username/password, etc.
     *
     * @return true/false
     */
    boolean isExclusive();

    /**
     * If no providers are defined, indicates whether or not access strategy should
     * authorize the request.
     *
     * @return true/false
     */
    boolean isPermitUndefined();

    /**
     * Is provider allowed to process the request for this service.
     *
     * @param provider          the provider
     * @param registeredService the registered service
     * @return true/false
     */
    @JsonIgnore
    default boolean isProviderAllowed(final String provider, final RegisteredService registeredService) {
        return true;
    }

    /**
     * Indicates whether use of the allowed providers should be required and forced.
     * @return true if the policy is exclusive and, either provides are defined or undefined providers are not allowed.
     */
    @JsonIgnore
    default boolean isProviderRequired() {
        return isExclusive() && (!getAllowedProviders().isEmpty() || (getAllowedProviders().isEmpty() && !isPermitUndefined()));
    }
}
