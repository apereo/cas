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

/**
 * Abstract Implementation that handles some of the commonalities between
 * distributed ticket registries.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractDistributedTicketRegistry extends
    AbstractTicketRegistry {

    protected abstract void updateTicket(final Ticket ticket);

    protected Ticket getProxiedTicketInstance(final Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        if (ticket instanceof ServiceTicket) {
            return new ProxiedServiceTicket((ServiceTicket) ticket, this);
        }

        return new ProxiedTicketGrantingTicket((TicketGrantingTicket) ticket,
            this);
    }

    private final class ProxiedServiceTicket implements ServiceTicket {

        /** Unique id for serialization */
        private static final long serialVersionUID = -8212338455270445676L;

        private final ServiceTicket serviceTicket;

        private AbstractDistributedTicketRegistry ticketRegistry;

        protected ProxiedServiceTicket(final ServiceTicket serviceTicket,
            final AbstractDistributedTicketRegistry ticketRegistry) {
            this.serviceTicket = serviceTicket;
            this.ticketRegistry = ticketRegistry;
        }

        public Service getService() {
            return this.serviceTicket.getService();
        }

        public TicketGrantingTicket grantTicketGrantingTicket(final String id,
            final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
            return this.serviceTicket.grantTicketGrantingTicket(id,
                authentication, expirationPolicy);
        }

        public boolean isFromNewLogin() {
            return this.serviceTicket.isFromNewLogin();
        }

        public long getCreationTime() {
            return this.serviceTicket.getCreationTime();
        }

        public TicketGrantingTicket getGrantingTicket() {
            return this.serviceTicket.getGrantingTicket();
        }

        public String getId() {
            return this.serviceTicket.getId();
        }

        public boolean isValidFor(final Service service) {
            final boolean result = this.serviceTicket.isValidFor(service);
            this.ticketRegistry.updateTicket(this.serviceTicket);

            return result;
        }

        public boolean isExpired() {
            return this.serviceTicket.isExpired();
        }

        public boolean equals(final Object obj) {
            return this.serviceTicket.equals(obj);
        }

        public String toString() {
            return this.serviceTicket.toString();
        }

        public int getCountOfUses() {
            return this.serviceTicket.getCountOfUses();
        }
    }

    private final class ProxiedTicketGrantingTicket implements
        TicketGrantingTicket {

        /** Unique Id for Serializaion */
        private static final long serialVersionUID = -4361481214176025025L;

        private final TicketGrantingTicket ticket;

        private final AbstractDistributedTicketRegistry ticketRegistry;

        protected ProxiedTicketGrantingTicket(
            final TicketGrantingTicket ticket,
            final AbstractDistributedTicketRegistry ticketRegistry) {
            this.ticket = ticket;
            this.ticketRegistry = ticketRegistry;
        }

        public long getCreationTime() {
            return this.ticket.getCreationTime();
        }

        public TicketGrantingTicket getGrantingTicket() {
            return this.ticket.getGrantingTicket();
        }

        public String getId() {
            return this.ticket.getId();
        }

        public boolean isExpired() {
            return this.ticket.isExpired();
        }

        public void expire() {
            this.ticket.expire();
            this.ticketRegistry.updateTicket(this.ticket);
        }

        public Authentication getAuthentication() {
            return this.ticket.getAuthentication();
        }

        public List<Authentication> getChainedAuthentications() {
            return this.ticket.getChainedAuthentications();
        }

        public ServiceTicket grantServiceTicket(final String id,
            final Service service, final ExpirationPolicy expirationPolicy,
            final boolean credentialsProvided) {
            final ServiceTicket serviceTicket = this.ticket.grantServiceTicket(
                id, service, expirationPolicy, credentialsProvided);
            this.ticketRegistry.updateTicket(this.ticket);
            return serviceTicket;
        }

        public boolean isRoot() {
            return this.ticket.isRoot();
        }

        public boolean equals(final Object obj) {
            return this.ticket.equals(obj);
        }

        public String toString() {
            return this.ticket.toString();
        }

        public int getCountOfUses() {
            return this.ticket.getCountOfUses();
        }
    }
}
