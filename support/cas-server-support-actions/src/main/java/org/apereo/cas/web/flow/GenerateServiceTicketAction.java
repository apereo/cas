package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to generate a service ticket for a given Ticket Granting Ticket and
 * Service.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class GenerateServiceTicketAction extends AbstractAction {

    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final CentralAuthenticationService centralAuthenticationService;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
    private final ServicesManager servicesManager;
    private final PrincipalElectionStrategy principalElectionStrategy;

    /**
     * {@inheritDoc}
     * <p>
     * In the initial primary authentication flow, credentials are cached and available.
     * Since they are authenticated as part of submission first, there is no need to doubly
     * authenticate and verify credentials.
     * <p>
     * In subsequent authentication flows where a TGT is available and only an ST needs to be
     * created, there are no cached copies of the credential, since we do have a TGT available.
     * So we will simply grab the available authentication and produce the final result based on that.
     */
    @Override
    protected Event doExecute(final RequestContext context) {
        val service = WebUtils.getService(context);
        LOGGER.trace("Service asking for service ticket is [{}]", service);

        val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.debug("Ticket-granting ticket found in the context is [{}]", ticketGrantingTicket);

        try {
            val authentication = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
            if (authentication == null) {
                val authn = new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket);
                throw new InvalidTicketException(authn, ticketGrantingTicket);
            }

            val selectedService = authenticationRequestServiceSelectionStrategies.resolveService(service);
            val registeredService = servicesManager.findServiceBy(selectedService);
            LOGGER.debug("Registered service asking for service ticket is [{}]", registeredService);
            WebUtils.putRegisteredService(context, registeredService);
            WebUtils.putServiceIntoFlowScope(context, service);

            if (registeredService != null) {
                val url = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl();
                if (url != null) {
                    LOGGER.debug("Registered service may redirect to [{}] for unauthorized access requests", url);
                }
                WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, url);
            }
            if (WebUtils.getWarningCookie(context)) {
                LOGGER.debug("Warning cookie is present in the request context. Routing result to [{}] state", CasWebflowConstants.STATE_ID_WARN);
                return result(CasWebflowConstants.STATE_ID_WARN);
            }

            val credential = WebUtils.getCredential(context);
            val builder = this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);
            val authenticationResult = builder.build(principalElectionStrategy, service);

            LOGGER.trace("Built the final authentication result [{}] to grant service ticket to [{}]", authenticationResult, service);
            val serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
            WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
            LOGGER.debug("Granted service ticket [{}] and added it to the request scope", serviceTicketId);
            return success();

        } catch (final AbstractTicketException e) {
            if (e instanceof InvalidTicketException) {
                LOGGER.debug("CAS has determined ticket-granting ticket [{}] is invalid and must be destroyed", ticketGrantingTicket);
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            }
            if (isGatewayPresent(context)) {
                LOGGER.debug("Request indicates that it is gateway. Routing result to [{}] state", CasWebflowConstants.TRANSITION_ID_GATEWAY);
                return result(CasWebflowConstants.TRANSITION_ID_GATEWAY);
            }
            LOGGER.warn("Could not grant service ticket [{}]. Routing to [{}]", e.getMessage(), CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }
    }

    /**
     * Checks if {@code gateway} is present in the request params.
     *
     * @param context the context
     * @return true, if gateway present
     */
    protected boolean isGatewayPresent(final RequestContext context) {
        val requestParameterMap = context.getExternalContext().getRequestParameterMap();
        return StringUtils.hasText(requestParameterMap.get(CasProtocolConstants.PARAMETER_GATEWAY));
    }

    /**
     * New event based on the id, which contains an error attribute referring to the exception occurred.
     *
     * @param id    the id
     * @param error the error
     * @return the event
     */
    private Event newEvent(final String id, final Exception error) {
        return new EventFactorySupport().event(this, id, new LocalAttributeMap<>("error", error));
    }
}
