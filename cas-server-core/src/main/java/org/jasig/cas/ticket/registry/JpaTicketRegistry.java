/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class JpaTicketRegistry extends AbstractDistributedTicketRegistry {
    
    @NotNull
    private JpaTemplate jpaTemplate;
        
    @NotNull
    private String ticketGrantingTicketPrefix = "TGT";
    
    public JpaTicketRegistry(final EntityManagerFactory factory) {
        this.jpaTemplate = new JpaTemplate(factory);
    }

    protected void updateTicket(final Ticket ticket) {
        this.jpaTemplate.merge(ticket);
    }

    public void addTicket(final Ticket ticket) {
        this.jpaTemplate.persist(ticket);
    }

    @Transactional(readOnly = false)
    public boolean deleteTicket(final String ticketId) {
        final Ticket ticket = getRawTicket(ticketId);
        
        if (ticket == null) {
            return false;
        }
        
        if (ticket instanceof ServiceTicket) {
            removeTicket(ticket);
            return true;
        }
        
        deleteTicketAndChildren(ticket);
        return true;        
    }
    
    private void deleteTicketAndChildren(final Ticket ticket) {
        final Map<String,Object> params = new HashMap<String,Object>();
        params.put("id", ticket.getId());
        final List<TicketGrantingTicketImpl> ticketGrantingTicketImpls = this.jpaTemplate.findByNamedParams("select t from TicketGrantingTicketImpl t where t.ticketGrantingTicket.id = :id", params);
        final List<ServiceTicketImpl> serviceTicketImpls = this.jpaTemplate.findByNamedParams("select s from ServiceTicketImpl s where s.ticketGrantingTicket.id = :id", params);
        
        for (final ServiceTicketImpl s : serviceTicketImpls) {
            removeTicket(s);
        }
        
        for (final TicketGrantingTicketImpl t : ticketGrantingTicketImpls) {
            deleteTicketAndChildren(t);
        }
        
        removeTicket(ticket);
    }
    
    private void removeTicket(final Ticket ticket) {
        try {
            if (log.isDebugEnabled()) {
                final Date creationDate = new Date(ticket.getCreationTime());
                log.debug("Removing Ticket >" + ticket.getId() + "< created: " + creationDate.toString());
             }
            this.jpaTemplate.remove(ticket);
        } catch (final Exception e) {
            log.error("Error removing " + ticket + " from registry.", e);
        }
    }
    
    public Ticket getTicket(final String ticketId) {
        return getProxiedTicketInstance(getRawTicket(ticketId));
    }
    
    private Ticket getRawTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(this.ticketGrantingTicketPrefix)) {
                return this.jpaTemplate.find(TicketGrantingTicketImpl.class, ticketId);
            }
            
            return this.jpaTemplate.find(ServiceTicketImpl.class, ticketId);
        } catch (final Exception e) {
            log.error("Error getting ticket " + ticketId + " from registry.", e);
        }
        return null;
    }

    @Transactional(readOnly=true)
    public Collection<Ticket> getTickets() {
        final List<TicketGrantingTicketImpl> tgts = this.jpaTemplate.find("select t from TicketGrantingTicketImpl t");
        final List<ServiceTicketImpl> sts = this.jpaTemplate.find("select s from ServiceTicketImpl s");
        
        final List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.addAll(tgts);
        tickets.addAll(sts);
        
        return tickets;
    }
    
    public void setTicketGrantingTicketPrefix(final String ticketGrantingTicketPrefix) {
        this.ticketGrantingTicketPrefix = ticketGrantingTicketPrefix;
    }
}
