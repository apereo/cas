package org.jasig.cas.services;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.PrincipalException;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RegisteredServiceAccessStrategySupport} that encapsulates common
 * operations relevant to registered service access strategy and authorizations.
 * This is a support utility class that acts as a façade around common authorization
 * and access strategy presented in CAS.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public final class RegisteredServiceAccessStrategySupport {

    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceAccessStrategySupport.class);

    /**
     * Instantiates a new Registered service access strategy support.
     */
    private RegisteredServiceAccessStrategySupport() {
    }

    /**
     * Ensure service access is allowed.
     *
     * @param service           the service
     * @param registeredService the registered service
     */
    public static void ensureServiceAccessIsAllowed(final String service, final RegisteredService registeredService) {
        if (registeredService == null) {
            final String msg = String.format("Unauthorized Service Access. Service [%s] is not found in service registry.",
                    service);
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("Unauthorized Service Access. Service [%s] is not enabled in service registry.",
                    service);

            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
    }
    
    /**
     * Ensure service access is allowed.
     *
     * @param service           the service
     * @param registeredService the registered service
     */
    public static void ensureServiceAccessIsAllowed(final Service service, final RegisteredService registeredService) {
        ensureServiceAccessIsAllowed(service.getId(), registeredService);
    }

    /**
     * Ensure service access is allowed.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final Service service,
                                                                final RegisteredService registeredService,
                                                                 final Authentication authentication)
            throws UnauthorizedServiceException, PrincipalException {
        ensureServiceAccessIsAllowed(service, registeredService);
        final Principal principal = authentication.getPrincipal();
        final Map<String, Object> principalAttrs = registeredService.getAttributeReleasePolicy().getAttributes(principal);
        if (!registeredService.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principal.getId(), principalAttrs)) {
            LOGGER.warn("Cannot grant access to service [{}] because it is not authorized for use by [{}].",
                    service.getId(), principal);

            final Map<String, Class<? extends Exception>> handlerErrors = new HashMap<>();
            handlerErrors.put(UnauthorizedServiceForPrincipalException.class.getSimpleName(),
                    UnauthorizedServiceForPrincipalException.class);
            throw new PrincipalException(UnauthorizedServiceForPrincipalException.CODE_UNAUTHZ_SERVICE, handlerErrors, new HashMap());
        }
    }

    /**
     * Ensure service access is allowed.
     *
     * @param serviceTicket        the service ticket
     * @param registeredService    the registered service
     * @param ticketGrantingTicket the ticket granting ticket
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final ServiceTicket serviceTicket,
                                                                final RegisteredService registeredService,
                                                                 final TicketGrantingTicket ticketGrantingTicket)
            throws UnauthorizedServiceException,  PrincipalException  {
        ensurePrincipalAccessIsAllowedForService(serviceTicket.getService(), registeredService, ticketGrantingTicket.getAuthentication());
    }

    /**
     * Ensure service access is allowed. Determines the final authentication object
     * by looking into the chained authentications of the ticket granting ticket.
     *
     * @param service              the service
     * @param registeredService    the registered service
     * @param ticketGrantingTicket the ticket granting ticket
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final Service service, final RegisteredService registeredService,
                                                                final TicketGrantingTicket ticketGrantingTicket)
            throws UnauthorizedServiceException,  PrincipalException  {
        final List<Authentication> authentications = ticketGrantingTicket.getChainedAuthentications();
        final Authentication authentication = authentications.get(authentications.size() - 1);
        ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);

    }

    /**
     * Ensure service access is allowed.
     *
     * @param serviceTicket     the service ticket
     * @param context           the context
     * @param registeredService the registered service
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final ServiceTicket serviceTicket,
                                                                final AuthenticationResult context,
                                                                final RegisteredService registeredService)
                                                    throws UnauthorizedServiceException,  PrincipalException  {
        ensurePrincipalAccessIsAllowedForService(serviceTicket.getService(), registeredService, context.getAuthentication());
    }

    /**
     * Ensure service sso access is allowed.
     *
     * @param registeredService    the registered service
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     */
    public static void ensureServiceSsoAccessIsAllowed(final RegisteredService registeredService, final Service service,
                                                       final TicketGrantingTicket ticketGrantingTicket) {

        if (!registeredService.getAccessStrategy().isServiceAccessAllowedForSso()) {
            LOGGER.debug("Service {} is configured to not use SSO", service.getId());
            if (ticketGrantingTicket.getProxiedBy() != null) {
                LOGGER.warn("ServiceManagement: Service [{}] is not allowed to use SSO for proxying.", service.getId());
                throw new UnauthorizedSsoServiceException();
            } else if (ticketGrantingTicket.getProxiedBy() == null && ticketGrantingTicket.getCountOfUses() > 0) {
                LOGGER.warn("ServiceManagement: Service [{}] is not allowed to use SSO.", service.getId());
                throw new UnauthorizedSsoServiceException();
            }
        }
        LOGGER.debug("Current authentication via ticket {} allows service {} to participate in the existing SSO session",
                ticketGrantingTicket.getId(), service.getId());
    }
    
}
