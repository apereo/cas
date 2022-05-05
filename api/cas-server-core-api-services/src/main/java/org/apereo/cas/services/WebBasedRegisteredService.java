package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link WebBasedRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface WebBasedRegisteredService extends RegisteredService {
    /**
     * Gets interrupt policy that is assigned to this service.
     *
     * @return the interrupt policy
     */
    RegisteredServiceWebflowInterruptPolicy getWebflowInterruptPolicy();

    /**
     * Get the acceptable usage policy linked to this application.
     *
     * @return an instance of {@link RegisteredServiceAcceptableUsagePolicy}
     */
    RegisteredServiceAcceptableUsagePolicy getAcceptableUsagePolicy();

    /**
     * Gets SSO participation strategy.
     *
     * @return the service ticket expiration policy
     */
    RegisteredServiceSingleSignOnParticipationPolicy getSingleSignOnParticipationPolicy();

}
