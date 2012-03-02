/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry;

import net.spy.memcached.MemcachedClient;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * Ticket registry implementation that serializes tickets and stores them by
 * ticket ID in a memcached store.
 * 
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.3
 *
 */
public final class MemcachedTicketRegistry extends AbstractDistributedTicketRegistry implements DisposableBean {
    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Memcached client instance. */
    @NotNull
	private final MemcachedClient client;

    /** TGT cache entry timeout in ms */
	@Min(0)
	private final int tgtTimeout;

    /** ST cache entry timeout in ms */
	@Min(0)
	private final int stTimeout;
	

	/**
     * Creates a new registry with the given parameters.
	 * 
	 * @param client Fully configured memcached client.
	 * @param ticketGrantingTicketTimeOut Cache entry timeout in ms for TicketGrantingTickets.
	 * @param serviceTicketTimeOut Cache entry timeout in ms for TicketGrantingTickets.
	 */
	public MemcachedTicketRegistry(final MemcachedClient client, final long ticketGrantingTicketTimeOut, final long serviceTicketTimeOut) {
		this.tgtTimeout = (int) ticketGrantingTicketTimeOut / 1000;
		this.stTimeout = (int) serviceTicketTimeOut / 1000;
        this.client = client;
	}

	protected void updateTicket(final Ticket ticket) {
        logger.debug("Updating ticket {}", ticket);
        try {
            if (!this.client.replace(ticket.getId(), getTimeout(ticket), ticket).get()) {
                logger.error("Failed updating {}", ticket);
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for response to async replace operation for ticket {}. " +
                    "Cannot determine whether update was successful.", ticket);
        } catch (Exception e) {
            logger.error("Failed updating {}", ticket, e);
        }
	}

	public void addTicket(final Ticket ticket) {
        logger.debug("Adding ticket {}", ticket);
        try {
            if (!this.client.add(ticket.getId(), getTimeout(ticket), ticket).get()) {
                logger.error("Failed adding {}", ticket);
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for response to async add operation for ticket {}. " +
                    "Cannot determine whether add was successful.", ticket);
        } catch (Exception e) {
            logger.error("Failed adding {}", ticket, e);
        }
	}

	public boolean deleteTicket(final String ticketId) {
        logger.debug("Deleting ticket {}", ticketId);
		try {
			return this.client.delete(ticketId).get();
		} catch (final Exception e) {
            log.error("Failed deleting {}", ticketId, e);
		}
        return false;
	}

	public Ticket getTicket(final String ticketId) {
        try {
            final Ticket t = (Ticket) this.client.get(ticketId);
            if (t != null) {
                return getProxiedTicketInstance(t);
            }
        } catch (Exception e) {
            log.error("Failed fetching {} ", ticketId, e);
        }
        return null;
	}

	/**
	 * This operation is not supported.
	 * 
	 * @throws UnsupportedOperationException if you try and call this operation.
	 */
	public Collection<Ticket> getTickets() {
		throw new UnsupportedOperationException("GetTickets not supported.");
	}

	public void destroy() throws Exception {
		this.client.shutdown();
	}

    @Override
    protected boolean needsCallback() {
        return true;
    }
    
    private int getTimeout(final Ticket t) {
        if (t instanceof TicketGrantingTicket) {
            return this.tgtTimeout;
        } else if (t instanceof ServiceTicket) {
            return this.stTimeout;
        }
        throw new IllegalArgumentException("Invalid ticket type");
    }
}
