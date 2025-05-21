package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link ServiceTicketRequestWebflowEventResolver}
 * that creates the next event responding to requests that are service-ticket requests.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class ServiceTicketRequestWebflowEventResolver extends AbstractCasWebflowEventResolver {
    public ServiceTicketRequestWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext context) {
        super(context);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) throws Throwable {
        if (isRequestAskingForServiceTicket(context)) {
            LOGGER.trace("Authentication request is asking for service tickets");
            val source = grantServiceTicket(context);
            return Optional.ofNullable(source).map(CollectionUtils::wrapSet).orElse(null);
        }
        return null;
    }

    protected boolean isRequestAskingForServiceTicket(final RequestContext context) throws Throwable {
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.trace("Located ticket-granting ticket [{}] from the request context", ticketGrantingTicketId);

        val service = WebUtils.getService(context);
        LOGGER.trace("Located service [{}] from the request context", service);

        if (service != null && StringUtils.isNotBlank(ticketGrantingTicketId)) {
            val authn = getConfigurationContext().getTicketRegistrySupport().getAuthenticationFrom(ticketGrantingTicketId);
            LOGGER.debug("Request identifies itself as one asking for service tickets. Checking for authentication context validity...");
            val validAuthn = validateExistingAuthentication(authn, context);
            if (validAuthn) {
                LOGGER.debug("Existing authentication context linked to ticket-granting ticket [{}] is valid. "
                    + "CAS will try to issue service tickets for [{}] once credentials are renewed", ticketGrantingTicketId, service);
                return true;
            }
            LOGGER.debug("Existing authentication context linked to ticket-granting ticket [{}] is NOT valid. "
                    + "CAS will not issue service tickets for [{}] just yet without renewing the authentication context",
                ticketGrantingTicketId, service);
            return false;
        }

        LOGGER.debug("Request is not eligible to be issued service tickets just yet");
        return false;
    }

    /**
     * Grant service ticket for the given credential based on the service and tgt
     * that are found in the request context.
     *
     * @param context the context
     * @return the resulting event. Warning, authentication failure or error.
     * @since 4.1.0
     */
    protected Event grantServiceTicket(final RequestContext context) throws Throwable {
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        val credentials = getCredentialFromContext(context);

        try {
            val service = WebUtils.getService(context);
            val configContext = getConfigurationContext();

            val existingAuthn = configContext.getTicketRegistrySupport().getAuthenticationFrom(ticketGrantingTicketId);
            val registeredService = configContext.getServicesManager().findServiceBy(service);

            if (existingAuthn != null && registeredService != null) {
                LOGGER.debug("Enforcing access strategy policies for registered service [{}] and principal [{}]",
                    registeredService, existingAuthn.getPrincipal());

                val audit = AuditableContext.builder().service(service)
                    .authentication(existingAuthn)
                    .registeredService(registeredService)
                    .build();
                val accessResult = configContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
                accessResult.throwExceptionIfNeeded();
            }

            val principal = getActivePrincipal(credentials, service, existingAuthn);
            LOGGER.debug("Primary principal for this authentication session to receive a service ticket is [{}]", principal);

            if (existingAuthn != null && !existingAuthn.getPrincipal().equals(principal)) {
                LOGGER.trace("Existing authentication context linked to ticket-granting ticket [{}] is issued for principal [{}] "
                        + " which does not match [{}], established by the requested authentication transaction. CAS will NOT reuse the existing "
                        + "authentication context to issue service tickets",
                    ticketGrantingTicketId, existingAuthn.getPrincipal(), principal);
                return null;
            }

            return newEvent(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET);
        } catch (final AuthenticationException | AbstractTicketException e) {
            LOGGER.trace(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }
    }

    private boolean validateExistingAuthentication(final Authentication authentication,
                                                   final RequestContext requestContext) throws Throwable {
        if (authentication != null) {
            val configContext = getConfigurationContext();
            val ssoStrategy = configContext.getSingleSignOnParticipationStrategy();

            val ssoRequest = SingleSignOnParticipationRequest.builder()
                .requestContext(requestContext)
                .build();

            if (ssoStrategy.supports(ssoRequest) && !ssoStrategy.isParticipating(ssoRequest)) {
                LOGGER.debug("Single sign-on strategy does not allow reusing the authentication attempt [{}]", authentication);
                return false;
            }
            LOGGER.trace("Existing authentication attempt [{}] is valid", authentication);
            return true;
        }
        LOGGER.trace("Cannot validate absent/missing authentication attempt");
        return false;
    }

    protected Principal getActivePrincipal(final List<Credential> credential,
                                           final WebApplicationService service,
                                           final Authentication authentication) throws Throwable {
        if (credential != null && !credential.isEmpty()) {
            LOGGER.trace("Finalizing authentication transaction for [{}]", credential);
            val authenticationResult = getConfigurationContext().getAuthenticationSystemSupport()
                .finalizeAuthenticationTransaction(service, credential);
            return authenticationResult.getAuthentication().getPrincipal();
        }
        return Objects.requireNonNull(authentication).getPrincipal();
    }

}
