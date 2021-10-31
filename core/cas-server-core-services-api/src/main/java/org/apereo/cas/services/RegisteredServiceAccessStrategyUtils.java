package org.apereo.cas.services;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
@Slf4j
@UtilityClass
public class RegisteredServiceAccessStrategyUtils {

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
            LOGGER.warn("Unauthorized Service Access. Service [{}] is not registered in service registry.", service);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Service is not found in service registry.");
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            val msg = String.format("Unauthorized Service Access. Service [%s] is not enabled in service registry.", service);
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!ensureServiceIsNotExpired(registeredService)) {
            val msg = String.format("Expired service access is denied. Service [%s] has been expired", service);
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_EXPIRED_SERVICE, msg);
        }
    }

    /**
     * Ensure service access is allowed.
     *
     * @param service           the service
     * @param registeredService the registered service
     */
    public static void ensureServiceAccessIsAllowed(final Service service, final RegisteredService registeredService) {
        ensureServiceAccessIsAllowed(service != null ? service.getId() : "unknown", registeredService);
    }

    /**
     * Ensure service is not expired.
     *
     * @param registeredService the service
     * @return boolean - true if service is not expired
     */
    public static boolean ensureServiceIsNotExpired(final RegisteredService registeredService) {
        return getRegisteredServiceExpirationPolicyPredicate().test(registeredService);
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
    public static void ensureServiceSsoAccessIsAllowed(final RegisteredService registeredService,
                                                       final Service service,
                                                       final TicketGrantingTicket ticketGrantingTicket,
                                                       final boolean credentialsProvided) {

        if (!registeredService.getAccessStrategy().isServiceAccessAllowedForSso()) {
            LOGGER.debug("Service [{}] is configured to not use SSO", service.getId());
            if (ticketGrantingTicket.getProxiedBy() != null) {
                LOGGER.warn("Service [{}] is not allowed to use SSO for proxying.", service.getId());
                throw new UnauthorizedSsoServiceException();
            }
            if (ticketGrantingTicket.getCountOfUses() > 0 && !credentialsProvided) {
                LOGGER.warn(
                    "Service [{}] is not allowed to use SSO. The ticket-granting ticket [{}] is not proxied and it's been used at least once. "
                    + "The authentication request must provide credentials before access can be granted", ticketGrantingTicket.getId(),
                    service.getId());
                throw new UnauthorizedSsoServiceException();
            }
        }
        LOGGER.debug("Current authentication via ticket [{}] allows service [{}] to participate in the existing SSO session",
            ticketGrantingTicket.getId(), service.getId());
    }

    /**
     * Ensure principal access is allowed for service.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param principalId       the principal id
     * @param attributes        the attributes
     * @return the boolean
     */
    public static boolean ensurePrincipalAccessIsAllowedForService(final Service service,
                                                                   final RegisteredService registeredService,
                                                                   final String principalId,
                                                                   final Map<String, List<Object>> attributes) {
        ensureServiceAccessIsAllowed(service, registeredService);
        LOGGER.trace("Checking access strategy for service [{}], requested by [{}] with attributes [{}].",
            service != null ? service.getId() : "unknown", principalId, attributes);

        if (!registeredService.getAccessStrategy().doPrincipalAttributesAllowServiceAccess(principalId, (Map) attributes)) {
            LOGGER.warn("Cannot grant access to service [{}]; it is not authorized for use by [{}].",
                service != null ? service.getId() : "unknown", principalId);
            val handlerErrors = new HashMap<String, Throwable>();
            val message = String.format("Cannot grant service access to %s", principalId);
            val exception = new UnauthorizedServiceForPrincipalException(message, registeredService, principalId, attributes);
            handlerErrors.put(UnauthorizedServiceForPrincipalException.class.getSimpleName(), exception);
            throw new PrincipalException(UnauthorizedServiceForPrincipalException.CODE_UNAUTHZ_SERVICE,
                handlerErrors, new HashMap<>(0));
        }
        return true;
    }


    /**
     * Gets registered service expiration policy predicate.
     *
     * @return the registered service expiration policy predicate
     */
    public static Predicate<RegisteredService> getRegisteredServiceExpirationPolicyPredicate() {
        return service -> {
            if (service == null) {
                return false;
            }
            val policy = service.getExpirationPolicy();
            return policy == null || !policy.isExpired();
        };
    }

}
