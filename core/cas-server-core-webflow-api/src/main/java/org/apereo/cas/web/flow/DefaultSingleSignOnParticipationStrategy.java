package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.TicketGrantingTicketAwareTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.common.web.ClientInfoHolder;
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
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final SingleSignOnProperties properties;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public DefaultSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                    final SingleSignOnProperties properties,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.properties = properties;
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) throws Throwable {
        if (properties.isRenewAuthnEnabled() && ssoRequest.isRequestingRenewAuthentication()) {
            LOGGER.debug("The authentication session is considered renewed.");
            return false;
        }
        if (!isTicketAssignedToRequestTenant(ssoRequest)) {
            LOGGER.debug("The authentication session belongs to another tenant and cannot participate in SSO");
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

                val effectivePrincipal = authenticationSystemSupport.getPrincipalResolver()
                    .resolve(new BasicIdentifiableCredential(authentication.getPrincipal().getId()));
                val revocationValue = Stream.of(
                        effectivePrincipal.getSingleValuedAttribute(properties.getRevocationAttributeName()),
                        authentication.getSingleValuedAttribute(properties.getRevocationAttributeName())
                    )
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(StringUtils::isNotBlank)
                    .findFirst();
                LOGGER.debug("Located revocation attribute value [{}] for authentication [{}]", revocationValue, authentication);
                val revokedBeforeSeconds = revocationValue.map(Longs::tryParse);
                if (revokedBeforeSeconds.isPresent()) {
                    val revokedBefore = Instant.ofEpochSecond(revokedBeforeSeconds.get());
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

    /**
     * Whether the ticket-granting ticket of the request, if any, belongs to the tenant of the request.
     * A session established in one tenant, or outside of any tenant, does not carry over to another.
     *
     * @param ssoRequest the sso request
     * @return true if there is no ticket or both belong to the same tenant
     */
    private boolean isTicketAssignedToRequestTenant(final SingleSignOnParticipationRequest ssoRequest) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val requestTenant = clientInfo != null ? clientInfo.getTenant() : null;
        return getTicketState(ssoRequest)
            .map(ticket -> Strings.CI.equals(StringUtils.defaultString(ticket.getTenantId()), StringUtils.defaultString(requestTenant)))
            .orElse(Boolean.TRUE);
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        val registeredService = (WebBasedRegisteredService) getRegisteredService(context);
        if (registeredService != null) {
            val ssoPolicy = registeredService.getSingleSignOnParticipationPolicy();
            if (ssoPolicy != null) {
                return Objects.requireNonNullElse(ssoPolicy.getCreateCookieOnRenewedAuthentication(), TriStateBoolean.UNDEFINED);
            }
        }
        return TriStateBoolean.fromBoolean(properties.isCreateSsoCookieOnRenewAuthn());
    }
}
