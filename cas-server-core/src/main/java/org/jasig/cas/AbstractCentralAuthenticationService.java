package org.jasig.cas;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Predicate;
import org.jasig.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ContextualAuthenticationPolicy;
import org.jasig.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceContext;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedProxyingException;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketFactory;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An abstract implementation of the {@link CentralAuthenticationService} that provides access to
 * the needed scaffolding and services that are necessary to CAS, such as ticket registry, service registry, etc.
 * The intention here is to allow extensions to easily benefit these already-configured components
 * without having to to duplicate them again.
 * @author Misagh Moayyed
 * @see CentralAuthenticationServiceImpl
 * @since 4.2.0
 */
public abstract class AbstractCentralAuthenticationService implements CentralAuthenticationService, Serializable,
        ApplicationEventPublisherAware {

    private static final long serialVersionUID = -7572316677901391166L;

    /** Log instance for logging events, info, warnings, errors, etc. */
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Application event publisher. */
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    /** {@link TicketRegistry}  for storing and retrieving tickets as needed. */
    @NotNull
    @Resource(name="ticketRegistry")
    protected TicketRegistry ticketRegistry;

    /** Implementation of Service Manager. */
    @NotNull
    @Resource(name="servicesManager")
    protected ServicesManager servicesManager;

    /** The logout manager. **/
    @NotNull
    @Resource(name="logoutManager")
    protected LogoutManager logoutManager;

    /** The ticket factory. **/
    @NotNull
    @Resource(name="defaultTicketFactory")
    protected TicketFactory ticketFactory;

    /**
     * Authentication policy that uses a service context to produce stateful security policies to apply when
     * authenticating credentials.
     */
    @NotNull
    @Resource(name="authenticationPolicyFactory")
    protected ContextualAuthenticationPolicyFactory<ServiceContext> serviceContextAuthenticationPolicyFactory =
            new AcceptAnyAuthenticationPolicyFactory();

    /** Factory to create the principal type. **/
    @NotNull
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /**
     * Instantiates a new Central authentication service impl.
     */
    protected AbstractCentralAuthenticationService() {}

    /**
     * Build the central authentication service implementation.
     *
     * @param ticketRegistry                     the tickets registry.
     * @param ticketFactory                      the ticket factory
     * @param servicesManager                    the services manager.
     * @param logoutManager                      the logout manager.
     */
    public AbstractCentralAuthenticationService(
            final TicketRegistry ticketRegistry,
            final TicketFactory ticketFactory,
            final ServicesManager servicesManager,
            final LogoutManager logoutManager) {

        this.ticketRegistry = ticketRegistry;
        this.servicesManager = servicesManager;
        this.logoutManager = logoutManager;
        this.ticketFactory = ticketFactory;
    }

    public final void setServiceContextAuthenticationPolicyFactory(final ContextualAuthenticationPolicyFactory<ServiceContext> policy) {
        this.serviceContextAuthenticationPolicyFactory = policy;
    }

    public void setTicketFactory(final TicketFactory ticketFactory) {
        this.ticketFactory = ticketFactory;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    @Autowired
    public final void setPrincipalFactory(@Qualifier("principalFactory")
                                    final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    /**
     * Publish CAS events.
     *
     * @param e the event
     */
    protected final void doPublishEvent(final ApplicationEvent e) {
        logger.debug("Publishing {}", e);
        this.eventPublisher.publishEvent(e);
    }


    /**
     * {@inheritDoc}
     *
     * Note:
     * Synchronization on ticket object in case of cache based registry doesn't serialize
     * access to critical section. The reason is that cache pulls serialized data and
     * builds new object, most likely for each pull. Is this synchronization needed here?
     */
    @Timed(name = "GET_TICKET_TIMER")
    @Metered(name = "GET_TICKET_METER")
    @Counted(name="GET_TICKET_COUNTER", monotonic=true)
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<? extends Ticket> clazz)
            throws InvalidTicketException {
        Assert.notNull(ticketId, "ticketId cannot be null");
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId, clazz);

        if (ticket == null) {
            logger.debug("Ticket [{}] by type [{}] cannot be found in the ticket registry.", ticketId, clazz.getSimpleName());
            throw new InvalidTicketException(ticketId);
        }

        if (ticket instanceof TicketGrantingTicket) {
            synchronized (ticket) {
                if (ticket.isExpired()) {
                    this.ticketRegistry.deleteTicket(ticketId);
                    logger.debug("Ticket [{}] has expired and is now deleted from the ticket registry.", ticketId);
                    throw new InvalidTicketException(ticketId);
                }
            }
        }
        return (T) ticket;
    }

    @Timed(name = "GET_TICKETS_TIMER")
    @Metered(name = "GET_TICKETS_METER")
    @Counted(name="GET_TICKETS_COUNTER", monotonic=true)
    @Override
    public Collection<Ticket> getTickets(final Predicate<Ticket> predicate) {
        final Collection<Ticket> c = new HashSet<>(this.ticketRegistry.getTickets());
        final Iterator<Ticket> it = c.iterator();
        while (it.hasNext()) {
            if (!predicate.apply(it.next())) {
                it.remove();
            }
        }
        return c;
    }

    /**
     * Gets the authentication satisfied by policy.
     *
     * @param ticket the ticket
     * @param context the context
     * @return the authentication satisfied by policy
     * @throws AbstractTicketException the ticket exception
     */
    protected final Authentication getAuthenticationSatisfiedByPolicy(
            final TicketGrantingTicket ticket, final ServiceContext context) throws AbstractTicketException {

        final ContextualAuthenticationPolicy<ServiceContext> policy =
                serviceContextAuthenticationPolicyFactory.createPolicy(context);
        if (policy.isSatisfiedBy(ticket.getAuthentication())) {
            return ticket.getAuthentication();
        }
        for (final Authentication auth : ticket.getSupplementalAuthentications()) {
            if (policy.isSatisfiedBy(auth)) {
                return auth;
            }
        }
        throw new UnsatisfiedAuthenticationPolicyException(policy);
    }

    /**
     * Ensure that the service is found and enabled in the service registry.
     * @param registeredService the located entry in the registry
     * @param service authenticating service
     * @throws UnauthorizedServiceException if service is unauthorized
     */
    protected final void verifyRegisteredServiceProperties(final RegisteredService registeredService,
                                                           final Service service) throws UnauthorizedServiceException {
        if (registeredService == null) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not found in service registry.", service.getId());
            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("ServiceManagement: Unauthorized Service Access. "
                    + "Service [%s] is not enabled in service registry.", service.getId());

            logger.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
    }

    /**
     * Evaluate proxied service if needed.
     *
     * @param service the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @param registeredService the registered service
     */
    protected final void evaluateProxiedServiceIfNeeded(final Service service, final TicketGrantingTicket ticketGrantingTicket,
                                                final RegisteredService registeredService) {
        final Service proxiedBy = ticketGrantingTicket.getProxiedBy();
        if (proxiedBy != null) {
            logger.debug("TGT is proxied by [{}]. Locating proxy service in registry...", proxiedBy.getId());
            final RegisteredService proxyingService = servicesManager.findServiceBy(proxiedBy);

            if (proxyingService != null) {
                logger.debug("Located proxying service [{}] in the service registry", proxyingService);
                if (!proxyingService.getProxyPolicy().isAllowedToProxy()) {
                    logger.warn("Found proxying service {}, but it is not authorized to fulfill the proxy attempt made by {}",
                            proxyingService.getId(), service.getId());
                    throw new UnauthorizedProxyingException("Proxying is not allowed for registered service "
                            + registeredService.getId());
                }
            } else {
                logger.warn("No proxying service found. Proxy attempt by service [{}] (registered service [{}]) is not allowed.",
                        service.getId(), registeredService.getId());
                throw new UnauthorizedProxyingException("Proxying is not allowed for registered service "
                        + registeredService.getId());
            }
        } else {
            logger.trace("TGT is not proxied by another service");
        }
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setLogoutManager(final LogoutManager logoutManager) {
        this.logoutManager = logoutManager;
    }
}
