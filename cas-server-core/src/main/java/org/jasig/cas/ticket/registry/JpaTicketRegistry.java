/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.inspektr.common.ioc.annotation.NotNull;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.springframework.orm.jpa.JpaTemplate;

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

    public boolean deleteTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        if (ticket != null) {
            this.jpaTemplate.remove(ticket);
            return true;
        }
        return false;
    }

    public Ticket getTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(this.ticketGrantingTicketPrefix)) {
                return this.jpaTemplate.find(TicketGrantingTicketImpl.class, ticketId);
            }
            
            return this.jpaTemplate.find(ServiceTicketImpl.class, ticketId);
        } catch (final Exception e) {
            log.error(e,e);
        }
        return null;
    }

    public Collection<Ticket> getTickets() {
        final List<TicketGrantingTicketImpl> tgts = this.jpaTemplate.find("from TicketGrantingTicketImpl");
        final List<ServiceTicketImpl> sts = this.jpaTemplate.find(" from ServiceTicketImpl");
        
        final List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.addAll(tgts);
        tickets.addAll(sts);
        
        return tickets;
    }
    
    public void setTicketGrantingTicketPrefix(final String ticketGrantingTicketPrefix) {
        this.ticketGrantingTicketPrefix = ticketGrantingTicketPrefix;
    }
}
