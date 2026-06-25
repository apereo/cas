package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.Ordered;

/**
 * This is {@link DefaultSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class DefaultSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {
    private final SingleSignOnProperties properties;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public DefaultSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                    final SingleSignOnProperties properties,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.properties = properties;
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        if (properties.isRenewAuthnEnabled() && ssoRequest.isRequestingRenewAuthentication()) {
            LOGGER.debug("The authentication session is considered renewed.");
            return false;
        }

        val registeredService = (WebBasedRegisteredService) getRegisteredService(ssoRequest);
        if (registeredService != null) {
            val isAllowedForSso = registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService);
            LOGGER.trace("Located [{}] in registry. Service access to participate in SSO is set to [{}]",
                registeredService.getServiceId(), isAllowedForSso);

            if (!isAllowedForSso) {
                LOGGER.debug("Service [{}] is not authorized to participate in SSO", registeredService.getServiceId());
                return false;
            }

            val ssoPolicy = registeredService.getSingleSignOnParticipationPolicy();
            if (ssoPolicy != null) {
                val ticketState = getTicketState(ssoRequest);
                if (ticketState.isPresent()) {
                    return ssoPolicy.shouldParticipateInSso(registeredService, (AuthenticationAwareTicket) ticketState.get());
                }
            }

            val tgtPolicy = registeredService.getTicketGrantingTicketExpirationPolicy();
            if (tgtPolicy != null) {
                val ticketState = getTicketState(ssoRequest);
                return tgtPolicy.toExpirationPolicy()
                    .filter(tgt -> ticketState.isPresent())
                    .map(policy -> !policy.isExpired((TicketGrantingTicketAwareTicket) ticketState.get()))
                    .orElse(Boolean.TRUE);
            }
        }

        if (StringUtils.isNotBlank(properties.getRevocationAttributeName())) {
            val ticketState = getTicketState(ssoRequest)
                .map(AuthenticationAwareTicket.class::cast)
                .filter(auth -> Objects.nonNull(auth.getAuthentication()));
            if (ticketState.isPresent()) {
                val authentication = ticketState.get().getAuthentication();
                LOGGER.debug("Checking authentication [{}] for revocation attribute [{}]",
                    authentication, properties.getRevocationAttributeName());
                val revocationValue = Stream.of(
                        authentication.getPrincipal().getSingleValuedAttribute(properties.getRevocationAttributeName()),
                        authentication.getSingleValuedAttribute(properties.getRevocationAttributeName())
                    )
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(StringUtils::isNotBlank)
                    .findFirst();
                LOGGER.debug("Located revocation attribute value [{}] for authentication [{}]", revocationValue, authentication);
                if (revocationValue.isPresent() && NumberUtils.isParsable(revocationValue.get())) {
                    val revokedBefore = Instant.ofEpochSecond(Long.parseLong(revocationValue.get()));
                    val createdAt = authentication.getAuthenticationDate().toInstant();
                    if (createdAt.isBefore(revokedBefore) || createdAt.equals(revokedBefore)) {
                        LOGGER.debug("Authentication created at [{}] and revoked before [{}]", createdAt, revokedBefore);
                        return false;
                    }
                }
            }
        }

        return properties.isSsoEnabled();
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        val registeredService = (WebBasedRegisteredService) getRegisteredService(context);
        if (registeredService != null) {
            val ssoPolicy = registeredService.getSingleSignOnParticipationPolicy();
            if (ssoPolicy != null) {
                return ssoPolicy.getCreateCookieOnRenewedAuthentication();
            }
        }
        return TriStateBoolean.fromBoolean(properties.isCreateSsoCookieOnRenewAuthn());
    }
}
