/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.ImmutableTicketProxyFactory;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketCreationException;
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

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantProxyGrantingTicket(org.jasig.cas.ticket.ServiceTicket)
     */
    public ProxyGrantingTicket grantProxyGrantingTicket(final ServiceTicket st) {
        final ProxyGrantingTicket pgt = st.grantProxyGrantingTicket();
        this.ticketRegistry.addTicket(pgt);
        return pgt;
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantProxyGrantingTicket(org.jasig.cas.ticket.ProxyTicket)
     */
    public ProxyGrantingTicket grantProxyGrantingTicket(final ProxyTicket pt) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#grantProxyTicket(org.jasig.cas.ticket.ProxyGrantingTicket, org.jasig.cas.Service)
     */
    public ProxyTicket grantProxyTicket(ProxyGrantingTicket pgt, Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#validate(org.jasig.cas.ticket.Ticket, org.jasig.cas.Service)
     */
    public boolean validate(Ticket ticket, Service service) {
        // TODO Auto-generated method stub
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#lookupTicketGrantingTicketForId(java.lang.String)
     */
    public TicketGrantingTicket lookupTicketGrantingTicketForId(final String tgtid) {
        return (TicketGrantingTicket)lookupTicketForId(tgtid, org.jasig.cas.ticket.TicketGrantingTicket.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#lookupServiceTicketForId(java.lang.String)
     */
    public ServiceTicket lookupServiceTicketForId(final String stid) {
        return (ServiceTicket)lookupTicketForId(stid, org.jasig.cas.ticket.ServiceTicket.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#lookupProxyGrantingTicketForId(java.lang.String)
     */
    public ProxyGrantingTicket lookupProxyGrantingTicketForId(final String pgtid) {
        return (ProxyGrantingTicket)lookupTicketForId(pgtid, org.jasig.cas.ticket.ProxyGrantingTicket.class);
    }

    /* (non-Javadoc)
     * @see org.jasig.cas.CentralAuthenticationSerivce#lookupProxyTicketForId(java.lang.String)
     */
    public ProxyTicket lookupProxyTicketForId(final String ptid) {
        return (ProxyTicket)lookupTicketForId(ptid, org.jasig.cas.ticket.ProxyTicket.class);
    }
    
    private Ticket lookupImmutableTicketForId(final String ticketId, final Class clazz) {
        final Ticket ticket = lookupTicketForId(ticketId, clazz);
        return this.immutableTicketProxyFactory.getProxyForTicket(ticket);
    }

    private Ticket lookupTicketForId(final String ticketId, final Class clazz) {
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId);
        if (ticket == null) {
            throw new InvalidTicketException("No ticket found for TicketId: " + ticketId);
        } else if (!ticket.getClass().isAssignableFrom(clazz)) {
            throw new InvalidTicketException("Ticket [" + ticket.getId() + "] is of type "
                + ticket.getClass() + " when we were expecting " + clazz);
        } else {
            return ticket;
        }
    }

    /* (non-Javadoc)
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
