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

import java.util.List;
import java.util.Map;

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

 * @since 3.1
 */
public abstract class AbstractDistributedTicketRegistry extends AbstractTicketRegistry {

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

        private static final long serialVersionUID = 1780193477774123440L;

        private final AbstractDistributedTicketRegistry ticketRegistry;

        private final T ticket;

        private final boolean callback;

        protected TicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry,
                final T ticket, final boolean callback) {
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

            return this.ticketRegistry.getTicket(old.getId(), Ticket.class);
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

    private static final class ServiceTicketDelegator extends TicketDelegator<ServiceTicket>
                           implements ServiceTicket {

        private static final long serialVersionUID = 8160636219307822967L;

        protected ServiceTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry,
                final ServiceTicket serviceTicket, final boolean callback) {
            super(ticketRegistry, serviceTicket, callback);
        }

        @Override
        public Service getService() {
            return getTicket().getService();
        }

        @Override
        public boolean isFromNewLogin() {
            return getTicket().isFromNewLogin();
        }

        @Override
        public boolean isValidFor(final Service service) {
            final boolean b = this.getTicket().isValidFor(service);
            updateTicket();
            return b;
        }

        @Override
        public TicketGrantingTicket grantTicketGrantingTicket(final String id,
                final Authentication authentication, final ExpirationPolicy expirationPolicy) {
            final TicketGrantingTicket t = this.getTicket().grantTicketGrantingTicket(id,
                    authentication, expirationPolicy);
            updateTicket();
            return t;
        }
    }

    private static final class TicketGrantingTicketDelegator extends TicketDelegator<TicketGrantingTicket>
            implements TicketGrantingTicket {

        private static final long serialVersionUID = 5312560061970601497L;

        protected TicketGrantingTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry,
                final TicketGrantingTicket ticketGrantingTicket, final boolean callback) {
            super(ticketRegistry, ticketGrantingTicket, callback);
        }

        @Override
        public Authentication getAuthentication() {
            return getTicket().getAuthentication();
        }

        @Override
        public List<Authentication> getSupplementalAuthentications() {
            return getTicket().getSupplementalAuthentications();
        }

        @Override
        public ServiceTicket grantServiceTicket(final String id, final Service service,
                final ExpirationPolicy expirationPolicy, final boolean credentialsProvided) {
            final ServiceTicket t = this.getTicket().grantServiceTicket(id, service,
                    expirationPolicy, credentialsProvided);
            updateTicket();
            return t;
        }

        @Override
        public void markTicketExpired() {
            this.getTicket().markTicketExpired();
            updateTicket();
        }

        @Override
        public boolean isRoot() {
            return getTicket().isRoot();
        }

        @Override
        public TicketGrantingTicket getRoot() {
            return getTicket().getRoot();
        }

        @Override
        public List<Authentication> getChainedAuthentications() {
            return getTicket().getChainedAuthentications();
        }

        @Override
        public Map<String, Service> getServices() {
            return this.getTicket().getServices();
        }

        @Override
        public void removeAllServices() {
            this.getTicket().removeAllServices();
        }
    }
}
