/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.factory.TicketFactory;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.validation.TicketValidator;
import org.jasig.cas.ticket.validation.ValidationRequest;
import org.springframework.aop.framework.ProxyFactory;


/**
 * Default implementation of the TicketManager. Maintains tickets in a TicketRegistry, and uses a TicketFactory to
 * create tickets. Validation is done by a TicketValidator.
 * <p>
 * Note that any ticket returned by the ticket manager will be wrapped in the "protective proxy" using
 * <code>ImmutableTicketProxyFactory</code> strategy, so that clients of the TicketManager do not have access (would
 * not be able to cast it to the internal implemantation class) to the methods that can modify the internal state of the
 * ticket. Only this factory is allowed to change the state of a Ticket by working directly with the internal
 * implementation of a Ticket -<code>AbstractTicket</code>
 * <p>
 * Note that the default <code>ImmutableTicketProxyFactory</code> strategy used by this factory is SpringAOP proxy
 * built on top of JDK1.3+ dynamic proxy but could easily be replaced by different implementation by any IoC container
 * via Dependency Injection.
 * 
 * @author Scott Battaglia
 * @author Dmitry Kopylenko
 * @version $Id$
 * @see org.jasig.cas.ticket.factory.TicketFactory
 * @see org.jasig.cas.ticket.registry.TicketRegistry
 * @see org.jasig.cas.ticket.validation.TicketValidator
 * @see org.jasig.cas.ticket.ImmutableTicketProxyFactory
 * @see org.jasig.cas.ticket.AbstractTicket
 */
public class TicketManagerImpl implements TicketManager {
	protected final Log logger = LogFactory.getLog(getClass());
    private TicketFactory ticketFactory;
    private TicketRegistry ticketRegistry;
    private TicketValidator ticketValidator;
    private ImmutableTicketProxyFactory immutableTicketProxyFactory = new SpringAOPImmutableTicketProxyFactory();

