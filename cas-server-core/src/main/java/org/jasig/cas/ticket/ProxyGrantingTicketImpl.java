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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;

/**
 * Concrete implementation of a proxy granting ticket (PGT). A PGT is
 * used by a service to obtain proxy tickets for obtaining access to a back-end
 * service on behalf of a client. It is analogous to a ticket-granting ticket
 * but only for proxying purposes. Proxy tickets will be issued off of
 * a given proxy granting ticket.
 * <p>
 * NOTE: A PGT shares the same implementation as a {@link TicketGrantingTicket}.
 * The intension of this implementation at this point is make explicit and visible
 * the use and declaration of a PGT per the CAS protocol.
 * 
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class ProxyGrantingTicketImpl extends TicketGrantingTicketImpl implements ProxyGrantingTicket {
    private static final long serialVersionUID = -8126909926138945649L;

    public ProxyGrantingTicketImpl(final String id, final Authentication authentication, final ExpirationPolicy policy) {
        super(id, authentication, policy);
    }

    public ProxyGrantingTicketImpl(final String id, final TicketGrantingTicket ticketGrantingTicket,
            final Authentication authentication, final ExpirationPolicy policy) {
        super(id, ticketGrantingTicket, authentication, policy);
    }

}
