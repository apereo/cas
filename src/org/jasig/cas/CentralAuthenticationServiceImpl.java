/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationSpecification;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.ImmutableTicketProxyFactory;
import org.jasig.cas.ticket.InternalTicketGrantingTicket;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.factory.TicketFactory;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Implemenation of a CentralAuthenticationService.
 * 
 * @author William G. Thompson, Jr.
 */
public final class CentralAuthenticationServiceImpl implements CentralAuthenticationSerivce {
    
    private final Log log = LogFactory.getLog(getClass());

    private TicketRegistry ticketRegistry;
    
    private TicketFactory ticketFactory;
    
    private ImmutableTicketProxyFactory immutableTicketProxyFactory = new SpringAOPImmutableTicketProxyFactory();
    
    private AuthenticationManager authenticationManager;

    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#destroyTicketGrantingTicket(java.lang.String)
     */
    public void destroyTicketGrantingTicket(String ticketGrantingTicketId) {
    	try {
    		this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
    	} catch (InvalidTicketException ite) {
    		throw new IllegalArgumentException("ticketGrantingTicketId must be the ID of a TicketGrantingTicket");
    	}
    	
    	this.ticketRegistry.deleteTicket(ticketGrantingTicketId);
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantServiceTicket(java.lang.String, org.jasig.cas.Service)
     */
    public ServiceTicket grantServiceTicket(String ticketGrantingTicketId, Service service) {
    	final InternalTicketGrantingTicket ticketGrantingTicket;
    	final ServiceTicket serviceTicket;
    	
    	ticketGrantingTicket = (InternalTicketGrantingTicket) ticketRegistry.getTicket(ticketGrantingTicketId, InternalTicketGrantingTicket.class);
    	serviceTicket = ticketGrantingTicket.grantServiceTicket(service);
    	ticketRegistry.addTicket(serviceTicket);
    	
    	return (ServiceTicket) immutableTicketProxyFactory.getProxyForTicket(serviceTicket);
    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantTicketGrantingTicket(java.lang.String, org.jasig.cas.authentication.principal.Credentials)
     */
    public TicketGrantingTicket delegateTicketGrantingTicket(String serviceTicketId, Credentials credentials) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#validateServiceTicket(java.lang.String, org.jasig.cas.Service, org.jasig.cas.authentication.AuthenticationSpecification)
     */
    public ServiceTicket validateServiceTicket(String serviceTicketId, Service service) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantTicketGrantingTicket(org.jasig.cas.authentication.principal.Principal)
     */
    public TicketGrantingTicket createTicketGrantingTicket(Credentials credentials) {
    	final Principal principal = authenticationManager.authenticateCredentials(credentials);
    	final TicketGrantingTicket ticketGrantingTicket;
    	
    	if (principal == null)
    		return null;
    	
    	ticketGrantingTicket = (TicketGrantingTicket) ticketFactory.getTicket(TicketGrantingTicket.class, principal, null, null);
    	
    	return (TicketGrantingTicket) this.immutableTicketProxyFactory.getProxyForTicket(ticketGrantingTicket);
    }

    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#getVersion()
     */
    public String getVersion() {
        // Fetches the "Implementation-Version" manifest attribute from the jar file.
        return CasVersion.class.getPackage().getImplementationVersion();
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
    
    private static class SpringAOPImmutableTicketProxyFactory implements ImmutableTicketProxyFactory {

        public Ticket getProxyForTicket(final Ticket ticketImpl) {
            if (ticketImpl == null) {
                throw new IllegalArgumentException("Cannot create a proxy for null object.");
            }
            // Create SpringAOP factory wrapping the target
            ProxyFactory pf = new ProxyFactory(ticketImpl);
            // Prevent the Proxy to be castable to Advised
            pf.setOpaque(true);
            return (Ticket)pf.getProxy();
        }
    }
}