    /**
     * All public ticket creation requests fall into this private method which calls the factory and then adds the
     * ticket to the registry.
     * 
     * @param clazz The class of ticket we want to make
     * @param ticketCreationAttributes the initial ticket parameters.
     * @param ticket the parent ticket
     * @return the newly created ticket.
     */
    private Ticket createAndAddToRegistry(final Class clazz, final Principal principal, final CasAttributes casAttributes, final Ticket ticket) {
        final Ticket newTicket = ticketFactory.getTicket(clazz, principal, casAttributes, ticket);
        ticketRegistry.addTicket(newTicket);
        return this.immutableTicketProxyFactory.getProxyForTicket(newTicket);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#createTicketGrantingTicket(org.jasig.cas.authentication.principal.Principal, org.jasig.cas.ticket.CasAttributes)
     */
    public TicketGrantingTicket createTicketGrantingTicket(final Principal principal, final CasAttributes casAttributes) {
        return (TicketGrantingTicket) this.createAndAddToRegistry(TicketGrantingTicket.class, principal, casAttributes, null);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#createProxyGrantingTicket(org.jasig.cas.authentication.principal.Principal, org.jasig.cas.ticket.CasAttributes, org.jasig.cas.ticket.ServiceTicket)
     */
    public ProxyGrantingTicket createProxyGrantingTicket(final Principal principal, final CasAttributes casAttributes, final ServiceTicket ticket) {
        return (ProxyGrantingTicket) this.createAndAddToRegistry(ProxyGrantingTicket.class,principal, casAttributes, ticket);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#createProxyTicket(org.jasig.cas.authentication.principal.Principal, org.jasig.cas.ticket.CasAttributes, org.jasig.cas.ticket.ProxyGrantingTicket)
     */
    public ProxyTicket createProxyTicket(final Principal principal, final CasAttributes casAttributes, final ProxyGrantingTicket ticket) {
        return (ProxyTicket) this.createAndAddToRegistry(ProxyTicket.class, principal, casAttributes, ticket);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#createServiceTicket(org.jasig.cas.authentication.principal.Principal, org.jasig.cas.ticket.CasAttributes, org.jasig.cas.ticket.TicketGrantingTicket)
     */
    public ServiceTicket createServiceTicket(final Principal principal, final CasAttributes casAttributes, final TicketGrantingTicket ticket) {
        return (ServiceTicket) this.createAndAddToRegistry(ServiceTicket.class, principal, casAttributes, ticket);
    }

    /**
     * @see org.jasig.cas.ticket.TicketManager#deleteTicket(java.lang.String)
     */
    public boolean deleteTicket(final String ticketId) {
    	return (ticketId == null) ? false : ticketRegistry.deleteTicket(ticketId);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#deleteTicket(org.jasig.cas.ticket.Ticket)
     */
    public boolean deleteTicket(final Ticket ticket) {
    	return (ticket == null) ? false : ticketRegistry.deleteTicket(ticket.getId());
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#validateProxyGrantingTicket(org.jasig.cas.ticket.validation.ValidationRequest)
     */
    public ProxyGrantingTicket validateProxyGrantingTicket(final ValidationRequest validationRequest) {
        return (ProxyGrantingTicket) retrieveValidTicket(validationRequest, ProxyGrantingTicket.class);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#validateProxyTicket(org.jasig.cas.ticket.validation.ValidationRequest)
     */
    public ProxyTicket validateProxyTicket(ValidationRequest validationRequest) {
        return (ProxyTicket) retrieveValidTicket(validationRequest, ProxyTicket.class);

    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#validateServiceTicket(org.jasig.cas.ticket.validation.ValidationRequest)
     */
    public ServiceTicket validateServiceTicket(final ValidationRequest validationRequest) {
        return (ServiceTicket) retrieveValidTicket(validationRequest, ServiceTicket.class);
    }

    /**
     * 
     * @see org.jasig.cas.ticket.TicketManager#validateTicketGrantingTicket(org.jasig.cas.ticket.validation.ValidationRequest)
     */
    public TicketGrantingTicket validateTicketGrantingTicket(final ValidationRequest validationRequest) {
        return (TicketGrantingTicket) retrieveValidTicket(validationRequest, TicketGrantingTicket.class);
    }

    /**
     * Method to retrieve the ticket from the registry and call the validator. If the validation is a success, the
     * internal state is updated.
     * 
     * @param ticketId the id of the ticket we want to retrieve.
     * @param request the validation request parameters.
     * @param clazz the type of ticket we are expecting.
     * @return
     */
    private Ticket retrieveValidTicket(final String ticketId, final ValidationRequest request, final Class clazz) {
        final Ticket ticket = ticketRegistry.getTicket(ticketId, clazz);

        if (ticket == null)
            return null;

        synchronized (ticket) {
            if (ticketValidator.validate(ticket, request)) {
            	logger.debug("Successful validation.  Updating state for ticket [" + ticket.getId() + "]");
                AbstractTicket t = (AbstractTicket) ticket;
                t.incrementCount();
                t.updateLastUse();
                return this.immutableTicketProxyFactory.getProxyForTicket(ticket);
            }
        }
        return null;
    }

    private Ticket retrieveValidTicket(final ValidationRequest request, final Class clazz) {
        return retrieveValidTicket(request.getTicket(), request, clazz);
    }

    /**
     * @param ticketCache The ticketCache to set.
     */
    public void setTicketRegistry(final TicketRegistry ticketCache) {
        this.ticketRegistry = ticketCache;
    }

    /**
     * @param ticketFactory The ticketFactory to set.
     */
    public void setTicketFactory(final TicketFactory ticketFactory) {
        this.ticketFactory = ticketFactory;
    }

    /**
     * @param ticketValidator The ticketValidator to set.
     */
    public void setTicketValidator(final TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

    /**
     * @param immutableTicketProxyFactory The immutableTicketProxyFactory to set.
     */
    public void setImmutableTicketProxyFactory(final ImmutableTicketProxyFactory immutableTicketProxyFactory) {
        this.immutableTicketProxyFactory = immutableTicketProxyFactory;
    }

    private static class SpringAOPImmutableTicketProxyFactory implements ImmutableTicketProxyFactory {

        public Ticket getProxyForTicket(final Ticket ticketImpl) {
            if (ticketImpl == null) {
                throw new IllegalArgumentException("Cannot create a proxy for null object.");
            }
            //Create SpringAOP factory wrapping the target
            ProxyFactory pf = new ProxyFactory(ticketImpl);
            //Prevent the Proxy to be castable to Advised
            pf.setOpaque(true);
            return (Ticket)pf.getProxy();
        }
    }
}