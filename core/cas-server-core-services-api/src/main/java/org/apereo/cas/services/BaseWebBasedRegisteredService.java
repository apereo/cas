package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BaseWebBasedRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class BaseWebBasedRegisteredService extends BaseRegisteredService implements WebBasedRegisteredService {
    private static final long serialVersionUID = 7766178156998290373L;

    private RegisteredServiceAcceptableUsagePolicy acceptableUsagePolicy = new DefaultRegisteredServiceAcceptableUsagePolicy();

    private RegisteredServiceSingleSignOnParticipationPolicy singleSignOnParticipationPolicy;

    private RegisteredServiceWebflowInterruptPolicy webflowInterruptPolicy = new DefaultRegisteredServiceWebflowInterruptPolicy();
}
