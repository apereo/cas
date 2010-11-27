/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Implementation that handles some of the commonalities between
 * distributed ticket registries.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractDistributedTicketRegistry extends AbstractTicketRegistry {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected abstract void updateTicket(final Ticket ticket);

    protected abstract boolean needsCallback();

    protected final Ticket getProxiedTicketInstance(final Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        if (ticket instanceof TicketGrantingTicket) {
            return new TicketGrantingTicketDelegator(this, (TicketGrantingTicket) ticket, needsCallback());
        }

        return new ServiceTicketDelegator(this, (ServiceTicket) ticket, needsCallback());
    }

    private static class TicketDelegator<T extends Ticket> implements Ticket {

        private final AbstractDistributedTicketRegistry ticketRegistry;

        private final T ticket;

        private final boolean callback;

        protected TicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry, final T ticket, final boolean callback) {
            this.ticketRegistry = ticketRegistry;
            this.ticket = ticket;
            this.callback = callback;
        }

        protected void updateTicket() {
            this.ticketRegistry.updateTicket(this.ticket);
        }

        protected T getTicket() {
            return this.ticket;
        }

        public final String getId() {
            return this.ticket.getId();
        }

        public final boolean isExpired() {
            if (!callback) {
                return this.ticket.isExpired();
            }

            final TicketGrantingTicket t = getGrantingTicket();

            return this.ticket.isExpired() || (t != null && t.isExpired());
        }

        public final TicketGrantingTicket getGrantingTicket() {
            final TicketGrantingTicket old = this.ticket.getGrantingTicket();

            if (old == null || !callback) {
                return old;
            }

            return (TicketGrantingTicket) this.ticketRegistry.getTicket(old.getId(), Ticket.class);
        }

        public final long getCreationTime() {
            return this.ticket.getCreationTime();
        }

        public final int getCountOfUses() {
            return this.ticket.getCountOfUses();
        }

        @Override
        public int hashCode() {
            return this.ticket.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            return this.ticket.equals(o);
        }
    }

    private static final class ServiceTicketDelegator extends TicketDelegator<ServiceTicket> implements ServiceTicket {

        protected ServiceTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry, final ServiceTicket serviceTicket, final boolean callback) {
            super(ticketRegistry, serviceTicket, callback);
        }


        public Service getService() {
            return getTicket().getService();
        }

        public boolean isFromNewLogin() {
            return getTicket().isFromNewLogin();
        }

        public boolean isValidFor(final Service service) {
            final boolean b = this.getTicket().isValidFor(service);
            updateTicket();
            return b;
        }

        public TicketGrantingTicket grantTicketGrantingTicket(final String id, final Authentication authentication, final ExpirationPolicy expirationPolicy) {
            final TicketGrantingTicket t = this.getTicket().grantTicketGrantingTicket(id, authentication, expirationPolicy);
            updateTicket();
            return t;
        }
    }

    private static final class TicketGrantingTicketDelegator extends TicketDelegator<TicketGrantingTicket> implements TicketGrantingTicket {

        protected TicketGrantingTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry, final TicketGrantingTicket ticketGrantingTicket, final boolean callback) {
            super(ticketRegistry, ticketGrantingTicket, callback);
        }

        public Authentication getAuthentication() {
            return getTicket().getAuthentication();
        }

        public ServiceTicket grantServiceTicket(final String id, final Service service, final ExpirationPolicy expirationPolicy, final boolean credentialsProvided) {
            final ServiceTicket t = this.getTicket().grantServiceTicket(id, service, expirationPolicy, credentialsProvided);
            updateTicket();
            return t;
        }

        public void expire() {
            this.getTicket().expire();
            updateTicket();
        }

        public boolean isRoot() {
            return getTicket().isRoot();
        }

        public List<Authentication> getChainedAuthentications() {
            return getTicket().getChainedAuthentications();
        }
    }
}
