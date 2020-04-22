package org.apereo.cas;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceMatchingStrategy;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An abstract implementation of the {@link CentralAuthenticationService} that provides access to
 * the needed scaffolding and services that are necessary to CAS, such as ticket registry, service registry, etc.
 * The intention here is to allow extensions to easily benefit from these already-configured components
 * without having to to duplicate them again.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@Setter
@AllArgsConstructor
public abstract class AbstractCentralAuthenticationService implements CentralAuthenticationService, Serializable, ApplicationEventPublisherAware {

    private static final long serialVersionUID = -7572316677901391166L;
    /**
     * Application event publisher.
     */
    protected ApplicationEventPublisher applicationEventPublisher;

    /**
     * {@link TicketRegistry} for storing and retrieving tickets as needed.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * Implementation of Service Manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The logout manager.
     **/
    protected final LogoutManager logoutManager;

    /**
     * The ticket factory.
     **/
    protected final TicketFactory ticketFactory;

    /**
     * The service selection strategy during validation events.
     **/
    protected final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * Authentication policy that uses a service context to produce stateful security policies to apply when
     * authenticating credentials.
     */
    protected final ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory;

    /**
     * Factory to create the principal type.
     **/
    protected final PrincipalFactory principalFactory;

    /**
     * Cipher executor to handle ticket validation.
     */
    protected final CipherExecutor<String, String> cipherExecutor;

    /**
     * Enforce registered service access in an auditable way
     * since the access strategy is not usually managed as a Spring bean.
     */
    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    /**
     * Service matching strategy to ensure, primarily, that service tickets
     * can be validated for services that owns them.
     */
    protected final ServiceMatchingStrategy serviceMatchingStrategy;

    /**
     * Publish CAS events.
     *
     * @param e the event
     */
    protected void doPublishEvent(final ApplicationEvent e) {
        if (applicationEventPublisher != null) {
            LOGGER.trace("Publishing [{}]", e);
            this.applicationEventPublisher.publishEvent(e);
        }
    }

    @Transactional(transactionManager = "ticketTransactionManager", noRollbackFor = InvalidTicketException.class)
    @Override
    public Ticket getTicket(final @NonNull String ticketId) throws InvalidTicketException {
        val ticket = this.ticketRegistry.getTicket(ticketId);
        verifyTicketState(ticket, ticketId, null);
        return ticket;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note:
     * Synchronization on ticket object in case of cache based registry doesn't serialize
     * access to critical section. The reason is that cache pulls serialized data and
     * builds new object, most likely for each pull. Is this synchronization needed here?
     */
    @Transactional(transactionManager = "ticketTransactionManager", noRollbackFor = InvalidTicketException.class)
    @Override
    public <T extends Ticket> T getTicket(final @NonNull String ticketId, final Class<T> clazz) throws InvalidTicketException {
        val ticket = this.ticketRegistry.getTicket(ticketId, clazz);
        verifyTicketState(ticket, ticketId, clazz);
        return (T) ticket;
    }

    @Transactional(transactionManager = "ticketTransactionManager")
    @Override
    public Collection<Ticket> getTickets(final Predicate<Ticket> predicate) {
        try (val ticketsStream = this.ticketRegistry.getTicketsStream().filter(predicate)) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Transactional(transactionManager = "ticketTransactionManager")
    @Override
    public void deleteTicket(final String ticketId) {
        this.ticketRegistry.deleteTicket(ticketId);
    }

    /**
     * Gets the authentication satisfied by policy.
     *
     * @param authentication the authentication
     * @param context        the context
     * @return the authentication satisfied by policy
     * @throws AbstractTicketException the ticket exception
     */
    protected Authentication getAuthenticationSatisfiedByPolicy(final Authentication authentication, final ServiceContext context) throws AbstractTicketException {
        val policy = this.serviceContextAuthenticationPolicyFactory.createPolicy(context);
        try {
            if (policy.isSatisfiedBy(authentication)) {
                return authentication;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        throw new UnsatisfiedAuthenticationPolicyException(policy);
    }

    /**
     * Evaluate proxied service if needed.
     *
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @param registeredService    the registered service
     */
    protected void evaluateProxiedServiceIfNeeded(final Service service, final TicketGrantingTicket ticketGrantingTicket, final RegisteredService registeredService) {
        val proxiedBy = ticketGrantingTicket.getProxiedBy();
        if (proxiedBy != null) {
            LOGGER.debug("Ticket-granting ticket is proxied by [{}]. Locating proxy service in registry...", proxiedBy.getId());
            val proxyingService = this.servicesManager.findServiceBy(proxiedBy);
            if (proxyingService != null) {
                LOGGER.debug("Located proxying service [{}] in the service registry", proxyingService);
                if (!proxyingService.getProxyPolicy().isAllowedToProxy()) {
                    LOGGER.warn("Proxying service [{}] is not authorized to fulfill the proxy attempt made by [{}]", proxyingService.getId(), service.getId());
                    throw new UnauthorizedProxyingException(UnauthorizedProxyingException.MESSAGE + registeredService.getId());
                }
            } else {
                LOGGER.warn("Proxy attempt by service [{}] (registered service [{}]) is not allowed.", service.getId(), registeredService.getId());
                throw new UnauthorizedProxyingException(UnauthorizedProxyingException.MESSAGE + registeredService.getId());
            }
        } else {
            LOGGER.trace("Ticket-granting ticket is not proxied by another service");
        }
    }

    /**
     * Validate ticket expiration policy and throws exception if ticket is no longer valid.
     * Expired tickets are also deleted from the registry immediately on demand.
     *
     * @param ticket the ticket
     * @param id     the original id
     * @param clazz  the clazz
     */
    @Synchronized
    protected void verifyTicketState(final Ticket ticket, final String id, final Class clazz) {
        if (ticket == null) {
            LOGGER.debug("Ticket [{}] by type [{}] cannot be found in the ticket registry.", id, clazz != null ? clazz.getSimpleName() : "unspecified");
            throw new InvalidTicketException(id);
        }
        if (ticket.isExpired()) {
            deleteTicket(id);
            LOGGER.debug("Ticket [{}] has expired and is now deleted from the ticket registry.", ticket);
            throw new InvalidTicketException(id);
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        this.ticketRegistry.updateTicket(ticket);
        return ticket;
    }

    @Override
    public Ticket addTicket(final Ticket ticket) {
        this.ticketRegistry.addTicket(ticket);
        return ticket;
    }

    /**
     * Resolve service from authentication request.
     *
     * @param service the service
     * @return the service
     */
    protected Service resolveServiceFromAuthenticationRequest(final Service service) {
        return authenticationRequestServiceSelectionStrategies.resolveService(service);
    }

    /**
     * Verify the ticket id received is actually legitimate
     * before contacting downstream systems to find and process it.
     *
     * @param ticketId the ticket id
     * @return true/false
     */
    protected boolean isTicketAuthenticityVerified(final String ticketId) {
        if (this.cipherExecutor != null) {
            LOGGER.trace("Attempting to decode service ticket [{}] to verify authenticity", ticketId);
            return !StringUtils.isEmpty(this.cipherExecutor.decode(ticketId));
        }
        return !StringUtils.isEmpty(ticketId);
    }
}
