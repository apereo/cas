/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.AuthenticationSpecification;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.ImmutableTicketProxyFactory;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
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
    
    private ImmutableTicketProxyFactory immutableTicketProxyFactory = new SpringAOPImmutableTicketProxyFactory();

    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#destroyTicketGrantingTicket(java.lang.String)
     */
    public void destroyTicketGrantingTicket(String ticketGrantingTicketId) {
        // TODO Auto-generated method stub

    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantServiceTicket(java.lang.String, org.jasig.cas.Service)
     */
    public ServiceTicket grantServiceTicket(String ticketGrantingTicketId,
            Service service) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantTicketGrantingTicket(java.lang.String, org.jasig.cas.authentication.principal.Credentials)
     */
    public TicketGrantingTicket grantTicketGrantingTicket(
            String serviceTicketId, Credentials credentials) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#validateServiceTicket(java.lang.String, org.jasig.cas.Service, org.jasig.cas.authentication.AuthenticationSpecification)
     */
    public ServiceTicket validateServiceTicket(String serviceTicketId,
            Service service, AuthenticationSpecification authspec) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantTicketGrantingTicket(org.jasig.cas.authentication.principal.Principal)
     */
    public TicketGrantingTicket createTicketGrantingTicket(Credentials credentials) {
        // authenticate credentials
        // resolve to Principal
        // create TGT
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantServiceTicket(org.jasig.cas.ticket.TicketGrantingTicket, org.jasig.cas.Service)
     */
    public ServiceTicket grantServiceTicket(final TicketGrantingTicket tgt, final Service service) {
        // TODO: do service white list check in TGT
        final ServiceTicket st = tgt.grantServiceTicket(service);
        this.ticketRegistry.addTicket(st);
        return st;
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
