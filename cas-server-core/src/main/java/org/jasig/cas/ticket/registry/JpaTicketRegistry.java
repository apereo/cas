/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        log.debug("Updated ticket [{}].", ticket);
    }

    @Transactional(readOnly = false)
    public void addTicket(final Ticket ticket) {
        entityManager.persist(ticket);
        log.debug("Added ticket [{}] to registry.", ticket);
    }

    @Transactional(readOnly = false)
    public boolean deleteTicket(final String ticketId) {
        final Ticket ticket = getRawTicket(ticketId);
        
        if (ticket == null) {
            return false;
        }
        
        if (ticket instanceof ServiceTicket) {
            removeTicket(ticket);
            log.debug("Deleted ticket [{}] from the registry.", ticket);
            return true;
        }
        
        deleteTicketAndChildren(ticket);
        log.debug("Deleted ticket [{}] and its children from the registry.", ticket);
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
                log.debug("Removing Ticket [{}] created: {}", ticket, creationDate.toString());
             }
            entityManager.remove(ticket);
        } catch (final Exception e) {
            log.error("Error removing {} from registry.", ticket, e);
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
            log.error("Error getting ticket {} from registry.", ticketId, e);
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

    @Transactional(readOnly=true)
    public int sessionCount() {
        return countToInt(entityManager.createQuery("select count(t) from TicketGrantingTicketImpl t").getSingleResult());
    }

    @Transactional(readOnly=true)
    public int serviceTicketCount() {
        return countToInt(entityManager.createQuery("select count(t) from ServiceTicketImpl t").getSingleResult());
    }

    private int countToInt(final Object result) {
        final int intval;
        if (result instanceof Long) {
            intval = ((Long) result).intValue();
        } else if (result instanceof Integer) {
            intval = (Integer) result;
        } else {
            // Must be a Number of some kind
            intval = ((Number) result).intValue();
        }
        return intval;
    }
}
