package org.apereo.cas.services;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.annotation.Nullable;
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
        ensureServiceAccessIsAllowed(null, registeredService);
    }


    /**
     * Ensure service access is allowed.
     *
     * @param service           the service
     * @param registeredService the registered service
     */
    public static void ensureServiceAccessIsAllowed(@Nullable final Service service, @Nullable final RegisteredService registeredService) {
        val id = service != null ? service.getId() : "unknown";
        if (registeredService == null) {
            LOGGER.warn("Unauthorized Service Access. Service [{}] is not registered in the service registry. "
                + "Review the service access strategy to evaluate policies required for service access", id);
            throw UnauthorizedServiceException.denied("Service " + id + " is not found or is disabled in the service registry.");
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            val msg = String.format("Unauthorized Service Access. Service [%s] is not enabled in service registry. You should "
                + "review the service access strategy to evaluate the conditions and policies required for service access.", id);
            throw UnauthorizedServiceException.denied(msg);
        }
        if (!ensureServiceIsNotExpired(registeredService)) {
            val msg = String.format("Expired service access is denied. Service [%s] has been expired", id);
            throw UnauthorizedServiceException.expired(msg);
        }
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

        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)) {
            LOGGER.debug("Service [{}] is configured to not use SSO", service.getId());
            if (ticketGrantingTicket.getProxiedBy() != null) {
                LOGGER.warn("Service [{}] is not allowed to use SSO for proxying.", service.getId());
            }
            if (ticketGrantingTicket.getCountOfUses() > 0 && !credentialsProvided) {
                LOGGER.warn(
                    "Service [{}] is not allowed to use SSO. The ticket-granting ticket [{}] is not proxied and it's been used at least once. "
                        + "The authentication request must provide credentials before access can be granted", ticketGrantingTicket.getId(), service.getId());
            }
            if (ticketGrantingTicket.getCountOfUses() == 0 && credentialsProvided) {
                LOGGER.debug("The ticket-granting ticket [{}] has never been used before and "
                    + "the authentication request has supplied credentials to access service [{}]", ticketGrantingTicket.getId(), service.getId());
            } else {
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
     * @return true/false
     * @throws Throwable the throwable
     */
    public static boolean ensurePrincipalAccessIsAllowedForService(final Service service,
                                                                   final RegisteredService registeredService,
                                                                   final String principalId,
                                                                   final Map<String, List<Object>> attributes) throws Throwable {
        ensureServiceAccessIsAllowed(service, registeredService);

        val serviceId = service != null ? service.getId() : "unknown";
        LOGGER.trace("Checking access strategy for service [{}], requested by [{}] with attributes [{}].", serviceId, principalId, attributes);
        val accessRequest = RegisteredServiceAccessStrategyRequest.builder()
            .service(service)
            .principalId(principalId)
            .attributes(attributes)
            .registeredService(registeredService)
            .build();
        if (!registeredService.getAccessStrategy().authorizeRequest(accessRequest)) {
            LOGGER.warn("Cannot grant access to service [{}]; it is not authorized for use by [{}].", serviceId, principalId);
            val handlerErrors = new HashMap<String, Throwable>();
            val message = String.format("Cannot authorize principal %s to access service %s, likely due to insufficient permissions", principalId, serviceId);
            val exception = new UnauthorizedServiceForPrincipalException(message, registeredService, principalId, attributes);
            handlerErrors.put(UnauthorizedServiceForPrincipalException.class.getSimpleName(), exception);
            throw new PrincipalException(message, handlerErrors, new HashMap<>(0));
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
