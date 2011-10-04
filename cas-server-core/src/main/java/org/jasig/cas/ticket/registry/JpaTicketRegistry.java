/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class JpaTicketRegistry extends AbstractDistributedTicketRegistry {
    
    @NotNull
    @PersistenceContext
    private EntityManager entityManager;
        
    @NotNull
    private String ticketGrantingTicketPrefix = "TGT";


    protected void updateTicket(final Ticket ticket) {
        entityManager.merge(ticket);
    }

    @Transactional(readOnly = false)
    public void addTicket(final Ticket ticket) {
        entityManager.persist(ticket);
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
        final List<TicketGrantingTicketImpl> ticketGrantingTicketImpls = entityManager
            .createQuery("select t from TicketGrantingTicketImpl t where t.ticketGrantingTicket.id = :id", TicketGrantingTicketImpl.class)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setParameter("id", ticket.getId())
            .getResultList();
        final List<ServiceTicketImpl> serviceTicketImpls = entityManager
	        .createQuery("select s from ServiceTicketImpl s where s.ticketGrantingTicket.id = :id", ServiceTicketImpl.class)
	        .setParameter("id", ticket.getId())
	        .getResultList();
        
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
            entityManager.remove(ticket);
        } catch (final Exception e) {
            log.error("Error removing " + ticket + " from registry.", e);
        }
    }
    
    @Transactional(readOnly=true)
    public Ticket getTicket(final String ticketId) {
        return getProxiedTicketInstance(getRawTicket(ticketId));
    }
    
    private Ticket getRawTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(this.ticketGrantingTicketPrefix)) {
                return entityManager.find(TicketGrantingTicketImpl.class, ticketId, LockModeType.PESSIMISTIC_WRITE);
            }
            
            return entityManager.find(ServiceTicketImpl.class, ticketId);
        } catch (final Exception e) {
            log.error("Error getting ticket " + ticketId + " from registry.", e);
        }
        return null;
    }

    @Transactional(readOnly=true)
    public Collection<Ticket> getTickets() {
        final List<TicketGrantingTicketImpl> tgts = entityManager
            .createQuery("select t from TicketGrantingTicketImpl t", TicketGrantingTicketImpl.class)
            .getResultList();
        final List<ServiceTicketImpl> sts = entityManager
            .createQuery("select s from ServiceTicketImpl s", ServiceTicketImpl.class)
            .getResultList();
        
        final List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.addAll(tgts);
        tickets.addAll(sts);
        
        return tickets;
    }
    
    public void setTicketGrantingTicketPrefix(final String ticketGrantingTicketPrefix) {
        this.ticketGrantingTicketPrefix = ticketGrantingTicketPrefix;
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }
}
