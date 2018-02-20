package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link RegisteredServiceAccessStrategyUtils} that encapsulates common
 * operations relevant to registered service access strategy and authorizations.
 * This is a support utility class that acts as a façade around common authorization
 * and access strategy presented in CAS.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public final class RegisteredServiceAccessStrategyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceAccessStrategyUtils.class);

    private RegisteredServiceAccessStrategyUtils() {
    }

    /**
     * Ensure service access is allowed.
     *
     * @param registeredService the registered service
     */
    public static void ensureServiceAccessIsAllowed(final RegisteredService registeredService) {
        ensureServiceAccessIsAllowed(registeredService != null ? registeredService.getName() : StringUtils.EMPTY, registeredService);
    }

    /**
     * Ensure service access is allowed.
     *
     * @param service           the service
     * @param registeredService the registered service
     */
    public static void ensureServiceAccessIsAllowed(final String service, final RegisteredService registeredService) {
        if (registeredService == null) {
            final String msg = String.format("Unauthorized Service Access. Service [%s] is not found in service registry.", service);
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("Unauthorized Service Access. Service [%s] is not enabled in service registry.", service);
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
     * Ensure principal access is allowed for service.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param principalId       the principal id
     * @param attributes        the attributes
     */
    public static void ensurePrincipalAccessIsAllowedForService(final Service service,
                                                                final RegisteredService registeredService,
                                                                final String principalId,
                                                                final Map<String, Object> attributes) {
        ensureServiceAccessIsAllowed(service, registeredService);
        if (!registeredService.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principalId, attributes)) {
            LOGGER.warn("Cannot grant access to service [{}] because it is not authorized for use by [{}].", service.getId(), principalId);
            final Map<String, Class<? extends Throwable>> handlerErrors = new HashMap<>();
            handlerErrors.put(UnauthorizedServiceForPrincipalException.class.getSimpleName(), UnauthorizedServiceForPrincipalException.class);
            throw new PrincipalException(UnauthorizedServiceForPrincipalException.CODE_UNAUTHZ_SERVICE, handlerErrors, new HashMap<>());
        }
    }

    /**
     * Ensure principal access is allowed for service.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param authentication    the authentication
     */
    public static void ensurePrincipalAccessIsAllowedForService(final Service service,
                                                                final RegisteredService registeredService,
                                                                final Authentication authentication) {
        ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication, true);
    }

    /**
     * Ensure service access is allowed.
     *
     * @param service                                      the service
     * @param registeredService                            the registered service
     * @param authentication                               the authentication
     * @param retrievePrincipalAttributesFromReleasePolicy retrieve attributes from release policy or simply rely on the principal attributes
     *                                                     already collected. Setting this value to false bears the assumption that the policy
     *                                                     has run already.
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final Service service,
                                                                final RegisteredService registeredService,
                                                                final Authentication authentication,
                                                                final boolean retrievePrincipalAttributesFromReleasePolicy)
        throws UnauthorizedServiceException, PrincipalException {
        ensureServiceAccessIsAllowed(service, registeredService);

        final Principal principal = authentication.getPrincipal();
        final Map<String, Object> principalAttrs;
        if (retrievePrincipalAttributesFromReleasePolicy && registeredService != null && registeredService.getAttributeReleasePolicy() != null) {
            principalAttrs = registeredService.getAttributeReleasePolicy().getAttributes(principal, service, registeredService);
        } else {
            principalAttrs = authentication.getPrincipal().getAttributes();
        }
        final Map<String, Object> attributes = new LinkedHashMap<>(principalAttrs);
        attributes.putAll(authentication.getAttributes());
        ensurePrincipalAccessIsAllowedForService(service, registeredService, principal.getId(), attributes);
    }

    /**
     * Ensure service access is allowed.
     *
     * @param serviceTicket                                the service ticket
     * @param registeredService                            the registered service
     * @param ticketGrantingTicket                         the ticket granting ticket
     * @param retrievePrincipalAttributesFromReleasePolicy the retrieve principal attributes from release policy
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final ServiceTicket serviceTicket,
                                                                final RegisteredService registeredService,
                                                                final TicketGrantingTicket ticketGrantingTicket,
                                                                final boolean retrievePrincipalAttributesFromReleasePolicy)
        throws UnauthorizedServiceException, PrincipalException {
        ensurePrincipalAccessIsAllowedForService(serviceTicket.getService(),
            registeredService, ticketGrantingTicket.getAuthentication(), retrievePrincipalAttributesFromReleasePolicy);
    }

    /**
     * Ensure service access is allowed. Determines the final authentication object
     * by looking into the chained authentications of the ticket granting ticket.
     *
     * @param service                                      the service
     * @param registeredService                            the registered service
     * @param ticketGrantingTicket                         the ticket granting ticket
     * @param retrievePrincipalAttributesFromReleasePolicy the retrieve principal attributes from release policy
     * @throws UnauthorizedServiceException the unauthorized service exception
     * @throws PrincipalException           the principal exception
     */
    public static void ensurePrincipalAccessIsAllowedForService(final Service service, final RegisteredService registeredService,
                                                                final TicketGrantingTicket ticketGrantingTicket,
                                                                final boolean retrievePrincipalAttributesFromReleasePolicy)
        throws UnauthorizedServiceException, PrincipalException {
        ensurePrincipalAccessIsAllowedForService(service, registeredService,
            ticketGrantingTicket.getRoot().getAuthentication(), retrievePrincipalAttributesFromReleasePolicy);

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
        throws UnauthorizedServiceException, PrincipalException {
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
        ensureServiceSsoAccessIsAllowed(registeredService, service, ticketGrantingTicket, false);
    }

    /**
     * Ensure service sso access is allowed.
     *
     * @param registeredService    the registered service
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @param credentialsProvided  the credentials provided
     */
    public static void ensureServiceSsoAccessIsAllowed(final RegisteredService registeredService, final Service service,
                                                       final TicketGrantingTicket ticketGrantingTicket, final boolean credentialsProvided) {

        if (!registeredService.getAccessStrategy().isServiceAccessAllowedForSso()) {
            LOGGER.debug("Service [{}] is configured to not use SSO", service.getId());
            if (ticketGrantingTicket.getProxiedBy() != null) {
                LOGGER.warn("Service [{}] is not allowed to use SSO for proxying.", service.getId());
                throw new UnauthorizedSsoServiceException();
            }
            if (ticketGrantingTicket.getProxiedBy() == null && ticketGrantingTicket.getCountOfUses() > 0 && !credentialsProvided) {
                LOGGER.warn("Service [{}] is not allowed to use SSO. The ticket-granting ticket [{}] is not proxied and it's been used at least once. "
                    + "The authentication request must provide credentials before access can be granted", ticketGrantingTicket.getId(), service.getId());
                throw new UnauthorizedSsoServiceException();
            }
        }
        LOGGER.debug("Current authentication via ticket [{}] allows service [{}] to participate in the existing SSO session",
            ticketGrantingTicket.getId(), service.getId());
    }

}
