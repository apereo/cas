package org.apereo.cas;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.UnauthorizedProxyingException;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.context.ApplicationEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCentralAuthenticationService implements CentralAuthenticationService, Serializable {

    private static final long serialVersionUID = -7572316677901391166L;

    /**
     * Configuration context.
     */
    protected final CentralAuthenticationServiceContext configurationContext;

    @Transactional(transactionManager = "ticketTransactionManager", noRollbackFor = InvalidTicketException.class)
    @Override
    public Ticket getTicket(final @NonNull String ticketId) throws InvalidTicketException {
        val ticket = configurationContext.getTicketRegistry().getTicket(ticketId);
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
        val ticket = configurationContext.getTicketRegistry().getTicket(ticketId, clazz);
        verifyTicketState(ticket, ticketId, clazz);
        return (T) ticket;
    }

    @Transactional(transactionManager = "ticketTransactionManager")
    @Override
    public Collection<Ticket> getTickets(final Predicate<Ticket> predicate) {
        try (val ticketsStream = configurationContext.getTicketRegistry().stream().filter(predicate)) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Transactional(transactionManager = "ticketTransactionManager")
    @Override
    public Stream<? extends Ticket> getTickets(final Predicate<Ticket> predicate, final long from, final long count) {
        return configurationContext.getTicketRegistry().stream().filter(predicate).skip(from).limit(count);
    }

    @Audit(
        action = AuditableActions.TICKET_DESTROYED,
        actionResolverName = AuditActionResolvers.DESTROY_TICKET_RESOLVER,
        resourceResolverName = AuditResourceResolvers.DESTROY_TICKET_RESOURCE_RESOLVER)
    @Transactional(transactionManager = "ticketTransactionManager")
    @Override
    public int deleteTicket(final String ticketId) throws Exception {
        return StringUtils.isNotBlank(ticketId)
            ? configurationContext.getTicketRegistry().deleteTicket(ticketId)
            : 0;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        configurationContext.getTicketRegistry().updateTicket(ticket);
        return ticket;
    }

    @Override
    public Ticket addTicket(final Ticket ticket) throws Exception {
        configurationContext.getTicketRegistry().addTicket(ticket);
        return ticket;
    }

    /**
     * Publish CAS events.
     *
     * @param e the event
     */
    protected void doPublishEvent(final ApplicationEvent e) {
        if (configurationContext.getApplicationContext() != null) {
            LOGGER.trace("Publishing [{}]", e);
            configurationContext.getApplicationContext().publishEvent(e);
        }
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
        val policy = configurationContext.getAuthenticationPolicyFactory().createPolicy(context);
        try {
            if (policy.isSatisfiedBy(authentication)) {
                return authentication;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
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
            val proxyingService = configurationContext.getServicesManager().findServiceBy(proxiedBy);
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
            FunctionUtils.doUnchecked(s -> deleteTicket(id));
            LOGGER.debug("Ticket [{}] has expired and is now deleted from the ticket registry.", ticket);
            throw new InvalidTicketException(id);
        }
    }

    /**
     * Resolve service from authentication request.
     *
     * @param service the service
     * @return the service
     */
    protected WebApplicationService resolveServiceFromAuthenticationRequest(final Service service) {
        return configurationContext.getAuthenticationServiceSelectionPlan().resolveService(service, WebApplicationService.class);
    }

    /**
     * Verify the ticket id received is actually legitimate
     * before contacting downstream systems to find and process it.
     *
     * @param ticketId the ticket id
     * @return true/false
     */
    protected boolean isTicketAuthenticityVerified(final String ticketId) {
        try {
            if (configurationContext.getCipherExecutor() != null) {
                LOGGER.trace("Attempting to decode service ticket [{}] to verify authenticity", ticketId);
                return !ObjectUtils.isEmpty(configurationContext.getCipherExecutor().decode(ticketId));
            }
            return !ObjectUtils.isEmpty(ticketId);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return false;
    }

    @Override
    public TicketFactory getTicketFactory() {
        return this.configurationContext.getTicketFactory();
    }
}
