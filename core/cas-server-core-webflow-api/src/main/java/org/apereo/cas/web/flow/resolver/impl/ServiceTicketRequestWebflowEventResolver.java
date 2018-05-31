package org.apereo.cas.web.flow.resolver.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final CasConfigurationProperties casProperties;

    public ServiceTicketRequestWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final CentralAuthenticationService centralAuthenticationService,
                                                    final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final CookieGenerator warnCookieGenerator,
                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                    final MultifactorAuthenticationProviderSelector selector,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                    final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector);
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.casProperties = casProperties;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        if (isRequestAskingForServiceTicket(context)) {
            LOGGER.debug("Authentication request is asking for service tickets");
            return CollectionUtils.wrapSet(grantServiceTicket(context));
        }
        return null;
    }

    /**
     * Is request asking for service ticket?
     *
     * @param context the context
     * @return true, if both service and tgt are found, and the request is not asking to renew.
     * @since 4.1.0
     */
    protected boolean isRequestAskingForServiceTicket(final RequestContext context) {
        final var ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.debug("Located ticket-granting ticket [{}] from the request context", ticketGrantingTicketId);

        final Service service = WebUtils.getService(context);
        LOGGER.debug("Located service [{}] from the request context", service);

        final var renewParam = casProperties.getSso().isRenewAuthnEnabled()
            ? context.getRequestParameters().get(CasProtocolConstants.PARAMETER_RENEW)
            : StringUtils.EMPTY;
        LOGGER.debug("Provided value for [{}] request parameter is [{}]", CasProtocolConstants.PARAMETER_RENEW, renewParam);

        if (service != null && StringUtils.isNotBlank(ticketGrantingTicketId)) {
            final var authn = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
            if (StringUtils.isNotBlank(renewParam)) {
                LOGGER.debug("Request identifies itself as one asking for service tickets. Checking for authentication context validity...");
                final var validAuthn = authn != null;
                if (validAuthn) {
                    LOGGER.debug("Existing authentication context linked to ticket-granting ticket [{}] is valid. "
                        + "CAS should begin to issue service tickets for [{}] once credentials are renewed", ticketGrantingTicketId, service);
                    return true;
                }
                LOGGER.debug("Existing authentication context linked to ticket-granting ticket [{}] is NOT valid. "
                        + "CAS will not issue service tickets for [{}] just yet without renewing the authentication context",
                    ticketGrantingTicketId, service);
                return false;
            }
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
    protected Event grantServiceTicket(final RequestContext context) {
        final var ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final var credential = getCredentialFromContext(context);

        try {
            final Service service = WebUtils.getService(context);
            final var authn = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
            final var registeredService = this.servicesManager.findServiceBy(service);

            if (authn != null && registeredService != null) {
                LOGGER.debug("Enforcing access strategy policies for registered service [{}] and principal [{}]", registeredService, authn.getPrincipal());

                final var audit = AuditableContext.builder().service(service)
                    .authentication(authn)
                    .registeredService(registeredService)
                    .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                    .build();
                final var accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
                accessResult.throwExceptionIfNeeded();
            }

            final var authenticationResult =
                this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
            final var serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service, authenticationResult);
            WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
            WebUtils.putWarnCookieIfRequestParameterPresent(this.warnCookieGenerator, context);
            return newEvent(CasWebflowConstants.TRANSITION_ID_WARN);

        } catch (final AuthenticationException | AbstractTicketException e) {
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }
    }
}
