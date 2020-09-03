package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    public ServiceTicketRequestWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext configContext) {
        super(configContext);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        if (isRequestAskingForServiceTicket(context)) {
            LOGGER.debug("Authentication request is asking for service tickets");
            val source = grantServiceTicket(context);
            return source != null ? CollectionUtils.wrapSet(source) : null;
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
    @SneakyThrows
    protected boolean isRequestAskingForServiceTicket(final RequestContext context) {
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.debug("Located ticket-granting ticket [{}] from the request context", ticketGrantingTicketId);

        val service = WebUtils.getService(context);
        LOGGER.debug("Located service [{}] from the request context", service);

        val configContext = getWebflowEventResolutionConfigurationContext();
        val renewParam = configContext.getCasProperties().getSso().isRenewAuthnEnabled()
            ? context.getRequestParameters().get(CasProtocolConstants.PARAMETER_RENEW)
            : StringUtils.EMPTY;
        LOGGER.debug("Provided value for [{}] request parameter is [{}]", CasProtocolConstants.PARAMETER_RENEW, renewParam);

        if (service != null && StringUtils.isNotBlank(ticketGrantingTicketId)) {
            val authn = configContext.getTicketRegistrySupport().getAuthenticationFrom(ticketGrantingTicketId);
            val validAuthn = authn != null;
            if (StringUtils.isNotBlank(renewParam)) {
                LOGGER.debug("Request identifies itself as one asking for service tickets. Checking for authentication context validity...");
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
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        val credential = getCredentialFromContext(context);

        try {
            val service = WebUtils.getService(context);
            val configContext = getWebflowEventResolutionConfigurationContext();

            val existingAuthn = configContext.getTicketRegistrySupport().getAuthenticationFrom(ticketGrantingTicketId);
            val registeredService = configContext.getServicesManager().findServiceBy(service);

            if (existingAuthn != null && registeredService != null) {
                LOGGER.debug("Enforcing access strategy policies for registered service [{}] and principal [{}]", registeredService, existingAuthn.getPrincipal());

                val audit = AuditableContext.builder().service(service)
                    .authentication(existingAuthn)
                    .registeredService(registeredService)
                    .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                    .build();
                val accessResult = configContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
                accessResult.throwExceptionIfNeeded();
            }

            LOGGER.trace("Finalizing authentication transaction for [{}]", credential);
            val authenticationResult = configContext.getAuthenticationSystemSupport()
                .handleAndFinalizeSingleAuthenticationTransaction(service, credential);

            val principal = authenticationResult.getAuthentication().getPrincipal();
            if (existingAuthn != null && !existingAuthn.getPrincipal().equals(principal)) {
                LOGGER.trace("Existing authentication context linked to ticket-granting ticket [{}] is issued for principal [{}] "
                        + " which does not match [{}], established by the requested authentication transaction. CAS will NOT re-use the existing "
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
}
